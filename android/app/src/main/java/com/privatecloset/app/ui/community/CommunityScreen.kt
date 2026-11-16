package com.privatecloset.app.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.privatecloset.app.data.model.CommunityChannel
import com.privatecloset.app.data.model.CommunityMessage
import com.privatecloset.app.data.model.UserRole
import com.privatecloset.app.ui.common.StateScaffold
import com.privatecloset.app.ui.common.UiState
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.BgStroke
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.GoldLight
import com.privatecloset.app.ui.theme.Purple
import com.privatecloset.app.ui.theme.PurpleLight
import com.privatecloset.app.ui.theme.ScreenBackgroundGradient
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary

@Composable
fun CommunityScreen(
    uiState: UiState<CommunityUiData>,
    onSelectChannel: (CommunityChannel) -> Unit,
    onSendMessage: (String) -> Unit,
) {
    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundGradient)
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Student & Tutor Community",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Live chat, chord feedback & masterclasses",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        StateScaffold(uiState) { data ->
            // Auto scroll to bottom when messages update
            LaunchedEffect(data.messages.size) {
                if (data.messages.isNotEmpty()) {
                    listState.animateScrollToItem(data.messages.size - 1)
                }
            }

            // Channel Selector Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(data.channels) { channel ->
                    val isSelected = channel.id == data.selectedChannel.id
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Purple else BgCard)
                            .border(1.dp, if (isSelected) Gold else BgStroke, RoundedCornerShape(20.dp))
                            .clickable { onSelectChannel(channel) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(channel.icon, fontSize = 13.sp)
                        Text(
                            text = "#${channel.name}",
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                        if (channel.unreadCount > 0 && !isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Gold),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    channel.unreadCount.toString(),
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Channel Topic description
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BgCard)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Topic: ${data.selectedChannel.topic}",
                    color = GoldLight,
                    fontSize = 11.sp,
                )
            }

            Spacer(Modifier.height(10.dp))

            // Chat Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(data.messages) { message ->
                    ChatMessageBubble(message = message)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Message Composer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .border(1.dp, BgStroke, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Message #${data.selectedChannel.name}...", color = TextSecondary, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                    singleLine = false,
                    maxLines = 3,
                )

                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            onSendMessage(messageInput)
                            messageInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (messageInput.isNotBlank()) Gold else BgStroke),
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = if (messageInput.isNotBlank()) Color.Black else TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: CommunityMessage) {
    val isTutor = message.senderRole == UserRole.INSTRUCTOR
    val isAdmin = message.senderRole == UserRole.ADMIN

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isTutor) Color(0x1AD4AF37) else BgCard)
            .border(1.dp, if (isTutor) Gold.copy(alpha = 0.35f) else BgStroke, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isTutor) Gold else PurpleLight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message.senderName.take(1).uppercase(),
                color = if (isTutor) Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            // Header: Name, Role Badge, Timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = message.senderName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )

                if (isTutor) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Gold.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Filled.Verified, contentDescription = null, tint = Gold, modifier = Modifier.size(10.dp))
                            Text("TUTOR", color = GoldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isAdmin) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFDC2626).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text("ADMIN", color = Color(0xFFF87171), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Purple.copy(alpha = 0.3f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text("STUDENT", color = TextSecondary, fontSize = 9.sp)
                    }
                }

                Spacer(Modifier.weight(1f))

                if (message.isPinned) {
                    Icon(Icons.Filled.PushPin, contentDescription = "Pinned", tint = Gold, modifier = Modifier.size(12.dp))
                }

                Text(
                    text = message.timestamp,
                    color = TextSecondary,
                    fontSize = 10.sp,
                )
            }

            Spacer(Modifier.height(4.dp))

            // Message Body
            Text(
                text = message.messageText,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
    }
}
