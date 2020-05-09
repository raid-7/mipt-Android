package ru.raid.miptandroid

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SyncLoadFragment : CameraFragment() {
    private lateinit var detector: BarcodeDetector

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setChipText(R.string.camera_qr_explanation)
        setActionBarTransparency(false)
        lightMode = LightMode.TORCH
    }

    override fun getCameraUseCases(): List<UseCase> {
        return super.getCameraUseCases() + makeDetector()
    }

    private fun runResultProcessor(): SendChannel<String> {
        val ch = Channel<String>()
        viewLifecycleOwner.lifecycleScope.launch {
            for (value in ch) {
                if (processResult(value))
                    break
            }
        }
        return ch
    }

    private suspend fun processResult(value: String): Boolean {
        detector.detectionEnabled = false
        if (tryLoadNote(value))
            return true

        detector.detectionEnabled = true
        return false
    }

    private suspend fun tryLoadNote(id: String): Boolean = withContext(Dispatchers.IO) {
        val service = RetrofitServices.getInstance(requireContext()).syncService
        val data: NoteData
        val imagePath: Uri
        try {
            data = service.fetchNoteData(id)
            val image = service.fetchNoteImage(id)
            imagePath = saveImage(image)
        } catch (exc: Exception) {
            if (!(exc is HttpException && exc.code() == HTTP_NOT_FOUND)) {
                launch(Dispatchers.Main) {
                    processFailure(exc)
                }
            }
            return@withContext false
        }
        welcomeNote(data, imagePath, id)
        true
    }

    private fun saveImage(body: ResponseBody): Uri {
        val file = nextRandomFile(requireContext())
        BufferedOutputStream(FileOutputStream(file)).use { out ->
            body.byteStream().use { inp ->
                val buf = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = inp.read(buf, 0, buf.size)
                    if (read == 0) {
                        break
                    }
                    out.write(buf, 0, read)
                }
            }
        }
        return Uri.fromFile(file)
    }

    private fun welcomeNote(data: NoteData, image: Uri, id: String) {
        val mainActivity = activity as? MainActivity
        mainActivity?.noteFlows?.addReadyNote(data, image, id)
        mainActivity?.popFragment()
    }

    private fun runFailureProcessor(): SendChannel<Exception> {
        val ch = Channel<Exception>()
        viewLifecycleOwner.lifecycleScope.launch {
            for (exc in ch) {
                processFailure(exc)
            }
        }
        return ch
    }

    // There're may be races but ok
    private suspend fun processFailure(exc: Exception) = withContext(Dispatchers.Main) {
        chipText = exc.message ?: "Unknown failure"
        delay(1000)
        setChipText(R.string.camera_qr_explanation)
    }

    private fun makeDetector(): ImageAnalysis {
        detector = BarcodeDetector(this)
        detector.setFailureConsumer(runFailureProcessor())
        detector.setResultConsumer(runResultProcessor())
        return detector.toUseCase(executor)
    }
}
