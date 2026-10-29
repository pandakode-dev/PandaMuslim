package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.random.Random

data class ServerContent(
    val id: Int,
    val title: String,
    val arabicText: String,
    val latinText: String,
    val description: String,
    val mediaUrl: String,
    val jilidLevel: Int = 1
)

data class HijaiyahSoundCard(
    val baseLetter: String,
    val fathah: String,
    val kasrah: String,
    val dhammah: String,
    val tanwin: String,
    val latinName: String,
    val readFathah: String,
    val readKasrah: String,
    val readDhammah: String,
    val readTanwin: String,
    val cardColor: Color
)

data class IqroReadingToken(
    val arabic: String,
    val latin: String
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

    // 3 Tab Utama di Menu Iqro:
    // 0 = 📗 Kartu Iqro Lengkap (36+ Kartu Jilid 1–6 + Penanda Mengaji!)
    // 1 = 📖 Latihan Membaca Iqro 1–6 (Generator Banyak Baris Huruf & Kata)
    // 2 = 🎵 Papan 30 Huruf Hijaiyah
    var iqroTabMode by remember { mutableStateOf(0) }
    var selectedJilid by remember { mutableStateOf(mengajiPrefs.getInt("last_iqro_jilid", 1)) }
    var bookmarkedIqroId by remember { mutableStateOf(mengajiPrefs.getInt("last_iqro_id", 901)) }
    var bookmarkedIqroLabel by remember {
        mutableStateOf(mengajiPrefs.getString("last_iqro_label", "Iqro 1 • Hal 1 (Alif - Ba)") ?: "Iqro 1 • Hal 1")
    }
    var selectedHarakatMode by remember { mutableStateOf(0) }

    var zoomedContent by remember { mutableStateOf<ServerContent?>(null) }
    var activeSpeakingTitle by remember { mutableStateOf<String?>(null) }
    val expandedFaqIds = remember { mutableStateMapOf<Int, Boolean>() }

    val mediaPlayer = remember { MediaPlayer() }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val arabResult = ttsInstance?.setLanguage(Locale("ar"))
                if (arabResult == TextToSpeech.LANG_MISSING_DATA || arabResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsInstance?.language = Locale("id", "ID")
                }
                ttsInstance?.setPitch(1.08f)
                ttsInstance?.setSpeechRate(0.80f)
            }
        }
        ttsEngine = ttsInstance

        onDispose {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
            } catch (_: Exception) {}
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    fun playIqroSound(arabicText: String, latinText: String, rawMediaUrl: String = "") {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        activeSpeakingTitle = "$arabicText  ➔  Dibaca: $latinText"

        val parts = rawMediaUrl.split("|")
        val audioUrl = parts.find { it.matches(Regex(".*\\.(mp3|wav|ogg|m4a)$", RegexOption.IGNORE_CASE)) } ?: ""

        if (audioUrl.isNotBlank()) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mediaPlayer.setDataSource(audioUrl)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener { it.start() }
                return
            } catch (_: Exception) {}
        }

        ttsEngine?.let { tts ->
            val arabLocale = Locale("ar")
            val isArabSupported = tts.isLanguageAvailable(arabLocale) >= TextToSpeech.LANG_AVAILABLE
            if (isArabSupported && arabicText.isNotBlank()) {
                tts.language = arabLocale
                tts.speak(arabicText, TextToSpeech.QUEUE_FLUSH, null, "iqro_arab")
            } else {
                tts.language = Locale("id", "ID")
                val spokenText = latinText.replace("—", " ").replace("-", " ").ifBlank { arabicText }
                tts.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, "iqro_id")
            }
        }
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
                        if (categoryKey == "iqro") {
                            Text(
                                text = "📌 Terakhir: $bookmarkedIqroLabel",
                                color = GoldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
            if (categoryKey == "iqro") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        0 to "📗 36+ Kartu Iqro",
                        1 to "📖 Latihan Baca",
                        2 to "🎵 30 Hijaiyah"
                    )
                    tabs.forEach { (index, title) ->
                        val selected = iqroTabMode == index
                        Surface(
                            color = if (selected) GoldAccent else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f).clickable { iqroTabMode = index }
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selected) Color(0xFF0F172A) else Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 9.dp)
                            )
                        }
                    }
                }

                if (iqroTabMode == 0 || iqroTabMode == 1) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().background(MintSurface)
                    ) {
                        items((1..6).toList()) { jilid ->
                            val isSelected = selectedJilid == jilid
                            Surface(
                                color = if (isSelected) EmeraldDark else Color.White,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) GoldAccent else EmeraldPrimary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedJilid = jilid }
                            ) {
                                Text(
                                    text = "📗 Iqro Jilid $jilid",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) GoldAccent else EmeraldDark,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                if (iqroTabMode == 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MintSurface)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val harakatTabs = listOf(
                            0 to "Fathah (A-Ba)",
                            1 to "Kasrah (I-Bi)",
                            2 to "Dhammah (U-Bu)",
                            3 to "Tanwin (An-Ban)"
                        )
                        harakatTabs.forEach { (idx, label) ->
                            val active = selectedHarakatMode == idx
                            Surface(
                                color = if (active) EmeraldDark else Color.White,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).clickable { selectedHarakatMode = idx }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (active) GoldAccent else EmeraldDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = activeSpeakingTitle != null) {
                    Surface(color = Color(0xFFFEF3C7), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeSpeakingTitle ?: "",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = EmeraldDark
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.align(Alignment.Center))
                } else if (categoryKey == "iqro" && iqroTabMode == 0) {
                    // TAB 0: 36+ KARTU IQRO JILID 1-6 + TOMBOL PENANDA MENGAJI
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
                                onPlaySound = { playIqroSound(item.arabicText, item.latinText, item.mediaUrl) },
                                onBookmarkClick = { saveIqroBookmark(item) },
                                onZoomClick = {
                                    playIqroSound(item.arabicText, item.latinText, item.mediaUrl)
                                    zoomedContent = item
                                }
                            )
                        }
                    }
                } else if (categoryKey == "iqro" && iqroTabMode == 1) {
                    // TAB 1: GENERATOR LATIHAN MEMBACA IQRO 1-6
                    IqroReadingSheetGeneratorView(
                        selectedJilid = selectedJilid,
                        onNextJilid = {
                            selectedJilid = if (selectedJilid < 6) selectedJilid + 1 else 1
                        },
                        onPlaySound = { arab, latin -> playIqroSound(arab, latin) }
                    )
                } else if (categoryKey == "iqro" && iqroTabMode == 2) {
                    // TAB 2: PAPAN 30 HURUF HIJAIYAH
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
                                onTap = { playIqroSound(dispArab, dispRead) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(items, key = { it.id }) { item ->
                            val isFaq = categoryKey == "faq"
                            val isExpanded = expandedFaqIds[item.id] ?: !isFaq
                            val imagePart = extractImageUrl(item.mediaUrl)

                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = isFaq) { expandedFaqIds[item.id] = !isExpanded }
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = EmeraldDark,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isFaq) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = EmeraldPrimary
                                            )
                                        } else if (item.arabicText.isNotBlank()) {
                                            FilledTonalIconButton(
                                                onClick = { playIqroSound(item.arabicText, item.latinText, item.mediaUrl) }
                                            ) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = "Suara", tint = EmeraldDark)
                                            }
                                        }
                                    }

                                    AnimatedVisibility(visible = isExpanded) {
                                        Column {
                                            if (imagePart.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                NetworkImageLoader(
                                                    imageUrl = imagePart,
                                                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))
                                                )
                                            }
                                            if (item.arabicText.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    text = item.arabicText,
                                                    fontSize = 28.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldDark,
                                                    textAlign = TextAlign.End,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                            if (item.latinText.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(item.latinText, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(item.description, fontSize = 14.sp, color = Color(0xFF334155), lineHeight = 21.sp)
                                            if (item.mediaUrl.isNotBlank() && imagePart.isBlank()) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Button(
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.mediaUrl))
                                                        context.startActivity(intent)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Putar Video / Buka Materi")
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
        val item = zoomedContent!!
        val imagePart = extractImageUrl(item.mediaUrl)
        AlertDialog(
            onDismissRequest = { zoomedContent = null },
            title = { Text(item.title, fontWeight = FontWeight.ExtraBold, color = EmeraldDark, fontSize = 16.sp) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (imagePart.isNotBlank()) {
                        NetworkImageLoader(
                            imageUrl = imagePart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { playIqroSound(item.arabicText, item.latinText, item.mediaUrl) }
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFFEF3C7))
                                .border(2.dp, GoldAccent, RoundedCornerShape(18.dp))
                                .clickable { playIqroSound(item.arabicText, item.latinText, item.mediaUrl) }
                        ) {
                            Text(
                                text = item.arabicText.ifBlank { "أَ بَ تَ" },
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { playIqroSound(item.arabicText, item.latinText, item.mediaUrl) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Suara", color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { saveIqroBookmark(item) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tandai Mengaji", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }

                    Text("Cara Baca: ${item.latinText}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = EmeraldPrimary)
                    Text(item.description, fontSize = 13.sp, textAlign = TextAlign.Center, color = Color.DarkGray)
                }
            },
            confirmButton = {
                TextButton(onClick = { zoomedContent = null }) { Text("Tutup 🌟", fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
fun IqroReadingSheetGeneratorView(
    selectedJilid: Int,
    onNextJilid: () -> Unit,
    onPlaySound: (String, String) -> Unit
) {
    var seed by remember { mutableStateOf(1) }
    var showLatinHelper by remember { mutableStateOf(true) }

    val readingRows = remember(selectedJilid, seed) {
        generateIqroReadingRows(selectedJilid, rowCount = 12)
    }

    val jilidInstruction = remember(selectedJilid) {
        when (selectedJilid) {
            1 -> "Jilid 1: Bacalah huruf Fathah dari KANAN ke KIRI dengan pendek & jelas. Ketuk huruf jika lupa bunyinya!"
            2 -> "Jilid 2: Latihan huruf sambung & bacaan panjang (Mad Alif 2 ketukan)."
            3 -> "Jilid 3: Latihan baris Kasrah (i), Dhammah (u), serta bacaan panjang Ya & Wawu sukun."
            4 -> "Jilid 4: Latihan Tanwin (an-in-un), Sukun, dan pantulan huruf Qalqalah."
            5 -> "Jilid 5: Latihan Alif Lam Syamsiyah, Tasydid, dan dengung Ghunnah (2 harakat)."
            else -> "Jilid 6: Latihan hukum Tajwid (Idgham, Iqlab, Ikhfa) & potongan kalimat Al-Quran."
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📖 Lembar Latihan Baca Iqro $selectedJilid", color = GoldAccent, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Surface(
                            color = Color.White.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { showLatinHelper = !showLatinHelper }
                        ) {
                            Text(
                                text = if (showLatinHelper) "🙈 Tutup Latin" else "👁️ Lihat Latin",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Text(jilidInstruction, color = Color.White, fontSize = 12.sp, lineHeight = 17.sp)

                    Button(
                        onClick = { seed++ },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Acak & Buat Halaman Huruf Baru 🔄", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }

        items(readingRows.size) { rowIndex ->
            val rowTokens = readingRows[rowIndex]
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Baris ${rowIndex + 1} • Baca dari Kanan ke Kiri ⬅️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        Text("🔊 Ketuk kotak untuk suara", fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTokens.forEach { token ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFFFBEB))
                                    .border(1.5.dp, GoldAccent.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                    .clickable { onPlaySound(token.arabic, token.latin) }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = token.arabic,
                                        fontSize = if (selectedJilid <= 2) 30.sp else 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldDark,
                                        textAlign = TextAlign.Center
                                    )
                                    if (showLatinHelper) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(token.latin, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                OutlinedButton(
                    onClick = { seed++ },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔄 Acak Lagi", fontWeight = FontWeight.ExtraBold, color = EmeraldDark)
                }
                Button(
                    onClick = onNextJilid,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (selectedJilid < 6) "Lanjut Iqro Jilid ${selectedJilid + 1} ➔" else "Kembali ke Jilid 1 ➔",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

fun generateIqroReadingRows(jilid: Int, rowCount: Int): List<List<IqroReadingToken>> {
    val pool: List<IqroReadingToken> = when (jilid) {
        1 -> listOf(
            IqroReadingToken("أَ", "A"), IqroReadingToken("بَ", "Ba"), IqroReadingToken("تَ", "Ta"),
            IqroReadingToken("ثَ", "Tsa"), IqroReadingToken("جَ", "Ja"), IqroReadingToken("حَ", "Ha"),
            IqroReadingToken("خَ", "Kho"), IqroReadingToken("دَ", "Da"), IqroReadingToken("ذَ", "Dza"),
            IqroReadingToken("رَ", "Ro"), IqroReadingToken("زَ", "Za"), IqroReadingToken("سَ", "Sa"),
            IqroReadingToken("شَ", "Sya"), IqroReadingToken("صَ", "Sho"), IqroReadingToken("ضَ", "Dho"),
            IqroReadingToken("طَ", "Tho"), IqroReadingToken("ظَ", "Zho"), IqroReadingToken("عَ", "'A"),
            IqroReadingToken("غَ", "Gho"), IqroReadingToken("فَ", "Fa"), IqroReadingToken("قَ", "Qo"),
            IqroReadingToken("كَ", "Ka"), IqroReadingToken("لَ", "La"), IqroReadingToken("مَ", "Ma"),
            IqroReadingToken("نَ", "Na"), IqroReadingToken("وَ", "Wa"), IqroReadingToken("هَ", "Ha"),
            IqroReadingToken("يَ", "Ya")
        )
        2 -> listOf(
            IqroReadingToken("بَتَ", "Ba-Ta"), IqroReadingToken("تَبَ", "Ta-Ba"), IqroReadingToken("ثَبَتَ", "Tsa-Ba-Ta"),
            IqroReadingToken("نَبَتَ", "Na-Ba-Ta"), IqroReadingToken("بَا", "Baa"), IqroReadingToken("تَا", "Taa"),
            IqroReadingToken("مَا", "Maa"), IqroReadingToken("لاَ", "Laa"), IqroReadingToken("كَتَبَ", "Ka-Ta-Ba"),
            IqroReadingToken("كَاتَبَ", "Kaa-Ta-Ba"), IqroReadingToken("جَلَسَ", "Ja-La-Sa"), IqroReadingToken("فَتَحَ", "Fa-Ta-Ha"),
            IqroReadingToken("صَدَقَ", "Sho-Da-Qo"), IqroReadingToken("قَالَ", "Qoo-La"), IqroReadingToken("كَانَ", "Kaa-Na"),
            IqroReadingToken("زَادَ", "Zaa-Da"), IqroReadingToken("نَصَرَ", "Na-Sho-Ro"), IqroReadingToken("عَبَدَ", "'A-Ba-Da")
        )
        3 -> listOf(
            IqroReadingToken("بِ", "Bi"), IqroReadingToken("تِ", "Ti"), IqroReadingToken("بِي", "Bii"),
            IqroReadingToken("تِي", "Tii"), IqroReadingToken("فِيهِ", "Fii-Hi"), IqroReadingToken("بُ", "Bu"),
            IqroReadingToken("تُ", "Tu"), IqroReadingToken("بُو", "Buu"), IqroReadingToken("تُو", "Tuu"),
            IqroReadingToken("كُتِبَ", "Ku-Ti-Ba"), IqroReadingToken("قُرِئَ", "Qu-Ri-A"), IqroReadingToken("سَمِعَ", "Sa-Mi-'A"),
            IqroReadingToken("عَلِمَ", "'A-Li-Ma"), IqroReadingToken("يَقُولُ", "Ya-Quu-Lu"), IqroReadingToken("يُوصِي", "Yuu-Shii"),
            IqroReadingToken("رُسُلُ", "Ru-Su-Lu"), IqroReadingToken("هُدِيَ", "Hu-Di-Ya"), IqroReadingToken("نُورِي", "Nuu-Rii")
        )
        4 -> listOf(
            IqroReadingToken("بً", "Ban"), IqroReadingToken("بٍ", "Bin"), IqroReadingToken("بٌ", "Bun"),
            IqroReadingToken("كِتَابًا", "Kitaaban"), IqroReadingToken("عَلِيمٌ", "'Aliimun"), IqroReadingToken("حَكِيمٍ", "Hakiimin"),
            IqroReadingToken("أَبْ", "Ab'"), IqroReadingToken("أَجْ", "Aj'"), IqroReadingToken("أَدْ", "Ad'"),
            IqroReadingToken("أَقْ", "Aq'"), IqroReadingToken("أَطْ", "Ath'"), IqroReadingToken("مَسْجِدٌ", "Masjidun"),
            IqroReadingToken("يَعْلَمُ", "Ya'lamu"), IqroReadingToken("أَنْعَمْتَ", "An'amta"), IqroReadingToken("مُسْلِمٌ", "Muslimun"),
            IqroReadingToken("أَحَدٌ", "Ahadun"), IqroReadingToken("صَمَدٌ", "Shomadun"), IqroReadingToken("كُفُوًا", "Kufuwan")
        )
        5 -> listOf(
            IqroReadingToken("إِنَّ", "In-na"), IqroReadingToken("عَمَّ", "'Am-ma"), IqroReadingToken("جَنَّةٌ", "Jan-natun"),
            IqroReadingToken("الْحَمْدُ", "Al-Hamdu"), IqroReadingToken("الْقَمَرُ", "Al-Qamaru"), IqroReadingToken("الشَّمْسُ", "Asy-Syamsu"),
            IqroReadingToken("الرَّحْمَنُ", "Ar-Rahmaanu"), IqroReadingToken("النَّاسِ", "An-Naasi"), IqroReadingToken("اللَّهُ", "Allaahu"),
            IqroReadingToken("رَبِّ", "Rab-bi"), IqroReadingToken("سُبِّحَ", "Sub-biha"), IqroReadingToken("قُدِّرَ", "Qud-dira"),
            IqroReadingToken("إِيَّاكَ", "Iy-yaaka"), IqroReadingToken("الصِّرَاطَ", "Ash-Shiraatho"), IqroReadingToken("الضَّالِّينَ", "Adh-Dhool-liina")
        )
        else -> listOf(
            IqroReadingToken("مَنْ يَقُولُ", "May-yaquulu"), IqroReadingToken("مِنْ مَالٍ", "Mim-maalin"),
            IqroReadingToken("مِنْ بَعْدِ", "Mim-ba'di"), IqroReadingToken("سَمِيعٌ بَصِيرٌ", "Samii'um-bashiir"),
            IqroReadingToken("مِنْ شَرِّ", "Min-syarri"), IqroReadingToken("أَنْفُسِهِمْ", "An-fusihim"),
            IqroReadingToken("مِنْ رَبِّهِمْ", "Mir-rabbihim"), IqroReadingToken("غَفُورٌ رَحِيمٌ", "Ghafuurur-rahiim"),
            IqroReadingToken("لَمْ يَلِدْ", "Lam yalid'"), IqroReadingToken("وَلَمْ يُولَدْ", "Wa lam yuulad'"),
            IqroReadingToken("فَسَبِّحْ", "Fasabbih"), IqroReadingToken("وَاسْتَغْفِرْهُ", "Wastaghfirhu")
        )
    }

    val columnsPerRow = if (jilid <= 4) 4 else 3
    return List(rowCount) {
        List(columnsPerRow) {
            pool[Random.nextInt(pool.size)]
        }
    }
}

@Composable
fun IqroVisualSoundCard(
    item: ServerContent,
    isBookmarked: Boolean,
    onPlaySound: () -> Unit,
    onBookmarkClick: () -> Unit,
    onZoomClick: () -> Unit
) {
    val imagePart = extractImageUrl(item.mediaUrl)
    val iqroPandaBitmap = rememberOptimizedIcon(R.drawable.ic_menu_iqro, 90)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBookmarked) Color(0xFFFEF3C7) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isBookmarked) 2.5.dp else 1.5.dp,
                color = if (isBookmarked) Color(0xFFD97706) else MintSurface,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onPlaySound)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (imagePart.isNotBlank()) {
                    NetworkImageLoader(
                        imageUrl = imagePart,
                        modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFEF9C3))
                            .border(2.dp, GoldAccent, RoundedCornerShape(16.dp))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                bitmap = iqroPandaBitmap,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.arabicText.ifBlank { "أَ بَ تَ" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Tombol Tandai Batas Mengaji di Pojok Kiri Atas Kartu
                Surface(
                    color = if (isBookmarked) Color(0xFFD97706) else Color.White.copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(30.dp)
                        .clickable { onBookmarkClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Tandai Mengaji",
                            tint = if (isBookmarked) Color.White else EmeraldDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Ikon Suara di Pojok Kanan Atas Kartu
                Surface(
                    color = GoldAccent,
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(item.title, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = EmeraldDark, textAlign = TextAlign.Center, maxLines = 1)
            Text(item.latinText, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldPrimary, textAlign = TextAlign.Center, maxLines = 1)
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = EmeraldPrimary,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).clickable { onPlaySound() }
                ) {
                    Text("🔊 Bunyi", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 5.dp))
                }
                Surface(
                    color = if (isBookmarked) Color(0xFFD97706) else MintSurface,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).clickable { onBookmarkClick() }
                ) {
                    Text(
                        text = if (isBookmarked) "📌 Ditandai" else "🔖 Tandai",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isBookmarked) Color.White else EmeraldDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HijaiyahInteractiveTile(
    arabic: String,
    soundRead: String,
    latinName: String,
    cardColor: Color,
    onTap: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.White, RoundedCornerShape(20.dp))
            .clickable(onClick = onTap)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = arabic, fontSize = 34.sp, fontWeight = FontWeight.Black, color = EmeraldDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = soundRead, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
            Text(text = "($latinName)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}

fun extractImageUrl(rawMediaUrl: String): String {
    val parts = rawMediaUrl.split("|")
    return parts.find { it.matches(Regex(".*\\.(jpg|jpeg|png|webp|gif)$", RegexOption.IGNORE_CASE)) } ?: ""
}

@Composable
fun NetworkImageLoader(imageUrl: String, modifier: Modifier = Modifier) {
    var bitmap by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }
    var isFailed by remember(imageUrl) { mutableStateOf(false) }

    LaunchedEffect(imageUrl) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 7000
                conn.readTimeout = 7000
                conn.doInput = true
                conn.connect()
                bitmap = BitmapFactory.decodeStream(conn.inputStream)
            } catch (_: Exception) {
                isFailed = true
            }
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier.background(MintSurface)) {
        when {
            bitmap != null -> {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            isFailed -> {
                Text("📗", fontSize = 32.sp)
            }
            else -> {
                CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(26.dp))
            }
        }
    }
}

fun get30HijaiyahSoundCards(): List<HijaiyahSoundCard> {
    val colors = listOf(
        Color(0xFFFEF9C3), Color(0xFFD1FAE5), Color(0xFFE0F2FE),
        Color(0xFFFCE7F3), Color(0xFFEDE9FE), Color(0xFFFFEDD5)
    )
    data class RawH(val base: String, val name: String, val f: String, val k: String, val d: String, val t: String)
    val list = listOf(
        RawH("ا", "Alif", "A", "I", "U", "An"),
        RawH("ب", "Ba", "Ba", "Bi", "Bu", "Ban"),
        RawH("ت", "Ta", "Ta", "Ti", "Tu", "Tan"),
        RawH("ث", "Tsa", "Tsa", "Tsi", "Tsu", "Tsan"),
        RawH("ج", "Jim", "Ja", "Ji", "Ju", "Jan"),
        RawH("ح", "Ha", "Ha", "Hi", "Hu", "Han"),
        RawH("خ", "Kho", "Kho", "Khi", "Khu", "Khon"),
        RawH("د", "Dal", "Da", "Di", "Du", "Dan"),
        RawH("ذ", "Dzal", "Dza", "Dzi", "Dzu", "Dzan"),
        RawH("ر", "Ro", "Ro", "Ri", "Ru", "Ron"),
        RawH("ز", "Zay", "Za", "Zi", "Zu", "Zan"),
        RawH("س", "Sin", "Sa", "Si", "Su", "San"),
        RawH("ش", "Syin", "Sya", "Syi", "Syu", "Syan"),
        RawH("ص", "Shod", "Sho", "Shi", "Shu", "Shon"),
        RawH("ض", "Dhod", "Dho", "Dhi", "Dhu", "Dhon"),
        RawH("ط", "Tho", "Tho", "Thi", "Thu", "Thon"),
        RawH("ظ", "Zho", "Zho", "Zhi", "Zhu", "Zhon"),
        RawH("ع", "Ain", "'A", "'I", "'U", "'An"),
        RawH("غ", "Ghain", "Gho", "Ghi", "Ghu", "Ghon"),
        RawH("ف", "Fa", "Fa", "Fi", "Fu", "Fan"),
        RawH("ق", "Qof", "Qo", "Qi", "Qu", "Qon"),
        RawH("ك", "Kaf", "Ka", "Ki", "Ku", "Kan"),
        RawH("ل", "Lam", "La", "Li", "Lu", "Lan"),
        RawH("م", "Mim", "Ma", "Mi", "Mu", "Man"),
        RawH("ن", "Nun", "Na", "Ni", "Nu", "Nan"),
        RawH("و", "Wawu", "Wa", "Wi", "Wu", "Wan"),
        RawH("هـ", "Ha Besar", "Ha", "Hi", "Hu", "Han"),
        RawH("لا", "Lam Alif", "Laa", "Lii", "Luu", "Lan"),
        RawH("ء", "Hamzah", "A", "I", "U", "An"),
        RawH("ي", "Ya", "Ya", "Yi", "Yu", "Yan")
    )
    return list.mapIndexed { idx, r ->
        HijaiyahSoundCard(
            baseLetter = r.base,
            fathah = "${r.base}\u064E",
            kasrah = "${r.base}\u0650",
            dhammah = "${r.base}\u064F",
            tanwin = "${r.base}\u064B",
            latinName = r.name,
            readFathah = r.f,
            readKasrah = r.k,
            readDhammah = r.d,
            readTanwin = r.t,
            cardColor = colors[idx % colors.size]
        )
    }
}

suspend fun fetchContentsFromServer(category: String): List<ServerContent> = withContext(Dispatchers.IO) {
    val resultList = mutableListOf<ServerContent>()
    try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=get_contents&category=$category")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().readText()
            val jsonObj = JSONObject(response)
            val dataArray = jsonObj.getJSONArray("data")
            for (i in 0 until dataArray.length()) {
                val obj = dataArray.getJSONObject(i)
                resultList.add(
                    ServerContent(
                        id = obj.getInt("id"),
                        title = obj.getString("title"),
                        arabicText = obj.optString("arabic_text", ""),
                        latinText = obj.optString("latin_text", ""),
                        description = obj.optString("description", ""),
                        mediaUrl = obj.optString("media_url", ""),
                        jilidLevel = 1
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    resultList
}

// 36+ Kartu Iqro Lengkap dari Jilid 1 sampai Jilid 6!
fun getBuiltInFallbackContents(category: String): List<ServerContent> {
    return when (category) {
        "iqro" -> listOf(
            // JILID 1 (8 Kartu Lengkap Seluruh Huruf Tunggal Fathah)
            ServerContent(901, "Iqro 1 • Hal 1 (Alif - Ba)", "أَ   بَ", "A  —  Ba", "Mulut terbuka saat 'A' dan bibir merapat saat 'Ba'.", "", 1),
            ServerContent(902, "Iqro 1 • Hal 2 (Ba - Ta - Tsa)", "بَ   تَ   ثَ", "Ba — Ta — Tsa", "Titik satu di bawah Ba, titik dua di atas Ta, titik tiga di atas Tsa.", "", 1),
            ServerContent(903, "Iqro 1 • Hal 3 (Jim - Ha - Kho)", "جَ   حَ   خَ", "Ja — Ha — Kho", "Jim bertitik di tengah, Ha bersih di tenggorokan, Kho sedikit bergetar.", "", 1),
            ServerContent(904, "Iqro 1 • Hal 4 (Dal - Dzal)", "دَ   ذَ   جَدَ", "Da — Dza — Ja-Da", "Sentuhkan ujung lidah ke gigi seri atas untuk huruf Dza.", "", 1),
            ServerContent(905, "Iqro 1 • Hal 5 (Ro - Za)", "رَ   زَ   رَزَ", "Ro — Za — Ro-Za", "Huruf Ro bergetar tebal, sedangkan Za berdesis halus.", "", 1),
            ServerContent(906, "Iqro 1 • Hal 6 (Sin - Syin)", "سَ   شَ   سَحَ", "Sa — Sya — Sa-Ha", "Bedakan desis tipis Sin (Sa) dan hembusan lebar Syin (Sya).", "", 1),
            ServerContent(907, "Iqro 1 • Hal 7 (Shod - Dhod - Tho - Zho)", "صَ   ضَ   طَ   ظَ", "Sho — Dho — Tho — Zho", "Keempat huruf ini dibaca tebal (Isti'la) memenuhi rongga mulut.", "", 1),
            ServerContent(908, "Iqro 1 • Hal 8 (Ain s/d Ya)", "عَ  غَ  فَ  قَ  كَ  لَ  مَ  نَ  وَ  هَ  يَ", "'A - Gho - Fa - Qo - Ka - La - Ma - Na - Wa - Ha - Ya", "Penutup Iqro Jilid 1! Pastikan semua huruf Fathah terbaca tegas.", "", 1),

            // JILID 2 (6 Kartu Lengkap Huruf Sambung & Mad Fathah Panjang)
            ServerContent(921, "Iqro 2 • Hal 1 (Sambung Ba-Ta)", "بَتَ   تَبَ", "Ba-Ta  —  Ta-Ba", "Mengenal bentuk huruf Ba dan Ta ketika disambung.", "", 2),
            ServerContent(922, "Iqro 2 • Hal 2 (Sambung 3 Huruf)", "نَبَتَ   ثَبَتَ", "Na-Ba-Ta — Tsa-Ba-Ta", "Bacalah pendek setiap huruf yang disambung.", "", 2),
            ServerContent(923, "Iqro 2 • Hal 3 (Sambung Jim-Ha-Kho)", "جَحَدَ   خَرَجَ", "Ja-Ha-Da — Kho-Ro-Ja", "Kepala huruf Jim, Ha, Kho tetap terlihat jelas saat disambung.", "", 2),
            ServerContent(924, "Iqro 2 • Hal 4 (Sambung Ain-Ghain-Fa-Qof)", "عَمَلَ   فَتَحَ", "'A-Ma-La — Fa-Ta-Ha", "Perhatikan bentuk huruf Ain dan Fa ketika berada di awal kata.", "", 2),
            ServerContent(925, "Iqro 2 • Hal 5 (Mad Panjang Alif)", "بَا   تَا   مَا   لاَ", "Baa — Taa — Maa — Laa", "Setiap Fathah diikuti Alif mati wajib dipanjangkan 2 harakat.", "", 2),
            ServerContent(926, "Iqro 2 • Hal 6 (Ayat Pendek vs Panjang)", "كَتَبَ   كَاتَبَ   صَدَقَ   صَادَقَ", "Ka-Ta-Ba  vs  Kaa-Ta-Ba", "Ujian Iqro 2: Jangan memanjangkan huruf yang tidak memiliki Alif!", "", 2),

            // JILID 3 (6 Kartu Lengkap Kasrah, Dhammah & Mad Ya/Wawu)
            ServerContent(931, "Iqro 3 • Hal 1 (Baris Kasrah i-bi-ti)", "إِ   بِ   تِ   ثِ", "I — Bi — Ti — Tsi", "Garis di bawah huruf menghasilkan bunyi vokal 'i' pendek.", "", 3),
            ServerContent(932, "Iqro 3 • Hal 2 (Latihan Kata Kasrah)", "عَمِلَ   سَمِعَ   حَمِدَ", "'A-Mi-La — Sa-Mi-'A — Ha-Mi-Da", "Jaga agar huruf Kasrah tidak memantul atau miring ke bunyi 'e'.", "", 3),
            ServerContent(933, "Iqro 3 • Hal 3 (Mad Ya Sukun Panjang)", "بِي   تِي   فِيهِ   دِينِي", "Bii — Tii — Fii-Hi — Dii-Nii", "Kasrah diikuti Ya sukun dibaca panjang 2 harakat.", "", 3),
            ServerContent(934, "Iqro 3 • Hal 4 (Baris Dhammah u-bu-tu)", "أُ   بُ   تُ   ثُ", "U — Bu — Tu — Tsu", "Bibirmu maju membulat saat mengucapkan baris Dhammah (u).", "", 3),
            ServerContent(935, "Iqro 3 • Hal 5 (Kata Gabungan 3 Harakat)", "كُتِبَ   قُرِئَ   فُتِحَ", "Ku-Ti-Ba — Qu-Ri-A — Fu-Ti-Ha", "Gabungan Dhammah, Kasrah, dan Fathah dalam satu kata.", "", 3),
            ServerContent(936, "Iqro 3 • Hal 6 (Mad Wawu Sukun Panjang)", "بُو   تُو   يَقُولُ   رَسُولُ", "Buu — Tuu — Ya-Quu-Lu — Ro-Suu-Lu", "Dhammah diikuti Wawu sukun dibaca panjang 2 harakat.", "", 3),

            // JILID 4 (6 Kartu Lengkap Tanwin, Sukun & Qalqalah)
            ServerContent(941, "Iqro 4 • Hal 1 (Fathatain -an)", "بً   تً   كِتَابًا   هُدًى", "Ban — Tan — Ki-Taa-Ban — Hu-Dan", "Baris dua di atas berbunyi '-an'.", "", 4),
            ServerContent(942, "Iqro 4 • Hal 2 (Kasratain -in)", "بٍ   تٍ   عَلِيمٍ   حَكِيمٍ", "Bin — Tin — 'A-Lii-Min — Ha-Kii-Min", "Baris dua di bawah berbunyi '-in'.", "", 4),
            ServerContent(943, "Iqro 4 • Hal 3 (Dhammatain -un)", "بٌ   تٌ   أَحَدٌ   صَمَدٌ", "Bun — Tun — A-Ha-Dun — Sho-Ma-Dun", "Dhammah rangkap di atas berbunyi '-un'.", "", 4),
            ServerContent(944, "Iqro 4 • Hal 4 (Huruf Sukun / Mati)", "أَمْ   أَنْ   يَعْلَمُونَ   مَسْجِدٌ", "Am — An — Ya'lamuuna — Masjidun", "Huruf bertanda Sukun dimatikan tanpa memantul (kecuali Qalqalah).", "", 4),
            ServerContent(945, "Iqro 4 • Hal 5 (Pantulan Qalqalah)", "أَبْ   أَجْ   أَدْ   أَقْ   أَطْ", "Ab' — Aj' — Ad' — Aq' — Ath'", "5 Huruf Qalqalah (ق ط ب ج د) wajib dipantulkan saat sukun.", "", 4),
            ServerContent(946, "Iqro 4 • Hal 6 (Ujian Evaluasi Jilid 4)", "أَنْعَمْتَ   الْمَغْضُوبِ   يَكْسِبُونَ", "An-'Am-Ta — Al-Magh-Dhuu-Bi", "Pastikan Sukun dan Qalqalah terdengar beda dengan jelas!", "", 4),

            // JILID 5 (6 Kartu Lengkap Alif Lam, Tasydid, Ghunnah & Waqaf)
            ServerContent(951, "Iqro 5 • Hal 1 (Alif Lam Qamariyah)", "الْقَمَرُ   الْحَمْدُ   الْعَالَمِينَ", "Al-Qamaru — Al-Hamdu — Al-'Aalamiina", "Huruf Lam sukun dibaca jelas.", "", 5),
            ServerContent(952, "Iqro 5 • Hal 2 (Alif Lam Syamsiyah)", "الشَّمْسُ   الرَّحْمَنُ   النَّاسِ", "Asy-Syamsu — Ar-Rahmaanu — An-Naasi", "Huruf Lam lebur langsung ke huruf bertasydid di depannya.", "", 5),
            ServerContent(953, "Iqro 5 • Hal 3 (Huruf Bertasydid)", "رَبِّ   قُدِّرَ   سُبِّحَ   إِيَّاكَ", "Rab-bi — Qud-dira — Sub-biha — Iy-yaaka", "Tahan dan tekan kuat huruf yang memiliki tanda Tasydid (ّ).", "", 5),
            ServerContent(954, "Iqro 5 • Hal 4 (Ghunnah Nun & Mim Tasydid)", "إِنَّ   عَمَّ   جَنَّةٌ   ثُمَّ", "In-na — 'Am-ma — Jan-natun — Tsum-ma", "Wajib ditahan dengungnya selama 2 harakat penuh!", "", 5),
            ServerContent(955, "Iqro 5 • Hal 5 (Lafadz Allah / Jalalah)", "بِسْمِ اللَّهِ   مِنَ اللَّهِ   رَسُولُ اللَّهِ", "Bismillaahi (Tipis) vs Minallaahi (Tebal)", "Dibaca tipis (Lillaah) setelah Kasrah, dan tebal (Allaah) setelah Fathah/Dhammah.", "", 5),
            ServerContent(956, "Iqro 5 • Hal 6 (Cara Waqaf / Berhenti)", "تَعْلَمُونَ ۝   أَحَدٌ ۝   الْفَلَقِ ۝", "Ta'lamuun — Ahad' — Al-Falaq'", "Huruf terakhir dimatikan ketika berhenti di akhir ayat.", "", 5),

            // JILID 6 (6 Kartu Lengkap Idgham, Iqlab, Ikhfa & Tanda Waqaf)
            ServerContent(961, "Iqro 6 • Hal 1 (Idgham Bighunnah)", "مَنْ يَقُولُ   مِنْ مَالٍ   مِنْ نِعْمَةٍ", "May-yaquulu — Mim-maalin — Min-ni'matin", "Nun mati/Tanwin bertemu ي ن م و dileburkan disertai dengung.", "", 6),
            ServerContent(962, "Iqro 6 • Hal 2 (Idgham Bilaghunnah)", "مِنْ رَبِّهِمْ   غَفُورٌ رَحِيمٌ   مِنْ لَدُنْهُ", "Mir-rabbihim — Ghafuurur-rahiim", "Nun mati/Tanwin bertemu Lam (ل) atau Ro (ر) melebur jelas tanpa dengung.", "", 6),
            ServerContent(963, "Iqro 6 • Hal 3 (Hukum Iqlab)", "مِنْ بَعْدِ   سَمِيعٌ بَصِيرٌ   لَيُنْبَذَنَّ", "Mim-ba'di — Samii'um-bashiir", "Nun mati/Tanwin bertemu Ba (ب) berubah menjadi bunyi Mim dengung.", "", 6),
            ServerContent(964, "Iqro 6 • Hal 4 (Hukum Ikhfa Haqiqi)", "مِنْ شَرِّ   أَنْفُسِهِمْ   مَنْصُورًا", "Min-syarri — An-fusihim — Man-shuuran", "Nun mati/Tanwin bertemu 15 huruf Ikhfa dibaca samar disertai dengung.", "", 6),
            ServerContent(965, "Iqro 6 • Hal 5 (Huruf Muqaththa'ah Awal Surah)", "الم   يس   طه   حم", "Alif-Laam-Miim — Yaa-Siin — Thoo-Haa", "Dibaca nama asli hurufnya dengan panjang Mad Lazim (6 harakat).", "", 6),
            ServerContent(966, "Iqro 6 • Hal 6 (Kelulusan Iqro 6)", "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝", "Qul huwallaahu ahad' — Allaahush-shomad'", "Alhamdulillah! Anda telah menamatkan seluruh kurikulum Iqro 1–6.", "", 6)
        )
        "belajar" -> listOf(
            ServerContent(101, "5 Rukun Islam & Penjelasannya", "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ", "Buniyal islaamu 'alaa khamsin", "1. Mengucapkan Dua Kalimat Syahadat\n2. Mendirikan Sholat 5 Waktu\n3. Menunaikan Zakat\n4. Berpuasa di Bulan Ramadhan\n5. Menunaikan Ibadah Haji bagi yang mampu.", ""),
            ServerContent(102, "6 Rukun Iman yang Wajib Diyakini", "أَنْ تُؤْمِنَ بِاللَّهِ وَمَلَائِكَتِهِ", "An tu'mina billaahi wa malaa'ikatihi...", "1. Iman kepada Allah SWT\n2. Iman kepada Malaikat Allah\n3. Iman kepada Kitab-Kitab Allah\n4. Iman kepada Rasul-Rasul Allah\n5. Iman kepada Hari Kiamat\n6. Iman kepada Qada dan Qadar.", "")
        )
        "faq" -> listOf(
            ServerContent(201, "❓ Bagaimana cara menandai batas mengaji di Al-Quran & Iqro?", "", "", "Ketuk ikon Pita Penanda (🔖) pada kartu ayat Al-Quran atau kartu Iqro. Batas mengaji Anda langsung tampil di Beranda Utama dan tersimpan ke server!", ""),
            ServerContent(202, "❓ Bagaimana cara membuka Panel Admin dari Aplikasi Android?", "", "", "Klik tombol '🔐 Login Google / Email' di kiri atas Beranda, lalu login menggunakan email admin@pandamuslim.pandakode-dev.my.id dan password humairoh26. Tombol '👑 Panel Admin' akan langsung muncul!", "")
        )
        "tajwid" -> listOf(
            ServerContent(301, "1. Izhar Halqi (Jelas)", "مَنْ آمَنَ", "Man aamana", "Apabila Nun Sukun atau Tanwin bertemu salah satu huruf tenggorokan (ء هـ ع ح غ خ), maka dibaca jelas tanpa dengung.", "")
        )
        else -> listOf(
            ServerContent(501, "Doa Berbuka Puasa", "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ", "Dzahabadz zhama'u wabtallatil 'uruqu wa tsabatal ajru insya Allah", "Telah hilang rasa haus, telah basah urat-urat, dan telah pasti pahala, insya Allah.", "")
        )
    }
}