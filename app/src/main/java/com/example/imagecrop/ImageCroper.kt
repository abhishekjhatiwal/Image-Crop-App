package com.example.imagecrop

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.roundToInt

// Helper enum class to keep track of corners
enum class Corner {
    TopLeft, TopRight, BottomLeft, BottomRight
}

@Composable
fun ImageCropper() {

    // Load or pass your ImageBitmap here
    val imageBitmap: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.image_to_crop)

    //create an image variable to show in image composable & initialise with imageBitmap
    var image by remember {
        mutableStateOf(imageBitmap)
    }

    // States/Offsets for each corner
    var topLeft by remember { mutableStateOf(Offset(400f, 400f)) }
    var topRight by remember { mutableStateOf(Offset(800f, 400f)) }
    var bottomLeft by remember { mutableStateOf(Offset(400f, 800f)) }
    var bottomRight by remember { mutableStateOf(Offset(800f, 800f)) }

    // Track which corner or center is being dragged
    var draggingCorner by remember { mutableStateOf<Corner?>(null) }
    var draggingCenter by remember { mutableStateOf(false) }



    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        //show the image to be crop
        Image(
            bitmap = image,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Canvas for drawing the crop rectangle and handling gestures
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(onDragStart = { offset ->
                    //check which corner is being dragged or not
                    draggingCorner = when {
                        offset.isNear(topLeft) -> Corner.TopLeft
                        offset.isNear(topRight) -> Corner.TopRight
                        offset.isNear(bottomLeft) -> Corner.BottomLeft
                        offset.isNear(bottomRight) -> Corner.BottomRight
                        else -> null
                    }

                    // is the cropping rectangle itself is being dragging or not
                    draggingCenter = draggingCorner == null && Rect(
                        topLeft, bottomRight
                    ).contains(offset)
                }, onDrag = { change, dragAmount ->
                    change.consume()

                    //while dragging any corner or whole rectangle, keep updating the offset of all the corners
                    when (draggingCorner) {
                        Corner.TopLeft -> {
                            topLeft += dragAmount
                            topRight = topRight.copy(y = topLeft.y)
                            bottomLeft = bottomLeft.copy(x = topLeft.x)
                        }

                        Corner.TopRight -> {
                            topRight += dragAmount
                            topLeft = topLeft.copy(y = topRight.y)
                            bottomRight = bottomRight.copy(x = topRight.x)
                        }

                        Corner.BottomLeft -> {
                            bottomLeft += dragAmount
                            topLeft = topLeft.copy(x = bottomLeft.x)
                            bottomRight = bottomRight.copy(y = bottomLeft.y)
                        }

                        Corner.BottomRight -> {
                            bottomRight += dragAmount
                            topRight = topRight.copy(x = bottomRight.x)
                            bottomLeft = bottomLeft.copy(y = bottomRight.y)
                        }

                        null -> if (draggingCenter) {
                            // Move the entire rectangle by adjusting all corners
                            topLeft += dragAmount
                            topRight += dragAmount
                            bottomLeft += dragAmount
                            bottomRight += dragAmount
                        }
                    }
                }, onDragEnd = {
                    draggingCorner = null
                    draggingCenter = false
                })
            }) {
            // Calculate the size of the crop rectangle
            val rectSize = Size(
                width = topRight.x - topLeft.x, height = bottomLeft.y - topLeft.y
            )

            // Draw the crop rectangle
            drawRect(
                color = Color.Green, topLeft = topLeft, size = rectSize, style = Stroke(width = 4f)
            )

            // Draw corner handles
            drawHandle(topLeft)
            drawHandle(topRight)
            drawHandle(bottomLeft)
            drawHandle(bottomRight)
        }

        // Save Crop button
        Button(
            onClick = {
                val croppedBitmap = getCroppedBitmap(
                    imageBitmap,
                    Rect(topLeft, bottomRight),
                    canvasWidth = constraints.maxWidth.toFloat(),
                    canvasHeight = constraints.maxHeight.toFloat()
                )

                //set the cropped image to the composable
                image = croppedBitmap.asImageBitmap()
            }, modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(text = "Show Cropped Image")
        }
    }
}

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