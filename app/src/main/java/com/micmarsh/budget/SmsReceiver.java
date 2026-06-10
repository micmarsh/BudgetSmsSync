package com.micmarsh.budget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

// copied from https://stackoverflow.com/q/39577427 so as not to waste time translating back and forth from Kotlin
public class SmsReceiver extends BroadcastReceiver {

    private final Function1<SmsMessage, Unit> runAction;

    public SmsReceiver(Function1<SmsMessage, Unit> runAction){
        this.runAction = runAction;
    }
    
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

                    runAction.invoke(smsMessage);
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