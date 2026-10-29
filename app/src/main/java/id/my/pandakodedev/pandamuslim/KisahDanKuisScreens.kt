package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// =========================================================================
// MENU BARU 1: KISAH 25 NABI & RASUL (AUDIO NARASI OTOMATIS + DATA SERVER)
// =========================================================================
data class KisahNabiItem(
val number: Int,
val name: String,
val arabicName: String,
val titleBadge: String,
val story: String,
val hikmah: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KisahNabiScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedNabi by remember { mutableStateOf<KisahNabiItem?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    val builtInKisah = remember { get25KisahNabiList() }
    var serverKisah by remember { mutableStateOf<List<KisahNabiItem>>(emptyList()) }

    // Tarik tambahan kisah nabi jika Admin menambahkannya dari Web Admin (kategori = 'kisah')
    LaunchedEffect(Unit) {
        val fromServer = fetchContentsFromServer("kisah")
        if (fromServer.isNotEmpty()) {
            serverKisah = fromServer.mapIndexed { idx, c ->
                KisahNabiItem(
                    number = 26 + idx,
                    name = c.title,
                    arabicName = c.arabicText.ifBlank { "عَلَيْهِ السَّلَامُ" },
                    titleBadge = c.latinText.ifBlank { "Kisah Teladan" },
                    story = c.description,
                    hikmah = "Kisah tambahan dari Server Admin PandaMuslim."
                )
            }
        }
    }

    val allKisah = remember(builtInKisah, serverKisah) { serverKisah + builtInKisah }

    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("id", "ID")
                tts?.setPitch(1.05f)
                tts?.setSpeechRate(0.88f)
            }
        }
        ttsEngine = tts
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun toggleReadStory(item: KisahNabiItem) {
        if (isSpeaking) {
            ttsEngine?.stop()
            isSpeaking = false
        } else {
            isSpeaking = true
            val fullNarration = "Kisah ${item.name}. Julukan beliau adalah ${item.titleBadge}. ${item.story}. Hikmah kisah ini: ${item.hikmah}"
            ttsEngine?.speak(fullNarration, TextToSpeech.QUEUE_FLUSH, null, "kisah_narasi")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("📖 Kisah 25 Nabi & Rasul", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                        Text("Dilengkapi Audio Dongeng Sebelum Tidur 🔊", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama Nabi (contoh: Ibrahim, Musa, Muhammad)...", fontSize = 13.sp) },
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

            val filtered = remember(allKisah, searchQuery) {
                if (searchQuery.isBlank()) allKisah
                else allKisah.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.titleBadge.contains(searchQuery, ignoreCase = true) ||
                            it.story.contains(searchQuery, ignoreCase = true)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.number }) { item ->
                    val isExpanded = selectedNabi?.number == item.number
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isExpanded) 2.dp else 1.dp,
                                color = if (isExpanded) EmeraldPrimary else MintSurface,
                                shape = RoundedCornerShape(22.dp)
                            )
                            .clickable {
                                ttsEngine?.stop()
                                isSpeaking = false
                                selectedNabi = if (isExpanded) null else item
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MintSurface)
                                    ) {
                                        Text("${item.number}", fontWeight = FontWeight.Black, color = EmeraldDark, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(item.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EmeraldDark)
                                        Text(item.titleBadge, fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(item.arabicName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                    HorizontalDivider(color = MintSurface)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { toggleReadStory(item) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSpeaking) Color(0xFFE11D48) else GoldAccent
                                        ),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Default.Close else Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = if (isSpeaking) Color.White else Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isSpeaking) "Hentikan Narasi Suara" else "🔊 Dengarkan Audio Dongeng Kisah Ini",
                                            color = if (isSpeaking) Color.White else Color(0xFF0F172A),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = item.story,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B),
                                        lineHeight = 22.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        color = Color(0xFFFEF9C3),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("🌟 Hikmah & Teladan untuk Anak Sholeh:", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF92400E))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(item.hikmah, fontSize = 12.sp, color = Color(0xFF78350F))
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

// =========================================================================
// MENU BARU 2: GAME KUIS CERDAS CERMAT ISLAMI (GAMIFIKASI POIN BINTANG)
// =========================================================================
data class IslamicGameQuestion(
    val category: String,
    val arabicClue: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuisIslamiGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("panda_game_prefs", Context.MODE_PRIVATE) }

    var totalStars by remember { mutableStateOf(prefs.getInt("bintang_kuis", 0)) }
    val questions = remember { getIslamicGameQuestions().shuffled() }

    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var sessionScore by remember { mutableStateOf(0) }

    val q = questions[currentIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎮 Cerdas Cermat Islami", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    Surface(
                        color = GoldAccent,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "⭐ $totalStars Bintang",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Level & Nomor Soal
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
                        Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                            Text(
                                text = q.category,
                                color = Color(0xFF0F172A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tantangan Soal ${currentIndex + 1} dari ${questions.size}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = "Skor Sesi: $sessionScore",
                        color = GoldAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }

            // Kartu Pertanyaan
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (q.arabicClue.isNotBlank()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFFBEB))
                                .border(1.5.dp, GoldAccent, RoundedCornerShape(16.dp))
                                .padding(vertical = 14.dp, horizontal = 10.dp)
                        ) {
                            Text(
                                text = q.arabicClue,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldDark,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text(
                        text = q.question,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Pilihan Jawaban
            q.options.forEachIndexed { idx, opt ->
                val isChosen = selectedOption == idx
                val isRight = idx == q.correctIndex

                val bg = when {
                    selectedOption == null -> Color.White
                    isRight -> Color(0xFFD1FAE5)
                    isChosen -> Color(0xFFFFE4E6)
                    else -> Color.White
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = bg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = if (selectedOption != null && isRight) Color(0xFF059669) else MintSurface,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable(enabled = selectedOption == null) {
                            selectedOption = idx
                            if (idx == q.correctIndex) {
                                sessionScore += 10
                                totalStars += 5
                                prefs.edit().putInt("bintang_kuis", totalStars).apply()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(opt, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = EmeraldDark, modifier = Modifier.weight(1f))
                        if (selectedOption != null && isRight) {
                            Text("✅ +5 ⭐", fontWeight = FontWeight.Black, color = Color(0xFF059669), fontSize = 13.sp)
                        } else if (isChosen && !isRight) {
                            Text("❌ Salah", fontWeight = FontWeight.Black, color = Color(0xFFE11D48), fontSize = 13.sp)
                        }
                    }
                }
            }

            if (selectedOption != null) {
                Surface(
                    color = MintSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 Penjelasan: ${q.explanation}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Button(
                    onClick = {
                        selectedOption = null
                        currentIndex = (currentIndex + 1) % questions.size
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lanjut Soal Berikutnya ➔", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

fun get25KisahNabiList(): List<KisahNabiItem> = listOf(
    KisahNabiItem(1, "Nabi Adam AS", "آدَم", "Manusia & Nabi Pertama", "Nabi Adam AS adalah manusia pertama yang diciptakan Allah SWT dari tanah. Beliau diajarkan seluruh nama benda di alam semesta dan ditempatkan di Surga bersama Siti Hawa sebelum diturunkan ke Bumi sebagai khalifah.", "Jika berbuat salah, segera bertaubat dan memohon ampun kepada Allah seperti doa Nabi Adam."),
    KisahNabiItem(2, "Nabi Idris AS", "إِدْرِيس", "Pelopor Ilmu Menulis & Berhitung", "Nabi Idris AS dikenal sangat cerdas, rajin beribadah, dan merupakan manusia pertama yang pandai menulis dengan pena, membaca rasi bintang, serta menjahit pakaian.", "Rajin belajar membaca, menulis, dan berhitung adalah sifat terpuji para Nabi."),
    KisahNabiItem(3, "Nabi Nuh AS", "نُوح", "Pembuat Bahtera Kapal Raksasa", "Nabi Nuh AS berdakwah selama 950 tahun dengan penuh kesabaran. Atas perintah Allah, beliau membangun kapal besar di atas bukit untuk menyelamatkan orang beriman dan sepasang hewan dari banjir besar.", "Jangan pernah lelah berbuat baik dan tetap sabar meskipun diejek."),
    KisahNabiItem(4, "Nabi Hud AS", "هُود", "Diutus kepada Kaum 'Ad", "Nabi Hud AS mengajak kaum 'Ad yang bertubuh besar dan pandai membangun gedung tinggi agar bersyukur kepada Allah dan tidak sombong.", "Kehebatan dan kekuatan tubuh harus digunakan untuk bersyukur, bukan menyombongkan diri."),
    KisahNabiItem(5, "Nabi Shalih AS", "صَالِح", "Mukjizat Unta Betina dari Batu", "Nabi Shalih AS diutus kepada kaum Tsamud yang pandai memahat gunung batu. Mukjizat beliau adalah keluarnya unta betina yang menghasilkan susu berlimpah dari sebongkah batu karang.", "Sayangi binatang ciptaan Allah dan jangan merusak alam."),
    KisahNabiItem(6, "Nabi Ibrahim AS", "إِبْرَاهِيم", "Khalilullah (Kekasih Allah)", "Nabi Ibrahim AS sangat pemberani mencari kebenaran. Ketika dibakar oleh Raja Namrud, Allah memerintahkan api menjadi dingin dan menyelamatkan beliau. Beliau juga membangun Ka'bah bersama putranya, Nabi Ismail AS.", "Keyakinan penuh kepada Allah membuat hati kita berani menghadapi rintangan."),
    KisahNabiItem(7, "Nabi Luth AS", "لُوط", "Penyeru Akhlak Mulia", "Nabi Luth AS adalah keponakan Nabi Ibrahim AS yang berdakwah mengajak kaumnya menjaga kesucian diri dan menghormati tamu.", "Jaga adab, kesopanan, dan muliakan tamu yang datang ke rumah."),
    KisahNabiItem(8, "Nabi Ismail AS", "إِسْمَاعِيل", "Anak Sholeh yang Sangat Sabar", "Sejak bayi, dari hentakan kaki Nabi Ismail AS keluar mata air Zamzam di Makkah. Beliau sangat patuh kepada orang tua dan ikhlas menjalankan perintah Allah hingga menjadi asal mula ibadah Qurban.", "Anak sholeh selalu patuh, sopan, dan mendukung orang tua dalam kebaikan."),
    KisahNabiItem(9, "Nabi Ishaq AS", "إِسْحَاق", "Putra Nabi Ibrahim & Siti Sarah", "Nabi Ishaq AS adalah nabi yang santun dan penuh berkah, yang menurunkan nabi-nabi Bani Israil termasuk Nabi Ya'qub AS.", "Kasih sayang dalam keluarga membawa keberkahan turun-temurun."),
    KisahNabiItem(10, "Nabi Ya'qub AS", "يَعْقُوب", "Ayah yang Penuh Kasih Sayang", "Nabi Ya'qub AS mendidik 12 putranya agar selalu rukun dan berpegang teguh pada tauhid hingga akhir hayat.", "Sayangi saudara kandung, kakak, dan adik tanpa rasa iri."),
    KisahNabiItem(11, "Nabi Yusuf AS", "يُوسُف", "Pemaaf & Ahli Tafsir Mimpi", "Meski pernah dimasukkan ke dalam sumur oleh saudara-saudaranya, Nabi Yusuf AS tumbuh menjadi pemimpin besar Mesir yang jujur dan memaafkan seluruh saudaranya dengan tulus.", "Memaafkan kesalahan orang lain membuat derajat kita ditinggikan oleh Allah."),
    KisahNabiItem(12, "Nabi Ayyub AS", "أَيُّوب", "Teladan Kesabaran Luar Biasa", "Nabi Ayyub AS diuji dengan sakit bertahun-tahun dan kehilangan harta, namun lisan beliau tidak pernah berhenti berdzikir memuji Allah hingga disembuhkan kembali.", "Selalu bersyukur saat sehat dan tetap bersabar ketika sedang sakit."),
    KisahNabiItem(13, "Nabi Syu'aib AS", "شُعَيْب", "Penegak Kejujuran Berdagang", "Nabi Syu'aib AS mengajak penduduk Madyan agar jujur dalam menimbang barang dagangan dan tidak merugikan orang lain.", "Jujur dalam jual beli dan tidak curang adalah ciri orang beriman."),
    KisahNabiItem(14, "Nabi Musa AS", "مُوسَى", "Kalimullah & Mukjizat Tongkat", "Nabi Musa AS menghadapi Fir'aun dengan berani. Atas izin Allah, tongkat beliau dapat membelah Laut Merah untuk menyelamatkan Bani Israil, dan beliau menerima kitab Taurat.", "Berdoa 'Robbisrohli sodri' agar hati lapang dan dimudahkan berbicara yang baik."),
    KisahNabiItem(15, "Nabi Harun AS", "هَارُون", "Saudara Setia yang Fasih Bicara", "Nabi Harun AS mendampingi Nabi Musa AS berdakwah dengan tutur kata yang lembut dan santun.", "Bekerja sama dengan saudara dalam kebaikan membuat tugas berat menjadi ringan."),
    KisahNabiItem(16, "Nabi Dzulkifli AS", "ذُو الْكِفْل", "Pemimpin yang Menepati Janji", "Nabi Dzulkifli AS terkenal karena sanggup menahan amarah, rajin berpuasa di siang hari, sholat malam, dan selalu menepati janjinya.", "Jika berjanji wajib ditepati dan belajarlah menahan marah."),
    KisahNabiItem(17, "Nabi Daud AS", "دَاوُد", "Bersuara Merdu & Melunakkan Besi", "Nabi Daud AS menerima kitab Zabur. Suara dzikir beliau sangat merdu hingga burung dan gunung ikut bertasbih, serta tangan beliau dapat melunakkan besi dengan izin Allah.", "Gunakan suara kita untuk melantunkan Al-Quran dan dzikir yang indah."),
    KisahNabiItem(18, "Nabi Sulaiman AS", "سُلَيْمَان", "Raja Bijaksana yang Mengerti Bahasa Hewan", "Nabi Sulaiman AS diberi kerajaan terbesar, mampu berbicara dengan semut dan burung Hud-hud, serta menundukkan angin, namun beliau tetap rendah hati dan rajin bersyukur.", "Setinggi apa pun prestasi kita, ingatlah semua itu karunia dari Allah."),
    KisahNabiItem(19, "Nabi Ilyas AS", "إِلْيَاس", "Penyeru Tauhid yang Teguh", "Nabi Ilyas AS mengingatkan kaumnya agar hanya menyembah Allah SWT yang menurunkan hujan dan menumbuhkan tanaman.", "Hanya kepada Allah kita berdoa dan meminta pertolongan."),
    KisahNabiItem(20, "Nabi Ilyasa' AS", "الْيَسَع", "Penerus Dakwah Nabi Ilyas", "Nabi Ilyasa' AS melanjutkan perjuangan gurunya, Nabi Ilyas AS, untuk menjaga keimanan dan kedamaian masyarakatnya.", "Hormati guru yang mengajarkan ilmu agama kepada kita."),
    KisahNabiItem(21, "Nabi Yunus AS", "يُونُس", "Berdzikir di Dalam Perut Ikan Paus", "Ketika berada di dalam kegelapan perut ikan Nun (Paus), Nabi Yunus AS membaca doa: 'Laa ilaaha illaa Anta Subhaanaka innii kuntu minazh-zhaalimiin', lalu Allah menyelamatkan beliau.", "Jangan cepat menyerah dan perbanyak dzikir Nabi Yunus saat mengalami kesulitan."),
    KisahNabiItem(22, "Nabi Zakariya AS", "زَكَرِيَّا", "Berdoa Tanpa Putus Asa", "Meski sudah berusia lanjut dan rambut telah memutih, Nabi Zakariya AS terus berdoa dengan lembut hingga Allah mengaruniakan putra yang sholeh, yaitu Nabi Yahya AS.", "Allah Maha Mendengar doa hamba-Nya yang tidak pernah putus asa."),
    KisahNabiItem(23, "Nabi Yahya AS", "يَحْيَى", "Pemuda Sholeh yang Lembut Hati", "Sejak kecil Nabi Yahya AS sangat tekun mempelajari kitab suci, berbakti kepada kedua orang tua, dan menyayangi sesama makhluk.", "Cintai Al-Quran dan ilmu agama sejak usia anak-anak."),
    KisahNabiItem(24, "Nabi Isa AS", "عِيسَى", "Penerima Kitab Injil & Mukjizat Mulia", "Nabi Isa AS dapat berbicara sejak bayi untuk membela kesucian ibundanya, Siti Maryam. Atas izin Allah, beliau dapat menyembuhkan orang buta dan menerima kitab Injil.", "Hormati dan muliakan seorang Ibu sebagaimana bakti Nabi Isa kepada ibundanya."),
    KisahNabiItem(25, "Nabi Muhammad SAW", "مُحَمَّد ﷺ", "Khatamul Anbiya & Rahmatan lil 'Alamin", "Nabi Muhammad SAW bergelar Al-Amin (Yang Dapat Dipercaya). Beliau menerima mukjizat terbesar sepanjang masa yaitu Al-Quran Al-Karim dan menjadi teladan akhlak paling sempurna bagi seluruh umat manusia.", "Perbanyak sholawat dan tirulah kejujuran serta kasih sayang Rasulullah SAW setiap hari.")
)

fun getIslamicGameQuestions(): List<IslamicGameQuestion> = listOf(
    IslamicGameQuestion("Tebak Asmaul Husna", "الرَّزَّاقُ", "Apakah arti dari Asmaul Husna 'Ar-Razzaq' di atas?", listOf("Yang Maha Pemberi Rezeki", "Yang Maha Pencipta", "Yang Maha Melihat"), 0, "Ar-Razzaq artinya Allah Yang Maha Pemberi Rezeki kepada seluruh makhluk-Nya."),
    IslamicGameQuestion("Sambung Ayat Juz Amma", "قُلْ هُوَ اللَّهُ ...", "Kata apakah yang tepat untuk melengkapi akhir ayat pertama Surah Al-Ikhlas di atas?", listOf("أَحَدٌ (Ahad)", "الصَّمَدُ (Ash-Shamad)", "الْفَلَقِ (Al-Falaq)"), 0, "Bunyi lengkap ayat pertama Surah Al-Ikhlas adalah: Qul huwallaahu Ahad."),
    IslamicGameQuestion("Kisah 25 Nabi", "نُوح عَلَيْهِ السَّلَامُ", "Siapakah Nabi yang diperintahkan Allah SWT membuat kapal besar di atas bukit?", listOf("Nabi Musa AS", "Nabi Nuh AS", "Nabi Ibrahim AS"), 1, "Nabi Nuh AS membuat bahtera kapal raksasa untuk menyelamatkan umat beriman dari banjir besar."),
    IslamicGameQuestion("Ilmu Tajwid", "إِنَّ  —  عَمَّ", "Huruf Nun dan Mim yang memiliki tanda Tasydid ( ّ ) wajib dibaca bagaimana?", listOf("Dengung ditahan 2 harakat (Ghunnah)", "Dipantulkan (Qalqalah)", "Dibaca cepat tanpa dengung"), 0, "Nun dan Mim bertasydid disebut Ghunnah Musyaddadah, wajib didengungkan 2 ketukan."),
    IslamicGameQuestion("Rukun Islam", "الصَّلَاة", "Berapa jumlah total rakaat Sholat Wajib 5 waktu dalam sehari semalam?", listOf("17 Rakaat", "15 Rakaat", "20 Rakaat"), 0, "Subuh 2, Dzuhur 4, Ashar 4, Maghrib 3, dan Isya 4 = Total 17 Rakaat."),
    IslamicGameQuestion("Tebak Asmaul Husna", "الْعَلِيمُ", "Asmaul Husna 'Al-'Alim' memiliki arti bahwa Allah...", listOf("Maha Mengetahui segala sesuatu", "Maha Mematikan", "Maha Menyempitkan"), 0, "Al-'Alim artinya Allah Maha Mengetahui baik yang tampak maupun yang tersembunyi.")
)