package com.swooby.phonewearremote

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.swooby.phonewearremote.Utils.quote

class MainActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val messageClient by lazy { Wearable.getMessageClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        messageClient.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        messageClient.removeListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        when (path) {
            "/pushToTalk" -> handlePushToTalkCommand(messageEvent)
            else -> Log.d(TAG, "Unhandled messageEvent.path=${quote(path)}")
        }
    }

    private fun handlePushToTalkCommand(messageEvent: MessageEvent) {
        val payload = messageEvent.data
        val payloadString = String(payload)
        Log.i(TAG, "handlePushToTalkCommand: PushToTalk command received! payloadString=${quote(payloadString)}")

        // TODO: Start your "PushToTalk" action:
        // e.g., open a microphone, start voice recognition, etc.
        // ...
    }
}