package com.ewqeqw.uvgrobertoetcamoposeco.ui.events

sealed class AssetDetailEvent {
    data class LoadAssetDetail(val assetId: String) : AssetDetailEvent()
    object RetryLoading : AssetDetailEvent()
    data class RefreshAssetDetail(val assetId: String) : AssetDetailEvent()
    object NavigateBack : AssetDetailEvent()
    data class ToggleFavorite(val assetId: String) : AssetDetailEvent()
}