package id.my.pandakodedev.pandamuslim

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

data class AsmaulHusnaItem(
    val number: Int,
    val arabic: String,
    val latin: String,
    val meaning: String
)

enum class AsmaulMelodyOption(val title: String, val subtitle: String, val streamUrl: String) {
    NADA_HAFALAN_CERIA(
        "🎵 Nada Lagu Hafalan Populer (1–99)",
        "Irama berirama yang mudah diikuti anak-anak untuk menghafal",
        "https://archive.org/download/asma-ul-husna-99-names-of-allah/Asma-ul-Husna.mp3"
    ),
    NADA_SYAHDU_ANAK(
        "🎶 Nada Nasyid Anak & Keluarga",
        "Lantunan lembut penuh penghayatan 99 Asmaul Husna",
        "https://archive.org/download/AsmaulHusna_201506/Asmaul%20Husna.mp3"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsmaulHusnaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val allNames = remember { get99AsmaulHusnaComplete() }
    var searchQuery by remember { mutableStateOf("") }

    var selectedMelody by remember { mutableStateOf(AsmaulMelodyOption.NADA_HAFALAN_CERIA) }
    var isPlayingSong by remember { mutableStateOf(false) }
    var isPreparingSong by remember { mutableStateOf(false) }

    var isAutoChanting99 by remember { mutableStateOf(false) }
    var activeChantIndex by remember { mutableStateOf(-1) }

    val mediaPlayer = remember {
        MediaPlayer().apply {
            setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        }
    }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res = tts?.setLanguage(Locale("ar", "SA"))
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale("ar"))
                }
                tts?.setSpeechRate(0.78f)
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

    LaunchedEffect(isAutoChanting99) {
        if (isAutoChanting99) {
            for (i in allNames.indices) {
                if (!isAutoChanting99) break
                activeChantIndex = i
                val item = allNames[i]
                val melodyPitches = listOf(1.15f, 1.05f, 1.20f, 0.98f)
                ttsEngine?.setPitch(melodyPitches[i % melodyPitches.size])
                ttsEngine?.setSpeechRate(0.78f)
                ttsEngine?.speak(item.arabic, TextToSpeech.QUEUE_FLUSH, null, "chant_$i")
                delay(2100L)
            }
            isAutoChanting99 = false
            activeChantIndex = -1
        }
    }

    fun toggleMelodySong(option: AsmaulMelodyOption) {
        isAutoChanting99 = false
        ttsEngine?.stop()

        if (isPlayingSong && selectedMelody == option) {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.reset()
            } catch (_: Exception) {}
            isPlayingSong = false
            isPreparingSong = false
        } else {
            selectedMelody = option
            try {
                isPreparingSong = true
                mediaPlayer.reset()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mediaPlayer.setDataSource(option.streamUrl)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener { mp ->
                    isPreparingSong = false
                    isPlayingSong = true
                    mp.start()
                }
                mediaPlayer.setOnCompletionListener {
                    isPlayingSong = false
                }
                mediaPlayer.setOnErrorListener { _, _, _ ->
                    isPreparingSong = false
                    isPlayingSong = false
                    isAutoChanting99 = true
                    true
                }
            } catch (_: Exception) {
                isPreparingSong = false
                isPlayingSong = false
                isAutoChanting99 = true
            }
        }
    }

    fun speakSingleAsmaul(item: AsmaulHusnaItem, idx: Int) {
        activeChantIndex = idx
        ttsEngine?.setPitch(1.08f)
        ttsEngine?.setSpeechRate(0.75f)
        ttsEngine?.speak(item.arabic, TextToSpeech.QUEUE_FLUSH, null, "single_asma")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("✨ 99 Asmaul Husna & Lagu Hafalan", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                        Text("Dilengkapi Nada Lagu Anak & Ketuk Kartu Bersuara 🎵", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        isAutoChanting99 = false
                        try { if (mediaPlayer.isPlaying) mediaPlayer.stop() } catch (_: Exception) {}
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
            Card(
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎶 Pilih Suara & Nada Lagu Hafalan Anak (1–99):",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsmaulMelodyOption.values().forEach { opt ->
                            val isThisPlaying = isPlayingSong && selectedMelody == opt
                            val isThisPreparing = isPreparingSong && selectedMelody == opt
                            Surface(
                                color = if (isThisPlaying) Color(0xFFE11D48) else GoldAccent,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { toggleMelodySong(opt) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isThisPlaying) Icons.Default.Stop else Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (isThisPlaying) Color.White else Color(0xFF0F172A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when {
                                            isThisPreparing -> "Memuat Lagu..."
                                            isThisPlaying -> "Stop Nada"
                                            else -> opt.title.take(22)
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isThisPlaying) Color.White else Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (isPlayingSong) {
                                    try { mediaPlayer.stop() } catch (_: Exception) {}
                                    isPlayingSong = false
                                }
                                isAutoChanting99 = !isAutoChanting99
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAutoChanting99) Color(0xFFE11D48) else EmeraldPrimary
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isAutoChanting99) Icons.Default.Stop else Icons.Default.PlayCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAutoChanting99) "Hentikan Bacaan Otomatis 1–99" else "▶️ Putar Berurutan 1–99 (Sorot Kartu)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari Asmaul Husna (contoh: Ar-Rahman, Al-Malik, Pemberi Rezeki)...", fontSize = 12.sp) },
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
            }

            val filtered = remember(allNames, searchQuery) {
                if (searchQuery.isBlank()) allNames
                else allNames.filter {
                    it.latin.contains(searchQuery, ignoreCase = true) ||
                            it.meaning.contains(searchQuery, ignoreCase = true) ||
                            it.number.toString() == searchQuery.trim()
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.number }) { item ->
                    val isHighlighted = (activeChantIndex == item.number - 1)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isHighlighted) Color(0xFFFEF9C3) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isHighlighted) 2.5.dp else 1.dp,
                                color = if (isHighlighted) Color(0xFFD97706) else MintSurface,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { speakSingleAsmaul(item, item.number - 1) }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(color = MintSurface, shape = CircleShape) {
                                    Text(
                                        text = "${item.number}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Suara",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.arabic,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.latin,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.meaning,
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                minLines = 2,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}