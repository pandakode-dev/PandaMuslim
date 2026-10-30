package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SurahSummary(
    val number: Int,
    val nameLatin: String,
    val nameArabic: String,
    val meaning: String,
    val totalAyahs: Int,
    val revelationType: String,
    val description: String = ""
)

data class AyahDetail(
    val numberInSurah: Int,
    val globalNumber: Int,
    val textArabic: String,
    val textLatin: String,
    val textTranslation: String
)

enum class QuranMainMode(val tabTitle: String) {
    BACA_TERJEMAH("📑 Baca & Arti"),
    MUSHAF_SAMBUNG("📖 Mushaf Arab"),
    HAFALAN("🧠 Hafalan 5x"),
    QUIZ_AYAT("🎮 Quiz Ayat")
}

// 8 PILIHAN MUSHAF LENGKAP (DENGAN RENDERING HURUF ARAB ANTI-RUSAK!)
enum class MushafStyleOption(
    val label: String,
    val fontSizeSp: Int,
    val isTajwidColored: Boolean,
    val bgPaper: Color,
    val textColor: Color,
    val borderColor: Color
) {
    TAJWID_WARNA("🌈 Tajwid Warna", 30, true, Color(0xFFFFFFFF), Color(0xFF0F172A), Color(0xFF10B981)),
    UTSMANI_MADINAH("🕋 Utsmani Madinah", 31, false, Color(0xFFFFFDF5), Color(0xFF1C1917), Color(0xFFD97706)),
    INDOPAK_ASIA("🕌 IndoPak Jelas", 34, false, Color(0xFFF0FDF4), Color(0xFF064E3B), Color(0xFF059669)),
    ANAK_BESAR("✨ Huruf Besar Anak", 38, true, Color(0xFFFEFCE8), Color(0xFF0F172A), Color(0xFFFACC15)),
    MALAM_TAHAJUD("🌙 Mode Malam", 31, false, Color(0xFF0F172A), Color(0xFFF8FAFC), Color(0xFF38BDF8)),
    RAUDHAH_HIJAU("🌿 Raudhah Nabawi", 30, true, Color(0xFFECFDF5), Color(0xFF065F46), Color(0xFF10B981)),
    KLASIK_SEPIA("📜 Klasik Antik", 29, false, Color(0xFFFEF3C7), Color(0xFF451A03), Color(0xFFB45309)),
    HUFAZ_FOKUS("💎 Mushaf Hufaz", 32, true, Color(0xFFF8FAFC), Color(0xFF1E293B), Color(0xFF6366F1))
}

// 8 PILIHAN QARI INTERNASIONAL TIMUR TENGAH
enum class QariSelection(val label: String, val shortLabel: String, val folderKey: String) {
    ALAFASY("Syaikh Mishary Rasyid Al-Afasy (Kuwait)", "🎙️ Al-Afasy", "Alafasy_128kbps"),
    SUDAIS("Syaikh Abdurrahman As-Sudais (Imam Makkah)", "🎙️ As-Sudais", "Abdurrahmaan_As-Sudais_192kbps"),
    GHAMIDI("Syaikh Sa'ad Al-Ghamidi (Suara Syahdu)", "🎙️ Al-Ghamidi", "Ghamadi_40kbps"),
    MUAIQLY("Syaikh Maher Al-Muaiqly (Imam Masjidil Haram)", "🎙️ Al-Muaiqly", "MaherAlMuaiqly128kbps"),
    HUSARY_MUALLIM("Syaikh Al-Hussary (Tartil Guru Mengaji)", "🎙️ Al-Hussary", "Husary_Muallim_128kbps"),
    MINSHAWI_MURATTAL("Syaikh Al-Minshawi (Murattal Klasik)", "🎙️ Al-Minshawi", "Minshawy_Murattal_128kbps"),
    SHATRI("Syaikh Abu Bakr Ash-Shathri (Merdu & Jelas)", "🎙️ Ash-Shathri", "Abu_Bakr_Ash-Shaatree_128kbps"),
    RIFAI("Syaikh Hani Ar-Rifa'i (Syahdu Menyentuh Hati)", "🎙️ Hani Ar-Rifa'i", "Hani_Rifai_192kbps")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val mengajiPrefs = remember { context.getSharedPreferences("panda_mengaji_prefs", Context.MODE_PRIVATE) }

    var surahList by remember { mutableStateOf<List<SurahSummary>>(emptyList()) }
    var isLoadingSurah by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedSurah by remember { mutableStateOf<SurahSummary?>(null) }
    var ayahList by remember { mutableStateOf<List<AyahDetail>>(emptyList()) }
    var isLoadingAyah by remember { mutableStateOf(false) }

    var activeMode by remember { mutableStateOf(QuranMainMode.BACA_TERJEMAH) }
    var selectedMushafStyle by remember { mutableStateOf(MushafStyleOption.TAJWID_WARNA) }
    var selectedQari by remember { mutableStateOf(QariSelection.ALAFASY) }
    var showQariPickerDialog by remember { mutableStateOf(false) }

    var showSurahDescription by remember { mutableStateOf(false) }
    var showLatinText by remember { mutableStateOf(true) }
    var showTranslationText by remember { mutableStateOf(true) }

    var isRepeatHafalan5x by remember { mutableStateOf(false) }
    val revealedHafalanAyahs = remember { mutableStateMapOf<Int, Boolean>() }
    var hideAllArabicForHafalan by remember { mutableStateOf(true) }

    var currentlyPlayingAyahIndex by remember { mutableStateOf(-1) }
    var currentRepeatCount by remember { mutableStateOf(0) }
    var isPreparingAudio by remember { mutableStateOf(false) }

    val mediaPlayer = remember {
        MediaPlayer().apply {
            setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
            } catch (_: Exception) {}
        }
    }

    fun playRealQariAudio(index: Int, forceRepeat5x: Boolean = (activeMode == QuranMainMode.HAFALAN || isRepeatHafalan5x)) {
        if (index < 0 || index >= ayahList.size) {
            currentlyPlayingAyahIndex = -1
            return
        }
        val ayah = ayahList[index]
        val surahNum = selectedSurah?.number ?: 1

        val formattedSurah = String.format("%03d", surahNum)
        val formattedAyah = String.format("%03d", ayah.numberInSurah)
        val audioUrl = "https://everyayah.com/data/${selectedQari.folderKey}/$formattedSurah$formattedAyah.mp3"

        try {
            isPreparingAudio = true
            currentlyPlayingAyahIndex = index
            mediaPlayer.reset()
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { mp ->
                isPreparingAudio = false
                mp.start()
            }
            mediaPlayer.setOnCompletionListener {
                if (forceRepeat5x && currentRepeatCount < 4) {
                    currentRepeatCount++
                    mediaPlayer.seekTo(0)
                    mediaPlayer.start()
                } else {
                    currentRepeatCount = 0
                    if (index + 1 < ayahList.size) {
                        playRealQariAudio(index + 1, forceRepeat5x)
                    } else {
                        currentlyPlayingAyahIndex = -1
                    }
                }
            }
        } catch (_: Exception) {
            isPreparingAudio = false
            currentlyPlayingAyahIndex = -1
            Toast.makeText(context, "Pastikan internet aktif untuk memutar suara Qari.", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopQariAudio() {
        try {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.reset()
        } catch (_: Exception) {}
        currentlyPlayingAyahIndex = -1
        isPreparingAudio = false
        currentRepeatCount = 0
    }

    fun saveQuranBookmark(surahName: String, ayahNum: Int) {
        val label = "$surahName : Ayat $ayahNum"
        mengajiPrefs.edit().putString("last_quran_label", label).apply()
        syncMengajiBookmarkToServer(context, "quran", label)
        Toast.makeText(context, "🔖 Penanda mengaji disimpan: $label", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        isLoadingSurah = true
        surahList = fetchAllSurahsReliable()
        isLoadingSurah = false
    }

    LaunchedEffect(selectedSurah) {
        val surah = selectedSurah ?: return@LaunchedEffect
        stopQariAudio()
        showSurahDescription = false
        revealedHafalanAyahs.clear()
        isLoadingAyah = true
        ayahList = fetchSurahAyahsReliable(surah.number)
        isLoadingAyah = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedSurah == null) "📖 Al-Quran Al-Karim (114 Surah)"
                            else "${selectedSurah!!.number}. Surah ${selectedSurah!!.nameLatin} (${selectedSurah!!.nameArabic})",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (selectedSurah == null) "8 Gaya Mushaf • 8 Qari Timur Tengah • Hafalan 5x"
                            else "${selectedSurah!!.meaning} • ${selectedSurah!!.totalAyahs} Ayat • ${selectedQari.shortLabel}",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        stopQariAudio()
                        if (selectedSurah != null) selectedSurah = null else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
            )
        },
        bottomBar = {
            if (currentlyPlayingAyahIndex != -1 && selectedSurah != null && currentlyPlayingAyahIndex < ayahList.size) {
                val activeAyah = ayahList[currentlyPlayingAyahIndex]
                val repeatInfo = if (activeMode == QuranMainMode.HAFALAN || isRepeatHafalan5x) " • Ulang ${currentRepeatCount + 1}/5x" else ""
                Surface(
                    color = EmeraldDark,
                    shadowElevation = 14.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPreparingAudio) "⏳ Memuat Audio Syaikh..."
                                else "🔊 Ayat ${activeAyah.numberInSurah}$repeatInfo",
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = selectedQari.label,
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (currentlyPlayingAyahIndex > 0) playRealQariAudio(currentlyPlayingAyahIndex - 1)
                                }
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Sebelumnya", tint = Color.White)
                            }
                            Button(
                                onClick = { stopQariAudio() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Stop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = {
                                    if (currentlyPlayingAyahIndex + 1 < ayahList.size) playRealQariAudio(currentlyPlayingAyahIndex + 1)
                                }
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Berikutnya", tint = Color.White)
                            }
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
            if (selectedSurah == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldDark)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari Surah (contoh: Al-Fatihah, Yasin, Al-Mulk, Al-Kahfi)...", fontSize = 13.sp) },
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
                    val filteredSurahs = remember(surahList, searchQuery) {
                        if (searchQuery.isBlank()) surahList
                        else surahList.filter {
                            it.nameLatin.contains(searchQuery, ignoreCase = true) ||
                                    it.meaning.contains(searchQuery, ignoreCase = true) ||
                                    it.number.toString() == searchQuery.trim()
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredSurahs, key = { it.number }) { surah ->
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MintSurface, RoundedCornerShape(20.dp))
                                    .clickable { selectedSurah = surah }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(MintSurface)
                                                .border(1.5.dp, EmeraldPrimary, CircleShape)
                                        ) {
                                            Text("${surah.number}", fontWeight = FontWeight.Black, color = EmeraldDark, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(surah.nameLatin, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EmeraldDark)
                                            Text("${surah.meaning} • ${surah.totalAyahs} Ayat (${surah.revelationType})", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                    Text(surah.nameArabic, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                }
                            }
                        }
                    }
                }
            } else {
                // =========================================================================
                // BAR KONTROL ATAS: 4 MODE UTAMA + 8 PILIHAN MUSHAF + 8 QARI INTERNASIONAL
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldDark)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        QuranMainMode.values().forEach { mode ->
                            val isSelected = activeMode == mode
                            Surface(
                                color = if (isSelected) GoldAccent else Color.Transparent,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { activeMode = mode }
                            ) {
                                Text(
                                    text = mode.tabTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color(0xFF0F172A) else Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Tombol Pilih 8 Qari Internasional
                        item {
                            Surface(
                                color = GoldAccent,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.clickable { showQariPickerDialog = true }
                            ) {
                                Text(
                                    text = "${selectedQari.shortLabel} (Pilih 8 Qari) ▾",
                                    color = Color(0xFF0F172A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // 8 Pilihan Gaya Mushaf
                        items(MushafStyleOption.values()) { style ->
                            val active = selectedMushafStyle == style
                            Surface(
                                color = if (active) EmeraldPrimary else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .border(
                                        width = if (active) 1.5.dp else 0.dp,
                                        color = if (active) GoldAccent else Color.Transparent,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .clickable { selectedMushafStyle = style }
                            ) {
                                Text(
                                    text = style.label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        item {
                            Surface(
                                color = if (showSurahDescription) GoldAccent else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.clickable { showSurahDescription = !showSurahDescription }
                            ) {
                                Text(
                                    text = if (showSurahDescription) "ℹ️ Tutup Info" else "ℹ️ Info Surah",
                                    color = if (showSurahDescription) Color(0xFF0F172A) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Legenda Warna Tajwid (Muncul jika pengguna memilih Mushaf Tajwid Warna)
                    AnimatedVisibility(visible = selectedMushafStyle.isTajwidColored) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.28f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🟢 Ghunnah/Idgham", color = Color(0xFF6EE7B7), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("🔵 Qalqalah/Ikhfa", color = Color(0xFF93C5FD), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("🔴 Mad Panjang", color = Color(0xFFFCA5A5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                AnimatedVisibility(visible = showSurahDescription && selectedSurah!!.description.isNotBlank()) {
                    Surface(color = Color(0xFFFEF9C3), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "📜 Penjelasan Surah ${selectedSurah!!.nameLatin}:",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = EmeraldDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedSurah!!.description,
                                fontSize = 12.sp,
                                color = Color(0xFF334155),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                if (isLoadingAyah) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = EmeraldPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Memuat Ayat-Ayat Mulia...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                        }
                    }
                } else {
                    when (activeMode) {
                        QuranMainMode.MUSHAF_SAMBUNG -> {
                            MushafContinuousFullArabicPage(
                                surah = selectedSurah!!,
                                ayahs = ayahList,
                                mushafStyle = selectedMushafStyle,
                                currentlyPlayingIndex = currentlyPlayingAyahIndex,
                                onPlayFromAyah = { idx -> playRealQariAudio(idx) },
                                onBookmarkAyah = { num -> saveQuranBookmark(selectedSurah!!.nameLatin, num) }
                            )
                        }

                        QuranMainMode.QUIZ_AYAT -> {
                            SurahInteractiveQuizView(
                                surah = selectedSurah!!,
                                ayahs = ayahList,
                                onPlayAudio = { idx -> playRealQariAudio(idx, false) }
                            )
                        }

                        QuranMainMode.BACA_TERJEMAH, QuranMainMode.HAFALAN -> {
                            val isHafalanMode = (activeMode == QuranMainMode.HAFALAN)

                            LazyColumn(
                                contentPadding = PaddingValues(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = MintSurface),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Button(
                                                    onClick = { playRealQariAudio(0, isHafalanMode || isRepeatHafalan5x) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                                    shape = RoundedCornerShape(50),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = if (isHafalanMode) "Mulai Hafalan (Ulang 5x/Ayat)" else "Putar Murottal 1 Surah",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }

                                                if (isHafalanMode) {
                                                    Surface(
                                                        color = if (hideAllArabicForHafalan) EmeraldDark else GoldAccent,
                                                        shape = RoundedCornerShape(50),
                                                        modifier = Modifier.clickable {
                                                            hideAllArabicForHafalan = !hideAllArabicForHafalan
                                                            revealedHafalanAyahs.clear()
                                                        }
                                                    ) {
                                                        Text(
                                                            text = if (hideAllArabicForHafalan) "🙈 Sensor Ayat: ON" else "👁️ Sensor Ayat: OFF",
                                                            color = if (hideAllArabicForHafalan) Color.White else Color(0xFF0F172A),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Black,
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                                        )
                                                    }
                                                } else {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Surface(
                                                            color = if (showLatinText) EmeraldDark else Color.White,
                                                            shape = RoundedCornerShape(50),
                                                            modifier = Modifier.clickable { showLatinText = !showLatinText }
                                                        ) {
                                                            Text(
                                                                text = "Latin",
                                                                color = if (showLatinText) GoldAccent else EmeraldDark,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                            )
                                                        }
                                                        Surface(
                                                            color = if (showTranslationText) EmeraldDark else Color.White,
                                                            shape = RoundedCornerShape(50),
                                                            modifier = Modifier.clickable { showTranslationText = !showTranslationText }
                                                        ) {
                                                            Text(
                                                                text = "Arti",
                                                                color = if (showTranslationText) GoldAccent else EmeraldDark,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (selectedSurah!!.number != 1 && selectedSurah!!.number != 9) {
                                    item {
                                        Card(
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = selectedMushafStyle.bgPaper),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.5.dp, selectedMushafStyle.borderColor, RoundedCornerShape(20.dp))
                                        ) {
                                            Text(
                                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = selectedMushafStyle.textColor,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth().padding(14.dp)
                                            )
                                        }
                                    }
                                }

                                items(ayahList.size, key = { idx -> ayahList[idx].numberInSurah }) { index ->
                                    val ayah = ayahList[index]
                                    val isPlayingThis = (currentlyPlayingAyahIndex == index)
                                    val isRevealedInHafalan = !isHafalanMode || !hideAllArabicForHafalan || (revealedHafalanAyahs[ayah.numberInSurah] == true)
                                    val isDarkMushaf = (selectedMushafStyle == MushafStyleOption.MALAM_TAHAJUD)

                                    Card(
                                        shape = RoundedCornerShape(22.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isPlayingThis && !isDarkMushaf) Color(0xFFFEF9C3) else selectedMushafStyle.bgPaper
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = if (isPlayingThis) 2.5.dp else 1.5.dp,
                                                color = if (isPlayingThis) GoldAccent else selectedMushafStyle.borderColor.copy(alpha = 0.45f),
                                                shape = RoundedCornerShape(22.dp)
                                            )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = if (isDarkMushaf) Color(0xFF1E293B) else MintSurface,
                                                    shape = RoundedCornerShape(50)
                                                ) {
                                                    Text(
                                                        text = "Ayat ${ayah.numberInSurah}  ﴿${toArabicNumerals(ayah.numberInSurah)}﴾",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 12.sp,
                                                        color = if (isDarkMushaf) GoldAccent else EmeraldDark,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                                    )
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Surface(
                                                        color = if (isPlayingThis) Color(0xFFE11D48) else GoldAccent,
                                                        shape = RoundedCornerShape(50),
                                                        modifier = Modifier.clickable {
                                                            if (isPlayingThis) stopQariAudio()
                                                            else playRealQariAudio(index, isHafalanMode || isRepeatHafalan5x)
                                                        }
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.VolumeUp,
                                                                contentDescription = null,
                                                                tint = if (isPlayingThis) Color.White else Color(0xFF0F172A),
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = if (isPlayingThis) "Stop" else if (isHafalanMode) "Ulang 5x" else "Putar",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Black,
                                                                color = if (isPlayingThis) Color.White else Color(0xFF0F172A)
                                                            )
                                                        }
                                                    }

                                                    Surface(
                                                        color = EmeraldPrimary,
                                                        shape = RoundedCornerShape(50),
                                                        modifier = Modifier.clickable {
                                                            saveQuranBookmark(selectedSurah!!.nameLatin, ayah.numberInSurah)
                                                        }
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                        ) {
                                                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text("Tandai", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            if (isRevealedInHafalan) {
                                                // RENDER HURUF ARAB DENGAN SISTEM WORD-SAFE (100% ANTI PECAH / ANTI RUSAK!)
                                                Text(
                                                    text = buildSafeArabicMushafString(
                                                        arabic = ayah.textArabic,
                                                        isTajwidColored = selectedMushafStyle.isTajwidColored,
                                                        baseColor = selectedMushafStyle.textColor,
                                                        isDarkTheme = isDarkMushaf
                                                    ),
                                                    fontSize = selectedMushafStyle.fontSizeSp.sp,
                                                    lineHeight = (selectedMushafStyle.fontSizeSp + 26).sp,
                                                    fontFamily = FontFamily.Serif,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.End,
                                                    style = TextStyle(textDirection = TextDirection.Rtl),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable(enabled = isHafalanMode) {
                                                            revealedHafalanAyahs[ayah.numberInSurah] = false
                                                        }
                                                )
                                            } else {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(MintSurface)
                                                        .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(16.dp))
                                                        .clickable { revealedHafalanAyahs[ayah.numberInSurah] = true }
                                                        .padding(vertical = 22.dp, horizontal = 12.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = "🙈 Awal Ayat: ${ayah.textArabic.split(" ").firstOrNull() ?: ""} ...",
                                                            fontSize = 22.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = EmeraldDark
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = "👆 Lafalkan hafalanmu, lalu KETUK DI SINI untuk melihat ayat lengkap!",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = EmeraldPrimary,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }

                                            if (showLatinText && ayah.textLatin.isNotBlank() && isRevealedInHafalan) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = ayah.textLatin,
                                                    fontSize = 13.sp,
                                                    fontStyle = FontStyle.Italic,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isDarkMushaf) GoldAccent else EmeraldPrimary
                                                )
                                            }

                                            if (showTranslationText && ayah.textTranslation.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = ayah.textTranslation,
                                                    fontSize = 14.sp,
                                                    color = if (isDarkMushaf) Color(0xFFCBD5E1) else Color(0xFF334155),
                                                    lineHeight = 20.sp
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
    }

    // DIALOG PILIHAN 8 QARI INTERNASIONAL
    if (showQariPickerDialog) {
        AlertDialog(
            onDismissRequest = { showQariPickerDialog = false },
            title = {
                Text("🎙️ Pilih Suara Qari Timur Tengah", fontWeight = FontWeight.ExtraBold, color = EmeraldDark, fontSize = 17.sp)
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(QariSelection.values()) { qari ->
                        val isChosen = (qari == selectedQari)
                        Surface(
                            color = if (isChosen) MintSurface else OffWhiteBg,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isChosen) 2.dp else 1.dp,
                                    color = if (isChosen) EmeraldPrimary else Color.LightGray,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedQari = qari
                                    showQariPickerDialog = false
                                    if (currentlyPlayingAyahIndex != -1) {
                                        playRealQariAudio(currentlyPlayingAyahIndex)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = qari.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isChosen) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = EmeraldDark,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isChosen) {
                                    Text("✅ Aktif", fontSize = 11.sp, fontWeight = FontWeight.Black, color = EmeraldPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQariPickerDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// =========================================================================
// MESIN RENDERING KHAT & TAJWID WARNA BERBASIS KATA UTUH (100% ANTI PECAH!)
// =========================================================================
fun buildSafeArabicMushafString(
    arabic: String,
    isTajwidColored: Boolean,
    baseColor: Color,
    isDarkTheme: Boolean = false
): AnnotatedString {
    // Bersihkan karakter kontrol Unicode aneh yang sering membuat kotak/pecah di HP Android
    val cleanArabic = arabic
        .replace("\u06DF", "")
        .replace("\u06E0", "")
        .replace("\u06E2", "")
        .trim()

    if (!isTajwidColored) {
        return buildAnnotatedString {
            withStyle(SpanStyle(color = baseColor, fontWeight = FontWeight.Bold)) {
                append(cleanArabic)
            }
        }
    }

    // Mewarnai per KATA UTUH (Word-Level Coloring) agar sambungan ligatur huruf Arab di dalam kata TIDAK PERNAH TERPUTUS!
    val ghunnahCol = if (isDarkTheme) Color(0xFF34D399) else Color(0xFF059669) // Hijau Zamrud
    val qalqalahCol = if (isDarkTheme) Color(0xFF60A5FA) else Color(0xFF2563EB) // Biru Cerah
    val madCol = if (isDarkTheme) Color(0xFFF87171) else Color(0xFFDC2626) // Merah Tajwid

    val words = cleanArabic.split(" ")
    return buildAnnotatedString {
        words.forEachIndexed { index, word ->
            val wordColor = when {
                // 1. Kata yang mengandung Ghunnah (Nun/Mim bertasydid نّ / مّ) -> Hijau
                word.contains("نّ") || word.contains("مّ") || word.contains("نْ") || word.contains("مِ") && word.contains("ن") -> ghunnahCol
                // 2. Kata yang mengandung Mad Panjang (~ / ٰ / آ) -> Merah
                word.contains("\u0653") || word.contains("\u0670") || word.contains("آ") -> madCol
                // 3. Kata yang mengandung huruf Qalqalah Sukun (قْ طْ بْ جْ دْ) -> Biru
                word.contains("قْ") || word.contains("طْ") || word.contains("بْ") || word.contains("جْ") || word.contains("دْ") -> qalqalahCol
                else -> baseColor
            }

            withStyle(SpanStyle(color = wordColor, fontWeight = FontWeight.Bold)) {
                append(word)
            }
            if (index < words.lastIndex) {
                append(" ")
            }
        }
    }
}

@Composable
fun MushafContinuousFullArabicPage(
    surah: SurahSummary,
    ayahs: List<AyahDetail>,
    mushafStyle: MushafStyleOption,
    currentlyPlayingIndex: Int,
    onPlayFromAyah: (Int) -> Unit,
    onBookmarkAyah: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    val isDarkMushaf = (mushafStyle == MushafStyleOption.MALAM_TAHAJUD)

    val continuousMushafText = remember(ayahs, currentlyPlayingIndex, mushafStyle) {
        buildAnnotatedString {
            ayahs.forEachIndexed { idx, ayah ->
                val isPlaying = (idx == currentlyPlayingIndex)
                pushStringAnnotation(tag = "AYAH_INDEX", annotation = idx.toString())

                if (isPlaying) {
                    withStyle(
                        SpanStyle(
                            background = Color(0xFFFDE047),
                            color = Color(0xFF064E3B),
                            fontWeight = FontWeight.Black
                        )
                    ) {
                        append(ayah.textArabic.trim())
                    }
                } else {
                    append(
                        buildSafeArabicMushafString(
                            arabic = ayah.textArabic,
                            isTajwidColored = mushafStyle.isTajwidColored,
                            baseColor = mushafStyle.textColor,
                            isDarkTheme = isDarkMushaf
                        )
                    )
                }

                val arabicNum = toArabicNumerals(ayah.numberInSurah)
                withStyle(
                    SpanStyle(
                        color = if (isPlaying) Color(0xFFBE123C) else if (isDarkMushaf) GoldAccent else Color(0xFFB45309),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                ) {
                    append(" \uFD3F$arabicNum\uFD3E ")
                }
                pop()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = mushafStyle.bgPaper),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, mushafStyle.borderColor, RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(EmeraldDark, EmeraldPrimary, EmeraldDark)))
                        .border(2.dp, GoldAccent, RoundedCornerShape(16.dp))
                        .padding(vertical = 10.dp, horizontal = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${surah.totalAyahs} Ayat", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        Text("سُورَةُ ${surah.nameArabic}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text(surah.revelationType, color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onPlayFromAyah(0) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Putar Murottal Surah Penuh (Nyambung Otomatis)", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }

                Text(
                    text = "💡 Ketuk langsung pada kalimat ayat mana saja di bawah untuk memutar suara Qari & menandai batas mengaji.",
                    fontSize = 11.sp,
                    color = if (isDarkMushaf) GoldAccent else EmeraldDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (surah.number != 1 && surah.number != 9) {
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = mushafStyle.textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                    )
                    HorizontalDivider(color = mushafStyle.borderColor.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                ClickableText(
                    text = continuousMushafText,
                    style = TextStyle(
                        fontSize = mushafStyle.fontSizeSp.sp,
                        lineHeight = (mushafStyle.fontSizeSp + 30).sp,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Justify,
                        textDirection = TextDirection.Rtl
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { offset ->
                        continuousMushafText.getStringAnnotations(
                            tag = "AYAH_INDEX",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let { annotation ->
                            val clickedIdx = annotation.item.toIntOrNull() ?: 0
                            onPlayFromAyah(clickedIdx)
                            if (clickedIdx in ayahs.indices) {
                                onBookmarkAyah(ayahs[clickedIdx].numberInSurah)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SurahInteractiveQuizView(
    surah: SurahSummary,
    ayahs: List<AyahDetail>,
    onPlayAudio: (Int) -> Unit
) {
    if (ayahs.size < 2) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pilih surah dengan minimal 3 ayat untuk bermain Quiz.", fontWeight = FontWeight.Bold)
        }
        return
    }

    var currentIndex by remember(surah.number) { mutableStateOf(0) }
    var score by remember(surah.number) { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }

    val questionAyahIdx = currentIndex % (ayahs.size - 1)
    val currentAyah = ayahs[questionAyahIdx]
    val correctNextAyah = ayahs[questionAyahIdx + 1]

    val options = remember(questionAyahIdx, ayahs) {
        val distractors = ayahs.filter { it.numberInSurah != correctNextAyah.numberInSurah && it.numberInSurah != currentAyah.numberInSurah }
            .shuffled()
            .take(2)
        (distractors + correctNextAyah).shuffled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🎮 Quiz Sambung Ayat", color = GoldAccent, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text("Surah ${surah.nameLatin}", color = Color.White, fontSize = 12.sp)
                }
                Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                    Text(
                        text = "⭐ Skor: $score",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Apakah bunyi Ayat ke-${correctNextAyah.numberInSurah} setelah ayat di bawah ini?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = currentAyah.textArabic,
                    fontSize = 28.sp,
                    lineHeight = 46.sp,
                    fontWeight = FontWeight.Black,
                    color = EmeraldDark,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onPlayAudio(questionAyahIdx) },
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dengarkan Suara Ayat Ini", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        options.forEachIndexed { idx, optAyah ->
            val isCorrect = optAyah.numberInSurah == correctNextAyah.numberInSurah
            val isChosen = selectedAnswer == idx
            val cardBg = when {
                selectedAnswer == null -> Color.White
                isCorrect -> Color(0xFFD1FAE5)
                isChosen -> Color(0xFFFFE4E6)
                else -> Color.White
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, if (selectedAnswer != null && isCorrect) Color(0xFF059669) else MintSurface, RoundedCornerShape(18.dp))
                    .clickable(enabled = selectedAnswer == null) {
                        selectedAnswer = idx
                        if (isCorrect) score += 10
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = optAyah.textArabic,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (selectedAnswer != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isCorrect) "✅ Benar! (Ayat ${optAyah.numberInSurah})" else "❌ Kurang tepat (Ini Ayat ${optAyah.numberInSurah})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCorrect) Color(0xFF059669) else Color(0xFFE11D48)
                        )
                    }
                }
            }
        }

        if (selectedAnswer != null) {
            Button(
                onClick = {
                    selectedAnswer = null
                    currentIndex++
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Soal Sambung Ayat Berikutnya ➔", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
    }
}

fun toArabicNumerals(number: Int): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { ch ->
        if (ch.isDigit()) arabicDigits[ch.digitToInt()] else ch
    }.joinToString("")
}

suspend fun fetchAllSurahsReliable(): List<SurahSummary> = withContext(Dispatchers.IO) {
    val list = mutableListOf<SurahSummary>()
    try {
        val url = URL("https://equran.id/api/v2/surat")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            val data = JSONObject(conn.inputStream.bufferedReader().readText()).getJSONArray("data")
            for (i in 0 until data.length()) {
                val o = data.getJSONObject(i)
                val cleanDesc = o.optString("deskripsi", "").replace(Regex("<[^>]*>"), "")
                list.add(
                    SurahSummary(
                        number = o.getInt("nomor"),
                        nameLatin = o.getString("namaLatin"),
                        nameArabic = o.getString("nama"),
                        meaning = o.getString("arti"),
                        totalAyahs = o.getInt("jumlahAyat"),
                        revelationType = o.getString("tempatTurun"),
                        description = cleanDesc
                    )
                )
            }
        }
    } catch (_: Exception) {}

    if (list.isEmpty()) {
        return@withContext getBuiltInFallbackSurahList()
    }
    list
}

suspend fun fetchSurahAyahsReliable(surahNumber: Int): List<AyahDetail> = withContext(Dispatchers.IO) {
    val list = mutableListOf<AyahDetail>()
    try {
        val url = URL("https://equran.id/api/v2/surat/$surahNumber")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            val data = JSONObject(conn.inputStream.bufferedReader().readText())
                .getJSONObject("data")
                .getJSONArray("ayat")
            for (i in 0 until data.length()) {
                val o = data.getJSONObject(i)
                val num = o.getInt("nomorAyat")
                list.add(
                    AyahDetail(
                        numberInSurah = num,
                        globalNumber = num,
                        textArabic = o.getString("teksArab"),
                        textLatin = o.optString("teksLatin", ""),
                        textTranslation = o.getString("teksIndonesia")
                    )
                )
            }
        }
    } catch (_: Exception) {}

    if (list.isEmpty()) {
        try {
            val url2 = URL("https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,id.indonesian")
            val conn2 = url2.openConnection() as HttpURLConnection
            conn2.connectTimeout = 8000
            conn2.readTimeout = 8000
            if (conn2.responseCode == 200) {
                val dataArr = JSONObject(conn2.inputStream.bufferedReader().readText()).getJSONArray("data")
                val arabAyahs = dataArr.getJSONObject(0).getJSONArray("ayahs")
                val indoAyahs = dataArr.getJSONObject(1).getJSONArray("ayahs")
                for (i in 0 until arabAyahs.length()) {
                    val aObj = arabAyahs.getJSONObject(i)
                    val iObj = indoAyahs.getJSONObject(i)
                    val num = aObj.getInt("numberInSurah")
                    list.add(
                        AyahDetail(
                            numberInSurah = num,
                            globalNumber = num,
                            textArabic = aObj.getString("text"),
                            textLatin = "",
                            textTranslation = iObj.getString("text")
                        )
                    )
                }
            }
        } catch (_: Exception) {}
    }

    if (list.isEmpty()) {
        return@withContext getBuiltInFallbackAyahs(surahNumber)
    }
    list
}

fun getBuiltInFallbackSurahList(): List<SurahSummary> = listOf(
    SurahSummary(1, "Al-Fatihah", "الفاتحة", "Pembukaan", 7, "Makkiyah", "Surah pembuka Al-Quran yang wajib dibaca pada setiap rakaat sholat."),
    SurahSummary(112, "Al-Ikhlas", "الإخلاص", "Kemurnian Keesaan Allah", 4, "Makkiyah", "Menegaskan keesaan Allah SWT yang sebanding dengan sepertiga Al-Quran."),
    SurahSummary(113, "Al-Falaq", "الفلق", "Waktu Subuh", 5, "Makkiyah", "Doa memohon perlindungan kepada Allah dari segala kejahatan makhluk."),
    SurahSummary(114, "An-Nas", "الناس", "Umat Manusia", 6, "Makkiyah", "Doa berlindung kepada Allah dari bisikan setan yang tersembunyi.")
)

fun getBuiltInFallbackAyahs(surahNumber: Int): List<AyahDetail> {
    return when (surahNumber) {
        112 -> listOf(
            AyahDetail(1, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Qul huwallaahu ahad", "Katakanlah (Muhammad), Dialah Allah, Yang Maha Esa."),
            AyahDetail(2, 2, "اللَّهُ الصَّمَدُ", "Allaahush-shamad", "Allah tempat meminta segala sesuatu."),
            AyahDetail(3, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "Lam yalid wa lam yuulad", "(Allah) tidak beranak dan tidak pula diperanakkan."),
            AyahDetail(4, 4, "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", "Wa lam yakul-lahuu kufuwan ahad", "Dan tidak ada sesuatu yang setara dengan Dia.")
        )
        113 -> listOf(
            AyahDetail(1, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Qul a'uudzu birabbil falaq", "Katakanlah, Aku berlindung kepada Tuhan yang menguasai subuh (fajar),"),
            AyahDetail(2, 2, "مِنْ شَرِّ مَا خَلَقَ", "Min syarri maa khalaq", "dari kejahatan (makhluk yang) Dia ciptakan,"),
            AyahDetail(3, 3, "وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ", "Wa min syarri ghaasiqin idzaa waqab", "dan dari kejahatan malam apabila telah gelap gulita,"),
            AyahDetail(4, 4, "وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "Wa min syarrin-naffaatsaati fil 'uqad", "dan dari kejahatan (perempuan-perempuan) penyihir yang meniup pada buhul-buhul,"),
            AyahDetail(5, 5, "وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ", "Wa min syarri haasidin idzaa hasad", "dan dari kejahatan orang yang dengki apabila dia dengki.")
        )
        114 -> listOf(
            AyahDetail(1, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Qul a'uudzu birabbin-naas", "Katakanlah, Aku berlindung kepada Tuhannya manusia,"),
            AyahDetail(2, 2, "مَلِكِ النَّاسِ", "Malikin-naas", "Raja manusia,"),
            AyahDetail(3, 3, "إِلَٰهِ النَّاسِ", "Ilaahin-naas", "Sembahan manusia,"),
            AyahDetail(4, 4, "مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "Min syarril waswaasil khannaas", "dari kejahatan (bisikan) setan yang bersembunyi,"),
            AyahDetail(5, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Alladzii yuwaswisu fii shuduurin-naas", "yang membisikkan (kejahatan) ke dalam dada manusia,"),
            AyahDetail(6, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "Minal jinnati wan-naas", "dari (golongan) jin dan manusia.")
        )
        else -> listOf(
            AyahDetail(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Bismillaahir-rahmaanir-rahiim", "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang."),
            AyahDetail(2, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Alhamdulillaahi rabbil 'aalamiin", "Segala puji bagi Allah, Tuhan seluruh alam,"),
            AyahDetail(3, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "Ar-rahmaanir-rahiim", "Yang Maha Pengasih, Maha Penyayang,"),
            AyahDetail(4, 4, "مَالِكِ يَوْمِ الدِّينِ", "Maaliki yaumid-diin", "Pemilik hari pembalasan."),
            AyahDetail(5, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Iyyaaka na'budu wa iyyaaka nasta'iin", "Hanya kepada Engkaulah kami menyembah dan hanya kepada Engkaulah kami mohon pertolongan."),
            AyahDetail(6, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Ihdinash-shiraathal mustaqiim", "Tunjukilah kami jalan yang lurus,"),
            AyahDetail(7, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "Shiraathalladziina an'amta 'alaihim ghairil maghdhuubi 'alaihim wa ladh-dhaalliin", "(yaitu) jalan orang-orang yang telah Engkau beri nikmat kepadanya; bukan (jalan) mereka yang dimurkai, dan bukan (pula jalan) mereka yang sesat.")
        )
    }
}