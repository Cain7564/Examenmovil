package com.ewqeqw.uvgrobertoetcamoposeco.repository

import android.content.Context
import com.ewqeqw.uvgrobertoetcamoposeco.data.LocalStore
import com.ewqeqw.uvgrobertoetcamoposeco.model.Asset
import com.ewqeqw.uvgrobertoetcamoposeco.network.CoinCapApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetRepository(private val context: Context) {
    private val api = CoinCapApi()
    private val local = LocalStore(context)

    suspend fun fetchAssetsOnline(): List<Asset> = withContext(Dispatchers.IO) {
        api.getAssets()
    }

    suspend fun fetchAssetOnline(id: String): Asset? = withContext(Dispatchers.IO) {
        api.getAssetById(id)
    }

    suspend fun saveAssetsForOffline(assets: List<Asset>) {
        val nowIso = java.time.Instant.now().toString()
        local.saveAssets(assets, nowIso)
    }

    suspend fun loadOfflineAssets(): Pair<List<Asset>?, String?> {
        return local.loadAssets()
    }

    fun formatSavedAt(iso: String?): String? {
        if (iso == null) return null
        return try {
            val instant = java.time.Instant.parse(iso)
            val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(java.time.ZoneId.systemDefault())
            fmt.format(instant)
        } catch (e: Exception) {
            null
        }
    }
}