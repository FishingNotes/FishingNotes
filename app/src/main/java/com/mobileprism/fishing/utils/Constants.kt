package com.mobileprism.fishing.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Constants {

    const val TIME_TO_EXIT = 2000L
    val defaultFabBottomPadding: Dp = 128.dp

    val bottomBannerPadding: Dp = 80.dp

    val modalBottomSheetCorners = RoundedCornerShape(
        topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp
    )

    const val MAX_PHOTOS: Int = 3
    const val WIND_ROTATION = 45f

    const val ITEM_ADD_PHOTO = "ITEM_ADD_PHOTO"
    const val ITEM_PHOTO = "ITEM_PHOTO"

    const val CURRENT_PLACE_ITEM_ID = "Current_place"

}