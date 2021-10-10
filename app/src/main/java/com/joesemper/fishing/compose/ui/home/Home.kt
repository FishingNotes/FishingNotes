package com.joesemper.fishing.compose.ui.home

import androidx.annotation.FloatRange
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.weather.Weather
import com.joesemper.fishing.ui.theme.FigmaTheme
import kotlinx.coroutines.InternalCoroutinesApi

@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@InternalCoroutinesApi
fun NavGraphBuilder.addHomeGraph(
    onSnackSelected: (Long, NavBackStackEntry) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    composable(HomeSections.MAP.route) { from ->
        Map(onSnackClick = { id -> onSnackSelected(id, from) }, modifier)
    }
    composable(HomeSections.NOTES.route) { from ->
        Notes(onSnackClick = { id -> onSnackSelected(id, from) }, modifier, navController)
    }
    composable(HomeSections.WEATHER.route) { from ->
        Weather(onSnackClick = { id -> onSnackSelected(id, from) }, modifier, navController)
    }
    composable(HomeSections.PROFILE.route) {
        Profile(navController, modifier)
    }
}

enum class HomeSections(
    @StringRes val title: Int,
    val icon: ImageVector,
    val route: String
) {
    MAP(R.string.map, Icons.Outlined.Map, "home/map"),
    NOTES(R.string.notes, Icons.Outlined.Menu, "home/notes"),
    WEATHER(R.string.weather, Icons.Outlined.WbSunny, "home/weather"),
    PROFILE(R.string.profile, Icons.Outlined.VerifiedUser, "home/profile")
}

@Composable
fun FishingNotesBottomBar(
    tabs: Array<HomeSections>,
    currentRoute: String,
    navigateToRoute: (String) -> Unit,
//    color: Color = Theme.colors.iconPrimary,
//    contentColor: Color = Theme.colors.iconInteractive
) {
    val routes = remember { tabs.map { it.route } }
    val currentSection = tabs.first { it.route == currentRoute }

    Surface(
//        color = color,
//        contentColor = contentColor
    elevation = 8.dp
    ) {
        val springSpec = SpringSpec<Float>(
            // Determined experimentally
            stiffness = 800f,
            dampingRatio = 0.8f
        )
        FishingNotesBottomNavLayout(
            selectedIndex = currentSection.ordinal,
            itemCount = routes.size,
            indicator = { FishingNotesBottomNavIndicator() },
            animSpec = springSpec,
            modifier = Modifier.navigationBarsPadding(start = false, end = false)
        ) {
            tabs.forEach { section ->
                val selected = section == currentSection
                val tint by animateColorAsState(
                    if (selected) {
                        Color.Black
                    } else {
                        Color.Black
                    }
                )

                FishingNotesBottomNavigationItem(
                    icon = {
                        Icon(
                            imageVector = section.icon,
                            tint = tint,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(section.title).uppercase(
                                ConfigurationCompat.getLocales(
                                    LocalConfiguration.current
                                ).get(0)
                            ),
                            color = tint,
                            style = MaterialTheme.typography.button,
                            maxLines = 1
                        )
                    },
                    selected = selected,
                    onSelected = { navigateToRoute(section.route) },
                    animSpec = springSpec,
                    modifier = BottomNavigationItemPadding
                        .clip(BottomNavIndicatorShape)
                )
            }
        }
    }
}

@Composable
private fun FishingNotesBottomNavLayout(
    selectedIndex: Int,
    itemCount: Int,
    animSpec: AnimationSpec<Float>,
    indicator: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Track how "selected" each item is [0, 1]
    val selectionFractions = remember(itemCount) {
        List(itemCount) { i ->
            Animatable(if (i == selectedIndex) 1f else 0f)
        }
    }
    selectionFractions.forEachIndexed { index, selectionFraction ->
        val target = if (index == selectedIndex) 1f else 0f
        LaunchedEffect(target, animSpec) {
            selectionFraction.animateTo(target, animSpec)
        }
    }

    // Animate the position of the indicator
    val indicatorIndex = remember { Animatable(0f) }
    val targetIndicatorIndex = selectedIndex.toFloat()
    LaunchedEffect(targetIndicatorIndex) {
        indicatorIndex.animateTo(targetIndicatorIndex, animSpec)
    }

    Layout(
        modifier = modifier.height(BottomNavHeight),
        content = {
            content()
            Box(Modifier.layoutId("indicator"), content = indicator)
        }
    ) { measurables, constraints ->
        check(itemCount == (measurables.size - 1)) // account for indicator

        // Divide the width into n+1 slots and give the selected item 2 slots
        val unselectedWidth = constraints.maxWidth / (itemCount + 1)
        val selectedWidth = 2 * unselectedWidth
        val indicatorMeasurable = measurables.first { it.layoutId == "indicator" }

        val itemPlaceables = measurables
            .filterNot { it == indicatorMeasurable }
            .mapIndexed { index, measurable ->
                // Animate item's width based upon the selection amount
                val width = lerp(unselectedWidth, selectedWidth, selectionFractions[index].value)
                measurable.measure(
                    constraints.copy(
                        minWidth = width,
                        maxWidth = width
                    )
                )
            }
        val indicatorPlaceable = indicatorMeasurable.measure(
            constraints.copy(
                minWidth = selectedWidth,
                maxWidth = selectedWidth
            )
        )

        layout(
            width = constraints.maxWidth,
            height = itemPlaceables.maxByOrNull { it.height }?.height ?: 0
        ) {
            val indicatorLeft = indicatorIndex.value * unselectedWidth
            indicatorPlaceable.placeRelative(x = indicatorLeft.toInt(), y = 0)
            var x = 0
            itemPlaceables.forEach { placeable ->
                placeable.placeRelative(x = x, y = 0)
                x += placeable.width
            }
        }
    }
}

@Composable
fun FishingNotesBottomNavigationItem(
    icon: @Composable BoxScope.() -> Unit,
    text: @Composable BoxScope.() -> Unit,
    selected: Boolean,
    onSelected: () -> Unit,
    animSpec: AnimationSpec<Float>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.selectable(selected = selected, onClick = onSelected),
        contentAlignment = Alignment.Center
    ) {
        // Animate the icon/text positions within the item based on selection
        val animationProgress by animateFloatAsState(if (selected) 1f else 0f, animSpec)
        FishingNotesBottomNavItemLayout(
            icon = icon,
            text = text,
            animationProgress = animationProgress
        )
    }
}

@Composable
private fun FishingNotesBottomNavItemLayout(
    icon: @Composable BoxScope.() -> Unit,
    text: @Composable BoxScope.() -> Unit,
    @FloatRange(from = 0.0, to = 1.0) animationProgress: Float
) {
    Layout(
        content = {
            Box(
                modifier = Modifier
                    .layoutId("icon")
                    .padding(horizontal = TextIconSpacing),
                content = icon
            )
            val scale = lerp(0.6f, 1f, animationProgress)
            Box(
                modifier = Modifier
                    .layoutId("text")
                    .padding(horizontal = TextIconSpacing)
                    .graphicsLayer {
                        alpha = animationProgress
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = BottomNavLabelTransformOrigin
                    },
                content = text
            )
        }
    ) { measurables, constraints ->
        val iconPlaceable = measurables.first { it.layoutId == "icon" }.measure(constraints)
        val textPlaceable = measurables.first { it.layoutId == "text" }.measure(constraints)

        placeTextAndIcon(
            textPlaceable,
            iconPlaceable,
            constraints.maxWidth,
            constraints.maxHeight,
            animationProgress
        )
    }
}

private fun MeasureScope.placeTextAndIcon(
    textPlaceable: Placeable,
    iconPlaceable: Placeable,
    width: Int,
    height: Int,
    @FloatRange(from = 0.0, to = 1.0) animationProgress: Float
): MeasureResult {
    val iconY = (height - iconPlaceable.height) / 2
    val textY = (height - textPlaceable.height) / 2

    val textWidth = textPlaceable.width * animationProgress
    val iconX = (width - textWidth - iconPlaceable.width) / 2
    val textX = iconX + iconPlaceable.width

    return layout(width, height) {
        iconPlaceable.placeRelative(iconX.toInt(), iconY)
        if (animationProgress != 0f) {
            textPlaceable.placeRelative(textX.toInt(), textY)
        }
    }
}

@Composable
private fun FishingNotesBottomNavIndicator(
    strokeWidth: Dp = 1.dp,
    color: Color = Color.Black,
    shape: Shape = BottomNavIndicatorShape
) {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .then(BottomNavigationItemPadding)
            .border(strokeWidth, color, shape)
    )
}

private val TextIconSpacing = 2.dp
private val BottomNavHeight = 56.dp
private val BottomNavLabelTransformOrigin = TransformOrigin(0f, 0.5f)
private val BottomNavIndicatorShape = RoundedCornerShape(percent = 50)
private val BottomNavigationItemPadding = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)

@Preview
@Composable
private fun FishingNotesBottomNavPreview() {
    FigmaTheme {
        FishingNotesBottomBar(
            tabs = HomeSections.values(),
            currentRoute = "home/map",
            navigateToRoute = { }
        )
    }
}
