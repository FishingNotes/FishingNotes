package com.joesemper.fishing.utils

import com.joesemper.fishing.model.entity.raw.RawMapMarker

interface AddNewMarkerListener {
    fun addNewMapMarker(marker: RawMapMarker)
}

interface NavigationHolder {
    fun hideNav()
    fun showNav()
}
