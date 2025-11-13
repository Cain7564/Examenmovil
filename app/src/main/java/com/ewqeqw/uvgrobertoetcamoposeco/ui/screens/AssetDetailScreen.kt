package com.ewqeqw.uvgrobertoetcamoposeco.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ewqeqw.uvgrobertoetcamoposeco.model.Asset
import com.ewqeqw.uvgrobertoetcamoposeco.ui.components.DetailInfoCard
import com.ewqeqw.uvgrobertoetcamoposeco.ui.events.AssetDetailEvent
import com.ewqeqw.uvgrobertoetcamoposeco.ui.events.NavigationEvent
import com.ewqeqw.uvgrobertoetcamoposeco.ui.state.UiState
import com.ewqeqw.uvgrobertoetcamoposeco.ui.viewmodel.AssetDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    viewModel: AssetDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedAt by viewModel.savedAt.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentAssetId by viewModel.currentAssetId.collectAsStateWithLifecycle()

    // Manejar eventos de navegación
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateBack -> {
                    onNavigateBack()
                }

                else -> {
                    // Otros eventos de navegación se manejarían aquí
                }
            }
        }
    }

    // Manejar eventos de UI (como snackbars)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            // Aquí podrías mostrar un snackbar con el mensaje
            // Por ahora solo lo ignoramos
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalles del Activo",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.onEvent(AssetDetailEvent.NavigateBack) }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            when (val currentState = state) {
                is UiState.Loading -> LoadingState()
                is UiState.Error -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(AssetDetailEvent.RetryLoading) },
                    onBack = { viewModel.onEvent(AssetDetailEvent.NavigateBack) }
                )

                is UiState.Success -> AssetDetailsContent(
                    asset = currentState.data,
                    savedAt = savedAt,
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        currentAssetId?.let { assetId ->
                            viewModel.onEvent(AssetDetailEvent.RefreshAssetDetail(assetId))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Cargando detalles...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Error",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    message,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onBack) {
                        Text("Volver")
                    }
                    Button(onClick = onRetry) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssetDetailsContent(
    asset: Asset,
    savedAt: String?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { AssetHeader(asset, savedAt) }
            item { PriceCard(asset) }
            item { ChangeCard(asset) }
            if (asset.rank != null) {
                item { DetailInfoCard("Ranking", "#${asset.rank}") }
            }
            item { DetailInfoCard("Supply", asset.supply ?: "-") }
            item { DetailInfoCard("Max Supply", asset.maxSupply ?: "Sin límite") }
            item {
                DetailInfoCard("Market Cap USD", asset.marketCapUsd?.let {
                    val value = it.toDoubleOrNull() ?: 0.0
                    "\$${String.format("%,.0f", value)}"
                } ?: "-")
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun AssetHeader(asset: Asset, savedAt: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusBadge(savedAt)
            Spacer(modifier = Modifier.height(16.dp))
            AssetAvatar(asset.symbol)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                asset.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                asset.symbol,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StatusBadge(savedAt: String?) {
    if (savedAt != null) {
        Text(
            "Datos guardados - $savedAt",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    } else {
        Text(
            "Datos en tiempo real",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4CAF50),
            modifier = Modifier
                .background(
                    Color(0xFF4CAF50).copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun AssetAvatar(symbol: String) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol.take(3),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun PriceCard(asset: Asset) {
    DetailInfoCard("Precio USD", asset.priceUsd?.let {
        "\$${String.format("%.4f", it.toDoubleOrNull() ?: 0.0)}"
    } ?: "-")
}

@Composable
private fun ChangeCard(asset: Asset) {
    val change = asset.changePercent24Hr?.toDoubleOrNull()
    if (change != null) {
        val isPositive = change >= 0
        val changeColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = changeColor.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = changeColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Cambio 24h",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "${if (isPositive) "+" else ""}${String.format("%.2f", change)}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                }
            }
        }
    }
}