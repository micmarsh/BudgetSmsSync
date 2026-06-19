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
import org.http4k.client.OkHttp
import org.http4k.core.Request
import org.http4k.core.Method.POST
import org.http4k.core.with
import org.http4k.format.Jackson

private val MESSAGE_BODY = "sms_message_body"
private val MESSAGE_SENDER = "sms_message_sender"
private val MESSAGE_TIMESTAMP = "sms_message_date"
private val MESSAGE_DB_ID = "sms_message_db_id"


class TextListenerReceiver : SmsReceiver() {
    override fun runAction(context: Context, message: SmsMessage) {
        val body = message.messageBody
        val sender = message.originatingAddress ?: "unknown"
        val timestamp = message.timestampMillis

        val db = SyncableMessageRepository.create(context)
        val insertedId = db.addUnsynced(sender, body, timestamp)

        val workerPayload = Data.Builder()
            .putString(MESSAGE_BODY, body)
            .putString(MESSAGE_SENDER, sender)
            .putLong(MESSAGE_TIMESTAMP, timestamp)
            .putLong(MESSAGE_DB_ID, insertedId)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReceivedTextWorker>()
            .setInputData(workerPayload)
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
        val repo = SyncableMessageRepository.create(context)


        val client = OkHttp()

        val request = Request(POST, "http://192.168.0.11:8000/sync_message_text")
            .header("Content-Type", "application/json")
            .with(bodyLens.of(SyncInput(body)))

        val response = client(request)
        
        if (response.status.successful){
            repo.markSuccessful(data.getLong(MESSAGE_DB_ID, -1))
            return Result.success()
        } else {
            repo.markFailed(data.getLong(MESSAGE_DB_ID, -1))
            return Result.retry() // todo retry policy in builder (and builder into own method)
        }

        Log.i("TEST RESPONSE FROM SERVER", response.body.toString())

    }

    companion object {
        private val bodyLens = Jackson.autoBody<SyncInput>().toLens()
    }
}