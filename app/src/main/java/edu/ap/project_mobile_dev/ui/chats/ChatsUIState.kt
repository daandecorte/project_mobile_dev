package edu.ap.project_mobile_dev.ui.chats

import edu.ap.project_mobile_dev.ui.model.ChatChats
import edu.ap.project_mobile_dev.ui.model.User

data class ChatsUIState (
    val chats: List<ChatChats> = emptyList(),
    val addMembers: Boolean = false,
    val groupSettings: Boolean = false,
    val groupName: String = "",
    val groupMembers: List<User> = emptyList(),
    val users: List<User> = emptyList(),
    val isLoading: Boolean = true
)