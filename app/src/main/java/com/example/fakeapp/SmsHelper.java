package com.example.fakeapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SmsHelper {

    public static void sendSilentSMS(Context context, List<String> phoneNumbers) {
        String message = "Your device is stolen. Check your Pre-configured email for the thief's image, recorded audio, and location. Check your spam folder as well. If not received, the device may not have had internet access.";
        SmsManager smsManager = SmsManager.getDefault();

        for (String phone : phoneNumbers) {
            PendingIntent sentPI = PendingIntent.getBroadcast(
                    context, 0, new Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(
                    context, 0, new Intent("SMS_DELIVERED"), PendingIntent.FLAG_IMMUTABLE);

            smsManager.sendTextMessage(phone, null, message, sentPI, deliveredPI);
        }
    }

}
