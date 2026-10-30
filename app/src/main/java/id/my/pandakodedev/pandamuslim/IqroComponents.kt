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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EmeraldDark)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                0 to "📗 36+ Kartu Iqro",
                1 to "📖 Latihan Baca",
                2 to "🎵 30 Hijaiyah"
            )
            tabs.forEach { (index, title) ->
                val selected = iqroTabMode == index
                Surface(
                    color = if (selected) GoldAccent else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).clickable { onSelectTabMode(index) }
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (selected) Color(0xFF0F172A) else Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 9.dp)
                    )
                }
            }
        }

        if (iqroTabMode == 0 || iqroTabMode == 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().background(MintSurface)
            ) {
                items((1..6).toList()) { jilid ->
                    val isSelected = selectedJilid == jilid
                    Surface(
                        color = if (isSelected) EmeraldDark else Color.White,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GoldAccent else EmeraldPrimary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectJilid(jilid) }
                    ) {
                        Text(
                            text = "📗 Iqro Jilid $jilid",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) GoldAccent else EmeraldDark,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

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
                        modifier = Modifier.weight(1f).clickable { onSelectHarakat(idx) }
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
                        color = EmeraldDark
                    )
                }
            }
        }
    }
}

@Composable
fun IqroReadingSheetGeneratorView(
    selectedJilid: Int,
    onNextJilid: () -> Unit,
    onPlaySound: (String, String) -> Unit
) {
    var seed by remember { mutableStateOf(1) }
    var showLatinHelper by remember { mutableStateOf(true) }

    val readingRows = remember(selectedJilid, seed) {
        generateIqroReadingRows(selectedJilid, rowCount = 12)
    }

    val jilidInstruction = remember(selectedJilid) {
        when (selectedJilid) {
            1 -> "Jilid 1: Bacalah huruf Fathah dari KANAN ke KIRI dengan pendek & jelas. Ketuk huruf untuk mendengar makhraj Arab asli!"
            2 -> "Jilid 2: Latihan huruf sambung & bacaan panjang (Mad Alif 2 ketukan)."
            3 -> "Jilid 3: Latihan baris Kasrah (i), Dhammah (u), serta bacaan panjang Ya & Wawu sukun."
            4 -> "Jilid 4: Latihan Tanwin (an-in-un), Sukun, dan pantulan huruf Qalqalah."
            5 -> "Jilid 5: Latihan Alif Lam Syamsiyah, Tasydid, dan dengung Ghunnah (2 harakat)."
            else -> "Jilid 6: Latihan hukum Tajwid (Idgham, Iqlab, Ikhfa) & potongan kalimat Al-Quran."
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📖 Lembar Latihan Baca Iqro $selectedJilid", color = GoldAccent, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Surface(
                            color = Color.White.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { showLatinHelper = !showLatinHelper }
                        ) {
                            Text(
                                text = if (showLatinHelper) "🙈 Tutup Latin" else "👁️ Lihat Latin",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Text(jilidInstruction, color = Color.White, fontSize = 12.sp, lineHeight = 17.sp)

                    Button(
                        onClick = { seed++ },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Acak & Buat Halaman Huruf Baru 🔄", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }

        items(readingRows.size) { rowIndex ->
            val rowTokens = readingRows[rowIndex]
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Baris ${rowIndex + 1} • Baca dari Kanan ke Kiri ⬅️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        Text("🔊 Ketuk kotak untuk suara", fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
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
                                    .background(Color(0xFFFFFBEB))
                                    .border(1.5.dp, GoldAccent.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                    .clickable { onPlaySound(token.arabic, token.latin) }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = token.arabic,
                                        fontSize = if (selectedJilid <= 2) 30.sp else 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldDark,
                                        textAlign = TextAlign.Center
                                    )
                                    if (showLatinHelper) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(token.latin, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary, textAlign = TextAlign.Center)
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
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                OutlinedButton(
                    onClick = { seed++ },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔄 Acak Lagi", fontWeight = FontWeight.ExtraBold, color = EmeraldDark)
                }
                Button(
                    onClick = onNextJilid,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (selectedJilid < 6) "Lanjut Iqro Jilid ${selectedJilid + 1} ➔" else "Kembali ke Jilid 1 ➔",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun IqroVisualSoundCard(
    item: ServerContent,
    isBookmarked: Boolean,
    onPlaySound: () -> Unit,
    onBookmarkClick: () -> Unit,
    onZoomClick: () -> Unit
) {
    val imagePart = extractMediaPreviewUrl(item.mediaUrl)
    val iqroPandaBitmap = rememberOptimizedIcon(R.drawable.ic_menu_iqro, 90)

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
            Box(modifier = Modifier.fillMaxWidth()) {
                if (imagePart.isNotBlank()) {
                    NetworkImageLoader(
                        imageUrl = imagePart,
                        modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFEF9C3))
                            .border(2.dp, GoldAccent, RoundedCornerShape(16.dp))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                bitmap = iqroPandaBitmap,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.arabicText.ifBlank { "أَ بَ تَ" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Surface(
                    color = if (isBookmarked) Color(0xFFD97706) else Color.White.copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(30.dp)
                        .clickable { onBookmarkClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Tandai Mengaji",
                            tint = if (isBookmarked) Color.White else EmeraldDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    color = GoldAccent,
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(item.title, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = EmeraldDark, textAlign = TextAlign.Center, maxLines = 1)
            Text(item.latinText, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldPrimary, textAlign = TextAlign.Center, maxLines = 1)
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = EmeraldPrimary,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).clickable { onPlaySound() }
                ) {
                    Text("🔊 Bunyi", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 5.dp))
                }
                Surface(
                    color = if (isBookmarked) Color(0xFFD97706) else MintSurface,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).clickable { onBookmarkClick() }
                ) {
                    Text(
                        text = if (isBookmarked) "📌 Ditandai" else "🔖 Tandai",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isBookmarked) Color.White else EmeraldDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 5.dp)
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
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = arabic, fontSize = 34.sp, fontWeight = FontWeight.Black, color = EmeraldDark)
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
                            .height(150.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(2.dp, GoldAccent, RoundedCornerShape(18.dp))
                            .clickable { onPlaySound() }
                    ) {
                        Text(
                            text = item.arabicText.ifBlank { "أَ بَ تَ" },
                            fontSize = 40.sp,
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
                        Text("Suara", color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onBookmark,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tandai Mengaji", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }

                Text("Cara Baca: ${item.latinText}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = EmeraldPrimary)
                Text(item.description, fontSize = 13.sp, textAlign = TextAlign.Center, color = Color.DarkGray)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup 🌟", fontWeight = FontWeight.Bold) }
        }
    )
}

fun generateIqroReadingRows(jilid: Int, rowCount: Int): List<List<IqroReadingToken>> {
    val pool: List<IqroReadingToken> = when (jilid) {
        1 -> listOf(
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
        2 -> listOf(
            IqroReadingToken("بَتَ", "Ba-Ta"), IqroReadingToken("تَبَ", "Ta-Ba"), IqroReadingToken("ثَبَتَ", "Tsa-Ba-Ta"),
            IqroReadingToken("نَبَتَ", "Na-Ba-Ta"), IqroReadingToken("بَا", "Baa"), IqroReadingToken("تَا", "Taa"),
            IqroReadingToken("مَا", "Maa"), IqroReadingToken("لاَ", "Laa"), IqroReadingToken("كَتَبَ", "Ka-Ta-Ba"),
            IqroReadingToken("كَاتَبَ", "Kaa-Ta-Ba"), IqroReadingToken("جَلَسَ", "Ja-La-Sa"), IqroReadingToken("فَتَحَ", "Fa-Ta-Ha"),
            IqroReadingToken("صَدَقَ", "Sho-Da-Qo"), IqroReadingToken("قَالَ", "Qoo-La"), IqroReadingToken("كَانَ", "Kaa-Na"),
            IqroReadingToken("زَادَ", "Zaa-Da"), IqroReadingToken("نَصَرَ", "Na-Sho-Ro"), IqroReadingToken("عَبَدَ", "'A-Ba-Da")
        )
        3 -> listOf(
            IqroReadingToken("بِ", "Bi"), IqroReadingToken("تِ", "Ti"), IqroReadingToken("بِي", "Bii"),
            IqroReadingToken("تِي", "Tii"), IqroReadingToken("فِيهِ", "Fii-Hi"), IqroReadingToken("بُ", "Bu"),
            IqroReadingToken("تُ", "Tu"), IqroReadingToken("بُو", "Buu"), IqroReadingToken("تُو", "Tuu"),
            IqroReadingToken("كُتِبَ", "Ku-Ti-Ba"), IqroReadingToken("قُرِئَ", "Qu-Ri-A"), IqroReadingToken("سَمِعَ", "Sa-Mi-'A"),
            IqroReadingToken("عَلِمَ", "'A-Li-Ma"), IqroReadingToken("يَقُولُ", "Ya-Quu-Lu"), IqroReadingToken("يُوصِي", "Yuu-Shii"),
            IqroReadingToken("رُسُلُ", "Ru-Su-Lu"), IqroReadingToken("هُدِيَ", "Hu-Di-Ya"), IqroReadingToken("نُورِي", "Nuu-Rii")
        )
        4 -> listOf(
            IqroReadingToken("بً", "Ban"), IqroReadingToken("بٍ", "Bin"), IqroReadingToken("بٌ", "Bun"),
            IqroReadingToken("كِتَابًا", "Kitaaban"), IqroReadingToken("عَلِيمٌ", "'Aliimun"), IqroReadingToken("حَكِيمٍ", "Hakiimin"),
            IqroReadingToken("أَبْ", "Ab'"), IqroReadingToken("أَجْ", "Aj'"), IqroReadingToken("أَدْ", "Ad'"),
            IqroReadingToken("أَقْ", "Aq'"), IqroReadingToken("أَطْ", "Ath'"), IqroReadingToken("مَسْجِدٌ", "Masjidun"),
            IqroReadingToken("يَعْلَمُ", "Ya'lamu"), IqroReadingToken("أَنْعَمْتَ", "An'amta"), IqroReadingToken("مُسْلِمٌ", "Muslimun"),
            IqroReadingToken("أَحَدٌ", "Ahadun"), IqroReadingToken("صَمَدٌ", "Shomadun"), IqroReadingToken("كُفُوًا", "Kufuwan")
        )
        5 -> listOf(
            IqroReadingToken("إِنَّ", "In-na"), IqroReadingToken("عَمَّ", "'Am-ma"), IqroReadingToken("جَنَّةٌ", "Jan-natun"),
            IqroReadingToken("الْحَمْدُ", "Al-Hamdu"), IqroReadingToken("الْقَمَرُ", "Al-Qamaru"), IqroReadingToken("الشَّمْسُ", "Asy-Syamsu"),
            IqroReadingToken("الرَّحْمَنُ", "Ar-Rahmaanu"), IqroReadingToken("النَّاسِ", "An-Naasi"), IqroReadingToken("اللَّهُ", "Allaahu"),
            IqroReadingToken("رَبِّ", "Rab-bi"), IqroReadingToken("سُبِّحَ", "Sub-biha"), IqroReadingToken("قُدِّرَ", "Qud-dira"),
            IqroReadingToken("إِيَّاكَ", "Iy-yaaka"), IqroReadingToken("الصِّرَاطَ", "Ash-Shiraatho"), IqroReadingToken("الضَّالِّينَ", "Adh-Dhool-liina")
        )
        else -> listOf(
            IqroReadingToken("مَنْ يَقُولُ", "May-yaquulu"), IqroReadingToken("مِنْ مَالٍ", "Mim-maalin"),
            IqroReadingToken("مِنْ بَعْدِ", "Mim-ba'di"), IqroReadingToken("سَمِيعٌ بَصِيرٌ", "Samii'um-bashiir"),
            IqroReadingToken("مِنْ شَرِّ", "Min-syarri"), IqroReadingToken("أَنْفُسِهِمْ", "An-fusihim"),
            IqroReadingToken("مِنْ رَبِّهِمْ", "Mir-rabbihim"), IqroReadingToken("غَفُورٌ رَحِيمٌ", "Ghafuurur-rahiim"),
            IqroReadingToken("لَمْ يَلِدْ", "Lam yalid'"), IqroReadingToken("وَلَمْ يُولَدْ", "Wa lam yuulad'"),
            IqroReadingToken("فَسَبِّحْ", "Fasabbih"), IqroReadingToken("وَاسْتَغْفِرْهُ", "Wastaghfirhu")
        )
    }

    val columnsPerRow = if (jilid <= 4) 4 else 3
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