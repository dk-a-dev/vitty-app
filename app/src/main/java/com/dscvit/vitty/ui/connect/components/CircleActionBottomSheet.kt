package com.dscvit.vitty.ui.connect.components

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dscvit.vitty.R
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.connect.ConnectViewModel
import com.dscvit.vitty.util.Constants
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleActionBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCreateCircleClick: () -> Unit,
    onJoinCircleClick: () -> Unit,
    connectViewModel: ConnectViewModel,
) {
    val context = LocalContext.current
    var isJoinCircleSheetVisible by remember { mutableStateOf(false) }

    var isCreateCircleSheetVisible by remember { mutableStateOf(false) }

    val handleJoinWithCode = { code: String ->
        // TODO: Implement joining with code functionality
    }

    val handleCreateCircle = { name: String, friends: List<String>, imageUri: Uri? ->
        Timber.d("handleCreateCircle called with name: $name")
        
        // Get the authentication token
        val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
        
        Timber.d("Token retrieved: ${if (token.isNotEmpty()) "Token exists" else "Token is empty"}")
        
        if (token.isNotEmpty() && name.isNotBlank()) {
            Timber.d("Calling connectViewModel.createCircle with token and name: $name")
            // Call the ViewModel to create the circle
            connectViewModel.createCircle(token, name)
            
            // Close the sheet
            isCreateCircleSheetVisible = false
        } else {
            Timber.e("Cannot create circle - token empty: ${token.isEmpty()}, name blank: ${name.isBlank()}")
        }
    }

    val handleJoinCircleClick = {
        onJoinCircleClick()
        isJoinCircleSheetVisible = true
    }

    val handleCreateCircleClick = {
        onCreateCircleClick()
        isCreateCircleSheetVisible = true
    }

    JoinCircleBottomSheet(
        isVisible = isJoinCircleSheetVisible,
        onDismiss = { isJoinCircleSheetVisible = false },
        onJoinWithCode = handleJoinWithCode,
    )

    CreateCircleBottomSheet(
        isVisible = isCreateCircleSheetVisible,
        onDismiss = { isCreateCircleSheetVisible = false },
        onCreateCircle = handleCreateCircle,
    )

    if (isVisible) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Secondary,
            dragHandle = {
                Box(
                    modifier =
                        Modifier
                            .padding(top = 16.dp)
                            .width(120.dp)
                            .height(7.dp)
                            .background(Accent.copy(alpha = .4f), shape = RoundedCornerShape(44.dp)),
                ) {
                }
            },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    CircleActionButton(
                        icon = painterResource(R.drawable.ic_circle_join),
                        label = "Join Circle",
                        onClick = {
                            handleJoinCircleClick()
                        },
                    )

                    CircleActionButton(
                        icon = painterResource(R.drawable.ic_community_outline),
                        label = "Create Circle",
                        onClick = {
                            handleCreateCircleClick()
                        },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CircleActionButton(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(TextColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = label,
                tint = Background,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = TextColor,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    letterSpacing = (-0.16).sp,
                ),
        )
    }
}
