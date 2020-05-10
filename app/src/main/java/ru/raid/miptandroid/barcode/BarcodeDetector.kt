package ru.raid.miptandroid.barcode

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class BarcodeDetector(private val lifecycleOwner: LifecycleOwner) : ImageAnalysis.Analyzer {
    private var resultChannel: SendChannel<String>? = null
    private var failureChannel: SendChannel<Exception>? = null

    @Volatile
    var detectionEnabled: Boolean = true

    private val detector = FirebaseVision.getInstance()
        .getVisionBarcodeDetector(
            FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build()
        )

    private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    @OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        Log.d("BarcodeDetector", "detectionEnabled=${detectionEnabled}")
        imageProxy.use { proxy ->
            if (!detectionEnabled)
                return

            val imageRotation = degreesToFirebaseRotation(proxy.imageInfo.rotationDegrees)
            proxy.image?.let {
                try {
                    val fbImage = FirebaseVisionImage.fromMediaImage(it, imageRotation)
                    process(fbImage, proxy.width, proxy.height)
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }
        }
    }

    fun setResultConsumer(ch: SendChannel<String>) {
        resultChannel = ch
    }

    fun setFailureConsumer(ch: SendChannel<Exception>) {
        failureChannel = ch
    }

    fun toUseCase(executor: Executor): ImageAnalysis {
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        analysis.setAnalyzer(executor, this)
        return analysis
    }

    private fun process(
        image: FirebaseVisionImage,
        width: Int, height: Int
    ) {
        Log.d("BarcodeDetector", "Processing")
        detector.detectInImage(image)
            .addOnSuccessListener {
                val bc = it.firstOrNull { barcode -> isInCenter(barcode, width, height) }
                bc?.rawValue?.let { value ->
                    lifecycleOwner.lifecycle.coroutineScope.launch {
                        Log.d("BarcodeDetector", "Result sent")
                        resultChannel?.send(value)
                    }
                }
            }
            .addOnFailureListener { exc ->
                lifecycleOwner.lifecycle.coroutineScope.launch {
                    failureChannel?.send(exc)
                }
            }
    }

    private fun isInCenter(barcode: FirebaseVisionBarcode, width: Int, height: Int) =
        barcode.boundingBox
            ?.contains(width / 2, height / 2)
            ?: false
}