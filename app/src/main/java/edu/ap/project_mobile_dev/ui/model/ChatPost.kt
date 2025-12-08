package edu.ap.project_mobile_dev.ui.model

data class ChatPost (
    val groupName: String,
    val users: List<String>,
    val newMessage: Boolean
)