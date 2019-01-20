package com.ysn.belajarotp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private var smsListener: SmsListener? = null

        fun bindListener(smsListener: SmsListener) {
            this.smsListener = smsListener
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent?.extras
        val pdus = extras?.get("pdus") as Array<*>
        for (item in pdus) {
            val smsMessage: SmsMessage
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val format = extras.getString("format")
                smsMessage = SmsMessage.createFromPdu(item as ByteArray, format)
            } else {
                smsMessage = SmsMessage.createFromPdu(item as ByteArray)
            }
            val message = smsMessage.messageBody
            smsListener?.messageReceived(message)
        }
    }

}