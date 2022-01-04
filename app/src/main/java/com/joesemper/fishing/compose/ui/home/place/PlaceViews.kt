package com.joesemper.fishing.compose.ui.home.place

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.catch_screen.EditNoteDialog
import com.joesemper.fishing.compose.ui.home.notes.*
import com.joesemper.fishing.compose.ui.home.views.*
import com.joesemper.fishing.compose.ui.theme.primaryTextColor
import com.joesemper.fishing.compose.ui.theme.supportTextColor
import com.joesemper.fishing.domain.UserPlaceViewModel
import com.joesemper.fishing.model.entity.common.Note
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.utils.Constants.bottomBannerPadding
import com.joesemper.fishing.utils.time.toDateTextMonth
import kotlinx.coroutines.launch

@Composable
fun PlaceTitleView(
    modifier: Modifier = Modifier,
    place: UserMapMarker,
    catchesAmount: Int,
) {

    ConstraintLayout(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        val (icon, title, fishIcon, navigateButton, date) = createRefs()

        Icon(
            modifier = Modifier
                .size(32.dp)
                .constrainAs(icon) {
                    top.linkTo(title.top)
                    bottom.linkTo(date.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                },
            painter = painterResource(R.drawable.ic_baseline_location_on_24),
            contentDescription = stringResource(R.string.place),
            tint = Color(place.markerColor)
        )

        HeaderText(
            modifier = Modifier.constrainAs(title) {
                absoluteLeft.linkTo(icon.absoluteRight, 8.dp)
                absoluteRight.linkTo(navigateButton.absoluteLeft, 8.dp)
                top.linkTo(parent.top, 16.dp)
                width = Dimension.fillToConstraints
            },
            text = place.title
        )

        DefaultIconButton(
            modifier = Modifier
                .size(32.dp)
                .constrainAs(navigateButton) {
                    top.linkTo(title.top)
                    bottom.linkTo(date.bottom)
                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                },
            icon = painterResource(id = R.drawable.ic_place_on_map),
            onClick = { }
        )

        SupportText(
            modifier = Modifier.constrainAs(date) {
                top.linkTo(title.bottom, 4.dp)
                bottom.linkTo(parent.bottom, 8.dp)
                absoluteLeft.linkTo(title.absoluteLeft)
            },
            text = place.dateOfCreation.toDateTextMonth()
        )

        ItemCounter(
            modifier = Modifier.constrainAs(fishIcon) {
                top.linkTo(date.top)
                bottom.linkTo(date.bottom)
                absoluteLeft.linkTo(date.absoluteRight, 8.dp)
            },
            count = catchesAmount,
            icon = R.drawable.ic_fishing
        )
    }
}

@ExperimentalPagerApi
@Composable
fun PlaceTabsView(
    modifier: Modifier = Modifier,
    tabs: List<TabItem>,
    pagerState: PagerState
) {
    val scope = rememberCoroutineScope()
    TabRow(
        modifier = modifier,
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = primaryTextColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }) {
        tabs.forEachIndexed { index, tab ->
            LeadingIconTab(
                icon = {
                    Icon(
                        painter = painterResource(id = tab.icon), contentDescription = "",
                        tint = MaterialTheme.colors.primaryVariant
                    )
                },
                text = {
                    Text(
                        stringResource(tab.titleRes),
                        color = MaterialTheme.colors.onSurface
                    )
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalPagerApi
@Composable
fun PlaceTabsContentView(
    tabs: List<TabItem>,
    pagerState: PagerState,
    navController: NavController,
    catches: List<UserCatch>,
    notes: MutableState<List<Note>?>,
    onNoteSelected: (Note) -> Unit
) {
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        count = tabs.size,
        verticalAlignment = Alignment.Top
    ) { page ->


        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            when (page) {
                0 -> PlaceCatchesView(
                    catches = catches,
                    userCatchClicked = { onCatchItemClick(it, navController) }
                )
                1 -> PlaceNotes(notes.value) {
                    onNoteSelected(it)
                }
            }
        }

    }
}

@ExperimentalComposeUiApi
@Composable
fun NoteModalBottomSheet(
    viewModel: UserPlaceViewModel,
    onCloseBottomSheet: () -> Unit,
) {
    EditNoteDialog(
        note = viewModel.currentNote.value ?: Note(),
        onSaveNote = { note ->
            viewModel.updateMarkerNotes(note)
        },
        onCloseDialog = onCloseBottomSheet
    )
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun PlaceNotes(
    notes: List<Note>?,
    onNoteSelected: (Note) -> Unit
) {
    LazyColumn(/*contentPadding = PaddingValues(8.dp)*/) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Card(onClick = { onNoteSelected(Note()) }) {
                    Icon(Icons.Default.Add, "")
                }
            }
        }
        notes?.let {
            items(notes) { note ->
                DefaultNoteView(
                    modifier = Modifier.padding(8.dp),
                    note = note,
                    onClick = { onNoteSelected(note) }
                )
            }
        } ?: item {
            DefaultNoteView(
                modifier = Modifier.padding(8.dp),
                note = Note(),
                onClick = { onNoteSelected(Note()) }
            )
        }

        item { Spacer(modifier = Modifier.size(bottomBannerPadding)) }
    }
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun PlaceCatchesView(
    modifier: Modifier = Modifier,
    catches: List<UserCatch>,
    userCatchClicked: (UserCatch) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        when {
            catches.isNotEmpty() -> {
                getDatesList(catches).forEach { catchDate ->
                    stickyHeader {
                        ItemDate(text = catchDate)
                    }
                    items(items = catches
                        .filter { userCatch ->
                            userCatch.date.toDateTextMonth() == catchDate
                        }
                        .sortedByDescending { it.date },
                        key = {
                            it
                        }
                    ) {
                        CatchItemView(
                            catch = it,
                            showPlace = false,
                            onClick = { userCatch -> userCatchClicked(userCatch) }
                        )
                    }
                }
            }
            catches.isEmpty() -> {
                item {
                    NoElementsView(
                        mainText = stringResource(R.string.no_cathces_added),
                        secondaryText = stringResource(R.string.add_catch_text),
                        onClickAction = { }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceButtonsView(
    modifier: Modifier = Modifier,
    place: UserMapMarker,
    navController: NavController,
    viewModel: UserPlaceViewModel
) {
    val context = LocalContext.current

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.padding(4.dp))

        DefaultButtonOutlined(
            text = stringResource(id = R.string.new_catch),
            icon = painterResource(id = R.drawable.ic_add_catch),
            onClick = { newCatchClicked(navController, viewModel) }
        )

        DefaultButtonOutlined(
            text = stringResource(id = R.string.navigate),
            icon = painterResource(id = R.drawable.ic_baseline_navigation_24),
            onClick = { onRouteClicked(context, place) }
        )

        DefaultButtonOutlined(
            text = stringResource(id = R.string.share),
            icon = painterResource(id = R.drawable.ic_baseline_share_24),
            onClick = { onShareClicked(context, place) }
        )

        DefaultButtonOutlined(
            text = stringResource(id = R.string.edit),
            icon = painterResource(id = R.drawable.ic_baseline_edit_24),
            onClick = { }
        )

        DefaultButtonOutlined(
            text = stringResource(id = R.string.delete),
            icon = painterResource(id = R.drawable.ic_baseline_delete_24),
            onClick = { }
        )

        Spacer(modifier = Modifier.padding(4.dp))
    }
}

@Composable
fun PlaceTopBar(
    backPress: () -> Unit,
    viewModel: UserPlaceViewModel,
    modifier: Modifier = Modifier,

    ) {

    val isVisible by remember(viewModel.markerVisibility.value) {
        if (viewModel.markerVisibility.value == null) mutableStateOf(true)
        else viewModel.markerVisibility
    }

    val color = animateColorAsState(
        targetValue = if (isVisible!!) {
            MaterialTheme.colors.onPrimary
        } else {
            supportTextColor
        },
        animationSpec = tween(800)
    )

    DefaultAppBar(
        modifier = modifier,
        title = stringResource(id = R.string.place),
        onNavClick = backPress,
        actions = {
            IconToggleButton(checked = isVisible!!,
                onCheckedChange = {
                    viewModel.changeVisibility(it)
                }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_remove_red_eye_24),
                    contentDescription = null,
                    tint = color.value
                )
            }
        }
    )
}