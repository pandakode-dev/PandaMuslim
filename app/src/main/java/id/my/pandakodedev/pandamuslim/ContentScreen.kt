package id.my.pandakodedev.pandamuslim

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Locale

data class ServerContent(
    val id: Int,
    val title: String,
    val arabicText: String,
    val latinText: String,
    val description: String,
    val mediaUrl: String,
    val jilidLevel: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicContentScreen(
    menuTitle: String,
    categoryKey: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val mengajiPrefs = remember { context.getSharedPreferences("panda_mengaji_prefs", Context.MODE_PRIVATE) }

    var items by remember { mutableStateOf<List<ServerContent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var doaHaditsSubTab by remember { mutableStateOf(0) }
    var belajarSubTab by remember { mutableStateOf("SEMUA") }

    var iqroTabMode by remember { mutableStateOf(0) }
    var selectedJilid by remember { mutableStateOf(mengajiPrefs.getInt("last_iqro_jilid", 1)) }
    var bookmarkedIqroId by remember { mutableStateOf(mengajiPrefs.getInt("last_iqro_id", 901)) }
    var bookmarkedIqroLabel by remember {
        mutableStateOf(mengajiPrefs.getString("last_iqro_label", "Iqro 1 • Hal 1 (Alif - Ba)") ?: "Iqro 1 • Hal 1")
    }
    var selectedHarakatMode by remember { mutableStateOf(0) }

    var zoomedContent by remember { mutableStateOf<ServerContent?>(null) }
    var activeSpeakingTitle by remember { mutableStateOf<String?>(null) }
    var currentlyReadingExplanationId by remember { mutableStateOf<Int?>(null) }

    val expandedDescriptionIds = remember { mutableStateMapOf<Int, Boolean>() }
    var activeInAppVideoItem by remember { mutableStateOf<ServerContent?>(null) }

    var isPlayingBackgroundMedia by remember { mutableStateOf(false) }
    var isPreparingBackgroundMedia by remember { mutableStateOf(false) }
    var backgroundMediaTitle by remember { mutableStateOf("") }

    val bgMediaPlayer = remember {
        MediaPlayer().apply {
            setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        }
    }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val saudiLocale = Locale("ar", "SA")
                val res = ttsInstance?.setLanguage(saudiLocale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsInstance?.setLanguage(Locale("id", "ID"))
                }
                ttsInstance?.setPitch(1.0f)
                ttsInstance?.setSpeechRate(0.75f)
            }
        }
        ttsEngine = ttsInstance

        onDispose {
            try {
                if (bgMediaPlayer.isPlaying) bgMediaPlayer.stop()
                bgMediaPlayer.release()
            } catch (_: Exception) {}
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    fun playAuthenticIslamicAudio(arabicText: String, latinText: String, rawMediaUrl: String = "") {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        currentlyReadingExplanationId = null
        activeSpeakingTitle = "🔊 Melafalkan Ayat/Doa: $arabicText"

        val parts = rawMediaUrl.split("|")
        val directMp3Url = parts.find { it.matches(Regex(".*\\.(mp3|wav|ogg|m4a)$", RegexOption.IGNORE_CASE)) } ?: ""

        if (directMp3Url.isNotBlank()) {
            try {
                ttsEngine?.stop()
                bgMediaPlayer.reset()
                bgMediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                bgMediaPlayer.setDataSource(directMp3Url)
                bgMediaPlayer.prepareAsync()
                bgMediaPlayer.setOnPreparedListener { mp -> mp.start() }
                return
            } catch (_: Exception) {}
        }

        ttsEngine?.let { tts ->
            val saudiLocale = Locale("ar", "SA")
            val genArabLocale = Locale("ar")
            if (tts.isLanguageAvailable(saudiLocale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.language = saudiLocale
            } else {
                tts.language = genArabLocale
            }
            tts.setPitch(1.0f)
            tts.setSpeechRate(0.70f)

            val normalizedArabic = normalizeArabicForAuthenticRecitation(arabicText)
            tts.speak(normalizedArabic, TextToSpeech.QUEUE_FLUSH, null, "makhraj_arab")
        }
    }

    fun toggleReadIndonesianExplanation(item: ServerContent) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        try {
            if (bgMediaPlayer.isPlaying) bgMediaPlayer.stop()
        } catch (_: Exception) {}

        if (currentlyReadingExplanationId == item.id) {
            ttsEngine?.stop()
            currentlyReadingExplanationId = null
            activeSpeakingTitle = null
        } else {
            currentlyReadingExplanationId = item.id
            val cleanTitle = item.title.replace(Regex("\\[.*?\\]"), "").trim()
            val cleanDesc = item.description
                .replace("📌", "")
                .replace("🔊", "")
                .replace("📖", "")
                .replace("🌟", "")
                .replace("⚠️", "Perhatian: ")
                .replace("🛡️", "")
                .replace("•", ", ")
                .replace("➔", " menjadi ")

            activeSpeakingTitle = "🎙️ Membacakan Penjelasan: $cleanTitle"

            ttsEngine?.let { tts ->
                tts.language = Locale("id", "ID")
                tts.setPitch(1.03f)
                tts.setSpeechRate(0.92f)
                val fullSpeech = "$cleanTitle. ${if (item.latinText.isNotBlank()) "Bacaan: ${item.latinText}." else ""} Penjelasan: $cleanDesc"
                tts.speak(fullSpeech, TextToSpeech.QUEUE_FLUSH, null, "indo_explanation_${item.id}")
            }
        }
    }

    fun playBackgroundLockScreenMedia(item: ServerContent) {
        val rawUrl = item.mediaUrl
        val parts = rawUrl.split("|")
        val directStreamUrl = parts.find {
            it.matches(Regex(".*\\.(mp4|mp3|wav|ogg|m4a)$", RegexOption.IGNORE_CASE))
        } ?: ""

        if (directStreamUrl.isNotBlank()) {
            try {
                ttsEngine?.stop()
                isPreparingBackgroundMedia = true
                backgroundMediaTitle = item.title
                bgMediaPlayer.reset()
                bgMediaPlayer.setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                bgMediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                bgMediaPlayer.setDataSource(directStreamUrl)
                bgMediaPlayer.prepareAsync()
                bgMediaPlayer.setOnPreparedListener { mp ->
                    isPreparingBackgroundMedia = false
                    isPlayingBackgroundMedia = true
                    mp.start()
                    Toast.makeText(context, "🔒 Mode Layar Terkunci Aktif! Suara tetap berjalan walau layar dimatikan.", Toast.LENGTH_LONG).show()
                }
                bgMediaPlayer.setOnCompletionListener {
                    isPlayingBackgroundMedia = false
                }
            } catch (_: Exception) {
                isPreparingBackgroundMedia = false
                isPlayingBackgroundMedia = false
            }
        } else {
            isPreparingBackgroundMedia = false
            isPlayingBackgroundMedia = true
            backgroundMediaTitle = "${item.title} (Mode Audio)"
            toggleReadIndonesianExplanation(item)
            Toast.makeText(context, "🔒 Mode Dengar Audio Aktif! Anda bisa mengunci layar HP sekarang.", Toast.LENGTH_LONG).show()
        }
    }

    fun stopBackgroundMedia() {
        try {
            if (bgMediaPlayer.isPlaying) bgMediaPlayer.stop()
            bgMediaPlayer.reset()
        } catch (_: Exception) {}
        ttsEngine?.stop()
        currentlyReadingExplanationId = null
        isPlayingBackgroundMedia = false
        isPreparingBackgroundMedia = false
    }

    fun saveIqroBookmark(item: ServerContent) {
        bookmarkedIqroId = item.id
        bookmarkedIqroLabel = item.title
        mengajiPrefs.edit()
            .putInt("last_iqro_id", item.id)
            .putInt("last_iqro_jilid", item.jilidLevel)
            .putString("last_iqro_label", item.title)
            .apply()
        syncMengajiBookmarkToServer(context, "iqro", item.title)
        Toast.makeText(context, "🔖 Batas mengaji Iqro disimpan: ${item.title}", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(categoryKey) {
        isLoading = true
        val serverItems = fetchContentsFromServer(categoryKey)
        val builtInItems = getBuiltInFallbackContents(categoryKey)
        items = if (serverItems.isNotEmpty()) serverItems + builtInItems else builtInItems
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(menuTitle, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                        when (categoryKey) {
                            "iqro" -> Text("📌 Terakhir: $bookmarkedIqroLabel", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            "doa" -> Text("🤲 Koleksi Doa Harian & Hadits Pilihan Bersuara", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            "belajar" -> Text("🛡️ Adab Pergaulan, Anti-Zina, Panduan & Bacaan Sholat", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            "tajwid" -> Text("📖 18 Hukum Tajwid Lengkap & Suara Penjelasan", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            "video" -> Text("🎬 Putar di Aplikasi & Mode Layar Terkunci 🔒", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        stopBackgroundMedia()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
            )
        },
        bottomBar = {
            if (isPlayingBackgroundMedia || isPreparingBackgroundMedia || currentlyReadingExplanationId != null) {
                Surface(
                    color = EmeraldDark,
                    shadowElevation = 14.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(color = GoldAccent, shape = CircleShape, modifier = Modifier.size(38.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (currentlyReadingExplanationId != null) Icons.Default.RecordVoiceOver else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentlyReadingExplanationId != null) "🔊 Sedang Membacakan Penjelasan Materi"
                                    else "🔒 Tetap Hidup Saat Layar Terkunci",
                                    color = GoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = activeSpeakingTitle ?: backgroundMediaTitle,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { stopBackgroundMedia() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Stop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
            AnimatedVisibility(visible = activeInAppVideoItem != null) {
                activeInAppVideoItem?.let { playingItem ->
                    InAppUniversalVideoPlayerBox(
                        item = playingItem,
                        onSwitchToLockScreenAudio = {
                            val itemToBg = playingItem
                            activeInAppVideoItem = null
                            playBackgroundLockScreenMedia(itemToBg)
                        },
                        onClosePlayer = { activeInAppVideoItem = null }
                    )
                }
            }

            if (categoryKey == "doa") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to "🌟 Semua (${items.size})",
                        1 to "🤲 Doa Harian",
                        2 to "📜 Hadits Pilihan"
                    ).forEach { (idx, label) ->
                        val active = doaHaditsSubTab == idx
                        Surface(
                            color = if (active) GoldAccent else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f).clickable { doaHaditsSubTab = idx }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (active) Color(0xFF0F172A) else Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            if (categoryKey == "belajar") {
                val belajarCategories = listOf(
                    "SEMUA" to "🌟 Semua Materi",
                    "ADAB" to "🛡️ Adab & Benteng Pergaulan (PENTING!)",
                    "SHOLAT" to "🕌 Panduan Sholat",
                    "BACAAN_SHOLAT" to "🤲 Bacaan Sholat",
                    "WUDHU" to "💧 Wudhu & Bersuci",
                    "AQIDAH" to "☝️ Rukun Iman & Islam"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldDark)
                ) {
                    items(belajarCategories) { (key, label) ->
                        val active = belajarSubTab == key
                        val isAdabTab = key == "ADAB"
                        Surface(
                            color = when {
                                active -> GoldAccent
                                isAdabTab -> Color(0xFFE11D48)
                                else -> Color.White.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(
                                    width = if (isAdabTab && !active) 1.5.dp else 0.dp,
                                    color = if (isAdabTab && !active) GoldAccent else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { belajarSubTab = key }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (active) Color(0xFF0F172A) else Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            if (categoryKey == "iqro") {
                IqroTopBarTabs(
                    iqroTabMode = iqroTabMode,
                    onSelectTabMode = { iqroTabMode = it },
                    selectedJilid = selectedJilid,
                    onSelectJilid = { selectedJilid = it },
                    selectedHarakatMode = selectedHarakatMode,
                    onSelectHarakat = { selectedHarakatMode = it },
                    activeSpeakingTitle = activeSpeakingTitle
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.align(Alignment.Center))
                } else if (categoryKey == "iqro" && iqroTabMode == 0) {
                    val jilidItems = remember(items, selectedJilid) {
                        items.filter { it.jilidLevel == selectedJilid || (selectedJilid == 1 && it.id < 900) }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(jilidItems, key = { it.id }) { item ->
                            IqroVisualSoundCard(
                                item = item,
                                isBookmarked = item.id == bookmarkedIqroId,
                                onPlaySound = { playAuthenticIslamicAudio(item.arabicText, item.latinText, item.mediaUrl) },
                                onBookmarkClick = { saveIqroBookmark(item) },
                                onZoomClick = {
                                    playAuthenticIslamicAudio(item.arabicText, item.latinText, item.mediaUrl)
                                    zoomedContent = item
                                }
                            )
                        }
                    }
                } else if (categoryKey == "iqro" && iqroTabMode == 1) {
                    IqroReadingSheetGeneratorView(
                        selectedJilid = selectedJilid,
                        onNextJilid = { selectedJilid = if (selectedJilid < 6) selectedJilid + 1 else 1 },
                        onPlaySound = { arab, latin -> playAuthenticIslamicAudio(arab, latin) }
                    )
                } else if (categoryKey == "iqro" && iqroTabMode == 2) {
                    val hijaiyahList = remember { get30HijaiyahSoundCards() }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(hijaiyahList, key = { it.latinName }) { card ->
                            val dispArab = when (selectedHarakatMode) {
                                1 -> card.kasrah
                                2 -> card.dhammah
                                3 -> card.tanwin
                                else -> card.fathah
                            }
                            val dispRead = when (selectedHarakatMode) {
                                1 -> card.readKasrah
                                2 -> card.readDhammah
                                3 -> card.readTanwin
                                else -> card.readFathah
                            }

                            HijaiyahInteractiveTile(
                                arabic = dispArab,
                                soundRead = dispRead,
                                latinName = card.latinName,
                                cardColor = card.cardColor,
                                onTap = { playAuthenticIslamicAudio(dispArab, dispRead) }
                            )
                        }
                    }
                } else {
                    val displayedList = remember(items, categoryKey, doaHaditsSubTab, belajarSubTab) {
                        when (categoryKey) {
                            "doa" -> when (doaHaditsSubTab) {
                                1 -> items.filter { !it.title.contains("Hadits", ignoreCase = true) }
                                2 -> items.filter { it.title.contains("Hadits", ignoreCase = true) }
                                else -> items
                            }
                            "belajar" -> when (belajarSubTab) {
                                "ADAB" -> items.filter { it.title.contains("[Adab]", ignoreCase = true) }
                                "SHOLAT" -> items.filter { it.title.contains("[Sholat]", ignoreCase = true) }
                                "BACAAN_SHOLAT" -> items.filter { it.title.contains("[Bacaan Sholat]", ignoreCase = true) }
                                "WUDHU" -> items.filter { it.title.contains("[Wudhu]", ignoreCase = true) }
                                "AQIDAH" -> items.filter { it.title.contains("[Aqidah]", ignoreCase = true) }
                                else -> items
                            }
                            else -> items
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (categoryKey == "belajar") {
                            item {
                                Card(
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF881337)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, GoldAccent, RoundedCornerShape(22.dp))
                                        .clickable {
                                            belajarSubTab = if (belajarSubTab == "ADAB") "SEMUA" else "ADAB"
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(GoldAccent)
                                        ) {
                                            Text("🛡️", fontSize = 26.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                                                Text(
                                                    text = "⚠️ PERISAI AKHLAK & BENTENG REMAJA MUSLIM",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF0F172A),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Utamakan Adab Sebelum Ilmu & Jauhi Pergaulan Bebas (Zina)",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = if (belajarSubTab == "ADAB") "Menampilkan khusus materi Adab & Benteng Pergaulan (Ketuk untuk lihat semua)"
                                                else "👉 Ketuk di sini untuk fokus membuka 10 Materi Adab & Benteng Diri!",
                                                fontSize = 11.sp,
                                                color = GoldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        items(displayedList, key = { it.id }) { item ->
                            val isVideoCategory = categoryKey == "video" ||
                                    extractYoutubeVideoId(item.mediaUrl) != null ||
                                    item.mediaUrl.contains(".mp4", ignoreCase = true)

                            val isAdabHighlighted = item.title.contains("[Adab]", ignoreCase = true)
                            val isDescOpen = expandedDescriptionIds[item.id] ?: false
                            val isReadingThisDesc = (currentlyReadingExplanationId == item.id)
                            val previewBannerUrl = extractMediaPreviewUrl(item.mediaUrl)
                            val hasQariMp3 = item.mediaUrl.contains(".mp3", ignoreCase = true)

                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAdabHighlighted) Color(0xFFFFFBEB) else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isAdabHighlighted) 5.dp else 3.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isAdabHighlighted) 2.dp else 1.dp,
                                        color = if (isAdabHighlighted) Color(0xFFD97706) else MintSurface,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                            ) {
                                Column {
                                    if (previewBannerUrl.isNotBlank() || isVideoCategory) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(16f / 9f)
                                                .background(Brush.linearGradient(listOf(EmeraldDark, EmeraldPrimary)))
                                                .clickable {
                                                    if (isVideoCategory) {
                                                        stopBackgroundMedia()
                                                        activeInAppVideoItem = item
                                                    }
                                                }
                                        ) {
                                            if (previewBannerUrl.isNotBlank()) {
                                                NetworkImageLoader(
                                                    imageUrl = previewBannerUrl,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                MosqueSilhouetteOrnamentCanvas(
                                                    accentColor = GoldAccent,
                                                    modifier = Modifier.matchParentSize()
                                                )
                                            }

                                            if (isVideoCategory) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.30f))
                                                )
                                                Surface(
                                                    color = Color(0xFFE11D48),
                                                    shape = CircleShape,
                                                    shadowElevation = 8.dp,
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .border(2.5.dp, Color.White, CircleShape)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Putar di Aplikasi",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(36.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                if (isAdabHighlighted) {
                                                    Surface(color = Color(0xFFE11D48), shape = RoundedCornerShape(50)) {
                                                        Text(
                                                            text = "🛡️ UTAMAKAN ADAB & BENTENG AKHLAK",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = Color.White,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(5.dp))
                                                } else if (hasQariMp3) {
                                                    Surface(color = Color(0xFFD1FAE5), shape = RoundedCornerShape(50)) {
                                                        Text(
                                                            text = "🎙️ Suara Qari Syaikh Al-Afasy",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = EmeraldDark,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }

                                                Text(
                                                    text = item.title,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 16.sp,
                                                    color = if (isAdabHighlighted) Color(0xFF881337) else EmeraldDark
                                                )
                                            }

                                            if (item.arabicText.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    color = GoldAccent,
                                                    shape = RoundedCornerShape(50),
                                                    modifier = Modifier.clickable {
                                                        playAuthenticIslamicAudio(item.arabicText, item.latinText, item.mediaUrl)
                                                    }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(Icons.Default.VolumeUp, contentDescription = "Suara Arab", tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Lafadz", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                                    }
                                                }
                                            }
                                        }

                                        if (item.arabicText.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = item.arabicText,
                                                fontSize = 27.sp,
                                                lineHeight = 46.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldDark,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        if (!isVideoCategory && item.latinText.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(item.latinText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        if (isVideoCategory) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Button(
                                                    onClick = {
                                                        stopBackgroundMedia()
                                                        activeInAppVideoItem = item
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(5.dp))
                                                    Text("Putar Video", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                                                }

                                                Button(
                                                    onClick = {
                                                        activeInAppVideoItem = null
                                                        playBackgroundLockScreenMedia(item)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(15.dp))
                                                    Spacer(modifier = Modifier.width(5.dp))
                                                    Text("Layar Mati (Audio)", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }

                                        Surface(
                                            color = if (isDescOpen) EmeraldDark else if (isAdabHighlighted) Color(0xFFFEF08A) else MintSurface,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { expandedDescriptionIds[item.id] = !isDescOpen }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (isDescOpen) "📖 Tutup Arti & Penjelasan" else "📖 Buka Arti & Penjelasan (+ Suara Penjelasan 🔊)",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isDescOpen) GoldAccent else EmeraldDark
                                                )
                                                Icon(
                                                    imageVector = if (isDescOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = if (isDescOpen) GoldAccent else EmeraldDark
                                                )
                                            }
                                        }

                                        AnimatedVisibility(visible = isDescOpen) {
                                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                                Surface(
                                                    color = OffWhiteBg,
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(1.5.dp, if (isAdabHighlighted) GoldAccent else MintSurface, RoundedCornerShape(16.dp))
                                                ) {
                                                    Column(modifier = Modifier.padding(14.dp)) {
                                                        Button(
                                                            onClick = { toggleReadIndonesianExplanation(item) },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = if (isReadingThisDesc) Color(0xFFE11D48) else GoldAccent
                                                            ),
                                                            shape = RoundedCornerShape(50),
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isReadingThisDesc) Icons.Default.Stop else Icons.Default.RecordVoiceOver,
                                                                contentDescription = null,
                                                                tint = if (isReadingThisDesc) Color.White else Color(0xFF0F172A),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = if (isReadingThisDesc) "⏹️ Hentikan Suara Penjelasan"
                                                                else "🔊 Putar Suara Penjelasan & Nasihat Ini",
                                                                color = if (isReadingThisDesc) Color.White else Color(0xFF0F172A),
                                                                fontWeight = FontWeight.Black,
                                                                fontSize = 12.sp
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(12.dp))

                                                        Text(
                                                            text = item.description.ifBlank { "Tidak ada penjelasan tambahan." },
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF1E293B),
                                                            lineHeight = 22.sp
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
        }
    }

    if (zoomedContent != null) {
        IqroZoomDialog(
            item = zoomedContent!!,
            onPlaySound = { playAuthenticIslamicAudio(zoomedContent!!.arabicText, zoomedContent!!.latinText, zoomedContent!!.mediaUrl) },
            onBookmark = { saveIqroBookmark(zoomedContent!!) },
            onDismiss = { zoomedContent = null }
        )
    }
}

fun normalizeArabicForAuthenticRecitation(rawArabic: String): String {
    val trimmed = rawArabic.trim()
    if (trimmed.length == 2 && (trimmed[1] == '\u064E' || trimmed[1] == '\u0650' || trimmed[1] == '\u064F')) {
        return "${trimmed}هْ"
    }
    return trimmed.replace(Regex("\\s{2,}"), " ، ")
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppUniversalVideoPlayerBox(
    item: ServerContent,
    onSwitchToLockScreenAudio: () -> Unit,
    onClosePlayer: () -> Unit
) {
    val cleanUrl = remember(item.mediaUrl) {
        item.mediaUrl.split("|").lastOrNull { it.isNotBlank() } ?: ""
    }
    val ytId = remember(cleanUrl) { extractYoutubeVideoId(cleanUrl) }

    val embedHtml = remember(ytId, cleanUrl) {
        if (ytId != null) {
            """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>body,html{margin:0;padding:0;background:#000;height:100%;overflow:hidden;} iframe{width:100%;height:100%;border:0;}</style>
            </head>
            <body>
                <iframe src="https://www.youtube.com/embed/$ytId?autoplay=1&playsinline=1&rel=0" allow="autoplay; encrypted-media; fullscreen" allowfullscreen></iframe>
            </body>
            </html>
            """.trimIndent()
        } else {
            """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>body,html{margin:0;padding:0;background:#000;height:100%;display:flex;align-items:center;justify-content:center;} video{width:100%;height:100%;}</style>
            </head>
            <body>
                <video src="$cleanUrl" controls autoplay playsinline></video>
            </body>
            </html>
            """.trimIndent()
        }
    }

    Card(
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldDark)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎬 Memutar: ${item.title}",
                    color = GoldAccent,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Color(0xFFE11D48),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { onClosePlayer() }
                ) {
                    Text(
                        text = "✕ Tutup Video",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.domStorageEnabled = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        webChromeClient = WebChromeClient()
                        webViewClient = WebViewClient()
                        keepScreenOn = true
                        loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ingin mematikan layar HP sambil tetap mendengar?",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = GoldAccent,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { onSwitchToLockScreenAudio() }
                ) {
                    Text(
                        text = "🔒 Aktifkan Mode Layar Mati",
                        color = Color(0xFF0F172A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}