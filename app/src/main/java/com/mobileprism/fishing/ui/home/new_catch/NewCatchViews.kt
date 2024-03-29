package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.custom.DefaultDialog
import com.mobileprism.fishing.ui.custom.FishingOutlinedTextField
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.views.DatePickerDialog
import com.mobileprism.fishing.ui.home.views.TimePickerDialog
import com.mobileprism.fishing.ui.home.weather.navigateToAddNewPlace
import com.mobileprism.fishing.utils.roundTo
import com.mobileprism.fishing.utils.time.TimeConstants
import com.mobileprism.fishing.utils.time.toDate
import com.mobileprism.fishing.utils.time.toTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


@Composable
fun FishSpecies(
    modifier: Modifier = Modifier,
    name: String,
    onNameChange: (String) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        FishingOutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = stringResource(R.string.fish_species),
            textFieldModifier = Modifier.fillMaxWidth(),
            isError = name.isBlank(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.size(2.dp))
        Text(
            stringResource(R.string.required), fontSize = 12.sp, modifier = Modifier.align(
                Alignment.End
            )
        )
    }
}


@Composable
fun NewCatchNoPlaceDialog(
    navController: NavController
) {
    DefaultDialog(
        primaryText = stringResource(R.string.no_places_added),
        secondaryText = stringResource(R.string.add_location_dialog),
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeClick = { navController.popBackStack() },
        positiveButtonText = stringResource(id = R.string.add),
        onPositiveClick = { navigateToAddNewPlace(navController) },
        onDismiss = { navController.popBackStack() },
        content = {
            LottieNoPlaces(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    )
}

@Composable
fun LottieNoPlaces(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_loaction))
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NewCatchPlaceSelectView(
    modifier: Modifier = Modifier,
    marker: UserMapMarker?,
    markersList: NewCatchPlacesState,
    isLocationLocked: Boolean,
    onNewPlaceSelected: (UserMapMarker) -> Unit,
    onInputError: (Boolean) -> Unit
) {

    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    var isDropMenuOpen by rememberSaveable { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        when (isDropMenuOpen) {
            true -> 180f
            else -> 0f
        }
    )

    var textFieldValue by rememberSaveable {
        mutableStateOf(marker?.title ?: "")
    }

    val suggestions = remember { mutableStateListOf<UserMapMarker>() }

    LaunchedEffect(key1 = markersList) {
        markersList.let { state ->
            when (state) {
                is NewCatchPlacesState.NotReceived -> {
                }
                is NewCatchPlacesState.Received -> {
                    if (state.locations.isEmpty()) {
//                        NewCatchNoPlaceDialog(navController)
                    } else {
                        suggestions.apply {
                            clear()
                            addAll(state.locations)
                        }
                    }
                }
            }
        }
    }

    val filteredList by rememberSaveable { mutableStateOf(suggestions.toMutableList()) }
    if (textFieldValue == "") searchFor("", suggestions, filteredList)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (isLocationLocked) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                label = { Text(text = stringResource(R.string.place)) },
                value = marker?.title ?: "",
                onValueChange = { },
                trailingIcon = {
                    IconButton(
                        onClick = { SnackbarManager.showMessage(R.string.another_place_in_new_catch) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.locked),
                            tint = MaterialTheme.colors.primary,
                        )
                    }
                }
            )
        } else {
            OutlinedTextField(
                readOnly = false,
                singleLine = true,
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    if (suggestions.isNotEmpty()) {
                        searchFor(textFieldValue, suggestions, filteredList)
                        if (!isDropMenuOpen) isDropMenuOpen = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        isDropMenuOpen = it.isFocused
                    },
                label = { Text(text = stringResource(R.string.place)) },
                trailingIcon = {
                    if (textFieldValue.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                textFieldValue = ""
                                isDropMenuOpen = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = Icons.Default.Close.name,
                                tint = MaterialTheme.colors.primary
                            )
                        }

                    } else {
                        IconButton(
                            onClick = {
                                if (!isDropMenuOpen) isDropMenuOpen = true
                            }
                        ) {
                            Icon(
                                modifier = Modifier.rotate(arrowRotation),
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = Icons.Default.KeyboardArrowDown.name,
                                tint = MaterialTheme.colors.primary
                            )
                        }
                    }
                },
                isError = !isThatPlaceInList(
                    textFieldValue,
                    suggestions
                ).apply { onInputError(this) }
            )
        }

        DropdownMenu(
            modifier = Modifier
                .wrapContentWidth(),
            expanded = isDropMenuOpen && suggestions.isNotEmpty(),
            onDismissRequest = {
                coroutineScope.launch {
                    delay(100)
                    if (isDropMenuOpen) isDropMenuOpen = false
                }
            },
            properties = PopupProperties(focusable = false)
        ) {
            filteredList.forEach { suggestion ->
                DropdownMenuItem(
                    onClick = {
                        textFieldValue = suggestion.title
                        onNewPlaceSelected(suggestion)
                        keyboard?.hide()
                        isDropMenuOpen = false
                    }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = Icons.Default.LocationOn.name,
                            tint = Color(suggestion.markerColor)
                        )
                        Text(text = suggestion.title)
                    }
                }
            }
        }
    }
}

@Composable
fun DateAndTimeItem(
    modifier: Modifier = Modifier,
    dateTime: Long,
    onDateChange: (Long) -> Unit,
) {
    var dateSetState by remember { mutableStateOf(false) }
    var timeSetState by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (dateSetState) {
        DatePickerDialog(
            context = context,
            initialDate = dateTime,
            minDate = Date().time - (TimeConstants.MILLISECONDS_IN_DAY * 5),
            onDateChange = onDateChange
        ) {
            dateSetState = false
        }
    }

    if (timeSetState) {
        TimePickerDialog(context = context, initialTime = dateTime, onTimeChange = onDateChange) {
            timeSetState = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = dateTime.toDate(),
            onValueChange = {},
            label = { Text(text = stringResource(R.string.date)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    dateSetState = true
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_event_24),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = stringResource(R.string.date)
                    )
                }

            })
        OutlinedTextField(
            value = dateTime.toTime(),
            onValueChange = {},
            label = { Text(text = stringResource(R.string.time)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    timeSetState = true
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_access_time_24),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = stringResource(R.string.time)
                    )
                }

            })
    }
}

@Composable
fun FishAmountAndWeightViewItem(
    modifier: Modifier = Modifier,
    amountState: Int,
    weightState: Double,
    onAmountChange: (Int) -> Unit,
    onWeightChange: (Double) -> Unit
) {
    Row(modifier = modifier) {
        Column(Modifier.weight(1F)) {
            OutlinedTextField(
                value = amountState.toString(),
                onValueChange = {
                    if (it.isEmpty()) onAmountChange(it.toInt())
                    else {
                        when (it.toIntOrNull()) {
                            null -> onAmountChange(amountState) //old value
                            else -> onAmountChange(it.toInt())   //new value
                        }
                    }
                },
                isError = amountState.toString().isEmpty(),
                label = { Text(text = stringResource(R.string.amount)) },
                trailingIcon = { Text(stringResource(R.string.pc)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.size(6.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        if (amountState >= 1 && amountState.toString().isNotBlank()) {
                            onAmountChange(amountState - 1)
                        }

                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_minus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                OutlinedButton(
                    onClick = {
                        if (amountState.toString().isEmpty()) onAmountChange(1)
                        else onAmountChange((amountState + 1))
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_plus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(6.dp))
        Column(Modifier.weight(1F)) {
            OutlinedTextField(
                value = weightState.toString(),
                onValueChange = {
                    if (it.isEmpty()) onWeightChange(it.toDouble())
                    else {
                        when (it.toDoubleOrNull()) {
                            null -> onWeightChange(weightState) //old value
                            else -> onWeightChange(it.toDouble())   //new value
                        }
                    }
                },
                label = { Text(text = stringResource(R.string.weight)) },
                trailingIcon = {
                    Text(stringResource(R.string.kg))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.size(6.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        if (weightState >= 0.1 && weightState.toString().isNotBlank()) {
                            onWeightChange((weightState - 0.1).roundTo(1))
                        }
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_minus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                OutlinedButton(
                    onClick = {
                        if (weightState.toString().isEmpty()) onWeightChange(
                            0.1.roundTo(1)
                        )
                        else onWeightChange(
                            (weightState + 0.1).roundTo(1)
                        )
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_plus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
            }
        }
    }
}

@Composable
fun WayOfFishingView(
    modifier: Modifier = Modifier,
    rodState: String,
    biteState: String,
    lureState: String,
    onRodChange: (String) -> Unit,
    onBiteChange: (String) -> Unit,
    onLureChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = rodState,
            onValueChange = { onRodChange(it) },
            label = { Text(text = stringResource(R.string.fish_rod)) }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = biteState,
            onValueChange = { onBiteChange(it) },
            label = { Text(text = stringResource(R.string.bait)) }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = lureState,
            onValueChange = { onLureChange(it) },
            label = { Text(text = stringResource(R.string.lure)) }
        )
    }
}

