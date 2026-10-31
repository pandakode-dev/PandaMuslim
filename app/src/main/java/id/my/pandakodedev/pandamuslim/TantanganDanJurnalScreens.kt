package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyChallengeQuest(
    val id: Int,
    val category: String,
    val emoji: String,
    val title: String,
    val description: String,
    val rewardStars: Int,
    val dalilShort: String
)

data class GratitudeJournalEntry(
    val id: Long,
    val dateStr: String,
    val syukur1: String,
    val syukur2: String,
    val syukur3: String,
    val personalPrayer: String
)

// =========================================================================
// FITUR 1: TANTANGAN KEBAIKAN HARIAN (DAILY GOOD DEEDS GAMIFICATION)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TantanganKebaikanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("panda_good_deeds_prefs", Context.MODE_PRIVATE) }
    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val allQuests = remember { getDailyGoodDeedsQuests() }
    var totalStars by remember { mutableStateOf(prefs.getInt("deed_total_stars", 0)) }

    val completedIdsToday = remember {
        val savedSet = prefs.getStringSet("done_$todayKey", emptySet()) ?: emptySet()
        mutableStateListOf<Int>().apply {
            savedSet.mapNotNull { it.toIntOrNull() }.forEach { add(it) }
        }
    }

    val currentRankBadge = remember(totalStars) {
        when {
            totalStars >= 200 -> "👑 Duta Akhlak Mulia"
            totalStars >= 100 -> "💎 Pahlawan Kebaikan"
            totalStars >= 50 -> "🌟 Sahabat Rasulullah"
            else -> "🌱 Tunas Anak Sholeh"
        }
    }

    fun toggleQuestCompletion(quest: DailyChallengeQuest) {
        if (completedIdsToday.contains(quest.id)) {
            completedIdsToday.remove(quest.id)
            totalStars = (totalStars - quest.rewardStars).coerceAtLeast(0)
        } else {
            completedIdsToday.add(quest.id)
            totalStars += quest.rewardStars
            Toast.makeText(context, "Alhamdulillah! +${quest.rewardStars} Bintang Kebaikan 🌟", Toast.LENGTH_SHORT).show()
        }
        prefs.edit()
            .putInt("deed_total_stars", totalStars)
            .putStringSet("done_$todayKey", completedIdsToday.map { it.toString() }.toSet())
            .apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🌟 Tantangan Kebaikan Harian", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                        Text("Gelar Kamu: $currentRankBadge", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBg)
                .padding(padding)
        ) {
            // Kartu Skor & Progress Hari Ini
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, GoldAccent, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                                    Text(
                                        text = currentRankBadge,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Misi Selesai Hari Ini: ${completedIdsToday.size}/${allQuests.size}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Ayo kumpulkan bintang pahala dengan berbuat baik setiap hari!",
                                    color = MintSurface,
                                    fontSize = 11.sp
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text("⭐", fontSize = 24.sp)
                                    Text("$totalStars", color = GoldAccent, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                    Text("Bintang", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            items(allQuests, key = { it.id }) { quest ->
                val isDone = completedIdsToday.contains(quest.id)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) Color(0xFFD1FAE5) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isDone) 2.dp else 1.dp,
                            color = if (isDone) Color(0xFF059669) else MintSurface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { toggleQuestCompletion(quest) }
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
                                .background(if (isDone) Color(0xFF059669) else Color(0xFFFEF3C7))
                        ) {
                            Text(if (isDone) "✅" else quest.emoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = quest.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary
                                )
                                Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                                    Text(
                                        text = "+${quest.rewardStars} ⭐",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = quest.title,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = EmeraldDark
                            )
                            Text(
                                text = quest.description,
                                fontSize = 12.sp,
                                color = Color(0xFF334155),
                                lineHeight = 17.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📜 ${quest.dalilShort}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getDailyGoodDeedsQuests(): List<DailyChallengeQuest> = listOf(
    DailyChallengeQuest(1, "Birrul Walidain (Bakti Orang Tua)", "🤲", "Cium Tangan & Doakan Ayah Bunda", "Mencium tangan Ayah & Bunda serta mendoakan ampunan bagi keduanya hari ini.", 15, "Ridha Allah terletak pada ridha orang tua. (HR. Tirmidzi)"),
    DailyChallengeQuest(2, "Bantu Pekerjaan Rumah", "🧹", "Merapikan Tempat Tidur & Meja Makan", "Membantu Bunda merapikan kamar tidur sendiri dan membereskan piring setelah makan.", 10, "Kebersihan itu adalah sebagian dari iman. (HR. Muslim)"),
    DailyChallengeQuest(3, "Sedekah & Kasih Sayang", "🐱", "Memberi Makan Kucing / Sedekah Subuh", "Memberi makan hewan di sekitar rumah atau memasukkan uang ke kotak infaq.", 15, "Pada setiap makhluk bernyawa yang diberi minum/makan terdapat pahala. (HR. Bukhari)"),
    DailyChallengeQuest(4, "Penjaga Lisan & Adab", "😊", "Tersenyum & Tidak Berkata Kasar Seharian", "Menjaga lisan dan ketikan jari di HP dari kata-kata buruk serta menyapa teman dengan senyum.", 10, "Senyummu di hadapan saudaramu adalah sedekah. (HR. Tirmidzi)"),
    DailyChallengeQuest(5, "Disiplin Waktu Sholat", "🕌", "Langsung Berwudhu Saat Adzan Berkumandang", "Segera meletakkan HP/mainan begitu mendengar suara Adzan dan sholat tepat waktu.", 20, "Amalan yang paling dicintai Allah adalah sholat tepat pada waktunya. (HR. Bukhari)"),
    DailyChallengeQuest(6, "Cinta Al-Quran & Iqro", "📖", "Mengaji Minimal 1 Halaman Hari Ini", "Membaca Al-Quran atau latihan Iqro minimal 1 halaman dengan tartil.", 15, "Satu huruf Al-Quran dilipatgandakan menjadi 10 kebaikan. (HR. Tirmidzi)"),
    DailyChallengeQuest(7, "Benteng Kehormatan Diri", "🛡️", "Menjaga Pandangan & Menutup Aurat Rapi", "Berpakaian sopan menutup aurat saat keluar rumah dan menjaga mata dari tontonan buruk.", 15, "Rasa malu itu adalah bagian dari keimanan. (HR. Bukhari & Muslim)")
)

// =========================================================================
// FITUR 4: JURNAL SYUKUR & DOA ANAK (ISLAMIC GRATITUDE JOURNAL)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JurnalSyukurScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("panda_gratitude_prefs", Context.MODE_PRIVATE) }

    var syukur1 by remember { mutableStateOf("") }
    var syukur2 by remember { mutableStateOf("") }
    var syukur3 by remember { mutableStateOf("") }
    var doaPribadi by remember { mutableStateOf("") }

    var journalEntries by remember {
        mutableStateOf(loadGratitudeEntries(prefs.getString("journal_json", "[]") ?: "[]"))
    }

    fun saveNewEntry() {
        if (syukur1.isBlank() && doaPribadi.isBlank()) {
            Toast.makeText(context, "Tuliskan minimal 1 rasa syukurmu hari ini ya!", Toast.LENGTH_SHORT).show()
            return
        }
        val dateStr = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID")).format(Date())
        val newEntry = GratitudeJournalEntry(
            id = System.currentTimeMillis(),
            dateStr = dateStr,
            syukur1 = syukur1.trim(),
            syukur2 = syukur2.trim(),
            syukur3 = syukur3.trim(),
            personalPrayer = doaPribadi.trim()
        )
        val updated = listOf(newEntry) + journalEntries
        journalEntries = updated
        prefs.edit().putString("journal_json", encodeGratitudeEntries(updated)).apply()

        syukur1 = ""
        syukur2 = ""
        syukur3 = ""
        doaPribadi = ""
        Toast.makeText(context, "Alhamdulillah! Jurnal Syukur & Doamu berhasil disimpan 📝", Toast.LENGTH_SHORT).show()
    }

    fun deleteEntry(id: Long) {
        val updated = journalEntries.filterNot { it.id == id }
        journalEntries = updated
        prefs.edit().putString("journal_json", encodeGratitudeEntries(updated)).apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("📝 Jurnal Syukur & Doa Anak", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                        Text("लئِنْ شَكَرْتُمْ لَأَزِيدَنَّكُمْ • Bersyukur Menambah Nikmat", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBg)
                .padding(padding)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🌈 3 Hal yang Aku Syukuri Hari Ini (Alhamdulillah):",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = EmeraldDark
                        )

                        OutlinedTextField(
                            value = syukur1,
                            onValueChange = { syukur1 = it },
                            label = { Text("1. Alhamdulillah hari ini aku bersyukur karena...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = syukur2,
                            onValueChange = { syukur2 = it },
                            label = { Text("2. Nikmat kebaikan kedua yang aku rasakan...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = syukur3,
                            onValueChange = { syukur3 = it },
                            label = { Text("3. Orang baik yang ingin aku doakan hari ini...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🤲 Doa & Harapan Pribadiku Kepada Allah SWT:",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = EmeraldDark
                        )
                        OutlinedTextField(
                            value = doaPribadi,
                            onValueChange = { doaPribadi = it },
                            label = { Text("Ya Allah Yang Maha Pengasih, kabulkanlah harapanku...") },
                            minLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { saveNewEntry() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan ke Buku Harian Syukurku ✨", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            if (journalEntries.isNotEmpty()) {
                item {
                    Text(
                        text = "📖 Riwayat Catatan Syukur & Doaku (${journalEntries.size}):",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = EmeraldDark
                    )
                }

                items(journalEntries, key = { it.id }) { entry ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, GoldAccent, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(color = EmeraldDark, shape = RoundedCornerShape(50)) {
                                    Text(
                                        text = "🗓️ ${entry.dateStr}",
                                        color = GoldAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { deleteEntry(entry.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFE11D48), modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (entry.syukur1.isNotBlank()) Text("✨ 1. ${entry.syukur1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                            if (entry.syukur2.isNotBlank()) Text("✨ 2. ${entry.syukur2}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                            if (entry.syukur3.isNotBlank()) Text("✨ 3. ${entry.syukur3}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                            if (entry.personalPrayer.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(color = Color.White, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "🤲 Doaku: \"${entry.personalPrayer}\"",
                                        fontSize = 13.sp,
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(10.dp)
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

private fun loadGratitudeEntries(jsonStr: String): List<GratitudeJournalEntry> {
    val list = mutableListOf<GratitudeJournalEntry>()
    try {
        val arr = JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                GratitudeJournalEntry(
                    id = o.optLong("id", System.currentTimeMillis()),
                    dateStr = o.optString("dateStr", ""),
                    syukur1 = o.optString("s1", ""),
                    syukur2 = o.optString("s2", ""),
                    syukur3 = o.optString("s3", ""),
                    personalPrayer = o.optString("prayer", "")
                )
            )
        }
    } catch (_: Exception) {}
    return list
}

private fun encodeGratitudeEntries(list: List<GratitudeJournalEntry>): String {
    val arr = JSONArray()
    list.forEach { item ->
        arr.put(
            JSONObject().apply {
                put("id", item.id)
                put("dateStr", item.dateStr)
                put("s1", item.syukur1)
                put("s2", item.syukur2)
                put("s3", item.syukur3)
                put("prayer", item.personalPrayer)
            }
        )
    }
    return arr.toString()
}