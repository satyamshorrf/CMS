package com.cms.canteenapp.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREF_NAME)

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_USER_ID)] = userId
        }
    }

    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_USER_ID)]
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}