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
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                isListening = false
                if (error != SpeechRecognizer.ERROR_NO_MATCH) partialText = ""
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
        onDispose { speechRecognizer.destroy() }
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

                AnimatedVisibility(visible = simplifiedText != null || isSimplifying) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Simplified Notes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { simplifiedText = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (isSimplifying) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Text("AI is simplifying the text...", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                            } else {
                                Text(simplifiedText ?: "", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

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
                                speechRecognizer.startListening(recognizerIntent)
                                isListening = true
                            }
                        },
                        containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            contentDescription = null,
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
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simplify Transcription")
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(onClick = { audioPermissionState.launchPermissionRequest() }) {
                        Text("Grant Microphone Permission")
                    }
                }
            }
        }
    }
}

private suspend fun simplifyTextWithGemini(text: String): String {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "null") {
        return "ERROR: Gemini API Key not found. Please add GEMINI_API_KEY to your local.properties file and Rebuild Project."
    }

    return try {
        // Updated to flash-latest for better regional support
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash-latest",
            apiKey = apiKey,
            generationConfig = generationConfig { 
                temperature = 0.5f 
                topP = 0.95f
                topK = 40
            },
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE)
            )
        )

        val prompt = "Please simplify this text for a student using clear bullet points: \"$text\""
        
        val response = generativeModel.generateContent(prompt)
        response.text ?: "AI returned no text. Try transcribing a longer sentence."
    } catch (e: Exception) {
        Log.e("GeminiError", "Connection failed", e)
        // Simplified error message for the user
        val errorMsg = e.message ?: "Unexpected response"
        if (errorMsg.contains("API_KEY_INVALID")) {
            "Error: Your API Key is invalid. Please create a new one in Google AI Studio."
        } else if (errorMsg.contains("User location is not supported")) {
            "Error: Gemini AI is not available in your region yet."
        } else {
            "AI Connection Error: $errorMsg. Please try again in a moment."
        }
    }
}
