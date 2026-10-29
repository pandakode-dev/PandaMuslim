package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
    val qiblaAngle = 294f
    val kiblatBitmap = rememberOptimizedIcon(R.drawable.ic_panda_kiblat, 200)

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

        onDispose { sensorManager.unregisterListener(listener) }
    }

    val animatedRotation by animateFloatAsState(targetValue = qiblaAngle - azimuth, label = "qibla_rot")

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
                .background(Brush.verticalGradient(listOf(EmeraldDark, EmeraldPrimary)))
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Sudut Kiblat Indonesia: 294° Barat Laut", color = GoldAccent, fontWeight = FontWeight.ExtraBold)
                    Text("Arah Kompas HP Anda: ${azimuth.toInt()}°", color = Color.White, fontSize = 13.sp)
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.rotate(animatedRotation)
                ) {
                    Text("🕋", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("▲", fontSize = 28.sp, color = GoldAccent)
                    Image(
                        bitmap = kiblatBitmap,
                        contentDescription = "Kompas Panda",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(130.dp).clip(CircleShape)
                    )
                }
            }

            Text(
                text = "Letakkan HP mendatar dan putar badan Anda hingga ikon Ka'bah berada lurus di atas.",
                color = Color.White,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =========================================================================
// 2. LAYAR CHECKLIST AMALAN HARIAN (AmalanScreen)
// =========================================================================
data class AmalanHarian(val id: Int, val title: String, val pahala: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmalanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("panda_amalan_prefs", Context.MODE_PRIVATE) }
    val amalanBitmap = rememberOptimizedIcon(R.drawable.ic_panda_amalan, 120)

    val daftarAmalan = remember {
        listOf(
            AmalanHarian(1, "Sholat Subuh Tepat Waktu", "Wajib • Cahaya di Hari Kiamat"),
            AmalanHarian(2, "Dzikir Pagi & Petang", "Sunnah • Benteng Diri Harian"),
            AmalanHarian(3, "Sholat Sunnah Dhuha (2–4 Rakaat)", "Sunnah • Sedekah 360 Persendian"),
            AmalanHarian(4, "Sholat Dzuhur Tepat Waktu", "Wajib • Amalan Dicintai Allah"),
            AmalanHarian(5, "Membaca Al-Quran / Iqro", "Sunnah • 1 Huruf = 10 Kebaikan"),
            AmalanHarian(6, "Sholat Ashar Tepat Waktu", "Wajib • Penjaga Pahala"),
            AmalanHarian(7, "Sedekah Harian / Membantu Orang Tua", "Sunnah • Pembuka Pintu Rezeki"),
            AmalanHarian(8, "Sholat Maghrib Tepat Waktu", "Wajib • Keberkahan Petang"),
            AmalanHarian(9, "Sholat Isya & Witir Sebelum Tidur", "Wajib & Sunnah • Penyempurna Malam"),
            AmalanHarian(10, "Membaca Surah Al-Mulk Sebelum Tidur", "Sunnah • Pelindung di Alam Kubur")
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
            modifier = Modifier.fillMaxSize().background(OffWhiteBg).padding(padding)
        ) {
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
                        bitmap = amalanBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(66.dp).clip(CircleShape)
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
                        colors = CardDefaults.cardColors(containerColor = if (isChecked) Color(0xFFD1FAE5) else Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val nextState = !isChecked
                                checkedMap[item.id] = nextState
                                prefs.edit().putBoolean("amalan_${item.id}", nextState).apply()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.ExtraBold, color = EmeraldDark, fontSize = 14.sp)
                                Text(item.pahala, color = EmeraldPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. KALKULATOR ISLAMI SUPER LENGKAP 5-IN-1 (WARISAN FARAIDH, ZAKAT, FIDYAH, QURBAN)
// =========================================================================
data class WarisShareResult(
    val ahliWaris: String,
    val bagianKetentuan: String,
    val nominalTotal: Double,
    val nominalPerOrang: Double,
    val jumlahOrang: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalkulatorZakatScreen(onBack: () -> Unit) {
    // 0 = Warisan (Faraidh), 1 = Zakat Maal & Profesi, 2 = Zakat Fitrah, 3 = Fidyah Puasa, 4 = Aqiqah & Qurban
    var selectedTab by remember { mutableStateOf(0) }
    val formatRupiah = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }

    val tabs = listOf(
        0 to "⚖️ Warisan (Faraidh)",
        1 to "💰 Zakat Maal/Profesi",
        2 to "🌾 Zakat Fitrah",
        3 to "🍛 Fidyah Puasa",
        4 to "🐑 Aqiqah & Qurban"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kalkulator Syariah Lengkap", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                        Text("Warisan Faraidh • Zakat • Fidyah • Qurban", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
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
            // Baris Tab 5 Kalkulator Syariah
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().background(EmeraldDark)
            ) {
                items(tabs) { (idx, title) ->
                    val active = selectedTab == idx
                    Surface(
                        color = if (active) GoldAccent else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable { selectedTab = idx }
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (active) Color(0xFF0F172A) else Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    // =====================================================
                    // TAB 0: KALKULATOR PEMBAGIAN WARISAN ISLAM (FARAIDH)
                    // =====================================================
                    0 -> {
                        item {
                            WarisanFaraidhCalculatorView(formatRupiah = formatRupiah)
                        }
                    }

                    // =====================================================
                    // TAB 1: KALKULATOR ZAKAT PROFESI & MAAL (SIMPANAN/EMAS)
                    // =====================================================
                    1 -> {
                        item {
                            ZakatMaalProfesiView(formatRupiah = formatRupiah)
                        }
                    }

                    // =====================================================
                    // TAB 2: KALKULATOR ZAKAT FITRAH KELUARGA
                    // =====================================================
                    2 -> {
                        item {
                            ZakatFitrahView(formatRupiah = formatRupiah)
                        }
                    }

                    // =====================================================
                    // TAB 3: KALKULATOR FIDYAH PUASA RAMADHAN
                    // =====================================================
                    3 -> {
                        item {
                            FidyahPuasaView(formatRupiah = formatRupiah)
                        }
                    }

                    // =====================================================
                    // TAB 4: KALKULATOR AQIQAH ANAK & PATUNGAN QURBAN
                    // =====================================================
                    4 -> {
                        item {
                            AqiqahQurbanView(formatRupiah = formatRupiah)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WarisanFaraidhCalculatorView(formatRupiah: NumberFormat) {
    var totalHartaInput by remember { mutableStateOf("120000000") }
    var hutangBiayaInput by remember { mutableStateOf("0") }
    var wasiatInput by remember { mutableStateOf("0") }

    // Jenis pasangan yang ditinggalkan: "NONE", "SUAMI" (Pewaris Wanita), "ISTRI" (Pewaris Pria)
    var pasanganType by remember { mutableStateOf("ISTRI") }
    var adaAyah by remember { mutableStateOf(true) }
    var adaIbu by remember { mutableStateOf(true) }
    var jumlahAnakLaki by remember { mutableStateOf(1) }
    var jumlahAnakPerempuan by remember { mutableStateOf(1) }

    val hartaKotor = totalHartaInput.toDoubleOrNull() ?: 0.0
    val hutang = hutangBiayaInput.toDoubleOrNull() ?: 0.0
    val sisaSetelahHutang = (hartaKotor - hutang).coerceAtLeast(0.0)
    // Wasiat maksimal 1/3 dari harta setelah hutang menurut syariat Islam
    val maxWasiat = sisaSetelahHutang / 3.0
    val wasiatRaw = wasiatInput.toDoubleOrNull() ?: 0.0
    val wasiatSah = wasiatRaw.coerceAtMost(maxWasiat)
    val hartaBersihTirkah = (sisaSetelahHutang - wasiatSah).coerceAtLeast(0.0)

    val hasilPembagian = remember(
        hartaBersihTirkah, pasanganType, adaAyah, adaIbu, jumlahAnakLaki, jumlahAnakPerempuan
    ) {
        hitungFaraidhSyariah(
            tirkah = hartaBersihTirkah,
            pasangan = pasanganType,
            adaAyah = adaAyah,
            adaIbu = adaIbu,
            jmlAnakLaki = jumlahAnakLaki,
            jmlAnakPerempuan = jumlahAnakPerempuan
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "⚖️ 1. Data Harta Peninggalan (Tirkah)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = EmeraldDark
                )
                Text(
                    text = "Berdasarkan QS. An-Nisa Ayat 11–12. Masukkan nilai harta setelah dikurangi hutang & biaya pengurusan jenazah:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = totalHartaInput,
                    onValueChange = { totalHartaInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Total Harta Peninggalan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hutangBiayaInput,
                    onValueChange = { hutangBiayaInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Hutang & Biaya Jenazah (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = wasiatInput,
                    onValueChange = { wasiatInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Wasiat (Maksimal 1/3 Harta) (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(color = MintSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Harta Bersih Siap Dibagi:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldDark)
                        Text(formatRupiah.format(hartaBersihTirkah), fontWeight = FontWeight.Black, fontSize = 14.sp, color = EmeraldPrimary)
                    }
                }
            }
        }

        // Pilih Ahli Waris
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "👨‍👩‍👧‍👦 2. Pilih Ahli Waris yang Hidup",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = EmeraldDark
                )

                Text("Pasangan yang Ditinggalkan:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldDark)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        "ISTRI" to "Istri",
                        "SUAMI" to "Suami",
                        "NONE" to "Tidak Ada"
                    ).forEach { (key, label) ->
                        val active = pasanganType == key
                        Surface(
                            color = if (active) EmeraldDark else MintSurface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).clickable { pasanganType = key }
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (active) GoldAccent else EmeraldDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = adaAyah,
                        onClick = { adaAyah = !adaAyah },
                        label = { Text("👴 Ayah Kandung Hidup", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = adaIbu,
                        onClick = { adaIbu = !adaIbu },
                        label = { Text("👵 Ibu Kandung Hidup", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Counter Anak Laki-laki
                CounterRowItem(
                    label = "👦 Jumlah Anak Laki-laki:",
                    count = jumlahAnakLaki,
                    onDecrement = { if (jumlahAnakLaki > 0) jumlahAnakLaki-- },
                    onIncrement = { jumlahAnakLaki++ }
                )

                // Counter Anak Perempuan
                CounterRowItem(
                    label = "👧 Jumlah Anak Perempuan:",
                    count = jumlahAnakPerempuan,
                    onDecrement = { if (jumlahAnakPerempuan > 0) jumlahAnakPerempuan-- },
                    onIncrement = { jumlahAnakPerempuan++ }
                )
            }
        }

        // Hasil Rincian Pembagian Warisan
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "📜 Hasil Perhitungan Faraidh Syariah",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = GoldAccent
                )

                hasilPembagian.forEach { item ->
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.ahliWaris, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 14.sp)
                                Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                                    Text(
                                        text = item.bagianKetentuan,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Total Bagian: ${formatRupiah.format(item.nominalTotal)}",
                                color = MintSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (item.jumlahOrang > 1) {
                                Text(
                                    text = "➔ Per Orang (${item.jumlahOrang} orang): ${formatRupiah.format(item.nominalPerOrang)}",
                                    color = GoldAccent,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CounterRowItem(
    label: String,
    count: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(OffWhiteBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldDark)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalIconButton(onClick = onDecrement, modifier = Modifier.size(34.dp)) {
                Text("-", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Text("$count", fontWeight = FontWeight.Black, fontSize = 16.sp, color = EmeraldDark)
            FilledIconButton(
                onClick = onIncrement,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.size(34.dp)
            ) {
                Text("+", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

// Mesin Hitung Faraidh sesuai Al-Quran Surah An-Nisa Ayat 11-12
fun hitungFaraidhSyariah(
    tirkah: Double,
    pasangan: String,
    adaAyah: Boolean,
    adaIbu: Boolean,
    jmlAnakLaki: Int,
    jmlAnakPerempuan: Int
): List<WarisShareResult> {
    if (tirkah <= 0.0) return emptyList()
    val adaKeturunan = (jmlAnakLaki + jmlAnakPerempuan) > 0

    // Gunakan penyebut dasar 24 untuk menghitung porsi Furudh (Ashabul Furudh)
    var porsiPasangan = 0.0
    var ketPasangan = ""
    if (pasangan == "SUAMI") {
        porsiPasangan = if (adaKeturunan) 6.0 else 12.0 // 1/4 atau 1/2
        ketPasangan = if (adaKeturunan) "1/4 (Ada Anak)" else "1/2 (Tanpa Anak)"
    } else if (pasangan == "ISTRI") {
        porsiPasangan = if (adaKeturunan) 3.0 else 6.0 // 1/8 atau 1/4
        ketPasangan = if (adaKeturunan) "1/8 (Ada Anak)" else "1/4 (Tanpa Anak)"
    }

    var porsiIbu = 0.0
    var ketIbu = ""
    if (adaIbu) {
        porsiIbu = if (adaKeturunan) 4.0 else 8.0 // 1/6 atau 1/3
        ketIbu = if (adaKeturunan) "1/6 (Ada Anak)" else "1/3 (Tanpa Anak)"
    }

    var porsiAyah = 0.0
    var ketAyah = ""
    if (adaAyah) {
        if (jmlAnakLaki > 0) {
            porsiAyah = 4.0 // 1/6 pasti
            ketAyah = "1/6 (Ada Anak Laki)"
        } else if (jmlAnakPerempuan > 0) {
            porsiAyah = 4.0 // 1/6 + sisa nanti
            ketAyah = "1/6 + Ashabah (Sisa)"
        } else {
            ketAyah = "Ashabah (Sisa Harta)"
        }
    }

    var porsiAnakPerempuanFurudh = 0.0
    var ketAnakPerempuan = ""
    if (jmlAnakLaki == 0 && jmlAnakPerempuan > 0) {
        if (jmlAnakPerempuan == 1) {
            porsiAnakPerempuanFurudh = 12.0 // 1/2
            ketAnakPerempuan = "1/2 (Tunggal)"
        } else {
            porsiAnakPerempuanFurudh = 16.0 // 2/3
            ketAnakPerempuan = "2/3 (2+ Anak Perempuan)"
        }
    }

    val totalFurudh = porsiPasangan + porsiIbu + porsiAyah + porsiAnakPerempuanFurudh
    // Jika terjadi 'Aul (total porsi > 24), maka pembagi dinaikkan ke totalFurudh
    val pembagiUtama = if (totalFurudh > 24.0) totalFurudh else 24.0
    val sisaPorsi = (pembagiUtama - totalFurudh).coerceAtLeast(0.0)

    val results = mutableListOf<WarisShareResult>()

    if (porsiPasangan > 0) {
        val nom = tirkah * (porsiPasangan / pembagiUtama)
        results.add(WarisShareResult(if (pasangan == "SUAMI") "Suami" else "Istri", ketPasangan, nom, nom, 1))
    }

    if (adaIbu) {
        val nom = tirkah * (porsiIbu / pembagiUtama)
        results.add(WarisShareResult("Ibu Kandung", ketIbu, nom, nom, 1))
    }

    if (jmlAnakLaki > 0) {
        // Anak laki-laki bersama anak perempuan menjadi Ashabah bil Ghair (2 : 1)
        if (adaAyah) {
            val nomAyah = tirkah * (porsiAyah / pembagiUtama)
            results.add(WarisShareResult("Ayah Kandung", ketAyah, nomAyah, nomAyah, 1))
        }
        val hartaAshabah = tirkah * (sisaPorsi / pembagiUtama)
        val totalUnitAnak = (jmlAnakLaki * 2) + jmlAnakPerempuan
        val nilaiPerUnit = if (totalUnitAnak > 0) hartaAshabah / totalUnitAnak else 0.0

        val totalBagianLaki = nilaiPerUnit * 2 * jmlAnakLaki
        results.add(
            WarisShareResult(
                ahliWaris = "Anak Laki-laki ($jmlAnakLaki orang)",
                bagianKetentuan = "Ashabah (2 Bagian)",
                nominalTotal = totalBagianLaki,
                nominalPerOrang = nilaiPerUnit * 2,
                jumlahOrang = jmlAnakLaki
            )
        )
        if (jmlAnakPerempuan > 0) {
            val totalBagianPr = nilaiPerUnit * jmlAnakPerempuan
            results.add(
                WarisShareResult(
                    ahliWaris = "Anak Perempuan ($jmlAnakPerempuan orang)",
                    bagianKetentuan = "Ashabah (1 Bagian)",
                    nominalTotal = totalBagianPr,
                    nominalPerOrang = nilaiPerUnit,
                    jumlahOrang = jmlAnakPerempuan
                )
            )
        }
    } else {
        // Tidak ada anak laki-laki
        if (jmlAnakPerempuan > 0) {
            val nomPr = tirkah * (porsiAnakPerempuanFurudh / pembagiUtama)
            results.add(
                WarisShareResult(
                    ahliWaris = "Anak Perempuan ($jmlAnakPerempuan orang)",
                    bagianKetentuan = ketAnakPerempuan,
                    nominalTotal = nomPr,
                    nominalPerOrang = nomPr / jmlAnakPerempuan,
                    jumlahOrang = jmlAnakPerempuan
                )
            )
        }
        if (adaAyah) {
            val totalPorsiAyah = porsiAyah + sisaPorsi
            val nomAyah = tirkah * (totalPorsiAyah / pembagiUtama)
            results.add(WarisShareResult("Ayah Kandung", ketAyah, nomAyah, nomAyah, 1))
        }
    }

    return results
}

@Composable
fun ZakatMaalProfesiView(formatRupiah: NumberFormat) {
    var penghasilanInput by remember { mutableStateOf("10000000") }
    var tabunganEmasInput by remember { mutableStateOf("0") }
    var hargaEmasPerGram by remember { mutableStateOf("1350000") }

    val gaji = penghasilanInput.toDoubleOrNull() ?: 0.0
    val tabungan = tabunganEmasInput.toDoubleOrNull() ?: 0.0
    val emasGram = hargaEmasPerGram.toDoubleOrNull() ?: 1350000.0

    val nisabTahun = 85.0 * emasGram
    val nisabBulan = nisabTahun / 12.0
    val totalHarta = gaji + tabungan
    val zakat25 = totalHarta * 0.025
    val wajibZakat = totalHarta >= nisabBulan

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("💰 Kalkulator Zakat Profesi & Maal (2,5%)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EmeraldDark)
            OutlinedTextField(
                value = penghasilanInput,
                onValueChange = { penghasilanInput = it.filter { c -> c.isDigit() } },
                label = { Text("Penghasilan Bulanan / Harta (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tabunganEmasInput,
                onValueChange = { tabunganEmasInput = it.filter { c -> c.isDigit() } },
                label = { Text("Bonus / Tabungan / Emas (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = hargaEmasPerGram,
                onValueChange = { hargaEmasPerGram = it.filter { c -> c.isDigit() } },
                label = { Text("Harga Emas 1 Gram Saat Ini (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Surface(color = EmeraldDark, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (wajibZakat) "✅ Telah Mencapai Nisab (Wajib Zakat)" else "ℹ️ Belum Mencapai Nisab (Bisa Infaq Sunnah)",
                        color = GoldAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(formatRupiah.format(zakat25), color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
                    Text("Nisab Bulanan (Setara 85gr Emas/thn): ${formatRupiah.format(nisabBulan)}", color = MintSurface, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ZakatFitrahView(formatRupiah: NumberFormat) {
    var jumlahJiwa by remember { mutableStateOf(4) }
    var hargaBerasPerKg by remember { mutableStateOf("16000") }

    val hargaKg = hargaBerasPerKg.toDoubleOrNull() ?: 16000.0
    val totalBerasKg = jumlahJiwa * 2.5
    val totalBerasLiter = jumlahJiwa * 3.5
    val totalUangFitrah = totalBerasKg * hargaKg

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🌾 Kalkulator Zakat Fitrah Keluarga", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EmeraldDark)
            CounterRowItem(
                label = "👨‍👩‍👧‍👦 Jumlah Anggota Keluarga (Jiwa):",
                count = jumlahJiwa,
                onDecrement = { if (jumlahJiwa > 1) jumlahJiwa-- },
                onIncrement = { jumlahJiwa++ }
            )
            OutlinedTextField(
                value = hargaBerasPerKg,
                onValueChange = { hargaBerasPerKg = it.filter { c -> c.isDigit() } },
                label = { Text("Harga Beras per Kg di Daerah Anda (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Surface(color = EmeraldDark, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Zakat Fitrah ($jumlahJiwa Jiwa):", color = MintSurface, fontSize = 12.sp)
                    Text(formatRupiah.format(totalUangFitrah), color = GoldAccent, fontWeight = FontWeight.Black, fontSize = 26.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Atau Beras: $totalBerasKg Kg ($totalBerasLiter Liter)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun FidyahPuasaView(formatRupiah: NumberFormat) {
    var jumlahHariHutang by remember { mutableStateOf(7) }
    var biayaMakanPerHari by remember { mutableStateOf("45000") }

    val biaya = biayaMakanPerHari.toDoubleOrNull() ?: 45000.0
    val totalFidyahRupiah = jumlahHariHutang * biaya
    val totalFidyahBerasKg = jumlahHariHutang * 1.5

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🍛 Kalkulator Fidyah Puasa Ramadhan", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EmeraldDark)
            Text("Bagi lansia, sakit menahun, atau ibu hamil/menyusui yang tidak sanggup berpuasa:", fontSize = 12.sp, color = Color.Gray)
            CounterRowItem(
                label = "📅 Jumlah Hari Puasa Ditinggalkan:",
                count = jumlahHariHutang,
                onDecrement = { if (jumlahHariHutang > 1) jumlahHariHutang-- },
                onIncrement = { jumlahHariHutang++ }
            )
            OutlinedTextField(
                value = biayaMakanPerHari,
                onValueChange = { biayaMakanPerHari = it.filter { c -> c.isDigit() } },
                label = { Text("Standar 1 Porsi Makan / Fidyah Harian (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Surface(color = EmeraldDark, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Kewajiban Fidyah ($jumlahHariHutang Hari):", color = MintSurface, fontSize = 12.sp)
                    Text(formatRupiah.format(totalFidyahRupiah), color = GoldAccent, fontWeight = FontWeight.Black, fontSize = 26.sp)
                    Text("Atau Makanan Pokok: $totalFidyahBerasKg Kg Beras", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun AqiqahQurbanView(formatRupiah: NumberFormat) {
    var anakLaki by remember { mutableStateOf(1) }
    var anakPerempuan by remember { mutableStateOf(0) }
    var hargaKambing by remember { mutableStateOf("2800000") }
    var hargaSapiQurban by remember { mutableStateOf("21000000") }

    val kambingRp = hargaKambing.toDoubleOrNull() ?: 2800000.0
    val sapiRp = hargaSapiQurban.toDoubleOrNull() ?: 21000000.0
    val totalKambingAqiqah = (anakLaki * 2) + anakPerempuan
    val totalBiayaAqiqah = totalKambingAqiqah * kambingRp
    val patunganSapiPerOrang = sapiRp / 7.0

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🐑 Kalkulator Aqiqah Anak & Patungan Qurban", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EmeraldDark)
            CounterRowItem(
                label = "👦 Aqiqah Anak Laki-laki (2 Kambing):",
                count = anakLaki,
                onDecrement = { if (anakLaki > 0) anakLaki-- },
                onIncrement = { anakLaki++ }
            )
            CounterRowItem(
                label = "👧 Aqiqah Anak Perempuan (1 Kambing):",
                count = anakPerempuan,
                onDecrement = { if (anakPerempuan > 0) anakPerempuan-- },
                onIncrement = { anakPerempuan++ }
            )
            OutlinedTextField(
                value = hargaKambing,
                onValueChange = { hargaKambing = it.filter { c -> c.isDigit() } },
                label = { Text("Estimasi Harga 1 Ekor Kambing (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = hargaSapiQurban,
                onValueChange = { hargaSapiQurban = it.filter { c -> c.isDigit() } },
                label = { Text("Estimasi Harga 1 Ekor Sapi Qurban (7 Orang) (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Surface(color = EmeraldDark, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Kebutuhan Aqiqah ($totalKambingAqiqah Ekor Kambing):", color = MintSurface, fontSize = 12.sp)
                    Text(formatRupiah.format(totalBiayaAqiqah), color = GoldAccent, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Patungan Qurban Sapi (1/7 Bagian): ${formatRupiah.format(patunganSapiPerOrang)} / orang", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
    val khatamBitmap = rememberOptimizedIcon(R.drawable.ic_menu_khatam, 180)

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
            modifier = Modifier.fillMaxSize().background(OffWhiteBg).padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Image(
                bitmap = khatamBitmap,
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