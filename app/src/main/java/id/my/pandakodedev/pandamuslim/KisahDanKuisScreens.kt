package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class ProphetCategoryInfo(
    val number: Int,
    val name: String,
    val arabicName: String,
    val titleBadge: String,
    val iconEmoji: String,
    val cardColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KisahNabiScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val allProphets = remember { get25ProphetsMasterList() }
    var serverStories by remember { mutableStateOf<List<ServerContent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Navigasi 3 Level: Daftar 25 Nabi -> Daftar Cerita Milik Nabi -> Baca & Dengar Dongeng
    var selectedProphet by remember { mutableStateOf<ProphetCategoryInfo?>(null) }
    var selectedStory by remember { mutableStateOf<ServerContent?>(null) }

    // State Mesin Suara Pendongeng Berirama (Expressive Neural Storyteller)
    var isStorytelling by remember { mutableStateOf(false) }
    var activeSentenceIndex by remember { mutableStateOf(-1) }
    var storySentences by remember { mutableStateOf<List<String>>(emptyList()) }
    var speechSpeedMode by remember { mutableStateOf(1) } // 0 = Santai, 1 = Pendongeng Normal, 2 = Aktif

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
                val indoLocale = Locale("id", "ID")
                tts?.language = indoLocale

                // Cari & Aktifkan Suara Neural / WaveNet Kualitas Tinggi (Tidak Kaku!)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        val allVoices = tts?.voices
                        val bestNeuralVoice = allVoices?.filter {
                            it.locale.language == "id" || it.locale.language == "in"
                        }?.maxByOrNull { voice ->
                            var score = 0
                            val vName = voice.name.lowercase()
                            if (!voice.isNetworkConnectionRequired) score += 10
                            if (vName.contains("network") || vName.contains("wavenet") || vName.contains("neural")) score += 50
                            if (vName.contains("id-id-x-ide") || vName.contains("id-id-x-dfz")) score += 25
                            score + voice.quality
                        }
                        if (bestNeuralVoice != null) {
                            tts?.voice = bestNeuralVoice
                        }
                    } catch (_: Exception) {}
                }
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

    fun stopAllStoryAudio() {
        isStorytelling = false
        activeSentenceIndex = -1
        try {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.reset()
        } catch (_: Exception) {}
        ttsEngine?.stop()
    }

    // =========================================================================
    // MESIN DONGENG BERIRAMA (MEMBACA PER KALIMAT DENGAN NAIK-TURUN NADA ALAMI)
    // =========================================================================
    LaunchedEffect(isStorytelling, selectedStory, speechSpeedMode) {
        val story = selectedStory
        if (!isStorytelling || story == null) return@LaunchedEffect

        // Pecah cerita menjadi kalimat-kalimat agar intonasi hidup & ada jeda napas alami
        val rawText = "${story.title}. ${story.description}"
        val sentences = rawText
            .replace("\n", ". ")
            .split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.length > 2 }

        storySentences = sentences

        val baseRate = when (speechSpeedMode) {
            0 -> 0.84f // Santai Penuh Penghayatan
            2 -> 1.02f // Cepat
            else -> 0.92f // Normal Pendongeng
        }

        for (i in sentences.indices) {
            if (!isStorytelling) break
            activeSentenceIndex = i
            val sentence = sentences[i]

            ttsEngine?.let { tts ->
                tts.language = Locale("id", "ID")
                // Variasikan Pitch (Nada) sesuai konteks kalimat agar terdengar bernada & ekspresif!
                val dynamicPitch = when {
                    i == 0 -> 1.08f // Judul dibuka dengan nada ceria hangat
                    sentence.endsWith("!") -> 1.12f // Kalimat seru/mukjizat lebih bersemangat
                    sentence.endsWith("?") -> 1.14f // Kalimat tanya naik di ujung
                    sentence.contains("Allah") || sentence.contains("Berdoa", ignoreCase = true) -> 0.96f // Khusyuk & teduh
                    i % 2 == 0 -> 1.04f
                    else -> 0.99f
                }
                tts.setPitch(dynamicPitch)
                tts.setSpeechRate(baseRate)
                tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, "story_sent_$i")
            }

            // Hitung estimasi waktu bicara kalimat + jeda napas pendongeng (450ms)
            val wordCount = sentence.split(" ").size
            val speakingTimeMs = (wordCount * 390L / baseRate).toLong().coerceAtLeast(1600L)
            delay(speakingTimeMs + 420L)
        }

        isStorytelling = false
        activeSentenceIndex = -1
    }

    fun playQariAyahInStory(rawMediaUrl: String) {
        stopAllStoryAudio()
        val mp3Url = rawMediaUrl.split("|").find { it.contains(".mp3", ignoreCase = true) } ?: ""
        if (mp3Url.isNotBlank()) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mediaPlayer.setDataSource(normalizeServerMediaUrl(mp3Url))
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener { it.start() }
            } catch (_: Exception) {}
        }
    }

    fun reloadStoriesFromWeb() {
        isLoading = true
        CoroutineScope(Dispatchers.Main).launch {
            serverStories = fetchContentsFromServer("kisah")
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        reloadStoriesFromWeb()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when {
                                selectedStory != null -> selectedStory!!.title
                                selectedProphet != null -> "Kisah ${selectedProphet!!.name}"
                                else -> "🐪 Kisah 25 Nabi & Rasul"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when {
                                selectedStory != null -> "🎙️ Pendongeng Berirama Natural • ${selectedProphet?.name ?: ""}"
                                selectedProphet != null -> "Pilih Bab / Episode Cerita di Bawah Ini"
                                else -> "Pilih Nabi ➔ Pilih Bab Cerita & Dengarkan Dongengnya!"
                            },
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        stopAllStoryAudio()
                        when {
                            selectedStory != null -> selectedStory = null
                            selectedProphet != null -> selectedProphet = null
                            else -> onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { reloadStoriesFromWeb() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang dari Web", tint = GoldAccent)
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            } else if (selectedProphet == null) {
                // =========================================================================
                // LEVEL 1: PILIH KATEGORI 25 NABI & RASUL
                // =========================================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldDark)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari Nama Nabi (contoh: Ibrahim, Musa, Yusuf, Muhammad)...", fontSize = 12.sp) },
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

                val filteredProphets = remember(allProphets, searchQuery) {
                    if (searchQuery.isBlank()) allProphets
                    else allProphets.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.titleBadge.contains(searchQuery, ignoreCase = true)
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProphets, key = { it.number }) { prophet ->
                        // Hitung berapa bab cerita yang ada di database web untuk Nabi ini
                        val storyCount = remember(serverStories, prophet) {
                            serverStories.count {
                                it.latinText.contains(prophet.name, ignoreCase = true) ||
                                        it.title.contains(prophet.name, ignoreCase = true)
                            }.coerceAtLeast(1)
                        }

                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, MintSurface, RoundedCornerShape(22.dp))
                                .clickable { selectedProphet = prophet }
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
                                    Surface(color = prophet.cardColor, shape = CircleShape) {
                                        Text(
                                            text = "#${prophet.number}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = EmeraldDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(color = MintSurface, shape = RoundedCornerShape(50)) {
                                        Text(
                                            text = "📚 $storyCount Cerita",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(prophet.cardColor)
                                ) {
                                    Text(prophet.iconEmoji, fontSize = 28.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = prophet.arabicName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = prophet.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldDark,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Text(
                                    text = prophet.titleBadge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            } else if (selectedStory == null) {
                // =========================================================================
                // LEVEL 2: DAFTAR BAB CERITA MILIK NABI YANG DIPILIH
                // =========================================================================
                val prophet = selectedProphet!!
                val storiesOfThisProphet = remember(serverStories, prophet) {
                    val matched = serverStories.filter {
                        it.latinText.contains(prophet.name, ignoreCase = true) ||
                                it.title.contains(prophet.name, ignoreCase = true)
                    }.sortedBy { it.id }

                    if (matched.isNotEmpty()) matched
                    else listOf(
                        ServerContent(
                            id = prophet.number * 100,
                            title = "Bab 1: Kisah Keteladanan & Mukjizat ${prophet.name}",
                            arabicText = prophet.arabicName,
                            latinText = "${prophet.number}. ${prophet.name}",
                            description = "${prophet.name} (${prophet.titleBadge}) adalah utusan Allah Subhanahu wa Ta'ala yang mengajarkan tauhid, kesabaran, dan akhlak mulia kepada umatnya. Klik tombol Impor di Web Admin Anda untuk menampilkan seluruh bab cerita lengkap!",
                            mediaUrl = ""
                        )
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent)
                                ) {
                                    Text(prophet.iconEmoji, fontSize = 32.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prophet.arabicName,
                                        color = GoldAccent,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = prophet.name,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "${prophet.titleBadge} • Tersedia ${storiesOfThisProphet.size} Episode Cerita",
                                        color = MintSurface,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    items(storiesOfThisProphet, key = { it.id }) { storyItem ->
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, MintSurface, RoundedCornerShape(22.dp))
                                .clickable { selectedStory = storyItem }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(50)) {
                                        Text(
                                            text = "📖 Episode Kisah Teladan",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF92400E),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(color = EmeraldPrimary, shape = RoundedCornerShape(50)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Baca & Dengar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = storyItem.title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = EmeraldDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = storyItem.description,
                                    fontSize = 13.sp,
                                    color = Color.DarkGray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            } else {
                // =========================================================================
                // LEVEL 3: RUANG BACA & DENGAR DONGENG BERIRAMA (SOROT KALIMAT OTOMATIS!)
                // =========================================================================
                val story = selectedStory!!
                val hasMp3Qari = story.mediaUrl.contains(".mp3", ignoreCase = true)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Panel Kontrol Pendongeng Berirama
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🎙️ Studio Dongeng Kak Panda",
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Suara berirama per kalimat dengan sorotan teks otomatis",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Pilihan Gaya Kecepatan & Intonasi Suara
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    0 to "🐢 Nada Santai",
                                    1 to "🎙️ Nada Pendongeng",
                                    2 to "⚡ Nada Ceria"
                                ).forEach { (modeIdx, label) ->
                                    val active = speechSpeedMode == modeIdx
                                    Surface(
                                        color = if (active) GoldAccent else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { speechSpeedMode = modeIdx }
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (active) Color(0xFF0F172A) else Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        if (isStorytelling) stopAllStoryAudio()
                                        else isStorytelling = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isStorytelling) Color(0xFFE11D48) else GoldAccent
                                    ),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isStorytelling) Icons.Default.Stop else Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = if (isStorytelling) Color.White else Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isStorytelling) "Hentikan Dongeng" else "▶️ Putar Suara Dongeng",
                                        color = if (isStorytelling) Color.White else Color(0xFF0F172A),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }

                                if (hasMp3Qari) {
                                    Button(
                                        onClick = { playQariAyahInStory(story.mediaUrl) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Suara Ayat Qari", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Kotak Ayat / Doa Nabi di Dalam Cerita
                    if (story.arabicText.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, GoldAccent, RoundedCornerShape(22.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "✨ Lafadz Doa / Ayat dalam Kisah Ini:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF92400E)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = story.arabicText,
                                    fontSize = 28.sp,
                                    lineHeight = 46.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldDark,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Isi Cerita dengan Fitur Sorot Kalimat yang Sedang Dibacakan!
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = story.title,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = EmeraldDark
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (isStorytelling && storySentences.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    storySentences.forEachIndexed { idx, sent ->
                                        val isCurrent = (idx == activeSentenceIndex)
                                        Surface(
                                            color = if (isCurrent) Color(0xFFFEF08A) else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = sent,
                                                fontSize = 15.sp,
                                                lineHeight = 24.sp,
                                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
                                                color = if (isCurrent) EmeraldDark else Color(0xFF334155),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = story.description,
                                    fontSize = 15.sp,
                                    lineHeight = 25.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun get25ProphetsMasterList(): List<ProphetCategoryInfo> {
    val bgColors = listOf(
        Color(0xFFFEF3C7), Color(0xFFD1FAE5), Color(0xFFE0F2FE),
        Color(0xFFFCE7F3), Color(0xFFEDE9FE), Color(0xFFFFEDD5)
    )
    val raw = listOf(
        Triple("Nabi Adam AS", "آدَم عَلَيْهِ السَّلَامُ", "Abul Basyar (Manusia Pertama)" to "🌍"),
        Triple("Nabi Idris AS", "إِدْرِيس عَلَيْهِ السَّلَامُ", "Pelopor Ilmu Tulis & Menjahit" to "✒️"),
        Triple("Nabi Nuh AS", "نُوح عَلَيْهِ السَّلَامُ", "Pembuat Bahtera Kapal Besar" to "🚢"),
        Triple("Nabi Hud AS", "هُود عَلَيْهِ السَّلَامُ", "Diutus kepada Kaum 'Ad" to "🌪️"),
        Triple("Nabi Shalih AS", "صَالِح عَلَيْهِ السَّلَامُ", "Mukjizat Unta dari Batu Karang" to "🐪"),
        Triple("Nabi Ibrahim AS", "إِبْرَاهِيم عَلَيْهِ السَّلَامُ", "Khalilullah & Pembangun Ka'bah" to "🕋"),
        Triple("Nabi Luth AS", "لُوط عَلَيْهِ السَّلَامُ", "Penjaga Kesucian & Adab" to "🛡️"),
        Triple("Nabi Ismail AS", "إِسْمَاعِيل عَلَيْهِ السَّلَامُ", "Mata Air Zamzam & Qurban" to "💧"),
        Triple("Nabi Ishaq AS", "إِسْحَاق عَلَيْهِ السَّلَامُ", "Putra yang Alim & Santun" to "🌟"),
        Triple("Nabi Ya'qub AS", "يَعْقُوب عَلَيْهِ السَّلَامُ", "Teladan Kesabaran Indah" to "🤲"),
        Triple("Nabi Yusuf AS", "يُوسُف عَلَيْهِ السَّلَامُ", "Pemimpin Mesir yang Pemaaf" to "👑"),
        Triple("Nabi Ayyub AS", "أَيُّوب عَلَيْهِ السَّلَامُ", "Puncak Kesabaran Saat Diuji" to "🌿"),
        Triple("Nabi Syu'aib AS", "شُعَيْب عَلَيْهِ السَّلَامُ", "Khathibul Anbiya & Kejujuran" to "⚖️"),
        Triple("Nabi Musa AS", "مُوسَىٰ عَلَيْهِ السَّلَامُ", "Kalimullah & Pembelah Lautan" to "🌊"),
        Triple("Nabi Harun AS", "هَارُون عَلَيْهِ السَّلَامُ", "Pendamping Dakwah yang Lembut" to "🤝"),
        Triple("Nabi Dzulkifli AS", "ذُو الْكِفْلِ عَلَيْهِ السَّلَامُ", "Raja Adil yang Menepati Janji" to "🏛️"),
        Triple("Nabi Dawud AS", "دَاوُد عَلَيْهِ السَّلَامُ", "Kitab Zabur & Melunakkan Besi" to "🏔️"),
        Triple("Nabi Sulaiman AS", "سُلَيْمَان عَلَيْهِ السَّلَامُ", "Memahami Bahasa Burung & Semut" to "🦅"),
        Triple("Nabi Ilyas AS", "إِلْيَاس عَلَيْهِ السَّلَامُ", "Penegak Tauhid yang Teguh" to "⛰️"),
        Triple("Nabi Ilyasa' AS", "الْيَسَع عَلَيْهِ السَّلَامُ", "Penerus Dakwah Penuh Berkah" to "🕊️"),
        Triple("Nabi Yunus AS", "يُونُس عَلَيْهِ السَّلَامُ", "Bertasbih di Perut Ikan Paus" to "🐋"),
        Triple("Nabi Zakariya AS", "زَكَرِيَّا عَلَيْهِ السَّلَامُ", "Doa Khusyuk di Dalam Mihrab" to "🕌"),
        Triple("Nabi Yahya AS", "يَحْيَىٰ عَلَيْهِ السَّلَامُ", "Pemuda Berbakti & Kasih Sayang" to "📖"),
        Triple("Nabi Isa AS", "عِيسَىٰ عَلَيْهِ السَّلَامُ", "Ruhullah & Penerima Kitab Injil" to "✨"),
        Triple("Nabi Muhammad SAW", "مُحَمَّد ﷺ", "Khatamul Anbiya & Al-Quran" to "🌙")
    )
    return raw.mapIndexed { index, item ->
        ProphetCategoryInfo(
            number = index + 1,
            name = item.first,
            arabicName = item.second,
            titleBadge = item.third.first,
            iconEmoji = item.third.second,
            cardColor = bgColors[index % bgColors.size]
        )
    }
}

// =========================================================================
// GAME KUIS PINTAR CERDAS CERMAT ISLAMI (MENDUKUNG SOAL DARI WEB ADMIN!)
// =========================================================================
data class QuizQuestionItem(
    val question: String,
    val arabicHint: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuisIslamiScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("panda_quiz_prefs", Context.MODE_PRIVATE) }

    var questions by remember { mutableStateOf<List<QuizQuestionItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var totalStars by remember { mutableStateOf(prefs.getInt("total_quiz_stars", 0)) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        val webQuiz = fetchContentsFromServer("kuis")
        val mappedWeb = webQuiz.mapNotNull { item ->
            val rawOpts = item.latinText.split("|").map { it.trim() }.filter { it.isNotBlank() }
            if (rawOpts.size >= 2) {
                val correct = rawOpts[0]
                QuizQuestionItem(
                    question = item.title,
                    arabicHint = item.arabicText,
                    options = rawOpts.shuffled(),
                    correctAnswer = correct,
                    explanation = item.description
                )
            } else null
        }
        questions = mappedWeb + getDefaultQuizQuestions()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🎮 Kuis Cerdas Cermat Islami", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                        Text("Total Tabungan Bintang: ⭐ $totalStars Bintang", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
        if (isLoading || questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        } else {
            val q = questions[currentIndex % questions.size]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OffWhiteBg)
                    .padding(padding)
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
                        Text("Soal ${(currentIndex % questions.size) + 1} dari ${questions.size}", color = Color.White, fontWeight = FontWeight.Bold)
                        Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                            Text(
                                text = "Skor Sesi Ini: $score",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (q.arabicHint.isNotBlank()) {
                            Text(
                                text = q.arabicHint,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Text(
                            text = q.question,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldDark,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                q.options.forEach { opt ->
                    val isCorrect = (opt == q.correctAnswer)
                    val isSelected = (selectedOption == opt)
                    val bg = when {
                        selectedOption == null -> Color.White
                        isCorrect -> Color(0xFFD1FAE5)
                        isSelected -> Color(0xFFFFE4E6)
                        else -> Color.White
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = bg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = if (selectedOption != null && isCorrect) Color(0xFF059669) else MintSurface,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable(enabled = selectedOption == null) {
                                selectedOption = opt
                                if (isCorrect) {
                                    score += 10
                                    totalStars += 1
                                    prefs.edit().putInt("total_quiz_stars", totalStars).apply()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(opt, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = EmeraldDark)
                            if (selectedOption != null && isCorrect) {
                                Text("✅ Benar!", fontWeight = FontWeight.Black, color = Color(0xFF059669), fontSize = 12.sp)
                            } else if (isSelected && !isCorrect) {
                                Text("❌ Salah", fontWeight = FontWeight.Black, color = Color(0xFFE11D48), fontSize = 12.sp)
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = selectedOption != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Penjelasan: ${q.explanation}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Button(
                            onClick = {
                                selectedOption = null
                                currentIndex++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Soal Berikutnya ➔", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

fun getDefaultQuizQuestions(): List<QuizQuestionItem> = listOf(
    QuizQuestionItem(
        "Siapakah Nabi yang mukjizatnya tidak terbakar oleh kobaran api Raja Namrud?",
        "قُلْنَا يَا نَارُ كُونِي بَرْدًا وَسَلَامًا",
        listOf("Nabi Ibrahim AS", "Nabi Musa AS", "Nabi Nuh AS", "Nabi Isa AS").shuffled(),
        "Nabi Ibrahim AS",
        "Allah memerintahkan api menjadi dingin dan menyelamatkan Nabi Ibrahim AS (QS. Al-Anbiya: 69)."
    ),
    QuizQuestionItem(
        "Apa hukum mendekati perbuatan zina dan pergaulan bebas (pacaran) di dalam Al-Quran Surah Al-Isra ayat 32?",
        "وَلَا تَقْرَبُوا الزِّنَا",
        listOf("Dilarang keras (Haram)", "Boleh saja", "Makruh", "Mubah").shuffled(),
        "Dilarang keras (Haram)",
        "Allah melarang keras mendekati zina karena merupakan perbuatan keji dan jalan yang buruk."
    ),
    QuizQuestionItem(
        "Berapakah jumlah total rakaat sholat fardhu 5 waktu dalam sehari semalam?",
        "الصَّلَاة",
        listOf("17 Rakaat", "15 Rakaat", "20 Rakaat", "12 Rakaat").shuffled(),
        "17 Rakaat",
        "Subuh 2, Dzuhur 4, Ashar 4, Maghrib 3, dan Isya 4 rakaat (Total = 17 rakaat)."
    )
)