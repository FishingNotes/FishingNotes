package com.joesemper.fishing.compose.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.google.accompanist.insets.statusBarsHeight
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.ui.theme.FigmaTheme
import kotlinx.coroutines.InternalCoroutinesApi
import me.vponomarenko.compose.shimmer.shimmer

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@Composable
fun Profile(modifier: Modifier = Modifier) {
    //val viewModel = getViewModel<UserViewModel>()
    //val uiState = viewModel.uiState
    Scaffold(
        topBar = { AppBar() },
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Card(
                    elevation = 10.dp,
                    modifier = Modifier.padding(25.dp),
                    shape = RoundedCornerShape(25.dp),
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    Column() {
                        UserInfo()
                        UserStats()
                    }

                }
                UserButtons()
            }
        })
}

@Composable
fun UserStats() {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(5.dp).fillMaxWidth().height(50.dp)
    ) {
        PlacesNumber()
        CatchesNumber()
    }
}

@Composable
fun PlacesNumber() {
    //val userPlacesNum by viewModel.getUserPlaces().collectAsState(null)
    /*userPlacesNum?.let {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, modifier = Modifier.size(50.dp)
        ) {
            Icon(
                Icons.Default.Place, stringResource(R.string.place),
                modifier = Modifier.size(25.dp)
            )
            //Text(it.size.toString())
        }
    } ?: */Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, modifier = Modifier.size(50.dp)
    ) {
        Icon(
            Icons.Default.Place, stringResource(R.string.place),
            modifier = Modifier.size(25.dp).shimmer(),
            tint = Color.LightGray
        )
        Text(
            "0",
            color = Color.LightGray,
            modifier = Modifier.background(Color.LightGray).shimmer()
        )
    }

}

@Composable
fun CatchesNumber() {
    //val userCatchesNum by viewModel.getUserCatches().collectAsState(null)
    /*userCatchesNum?.let {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, modifier = Modifier.size(50.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_fishing), stringResource(R.string.place),
                modifier = Modifier.size(25.dp)
            )
            Text(it.size.toString())
        }
    } ?: */Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, modifier = Modifier.size(50.dp)
    ) {
        Icon(
            painterResource(R.drawable.ic_fishing), stringResource(R.string.place),
            modifier = Modifier.size(25.dp).shimmer(),
            tint = Color.LightGray
        )
        Text(
            "0",
            color = Color.LightGray,
            modifier = Modifier.background(Color.LightGray).shimmer()
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun UserButtons() {
    val dialogOnLogout = rememberSaveable { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(horizontal = 80.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.Bottom)
    ) {
        ColumnButton(painterResource(R.drawable.ic_friends), stringResource(R.string.friends)) {
            //notReadyYetToast()
        }

        ColumnButton(
            painterResource(R.drawable.ic_edit),
            stringResource(R.string.edit_profile)
        ) {
            //notReadyYetToast()
        }

        ColumnButton(
            painterResource(R.drawable.ic_settings),
            stringResource(R.string.settings)
        ) {
//            val action =
//                UserFragmentDirections.actionUserFragmentToSettingsFragment()
//            findNavController().navigate(action)
        }
        Spacer(modifier = Modifier.size(15.dp))
        OutlinedButton(onClick = {
            dialogOnLogout.value = true
        }) { Text(stringResource(R.string.logout)) }
        Spacer(modifier = Modifier.size(30.dp))
        if (dialogOnLogout.value) LogoutDialog(dialogOnLogout)
    }
}

@Composable
fun LogoutDialog(dialogOnLogout: MutableState<Boolean>) {
    AlertDialog(
        title = { Text("Выход из аккаунта") },
        text = { Text("Вы уверены, что хотите выйти из своего аккаунта?") },
        onDismissRequest = { dialogOnLogout.value = false },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    /*lifecycleScope.launch {
                        viewModel.logoutCurrentUser().collect { isLogout ->
                            if (isLogout) startLoginActivity()
                        }
                    }*/
                },
                content = { /*Text(getString(R.string.Yes))*/ })
        }, dismissButton = {
            OutlinedButton(
                onClick = { dialogOnLogout.value = false },
                content = { /*Text(getString(R.string.No))*/ })
        }
    )
}

@Composable
fun ColumnButton(image: Painter, name: String, click: () -> Unit) {
    OutlinedButton(
        onClick = click,
        modifier = Modifier.fillMaxWidth(),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(image, name, modifier = Modifier.size(25.dp))
                Text(name, modifier = Modifier.padding(start = 10.dp))
            }
        })
}

@ExperimentalCoilApi
@Composable
fun UserInfo() {
    //val user by viewModel.getCurrentUser().collectAsState(null)
    /*user?.let { nutNullUser ->
        Crossfade(nutNullUser, animationSpec = tween(500)) { animatedUser ->
            UserNameAndImage(animatedUser)
        }
    } ?: */Row(
        modifier = Modifier.fillMaxWidth().height(150.dp).padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}


@ExperimentalCoilApi
@Composable
fun UserNameAndImage(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth().height(150.dp).padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = if (user.userPic.isNullOrEmpty() or user.isAnonymous)
                painterResource(R.drawable.ic_fisher)
            else rememberImagePainter(
                data = user.userPic,
                builder = {
                    transformations(CircleCropTransformation())
                    //crossfade(500)
                }
            ),
            contentDescription = stringResource(R.string.fisher),
            modifier = Modifier.size(125.dp),
        )
        Text(
            text = when (user.isAnonymous) {
                true -> stringResource(R.string.anonymous)
                false -> user.userName
            }, style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AppBar() {
    Column {
        Spacer(Modifier.statusBarsHeight())
        TopAppBar(
            title = { Text(text = "User") },
            navigationIcon = {
                IconButton(onClick = { /*findNavController().popBackStack()*/ }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            elevation = 2.dp
        )
    }

}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@InternalCoroutinesApi
@Preview("default")
//@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
fun ProfilePreview() {
    FigmaTheme {
        Profile()
    }
}

