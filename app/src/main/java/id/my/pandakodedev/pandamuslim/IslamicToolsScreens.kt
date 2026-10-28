package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

// =========================================================================
// 1. LAYAR KOMPAS ARAH KIBLAT SENSOR REAL-TIME (KiblatScreen)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiblatScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var azimuth by remember { mutableStateOf(0f) }
    // Arah Kiblat rata-rata dari Indonesia (Pekanbaru/Sumatra/Jawa) ~ 294 derajat Barat Laut
    val qiblaAngle = 294f

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                }
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                }
                val r = FloatArray(9)
                val i = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (deg + 360) % 360
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val animatedRotation by animateFloatAsState(
        targetValue = qiblaAngle - azimuth,
        label = "qibla_rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kompas Arah Kiblat", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(EmeraldDark, Color(0xFF002B24))))
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Sudut Kiblat Indonesia: 294° Barat Laut", color = GoldAccent, fontWeight = FontWeight.ExtraBold)
                    Text("Arah HP Anda saat ini: ${azimuth.toInt()}°", color = MintSurface, fontSize = 13.sp)
                }
            }

            // Piringan Kompas Kiblat 3D
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.rotate(animatedRotation)
                ) {
                    Text("🕋", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("▲", fontSize = 28.sp, color = GoldAccent)
                    Image(
                        painter = painterResource(id = R.drawable.ic_panda_kiblat),
                        contentDescription = "Kompas Panda",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Text(
                text = "Letakkan HP mendatar dan jauhkan dari benda logam.\nPutar badan Anda hingga ikon Ka'bah berada lurus di atas.",
                color = MintSurface,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =========================================================================
// 2. LAYAR CHECKLIST AMALAN HARIAN RAMADHAN & SUNNAH (AmalanScreen)
// =========================================================================
data class AmalanHarian(val id: Int, val title: String, val pahala: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmalanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("panda_amalan_prefs", Context.MODE_PRIVATE) }

    val daftarAmalan = remember {
        listOf(
            AmalanHarian(1, "Sholat Subuh Berjamaah / Tepat Waktu", "Wajib • Cahaya di Hari Kiamat"),
            AmalanHarian(2, "Dzikir Pagi & Petang", "Sunnah • Benteng Diri Harian"),
            AmalanHarian(3, "Sholat Sunnah Dhuha (2-4 Rakaat)", "Sunnah • Sedekah 360 Persendian"),
            AmalanHarian(4, "Sholat Dzuhur Tepat Waktu", "Wajib • Amalan Paling Dicintai Allah"),
            AmalanHarian(5, "Membaca Al-Quran Minimal 1 Halaman", "Sunnah • 1 Huruf = 10 Kebaikan"),
            AmalanHarian(6, "Sholat Ashar Tepat Waktu", "Wajib • Dijauhkan dari Api Neraka"),
            AmalanHarian(7, "Sedekah Harian / Senyum kepada Sesama", "Sunnah • Penghapus Dosa"),
            AmalanHarian(8, "Sholat Maghrib Tepat Waktu", "Wajib • Penjaga Keberkahan"),
            AmalanHarian(9, "Sholat Isya & Witir Sebelum Tidur", "Wajib & Sunnah • Penyempurna Malam"),
            AmalanHarian(10, "Membaca Surah Al-Mulk Sebelum Tidur", "Sunnah • Pelindung Siksa Kubur")
        )
    }

    val checkedMap = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            daftarAmalan.forEach { item ->
                put(item.id, prefs.getBoolean("amalan_${item.id}", false))
            }
        }
    }

    val completedCount = checkedMap.values.count { it }
    val progress = completedCount.toFloat() / daftarAmalan.size.toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checklist Amalan Harian", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
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
            // Header Progress Amalan
            Card(
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_panda_amalan),
                        contentDescription = "Amalan Panda",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(70.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Capaian Ibadah Hari Ini: $completedCount / ${daftarAmalan.size}",
                            color = GoldAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = GoldAccent,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50))
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(daftarAmalan, key = { it.id }) { item ->
                    val isChecked = checkedMap[item.id] == true
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) Color(0xFFD1FAE5) else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val nextState = !isChecked
                                checkedMap[item.id] = nextState
                                prefs.edit().putBoolean("amalan_${item.id}", nextState).apply()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { nextState ->
                                    checkedMap[item.id] = nextState
                                    prefs.edit().putBoolean("amalan_${item.id}", nextState).apply()
                                },
                                colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldDark,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = item.pahala,
                                    color = EmeraldPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. LAYAR KALKULATOR ZAKAT PENGHASILAN & EMAS (KalkulatorScreen)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalkulatorZakatScreen(onBack: () -> Unit) {
    var penghasilanBulanan by remember { mutableStateOf("") }
    var bonusLainnya by remember { mutableStateOf("") }

    val gaji = penghasilanBulanan.toDoubleOrNull() ?: 0.0
    val bonus = bonusLainnya.toDoubleOrNull() ?: 0.0
    val totalPendapatan = gaji + bonus
    val zakatWajib = totalPendapatan * 0.025 // 2.5%

    val formatRupiah = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalkulator Zakat (2,5%)", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_menu_kalkulator),
                            contentDescription = "Kalkulator Panda",
                            modifier = Modifier.size(54.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Hitung Zakat Profesi & Maal",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = EmeraldDark
                        )
                    }

                    OutlinedTextField(
                        value = penghasilanBulanan,
                        onValueChange = { penghasilanBulanan = it.filter { c -> c.isDigit() } },
                        label = { Text("Penghasilan / Tabungan (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bonusLainnya,
                        onValueChange = { bonusLainnya = it.filter { c -> c.isDigit() } },
                        label = { Text("Pendapatan Tambahan / THR (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Jumlah Zakat Anda (2,5%):", color = MintSurface, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatRupiah.format(zakatWajib),
                        color = GoldAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp
                    )
                }
            }
        }
    }
}

// =========================================================================
// 4. LAYAR TRACKER KHATAM AL-QURAN 30 JUZ (KhatamScreen)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhatamTrackerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("panda_khatam_prefs", Context.MODE_PRIVATE) }
    var currentJuz by remember { mutableStateOf(prefs.getInt("juz_selesai", 1)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Target Khatam 30 Juz", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_menu_khatam),
                contentDescription = "Khatam Panda",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(110.dp).clip(CircleShape)
            )

            Text(
                text = "Progres Tilawah: Juz $currentJuz dari 30 Juz",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = EmeraldDark
            )

            LinearProgressIndicator(
                progress = { currentJuz / 30f },
                color = EmeraldPrimary,
                trackColor = MintSurface,
                modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(50))
            )

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Button(
                    onClick = {
                        if (currentJuz > 0) {
                            currentJuz--
                            prefs.edit().putInt("juz_selesai", currentJuz).apply()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("- Kurangi Juz")
                }

                Button(
                    onClick = {
                        if (currentJuz < 30) {
                            currentJuz++
                            prefs.edit().putInt("juz_selesai", currentJuz).apply()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Selesai 1 Juz Lagi!")
                }
            }
        }
    }
}