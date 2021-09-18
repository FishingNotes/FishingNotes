package com.joesemper.fishing.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.UserPlaceViewModel
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.composable.MyCardNoPadding
import com.joesemper.fishing.ui.composable.UserProfile
import com.joesemper.fishing.ui.theme.FigmaTheme
import com.joesemper.fishing.ui.theme.primaryFigmaBackgroundTint
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import com.joesemper.fishing.utils.NavigationHolder
import com.joesemper.fishing.utils.showToast
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope
import java.util.*


class UserPlaceFragment : Fragment(), AndroidScopeComponent {

    private val args: UserPlaceFragmentArgs by navArgs()

    override val scope: Scope by fragmentScope()
    private val viewModel: UserPlaceViewModel by viewModel()

    companion object {
        private const val TAG = "PLACE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mark = args.userMapMarker
        viewModel.marker.value = mark
    }

    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FigmaTheme {
                    UserPlaceScreen()
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @Composable
    fun UserPlaceScreen() {
        val isEdit = rememberSaveable { mutableStateOf(false) }
        val user by viewModel.getCurrentUser().collectAsState(null)
        Scaffold(
            topBar = { AppBar(isEdit) },
        ) {
            if (isEdit.value) {
                val userCatches by viewModel.getCatchesByMarkerId(viewModel.marker.value.id).collectAsState(
                    listOf())
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primaryFigmaBackgroundTint)
                ) {
                    Column {
                        EditPlaceInfo()
                        Buttons()
                    }
                    Catches(userCatches)
                }
            } else {
                val userCatches by viewModel.getCatchesByMarkerId(viewModel.marker.value.id).collectAsState(listOf())
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primaryFigmaBackgroundTint)
                ) {
                    Column {
                        PlaceInfo(user)
                        Buttons()
                    }
                    Catches(userCatches)
                }
            }
        }
    }

    @Composable
    fun Buttons() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(primaryFigmaColor),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                onClick = { routeClicked() },
                border = BorderStroke(0.dp, color = Color.Transparent),
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Column() {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(30.dp)
                            .rotate(45f),
                        painter = painterResource(R.drawable.ic_baseline_navigation_24),
                        contentDescription = stringResource(R.string.navigate)
                    )
                    Text(fontSize = 10.sp, text = stringResource(R.string.navigate))
                }
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                onClick = { shareClicked() },
                border = BorderStroke(0.dp, color = Color.Transparent),
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Column() {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(30.dp),
                        painter = painterResource(R.drawable.ic_baseline_share_24),
                        contentDescription = stringResource(R.string.share)
                    )
                    Text(fontSize = 10.sp, text = stringResource(R.string.share))
                }
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                onClick = { newCatchClicked() },
                border = BorderStroke(0.dp, color = Color.Transparent),
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Column() {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(30.dp),
                        painter = painterResource(R.drawable.ic_fish),
                        contentDescription = stringResource(R.string.new_catch)
                    )
                    Text(fontSize = 10.sp, text = stringResource(R.string.new_catch))
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    private fun PlaceInfo(user: User?) {
        MyCardNoPadding {

            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .height(50.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Place, stringResource(R.string.place))
                    Spacer(modifier = Modifier.width(150.dp))
                    UserProfile(user)
                }
                Text(viewModel.title, fontWeight = FontWeight.Bold)
                Text(viewModel.description ?: "Нет описания")
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    private fun EditPlaceInfo() {
        MyCardNoPadding {
            val user by viewModel.getCurrentUser().collectAsState(null)

            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .height(50.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Place, stringResource(R.string.place))
                    Spacer(modifier = Modifier.width(150.dp))
                    UserProfile(user)
                }

                viewModel.titleTemp = rememberSaveable {
                    mutableStateOf(viewModel.title)
                }
                viewModel.descriptionTemp = rememberSaveable {
                    mutableStateOf(viewModel.description ?: "")
                }
                OutlinedTextField(
                    value = viewModel.titleTemp.value,
                    onValueChange = { viewModel.titleTemp.value = it },
                    label = { Text(stringResource(R.string.place)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.descriptionTemp.value ?: "",
                    onValueChange = { viewModel.descriptionTemp.value = it },
                    label = { Text(stringResource(R.string.description)) },
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    fun Catches(
        catches: List<UserCatch>?,
    ) {
        catches?.let { userCatches ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = userCatches) {
                    ItemCatch(
                        catch = it
                    )
                }
            }
        } ?: CircularProgressIndicator()
    }

    @ExperimentalCoilApi
    @Composable
    fun ItemCatch(
        catch: UserCatch,
    ) {
        Card(elevation = 0.dp) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
            ) {
                Text(
                    text = catch.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.size(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                {
                    Box(
                        modifier = Modifier
                            .size(125.dp)
                            .weight(2f)
                    ) {
                        Image(painter = rememberImagePainter(R.drawable.ulov),
                            contentDescription = stringResource(R.string.catch_photo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(height = 125.dp)
                                .fillMaxWidth()
                                .clickable { /*clickedPhoto(photo)*/ })
                    }
                    Box(
                        modifier = Modifier
                            .size(125.dp)
                            .weight(2.35f)
                    ) {
                        Text(
                            text = catch.fishWeight.toString() + " " + getString(R.string.kg),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .align(Alignment.Center)
                        )
                        Text(
                            text = catch.date.toString(),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun AppBar(isEdit: MutableState<Boolean>) {
        TopAppBar(
            title = { Text(text = stringResource(R.string.place)) },
            navigationIcon = {
                IconButton(onClick = {
                    if (!isEdit.value) findNavController().popBackStack()
                    else {
                        isEdit.value = false
                        findNavController().popBackStack()
                    }
                }, content = {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = getString(R.string.back)
                    )
                })
            }, actions = {
                Row(
                    modifier = Modifier.padding(end = 3.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isEdit.value) IconButton(
                        onClick = {
                            editPlace()
                        },
                        content = {
                            Icon(
                                Icons.Filled.Edit,
                                stringResource(R.string.edit),
                                modifier = Modifier.clickable { isEdit.value = true })
                        })
                    if (isEdit.value) IconButton(
                        onClick = {
                            editPlace()
                        },
                        content = {
                            Icon(
                                Icons.Filled.Done,
                                stringResource(R.string.save),
                                modifier = Modifier.clickable {
                                    viewModel.save()
                                })
                        })
                    IconButton(
                        onClick = {
                            showToast(
                                requireContext(),
                                "Not Yet Implemented"
                            )
                        },
                        content = { Icon(Icons.Filled.Delete, stringResource(R.string.delete)) }
                    )
                }
            })
    }

    private fun editPlace() {
        //isEdit = true
        // TODO Redraw the screen
    }

    private fun newCatchClicked() {
        val action =
            UserPlaceFragmentDirections.actionUserPlaceFragmentToNewCatchDialogFragment(
                viewModel.marker.value
            )
        findNavController().navigate(action)
    }

    private fun routeClicked() {
        val uri = String.format(
            Locale.ENGLISH,
            "http://maps.google.com/maps?daddr=%f,%f (%s)",
            viewModel.marker.value.latitude,
            viewModel.marker.value.longitude,
            viewModel.title
        )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                val unrestrictedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(unrestrictedIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Please install a maps application", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun shareClicked() {
        val text =
            "${viewModel.title}\nhttps://www.google.com/maps/search/?api=1&query=${viewModel.marker.value.latitude}" +
                    ",${viewModel.marker.value.longitude}"
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as NavigationHolder).hideNav()
    }

    override fun onDetach() {
        super.onDetach()
        (requireActivity() as NavigationHolder).showNav()
    }
}


