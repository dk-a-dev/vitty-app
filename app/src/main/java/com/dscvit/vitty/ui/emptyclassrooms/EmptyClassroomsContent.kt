package com.dscvit.vitty.ui.emptyclassrooms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dscvit.vitty.R
import com.dscvit.vitty.network.api.community.APICommunityRestClient
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyClassroomsContent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(Constants.USER_INFO, 0) }
    val token = remember { prefs.getString(Constants.COMMUNITY_TOKEN, "") ?: "" }

    var emptyClassrooms by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedSlot by remember { mutableStateOf("A1") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val regularSlots =
        listOf(
            "A1",
            "A2",
            "B1",
            "B2",
            "C1",
            "C2",
            "D1",
            "D2",
            "E1",
            "E2",
            "F1",
            "F2",
            "G1",
            "G2",
            "V1",
            "V2",
            "TA1",
            "TA2",
            "TB1",
            "TB2",
            "TC1",
            "TC2",
            "TD1",
            "TD2",
            "TE1",
            "TE2",
            "TF1",
            "TF2",
            "TG1",
            "TG2",
        )

    fun fetchEmptyClassrooms(slot: String) {
        if (token.isEmpty()) {
            errorMessage = "Authentication required"
            isLoading = false
            return
        }

        isLoading = true
        errorMessage = null
        APICommunityRestClient.instance.getEmptyClassrooms(token, slot) { response ->
            isLoading = false
            if (response != null) {
                emptyClassrooms = response[slot] ?: emptyList()
            } else {
                errorMessage = "Failed to fetch empty classrooms"
                emptyClassrooms = emptyList()
            }
        }
    }

    LaunchedEffect(selectedSlot) {
        fetchEmptyClassrooms(selectedSlot)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Empty Classrooms",
                        color = TextColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_chevron_left),
                            contentDescription = "Back",
                            tint = TextColor,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { fetchEmptyClassrooms(selectedSlot) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextColor,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Background,
                    ),
            )
        },
        containerColor = Background,
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    items(regularSlots) { slot ->
                        SlotChip(
                            slot = slot,
                            isSelected = selectedSlot == slot,
                            onClick = { selectedSlot = slot },
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isLoading) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CircularProgressIndicator(color = Accent)
                            }
                        }
                    }
                } else if (errorMessage != null) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Accent,
                                    modifier = Modifier.size(48.dp),
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = TextColor,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                } else if (emptyClassrooms.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_empty_classroom),
                                    contentDescription = null,
                                    tint = Accent,
                                    modifier = Modifier.size(48.dp),
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No empty classrooms found",
                                    color = TextColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "All classrooms are occupied during $selectedSlot slot",
                                    color = Accent,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                } else {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "${emptyClassrooms.size} empty classrooms found for $selectedSlot",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextColor,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }

                    items(emptyClassrooms) { classroom ->
                        ClassroomCard(classroom = classroom)
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotChip(
    slot: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isSelected) Accent else Secondary,
                ).border(
                    1.dp,
                    if (isSelected) Accent else Secondary,
                    RoundedCornerShape(20.dp),
                ).clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = slot,
            color = if (isSelected) Background else TextColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun ClassroomCard(classroom: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(Accent.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = classroom,
                style = MaterialTheme.typography.titleMedium,
                color = TextColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Available",
                style = MaterialTheme.typography.bodySmall,
                color = Accent,
                textAlign = TextAlign.Center,
            )
        }
    }
}
