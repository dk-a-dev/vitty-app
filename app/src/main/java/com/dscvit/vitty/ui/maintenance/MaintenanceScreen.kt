package com.dscvit.vitty.ui.maintenance

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dscvit.vitty.R
import com.dscvit.vitty.theme.*

@Composable
fun MaintenanceScreen(
    onRetryClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_maintenance),
                contentDescription = stringResource(R.string.maintenance_icon_desc),
                modifier = Modifier.size(120.dp),
                colorFilter = ColorFilter.tint(Accent)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.maintenance_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextColor,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.maintenance_message),
                fontSize = 16.sp,
                color = Accent,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.maintenance_description),
                fontSize = 14.sp,
                color = Accent.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onRetryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = Background
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.retry_connection),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onExitClick
            ) {
                Text(
                    text = stringResource(R.string.exit_app),
                    fontSize = 14.sp,
                    color = Accent.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.maintenance_footer),
                fontSize = 12.sp,
                color = Accent.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
