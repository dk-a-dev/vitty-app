package com.dscvit.vitty.ui.connect.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Poppins
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinCircleBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onJoinWithCode: (String) -> Unit,
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState()
        var circleCode by remember { mutableStateOf("") }
        val isJoinEnabled = circleCode.isNotBlank()

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
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Join Circle",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Enter Circle Code",
                    color = Accent,
                    fontFamily = Poppins,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.18.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                BasicTextField(
                    value = circleCode,
                    onValueChange = { circleCode = it },
                    singleLine = true,
                    cursorBrush = SolidColor(Accent),
                    textStyle =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = TextColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.18.sp,
                            textAlign = TextAlign.Start,
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

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "OR",
                        color = TextColor,
                        fontFamily = Poppins,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp,
                        letterSpacing = 0.18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                Text(
                    text = "Scan QR Code",
                    color = Accent,
                    fontFamily = Poppins,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.18.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(239.dp)
                            .background(Background, RoundedCornerShape(7.dp))
                            .padding(10.dp),
                ) {
                    QRCodeScanner(
                        onQRCodeScanned = { scannedCode ->
                            onJoinWithCode(scannedCode)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = {
                            if (isJoinEnabled) {
                                onJoinWithCode(circleCode)
                                onDismiss()
                            }
                        },
                        enabled = isJoinEnabled,
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
                            text = "Join",
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
    }
}
