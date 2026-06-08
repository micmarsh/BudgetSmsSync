package com.example.budget

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

class TextListenerService : Service() {
    val smsReceiver = SmsReceiver()

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerReceiver(smsReceiver, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
        return START_STICKY;//super.onStartCommand(intent, flags, startId)
    }
}