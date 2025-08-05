package com.dscvit.vitty.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dscvit.vitty.network.api.community.APICommunityRestClient
import com.dscvit.vitty.network.api.community.RetrofitUserActionListener
import com.dscvit.vitty.network.api.community.responses.user.PostResponse
import com.dscvit.vitty.util.Constants
import retrofit2.Call

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusUpdateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(Constants.USER_INFO, 0) }
    val authToken = remember { prefs.getString(Constants.COMMUNITY_TOKEN, "") ?: "" }

    var selectedCampus by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val campusOptions = listOf("Vellore", "Chennai", "Bhopal")

    Dialog(
        onDismissRequest = { /* Prevent dismissal */ },
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Select Your Campus",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please select your campus to continue using the app",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { if (!isLoading) expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = selectedCampus,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Campus") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            ),
                        modifier =
                            Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                        enabled = !isLoading,
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.exposedDropdownSize(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                    ) {
                        campusOptions.forEach { campus ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = campus,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                onClick = {
                                    selectedCampus = campus
                                    expanded = false
                                },
                                colors =
                                    MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onSurface,
                                        leadingIconColor = MaterialTheme.colorScheme.onSurface,
                                        trailingIconColor = MaterialTheme.colorScheme.onSurface,
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (selectedCampus.isNotEmpty() && !isLoading) {
                            updateCampus(
                                authToken = authToken,
                                campus = selectedCampus,
                                onLoading = { isLoading = it },
                                onSuccess = {
                                    prefs.edit().putString(Constants.COMMUNITY_CAMPUS, selectedCampus).apply()
                                    Toast.makeText(context, "Campus updated successfully", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                onError = { /* Keep dialog open on error */ },
                            )
                        }
                    },
                    enabled = selectedCampus.isNotEmpty() && !isLoading,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Updating...")
                        }
                    } else {
                        Text(
                            text = "Update Campus",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

private fun updateCampus(
    authToken: String,
    campus: String,
    onLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    onLoading(true)

    APICommunityRestClient.instance.updateCampus(
        token = authToken,
        campus = campus.lowercase(),
        retrofitUserActionListener =
            object : RetrofitUserActionListener {
                override fun onSuccess(
                    call: Call<PostResponse>?,
                    response: PostResponse?,
                ) {
                    onLoading(false)
                    onSuccess()
                }

                override fun onError(
                    call: Call<PostResponse>?,
                    t: Throwable?,
                ) {
                    onLoading(false)
                    onError("Failed to update campus: ${t?.message}")
                }
            },
    )
}
