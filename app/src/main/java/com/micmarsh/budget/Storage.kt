package com.micmarsh.budget

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Storage(val dataStore: DataStore<Preferences>) {
    val PHONE_NUMBERS = stringSetPreferencesKey("phone_numbers")

    fun getPhoneNumbers() : Flow<Set<String>>{
        return dataStore.data.map { it[PHONE_NUMBERS] ?: setOf() }
    }

    suspend fun addPhoneNumber(number: String){
        updateSet(number) { set, item -> set.plus(item) }
    }

    private suspend fun updateSet(
        number: String,
        update: (set: Set<String>, item: String) -> Set<String>
    ){
        dataStore.updateData { it.toMutablePreferences().also { preferences ->
            val existing = preferences[PHONE_NUMBERS] ?: setOf()
            preferences[PHONE_NUMBERS] = update(existing, number)
        } }
    }

    suspend fun removePhoneNumber(number: String){
        updateSet(number) { set, item -> set.minus(item) }
    }
}