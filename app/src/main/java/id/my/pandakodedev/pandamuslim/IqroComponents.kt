package id.my.pandakodedev.pandamuslim

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

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

data class JilidCurriculumSummary(
    val jilid: Int,
    val title: String,
    val targetSkill: String,
    val tipText: String
)

fun getJilidCurriculumSummary(jilid: Int): JilidCurriculumSummary {
    return when (jilid) {
        1 -> JilidCurriculumSummary(
            1, "Iqro Jilid 1: Pengenalan Huruf Fathah (A - Ya)",
            "Target: Membaca huruf tunggal berharakat Fathah secara pendek, tegas & tepat makhraj.",
            "Jangan dipanjangkan! Mulut terbuka secukupnya, baca putus-putus: A - Ba - Ta."
        )
        2 -> JilidCurriculumSummary(
            2, "Iqro Jilid 2: Huruf Sambung & Mad Thabi'i (Alif)",
            "Target: Mengenali bentuk huruf ketika disambung (awal, tengah, akhir) & bacaan panjang 2 ketukan.",
            "Bedakan dengan tegas mana huruf pendek (1 ketukan) dan huruf ber-Alif panjang (2 ketukan)."
        )
        3 -> JilidCurriculumSummary(
            3, "Iqro Jilid 3: Baris Kasrah (i), Dhammah (u) & Mad Ya/Wawu",
            "Target: Lancar membaca vokal i dan u, serta bacaan panjang Ya Sukun (ii) dan Wawu Sukun (uu).",
            "Jaga agar suara Kasrah tidak miring ke 'e', dan Dhammah bibir maju sempurna."
        )
        4 -> JilidCurriculumSummary(
            4, "Iqro Jilid 4: Tanwin (an-in-un), Sukun & Pantulan Qalqalah",
            "Target: Menguasai bunyi Tanwin, huruf mati (Sukun), dan 5 huruf pantulan Qalqalah (ق ط ب ج د).",
            "Hati-hati! Huruf Sukun biasa jangan ikut memantul, hanya 5 huruf Qalqalah yang memantul."
        )
        5 -> JilidCurriculumSummary(
            5, "Iqro Jilid 5: Alif Lam, Tasydid, Ghunnah & Lafadz Allah",
            "Target: Menguasai tekan kuat Tasydid, dengung Ghunnah 2 harakat, dan cara berhenti (Waqaf).",
            "Setiap bertemu Nun Tasydid (نّ) atau Mim Tasydid (مّ), wajib ditahan dengung 2 harakat!"
        )
        else -> JilidCurriculumSummary(
            6, "Iqro Jilid 6: Hukum Tajwid Lengkap (Idgham, Iqlab, Ikhfa)",
            "Target: Menguasai leburan Nun Mati/Tanwin, tanda Waqaf Mushaf & siap membaca Al-Quran.",
            "Tahap akhir sebelum Al-Quran! Perhatikan dengung halus Ikhfa dan leburan Idgham."
        )
    }
}

@Composable
fun IqroTopBarTabs(
    iqroTabMode: Int,
    onSelectTabMode: (Int) -> Unit,
    selectedJilid: Int,
    onSelectJilid: (Int) -> Unit,
    selectedHarakatMode: Int,
    onSelectHarakat: (Int) -> Unit,
    activeSpeakingTitle: String?
) {
    Column {
        // 3 Mode Utama Iqro
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EmeraldDark)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                0 to "📗 Kartu Iqro 1–6",
                1 to "📖 Lembar Latihan",
                2 to "🎵 30 Hijaiyah"
            )
            tabs.forEach { (index, title) ->
                val selected = iqroTabMode == index
                Surface(
                    color = if (selected) GoldAccent else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTabMode(index) }
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (selected) Color(0xFF0F172A) else Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 9.dp)
                    )
                }
            }
        }

        // Pilihan Jilid 1 s/d 6
        if (iqroTabMode == 0 || iqroTabMode == 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MintSurface)
            ) {
                items((1..6).toList()) { jilid ->
                    val isSelected = selectedJilid == jilid
                    Surface(
                        color = if (isSelected) EmeraldDark else Color.White,
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = if (isSelected) 3.dp else 0.dp,
                        modifier = Modifier
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GoldAccent else EmeraldPrimary.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectJilid(jilid) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "📗 Jilid $jilid",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) GoldAccent else EmeraldDark
                            )
                        }
                    }
                }
            }
        }

        // Pilihan Baris Harakat (Khusus Tab 30 Hijaiyah)
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
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectHarakat(idx) }
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
                        color = EmeraldDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// =========================================================================
// HEADER RINGKASAN KURIKULUM JILID (TAMPIL DI ATAS KARTU IQRO)
// =========================================================================
@Composable
fun IqroJilidHeaderBanner(jilid: Int, totalCardsInJilid: Int) {
    val summary = remember(jilid) { getJilidCurriculumSummary(jilid) }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, GoldAccent, RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                    Text(
                        text = "KURIKULUM JILID $jilid • $totalCardsInJilid HALAMAN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                Text("🔊 Ketuk Kartu = Suara", fontSize = 10.sp, color = MintSurface, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = summary.title,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = summary.targetSkill,
                fontSize = 12.sp,
                color = MintSurface,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 Petunjuk Guru: ${summary.tipText}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// =========================================================================
// MODE 2: GENERATOR LEMBAR LATIHAN BACA IQRO (DASAR vs EVALUASI EBTA)
// =========================================================================
@Composable
fun IqroReadingSheetGeneratorView(
    selectedJilid: Int,
    onNextJilid: () -> Unit,
    onPlaySound: (String, String) -> Unit
) {
    var seed by remember { mutableStateOf(1) }
    var showLatinHelper by remember { mutableStateOf(true) }
    var practiceSubMode by remember { mutableStateOf(0) } // 0 = Latihan Bertahap, 1 = Ujian Evaluasi (EBTA)

    val summary = remember(selectedJilid) { getJilidCurriculumSummary(selectedJilid) }
    val readingRows = remember(selectedJilid, seed, practiceSubMode) {
        generateIqroReadingRows(selectedJilid, rowCount = 15, isExamMode = (practiceSubMode == 1))
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, GoldAccent, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📖 Lembar Latihan Baca Iqro $selectedJilid",
                                color = GoldAccent,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "15 Baris Latihan Interaktif • Baca dari KANAN ke KIRI ⬅️",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            color = if (showLatinHelper) Color.White.copy(alpha = 0.18f) else GoldAccent,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { showLatinHelper = !showLatinHelper }
                        ) {
                            Text(
                                text = if (showLatinHelper) "🙈 Tutup Latin" else "👁️ Buka Latin",
                                color = if (showLatinHelper) Color.White else Color(0xFF0F172A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Saklar Mode Latihan Dasar vs Mode Ujian Evaluasi Jilid (EBTA)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0 to "📝 Latihan Pokok Jilid $selectedJilid",
                            1 to "🎓 Ujian Evaluasi (EBTA) Jilid $selectedJilid"
                        ).forEach { (modeIdx, label) ->
                            val active = practiceSubMode == modeIdx
                            Surface(
                                color = if (active) GoldAccent else Color.Transparent,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        practiceSubMode = modeIdx
                                        if (modeIdx == 1) showLatinHelper = false // Saat EBTA, sembunyikan latin otomatis!
                                    }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (active) Color(0xFF0F172A) else Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = summary.tipText,
                        color = MintSurface,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Button(
                        onClick = { seed++ },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Acak & Buat Halaman Latihan Baru (Halaman #$seed) 🔄",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        items(readingRows.size) { rowIndex ->
            val rowTokens = readingRows[rowIndex]
            val fullRowArabic = remember(rowTokens) { rowTokens.joinToString(" ، ") { it.arabic } }
            val fullRowLatin = remember(rowTokens) { rowTokens.joinToString(" — ") { it.latin } }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MintSurface, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(color = MintSurface, shape = RoundedCornerShape(50)) {
                            Text(
                                text = "Baris ${rowIndex + 1} • Kanan ke Kiri ⬅️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Tombol Putar Suara 1 Baris Penuh
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(1.dp, GoldAccent, RoundedCornerShape(50))
                                .clickable { onPlaySound(fullRowArabic, fullRowLatin) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Dengar 1 Baris",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Kotak-Kotak Kata/Huruf Iqro (Diurutkan dari Kanan ke Kiri)
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
                                    .background(Color(0xFFFFFDF7))
                                    .border(1.5.dp, GoldAccent.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
                                    .clickable { onPlaySound(token.arabic, token.latin) }
                                    .padding(vertical = 14.dp, horizontal = 4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = token.arabic,
                                        fontSize = when {
                                            selectedJilid == 1 -> 32.sp
                                            selectedJilid <= 3 -> 27.sp
                                            else -> 23.sp
                                        },
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark,
                                        textAlign = TextAlign.Center
                                    )
                                    if (showLatinHelper) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = token.latin,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldPrimary,
                                            textAlign = TextAlign.Center
                                        )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedButton(
                    onClick = { seed++ },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔄 Halaman Baru", fontWeight = FontWeight.ExtraBold, color = EmeraldDark)
                }
                Button(
                    onClick = onNextJilid,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (selectedJilid < 6) "Lanjut Jilid ${selectedJilid + 1} ➔" else "Ulang Jilid 1 ➔",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// =========================================================================
// KARTU VISUAL IQRO BERSUARA (DESAIN RAPI, PROPORSIONAL & JELAS)
// =========================================================================
@Composable
fun IqroVisualSoundCard(
    item: ServerContent,
    isBookmarked: Boolean,
    onPlaySound: () -> Unit,
    onBookmarkClick: () -> Unit,
    onZoomClick: () -> Unit
) {
    val imagePart = extractMediaPreviewUrl(item.mediaUrl)
    val cleanTitle = remember(item.title) {
        item.title.replace(Regex("^Iqro\\s*\\d+\\s*•\\s*", RegexOption.IGNORE_CASE), "")
    }

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
            // Baris Atas: Nomor Halaman & Tombol Penanda / Zoom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isBookmarked) Color(0xFFD97706) else MintSurface,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { onBookmarkClick() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Tandai",
                            tint = if (isBookmarked) Color.White else EmeraldDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isBookmarked) "Batas Baca" else "Tandai",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isBookmarked) Color.White else EmeraldDark
                        )
                    }
                }

                Surface(
                    color = GoldAccent.copy(alpha = 0.35f),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onZoomClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Perbesar Kartu",
                            tint = EmeraldDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kotak Kaligrafi Huruf Iqro (Besar & Tajam)
            if (imagePart.isNotBlank()) {
                NetworkImageLoader(
                    imageUrl = imagePart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFFDF5), Color(0xFFFEF3C7))
                            )
                        )
                        .border(2.dp, GoldAccent, RoundedCornerShape(16.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = item.arabicText.ifBlank { "أَ   بَ" },
                        fontSize = if (item.arabicText.length > 18) 22.sp else 28.sp,
                        lineHeight = 38.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = cleanTitle,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = EmeraldDark,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.latinText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = EmeraldPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol Bunyi & Detail
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
                        text = "🔊 Dengar",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
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
                        text = "🔍 Perbesar",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = arabic,
                fontSize = 34.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                color = EmeraldDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = soundRead, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
            Text(text = "($latinName)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}

@Composable
fun IqroZoomDialog(
    item: ServerContent,
    onPlaySound: () -> Unit,
    onBookmark: () -> Unit,
    onDismiss: () -> Unit
) {
    val imagePart = extractMediaPreviewUrl(item.mediaUrl)
    AlertDialog(
        onDismissRequest = onDismiss,
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
                            .clickable { onPlaySound() }
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(2.dp, GoldAccent, RoundedCornerShape(18.dp))
                            .clickable { onPlaySound() }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = item.arabicText.ifBlank { "أَ بَ تَ" },
                            fontSize = 38.sp,
                            lineHeight = 52.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Black,
                            color = EmeraldDark,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onPlaySound,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dengar Bacaan", color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onBookmark,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tandai Batas", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }

                Surface(color = MintSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Cara Baca: ${item.latinText}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = EmeraldDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup 🌟", fontWeight = FontWeight.Bold) }
        }
    )
}

// =========================================================================
// BANK KATA RAKSASA (150+ KOSAKATA LATIHAN & EVALUASI EBTA IQRO JILID 1-6)
// =========================================================================
fun generateIqroReadingRows(jilid: Int, rowCount: Int, isExamMode: Boolean = false): List<List<IqroReadingToken>> {
    val pool: List<IqroReadingToken> = when (jilid) {
        1 -> if (!isExamMode) {
            listOf(
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
        } else {
            // Evaluasi EBTA Jilid 1: 3 Huruf Terpisah yang Mirip Makhrajnya
            listOf(
                IqroReadingToken("أَ عَ أَ", "A - 'A - A"), IqroReadingToken("حَ هَ خَ", "Ha - Ha - Kho"),
                IqroReadingToken("ثَ سَ شَ", "Tsa - Sa - Sya"), IqroReadingToken("ذَ زَ ظَ", "Dza - Za - Zho"),
                IqroReadingToken("تَ طَ دَ", "Ta - Tho - Da"), IqroReadingToken("صَ ضَ سَ", "Sho - Dho - Sa"),
                IqroReadingToken("قَ كَ فَقَ", "Qo - Ka - Fa-Qo"), IqroReadingToken("غَ خَ عَ", "Gho - Kho - 'A"),
                IqroReadingToken("يَ وَ نَ", "Ya - Wa - Na"), IqroReadingToken("جَ دَ خَ", "Ja - Da - Kho"),
                IqroReadingToken("شَضَ صَثَ", "Sya-Dho Sho-Tsa"), IqroReadingToken("ظَضَ ذَزَ", "Zho-Dho Dza-Za")
            )
        }

        2 -> if (!isExamMode) {
            listOf(
                IqroReadingToken("بَتَ", "Ba-Ta"), IqroReadingToken("تَبَ", "Ta-Ba"), IqroReadingToken("ثَبَتَ", "Tsa-Ba-Ta"),
                IqroReadingToken("نَبَتَ", "Na-Ba-Ta"), IqroReadingToken("بَا", "Baa"), IqroReadingToken("تَا", "Taa"),
                IqroReadingToken("مَا", "Maa"), IqroReadingToken("لَا", "Laa"), IqroReadingToken("كَتَبَ", "Ka-Ta-Ba"),
                IqroReadingToken("كَاتَبَ", "Kaa-Ta-Ba"), IqroReadingToken("جَلَسَ", "Ja-La-Sa"), IqroReadingToken("فَتَحَ", "Fa-Ta-Ha"),
                IqroReadingToken("صَدَقَ", "Sho-Da-Qo"), IqroReadingToken("قَالَ", "Qoo-La"), IqroReadingToken("كَانَ", "Kaa-Na"),
                IqroReadingToken("زَادَ", "Zaa-Da"), IqroReadingToken("نَصَرَ", "Na-Sho-Ro"), IqroReadingToken("عَبَدَ", "'A-Ba-Da"),
                IqroReadingToken("حَسَدَ", "Ha-Sa-Da"), IqroReadingToken("خَلَقَ", "Kho-La-Qo"), IqroReadingToken("ذَهَبَ", "Dza-Ha-Ba"),
                IqroReadingToken("شَكَرَ", "Sya-Ka-Ro"), IqroReadingToken("ضَرَبَ", "Dho-Ro-Ba"), IqroReadingToken("طَبَعَ", "Tho-Ba-'A")
            )
        } else {
            // Evaluasi EBTA Jilid 2: Membedakan Pendek vs Panjang Mad Alif
            listOf(
                IqroReadingToken("جَمَعَ", "Ja-Ma-'A"), IqroReadingToken("جَامَعَ", "Jaa-Ma-'A"),
                IqroReadingToken("فَعَلَ", "Fa-'A-La"), IqroReadingToken("فَاعَلَ", "Faa-'A-La"),
                IqroReadingToken("حَكَمَ", "Ha-Ka-Ma"), IqroReadingToken("حَاكَمَ", "Haa-Ka-Ma"),
                IqroReadingToken("قَتَلَ", "Qo-Ta-La"), IqroReadingToken("قَاتَلَ", "Qoo-Ta-La"),
                IqroReadingToken("تَعَالَىٰ", "Ta-'Aa-Laa"), IqroReadingToken("أَطَاعَ", "A-Thoo-'A"),
                IqroReadingToken("أَصَابَ", "A-Shoo-Ba"), IqroReadingToken("مَالِكَ", "Maa-Li-Ka"),
                IqroReadingToken("صِرَاطَ", "Shi-Roo-Tho"), IqroReadingToken("طَعَامَ", "Tho-'Aa-Ma")
            )
        }

        3 -> if (!isExamMode) {
            listOf(
                IqroReadingToken("بِ", "Bi"), IqroReadingToken("تِ", "Ti"), IqroReadingToken("بِي", "Bii"),
                IqroReadingToken("تِي", "Tii"), IqroReadingToken("فِيهِ", "Fii-Hi"), IqroReadingToken("بُ", "Bu"),
                IqroReadingToken("تُ", "Tu"), IqroReadingToken("بُو", "Buu"), IqroReadingToken("تُو", "Tuu"),
                IqroReadingToken("كُتِبَ", "Ku-Ti-Ba"), IqroReadingToken("قُرِئَ", "Qu-Ri-A"), IqroReadingToken("سَمِعَ", "Sa-Mi-'A"),
                IqroReadingToken("عَلِمَ", "'A-Li-Ma"), IqroReadingToken("يَقُولُ", "Ya-Quu-Lu"), IqroReadingToken("يُوصِي", "Yuu-Shii"),
                IqroReadingToken("رُسُلُ", "Ru-Su-Lu"), IqroReadingToken("هُدِيَ", "Hu-Di-Ya"), IqroReadingToken("نُورِي", "Nuu-Rii"),
                IqroReadingToken("حَمِدَ", "Ha-Mi-Da"), IqroReadingToken("شَهِدَ", "Sya-Hi-Da"), IqroReadingToken("بَخِلَ", "Ba-Khi-La"),
                IqroReadingToken("فَتِيلَا", "Fa-Tii-Laa"), IqroReadingToken("بَصِيرَا", "Ba-Shii-Roo"), IqroReadingToken("شَكُورَا", "Sya-Kuu-Roo")
            )
        } else {
            // Evaluasi EBTA Jilid 3: Kombinasi Mad Alif, Mad Ya, dan Mad Wawu
            listOf(
                IqroReadingToken("قَالُوا", "Qoo-Luu"), IqroReadingToken("كَانُوا", "Kaa-Nuu"),
                IqroReadingToken("فِيْهَا", "Fii-Haa"), IqroReadingToken("أُوتِيَ", "Uu-Ti-Ya"),
                IqroReadingToken("تُوبُوا", "Tuu-Buu"), IqroReadingToken("يُرِيدُ", "Yu-Rii-Du"),
                IqroReadingToken("مَقَامِي", "Ma-Qoo-Mii"), IqroReadingToken("عِبَادِي", "'I-Baa-Dii"),
                IqroReadingToken("يُقِيمُوْنَ", "Yu-Qii-Muuna"), IqroReadingToken("هَارُوْنَ", "Haa-Ruuna"),
                IqroReadingToken("قَارُوْنَ", "Qoo-Ruuna"), IqroReadingToken("دَاوُودَ", "Daa-Wuuda")
            )
        }

        4 -> if (!isExamMode) {
            listOf(
                IqroReadingToken("بً", "Ban"), IqroReadingToken("بٍ", "Bin"), IqroReadingToken("بٌ", "Bun"),
                IqroReadingToken("كِتَابًا", "Kitaaban"), IqroReadingToken("عَلِيمٌ", "'Aliimun"), IqroReadingToken("حَكِيمٍ", "Hakiimin"),
                IqroReadingToken("أَبْ", "Ab'"), IqroReadingToken("أَجْ", "Aj'"), IqroReadingToken("أَدْ", "Ad'"),
                IqroReadingToken("أَقْ", "Aq'"), IqroReadingToken("أَطْ", "Ath'"), IqroReadingToken("مَسْجِدٌ", "Masjidun"),
                IqroReadingToken("يَعْلَمُ", "Ya'lamu"), IqroReadingToken("أَنْعَمْتَ", "An'amta"), IqroReadingToken("مُسْلِمٌ", "Muslimun"),
                IqroReadingToken("أَحَدٌ", "Ahadun"), IqroReadingToken("صَمَدٌ", "Shomadun"), IqroReadingToken("كُفُوًا", "Kufuwan"),
                IqroReadingToken("أَطْعَمَ", "Ath'-'ama"), IqroReadingToken("يَجْعَلْ", "Yaj'-'al"), IqroReadingToken("يَدْخُلُ", "Yad'-khulu"),
                IqroReadingToken("أَقْرَبُ", "Aq'-rabu"), IqroReadingToken("حَبْلٌ", "Hab'-lun"), IqroReadingToken("أَجْرٌ", "Aj'-run")
            )
        } else {
            // Evaluasi EBTA Jilid 4: Membedakan Sukun Biasa vs Pantulan Qalqalah
            listOf(
                IqroReadingToken("مَغْضُوبِ", "Magh-dhuubi"), IqroReadingToken("مَقْطُوعٍ", "Maq'-thuu'in"),
                IqroReadingToken("تَكْسِبُ", "Taksibu"), IqroReadingToken("يَقْبَلُ", "Yaq'-balu"),
                IqroReadingToken("مُؤْمِنٌ", "Mu'minun"), IqroReadingToken("مُبْصِرٌ", "Mub'-shirun"),
                IqroReadingToken("يَشْكُرُ", "Yasy-kuru"), IqroReadingToken("يَطْبَعُ", "Yath'-ba'u"),
                IqroReadingToken("أَنْهَارٌ", "An-haarun"), IqroReadingToken("أَبْوَابٌ", "Ab'-waabun"),
                IqroReadingToken("مِنْ خَوْفٍ", "Min khaufin"), IqroReadingToken("مِنْ هَادٍ", "Min haadin")
            )
        }

        5 -> if (!isExamMode) {
            listOf(
                IqroReadingToken("إِنَّ", "In-na"), IqroReadingToken("عَمَّ", "'Am-ma"), IqroReadingToken("جَنَّةٌ", "Jan-natun"),
                IqroReadingToken("الْحَمْدُ", "Al-Hamdu"), IqroReadingToken("الْقَمَرُ", "Al-Qamaru"), IqroReadingToken("الشَّمْسُ", "Asy-Syamsu"),
                IqroReadingToken("الرَّحْمَنُ", "Ar-Rahmaanu"), IqroReadingToken("النَّاسِ", "An-Naasi"), IqroReadingToken("اللَّهُ", "Allaahu"),
                IqroReadingToken("رَبِّ", "Rab-bi"), IqroReadingToken("سُبِّحَ", "Sub-biha"), IqroReadingToken("قُدِّرَ", "Qud-dira"),
                IqroReadingToken("إِيَّاكَ", "Iy-yaaka"), IqroReadingToken("الصِّرَاطَ", "Ash-Shiraatho"), IqroReadingToken("الضَّالِّينَ", "Adh-Dhool-liina"),
                IqroReadingToken("بِسْمِ اللَّهِ", "Bismillaahi"), IqroReadingToken("مِنَ اللَّهِ", "Minallaahi"), IqroReadingToken("ثُمَّ", "Tsum-ma"),
                IqroReadingToken("فَعَّالٌ", "Fa'-'aalun"), IqroReadingToken("كَذَّابٌ", "Kadz-dzaabun"), IqroReadingToken("وَهَّابٌ", "Wah-haabun")
            )
        } else {
            // Evaluasi EBTA Jilid 5: Latihan Waqaf (Berhenti di Akhir Ayat) & Mad Lazim
            listOf(
                IqroReadingToken("الْعَالَمِينَ ۝", "Al-'Aalamiin"), IqroReadingToken("الدِّينِ ۝", "Ad-Diin"),
                IqroReadingToken("نَسْتَعِينُ ۝", "Nasta'iin"), IqroReadingToken("أَحَدٌ ۝", "Ahad'"),
                IqroReadingToken("الْفَلَقِ ۝", "Al-Falaq'"), IqroReadingToken("وَاسِقٍ إِذَا وَقَبَ ۝", "Idzaa waqab'"),
                IqroReadingToken("ذَاتَ لَهَبٍ ۝", "Dzaata lahab'"), IqroReadingToken("الْحَطَبِ ۝", "Al-Hathab'"),
                IqroReadingToken("الْكَوْثَرَ ۝", "Al-Kautsar"), IqroReadingToken("وَانْحَرْ ۝", "Wan-har")
            )
        }

        else -> if (!isExamMode) {
            listOf(
                IqroReadingToken("مَنْ يَقُولُ", "May-yaquulu"), IqroReadingToken("مِنْ مَالٍ", "Mim-maalin"),
                IqroReadingToken("مِنْ بَعْدِ", "Mim-ba'di"), IqroReadingToken("سَمِيعٌ بَصِيرٌ", "Samii'um-bashiir"),
                IqroReadingToken("مِنْ شَرِّ", "Min-syarri"), IqroReadingToken("أَنْفُسِهِمْ", "An-fusihim"),
                IqroReadingToken("مِنْ رَبِّهِمْ", "Mir-rabbihim"), IqroReadingToken("غَفُورٌ رَحِيمٌ", "Ghafuurur-rahiim"),
                IqroReadingToken("لَمْ يَلِدْ", "Lam yalid'"), IqroReadingToken("وَلَمْ يُولَدْ", "Wa lam yuulad'"),
                IqroReadingToken("فَسَبِّحْ", "Fasabbih"), IqroReadingToken("وَاسْتَغْفِرْهُ", "Wastaghfirhu"),
                IqroReadingToken("فَمَنْ يَعْمَلْ", "Famay-ya'mal"), IqroReadingToken("مِنْ وَالٍ", "Miw-waalin"),
                IqroReadingToken("لَيُنْبَذَنَّ", "Layumbadzanna"), IqroReadingToken("مَنْصُورًا", "Man-shuuran")
            )
        } else {
            // Evaluasi EBTA Jilid 6: Potongan Ayat Al-Quran Lengkap Tajwid
            listOf(
                IqroReadingToken("الم ۝ ذَٰلِكَ الْكِتَابُ", "Alif-Laam-Miim"),
                IqroReadingToken("يس ۝ وَالْقُرْآنِ الْحَكِيمِ", "Yaa-Siin"),
                IqroReadingToken("إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Innaa a'thainaaka"),
                IqroReadingToken("تَرْمِيهِمْ بِحِجَارَةٍ", "Tarmiihim-bihijaarah"),
                IqroReadingToken("أَطْعَمَهُمْ مِنْ جُوعٍ", "Ath'amahum-min juu'"),
                IqroReadingToken("وَآمَنَهُمْ مِنْ خَوْفٍ", "Wa aamanahum-min khauf"),
                IqroReadingToken("كَلَّا لَيُنْبَذَنَّ فِي الْحُطَمَةِ", "Kallaa layumbadzanna"),
                IqroReadingToken("فِي عَمَدٍ مُمَدَّدَةٍ", "Fii 'amadim-mumaddadah")
            )
        }
    }

    val columnsPerRow = when {
        isExamMode && jilid >= 5 -> 2
        jilid <= 3 && !isExamMode -> 4
        else -> 3
    }
    return List(rowCount) {
        List(columnsPerRow) {
            pool[Random.nextInt(pool.size)]
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
        RawH("أ", "Alif", "A", "I", "U", "An"),
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
        RawH("ه", "Ha Besar", "Ha", "Hi", "Hu", "Han"),
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