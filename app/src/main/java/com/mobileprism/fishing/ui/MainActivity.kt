package com.mobileprism.fishing.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.MobileAds.setAppMuted
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.SnackbarAction
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.login.StartNavigation
import com.mobileprism.fishing.ui.theme.FishingNotesTheme
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.viewmodels.MainViewModel
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.Logger
import com.mobileprism.fishing.utils.checkNotificationPolicyAccess
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.*

class MainActivity : ComponentActivity() {

    private val logger: Logger by inject()
    private val appUpdateManager: AppUpdateManager = get()

    private lateinit var installStateUpdatedListener: InstallStateUpdatedListener

    companion object {
        const val splashFadeDurationMillis = 350
        const val UPDATE_REQUEST_CODE = 984165687

    }

    @OptIn(
        ExperimentalPermissionsApi::class,
        ExperimentalAnimationApi::class, InternalCoroutinesApi::class,
        ExperimentalPagerApi::class, ExperimentalMaterialApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MainViewModel = getViewModel()

        val screenState = viewModel.mutableStateFlow
        val appTheme = viewModel.appTheme

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                screenState.value is BaseViewState.Loading && appTheme.value == null
            }
            setOnExitAnimationListener { splashScreenViewProvider ->
                // Get icon instance and start a fade out animation
                if (Build.VERSION.SDK_INT >= 31) {
                    splashScreenViewProvider.view
                        .animate()
                        .setDuration(splashFadeDurationMillis.toLong())
                        .alpha(0f)
                        .start()
                }

                splashScreenViewProvider.iconView
                    .animate()
                    .setDuration(splashFadeDurationMillis.toLong())
                    .alpha(0f)
                    /*.scaleX(50f)
                    .scaleY(50f)*/
                    .withEndAction {
                        splashScreenViewProvider.remove()
                        if (Build.VERSION.SDK_INT < 31) {
                            setContent {
                                Distribution(
                                    appTheme = appTheme.value,
                                    viewModel.isUserLoggedState.collectAsState()
                                )
                            }
                        }
                    }
                    .start()
            }
        }

        if (Build.VERSION.SDK_INT >= 31) {
            setContent {
                Distribution(
                    appTheme = appTheme.value,
                    viewModel.isUserLoggedState.collectAsState()
                )
            }
        }


        MobileAds.initialize(this) {}

        /*
            Kostya's Pixel XL = 7254B9BDD30F1D2EACA4C4EAD6B31F2C
            Oleg = F70916713215C0BC73564CDFEC4D3ECB

        */
        val testDeviceIds =
            Arrays.asList("7254B9BDD30F1D2EACA4C4EAD6B31F2C", "F70916713215C0BC73564CDFEC4D3ECB")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)
        setAppMuted(true)

        checkForUpdates()
    }

    private fun checkForUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        installStateUpdatedListener = createUpdateListener()

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                // Before starting an update, register a listener for updates.
                appUpdateManager.registerListener(installStateUpdatedListener)

                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo, AppUpdateType.FLEXIBLE,
                    this, UPDATE_REQUEST_CODE
                )
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate()
            }
        }.addOnFailureListener {
            //SnackbarManager.showMessage(R.string.error_occured)
        }
    }

    // Create a listener to track request state updates.
    private fun createUpdateListener() =
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    val bytesDownloaded = state.bytesDownloaded()
                    val totalBytesToDownload = state.totalBytesToDownload()
                    // Show update progress bar.
                }
                InstallStatus.DOWNLOADED -> {
                    popupSnackbarForCompleteUpdate()
                }
                InstallStatus.INSTALLED -> {
                    SnackbarManager.showMessage(R.string.update_installed)
                    removeInstallStateUpdateListener()
                }
                InstallStatus.CANCELED -> {
                    SnackbarManager.showMessage(R.string.update_canceled)
                }
                InstallStatus.FAILED -> {
                    SnackbarManager.showMessage(R.string.update_failed)
                }
                else -> {}
            }
        }

    private fun removeInstallStateUpdateListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    private fun popupSnackbarForCompleteUpdate() {
        SnackbarManager.showMessage(
            R.string.update_ready,
            SnackbarAction(R.string.reload_app) { appUpdateManager.completeUpdate() },
            duration = SnackbarDuration.Indefinite
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_CANCELED -> {
                    SnackbarManager.showMessage(R.string.update_canceled)
                }
                RESULT_OK -> {
                    SnackbarManager.showMessage(R.string.update_downloading)
                }
                else -> {
                    SnackbarManager.showMessage(R.string.update_failed)
                    checkForUpdates()
                }
            }
        }
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
    fun Distribution(appTheme: AppThemeValues?, isUserLogged: State<Boolean>) {

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Crossfade(targetState = isUserLogged.value) { state ->
            when (state) {
                false -> {
                    FishingNotesTheme(appTheme, isLoginScreen = true) {
                        StartNavigation()
                    }
                }
                true -> {
                    FishingNotesTheme(appTheme) {
                        checkNotificationPolicyAccess(notificationManager)
                        FishingNotesApp()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        removeInstallStateUpdateListener()
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
