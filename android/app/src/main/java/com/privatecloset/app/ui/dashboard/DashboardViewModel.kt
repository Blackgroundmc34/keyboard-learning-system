package com.privatecloset.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecloset.app.data.model.Lesson
import com.privatecloset.app.data.model.Student
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiData(
    val student: Student,
    val continueLearningLesson: Lesson,
)

class DashboardViewModel(private val repository: LearningRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<DashboardUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardUiData>> = _uiState
    val connectionState = repository.getConnectionState()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val student = repository.getStudent()
                val lesson = repository.getContinueLearningLesson()
                _uiState.value = UiState.Success(DashboardUiData(student, lesson))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Something went wrong")
            }
        }
    }
}
