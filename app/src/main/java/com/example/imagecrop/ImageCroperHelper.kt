package com.example.imagecrop

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.min
import kotlin.math.roundToInt

// Check if the touch is near a specific point
fun Offset.isNear(point: Offset, threshold: Float = 50f): Boolean {
    return (this - point).getDistance() <= threshold
}

// Draw a handle at the specified position
fun DrawScope.drawHandle(center: Offset) {
    drawCircle(
        color = Color.Green, radius = 25f, center = center
    )
}

/**
 * Extracts the cropped area from the original image based on the given crop rectangle.
 *
 * @param imageBitmap The original image bitmap.
 * @param cropRect The crop rectangle in canvas coordinates.
 * @param canvasWidth The width of the canvas.
 * @param canvasHeight The height of the canvas.
 * @return A Bitmap representing the cropped area.
 */
fun getCroppedBitmap(
    imageBitmap: ImageBitmap,   // The original ImageBitmap
    cropRect: Rect,             // The crop rectangle area on the canvas
    canvasWidth: Float,         // The width of the canvas where the image is displayed
    canvasHeight: Float         // The height of the canvas where the image is displayed
): Bitmap {
    val bitmapWidth = imageBitmap.width.toFloat()
    val bitmapHeight = imageBitmap.height.toFloat()

    // Calculate scaling factors to fit the image within the canvas
    val widthRatio = canvasWidth / bitmapWidth
    val heightRatio = canvasHeight / bitmapHeight

    val scaleFactor = min(widthRatio, heightRatio) // Preserve aspect ratio

    // Calculate the actual displayed image dimensions within the canvas
    val displayedImageWidth = bitmapWidth * scaleFactor
    val displayedImageHeight = bitmapHeight * scaleFactor

    // Calculate the offset to center the image within the canvas
    val offsetX = (canvasWidth - displayedImageWidth) / 2
    val offsetY = (canvasHeight - displayedImageHeight) / 2

    // Map the crop rectangle coordinates from the canvas to the original image dimensions
    val cropLeft =
        ((cropRect.left - offsetX) / scaleFactor).roundToInt().coerceIn(0, bitmapWidth.toInt())
    val cropTop =
        ((cropRect.top - offsetY) / scaleFactor).roundToInt().coerceIn(0, bitmapHeight.toInt())
    val cropRight =
        ((cropRect.right - offsetX) / scaleFactor).roundToInt().coerceIn(0, bitmapWidth.toInt())
    val cropBottom =
        ((cropRect.bottom - offsetY) / scaleFactor).roundToInt().coerceIn(0, bitmapHeight.toInt())

    // Calculate the cropped area width and height
    val cropWidth = (cropRight - cropLeft).coerceAtLeast(1)  // Ensure minimum 1px width
    val cropHeight = (cropBottom - cropTop).coerceAtLeast(1)  // Ensure minimum 1px height

    // Create a cropped bitmap from the original bitmap using the calculated rectangle
    return Bitmap.createBitmap(
        imageBitmap.asAndroidBitmap(), cropLeft, cropTop, cropWidth, cropHeight
    )
}