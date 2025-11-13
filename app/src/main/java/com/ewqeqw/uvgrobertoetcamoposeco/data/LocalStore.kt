package com.ewqeqw.uvgrobertoetcamoposeco.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.ewqeqw.uvgrobertoetcamoposeco.model.Asset

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("assets_store")

class LocalStore(private val context: Context) {
    private val JSON = Json { ignoreUnknownKeys = true }

    companion object Keys {
        val ASSETS_JSON = stringPreferencesKey("assets_json")
        val SAVED_AT = stringPreferencesKey("saved_at")
    }

    suspend fun saveAssets(assets: List<Asset>, savedAtIso: String) {
        val string = JSON.encodeToString(assets)
        context.dataStore.edit { prefs ->
            prefs[ASSETS_JSON] = string
            prefs[SAVED_AT] = savedAtIso
        }
    }

    suspend fun loadAssets(): Pair<List<Asset>?, String?> {
        val prefs = context.dataStore.data.map { it }.first()
        val json = prefs[ASSETS_JSON]
        val savedAt = prefs[SAVED_AT]
        return if (json != null) {
            try {
                val list = JSON.decodeFromString<List<Asset>>(json)
                list to savedAt
            } catch (e: Exception) {
                null to null
            }
        } else {
            null to null
        }
    }
}