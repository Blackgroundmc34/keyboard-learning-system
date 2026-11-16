package com.privatecloset.app.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecloset.app.data.model.Course
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoursesViewModel(private val repository: LearningRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Course>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Course>>> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _uiState.value = UiState.Success(repository.getCourses())
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Couldn't load courses")
            }
        }
    }
}
