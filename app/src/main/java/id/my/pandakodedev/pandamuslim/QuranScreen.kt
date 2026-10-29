package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
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

enum class MushafStyle(
    val label: String,
    val subtitle: String,
    val fontFileName: String,
    val fontDownloadUrl: String,
    val lineSpacing: Int,
    val paperColor: Color,
    val baseTextColor: Color,
    val enableTajwidColor: Boolean
) {
    TAJWID_WARNA(
        label = "🌈 Mushaf Tajwid Warna",
        subtitle = "Hukum Bacaan Berwarna",
        fontFileName = "AmiriQuran.ttf",
        fontDownloadUrl = "https://github.com/google/fonts/raw/main/ofl/amiriquran/AmiriQuran-Regular.ttf",
        lineSpacing = 56,
        paperColor = Color(0xFFFFFBEB),
        baseTextColor = Color(0xFF0F172A),
        enableTajwidColor = true
    ),
    MADINAH(
        label = "🕌 Mushaf Madinah",
        subtitle = "Khat Amiri Klasik Nabawi",
        fontFileName = "AmiriRegular.ttf",
        fontDownloadUrl = "https://github.com/google/fonts/raw/main/ofl/amiri/Amiri-Bold.ttf",
        lineSpacing = 54,
        paperColor = Color(0xFFFFF8E7),
        baseTextColor = Color(0xFF1E293B),
        enableTajwidColor = false
    ),
    MAKKAH(
        label = "🕋 Mushaf Makkah",
        subtitle = "Khat Scheherazade Haramain",
        fontFileName = "ScheherazadeNew.ttf",
        fontDownloadUrl = "https://github.com/google/fonts/raw/main/ofl/scheherazadenew/ScheherazadeNew-Bold.ttf",
        lineSpacing = 56,
        paperColor = Color(0xFFFFFFFF),
        baseTextColor = Color(0xFF064E3B),
        enableTajwidColor = false
    ),
    INDOPAK(
        label = "🌏 Mushaf IndoPak (Asia)",
        subtitle = "Khat Lateef Bulat & Besar",
        fontFileName = "LateefBold.ttf",
        fontDownloadUrl = "https://github.com/google/fonts/raw/main/ofl/lateef/Lateef-Bold.ttf",
        lineSpacing = 58,
        paperColor = Color(0xFFF0FDF4),
        baseTextColor = Color(0xFF0F172A),
        enableTajwidColor = true
    ),
    NASKHI_ANAK(
        label = "🧒 Khat Naskhi Anak",
        subtitle = "Huruf Jelas + Tajwid Warna",
        fontFileName = "NotoNaskhArabic.ttf",
        fontDownloadUrl = "https://github.com/google/fonts/raw/main/ofl/notonaskharabic/NotoNaskhArabic%5Bwght%5D.ttf",
        lineSpacing = 52,
        paperColor = Color(0xFFF0F9FF),
        baseTextColor = Color(0xFF0F172A),
        enableTajwidColor = true
    )
}

enum class QuranMode(val label: String) {
    BACA("📖 Mode Baca"),
    HAFALAN("🔁 Mode Hafalan"),
    QUIZ("🧠 Mode Quiz")
}

@Composable
fun rememberArabicFontFamily(style: MushafStyle): FontFamily {
    val context = LocalContext.current
    var fontFamily by remember(style) { mutableStateOf<FontFamily>(FontFamily.Serif) }

    LaunchedEffect(style) {
        withContext(Dispatchers.IO) {
            try {
                val fontFile = File(context.filesDir, style.fontFileName)
                if (!fontFile.exists() || fontFile.length() < 1000L) {
                    val url = URL(style.fontDownloadUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 10000
                    conn.readTimeout = 10000
                    conn.instanceFollowRedirects = true
                    if (conn.responseCode == 200) {
                        conn.inputStream.use { input ->
                            fontFile.outputStream().use { output -> input.copyTo(output) }
                        }
                    }
                }
                if (fontFile.exists() && fontFile.length() > 1000L) {
                    val tf = Typeface.createFromFile(fontFile)
                    withContext(Dispatchers.Main) { fontFamily = FontFamily(tf) }
                }
            } catch (_: Exception) {}
        }
    }
    return fontFamily
}

fun buildTajwidColoredText(arabicText: String, baseColor: Color, enableColor: Boolean): AnnotatedString {
    if (!enableColor) return AnnotatedString(arabicText)

    val ghunnahColor = Color(0xFF16A34A)
    val qalqalahColor = Color(0xFF0284C7)
    val madColor = Color(0xFFE11D48)
    val ikhfaTanwinColor = Color(0xFFD97706)

    val qalqalahLetters = setOf('ق', 'ط', 'ب', 'ج', 'د')
    val tanwinMarks = setOf('\u064B', '\u064C', '\u064D')
    val sukunMarks = setOf('\u0652', '\u06E1')

    return buildAnnotatedString {
        append(arabicText)
        addStyle(SpanStyle(color = baseColor), 0, arabicText.length)

        val len = arabicText.length
        for (i in 0 until len) {
            val ch = arabicText[i]
            val next1 = if (i + 1 < len) arabicText[i + 1] else ' '
            val next2 = if (i + 2 < len) arabicText[i + 2] else ' '

            if (ch in qalqalahLetters && (next1 in sukunMarks || next2 in sukunMarks)) {
                val end = (i + 3).coerceAtMost(len)
                addStyle(SpanStyle(color = qalqalahColor, fontWeight = FontWeight.ExtraBold), i, end)
            }
            if ((ch == 'ن' || ch == 'م') && (next1 == '\u0651' || next2 == '\u0651')) {
                val end = (i + 3).coerceAtMost(len)
                addStyle(SpanStyle(color = ghunnahColor, fontWeight = FontWeight.ExtraBold), i, end)
            }
            if ((ch == 'ن' && next1 in sukunMarks) || ch in tanwinMarks) {
                val start = (i - 1).coerceAtLeast(0)
                val end = (i + 2).coerceAtMost(len)
                addStyle(SpanStyle(color = ikhfaTanwinColor, fontWeight = FontWeight.ExtraBold), start, end)
            }
            if (ch == '\u0653' || ch == '\u0670') {
                val start = (i - 1).coerceAtLeast(0)
                val end = (i + 1).coerceAtMost(len)
                addStyle(SpanStyle(color = madColor, fontWeight = FontWeight.ExtraBold), start, end)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val mengajiPrefs = remember { context.getSharedPreferences("panda_mengaji_prefs", Context.MODE_PRIVATE) }

    var surahList by remember { mutableStateOf<List<SurahInfo>>(emptyList()) }
    var isLoadingSurah by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSurah by remember { mutableStateOf<SurahInfo?>(null) }
    var targetScrollAyat by remember { mutableStateOf(1) }

    var savedSurahNo by remember { mutableStateOf(mengajiPrefs.getInt("last_surah_no", 1)) }
    var savedAyatNo by remember { mutableStateOf(mengajiPrefs.getInt("last_ayat_no", 1)) }
    var savedLabel by remember { mutableStateOf(mengajiPrefs.getString("last_quran_label", "Al-Fatihah : Ayat 1") ?: "Al-Fatihah : Ayat 1") }

    val quranIconBitmap = rememberOptimizedIcon(R.drawable.ic_panda_quran, 100)

    LaunchedEffect(Unit) {
        surahList = fetchSurahList()
        isLoadingSurah = false
    }

    if (selectedSurah != null) {
        SurahDetailScreen(
            surah = selectedSurah!!,
            initialScrollAyat = targetScrollAyat,
            onBookmarkSaved = { sNo, aNo, label ->
                savedSurahNo = sNo
                savedAyatNo = aNo
                savedLabel = label
            },
            onBackToList = {
                selectedSurah = null
                targetScrollAyat = 1
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                bitmap = quranIconBitmap,
                                contentDescription = "Quran Panda",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Al-Quran & Penanda Mengaji", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
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
                // Kolom Pencarian & Kartu Lompat ke Batas Mengaji Terakhir
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldDark)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Kartu Lanjutkan Mengaji
                    Surface(
                        color = GoldAccent,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val found = surahList.find { it.nomor == savedSurahNo }
                                if (found != null) {
                                    targetScrollAyat = savedAyatNo
                                    selectedSurah = found
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("📌 Batas Mengaji Terakhir Anda:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Text(savedLabel, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                }
                            }
                            Text("Lanjutkan ➔", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF0F172A))
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari surah (contoh: Yasin, Al-Mulk, Al-Kahf)...", fontSize = 13.sp) },
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
                        CircularProgressIndicator(color = EmeraldPrimary)
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
                                    .clickable {
                                        targetScrollAyat = 1
                                        selectedSurah = surah
                                    }
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
                                        Text("${surah.nomor}", fontWeight = FontWeight.ExtraBold, color = EmeraldDark, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(surah.namaLatin, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EmeraldDark)
                                        Text("${surah.arti} • ${surah.jumlahAyat} Ayat (${surah.tempatTurun})", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Text(surah.namaArab, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
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
    initialScrollAyat: Int = 1,
    onBookmarkSaved: (Int, Int, String) -> Unit,
    onBackToList: () -> Unit
) {
    val context = LocalContext.current
    val mengajiPrefs = remember { context.getSharedPreferences("panda_mengaji_prefs", Context.MODE_PRIVATE) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var bookmarkedSurah by remember { mutableStateOf(mengajiPrefs.getInt("last_surah_no", 1)) }
    var bookmarkedAyat by remember { mutableStateOf(mengajiPrefs.getInt("last_ayat_no", 1)) }

    var ayatList by remember { mutableStateOf<List<AyatItem>>(emptyList()) }
    var isLoadingAyat by remember { mutableStateOf(true) }

    var selectedMode by remember { mutableStateOf(QuranMode.BACA) }
    var selectedStyle by remember { mutableStateOf(MushafStyle.TAJWID_WARNA) }
    var arabicFontSize by remember { mutableStateOf(30f) }
    var showLatin by remember { mutableStateOf(true) }
    var showTranslation by remember { mutableStateOf(true) }

    val activeArabicFont = rememberArabicFontFamily(selectedStyle)

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
                        if (nextAyat != null) playAyatAudio(nextAyat, 1)
                        else {
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
        if (initialScrollAyat > 1) {
            coroutineScope.launch {
                listState.scrollToItem((initialScrollAyat - 1).coerceAtLeast(0))
            }
        }
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
                            text = "Ketuk 🔖 pada ayat untuk tandai batas mengaji",
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
                Surface(color = EmeraldDark, shadowElevation = 12.dp, modifier = Modifier.fillMaxWidth()) {
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
                        modifier = Modifier.weight(1f).clickable { selectedMode = mode }
                    ) {
                        Text(
                            text = mode.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color(0xFF0F172A) else Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 9.dp)
                        )
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().background(MintSurface)
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
                            Text(style.label, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (chosen) GoldAccent else EmeraldDark)
                            Text(style.subtitle, fontSize = 9.sp, color = if (chosen) Color.White else Color.Gray)
                        }
                    }
                }
            }

            if (selectedStyle.enableTajwidColor) {
                Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TajwidLegendDot("Ghunnah", Color(0xFF16A34A))
                        TajwidLegendDot("Ikhfa/Tanwin", Color(0xFFD97706))
                        TajwidLegendDot("Qalqalah", Color(0xFF0284C7))
                        TajwidLegendDot("Mad Panjang", Color(0xFFE11D48))
                    }
                }
            }

            AnimatedVisibility(visible = showSettingsPanel || selectedMode != QuranMode.BACA) {
                Card(
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                Text("🧠 Mode Quiz Hafalan (Ketuk kartu ayat untuk membuka jawaban):", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = EmeraldDark)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = quizDifficulty == 1,
                                        onClick = { quizDifficulty = 1; revealedAyatNumbers.clear() },
                                        label = { Text("Sembunyikan Akhir Ayat", fontSize = 11.sp) }
                                    )
                                    FilterChip(
                                        selected = quizDifficulty == 2,
                                        onClick = { quizDifficulty = 2; revealedAyatNumbers.clear() },
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
                                    TextButton(onClick = { if (arabicFontSize > 22f) arabicFontSize -= 2f }) {
                                        Text("A-", fontWeight = FontWeight.ExtraBold, color = EmeraldDark)
                                    }
                                    Text("${arabicFontSize.toInt()} pt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { if (arabicFontSize < 48f) arabicFontSize += 2f }) {
                                        Text("A+", fontWeight = FontWeight.ExtraBold, color = EmeraldDark)
                                    }
                                }
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
                    state = listState,
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
                                    fontFamily = activeArabicFont,
                                    fontSize = 30.sp,
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
                        val isBookmarked = (bookmarkedSurah == surah.nomor && bookmarkedAyat == ayat.nomorAyat)

                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isPlayingThis -> Color(0xFFE0F7FA)
                                    isBookmarked -> Color(0xFFFEF3C7)
                                    else -> selectedStyle.paperColor
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isPlayingThis || isBookmarked) 2.5.dp else 1.dp,
                                    color = if (isBookmarked) Color(0xFFD97706) else if (isPlayingThis) EmeraldPrimary else GoldAccent.copy(alpha = 0.35f),
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
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                        if (isBookmarked) {
                                            Surface(color = Color(0xFFD97706), shape = RoundedCornerShape(50)) {
                                                Text(
                                                    text = "📌 Batas Mengaji",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Tombol Tandai Batas Mengaji
                                        FilledTonalIconButton(
                                            onClick = {
                                                val label = "${surah.namaLatin} : Ayat ${ayat.nomorAyat}"
                                                bookmarkedSurah = surah.nomor
                                                bookmarkedAyat = ayat.nomorAyat
                                                mengajiPrefs.edit()
                                                    .putInt("last_surah_no", surah.nomor)
                                                    .putInt("last_ayat_no", ayat.nomorAyat)
                                                    .putString("last_quran_label", label)
                                                    .apply()
                                                onBookmarkSaved(surah.nomor, ayat.nomorAyat, label)
                                                syncMengajiBookmarkToServer(context, "quran", label)
                                                Toast.makeText(context, "🔖 Batas mengaji ditandai: $label", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = "Tandai Mengaji",
                                                tint = if (isBookmarked) Color(0xFFD97706) else EmeraldDark
                                            )
                                        }

                                        // Tombol Play Audio Murottal
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
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                val rawArabic = remember(ayat.teksArab, selectedMode, quizDifficulty, isRevealed) {
                                    if (selectedMode != QuranMode.QUIZ || isRevealed) ayat.teksArab
                                    else maskArabicForQuiz(ayat.teksArab, quizDifficulty)
                                }

                                val styledArabic = remember(rawArabic, selectedStyle) {
                                    buildTajwidColoredText(
                                        arabicText = rawArabic,
                                        baseColor = selectedStyle.baseTextColor,
                                        enableColor = selectedStyle.enableTajwidColor
                                    )
                                }

                                Text(
                                    text = styledArabic,
                                    fontFamily = activeArabicFont,
                                    fontSize = arabicFontSize.sp,
                                    lineHeight = selectedStyle.lineSpacing.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )

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

@Composable
fun TajwidLegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
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