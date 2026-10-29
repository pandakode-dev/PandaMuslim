package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =========================================================================
// MENU BARU 5: TANYA KAK PANDA (ASISTEN EDUKASI ISLAM INTERAKTIF ANAK)
// =========================================================================
data class PandaChatBubble(
    val isFromPanda: Boolean,
    val message: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TanyaKakPandaScreen(onBack: () -> Unit) {
    val pandaAvatar = rememberOptimizedIcon(R.drawable.ic_panda_home, 100)
    var userInput by remember { mutableStateOf("") }

    val chatHistory = remember {
        mutableStateListOf(
            PandaChatBubble(
                isFromPanda = true,
                message = "Assalamu'alaikum Sahabat Kecil! 🐼✨\nAku Kak Panda! Kamu mau tanya apa hari ini? Ketuk tombol pertanyaan cepat di bawah atau ketik pertanyaanmu ya! (Contoh: 'Kenapa harus sholat?', 'Doa mau makan', 'Siapa Nabi terakhir?')"
            )
        )
    }

    val quickTopics = remember {
        listOf(
            "Kenapa kita harus Sholat? 🕌",
            "Apa doa sebelum makan? 🍽️",
            "Bagaimana cara berbakti pada Ayah Bunda? ❤️",
            "Kenapa tidak boleh bohong? 🤥",
            "Apa keutamaan membaca Al-Quran? 📖",
            "Bagaimana adab sebelum tidur? 🌙"
        )
    }

    fun sendQuestionToPanda(question: String) {
        if (question.isBlank()) return
        chatHistory.add(PandaChatBubble(isFromPanda = false, message = question))
        val reply = generateKakPandaAnswer(question)
        chatHistory.add(PandaChatBubble(isFromPanda = true, message = reply))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            bitmap = pandaAvatar,
                            contentDescription = "Kak Panda",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("🤖 Tanya Kak Panda", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                            Text("Asisten Pintar Anak Muslim Sholeh 🌟", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            // Daftar Percakapan
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(chatHistory) { bubble ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (bubble.isFromPanda) Arrangement.Start else Arrangement.End
                    ) {
                        if (bubble.isFromPanda) {
                            Image(
                                bitmap = pandaAvatar,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, EmeraldPrimary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Surface(
                            color = if (bubble.isFromPanda) Color.White else EmeraldPrimary,
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (bubble.isFromPanda) 4.dp else 18.dp,
                                bottomEnd = if (bubble.isFromPanda) 18.dp else 4.dp
                            ),
                            shadowElevation = 2.dp,
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Text(
                                text = bubble.message,
                                fontSize = 13.sp,
                                color = if (bubble.isFromPanda) Color(0xFF1E293B) else Color.White,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }

            // Deretan Pertanyaan Cepat untuk Anak-Anak
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickTopics) { topic ->
                    Surface(
                        color = MintSurface,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .clickable { sendQuestionToPanda(topic) }
                    ) {
                        Text(
                            text = topic,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Kolom Ketik Pertanyaan
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        placeholder = { Text("Tulis pertanyaanmu ke Kak Panda...", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    )
                    FloatingActionButton(
                        onClick = {
                            if (userInput.isNotBlank()) {
                                sendQuestionToPanda(userInput)
                                userInput = ""
                            }
                        },
                        containerColor = EmeraldPrimary
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Color.White)
                    }
                }
            }
        }
    }
}

fun generateKakPandaAnswer(q: String): String {
    val low = q.lowercase()
    return when {
        low.contains("sholat") || low.contains("salat") ->
            "🐼 Masya Allah, pertanyaan hebat! Kita sholat 5 waktu sebagai tanda syukur kepada Allah yang telah memberi kita mata, kesehatan, dan keluarga yang menyayangi kita. Sholat juga menjadi kunci pembuka pintu Surga lho! 🕌✨"
        low.contains("makan") ->
            "🐼 Sebelum makan, ucapkan: 'Bismillaah' dan makanlah dengan tangan kanan ya! Jika lupa baca Bismillah di awal, bacalah: 'Bismillaahi awwalahu wa aakhirahu'. Jangan lupa habiskan makananmu agar berkah! 🍽️🌟"
        low.contains("ayah") || low.contains("bunda") || low.contains("ibu") || low.contains("orang tua") ->
            "🐼 Ridha Allah ada pada ridha Ayah dan Bunda! Cara berbaktinya: berbicara dengan suara lembut, segera membantu saat diminta tolong, dan mendoakan mereka setiap selesai sholat: 'Rabbighfirlii waliwaalidayya warhamhumaa kamaa rabbayaanii shaghiiraa'. ❤️"
        low.contains("bohong") || low.contains("jujur") ->
            "🐼 Anak sholeh selalu berkata jujur seperti Rasulullah SAW yang diberi gelar 'Al-Amin' (Yang Dapat Dipercaya). Kejujuran menuntun kita pada kebaikan, dan kebaikan menuntun kita menuju Surga! 🌟"
        low.contains("quran") || low.contains("ngaji") || low.contains("iqro") ->
            "🐼 Setiap 1 huruf Al-Quran yang kamu baca bernilai 10 kebaikan lho! Apalagi kalau kamu rajin latihan di menu Iqro dan Al-Quran Tajwid Warna di aplikasi PandaMuslim ini! 📖🌈"
        low.contains("tidur") ->
            "🐼 Adab sebelum tidur ala Rasulullah SAW: Berwudhu dulu, kibaskan tempat tidur 3 kali sambil baca Bismillah, tidur miring ke kanan, lalu baca doa: 'Bismika Allaahumma ahyaa wa bismika amuut' serta Ayat Kursi! 🌙"
        low.contains("nabi") || low.contains("rasul") ->
            "🐼 Jumlah Nabi dan Rasul yang wajib kita imani ada 25, dimulai dari Nabi Adam AS hingga Nabi penutup kita, Nabi Muhammad SAW. Kamu bisa mendengarkan dongeng suara mereka di menu 'Kisah 25 Nabi' ya! 📖"
        else ->
            "🐼 Subhanallah, Kak Panda senang sekali melihat semangat belajarmu! Teruslah rajin sholat 5 waktu, mengaji Al-Quran & Iqro, serta berbuat baik kepada sesama ya Sahabat Kecil! 🌟🤲"
    }
}

// =========================================================================
// MENU BARU 6: RUANG PIALA & SERTIFIKAT PRESTASI DIGITAL ANAK SHOLEH
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PialaSertifikatScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("panda_auth_prefs", Context.MODE_PRIVATE) }
    val khatamPrefs = remember { context.getSharedPreferences("panda_khatam_prefs", Context.MODE_PRIVATE) }
    val gamePrefs = remember { context.getSharedPreferences("panda_game_prefs", Context.MODE_PRIVATE) }

    var childName by remember {
        mutableStateOf(authPrefs.getString("user_name", "Ananda Sholeh / Sholehah") ?: "Ananda Sholeh")
    }
    var selectedAwardTitle by remember { mutableStateOf("Bintang Teladan Belajar Iqro 1–6") }

    val juzCompleted = remember { khatamPrefs.getInt("juz_selesai", 1) }
    val quizStars = remember { gamePrefs.getInt("bintang_kuis", 25) }

    val awardChoices = listOf(
        "Bintang Teladan Belajar Iqro 1–6 📗",
        "Penghafal Cilik Juz 'Amma & Al-Quran 📖",
        "Anak Sholeh Rajin Sholat 5 Waktu 🕌",
        "Juara Cerdas Cermat Islami PandaMuslim 🏆"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🏆 Piala & Sertifikat Prestasi", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                        Text("Cetak Piagam Digital & Bagikan ke Ayah Bunda!", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBg)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Papan Statistik Lencana Piala
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏅", fontSize = 30.sp)
                            Text("$juzCompleted Juz", fontWeight = FontWeight.Black, fontSize = 18.sp, color = EmeraldDark)
                            Text("Capaian Tilawah", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⭐", fontSize = 30.sp)
                            Text("$quizStars Poin", fontWeight = FontWeight.Black, fontSize = 18.sp, color = EmeraldDark)
                            Text("Bintang Kuis", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // 2. Kustomisasi Nama Anak & Jenis Penghargaan pada Sertifikat
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("✏️ Tulis Nama pada Sertifikat:", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = EmeraldDark)
                        OutlinedTextField(
                            value = childName,
                            onValueChange = { childName = it },
                            label = { Text("Nama Lengkap Anak / Jamaah") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Pilih Gelar Piagam Penghargaan:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldDark)
                        awardChoices.forEach { award ->
                            val active = selectedAwardTitle == award
                            Surface(
                                color = if (active) MintSurface else OffWhiteBg,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (active) 2.dp else 1.dp,
                                        color = if (active) EmeraldPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedAwardTitle = award }
                            ) {
                                Text(
                                    text = if (active) "✅ $award" else award,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. KARTU SERTIFIKAT EMAS DIGITAL (SIAP SCREENSHOT / SHARE)
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(EmeraldDark, EmeraldPrimary, EmeraldDark)
                                )
                            )
                            .border(4.dp, GoldAccent, RoundedCornerShape(28.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", fontSize = 22.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                            Text(
                                text = "🏆 SERTIFIKAT PENGHARGAAN PANDAMUSLIM 🏆",
                                color = Color(0xFF0F172A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Diberikan dengan bangga kepada:", color = MintSurface, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = childName,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Atas pencapaian istimewa sebagai:", color = MintSurface, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedAwardTitle,
                            color = GoldAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "\"Semoga Allah SWT senantiasa memberkahi ilmunya, menjadikannya anak sholeh penyejuk hati orang tua, dan mengangkat derajatnya di dunia maupun akhirat. Aamiin.\"",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val shareText = "Alhamdulillah! 🏆 *$childName* telah meraih piagam penghargaan *$selectedAwardTitle* di Aplikasi PandaMuslim! 🌟🐼\n\nYuk belajar mengaji Al-Quran Tajwid Warna & Iqro 1–6 Bersuara di: https://pandamuslim.pandakode-dev.my.id"
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Prestasi ke WhatsApp"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bagikan Prestasi ke WhatsApp Ayah Bunda 📲", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}