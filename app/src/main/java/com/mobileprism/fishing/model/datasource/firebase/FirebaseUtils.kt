package com.mobileprism.fishing.model.datasource.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

const val CATCHES_COLLECTION = "catches"

@ExperimentalCoroutinesApi
fun getCatchesFromDoc(docs: List<DocumentSnapshot>) = callbackFlow {
    docs.forEach { doc ->
        doc.reference.collection(CATCHES_COLLECTION)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    val catches = snapshots.toObjects(UserCatch::class.java)
                    trySend(catches)
                } else {
                    trySend(listOf<UserCatch>())
                }

            }
    }
    awaitClose { }
}