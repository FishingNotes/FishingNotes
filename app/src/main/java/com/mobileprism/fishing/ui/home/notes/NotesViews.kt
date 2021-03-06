package com.mobileprism.fishing.ui.home.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.theme.cardColor
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.utils.time.toDateTextMonth
import com.mobileprism.fishing.utils.time.toTime
import org.koin.androidx.compose.get

/**
 * @param[childModifier] This is a modifier which is used in all child views
 * in order to show placeholder loading
 */
@Composable
fun ItemUserPlace(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    place: UserMapMarker,
    userPlaceClicked: (UserMapMarker) -> Unit,
    navigateToMap: () -> Unit,
) {

    DefaultCardClickable(
        modifier = modifier.padding(bottom = 4.dp),
        onClick = { userPlaceClicked(place) }
    ) {
        ConstraintLayout(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            val (icon, title, amount, date, navigateButton) = createRefs()

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

            PrimaryText(
                modifier = Modifier
                    .constrainAs(title) {
                        absoluteLeft.linkTo(icon.absoluteRight, 8.dp)
                        absoluteRight.linkTo(navigateButton.absoluteLeft, 8.dp)
                        top.linkTo(parent.top, 16.dp)
                        width = Dimension.fillToConstraints
                    }
                    .then(childModifier),
                text = place.title,
            )

            DefaultIconButton(
                modifier = Modifier.constrainAs(navigateButton) {
                    top.linkTo(title.top)
                    bottom.linkTo(date.bottom)
                    absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                },
                childModifier = childModifier,
                icon = painterResource(id = R.drawable.ic_place_on_map),
                tint = if (!place.visible) MaterialTheme.customColors.secondaryTextColor
                        else MaterialTheme.colors.onSurface,
                onClick = { navigateToMap() }
            )

            SupportText(
                modifier = Modifier
                    .constrainAs(date) {
                        top.linkTo(title.bottom, 4.dp)
                        bottom.linkTo(parent.bottom, 16.dp)
                        absoluteLeft.linkTo(title.absoluteLeft)
                    }
                    .then(childModifier),
                text = place.dateOfCreation.toDateTextMonth()
            )

            ItemCounter(
                modifier = Modifier.constrainAs(amount) {
                    bottom.linkTo(date.bottom)
                    top.linkTo(date.top)
                    height = Dimension.fillToConstraints
                    absoluteLeft.linkTo(date.absoluteRight, 8.dp)
                },
                count = place.catchesCount,
                icon = R.drawable.ic_fishing,
                tint = MaterialTheme.colors.primaryVariant.copy(0.25f)
            )
        }
    }
}

@Composable
fun ItemDate(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .zIndex(1f)
    ) {
        Surface(
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            shape = RoundedCornerShape(24.dp), color = cardColor
        ) {
            SecondaryTextColored(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                text = text,
                color = Color.White
            )
        }
    }
}

/**
 * @param[childModifier] This is a modifier which is used in all child views
 * in order to show placeholder loading
 */
@ExperimentalMaterialApi
@Composable
fun CatchItemView(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    catch: UserCatch,
    showPlace: Boolean = true,
    onClick: (UserCatch) -> Unit,

    ) {
    val preferences: UserPreferences = get()
    val is12hTimeFormat by preferences.use12hTimeFormat.collectAsState(initial = false)

    DefaultCardClickable(
        modifier = modifier.padding(bottom = 4.dp),
        onClick = { onClick(catch) }
    ) {
        ConstraintLayout(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            val (fishType, amount, weight, placeIcon, place, time, photosCount) = createRefs()

            PrimaryText(
                modifier = Modifier
                    .constrainAs(fishType) {
                        top.linkTo(parent.top)
                        absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                        absoluteRight.linkTo(weight.absoluteLeft, 16.dp)
                        width = Dimension.fillToConstraints
                    }
                    .then(childModifier),
                text = catch.fishType,
                maxLines = 1
            )

            SecondaryTextSmall(
                modifier = Modifier
                    .constrainAs(amount) {
                        top.linkTo(fishType.bottom, 4.dp)
                        absoluteLeft.linkTo(fishType.absoluteLeft)
                    }
                    .then(childModifier),
                text = "${stringResource(id = R.string.amount)}: ${catch.fishAmount}" +
                        " ${stringResource(id = R.string.pc)}"
            )

            PrimaryText(
                modifier = Modifier
                    .constrainAs(weight) {
                        top.linkTo(fishType.top)
                        absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                    }
                    .then(childModifier),
                text = "${catch.fishWeight} ${stringResource(id = R.string.kg)}"
            )

            //TODO: if place is hidden setting preference
            if (showPlace) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .constrainAs(placeIcon) {
                            absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                            top.linkTo(time.top)
                            bottom.linkTo(time.bottom)
                        },
                    painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                    contentDescription = stringResource(id = R.string.location),
                    tint = MaterialTheme.colors.secondaryVariant
                )

                SecondaryText(
                    modifier = Modifier
                        .constrainAs(place) {
                            absoluteLeft.linkTo(placeIcon.absoluteRight, 8.dp)
                            absoluteRight.linkTo(photosCount.absoluteLeft, 8.dp)
                            top.linkTo(placeIcon.top)
                            bottom.linkTo(placeIcon.bottom)
                            width = Dimension.fillToConstraints
                        }
                        .then(childModifier),
                    text = catch.placeTitle,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    textColor = Color.Gray
                )
            }

            SupportText(
                modifier = Modifier
                    .constrainAs(time) {
                        absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                        top.linkTo(amount.bottom, 16.dp)
                    }
                    .then(childModifier),
                text = catch.date.toTime(is12hTimeFormat)
            )

            ItemCounter(
                modifier = Modifier
                    .constrainAs(photosCount) {
                        top.linkTo(time.top)
                        bottom.linkTo(time.bottom)
                        height = Dimension.fillToConstraints
                        absoluteRight.linkTo(time.absoluteLeft, 12.dp)
                    }
                    .then(childModifier),
                count = catch.downloadPhotoLinks.size,
                icon = R.drawable.ic_baseline_photo_24,
                tint = MaterialTheme.colors.primaryVariant.copy(0.25f)
            )

        }
    }
}

@Composable
fun ItemCounter(
    modifier: Modifier = Modifier,
    count: Number,
    icon: Int,
    tint: Color = MaterialTheme.customColors.secondaryIconColor
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier.size(24.dp),
            tint = tint,
            painter = painterResource(id = icon),
            contentDescription = null,
        )
        SupportText(text = " x $count")
    }

}