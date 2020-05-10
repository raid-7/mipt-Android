package ru.raid.miptandroid


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.camera_action_bar.*
import kotlinx.android.synthetic.main.fragment_camera.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class CameraFragment : Fragment() {
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var capture: ImageCapture? = null

    protected lateinit var executor: ExecutorService

    protected var lightMode: LightMode = LightMode.TORCH
        set(value) {
            field = value
            manageLight()
        }

    protected var lightEnabled: Boolean = false
        set(value) {
            field = value
            flashButton.isSelected = value
            manageLight()
        }

    protected var chipText: String
        get() = cameraChip.text.toString()
        set(value) {
            cameraChip.text = value
        }

    protected fun setChipText(resourceId: Int) {
        cameraChip.setText(resourceId)
    }

    protected fun setActionBarTransparency(transparent: Boolean) {
        val res = if (transparent) 0 else R.drawable.top_action_bar_scrim
        cameraActionBar.setBackgroundResource(res)
    }

    protected fun setActionBarVisibility(visible: Boolean) {
        cameraActionBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    protected open fun getCameraUseCases(): List<UseCase> {
        return listOf(makePreview())
    }

    private fun makePreview(): Preview {
        return Preview.Builder().build()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        executor = Executors.newSingleThreadExecutor()
        startCamera()

        closeButton.setOnClickListener {
            val mainActivity = activity as? MainActivity
            mainActivity?.popFragment()
        }

        flashButton.setOnClickListener {
            lightEnabled = !lightEnabled
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        executor.shutdown()
    }

    private fun startCamera() {
        val camProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        camProviderFuture.addListener(Runnable {
            val provider = camProviderFuture.get()
            initCamera(provider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    protected open fun onCameraReady(cameraProvider: ProcessCameraProvider, camera: Camera) {
        preview?.setSurfaceProvider(cameraView.createSurfaceProvider(camera.cameraInfo))
        if (!camera.cameraInfo.hasFlashUnit()) {
            flashButton.visibility = View.INVISIBLE
        }
    }

    private fun initCamera(cameraProvider: ProcessCameraProvider) {
        if (!isAdded)
            return

        val selector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProvider.unbindAll()
        val useCases = getCameraUseCases()
        processUseCases(useCases)
        try {
            camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, selector, *useCases.toTypedArray())
        } catch (exc: Exception) {
            exc.printStackTrace()
            val mainActivity = activity as? MainActivity
            mainActivity?.let {
                Toast.makeText(it, exc.message, Toast.LENGTH_SHORT)
                it.popFragment()
            }
            return
        }
        onCameraReady(cameraProvider, camera!!)
        manageLight()
    }

    private fun processUseCases(useCases: List<UseCase>) {
        for (case in useCases) {
            when (case) {
                is ImageCapture -> capture = case
                is Preview -> preview = case
            }
        }
    }

    private fun manageLight() {
        val controls = camera?.cameraControl ?: return

        if (lightMode == LightMode.TORCH || !lightEnabled) {
            capture?.flashMode = ImageCapture.FLASH_MODE_OFF
        }
        if (!lightEnabled) {
            controls.enableTorch(false)
        }

        if (lightEnabled) {
            if (lightMode == LightMode.TORCH) {
                controls.enableTorch(true)
            } else {
                capture?.flashMode = ImageCapture.FLASH_MODE_ON
            }
        }
    }

    enum class LightMode {
        TORCH, FLASH
    }
}
