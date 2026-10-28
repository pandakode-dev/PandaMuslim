package id.my.pandakodedev.pandamuslim

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SurahInfo(
    val nomor: Int,
    val namaArab: String,
    val namaLatin: String,
    val jumlahAyat: Int,
    val tempatTurun: String,
    val arti: String
)

data class AyatItem(
    val nomorAyat: Int,
    val teksArab: String,
    val teksLatin: String,
    val teksIndonesia: String,
    val audioUrl: String
)

// 6 Pilihan Gaya Mushaf (Timur Tengah, Makkah, Madinah, IndoPak, Naskhi, Modern)
enum class MushafStyle(
    val label: String,
    val subtitle: String,
    val fontFamily: FontFamily,
    val fontWeight: FontWeight,
    val lineSpacing: Int,
    val letterSpacingSp: Float,
    val paperColor: Color,
    val textColor: Color
) {
    MADINAH(
        label = "🕌 Mushaf Madinah",
        subtitle = "Kertas Krem Khas Nabawi",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        lineSpacing = 50,
        letterSpacingSp = 0.5f,
        paperColor = Color(0xFFFFFBEB),
        textColor = Color(0xFF1E293B)
    ),
    MAKKAH(
        label = "🕋 Mushaf Makkah",
        subtitle = "Khat Tebal Masjidil Haram",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.ExtraBold,
        lineSpacing = 54,
        letterSpacingSp = 1.0f,
        paperColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF064E3B)
    ),
    TIMUR_TENGAH(
        label = "📜 Timur Tengah Klasik",
        subtitle = "Rasm Utsmani Renggang",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        lineSpacing = 56,
        letterSpacingSp = 1.6f,
        paperColor = Color(0xFFFAF5EF),
        textColor = Color(0xFF27272A)
    ),
    INDOPAK(
        label = "🌏 Mushaf IndoPak (Asia)",
        subtitle = "Bulat & Ramah Anak-Anak",
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        lineSpacing = 52,
        letterSpacingSp = 0.2f,
        paperColor = Color(0xFFF0FDF4),
        textColor = Color(0xFF0F172A)
    ),
    NASKHI_ANAK(
        label = "🧒 Khat Naskhi Anak",
        subtitle = "Huruf Jelas & Mudah Dibaca",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        lineSpacing = 48,
        letterSpacingSp = 0.8f,
        paperColor = Color(0xFFF0F9FF),
        textColor = Color(0xFF0369A1)
    ),
    MODERN(
        label = "✨ Mushaf Digital Modern",
        subtitle = "Ringkas & Tajam",
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        lineSpacing = 46,
        letterSpacingSp = 0f,
        paperColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF0F172A)
    )
}

enum class QuranMode(val label: String) {
    BACA("📖 Mode Baca"),
    HAFALAN("🔁 Mode Hafalan"),
    QUIZ("🧠 Mode Quiz")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(onBack: () -> Unit) {
    var surahList by remember { mutableStateOf<List<SurahInfo>>(emptyList()) }
    var isLoadingSurah by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSurah by remember { mutableStateOf<SurahInfo?>(null) }

    LaunchedEffect(Unit) {
        surahList = fetchSurahList()
        isLoadingSurah = false
    }

    if (selectedSurah != null) {
        SurahDetailScreen(
            surah = selectedSurah!!,
            onBackToList = { selectedSurah = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_panda_quran),
                                contentDescription = "Quran Panda",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Al-Quran 114 Surah", fontWeight = FontWeight.ExtraBold, color = Color.White)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldDark)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama surah (contoh: Yasin, Al-Mulk, An-Naba)...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isLoadingSurah) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = EmeraldPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Memuat 114 Surah Al-Quran...", color = EmeraldDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    val filteredList = remember(surahList, searchQuery) {
                        if (searchQuery.isBlank()) surahList
                        else surahList.filter {
                            it.namaLatin.contains(searchQuery, ignoreCase = true) ||
                                    it.arti.contains(searchQuery, ignoreCase = true) ||
                                    it.nomor.toString() == searchQuery.trim()
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList, key = { it.nomor }) { surah ->
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSurah = surah }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(MintSurface)
                                            .border(1.5.dp, EmeraldPrimary, CircleShape)
                                    ) {
                                        Text(
                                            text = "${surah.nomor}",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldDark,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = surah.namaLatin,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = EmeraldDark
                                        )
                                        Text(
                                            text = "${surah.arti} • ${surah.jumlahAyat} Ayat (${surah.tempatTurun})",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Text(
                                        text = surah.namaArab,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    surah: SurahInfo,
    onBackToList: () -> Unit
) {
    var ayatList by remember { mutableStateOf<List<AyatItem>>(emptyList()) }
    var isLoadingAyat by remember { mutableStateOf(true) }

    var selectedMode by remember { mutableStateOf(QuranMode.BACA) }
    var selectedStyle by remember { mutableStateOf(MushafStyle.MADINAH) }
    var arabicFontSize by remember { mutableStateOf(28f) }
    var showLatin by remember { mutableStateOf(true) }
    var showTranslation by remember { mutableStateOf(true) }

    val repeatOptions = listOf(1, 3, 5, 7)
    var targetRepeatCount by remember { mutableStateOf(5) }
    var currentRepeatProgress by remember { mutableStateOf(0) }
    var autoContinueNextAyat by remember { mutableStateOf(true) }

    var quizDifficulty by remember { mutableStateOf(1) }
    val revealedAyatNumbers = remember { mutableStateMapOf<Int, Boolean>() }

    var currentlyPlayingAyat by remember { mutableStateOf<Int?>(null) }
    var isPreparingAudio by remember { mutableStateOf(false) }
    val mediaPlayer = remember { MediaPlayer() }

    fun playAyatAudio(ayat: AyatItem, repeatNumber: Int = 1) {
        try {
            isPreparingAudio = true
            currentlyPlayingAyat = ayat.nomorAyat
            currentRepeatProgress = repeatNumber

            mediaPlayer.reset()
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mediaPlayer.setDataSource(ayat.audioUrl)
            mediaPlayer.prepareAsync()

            mediaPlayer.setOnPreparedListener { mp ->
                isPreparingAudio = false
                mp.start()
            }

            mediaPlayer.setOnCompletionListener {
                val maxRepeat = if (selectedMode == QuranMode.HAFALAN) targetRepeatCount else 1
                if (currentRepeatProgress < maxRepeat) {
                    playAyatAudio(ayat, currentRepeatProgress + 1)
                } else {
                    if (autoContinueNextAyat) {
                        val nextAyat = ayatList.find { it.nomorAyat == ayat.nomorAyat + 1 }
                        if (nextAyat != null) {
                            playAyatAudio(nextAyat, 1)
                        } else {
                            currentlyPlayingAyat = null
                            currentRepeatProgress = 0
                        }
                    } else {
                        currentlyPlayingAyat = null
                        currentRepeatProgress = 0
                    }
                }
            }
        } catch (_: Exception) {
            isPreparingAudio = false
            currentlyPlayingAyat = null
        }
    }

    fun stopAudio() {
        try {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.reset()
        } catch (_: Exception) {}
        currentlyPlayingAyat = null
        currentRepeatProgress = 0
        isPreparingAudio = false
    }

    DisposableEffect(Unit) {
        onDispose {
            try { mediaPlayer.release() } catch (_: Exception) {}
        }
    }

    LaunchedEffect(surah.nomor) {
        isLoadingAyat = true
        ayatList = fetchSurahDetail(surah.nomor)
        isLoadingAyat = false
    }

    var showSettingsPanel by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${surah.nomor}. ${surah.namaLatin} (${surah.namaArab})",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "${selectedStyle.label} • ${surah.jumlahAyat} Ayat",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        stopAudio()
                        onBackToList()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsPanel = !showSettingsPanel }) {
                        Icon(Icons.Default.Settings, contentDescription = "Pengaturan", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
            )
        },
        bottomBar = {
            if (currentlyPlayingAyat != null) {
                Surface(
                    color = EmeraldDark,
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPreparingAudio) "Memuat Audio Ayat $currentlyPlayingAyat..."
                                else "Memutar ${surah.namaLatin} : Ayat $currentlyPlayingAyat",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            val maxRep = if (selectedMode == QuranMode.HAFALAN) targetRepeatCount else 1
                            Text(
                                text = "Putaran ke-$currentRepeatProgress dari ${maxRep}x • Syaikh Misyari Rasyid",
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = { stopAudio() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Stop", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBg)
                .padding(padding)
        ) {
            // Bar Pemilih 3 Mode Utama: Baca, Hafalan 5x, Quiz
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuranMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    Surface(
                        color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMode = mode }
                    ) {
                        Text(
                            text = mode.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) EmeraldDark else Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 9.dp)
                        )
                    }
                }
            }

            // BARIS PEMILIH CEPAT 6 JENIS KHAT / TULISAN (MAKKAH, MADINAH, TIMUR TENGAH, INDOPAK, ANAK)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MintSurface)
            ) {
                items(MushafStyle.values()) { style ->
                    val chosen = selectedStyle == style
                    Surface(
                        color = if (chosen) EmeraldDark else Color.White,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .border(
                                width = if (chosen) 2.dp else 1.dp,
                                color = if (chosen) GoldAccent else EmeraldPrimary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedStyle = style }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = style.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (chosen) GoldAccent else EmeraldDark
                            )
                            Text(
                                text = style.subtitle,
                                fontSize = 9.sp,
                                color = if (chosen) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }

            // Panel Kontrol Mode Hafalan / Quiz / Ukuran Huruf
            AnimatedVisibility(visible = showSettingsPanel || selectedMode != QuranMode.BACA) {
                Card(
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (selectedMode == QuranMode.HAFALAN) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🔁 Ulangi Audio Per Ayat:", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = EmeraldDark)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    repeatOptions.forEach { countOption ->
                                        val active = targetRepeatCount == countOption
                                        Surface(
                                            color = if (active) EmeraldPrimary else MintSurface,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.clickable { targetRepeatCount = countOption }
                                        ) {
                                            Text(
                                                text = "${countOption}x",
                                                color = if (active) Color.White else EmeraldDark,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (selectedMode == QuranMode.QUIZ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "🧠 Mode Quiz Hafalan (Ketuk kartu ayat untuk membuka jawaban):",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = EmeraldDark
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = quizDifficulty == 1,
                                        onClick = {
                                            quizDifficulty = 1
                                            revealedAyatNumbers.clear()
                                        },
                                        label = { Text("Sembunyikan Akhir Ayat", fontSize = 11.sp) }
                                    )
                                    FilterChip(
                                        selected = quizDifficulty == 2,
                                        onClick = {
                                            quizDifficulty = 2
                                            revealedAyatNumbers.clear()
                                        },
                                        label = { Text("Sembunyikan Full Ayat", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        if (showSettingsPanel) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ukuran Huruf Arab:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { if (arabicFontSize > 20f) arabicFontSize -= 2f }) {
                                        Text("A-", fontWeight = FontWeight.ExtraBold, color = EmeraldDark)
                                    }
                                    Text("${arabicFontSize.toInt()} pt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { if (arabicFontSize < 46f) arabicFontSize += 2f }) {
                                        Text("A+", fontWeight = FontWeight.ExtraBold, color = EmeraldDark)
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                FilterChip(
                                    selected = showLatin,
                                    onClick = { showLatin = !showLatin },
                                    label = { Text("Tampilkan Latin", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = showTranslation,
                                    onClick = { showTranslation = !showTranslation },
                                    label = { Text("Tampilkan Terjemahan", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            if (isLoadingAyat) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (surah.nomor != 9 && surah.nomor != 1) {
                        item(key = "bismillah_banner") {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp)
                                )
                            }
                        }
                    }

                    items(ayatList, key = { it.nomorAyat }) { ayat ->
                        val isPlayingThis = currentlyPlayingAyat == ayat.nomorAyat
                        val isRevealed = revealedAyatNumbers[ayat.nomorAyat] == true

                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPlayingThis) Color(0xFFE0F7FA) else selectedStyle.paperColor
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isPlayingThis) 6.dp else 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isPlayingThis) 2.dp else 1.dp,
                                    color = if (isPlayingThis) EmeraldPrimary else GoldAccent.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .clickable {
                                    if (selectedMode == QuranMode.QUIZ) {
                                        revealedAyatNumbers[ayat.nomorAyat] = !isRevealed
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(color = EmeraldDark, shape = RoundedCornerShape(50)) {
                                            Text(
                                                text = "Ayat ${ayat.nomorAyat}",
                                                color = GoldAccent,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                            )
                                        }
                                        if (isPlayingThis) {
                                            val maxRep = if (selectedMode == QuranMode.HAFALAN) targetRepeatCount else 1
                                            Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                                                Text(
                                                    text = "🔊 Putaran $currentRepeatProgress/${maxRep}x",
                                                    color = EmeraldDark,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    FilledIconButton(
                                        onClick = {
                                            if (isPlayingThis) stopAudio()
                                            else playAyatAudio(ayat, 1)
                                        },
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = if (isPlayingThis) Color(0xFFE11D48) else EmeraldPrimary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingThis) Icons.Default.Close else Icons.Default.PlayArrow,
                                            contentDescription = "Putar Murottal",
                                            tint = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                val displayedArabic = remember(ayat.teksArab, selectedMode, quizDifficulty, isRevealed) {
                                    if (selectedMode != QuranMode.QUIZ || isRevealed) {
                                        ayat.teksArab
                                    } else {
                                        maskArabicForQuiz(ayat.teksArab, quizDifficulty)
                                    }
                                }

                                Text(
                                    text = displayedArabic,
                                    fontFamily = selectedStyle.fontFamily,
                                    fontWeight = selectedStyle.fontWeight,
                                    fontSize = arabicFontSize.sp,
                                    lineHeight = selectedStyle.lineSpacing.sp,
                                    letterSpacing = selectedStyle.letterSpacingSp.sp,
                                    color = selectedStyle.textColor,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (selectedMode == QuranMode.QUIZ) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = if (isRevealed) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (isRevealed) "✅ Jawaban Terbuka (Ketuk lagi untuk menutup)"
                                            else "🔒 Mode Quiz Aktif: Sambung bacaan ayat ini, lalu ketuk kartu untuk cek jawaban!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRevealed) Color(0xFF065F46) else Color(0xFF92400E),
                                            modifier = Modifier.padding(10.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                if (selectedMode != QuranMode.QUIZ || isRevealed) {
                                    if (showLatin && ayat.teksLatin.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = ayat.teksLatin,
                                            fontSize = 13.sp,
                                            fontStyle = FontStyle.Italic,
                                            fontWeight = FontWeight.SemiBold,
                                            color = EmeraldPrimary
                                        )
                                    }
                                    if (showTranslation && ayat.teksIndonesia.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = ayat.teksIndonesia,
                                            fontSize = 14.sp,
                                            color = Color(0xFF334155)
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
}

fun maskArabicForQuiz(arabicText: String, difficulty: Int): String {
    val words = arabicText.trim().split("\\s+".toRegex())
    if (words.isEmpty()) return arabicText

    return if (difficulty == 2) {
        val firstWord = words.first()
        "$firstWord  • • • • • • ❓"
    } else {
        val halfIndex = (words.size / 2).coerceAtLeast(1)
        val visiblePart = words.take(halfIndex).joinToString(" ")
        "$visiblePart   [ • • • ❓ ]"
    }
}

suspend fun fetchSurahList(): List<SurahInfo> = withContext(Dispatchers.IO) {
    val list = mutableListOf<SurahInfo>()
    try {
        val url = URL("https://equran.id/api/v2/surat")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().readText()
            val dataArray = JSONObject(response).getJSONArray("data")
            for (i in 0 until dataArray.length()) {
                val obj = dataArray.getJSONObject(i)
                list.add(
                    SurahInfo(
                        nomor = obj.getInt("nomor"),
                        namaArab = obj.getString("nama"),
                        namaLatin = obj.getString("namaLatin"),
                        jumlahAyat = obj.getInt("jumlahAyat"),
                        tempatTurun = obj.getString("tempatTurun"),
                        arti = obj.getString("arti")
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    list
}

suspend fun fetchSurahDetail(nomorSurah: Int): List<AyatItem> = withContext(Dispatchers.IO) {
    val list = mutableListOf<AyatItem>()
    try {
        val url = URL("https://equran.id/api/v2/surat/$nomorSurah")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().readText()
            val dataObj = JSONObject(response).getJSONObject("data")
            val ayatArray = dataObj.getJSONArray("ayat")
            for (i in 0 until ayatArray.length()) {
                val obj = ayatArray.getJSONObject(i)
                val audioObj = obj.getJSONObject("audio")
                val audioUrl = audioObj.optString("05", audioObj.optString("01", ""))
                list.add(
                    AyatItem(
                        nomorAyat = obj.getInt("nomorAyat"),
                        teksArab = obj.getString("teksArab"),
                        teksLatin = obj.getString("teksLatin"),
                        teksIndonesia = obj.getString("teksIndonesia"),
                        audioUrl = audioUrl
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    list
}