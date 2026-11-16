package com.privatecloset.app.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecloset.app.data.model.MarketplaceCategory
import com.privatecloset.app.data.model.MarketplaceItem
import com.privatecloset.app.data.model.PaymentMethod
import com.privatecloset.app.data.model.PaymentTransaction
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MarketplaceUiData(
    val items: List<MarketplaceItem>,
    val selectedCategory: MarketplaceCategory = MarketplaceCategory.ALL,
    val searchQuery: String = "",
    val lastTransaction: PaymentTransaction? = null,
)

class MarketplaceViewModel(
    private val repository: LearningRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<MarketplaceUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<MarketplaceUiData>> = _uiState.asStateFlow()

    init {
        loadMarketplace()
    }

    fun loadMarketplace() {
        viewModelScope.launch {
            try {
                val items = repository.getMarketplaceItems()
                _uiState.value = UiState.Success(
                    MarketplaceUiData(items = items)
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load marketplace")
            }
        }
    }

    fun setCategory(category: MarketplaceCategory) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(selectedCategory = category))
    }

    fun setSearchQuery(query: String) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(searchQuery = query))
    }

    fun purchaseItem(
        itemId: String,
        method: PaymentMethod,
        registeredDetails: String? = null,
        onComplete: (Result<PaymentTransaction>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = repository.purchaseMarketplaceItem(itemId, method, registeredDetails)
            if (result.isSuccess) {
                val tx = result.getOrNull()
                val current = (_uiState.value as? UiState.Success)?.data
                if (current != null) {
                    val updatedItems = repository.getMarketplaceItems()
                    _uiState.value = UiState.Success(
                        current.copy(items = updatedItems, lastTransaction = tx)
                    )
                }
            }
            onComplete(result)
        }
    }

    fun publishItem(item: MarketplaceItem, onComplete: (Result<MarketplaceItem>) -> Unit) {
        viewModelScope.launch {
            val res = repository.publishMarketplaceItem(item)
            if (res.isSuccess) {
                val current = (_uiState.value as? UiState.Success)?.data
                if (current != null) {
                    val updated = repository.getMarketplaceItems()
                    _uiState.value = UiState.Success(current.copy(items = updated))
                }
            }
            onComplete(res)
        }
    }
}
