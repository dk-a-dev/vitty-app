package com.dscvit.vitty.ui.connect.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Poppins
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCircleBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCreateCircle: (String, Uri?) -> Unit,
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState()

        var circleName by remember { mutableStateOf("") }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

        val isCreateEnabled = circleName.isNotBlank()

        val imagePickerLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
            ) { uri: Uri? ->
                selectedImageUri = uri
            }

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
                )
            },
        ) {
            CreateCircleMainPage(
                circleName = circleName,
                onCircleNameChange = { circleName = it },
                isCreateEnabled = isCreateEnabled,
                selectedImageUri = selectedImageUri,
                onImagePickerClick = {
                    imagePickerLauncher.launch("image/*")
                },
                onCreateCircle = {
                    onCreateCircle(circleName, selectedImageUri)
                    onDismiss()
                },
            )
        }
    }
}

@Composable
fun CreateCircleMainPage(
    circleName: String,
    onCircleNameChange: (String) -> Unit,
    isCreateEnabled: Boolean,
    selectedImageUri: Uri?,
    onImagePickerClick: () -> Unit,
    onCreateCircle: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(modifier = Modifier.padding(bottom = 24.dp)) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "Create Circle", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.weight(1f).padding(bottom = 24.dp))
        }

        Box(
            modifier =
                Modifier
                    .size(89.dp)
                    .background(Accent, CircleShape)
                    .clip(CircleShape)
                    .clickable { onImagePickerClick() }
                    .padding(if (selectedImageUri != null) 0.dp else 33.dp)
                    .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Circle Image",
                    modifier =
                        Modifier
                            .size(89.dp)
                            .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_a_photo),
                    contentDescription = "Add Photo",
                    tint = Background,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Enter Circle Name",
            color = Accent,
            fontFamily = Poppins,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.18.sp,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        BasicTextField(
            value = circleName,
            onValueChange = onCircleNameChange,
            singleLine = true,
            cursorBrush = SolidColor(Accent),
            textStyle =
                MaterialTheme.typography.bodyMedium.copy(
                    color = TextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.18.sp,
                ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .border(
                                width = 1.dp,
                                color = Accent.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(7.dp),
                            ).background(Background, RoundedCornerShape(7.dp))
                            .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    innerTextField()
                }
            },
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Button(
                onClick = {
                    if (isCreateEnabled) {
                        onCreateCircle()
                    }
                },
                enabled = isCreateEnabled,
                shape = RoundedCornerShape(7.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        disabledContainerColor = Accent.copy(alpha = 0.5f),
                    ),
                modifier =
                    Modifier
                        .height(37.dp)
                        .padding(horizontal = 0.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 7.dp),
            ) {
                Text(
                    text = "Create",
                    fontFamily = Poppins,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.17.sp,
                    color = Secondary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun getPlaceholderFriends(): List<UserResponse> =
    remember {
        listOf(
            UserResponse(
                name = "Alex Johnson",
                username = "alexj",
                email = "alex@example.com",
                picture = "",
                friend_status = "not_friends",
                current_status = null,
                friends_count = 10,
                mutual_friends_count = 1,
                timetable = null,
            ),
            UserResponse(
                name = "Sarah Chen",
                username = "sarahc",
                email = "sarah@example.com",
                picture = "",
                friend_status = "not_friends",
                current_status = null,
                friends_count = 8,
                mutual_friends_count = 2,
                timetable = null,
            ),
            UserResponse(
                name = "Mike Rodriguez",
                username = "mikerod",
                email = "mike@example.com",
                picture = "",
                friend_status = "not_friends",
                current_status = null,
                friends_count = 5,
                mutual_friends_count = 0,
                timetable = null,
            ),
            UserResponse(
                name = "Emma Wilson",
                username = "emmaw",
                email = "emma@example.com",
                picture = "",
                friend_status = "not_friends",
                current_status = null,
                friends_count = 12,
                mutual_friends_count = 3,
                timetable = null,
            ),
            UserResponse(
                name = "David Kim",
                username = "davidk",
                email = "david@example.com",
                picture = "",
                friend_status = "not_friends",
                current_status = null,
                friends_count = 7,
                mutual_friends_count = 1,
                timetable = null,
            ),
            UserResponse(
                name = "Lisa Thompson",
                username = "lisat",
                email = "lisa@example.com",
                picture = "",
                friend_status = "not_friends",
                current_status = null,
                friends_count = 9,
                mutual_friends_count = 2,
                timetable = null,
            ),
            UserResponse(
                name = "Ryan Martinez",
                username = "ryanm",
                email = "ryan@example.com",
                picture = "",
                friend_status = "not_friends",
                current_status = null,
                friends_count = 6,
                mutual_friends_count = 0,
                timetable = null,
            ),
            UserResponse(
                name = "Ashley Davis",
                username = "ashleyd",
                email = "ashley@example.com",
                picture = "",
                friend_status = "not_friends",
                current_status = null,
                friends_count = 11,
                mutual_friends_count = 4,
                timetable = null,
            ),
        )
    }
