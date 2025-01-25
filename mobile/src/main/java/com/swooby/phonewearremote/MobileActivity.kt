package com.swooby.phonewearremote

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CaretScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ContentAlpha
import com.swooby.phonewearremote.ui.theme.PhoneWearRemoteTheme

class MobileActivity : ComponentActivity() {
    private val mobileViewModel: MobileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileApp(mobileViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        mobileViewModel.init()
    }

    override fun onPause() {
        super.onPause()
        mobileViewModel.close()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MobileApp()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileApp(
    mobileViewModel: MobileViewModel? = null
) {
    PhoneWearRemoteTheme {
        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.primary),
            topBar = {
                TopAppBar(
                    title = { Text("Phone") },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        ) { innerPadding ->
            KeepScreenOnComposable()
            MainScreen(
                mobileViewModel = mobileViewModel,
                modifier = Modifier
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
fun MainScreen(
    mobileViewModel: MobileViewModel? = null,
    modifier: Modifier = Modifier
) {

    var isConnectingOrConnected by remember { mutableStateOf(true) }
    var isConnected by remember { mutableStateOf(true) }
    var isCancelingResponse by remember { mutableStateOf(false) }

    val wearAppNodeId by mobileViewModel
        ?.remoteAppNodeId
        ?.collectAsState()
        ?: remember { mutableStateOf(null) }

    val disabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled)

    Column(
        modifier = modifier
            //.border(1.dp, Color.Green)
        ,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            modifier = Modifier
                //.border(1.dp, Color.Red)
                .fillMaxWidth(),
            text = "Hello!",
            textAlign = TextAlign.Center
        )

        TextField(
            modifier = Modifier
                //.border(1.dp, Color.Blue)
                .fillMaxWidth(),
            label = { Text("Wear App Node ID") },
            value = if (wearAppNodeId != null) "$wearAppNodeId" else "None",
            readOnly = true,
            onValueChange = {},
        )

        Box(
            modifier = Modifier
                .size(150.dp)
            ,
            contentAlignment = Alignment.Center
        ) {
            Box {
                when {
                    isConnected -> {
                        CircularProgressIndicator(
                            progress = { 1f },
                            color = Color.Green,
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(150.dp)
                        )
                    }
                    isConnectingOrConnected -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(150.dp)
                        )
                    }
                    else -> {
                        CircularProgressIndicator(
                            progress = { 0f },
                            color = disabledColor,
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(150.dp)
                        )
                    }
                }
            }
            PushToTalkButton(
                mobileViewModel = mobileViewModel,
                enabled = isConnected && !isCancelingResponse,
                onPushToTalkStart = {
                    mobileViewModel?.pushToTalk(true)
                },
                onPushToTalkStop = {
                    mobileViewModel?.pushToTalk(false)
                },
            )
        }

    }
}

@Composable
fun PushToTalkButton(
    mobileViewModel: MobileViewModel? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    iconIdle: Int = R.drawable.baseline_mic_24,
    iconPressed: Int = R.drawable.baseline_mic_24,
    iconDisabled: Int = R.drawable.baseline_mic_off_24,
    onPushToTalkStart: () -> Unit = {},
    onPushToTalkStop: () -> Unit = {}
) {
    val pttState by mobileViewModel
        ?.pushToTalkState
        ?.collectAsState()
        ?: remember { mutableStateOf(SharedViewModel.PttState.Idle) }

    val disabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled)
    val boxAlpha = if (enabled) 1.0f else 0.38f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(120.dp)
            .border(4.dp, if (enabled) MaterialTheme.colorScheme.primary else disabledColor, shape = CircleShape)
            .background(
                color = if (pttState == SharedViewModel.PttState.Pressed) Color.Green else Color.Transparent,
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
            }
    ) {
        val iconRes = if (enabled) {
            if (pttState == SharedViewModel.PttState.Pressed) {
                iconPressed
            } else {
                iconIdle
            }
        } else {
            iconDisabled
        }
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = "Microphone",
            modifier = Modifier
                .size(90.dp)
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
