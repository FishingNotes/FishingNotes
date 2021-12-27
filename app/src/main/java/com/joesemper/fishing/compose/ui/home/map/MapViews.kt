package com.joesemper.fishing.compose.ui.home.map

import android.location.Geocoder
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.views.DefaultDialog
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaColor
import com.joesemper.fishing.compose.ui.theme.supportTextColor
import com.joesemper.fishing.compose.ui.utils.currentFraction
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.vponomarenko.compose.shimmer.shimmer
import org.koin.androidx.compose.getViewModel

@ExperimentalMaterialApi
@Composable
fun MapScaffold(
    mapUiState: MapUiState,
    currentPlace: MutableState<UserMapMarker?>,
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState,
    fab: @Composable() (() -> Unit)?,
    bottomSheet: @Composable() (ColumnScope.() -> Unit),
    content: @Composable (PaddingValues) -> Unit,

    ) {
    val dp = animateDpAsState(
        when (mapUiState) {
            is MapUiState.BottomSheetInfoMode,
            MapUiState.BottomSheetFullyExpanded -> 158.dp
            else -> 0.dp
        }
    )

    val sheetColor = animateColorAsState(
        if (scaffoldState.currentFraction != 1f) {
            MaterialTheme.colors.surface.copy(0f)
        } else MaterialTheme.colors.surface.copy(scaffoldState.currentFraction)

    )

    val radius = (30 * (1f - scaffoldState.currentFraction)).dp

    BottomSheetScaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetBackgroundColor = sheetColor.value,
        sheetElevation = 0.dp,
        sheetShape = RoundedCornerShape(radius),
        sheetPeekHeight = dp.value,
        floatingActionButton = fab,
        sheetContent = bottomSheet,
        sheetGesturesEnabled = scaffoldState.currentFraction != 1f,
        content = content
    )
}

@ExperimentalPermissionsApi
@Composable
fun MyLocationButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = onClick
        ) {
            Icon(Icons.Default.MyLocation, stringResource(R.string.my_location))
        }
    }
}

@Composable
fun MapLayersButton(layersSelectionMode: MutableState<Boolean>, modifier: Modifier) {
    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconButton(modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
            onClick = { layersSelectionMode.value = true }) {
            Icon(painterResource(R.drawable.ic_baseline_layers_24), stringResource(R.string.layers))
        }
    }
}

@Composable
fun LayersView(
    mapLayersSelection: MutableState<Boolean>,
    mapType: MutableState<Int>
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(250.dp)
            .wrapContentHeight()

    ) {
        Column(
            modifier = Modifier
                .padding(2.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.map_type))
                Card(shape = CircleShape, modifier = Modifier.size(20.dp)) {
                    IconButton(onClick = { mapLayersSelection.value = false }) {
                        Icon(Icons.Default.Close, stringResource(R.string.close))
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                MapLayerItem(
                    mapType,
                    layer = MapTypes.roadmap,
                    painter = painterResource(R.drawable.ic_map_default),
                    name = stringResource(R.string.roadmap)
                )
                MapLayerItem(
                    mapType,
                    layer = MapTypes.hybrid,
                    painter = painterResource(R.drawable.ic_map_satellite),
                    name = stringResource(R.string.satellite)
                )
                MapLayerItem(
                    mapType,
                    layer = MapTypes.terrain,
                    painter = painterResource(R.drawable.ic_map_terrain),
                    name = stringResource(R.string.terrain)
                )
            }
        }
    }
}

@Composable
fun MapLayerItem(mapType: MutableState<Int>, layer: Int, painter: Painter, name: String) {
    val animatedColor by animateColorAsState(
        if (mapType.value == layer) Color.Blue else Color.White,
        animationSpec = tween(300)
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        IconToggleButton(
            onCheckedChange = { if (it) mapType.value = layer },
            checked = mapType.value == layer,
            modifier = if (mapType.value == layer) Modifier
                .size(70.dp)
                .border(
                    width = 2.dp,
                    color = animatedColor,
                    shape = RoundedCornerShape(15.dp)
                ) else Modifier
                .size(70.dp)
                .padding(0.dp)
        ) {
            Image(
                painter, layer.toString(),
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Text(text = name, fontSize = 12.sp, overflow = TextOverflow.Ellipsis, maxLines = 1)
    }
}

@Composable
fun MapFilterButton(
    modifier: Modifier, showHiddenPlaces: Boolean,
    onCLick: (Boolean) -> Unit,
) {


    val color = animateColorAsState(
        targetValue = if (showHiddenPlaces) {
            MaterialTheme.colors.onSurface
        } else {
            supportTextColor
        },
        animationSpec = tween(800)
    )

    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconToggleButton(checked = showHiddenPlaces,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onCheckedChange = { onCLick(it) }) {
            Icon(Icons.Default.Visibility, Icons.Default.Visibility.name,
                tint = color.value)
        }
    }
}

@Composable
fun PointerIcon(
    pointerState: MutableState<PointerState>,
    modifier: Modifier = Modifier,
) {
    var isFirstTimeCalled by remember { mutableStateOf(false) }

    val darkTheme = isSystemInDarkTheme()
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(if (darkTheme) R.raw.marker_night else R.raw.marker)
    )
    val lottieAnimatable = rememberLottieAnimatable()

    val startMinMaxFrame by remember {
        mutableStateOf(LottieClipSpec.Frame(0, 50))
    }
    val finishMinMaxFrame by remember {
        mutableStateOf(LottieClipSpec.Frame(50, 82))
    }

    LaunchedEffect(isFirstTimeCalled) {
        lottieAnimatable.animate(
            composition,
            iteration = 1,
            continueFromPreviousAnimate = true,
            clipSpec = startMinMaxFrame,
        )
    }

    LaunchedEffect(pointerState.value) {
        if (pointerState.value == PointerState.ShowMarker) {
            lottieAnimatable.animate(
                composition,
                iteration = 1,
                continueFromPreviousAnimate = true,
                clipSpec = startMinMaxFrame,
            )
        } else {
            lottieAnimatable.animate(
                composition,
                iteration = 1,
                continueFromPreviousAnimate = false,
                clipSpec = finishMinMaxFrame,
            )
        }
    }

    LottieAnimation(
        modifier = modifier.size(128.dp),
        composition = composition,
        progress = lottieAnimatable.progress
    )

    isFirstTimeCalled = true

}

@Composable
fun FishLoading(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fish_loading))
    val progress by animateLottieCompositionAsState(composition)
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun PlaceTileView(
    modifier: Modifier,
    cameraMoveState: CameraMoveState,
    currentCameraPosition: MutableState<Pair<LatLng, Float>>,
    pointerState: MutableState<PointerState>
) {
    val context = LocalContext.current
    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()
    val geocoder = Geocoder(context)
    var selectedPlace by remember { mutableStateOf<String?>(null) }

    val unnamedPlaceStr = stringResource(R.string.unnamed_place)
    val cantRecognizePlace = stringResource(R.string.cant_recognize_place)

    when (cameraMoveState) {
        CameraMoveState.MoveStart -> {
            pointerState.value = PointerState.ShowMarker
            selectedPlace = null
            viewModel.chosenPlace.value = null
            viewModel.showMarker.value = false
        }
        CameraMoveState.MoveFinish -> {
            LaunchedEffect(cameraMoveState) {
                delay(1200)
                coroutineScope.launch(Dispatchers.Default) {
                    try {
                        val position = geocoder.getFromLocation(
                            currentCameraPosition.component1().first.latitude,
                            currentCameraPosition.component1().first.longitude,
                            1
                        )
                        position?.first()?.let { address ->
                            viewModel.showMarker.value = true
                            if (!address.subAdminArea.isNullOrBlank()) {
                                viewModel.chosenPlace.value =
                                    address.subAdminArea.replaceFirstChar { it.uppercase() }
                            } else if (!address.adminArea.isNullOrBlank()) {
                                viewModel.chosenPlace.value = address.adminArea
                                    .replaceFirstChar { it.uppercase() }
                            } else viewModel.chosenPlace.value = "Место без названия"
                        }
                    } catch (e: Throwable) {
                        viewModel.chosenPlace.value = "Не удалось определить место"
                    }
                    pointerState.value = PointerState.HideMarker
                    selectedPlace = viewModel.chosenPlace.value
                }
            }
        }
    }

    val placeName = viewModel.chosenPlace.value ?: stringResource(R.string.searching)
    val pointerIconColor by animateColorAsState(
        if (selectedPlace != null) secondaryFigmaColor
        else Color.LightGray
    )
    val textColor by animateColorAsState(
        if (selectedPlace != null) MaterialTheme.colors.onSurface
        else Color.LightGray
    )
    val shimmerModifier = if (viewModel.chosenPlace.value != null) Modifier else Modifier.shimmer()

    Card(
        shape = RoundedCornerShape(size = 20.dp),
        modifier = modifier
            .heightIn(min = 40.dp, max = 80.dp)
            .widthIn(max = 240.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                contentDescription = stringResource(R.string.marker_icon),
                tint = pointerIconColor,
                modifier = Modifier
                    .size(30.dp)
            )
            Spacer(Modifier.size(4.dp))
            Text(
                placeName,
                overflow = TextOverflow.Ellipsis,
                color = textColor,
                modifier = shimmerModifier.padding(end = 2.dp)
            )
            Spacer(Modifier.size(4.dp))
        }
    }
}

@ExperimentalComposeUiApi
@OptIn(ExperimentalAnimationApi::class)
@Composable
@ExperimentalPermissionsApi
fun GrantLocationPermissionsDialog(
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit,
    onDontAskClick: () -> Unit
) {

    DefaultDialog(
        primaryText = stringResource(R.string.location_permission_dialog),
        neutralButtonText = stringResource(id = R.string.dont_ask_again),
        onNeutralClick = onDontAskClick,
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeClick = onNegativeClick,
        positiveButtonText = stringResource(id = R.string.ok_button),
        onPositiveClick = onPositiveClick,
        onDismiss = onDismiss,
        content = {
            LottieMyLocation(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    )
}

@Composable
fun LottieMyLocation(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.my_location))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@Composable
fun BottomSheetLine(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 25.dp, height = 3.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {}
    }
}