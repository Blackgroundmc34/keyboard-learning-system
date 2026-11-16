package com.privatecloset.app.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecloset.app.data.model.PracticeExercise
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PracticeViewModel(private val repository: LearningRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<PracticeExercise>>(UiState.Loading)
    val uiState: StateFlow<UiState<PracticeExercise>> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _uiState.value = UiState.Success(repository.getPracticeExercise())
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Couldn't load practice exercise")
            }
        }
    }
}
