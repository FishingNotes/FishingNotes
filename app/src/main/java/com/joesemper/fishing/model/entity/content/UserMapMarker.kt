package com.joesemper.fishing.model.entity.content

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import kotlinx.parcelize.Parcelize

@Parcelize
class UserMapMarker(
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var title: String = "My marker",
    var description: String = "",
    var markerColor: Int = Color(0xFFEC407A).hashCode(),
    val catchesCount: Int = 0,
    val dateOfCreation: Long = 0,
    val isVisible: Boolean = true,
    val isPublic: Boolean = false
) : Parcelable, MapMarker