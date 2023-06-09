package com.example.cameraapptestcode

import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import androidx.core.math.MathUtils


class Zoom(characteristics: CameraCharacteristics) {

    private val mCropRegion = Rect()

    private var maxZoom: Float
    private val mSensorSize: Rect?
    private var hasSupport: Boolean

    companion object {
            private const val DEFAULT_ZOOM_FACTOR = 1.0f
    }

    init {
        mSensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)

        if (mSensorSize == null) {
            maxZoom = DEFAULT_ZOOM_FACTOR
            hasSupport = false
        }
        else {
            val value = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)

            maxZoom = if (value == null || value < DEFAULT_ZOOM_FACTOR) {
                DEFAULT_ZOOM_FACTOR
            } else {
                value
            }

            hasSupport = maxZoom > DEFAULT_ZOOM_FACTOR
        }
    }

    fun setZoom(builder: CaptureRequest.Builder, zoom: Float) {
        if (!hasSupport) {
            return
        }

        val newZoom = MathUtils.clamp(zoom, DEFAULT_ZOOM_FACTOR, maxZoom)

        val centerX = mSensorSize!!.width() / 2
        val centerY = mSensorSize.height() / 2
        val deltaX = (0.5f * mSensorSize.width() / newZoom).toInt()
        val deltaY = (0.5f * mSensorSize.height() / newZoom).toInt()

        mCropRegion.set(
            centerX - deltaX,
            centerY - deltaY,
            centerX + deltaX,
            centerY + deltaY
        )

        builder.set(CaptureRequest.SCALER_CROP_REGION, mCropRegion)
    }

}