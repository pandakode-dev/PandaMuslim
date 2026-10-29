package id.my.pandakodedev.pandamuslim

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class DonationAccount(
    val id: Int,
    val providerType: String, // "BANK" atau "EWALLET"
    val providerName: String,
    val accountNumber: String,
    val accountHolder: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonasiScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var accounts by remember { mutableStateOf<List<DonationAccount>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var copiedNumber by remember { mutableStateOf<String?>(null) }

    val amalanIcon = rememberOptimizedIcon(R.drawable.ic_panda_amalan, 110)

    LaunchedEffect(Unit) {
        val fromServer = fetchDonationAccounts()
        accounts = if (fromServer.isNotEmpty()) fromServer else listOf(
            DonationAccount(1, "BANK", "Bank Syariah Indonesia (BSI)", "7123456789", "Yayasan PandaMuslim Developer"),
            DonationAccount(2, "EWALLET", "DANA", "081234567890", "Admin PandaMuslim"),
            DonationAccount(3, "EWALLET", "ShopeePay", "081234567890", "Admin PandaMuslim"),
            DonationAccount(4, "EWALLET", "GoPay", "081234567890", "Admin PandaMuslim")
        )
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            bitmap = amalanIcon,
                            contentDescription = "Donasi Panda",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Infaq & Wakaf Jariyah", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Kartu Highlight Keutamaan Sedekah Jariyah
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(EmeraldDark, EmeraldPrimary)))
                            .border(2.dp, GoldAccent, RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                            Text(
                                text = "✨ INVESTASI AKHIRAT • PAHALA MENGALIR ABADI ✨",
                                color = Color(0xFF0F172A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "مَا نَقَصَتْ صَدَقَةٌ مِنْ مَالٍ",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "\"Sedekah tidaklah mengurangi harta sedikit pun.\" (HR. Muslim)",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Setiap huruf Al-Quran & Iqro yang dibaca oleh ribuan anak dan jamaah di aplikasi PandaMuslim ini, insya Allah pahalanya turut mengalir kepada Anda.",
                            fontSize = 12.sp,
                            color = MintSurface,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "💳 Saluran Rekening Infaq & E-Wallet Resmi",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = EmeraldDark
                )
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EmeraldPrimary)
                    }
                }
            } else {
                items(accounts, key = { it.id }) { acc ->
                    val isCopied = copiedNumber == acc.accountNumber
                    val isBank = acc.providerType == "BANK"

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isCopied) 2.dp else 1.dp,
                                color = if (isCopied) EmeraldPrimary else MintSurface,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                copyAccountToClipboard(context, acc.providerName, acc.accountNumber)
                                copiedNumber = acc.accountNumber
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Baris Atas: Label Bank/E-Wallet & Nama Penyedia
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    color = if (isBank) Color(0xFFFEF3C7) else MintSurface,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isBank) "🏦 BANK" else "📱 E-WALLET",
                                        color = if (isBank) Color(0xFF92400E) else EmeraldDark,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = acc.providerName,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = EmeraldDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Kotak Nomor Rekening 1 Baris Rapi (Tidak Pernah Turun ke Bawah!)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(OffWhiteBg)
                                    .border(1.dp, MintSurface, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = acc.accountNumber,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        letterSpacing = 0.5.sp,
                                        color = EmeraldDark,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "a.n. ${acc.accountHolder}",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    color = if (isCopied) Color(0xFF059669) else EmeraldPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            copyAccountToClipboard(context, acc.providerName, acc.accountNumber)
                                            copiedNumber = acc.accountNumber
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                            contentDescription = "Salin",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (isCopied) "Tersalin" else "Salin",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Doa Keberkahan untuk Donatur
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MintSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🤲 Doa Malaikat untuk Para Muhsinin",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = EmeraldDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "اللَّهُمَّ أَعْطِ مُنْفِقًا خَلَفًا",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"Ya Allah, berikanlah ganti yang lebih berkah kepada orang yang berinfaq.\" (HR. Bukhari & Muslim)",
                            fontSize = 12.sp,
                            color = EmeraldPrimary,
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private fun copyAccountToClipboard(context: Context, provider: String, number: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Rekening $provider", number)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Nomor $provider ($number) berhasil disalin!", Toast.LENGTH_SHORT).show()
}

suspend fun fetchDonationAccounts(): List<DonationAccount> = withContext(Dispatchers.IO) {
    val list = mutableListOf<DonationAccount>()
    try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=get_donations")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().readText()
            val arr = JSONObject(response).getJSONArray("data")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    DonationAccount(
                        id = obj.getInt("id"),
                        providerType = obj.optString("provider_type", "BANK"),
                        providerName = obj.getString("provider_name"),
                        accountNumber = obj.getString("account_number"),
                        accountHolder = obj.getString("account_holder")
                    )
                )
            }
        }
    } catch (_: Exception) {}
    list
}