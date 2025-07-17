package com.dscvit.vitty.ui.connect

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.network.api.community.responses.user.CircleItem
import com.dscvit.vitty.network.api.community.responses.user.FriendResponse
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.util.QRCodeGenerator
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleDetailScreenContent(
    circle: CircleItem,
    circleMembers: FriendResponse?,
    onBackClick: () -> Unit = {},
    onMemberClick: (UserResponse, String) -> Unit = { _, _ -> },
) {
    var searchQuery by remember { mutableStateOf("") }
    var showQrDialog by remember { mutableStateOf(false) }

    val circleFriends = circleMembers?.data

    val busyCount =
        circleFriends?.count { friend ->
            friend.current_status?.status?.lowercase(Locale.ROOT) != "free"
        } ?: 0

    val availableCount =
        circleFriends?.count { friend ->
            friend.current_status?.status?.lowercase(Locale.ROOT) == "free"
        } ?: 0

    val filteredFriends =
        circleFriends?.filter { friend ->
            searchQuery.isBlank() ||
                friend.name.contains(searchQuery, ignoreCase = true) ||
                friend.username.contains(searchQuery, ignoreCase = true)
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Circle",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_chevron_left),
                        contentDescription = "Back",
                        tint = TextColor,
                    )
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logout_2),
                        contentDescription = "Leave Circle",
                        modifier = Modifier.size(24.dp),
                        tint = TextColor,
                    )
                }
            },
            colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Background,
                ),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .border(2.dp, Secondary, RoundedCornerShape(9999.dp))
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
                                        text = "Search",
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

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = circle.circle_name,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            letterSpacing = (0.28).sp,
                        ),
                    color = TextColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = { showQrDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_qr),
                        contentDescription = "Generate QR Code",
                        tint = Accent,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (busyCount > 0) {
                    Box(
                        modifier =
                            Modifier
                                .background(Secondary, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_busy),
                                contentDescription = "Busy",
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "$busyCount busy",
                                color = TextColor,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
                if (availableCount > 0) {
                    Box(
                        modifier =
                            Modifier
                                .background(Secondary, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_free),
                                contentDescription = "Available",
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "$availableCount available",
                                color = TextColor,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(Secondary, RoundedCornerShape(16.dp))
                        .clickable { }
                        .padding(
                            horizontal = 12.dp,
                            vertical = 18.dp,
                        ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .clip(CircleShape)
                                .background(Color(0xff477397))
                                .padding(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Participants",
                            tint = Background,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Add Participants",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                letterSpacing = (0.16).sp,
                            ),
                        color = TextColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filteredFriends ?: emptyList()) { friend ->
                    CircleFriendCard(
                        friend = friend,
                        onClick = { onMemberClick(friend, circle.circle_id) },
                    )
                }
            }
        }
    }

    if (showQrDialog) {
        val qrBitmap = remember { QRCodeGenerator.generateQRCode(circle.circle_join_code ?: "", 400) }

        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            containerColor = Background,
            titleContentColor = TextColor,
            textContentColor = TextColor,
            title = {
                Text(
                    "Circle QR Code",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = TextColor,
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Scan this QR code to join the circle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    qrBitmap?.let { bitmap ->
                        Box(
                            modifier =
                                Modifier
                                    .size(250.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Circle QR Code",
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Join Code: ${circle.circle_join_code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Accent,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showQrDialog = false },
                ) {
                    Text(
                        "Close",
                        color = Accent,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
    }
}

@Composable
fun CircleFriendCard(
    friend: UserResponse,
    onClick: () -> Unit = {},
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Secondary, RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(vertical = 28.dp, horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(52.dp)
                        .background(Accent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (friend.picture.isNotEmpty()) {
                    AsyncImage(
                        model = friend.picture,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                    )
                } else {
                    Text(
                        text =
                            friend.name
                                .take(2)
                                .map { it.uppercaseChar() }
                                .joinToString(""),
                        color = TextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.name,
                    color = TextColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(4.dp))

                val currentStatus = friend.current_status
                val statusIcon =
                    when {
                        currentStatus?.status?.lowercase() == "free" -> R.drawable.ic_free
                        currentStatus?.status?.lowercase() == "busy" -> R.drawable.ic_busy
                        else -> R.drawable.ic_busy
                    }
                val statusText =
                    when {
                        currentStatus?.venue?.isNotEmpty() == true -> currentStatus.venue
                        currentStatus?.status?.lowercase(Locale.ROOT) == "free" -> "Available"
                        else -> "Not in a class right now"
                    }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Image(
                        painter = painterResource(id = statusIcon),
                        contentDescription = "Status Icon",
                        modifier = Modifier.size(12.dp),
                    )

                    Text(
                        text = statusText,
                        color = Accent,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}
