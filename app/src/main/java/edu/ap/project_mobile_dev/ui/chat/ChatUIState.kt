package edu.ap.project_mobile_dev.ui.chat

import edu.ap.project_mobile_dev.ui.model.Chat

data class ChatUIState (
    val chat: Chat? = null,
    val username: String = "",
    val canSend: Boolean = false
)