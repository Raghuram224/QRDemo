package com.example.qrdemo

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier
) {
    val localContext = LocalContext.current
    val code = remember {
        mutableStateOf("")
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val selector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()
    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(localContext)
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                localContext,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = modifier
            .fillMaxSize(),

    ) {
        if (hasCameraPermission){
            AndroidView(
                modifier = Modifier
                    .weight(0.8f),
                factory = { context->
                    val previewView = PreviewView(context)
                    val preview = Preview.Builder().build()

                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        QRCodeAnalyzer{ result->
                            code.value =result
                        }
                    )

                    try {
                        cameraProviderFuture.get().bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageAnalysis
                        )
                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                    return@AndroidView previewView


                }
            )
        }
        Text(
            modifier = Modifier
                .weight(0.2f),
            text = code.value,
            style = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
        )


    }
    
}