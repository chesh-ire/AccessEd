package com.example.accessed.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SafetyHubScreen() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("safety_prefs", Context.MODE_PRIVATE) }
    
    var guardianNumber by remember { mutableStateOf(sharedPrefs.getString("guardian_num", "") ?: "") }
    var savedGuardianNumber by remember { mutableStateOf(sharedPrefs.getString("guardian_num", "") ?: "") }
    
    // Emergency State
    var isEmergencyTriggered by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableIntStateOf(10) }
    
    val smsPermissionState = rememberPermissionState(Manifest.permission.SEND_SMS)

    // Sensor Logic for Shake Detection
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        var lastUpdate: Long = 0
        var lastX = 0f
        var lastY = 0f
        var lastZ = 0f
        val SHAKE_THRESHOLD = 800 

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                
                val curTime = System.currentTimeMillis()
                if ((curTime - lastUpdate) > 100) {
                    val diffTime = curTime - lastUpdate
                    lastUpdate = curTime

                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val speed = sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)) / diffTime * 10000

                    if (speed > SHAKE_THRESHOLD && !isEmergencyTriggered && savedGuardianNumber.isNotEmpty()) {
                        isEmergencyTriggered = true
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(500)
                        }
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    // Countdown Logic
    LaunchedEffect(isEmergencyTriggered) {
        if (isEmergencyTriggered) {
            countdownValue = 10
            while (countdownValue > 0 && isEmergencyTriggered) {
                delay(1000)
                countdownValue--
            }
            if (isEmergencyTriggered && countdownValue == 0) {
                if (smsPermissionState.status.isGranted) {
                    sendEmergencyAlert(context, savedGuardianNumber)
                    isEmergencyTriggered = false
                    Toast.makeText(context, "Emergency Alert Sent!", Toast.LENGTH_LONG).show()
                } else {
                    smsPermissionState.launchPermissionRequest()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Safety Hub", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        titleContentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Guardian Configuration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = guardianNumber,
                                onValueChange = { guardianNumber = it },
                                label = { Text("Enter Guardian Number") },
                                placeholder = { Text("+1234567890") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    sharedPrefs.edit().putString("guardian_num", guardianNumber).apply()
                                    savedGuardianNumber = guardianNumber
                                    Toast.makeText(context, "Guardian Saved", Toast.LENGTH_SHORT).show()
                                    if (!smsPermissionState.status.isGranted) {
                                        smsPermissionState.launchPermissionRequest()
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save Guardian")
                            }
                        }
                    }
                }

                if (savedGuardianNumber.isNotEmpty()) {
                    item {
                        Text(
                            text = "Emergency Contact",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Guardian", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(savedGuardianNumber, style = MaterialTheme.typography.bodyMedium)
                                }
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$savedGuardianNumber") }
                                        context.startActivity(intent)
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                                ) {
                                    Icon(Icons.Rounded.Call, contentDescription = "Call Guardian")
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Indian Emergency Services",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    EmergencyActionCard(
                        title = "Call Police",
                        subtitle = "Dial 100 for immediate help",
                        icon = Icons.Rounded.LocalPolice,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:100") }
                            context.startActivity(intent)
                        }
                    )
                }

                item {
                    EmergencyActionCard(
                        title = "Call Ambulance",
                        subtitle = "Dial 102 for medical help",
                        icon = Icons.Rounded.MedicalServices,
                        color = Color(0xFFE53935), // Urgent Red
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:102") }
                            context.startActivity(intent)
                        }
                    )
                }

                item {
                    EmergencyActionCard(
                        title = "Call Fire Brigade",
                        subtitle = "Dial 101 for fire emergency",
                        icon = Icons.Rounded.LocalFireDepartment,
                        color = Color(0xFFF4511E), // Fire Orange
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:101") }
                            context.startActivity(intent)
                        }
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Quick SOS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    EmergencyActionCard(
                        title = "SOS Panic Button",
                        subtitle = "Trigger immediate alert",
                        icon = Icons.Rounded.Warning,
                        color = MaterialTheme.colorScheme.error,
                        onClick = {
                            if (savedGuardianNumber.isEmpty()) {
                                Toast.makeText(context, "Please save a guardian number first", Toast.LENGTH_SHORT).show()
                            } else {
                                isEmergencyTriggered = true
                            }
                        }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Security, contentDescription = null, tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Panic Detection is ACTIVE", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text("App is monitoring for sudden shaking.", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Emergency Countdown Overlay
        AnimatedVisibility(
            visible = isEmergencyTriggered,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.Warning, 
                        contentDescription = null, 
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Panic Detected!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            text = countdownValue.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        "Sending emergency SMS with your location to your guardian in $countdownValue seconds...",
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = { isEmergencyTriggered = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("I AM SAFE (CANCEL)", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun sendEmergencyAlert(context: Context, phoneNumber: String) {
    if (phoneNumber.isEmpty()) return
    
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            val locString = if (location != null) {
                "My Location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "Location unavailable"
            }
            
            val message = "EMERGENCY ALERT from AccessEd! I might be in danger. $locString"
            
            try {
                val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}

@Composable
fun EmergencyActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = MaterialTheme.shapes.large,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
