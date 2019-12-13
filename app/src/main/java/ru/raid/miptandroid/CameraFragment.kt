package ru.raid.miptandroid


import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.view.CameraView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_camera.cameraView
import java.io.File
import java.util.UUID

class CameraFragment : Fragment() {
    private val imageCaptureListener = ImageCaptureListener()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraView.captureMode = CameraView.CaptureMode.IMAGE
        cameraView.bindToLifecycle(this)
        cameraView.setOnClickListener { takePicture() }

        Toast.makeText(context, R.string.camera_tap_explanation, Toast.LENGTH_SHORT).show()
    }

    private fun takePicture() {
        cameraView.takePicture(nextRandomFile(requireContext()), AsyncTask.SERIAL_EXECUTOR, imageCaptureListener)
    }

    private inner class ImageCaptureListener : ImageCapture.OnImageSavedListener {
        override fun onImageSaved(file: File) {
            val activity = activity as? MainActivity
            activity?.let {
                it.noteFlows.addNewNote(file)
                it.popFragment()
            }
        }

        override fun onError(imageCaptureError: ImageCapture.ImageCaptureError, message: String, cause: Throwable?) {
            Log.e("ImageCapture", message)
            context?.let {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }

    }
}

private fun nextRandomFile(context: Context) =
    File(context.filesDir, UUID.randomUUID().toString())
