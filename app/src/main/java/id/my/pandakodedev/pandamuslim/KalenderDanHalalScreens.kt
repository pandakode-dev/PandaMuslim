package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

// =========================================================================
// MENU BARU 3: KALENDER HIJRIAH & CELENGAN BINTANG AMAL BULANAN
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalenderHijriahScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("panda_kalender_prefs", Context.MODE_PRIVATE) }

    val hijriMonths = remember {
        listOf(
            "Muharram", "Shafar", "Rabi'ul Awwal", "Rabi'ul Akhir",
            "Jumadil Awwal", "Jumadil Akhir", "Rajab", "Sya'ban",
            "Ramadhan", "Syawwal", "Dzulqa'dah", "Dzulhijjah"
        )
    }
    var selectedMonthIdx by remember { mutableStateOf(8) } // Default Ramadhan / Bulan Pilihan
    val todayDay = remember { (Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).coerceIn(1, 30) }

    // Status Bintang Amal per Tanggal (1 - 30)
    val starredDays = remember(selectedMonthIdx) {
        mutableStateMapOf<Int, Boolean>().apply {
            for (d in 1..30) {
                put(d, prefs.getBoolean("star_${selectedMonthIdx}_$d", false))
            }
        }
    }

    val totalStarredThisMonth = starredDays.values.count { it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🌙 Kalender Hijriah & Celengan Amal", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                        Text("Ketuk tanggal untuk menempel Stiker Bintang Ibadah ⭐", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Pemilih Bulan Hijriah
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            selectedMonthIdx = if (selectedMonthIdx > 0) selectedMonthIdx - 1 else 11
                        }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Sebelumnya", tint = GoldAccent)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${hijriMonths[selectedMonthIdx]} 1447 H",
                                color = GoldAccent,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Terkumpul $totalStarredThisMonth / 30 Bintang Kebaikan ⭐",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = {
                            selectedMonthIdx = (selectedMonthIdx + 1) % 12
                        }) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Berikutnya", tint = GoldAccent)
                        }
                    }
                }
            }

            // Papan 30 Tanggal Hijriah + Penanda Puasa Ayyamul Bidh (Tanggal 13, 14, 15)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📅 Ketuk Tanggal Saat Kamu Selesai Sholat & Mengaji:",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = EmeraldDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items((1..30).toList()) { day ->
                            val hasStar = starredDays[day] == true
                            val isAyyamulBidh = day in listOf(13, 14, 15)

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        when {
                                            hasStar -> Color(0xFFFEF08A)
                                            isAyyamulBidh -> MintSurface
                                            else -> OffWhiteBg
                                        }
                                    )
                                    .border(
                                        width = if (day == todayDay) 2.5.dp else 1.dp,
                                        color = if (hasStar) Color(0xFFD97706) else if (isAyyamulBidh) EmeraldPrimary else Color.LightGray,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        val next = !hasStar
                                        starredDays[day] = next
                                        prefs.edit().putBoolean("star_${selectedMonthIdx}_$day", next).apply()
                                        if (next) {
                                            Toast.makeText(context, "Alhamdulillah! +1 Bintang Amal di tanggal $day ${hijriMonths[selectedMonthIdx]} ⭐", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$day",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = EmeraldDark
                                    )
                                    Text(
                                        text = if (hasStar) "⭐" else if (isAyyamulBidh) "🌕" else "•",
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Keterangan Sunnah Bulanan
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MintSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌕", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tanggal 13, 14, dan 15 setiap bulan Hijriah adalah jadwal Puasa Sunnah Ayyamul Bidh (Pahala seperti puasa sepanjang tahun!).",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                }
            }
        }
    }
}

// =========================================================================
// MENU BARU 4: KAMUS HALAL & PANDUAN MAKANAN THAYYIB KELUARGA
// =========================================================================
data class HalalGuideItem(
    val name: String,
    val codeOrCategory: String,
    val status: String, // "HALAL", "SYUBHAT (CEK LOGO HALAL)", "HARAM"
    val explanation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KamusHalalScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("SEMUA") }
    val allGuides = remember { getHalalGuideDatabase() }

    val filtered = remember(allGuides, searchQuery, selectedFilter) {
        allGuides.filter { item ->
            val matchFilter = selectedFilter == "SEMUA" || item.status.startsWith(selectedFilter)
            val matchSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.codeOrCategory.contains(searchQuery, ignoreCase = true) ||
                    item.explanation.contains(searchQuery, ignoreCase = true)
            matchFilter && matchSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🍜 Kamus Halal & Makanan Thayyib", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                        Text("Panduan Bahan Makanan, Kode E-Number & Titik Kritis", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari bahan (contoh: Gelatin, Mirin, E471, Lesitin)...", fontSize = 13.sp) },
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        "SEMUA" to "Semua",
                        "HALAL" to "✅ Halal",
                        "SYUBHAT" to "⚠️ Titik Kritis",
                        "HARAM" to "❌ Haram"
                    ).forEach { (key, label) ->
                        val active = selectedFilter == key
                        Surface(
                            color = if (active) GoldAccent else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f).clickable { selectedFilter = key }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (active) Color(0xFF0F172A) else Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.name }) { item ->
                    val badgeBg = when {
                        item.status.startsWith("HALAL") -> Color(0xFFD1FAE5)
                        item.status.startsWith("HARAM") -> Color(0xFFFFE4E6)
                        else -> Color(0xFFFEF3C7)
                    }
                    val badgeTextCol = when {
                        item.status.startsWith("HALAL") -> Color(0xFF065F46)
                        item.status.startsWith("HARAM") -> Color(0xFFBE123C)
                        else -> Color(0xFF92400E)
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EmeraldDark)
                                    Text(item.codeOrCategory, fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                }
                                Surface(color = badgeBg, shape = RoundedCornerShape(50)) {
                                    Text(
                                        text = item.status,
                                        color = badgeTextCol,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.explanation, fontSize = 13.sp, color = Color(0xFF334155), lineHeight = 19.sp)
                        }
                    }
                }
            }
        }
    }
}

fun getHalalGuideDatabase(): List<HalalGuideItem> = listOf(
    HalalGuideItem("Madu Murni, Kurma & Susu Segar", "Pangan Alami Thayyib", "HALAL ✅", "Halal secara zatnya dan sangat dianjurkan dalam Al-Quran & Sunnah untuk kesehatan tubuh."),
    HalalGuideItem("Lesitin Kedelai (Soy Lecithin / E322)", "Kode Aditif E322 Nabati", "HALAL ✅", "Pengemulsi pada cokelat, susu, dan roti yang berasal dari kacang kedelai murni berstatus Halal."),
    HalalGuideItem("Asam Sitrat (Citric Acid / E330)", "Kode Aditif E330", "HALAL ✅", "Pengatur keasaman alami dari buah-buahan atau fermentasi nabati yang aman dan halal dikonsumsi."),
    HalalGuideItem("Gelatin (Kode E441)", "Bahan Permen Kenyal / Marshmallow / Kapsul", "SYUBHAT ⚠️ CEK LOGO HALAL", "Titik kritis tinggi! Gelatin bisa berasal dari tulang/kulit sapi halal atau dari babi. Wajib memastikan ada Logo Halal Resmi pada kemasan."),
    HalalGuideItem("Mono & Digliserida (Kode E471)", "Pengemulsi Es Krim, Margarin & Roti", "SYUBHAT ⚠️ CEK LOGO HALAL", "Bisa berasal dari minyak nabati (kelapa sawit) atau lemak hewani. Pilih produk yang telah bersertifikat Halal."),
    HalalGuideItem("Whey & Rennet Keju", "Bahan Pembuatan Keju & Biskuit", "SYUBHAT ⚠️ CEK LOGO HALAL", "Enzim rennet berasal dari lambung hewan. Pastikan berasal dari mikroba/nabati atau hewan yang disembelih secara syar'i."),
    HalalGuideItem("Kuas Roti / Bristle Brush", "Alat Oles Mentega & Kue", "SYUBHAT ⚠️ CEK KETERANGAN", "Pastikan menggunakan kuas silikon atau sintetis/nabati, bukan kuas bulu hewan yang tidak jelas sumbernya."),
    HalalGuideItem("Mirin, Sake & Angciu", "Bumbu Masak Jepang / Oriental", "HARAM ❌", "Mengandung khamr (alkohol hasil fermentasi beras) yang memabukkan sehingga haram meskipun hanya digunakan sedikit pada masakan."),
    HalalGuideItem("Lard / Shortening Hewani Babi", "Minyak & Lemak Masak", "HARAM ❌", "Lemak yang berasal dari babi (Porcine/Swine/Lard) hukumnya haram secara mutlak dalam Al-Quran."),
    HalalGuideItem("Karmin / Pewarna Alami Serangga (E120)", "Pewarna Merah Makanan/Yogurt", "HALAL / CEK FATWA ✅", "Menurut Fatwa MUI Nomor 33 Tahun 2011, pewarna makanan dari serangga Cochineal (E120) hukumnya halal selama bermanfaat dan tidak membahayakan.")
)