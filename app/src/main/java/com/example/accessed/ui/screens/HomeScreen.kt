package com.example.accessed.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accessed.ui.navigation.Screen

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "AccessEd",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Choose a service to begin",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AccessibleModuleButton(
            title = "Smart Campus Map",
            subtitle = "Accessible navigation & GPS",
            icon = Icons.Rounded.Map,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onClick = { onNavigate(Screen.Map.route) }
        )

        AccessibleModuleButton(
            title = "Accessible Dialogue",
            subtitle = "Communication Hub",
            icon = Icons.AutoMirrored.Rounded.Chat,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            onClick = { onNavigate(Screen.Dialogue.route) }
        )

        AccessibleModuleButton(
            title = "Learning Support",
            subtitle = "OCR Reader & TTS",
            icon = Icons.Rounded.Description,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = { onNavigate(Screen.OCR.route) }
        )

        AccessibleModuleButton(
            title = "Live Transcription",
            subtitle = "STT & Text Simplification",
            icon = Icons.Rounded.RecordVoiceOver,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onClick = { onNavigate(Screen.Transcription.route) }
        )

        AccessibleModuleButton(
            title = "Emergency Help",
            subtitle = "Call for immediate assistance",
            icon = Icons.Rounded.Warning,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            onClick = { onNavigate(Screen.SafetyHub.route) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AccessibleModuleButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 18.sp
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor.copy(alpha = 0.5f)
            )
        }
    }
}
