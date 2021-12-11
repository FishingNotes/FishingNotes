package com.joesemper.fishing.compose.ui.home.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.maps.android.ktx.awaitMap
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.MainActivity
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object MapTypes {
    const val roadmap = GoogleMap.MAP_TYPE_NORMAL
    const val satellite = GoogleMap.MAP_TYPE_SATELLITE
    const val hybrid = GoogleMap.MAP_TYPE_HYBRID
    const val terrain = GoogleMap.MAP_TYPE_TERRAIN
}

fun getHue(red: Float, green: Float, blue: Float): Float {
    val min = min(min(red, green), blue)
    val max = max(max(red, green), blue)
    val c = max-min
    if (min == max) {
        return 0f
    }
    var hue = 0f
    when (max) {
        red -> {
            val segment = (green - blue) / c;
            var shift = 0 / 60;       // R° / (360° / hex sides)
            if (segment < 0) {          // hue > 180, full rotation
                shift = 360 / 60;         // R° / (360° / hex sides)
            }
            hue = segment + shift;
        }
        green -> {
            val segment = (blue - red) / c;
            val shift = 120 / 60;     // G° / (360° / hex sides)
            hue = segment + shift;
        }
        blue -> {
            val segment = (red - green) / c;
            val shift = 240 / 60;     // B° / (360° / hex sides)
            hue = segment + shift;
        }
    }
    return hue * 60; // hue is in [0,6], scale it up
}

sealed class CameraMoveState {
    object MoveStart : CameraMoveState()
    object MoveFinish : CameraMoveState()
}

sealed class PointerState {
    object HideMarker : PointerState()
    object ShowMarker : PointerState()
}

sealed class LocationState() {
    object NoPermission : LocationState()
    class LocationGranted(val location: LatLng) : LocationState()
    object LocationNotGranted : LocationState()
}

sealed class MapUiState {
    object NormalMode : MapUiState()
    object PlaceSelectMode : MapUiState()
    //object FastAddPlace : MapUiState()
    object BottomSheetInfoMode : MapUiState()
}

const val DEFAULT_ZOOM = 15f

val locationPermissionsList = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

fun moveCameraToLocation(
    coroutineScope: CoroutineScope,
    map: MapView,
    location: LatLng,
    zoom: Float = DEFAULT_ZOOM
) {
    coroutineScope.launch {
        val googleMap = map.awaitMap()
        googleMap.stopAnimation()
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                zoom
            )
        )
    }
}

fun setCameraPosition(
    coroutineScope: CoroutineScope,
    map: MapView,
    location: LatLng,
    zoom: Float = DEFAULT_ZOOM
) {
    coroutineScope.launch {
        val googleMap = map.awaitMap()
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                zoom
            )
        )
    }
}

fun checkPermission(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED
}

fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> throw IllegalStateException()
        }
    }


@ExperimentalCoroutinesApi
@ExperimentalPermissionsApi
fun getCurrentLocationFlow(
    context: Context,
    permissionsState: MultiplePermissionsState,
) = callbackFlow {
    val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    checkPermission(context)

    val result = mutableStateOf(LatLng(0.0, 0.0))

    if (permissionsState.allPermissionsGranted) {
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnSuccessListener { task ->

            try {
                result.value = LatLng(task.latitude, task.longitude)
                trySend(
                    LocationState.LocationGranted(
                        location = LatLng(
                            task.latitude,
                            task.longitude
                        )
                    )
                )

            } catch (e: Exception) {
                Log.d("MAP", "Unable to get location")
                Toast.makeText(context, R.string.cant_get_current_location, Toast.LENGTH_SHORT)
                    .show()
            }

        }
    } else {
        trySend(LocationState.NoPermission)
    }
    awaitClose { }
}

@ExperimentalPermissionsApi
fun getCurrentLocation(
    context: Context,
    permissionsState: MultiplePermissionsState,
) = runBlocking {
    val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    checkPermission(context)

    val result = mutableStateOf(LatLng(0.0, 0.0))

    if (permissionsState.allPermissionsGranted) {
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnSuccessListener { task ->

            try {
                result.value = LatLng(task.latitude, task.longitude)

            } catch (e: Exception) {
                Log.d("MAP", "Unable to get location")
                Toast.makeText(context, R.string.cant_get_current_location, Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }
    result
}

fun startMapsActivityForNavigation(mapMarker: UserMapMarker, context: Context) {
    val uri = String.format(
        Locale.ENGLISH,
        "http://maps.google.com/maps?daddr=%f,%f (%s)",
        mapMarker.latitude,
        mapMarker.longitude,
        mapMarker.title
    )
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("com.google.android.apps.maps")
    try {
        ContextCompat.startActivity(context, intent, null)
    } catch (e: ActivityNotFoundException) {
        try {
            val unrestrictedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            ContextCompat.startActivity(context, unrestrictedIntent, null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.install_maps_app), Toast.LENGTH_LONG)
                .show()
        }
    }
}

@Composable
fun BackPressHandler(
    mapUiState: MapUiState,
    onBackPressedCallback: () -> Unit
) {
    val context = LocalContext.current
    var lastPressed: Long = 0
    BackHandler(onBack = {
        when (mapUiState) {
            MapUiState.NormalMode -> {
                val currentMillis = System.currentTimeMillis()
                if (currentMillis - lastPressed < 2000) {
                    (context as MainActivity).finish()
                } else {
                    showToast(context, "Do it again to close the app")
                }
                lastPressed = currentMillis
            }
            else -> onBackPressedCallback()
        }
    })
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = (LocalContext.current as MainActivity)
    val mapView: MapView = remember { MapView(context).apply { id = R.id.map } }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        // Make MapView follow the current lifecycle
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
    return mapView
}