package com.qhping.kotlin.app.ui.home

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HomeViewModel(
    private val application: Application, private val homeRepository: HomeRepository
) : AndroidViewModel(application) {

    data class AnalysisResult(
        val image: Bitmap? = null,
        val metadata: ImageProxy.PlaneProxy? = null,
        val timestamp: Long = 0
    )

    private val TAG = "HomeViewModel"
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val _analysis = MutableLiveData<AnalysisResult>()
    private val frameAnalysisResult = MutableLiveData<ImageProxy>()

    val previewUseCase = MutableLiveData<PreviewView>()
    val imageCaptureUseCase = MutableLiveData<ImageCapture>()
    val videoCaptureUseCapture = MutableLiveData<VideoCapture<Recorder>>()

    private val analysisScope = CoroutineScope(Dispatchers.Default)
    private fun createImageAnalysis() {
        imageAnalysis =
            ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
                .also { analysis ->
                    analysis.setAnalyzer(executor) { imageProxy ->
                        analysisScope.launch {
                            //耗时操作
                            processImage(imageProxy)
                            imageProxy.close()
                        }
                    }
                }
    }

    private val executor = Executors.newSingleThreadExecutor()

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        try {
            val image = imageProxy.image ?: return
            if (image.format != ImageFormat.YUV_420_888) {
                Log.e(TAG, "processImage: unsupported image format:${image.format}")
                return
            }
            val width = image.width
            val height = image.height
            val yBuffer = image.planes[0].buffer
            val uBuffer = image.planes[1].buffer
            val vBuffer = image.planes[2].buffer
            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()
            val nv21 = ByteArray(width * height * 3 / 2)
            yBuffer.get(nv21, 0, ySize)
            var uvSize = width * height / 4
            var pos = ySize
            val vPixelStride = image.planes[2].pixelStride
            val vRowStride = image.planes[2].rowStride
            val vOffset = vRowStride - width / 2 + vPixelStride - 1
            for (i in 0 until uvSize) {
                if ((i / (width / 2) == 0) && i > 0) {
                    pos += vOffset
                }
                nv21[pos] = vBuffer.get()
                pos += 2
            }
            pos = ySize + 1
            val uPixelStride = image.planes[1].pixelStride
            val uRowStride = image.planes[1].rowStride
            val uOffset = uRowStride - width / 2 + uPixelStride - 1
            for (i in 0 until uvSize) {
                if (i % (width / 2) == 0 && i > 0) {
                    pos += uOffset
                }
                nv21[pos] = uBuffer.get()
                pos += 2
            }
        } catch (e: Exception) {
            Log.e(TAG, "processImage: ${e.message}}")
        }

    }

    fun startCamera(
        lifecycleOwner: LifecycleOwner, context: Context, surfaceProvider: Preview.SurfaceProvider
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(surfaceProvider)
            }
            imageCapture =
                ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
            val recorder =
                Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
            videoCapture = VideoCapture.withOutput(recorder)
            createImageAnalysis()
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

    fun takePicture() {
        val imageCapture = imageCapture ?: return
        val outputFile = recreateImageFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(application.applicationContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "IMAGE saved :${outputFile.absoluteFile}")
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d(TAG, "IMAGE error :${exception.message}")
                }
            })
    }


    private fun recreateImageFile(): File {
        val context = application.applicationContext
        val mediaDir = context.externalCacheDirs.firstOrNull()?.let {
            File(it, "CameraXApp").apply { mkdirs() }
        }
        return File.createTempFile(
            "IMG_${System.currentTimeMillis()}", ".jpg", mediaDir ?: context.filesDir
        )

    }
}