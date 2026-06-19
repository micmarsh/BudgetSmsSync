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
import kotlinx.coroutines.flow.first
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
        val workerPayload = Data.Builder()
            .putString(MESSAGE_BODY, message.messageBody)
            .putString(MESSAGE_SENDER, message.originatingAddress ?: "unknown")
            .putLong(MESSAGE_TIMESTAMP, message.timestampMillis)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<ReceivedTextWorker>()
            .setInputData(workerPayload)
            .build()

        WorkManager.getInstance(context).enqueue(syncWorkRequest)
    }
}


class ReceivedTextWorker(val context: Context, val params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val data = params.inputData
        val sender = data.getString(MESSAGE_SENDER)!!

        val storage = SettingsStorage.create(context)

        val phoneNumbers = storage.getPhoneNumbers().first()
        if (!phoneNumbers.contains(sender)){
            Log.d("RECEIVED TEXT", "$sender was not contained in set of allow phone numbers [${phoneNumbers.joinToString(", ")}]")
            return Result.success();
        }

        val body = data.getString(MESSAGE_BODY)!!
        val timestamp = data.getLong(MESSAGE_TIMESTAMP, -1) // todo error on this value?

        val db = SyncableMessageRepository.create(context)
        val insertedId = db.addUnsynced(sender, body, timestamp)

        Log.d("RECEIVED TEXT", "Saved message to db, got id $insertedId, scheduling sync")
        SyncTextWorker.scheduleSyncWork(body, insertedId, context)

        return Result.success()
    }
}

data class SyncInput(val message_text: String?)

class SyncTextWorker(val context: Context, val params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val data = params.inputData
        val body = data.getString(MESSAGE_BODY)!!
        val id = data.getLong(MESSAGE_DB_ID, -1)

        val repo = SyncableMessageRepository.create(context)

        val client = OkHttp()

        val request = Request(POST, "http://192.168.0.11:8000/sync_message_text")
            .header("Content-Type", "application/json")
            .with(bodyLens.of(SyncInput(body)))

        val response = client(request)

        Log.d("RECEIVED TEXT", "Response from server ${response.body.toString()}")

        if (response.status.successful){
            repo.markSuccessful(id)
            return Result.success()
        } else {
            repo.markFailed(id, response.body.toString())
            return Result.failure() // todo retry policy in builder (and builder into own method)
        }
    }

    companion object {
        fun scheduleSyncWork(body: String, id: Long, context: Context){
            val workerPayload = Data.Builder()
                .putString(MESSAGE_BODY, body)
                .putLong(MESSAGE_DB_ID, id)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncTextWorker>()
                .setInputData(workerPayload)
                .setConstraints(Constraints(requiredNetworkType = NetworkType.UNMETERED))
                .build()

            WorkManager.getInstance(context).enqueue(syncWorkRequest)
        }

        private val bodyLens = Jackson.autoBody<SyncInput>().toLens()
    }
}