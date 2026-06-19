package com.micmarsh.budget

import android.content.Context
import android.telephony.SmsMessage
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.http4k.client.OkHttp
import org.http4k.core.Request
import org.http4k.core.Method.POST
import org.http4k.core.with
import org.http4k.format.Jackson

private val MESSAGE_BODY = "sms_message_body"
private val MESSAGE_SENDER = "sms_message_sender"
private val MESSAGE_TIMESTAMP = "sms_message_date"

class TextListenerReceiver : SmsReceiver() {
    override fun runAction(context: Context, message: SmsMessage) {
        val workerPayload = Data.Builder()
            .putString(MESSAGE_BODY, message.messageBody)
            .putString(MESSAGE_SENDER, message.originatingAddress)
            .putLong(MESSAGE_TIMESTAMP, message.timestampMillis)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReceivedTextWorker>()
            .setInputData(workerPayload)
            //TODO introduce two workers!? first sticks in db, doesn't need unmetered, then dispatches to actual sync request that does
            .setConstraints(Constraints(requiredNetworkType = NetworkType.UNMETERED))
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}

data class SyncInput(val message_text: String?)

class ReceivedTextWorker(val context: Context, val params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val data = params.inputData
        val sender = data.getString(MESSAGE_SENDER)!!
        val body = data.getString(MESSAGE_BODY)!!
        val timestamp = data.getLong(MESSAGE_TIMESTAMP, -1) // todo error on this value?

        Log.i("TEST RUNNING WORKER FROM RECEIVER", "$sender: $body")
        val db = DatabaseStorage.create(context)
        val insertedId = db.insert(sender, body, timestamp)


        val client = OkHttp()

        val request = Request(POST, "http://192.168.0.11:8000/sync_message_text")
            .header("Content-Type", "application/json")
            .with(bodyLens.of(SyncInput(body)))

        val response = client(request)

        Log.i("TEST RESPONSE FROM SERVER", response.body.toString())

        return Result.success()
    }

    companion object {
        private val bodyLens = Jackson.autoBody<SyncInput>().toLens()
    }
}