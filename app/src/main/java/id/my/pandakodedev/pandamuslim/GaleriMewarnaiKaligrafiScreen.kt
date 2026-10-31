package id.my.pandakodedev.pandamuslim

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BrushStrokePath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

enum class CalligraphyTemplate(val title: String, val arabicCalligraphy: String, val subtitle: String) {
    MASJID_NABAWI("🕌 Masjid Nabawi & Kubah Hijau", "مُحَمَّد ﷺ", "Warnai Kubah Masjid & Kaligrafi"),
    KAABAH_MAKKAH("🕋 Ka'bah Masjidil Haram", "اللَّهُ أَكْبَرُ", "Warnai Baitullah & Ornamen Emas"),
    LAFADZ_ALLAH("✨ Kaligrafi Lafadz Allah & Bintang", "اللَّهُ خَالِقُ كُلِّ شَيْءٍ", "Warnai Kaligrafi Agung"),
    MASJID_AQSA("🌙 Masjidil Aqsa & Bulan Sabit", "بَارَكْنَا حَوْلَهُ", "Warnai Kubah Emas Al-Aqsa"),
    BISMILLAH("🌸 Kaligrafi Basmalah Indah", "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Warnai Kaligrafi Basmalah")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaleriMewarnaiKaligrafiScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var selectedTemplate by remember { mutableStateOf(CalligraphyTemplate.MASJID_NABAWI) }
    val strokes = remember { mutableStateListOf<BrushStrokePath>() }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    val colorPalette = remember {
        listOf(
            Color(0xFF10B981), Color(0xFFFACC15), Color(0xFF3B82F6),
            Color(0xFFEC4899), Color(0xFFF97316), Color(0xFF8B5CF6),
            Color(0xFFEF4444), Color(0xFF14B8A6), Color(0xFF84CC16),
            Color(0xFFD97706), Color(0xFF0F172A), Color(0xFFFFFFFF)
        )
    }
    var selectedColor by remember { mutableStateOf(colorPalette[0]) }
    var brushSize by remember { mutableStateOf(28f) }
    var bgSkyColor by remember { mutableStateOf(Color(0xFFFFFDF7)) }

    fun saveArtworkToGallery(ctx: Context) {
        try {
            val width = 1080
            val height = 1080
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bmp)
            canvas.drawColor(bgSkyColor.toArgb())

            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            strokes.forEach { s ->
                paint.color = s.color.toArgb()
                paint.strokeWidth = s.strokeWidth * 1.5f
                for (i in 0 until s.points.size - 1) {
                    canvas.drawLine(
                        s.points[i].x * 1.4f, s.points[i].y * 1.4f,
                        s.points[i + 1].x * 1.4f, s.points[i + 1].y * 1.4f,
                        paint
                    )
                }
            }

            val textPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.parseColor("#064E3B")
                textSize = 95f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText(selectedTemplate.arabicCalligraphy, width / 2f, height * 0.35f, textPaint)

            val filename = "PandaMuslim_Karya_${System.currentTimeMillis()}.png"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PandaMuslim")
                }
            }
            val uri = ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                ctx.contentResolver.openOutputStream(uri)?.use { out ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Toast.makeText(ctx, "Alhamdulillah! Karya lukisan disimpan ke Galeri HP (Folder Pictures/PandaMuslim) 🎨", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(ctx, "Gagal menyimpan gambar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🎨 Studio Mewarnai Kaligrafi & Masjid", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                        Text(selectedTemplate.title, color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { if (strokes.isNotEmpty()) strokes.removeLast() }) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.White)
                    }
                    IconButton(onClick = { strokes.clear() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Bersihkan", tint = Color.White)
                    }
                    IconButton(onClick = { saveArtworkToGallery(context) }) {
                        Icon(Icons.Default.Download, contentDescription = "Simpan ke Galeri", tint = GoldAccent)
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
        ) {
            // 1. Pilihan 5 Sketsa Masjid Dunia & Kaligrafi
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldDark)
            ) {
                items(CalligraphyTemplate.values()) { tpl ->
                    val active = selectedTemplate == tpl
                    Surface(
                        color = if (active) GoldAccent else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable {
                            selectedTemplate = tpl
                            strokes.clear()
                        }
                    ) {
                        Text(
                            text = tpl.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (active) Color(0xFF0F172A) else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // 2. KANVAS GAMBAR SKETSA MASJID & KALIGRAFI
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(bgSkyColor)
                    .border(3.dp, GoldAccent, RoundedCornerShape(24.dp))
                    .pointerInput(selectedColor, brushSize) {
                        detectDragGestures(
                            onDragStart = { offset -> currentPoints = listOf(offset) },
                            onDrag = { change, _ ->
                                currentPoints = currentPoints + change.position
                            },
                            onDragEnd = {
                                if (currentPoints.size > 1) {
                                    strokes.add(BrushStrokePath(currentPoints, selectedColor, brushSize))
                                }
                                currentPoints = emptyList()
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // A. Gambar Goresan Kuas Warna Anak (Di Bawah Garis Sketsa Hitam agar Sketsa Tetap Tegas!)
                    strokes.forEach { stroke ->
                        val path = Path().apply {
                            stroke.points.firstOrNull()?.let { moveTo(it.x, it.y) }
                            stroke.points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = path,
                            color = stroke.color,
                            style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    if (currentPoints.size > 1) {
                        val livePath = Path().apply {
                            moveTo(currentPoints.first().x, currentPoints.first().y)
                            currentPoints.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = livePath,
                            color = selectedColor,
                            style = Stroke(width = brushSize, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // B. Garis Sketsa Outlines Masjid & Kaligrafi (Selalu Tampil Jelas di Atas Warna!)
                    val outlineColor = Color(0xFF0F172A)
                    val baseFloorY = h * 0.85f

                    // Garis Lantai Masjid
                    drawLine(outlineColor, Offset(w * 0.08f, baseFloorY), Offset(w * 0.92f, baseFloorY), strokeWidth = 6f)

                    if (selectedTemplate == CalligraphyTemplate.KAABAH_MAKKAH) {
                        // Sketsa Bangunan Kubus Ka'bah & Kiswah
                        val kaabahLeft = w * 0.28f
                        val kaabahTop = h * 0.45f
                        val kaabahW = w * 0.44f
                        val kaabahH = baseFloorY - kaabahTop
                        drawRect(outlineColor, Offset(kaabahLeft, kaabahTop), Size(kaabahW, kaabahH), style = Stroke(width = 7f))
                        // Sabuk Emas Kiswah
                        drawRect(outlineColor, Offset(kaabahLeft, kaabahTop + kaabahH * 0.18f), Size(kaabahW, kaabahH * 0.12f), style = Stroke(width = 5f))
                        // Pintu Ka'bah
                        drawRect(outlineColor, Offset(kaabahLeft + kaabahW * 0.6f, kaabahTop + kaabahH * 0.42f), Size(kaabahW * 0.22f, kaabahH * 0.45f), style = Stroke(width = 5f))
                    } else {
                        // Sketsa Kubah Utama Masjid & 2 Menara Kembar
                        val domePath = Path().apply {
                            moveTo(w * 0.25f, h * 0.62f)
                            quadraticBezierTo(w * 0.50f, h * 0.34f, w * 0.75f, h * 0.62f)
                            close()
                        }
                        drawPath(domePath, outlineColor, style = Stroke(width = 6f))
                        // Dinding Bawah Masjid
                        drawRect(outlineColor, Offset(w * 0.22f, h * 0.62f), Size(w * 0.56f, baseFloorY - h * 0.62f), style = Stroke(width = 6f))
                        // Pintu Lengkung Mihrab Tengah
                        drawArc(
                            color = outlineColor,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = true,
                            topLeft = Offset(w * 0.42f, h * 0.70f),
                            size = Size(w * 0.16f, (baseFloorY - h * 0.70f) * 2),
                            style = Stroke(width = 5f)
                        )
                        // Menara Kiri & Kanan
                        drawRect(outlineColor, Offset(w * 0.11f, h * 0.38f), Size(w * 0.08f, baseFloorY - h * 0.38f), style = Stroke(width = 5f))
                        drawRect(outlineColor, Offset(w * 0.81f, h * 0.38f), Size(w * 0.08f, baseFloorY - h * 0.38f), style = Stroke(width = 5f))
                    }

                    // C. Gambar Teks Kaligrafi Arab Besar di Langit Atas Masjid untuk Diwarnai
                    drawContext.canvas.nativeCanvas.apply {
                        val strokeTextPaint = Paint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.parseColor("#064E3B")
                            textSize = 86f
                            textAlign = Paint.Align.CENTER
                            style = Paint.Style.STROKE
                            strokeWidth = 4.5f
                            isFakeBoldText = true
                        }
                        drawText(selectedTemplate.arabicCalligraphy, w / 2f, h * 0.24f, strokeTextPaint)
                    }
                }
            }

            // 3. PALET WARNA CERIA & UKURAN KUAS
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎨 Pilih Warna Kuas:", fontSize = 12.sp, fontWeight = FontWeight.Black, color = EmeraldDark)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(16f to "Kecil", 28f to "Sedang", 46f to "Besar").forEach { (sz, label) ->
                                val active = brushSize == sz
                                Surface(
                                    color = if (active) EmeraldDark else MintSurface,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable { brushSize = sz }
                                ) {
                                    Text(
                                        text = "🖌️ $label",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) GoldAccent else EmeraldDark,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(colorPalette) { col ->
                            val isSelected = selectedColor == col
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 44.dp else 38.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (isSelected) 3.5.dp else 1.5.dp,
                                        color = if (isSelected) EmeraldDark else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = col }
                            )
                        }
                    }
                }
            }
        }
    }
}