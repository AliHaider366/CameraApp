package com.example.cameraapptestcode

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraCharacteristics.*
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class CameraHelper {

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var cameraDevice: CameraDevice
    private lateinit var capReq: CaptureRequest.Builder
    private lateinit var imageReader: ImageReader
    private lateinit var cameraCharacteristics: CameraCharacteristics
    private lateinit var streamConfigMap: StreamConfigurationMap
    private lateinit var outputSizes: Array<Size>
    private var flag: Int = 0
    private var zoomValue = 1f
    private lateinit var surface: Surface

    //Pass Aspect Ratio Here
    private var targetAspectRatio = Rational(1, 1)
    private var currentCameraId: String = "0"
    private var sensorOrientation: Int? = null
    private var DSI_width: Int? = null
    private var DSI_height: Int? = null
    private lateinit var windowManager: WindowManager

//    private lateinit var handler: Handler
//    private lateinit var handlerThread: HandlerThread


    fun zoomIn(textureView: TextureView) {
        flag = 1
        if (zoomValue < 3f) {
            zoomValue = (zoomValue + 0.5).toFloat()
            openCamera(textureView)
        }
        if (zoomValue == 3f || zoomValue > 3f) {
            Toast.makeText(textureView.context, "Maximum Zoomed In", Toast.LENGTH_SHORT).show()
        }
    }

    fun zoomOut(textureView: TextureView) {
        flag = 2
        if (zoomValue > 1f) {
            zoomValue = (zoomValue - 0.5).toFloat()
            openCamera(textureView)
        }
        if (zoomValue == 1f || zoomValue < 1f) {
            Toast.makeText(textureView.context, "Maximum Zoomed Out", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    fun openCamera(textureView: TextureView) {

//        handlerThread = HandlerThread("captureThread")
//        handlerThread.start()
//        handler = Handler(handlerThread.looper)

        cameraManager =
            textureView.context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.openCamera(currentCameraId, object :
            CameraDevice.StateCallback() {
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0
                if (flag == 0) {
                    zoomValue = 1f
                    cameraCharacteristics = cameraManager.getCameraCharacteristics(currentCameraId)
                    streamConfigMap =
                        cameraCharacteristics.get(SCALER_STREAM_CONFIGURATION_MAP)!!
                    outputSizes = streamConfigMap.getOutputSizes(SurfaceTexture::class.java)
                    sensorOrientation =
                        cameraCharacteristics.get(SENSOR_ORIENTATION)
                    windowManager =
                        textureView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                    surface = Surface(textureView.surfaceTexture)
                    capReq.addTarget(surface)

                    val selectedSize: Size? = aspectRatio(targetAspectRatio)
                    //val cropRegion = selectedSize?.let { Rect(0, 0, it.width, it.height) }
                    //capReq.set(CaptureRequest.SCALER_CROP_REGION, cropRegion)

                    //For setting zoom request
                    Zoom(cameraCharacteristics).setZoom(capReq, zoomValue)

                    //Setting up aspect ratio
                    val displayMetrics = DisplayMetrics()
                    windowManager.defaultDisplay.getMetrics(displayMetrics)
                    DSI_height = displayMetrics.heightPixels
                    DSI_width = displayMetrics.widthPixels
                    setAspectRatioTextureView(
                        textureView,
                        selectedSize!!.height,
                        selectedSize.width
                    )

                    imageReader = ImageReader.newInstance(
                        selectedSize.width,
                        selectedSize.height,
                        ImageFormat.JPEG,
                        1
                    )

                }

                //Checking for zoom level
                if (flag == 1) {
                    Zoom(cameraCharacteristics).setZoom(capReq, zoomValue)
                    flag = 0
                }
                if (flag == 2) {
                    Zoom(cameraCharacteristics).setZoom(capReq, zoomValue)
                    flag = 0
                }

                cameraDevice.createCaptureSession(
                    listOf(surface, imageReader.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(p0: CameraCaptureSession) {
                            cameraCaptureSession = p0
                            cameraCaptureSession.setRepeatingRequest(capReq.build(), null, null)
                        }

                        override fun onConfigureFailed(p0: CameraCaptureSession) {

                        }

                    },
                    null
                )
            }

            override fun onDisconnected(p0: CameraDevice) {

            }

            override fun onError(p0: CameraDevice, p1: Int) {

            }

        }, null)
    }

    fun setAspectRatioBySpinner(ratio: String, textureView: TextureView) {
        if (ratio == "1:1") {
            targetAspectRatio = Rational(1, 1)
            openCamera(textureView)
        }
        if (ratio == "4:3") {
            targetAspectRatio = Rational(4, 3)
            openCamera(textureView)
        }
        if (ratio == "16:9") {
            targetAspectRatio = Rational(16, 9)
            openCamera(textureView)
        }
    }


    private fun setAspectRatioTextureView(
        textureView: TextureView,
        ResolutionWidth: Int,
        ResolutionHeight: Int
    ) {
        if (ResolutionWidth > ResolutionHeight) {
            val newWidth: Int = DSI_width!!
            val newHeight: Int = DSI_width!! * ResolutionWidth / ResolutionHeight
            updateTextureViewSize(textureView, newWidth, newHeight)
        } else {
            val newWidth: Int = DSI_width!!
            val newHeight: Int = DSI_width!! * ResolutionHeight / ResolutionWidth
            updateTextureViewSize(textureView, newWidth, newHeight)
        }
    }

    private fun updateTextureViewSize(textureView: TextureView, viewWidth: Int, viewHeight: Int) {
        textureView.layoutParams = LinearLayout.LayoutParams(viewWidth, viewHeight)
    }

    fun aspectRatio(targetAspectRatio: Rational): Size? {
        var selectedSize: Size? = null

        for (size in outputSizes) {
            val aspectRatio = Rational(size.width, size.height)
            if (aspectRatio == targetAspectRatio) {
                selectedSize = size
                break
            }
        }

        return selectedSize
    }

    fun imageCapRequest() {
        capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        capReq.addTarget(imageReader.surface)
        cameraCaptureSession.capture(capReq.build(), null, null)
    }

    fun takePicture(imageView: ImageView) {
        imageReader.setOnImageAvailableListener({

            //Getting image and converting to buffer
            val image = it.acquireLatestImage()
            val rotation = windowManager.defaultDisplay.rotation
            val imageRotation = getImageRotation(rotation, sensorOrientation!!)
            val rotatedBitmap = rotateImage(image, imageRotation)
//            var buffer = image.planes[0].buffer
//            var bytes = ByteArray(buffer.remaining())
//            buffer.get(bytes)
            val byteArray: ByteArray = bitmapToByteArray(rotatedBitmap)

            //for image path
            val folderName = "Mycamera"
            val dcimDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val newFolder = File(dcimDirectory, folderName)
            if (!newFolder.exists()) {
                newFolder.mkdirs()
            }

            //For creating image file
            val file = File(newFolder, "IMG_${System.currentTimeMillis()}.jpg")
            val opStream = FileOutputStream(file)
            opStream.write(byteArray)

            //For adding captured picture to media database
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            imageView.context.sendBroadcast(mediaScanIntent)

            imageView.setImageBitmap(rotatedBitmap)
            opStream.close()
            image.close()
        }, null)


    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun getImageRotation(rotation: Int, sensorOrientation: Int): Int {
        val displayRotation = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        return if (sensorOrientation >= 0) (sensorOrientation - displayRotation + 360) % 360 else (sensorOrientation + displayRotation) % 360
    }

    private fun rotateImage(image: Image, rotation: Int): Bitmap {
        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val bitmap = decodeSampledBitmap(data, DSI_width!!, DSI_height!!)

        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun decodeSampledBitmap(
        data: ByteArray,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(data, 0, data.size, this)

            // Calculate inSampleSize
            inSampleSize = calculateSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeByteArray(data, 0, data.size, this)
        }
    }

    private fun calculateSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }


    private fun getCameraId(lensFacing: Int): String {
        val cameraIds = cameraManager.cameraIdList
        for (cameraId in cameraIds) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val cameraDirection = characteristics.get(LENS_FACING)
            //Log.d("CheckMyLog : ", "Array : $cameraId Dire: $cameraDirection Lens : $lensFacing")
            if (cameraDirection == lensFacing) {
                return cameraDirection.toString()
            }
        }
        return cameraIds[0]
    }

    fun switchCamera(textureView: TextureView) {
        val lensFacing =
            if (currentCameraId == getCameraId(LENS_FACING_FRONT)) {
                LENS_FACING_BACK
            } else {
                LENS_FACING_FRONT
            }
        currentCameraId = lensFacing.toString()
        cameraDevice.close()
        openCamera(textureView)
    }

    fun getImagesFromFolder(folderPath: String): List<String> {
        val folder = File(folderPath)
        val imageExtensions = listOf(".jpg") // Add more extensions if needed
        val images = mutableListOf<String>()

        folder.listFiles()?.forEach { file ->
            if (file.isFile && imageExtensions.any { file.name.toLowerCase().endsWith(it) }) {
                images.add(file.absolutePath)
            }
        }

        return images
    }

}