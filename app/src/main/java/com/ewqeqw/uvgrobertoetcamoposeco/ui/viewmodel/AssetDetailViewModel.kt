package com.ewqeqw.uvgrobertoetcamoposeco.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ewqeqw.uvgrobertoetcamoposeco.model.Asset
import com.ewqeqw.uvgrobertoetcamoposeco.repository.AssetRepository
import com.ewqeqw.uvgrobertoetcamoposeco.ui.events.AssetDetailEvent
import com.ewqeqw.uvgrobertoetcamoposeco.ui.events.NavigationEvent
import com.ewqeqw.uvgrobertoetcamoposeco.ui.state.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AssetDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AssetRepository(application.applicationContext)

    private val _state = MutableStateFlow<UiState<Asset>>(UiState.Loading)
    val state: StateFlow<UiState<Asset>> = _state.asStateFlow()

    private val _savedAt = MutableStateFlow<String?>(null)
    val savedAt: StateFlow<String?> = _savedAt.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _currentAssetId = MutableStateFlow<String?>(null)
    val currentAssetId: StateFlow<String?> = _currentAssetId.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: AssetDetailEvent) {
        when (event) {
            is AssetDetailEvent.LoadAssetDetail -> loadAssetDetail(event.assetId)
            is AssetDetailEvent.RetryLoading -> retryLoading()
            is AssetDetailEvent.RefreshAssetDetail -> refreshAssetDetail(event.assetId)
            is AssetDetailEvent.NavigateBack -> navigateBack()
            is AssetDetailEvent.ToggleFavorite -> toggleFavorite(event.assetId)
        }
    }

    private fun loadAssetDetail(assetId: String, showLoading: Boolean = true) {
        viewModelScope.launch {
            _currentAssetId.value = assetId

            if (showLoading) {
                _state.value = UiState.Loading
            }

            try {
                val asset = repo.fetchAssetOnline(assetId)
                if (asset != null) {
                    _state.value = UiState.Success(asset)
                    _savedAt.value = null
                } else {
                    _state.value = UiState.Error("Asset no encontrado")
                }
            } catch (e: Exception) {
                handleApiError(assetId, e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun retryLoading() {
        val assetId = _currentAssetId.value
        if (assetId != null) {
            loadAssetDetail(assetId, showLoading = true)
        }
    }

    private fun refreshAssetDetail(assetId: String) {
        _isRefreshing.value = true
        loadAssetDetail(assetId, showLoading = false)
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _navigationEvent.send(NavigationEvent.NavigateBack)
        }
    }

    private fun toggleFavorite(assetId: String) {
        viewModelScope.launch {
            try {

                _uiEvent.send("Funcionalidad de favoritos próximamente")
            } catch (e: Exception) {
                _uiEvent.send("Error al procesar favorito")
            }
        }
    }

    private suspend fun handleApiError(assetId: String, exception: Exception) {
        try {
            val (offlineAssets, savedAtIso) = repo.loadOfflineAssets()
            val offlineAsset = offlineAssets?.firstOrNull { it.id == assetId }

            if (offlineAsset != null) {
                _state.value = UiState.Success(offlineAsset)
                _savedAt.value = repo.formatSavedAt(savedAtIso)
            } else {
                _state.value = UiState.Error(
                    "No se pudo conectar al servidor y no hay datos guardados para este asset"
                )
            }
        } catch (localException: Exception) {
            _state.value = UiState.Error(
                "Error de conexión: ${exception.message ?: "Error desconocido"}"
            )
        }
    }

    fun loadAsset(id: String) = onEvent(AssetDetailEvent.LoadAssetDetail(id))
}