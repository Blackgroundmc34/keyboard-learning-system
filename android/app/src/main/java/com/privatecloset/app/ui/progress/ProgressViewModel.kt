package com.privatecloset.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecloset.app.data.model.ProgressSnapshot
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProgressViewModel(private val repository: LearningRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ProgressSnapshot>>(UiState.Loading)
    val uiState: StateFlow<UiState<ProgressSnapshot>> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _uiState.value = UiState.Success(repository.getProgressSnapshot())
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Couldn't load progress")
            }
        }
    }
}
