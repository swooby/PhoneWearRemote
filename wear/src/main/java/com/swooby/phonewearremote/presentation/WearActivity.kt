/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.swooby.phonewearremote.presentation

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.swooby.phonewearremote.SharedViewModel
import com.swooby.phonewearremote.WearViewModel
import com.swooby.phonewearremote.presentation.theme.PhoneWearRemoteTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val wearViewModel: WearViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            WearApp("Wear", wearViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        wearViewModel.init()
    }

    override fun onPause() {
        super.onPause()
        wearViewModel.close()
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview")
}

@Composable
fun WearApp(
    targetNameDefault: String,
    wearViewModel: WearViewModel? = null,
) {
    PhoneWearRemoteTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            KeepScreenOnComposable()
            PushToTalkButton(
                wearViewModel = wearViewModel,
                targetNameDefault = targetNameDefault,
                onPushToTalkStart = {
                    wearViewModel?.pushToTalk(true)
                },
                onPushToTalkStop = {
                    wearViewModel?.pushToTalk(false)
                },
            )
        }
    }
}

@Composable
fun PushToTalkButton(
    wearViewModel: WearViewModel? = null,
    targetNameDefault: String,
    enabled: Boolean = true,
    onPushToTalkStart: () -> Unit = {},
    onPushToTalkStop: () -> Unit = {}
) {
    val pttState by wearViewModel
        ?.pushToTalkState
        ?.collectAsState()
        ?: remember { mutableStateOf(SharedViewModel.PttState.Idle) }

    val phoneAppNodeId by wearViewModel
        ?.remoteAppNodeId
        ?.collectAsState()
        ?: remember { mutableStateOf(null) }
    val targetName = if (phoneAppNodeId != null) "Phone" else targetNameDefault

    val disabledColor = MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
    val boxAlpha = if (enabled) 1.0f else 0.38f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(120.dp)
            .border(4.dp, if (enabled) MaterialTheme.colors.primary else disabledColor, shape = CircleShape)
            .background(
                color = if (pttState == SharedViewModel.PttState.Pressed) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                shape = CircleShape
            )
            .let { baseModifier ->
                if (enabled) {
                    baseModifier.pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            onPushToTalkStart()
                            do {
                                val event = awaitPointerEvent()
                            } while (event.changes.any { !it.changedToUp() })
                            onPushToTalkStop()
                        }
                    }
                } else {
                    baseModifier.pointerInput(Unit) {
                        awaitEachGesture {
                            while (true) {
                                val event = awaitPointerEvent()
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                }
            }
            .then(Modifier.background(Color.Transparent))
            .let {
                it.background(Color.Transparent).graphicsLayer {
                    this.alpha = boxAlpha
                }
            },
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = targetName
        )
    }
}

@Composable
fun KeepScreenOnComposable() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }
}
