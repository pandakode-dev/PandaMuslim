package id.my.pandakodedev.pandamuslim

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// Palet Warna Utama PandaMuslim
val EmeraldDark = Color(0xFF004D40)
val EmeraldPrimary = Color(0xFF00897B)
val MintSurface = Color(0xFFE0F2F1)
val GoldAccent = Color(0xFFFFCA28)
val OffWhiteBg = Color(0xFFF4FBF9)

// Data Model
data class MenuItem(val title: String, @DrawableRes val iconRes: Int)
data class PrayerTime(val name: String, val time: String, val isNext: Boolean = false)

@Composable
fun PandaMuslimHomeScreen(
    onMenuClick: (String) -> Unit = {}
) {
    // Disimpan dalam remember agar tidak dibuat ulang saat scroll (Anti-Lag)
    val menuList = remember {
        listOf(
            MenuItem("Al-Quran", R.drawable.ic_panda_quran),
            MenuItem("Tajwid", R.drawable.ic_menu_tajwid),
            MenuItem("Doa", R.drawable.ic_panda_doa),
            MenuItem("Kiblat", R.drawable.ic_panda_kiblat),
            MenuItem("Tasbih", R.drawable.ic_panda_tasbih),
            MenuItem("Video", R.drawable.ic_menu_video),
            MenuItem("Asmaul", R.drawable.ic_menu_asmaul),
            MenuItem("Belajar", R.drawable.ic_menu_belajar),
            MenuItem("Amalan", R.drawable.ic_panda_amalan),
            MenuItem("Kalkulator", R.drawable.ic_menu_kalkulator),
            MenuItem("Khatam", R.drawable.ic_menu_khatam),
            MenuItem("Komunitas", R.drawable.ic_panda_komunitas),
            MenuItem("FAQ", R.drawable.ic_menu_faq),
            MenuItem("Masukan", R.drawable.ic_menu_masukan),
            MenuItem("Mewarnai", R.drawable.ic_menu_mewarnai),
            MenuItem("Iqro", R.drawable.ic_menu_iqro)
        )
    }

    val prayerTimes = remember {
        listOf(
            PrayerTime("Imsak", "04:26"),
            PrayerTime("Shubuh", "04:36"),
            PrayerTime("Dzuhur", "11:58"),
            PrayerTime("Ashar", "15:15"),
            PrayerTime("Maghrib", "18:02"),
            PrayerTime("Isya", "19:12", isNext = true)
        )
    }

    var showFeedbackDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDark)
    ) {
        item(key = "header") {
            HeaderSection(
                city = "Pekanbaru",
                date = "28 Okt 2026",
                currentTime = "19:09"
            )
        }

        item(key = "main_body") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(OffWhiteBg)
                    .padding(16.dp)
            ) {
                BannerSection3D()
                Spacer(modifier = Modifier.height(22.dp))
                SectionTitle(title = "Menu Utama")
                Spacer(modifier = Modifier.height(14.dp))

                MenuGridSection3D(
                    menus = menuList,
                    onMenuClick = { menuTitle ->
                        if (menuTitle == "Masukan") {
                            showFeedbackDialog = true
                        } else {
                            onMenuClick(menuTitle)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(22.dp))
                SupportDeveloperCard3D(onClick = { showFeedbackDialog = true })
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(title = "Jadwal Sholat Hari Ini")
                Spacer(modifier = Modifier.height(12.dp))
                PrayerTimesRow(prayerTimes = prayerTimes)
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    if (showFeedbackDialog) {
        FeedbackDialog(onDismiss = { showFeedbackDialog = false })
    }
}

@Composable
fun HeaderSection(city: String, date: String, currentTime: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Lokasi",
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$city • $date",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifikasi",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentTime,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Menuju waktu Isya (19:12 WIB)",
                    color = MintSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_panda_home),
                contentDescription = "Avatar Panda",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MintSurface)
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        color = EmeraldDark
    )
}

@Composable
fun MenuGridSection3D(menus: List<MenuItem>, onMenuClick: (String) -> Unit) {
    val chunkedMenus = remember(menus) { menus.chunked(4) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
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
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncy_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MintSurface)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun BannerSection3D() {
    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF004D40), Color(0xFF00897B), Color(0xFF26A69A))
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
                            color = GoldAccent,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "RAMADHAN 1447 H",
                                color = EmeraldDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Doa Berbuka Puasa",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ",
                            color = MintSurface,
                            fontSize = 15.sp
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.ic_panda_home),
                        contentDescription = "Panda Maskot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MintSurface)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "di PandaMuslim. Jangan lupa tunaikan sholat tepat waktu!",
                    color = EmeraldDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SupportDeveloperCard3D(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MintSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_panda_komunitas),
                contentDescription = "Developer Panda",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dukung PandaMuslim",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = EmeraldDark
                )
                Text(
                    text = "Kirim saran & bantu operasional server Developer",
                    fontSize = 12.sp,
                    color = EmeraldPrimary
                )
            }
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldDark)
        }
    }
}

@Composable
fun PrayerTimesRow(prayerTimes: List<PrayerTime>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(prayerTimes, key = { it.name }) { prayer ->
            val bgColor = if (prayer.isNext) EmeraldPrimary else Color.White
            val textColor = if (prayer.isNext) Color.White else EmeraldDark

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.width(92.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = prayer.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = prayer.time,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (prayer.isNext) GoldAccent else EmeraldPrimary
                    )
                }
            }
        }
    }
}

