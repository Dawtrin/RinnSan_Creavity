package com.rinnsan.creavity.presentation.cart

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import com.rinnsan.creavity.core.theme.*
import kotlinx.coroutines.delay

@Composable
fun LiveTrackingScreen(navController: NavController, orderId: String) {
    val db = remember { FirebaseFirestore.getInstance() }
    
    var shipperLocation by remember { mutableStateOf<LatLng?>(null) }
    var customerAddressStr by remember { mutableStateOf("") }
    
    // Realtime Listener cho Tracking Data
    DisposableEffect(orderId) {
        val listener = db.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                val addressMap = snapshot.get("address") as? Map<String, String>
                if (addressMap != null) {
                    val street = addressMap["street"] ?: ""
                    val city = addressMap["city"] ?: ""
                    customerAddressStr = "$street, $city"
                }
                
                val tracking = snapshot.get("tracking") as? Map<String, Any>
                if (tracking != null && tracking.containsKey("lat") && tracking.containsKey("lng")) {
                    val lat = (tracking["lat"] as? Number)?.toDouble() ?: 0.0
                    val lng = (tracking["lng"] as? Number)?.toDouble() ?: 0.0
                    shipperLocation = LatLng(lat, lng)
                }
            }
        onDispose { listener.remove() }
    }

    // Default center to Da Nang if no shipper location
    val defaultLocation = LatLng(16.0544, 108.2022)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // Move camera smoothly when shipper location updates
    LaunchedEffect(shipperLocation) {
        shipperLocation?.let {
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 16f),
                durationMs = 1500
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapStyleOptions = null // Could use custom dark JSON style here
            ),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            // Shipper Marker
            shipperLocation?.let { loc ->
                Marker(
                    state = MarkerState(position = loc),
                    title = "Shipper đang giao hàng",
                    snippet = "Đang di chuyển...",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }
        }

        // Top Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 52.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(36.dp).background(VoidBlack, RoundedCornerShape(8.dp)).border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = TeslaWhite, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFF9B9BFF).copy(0.9f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "LIVE TRACKING",
                    fontFamily = AppFonts.spaceMono, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, color = VoidBlack
                )
            }
        }

        // Bottom Info Card
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp)
                .background(VoidBlack.copy(alpha = 0.9f))
                .border(1.dp, Color(0xFF9B9BFF).copy(0.4f))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF9B9BFF), modifier = Modifier.size(24.dp))
                    Column {
                        Text(
                            "ĐƠN HÀNG ĐANG TRÊN ĐƯỜNG",
                            fontFamily = AppFonts.oswald, fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = TeslaWhite
                        )
                        Text(
                            "Đơn hàng #${orderId.take(8).uppercase()}",
                            fontFamily = AppFonts.spaceMono, fontSize = 10.sp, color = TechSilver
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (customerAddressStr.isNotEmpty()) {
                    Text(
                        "GIAO ĐẾN",
                        fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver, letterSpacing = 1.sp
                    )
                    Text(
                        customerAddressStr,
                        fontFamily = AppFonts.spaceMono, fontSize = 12.sp, color = TeslaWhite
                    )
                }
                if (shipperLocation == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF9B9BFF), trackColor = VoidBlack)
                    Text(
                        "Đang kết nối vị trí Shipper...",
                        fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver, modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
