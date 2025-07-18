package com.dscvit.vitty.ui.connect.components

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Poppins
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.community.CommunityViewModel
import com.dscvit.vitty.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParticipantsBottomSheet(
    sheetState: SheetState,
    circleId: String,
    onDismiss: () -> Unit,
    onSendCircleRequest: (String, String) -> Unit,
) {
    val context = LocalContext.current
    val communityViewModel: CommunityViewModel = viewModel()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var selectedUsers by remember { mutableStateOf(setOf<String>()) }
    var sendingRequests by remember { mutableStateOf(false) }
    var requestStates by remember { mutableStateOf(mapOf<String, Boolean?>()) } 
    var pendingRequestsCount by remember { mutableStateOf(0) }

    val sharedPreferences =
        remember {
            context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
        }
    val currentUsername =
        remember {
            sharedPreferences.getString(Constants.COMMUNITY_USERNAME, "") ?: ""
        }
    val currentName =
        remember {
            sharedPreferences.getString(Constants.COMMUNITY_NAME, "") ?: ""
        }
    val token =
        remember {
            sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
        }

    val suggestedFriends by communityViewModel.suggestedFriends.observeAsState()
    val searchResults by communityViewModel.searchResult.observeAsState()
    val actionResponse by communityViewModel.actionResponse.observeAsState()

    
    LaunchedEffect(actionResponse) {
        if (sendingRequests && actionResponse != null) {
            val currentUser = requestStates.keys.find { requestStates[it] == null }
            if (currentUser != null) {
                val isSuccess = actionResponse?.detail == "request sent successfully"
                requestStates =
                    requestStates.toMutableMap().apply {
                        this[currentUser] = isSuccess
                    }
                pendingRequestsCount--

                
                communityViewModel.actionResponse.postValue(null)

                
                if (pendingRequestsCount == 0) {
                    val failedUsers = requestStates.filter { it.value == false }.keys.toList()

                    if (failedUsers.isEmpty()) {
                        Toast
                            .makeText(
                                context,
                                "All requests sent successfully!",
                                Toast.LENGTH_SHORT,
                            ).show()
                    } else {
                        val failedUsersText = failedUsers.joinToString(", ")
                        Toast
                            .makeText(
                                context,
                                "Failed to send requests to: $failedUsersText",
                                Toast.LENGTH_LONG,
                            ).show()
                    }

                    sendingRequests = false
                    selectedUsers = setOf()
                    requestStates = mapOf()

                    if (failedUsers.isEmpty()) {
                        scope.launch {
                            onDismiss()
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            communityViewModel.getSuggestedFriends(token)
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isSearching = true
            delay(300) 
            if (token.isNotEmpty() && searchQuery.isNotBlank()) { 
                communityViewModel.getSearchResult(token, searchQuery)
            }
            isSearching = false
        } else {
            
            isSearching = false
        }
    }

    val filteredSuggestedFriends =
        suggestedFriends?.filter { user ->
            user.username != currentUsername && user.name != currentName
        }
    val filteredSearchResults =
        searchResults?.filter { user ->
            user.username != currentUsername && user.name != currentName
        }

    fun sendAllRequests() {
        if (selectedUsers.isNotEmpty() && !sendingRequests && token.isNotEmpty()) {
            sendingRequests = true
            pendingRequestsCount = selectedUsers.size
            requestStates = selectedUsers.associateWith { null } 

            selectedUsers.forEach { username ->
                communityViewModel.sendCircleRequest(token, circleId, username)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Secondary,
        contentColor = TextColor,
        dragHandle = {
            Box(
                modifier =
                    Modifier
                        .padding(top = 16.dp)
                        .width(120.dp)
                        .height(7.dp)
                        .background(Accent.copy(alpha = .4f), shape = RoundedCornerShape(44.dp)),
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart).size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_chevron_left),
                        contentDescription = "Back",
                        tint = TextColor,
                    )
                }

                Text(
                    text = "Add Participants",
                    color = TextColor,
                    fontFamily = Poppins,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .border(.8.dp, Accent, RoundedCornerShape(9999.dp))
                        .background(Background, RoundedCornerShape(9999.dp)),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        cursorBrush = SolidColor(Accent),
                        textStyle =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = TextColor,
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                            ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search users...",
                                        color = Accent.copy(alpha = 0.3f),
                                        style =
                                            MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 16.sp,
                                                lineHeight = 16.sp,
                                            ),
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Accent,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            
            if (selectedUsers.isNotEmpty()) {
                Text(
                    text = "Selected (${selectedUsers.size})",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextColor,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(8.dp))

                
                LazyColumn(
                    modifier = Modifier.height(120.dp), 
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val allUsers = (filteredSuggestedFriends ?: emptyList()) + (filteredSearchResults ?: emptyList())
                    val selectedUsersList =
                        allUsers.distinctBy { it.username }.filter { user ->
                            selectedUsers.contains(user.username)
                        }

                    items(selectedUsersList) { user ->
                        SelectedUserCard(
                            user = user,
                            onRemove = {
                                selectedUsers = selectedUsers - user.username
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Add More Users",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextColor,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            
            val displayUsers = if (searchQuery.isBlank()) filteredSuggestedFriends else filteredSearchResults
            val filteredUsers =
                displayUsers?.filter { user ->
                    !selectedUsers.contains(user.username) &&
                        
                        (
                            searchQuery.isBlank() ||
                                user.name.contains(searchQuery, ignoreCase = true) ||
                                user.username.contains(searchQuery, ignoreCase = true)
                        )
                }

            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(max = 300.dp),
                
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isSearching) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = Accent)
                        }
                    }
                } else if (filteredUsers.isNullOrEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No users found" else "No users available",
                                color = Accent.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                } else {
                    items(filteredUsers) { user ->
                        UserSelectionCard(
                            user = user,
                            isSelected = selectedUsers.contains(user.username),
                            onSelectionChanged = { isSelected ->
                                selectedUsers =
                                    if (isSelected) {
                                        selectedUsers + user.username
                                    } else {
                                        selectedUsers - user.username
                                    }
                            },
                        )
                    }
                }
            }

            
            if (selectedUsers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { sendAllRequests() },
                    enabled = !sendingRequests,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            contentColor = Secondary,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (sendingRequests) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Secondary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (sendingRequests) "Sending..." else "Add ${selectedUsers.size} Participant${if (selectedUsers.size > 1) "s" else ""}",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun UserSelectionCard(
    user: UserResponse,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onSelectionChanged(!isSelected) },
        colors =
            CardDefaults.cardColors(
                containerColor = Background,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(Accent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (user.picture.isNotEmpty()) {
                    AsyncImage(
                        model = user.picture,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                    )
                } else {
                    Text(
                        text =
                            user.name
                                .take(2)
                                .map { it.uppercaseChar() }
                                .joinToString(""),
                        color = TextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    color = TextColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "@${user.username}",
                    color = TextColor.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            
            Box(
                modifier =
                    Modifier
                        .size(24.dp)
                        .background(
                            if (isSelected) Accent else Background,
                            CircleShape,
                        ).border(
                            2.dp,
                            if (isSelected) Accent else TextColor.copy(alpha = 0.3f),
                            CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Background,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedUserCard(
    user: UserResponse,
    onRemove: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Background, RoundedCornerShape(12.dp))
                .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .background(Accent.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (user.picture.isNotEmpty()) {
                        AsyncImage(
                            model = user.picture,
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                        )
                    } else {
                        Text(
                            text =
                                user.name
                                    .take(2)
                                    .map { it.uppercaseChar() }
                                    .joinToString(""),
                            color = TextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = user.name,
                        color = TextColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "@${user.username}",
                        color = TextColor.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Accent,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
