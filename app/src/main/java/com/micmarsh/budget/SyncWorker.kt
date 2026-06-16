package com.micmarsh.budget

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class SyncWorker (private val context: Context, params: WorkerParameters): Worker(context, params) {
    override fun doWork(): Result {

        try {
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDate = sdf.format(Date())
            SettingsStorage.create(context).addTestString(currentDate)
        } catch (e: Exception){
            SettingsStorage.create(context).addTestString(e.message ?: "exception, but no message")
        }

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    companion object {
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