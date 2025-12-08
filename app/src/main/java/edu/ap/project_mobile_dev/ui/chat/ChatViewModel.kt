package edu.ap.project_mobile_dev.ui.chat

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.model.Chat
import edu.ap.project_mobile_dev.ui.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.String
import kotlin.collections.List

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUIState())
    val uiState: StateFlow<ChatUIState> = _uiState.asStateFlow()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser;

    fun getChat(chatId: String) {
        val uid = currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                _uiState.update { it.copy(username = doc.getString("username") ?: "") }
            }

        val chatRef = db.collection("chats").document(chatId)

        chatRef.get()
            .addOnSuccessListener { chatDoc ->
                if (!chatDoc.exists()) return@addOnSuccessListener

                var groupName = chatDoc.getString("groupName")
                val users = chatDoc.get("users") as? List<String> ?: emptyList()
                val newMessage = chatDoc.getBoolean("newMessage") ?: false

                if (users.size <= 2) {
                    val otherId = users.firstOrNull { it != uid }
                    if (otherId != null) {
                        db.collection("users").document(otherId).get()
                            .addOnSuccessListener { userDoc ->
                                groupName = userDoc.getString("username") ?: groupName

                                chatRef.collection("messages")
                                    .orderBy("dateTime")
                                    .addSnapshotListener { snapshot, error ->
                                        if (error != null || snapshot == null) return@addSnapshotListener

                                        val messages = snapshot.documents.mapNotNull { msgDoc ->
                                            val username = msgDoc.getString("username") ?: return@mapNotNull null
                                            val text = msgDoc.getString("message") ?: ""
                                            val timestamp = msgDoc.getTimestamp("dateTime") ?: Timestamp.now()
                                            Message(username = username, message = text, dateTime = timestamp)
                                        }

                                        val chat = Chat(
                                            id = chatId,
                                            groupName = groupName ?: "",
                                            users = users,
                                            messages = messages,
                                            newMessage = newMessage
                                        )

                                        _uiState.update { it.copy(chat = chat) }
                                    }
                            }
                    }
                } else {
                    chatRef.collection("messages")
                        .orderBy("dateTime")
                        .addSnapshotListener { snapshot, error ->
                            if (error != null || snapshot == null) return@addSnapshotListener

                            val messages = snapshot.documents.mapNotNull { msgDoc ->
                                val username = msgDoc.getString("username") ?: return@mapNotNull null
                                val text = msgDoc.getString("message") ?: ""
                                val timestamp = msgDoc.getTimestamp("dateTime") ?: Timestamp.now()
                                Message(username = username, message = text, dateTime = timestamp)
                            }

                            val chat = Chat(
                                id = chatId,
                                groupName = groupName ?: "",
                                users = users,
                                messages = messages,
                                newMessage = newMessage
                            )

                            _uiState.update { it.copy(chat = chat) }
                        }
                }
            }
    }



    fun sendMessage(text: String){
        val message = Message(
            username = uiState.value.username,
            message = text,
            dateTime = Timestamp.now()
        )

        db.collection("chats")
            .document(uiState.value.chat?.id ?: "")
            .collection("messages")
            .add(message)
    }

    fun checkUser(message: Message): Boolean{
        if(message.username == uiState.value.username)
            return true;
        return false;
    }
}