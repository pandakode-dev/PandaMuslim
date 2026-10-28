package id.my.pandakodedev.pandamuslim

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DzikirOption(val title: String, val arabic: String, val target: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tasbih3DScreen(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    val dzikirList = remember {
        listOf(
            DzikirOption("Subhanallah", "سُبْحَانَ اللَّهِ", 33),
            DzikirOption("Alhamdulillah", "الْحَمْدُ لِلَّهِ", 33),
            DzikirOption("Allahu Akbar", "اللَّهُ أَكْبَرُ", 33),
            DzikirOption("Astaghfirullah", "أَسْتَغْفِرُ اللَّهَ", 100)
        )
    }

    var selectedIndex by remember { mutableStateOf(0) }
    var count by remember { mutableStateOf(0) }
    var totalCount by remember { mutableStateOf(0) }

    val currentDzikir = dzikirList[selectedIndex]
    val progress = (count.toFloat() / currentDzikir.target.toFloat()).coerceIn(0f, 1f)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tasbih_press"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasbih Digital 3D", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { count = 0 }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(EmeraldDark, Color(0xFF002B24))
                    )
                )
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Pilihan Bacaan Dzikir
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                dzikirList.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    Surface(
                        color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedIndex = index
                                count = 0
                            }
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) EmeraldDark else Color.White,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            // Kartu Kaligrafi Dzikir
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentDzikir.arabic,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentDzikir.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MintSurface
                    )
                }
            }

            // Lingkaran Hitungan & Tombol Tekan 3D Panda
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(250.dp),
                    color = GoldAccent,
                    strokeWidth = 12.dp,
                    trackColor = Color.White.copy(alpha = 0.15f),
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(205.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .shadow(20.dp, CircleShape, spotColor = GoldAccent)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(EmeraldPrimary, EmeraldDark)
                            )
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            count++
                            totalCount++
                            if (count > currentDzikir.target) {
                                count = 1
                            }
                        }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_panda_tasbih),
                            contentDescription = "Panda Tasbih",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$count",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Target: ${currentDzikir.target}",
                            fontSize = 12.sp,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Statistik Total Dzikir Sesi Ini
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Dzikir Hari Ini:", color = MintSurface, fontSize = 14.sp)
                    Text("$totalCount Kali", color = GoldAccent, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}