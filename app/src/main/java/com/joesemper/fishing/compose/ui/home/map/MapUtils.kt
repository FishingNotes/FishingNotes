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
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.joesemper.fishing.model.entity.content.UserMapMarker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

object MapTypes {
    const val roadmap = GoogleMap.MAP_TYPE_NORMAL
    const val satellite = GoogleMap.MAP_TYPE_SATELLITE
    const val hybrid = GoogleMap.MAP_TYPE_HYBRID
    const val terrain = GoogleMap.MAP_TYPE_TERRAIN
}

sealed class CameraMoveState {
    object MoveStart : CameraMoveState()
    object MoveFinish : CameraMoveState()
}

sealed class PointerState {
    object HideMarker : PointerState()
    object ShowMarker : PointerState()
}

val locationPermissionsList = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

fun moveCameraToLocation(coroutineScope: CoroutineScope, map: MapView, location: LatLng) {
    coroutineScope.launch {
        val googleMap = map.awaitMap()
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                DEFAULT_ZOOM
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

const val DEFAULT_ZOOM = 15f