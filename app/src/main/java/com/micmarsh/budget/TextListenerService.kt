package com.micmarsh.budget

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.telephony.SmsMessage
import android.util.Log

class TextListenerService : Service() {

    //todo this can probably just be a lambda passed into constructor, but get running for now
    private class Receiver : SmsReceiver() {
        override fun runAction(message: SmsMessage?) {
            Log.i("TEST MESSAGES", "RECEIVED MESSAGE ${message?.messageBody}")
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private val receiver = Receiver()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("YES", "REGISTERING TEXT RECEIVER")
        registerReceiver(receiver, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
        return START_STICKY;
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }
}