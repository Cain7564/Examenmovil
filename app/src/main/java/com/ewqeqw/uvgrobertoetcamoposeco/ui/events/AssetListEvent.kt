package com.ewqeqw.uvgrobertoetcamoposeco.ui.events
sealed class AssetListEvent {
    object LoadAssets : AssetListEvent()
    object SaveOffline : AssetListEvent()
    object RetryLoading : AssetListEvent()
    object RefreshData : AssetListEvent()
    data class NavigateToDetail(val assetId: String) : AssetListEvent()
}