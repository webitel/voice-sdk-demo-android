package com.webitel.voice.sdk.demo_android
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.webitel.voice.sdk.CallState


class MainActivity : AppCompatActivity(), SettingDialogFragment.OnConnectionDataEntered,
    DtmfDialogFragment.OnDtmfDataEntered {

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainActivityVM::class.java]
    }

    // UI references
    private lateinit var btnOpenSetting: Button
    private lateinit var btnMake: Button
    private lateinit var btnHold: Button
    private lateinit var btnDTMF: Button
    private lateinit var btnDisconnect: Button
    private lateinit var btnMute: Button
    private lateinit var callState: TextView
    private lateinit var timer: Chronometer
    private lateinit var controlContainer: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
        bindListeners()
        observeViewModel()
        checkPermissions()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }


    /**
     * Initializes view references.
     */
    private fun initUI() {
        btnOpenSetting = findViewById(R.id.openSettingButton)
        btnMake = findViewById(R.id.makeBtn)
        btnHold = findViewById(R.id.holdBtn)
        btnDTMF = findViewById(R.id.dtmfBtn)
        btnDisconnect = findViewById(R.id.disconnectBtn)
        btnMute = findViewById(R.id.muteBtn)
        callState = findViewById(R.id.callStateTextView)
        timer = findViewById(R.id.timerChronometer)
        controlContainer = findViewById(R.id.controlContainer)

        timer.format = "00:%s"
    }


    /**
     * Sets click listeners on UI buttons.
     */
    private fun bindListeners() {
        btnMake.setOnClickListener {
            checkPermissions()
            viewModel.makeCall()
        }

        btnOpenSetting.setOnClickListener {
            showConnectionDialog()
        }

        btnDisconnect.setOnClickListener {
            viewModel.disconnect()
        }

        btnHold.setOnClickListener {
            viewModel.toggleHold()
        }

        btnDTMF.setOnClickListener {
            showDTMFDialog()
        }

        btnMute.setOnClickListener {
            viewModel.toggleMute()
        }
    }


    /**
     * Observes active call LiveData and updates the UI.
     */
    private fun observeViewModel() {
        viewModel.activeCallLive.observe(this) { call ->
            callState.text = call.state.toString()

            when (call.state) {
                is CallState.Ongoing -> {
                    timer.visibility = View.VISIBLE
                    startTimer(call.answeredAt)
                }
                else -> {
                    timer.stop()
                    timer.visibility = View.GONE
                }
            }

            // Update UI visibility and button states
            val callActive = call.state !is CallState.Disconnected
            controlContainer.visibility = if (callActive) View.VISIBLE else View.GONE
            btnDisconnect.visibility = if (callActive) View.VISIBLE else View.GONE
            btnMake.visibility = if (callActive) View.GONE else View.VISIBLE
            btnMute.text = if (call.isMuted) "Unmute" else "Mute"
            btnHold.text = if (call.isOnHold) "Resume" else "Hold"
        }
    }


    /**
     * Starts or resumes the call timer based on call answer time.
     */
    @SuppressLint("SetTextI18n")
    private fun startTimer(answeredAt: Long) {
        val duration = System.currentTimeMillis() - answeredAt
        timer.base = SystemClock.elapsedRealtime() - duration
        timer.setOnChronometerTickListener(null) // No formatting magic in demo
        timer.start()
    }


    /**
     * Opens the connection config dialog.
     */
    private fun showConnectionDialog() {
        val dialog = viewModel.getAuthInfo()
            ?.let { SettingDialogFragment.newInstance(it) }
            ?: SettingDialogFragment()
        dialog.show(supportFragmentManager, "SettingDialog")
    }


    /**
     * Opens the dtmf dialog.
     */
    private fun showDTMFDialog() {
        val args = Bundle().apply {
            putString("dtmfHistory", viewModel.getDTMFHistory())
        }
        val fragment = DtmfDialogFragment()
        fragment.arguments = args
        fragment.show(supportFragmentManager, "DtmfDialog")
    }


    /**
     * Handles intent actions like "hang up".
     */
    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            "ACTION_HANGUP_CALL" -> viewModel.disconnect()
        }
    }


    /**
     * Requests required permissions (e.g. audio, notifications).
     */
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 123)
        }

        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        ActivityCompat.requestPermissions(this, permissions, 0)
    }


    /**
     * Callback from setting dialog when user enters new connection data.
     */
    override fun onDataEntered(auth: AuthInfo) {
        viewModel.setAuthInfo(auth)
    }


    /**
     * Callback from DTMF dialog when user presses a digit.
     */
    override fun onDTMFEntered(value: String) {
        viewModel.sendDTMF(value)
    }
}