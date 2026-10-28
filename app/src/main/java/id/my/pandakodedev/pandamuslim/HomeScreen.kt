package id.my.pandakodedev.pandamuslim

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

// Palet Warna Default Global (Digunakan juga oleh layar lain)
val EmeraldDark = Color(0xFF004D40)
val EmeraldPrimary = Color(0xFF00897B)
val MintSurface = Color(0xFFE0F2F1)
val GoldAccent = Color(0xFFFFCA28)
val OffWhiteBg = Color(0xFFF4FBF9)

// Sistem 4 Tema Unik & Ramah Anak-Anak
enum class KidsAppTheme(
    val label: String,
    val emoji: String,
    val headerBg: Color,
    val primaryColor: Color,
    val accentColor: Color,
    val surfaceColor: Color,
    val bodyBg: Color
) {
    ZAMRUD(
        label = "Zamrud Nabawi",
        emoji = "🌿",
        headerBg = Color(0xFF004D40),
        primaryColor = Color(0xFF00897B),
        accentColor = Color(0xFFFFCA28),
        surfaceColor = Color(0xFFE0F2F1),
        bodyBg = Color(0xFFF4FBF9)
    ),
    CERIA_ANAK(
        label = "Ceria Anak Sholeh",
        emoji = "🎈",
        headerBg = Color(0xFF0284C7),
        primaryColor = Color(0xFF0EA5E9),
        accentColor = Color(0xFFFACC15),
        surfaceColor = Color(0xFFE0F2FE),
        bodyBg = Color(0xFFF0F9FF)
    ),
    MALAM_BINTANG(
        label = "Malam Bintang",
        emoji = "🌙",
        headerBg = Color(0xFF311042),
        primaryColor = Color(0xFF6B21A8),
        accentColor = Color(0xFFFDE047),
        surfaceColor = Color(0xFFF3E8FF),
        bodyBg = Color(0xFFFAF5FF)
    ),
    PINK_MUSLIMAH(
        label = "Pinky Muslimah",
        emoji = "🌸",
        headerBg = Color(0xFF9D174D),
        primaryColor = Color(0xFFDB2777),
        accentColor = Color(0xFFFDE047),
        surfaceColor = Color(0xFFFCE7F3),
        bodyBg = Color(0xFFFFF1F2)
    )
}

// Data Model Menu & Jadwal Sholat
data class MenuItem(val title: String, @DrawableRes val iconRes: Int, val badgeColor: Color)
data class PrayerTime(val name: String, val time: String, val isNext: Boolean = false)

@Composable
fun PandaMuslimHomeScreen(
    onMenuClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("panda_theme_prefs", Context.MODE_PRIVATE) }

    // State Tema Aktif
    var currentTheme by remember {
        val savedName = prefs.getString("selected_theme", KidsAppTheme.CERIA_ANAK.name)
        mutableStateOf(
            try { KidsAppTheme.valueOf(savedName!!) } catch (_: Exception) { KidsAppTheme.CERIA_ANAK }
        )
    }

    val animatedHeaderBg by animateColorAsState(targetValue = currentTheme.headerBg, label = "header_bg")
    val animatedBodyBg by animateColorAsState(targetValue = currentTheme.bodyBg, label = "body_bg")

    // 16 Menu dengan Warna Latar Pastel Warna-Warni agar Anak-Anak Senang!
    val menuList = remember {
        listOf(
            MenuItem("Al-Quran", R.drawable.ic_panda_quran, Color(0xFFD1FAE5)),
            MenuItem("Tajwid", R.drawable.ic_menu_tajwid, Color(0xFFFEF3C7)),
            MenuItem("Doa", R.drawable.ic_panda_doa, Color(0xFFE0E7FF)),
            MenuItem("Kiblat", R.drawable.ic_panda_kiblat, Color(0xFFFCE7F3)),
            MenuItem("Tasbih", R.drawable.ic_panda_tasbih, Color(0xFFCCFBF1)),
            MenuItem("Video", R.drawable.ic_menu_video, Color(0xFFFFE4E6)),
            MenuItem("Asmaul", R.drawable.ic_menu_asmaul, Color(0xFFFEF9C3)),
            MenuItem("Belajar", R.drawable.ic_menu_belajar, Color(0xFFDBEAFE)),
            MenuItem("Amalan", R.drawable.ic_panda_amalan, Color(0xFFDCFCE7)),
            MenuItem("Kalkulator", R.drawable.ic_menu_kalkulator, Color(0xFFF3E8FF)),
            MenuItem("Khatam", R.drawable.ic_menu_khatam, Color(0xFFFFEDD5)),
            MenuItem("Komunitas", R.drawable.ic_panda_komunitas, Color(0xFFE0F2FE)),
            MenuItem("FAQ", R.drawable.ic_menu_faq, Color(0xFFFAE8FF)),
            MenuItem("Masukan", R.drawable.ic_menu_masukan, Color(0xFFECFCCB)),
            MenuItem("Mewarnai", R.drawable.ic_menu_mewarnai, Color(0xFFFFE4E6)),
            MenuItem("Iqro", R.drawable.ic_menu_iqro, Color(0xFFD1FAE5))
        )
    }

    // Jam Digital Real-Time
    var currentTimeStr by remember { mutableStateOf("00:00") }
    var currentDateStr by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        while (true) {
            val now = Date()
            currentTimeStr = timeFormat.format(now)
            currentDateStr = dateFormat.format(now)
            delay(10_000L)
        }
    }

    var currentCity by remember { mutableStateOf("Pekanbaru") }
    var prayerTimes by remember {
        mutableStateOf(
            listOf(
                PrayerTime("Imsak", "04:26"),
                PrayerTime("Shubuh", "04:36"),
                PrayerTime("Dzuhur", "11:58"),
                PrayerTime("Ashar", "15:15"),
                PrayerTime("Maghrib", "18:02"),
                PrayerTime("Isya", "19:12", isNext = true)
            )
        )
    }

    // Pemutar Suara Adzan
    var isPlayingAdzan by remember { mutableStateOf(false) }
    var isPreparingAdzan by remember { mutableStateOf(false) }
    val adzanPlayer = remember { MediaPlayer() }

    fun toggleAdzanSound() {
        if (isPlayingAdzan || isPreparingAdzan) {
            try {
                if (adzanPlayer.isPlaying) adzanPlayer.stop()
                adzanPlayer.reset()
            } catch (_: Exception) {}
            isPlayingAdzan = false
            isPreparingAdzan = false
        } else {
            try {
                isPreparingAdzan = true
                adzanPlayer.reset()
                adzanPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                adzanPlayer.setDataSource("https://www.islamcan.com/audio/adhan/azan1.mp3")
                adzanPlayer.prepareAsync()
                adzanPlayer.setOnPreparedListener { mp ->
                    isPreparingAdzan = false
                    isPlayingAdzan = true
                    mp.start()
                }
                adzanPlayer.setOnCompletionListener {
                    isPlayingAdzan = false
                }
            } catch (_: Exception) {
                isPreparingAdzan = false
                isPlayingAdzan = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { adzanPlayer.release() } catch (_: Exception) {}
        }
    }

    fun loadPrayerScheduleByCity(city: String) {
        currentCity = city
        CoroutineScope(Dispatchers.IO).launch {
            val fetched = fetchPrayerTimesByCity(city)
            if (fetched.isNotEmpty()) {
                withContext(Dispatchers.Main) { prayerTimes = fetched }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun detectGpsLocationAndSchedule() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val locManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (loc != null) {
                    val geocoder = Geocoder(context, Locale("id", "ID"))
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    val detectedCity = addresses?.firstOrNull()?.subAdminArea
                        ?: addresses?.firstOrNull()?.locality
                        ?: "Lokasi GPS"
                    val cleanCity = detectedCity.replace("Kota ", "").replace("Kabupaten ", "")
                    val schedule = fetchPrayerTimesByCoords(loc.latitude, loc.longitude)
                    withContext(Dispatchers.Main) {
                        currentCity = cleanCity
                        if (schedule.isNotEmpty()) prayerTimes = schedule
                    }
                } else {
                    loadPrayerScheduleByCity(currentCity)
                }
            } catch (_: Exception) {
                loadPrayerScheduleByCity(currentCity)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) detectGpsLocationAndSchedule()
        else loadPrayerScheduleByCity(currentCity)
    }

    LaunchedEffect(Unit) {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) detectGpsLocationAndSchedule()
        else loadPrayerScheduleByCity(currentCity)
    }

    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showCityPickerDialog by remember { mutableStateOf(false) }

    val nextPrayer = remember(prayerTimes) {
        prayerTimes.find { it.isNext } ?: prayerTimes.last()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedHeaderBg)
    ) {
        // Animasi Bintang Berkilau Melayang di Latar Belakang Atas
        AnimatedStarryHeaderCanvas()

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item(key = "header") {
                HeaderSectionKids(
                    theme = currentTheme,
                    onSelectTheme = { chosenTheme ->
                        currentTheme = chosenTheme
                        prefs.edit().putString("selected_theme", chosenTheme.name).apply()
                    },
                    city = currentCity,
                    date = currentDateStr,
                    currentTime = currentTimeStr,
                    nextPrayerText = "Menuju waktu ${nextPrayer.name} (${nextPrayer.time} WIB)",
                    isPlayingAdzan = isPlayingAdzan,
                    isPreparingAdzan = isPreparingAdzan,
                    onToggleAdzan = { toggleAdzanSound() },
                    onClickLocation = { showCityPickerDialog = true }
                )
            }

            item(key = "main_body") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                        .background(animatedBodyBg)
                        .padding(16.dp)
                ) {
                    // Balon Sapaan Interaktif Maskot Panda untuk Anak-Anak
                    KidsGreetingBubble(theme = currentTheme)
                    Spacer(modifier = Modifier.height(14.dp))

                    BannerSection3D(theme = currentTheme)
                    Spacer(modifier = Modifier.height(18.dp))

                    // Kartu Highlight Donasi & Infaq Jariyah
                    DonasiHighlightBanner(
                        theme = currentTheme,
                        onClickDonasi = { onMenuClick("Donasi") }
                    )

                    Spacer(modifier = Modifier.height(22.dp))
                    SectionTitle(title = "✨ Petualangan Belajar & Ibadah", color = currentTheme.headerBg)
                    Spacer(modifier = Modifier.height(14.dp))

                    MenuGridSection3D(
                        menus = menuList,
                        theme = currentTheme,
                        onMenuClick = { menuTitle ->
                            if (menuTitle == "Masukan") {
                                showFeedbackDialog = true
                            } else {
                                onMenuClick(menuTitle)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle(title = "🕌 Jadwal Sholat • $currentCity", color = currentTheme.headerBg)
                        TextButton(onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        }) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = currentTheme.primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GPS Otomatis", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = currentTheme.primaryColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    PrayerTimesRow(prayerTimes = prayerTimes, theme = currentTheme)
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }

    if (showFeedbackDialog) {
        FeedbackDialog(onDismiss = { showFeedbackDialog = false })
    }

    if (showCityPickerDialog) {
        CityPickerDialog(
            currentCity = currentCity,
            onSelectCity = { chosen ->
                showCityPickerDialog = false
                loadPrayerScheduleByCity(chosen)
            },
            onUseGps = {
                showCityPickerDialog = false
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            },
            onDismiss = { showCityPickerDialog = false }
        )
    }
}

@Composable
fun AnimatedStarryHeaderCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars_anim")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        val starPositions = listOf(
            Offset(0.12f, 0.20f), Offset(0.28f, 0.12f), Offset(0.48f, 0.25f),
            Offset(0.68f, 0.15f), Offset(0.85f, 0.22f), Offset(0.22f, 0.45f),
            Offset(0.75f, 0.42f), Offset(0.92f, 0.35f), Offset(0.08f, 0.38f)
        )
        starPositions.forEachIndexed { index, pos ->
            val offsetY = sin(animPhase + index) * 8f
            val radius = if (index % 2 == 0) 6f else 4f
            drawCircle(
                color = Color(0xFFFFF59D).copy(alpha = 0.45f),
                radius = radius,
                center = Offset(size.width * pos.x, (size.height * pos.y) + offsetY)
            )
        }
    }
}

@Composable
fun HeaderSectionKids(
    theme: KidsAppTheme,
    onSelectTheme: (KidsAppTheme) -> Unit,
    city: String,
    date: String,
    currentTime: String,
    nextPrayerText: String,
    isPlayingAdzan: Boolean,
    isPreparingAdzan: Boolean,
    onToggleAdzan: () -> Unit,
    onClickLocation: () -> Unit
) {
    // Animasi Mengambang Lembut pada Maskot Panda
    val infiniteTransition = rememberInfiniteTransition(label = "panda_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "panda_y"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        // Baris Pemilih Tema Warna Ceria Anak-Anak
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(KidsAppTheme.values()) { itemTheme ->
                val isSelected = itemTheme == theme
                Surface(
                    color = if (isSelected) theme.accentColor else Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { onSelectTheme(itemTheme) }
                ) {
                    Text(
                        text = "${itemTheme.emoji} ${itemTheme.label}",
                        color = if (isSelected) Color(0xFF0F172A) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.clickable { onClickLocation() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Lokasi",
                        tint = theme.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$city • $date ▾",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                color = if (isPlayingAdzan) Color(0xFFE11D48) else theme.accentColor,
                shape = RoundedCornerShape(50),
                modifier = Modifier.clickable { onToggleAdzan() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isPlayingAdzan) Icons.Default.Close else Icons.Default.Notifications,
                        contentDescription = "Suara Adzan",
                        tint = if (isPlayingAdzan) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            isPreparingAdzan -> "Memuat..."
                            isPlayingAdzan -> "Stop Adzan"
                            else -> "🔊 Suara Adzan"
                        },
                        color = if (isPlayingAdzan) Color.White else Color(0xFF0F172A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentTime,
                    color = Color.White,
                    fontSize = 46.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = nextPrayerText,
                    color = theme.surfaceColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Maskot Panda dengan Animasi Melayang 3D
            Image(
                painter = painterResource(id = R.drawable.ic_panda_home),
                contentDescription = "Avatar Panda",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(78.dp)
                    .graphicsLayer {
                        translationY = floatOffset
                    }
                    .clip(CircleShape)
                    .border(3.dp, theme.accentColor, CircleShape)
                    .background(theme.surfaceColor)
            )
        }
    }
}

@Composable
fun KidsGreetingBubble(theme: KidsAppTheme) {
    val quotes = remember {
        listOf(
            "🐼 Assalamu'alaikum Sahabat Kecil! Yuk ngaji Iqro & Al-Quran bareng Panda! 🌟",
            "🐼 Jangan lupa senyum dan bantu Ayah Bunda hari ini ya! pahala besar menantimu! 🍬",
            "🐼 Yuk warnai gambar Masjid dan putar mode Hafalan Al-Quran 5x! 🎨"
        )
    }
    var quoteIndex by remember { mutableStateOf(0) }

    Surface(
        color = theme.surfaceColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .clickable { quoteIndex = (quoteIndex + 1) % quotes.size }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = quotes[quoteIndex],
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = theme.headerBg,
                modifier = Modifier.weight(1f)
            )
            Text("Ketuk 👆", fontSize = 10.sp, color = theme.primaryColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionTitle(title: String, color: Color = EmeraldDark) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = color
    )
}

@Composable
fun MenuGridSection3D(menus: List<MenuItem>, theme: KidsAppTheme, onMenuClick: (String) -> Unit) {
    val chunkedMenus = remember(menus) { menus.chunked(4) }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, theme.surfaceColor, RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            chunkedMenus.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { item ->
                        key(item.title) {
                            Bouncy3DMenuIcon(
                                item = item,
                                modifier = Modifier.weight(1f),
                                onClick = { onMenuClick(item.title) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Bouncy3DMenuIcon(item: MenuItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.80f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncy_scale"
    )

    val rotationZ by animateFloatAsState(
        targetValue = if (isPressed) -8f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bouncy_rot"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.rotationZ = rotationZ
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(item.badgeColor)
                .border(2.dp, Color.White, RoundedCornerShape(22.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun BannerSection3D(theme: KidsAppTheme) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(theme.headerBg, theme.primaryColor)
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = theme.accentColor,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "🌟 ANAK SHOLEH & SHOLEHAH",
                                color = Color(0xFF0F172A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PandaMuslim Ceria",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "رَبِّ زِدْنِي عِلْمًا وَارْزُقْنِي فَهِمًا",
                            color = theme.surfaceColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.ic_panda_home),
                        contentDescription = "Panda Maskot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .border(2.dp, theme.accentColor, CircleShape)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surfaceColor)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "\"Ya Allah, tambahkanlah ilmuku dan berikanlah aku pemahaman yang baik!\"",
                    color = theme.headerBg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun DonasiHighlightBanner(theme: KidsAppTheme, onClickDonasi: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDonasi() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(theme.headerBg, theme.primaryColor)
                    )
                )
                .border(2.dp, theme.accentColor, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = theme.accentColor,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "💝 CELENGAN SURGA & INFAQ JARIYAH",
                        color = Color(0xFF0F172A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text("BSI • DANA • Shopee • GoPay", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_panda_amalan),
                    contentDescription = "Panda Donasi",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .border(2.dp, theme.accentColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "\"Sedekah tidak akan mengurangi harta sedikit pun...\"",
                        color = theme.accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        text = "Dukung pengembangan fitur belajar anak & server dakwah PandaMuslim.",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onClickDonasi,
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Salurkan Infaq Terbaik Sekarang ✨",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun PrayerTimesRow(prayerTimes: List<PrayerTime>, theme: KidsAppTheme) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(prayerTimes, key = { it.name }) { prayer ->
            val bgColor = if (prayer.isNext) theme.primaryColor else Color.White
            val textColor = if (prayer.isNext) Color.White else theme.headerBg

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.width(94.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = prayer.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = prayer.time,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (prayer.isNext) theme.accentColor else theme.primaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun CityPickerDialog(
    currentCity: String,
    onSelectCity: (String) -> Unit,
    onUseGps: () -> Unit,
    onDismiss: () -> Unit
) {
    val popularCities = listOf("Pekanbaru", "Jakarta", "Bandung", "Surabaya", "Medan", "Padang", "Palembang", "Makassar", "Yogyakarta", "Balikpapan")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Lokasi Jadwal Sholat 📍", fontWeight = FontWeight.ExtraBold, color = EmeraldDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onUseGps,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gunakan Deteksi GPS Otomatis")
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Atau pilih kota secara manual:", fontSize = 12.sp, color = Color.Gray)
                LazyColumn(modifier = Modifier.height(220.dp)) {
                    items(popularCities) { city ->
                        TextButton(
                            onClick = { onSelectCity(city) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (city == currentCity) "✅ $city" else city,
                                fontWeight = if (city == currentCity) FontWeight.ExtraBold else FontWeight.Medium,
                                color = EmeraldDark,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

suspend fun fetchPrayerTimesByCoords(lat: Double, lng: Double): List<PrayerTime> = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.aladhan.com/v1/timings?latitude=$lat&longitude=$lng&method=20")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().readText()
            return@withContext parseAladhanTimings(response)
        }
    } catch (_: Exception) {}
    emptyList()
}

suspend fun fetchPrayerTimesByCity(city: String): List<PrayerTime> = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.aladhan.com/v1/timingsByCity?city=$city&country=Indonesia&method=20")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().readText()
            return@withContext parseAladhanTimings(response)
        }
    } catch (_: Exception) {}
    emptyList()
}

fun parseAladhanTimings(jsonResponse: String): List<PrayerTime> {
    val timings = JSONObject(jsonResponse).getJSONObject("data").getJSONObject("timings")
    val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    val rawList = listOf(
        "Imsak" to timings.optString("Imsak", "04:25").take(5),
        "Shubuh" to timings.optString("Fajr", "04:35").take(5),
        "Dzuhur" to timings.optString("Dhuhr", "12:00").take(5),
        "Ashar" to timings.optString("Asr", "15:15").take(5),
        "Maghrib" to timings.optString("Maghrib", "18:02").take(5),
        "Isya" to timings.optString("Isha", "19:12").take(5)
    )

    var nextFound = false
    return rawList.map { (name, time) ->
        val isNext = if (!nextFound && time > nowTime) {
            nextFound = true
            true
        } else false
        PrayerTime(name, time, isNext)
    }
}