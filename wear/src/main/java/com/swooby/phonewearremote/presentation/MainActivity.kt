/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.swooby.phonewearremote.presentation

import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.swooby.phonewearremote.WearViewModel
import com.swooby.phonewearremote.presentation.theme.PhoneWearRemoteTheme

class MainActivity : ComponentActivity() {

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
    @Suppress("LocalVariableName")
    val TAG = "WearApp"

    val phoneAppNodeId by wearViewModel?.phoneAppNodeId?.collectAsState() ?: remember { mutableStateOf(null) }

    PhoneWearRemoteTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            RemoteButton(
                targetName = if (phoneAppNodeId != null) "Phone" else targetNameDefault,
                onPushToTalkStart = {
                    @Suppress("NAME_SHADOWING")
                    val phoneAppNodeId = phoneAppNodeId
                    if (phoneAppNodeId != null) {
                        Log.d(TAG, "onClick: PTT on phone...")
                        wearViewModel?.sendPushToTalkCommand(phoneAppNodeId, true)
                    } else {
                        Log.d(TAG, "onClick: TODO: PTT on local ...")
                        // ...
                    }
                    true
                },
                onPushToTalkStop = {
                    @Suppress("NAME_SHADOWING")
                    val phoneAppNodeId = phoneAppNodeId
                    if (phoneAppNodeId != null) {
                        Log.d(TAG, "onClick: PTT off phone...")
                        wearViewModel?.sendPushToTalkCommand(phoneAppNodeId, false)
                    } else {
                        Log.d(TAG, "onClick: TODO: PTT off local ...")
                        // ...
                    }
                    true
                },
            )
        }
    }
}

enum class PTTState {
    Idle,
    Pressed
}

@Composable
fun RemoteButton(
    targetName: String,
    enabled: Boolean = true,
    onPushToTalkStart: (pttState: PTTState) -> Boolean = {
        Log.d("PTT", "Push-to-Talk Start")
        false
    },
    onPushToTalkStop: (pttState: PTTState) -> Boolean = {
        Log.d("PTT", "Push-to-Talk Stop")
        false
    }
) {
    var pttState by remember { mutableStateOf(PTTState.Idle) }

    val disabledColor = MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
    val boxAlpha = if (enabled) 1.0f else 0.38f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(120.dp)
            .border(4.dp, if (enabled) MaterialTheme.colors.primary else disabledColor, shape = CircleShape)
            .background(
                color = if (pttState == PTTState.Pressed) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                shape = CircleShape
            )
            .let { baseModifier ->
                if (enabled) {
                    baseModifier.pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            if (pttState == PTTState.Idle) {
                                pttState = PTTState.Pressed
                                if (!onPushToTalkStart(pttState)) {
                                    //provideHapticFeedback(context)
                                    //provideAudibleFeedback(context, pttState)
                                }
                            }
                            do {
                                val event = awaitPointerEvent()
                            } while (event.changes.any { !it.changedToUp() })
                            if (pttState == PTTState.Pressed) {
                                pttState = PTTState.Idle
                                if (!onPushToTalkStop(pttState)) {
                                    //provideHapticFeedback(context)
                                    //provideAudibleFeedback(context, pttState)
                                }
                            }
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
