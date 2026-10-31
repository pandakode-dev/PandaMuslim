package id.my.pandakodedev.pandamuslim

import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

data class NobleWomanStory(
    val id: Int,
    val name: String,
    val arabicName: String,
    val titleHonor: String,
    val emoji: String,
    val duaArabic: String,
    val storyParagraphs: String,
    val moralLesson: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KisahWanitaMuliaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val stories = remember { getNobleWomenStoriesList() }

    var selectedWoman by remember { mutableStateOf<NobleWomanStory?>(null) }
    var isStorytelling by remember { mutableStateOf(false) }
    var activeSentenceIdx by remember { mutableStateOf(-1) }
    var sentencesList by remember { mutableStateOf<List<String>>(emptyList()) }

    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("id", "ID")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        val bestIndo = tts?.voices?.filter {
                            it.locale.language == "id" || it.locale.language == "in"
                        }?.maxByOrNull { v ->
                            val n = v.name.lowercase()
                            (if (n.contains("network") || n.contains("wavenet") || n.contains("neural")) 50 else 0) + v.quality
                        }
                        if (bestIndo != null) tts?.voice = bestIndo
                    } catch (_: Exception) {}
                }
            }
        }
        ttsEngine = tts
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(isStorytelling, selectedWoman) {
        val woman = selectedWoman
        if (!isStorytelling || woman == null) return@LaunchedEffect

        val fullText = "Kisah Teladan ${woman.name}, ${woman.titleHonor}. ${woman.storyParagraphs}. Hikmah teladan: ${woman.moralLesson}"
        val sents = fullText
            .replace("\n", ". ")
            .split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.length > 2 }

        sentencesList = sents

        for (i in sents.indices) {
            if (!isStorytelling) break
            activeSentenceIdx = i
            val s = sents[i]
            ttsEngine?.let { tts ->
                tts.language = Locale("id", "ID")
                val pitch = when {
                    i == 0 -> 1.08f
                    s.endsWith("!") -> 1.12f
                    i % 2 == 0 -> 1.04f
                    else -> 0.99f
                }
                tts.setPitch(pitch)
                tts.setSpeechRate(0.91f)
                tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "woman_story_$i")
            }
            val words = s.split(" ").size
            delay((words * 400L).coerceAtLeast(1600L) + 400L)
        }
        isStorytelling = false
        activeSentenceIdx = -1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = selectedWoman?.name ?: "🧕 Kisah Wanita Teladan & Shahabiyah",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = selectedWoman?.titleHonor ?: "Ibu Para Nabi, Penghuni Surga & Pejuang Iman",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        isStorytelling = false
                        ttsEngine?.stop()
                        if (selectedWoman != null) selectedWoman = null else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
            )
        }
    ) { padding ->
        if (selectedWoman == null) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(OffWhiteBg)
                    .padding(padding)
            ) {
                items(stories, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, MintSurface, RoundedCornerShape(22.dp))
                            .clickable { selectedWoman = item }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFCE7F3))
                                    .border(2.dp, Color(0xFFF472B6), CircleShape)
                            ) {
                                Text(item.emoji, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(50)) {
                                    Text(
                                        text = item.titleHonor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.name, fontWeight = FontWeight.Black, fontSize = 16.sp, color = EmeraldDark)
                                Text(item.arabicName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = EmeraldPrimary)
                        }
                    }
                }
            }
        } else {
            val w = selectedWoman!!
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
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(w.arabicName, fontSize = 28.sp, fontWeight = FontWeight.Black, color = GoldAccent)
                        Text(w.name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text(w.titleHonor, fontSize = 12.sp, color = MintSurface)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (isStorytelling) {
                                    isStorytelling = false
                                    ttsEngine?.stop()
                                } else {
                                    isStorytelling = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isStorytelling) Color(0xFFE11D48) else GoldAccent
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (isStorytelling) Icons.Default.Stop else Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = if (isStorytelling) Color.White else Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isStorytelling) "⏹️ Hentikan Suara Pendongeng" else "🔊 Dengarkan Kisah Berirama",
                                color = if (isStorytelling) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                if (w.duaArabic.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, GoldAccent, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✨ Doa / Ayat Kemuliaan Beliau:", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF92400E))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(w.duaArabic, fontSize = 25.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold, color = EmeraldDark, textAlign = TextAlign.Center)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        if (isStorytelling && sentencesList.isNotEmpty()) {
                            sentencesList.forEachIndexed { idx, s ->
                                val active = (idx == activeSentenceIdx)
                                Surface(
                                    color = if (active) Color(0xFFFEF08A) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = s,
                                        fontSize = 15.sp,
                                        lineHeight = 24.sp,
                                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = EmeraldDark,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        } else {
                            Text(w.storyParagraphs, fontSize = 15.sp, lineHeight = 24.sp, color = Color(0xFF1E293B))
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(color = MintSurface, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("🌟 Hikmah & Teladan Untuk Kita:", fontWeight = FontWeight.Black, fontSize = 13.sp, color = EmeraldDark)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(w.moralLesson, fontSize = 13.sp, color = EmeraldDark, lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getNobleWomenStoriesList(): List<NobleWomanStory> = listOf(
    NobleWomanStory(
        1, "Siti Khadijah binti Khuwailid RA", "خَدِيجَة بِنْت خُوَيْلِد", "Ummul Mukminin & Wanita Pertama Beriman", "👑",
        "إِنَّ اللَّهَ يُبَشِّرُكِ بِبَيْتٍ فِي الْجَنَّةِ مِنْ قَصَبٍ",
        "Siti Khadijah adalah istri pertama Rasulullah SAW yang sangat mulia, dermawan, dan cerdas. Ketika wahyu pertama turun di Gua Hira dan Rasulullah pulang dalam keadaan gemetar, Khadijah adalah orang pertama yang menenangkan, menyelimuti, dan membenarkan kenabian beliau. Seluruh harta kekayaannya disedekahkan untuk membela dakwah Islam dan membantu fakir miskin. Malaikat Jibril bahkan menyampaikan salam langsung dari Allah SWT untuk Khadijah dan memberi kabar gembira tentang sebuah istana mutiara di Surga yang tiada kebisingan maupun kelelahan di dalamnya!",
        "Jadilah pribadi yang setia mendukung kebaikan, dermawan menolong sesama, dan menenangkan hati keluarga."
    ),
    NobleWomanStory(
        2, "Siti Hajar (Ibunda Nabi Ismail AS)", "هَاجَر عَلَيْهَا السَّلَامُ", "Simbol Tawakkal & Asal Mula Sa'i Air Zamzam", "💧",
        "إِذًا لَا يُضَيِّعُنَا اللَّهُ",
        "Ketika Nabi Ibrahim AS diperintahkan Allah meninggalkan Siti Hajar dan bayi Ismail di lembah padang pasir Makkah yang tandus, Siti Hajar bertanya: Apakah Allah yang memerintahkan ini? Saat Nabi Ibrahim mengangguk, Siti Hajar berkata dengan iman yang kokoh: Jika demikian, pasti Allah tidak akan menyia-nyiakan kami! Saat perbekalan air habis, beliau tidak duduk diam, melainkan berusaha berlari tujuh kali antara bukit Shafa dan Marwah hingga Allah memancarkan mata air Zamzam yang penuh berkah.",
        "Tawakkal kepada Allah harus disertai dengan usaha (ikhtiar) yang sungguh-sungguh dan prasangka baik kepada Allah."
    ),
    NobleWomanStory(
        3, "Siti Asiyah binti Muzahim", "آسِيَة بِنْت مُزَاحِم", "Ratu Mesir yang Memilih Istana di Surga", "🏰",
        "رَبِّ ابْنِ لِي عِنْدَكَ بَيْتًا فِي الْجَنَّةِ",
        "Siti Asiyah hidup di dalam istana termegah di Mesir sebagai permaisuri Fir'aun. Beliau yang menyelamatkan dan merawat bayi Nabi Musa AS dengan penuh kasih sayang. Meskipun dikelilingi kemewahan dunia, hati Siti Asiyah hanya beriman kepada Allah Yang Maha Esa. Saat Fir'aun menyiksanya agar meninggalkan iman, Siti Asiyah tetap teguh dan berdoa: Ya Tuhanku, bangunkanlah untukku sebuah rumah di sisi-Mu di dalam Surga! Allah pun memperlihatkan istananya di Surga hingga beliau tersenyum bahagia.",
        "Keimanan dan ketaatan kepada Allah jauh lebih berharga daripada seluruh harta dan kemewahan dunia."
    ),
    NobleWomanStory(
        4, "Maryam binti Imran", "مَرْيَم بِنْت عِمْرَان", "Wanita Terbaik Penjaga Kesucian & Kehormatan", "🕊️",
        "يَا مَرْيَمُ إِنَّ اللَّهَ اصْطَفَاكِ وَطَهَّرَكِ",
        "Maryam binti Imran adalah satu-satunya wanita yang namanya diabadikan menjadi nama Surah di dalam Al-Quran (Surah Maryam). Sejak kecil beliau tumbuh dalam ketaatan beribadah di mihrab Baitul Maqdis dan sangat menjaga kehormatan serta kesucian dirinya dari pergaulan buruk. Kemuliaan adab dan kesuciannya membuat Malaikat Jibril menyampaikan bahwa Allah telah memilih dan menyucikannya di atas wanita-wanita seluruh alam, serta mengaruniakan putra mulia Nabi Isa AS.",
        "Menjaga kesucian diri (iffah), menutup aurat, dan rajin beribadah akan mengangkat derajat seorang muslimah di sisi Allah."
    ),
    NobleWomanStory(
        5, "Fathimah Az-Zahra RA", "فَاطِمَة الزَّهْرَاء", "Putri Tercinta Rasulullah & Pemimpin Wanita Surga", "🌸",
        "فَاطِمَةُ بَضْعَةٌ مِنِّي",
        "Fathimah Az-Zahra adalah putri bungsu Rasulullah SAW yang paling mirip cara berjalan, cara berbicara, dan akhlaknya dengan sang Ayah. Setiap kali Fathimah datang, Rasulullah berdiri menyambutnya dan mencium keningnya. Beliau hidup sangat sederhana bersama suaminya Ali bin Abi Thalib RA, rajin menggiling gandum dengan tangannya sendiri, namun tetap gemar bersedekah memberikan makanannya kepada orang miskin, anak yatim, dan tawanan meski dirinya sedang berpuasa.",
        "Kesederhanaan, rasa malu yang tinggi, dan bakti tulus kepada orang tua adalah mahkota terindah anak sholehah."
    ),
    NobleWomanStory(
        6, "Aisyah binti Abu Bakar RA", "عَائِشَة بِنْت أَبِي بَكْر", "Ummul Mukminin & Guru Besar Ilmu Hadits", "📚",
        "خُذُوا شَطْرَ دِينِكُمْ عَنْ هَذِهِ الْحُمَيْرَاءِ",
        "Sayyidatina Aisyah RA dikaruniai kecerdasan dan daya ingat yang luar biasa tajam. Beliau meriwayatkan lebih dari 2.210 hadits Rasulullah SAW dan menjadi tempat bertanya para sahabat senior tentang ilmu Al-Quran, fiqih, waris (faraidh), hingga pengobatan. Hal ini membuktikan bahwa Islam sangat memuliakan perempuan yang gemar menuntut ilmu pengetahuan dan mengajarkannya.",
        "Anak perempuan maupun laki-laki muslim wajib semangat belajar ilmu agama dan ilmu pengetahuan setinggi mungkin."
    )
)