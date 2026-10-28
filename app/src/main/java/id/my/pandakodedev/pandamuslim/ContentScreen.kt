package id.my.pandakodedev.pandamuslim

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
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

data class ServerContent(
    val id: Int,
    val title: String,
    val arabicText: String,
    val latinText: String,
    val description: String,
    val mediaUrl: String
)

data class HijaiyahSoundCard(
    val arabic: String,
    val fathah: String,
    val latinName: String,
    val soundRead: String,
    val cardColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicContentScreen(
    menuTitle: String,
    categoryKey: String, // "doa", "tajwid", "video", "belajar", "iqro", "faq"
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var items by remember { mutableStateOf<List<ServerContent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 3 Mode Tampilan Khusus Menu Iqro:
    // 0 = Kartu Gambar Iqro Bersuara, 1 = Papan 28 Huruf Hijaiyah Bersuara, 2 = Daftar Bacaan
    var iqroTabMode by remember { mutableStateOf(0) }
    var zoomedContent by remember { mutableStateOf<ServerContent?>(null) }
    var activeSpeakingTitle by remember { mutableStateOf<String?>(null) }

    val expandedFaqIds = remember { mutableStateMapOf<Int, Boolean>() }

    // Inisialisasi Mesin Suara Pintar (MediaPlayer untuk MP3 Web Admin + TextToSpeech Arab/Indonesia)
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
                ttsInstance?.setPitch(1.1f) // Nada sedikit ceria ramah anak
                ttsInstance?.setSpeechRate(0.82f) // Tempo lambat & jelas untuk mengeja makhraj Iqro
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

    // Fungsi Pintar Memutar Suara Huruf Iqro / Doa
    fun playIqroSound(arabicText: String, latinText: String, rawMediaUrl: String = "") {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        activeSpeakingTitle = latinText.ifBlank { arabicText }

        // Memisahkan jika media_url berisi gabungan "url_gambar|url_audio_mp3"
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

        // Fallback Otomatis: Suarakan huruf Arab & cara baca Latin menggunakan mesin TTS HP
        ttsEngine?.let { tts ->
            val arabLocale = Locale("ar")
            val isArabSupported = tts.isLanguageAvailable(arabLocale) >= TextToSpeech.LANG_AVAILABLE
            if (isArabSupported && arabicText.isNotBlank()) {
                tts.language = arabLocale
                tts.speak(arabicText, TextToSpeech.QUEUE_FLUSH, null, "iqro_arab_utterance")
            } else {
                tts.language = Locale("id", "ID")
                val spokenText = latinText.replace("—", " ").replace("-", " ").ifBlank { arabicText }
                tts.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, "iqro_id_utterance")
            }
        }
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
                                text = "🔊 Ketuk kartu untuk mendengar suara pengucapan huruf!",
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
            // Bar Pemilih 3 Mode Interaktif khusus Menu Iqro
            if (categoryKey == "iqro") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldDark)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        0 to "🖼️ Kartu Iqro",
                        1 to "🎵 28 Hijaiyah",
                        2 to "📋 Daftar Bacaan"
                    )
                    tabs.forEach { (index, title) ->
                        val selected = iqroTabMode == index
                        Surface(
                            color = if (selected) GoldAccent else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { iqroTabMode = index }
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selected) EmeraldDark else Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }

                // Banner Indikator Suara yang Sedang Berbunyi
                AnimatedVisibility(visible = activeSpeakingTitle != null) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Melafalkan: $activeSpeakingTitle",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = EmeraldDark
                                )
                            }
                            Text(
                                text = "Ketuk ulang untuk dengar lagi 🔊",
                                fontSize = 10.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = EmeraldPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (categoryKey == "iqro" && iqroTabMode == 0) {
                    // MODE 0: KARTU GAMBAR IQRO BERSUARA (Ketuk langsung bunyi!)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items, key = { it.id }) { item ->
                            IqroVisualSoundCard(
                                item = item,
                                onPlaySound = {
                                    playIqroSound(item.arabicText, item.latinText, item.mediaUrl)
                                },
                                onZoomClick = {
                                    playIqroSound(item.arabicText, item.latinText, item.mediaUrl)
                                    zoomedContent = item
                                }
                            )
                        }
                    }
                } else if (categoryKey == "iqro" && iqroTabMode == 1) {
                    // MODE 1: PAPAN PIANO 28 HURUF HIJAIYAH BERSUARA UNTUK ANAK-ANAK
                    val hijaiyahList = remember { get28HijaiyahSoundCards() }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(hijaiyahList, key = { it.latinName }) { card ->
                            HijaiyahInteractiveTile(
                                card = card,
                                onTap = {
                                    playIqroSound(card.fathah, "${card.latinName}, dibaca ${card.soundRead}")
                                }
                            )
                        }
                    }
                } else {
                    // MODE DAFTAR REGULER (DOA, TAJWID, BELAJAR, VIDEO, FAQ, ATAU DAFTAR IQRO)
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
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = isFaq) {
                                        expandedFaqIds[item.id] = !isExpanded
                                    }
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
                                        } else if (item.arabicText.isNotBlank() || categoryKey == "iqro") {
                                            FilledTonalIconButton(
                                                onClick = { playIqroSound(item.arabicText, item.latinText, item.mediaUrl) }
                                            ) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = "Dengarkan Suara", tint = EmeraldDark)
                                            }
                                        }
                                    }

                                    AnimatedVisibility(visible = isExpanded) {
                                        Column {
                                            if (imagePart.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                NetworkImageLoader(
                                                    imageUrl = imagePart,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(210.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                )
                                            }

                                            if (item.arabicText.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    text = item.arabicText,
                                                    fontSize = 28.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF065F46),
                                                    textAlign = TextAlign.End,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                            if (item.latinText.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = item.latinText,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = EmeraldPrimary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = item.description,
                                                fontSize = 14.sp,
                                                color = Color(0xFF334155),
                                                lineHeight = 21.sp
                                            )
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

    // Pop-up Zoom Kartu Iqro Bergambar + Tombol Putar Ulang Suara
    if (zoomedContent != null) {
        val item = zoomedContent!!
        val imagePart = extractImageUrl(item.mediaUrl)
        AlertDialog(
            onDismissRequest = { zoomedContent = null },
            title = {
                Text(item.title, fontWeight = FontWeight.ExtraBold, color = EmeraldDark, fontSize = 16.sp)
            },
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
                                .height(230.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { playIqroSound(item.arabicText, item.latinText, item.mediaUrl) }
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFFEF3C7))
                                .border(2.dp, GoldAccent, RoundedCornerShape(18.dp))
                                .clickable { playIqroSound(item.arabicText, item.latinText, item.mediaUrl) }
                        ) {
                            Text(
                                text = item.arabicText.ifBlank { "أَ بَ تَ" },
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldDark
                            )
                        }
                    }

                    Button(
                        onClick = { playIqroSound(item.arabicText, item.latinText, item.mediaUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = EmeraldDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🔊 Bunyikan Suara Huruf", color = EmeraldDark, fontWeight = FontWeight.ExtraBold)
                    }

                    if (item.latinText.isNotBlank()) {
                        Text(
                            text = "Cara Baca: ${item.latinText}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = EmeraldPrimary
                        )
                    }
                    Text(
                        text = item.description,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { zoomedContent = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Tutup 🌟")
                }
            }
        )
    }
}

@Composable
fun IqroVisualSoundCard(
    item: ServerContent,
    onPlaySound: () -> Unit,
    onZoomClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iqro_card_bounce"
    )
    val imagePart = extractImageUrl(item.mediaUrl)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(2.dp, MintSurface, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onPlaySound
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (imagePart.isNotBlank()) {
                    NetworkImageLoader(
                        imageUrl = imagePart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFEF9C3))
                            .border(2.dp, GoldAccent, RoundedCornerShape(18.dp))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_menu_iqro),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.arabicText.ifBlank { "أَ بَ تَ" },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Ikon Speaker Emas di Pojok Kanan Atas Kartu
                Surface(
                    color = GoldAccent,
                    shape = CircleShape,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Bunyikan",
                            tint = EmeraldDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = EmeraldDark,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (item.latinText.isNotBlank()) {
                Text(
                    text = item.latinText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = EmeraldPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = EmeraldPrimary,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPlaySound() }
                ) {
                    Text(
                        text = "🔊 Suara",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }
                Surface(
                    color = MintSurface,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onZoomClick() }
                ) {
                    Text(
                        text = "🔍 Detail",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HijaiyahInteractiveTile(card: HijaiyahSoundCard, onTap: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "hijaiyah_tile_scale"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = card.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(2.dp, Color.White, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onTap
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.fathah,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = EmeraldDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = card.soundRead,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldPrimary
            )
            Text(
                text = "(${card.latinName})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
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
                val input = conn.inputStream
                bitmap = BitmapFactory.decodeStream(input)
            } catch (_: Exception) {
                isFailed = true
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.background(MintSurface)
    ) {
        when {
            bitmap != null -> {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Gambar Materi",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            isFailed -> {
                Image(
                    painter = painterResource(id = R.drawable.ic_menu_iqro),
                    contentDescription = "Fallback Iqro",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp).clip(CircleShape)
                )
            }
            else -> {
                CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(28.dp))
            }
        }
    }
}

fun get28HijaiyahSoundCards(): List<HijaiyahSoundCard> {
    val colors = listOf(
        Color(0xFFFEF9C3), Color(0xFFD1FAE5), Color(0xFFE0F2FE),
        Color(0xFFFCE7F3), Color(0xFFEDE9FE), Color(0xFFFFEDD5)
    )
    val raw = listOf(
        Triple("ا", "أَ", "Alif" to "A"),
        Triple("ب", "بَ", "Ba" to "Ba"),
        Triple("ت", "تَ", "Ta" to "Ta"),
        Triple("ث", "ثَ", "Tsa" to "Tsa"),
        Triple("ج", "جَ", "Jim" to "Ja"),
        Triple("ح", "حَ", "Ha" to "Ha"),
        Triple("خ", "خَ", "Kho" to "Kho"),
        Triple("د", "دَ", "Dal" to "Da"),
        Triple("ذ", "ذَ", "Dzal" to "Dza"),
        Triple("ر", "رَ", "Ro" to "Ro"),
        Triple("ز", "زَ", "Zay" to "Za"),
        Triple("س", "سَ", "Sin" to "Sa"),
        Triple("ش", "شَ", "Syin" to "Sya"),
        Triple("ص", "صَ", "Shod" to "Sho"),
        Triple("ض", "ضَ", "Dhod" to "Dho"),
        Triple("ط", "طَ", "Tho" to "Tho"),
        Triple("ظ", "ظَ", "Zho" to "Zho"),
        Triple("ع", "عَ", "Ain" to "'A"),
        Triple("غ", "غَ", "Ghain" to "Gho"),
        Triple("ف", "فَ", "Fa" to "Fa"),
        Triple("ق", "قَ", "Qof" to "Qo"),
        Triple("ك", "كَ", "Kaf" to "Ka"),
        Triple("ل", "لَ", "Lam" to "La"),
        Triple("م", "مَ", "Mim" to "Ma"),
        Triple("ن", "نَ", "Nun" to "Na"),
        Triple("و", "وَ", "Wawu" to "Wa"),
        Triple("هـ", "هَ", "Ha Besar" to "Ha"),
        Triple("ي", "يَ", "Ya" to "Ya")
    )
    return raw.mapIndexed { idx, item ->
        HijaiyahSoundCard(
            arabic = item.first,
            fathah = item.second,
            latinName = item.third.first,
            soundRead = item.third.second,
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
                        mediaUrl = obj.optString("media_url", "")
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    resultList
}

fun getBuiltInFallbackContents(category: String): List<ServerContent> {
    return when (category) {
        "iqro" -> listOf(
            ServerContent(901, "Iqro 1 • Hal 1 (Alif - Ba)", "أَ   بَ", "A  —  Ba", "Ketuk kartu untuk mendengar bunyi! Mulut terbuka saat mengucapkan 'A' dan bibir merapat saat 'Ba'.", ""),
            ServerContent(902, "Iqro 1 • Hal 2 (Ba - Ta - Tsa)", "بَ   تَ   ثَ", "Ba  —  Ta  —  Tsa", "Perhatikan titiknya! Titik satu di bawah dibaca Ba, titik dua di atas Ta, titik tiga di atas Tsa.", ""),
            ServerContent(903, "Iqro 1 • Hal 3 (Jim - Ha - Kho)", "جَ   حَ   خَ", "Ja  —  Ha  —  Kho", "Huruf Ha keluar bersih dari tenggorokan, sedangkan Kho berbunyi sedikit bergetar.", ""),
            ServerContent(904, "Iqro 1 • Hal 4 (Dal - Dzal)", "دَ   ذَ", "Da  —  Dza", "Ujung lidah sedikit disentuhkan ke ujung gigi seri atas saat membaca huruf Dza.", ""),
            ServerContent(905, "Iqro 1 • Hal 5 (Ro - Za)", "رَ   زَ", "Ro  —  Za", "Huruf Ro dibaca tebal dan bergetar ringan, sedangkan Za berdesis seperti lebah.", ""),
            ServerContent(906, "Iqro 1 • Hal 6 (Sin - Syin)", "سَ   شَ", "Sa  —  Sya", "Huruf Sya memiliki tiga titik di atas dan bunyinya menyebar di dalam mulut.", ""),
            ServerContent(907, "Iqro 1 • Hal 7 (Shod - Dhod)", "صَ   ضَ", "Sho  —  Dho", "Keduanya dibaca tebal dengan mengangkat pangkal lidah ke langit-langit mulut.", ""),
            ServerContent(908, "Iqro 1 • Hal 8 (Tho - Zho)", "طَ   ظَ", "Tho  —  Zho", "Dibaca tebal dan bulat tanpa disertai hembusan nafas berlebih.", "")
        )
        "belajar" -> listOf(
            ServerContent(101, "5 Rukun Islam & Penjelasannya", "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ", "Buniyal islaamu 'alaa khamsin", "1. Mengucapkan Dua Kalimat Syahadat\n2. Mendirikan Sholat 5 Waktu\n3. Menunaikan Zakat\n4. Berpuasa di Bulan Ramadhan\n5. Menunaikan Ibadah Haji bagi yang mampu.", ""),
            ServerContent(102, "6 Rukun Iman yang Wajib Diyakini", "أَنْ تُؤْمِنَ بِاللَّهِ وَمَلَائِكَتِهِ", "An tu'mina billaahi wa malaa'ikatihi...", "1. Iman kepada Allah SWT\n2. Iman kepada Malaikat Allah\n3. Iman kepada Kitab-Kitab Allah\n4. Iman kepada Rasul-Rasul Allah\n5. Iman kepada Hari Kiamat\n6. Iman kepada Qada dan Qadar.", "")
        )
        "faq" -> listOf(
            ServerContent(201, "❓ Bagaimana cara mendengar suara huruf di menu Iqro?", "", "", "Cukup ketuk langsung Kartu Gambar Iqro atau buka tab '🎵 28 Hijaiyah' lalu ketuk huruf yang ingin didengar suaranya!", "")
        )
        "tajwid" -> listOf(
            ServerContent(301, "1. Izhar Halqi (Jelas)", "مَنْ آمَنَ", "Man aamana", "Apabila Nun Sukun atau Tanwin bertemu salah satu huruf tenggorokan (ء هـ ع ح غ خ), maka dibaca jelas tanpa dengung.", "")
        )
        else -> listOf(
            ServerContent(501, "Doa Berbuka Puasa", "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ", "Dzahabadz zhama'u wabtallatil 'uruqu wa tsabatal ajru insya Allah", "Telah hilang rasa haus, telah basah urat-urat, dan telah pasti pahala, insya Allah.", "")
        )
    }
}