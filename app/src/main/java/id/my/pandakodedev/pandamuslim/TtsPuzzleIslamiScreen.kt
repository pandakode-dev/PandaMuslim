package id.my.pandakodedev.pandamuslim

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class CrosswordClueItem(
    val number: Int,
    val direction: String, // "MENDATAR" atau "MENURUN"
    val clueText: String,
    val answer: String,
    val startRow: Int,
    val startCol: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsPuzzleIslamiScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clues = remember { getIslamicCrosswordClues() }

    // Menyimpan jawaban yang sudah berhasil ditebak pengguna
    val solvedClueNumbers = remember { mutableStateListOf<Int>() }
    var activeSelectedClue by remember { mutableStateOf(clues[0]) }
    var userInputAnswer by remember { mutableStateOf("") }

    // Matriks Papan TTS 7x7
    val gridSize = 7
    val boardSolution = remember {
        Array(gridSize) { CharArray(gridSize) { ' ' } }.apply {
            clues.forEach { c ->
                c.answer.forEachIndexed { i, ch ->
                    val r = if (c.direction == "MENURUN") c.startRow + i else c.startRow
                    val col = if (c.direction == "MENDATAR") c.startCol + i else c.startCol
                    if (r in 0 until gridSize && col in 0 until gridSize) {
                        this[r][col] = ch
                    }
                }
            }
        }
    }

    fun checkClueAnswer() {
        val cleanInput = userInputAnswer.trim().uppercase()
        if (cleanInput == activeSelectedClue.answer.uppercase()) {
            if (!solvedClueNumbers.contains(activeSelectedClue.number)) {
                solvedClueNumbers.add(activeSelectedClue.number)
            }
            userInputAnswer = ""
            Toast.makeText(context, "MasyaAllah! Jawaban Nomor ${activeSelectedClue.number} BENAR! 🎉", Toast.LENGTH_SHORT).show()
            // Pindah otomatis ke soal yang belum terjawab
            val nextUnsolved = clues.firstOrNull { !solvedClueNumbers.contains(it.number) }
            if (nextUnsolved != null) activeSelectedClue = nextUnsolved
        } else {
            Toast.makeText(context, "Belum tepat, coba periksa jumlah hurufnya ya! 🤔", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🧩 Teka-Teki Silang (TTS) Islami", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                        Text("Terjawab: ${solvedClueNumbers.size} dari ${clues.size} Kata", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { solvedClueNumbers.clear() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Ulangi", tint = GoldAccent)
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
            // 1. PAPAN KOTAK TEKA-TEKI SILANG 7x7 INTERAKTIF
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👆 Ketuk Petunjuk di Bawah untuk Mengisi Kotak TTS",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        for (r in 0 until gridSize) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                for (c in 0 until gridSize) {
                                    val solutionChar = boardSolution[r][c]
                                    val isPlayableCell = solutionChar != ' '

                                    // Cek apakah kotak ini termasuk dalam kata yang sudah terjawab
                                    val isRevealed = clues.any { clue ->
                                        solvedClueNumbers.contains(clue.number) && isCellInClue(r, c, clue)
                                    }
                                    val isHighlightedCell = isCellInClue(r, c, activeSelectedClue)
                                    val startNumber = clues.find { it.startRow == r && it.startCol == c }?.number

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    !isPlayableCell -> Color(0xFF022C22)
                                                    isRevealed -> Color(0xFFD1FAE5)
                                                    isHighlightedCell -> Color(0xFFFEF08A)
                                                    else -> Color.White
                                                }
                                            )
                                            .border(
                                                width = if (isHighlightedCell) 2.dp else 1.dp,
                                                color = if (isHighlightedCell) Color(0xFFD97706) else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(enabled = isPlayableCell) {
                                                clues.firstOrNull { isCellInClue(r, c, it) }?.let {
                                                    activeSelectedClue = it
                                                }
                                            }
                                    ) {
                                        if (startNumber != null) {
                                            Text(
                                                text = "$startNumber",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF047857),
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(start = 3.dp, top = 1.dp)
                                            )
                                        }
                                        if (isPlayableCell && isRevealed) {
                                            Text(
                                                text = solutionChar.toString(),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black,
                                                color = EmeraldDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. KOTAK INPUT JAWABAN SOAL YANG SEDANG DIPILIH
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, GoldAccent, RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(color = EmeraldDark, shape = RoundedCornerShape(50)) {
                                Text(
                                    text = "SOAL NO. ${activeSelectedClue.number} (${activeSelectedClue.direction} • ${activeSelectedClue.answer.length} Huruf)",
                                    color = GoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            TextButton(onClick = {
                                userInputAnswer = activeSelectedClue.answer.take(2)
                            }) {
                                Text("💡 Bantuan 2 Huruf", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = activeSelectedClue.clueText,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = EmeraldDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = userInputAnswer,
                                onValueChange = { userInputAnswer = it.uppercase() },
                                placeholder = { Text("Ketik ${activeSelectedClue.answer.length} huruf jawaban...") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { checkClueAnswer() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(54.dp)
                            ) {
                                Text("Jawab ✅", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // 3. DAFTAR SEMUA PETUNJUK MENDATAR & MENURUN
            items(clues, key = { it.number }) { clue ->
                val isSolved = solvedClueNumbers.contains(clue.number)
                val isSelected = activeSelectedClue.number == clue.number

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSolved -> Color(0xFFD1FAE5)
                            isSelected -> Color(0xFFFEF9C3)
                            else -> Color.White
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (isSelected) GoldAccent else MintSurface, RoundedCornerShape(16.dp))
                        .clickable { activeSelectedClue = clue }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${clue.number}. [${clue.direction}] — ${clue.answer.length} Huruf",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = clue.clueText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        }
                        if (isSolved) {
                            Surface(color = Color(0xFF059669), shape = RoundedCornerShape(50)) {
                                Text(
                                    text = "✅ ${clue.answer}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isCellInClue(r: Int, c: Int, clue: CrosswordClueItem): Boolean {
    return if (clue.direction == "MENDATAR") {
        r == clue.startRow && c in clue.startCol until (clue.startCol + clue.answer.length)
    } else {
        c == clue.startCol && r in clue.startRow until (clue.startRow + clue.answer.length)
    }
}

fun getIslamicCrosswordClues(): List<CrosswordClueItem> = listOf(
    CrosswordClueItem(1, "MENDATAR", "Ibadah tiang agama yang wajib dikerjakan 5 waktu sehari semalam", "SHOLAT", 0, 0),
    CrosswordClueItem(2, "MENURUN", "Keadaan menahan diri dari makan, minum & hawa nafsu dari terbit fajar hingga Maghrib", "PUASA", 0, 5),
    CrosswordClueItem(3, "MENDATAR", "Nama bulan suci saat diturunkannya Al-Quran", "RAMADAN", 2, 0),
    CrosswordClueItem(4, "MENURUN", "Bangunan suci di Makkah yang menjadi kiblat sholat umat Islam", "KABAH", 2, 1),
    CrosswordClueItem(5, "MENDATAR", "Kitab suci umat Islam yang diturunkan kepada Nabi Muhammad SAW", "QURAN", 4, 2),
    CrosswordClueItem(6, "MENDATAR", "Mengeluarkan sebagian harta untuk fakir miskin (Rukun Islam ke-3)", "ZAKAT", 6, 1)
)