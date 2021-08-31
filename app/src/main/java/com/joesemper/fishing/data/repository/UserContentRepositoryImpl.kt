package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.data.entity.common.Progress
import com.joesemper.fishing.data.entity.content.MapMarker
import com.joesemper.fishing.data.entity.content.UserCatch
import com.joesemper.fishing.data.entity.content.UserMapMarker
import com.joesemper.fishing.data.entity.raw.RawMapMarker
import com.joesemper.fishing.data.entity.raw.RawUserCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class UserContentRepositoryImpl(private val dataProvider: DatabaseProvider) :
    UserContentRepository {

    override fun getMapMarker(markerId: String): Flow<UserMapMarker?> =
        dataProvider.getMapMarker(markerId)

    override fun getAllUserMarkers(): Flow<MapMarker> = dataProvider.getAllMarkers()

    override fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>> =
        dataProvider.getCatchesByMarkerId(markerId)

    override suspend fun addNewCatch(newCatch: RawUserCatch): StateFlow<Progress> =
        dataProvider.addNewCatch(newCatch)

    override suspend fun deleteMarker(userCatch: UserCatch) = dataProvider.deleteMarker(userCatch)

    override suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress> =
        dataProvider.addNewMarker(newMarker)

}