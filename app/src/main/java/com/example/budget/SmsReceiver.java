package com.example.budget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

// copied from https://stackoverflow.com/q/39577427 so as not to waste time translating back and forth from Kotlin
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String telnr = "", nachricht = "";

        Bundle extras = intent.getExtras();

        if (extras != null) {
            Object[] pdus = (Object[]) extras.get("pdus");
            if (pdus != null) {

                for (Object pdu : pdus) {
                    SmsMessage smsMessage = getIncomingMessage(pdu, extras);
                    telnr = smsMessage.getDisplayOriginatingAddress();
                    nachricht += smsMessage.getDisplayMessageBody();

                    Log.i("TEST_TEXT", smsMessage.getDisplayMessageBody());
                }

                // Here the message content is processed within MainAct
                //MainAct.instance().processSMS(telnr.replace("+49", "0").replace(" ", ""), nachricht);
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