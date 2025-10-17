package com.example.imagecrop.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.example.imagecrop.R
import com.example.imagecrop.data.Corner
import com.example.imagecrop.drawHandle
import com.example.imagecrop.getCroppedBitmap
import com.example.imagecrop.isNear
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ImageCropper() {

    // Load or pass your ImageBitmap here
    val imageBitmap: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.abhishek)

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
    val coroutineScope = rememberCoroutineScope()



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
        Canvas(
            modifier = Modifier
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

//            drawRect(
//                color = Color.Black.copy(alpha = 0.5f),
//                size = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
//            )
//            drawRect(
//                color = Color.Transparent,
//                topLeft = topLeft,
//                size = rectSize,
//                blendMode = BlendMode.Clear
//            )


            // Draw corner handles
            drawHandle(topLeft)
            drawHandle(topRight)
            drawHandle(bottomLeft)
            drawHandle(bottomRight)
        }

        // Save Crop button
        Button(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val croppedBitmap = getCroppedBitmap(
                        imageBitmap,
                        Rect(topLeft, bottomRight),
                        canvasWidth = constraints.maxWidth.toFloat(),
                        canvasHeight = constraints.maxHeight.toFloat()
                    )
                    image = croppedBitmap.asImageBitmap()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(text = "Show Cropped Image")
        }
    }
}