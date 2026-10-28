package id.my.pandakodedev.pandamuslim

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AsmaulHusnaItem(
    val number: Int,
    val arabic: String,
    val latin: String,
    val meaning: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsmaulHusnaScreen(onBack: () -> Unit) {
    val allItems = remember { get99AsmaulHusna() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedNumber by remember { mutableStateOf<Int?>(null) }

    val filteredItems = remember(allItems, searchQuery) {
        if (searchQuery.isBlank()) allItems
        else allItems.filter {
            it.latin.contains(searchQuery, ignoreCase = true) ||
                    it.meaning.contains(searchQuery, ignoreCase = true) ||
                    it.number.toString() == searchQuery.trim()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_menu_asmaul),
                            contentDescription = "Asmaul Husna",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("99 Asmaul Husna", fontWeight = FontWeight.ExtraBold, color = Color.White)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldDark)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari Asmaul Husna (contoh: Ar-Rahman, Maha Pengasih)...", fontSize = 13.sp) },
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems, key = { it.number }) { item ->
                    val isSelected = selectedNumber == item.number
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) EmeraldDark else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedNumber = if (isSelected) null else item.number
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) GoldAccent else MintSurface)
                            ) {
                                Text(
                                    text = "${item.number}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldDark
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = item.arabic,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GoldAccent else EmeraldDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.latin,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else EmeraldPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.meaning,
                                fontSize = 11.sp,
                                color = if (isSelected) MintSurface else Color.Gray,
                                textAlign = TextAlign.Center,
                                minLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

fun get99AsmaulHusna(): List<AsmaulHusnaItem> = listOf(
    AsmaulHusnaItem(1, "الرَّحْمَنُ", "Ar-Rahman", "Yang Maha Pengasih"),
    AsmaulHusnaItem(2, "الرَّحِيمُ", "Ar-Rahim", "Yang Maha Penyayang"),
    AsmaulHusnaItem(3, "الْمَلِكُ", "Al-Malik", "Yang Maha Merajai"),
    AsmaulHusnaItem(4, "الْقُدُّوسُ", "Al-Quddus", "Yang Maha Suci"),
    AsmaulHusnaItem(5, "السَّلَامُ", "As-Salam", "Yang Maha Memberi Kesejahteraan"),
    AsmaulHusnaItem(6, "الْمُؤْمِنُ", "Al-Mu'min", "Yang Maha Memberi Keamanan"),
    AsmaulHusnaItem(7, "الْمُهَيْمِنُ", "Al-Muhaimin", "Yang Maha Pemelihara"),
    AsmaulHusnaItem(8, "الْعَزِيزُ", "Al-Aziz", "Yang Maha Perkasa"),
    AsmaulHusnaItem(9, "الْجَبَّارُ", "Al-Jabbar", "Yang Memiliki Mutlak Kegagahan"),
    AsmaulHusnaItem(10, "الْمُتَكَبِّرُ", "Al-Mutakabbir", "Yang Maha Megah"),
    AsmaulHusnaItem(11, "الْخَالِقُ", "Al-Khaliq", "Yang Maha Pencipta"),
    AsmaulHusnaItem(12, "الْبَارِئُ", "Al-Bari'", "Yang Maha Melepaskan"),
    AsmaulHusnaItem(13, "الْمُصَوِّرُ", "Al-Mushawwir", "Yang Maha Membentuk Rupa"),
    AsmaulHusnaItem(14, "الْغَفَّارُ", "Al-Ghaffar", "Yang Maha Pengampun"),
    AsmaulHusnaItem(15, "الْقَهَّارُ", "Al-Qahhar", "Yang Maha Memaksa"),
    AsmaulHusnaItem(16, "الْوَهَّابُ", "Al-Wahhab", "Yang Maha Pemberi Karunia"),
    AsmaulHusnaItem(17, "الرَّزَّاقُ", "Ar-Razzaq", "Yang Maha Pemberi Rezeki"),
    AsmaulHusnaItem(18, "الْفَتَّاحُ", "Al-Fattah", "Yang Maha Pembuka Rahmat"),
    AsmaulHusnaItem(19, "الْعَلِيمُ", "Al-'Alim", "Yang Maha Mengetahui"),
    AsmaulHusnaItem(20, "الْقَابِضُ", "Al-Qabidh", "Yang Maha Menyempitkan"),
    AsmaulHusnaItem(21, "الْبَاسِطُ", "Al-Basith", "Yang Maha Melapangkan"),
    AsmaulHusnaItem(22, "الْخَافِضُ", "Al-Khafidh", "Yang Maha Merendahkan"),
    AsmaulHusnaItem(23, "الرَّافِعُ", "Ar-Rafi'", "Yang Maha Meninggikan"),
    AsmaulHusnaItem(24, "الْمُعِزُّ", "Al-Mu'izz", "Yang Maha Memuliakan"),
    AsmaulHusnaItem(25, "الْمُذِلُّ", "Al-Mudzil", "Yang Maha Menghinakan"),
    AsmaulHusnaItem(26, "السَّمِيعُ", "As-Sami'", "Yang Maha Mendengar"),
    AsmaulHusnaItem(27, "الْبَصِيرُ", "Al-Bashir", "Yang Maha Melihat"),
    AsmaulHusnaItem(28, "الْحَكَمُ", "Al-Hakam", "Yang Maha Menetapkan"),
    AsmaulHusnaItem(29, "الْعَدْلُ", "Al-'Adl", "Yang Maha Adil"),
    AsmaulHusnaItem(30, "اللَّطِيفُ", "Al-Lathif", "Yang Maha Lembut"),
    AsmaulHusnaItem(31, "الْخَبِيرُ", "Al-Khabir", "Yang Maha Mengenal"),
    AsmaulHusnaItem(32, "الْحَلِيمُ", "Al-Halim", "Yang Maha Penyantun"),
    AsmaulHusnaItem(33, "الْعَظِيمُ", "Al-'Azhim", "Yang Maha Agung"),
    AsmaulHusnaItem(34, "الْغَفُورُ", "Al-Ghafur", "Yang Maha Pengampun"),
    AsmaulHusnaItem(35, "الشَّكُورُ", "Asy-Syakur", "Yang Maha Pembalas Budi"),
    AsmaulHusnaItem(36, "الْعَلِيُّ", "Al-'Aliy", "Yang Maha Tinggi"),
    AsmaulHusnaItem(37, "الْكَبِيرُ", "Al-Kabir", "Yang Maha Besar"),
    AsmaulHusnaItem(38, "الْحَفِيظُ", "Al-Hafizh", "Yang Maha Memelihara"),
    AsmaulHusnaItem(39, "الْمُقِيتُ", "Al-Muqit", "Yang Maha Pemberi Kecukupan"),
    AsmaulHusnaItem(40, "الْحَسِيبُ", "Al-Hasib", "Yang Maha Membuat Perhitungan"),
    AsmaulHusnaItem(41, "الْجَلِيلُ", "Al-Jalil", "Yang Maha Luhur"),
    AsmaulHusnaItem(42, "الْكَرِيمُ", "Al-Karim", "Yang Maha Pemurah"),
    AsmaulHusnaItem(43, "الرَّقِيبُ", "Ar-Raqib", "Yang Maha Mengawasi"),
    AsmaulHusnaItem(44, "الْمُجِيبُ", "Al-Mujib", "Yang Maha Mengabulkan"),
    AsmaulHusnaItem(45, "الْوَاسِعُ", "Al-Wasi'", "Yang Maha Luas"),
    AsmaulHusnaItem(46, "الْحَكِيمُ", "Al-Hakim", "Yang Maha Bijaksana"),
    AsmaulHusnaItem(47, "الْوَدُودُ", "Al-Wadud", "Yang Maha Mengasihi"),
    AsmaulHusnaItem(48, "الْمَجِيدُ", "Al-Majid", "Yang Maha Mulia"),
    AsmaulHusnaItem(49, "الْبَاعِثُ", "Al-Ba'its", "Yang Maha Membangkitkan"),
    AsmaulHusnaItem(50, "الشَّهِيدُ", "Asy-Syahid", "Yang Maha Menyaksikan"),
    AsmaulHusnaItem(51, "الْحَقُّ", "Al-Haqq", "Yang Maha Benar"),
    AsmaulHusnaItem(52, "الْوَكِيلُ", "Al-Wakil", "Yang Maha Memelihara"),
    AsmaulHusnaItem(53, "الْقَوِيُّ", "Al-Qawiyyu", "Yang Maha Kuat"),
    AsmaulHusnaItem(54, "الْمَتِينُ", "Al-Matin", "Yang Maha Kokoh"),
    AsmaulHusnaItem(55, "الْوَلِيُّ", "Al-Waliy", "Yang Maha Melindungi"),
    AsmaulHusnaItem(56, "الْحَمِيدُ", "Al-Hamid", "Yang Maha Terpuji"),
    AsmaulHusnaItem(57, "الْمُحْصِي", "Al-Muhshi", "Yang Maha Menghitung"),
    AsmaulHusnaItem(58, "الْمُبْدِئُ", "Al-Mubdi'", "Yang Maha Memulai"),
    AsmaulHusnaItem(59, "الْمُعِيدُ", "Al-Mu'id", "Yang Maha Mengembalikan Kehidupan"),
    AsmaulHusnaItem(60, "الْمُحْيِي", "Al-Muhyi", "Yang Maha Menghidupkan"),
    AsmaulHusnaItem(61, "الْمُمِيتُ", "Al-Mumit", "Yang Maha Mematikan"),
    AsmaulHusnaItem(62, "الْحَيُّ", "Al-Hayyu", "Yang Maha Hidup"),
    AsmaulHusnaItem(63, "الْقَيُّومُ", "Al-Qayyum", "Yang Maha Mandiri"),
    AsmaulHusnaItem(64, "الْوَاجِدُ", "Al-Wajid", "Yang Maha Penemu"),
    AsmaulHusnaItem(65, "الْمَاجِدُ", "Al-Majid", "Yang Maha Mulia"),
    AsmaulHusnaItem(66, "الْوَاحِدُ", "Al-Wahid", "Yang Maha Tunggal"),
    AsmaulHusnaItem(67, "الْأَحَدُ", "Al-Ahad", "Yang Maha Esa"),
    AsmaulHusnaItem(68, "الصَّمَدُ", "Ash-Shamad", "Yang Maha Dibutuhkan"),
    AsmaulHusnaItem(69, "الْقَادِرُ", "Al-Qadir", "Yang Maha Menentukan"),
    AsmaulHusnaItem(70, "الْمُقْتَدِرُ", "Al-Muqtadir", "Yang Maha Berkuasa"),
    AsmaulHusnaItem(71, "الْمُقَدِّمُ", "Al-Muqaddim", "Yang Maha Mendahulukan"),
    AsmaulHusnaItem(72, "الْمُؤَخِّرُ", "Al-Mu'akkhir", "Yang Maha Mengakhirkan"),
    AsmaulHusnaItem(73, "الْأَوَّلُ", "Al-Awwal", "Yang Maha Awal"),
    AsmaulHusnaItem(74, "الْآخِرُ", "Al-Akhir", "Yang Maha Akhir"),
    AsmaulHusnaItem(75, "الظَّاهِرُ", "Azh-Zhahir", "Yang Maha Nyata"),
    AsmaulHusnaItem(76, "الْبَاطِنُ", "Al-Bathin", "Yang Maha Ghaib"),
    AsmaulHusnaItem(77, "الْوَالِي", "Al-Wali", "Yang Maha Memerintah"),
    AsmaulHusnaItem(78, "الْمُتَعَالِي", "Al-Muta'ali", "Yang Maha Tinggi"),
    AsmaulHusnaItem(79, "الْبَرُّ", "Al-Barr", "Yang Maha Penderma"),
    AsmaulHusnaItem(80, "التَّوَّابُ", "At-Tawwab", "Yang Maha Penerima Tobat"),
    AsmaulHusnaItem(81, "الْمُنْتَقِمُ", "Al-Muntaqim", "Yang Maha Pemberi Balasan"),
    AsmaulHusnaItem(82, "الْعَفُوُّ", "Al-'Afuww", "Yang Maha Pemaaf"),
    AsmaulHusnaItem(83, "الرَّؤُوفُ", "Ar-Ra'uf", "Yang Maha Pengasih"),
    AsmaulHusnaItem(84, "مَالِكُ الْمُلْكِ", "Malikul-Mulk", "Yang Maha Penguasa Kerajaan"),
    AsmaulHusnaItem(85, "ذُو الْجَلَالِ وَالْإِكْرَامِ", "Dzul-Jalali wal-Ikram", "Yang Maha Pemilik Kebesaran dan Kemuliaan"),
    AsmaulHusnaItem(86, "الْمُقْسِطُ", "Al-Muqsith", "Yang Maha Pemberi Keadilan"),
    AsmaulHusnaItem(87, "الْجَامِعُ", "Al-Jami'", "Yang Maha Mengumpulkan"),
    AsmaulHusnaItem(88, "الْغَنِيُّ", "Al-Ghaniyy", "Yang Maha Kaya"),
    AsmaulHusnaItem(89, "الْمُغْنِي", "Al-Mughni", "Yang Maha Pemberi Kekayaan"),
    AsmaulHusnaItem(90, "الْمَانِعُ", "Al-Mani'", "Yang Maha Mencegah"),
    AsmaulHusnaItem(91, "الضَّارُّ", "Adh-Dharr", "Yang Maha Penimpa Kemudaratan"),
    AsmaulHusnaItem(92, "النَّافِعُ", "An-Nafi'", "Yang Maha Memberi Manfaat"),
    AsmaulHusnaItem(93, "النُّورُ", "An-Nur", "Yang Maha Bercahaya"),
    AsmaulHusnaItem(94, "الْهَادِي", "Al-Hadi", "Yang Maha Pemberi Petunjuk"),
    AsmaulHusnaItem(95, "الْبَدِيعُ", "Al-Badi'", "Yang Maha Pencipta yang Tiada Bandingannya"),
    AsmaulHusnaItem(96, "الْبَاقِي", "Al-Baqi", "Yang Maha Kekal"),
    AsmaulHusnaItem(97, "الْوَارِثُ", "Al-Warits", "Yang Maha Pewaris"),
    AsmaulHusnaItem(98, "الرَّشِيدُ", "Ar-Rasyid", "Yang Maha Pandai"),
    AsmaulHusnaItem(99, "الصَّبُورُ", "Ash-Shabur", "Yang Maha Sabar")
)