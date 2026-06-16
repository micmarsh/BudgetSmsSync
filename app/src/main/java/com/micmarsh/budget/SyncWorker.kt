package com.micmarsh.budget

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class SyncWorker (context: Context, params: WorkerParameters): Worker(context, params) {
    override fun doWork(): Result {

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())

        Log.i("SYNC_WORKER", "Running sync worker! ${sdf.format(currentDate)}")

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    companion object {
        private val MINIMUM_ALLOWED_MINUTES:Long = 15
        fun create() : WorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            return PeriodicWorkRequestBuilder<SyncWorker>(
                MINIMUM_ALLOWED_MINUTES,
                TimeUnit.MINUTES) // flexInterval
                .build()
        }
    }
}