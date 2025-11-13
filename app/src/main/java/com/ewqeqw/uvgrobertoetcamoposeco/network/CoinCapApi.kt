package com.ewqeqw.uvgrobertoetcamoposeco.network

import com.ewqeqw.uvgrobertoetcamoposeco.model.Asset
import com.ewqeqw.uvgrobertoetcamoposeco.model.AssetsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

class CoinCapApi {
    private val jsonConfig = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.coincap.io"
            }
            header(
                "Authorization",
                "Bearer 6f8c2f757cc81e9950a05aeed8292abff853114ebc731977f3f5a580b1e9371a"
            )
        }
    }

    suspend fun getAssets(): List<Asset> {
        return try {
            val res: AssetsResponse = client.get("/v2/assets").body()
            res.data
        } catch (e: Exception) {
            getMockAssets()
        }
    }

    suspend fun getAssetById(id: String): Asset? {
        return try {
            val res: AssetsResponse = client.get("/v2/assets/$id").body()
            res.data.firstOrNull()
        } catch (e: Exception) {
            getMockAssets().firstOrNull { it.id == id }
        }
    }

    private suspend fun getMockAssets(): List<Asset> {
        delay(1000)
        return listOf(
            Asset(
                id = "bitcoin",
                rank = "1",
                symbol = "BTC",
                name = "Bitcoin",
                supply = "19500000",
                maxSupply = "21000000",
                marketCapUsd = "800000000000",
                priceUsd = "41000.50",
                changePercent24Hr = "2.34"
            ),
            Asset(
                id = "ethereum",
                rank = "2",
                symbol = "ETH",
                name = "Ethereum",
                supply = "120000000",
                maxSupply = null,
                marketCapUsd = "250000000000",
                priceUsd = "2100.75",
                changePercent24Hr = "-1.45"
            ),
            Asset(
                id = "cardano",
                rank = "3",
                symbol = "ADA",
                name = "Cardano",
                supply = "35000000000",
                maxSupply = "45000000000",
                marketCapUsd = "20000000000",
                priceUsd = "0.58",
                changePercent24Hr = "3.21"
            )
        )
    }
}