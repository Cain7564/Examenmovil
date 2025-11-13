package com.ewqeqw.uvgrobertoetcamoposeco.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Asset(
    val id: String,
    val rank: String? = null,
    val symbol: String,
    val name: String,
    @SerialName("supply") val supply: String? = null,
    @SerialName("maxSupply") val maxSupply: String? = null,
    @SerialName("marketCapUsd") val marketCapUsd: String? = null,
    @SerialName("priceUsd") val priceUsd: String? = null,
    @SerialName("changePercent24Hr") val changePercent24Hr: String? = null
)

@Serializable
data class AssetsResponse(
    val data: List<Asset>
)