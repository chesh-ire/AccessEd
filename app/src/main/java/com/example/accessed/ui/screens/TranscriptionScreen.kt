package com.example.accessed.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.accessed.BuildConfig
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TranscriptionScreenContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    var transcribedText by remember { mutableStateOf("") }
    var partialText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var simplifiedText by remember { mutableStateOf<String?>(null) }
    var isSimplifying by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val recognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("STT", "Ready for speech")
                isListening = true
            }
            override fun onBeginningOfSpeech() {
                Log.d("STT", "Beginning of speech")
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d("STT", "End of speech")
            }
            override fun onError(error: Int) {
                isListening = false
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech input detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    else -> "Unknown error: $error"
                }
                Log.e("STT", "Error: $errorMessage ($error)")
                if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                    partialText = ""
                }
            }
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val result = matches[0]
                    transcribedText = if (transcribedText.isEmpty()) result else "$transcribedText $result"
                    partialText = ""
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    partialText = matches[0]
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose {
            speechRecognizer.destroy()
        }
    }

    LaunchedEffect(transcribedText, partialText) {
        if (transcribedText.isNotEmpty() || partialText.isNotEmpty()) {
            listState.animateScrollToItem(0) 
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Live Transcription", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { 
                        transcribedText = ""
                        partialText = ""
                        simplifiedText = null 
                    }) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (audioPermissionState.status.isGranted) {
                // Transcription Area
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            if (transcribedText.isEmpty() && partialText.isEmpty()) {
                                Text(
                                    text = if (isListening) "Listening..." else "Tap the microphone to start transcribing",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            } else {
                                Text(
                                    text = transcribedText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (partialText.isNotEmpty()) {
                                    Text(
                                        text = if (transcribedText.isEmpty()) partialText else " $partialText",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simplified Text Area
                AnimatedVisibility(visible = simplifiedText != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simplified Explanation", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { simplifiedText = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(simplifiedText ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LargeFloatingActionButton(
                        onClick = {
                            if (isListening) {
                                speechRecognizer.stopListening()
                                isListening = false
                            } else {
                                try {
                                    speechRecognizer.startListening(recognizerIntent)
                                    isListening = true
                                } catch (e: Exception) {
                                    Log.e("STT", "Failed to start listening", e)
                                    isListening = false
                                }
                            }
                        },
                        containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            contentDescription = if (isListening) "Stop Recording" else "Start Recording",
                            modifier = Modifier.size(36.dp),
                            tint = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            val textToSimplify = (transcribedText + " " + partialText).trim()
                            if (textToSimplify.isNotBlank()) {
                                scope.launch {
                                    isSimplifying = true
                                    simplifiedText = simplifyTextWithGemini(textToSimplify)
                                    isSimplifying = false
                                }
                            }
                        },
                        enabled = !isSimplifying && (transcribedText.isNotBlank() || partialText.isNotBlank()),
                        modifier = Modifier.height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (isSimplifying) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simplify")
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Microphone permission is required for transcription")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { audioPermissionState.launchPermissionRequest() }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}

private suspend fun simplifyTextWithGemini(text: String): String {
    val config = generationConfig {
        temperature = 0.7f
    }
    
    val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
    )

    return try {
        // Using "gemini-1.5-flash-latest" as it is often more reliably mapped in the SDK
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash-latest", 
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = config,
            safetySettings = safetySettings
        )

        val prompt = "Please simplify this text into easy language for a student: \"$text\""
        val response = generativeModel.generateContent(prompt)
        response.text ?: "The AI returned an empty response."
    } catch (e: Exception) {
        Log.e("Gemini", "Error with flash-latest, trying flash", e)
        try {
            val fallbackModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY,
                generationConfig = config
            )
            val response = fallbackModel.generateContent("Simplify this: $text")
            response.text ?: "Empty response from fallback."
        } catch (e2: Exception) {
            Log.e("Gemini", "Final failure", e2)
            "Error: ${e2.message}. Please check if your API Key is valid and has Gemini API enabled in Google AI Studio."
        }
    }
}
