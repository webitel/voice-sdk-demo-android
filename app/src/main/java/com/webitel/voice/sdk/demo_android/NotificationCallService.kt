package com.webitel.voice.sdk.demo_android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat


class NotificationCallService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var isForegroundStarted = false

    private val notificationId: Int
        get() {
            return (System.currentTimeMillis() % 10000).toInt()
        }


    override fun onCreate() {
        super.onCreate()
        acquireProximityWakeLock()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isForegroundStarted) {
            isForegroundStarted = true
            startForegroundWithNotification()
        }
        when (intent?.action) {
            ACTION_HANGUP_CALL -> stopSelf()
            ACTION_STOP_SERVICE -> stopSelf()
        }
        return START_NOT_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? = null


    override fun onDestroy() {
        super.onDestroy()
        releaseProximityWakeLock()
    }


    /**
     * Starts the service in the foreground with a call-style notification.
     */
    private fun startForegroundWithNotification() {
        val channelId = "call_notification_channel"
        val channelName = "Call Notification"

        createNotificationChannel(channelId, channelName)
        val notification = buildNotification(channelId)

        startForeground(notificationId, notification)
    }


    /**
     * Creates a notification channel for Android O and above.
     */
    private fun createNotificationChannel(id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }


    /**
     * Builds a call-style notification depending on Android version.
     */
    private fun buildNotification(channelId: String): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val caller = Person.Builder()
                .setName("Webitel")
                .setUri("Service")
                .setImportant(true)
                .build()

            Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_call)
                .setStyle(Notification.CallStyle.forOngoingCall(caller, getHangupPendingIntent()))
                .addPerson(caller)
                .build()
        } else {
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_call)
                .setContentTitle("Webitel")
                .setContentText("Service")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .build()
        }
    }


    /**
     * Creates a PendingIntent to hang up the call.
     */
    private fun getHangupPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_HANGUP_CALL
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    /**
     * Acquires a proximity wake lock to keep the screen off during a call.
     */
    private fun acquireProximityWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "CallNotificationService::ProximityLock"
        ).apply {
            acquire()
        }
    }


    /**
     * Releases the proximity wake lock if it's held.
     */
    private fun releaseProximityWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }


    companion object {
        private const val ACTION_HANGUP_CALL = "ACTION_HANGUP_CALL"
        private const val ACTION_STOP_SERVICE = "ACTION_STOP_NOW"
    }
}