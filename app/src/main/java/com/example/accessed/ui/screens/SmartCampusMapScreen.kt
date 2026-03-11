package com.example.accessed.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

data class CampusLocation(
    val name: String,
    val latLng: LatLng,
    val category: String,
    val snippet: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun SmartCampusMapScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var showLocationInfo by remember { mutableStateOf(false) }
    
    val defaultCenter = LatLng(19.0760, 72.8777)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 15f)
    }

    var selectedLocation by remember { mutableStateOf<CampusLocation?>(null) }
    var currentCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc: Location? ->
                    loc?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        userLocation = latLng
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 16f)
                    }
                }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    val locations = remember(userLocation) {
        val base = userLocation ?: defaultCenter
        listOf(
            CampusLocation("Nearest Hospital", LatLng(base.latitude + 0.0015, base.longitude + 0.0005), "Hospital", "Finding real medical centers near you..."),
            CampusLocation("Nearby Restaurant", LatLng(base.latitude - 0.0010, base.longitude + 0.0012), "Restaurant", "Finding dining options near you..."),
            CampusLocation("Campus Parking", LatLng(base.latitude + 0.0020, base.longitude - 0.0015), "Parking", "Accessible parking spots"),
            CampusLocation("College Building", LatLng(base.latitude + 0.0015, base.longitude + 0.0025), "College", "Educational facilities"),
            CampusLocation("Student Cafe", LatLng(base.latitude - 0.0005, base.longitude + 0.0018), "Cafe", "Coffee and study spaces"),
            CampusLocation("Nearby ATM", LatLng(base.latitude + 0.0005, base.longitude - 0.0005), "ATM", "Bank and cash services"),
            CampusLocation("Local Library", LatLng(base.latitude + 0.0012, base.longitude - 0.0020), "Library", "Reading and research centers"),
        )
    }

    val filteredLocations = remember(currentCategory, locations) {
        if (currentCategory == null) locations else locations.filter { it.category == currentCategory }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Campus Explorer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true),
                properties = MapProperties(isMyLocationEnabled = locationPermissionState.status.isGranted)
            ) {
                filteredLocations.forEach { location ->
                    Marker(
                        state = MarkerState(position = location.latLng),
                        title = location.name,
                        snippet = location.snippet,
                        onClick = {
                            selectedLocation = location
                            false
                        }
                    )
                }
            }

            // Location Info Overlay
            if (showLocationInfo) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Current Coordinates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { showLocationInfo = false }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close")
                            }
                        }
                        val coordString = userLocation?.let { "${it.latitude}, ${it.longitude}" } ?: "Locating..."
                        Text(coordString, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Location", coordString)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Coordinates copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy")
                            }
                            Button(
                                onClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "My Location: https://www.google.com/maps/search/?api=1&query=$coordString")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Action Buttons (Removed Explorer)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MapActionButton("My Location", Modifier.weight(1f)) { 
                        userLocation?.let {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 18f)
                            showLocationInfo = true
                        } ?: run {
                            Toast.makeText(context, "Still locating...", Toast.LENGTH_SHORT).show()
                        }
                    }
                    MapActionButton("Search", Modifier.weight(1f)) {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    }
                }

                // Bottom Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedLocation?.let { location ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(location.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("Searching for the nearest real ${location.category.lowercase()}...", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    IconButton(onClick = { selectedLocation = null }) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val query = Uri.encode("${location.category} near me")
                                        val gmmIntentUri = Uri.parse("google.navigation:q=$query&mode=w")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        context.startActivity(mapIntent)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Navigation, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Find & Navigate Nearest", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Surface(
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        val categories = listOf("Restaurant", "Hospital", "Parking", "College", "Cafe", "ATM", "Library")
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(195.dp)
                        ) {
                            items(categories) { category ->
                                CategoryButton(
                                    text = category + "s",
                                    selected = currentCategory == category,
                                    onClick = {
                                        currentCategory = category
                                        val origin = userLocation ?: defaultCenter
                                        val nearest = locations
                                            .filter { it.category == category }
                                            .minByOrNull { 
                                                val results = FloatArray(1)
                                                Location.distanceBetween(
                                                    origin.latitude, origin.longitude,
                                                    it.latLng.latitude, it.latLng.longitude,
                                                    results
                                                )
                                                results[0]
                                            }
                                        selectedLocation = nearest
                                        nearest?.let { cameraPositionState.position = CameraPosition.fromLatLngZoom(it.latLng, 18f) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapActionButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CategoryButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(46.dp),
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            contentColor = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
