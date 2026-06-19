package com.micmarsh.budget

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver


class SyncableMessageRepository(val database: Database) {

    fun insert(sender: String, body: String, timestamp: Long) : Long {
        return database.syncableMessageQueries.transactionWithResult {
            database.syncableMessageQueries.addUnsynced(
                sender = sender,
                body = body,
                timestamp = timestamp)
            database.syncableMessageQueries.lastId().executeAsOne()
        }
    }

    companion object {
        fun create(context: Context) : SyncableMessageRepository {
            val database = Database(AndroidSqliteDriver(Database.Schema, context, "sms_sync.db"))
            return SyncableMessageRepository(database)
        }
    }
}