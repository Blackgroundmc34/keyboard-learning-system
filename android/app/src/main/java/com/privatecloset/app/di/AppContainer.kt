package com.privatecloset.app.di

import android.content.Context
import com.privatecloset.app.data.local.TokenManager
import com.privatecloset.app.data.remote.NetworkModule
import com.privatecloset.app.data.repository.FakeLearningRepository
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.data.repository.NetworkLearningRepository

/**
 * Manual DI container providing network and repository singletons to the app.
 * Can be instantiated with [Context] in the Application class for live network + SharedPreferences,
 * or without parameters for testing/fake previews.
 */
class AppContainer(context: Context? = null) {
    val tokenManager: TokenManager? = context?.let { TokenManager(it) }

    val networkModule: NetworkModule? = tokenManager?.let { NetworkModule(it) }

    val learningRepository: LearningRepository by lazy {
        if (networkModule != null && tokenManager != null) {
            NetworkLearningRepository(
                apiService = networkModule.apiService,
                tokenManager = tokenManager,
                fallbackRepo = FakeLearningRepository(),
            )
        } else {
            FakeLearningRepository()
        }
    }
}
