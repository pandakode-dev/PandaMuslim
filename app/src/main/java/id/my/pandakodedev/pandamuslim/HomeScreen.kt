package id.my.pandakodedev.pandamuslim

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val IconMemoryCache = mutableMapOf<Int, ImageBitmap>()

@Composable
fun rememberOptimizedIcon(@DrawableRes resId: Int, targetPx: Int = 140): ImageBitmap {
    val context = LocalContext.current
    return remember(resId) {
        IconMemoryCache.getOrPut(resId) {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, resId, opts)
            var sampleSize = 1
            while (opts.outWidth / sampleSize > targetPx * 2 || opts.outHeight / sampleSize > targetPx * 2) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeResource(context.resources, resId, decodeOpts).asImageBitmap()
        }
    }
}

data class MenuItem(val title: String, @DrawableRes val iconRes: Int, val badgeColor: Color)
data class PrayerTime(val name: String, val time: String, val isNext: Boolean = false)
data class HomeBannerSlide(
    val id: Int,
    val badge: String,
    val title: String,
    val arabicText: String,
    val subtitle: String,
    val imageUrl: String = ""
)

data class ExtraFeatureCardItem(
    val routeKey: String,
    @DrawableRes val iconRes: Int,
    val title: String,
    val subtitle: String,
    val bgColor: Color
)

@Composable
fun PandaMuslimHomeScreen(
    currentTheme: KidsAppTheme = LocalPandaTheme.current,
    onSelectTheme: (KidsAppTheme) -> Unit = {},
    onMenuClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("panda_auth_prefs", Context.MODE_PRIVATE) }
    val mengajiPrefs = remember { context.getSharedPreferences("panda_mengaji_prefs", Context.MODE_PRIVATE) }

    var loggedUser by remember {
        val savedEmail = authPrefs.getString("user_email", null)
        if (!savedEmail.isNullOrBlank()) {
            mutableStateOf<LoggedInUser?>(
                LoggedInUser(
                    name = authPrefs.getString("user_name", "Sahabat Panda") ?: "Sahabat Panda",
                    email = savedEmail,
                    role = authPrefs.getString("user_role", "user") ?: "user",
                    status = authPrefs.getString("user_status", "approved") ?: "approved"
                )
            )
        } else {
            mutableStateOf<LoggedInUser?>(null)
        }
    }

    val lastQuranMark = remember(loggedUser) {
        mengajiPrefs.getString("last_quran_label", "Al-Fatihah : Ayat 1") ?: "Al-Fatihah : Ayat 1"
    }
    val lastIqroMark = remember(loggedUser) {
        mengajiPrefs.getString("last_iqro_label", "Iqro 1 • Hal 1") ?: "Iqro 1 • Hal 1"
    }

    // MURNI MENGAMBIL BANNER DARI WEB ADMIN SAJA (Tanpa mencampur banner bawaan Android!)
    var bannerSlides by remember { mutableStateOf<List<HomeBannerSlide>>(emptyList()) }
    var isLoadingBanners by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoadingBanners = true
        val serverBanners = fetchContentsFromServer("banner")
        bannerSlides = serverBanners.map { b ->
            HomeBannerSlide(
                id = b.id,
                badge = if (b.latinText.isNotBlank()) b.latinText else "✨ INFO PANDAMUSLIM",
                title = b.title,
                arabicText = b.arabicText,
                subtitle = b.description.ifBlank { "Selamat beribadah bersama PandaMuslim!" },
                imageUrl = extractMediaPreviewUrl(b.mediaUrl)
            )
        }
        isLoadingBanners = false
    }

    val menuRows = remember {
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
        ).chunked(4)
    }

    var currentTimeStr by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }
    var currentDateStr by remember {
        mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date()))
    }

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
    var rawPrayerSchedule by remember {
        mutableStateOf(
            listOf(
                "Imsak" to "04:26",
                "Shubuh" to "04:36",
                "Dzuhur" to "11:58",
                "Ashar" to "15:15",
                "Maghrib" to "18:02",
                "Isya" to "19:12"
            )
        )
    }

    val prayerTimes = remember(rawPrayerSchedule, currentTimeStr) {
        calculateActivePrayerList(rawPrayerSchedule, currentTimeStr)
    }

    val nextPrayerBannerText = remember(prayerTimes, currentTimeStr) {
        buildNextPrayerCountdownText(prayerTimes, currentTimeStr)
    }

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
                adzanPlayer.setOnCompletionListener { isPlayingAdzan = false }
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
            val fetched = fetchRawPrayerTimesByCity(city)
            if (fetched.isNotEmpty()) {
                withContext(Dispatchers.Main) { rawPrayerSchedule = fetched }
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
                        ?: "Pekanbaru"
                    val cleanCity = detectedCity.replace("Kota ", "").replace("Kabupaten ", "")
                    val schedule = fetchRawPrayerTimesByCoords(loc.latitude, loc.longitude)
                    withContext(Dispatchers.Main) {
                        currentCity = cleanCity
                        if (schedule.isNotEmpty()) rawPrayerSchedule = schedule
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
    var showAuthDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.bodyBg)
    ) {
        item(key = "top_header") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(currentTheme.headerBg, currentTheme.primaryColor)
                        )
                    )
            ) {
                MosqueSilhouetteOrnamentCanvas(
                    accentColor = currentTheme.accentColor,
                    modifier = Modifier.matchParentSize()
                )

                HeaderSectionFast(
                    theme = currentTheme,
                    onSelectTheme = onSelectTheme,
                    loggedUser = loggedUser,
                    onClickAuth = { showAuthDialog = true },
                    onClickAdminPanel = { onMenuClick("AdminPanel") },
                    city = currentCity,
                    date = currentDateStr,
                    currentTime = currentTimeStr,
                    nextPrayerText = nextPrayerBannerText,
                    isPlayingAdzan = isPlayingAdzan,
                    isPreparingAdzan = isPreparingAdzan,
                    onToggleAdzan = { toggleAdzanSound() },
                    onClickLocation = { showCityPickerDialog = true }
                )
            }
        }

        item(key = "banner_slider_section") {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                if (bannerSlides.isNotEmpty()) {
                    BannerCarouselSlider3D(
                        slides = bannerSlides,
                        theme = currentTheme
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                } else if (isLoadingBanners) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(currentTheme.surfaceColor)
                    ) {
                        CircularProgressIndicator(color = currentTheme.primaryColor, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                MengajiBookmarkTrackerCard(
                    theme = currentTheme,
                    lastQuran = lastQuranMark,
                    lastIqro = lastIqroMark,
                    onContinueQuran = { onMenuClick("Al-Quran") },
                    onContinueIqro = { onMenuClick("Iqro") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitleWithOrnament(title = "Menu Petualangan Islami", theme = currentTheme)
            }
        }

        items(menuRows.size, key = { idx -> "menu_row_$idx" }) { rowIdx ->
            val rowItems = menuRows[rowIdx]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, currentTheme.surfaceColor, RoundedCornerShape(22.dp))
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowItems.forEach { item ->
                    FastMenuIconItem(
                        item = item,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (item.title == "Masukan") showFeedbackDialog = true
                            else onMenuClick(item.title)
                        }
                    )
                }
            }
        }

        item(key = "bottom_sections") {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                SixNewFeaturesGridSection(
                    theme = currentTheme,
                    onSelectFeature = { route -> onMenuClick(route) }
                )
                Spacer(modifier = Modifier.height(18.dp))
                DonasiHighlightBanner(
                    theme = currentTheme,
                    onClickDonasi = { onMenuClick("Donasi") }
                )
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitleWithOrnament(title = "Jadwal Sholat • $currentCity", theme = currentTheme)
                    Surface(
                        color = currentTheme.surfaceColor,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = currentTheme.primaryColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Update GPS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = currentTheme.headerBg,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                PrayerTimesRow(prayerTimes = prayerTimes, theme = currentTheme)
                Surface(
                    color = GoldAccent,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { onMenuClick("Imsakiyah") }
                ) {
                    Text(
                        text = "🌙 Jadwal Imsakiyah & Puasa",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showFeedbackDialog) {
        FeedbackDialog(
            loggedUser = loggedUser,
            onDismiss = { showFeedbackDialog = false }
        )
    }

    if (showAuthDialog) {
        AuthLoginRegisterDialog(
            currentUser = loggedUser,
            onLoginSuccess = { user ->
                loggedUser = user
                authPrefs.edit()
                    .putString("user_name", user.name)
                    .putString("user_email", user.email)
                    .putString("user_role", user.role)
                    .putString("user_status", user.status)
                    .apply()
            },
            onLogout = {
                loggedUser = null
                authPrefs.edit().clear().apply()
                onMenuClick("LOGOUT")
            },
            onOpenAdminPanel = { onMenuClick("AdminPanel") },
            onDismiss = { showAuthDialog = false }
        )
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
fun SixNewFeaturesGridSection(
    theme: KidsAppTheme,
    onSelectFeature: (String) -> Unit
) {
    val extraFeatures = remember {
        listOf(
            ExtraFeatureCardItem("KisahNabi", R.drawable.ic_menu_kisah_nabi, "Kisah 25 Nabi", "Audio Dongeng Teladan", Color(0xFFFEF3C7)),
            ExtraFeatureCardItem("KuisIslami", R.drawable.ic_menu_kuis_islami, "Kuis Pintar", "Game Tebak Ayat & Asmaul", Color(0xFFD1FAE5)),
            ExtraFeatureCardItem("KalenderHijriah", R.drawable.ic_menu_kalender_hijriah, "Celengan Amal", "Kalender Hijriah & Bintang", Color(0xFFE0F2FE)),
            ExtraFeatureCardItem("KamusHalal", R.drawable.ic_menu_kamus_halal, "Kamus Halal", "Cek Bahan & Kode Makanan", Color(0xFFFCE7F3)),
            ExtraFeatureCardItem("TanyaPanda", R.drawable.ic_menu_tanya_panda, "Tanya Kak Panda", "Chat Tanya Jawab Anak", Color(0xFFEDE9FE)),
            ExtraFeatureCardItem("PialaSertifikat", R.drawable.ic_menu_sertifikat, "Piala & Piagam", "Cetak Sertifikat Anak Sholeh", Color(0xFFFFEDD5))
        ).chunked(2)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitleWithOrnament(title = "🌟 Zona Pintar & Permainan Edukasi", theme = theme)
        Spacer(modifier = Modifier.height(2.dp))

        extraFeatures.forEach { rowPair ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowPair.forEach { feat ->
                    val iconBitmap = rememberOptimizedIcon(feat.iconRes, 130)

                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, theme.surfaceColor, RoundedCornerShape(22.dp))
                            .clickable { onSelectFeature(feat.routeKey) }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(feat.bgColor)
                                    .padding(3.dp)
                            ) {
                                Image(
                                    bitmap = iconBitmap,
                                    contentDescription = feat.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = feat.title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = theme.headerBg,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = feat.subtitle,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerCarouselSlider3D(
    slides: List<HomeBannerSlide>,
    theme: KidsAppTheme
) {
    if (slides.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val pandaBannerBitmap = rememberOptimizedIcon(R.drawable.ic_panda_home, 160)

    LaunchedEffect(slides.size) {
        if (slides.size > 1) {
            while (true) {
                delay(4500L)
                val nextPage = (pagerState.currentPage + 1) % slides.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { pageIdx ->
            val slide = slides[pageIdx]

            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, theme.primaryColor.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 8.2f)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(theme.headerBg, theme.primaryColor)
                                )
                            )
                    ) {
                        if (slide.imageUrl.isNotBlank()) {
                            NetworkImageLoader(
                                imageUrl = slide.imageUrl,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                                        )
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Column {
                                    Surface(color = theme.accentColor, shape = RoundedCornerShape(50)) {
                                        Text(
                                            text = slide.badge,
                                            color = Color(0xFF0F172A),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = slide.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (slide.arabicText.isNotBlank()) {
                                        Text(
                                            text = slide.arabicText,
                                            color = theme.accentColor,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        } else {
                            MosqueSilhouetteOrnamentCanvas(
                                accentColor = theme.accentColor,
                                modifier = Modifier.matchParentSize()
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(color = theme.accentColor, shape = RoundedCornerShape(50)) {
                                        Text(
                                            text = slide.badge,
                                            color = Color(0xFF0F172A),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = slide.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (slide.arabicText.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = slide.arabicText,
                                            color = theme.accentColor,
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Image(
                                    bitmap = pandaBannerBitmap,
                                    contentDescription = "Panda Banner",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .border(2.5.dp, theme.accentColor, CircleShape)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(theme.surfaceColor)
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = theme.primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = slide.subtitle,
                                color = theme.headerBg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${pageIdx + 1}/${slides.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.primaryColor
                        )
                    }
                }
            }
        }

        if (slides.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.indices.forEach { idx ->
                    val isSelected = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .height(7.dp)
                            .width(if (isSelected) 22.dp else 7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) theme.primaryColor
                                else theme.primaryColor.copy(alpha = 0.25f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun MosqueSilhouetteOrnamentCanvas(accentColor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = w * 0.45f,
            center = Offset(w * 0.85f, h * 0.2f)
        )

        val lanternColor = accentColor.copy(alpha = 0.28f)
        val lanternPositions = listOf(w * 0.88f to h * 0.22f, w * 0.76f to h * 0.16f)
        lanternPositions.forEach { (lx, ly) ->
            drawLine(color = lanternColor, start = Offset(lx, 0f), end = Offset(lx, ly), strokeWidth = 3f)
            drawCircle(color = lanternColor, radius = 16f, center = Offset(lx, ly + 16f), style = Stroke(width = 3f))
            drawCircle(color = accentColor.copy(alpha = 0.45f), radius = 6f, center = Offset(lx, ly + 16f))
        }

        val silhouetteColor = Color.Black.copy(alpha = 0.14f)
        val baseLineY = h

        val mainDomePath = Path().apply {
            moveTo(w * 0.30f, baseLineY)
            quadraticBezierTo(w * 0.50f, baseLineY - 130f, w * 0.70f, baseLineY)
            close()
        }
        drawPath(mainDomePath, color = silhouetteColor)

        drawLine(
            color = accentColor.copy(alpha = 0.35f),
            start = Offset(w * 0.50f, baseLineY - 65f),
            end = Offset(w * 0.50f, baseLineY - 92f),
            strokeWidth = 4f
        )
        drawCircle(color = accentColor.copy(alpha = 0.40f), radius = 8f, center = Offset(w * 0.50f, baseLineY - 100f))

        val leftDomePath = Path().apply {
            moveTo(w * 0.12f, baseLineY)
            quadraticBezierTo(w * 0.23f, baseLineY - 75f, w * 0.34f, baseLineY)
            close()
        }
        drawPath(leftDomePath, color = silhouetteColor)

        val rightDomePath = Path().apply {
            moveTo(w * 0.66f, baseLineY)
            quadraticBezierTo(w * 0.77f, baseLineY - 75f, w * 0.88f, baseLineY)
            close()
        }
        drawPath(rightDomePath, color = silhouetteColor)

        drawRect(color = silhouetteColor, topLeft = Offset(w * 0.06f, baseLineY - 125f), size = Size(w * 0.045f, 125f))
        drawRect(color = silhouetteColor, topLeft = Offset(w * 0.895f, baseLineY - 125f), size = Size(w * 0.045f, 125f))
    }
}

@Composable
fun HeaderSectionFast(
    theme: KidsAppTheme,
    onSelectTheme: (KidsAppTheme) -> Unit,
    loggedUser: LoggedInUser?,
    onClickAuth: () -> Unit,
    onClickAdminPanel: () -> Unit,
    city: String,
    date: String,
    currentTime: String,
    nextPrayerText: String,
    isPlayingAdzan: Boolean,
    isPreparingAdzan: Boolean,
    onToggleAdzan: () -> Unit,
    onClickLocation: () -> Unit
) {
    val homePandaBitmap = rememberOptimizedIcon(R.drawable.ic_panda_home, 180)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clickable { onClickAuth() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (loggedUser != null) "Assalamu'alaikum, ${loggedUser.name} ▾" else "🔐 Masuk / Daftar Akun",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (loggedUser?.role == "admin") {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = theme.accentColor,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { onClickAdminPanel() }
                ) {
                    Text(
                        text = "👑 Admin",
                        color = Color(0xFF0F172A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clickable { onClickLocation() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "$city • $date ▾",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = if (isPlayingAdzan) Color(0xFFE11D48) else theme.accentColor,
                shape = RoundedCornerShape(50),
                shadowElevation = 2.dp,
                modifier = Modifier.clickable { onToggleAdzan() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = if (isPlayingAdzan) Icons.Default.Close else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (isPlayingAdzan) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = when {
                            isPreparingAdzan -> "Memuat..."
                            isPlayingAdzan -> "Stop Adzan"
                            else -> "Kumandang Adzan"
                        },
                        color = if (isPlayingAdzan) Color.White else Color(0xFF0F172A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentTime,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = theme.accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = nextPrayerText,
                            color = theme.accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Image(
                bitmap = homePandaBitmap,
                contentDescription = "Avatar Panda",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .border(2.5.dp, theme.accentColor, CircleShape)
            )
        }
    }
}

@Composable
fun MengajiBookmarkTrackerCard(
    theme: KidsAppTheme,
    lastQuran: String,
    lastIqro: String,
    onContinueQuran: () -> Unit,
    onContinueIqro: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, theme.primaryColor.copy(alpha = 0.30f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Penanda Batas Mengaji",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = theme.headerBg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Ketuk kartu di bawah untuk melanjutkan bacaan",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    color = Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tersimpan",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF065F46),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = theme.surfaceColor,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                        .clickable { onContinueQuran() }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📖", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Al-Quran",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.primaryColor,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = lastQuran.replace(" (Ketuk untuk lanjut)", ""),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.headerBg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Lanjut Baca ➔",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.primaryColor,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    color = Color(0xFFFEF9C3),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFFACC15), RoundedCornerShape(18.dp))
                        .clickable { onContinueIqro() }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📗", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Buku Iqro",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF92400E),
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = lastIqro.replace(" (Ketuk untuk lanjut)", ""),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF78350F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Lanjut Iqro ➔",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitleWithOrnament(title: String, theme: KidsAppTheme) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(50))
                .background(theme.primaryColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = theme.headerBg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FastMenuIconItem(item: MenuItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val iconBitmap = rememberOptimizedIcon(item.iconRes, 130)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(item.badgeColor)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = iconBitmap,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
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
fun DonasiHighlightBanner(theme: KidsAppTheme, onClickDonasi: () -> Unit) {
    val amalanPanda = rememberOptimizedIcon(R.drawable.ic_panda_amalan, 140)

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDonasi() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(theme.headerBg, theme.primaryColor)))
                .border(2.dp, theme.accentColor, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    bitmap = amalanPanda,
                    contentDescription = "Panda Donasi",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(2.dp, theme.accentColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "💝 Infaq & Wakaf Jariyah Server Dakwah",
                        color = theme.accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Dukung operasional aplikasi via BSI, DANA, ShopeePay & GoPay.",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onClickDonasi,
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salurkan Infaq Terbaik Sekarang ✨", color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PrayerTimesRow(prayerTimes: List<PrayerTime>, theme: KidsAppTheme) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(prayerTimes, key = { it.name }) { prayer ->
            val bgColor = if (prayer.isNext) theme.primaryColor else Color.White
            val textColor = if (prayer.isNext) Color.White else theme.headerBg

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                elevation = CardDefaults.cardElevation(defaultElevation = if (prayer.isNext) 4.dp else 1.dp),
                modifier = Modifier
                    .width(96.dp)
                    .border(
                        width = if (prayer.isNext) 2.dp else 1.dp,
                        color = if (prayer.isNext) theme.accentColor else theme.surfaceColor,
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (prayer.isNext) {
                        Text(
                            text = "BERIKUTNYA",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.accentColor,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = prayer.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prayer.time,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (prayer.isNext) theme.accentColor else theme.primaryColor,
                        maxLines = 1
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
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(popularCities) { city ->
                        TextButton(onClick = { onSelectCity(city) }, modifier = Modifier.fillMaxWidth()) {
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

// =========================================================================
// HELPER DETEKSI GAMBAR SERVER & THUMBNAIL YOUTUBE OTOMATIS (ANTI-ERROR)
// =========================================================================
fun normalizeServerMediaUrl(rawUrl: String): String {
    val clean = rawUrl.trim()
    if (clean.isBlank()) return ""
    if (clean.startsWith("http://") || clean.startsWith("https://")) {
        return clean.replace(" ", "%20")
    }
    return "https://pandamuslim.pandakode-dev.my.id/${clean.trimStart('/')}".replace(" ", "%20")
}

fun extractYoutubeVideoId(url: String): String? {
    if (url.isBlank()) return null
    val patterns = listOf(
        "(?:v=|/)([a-zA-Z0-9_-]{11})(?:[?&]|$)",
        "youtu\\.be/([a-zA-Z0-9_-]{11})",
        "embed/([a-zA-Z0-9_-]{11})",
        "shorts/([a-zA-Z0-9_-]{11})",
        "live/([a-zA-Z0-9_-]{11})"
    )
    for (p in patterns) {
        val match = Regex(p).find(url.trim())
        if (match != null) return match.groupValues[1]
    }
    return null
}

fun extractMediaPreviewUrl(rawMediaUrl: String): String {
    if (rawMediaUrl.isBlank()) return ""
    val parts = rawMediaUrl.split("|")

    // 1. Cek jika ada gambar langsung (.jpg / .png / .webp)
    for (part in parts) {
        val clean = part.trim()
        if (clean.matches(Regex(".*\\.(jpg|jpeg|png|webp|gif)(\\?.*)?$", RegexOption.IGNORE_CASE))) {
            return normalizeServerMediaUrl(clean)
        }
    }

    // 2. Cek jika mengandung Link YouTube -> ambil Thumbnail HQ otomatis!
    for (part in parts) {
        val ytId = extractYoutubeVideoId(part.trim())
        if (ytId != null) {
            return "https://img.youtube.com/vi/$ytId/hqdefault.jpg"
        }
    }

    val directYt = extractYoutubeVideoId(rawMediaUrl)
    if (directYt != null) {
        return "https://img.youtube.com/vi/$directYt/hqdefault.jpg"
    }

    return ""
}

fun extractImageUrl(rawMediaUrl: String): String = extractMediaPreviewUrl(rawMediaUrl)

suspend fun fetchContentsFromServer(category: String): List<ServerContent> = withContext(Dispatchers.IO) {
    val resultList = mutableListOf<ServerContent>()
    try {
        val ts = System.currentTimeMillis()
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=get_contents&category=$category&_t=$ts")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.useCaches = false
        conn.setRequestProperty("Cache-Control", "no-cache")
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().readText()
            val jsonObj = JSONObject(response)
            val dataArray = jsonObj.getJSONArray("data")
            for (i in 0 until dataArray.length()) {
                val obj = dataArray.getJSONObject(i)
                resultList.add(
                    ServerContent(
                        id = obj.getInt("id"),
                        title = obj.getString("title"),
                        arabicText = obj.optString("arabic_text", ""),
                        latinText = obj.optString("latin_text", ""),
                        description = obj.optString("description", ""),
                        mediaUrl = obj.optString("media_url", ""),
                        jilidLevel = 1
                    )
                )
            }
        }
    } catch (_: Exception) {}
    resultList
}

@Composable
fun NetworkImageLoader(imageUrl: String, modifier: Modifier = Modifier) {
    var bitmap by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }
    var isFailed by remember(imageUrl) { mutableStateOf(false) }

    LaunchedEffect(imageUrl) {
        if (imageUrl.isBlank()) {
            isFailed = true
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val normalized = normalizeServerMediaUrl(imageUrl)
                val url = URL(normalized)
                val conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = true
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; PandaMuslimApp)")
                conn.doInput = true
                conn.connect()
                val decoded = BitmapFactory.decodeStream(conn.inputStream)
                if (decoded != null) {
                    bitmap = decoded
                } else {
                    isFailed = true
                }
            } catch (_: Exception) {
                isFailed = true
            }
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier.background(EmeraldDark)) {
        when {
            bitmap != null -> {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            isFailed -> {
                MosqueSilhouetteOrnamentCanvas(
                    accentColor = GoldAccent,
                    modifier = Modifier.matchParentSize()
                )
            }
            else -> {
                CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(24.dp))
            }
        }
    }
}

fun timeStrToMinutes(timeStr: String): Int {
    return try {
        val clean = timeStr.trim().take(5)
        val parts = clean.split(":")
        val h = parts[0].toInt()
        val m = parts[1].toInt()
        (h * 60) + m
    } catch (_: Exception) {
        0
    }
}

fun calculateActivePrayerList(
    rawList: List<Pair<String, String>>,
    currentTimeStr: String
): List<PrayerTime> {
    if (rawList.isEmpty()) return emptyList()
    val nowMinutes = timeStrToMinutes(currentTimeStr)

    val nextIndex = rawList.indexOfFirst { (_, time) ->
        timeStrToMinutes(time) > nowMinutes
    }

    val highlightedIndex = if (nextIndex != -1) nextIndex else 1.coerceAtMost(rawList.lastIndex)

    return rawList.mapIndexed { idx, (name, time) ->
        PrayerTime(
            name = name,
            time = time,
            isNext = (idx == highlightedIndex)
        )
    }
}

fun buildNextPrayerCountdownText(prayerTimes: List<PrayerTime>, currentTimeStr: String): String {
    val nextPrayer = prayerTimes.find { it.isNext } ?: return "Menuju waktu Sholat"
    val nowMin = timeStrToMinutes(currentTimeStr)
    val targetMin = timeStrToMinutes(nextPrayer.time)

    val diffMin = if (targetMin >= nowMin) {
        targetMin - nowMin
    } else {
        (24 * 60 - nowMin) + targetMin
    }

    val hours = diffMin / 60
    val mins = diffMin % 60
    val countdownPart = if (hours > 0) "${hours}j ${mins}m lagi" else "${mins} menit lagi"

    return "Menuju ${nextPrayer.name} (${nextPrayer.time}) • $countdownPart"
}

suspend fun fetchRawPrayerTimesByCoords(lat: Double, lng: Double): List<Pair<String, String>> = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.aladhan.com/v1/timings?latitude=$lat&longitude=$lng&method=20")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            return@withContext parseRawAladhanTimings(conn.inputStream.bufferedReader().readText())
        }
    } catch (_: Exception) {}
    emptyList()
}

suspend fun fetchRawPrayerTimesByCity(city: String): List<Pair<String, String>> = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.aladhan.com/v1/timingsByCity?city=$city&country=Indonesia&method=20")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            return@withContext parseRawAladhanTimings(conn.inputStream.bufferedReader().readText())
        }
    } catch (_: Exception) {}
    emptyList()
}

fun parseRawAladhanTimings(jsonResponse: String): List<Pair<String, String>> {
    val timings = JSONObject(jsonResponse).getJSONObject("data").getJSONObject("timings")
    return listOf(
        "Imsak" to timings.optString("Imsak", "04:26").take(5),
        "Shubuh" to timings.optString("Fajr", "04:36").take(5),
        "Dzuhur" to timings.optString("Dhuhr", "11:58").take(5),
        "Ashar" to timings.optString("Asr", "15:15").take(5),
        "Maghrib" to timings.optString("Maghrib", "18:02").take(5),
        "Isya" to timings.optString("Isha", "19:12").take(5)
    )
}

@Composable
fun FeedbackDialog(
    loggedUser: LoggedInUser? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(loggedUser?.name ?: "") }
    var email by remember { mutableStateOf(loggedUser?.email ?: "") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var responseText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Saran & Laporan Bug 🐼", fontWeight = FontWeight.ExtraBold, color = EmeraldDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (responseText.isNotEmpty()) {
                    Text(responseText, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                } else {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Pengirim") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email / WhatsApp Anda") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Tulis masukan atau kendala bug...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (responseText.isEmpty()) {
                Button(
                    onClick = {
                        isSending = true
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = sendFeedbackToServer(name, email, message)
                            withContext(Dispatchers.Main) {
                                isSending = false
                                responseText = result
                            }
                        }
                    },
                    enabled = !isSending && message.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isSending) "Mengirim..." else "Kirim ke Admin")
                }
            } else {
                Button(onClick = onDismiss) { Text("Tutup") }
            }
        },
        dismissButton = {
            if (responseText.isEmpty()) {
                TextButton(onClick = onDismiss) { Text("Batal") }
            }
        }
    )
}

fun sendFeedbackToServer(name: String, email: String, message: String): String {
    return try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=send_feedback")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true

        val jsonParam = JSONObject().apply {
            put("name", if (name.isBlank()) "Sahabat Panda" else name)
            put("email", if (email.isBlank()) "Tanpa Email" else email)
            put("message", message)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonParam.toString()) }

        if (conn.responseCode == 200) {
            val resp = JSONObject(conn.inputStream.bufferedReader().readText())
            resp.optString("message", "Alhamdulillah! Masukan Anda sudah diterima Admin.")
        } else {
            "Gagal mengirim (Kode: ${conn.responseCode})"
        }
    } catch (e: Exception) {
        "Pastikan internet menyala: ${e.localizedMessage}"
    }
}