package com.joesemper.fishing.compose.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import java.lang.Exception

@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun Map(
    onSnackClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val mapView = rememberMapViewWithLifecycle()
    val viewModel: MapViewModel = getViewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    val scaffoldState = rememberBottomSheetScaffoldState(
        //bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val lastKnownLocation = remember {
        getCurrentLocation(context = context, permissionsState = permissionsState)
    }

    val currentMarker = remember {
        mutableStateOf<UserMapMarker?>(null)
    }

    val currentGoogleMap = remember {
        mutableStateOf<GoogleMap?>(null)
    }
    val currentPosition = remember {
        mutableStateOf<LatLng?>(null)
    }

    var placeSelectMode by remember {
        mutableStateOf(false)
    }

    var uiState: MapUiState by remember {
        mutableStateOf(MapUiState.NormalMode)
    }

    ModalBottomSheetLayout(sheetContent = { BottomSheetAddMarkerDialog(currentGoogleMap, currentPosition, modalBottomSheetState) },
        sheetState = modalBottomSheetState, ) {


        BottomSheetScaffold(
            modifier = modifier.fillMaxSize(),
            scaffoldState = scaffoldState,
            sheetContent = {
                BottomSheetMarkerDialog(currentMarker.value, navController)
            },
            drawerGesturesEnabled = true,
            sheetPeekHeight = 0.dp,
            floatingActionButton = {
                FubOnMap(
                    state = uiState,
                    onClick = {
                        when (uiState) {
                            MapUiState.NormalMode -> {
                                moveCameraToLocation(
                                    coroutineScope = coroutineScope,
                                    map = mapView,
                                    location = lastKnownLocation.value
                                )
                                coroutineScope.launch {
                                    Toast.makeText(
                                        context,
                                        "Place select mode on",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                placeSelectMode = !placeSelectMode
                            }
                            MapUiState.PlaceSelectMode -> {
                                coroutineScope.launch {
                                    Toast.makeText(
                                        context,
                                        "Place select mode off",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                mapView.getMapAsync { googleMap ->
                                    val target = googleMap.cameraPosition.target
                                    currentPosition.value = LatLng(target.latitude, target.longitude)
                                    coroutineScope.launch {
                                        modalBottomSheetState.show()
                                    }

                                }
                                placeSelectMode = !placeSelectMode
                            }
                            MapUiState.BottomSheetAddMode -> {
                                coroutineScope.launch {
                                    Toast.makeText(
                                        context,
                                        "Add New Catch",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    })
            },
            floatingActionButtonPosition = FabPosition.End,
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (permissionDialog, mapLayout, pointer) = createRefs()

                var cameraMoveState: CameraMoveState by remember {
                    mutableStateOf(CameraMoveState.MoveFinish)
                }

                uiState = when {
                    scaffoldState.bottomSheetState.isExpanded -> MapUiState.BottomSheetInfoMode
                    placeSelectMode -> MapUiState.PlaceSelectMode
                    modalBottomSheetState.isVisible -> MapUiState.BottomSheetAddMode
                    else -> MapUiState.NormalMode
                }

                PermissionDialog(modifier = Modifier.constrainAs(permissionDialog) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                }, permissionsState = permissionsState)

                GoogleMapLayout(
                    modifier = Modifier.constrainAs(mapLayout) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    },
                    map = mapView,
                    permissionsState = permissionsState,
                    viewModel = viewModel,
                    onMarkerClick = { marker ->
                        currentMarker.value = marker
                        coroutineScope.launch {
                            moveCameraToLocation(
                                location = LatLng(marker.latitude, marker.longitude),
                                coroutineScope = coroutineScope,
                                map = mapView
                            )
                            uiState = MapUiState.BottomSheetInfoMode
                            scaffoldState.bottomSheetState.expand()

                        }
                    },
                    cameraMoveCallback = { state ->
                        cameraMoveState = state
                    }
                )

                if (uiState is MapUiState.PlaceSelectMode) {
                    PointerIcon(modifier = Modifier.constrainAs(pointer) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    }, cameraMoveState = cameraMoveState)
                }
            }
        }
    }
}

/*private fun addMarkerOnMap(marker: UserMapMarker) {
    val latLng = com.google.android.gms.maps.model.LatLng(marker.latitude, marker.longitude)
    val mapMarker = map.addMarker(
        com.google.android.gms.maps.model.MarkerOptions()
            .position(latLng)
            .title(marker.title)
    )
    mapMarker?.tag = marker.id
}*/


@ExperimentalMaterialApi
@Composable
fun BottomSheetAddMarkerDialog(
    currentGoogleMap: MutableState<GoogleMap?>,
    currentPosition: MutableState<LatLng?>,
    modalBottomSheetState: ModalBottomSheetState
) {
    val viewModel = get<MapViewModel>()
    val progressBar = remember { mutableStateOf(false) }
    Spacer(modifier = Modifier.size(1.dp))

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        if (progressBar.value) {
            Surface {
                CircularProgressIndicator()
            }
        }
        val coroutineScope = rememberCoroutineScope()
        val (name, locationIcon, title, description, saveButton, cancelButton) = createRefs()
        Text(
            text = "Новая точка",
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.constrainAs(name) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight)
            }
        )
        val titleValue = remember { mutableStateOf("") }
        val descriptionValue = remember { mutableStateOf("") }
        OutlinedTextField(
            value = titleValue.value,
            onValueChange = {
                titleValue.value = it

            },
            label = { Text(text = "Название") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.constrainAs(title) {
                top.linkTo(name.bottom, 2.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 2.dp)
                absoluteRight.linkTo(parent.absoluteRight, 2.dp)
            }
        )
        OutlinedTextField(
            value = descriptionValue.value,
            onValueChange = {
                descriptionValue.value = it

            },
            label = { Text(text = "Описание") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.constrainAs(description) {
                top.linkTo(title.bottom, 2.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight, 4.dp)
            }
        )

        /*Icon(
            painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
            contentDescription = "Marker",
            tint = secondaryFigmaColor,
            modifier = Modifier
                .size(32.dp)
                .constrainAs(locationIcon) {
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                    top.linkTo(title.top)
                    bottom.linkTo(title.bottom)
                }
        )*/



        OutlinedButton(modifier = Modifier.constrainAs(cancelButton) {
            absoluteRight.linkTo(parent.absoluteRight, 16.dp)
            top.linkTo(description.bottom, 8.dp)
            bottom.linkTo(parent.bottom, 16.dp)
        },
            shape = RoundedCornerShape(24.dp), onClick = { coroutineScope.launch {
                modalBottomSheetState.hide()
            } }) {
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*Icon(
                    painterResource(id = R.drawable.ic_baseline_shortcut_24),
                    "",
                    modifier = Modifier.size(24.dp)
                )*/
                Text(
                    stringResource(id = R.string.cancel),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Button(modifier = Modifier.constrainAs(saveButton) {
            absoluteRight.linkTo(cancelButton.absoluteLeft, 8.dp)
            top.linkTo(cancelButton.top)
            bottom.linkTo(cancelButton.bottom)
        }, shape = RoundedCornerShape(24.dp), onClick = {progressBar.value = true
            viewModel.addNewMarker(RawMapMarker
                (titleValue.value, descriptionValue.value, currentPosition.value?.latitude ?: 0.0,
                currentPosition.value?.longitude ?: 0.0))
            /*saveNewMarker(titleValue, descriptionValue, currentGoogleMap, currentPosition)*/ }) {
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*Icon(
                    painterResource(id = R.drawable.ic_baseline_navigation_24),
                    "",
                    modifier = Modifier.size(24.dp)
                )*/
                Text(
                    stringResource(id = R.string.save),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

fun saveNewMarker(
    titleValue: MutableState<String>,
    descriptionValue: MutableState<String>,
    currentGoogleMap: MutableState<GoogleMap?>,
    currentPosition: MutableState<LatLng?>
) {
    currentGoogleMap.value?.addMarker {
        currentPosition.value
    }
}


@ExperimentalMaterialApi
@Composable
fun BottomSheetMarkerDialog(marker: UserMapMarker?, navController: NavController) {

    Spacer(modifier = Modifier.size(2.dp))
    marker?.let {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            val (locationIcon, title, description, navigateButton, detailsButton) = createRefs()
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                contentDescription = "Marker",
                tint = secondaryFigmaColor,
                modifier = Modifier
                    .size(32.dp)
                    .constrainAs(locationIcon) {
                        absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                        top.linkTo(title.top)
                        bottom.linkTo(title.bottom)
                    }
            )

            Text(
                text = marker.title,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(parent.top, 16.dp)
                    absoluteLeft.linkTo(locationIcon.absoluteRight, 8.dp)
                }
            )

            Text(
                text = if (marker.description.isNullOrEmpty()) {
                    "No description"
                } else {
                    marker.description!!
                },
                style = MaterialTheme.typography.body1,
                modifier = Modifier.constrainAs(description) {
                    absoluteLeft.linkTo(title.absoluteLeft)
                    top.linkTo(title.bottom, 4.dp)
                }
            )

            Button(modifier = Modifier.constrainAs(detailsButton) {
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                top.linkTo(description.bottom, 8.dp)
                bottom.linkTo(parent.bottom, 16.dp)
            },
                shape = RoundedCornerShape(24.dp), onClick = { detailsOnClick(navController, it) }) {
                Row(
                    modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_baseline_shortcut_24),
                        "",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        stringResource(id = R.string.details),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            OutlinedButton(modifier = Modifier.constrainAs(navigateButton) {
                absoluteRight.linkTo(detailsButton.absoluteLeft, 8.dp)
                top.linkTo(detailsButton.top)
                bottom.linkTo(detailsButton.bottom)
            }, shape = RoundedCornerShape(24.dp), onClick = { /*TODO*/ }) {
                Row(
                    modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_baseline_navigation_24),
                        "",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        stringResource(id = R.string.navigate),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

fun detailsOnClick(navController: NavController, userMarker: UserMapMarker) {
    navController.currentBackStackEntry?.arguments?.putParcelable(Arguments.PLACE, userMarker)
    navController.navigate(MainDestinations.PLACE_ROUTE)
}

@ExperimentalPermissionsApi
@Composable
fun GoogleMapLayout(
    modifier: Modifier,
    map: MapView,
    viewModel: MapViewModel,
    permissionsState: MultiplePermissionsState,
    onMarkerClick: (marker: UserMapMarker) -> Unit,
    cameraMoveCallback: (state: CameraMoveState) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val markers = viewModel.getAllMarkers().collectAsState()

    AndroidView(
        { map },
        modifier = modifier.zIndex(-1.0f)
    ) { mapView ->
        coroutineScope.launch {
            val googleMap = mapView.awaitMap()
            markers.value.forEach {
                val position = LatLng(it.latitude, it.longitude)
                val marker = googleMap
                    .addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(it.title)
                    )
                marker.tag = it.id
            }
            googleMap.setOnCameraMoveStartedListener {
                cameraMoveCallback(CameraMoveState.MoveStart)
            }
            googleMap.setOnCameraIdleListener {
                cameraMoveCallback(CameraMoveState.MoveFinish)
            }

        }
    }

    LaunchedEffect(map) {
        val googleMap = map.awaitMap()
        checkPermission(context)
        googleMap.isMyLocationEnabled = permissionsState.allPermissionsGranted
        googleMap.setOnMarkerClickListener { marker ->
            val mapMarker = markers.value.first { it.id == marker.tag }
            onMarkerClick(mapMarker)
            true
        }
    }
}

@Composable
fun PointerIcon(cameraMoveState: CameraMoveState, modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.another_marker))
    val lottieAnimatable = rememberLottieAnimatable()
    var minMaxFrame by remember {
        mutableStateOf(LottieClipSpec.Frame(50, 82))
    }

    when (cameraMoveState) {
        CameraMoveState.MoveFinish -> {
            minMaxFrame = LottieClipSpec.Frame(50, 82).also { Log.d("MAP", "MoveStart") }
            LaunchedEffect(Unit) {
                lottieAnimatable.animate(
                    composition,
                    iteration = 1,
                    clipSpec = minMaxFrame,
                )
            }
        }
        CameraMoveState.MoveStart -> {
            minMaxFrame = LottieClipSpec.Frame(0, 50).also { Log.d("MAP", "MoveStart") }
            LaunchedEffect(Unit) {
                lottieAnimatable.animate(
                    composition,
                    iteration = 1,
                    clipSpec = minMaxFrame,
                )
            }

        }
    }

    LottieAnimation(
        modifier = modifier.size(128.dp),
        composition = composition,
        progress = lottieAnimatable.progress
    )
}

@ExperimentalMaterialApi
@Composable
fun FubOnMap(state: MapUiState, onClick: () -> Unit) {

    val fabImg = remember {
        mutableStateOf(R.drawable.ic_baseline_add_location_24)
    }

    val padding = remember {
        mutableStateOf(128.dp)
    }

    when (state) {
        MapUiState.BottomSheetAddMode -> {
            fabImg.value = R.drawable.ic_add_catch
            padding.value = 15.dp
        }
        MapUiState.NormalMode -> {
            fabImg.value = R.drawable.ic_baseline_add_location_24
            padding.value = 128.dp
        }
        MapUiState.PlaceSelectMode -> {
            fabImg.value = R.drawable.ic_baseline_check_24
            padding.value = 128.dp
        }
    }

    FloatingActionButton(
        modifier = Modifier
            .animateContentSize()
            .padding(
                bottom = padding.value
            ),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = fabImg.value),
            contentDescription = "Add new location",
            tint = Color.White,
        )
    }
}

@ExperimentalPermissionsApi
@Composable
fun PermissionDialog(modifier: Modifier, permissionsState: MultiplePermissionsState) {
    PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            Card(
                modifier = modifier
                    .wrapContentSize()
                    .padding(8.dp)
                    .zIndex(1.0f)
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(8.dp)
                ) {
                    Text("The location is important for this app. \nPlease grant the permission.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                            Text("Ok!")
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }

        },
        permissionsNotAvailableContent = {
            Text("The location is not available")
        }) {
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val viewModel: MapViewModel = getViewModel()
    val context = LocalContext.current
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

@Composable
fun IconButton(image: Painter, name: String, click: () -> Unit, modifier: Modifier) {
    OutlinedButton(
        onClick = click,
        modifier = modifier
            .wrapContentSize()
            .padding(4.dp),
        content = {
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(image, name, modifier = Modifier.size(25.dp))
                Text(name, modifier = Modifier.padding(start = 10.dp))
            }
        })
}

@ExperimentalPermissionsApi
fun getCurrentLocation(context: Context, permissionsState: MultiplePermissionsState) = runBlocking {
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
            }

        }
    }
    result
}

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

private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
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

sealed class CameraMoveState {
    object MoveStart : CameraMoveState()
    object MoveFinish : CameraMoveState()
}

val locationPermissionsList = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

const val DEFAULT_ZOOM = 15f



