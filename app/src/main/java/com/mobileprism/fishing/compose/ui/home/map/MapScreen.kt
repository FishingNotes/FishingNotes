package com.mobileprism.fishing.compose.ui.home.map

import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.maps.android.ktx.awaitMap
import com.mobileprism.fishing.R
import com.mobileprism.fishing.compose.ui.Arguments
import com.mobileprism.fishing.compose.ui.MainActivity
import com.mobileprism.fishing.compose.ui.MainDestinations
import com.mobileprism.fishing.compose.ui.home.SnackbarManager
import com.mobileprism.fishing.compose.ui.navigate
import com.mobileprism.fishing.compose.viewmodels.MapViewModel
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.raw.RawMapMarker
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.Constants.defaultFabBottomPadding
import com.mobileprism.fishing.utils.getCameraPosition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    upPress: () -> Unit,
    navController: NavController,
    addPlaceOnStart: Boolean = false,
    place: UserMapMarker?
) {
    val viewModel: MapViewModel = getViewModel()
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    val context = LocalContext.current

    var addingPlace by remember { mutableStateOf(addPlaceOnStart) }
    var chosenPlace: UserMapMarker? by remember { mutableStateOf(place) }

    val map = rememberMapViewWithLifecycle()

    chosenPlace?.let {
        if (it.id.isNotEmpty()) {
            viewModel.currentMarker.value = chosenPlace
        }
        viewModel.lastMapCameraPosition.value =
            Pair(LatLng(it.latitude, it.longitude), DEFAULT_ZOOM)
        chosenPlace = null
    }
    val coroutineScope = rememberCoroutineScope()
    val userPreferences: UserPreferences = get()
    val showHiddenPlaces by userPreferences.shouldShowHiddenPlacesOnMap.collectAsState(true)
    val useZoomButtons by userPreferences.useMapZoomButons.collectAsState(false)

    val scaffoldState = rememberBottomSheetScaffoldState()
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val dialogAddPlaceIsShowing = remember { mutableStateOf(false) }

    val mapLayersSelection = rememberSaveable { mutableStateOf(false) }
    val mapType = rememberSaveable { mutableStateOf(MapTypes.roadmap) }
    val mapBearing = remember { mutableStateOf(0f) }


    var mapUiState: MapUiState by remember {
        when {
            addingPlace -> mutableStateOf(MapUiState.PlaceSelectMode)
            viewModel.currentMarker.value != null -> {
                mutableStateOf(MapUiState.BottomSheetInfoMode)
            }
            else -> mutableStateOf(viewModel.mapUiState.value)
        }
    }

    val pointerState: MutableState<PointerState> = remember {
        mutableStateOf(PointerState.HideMarker)
    }

    val currentCameraPosition = remember { mutableStateOf(Pair(LatLng(0.0, 0.0), 0f)) }

    LaunchedEffect(mapUiState) {
        if (mapUiState != viewModel.mapUiState) {
            viewModel.mapUiState.value = mapUiState
            when (mapUiState) {
                is MapUiState.NormalMode -> {
                    viewModel.currentMarker.value = null
                    addingPlace = false
                }
                is MapUiState.BottomSheetInfoMode -> {
                    addingPlace = false
                }
                is MapUiState.PlaceSelectMode -> {
                }
                /*is MapUiState.BottomSheetFullyExpanded -> {
                    scaffoldState.bottomSheetState.expand()
                }*/
            }
        }
    }

    val currentLocationFlow = remember(permissionsState.allPermissionsGranted) {
        getCurrentLocationFlow(context, permissionsState)
    }

    LaunchedEffect(currentLocationFlow) {
        currentLocationFlow.collect { currentLocationState ->
            when (currentLocationState) {
                is LocationState.LocationGranted -> {
                    viewModel.lastKnownLocation.value = currentLocationState.location
                    if (viewModel.firstLaunchLocation.value) {
                        viewModel.currentMarker.value?.let {
                        } ?: kotlin.run {
                            viewModel.lastMapCameraPosition.value =
                                Pair(currentLocationState.location, DEFAULT_ZOOM)
                        }
                        viewModel.firstLaunchLocation.value = false
                    }
                }
                is LocationState.GpsNotEnabled -> {
                    checkGPSEnabled(context) //{ currentLocationFlow.collectAsState() }
                }
            }
        }
    }

    /*LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        viewModel.sheetState = scaffoldState.bottomSheetState.currentValue
        if (!addingPlace) {
            when (scaffoldState.bottomSheetState.currentValue) {
                BottomSheetValue.Collapsed -> if (viewModel.currentMarker.value != null &&
                    mapUiState == MapUiState.BottomSheetFullyExpanded
                )
                    mapUiState = MapUiState.BottomSheetInfoMode
                BottomSheetValue.Expanded -> if (mapUiState == MapUiState.BottomSheetInfoMode)
                    mapUiState = MapUiState.BottomSheetFullyExpanded
            }
        }
    }*/

    BackPressHandler(
        mapUiState = mapUiState,
        navController = navController,
    ) { mapUiState = MapUiState.NormalMode }

    val noNamePlace = stringResource(R.string.no_name_place)

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetShape = Constants.modalBottomSheetCorners,
        sheetContent = {
            MapModalBottomSheet(
                mapPreferences = userPreferences,
            )
        }
    ) {
        MapScaffold(
            mapUiState = mapUiState,
            scaffoldState = scaffoldState,
            fab = {
                MapFab(
                    state = mapUiState,
                    onClick = {
                        when (mapUiState) {
                            MapUiState.NormalMode -> {
                                mapUiState = MapUiState.PlaceSelectMode
                            }
                            MapUiState.PlaceSelectMode -> {
                                dialogAddPlaceIsShowing.value = true
                                mapUiState = MapUiState.NormalMode
                            }
                            MapUiState.BottomSheetInfoMode -> {
                                onAddNewCatchClick(navController, viewModel)
                            }
                        }
                    },
                    onLongPress = {
                        when (mapUiState) {
                            MapUiState.NormalMode -> {
                                viewModel.lastKnownLocation.value?.let {
                                    viewModel.addNewMarker(
                                        RawMapMarker(
                                            noNamePlace,
                                            latitude = it.latitude,
                                            longitude = it.longitude,
                                        )
                                    )
                                }
                            }
                        }
                    },
                    userSettings = userPreferences,
                )
            },
            bottomSheet = {
                MarkerInfoDialog(
                    receivedMarker = viewModel.currentMarker.value,
                    lastKnownLocation = viewModel.lastKnownLocation,
                    navController = navController,
                    mapBearing = mapBearing,
                    onMarkerIconClicked = {
                        viewModel.currentMarker.value?.let {
                            moveCameraToLocation(
                                coroutineScope, map,
                                LatLng(it.latitude, it.longitude), DEFAULT_ZOOM, mapBearing.value
                            )
                        }
                    }
                )
            }
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (mapLayout, addMarkerFragment, mapMyLocationButton,
                    mapCompassButton, mapLayersButton, zoomInButton, zoomOutButton,
                    mapSettingsButton, mapLayersView, pointer) = createRefs()
                val verticalMyLocationButtonGl = createGuidelineFromAbsoluteRight(56.dp)
                val centerHorizontal = createGuidelineFromBottom(0.5f)

                MapLayout(
                    modifier = Modifier.constrainAs(mapLayout) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    }, map = map,
                    onMarkerClick = {
                        viewModel.currentMarker.value = it
                        mapUiState = MapUiState.BottomSheetInfoMode
                    },
                    currentCameraPosition = currentCameraPosition,
                    mapType = mapType, mapBearing = mapBearing,
                    onMapClick = {
                        mapUiState = MapUiState.NormalMode
                    }
                )

                MapLayersButton(
                    modifier = Modifier.constrainAs(mapLayersButton) {
                        top.linkTo(parent.top, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    },
                    layersSelectionMode = mapLayersSelection,
                )

                if (mapLayersSelection.value)
                    Surface(modifier = Modifier
                        .fillMaxSize()
                        .alpha(0f)
                        .zIndex(4f)
                        .clickable { mapLayersSelection.value = false }) {}
                AnimatedVisibility(
                    mapLayersSelection.value,
                    enter = expandIn(
                        expandFrom = Alignment.TopStart,
                        animationSpec = tween(380)
                    ) + fadeIn(animationSpec = tween(480)),
                    exit = shrinkOut(
                        shrinkTowards = Alignment.TopStart,
                        animationSpec = tween(380)
                    ) + fadeOut(animationSpec = tween(480)),
                    modifier = Modifier
                        .constrainAs(mapLayersView) {
                            top.linkTo(parent.top, 16.dp)
                            absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                        }
                        .zIndex(5f)
                ) {
                    LayersView(mapLayersSelection, mapType)
                }

                MapSettingsButton(
                    modifier = Modifier.constrainAs(mapSettingsButton) {
                        top.linkTo(mapLayersButton.bottom, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    },
                ) {
                    coroutineScope.launch {
                        Firebase.analytics.logEvent("map_settings", null)
                        modalBottomSheetState.show()
                    }
                }


                MyLocationButton(
                    modifier = modifier.constrainAs(mapMyLocationButton) {
                        top.linkTo(parent.top, 16.dp)
                        absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                    },
                    lastKnownLocation = viewModel.lastKnownLocation,
                    userPreferences = userPreferences,
                ) {
                    viewModel.lastKnownLocation.value?.let {
                        viewModel.lastMapCameraPosition.value = getCameraPosition(it)
                    }
                }

                CompassButton(modifier = modifier.constrainAs(mapCompassButton) {
                    top.linkTo(mapMyLocationButton.bottom, 16.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                }, mapBearing = mapBearing) {
                    setCameraBearing(
                        coroutineScope, map,
                        currentCameraPosition.value.first,
                        currentCameraPosition.value.second
                    )
                }

                if (useZoomButtons) {
                    MapZoomInButton(
                        modifier = Modifier.constrainAs(zoomInButton) {
                            linkTo(parent.top, centerHorizontal, 4.dp, 4.dp, 1f)
                            linkTo(parent.absoluteLeft, parent.absoluteRight, 16.dp, 16.dp, 1f)
                        },
                    ) {
                        currentCameraPosition.value.let {
                            moveCameraToLocation(
                                coroutineScope,
                                map,
                                it.first,
                                it.second + 1f,
                                mapBearing.value
                            )
                        }

                    }

                    MapZoomOutButton(
                        modifier = Modifier.constrainAs(zoomOutButton) {
                            linkTo(centerHorizontal, parent.bottom, 4.dp, 4.dp, 0f)
                            linkTo(parent.absoluteLeft, parent.absoluteRight, 16.dp, 16.dp, 1f)
                        },
                    ) {
                        currentCameraPosition.value.let {
                            moveCameraToLocation(
                                coroutineScope,
                                map,
                                it.first,
                                it.second - 1f,
                                mapBearing.value
                            )
                        }
                    }
                }


                AnimatedVisibility(mapUiState == MapUiState.PlaceSelectMode,
                    modifier = Modifier.constrainAs(pointer) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom, 65.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    }) {
                    PointerIcon(pointerState)
                }

                AnimatedVisibility(mapUiState == MapUiState.PlaceSelectMode && !mapLayersSelection.value,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                    modifier = Modifier.constrainAs(addMarkerFragment) {
                        top.linkTo(parent.top, 16.dp)
                        absoluteLeft.linkTo(mapLayersButton.absoluteRight, 8.dp)
                        absoluteRight.linkTo(verticalMyLocationButtonGl, 8.dp)
                    }
                ) {
                    PlaceTileView(
                        modifier = Modifier.wrapContentSize(),
                        currentCameraPosition = currentCameraPosition,
                        pointerState = pointerState
                    )
                }

                if (dialogAddPlaceIsShowing.value)
                    Dialog(onDismissRequest = { dialogAddPlaceIsShowing.value = false }) {
                        NewPlaceDialog(
                            currentCameraPosition = currentCameraPosition,
                            dialogState = dialogAddPlaceIsShowing,
                            chosenPlace = viewModel.chosenPlace
                        )
                    }
            }
        }
    }
}


@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalPermissionsApi
@Composable
fun MapLayout(
    modifier: Modifier = Modifier,
    map: MapView,
    onMarkerClick: (marker: UserMapMarker) -> Unit,
    currentCameraPosition: MutableState<Pair<LatLng, Float>>,
    mapType: MutableState<Int>,
    mapBearing: MutableState<Float>,
    onMapClick: () -> Unit,
) {
    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()
    val darkTheme = isSystemInDarkTheme()
    val userPreferences: UserPreferences = get()
    val showHiddenPlaces by userPreferences.shouldShowHiddenPlacesOnMap.collectAsState(true)
    val context = LocalContext.current
    val markers by viewModel.mapMarkers.collectAsState()
    val markersToShow by remember(markers, showHiddenPlaces) {
        mutableStateOf(if (showHiddenPlaces) markers
        else markers.filter { it.visible })
    }

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

    var isMapVisible by remember {
        mutableStateOf(false)
    }

    AnimatedVisibility(
        visible = isMapVisible,
        enter = fadeIn(), exit = fadeOut()
    ) {
        AndroidView(
            { map },
            modifier = modifier
                .fillMaxSize()
                .zIndex(-1.0f)
        ) { mapView ->
            coroutineScope.launch {
                val googleMap = mapView.awaitMap()

                //Map styles: https://mapstyle.withgoogle.com
                when (darkTheme) {
                    true -> {
                        googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_fishing_night)
                        )
                    }
                    false -> {
                        googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_fishing)
                        )
                    }
                }

                googleMap.clear()
                markersToShow.forEach {
                    val position = LatLng(it.latitude, it.longitude)
                    val markerColor = Color(it.markerColor)
                    val hue = getHue(
                        red = markerColor.red,
                        green = markerColor.green,
                        blue = markerColor.blue
                    )
                    val marker = googleMap
                        .addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(it.title)
                                .icon(BitmapDescriptorFactory.defaultMarker(hue))

                        )
                    marker?.tag = it.id
                }
                googleMap.setOnCameraMoveStartedListener {
                    viewModel.setCameraMoveState(CameraMoveState.MoveStart)

                }
                googleMap.setOnCameraMoveListener {
                    mapBearing.value = googleMap.cameraPosition.bearing
                    currentCameraPosition.value =
                        Pair(googleMap.cameraPosition.target, googleMap.cameraPosition.zoom)
                }
                googleMap.setOnCameraIdleListener {
                    viewModel.setCameraMoveState(CameraMoveState.MoveFinish)
                    /*currentCameraPosition.value =
                        Pair(googleMap.cameraPosition.target, googleMap.cameraPosition.zoom)*/
                    //mapBearing.value = googleMap.cameraPosition.bearing
                }
                googleMap.setOnMarkerClickListener { marker ->
                    onMarkerClick(markers.first { it.id == marker.tag })
                    viewModel.lastMapCameraPosition.value = Pair(marker.position, DEFAULT_ZOOM)
                    true
                }
                googleMap.setOnMapClickListener {
                    onMapClick()
                    return@setOnMapClickListener
                }



                googleMap.uiSettings.isZoomGesturesEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = false
            }
        }
    }

    LaunchedEffect(map, permissionsState.allPermissionsGranted) {
        val googleMap = map.awaitMap()
        checkPermission(context)
        googleMap.isMyLocationEnabled = permissionsState.allPermissionsGranted
        isMapVisible = true
    }

    LaunchedEffect(viewModel.lastMapCameraPosition.value) {
        viewModel.lastMapCameraPosition.value?.let {
            moveCameraToLocation(this, map, it.first, it.second, mapBearing.value)
        }
    }

    LaunchedEffect(mapType.value) {
        val googleMap = map.awaitMap()
        googleMap.mapType = mapType.value
    }

    DisposableEffect(map) {
        onDispose {
            viewModel.lastMapCameraPosition.value = currentCameraPosition.value
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalPermissionsApi
@Composable
fun LocationPermissionDialog(
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences,
    onCloseCallback: () -> Unit = { },
) {
    val context = LocalContext.current
    var isDialogOpen by remember {
        mutableStateOf(true)
    }
    val coroutineScope = rememberCoroutineScope()

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            if (isDialogOpen) {
                GrantLocationPermissionsDialog(
                    onDismiss = {
                        isDialogOpen = false
                        onCloseCallback()
                    },
                    onNegativeClick = {
                        isDialogOpen = false
                        onCloseCallback()
                    },
                    onPositiveClick = {
                        isDialogOpen = false
                        permissionsState.launchMultiplePermissionRequest()
                        onCloseCallback()
                    },
                    onDontAskClick = {
                        isDialogOpen = false
                        SnackbarManager.showMessage(R.string.location_dont_ask)
                        coroutineScope.launch {
                            userPreferences.saveLocationPermissionStatus(false)
                        }
                        onCloseCallback()
                    }
                )
            }
        },
        permissionsNotAvailableContent = { onCloseCallback(); SnackbarManager.showMessage(R.string.location_permission_denied) })
    { checkPermission(context); }
}

@ExperimentalMaterialApi
@Composable
fun MapFab(
    state: MapUiState,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    userSettings: UserPreferences,
) {
    val useFastFabAdd by userSettings.useFabFastAdd.collectAsState(false)
    val fabImg = remember { mutableStateOf(R.drawable.ic_baseline_add_location_24) }

    val context = LocalContext.current

    val paddingBottom = animateDpAsState(
        when (state) {
            MapUiState.NormalMode -> {
                defaultFabBottomPadding
            }
            MapUiState.BottomSheetInfoMode -> {
                34.dp
            }
            MapUiState.PlaceSelectMode -> {
                defaultFabBottomPadding
            }
        }
    )

    val paddingTop = animateDpAsState(
        when (state) {
            MapUiState.NormalMode -> {
                0.dp
            }
            MapUiState.BottomSheetInfoMode -> {
                26.dp
            }
            MapUiState.PlaceSelectMode -> {
                0.dp
            }
        }
    )

    val adding_place = stringResource(R.string.adding_place_on_current_location)
    val permissions_required = stringResource(R.string.location_permissions_required)
    AnimatedVisibility(
        true,
        exit = fadeOut(),
        enter = fadeIn()
    ) {

        FishingFab(
            modifier = Modifier
                .animateContentSize()
                .padding(bottom = paddingBottom.value, top = paddingTop.value),
            onClick = onClick,
            onLongPress = {
                if (state == MapUiState.NormalMode && useFastFabAdd) {
                    if (!checkPermission(context)) {
                        Toast.makeText(context, adding_place, Toast.LENGTH_SHORT).show()
                        onLongPress()
                    } else Toast.makeText(context, permissions_required, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        ) {
            AnimatedVisibility(state is MapUiState.NormalMode) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_add_location_24),
                    contentDescription = stringResource(R.string.new_place),
                    tint = MaterialTheme.colors.onPrimary,
                )
            }
            AnimatedVisibility(state is MapUiState.BottomSheetInfoMode) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_catch),
                    contentDescription = stringResource(R.string.new_place),
                    tint = MaterialTheme.colors.onPrimary,
                )
            }
            AnimatedVisibility(state is MapUiState.PlaceSelectMode) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_check_24),
                    contentDescription = stringResource(R.string.new_place),
                    tint = MaterialTheme.colors.onPrimary,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FishingFab(
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    backgroundColor: Color = MaterialTheme.colors.secondary,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor.copy(alpha = 1f),

        elevation = elevation.elevation(interactionSource).value,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides contentColor.alpha) {
            ProvideTextStyle(MaterialTheme.typography.button) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = FabSize, minHeight = FabSize)
                        .combinedClickable(
                            interactionSource = interactionSource,
                            indication = rememberRipple(),
                            enabled = true,
                            role = Role.Button,
                            onClick = onClick,
                            onDoubleClick = { },
                            onLongClick = onLongPress
                        ),
                    contentAlignment = Alignment.Center
                ) { content() }
            }
        }
    }
}

private val FabSize = 56.dp

private fun onAddNewCatchClick(navController: NavController, viewModel: MapViewModel) {
    viewModel.currentMarker.value?.let {
        navController.navigate(
            MainDestinations.NEW_CATCH_ROUTE,
            Arguments.PLACE to it
        )
    }
}

private fun onMarkerDetailsClick(navController: NavController, marker: UserMapMarker) {
    navController.navigate(MainDestinations.PLACE_ROUTE, Arguments.PLACE to marker)
}