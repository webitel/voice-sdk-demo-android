package com.webitel.voice.sdk.demo_android

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.webitel.voice.sdk.Call
import com.webitel.voice.sdk.CallListener
import com.webitel.voice.sdk.CallState
import com.webitel.voice.sdk.LogLevel
import com.webitel.voice.sdk.User
import com.webitel.voice.sdk.VoiceClient


class MainActivityVM(
    private val application: Application
) : AndroidViewModel(application), CallListener {

    private val storage = AuthStorage(application)
    private var voiceClient: VoiceClient? = null
    private var authInfo: AuthInfo? = null
    private val dtmfHistory = StringBuilder()

    private val _activeCallLive = MutableLiveData<Call>()
    val activeCallLive: LiveData<Call> = _activeCallLive

    private var isNotificationShown = false

    init {
        // Load saved auth config and initialize VoiceClient if available
        authInfo = storage.getAuthInfo()
        authInfo?.let { initVoiceClient(it) }
    }


    /**
     * Initializes the VoiceClient instance using the provided AuthInfo.
     *
     * NOTE: For demo purposes only, [userName] is reused as both user ID and display name.
     * In production, use a proper unique user ID.
     */
    private fun initVoiceClient(auth: AuthInfo) {
        val userID = auth.userName // demo shortcut: use userName as userId
        val user = User.Builder(auth.issuer, userID, auth.userName).build()

        val createClient = {
            voiceClient = VoiceClient.Builder(application, auth.host, auth.token)
                .logLevel(LogLevel.DEBUG)
                .user(user)
                .build()
        }

        voiceClient?.shutdown {
            createClient()
        } ?: createClient()
    }


    /**
     * Saves authentication info and reinitializes VoiceClient.
     */
    fun setAuthInfo(auth: AuthInfo) {
        authInfo = auth
        storage.saveAuthInfo(auth)
        initVoiceClient(auth)
    }


    /**
     * Returns current stored AuthInfo.
     */
    fun getAuthInfo(): AuthInfo? = authInfo


    /**
     * Initiates an outgoing audio call.
     */
    fun makeCall() {
        val vc = voiceClient
        if (vc == null) {
            Toast.makeText(application, "Please set connection config first", Toast.LENGTH_LONG).show()
            return
        }
        vc.makeAudioCall(this)

        // Example with JWT token and CallListener
        // vc.makeAudioCall(jwt, this)
    }


    /**
     * Disconnects the active call.
     */
    fun disconnect() {
        _activeCallLive.value?.let { call ->
            call.disconnect()
                .onFailure { Log.e("Demo", "Disconnect error: ${it.message}", it) }
        }
    }


    /**
     * Toggles the mute state of the current call.
     */
    fun toggleMute() {
        _activeCallLive.value?.let { call ->
            call.mute(!call.isMuted)
                .onSuccess { updateCall(call) }
                .onFailure { Log.e("Demo", "Mute error: ${it.message}", it) }
        }
    }


    /**
     * Toggles the hold state of the current call.
     */
    fun toggleHold() {
        _activeCallLive.value?.let { call ->
            call.hold(!call.isOnHold)
                .onFailure { Log.e("Demo", "Hold error: ${it.message}", it) }
        }
    }


    /**
     * Sends DTMF tone to the active call and appends it to local history.
     */
    fun sendDTMF(value: String) {
        _activeCallLive.value?.let { call ->
            call.sendDTMF(value)
                .onSuccess { dtmfHistory.append(value) }
                .onFailure { Log.e("Demo", "DTMF error: ${it.message}", it) }
        }
    }


    /**
     * Returns the concatenated history of DTMF tones sent during the call.
     */
    fun getDTMFHistory(): String = dtmfHistory.toString()


    /**
     * Updates internal call state LiveData.
     */
    private fun updateCall(call: Call) {
        _activeCallLive.postValue(call)
    }

    // --- CallListener overrides ---

    override fun onCallStateChanged(call: Call, state: CallState) {
        updateCall(call)

        when (state) {
            is CallState.Ongoing -> {
                // Start foreground notification for ongoing call
                val intent = Intent(application, NotificationCallService::class.java)
                ContextCompat.startForegroundService(application, intent)
                isNotificationShown = true
            }

            is CallState.Disconnected -> {
                if (isNotificationShown) {
                    isNotificationShown = false
                    val stopIntent = Intent(application, NotificationCallService::class.java).apply {
                        action = "ACTION_STOP_NOW"
                    }
                    ContextCompat.startForegroundService(application, stopIntent)
                }
                dtmfHistory.clear()
            }

            else -> Unit
        }
    }

    override fun onHoldChanged(call: Call, isOnHold: Boolean) {
        updateCall(call)
    }
}
