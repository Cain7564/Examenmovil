package com.ewqeqw.uvgrobertoetcamoposeco.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ewqeqw.uvgrobertoetcamoposeco.model.Asset
import com.ewqeqw.uvgrobertoetcamoposeco.repository.AssetRepository
import com.ewqeqw.uvgrobertoetcamoposeco.ui.events.AssetListEvent
import com.ewqeqw.uvgrobertoetcamoposeco.ui.events.NavigationEvent
import com.ewqeqw.uvgrobertoetcamoposeco.ui.state.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AssetListViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AssetRepository(application.applicationContext)

    private val _state = MutableStateFlow<UiState<List<Asset>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Asset>>> = _state.asStateFlow()

    private val _savedAt = MutableStateFlow<String?>(null)
    val savedAt: StateFlow<String?> = _savedAt.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {

        onEvent(AssetListEvent.LoadAssets)
    }

    fun onEvent(event: AssetListEvent) {
        when (event) {
            is AssetListEvent.LoadAssets -> loadAssetsInternal()
            is AssetListEvent.SaveOffline -> saveOfflineInternal()
            is AssetListEvent.RetryLoading -> retryLoading()
            is AssetListEvent.RefreshData -> refreshData()
            is AssetListEvent.NavigateToDetail -> navigateToDetail(event.assetId)
        }
    }

    private fun loadAssetsInternal(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _state.value = UiState.Loading
            }

            try {
                val assets = repo.fetchAssetsOnline()
                _state.value = UiState.Success(assets)
                _savedAt.value = null
            } catch (e: Exception) {
                handleApiError(e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun saveOfflineInternal() {
        viewModelScope.launch {
            try {
                val currentAssets = (_state.value as? UiState.Success<List<Asset>>)?.data
                if (currentAssets != null) {
                    repo.saveAssetsForOffline(currentAssets)
                    updateSavedAtTimestamp()
                }
            } catch (e: Exception) {

            }
        }
    }


    private fun retryLoading() {
        loadAssetsInternal(showLoading = true)
    }

    private fun refreshData() {
        _isRefreshing.value = true
        loadAssetsInternal(showLoading = false)
    }
    private fun navigateToDetail(assetId: String) {
        viewModelScope.launch {
            _navigationEvent.send(NavigationEvent.NavigateToAssetDetail(assetId))
        }
    }
    private suspend fun handleApiError(exception: Exception) {
        try {
            val (offlineAssets, savedAtIso) = repo.loadOfflineAssets()
            if (offlineAssets != null && offlineAssets.isNotEmpty()) {
                _state.value = UiState.Success(offlineAssets)
                _savedAt.value = repo.formatSavedAt(savedAtIso)
            } else {
                _state.value = UiState.Error(
                    "No se pudo conectar al servidor y no hay datos guardados offline"
                )
            }
        } catch (localException: Exception) {
            _state.value = UiState.Error(
                "Error de conexión: ${exception.message ?: "Error desconocido"}"
            )
        }
    }
    private suspend fun updateSavedAtTimestamp() {
        val (_, savedAtIso) = repo.loadOfflineAssets()
        _savedAt.value = repo.formatSavedAt(savedAtIso)
    }

    fun loadAssets() = onEvent(AssetListEvent.LoadAssets)
    fun saveOffline() = onEvent(AssetListEvent.SaveOffline)
}