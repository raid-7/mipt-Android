package ru.raid.miptandroid

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TextRecognizer(private val context: Context) {
    suspend fun recognizeText(imageUri: Uri): String =
        withContext(Dispatchers.IO) {
            try {
                val image = FirebaseVisionImage.fromFilePath(context, imageUri)
                val recognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
                val result = recognizer.process(image)
                result.text
            } catch (exc: Exception) {
                exc.printStackTrace()
                launch(Dispatchers.Main) {
                    Toast.makeText(context, R.string.recognition_error, Toast.LENGTH_SHORT).show()
                }
                ""
            }
        }

    companion object {
        private suspend fun FirebaseVisionTextRecognizer.process(image: FirebaseVisionImage) =
            suspendCoroutine<FirebaseVisionText> { continuation ->
                processImage(image).addOnSuccessListener {
                    continuation.resume(it)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
            }
    }
}
