package id.my.pandakodedev.pandamuslim

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ColoredStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

enum class ColoringTemplate(val title: String) {
    MASJID("🕌 Masjid Nabawi"),
    LENTERA("🏮 Lentera Ramadhan"),
    BULAN_BINTANG("🌙 Bulan & Bintang")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MewarnaiScreen(onBack: () -> Unit) {
    val colorPalette = remember {
        listOf(
            Color(0xFF10B981), // Emerald Green
            Color(0xFFF59E0B), // Gold
            Color(0xFF3B82F6), // Sky Blue
            Color(0xFFEF4444), // Coral Red
            Color(0xFF8B5CF6), // Royal Purple
            Color(0xFFEC4899), // Pink
            Color(0xFF14B8A6), // Teal
            Color(0xFF84CC16), // Lime
            Color(0xFF78350F), // Wood Brown
            Color(0xFFFFFFFF)  // Penghapus (Putih)
        )
    }

    var selectedColor by remember { mutableStateOf(colorPalette[0]) }
    var brushSize by remember { mutableStateOf(28f) }
    var selectedTemplate by remember { mutableStateOf(ColoringTemplate.MASJID) }

    val strokes = remember { mutableStateListOf<ColoredStroke>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_menu_mewarnai),
                            contentDescription = "Mewarnai Panda",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Kanvas Mewarnai Islami", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    // Tombol Undo Goresan Terakhir
                    IconButton(onClick = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Undo", tint = GoldAccent)
                    }
                    // Tombol Bersihkan Kanvas
                    IconButton(onClick = { strokes.clear() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Bersihkan", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBg)
                .padding(padding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pilihan Sketsa Gambar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColoringTemplate.values().forEach { template ->
                    val isSelected = selectedTemplate == template
                    Surface(
                        color = if (isSelected) EmeraldDark else Color.White,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedTemplate = template
                                strokes.clear()
                            }
                    ) {
                        Text(
                            text = template.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) GoldAccent else EmeraldDark,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                        )
                    }
                }
            }

            // Area Kanvas Gambar & Mewarnai
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedColor, brushSize) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPoints.clear()
                                    currentPoints.add(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPoints.add(change.position)
                                },
                                onDragEnd = {
                                    if (currentPoints.isNotEmpty()) {
                                        strokes.add(
                                            ColoredStroke(
                                                points = currentPoints.toList(),
                                                color = selectedColor,
                                                strokeWidth = brushSize
                                            )
                                        )
                                        currentPoints.clear()
                                    }
                                }
                            )
                        }
                ) {
                    // 1. Gambar semua goresan warna pengguna terlebih dahulu
                    strokes.forEach { stroke ->
                        drawSmoothStroke(stroke.points, stroke.color, stroke.strokeWidth)
                    }
                    if (currentPoints.isNotEmpty()) {
                        drawSmoothStroke(currentPoints, selectedColor, brushSize)
                    }

                    // 2. Gambar Garis Sketsa Hitam (Outline) di atas warna agar selalu rapi seperti buku mewarnai asli!
                    drawIslamicTemplateOutline(selectedTemplate)
                }
            }

            // Pengaturan Ukuran Kuas & Palet Warna
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ukuran Kuas Warna:", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldDark)
                        Slider(
                            value = brushSize,
                            onValueChange = { brushSize = it },
                            valueRange = 12f..65f,
                            modifier = Modifier.width(190.dp)
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(colorPalette) { color ->
                            val isSelected = selectedColor == color
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 46.dp else 38.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) EmeraldDark else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawSmoothStroke(points: List<Offset>, color: Color, width: Float) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawIslamicTemplateOutline(template: ColoringTemplate) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val outlineStroke = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val lineColor = Color(0xFF1E293B)

    when (template) {
        ColoringTemplate.MASJID -> {
            // Badan Masjid
            drawRect(
                color = lineColor,
                topLeft = Offset(cx - 220f, cy),
                size = Size(440f, 260f),
                style = outlineStroke
            )
            // Kubah Utama Tengah
            val domePath = Path().apply {
                moveTo(cx - 160f, cy)
                quadraticBezierTo(cx, cy - 230f, cx + 160f, cy)
                close()
            }
            drawPath(domePath, color = lineColor, style = outlineStroke)
            // Tiang Bulan Sabit di Puncak Kubah
            drawLine(lineColor, Offset(cx, cy - 115f), Offset(cx, cy - 175f), strokeWidth = 7f)
            drawCircle(lineColor, radius = 18f, center = Offset(cx, cy - 195f), style = outlineStroke)
            // Pintu Mihrab Lengkung
            val doorPath = Path().apply {
                moveTo(cx - 65f, cy + 260f)
                lineTo(cx - 65f, cy + 120f)
                quadraticBezierTo(cx, cy + 50f, cx + 65f, cy + 120f)
                lineTo(cx + 65f, cy + 260f)
            }
            drawPath(doorPath, color = lineColor, style = outlineStroke)
        }

        ColoringTemplate.LENTERA -> {
            // Tali Gantungan Lentera
            drawLine(lineColor, Offset(cx, cy - 280f), Offset(cx, cy - 180f), strokeWidth = 7f)
            // Topi Lentera
            val topPath = Path().apply {
                moveTo(cx - 90f, cy - 120f)
                lineTo(cx, cy - 185f)
                lineTo(cx + 90f, cy - 120f)
                close()
            }
            drawPath(topPath, color = lineColor, style = outlineStroke)
            // Kaca Lentera Segi Enam
            val bodyPath = Path().apply {
                moveTo(cx - 90f, cy - 120f)
                lineTo(cx - 135f, cy + 10f)
                lineTo(cx - 85f, cy + 160f)
                lineTo(cx + 85f, cy + 160f)
                lineTo(cx + 135f, cy + 10f)
                lineTo(cx + 90f, cy - 120f)
                close()
            }
            drawPath(bodyPath, color = lineColor, style = outlineStroke)
            // Cahaya Lilin di Dalam
            drawCircle(lineColor, radius = 45f, center = Offset(cx, cy + 20f), style = outlineStroke)
            // Alas Lentera
            drawRect(lineColor, topLeft = Offset(cx - 105f, cy + 160f), size = Size(210f, 35f), style = outlineStroke)
        }

        ColoringTemplate.BULAN_BINTANG -> {
            // Lingkaran Bulan Sabit Besar
            drawCircle(lineColor, radius = 190f, center = Offset(cx - 20f, cy), style = outlineStroke)
            drawCircle(lineColor, radius = 145f, center = Offset(cx + 45f, cy - 25f), style = outlineStroke)
            // Bintang Segi Empat Islami
            val starPath = Path().apply {
                moveTo(cx + 120f, cy - 150f)
                lineTo(cx + 140f, cy - 95f)
                lineTo(cx + 195f, cy - 75f)
                lineTo(cx + 140f, cy - 55f)
                lineTo(cx + 120f, cy)
                lineTo(cx + 100f, cy - 55f)
                lineTo(cx + 45f, cy - 75f)
                lineTo(cx + 100f, cy - 95f)
                close()
            }
            drawPath(starPath, color = lineColor, style = outlineStroke)
        }
    }
}