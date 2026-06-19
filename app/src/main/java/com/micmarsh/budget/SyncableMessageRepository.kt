package com.micmarsh.budget

import android.content.Context
import androidx.lifecycle.ViewModel
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow


class SyncableMessageRepository(val database: Database) {

    fun getAll() : Flow<List<SyncableMessage>>{
        return database.syncableMessageQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    fun addUnsynced(sender: String, body: String, timestamp: Long) : Long {
        return database.syncableMessageQueries.transactionWithResult {
            database.syncableMessageQueries.add(
                sender = sender,
                body = body,
                timestamp = timestamp,
                sync_status = SyncStatus.Pending.value.toLong())
            database.syncableMessageQueries.lastId().executeAsOne()
        }
    }

    fun markSuccessful(id: Long){
        database.syncableMessageQueries.updateStatus(sync_status = SyncStatus.Successful.value.toLong(),
            rowid = id)
    }

    fun markPending(id: Long){
        database.syncableMessageQueries.updateStatus(sync_status = SyncStatus.Pending.value.toLong(),
            rowid = id)
    }

    fun markFailed(id: Long, message: String){
        database.syncableMessageQueries.updateStatusWithError(sync_status = SyncStatus.Failed.value.toLong(),
            last_error = message,
            rowid = id)
    }

    companion object {
        fun create(context: Context) : SyncableMessageRepository {
            val database = Database(AndroidSqliteDriver(Database.Schema, context, "sms_sync.db"))
            return SyncableMessageRepository(database)
        }
    }
}

enum class SyncStatus(val value: Int) {
    Pending(1),
    Successful(2),
    Failed(3)
}