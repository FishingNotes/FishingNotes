package com.joesemper.fishing.compose.ui

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.compose.ui.login.LoginScreen
import com.joesemper.fishing.compose.ui.theme.FishingNotesTheme
import com.joesemper.fishing.compose.viewmodels.MainViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.utils.Logger
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val logger: Logger by inject()

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    //private lateinit var user: State<BaseViewState?>

    private val registeredActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result)
        }

    companion object {
        const val splashFadeDurationMillis = 300
    }

    @OptIn(
        ExperimentalComposeUiApi::class, ExperimentalPermissionsApi::class,
        ExperimentalAnimationApi::class, InternalCoroutinesApi::class,
        ExperimentalPagerApi::class, ExperimentalMaterialApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MainViewModel = get()

        val userStateFlow: StateFlow<BaseViewState> = viewModel.subscribe()

        val userPreferences: UserPreferences = get()
        var appTheme: String? = null

        lifecycleScope.launchWhenStarted {
            userPreferences.appTheme.collect {
                appTheme = it
            }
        }

        if (Build.VERSION.SDK_INT < 31) {
            val splashWasDisplayed = savedInstanceState != null
            if (true /*!splashWasDisplayed*/) {
                val splashScreen = installSplashScreen()

                splashScreen.setKeepVisibleCondition {
                    userStateFlow.value is BaseViewState.Loading
                            && appTheme == null
                }

                splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
                    // Get icon instance and start a fade out animation
                    splashScreenViewProvider.iconView
                        .animate()
                        .setDuration(splashFadeDurationMillis.toLong())
                        .alpha(0f)
                        .withEndAction {
                            // After the fade out, remove the splash and set content view
                            splashScreenViewProvider.remove()
                            setContent {
                                FishingNotesTheme(appTheme) {
                                    if (viewModel.user == null)
                                        Navigation()
                                    else
                                        FishingNotesApp()
                                }
                            }
                        }.start()
                }
            } else {
                setTheme(R.style.Theme_SplashScreen)
                setContent {
                    FishingNotesTheme(appTheme) {
                        if (viewModel.user == null)
                            Navigation()
                        else
                            FishingNotesApp()
                    }
                }
            }
        } else {
            setContent {
                FishingNotesTheme(appTheme) {
                    if (viewModel.user == null)
                        Navigation()
                    else
                        FishingNotesApp()
                }
            }
        }

        auth = FirebaseAuth.getInstance()

        MobileAds.initialize(this) {}
    }

    // This app draws behind the system bars, so we want to handle fitting system windows
    // WindowCompat.setDecorFitsSystemWindows(window, false)

    //light тема
    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


    @OptIn(ExperimentalComposeUiApi::class)
    @InternalCoroutinesApi
    @ExperimentalMaterialApi
    @ExperimentalPagerApi
    @ExperimentalAnimationApi
    @ExperimentalPermissionsApi
    @Composable
    fun Navigation() {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "login_screen"
        ) {
            composable("login_screen") {
                LoginScreen(navController = navController)
            }
            // Main Screen
            composable(MainDestinations.HOME_ROUTE) {
                FishingNotesApp()
            }
        }
    }

    fun startGoogleLogin() {
        // Configure GOOGLE sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent: Intent = googleSignInClient.signInIntent
        registeredActivity.launch(signInIntent)
    }

    private fun onActivityResult(result: ActivityResult) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val exception = task.exception
        if (task.isSuccessful) {
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                handleError(exception as Throwable)
            }
        } else {
            handleError(exception as Throwable)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                } else {
                    // If sign in fails, display a message to the user.
                    handleError(task.exception as Throwable)
                }
            }
    }

    private fun handleError(error: Throwable) {
        /*setViews(false)
        vb.warning.visibility = View.VISIBLE
        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
        vb.warning.setOnClickListener {
            Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
        }*/
        SnackbarManager.showMessage(R.string.google_login_failed)
        logger.log(error.message)
    }

    private fun onSuccess(user: User?, navController: NavController) {
        if (user != null) {
            //vb.progressAnimationView.playAnimation()
            //Timer().schedule(2250) {
            navController.navigate("main_screen")
            //}
        } //TODO: Else
    }
}

/**
 * A composable function that returns the [Resources]. It will be recomposed when `Configuration`
 * gets updated.
 */
@Composable
@ReadOnlyComposable
fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}
