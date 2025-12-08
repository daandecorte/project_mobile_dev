package edu.ap.project_mobile_dev.ui.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.InputChipDefaults.inputChipColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.project_mobile_dev.ui.model.Chat
import edu.ap.project_mobile_dev.ui.model.ChatChats
import edu.ap.project_mobile_dev.ui.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onBack: () -> Unit,
    onChatClick: (ChatChats) -> Unit,
    viewModel: ChatsViewModel = viewModel()
){
    val uiState by viewModel.uiState.collectAsState()

    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getChats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Berichten", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Terug",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E2837)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddMembers(true) },
                containerColor = Color.Transparent,
                modifier = Modifier.padding(8.dp),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ){
                    Icon(Icons.Default.Add, contentDescription = "Voeg chat toe")
                }
            }
        },
    ) { padding ->
        Surface(
            modifier = Modifier.padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                    }
                    uiState.chats.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.chats) { chat -> Chat(chat, { onChatClick(chat) })
                            }
                        }
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Geen chats",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Geen chats",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start een gesprek door op + te klikken",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }


            if(uiState.addMembers){
                Dialog(
                    onDismissRequest = { viewModel.showAddMembers(false) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E2A3A)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Nieuwe chat",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    IconButton(onClick = { viewModel.showAddMembers(false) }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Sluit",
                                            tint = Color.White
                                        )
                                    }
                                }

                                Text(
                                    "Leden",
                                    color = Color(0xFFB0BEC5),
                                    modifier = Modifier.padding(top = 10.dp),
                                    fontSize = 14.sp
                                )

                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    InputChip(
                                        selected = true,
                                        onClick = { /* optional */ },
                                        label = {
                                            Text(
                                                "You",
                                                fontSize = 13.sp,
                                                maxLines = 1
                                            )
                                        },
                                        colors = inputChipColors(
                                            selectedContainerColor = Color(0xFF2C3E50),
                                            selectedLabelColor = Color.White,
                                            selectedTrailingIconColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    if (uiState.groupMembers.isNotEmpty()) {
                                        uiState.groupMembers.forEach { member ->
                                            InputChip(
                                                selected = true,
                                                onClick = { /* optional */ },
                                                label = {
                                                    Text(
                                                        member.username,
                                                        fontSize = 13.sp,
                                                        maxLines = 1
                                                    )
                                                },
                                                trailingIcon = {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "remove",
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clickable { viewModel.removeMember(member) }
                                                    )
                                                },
                                                colors = inputChipColors(
                                                    selectedContainerColor = Color(0xFF2C3E50),
                                                    selectedLabelColor = Color.White,
                                                    selectedTrailingIconColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = query,
                                    onValueChange = { text ->
                                        query = text
                                        if (text.length >= 4) {
                                            viewModel.searchUsers(text)
                                        } else {
                                            viewModel.clearSearchResults()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    placeholder = { Text("Zoeken") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFF3F5266),
                                        focusedBorderColor = Color(0xFFFF6B35),
                                        unfocusedContainerColor = Color(0xFF2C3E50),
                                        focusedContainerColor = Color(0xFF2C3E50),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                if(query.length >= 4) {
                                    Popup(
                                        alignment = Alignment.TopCenter,
                                        offset = IntOffset(0, 500),

                                    ) {
                                        Surface(
                                            shape=RoundedCornerShape(8.dp),
                                            color = Color(0xFF2C3E50),
                                            shadowElevation = 8.dp,
                                            modifier = Modifier.width(250.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .background(Color(0xFF2C3E50))
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                for(user in uiState.users){
                                                    UserOption(
                                                        user = user,
                                                        onClick = { viewModel.addMember(user) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { viewModel.showAddMembers(false) }) {
                                        Text("Annuleren", color = Color(0xFFB0BEC5))
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { viewModel.nextStep() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Volgende")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(uiState.groupSettings){
                Dialog(
                    onDismissRequest = { viewModel.showGroupSettings(false) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E2A3A)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Groep settings",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    IconButton(onClick = { viewModel.showGroupSettings(false) }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Sluit",
                                            tint = Color.White
                                        )
                                    }
                                }

                                Text(
                                    "Groepsnaam",
                                    color = Color(0xFFB0BEC5),
                                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                                    fontSize = 14.sp
                                )

                                OutlinedTextField(
                                    value = uiState.groupName,
                                    onValueChange = { text ->
                                        viewModel.updateGroupName(text)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    placeholder = { Text("Naam") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFF3F5266),
                                        focusedBorderColor = Color(0xFFFF6B35),
                                        unfocusedContainerColor = Color(0xFF2C3E50),
                                        focusedContainerColor = Color(0xFF2C3E50),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { viewModel.showGroupSettings(false) }) {
                                        Text("Annuleren", color = Color(0xFFB0BEC5))
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { viewModel.addChat() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Volgende")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Chat(
    chat: ChatChats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatsViewModel = viewModel()
) {
    val hasNew = chat.newMessage

    val backgroundBrush = if (hasNew) {
        Brush.horizontalGradient(colors = listOf(Color(0xFFBDBDBD), Color.White))
    } else {
        Brush.horizontalGradient(colors = listOf(Color(0xFF1E2837), Color(0xFF1E2837)))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .background(brush = backgroundBrush, shape = RoundedCornerShape(10.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(width = 64.dp, height = 48.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (chat.lastMessage != null) {
                    Text(
                        text = viewModel.formatTime(chat.lastMessage.dateTime),
                        fontSize = 12.sp,
                        color = if (hasNew) Color.Black else Color(0xFFB0BEC5)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.groupName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasNew) Color.Black else Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (chat.lastMessage.username != "" && chat.lastMessage.message != "") {
                    Text(
                        text = "${chat.lastMessage.username}: ${chat.lastMessage.message}",
                        maxLines = 1,
                        fontSize = 14.sp,
                        fontWeight = if (hasNew) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (hasNew) Color.Black else Color(0xFFB0BEC5)
                    )
                } else {
                    Text(
                        text = "Geen berichten",
                        fontSize = 14.sp,
                        color = if (hasNew) Color.Black else Color(0xFFB0BEC5)
                    )
                }
            }
        }
    }
}

@Composable
fun UserOption(
    user: User,
    onClick: () -> Unit
){
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ){
            Text(
                user.username,
                color = Color.White,
                fontSize = 14.sp
            )

        }
    }

}