package com.privatecloset.app.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecloset.app.data.model.Course
import com.privatecloset.app.data.model.Student
import com.privatecloset.app.data.model.TutorCourseDraft
import com.privatecloset.app.data.model.TutorStats
import com.privatecloset.app.data.model.UserRole
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TutorStudioUiData(
    val tutor: Student,
    val stats: TutorStats,
    val myCourses: List<Course>,
    val isTutorAuthorized: Boolean,
)

class TutorStudioViewModel(
    private val repository: LearningRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<TutorStudioUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<TutorStudioUiData>> = _uiState.asStateFlow()

    init {
        loadTutorStudio()
    }

    fun loadTutorStudio() {
        viewModelScope.launch {
            try {
                val student = repository.getStudent()
                val stats = repository.getTutorStats()
                val courses = repository.getCourses()
                val role = repository.getCurrentRole()
                val isAuthorized = role == UserRole.INSTRUCTOR || role == UserRole.ADMIN

                _uiState.value = UiState.Success(
                    TutorStudioUiData(
                        tutor = student,
                        stats = stats,
                        myCourses = courses,
                        isTutorAuthorized = isAuthorized,
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load Tutor Studio")
            }
        }
    }

    fun uploadCourse(draft: TutorCourseDraft, onComplete: (Result<Course>) -> Unit) {
        viewModelScope.launch {
            val result = repository.uploadTutorCourse(draft)
            if (result.isSuccess) {
                loadTutorStudio()
            }
            onComplete(result)
        }
    }

    fun verifyTutorWithCode(code: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isValid = repository.validateTutorInviteCode(code)
            if (isValid) {
                repository.setUserRole(UserRole.INSTRUCTOR)
                loadTutorStudio()
            }
            onComplete(isValid)
        }
    }
}
