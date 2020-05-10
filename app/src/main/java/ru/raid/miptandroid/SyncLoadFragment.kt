package ru.raid.miptandroid

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.overlay_sync_load.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import okhttp3.ResponseBody
import java.io.BufferedOutputStream
import java.io.FileOutputStream

class SyncLoadFragment : CameraFragment() {
    private lateinit var detector: BarcodeDetector

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val topContainer = super.onCreateView(inflater, container, savedInstanceState) as ViewGroup
        inflater.inflate(R.layout.overlay_sync_load, topContainer, true)
        return topContainer
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setChipText(R.string.camera_qr_explanation)
        lightMode = LightMode.TORCH
        setActionBarTransparency(false)
        showReticleOverlay()
        runReticleUpdater()
    }

    private fun runReticleUpdater() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(20)
                reticleOverlay.invalidate()
            }
        }
    }

    override fun getCameraUseCases(): List<UseCase> {
        return super.getCameraUseCases() + makeDetector()
    }

    private fun showReticleOverlay() {
        reticleOverlay.visibility = View.VISIBLE
        progressOverlay.visibility = View.GONE
        setActionBarVisibility(true)
    }

    private fun showProgressOverlay() {
        reticleOverlay.visibility = View.GONE
        progressOverlay.visibility = View.VISIBLE
        setActionBarVisibility(false)
    }

    private fun runResultProcessor(): SendChannel<String> {
        val ch = Channel<String>(Channel.CONFLATED) // the best kotlin feature ever
        viewLifecycleOwner.lifecycleScope.launch {
            for (value in ch) {
                Log.d("SyncLoad", "Got value: ${value}")
                if (processResult(value))
                    break
            }
        }
        return ch
    }

    private suspend fun processResult(value: String): Boolean {
        detector.detectionEnabled = false
        withContext(Dispatchers.Main) {
            showProgressOverlay()
        }

        if (tryLoadNote(value))
            return true

        withContext(Dispatchers.Main) {
            showReticleOverlay()
        }
        delay(600)
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
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                processFailure(exc)
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
                    if (read < 0) {
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
        val ch = Channel<Exception>(Channel.CONFLATED)
        viewLifecycleOwner.lifecycleScope.launch {
            for (exc in ch) {
                processFailure(exc)
            }
        }
        return ch
    }

    // There're may be races but ok
    private suspend fun processFailure(exc: Exception) = withContext(Dispatchers.Main) {
        Log.e("SyncLoad", "Failure", exc)
        chipText = exc.message ?: "Unknown failure"
        delay(4000)
        setChipText(R.string.camera_qr_explanation)
    }

    private fun makeDetector(): ImageAnalysis {
        detector = BarcodeDetector(this)
        detector.setFailureConsumer(runFailureProcessor())
        detector.setResultConsumer(runResultProcessor())
        return detector.toUseCase(executor)
    }
}
