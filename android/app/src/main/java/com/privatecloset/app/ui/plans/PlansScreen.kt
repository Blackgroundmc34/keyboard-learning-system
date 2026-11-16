package com.privatecloset.app.ui.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privatecloset.app.data.model.OneOnOneSession
import com.privatecloset.app.data.model.PaymentMethod
import com.privatecloset.app.data.model.Plan
import com.privatecloset.app.ui.common.StateScaffold
import com.privatecloset.app.ui.common.UiState
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.BgStroke
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.Purple
import com.privatecloset.app.ui.theme.PurpleLight
import com.privatecloset.app.ui.theme.ScreenBackgroundGradient
import com.privatecloset.app.ui.theme.SuccessGreen
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary

@Composable
fun PlansScreen(uiState: UiState<PlansUiData>) {
    var checkoutTitle by remember { mutableStateOf<String?>(null) }
    var checkoutPrice by remember { mutableStateOf<String?>(null) }
    var successReceipt by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundGradient)
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text("Membership & 1-on-1 Coaching", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        StateScaffold(uiState) { data ->
            LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                item {
                    Text("CHOOSE YOUR PLAN", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        data.plans.forEach { plan ->
                            PlanCard(
                                plan = plan,
                                onChoose = {
                                    checkoutTitle = plan.name
                                    checkoutPrice = plan.priceLabel
                                }
                            )
                        }
                    }
                }
                item {
                    Text("ONE-ON-ONE LESSONS", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(10.dp))
                }
                items(data.sessions) { session ->
                    SessionCard(
                        session = session,
                        onBook = {
                            checkoutTitle = "1-on-1 Coaching (${session.durationLabel})"
                            checkoutPrice = session.priceLabel
                        }
                    )
                }
            }
        }
    }

    if (checkoutTitle != null && checkoutPrice != null) {
        CheckoutDialog(
            title = checkoutTitle!!,
            priceLabel = checkoutPrice!!,
            onDismiss = { checkoutTitle = null },
            onSuccess = { method ->
                successReceipt = "Subscribed to $checkoutTitle ($checkoutPrice) via ${method.title}"
                checkoutTitle = null
            }
        )
    }

    if (successReceipt != null) {
        AlertDialog(
            onDismissRequest = { successReceipt = null },
            containerColor = BgCard,
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(36.dp)) },
            title = { Text("Payment Successful!", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(successReceipt!!, color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = { successReceipt = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)
                ) {
                    Text("Start Learning", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun PlanCard(plan: Plan, onChoose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(16.dp))
            .then(
                if (plan.isMostPopular) Modifier.border(1.5.dp, Gold, RoundedCornerShape(16.dp))
                else Modifier.border(1.dp, BgStroke, RoundedCornerShape(16.dp))
            )
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(plan.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (plan.isMostPopular) {
                Box(
                    modifier = Modifier
                        .background(Gold, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text("Most Popular", color = Color.Black, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(plan.priceLabel, color = Gold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Text(plan.billingCycle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(10.dp))
        plan.features.forEach { feature ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                Text("✓ ", color = SuccessGreen, style = MaterialTheme.typography.bodySmall)
                Text(feature, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onChoose,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (plan.isMostPopular) Gold else Purple, contentColor = if (plan.isMostPopular) Color.Black else Color.White),
            modifier = Modifier.fillMaxWidth().height(42.dp),
        ) {
            Text("Choose Plan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SessionCard(session: OneOnOneSession, onBook: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(16.dp))
            .border(1.dp, BgStroke, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(session.durationLabel, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(session.priceLabel, color = Gold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        session.features.forEach { feature ->
            Text("✓ $feature", color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onBook,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            modifier = Modifier.fillMaxWidth().height(42.dp),
        ) {
            Text("Book With Multiple Payment Options", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CheckoutDialog(
    title: String,
    priceLabel: String,
    onDismiss: () -> Unit,
    onSuccess: (PaymentMethod) -> Unit,
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CARD) }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = BgCard,
        title = {
            Text("Checkout: $title", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x1AD4AF37))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount", color = TextSecondary, fontSize = 12.sp)
                        Text(priceLabel, color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text("Select your payment method:", color = TextSecondary, fontSize = 12.sp)

                PaymentMethod.values().forEach { method ->
                    val isSelected = method == selectedMethod
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Purple.copy(alpha = 0.35f) else Color(0xFF130E1F))
                            .border(1.dp, if (isSelected) Gold else BgStroke, RoundedCornerShape(8.dp))
                            .clickable { selectedMethod = method }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(method.iconRes, fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(method.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(method.subtitle, color = TextSecondary, fontSize = 10.sp)
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedMethod = method },
                            colors = RadioButtonDefaults.colors(selectedColor = Gold, unselectedColor = TextSecondary)
                        )
                    }
                }

                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = Gold, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Processing via ${selectedMethod.title}...", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isProcessing = true
                    onSuccess(selectedMethod)
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)
            ) {
                Text("Confirm & Pay", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
