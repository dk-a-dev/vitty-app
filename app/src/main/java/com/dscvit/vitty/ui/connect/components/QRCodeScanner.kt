package com.dscvit.vitty.ui.connect.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Poppins
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.CompoundBarcodeView

@Composable
fun QRCodeScanner(
    onQRCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PermissionChecker.PERMISSION_GRANTED,
        )
    }

    var hasScannedCode by remember { mutableStateOf(false) }
    var scannedText by remember { mutableStateOf<String?>(null) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            hasCameraPermission = isGranted
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        var barcodeView by remember { mutableStateOf<CompoundBarcodeView?>(null) }

        AndroidView(
            factory = { context ->
                CompoundBarcodeView(context).apply {
                    val callback =
                        BarcodeCallback { result ->
                            result?.text?.let { newScannedText ->
                                if (!hasScannedCode || scannedText != newScannedText) {
                                    hasScannedCode = true
                                    scannedText = newScannedText
                                    onQRCodeScanned(newScannedText)
                                }
                            }
                        }
                    this.decodeContinuous(callback)
                    barcodeView = this
                }
            },
            modifier =
                modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(7.dp)),
            update = { view ->
                if (view != barcodeView) {
                    barcodeView?.pause()
                    barcodeView = view
                }
                view.resume()
            },
        )

        DisposableEffect(Unit) {
            onDispose {
                barcodeView?.pause()
                barcodeView = null
                hasScannedCode = false
                scannedText = null
            }
        }
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Camera permission is required to scan QR codes",
                color = Accent,
                textAlign = TextAlign.Center,
                fontFamily = Poppins,
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
