package com.mobileprism.fishing.utils.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.map.LocationState
import com.mobileprism.fishing.utils.showToast
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class LocationManagerImpl(private val context: Context) : LocationManager {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val manager =
        context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

    companion object {
        val lastCoordinates = MutableStateFlow(LatLng(0.0, 0.0))
    }

    @SuppressLint("MissingPermission")
    @ExperimentalPermissionsApi
    fun getCurrentLocation(
        context: Context,
        permissionsState: MultiplePermissionsState,
    ) = runBlocking {

        checkLocationPermissions(context)

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

    @SuppressLint("MissingPermission")
    override fun getCurrentLocationFlow(): Flow<LocationState> = flow {
        val locationPermissionsGiven = checkLocationPermissions(context)
        when {
            manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                .not() -> {
                emit(LocationState.GpsNotEnabled)
            }
            locationPermissionsGiven -> {
                val locationResult = fusedLocationProviderClient.lastLocation.await()
                kotlin.runCatching {
                    val newCoordinates = LatLng(locationResult.latitude, locationResult.longitude)
                    //if (isCoordinatesFar(lastCoordinates.value, newCoordinates)) {
                    emit(LocationState.LocationGranted(newCoordinates))
                    lastCoordinates.value = newCoordinates
                    //}
                }.onFailure {
                    Log.d("MAP", "GPS is off")
                    context.showToast(context.getString(R.string.cant_get_current_location))
                }
            }
            else -> { emit(LocationState.NoPermission) }
        }
    }

    override fun checkGPSEnabled(activity: Activity, onGpsEnabled: () -> Unit) {
        if (manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER).not()) {
            SnackbarManager.showMessage(R.string.gps_is_off)
            turnOnGPS(activity, onGpsEnabled)
        } else onGpsEnabled()
    }

    private fun turnOnGPS(activity: Activity, onGpsEnabled: () -> Unit) {
        val request = LocationRequest.create().apply {
            interval = 8000
            fastestInterval = 5000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        activity,
                        /*REQUEST_CHECK_SETTINGS*/12345
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }.addOnSuccessListener {
            onGpsEnabled()
        }
    }


}

fun checkLocationPermissions(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
