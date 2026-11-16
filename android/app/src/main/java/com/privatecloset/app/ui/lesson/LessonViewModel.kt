package com.privatecloset.app.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecloset.app.data.model.Lesson
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LessonViewModel(private val repository: LearningRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Lesson>>(UiState.Loading)
    val uiState: StateFlow<UiState<Lesson>> = _uiState

    fun load(lessonId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val lesson = repository.getLesson(lessonId) ?: repository.getContinueLearningLesson()
                _uiState.value = UiState.Success(lesson)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Couldn't load lesson")
            }
        }
    }

    fun markComplete(lessonId: String) {
        viewModelScope.launch {
            repository.markLessonComplete(lessonId)
        }
    }
}
