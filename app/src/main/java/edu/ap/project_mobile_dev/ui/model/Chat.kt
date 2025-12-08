package edu.ap.project_mobile_dev.ui.model

data class Chat (
    val id: String,
    val groupName: String,
    val users: List<String>,
    val messages: List<Message>,
    val newMessage: Boolean
)