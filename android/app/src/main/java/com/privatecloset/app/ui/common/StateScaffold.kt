package com.privatecloset.app.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.TextSecondary

/**
 * Renders loading / error / success states the same way across every screen so
 * each screen composable only has to describe its Success content.
 */
@Composable
fun <T> StateScaffold(
    state: UiState<T>,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        is UiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
        }
        is UiState.Error -> {
            Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = state.message,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        is UiState.Success -> content(state.data)
    }
}

@Composable
fun SectionLabel(text: String, color: Color = Gold) {
    Text(
        text = text.uppercase(),
        color = color,
        style = MaterialTheme.typography.labelSmall,
    )
}
