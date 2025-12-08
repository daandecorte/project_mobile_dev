package edu.ap.project_mobile_dev.ui.model

data class ChatChats (
    val id: String,
    val groupName: String,
    val lastMessage: Message,
    val newMessage: Boolean
)