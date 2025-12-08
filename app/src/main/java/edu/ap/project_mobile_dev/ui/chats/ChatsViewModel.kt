package edu.ap.project_mobile_dev.ui.chats

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import edu.ap.project_mobile_dev.ui.model.ChatChats
import edu.ap.project_mobile_dev.ui.model.ChatPost
import edu.ap.project_mobile_dev.ui.model.Message
import edu.ap.project_mobile_dev.ui.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class ChatsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatsUIState())
    val uiState: StateFlow<ChatsUIState> = _uiState.asStateFlow()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser;
    private var user = User(userId = "", username = "", bitmap = null)

    fun getChats() {
        val uid = currentUser?.uid ?: return
        _uiState.update { it.copy(chats = emptyList(), isLoading = true) }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                user = User(
                    userId = uid,
                    username = userDoc.getString("username") ?: "",
                    bitmap = decodeBase64ToBitmap(userDoc.getString("profilePicture") ?: "")
                )

                val chatIds = userDoc.get("chats") as? List<String> ?: emptyList()

                for (chatId in chatIds) {
                    val chatRef = db.collection("chats").document(chatId)

                    chatRef.get()
                        .addOnSuccessListener { chatDoc ->
                            if (!chatDoc.exists()) return@addOnSuccessListener

                            val id = chatDoc.id
                            var groupName = chatDoc.getString("groupName")
                            val newMessage = chatDoc.getBoolean("newMessage") ?: false
                            val usersInChat = (chatDoc.get("users") as? List<String>) ?: emptyList()

                            fun pushChatToState(lastMsg: Message?) {
                                val newChat = ChatChats(
                                    id = id,
                                    groupName = groupName ?: "",
                                    lastMessage = lastMsg ?: Message(username = "", message = "", dateTime = Timestamp.now()),
                                    newMessage = newMessage
                                )

                                _uiState.update { currentState ->
                                    val updated = currentState.chats.toMutableList()
                                    val existingIndex = updated.indexOfFirst { it.id == id }
                                    if (existingIndex != -1) updated[existingIndex] = newChat
                                    else updated.add(newChat)
                                    currentState.copy(chats = updated)
                                }
                            }

                            chatRef.collection("messages")
                                .orderBy("dateTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                .limit(1)
                                .addSnapshotListener { msgSnap, error ->
                                    if (error != null || msgSnap == null) return@addSnapshotListener

                                    val lastMsgDoc = msgSnap.documents.firstOrNull()
                                    val lastMsg = lastMsgDoc?.let { doc ->
                                        val username = doc.getString("username") ?: ""
                                        val text = doc.getString("message") ?: ""
                                        val ts = doc.getTimestamp("dateTime") ?: Timestamp.now()
                                        Message(username = username, message = text, dateTime = ts)
                                    }

                                    if (usersInChat.size <= 2) {
                                        val otherId = usersInChat.first { it != uid }
                                        db.collection("users").document(otherId).get()
                                            .addOnSuccessListener { otherDoc ->
                                                groupName = otherDoc.getString("username") ?: groupName
                                                pushChatToState(lastMsg)
                                            }
                                    } else {
                                        pushChatToState(lastMsg)
                                    }
                                }
                        }
                }
            }

        _uiState.update { it.copy(isLoading = false) }
    }

    fun showAddMembers(show: Boolean){
        _uiState.update { it.copy(groupMembers = emptyList(), users = emptyList(), groupName = "", addMembers = show) }
    }

    fun addChat(){
        _uiState.update { it.copy(groupMembers = it.groupMembers + user) }

        val members = uiState.value.groupMembers.map { it.userId }

        val chat = ChatPost(
            groupName = uiState.value.groupName,
            users = members,
            newMessage = false
        )

        db.collection("chats").add(chat)
            .addOnSuccessListener { doc ->
                for(member in members){
                    db.collection("users").document(member)
                        .update("chats", FieldValue.arrayUnion(doc.id))

                    getChats()
                }
            }

        _uiState.update { it.copy(addMembers = false, groupSettings = false) }
    }

    fun updateGroupName(name: String){
        _uiState.update { it.copy(groupName = name) }
    }

    fun nextStep(){
        if(uiState.value.groupMembers.count() + 1 <= 2){
            addChat()
        }
        else {
            _uiState.update { it.copy(addMembers = false, groupSettings = true) }
        }
    }

    fun showGroupSettings(groupSetting: Boolean){
        _uiState.update { it.copy(groupSettings = groupSetting) }
    }

    fun removeMember(member: User){
        if(uiState.value.groupMembers.contains(member))
            _uiState.update { it.copy(groupMembers = it.groupMembers - member) }
    }

    fun addMember(member: User){
        if(!uiState.value.groupMembers.contains(member))
            _uiState.update { it.copy(groupMembers = it.groupMembers + member) }
    }

    fun searchUsers(input: String){
        val q = input.trim()
        if (q.length < 4)
            return
        val end = q + "\uf8ff"

        db.collection("users")
            .whereGreaterThanOrEqualTo("username", q)
            .orderBy("username")
            .startAt(q)
            .endAt(end)
            .limit(50)
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents.mapNotNull { doc ->
                    val userId = doc.id
                    val username = doc.getString("username") ?: ""
                    val bitmap = decodeBase64ToBitmap(doc.getString("profilePicture") ?: "")

                    User(
                        userId = userId,
                        username = username,
                        bitmap = bitmap
                    )
                }
                    .filter { it.username != user.username }

                _uiState.update{ it.copy(users = users) }
            }
    }

    fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearSearchResults(){
        _uiState.update { it.copy( users = emptyList() ) }
    }

    fun formatTime(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val now = java.util.Date()
        val sameDay = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date) ==
                SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now)

        return if (sameDay) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        } else {
            SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
        }
    }
}
