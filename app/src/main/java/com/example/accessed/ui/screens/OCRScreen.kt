package com.example.accessed.ui.screens

import android.Manifest
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.*
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun OCRScreen() {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var recognizedText by remember { mutableStateOf("No text detected yet.") }
    var isScanning by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    
    DisposableEffect(context) {
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.US
            }
        }
        tts.value = TextToSpeech(context, listener)
        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Smart OCR Reader", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { 
                        recognizedText = "No text detected yet."
                        isScanning = false
                    }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Clear")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (cameraPermissionState.status.isGranted) {
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    if (isScanning) {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onTextDetected = { text ->
                                if (text.isNotBlank()) {
                                    recognizedText = text
                                }
                            }
                        )
                        // Overlay to indicate scanning area
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // You could add a scanning frame here
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (recognizedText != "No text detected yet." && recognizedText != "Scanning...") 
                                    Icons.Rounded.Description else Icons.Rounded.Camera,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (recognizedText != "No text detected yet." && recognizedText != "Scanning...")
                                    "Extraction Complete" else "Tap below to start scanning",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera access is required for OCR")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Extracted Content",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            item {
                                Text(
                                    text = recognizedText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LargeFloatingActionButton(
                            onClick = { 
                                if (!isScanning) {
                                    recognizedText = "Scanning..."
                                }
                                isScanning = !isScanning 
                            },
                            containerColor = if (isScanning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                if (isScanning) Icons.Rounded.Stop else Icons.Rounded.CenterFocusStrong,
                                contentDescription = if (isScanning) "Stop Scanning" else "Start Scanning",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        FloatingActionButton(
                            onClick = {
                                if (isPlaying) {
                                    tts.value?.stop()
                                    isPlaying = false
                                } else {
                                    if (recognizedText.isNotBlank() && 
                                        recognizedText != "No text detected yet." && 
                                        recognizedText != "Scanning...") {
                                        tts.value?.speak(recognizedText, TextToSpeech.QUEUE_FLUSH, null, "OCR_ID")
                                        isPlaying = true
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(
                                if (isPlaying) Icons.Rounded.Pause else Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = if (isPlaying) "Pause Reading" else "Read Aloud"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onTextDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    
    val previewView = remember { 
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(lifecycleOwner) {
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(context).await()
            
            // Modern Resolution Selector
            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        Size(1280, 720),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                    )
                )
                .build()

            val preview = Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setResolutionSelector(resolutionSelector)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(recognizer, imageProxy, onTextDetected)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            Log.e("OCR", "Use case binding failed", e)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    imageProxy: ImageProxy,
    onTextDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Filter out very short or likely noise text to improve quality
                val filteredText = visionText.text
                if (filteredText.length > 3) {
                    onTextDetected(filteredText)
                }
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Text recognition failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
