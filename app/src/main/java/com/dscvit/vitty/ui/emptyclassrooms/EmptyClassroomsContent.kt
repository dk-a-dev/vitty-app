package com.dscvit.vitty.ui.emptyclassrooms

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dscvit.vitty.R
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.util.Constants
import java.util.Calendar

data class EmptyClassroom(
    val roomNumber: String,
    val building: String,
    val capacity: Int,
    val availableUntil: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyClassroomsContent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(Constants.USER_INFO, 0) }

    var emptyClassrooms by remember { mutableStateOf<List<EmptyClassroom>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentTimeSlot by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        currentTimeSlot = getCurrentTimeSlot()
        emptyClassrooms = generateMockEmptyClassrooms()
        isLoading = false
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
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Background,
                    ),
            )
        },
        containerColor = Background,
        modifier = modifier,
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                        Text(
                            text = "Loading empty classrooms...",
                            color = Accent,
                            fontSize = 16.sp,
                        )
                    }
                }
            } else if (emptyClassrooms.isEmpty()) {
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
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All classrooms are currently occupied",
                                color = Accent,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                items(emptyClassrooms) { classroom ->
                    EmptyClassroomItem(classroom = classroom)
                }
            }
        }
    }
}

@Composable
private fun EmptyClassroomItem(
    classroom: EmptyClassroom,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = Secondary,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = classroom.roomNumber,
                color = TextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = classroom.building,
                color = Accent,
                fontSize = 12.sp,
            )
        }
    }
}

private fun getCurrentTimeSlot(): String {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    return when {
        hour < 8 -> "Pre-class hours"
        hour == 8 && minute < 50 -> "A1 (8:00 - 8:50)"
        hour == 8 && minute >= 50 || hour == 9 && minute < 40 -> "A2 (8:50 - 9:40)"
        hour == 9 && minute >= 40 || hour == 10 && minute < 30 -> "B1 (9:40 - 10:30)"
        hour == 10 && minute >= 30 || hour == 11 && minute < 20 -> "B2 (10:30 - 11:20)"
        hour == 11 && minute >= 20 || hour == 12 && minute < 10 -> "C1 (11:20 - 12:10)"
        hour == 12 && minute >= 10 || hour == 13 && minute < 0 -> "C2 (12:10 - 13:00)"
        hour == 13 || hour == 14 && minute < 0 -> "Lunch Break"
        hour == 14 && minute < 50 -> "D1 (14:00 - 14:50)"
        hour == 14 && minute >= 50 || hour == 15 && minute < 40 -> "D2 (14:50 - 15:40)"
        hour == 15 && minute >= 40 || hour == 16 && minute < 30 -> "E1 (15:40 - 16:30)"
        hour == 16 && minute >= 30 || hour == 17 && minute < 20 -> "E2 (16:30 - 17:20)"
        hour == 17 && minute >= 20 || hour == 18 && minute < 10 -> "F1 (17:20 - 18:10)"
        hour == 18 && minute >= 10 || hour == 19 && minute < 0 -> "F2 (18:10 - 19:00)"
        else -> "After class hours"
    }
}

private fun generateMockEmptyClassrooms(): List<EmptyClassroom> {
    val buildings = listOf("SJT", "TT", "MB", "PRP", "SMV", "CDMM", "GDN")
    val roomNumbers = (101..520).random() to (101..520).random()

    return listOf(
        EmptyClassroom("SJT 101", "SJT Block", 60, "11:20 AM"),
        EmptyClassroom("TT 205", "TT Block", 45, "12:10 PM"),
        EmptyClassroom("MB 303", "Main Building", 80, "1:00 PM"),
        EmptyClassroom("PRP 150", "PRP Block", 40, "2:00 PM"),
        EmptyClassroom("SMV 201", "SMV Block", 55, "3:00 PM"),
        EmptyClassroom("CDMM 102", "CDMM Block", 35, "4:00 PM"),
        EmptyClassroom("GDN 301", "GDN Block", 70, "5:00 PM"),
        EmptyClassroom("SJT 205", "SJT Block", 50, "6:00 PM"),
        EmptyClassroom("TT 101", "TT Block", 65, "7:00 PM"),
    ).shuffled().take(6)
}
