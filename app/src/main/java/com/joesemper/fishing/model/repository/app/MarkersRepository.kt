package com.joesemper.fishing.model.repository.app

import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.MapMarker
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MarkersRepository {
    fun getMapMarker(markerId: String): Flow<UserMapMarker?>
    fun getAllUserMarkers(): Flow<MapMarker>
    fun getAllUserMarkersList(): Flow<List<MapMarker>>

    suspend fun deleteMarker(userMapMarker: UserMapMarker)
    suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress>
}