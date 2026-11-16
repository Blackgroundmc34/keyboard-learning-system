package com.privatecloset.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.courses.CoursesViewModel
import com.privatecloset.app.ui.dashboard.DashboardViewModel
import com.privatecloset.app.ui.lesson.LessonViewModel
import com.privatecloset.app.ui.plans.PlansViewModel
import com.privatecloset.app.ui.practice.PracticeViewModel
import com.privatecloset.app.ui.progress.ProgressViewModel

/**
 * A single factory that knows how to construct every screen ViewModel with the
 * repository from [AppContainer]. Keeps DI manual and dependency-free.
 */
class ViewModelFactory(private val repository: LearningRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            DashboardViewModel::class.java -> DashboardViewModel(repository) as T
            CoursesViewModel::class.java -> CoursesViewModel(repository) as T
            LessonViewModel::class.java -> LessonViewModel(repository) as T
            PracticeViewModel::class.java -> PracticeViewModel(repository) as T
            ProgressViewModel::class.java -> ProgressViewModel(repository) as T
            PlansViewModel::class.java -> PlansViewModel(repository) as T
            com.privatecloset.app.ui.community.CommunityViewModel::class.java -> com.privatecloset.app.ui.community.CommunityViewModel(repository) as T
            com.privatecloset.app.ui.marketplace.MarketplaceViewModel::class.java -> com.privatecloset.app.ui.marketplace.MarketplaceViewModel(repository) as T
            com.privatecloset.app.ui.tutor.TutorStudioViewModel::class.java -> com.privatecloset.app.ui.tutor.TutorStudioViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
