package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

data class DzikirOption(
    val id: Int,
    val title: String,
    val arabic: String,
    val latin: String,
    val meaning: String,
    val defaultTarget: Int,
    val fadhilah: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tasbih3DScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("panda_tasbih_prefs", Context.MODE_PRIVATE) }

    val dzikirList = remember { getCompleteDzikirOptions() }
    var selectedDzikir by remember { mutableStateOf(dzikirList[0]) }

    var count by remember(selectedDzikir.id) {
        mutableStateOf(prefs.getInt("count_${selectedDzikir.id}", 0))
    }
    var totalLifetimeCount by remember {
        mutableStateOf(prefs.getInt("total_lifetime_dzikir", 0))
    }
    var target by remember(selectedDzikir.id) {
        mutableStateOf(selectedDzikir.defaultTarget)
    }
    var isSoundEnabled by remember { mutableStateOf(true) }
    var isPressed by remember { mutableStateOf(false) }

    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "tasbih_scale"
    )

    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res = tts?.setLanguage(Locale("ar", "SA"))
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale("ar"))
                }
                tts?.setSpeechRate(0.85f)
            }
        }
        ttsEngine = tts
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(110)
            isPressed = false
        }
    }

    fun speakCurrentDzikir() {
        ttsEngine?.speak(selectedDzikir.arabic, TextToSpeech.QUEUE_FLUSH, null, "dzikir_tts")
    }

    fun incrementTasbih() {
        isPressed = true
        val nextCount = count + 1
        count = nextCount
        totalLifetimeCount += 1

        prefs.edit()
            .putInt("count_${selectedDzikir.id}", nextCount)
            .putInt("total_lifetime_dzikir", totalLifetimeCount)
            .apply()

        if (nextCount % target == 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, "Alhamdulillah! Mencapai target ${target}x ${selectedDzikir.title} 🌟", Toast.LENGTH_SHORT).show()
            if (isSoundEnabled) speakCurrentDzikir()
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("📿 Tasbih Digital 3D & Dzikir", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                        Text("Total Tabungan Dzikir: $totalLifetimeCount Kali", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                actions = {
                    IconButton(onClick = { isSoundEnabled = !isSoundEnabled }) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Suara",
                            tint = GoldAccent
                        )
                    }
                    IconButton(onClick = {
                        count = 0
                        prefs.edit().putInt("count_${selectedDzikir.id}", 0).apply()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
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
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Deretan Pilihan 12 Dzikir Harian
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "👆 Pilih Bacaan Dzikir Harian:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldDark,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(dzikirList, key = { it.id }) { item ->
                        val isSelected = item.id == selectedDzikir.id
                        Surface(
                            color = if (isSelected) EmeraldDark else Color.White,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) GoldAccent else MintSurface,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    selectedDzikir = item
                                    if (isSoundEnabled) {
                                        ttsEngine?.speak(item.arabic, TextToSpeech.QUEUE_FLUSH, null, "dzikir_select")
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) GoldAccent else EmeraldDark
                                )
                                Text(
                                    text = "Target: ${item.defaultTarget}x",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // 2. Kartu Lafadz Arab, Latin, Arti & Keutamaan
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(color = MintSurface, shape = RoundedCornerShape(50)) {
                            Text(
                                text = selectedDzikir.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = GoldAccent,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { speakCurrentDzikir() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dengar Lafadz", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = selectedDzikir.arabic,
                        fontSize = 27.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedDzikir.latin,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${selectedDzikir.meaning}\"",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = Color(0xFFFEF9C3), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = "🌟 Keutamaan: ${selectedDzikir.fadhilah}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // 3. Pilihan Target Putaran (33x / 99x / 100x / 1000x)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Target:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                listOf(33, 99, 100, 1000).forEach { t ->
                    val active = target == t
                    Surface(
                        color = if (active) GoldAccent else Color.White,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(1.dp, EmeraldPrimary, RoundedCornerShape(50))
                            .clickable { target = t }
                    ) {
                        Text(
                            text = "${t}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (active) Color(0xFF0F172A) else EmeraldDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // 4. Tombol Biji Tasbih 3D Raksasa Interaktif
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(210.dp)
                    .scale(scaleAnim)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(EmeraldPrimary, EmeraldDark)
                        )
                    )
                    .border(6.dp, GoldAccent, CircleShape)
                    .clickable { incrementTasbih() }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = size.minDimension * 0.38f,
                        center = Offset(size.width * 0.35f, size.height * 0.32f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$count",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "dari Target $target",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(50)) {
                        Text(
                            text = "👆 KETUK TASBIH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getCompleteDzikirOptions(): List<DzikirOption> = listOf(
    DzikirOption(1, "Istighfar Agung", "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ", "Astaghfirullaahal 'Azhiim", "Aku memohon ampun kepada Allah Yang Maha Agung", 33, "Membuka pintu rezeki dan menghapus dosa-dosa harian."),
    DzikirOption(2, "Tasbih & Tahmid", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "Subhaanallaahi wa bihamdih", "Maha Suci Allah dan segala puji bagi-Nya", 100, "Barangsiapa membacanya 100x sehari, dihapuskan dosanya walau sebanyak buih di lautan. (HR. Bukhari)"),
    DzikirOption(3, "Tasbih Agung", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ", "Subhaanallaahi wa bihamdihi, Subhaanallaahil 'Azhiim", "Maha Suci Allah dan segala puji bagi-Nya, Maha Suci Allah Yang Maha Agung", 33, "Dua kalimat yang ringan di lisan, dicintai Ar-Rahman, dan berat di timbangan amal. (HR. Bukhari & Muslim)"),
    DzikirOption(4, "Subhanallah", "سُبْحَانَ اللَّهِ", "Subhaanallaah", "Maha Suci Allah", 33, "Dzikir utama bakda sholat fardhu 33 kali."),
    DzikirOption(5, "Alhamdulillah", "الْحَمْدُ لِلَّهِ", "Alhamdulillaah", "Segala puji bagi Allah", 33, "Kalimat syukur yang memenuhi timbangan pahala kebaikan."),
    DzikirOption(6, "Allahu Akbar", "اللَّهُ أَكْبَرُ", "Allaahu Akbar", "Allah Maha Besar", 33, "Meneguhkan kebesaran Allah di dalam hati."),
    DzikirOption(7, "Tahlil & Tauhid", "لَا إِلَٰهَ إِلَّا اللَّهُ", "Laa ilaaha illallaah", "Tiada Tuhan yang berhak disembah selain Allah", 100, "Sebaik-baik dzikir adalah Laa ilaaha illallaah. (HR. Tirmidzi)"),
    DzikirOption(8, "Hauqalah (Harta Surga)", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "Laa haula wa laa quwwata illaa billaah", "Tiada daya dan upaya kecuali dengan pertolongan Allah", 33, "Salah satu perbendaharaan harta simpanan di Surga. (HR. Bukhari)"),
    DzikirOption(9, "Sholawat Nabi SAW", "اللَّهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ", "Allaahumma shalli 'alaa Sayyidinaa Muhammad", "Ya Allah, limpahkanlah rahmat kepada Nabi kami Muhammad SAW", 100, "Barangsiapa bersholawat 1 kali, Allah bersholawat (memberi rahmat) kepadanya 10 kali. (HR. Muslim)"),
    DzikirOption(10, "Dzikir Empat Kalimat Mulia", "سُبْحَانَ اللَّهِ ، وَالْحَمْدُ لِلَّهِ ، وَلَا إِلَٰهَ إِلَّا اللَّهُ ، وَاللَّهُ أَكْبَرُ", "Subhaanallaah, walhamdulillaah, wa laa ilaaha illallaah, wallaahu akbar", "Maha Suci Allah, segala puji bagi Allah, tiada Tuhan selain Allah, Allah Maha Besar", 33, "Kalimat yang paling dicintai Allah dan menjadi tanaman pohon di Surga."),
    DzikirOption(11, "Dzikir Nabi Yunus AS", "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ", "Laa ilaaha illaa Anta subhaanaka innii kuntu minazh-zhaalimiin", "Tiada Tuhan selain Engkau, Maha Suci Engkau, sungguh aku termasuk orang yang zalim", 33, "Doa mustajab pengangkat kesulitan dan kesedihan."),
    DzikirOption(12, "Hasbunallah Wa Ni'mal Wakil", "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ", "Hasbunallaahu wa ni'mal wakiil", "Cukuplah Allah menjadi Penolong kami dan Allah adalah sebaik-baik Pelindung", 33, "Dzikir keberanian dan tawakkal penuh yang dibaca Nabi Ibrahim AS & Rasulullah SAW.")
)