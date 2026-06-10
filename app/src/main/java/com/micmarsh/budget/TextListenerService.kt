package com.micmarsh.budget

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log

class TextListenerService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val smsReceiver = SmsReceiver {message ->
            Log.i("TEST RECEIVING MESSAGES", message.messageBody)
        }

        registerReceiver(smsReceiver, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
        return START_STICKY;
    }
}