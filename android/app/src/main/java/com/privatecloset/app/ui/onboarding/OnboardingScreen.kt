package com.privatecloset.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.privatecloset.app.ui.common.BrandWordmark
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.PurpleLight
import com.privatecloset.app.ui.theme.ScreenBackgroundGradient
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary

private data class OnboardingFeature(val icon: ImageVector, val label: String)

private val features = listOf(
    OnboardingFeature(Icons.Filled.MenuBook, "Step-by-step video lessons"),
    OnboardingFeature(Icons.Filled.Piano, "Interactive keyboard & exercises"),
    OnboardingFeature(Icons.Filled.TrendingUp, "Track your progress"),
    OnboardingFeature(Icons.Filled.Schedule, "Learn at your own pace"),
    OnboardingFeature(Icons.Filled.Groups, "All skills. All ages. All levels."),
)

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundGradient)
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandWordmark(markSize = 90.dp, showTagline = true)

        Spacer(Modifier.height(28.dp))

        Text(
            text = "LEARN. PRACTICE. PERFORM.",
            color = PurpleLight,
            style = MaterialTheme.typography.labelLarge,
        )

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            features.forEach { feature ->
                FeatureRow(feature)
            }
        }

        Spacer(Modifier.weight(1f))

        androidx.compose.material3.Button(
            onClick = onContinue,
            shape = RoundedCornerShape(14.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PurpleLight),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Get Started", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun FeatureRow(feature: OnboardingFeature) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(BgCard, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(feature.icon, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
        }
        Text(
            text = feature.label,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}
