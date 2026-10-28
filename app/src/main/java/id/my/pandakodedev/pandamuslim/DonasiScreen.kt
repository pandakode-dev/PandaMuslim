package id.my.pandakodedev.pandamuslim

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                            painter = painterResource(id = R.drawable.ic_panda_amalan),
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Kartu Highlight Utama: Keutamaan Amal Jariyah & Doa Malaikat
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF004D40), Color(0xFF00796B), Color(0xFF004D40))
                                )
                            )
                            .border(2.dp, GoldAccent, RoundedCornerShape(26.dp))
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = GoldAccent,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "✨ INVESTASI AKHIRAT • PAHALA MENGALIR ABADI ✨",
                                color = EmeraldDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "مَا نَقَصَتْ صَدَقَةٌ مِنْ مَالٍ",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "\"Sedekah tidaklah mengurangi harta sedikit pun.\" (HR. Muslim)",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Setiap huruf Al-Quran yang dibaca, dihafal, dan diamalkan oleh ribuan pengguna aplikasi PandaMuslim ini, insya Allah pahalanya turut mengalir deras kepada Anda yang ikut menjaga keberlangsungan server dakwah ini.",
                            fontSize = 12.sp,
                            color = MintSurface,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 2. Judul Daftar Rekening Resmi
            item {
                Text(
                    text = "💳 Pilih Saluran Infaq Terbaik Anda",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = EmeraldDark
                )
                Text(
                    text = "Ketuk tombol 'Salin Nomor' untuk menyalin nomor rekening atau E-Wallet secara otomatis:",
                    fontSize = 12.sp,
                    color = Color.Gray
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
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isCopied) 2.dp else 1.dp,
                                color = if (isCopied) EmeraldPrimary else MintSurface,
                                shape = RoundedCornerShape(22.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (isBank) Color(0xFFFEF3C7) else MintSurface,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = if (isBank) "🏦 BANK" else "📱 E-WALLET",
                                            color = if (isBank) Color(0xFF92400E) else EmeraldDark,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = acc.providerName,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = EmeraldDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(OffWhiteBg)
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = acc.accountNumber,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = EmeraldDark
                                    )
                                    Text(
                                        text = "a.n. ${acc.accountHolder}",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Rekening Donasi", acc.accountNumber)
                                        clipboard.setPrimaryClip(clip)
                                        copiedNumber = acc.accountNumber
                                        Toast.makeText(context, "Nomor ${acc.providerName} berhasil disalin!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCopied) Color(0xFF059669) else EmeraldPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCopied) Icons.Default.CheckCircle else Icons.Default.Favorite,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isCopied) "Tersalin" else "Salin Nomor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Kartu Doa Khusus bagi Para Donatur
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
                            text = "🤲 Doa Kami untuk Anda & Keluarga",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = EmeraldDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "آجَرَكَ اللَّهُ فِيمَا أَعْطَيْتَ، وَبَارَكَ فِيمَا أَبْقَيْتَ وَجَعَلَهُ لَكَ طَهُورًا",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"Semoga Allah memberikan pahala atas apa yang engkau berikan, memberkahi harta yang masih engkau simpan, dan menjadikannya pembersih bagimu. Aamiin Ya Rabbal 'Alamin.\"",
                            fontSize = 12.sp,
                            color = EmeraldPrimary,
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
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