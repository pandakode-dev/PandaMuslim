package id.my.pandakodedev.pandamuslim

import android.media.MediaPlayer
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
    var stories by remember { mutableStateOf(getNobleWomenStoriesList()) }
    LaunchedEffect(Unit) {
        val webItems = fetchContentsFromServer("wanita")
        if (webItems.isNotEmpty()) {
            val mappedWeb = webItems.map { w ->
                NobleWomanStory(
                    id = w.id,
                    name = w.title,
                    arabicName = w.arabicText.ifBlank { "رَضِيَ اللَّهُ عَنْهَا" },
                    titleHonor = w.latinText.ifBlank { "Tokoh Wanita Mulia" },
                    emoji = "🧕",
                    duaArabic = w.arabicText,
                    storyParagraphs = w.description,
                    moralLesson = "Teladani keimanan, kesabaran, dan kemuliaan adab beliau dalam kehidupan sehari-hari."
                )
            }
            stories = mappedWeb + getNobleWomenStoriesList()
        }
    }

    var selectedWoman by remember { mutableStateOf<NobleWomanStory?>(null) }
    var selectedVoiceStyle by remember { mutableStateOf(StorytellerVoiceStyle.KAKAK_LEMBUT) }

    var isStorytelling by remember { mutableStateOf(false) }
    var activeSentenceIdx by remember { mutableStateOf(-1) }
    var sentencesList by remember { mutableStateOf<List<String>>(emptyList()) }

    val mediaPlayer = remember { MediaPlayer() }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { safeTts ->
                    IndonesianStorytellerEngine.configureSoftIndonesianVoice(safeTts, selectedVoiceStyle)
                }
            }
        }
        ttsEngine = tts
        onDispose {
            IndonesianStorytellerEngine.stopStorytelling(tts)
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
            } catch (_: Exception) {}
            tts?.shutdown()
        }
    }

    fun startOrRestartStory(woman: NobleWomanStory, style: StorytellerVoiceStyle = selectedVoiceStyle) {
        val fullStoryBody = "${woman.storyParagraphs}. Hikmah teladan untuk kita: ${woman.moralLesson}"
        val sents = IndonesianStorytellerEngine.splitIntoNaturalSentences(
            rawTitle = "Kisah Teladan ${woman.name}, ${woman.titleHonor}",
            rawBody = fullStoryBody
        )
        sentencesList = sents
        isStorytelling = true
        activeSentenceIdx = 0

        IndonesianStorytellerEngine.startExpressiveStorytelling(
            tts = ttsEngine,
            sentences = sents,
            style = style,
            onSentenceHighlight = { idx -> activeSentenceIdx = idx },
            onFinished = {
                isStorytelling = false
                activeSentenceIdx = -1
            }
        )
    }

    fun stopStoryAudio() {
        isStorytelling = false
        activeSentenceIdx = -1
        IndonesianStorytellerEngine.stopStorytelling(ttsEngine)
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
                            text = selectedWoman?.titleHonor ?: "Suara Pendongeng Lembut & Penuh Hikmah 🌸",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        stopStoryAudio()
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
                // Panel Studio Suara Pendongeng Lembut
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(w.arabicName, fontSize = 28.sp, fontWeight = FontWeight.Black, color = GoldAccent)
                        Text(w.name, fontSize = 19.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
                        Text(w.titleHonor, fontSize = 12.sp, color = MintSurface, textAlign = TextAlign.Center)

                        // Pilihan 3 Karakter Suara Pendongeng
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StorytellerVoiceStyle.values().forEach { style ->
                                val active = selectedVoiceStyle == style
                                Surface(
                                    color = if (active) GoldAccent else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedVoiceStyle = style
                                            if (isStorytelling) {
                                                startOrRestartStory(w, style)
                                            }
                                        }
                                ) {
                                    Text(
                                        text = style.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (active) Color(0xFF0F172A) else Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (isStorytelling) stopStoryAudio()
                                else startOrRestartStory(w, selectedVoiceStyle)
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
                                text = if (isStorytelling) "⏹️ Hentikan Suara Pendongeng" else "🔊 Dengarkan Kisah (Suara Lembut Natural)",
                                color = if (isStorytelling) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Kotak Doa / Ayat Kemuliaan (Dilengkapi Tombol Suara Arab Fasih!)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✨ Doa / Ayat Kemuliaan Beliau:", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF92400E))
                                Surface(
                                    color = GoldAccent,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable {
                                        stopStoryAudio()
                                        ArabicWaqafPhoneticEngine.speakAuthenticArabic(
                                            arabicText = w.duaArabic,
                                            tts = ttsEngine,
                                            mediaPlayer = mediaPlayer
                                        )
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Dengar Doa", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = w.duaArabic,
                                fontSize = 25.sp,
                                lineHeight = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Isi Cerita (Menyorot Kalimat Demi Kalimat Tanpa Patah-Patah!)
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        if (isStorytelling && sentencesList.isNotEmpty()) {
                            Text(
                                text = "💡 Ketuk kalimat mana saja untuk langsung melompat ke bagian tersebut:",
                                fontSize = 11.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            sentencesList.forEachIndexed { idx, s ->
                                val active = (idx == activeSentenceIdx)
                                Surface(
                                    color = if (active) Color(0xFFFEF08A) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            IndonesianStorytellerEngine.startExpressiveStorytelling(
                                                tts = ttsEngine,
                                                sentences = sentencesList.drop(idx),
                                                style = selectedVoiceStyle,
                                                onSentenceHighlight = { subIdx -> activeSentenceIdx = idx + subIdx },
                                                onFinished = {
                                                    isStorytelling = false
                                                    activeSentenceIdx = -1
                                                }
                                            )
                                        }
                                ) {
                                    Text(
                                        text = s,
                                        fontSize = 15.sp,
                                        lineHeight = 24.sp,
                                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = if (active) EmeraldDark else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        } else {
                            Text(w.storyParagraphs, fontSize = 15.sp, lineHeight = 25.sp, color = Color(0xFF1E293B))
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
        "Pada suatu hari di kota Makkah, hiduplah seorang wanita bangsawan yang sangat mulia, jujur, dan dermawan bernama Siti Khadijah. Beliau adalah istri tercinta Rasulullah SAW dan manusia pertama di muka bumi yang beriman kepada ajaran Islam. Ketika wahyu pertama turun di Gua Hira dan Rasulullah pulang dalam keadaan gemetar, Khadijah dengan penuh kelembutan menyelimuti dan menenangkan hati beliau. Seluruh harta kekayaannya disedekahkan untuk membela dakwah dan menolong kaum fakir miskin. Karena ketulusannya yang luar biasa, Malaikat Jibril menyampaikan salam langsung dari Allah SWT untuk Siti Khadijah, serta memberi kabar gembira tentang sebuah istana indah dari mutiara di dalam Surga!",
        "Jadilah pribadi yang lembut tutur katanya, setia mendukung kebaikan, dan dermawan menolong sesama."
    ),
    NobleWomanStory(
        2, "Siti Hajar (Ibunda Nabi Ismail AS)", "هَاجَر عَلَيْهَا السَّلَامُ", "Simbol Tawakkal & Asal Mula Sa'i Air Zamzam", "💧",
        "إِذًا لَا يُضَيِّعُنَا اللَّهُ",
        "Dahulu kala, Nabi Ibrahim AS diperintahkan oleh Allah SWT untuk menempatkan Siti Hajar dan putranya yang masih bayi, Ismail, di lembah padang pasir Makkah yang sunyi. Dengan iman yang sangat teguh, Siti Hajar bertanya: Wahai suamiku, apakah Allah yang memerintahkan hal ini? Ketika Nabi Ibrahim mengangguk, Siti Hajar berkata dengan tenang: Jika demikian, pasti Allah tidak akan pernah menyia-nyiakan kami! Ketika persediaan air habis dan bayi Ismail menangis kehausan, Siti Hajar tidak berputus asa. Beliau berlari kecil tujuh kali antara bukit Shafa dan Marwah sambil berdoa, hingga Allah mengutus Malaikat Jibril dan memancarkan mata air Zamzam yang tidak pernah kering hingga hari ini!",
        "Keyakinan penuh kepada pertolongan Allah harus selalu disertai dengan usaha yang sungguh-sungguh dan pantang menyerah."
    ),
    NobleWomanStory(
        3, "Siti Asiyah binti Muzahim", "آسِيَة بِنْت مُزَاحِم", "Ratu Mesir yang Memilih Istana di Surga", "🏰",
        "رَبِّ ابْنِ لِي عِنْدَكَ بَيْتًا فِي الْجَنَّةِ",
        "Siti Asiyah adalah permaisuri kerajaan Mesir yang sangat lembut hatinya. Beliau adalah sosok ibu angkat yang menyelamatkan bayi Nabi Musa AS dari peti yang hanyut di sungai Nil. Meskipun hidup di tengah istana yang penuh kemewahan, hati Siti Asiyah tidak pernah silau dengan harta dunia dan hanya menyembah Allah Yang Maha Esa. Ketika Fir'aun memaksanya meninggalkan keimanan, Siti Asiyah tetap teguh mempertahankan tauhidnya sambil berdoa: Ya Tuhanku, bangunkanlah untukku sebuah rumah di sisi-Mu di dalam Surga! Allah pun mengabulkan doanya dan memperlihatkan istana indah miliknya di Surga.",
        "Keimanan, keteguhan hati, dan ketaatan kepada Allah jauh lebih berharga daripada segala kemewahan dunia."
    ),
    NobleWomanStory(
        4, "Maryam binti Imran", "مَرْيَم بِنْت عِمْرَان", "Wanita Terbaik Penjaga Kesucian & Kehormatan", "🕊️",
        "يَا مَرْيَمُ إِنَّ اللَّهَ اصْطَفَاكِ وَطَهَّرَكِ",
        "Maryam binti Imran adalah wanita suci yang namanya diabadikan menjadi nama sebuah surah di dalam Al-Quran. Sejak kecil, Maryam tumbuh dalam asuhan Nabi Zakariya AS di Baitul Maqdis. Beliau sangat rajin beribadah, menjaga rasa malu, dan membentengi kehormatan dirinya dari segala pergaulan yang buruk. Setiap kali Nabi Zakariya masuk ke mihrab tempat Maryam beribadah, beliau mendapati hidangan buah-buahan segar karunia dari Allah. Kemuliaan akhlak dan kesucian Maryam menjadikannya wanita teladan sepanjang masa, dan dari rahimnya lahir seorang Nabi mulia yaitu Nabi Isa AS.",
        "Menjaga kesucian diri, menutup aurat dengan sopan, dan rajin beribadah akan mengangkat kemuliaan seorang muslimah di sisi Allah."
    ),
    NobleWomanStory(
        5, "Fathimah Az-Zahra RA", "فَاطِمَة الزَّهْرَاء", "Putri Tercinta Rasulullah & Pemimpin Wanita Surga", "🌸",
        "فَاطِمَةُ بَضْعَةٌ مِنِّي",
        "Fathimah Az-Zahra RA adalah putri bungsu Rasulullah SAW yang paling mirip cara berjalan, cara berbicara, serta kelembutan akhlaknya dengan sang Ayah. Setiap kali Fathimah datang berkunjung, Rasulullah berdiri menyambutnya dengan penuh kasih sayang dan mencium keningnya. Meskipun beliau adalah putri seorang Nabi dan pemimpin umat, Fathimah hidup sangat sederhana, rajin bekerja membantu keluarganya, dan selalu mengutamakan sedekah kepada anak yatim serta orang miskin walaupun dirinya sendiri sedang berpuasa.",
        "Kesederhanaan, rasa malu yang tinggi, dan bakti tulus kepada orang tua adalah mahkota terindah anak sholehah."
    ),
    NobleWomanStory(
        6, "Aisyah binti Abu Bakar RA", "عَائِشَة بِنْت أَبِي بَكْر", "Ummul Mukminin & Guru Besar Ilmu Pengetahuan", "📚",
        "خُذُوا شَطْرَ دِينِكُمْ عَنْ هَذِهِ الْحُمَيْرَاءِ",
        "Sayyidatina Aisyah RA adalah istri Rasulullah SAW yang dikaruniai kecerdasan luar biasa dan ingatan yang sangat kuat. Beliau menghafal dan meriwayatkan lebih dari dua ribu dua ratus hadits Nabi, serta menjadi guru besar tempat bertanya para sahabat tentang tafsir Al-Quran, hukum fiqih, sastra, hingga ilmu pengobatan. Kisah hidup Aisyah RA mengajarkan kepada kita bahwa agama Islam sangat memuliakan perempuan yang gemar membaca, menuntut ilmu pengetahuan, dan menyebarkan kebaikan.",
        "Setiap anak muslim dan muslimah wajib bersemangat menuntut ilmu agama dan ilmu pengetahuan setinggi mungkin."
    )
)