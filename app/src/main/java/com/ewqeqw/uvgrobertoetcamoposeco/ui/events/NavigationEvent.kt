package com.ewqeqw.uvgrobertoetcamoposeco.ui.events

sealed class NavigationEvent {

    data class NavigateToAssetDetail(val assetId: String) : NavigationEvent()


    object NavigateBack : NavigationEvent()


    object NavigateToHome : NavigationEvent()

    object NavigateToHomeAndClearStack : NavigationEvent()
}