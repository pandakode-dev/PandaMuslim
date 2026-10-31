package id.my.pandakodedev.pandamuslim

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyImsakiyahRow(
    val gregorianDate: String,
    val dayNameIndo: String,
    val hijriDay: Int,
    val hijriMonthName: String,
    val hijriYear: String,
    val imsak: String,
    val subuh: String,
    val dzuhur: String,
    val ashar: String,
    val maghribBuka: String,
    val isya: String,
    val fastingBadge: String, // Contoh: "Puasa Senin", "Ayyamul Bidh", "Puasa Ramadhan"
    val isToday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImsakiyahPuasaScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val cities = remember {
        listOf("Pekanbaru", "Jakarta", "Bandung", "Surabaya", "Medan", "Padang", "Palembang", "Makassar", "Yogyakarta", "Balikpapan", "Banda Aceh", "Banjarmasin")
    }
    var selectedCity by remember { mutableStateOf("Pekanbaru") }
    var activeTab by remember { mutableStateOf(0) } // 0 = Tabel Imsakiyah 30 Hari, 1 = Puasa Sunnah, 2 = Doa & Niat Puasa

    var scheduleList by remember { mutableStateOf<List<DailyImsakiyahRow>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val mediaPlayer = remember { MediaPlayer() }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            val engine = tts
            if (status == TextToSpeech.SUCCESS && engine != null) {
                ArabicWaqafPhoneticEngine.configureStrictArabicVoice(engine, speechRate = 0.75f)
            }
        }
        ttsEngine = tts
        onDispose {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
            } catch (_: Exception) {}
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun playFastingPrayerAudio(arabic: String, mp3Url: String = "") {
        if (mp3Url.isNotBlank()) {
            try {
                ttsEngine?.stop()
                mediaPlayer.reset()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mediaPlayer.setDataSource(mp3Url)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener { it.start() }
                return
            } catch (_: Exception) {}
        }
        ttsEngine?.let { tts ->
            ArabicWaqafPhoneticEngine.configureStrictArabicVoice(tts, speechRate = 0.74f)
            val waqafArabic = ArabicWaqafPhoneticEngine.toWaqafRecitationText(arabic)
            tts.speak(waqafArabic, TextToSpeech.QUEUE_FLUSH, null, "puasa_doa")
        }
    }

    LaunchedEffect(selectedCity) {
        isLoading = true
        scheduleList = fetchMonthlyImsakiyahFromAladhan(selectedCity)
        isLoading = false
    }

    val todayItem = remember(scheduleList) {
        scheduleList.find { it.isToday } ?: scheduleList.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🌙 Jadwal Imsakiyah & Puasa", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                        Text("Metode Kemenag RI • Kota $selectedCity", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        ttsEngine?.stop()
                        onBack()
                    }) {
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
            // 1. Pilihan Kota & Tab Mode
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(cities) { city ->
                        val isChosen = city == selectedCity
                        Surface(
                            color = if (isChosen) GoldAccent else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { selectedCity = city }
                        ) {
                            Text(
                                text = "📍 $city",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isChosen) Color(0xFF0F172A) else Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        0 to "📅 Imsakiyah 30 Hari",
                        1 to "🌟 Puasa Sunnah",
                        2 to "🤲 Niat & Buka Puasa"
                    ).forEach { (idx, title) ->
                        val active = activeTab == idx
                        Surface(
                            color = if (active) GoldAccent else Color.Transparent,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = idx }
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (active) Color(0xFF0F172A) else Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // KARTU HIGHLIGHT IMSAK & BUKA PUASA HARI INI
                    todayItem?.let { today ->
                        item {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, GoldAccent, RoundedCornerShape(24.dp))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                                            Text(
                                                text = "HARI INI • ${today.dayNameIndo}, ${today.gregorianDate}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF0F172A),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        Text(
                                            text = "${today.hijriDay} ${today.hijriMonthName} ${today.hijriYear} H",
                                            color = MintSurface,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        ImsakHighlightBadge("⏳ WAKTU IMSAK", today.imsak, Color(0xFFFEF08A))
                                        ImsakHighlightBadge("🕌 SUBUH", today.subuh, Color.White)
                                        ImsakHighlightBadge("🥤 MAGHRIB (BUKA)", today.maghribBuka, Color(0xFF6EE7B7))
                                    }

                                    if (today.fastingBadge.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "✨ Rekomendasi Ibadah Hari Ini: ${today.fastingBadge}",
                                                color = GoldAccent,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    when (activeTab) {
                        0 -> {
                            // TAB 1: TABEL JADWAL IMSAKIYAH 1 BULAN PENUH
                            item {
                                Surface(
                                    color = EmeraldPrimary,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Tanggal", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, modifier = Modifier.weight(1.3f))
                                        Text("Imsak", color = GoldAccent, fontWeight = FontWeight.Black, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                        Text("Subuh", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                        Text("Dzuhur", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                        Text("Maghrib", color = Color(0xFFA7F3D0), fontWeight = FontWeight.Black, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            items(scheduleList) { row ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (row.isToday) Color(0xFFFEF9C3) else Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (row.isToday) 2.dp else 1.dp,
                                            color = if (row.isToday) Color(0xFFD97706) else MintSurface,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1.3f)) {
                                                Text(
                                                    text = "${row.dayNameIndo}, ${row.gregorianDate.take(6)}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = EmeraldDark
                                                )
                                                Text(
                                                    text = "${row.hijriDay} ${row.hijriMonthName}",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            Text(row.imsak, fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color(0xFFD97706), textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                            Text(row.subuh, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldDark, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                            Text(row.dzuhur, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.DarkGray, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                            Text(row.maghribBuka, fontWeight = FontWeight.Black, fontSize = 13.sp, color = EmeraldPrimary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                        }

                                        if (row.fastingBadge.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "🌙 ${row.fastingBadge}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = EmeraldPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // TAB 2: DAFTAR TANGGAL PUASA SUNNAH BULAN INI (SENIN-KAMIS & AYYAMUL BIDH)
                            val sunnahDays = scheduleList.filter { it.fastingBadge.isNotBlank() }
                            items(sunnahDays) { row ->
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.5.dp, MintSurface, RoundedCornerShape(20.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Surface(color = Color(0xFFD1FAE5), shape = RoundedCornerShape(50)) {
                                                Text(
                                                    text = "🌟 ${row.fastingBadge}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = EmeraldDark,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "${row.dayNameIndo}, ${row.gregorianDate}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = EmeraldDark
                                            )
                                            Text(
                                                text = "Bertepatan dengan ${row.hijriDay} ${row.hijriMonthName} ${row.hijriYear} H",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Imsak: ${row.imsak}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                            Text("Buka: ${row.maghribBuka}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = EmeraldPrimary)
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // TAB 3: DOA NIAT PUASA & BUKA PUASA BERSUARA WAQAF BENAR
                            val fastingPrayers = listOf(
                                Triple(
                                    "🌙 Niat Puasa Ramadhan",
                                    "نَوَيْتُ صَوْمَ غَدٍ عَنْ أَدَاءِ فَرْضِ شَهْرِ رَمَضَانَ هَٰذِهِ السَّنَةِ لِلَّهِ تَعَالَىٰ",
                                    "Nawaitu shauma ghadin 'an adaa'i fardhi syahri Ramadhaana haadzihis-sanati lillaahi Ta'aalaa.\n\nArtinya: \"Aku berniat puasa esok hari untuk menunaikan kewajiban bulan Ramadhan tahun ini karena Allah Ta'ala.\""
                                ),
                                Triple(
                                    "🥤 Doa Berbuka Puasa (Sesuai Sunnah)",
                                    "ذَهَبَ الظَّمَأُ ، وَابْتَلَّتِ الْعُرُوقُ ، وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
                                    "Dzahabadz-zhama'u, wabtallatil 'uruuqu, wa tsabatal ajru insyaa Allaah.\n\nArtinya: \"Telah hilang rasa haus, telah basah urat-urat, dan telah tetap pahala, insya Allah.\" (HR. Abu Dawud)"
                                ),
                                Triple(
                                    "🌟 Niat Puasa Sunnah Senin",
                                    "نَوَيْتُ صَوْمَ يَوْمِ الِاثْنَيْنِ سُنَّةً لِلَّهِ تَعَالَىٰ",
                                    "Nawaitu shauma yaumil itsnaini sunnatan lillaahi Ta'aalaa.\n\nArtinya: \"Aku berniat puasa sunnah hari Senin karena Allah Ta'ala.\""
                                ),
                                Triple(
                                    "🌟 Niat Puasa Sunnah Kamis",
                                    "نَوَيْتُ صَوْمَ يَوْمِ الْخَمِيسِ سُنَّةً لِلَّهِ تَعَالَىٰ",
                                    "Nawaitu shauma yaumil khamiisi sunnatan lillaahi Ta'aalaa.\n\nArtinya: \"Aku berniat puasa sunnah hari Kamis karena Allah Ta'ala.\""
                                ),
                                Triple(
                                    "🌕 Niat Puasa Sunnah Ayyamul Bidh (Tgl 13, 14, 15 Hijriah)",
                                    "نَوَيْتُ صَوْمَ أَيَّامِ الْبِيضِ سُنَّةً لِلَّهِ تَعَالَىٰ",
                                    "Nawaitu shauma ayyaamil biidhi sunnatan lillaahi Ta'aalaa.\n\nArtinya: \"Aku berniat puasa sunnah Ayyamul Bidh (hari-hari putih pertengahan bulan) karena Allah Ta'ala.\""
                                )
                            )

                            items(fastingPrayers) { (title, arab, desc) ->
                                Card(
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(title, fontWeight = FontWeight.Black, fontSize = 15.sp, color = EmeraldDark, modifier = Modifier.weight(1f))
                                            Surface(
                                                color = GoldAccent,
                                                shape = RoundedCornerShape(50),
                                                modifier = Modifier.clickable { playFastingPrayerAudio(arab) }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Dengar", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = arab,
                                            fontSize = 26.sp,
                                            lineHeight = 44.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldDark,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(desc, fontSize = 13.sp, color = Color(0xFF334155), lineHeight = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImsakHighlightBadge(label: String, time: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MintSurface)
        Spacer(modifier = Modifier.height(2.dp))
        Text(time, fontSize = 22.sp, fontWeight = FontWeight.Black, color = valueColor)
    }
}

// =========================================================================
// FETCHER OPEN-SOURCE API ALADHAN (KALENDER IMSAKIYAH KEMENAG RI 30 HARI)
// =========================================================================
suspend fun fetchMonthlyImsakiyahFromAladhan(city: String): List<DailyImsakiyahRow> = withContext(Dispatchers.IO) {
    val result = mutableListOf<DailyImsakiyahRow>()
    try {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val todayStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        val url = URL("https://api.aladhan.com/v1/calendarByCity/$year/$month?city=$city&country=Indonesia&method=20")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.requestMethod = "GET"

        if (conn.responseCode == 200) {
            val dataArray = JSONObject(conn.inputStream.bufferedReader().readText()).getJSONArray("data")
            for (i in 0 until dataArray.length()) {
                val obj = dataArray.getJSONObject(i)
                val timings = obj.getJSONObject("timings")
                val dateObj = obj.getJSONObject("date")
                val greg = dateObj.getJSONObject("gregorian")
                val hijri = dateObj.getJSONObject("hijri")

                val gregDate = greg.getString("date") // dd-MM-yyyy
                val readableDate = gregDate.replace("-", " ")
                val engDay = greg.getJSONObject("weekday").getString("en")
                val indoDay = when (engDay) {
                    "Monday" -> "Senin"
                    "Tuesday" -> "Selasa"
                    "Wednesday" -> "Rabu"
                    "Thursday" -> "Kamis"
                    "Friday" -> "Jumat"
                    "Saturday" -> "Sabtu"
                    else -> "Ahad"
                }

                val hDay = hijri.getString("day").toIntOrNull() ?: 1
                val hMonthEn = hijri.getJSONObject("month").getString("en")
                val hYear = hijri.getString("year")

                val fastingRecommendations = mutableListOf<String>()
                if (hMonthEn.contains("Ramadan", ignoreCase = true)) {
                    fastingRecommendations.add("Puasa Wajib Ramadhan")
                } else {
                    if (indoDay == "Senin") fastingRecommendations.add("Puasa Sunnah Senin")
                    if (indoDay == "Kamis") fastingRecommendations.add("Puasa Sunnah Kamis")
                    if (hDay in 13..15) fastingRecommendations.add("Puasa Ayyamul Bidh (Tgl $hDay Hijriah)")
                    if (hMonthEn.contains("Muḥarram", true) && hDay == 10) fastingRecommendations.add("Puasa Asyura (10 Muharram)")
                    if (hMonthEn.contains("Dhū al-Ḥijjah", true) && hDay == 9) fastingRecommendations.add("Puasa Arafah (9 Dzulhijjah)")
                }

                result.add(
                    DailyImsakiyahRow(
                        gregorianDate = readableDate,
                        dayNameIndo = indoDay,
                        hijriDay = hDay,
                        hijriMonthName = hMonthEn,
                        hijriYear = hYear,
                        imsak = timings.getString("Imsak").take(5),
                        subuh = timings.getString("Fajr").take(5),
                        dzuhur = timings.getString("Dhuhr").take(5),
                        ashar = timings.getString("Asr").take(5),
                        maghribBuka = timings.getString("Maghrib").take(5),
                        isya = timings.getString("Isha").take(5),
                        fastingBadge = fastingRecommendations.joinToString(" & "),
                        isToday = (gregDate == todayStr)
                    )
                )
            }
        }
    } catch (_: Exception) {}
    result
}