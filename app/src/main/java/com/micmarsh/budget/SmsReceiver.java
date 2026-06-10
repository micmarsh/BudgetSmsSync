package com.micmarsh.budget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

// copied from https://stackoverflow.com/q/39577427 so as not to waste time translating back and forth from Kotlin
public abstract class SmsReceiver extends BroadcastReceiver {

    public abstract Unit runAction(SmsMessage message);
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String telnr = "", nachricht = "";

        Bundle extras = intent.getExtras();

        Log.i("TEST NON-CALLBACK", "onReceive fired!!!");

        if (extras != null) {
            Object[] pdus = (Object[]) extras.get("pdus");
            if (pdus != null) {

                for (Object pdu : pdus) {
                    SmsMessage smsMessage = getIncomingMessage(pdu, extras);
                    telnr = smsMessage.getDisplayOriginatingAddress();
                    nachricht += smsMessage.getDisplayMessageBody();

                    runAction(smsMessage);
                    Log.i("TEST NON-CALLBACK", smsMessage.getDisplayMessageBody());
                }
            }
        }
    }

    private SmsMessage getIncomingMessage(Object object, Bundle bundle) {
        SmsMessage smsMessage;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            smsMessage = SmsMessage.createFromPdu((byte[]) object, format);
        } else {
            smsMessage = SmsMessage.createFromPdu((byte[]) object);
        }

        return smsMessage;
    }
}