package com.micmarsh.budget


class DatabaseStorage(val database: Database) {

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
        fun create(context: Context) : DatabaseStorage {
            val database = Database(AndroidSqliteDriver(Database.Schema, context, "sms_sync.db"))
            return DatabaseStroage(database)
        }
    }
}