package com.joesemper.fishing.compose.ui.home.user_places

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.fragment.findNavController
import com.joesemper.fishing.domain.UserPlacesViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.fragments.NotesFragmentDirections
import com.joesemper.fishing.utils.showToast
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@Composable
fun UserPlacesScreen( viewModel: UserPlacesViewModel = getViewModel<UserPlacesViewModel>()) {
    Scaffold() {
        val uiState by viewModel.uiState.collectAsState()
        Crossfade(uiState, animationSpec = tween(500)) { animatedUiState ->
            when (animatedUiState) {
                is BaseViewState.Loading ->
                    UserPlacesLoading { onAddNewPlaceClick() }
                is BaseViewState.Success<*> -> UserPlaces(
                    (animatedUiState as BaseViewState.Success<*>).data as List<UserMapMarker>, {
                        onAddNewPlaceClick()
                    }, { userMarker ->
                        onPlaceItemClick(userMarker)
                    }
                )
                is BaseViewState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "An error ocurred fetching the catches.")
                    }
                }
            }
        }

    }
}



private fun onAddNewPlaceClick() {
    /*showToast(requireContext(), "Not yet implemented")*/
}

private fun onPlaceItemClick(place: UserMapMarker) {
    /*val action =
        NotesFragmentDirections.actionNotesFragmentToUserPlaceFragment(place)
    findNavController().navigate(action)*/
}
