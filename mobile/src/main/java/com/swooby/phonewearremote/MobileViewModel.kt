package com.swooby.phonewearremote

import android.app.Application
import android.util.Log
import com.swooby.phonewearremote.Utils.playAudioResourceOnce

class MobileViewModel(application: Application) :
    SharedViewModel(application) {
    override val TAG: String
        get() = "MobileViewModel"
    override val remoteTypeName: String
        get() = "WEAR"
    override val remoteCapabilityName: String
        get() = "verify_remote_example_wear_app"

    override fun pushToTalk(on: Boolean, sourceNodeId: String?) {
        Log.i(TAG, "pushToTalk(on=$on)")
        if (on) {
            if (_pushToTalkState.value != PttState.Pressed) {
                _pushToTalkState.value = PttState.Pressed
                playAudioResourceOnce(getApplication(), R.raw.ptt_touch)
                //provideHapticFeedback(context)
            }
        } else {
            if (_pushToTalkState.value != PttState.Idle) {
                _pushToTalkState.value = PttState.Idle
                playAudioResourceOnce(getApplication(), R.raw.ptt_touch)
                //provideHapticFeedback(context)
            }
        }

        if (sourceNodeId == null) {
            // request from local/mobile
            Log.d(TAG, "pushToTalk: PTT $on **from** local/mobile...")
            val remoteAppNodeId = _remoteAppNodeId.value
            if (remoteAppNodeId != null) {
                // tell remote/wear app that we are PTTing...
                sendPushToTalkCommand(remoteAppNodeId, on)
            }
            //...
        } else {
            // request from remote/wear
            _remoteAppNodeId.value = sourceNodeId
            Log.d(TAG, "pushToTalk: PTT $on **from** remote/wear...")
            //...
        }

        pushToTalkLocal(on)
    }

    override fun pushToTalkLocal(on: Boolean) {
        super.pushToTalkLocal(on)
        if (on) {
            /*
            pushToTalkViewModel?.realtimeClient?.also { realtimeClient ->
                Log.d(TAG, "")
                Log.d(TAG, "+onPushToTalkStart: pttState=$pttState")
                // 1. Play the start sound
                Log.d(TAG, "onPushToTalkStart: playing start sound")
                playAudioResourceOnce(
                    context = pushToTalkViewModel.getApplication(),
                    audioResourceId = R.raw.quindar_nasa_apollo_intro,
                    volume = 0.2f,
                ) {
                    // 2. Wait for the start sound to finish
                    Log.d(TAG, "onPushToTalkStart: start sound finished")
                    // 3. Open the mic
                    Log.d(TAG, "onPushToTalkStart: opening mic")
                    realtimeClient.setLocalAudioTrackMicrophoneEnabled(true)
                    Log.d(TAG, "onPushToTalkStart: mic opened")
                    // 4. Wait for the mic to open successfully
                    //...
                    Log.d(TAG, "-onPushToTalkStart")
                    Log.d(TAG, "")
                }
            }
            */
        } else {
            /*
            pushToTalkViewModel?.realtimeClient?.also { realtimeClient ->
                Log.d(TAG, "")
                Log.d(TAG, "+onPushToTalkStop: pttState=$pttState")
                // 1. Close the mic
                Log.d(TAG, "onPushToTalkStop: closing mic")
                realtimeClient.setLocalAudioTrackMicrophoneEnabled(false)
                Log.d(TAG, "onPushToTalkStop: mic closed")
                // 2. Wait for the mic to close successfully
                //...
                // 3. Send input_audio_buffer.commit
                Log.d(TAG, "onPushToTalkStop: sending input_audio_buffer.commit")
                realtimeClient.dataSendInputAudioBufferCommit()
                Log.d(TAG, "onPushToTalkStop: input_audio_buffer.commit sent")
                // 4. Send response.create
                Log.d(TAG, "onPushToTalkStop: sending response.create")
                realtimeClient.dataSendResponseCreate()
                Log.d(TAG, "onPushToTalkStop: response.create sent")
                // 5. Play the stop sound
                Log.d(TAG, "onPushToTalkStop: playing stop sound")
                playAudioResourceOnce(
                    context = pushToTalkViewModel.getApplication(),
                    audioResourceId = R.raw.quindar_nasa_apollo_outro,
                    volume = 0.2f,
                ) {
                    // 6. Wait for the stop sound to finish
                    Log.d(TAG, "onPushToTalkStop: stop sound finished")
                    //...
                    Log.d(TAG, "-onPushToTalkStop")
                    Log.d(TAG, "")
                }
            }
            */
        }
    }
}