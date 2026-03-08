package com.example.accessed.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogueScreen() {
    var text by remember { mutableStateOf("") }
    val quickPhrases = listOf(
        "I need help",
        "Please repeat that",
        "Where is the classroom?",
        "Call faculty"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Accessible Dialogue", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Communication Hub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Type or use speech-to-text") },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        maxLines = 10
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly, 
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { /* TODO: Implement TTS */ }) {
                            Icon(Icons.Rounded.RecordVoiceOver, contentDescription = "Speak Text")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Speak")
                        }
                        Button(onClick = { /* TODO: Implement STT */ }) {
                            Icon(Icons.Rounded.Hearing, contentDescription = "Listen")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Listen")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Quick Phrases", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickPhrases) { phrase ->
                    AssistChip(
                        onClick = { text = phrase },
                        label = { Text(phrase) },
                        leadingIcon = { 
                            Icon(
                                Icons.AutoMirrored.Rounded.Send, 
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            ) 
                        }
                    )
                }
            }
        }
    }
}
