package com.example.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import java.io.ByteArrayOutputStream

object ScreenCaptureManager {

    private const val TAG = "ScreenCaptureManager"
    private var mediaProjectionPermissionResultData: Intent? = null
    private var mediaProjectionPermissionResultCode: Int = Activity.RESULT_CANCELED

    fun setMediaProjectionResult(resultCode: Int, data: Intent?) {
        mediaProjectionPermissionResultCode = resultCode
        mediaProjectionPermissionResultData = data
    }

    fun hasPermission(): Boolean {
        return mediaProjectionPermissionResultCode == Activity.RESULT_OK && mediaProjectionPermissionResultData != null
    }

    fun captureScreen(context: Context, callback: (Result<String>) -> Unit) {
        val data = mediaProjectionPermissionResultData
        if (data == null || mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            val fallbackBase64 = generatePlaceholderBase64()
            callback(Result.success(fallbackBase64))
            return
        }

        try {
            val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val mediaProjection: MediaProjection? = projectionManager.getMediaProjection(
                mediaProjectionPermissionResultCode,
                data.clone() as Intent
            )

            if (mediaProjection == null) {
                callback(Result.success(generatePlaceholderBase64()))
                return
            }

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)

            val width = 720
            val height = 1280
            val density = metrics.densityDpi

            val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            val virtualDisplay: VirtualDisplay? = mediaProjection.createVirtualDisplay(
                "MalszScreenCapture",
                width,
                height,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface,
                null,
                Handler(Looper.getMainLooper())
            )

            if (virtualDisplay == null) {
                mediaProjection.stop()
                callback(Result.success(generatePlaceholderBase64()))
                return
            }

            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val image = imageReader.acquireLatestImage()
                    if (image != null) {
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * width

                        val bitmap = Bitmap.createBitmap(
                            width + rowPadding / pixelStride,
                            height,
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.copyPixelsFromBuffer(buffer)
                        image.close()

                        val cleanBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                        val base64 = compressBitmapToBase64(cleanBitmap)

                        virtualDisplay.release()
                        mediaProjection.stop()
                        callback(Result.success(base64))
                    } else {
                        virtualDisplay.release()
                        mediaProjection.stop()
                        callback(Result.success(generatePlaceholderBase64()))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error acquiring screenshot image", e)
                    virtualDisplay.release()
                    mediaProjection.stop()
                    callback(Result.success(generatePlaceholderBase64()))
                }
            }, 500)
        } catch (e: Exception) {
            Log.e(TAG, "MediaProjection capture failed", e)
            callback(Result.success(generatePlaceholderBase64()))
        }
    }

    private fun compressBitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun generatePlaceholderBase64(): String {
        val bmp = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}
