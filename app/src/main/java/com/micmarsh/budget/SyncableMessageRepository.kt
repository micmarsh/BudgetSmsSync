package com.micmarsh.budget

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver


class SyncableMessageRepository(val database: Database) {

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

    fun markFailed(id: Long){
        database.syncableMessageQueries.updateStatus(sync_status = SyncStatus.Failed.value.toLong(),
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