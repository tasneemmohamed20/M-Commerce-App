package com.example.m_commerce.presentation.account.settings.view

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.m_commerce.R
import com.example.m_commerce.ResponseState
import com.example.m_commerce.presentation.account.settings.view_model.AddressMapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressMapToolbar(
    onBackClick: () -> Unit,
    onSearchClicked: () -> Unit,
) {
    TopAppBar(
        title = {

            Text(
                text = "Confirm Location" ,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

        },
        actions = {
            IconButton(onClick = onSearchClicked) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = Color.Blue
                )
            }
        },

        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        ),
    )
}

@Composable
fun AddressMap(
    onBackClick: () -> Unit = {},
    onConfirmLocation: (String) -> Unit = {},
    viewModel: AddressMapViewModel,
//    = hiltViewModel(),
    onSearchClicked: () -> Unit
) {
    val locationState by viewModel.locationState.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val address by viewModel.address.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val defaultLocation = LatLng(30.0444, 31.2357)
    var isMapIdle by remember { mutableStateOf(true) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation ?: defaultLocation,
            15f
        )
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AddressMapToolbar(
            onBackClick = onBackClick,
            onSearchClicked = onSearchClicked
        )

        Box(modifier = Modifier.weight(1f)) {
            when (locationState) {
                is ResponseState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is ResponseState.Success -> {
                    MapContent(
                        cameraPositionState = cameraPositionState,
                        currentLocation = currentLocation,
                        defaultLocation = defaultLocation,
                        isLoading = isLoading,
                        onLocationClick = { latLng ->
                            viewModel.fetchAddress("${latLng.latitude},${latLng.longitude}")
                        },
                        onMapIdle = { idle -> isMapIdle = idle }
                    )
                }
                is ResponseState.Failure -> {
                    Text(
                        text = "Failed to get location. Please check your permissions and try again.",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        BottomBar(
            onConfirmClick = { onConfirmLocation(address) },
            isEnabled = isMapIdle && address.isNotEmpty()
        )
    }
}


@Composable
private fun MapContent(
    cameraPositionState: CameraPositionState,
    currentLocation: LatLng?,
    defaultLocation: LatLng,
    isLoading: Boolean,
    onLocationClick: (LatLng) -> Unit,
    onMapIdle: (Boolean) -> Unit
) {

    LaunchedEffect(cameraPositionState.isMoving) {
        onMapIdle(!cameraPositionState.isMoving)
        if (!cameraPositionState.isMoving) {
            val targetLocation = cameraPositionState.position.target
            Log.d("MapContent", "Location Changed - Lat: ${targetLocation.latitude}, Lng: ${targetLocation.longitude}")
            Log.d("MapContent", "Map State: Idle")
            onLocationClick(targetLocation)
        } else {
            Log.d("MapContent", "Map State: Moving")
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            Log.d("MapContent", "Current Location Updated - Lat: ${it.latitude}, Lng: ${it.longitude}")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { }
        ) {
            Circle(
                center = cameraPositionState.position.target,
                radius = 200.0,
                fillColor = Color(0x553498DB),
                strokeColor = Color(0xFF3498DB),
                strokeWidth = 2f
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-32).dp)
            ) {
                AnimatedVisibility(
                    visible = !cameraPositionState.isMoving,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Your order will be delivered here",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .alpha(1f),
                    painter = painterResource(id = R.drawable.map_marker),
                    contentDescription = "Location marker",
                    tint = Color.Blue
                )
            }
        }
    }

    if (isLoading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun BottomBar(
    onConfirmClick: () -> Unit,
    isEnabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        shadowElevation = 16.dp,
        tonalElevation = 8.dp,
        color = Color.White,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = onConfirmClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                ),
                enabled = isEnabled
            ) {
                Text("Enter Complete Address")
                Log.d("TAG", "BottomBar: $isEnabled")
            }
        }
    }
}

@Preview
@Composable
fun AddressMapPreview() {
    BottomBar(
        onConfirmClick = { /* Handle confirm click */ }
    )
}