package ru.raid.miptandroid

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.UseCase
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.util.*

class NoteCaptureFragment : CameraFragment() {
    private lateinit var capture: ImageCapture

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraView.setOnClickListener { takePicture() }

        setChipText(R.string.camera_tap_explanation)
        setActionBarTransparency(true)
        lightMode = LightMode.FLASH
    }

    override fun getCameraUseCases(): List<UseCase> {
        return super.getCameraUseCases() + makeCapture()
    }

    private fun makeCapture(): ImageCapture {
        capture = ImageCapture.Builder().build()
        return capture
    }

    private fun takePicture() {
        val file = nextRandomFile(requireContext())
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        capture.takePicture(options, executor, CaptureListener(file))
    }

    private inner class CaptureListener(file: File) : ImageCapture.OnImageSavedCallback {
        private val uri = Uri.fromFile(file)

        override fun onImageSaved(imageOutput: ImageCapture.OutputFileResults) {
            val activity = activity as? MainActivity
            activity?.let {
                it.noteFlows.addNewNote(uri)
                it.popFragment()
            }
        }

        override fun onError(exc: ImageCaptureException) {
            val message = exc.message ?: return
            Log.e("ImageCapture", message)
            context?.let {
                chipText = message
            }
        }
    }
}

fun nextRandomFile(context: Context) =
    File(context.filesDir, UUID.randomUUID().toString())
