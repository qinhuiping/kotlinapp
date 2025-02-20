package com.qhping.kotlin.app.ui.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HomeViewModel(
    private val application: Application, private val homeRepository: HomeRepository
) : AndroidViewModel(application) {

    private val TAG = "HomeViewModel"
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    val previewUseCase = MutableLiveData<PreviewView>()
    val imageCaptureUseCase = MutableLiveData<ImageCapture>()
    val videoCaptureUseCapture = MutableLiveData<VideoCapture<Recorder>>()


    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        context: Context,
        surfaceProvider: Preview.SurfaceProvider
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
            val recorder =
                Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
            videoCapture = VideoCapture.withOutput(recorder)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture, videoCapture
                )
                previewUseCase.value?.let { previewView ->
                    preview?.setSurfaceProvider { previewView.surfaceProvider }
                }
            } catch (e: Exception) {
                Log.e(TAG, "startCamera: $e")
            }
        }, ContextCompat.getMainExecutor(application.applicationContext))
    }
}