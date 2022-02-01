package com.mobileprism.fishing.compose.ui.home.notes

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.compose.AsyncImageContent
import coil.compose.AsyncImagePainter
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.compose.ui.home.views.*
import com.mobileprism.fishing.compose.ui.theme.*
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.utils.Constants.ITEM_PHOTO
import com.mobileprism.fishing.utils.time.toDateTextMonth
import com.mobileprism.fishing.utils.time.toTime
import org.koin.androidx.compose.get

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun ItemPhoto(
    photo: Uri,
    clickedPhoto: (Uri) -> Unit,
    deletedPhoto: (Uri) -> Unit,
    deleteEnabled: Boolean = true
) {

    val fullScreenPhoto = remember {
        mutableStateOf<Uri?>(null)
    }

    Box(
        modifier = Modifier
            .size(150.dp)
            .padding(4.dp)
    ) {

        AsyncImage(
            model = photo,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .clickable {
                    clickedPhoto(photo)
                    fullScreenPhoto.value = photo
                },
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Low
        ) { state ->
            if (state is AsyncImagePainter.State.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(64.dp)
                        .align(Alignment.Center)
                )
            } else {
                AsyncImageContent()
            }
        }
        if (deleteEnabled) {
            Surface(
                color = Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    tint = Color.White,
                    contentDescription = stringResource(R.string.delete_photo),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { deletedPhoto(photo) })
            }
        }
    }

    AnimatedVisibility(fullScreenPhoto.value != null) {
        FullScreenPhoto(fullScreenPhoto)
    }
}

@ExperimentalAnimationApi
@Composable
fun ItemCatchPhotos(
    modifier: Modifier = Modifier,
    photo: Uri? = null,
    photosCount: Int = 0
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .padding(4.dp)
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp)),
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Low
            ) { state ->
                if (state is AsyncImagePainter.State.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(64.dp)
                            .align(Alignment.Center)
                    )
                } else {
                    AsyncImageContent()
                }
            }
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_no_photo_vector),
                contentDescription = ITEM_PHOTO,
                tint = secondaryFigmaTextColor,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(5.dp))
            )
        }

        if (photosCount > 1) {
            Surface( //For making delete button background half transparent
                color = Color.LightGray.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(25.dp)
                    .align(Alignment.BottomStart)
                    .padding(3.dp)
            ) {
                Text(
                    text = "X$photosCount",
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ItemUserCatch(
    userCatch: UserCatch,
    userCatchClicked: (UserCatch) -> Unit
) {
    val photo = if (userCatch.downloadPhotoLinks.isNotEmpty()) {
        userCatch.downloadPhotoLinks.first()
    } else {
        null
    }

    val preferences: UserPreferences = get()
    val is12hTimeFormat by preferences.use12hTimeFormat.collectAsState(initial = false)

    DefaultCardClickable(onClick = { userCatchClicked(userCatch) }) {
        ConstraintLayout(modifier = Modifier.padding(8.dp)) {
            val (photos, fish, weight, kg, description, icon, place, date) = createRefs()
            val guideline = createGuidelineFromAbsoluteLeft(110.dp)

            ItemCatchPhotos(
                modifier = Modifier.constrainAs(photos) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                photo = photo?.toUri(),
                photosCount = userCatch.downloadPhotoLinks.count()
            )

            PrimaryText(
                modifier = Modifier.constrainAs(fish) {
                    absoluteLeft.linkTo(guideline, 4.dp)
                    top.linkTo(parent.top)
                },
                text = userCatch.fishType
            )

            SecondaryText(
                modifier = Modifier.constrainAs(kg) {
                    absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                    top.linkTo(parent.top)
                },
                text = stringResource(id = R.string.kg)
            )

            PrimaryText(
                modifier = Modifier.constrainAs(weight) {
                    absoluteRight.linkTo(kg.absoluteLeft, 4.dp)
                    top.linkTo(parent.top)
                },
                text = userCatch.fishWeight.toString()
            )

            SecondaryText(
                modifier = Modifier.constrainAs(description) {
                    absoluteLeft.linkTo(fish.absoluteLeft)
                    linkTo(fish.bottom, icon.top, bottomMargin = 2.dp, bias = 0F)
                },
                text = if (userCatch.description.isNotBlank()) {
                    userCatch.description
                } else {
                    stringResource(id = R.string.no_description)
                }
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(icon) {
                        absoluteLeft.linkTo(guideline)
                        bottom.linkTo(parent.bottom)
                    },
                painter = painterResource(
                    id = R.drawable.ic_baseline_location_on_24
                ),
                contentDescription = stringResource(R.string.icon),
                tint = secondaryFigmaTextColor
            )

            SecondaryTextColored(
                modifier = Modifier.constrainAs(place) {
                    top.linkTo(icon.top)
                    bottom.linkTo(icon.bottom)
                    absoluteLeft.linkTo(icon.absoluteRight)
                },
                text = userCatch.placeTitle
            )

            SupportText(
                modifier = Modifier.constrainAs(date) {
                    absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                    top.linkTo(place.top)
                },
                text = userCatch.date.toTime(is12hTimeFormat)
            )
        }
    }
}

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
                modifier = Modifier.constrainAs(title) {
                    absoluteLeft.linkTo(icon.absoluteRight, 8.dp)
                    absoluteRight.linkTo(navigateButton.absoluteLeft, 8.dp)
                    top.linkTo(parent.top, 16.dp)
                    width = Dimension.fillToConstraints
                }.then(childModifier),
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
                modifier = Modifier.constrainAs(date) {
                    top.linkTo(title.bottom, 4.dp)
                    bottom.linkTo(parent.bottom, 16.dp)
                    absoluteLeft.linkTo(title.absoluteLeft)
                }.then(childModifier),
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
fun ItemAdd(
    icon: Painter,
    text: String,
    onClickAction: () -> Unit
) {
    DefaultCard {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .clickable { onClickAction() }
                .padding(5.dp)
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Icon(
                    painter = icon,
                    contentDescription = stringResource(R.string.new_catch),
                    modifier = Modifier
                        .weight(2f)
                        .align(Alignment.CenterHorizontally)
                        .size(48.dp),
                    tint = primaryFigmaColor
                )
                SecondaryText(
                    text = text,
                    modifier = Modifier.weight(1f)
                )
            }
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
                modifier = Modifier.constrainAs(fishType) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                    absoluteRight.linkTo(weight.absoluteLeft, 16.dp)
                    width = Dimension.fillToConstraints
                }.then(childModifier),
                text = catch.fishType,
                maxLines = 1
            )

            SecondaryTextSmall(
                modifier = Modifier.constrainAs(amount) {
                    top.linkTo(fishType.bottom, 4.dp)
                    absoluteLeft.linkTo(fishType.absoluteLeft)
                }.then(childModifier),
                text = "${stringResource(id = R.string.amount)}: ${catch.fishAmount}" +
                        " ${stringResource(id = R.string.pc)}"
            )

            PrimaryText(
                modifier = Modifier.constrainAs(weight) {
                    top.linkTo(fishType.top)
                    absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                }.then(childModifier),
                text = "${catch.fishWeight} ${stringResource(id = R.string.kg)}"
            )

            //TODO: if place is hidden
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
                    modifier = Modifier.constrainAs(place) {
                        absoluteLeft.linkTo(placeIcon.absoluteRight, 8.dp)
                        absoluteRight.linkTo(photosCount.absoluteLeft, 8.dp)
                        top.linkTo(placeIcon.top)
                        bottom.linkTo(placeIcon.bottom)
                        width = Dimension.fillToConstraints
                    }.then(childModifier),
                    text = catch.placeTitle,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    textColor = Color.Gray
                )
            }

            SupportText(
                modifier = Modifier.constrainAs(time) {
                    absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                    top.linkTo(amount.bottom, 16.dp)
                }.then(childModifier),
                text = catch.date.toTime(is12hTimeFormat)
            )

            ItemCounter(
                modifier = Modifier.constrainAs(photosCount) {
                    top.linkTo(time.top)
                    bottom.linkTo(time.bottom)
                    height = Dimension.fillToConstraints
                    absoluteRight.linkTo(time.absoluteLeft, 12.dp)
                }.then(childModifier),
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