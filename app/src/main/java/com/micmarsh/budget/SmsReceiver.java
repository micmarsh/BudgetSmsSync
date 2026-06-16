package com.micmarsh.budget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import kotlin.Unit;

// copied from https://stackoverflow.com/q/39577427 so as not to waste time translating back and forth from Kotlin
public abstract class SmsReceiver extends BroadcastReceiver {

    public abstract Unit runAction(SmsMessage message);
    
    @Override
    public void onReceive(Context context, Intent intent) {

        if(! intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
            return;
        
        String telnr = "", nachricht = "";

        Bundle extras = intent.getExtras();

        if (extras != null) {
            Object[] pdus = (Object[]) extras.get("pdus");
            if (pdus != null) {

                for (Object pdu : pdus) {
                    SmsMessage smsMessage = getIncomingMessage(pdu, extras);
                    telnr = smsMessage.getDisplayOriginatingAddress();
                    nachricht += smsMessage.getDisplayMessageBody();

                    runAction(smsMessage);
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
