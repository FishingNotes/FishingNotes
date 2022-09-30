package com.mobileprism.fishing.ui.home.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.custom.CircleButton
import com.mobileprism.fishing.ui.custom.FishingTextButton
import com.mobileprism.fishing.ui.theme.customColors

@ExperimentalComposeUiApi
@Composable
fun DefaultDialog(
    primaryText: String? = null,
    secondaryText: String? = null,
    primaryTextWeight: FontWeight = FontWeight.SemiBold,
    neutralButtonText: String = "",
    onNeutralClick: (() -> Unit)? = null,
    negativeButtonText: String = stringResource(id = R.string.no),
    onNegativeClick: (() -> Unit)? = null,
    positiveButtonText: String = stringResource(id = R.string.yes),
    onPositiveClick: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    content: @Composable() (() -> Unit)? = null
) {

    /*val textBias = when (textAlign) {
        TextAlign.Start -> 0f
        TextAlign.End -> 1f
        else -> 0.5f
    }*/

    Dialog(onDismissRequest = onDismiss) {
        DefaultCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize()
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 2.dp)
            ) {
                val (title, subtitle, mainContent, neutralButton, negativeButton, positiveButton) = createRefs()

                primaryText?.let {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                        val textStyle = MaterialTheme.typography.subtitle1
                        ProvideTextStyle(textStyle) {
                            Text(
                                modifier = Modifier.constrainAs(title) {
                                    top.linkTo(parent.top, 16.dp)
                                    linkTo(
                                        parent.absoluteLeft,
                                        parent.absoluteRight,
                                        16.dp,
                                        16.dp,
                                        bias = 0f
                                    )
                                    width = Dimension.fillToConstraints
                                },
                                text = primaryText,
                                fontWeight = primaryTextWeight,
                            )
                        }
                    }
                }

                if (secondaryText != null) {
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.medium
                    ) {
                        val textStyle = MaterialTheme.typography.body2
                        ProvideTextStyle(textStyle) {
                            Text(
                                modifier = Modifier.constrainAs(subtitle) {
                                    top.linkTo(title.bottom, 8.dp)
                                    linkTo(
                                        start = parent.absoluteLeft,
                                        end = parent.absoluteRight,
                                        16.dp,
                                        16.dp,
                                        bias = 0f
                                    )
                                    width = Dimension.fillToConstraints
                                },
                                text = secondaryText,
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier
                        .size(0.dp)
                        .constrainAs(subtitle) {
                            top.linkTo(title.bottom, 2.dp)
                            absoluteLeft.linkTo(title.absoluteLeft)
                        })
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .constrainAs(mainContent) {
                            top.linkTo(subtitle.bottom, 14.dp)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight)
                            width = Dimension.fillToConstraints
                        },
                    contentAlignment = Alignment.Center
                ) {
                    content?.invoke()
                }

                onNeutralClick?.let {
                    // TODO: Разобраться с цветом
                    val textStyle = MaterialTheme.typography.body2.copy(color = MaterialTheme.customColors.secondaryTextColor)

                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.disabled
                    ) {
                        ProvideTextStyle(textStyle) {
                            FishingTextButton(
                                modifier = Modifier.constrainAs(neutralButton) {
                                    top.linkTo(mainContent.bottom, 16.dp)
                                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                                },
                                content = { Text(neutralButtonText, color = MaterialTheme.customColors.secondaryTextColor) },
                                onClick = onNeutralClick,
                            )
                        }
                    }
                }

                onPositiveClick?.let {
                    DefaultButtonFilled(
                        modifier = Modifier.constrainAs(positiveButton) {
                            top.linkTo(mainContent.bottom, 16.dp)
                            bottom.linkTo(parent.bottom, 4.dp)
                            absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                        },
                        text = positiveButtonText,
                        onClick = onPositiveClick,
                    )
                } ?: run {
                    Spacer(
                        modifier = Modifier
                            .size(0.dp)
                            .constrainAs(positiveButton) {
                                top.linkTo(mainContent.bottom, 16.dp)
                                bottom.linkTo(parent.bottom)
                                absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                            },
                    )
                }

                onNegativeClick?.let {
                    DefaultButton(
                        modifier = Modifier.constrainAs(negativeButton) {
                            top.linkTo(mainContent.bottom, 16.dp)
                            absoluteRight.linkTo(positiveButton.absoluteLeft, 4.dp)
                        },
                        text = negativeButtonText,
                        onClick = onNegativeClick,
                    )
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun LoadingDialog() {
    DefaultDialog(
        primaryText = stringResource(R.string.loading),
        onDismiss = {}
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_animation))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                modifier = Modifier.size(256.dp),
                composition = composition,
                iterations = LottieConstants.IterateForever,
                isPlaying = true
            )
        }
    }
}

@Composable
fun ModalLoadingDialog(
    isLoading: Boolean,
    text: String,
    onDismiss: (() -> Unit)? = null
) {
    if (isLoading) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp)
                    )
                    PrimaryText(
                        text = text,
                        textColor = Color.White
                    )
                }
                onDismiss?.let {
                    CircleButton(onClick = { onDismiss() }) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginHelpDialog(
    onDismiss: () -> Unit
) {
    DefaultDialog(
        primaryText = stringResource(R.string.auth),
        onDismiss = onDismiss,
        positiveButtonText = stringResource(id = R.string.close),
        onPositiveClick = onDismiss
    ) {
        PrimaryText(
            modifier = Modifier.padding(8.dp),
            text = stringResource(R.string.login_help_text)
        )

    }
}

@Composable
fun ModalLoading() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        content = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colors.surface)
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp), strokeWidth = 4.dp)
            }
        }
    )
}


