package com.example.ui.view

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WaterTelemetry
import com.example.ui.theme.*
import com.example.ui.viewmodel.ToastType
import com.example.ui.viewmodel.WaterViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NexusUcspApp(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()
    val toastState = viewModel.toastState
    var currentTab by remember { mutableStateOf("dashboard") }

    // Pulse effect for satellite link
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val satellitePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UcspBackground)
    ) {
        // --- Radial Gradient Orbs for Holographic Atmosphere ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        color = UcspBlue.copy(alpha = 0.25f),
                        radius = this.size.width * 0.7f,
                        center = Offset(0f, 0f)
                    )
                    
                    val activeStatusColor = when {
                        viewModel.waterBidones <= 10 -> ColorCritical
                        viewModel.waterBidones <= 25 -> ColorWarning
                        else -> ColorOptimal
                    }
                    drawCircle(
                        color = activeStatusColor.copy(alpha = 0.08f),
                        radius = this.size.width * 0.5f,
                        center = Offset(this.size.width * 0.8f, this.size.height * 0.4f)
                    )
                }
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 100.dp) // Leave roomy bottom margin for HUD actionbar
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    // --- HEADER BRANDING BLOCK ---
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ColorOptimal.copy(alpha = satellitePulseAlpha))
                                )
                                Text(
                                    text = "ENLACE SATELITAL ACTIVO",
                                    color = Color.LightGray.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "NEXUS UCSP",
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Last Sync Indicator
                            Surface(
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Sync Info",
                                        tint = UcspGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Sync: " + formatTimeElapsed(viewModel.lastUpdate),
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Logo Building Card
                        Surface(
                            modifier = Modifier.size(56.dp),
                            color = Color.White.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = "UCSP Logo",
                                    tint = UcspGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- SEGMENTED NAV CONTROL (iOS Neo Theme) ---
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TabButton(
                                text = "Telemetría",
                                selected = (currentTab == "dashboard"),
                                onClick = { currentTab = "dashboard" },
                                modifier = Modifier.weight(1f).testTag("tab_telemetry")
                            )
                            TabButton(
                                text = "Registros",
                                selected = (currentTab == "history"),
                                onClick = { currentTab = "history" },
                                modifier = Modifier.weight(1f).testTag("tab_records")
                            )
                        }
                    }

                    spacerDivider(height = 24.dp)

                    // --- CENTRAL CONTENT ROUTER ---
                    Box(modifier = Modifier.weight(1f)) {
                        if (currentTab == "dashboard") {
                            WaterDashboardTab(viewModel = viewModel)
                        } else {
                            WaterHistoryTab(viewModel = viewModel, records = historyRecords)
                        }
                    }
                }

                // --- FLOATING ACTION HUD BAR (Col-span action array) ---
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, UcspBackground.copy(alpha = 0.95f), UcspBackground),
                                startY = 0f
                            )
                        )
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Sincronizar Button
                        Button(
                            onClick = { viewModel.finalizeDaySync() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.06f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .testTag("sync_telemetry_button"),
                            contentPadding = PaddingValues(0.dp),
                            enabled = !viewModel.isSyncing
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (viewModel.isSyncing) {
                                    CircularProgressIndicator(
                                        color = UcspGold,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "SINCRONIZANDO...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Save Icon",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "SINCRONIZAR",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        // Reporte Oficial Button
                        Button(
                            onClick = { viewModel.showReportDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .testTag("share_report_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Report Icon",
                                    tint = UcspBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "REPORTE OFICIAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- REALTIME TOAST ALERTS (Top Float) ---
        AnimatedVisibility(
            visible = toastState.visible,
            enter = fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .zIndex(100f)
        ) {
            Surface(
                color = CardSurface,
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val iconTint = when (toastState.type) {
                        ToastType.SUCCESS -> ColorOptimal
                        ToastType.WARNING -> ColorWarning
                        ToastType.INFO -> UcspGold
                    }
                    val iconImg = when (toastState.type) {
                        ToastType.SUCCESS -> Icons.Default.Check
                        ToastType.WARNING -> Icons.Default.Warning
                        ToastType.INFO -> Icons.Default.Info
                    }

                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconImg,
                            contentDescription = "Toast Icon",
                            tint = iconTint,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = toastState.message,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }

        // --- THE FORMAL TELEMETRY REPORT DIALOG ---
        if (viewModel.showReportDialog) {
            WaterReportDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showReportDialog = false },
                onShare = {
                    val reportText = buildShareReportBody(
                        wellLevel = viewModel.wellLevel,
                        waterBidones = viewModel.waterBidones
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "REPORTE HÍDRICO UCSP")
                        putExtra(Intent.EXTRA_TEXT, reportText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Enviar Reporte Oficial UCSP"))
                    viewModel.showToast("Reporte oficial cargado al selector de envío", ToastType.SUCCESS)
                }
            )
        }
    }
}

// --- SUB-TABS VIEWS ---

@Composable
fun WaterDashboardTab(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // --- POZO CYLINDER CARD ---
        item {
            val wellPercent = (viewModel.wellLevel / 2.0f) * 100f
            val statusColor = when {
                viewModel.wellLevel <= 0.5f -> ColorCritical
                viewModel.wellLevel <= 1.0f -> ColorWarning
                else -> ColorOptimal
            }
            val statusMsg = when {
                viewModel.wellLevel <= 0.5f -> "CONCENTRACIÓN CRÍTICA"
                viewModel.wellLevel <= 1.0f -> "NIVEL INSUFICIENTE • REVISAR"
                else -> "SUBTERRÁNEO DISPONIBLE"
            }

            Surface(
                color = CardSurface,
                shape = RoundedCornerShape(36.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("well_card_section")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(UcspGold)
                                )
                                Text(
                                    text = "SUBTERRÁNEO",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nivel de Pozo Principal",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Vol % Shield
                        Surface(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = "${Math.round(wellPercent)}% VOL",
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Sleek Realistic Physical Tank Cylinder Container
                        CylinderTank(
                            percentage = wellPercent,
                            themeColor = statusColor,
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        // 2. Action Controls HUD Column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Metrics Label
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(contentAlignment = Alignment.BottomCenter) {
                                    Text(
                                        text = String.format(Locale.US, "%.2f", viewModel.wellLevel),
                                        color = Color.White,
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-2).sp,
                                        lineHeight = 72.sp
                                    )
                                }
                                Text(
                                    text = "METROS",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Minus / Plus Incremental Adjustments Duo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.adjustWellLevel(-0.05f) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.05f),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .testTag("well_decrement_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease well",
                                        tint = Color.LightGray
                                    )
                                }

                                Button(
                                    onClick = { viewModel.adjustWellLevel(0.05f) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.05f),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .testTag("well_increment_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase well",
                                        tint = Color.LightGray
                                    )
                                }
                            }

                            // Rapid HUD Preset Row Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { preset ->
                                    val isSelected = (Math.abs(viewModel.wellLevel - preset.toDouble()) < 0.01)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color.White else Color.Black.copy(alpha = 0.3f))
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.06f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { viewModel.selectWellLevelPreset(preset) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%.1f", preset),
                                            color = if (isSelected) Color.Black else Color.Gray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- BIDONES CIRCULAR PROGRESS CARD ---
        item {
            val progressPercent = (viewModel.waterBidones / 50f) * 100f
            val statusColor = when {
                viewModel.waterBidones <= 10 -> ColorCritical
                viewModel.waterBidones <= 25 -> ColorWarning
                else -> ColorOptimal
            }
            val statusMsg = when {
                viewModel.waterBidones <= 10 -> "ESTADO CRÍTICO • REPONER INMEDIATAMENTE"
                viewModel.waterBidones <= 25 -> "ALERTA TEMPRANA • PREVER REPOSICIÓN"
                else -> "SISTEMA ESTABLE • NIVELES ÓPTIMOS"
            }

            Surface(
                color = CardSurface,
                shape = RoundedCornerShape(36.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jugs_card_section")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Header Alert Banner block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(UcspGold)
                                )
                                Text(
                                    text = "SUPERFICIE",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = statusMsg,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.2.sp
                            )
                        }

                        // Status Icon Indicator
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(statusColor.copy(alpha = 0.1f))
                                .border(1.dp, statusColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (viewModel.waterBidones <= 10) Icons.Default.Warning else Icons.Default.Check,
                                contentDescription = "Water Alert Level Icon",
                                tint = statusColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Controls and Progress Center Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dec button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                                .clickable { viewModel.adjustWaterBidones(-1) }
                                .testTag("water_decrement_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease jugs",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Central Neon Progress ring with big number inside
                        NeonCircularProgress(
                            percentage = progressPercent,
                            themeColor = statusColor,
                            size = 148.dp
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${viewModel.waterBidones}",
                                    color = Color.White,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 44.sp
                                )
                                Text(
                                    text = "/ 50 UND",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // Inc button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (viewModel.waterBidones >= 50) Color.White.copy(alpha = 0.05f) else Color.White)
                                .border(1.dp, if (viewModel.waterBidones >= 50) Color.White.copy(alpha = 0.08f) else Color.White, RoundedCornerShape(20.dp))
                                .clickable { viewModel.adjustWaterBidones(1) }
                                .testTag("water_increment_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase jugs",
                                tint = if (viewModel.waterBidones >= 50) Color.LightGray else Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WaterHistoryTab(
    viewModel: WaterViewModel,
    records: List<WaterTelemetry>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // --- SUMMARY STATS PANEL GRID ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stat 1: Total Data
                Surface(
                    color = CardSurface,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Records count",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "TOTAL DATA",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${records.size}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Stat 2: Sistema Status
                Surface(
                    color = CardSurface,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "System health",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "SISTEMA",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val isCritical = viewModel.waterBidones <= 10
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isCritical) ColorCritical else ColorOptimal)
                            )
                            Text(
                                text = if (isCritical) "ALERTA" else "EN LÍNEA",
                                color = if (isCritical) ColorCritical else ColorOptimal,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // --- RECORDS ARRAY LIST ---
        if (records.isEmpty()) {
            item {
                Surface(
                    color = CardSurface,
                    shape = RoundedCornerShape(36.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Empty Lock Icon",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Sin Registros",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Inicia la captura de telemetría para poblar la base de datos.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            itemsIndexed(records) { index, record ->
                val prevRecord = if (index + 1 < records.size) records[index + 1] else null
                val trend = when {
                    prevRecord == null -> null
                    record.wellLevel > prevRecord.wellLevel -> true  // trending up (increase in well level)
                    record.wellLevel < prevRecord.wellLevel -> false // trending down (decrease)
                    else -> null
                }

                val rowStatusColor = when {
                    record.waterBidones <= 10 -> ColorCritical
                    record.waterBidones <= 25 -> ColorWarning
                    else -> ColorOptimal
                }

                Surface(
                    color = CardSurface,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_item_${record.id}")
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = record.dateStr,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Time stamp clock",
                                            tint = Color.LightGray.copy(alpha = 0.6f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = record.timeStr,
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Folio indicator
                                    Surface(
                                        color = Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                    ) {
                                        Text(
                                            text = record.folio,
                                            color = Color.LightGray,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    // Trash Icon for absolute delete control
                                    IconButton(
                                        onClick = { viewModel.deleteTelemetry(record.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete record",
                                            tint = ColorCritical.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Grid Metrics
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Pozo Measure info
                                Column {
                                    Text(
                                        text = "POZO",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%.2fm", record.wellLevel),
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        if (trend != null) {
                                            Icon(
                                                imageVector = if (trend) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Trend Icon",
                                                tint = if (trend) ColorOptimal else ColorCritical,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // Bidones info
                                Column {
                                    Text(
                                        text = "BIDONES",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${record.waterBidones} und",
                                        color = rowStatusColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPLEX SCALE CUSTOM RENDERS ---

@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = if (selected) Color.White else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun CylinderTank(
    percentage: Float,
    themeColor: Color,
    modifier: Modifier = Modifier,
    width: Dp = 96.dp,
    height: Dp = 236.dp
) {
    Box(
        modifier = modifier
            .size(width, height)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF000510))
            .border(4.dp, Color.Black, RoundedCornerShape(32.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Core Cylindrical Liquid Level representation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(percentage / 100f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(themeColor.copy(alpha = 0.9f), UcspBlue.copy(alpha = 0.8f))
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            // Shiny Glass reflection lines on fluid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Glossy vertical line in center
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.2f), Color.Transparent)
                            ),
                            start = Offset(this.size.width * 0.4f, 0f),
                            end = Offset(this.size.width * 0.4f, this.size.height),
                            strokeWidth = this.size.width * 0.2f
                        )
                    }
            )
        }

        // measurement neón markings printed on glass
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            val totalTicks = 5
            val tickInterval = this.size.height / (totalTicks - 1)
            val markers = listOf("2.0", "1.5", "1.0", "0.5", "0.0")

            markers.forEachIndexed { i, label ->
                val y = i * tickInterval
                
                // Draw horizontal small line glow
                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = Offset(this.size.width - 8.dp.toPx(), y),
                    end = Offset(this.size.width, y),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun NeonCircularProgress(
    percentage: Float,
    themeColor: Color,
    size: Dp = 140.dp,
    strokeWidth: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Background Orb Bloom
        Box(
            modifier = Modifier
                .size(size - 12.dp)
                .blur(16.dp)
                .background(themeColor.copy(alpha = 0.12f), CircleShape)
        )

        // Draw Core Arch
        Canvas(modifier = Modifier.size(size)) {
            val r = (this.size.width - strokeWidth.toPx()) / 2f
            
            // Background Ring Track
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = r,
                style = Stroke(width = strokeWidth.toPx())
            )

            // Live accent Progress Arc
            val sweep = (percentage / 100f) * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(themeColor.copy(alpha = 0.6f), themeColor, themeColor.copy(alpha = 0.8f))
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        // Inner nested composable label details
        content()
    }
}

// Dialog helper, details and formatted layouts
@Composable
fun WaterReportDialog(
    viewModel: WaterViewModel,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val statusColor = when {
        viewModel.waterBidones <= 10 -> ColorCritical
        viewModel.waterBidones <= 25 -> ColorWarning
        else -> ColorOptimal
    }
    val statusMsg = when {
        viewModel.waterBidones <= 10 -> "ESTADO CRÍTICO • REPONER INMEDIATAMENTE"
        viewModel.waterBidones <= 25 -> "ALERTA TEMPRANA • PREVER REPOSICIÓN"
        else -> "SISTEMA ESTABLE • NIVELES ÓPTIMOS"
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onDismiss() }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color(0xFF000814),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {} // block click propagation
                    .testTag("report_modal_card")
            ) {
                Column(
                    modifier = Modifier.padding(28.dp)
                ) {
                    // Header inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info, // drop substitute
                                    contentDescription = "Drop",
                                    tint = UcspBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "REPORTE HÍDRICO",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "UNIVERSIDAD CATÓLICA SAN PABLO",
                                color = UcspGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Operational Time info block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FECHA OPERATIVA",
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = SimpleDateFormat("dd 'de' MMM, yyyy", Locale("es", "PE")).format(Date()),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.1f)))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "HORA LOCAL",
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = SimpleDateFormat("hh:mm a", Locale.US).format(Date()),
                                color = UcspGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Pozo
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF001429), RoundedCornerShape(16.dp))
                                .border(1.dp, UcspBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "POZO",
                                color = Color(0xFF60A5FA), // Blue accent
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format(Locale.US, "%.2f", viewModel.wellLevel),
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 28.sp
                                )
                                Text(
                                    text = "m",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                                )
                            }
                        }

                        // Bidones
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF001429), RoundedCornerShape(16.dp))
                                .border(1.dp, UcspBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "BIDONES",
                                color = UcspGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${viewModel.waterBidones}",
                                    color = statusColor,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 28.sp
                                )
                                Text(
                                    text = "und",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // System Ruling status banner
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (viewModel.waterBidones <= 10) Icons.Default.Warning else Icons.Default.Check,
                                contentDescription = "Modal Alert Icon",
                                tint = statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "DICTAMEN DEL SISTEMA",
                                    color = statusColor.copy(alpha = 0.7f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = statusMsg,
                                    color = statusColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Signatures Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Box(modifier = Modifier.width(64.dp).height(1.dp).background(Color.DarkGray))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "AUTORIZADO POR",
                                color = Color.Gray,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Mantenimiento UCSP",
                                color = Color.LightGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ID HASH",
                                color = Color.Gray,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Text(
                                    text = "#" + Integer.toHexString(viewModel.wellLevel.hashCode() xor viewModel.waterBidones.hashCode()).uppercase().take(6),
                                    color = Color.LightGray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Buttons inside dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onShare() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Send WhatsApp Icon",
                                tint = UcspBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "COMPARTIR REPORT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier
                                .weight(0.8f)
                                .height(54.dp)
                        ) {
                            Text(
                                text = "CERRAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Utility formatting methods
fun formatTimeElapsed(timestamp: Long): String {
    if (timestamp == 0L) return "En espera de datos"
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / 60000L
    return when {
        mins < 1 -> "Sincronizado ahora"
        mins < 60 -> "Hace $mins min"
        mins < 1440 -> {
            val hours = mins / 60
            "Hace $hours hora" + if (hours > 1) "s" else ""
        }
        else -> {
            val days = mins / 1440
            "Hace $days día" + if (days > 1) "s" else ""
        }
    }
}

fun buildShareReportBody(wellLevel: Float, waterBidones: Int): String {
    val dateText = SimpleDateFormat("EEEE d 'de' MMMM yyyy", Locale("es", "PE")).format(Date())
    val timeText = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
    val dictamen = when {
        waterBidones <= 10 -> "🔴 ESTADO CRÍTICO • REPONER INMEDIATAMENTE"
        waterBidones <= 25 -> "🟡 ALERTA TEMPRANA • PREVER REPOSICIÓN"
        else -> "🟢 SISTEMA ESTABLE • NIVELES ÓPTIMOS"
    }

    return """
        *NEXUS UCSP - REPORTE HÍDRICO OFICIAL*
        *UNIVERSIDAD CATÓLICA SAN PABLO*
        
        📅 *Fecha:* $dateText
        ⏰ *Hora:* $timeText
        
        📈 *TELEMETRÍA CAPTURADA:*
        • Nivel de Pozo: ${String.format(Locale.US, "%.2f", wellLevel)} metros
        • Cantidad de Bidones: $waterBidones unidades
        
        ⚙️ *ESTADO DEL FLUIDO:*
        $dictamen
        
        👤 *Gestor:* Mantenimiento UCSP
        🆔 *Id Hash:* #${Integer.toHexString(wellLevel.hashCode() xor waterBidones.hashCode()).uppercase().take(6)}
        
        _Generado mediante enlace satelital Nexus UCSP._
    """.trimIndent()
}

@Composable
fun spacerDivider(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}
