package com.privatecloset.app.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecloset.app.data.model.CommunityChannel
import com.privatecloset.app.data.model.CommunityMessage
import com.privatecloset.app.data.model.UserRole
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CommunityUiData(
    val channels: List<CommunityChannel>,
    val selectedChannel: CommunityChannel,
    val messages: List<CommunityMessage>,
    val currentRole: UserRole,
)

class CommunityViewModel(
    private val repository: LearningRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<CommunityUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<CommunityUiData>> = _uiState.asStateFlow()

    init {
        loadCommunity()
    }

    fun loadCommunity(targetChannelId: String? = null) {
        viewModelScope.launch {
            try {
                val channels = repository.getCommunityChannels()
                val selected = targetChannelId?.let { id -> channels.find { it.id == id } }
                    ?: channels.firstOrNull()
                    ?: CommunityChannel("c_general", "general-lounge", "General discussions")

                val messages = repository.getCommunityMessages(selected.id)
                val role = repository.getCurrentRole()

                _uiState.value = UiState.Success(
                    CommunityUiData(
                        channels = channels,
                        selectedChannel = selected,
                        messages = messages,
                        currentRole = role,
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load community chat")
            }
        }
    }

    fun selectChannel(channel: CommunityChannel) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        if (current.selectedChannel.id == channel.id) return

        viewModelScope.launch {
            try {
                val messages = repository.getCommunityMessages(channel.id)
                _uiState.value = UiState.Success(
                    current.copy(
                        selectedChannel = channel,
                        messages = messages,
                    )
                )
            } catch (e: Exception) {
                // Keep current state on error
            }
        }
    }

    fun sendMessage(text: String) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                val newMsg = repository.sendCommunityMessage(current.selectedChannel.id, text.trim())
                _uiState.value = UiState.Success(
                    current.copy(
                        messages = current.messages + newMsg
                    )
                )
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
