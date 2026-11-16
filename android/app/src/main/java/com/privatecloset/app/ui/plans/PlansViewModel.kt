package com.privatecloset.app.ui.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecloset.app.data.model.OneOnOneSession
import com.privatecloset.app.data.model.Plan
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PlansUiData(
    val plans: List<Plan>,
    val sessions: List<OneOnOneSession>,
)

class PlansViewModel(private val repository: LearningRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<PlansUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<PlansUiData>> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val plans = repository.getPlans()
                val sessions = repository.getOneOnOneSessions()
                _uiState.value = UiState.Success(PlansUiData(plans, sessions))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Couldn't load plans")
            }
        }
    }
}
