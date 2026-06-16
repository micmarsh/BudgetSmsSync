package com.micmarsh.budget

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.last
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class SyncWorker (private val context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {

        val storage = SettingsStorage.create(context)
        logTestString(storage)

        val lastSyncDate = initialLastSyncDate(storage)
        val phoneNumbers = storage.getPhoneNumbers().last()
        val newMessages = getMessages(lastSyncDate, phoneNumbers)
        storage.addTestStrings(newMessages.map { it.content })

        return Result.success()
    }

    private suspend fun getMessages(
        lastSyncDate: Date,
        phoneNumbers: Set<String>
    ) : List<SmsMessage>  {
        TODO("Not yet implemented")
    }

    companion object {

        private suspend fun initialLastSyncDate(storage: LastSyncDateStorage): Date {
           val date = storage.getLastSyncDate().last()
            if (date.time == 0L){
                val currentDate = Date(System.currentTimeMillis())
                storage.setLastSyncDate(currentDate)
                return currentDate
            }
            return date
        }

        private val MINIMUM_ALLOWED_MINUTES:Long = 15
        fun create() : PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            return PeriodicWorkRequestBuilder<SyncWorker>(
                MINIMUM_ALLOWED_MINUTES,
                TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        }
    }
}

data class SmsMessage(val content : String)

private fun logTestString(storage: TestStorage) {
    try {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        storage.addTestString(currentDate)
    } catch (e: Exception) {
        storage.addTestString(e.message ?: "exception, but no message")
    }
}