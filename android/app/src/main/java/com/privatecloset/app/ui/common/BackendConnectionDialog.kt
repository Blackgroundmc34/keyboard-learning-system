package com.privatecloset.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privatecloset.app.data.model.BackendConnectionState
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.BgStroke
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.GoldLight
import com.privatecloset.app.ui.theme.Purple
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Visual badge displaying live backend status (Connected / Offline Cached)
 * Tapping opens the connection dialog to configure and test the teammate's backend URL.
 */
@Composable
fun BackendConnectionChip(
    state: BackendConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val indicatorColor = if (state.isConnected) Color(0xFF34D399) else Color(0xFFFBBF24)
    val statusText = if (state.isConnected) "Live Backend" else "Offline Cache"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .border(1.dp, if (state.isConnected) indicatorColor.copy(alpha = 0.5f) else BgStroke, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(indicatorColor, CircleShape)
        )
        Text(
            text = statusText,
            color = if (state.isConnected) GoldLight else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Backend Settings",
            tint = TextSecondary,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
fun BackendConnectionDialog(
    repository: LearningRepository,
    currentState: BackendConnectionState,
    onDismiss: () -> Unit,
) {
    var urlInput by remember { mutableStateOf(currentState.baseUrl) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Cloud, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Backend API Connection", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Configure the Express/TypeScript API host to connect the Android app to your teammate's backend.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )

                // Current status banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (currentState.isConnected) Color(0x1A34D399) else Color(0x1AFBBF24))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (currentState.isConnected) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (currentState.isConnected) Color(0xFF34D399) else Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = currentState.message,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Text Input
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Backend Base URL") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = BgStroke,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = TextSecondary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Quick presets
                Text("Presets:", color = TextSecondary, fontSize = 11.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { urlInput = "http://10.0.2.2:3000/" },
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                    ) {
                        Text("Emulator", fontSize = 11.sp, color = TextPrimary)
                    }
                    OutlinedButton(
                        onClick = { urlInput = "http://localhost:3000/" },
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                    ) {
                        Text("Localhost", fontSize = 11.sp, color = TextPrimary)
                    }
                    OutlinedButton(
                        onClick = { urlInput = "http://192.168.1.100:3000/" },
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                    ) {
                        Text("LAN IP", fontSize = 11.sp, color = TextPrimary)
                    }
                }

                if (isTesting) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = Gold, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Testing /api/health route...", color = TextSecondary, fontSize = 12.sp)
                    }
                } else if (testResult != null) {
                    Text(
                        text = testResult!!,
                        color = if (testResult!!.contains("Success")) Color(0xFF34D399) else Color(0xFFF87171),
                        fontSize = 12.sp,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    repository.setBackendBaseUrl(urlInput)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)
            ) {
                Text("Save & Connect", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        isTesting = true
                        testResult = null
                        repository.setBackendBaseUrl(urlInput)
                        scope.launch {
                            val ok = repository.testConnection()
                            isTesting = false
                            testResult = if (ok) "Success! Backend is online and responding." else "Could not reach server. Verify Node backend is running."
                        }
                    }
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Test Ping", color = GoldLight)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        }
    )
}
