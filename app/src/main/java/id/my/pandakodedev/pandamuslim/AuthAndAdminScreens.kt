package id.my.pandakodedev.pandamuslim

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class LoggedInUser(
    val name: String,
    val email: String,
    val role: String, // "user", "donatur", atau "admin"
    val status: String
)

// =========================================================================
// 1. LAYAR WAJIB LOGIN SAAT PERTAMA BUKA APLIKASI (MANDATORY LOGIN GATE)
// =========================================================================
@Composable
fun MandatoryLoginScreen(
    onLoginSuccess: (LoggedInUser) -> Unit
) {
    val context = LocalContext.current
    val pandaLogo = rememberOptimizedIcon(R.drawable.ic_panda_home, 220)

    var isRegisterMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isPendingStatus by remember { mutableStateOf(false) }

    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val chosenEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) ?: ""
            if (chosenEmail.isNotBlank()) {
                val derivedName = chosenEmail.substringBefore("@").replace(".", " ")
                    .replaceFirstChar { it.uppercase() }
                isLoading = true
                CoroutineScope(Dispatchers.IO).launch {
                    val res = loginGoogleToServer(derivedName, chosenEmail)
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        if (res.first != null) {
                            Toast.makeText(context, "Assalamu'alaikum, ${res.first!!.name}!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(res.first!!)
                        } else {
                            isPendingStatus = true
                            statusMessage = res.second
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(EmeraldDark, EmeraldPrimary, Color(0xFF002B24))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))

            Image(
                bitmap = pandaLogo,
                contentDescription = "PandaMuslim Maskot",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(3.dp, GoldAccent, CircleShape)
                    .background(MintSurface)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "PandaMuslim",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Sahabat Belajar Al-Quran, Iqro 1–6 & Ibadah Harian",
                fontSize = 12.sp,
                color = MintSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(MintSurface)
                            .padding(4.dp)
                    ) {
                        Surface(
                            color = if (!isRegisterMode) EmeraldDark else Color.Transparent,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isRegisterMode = false
                                    statusMessage = ""
                                }
                        ) {
                            Text(
                                text = "🔐 Masuk (Login)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (!isRegisterMode) GoldAccent else EmeraldDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                        Surface(
                            color = if (isRegisterMode) EmeraldDark else Color.Transparent,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isRegisterMode = true
                                    statusMessage = ""
                                }
                        ) {
                            Text(
                                text = "📝 Daftar Email",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isRegisterMode) GoldAccent else EmeraldDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }

                    if (statusMessage.isNotBlank()) {
                        Surface(
                            color = if (isPendingStatus) Color(0xFFFEF3C7) else Color(0xFFFFE4E6),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isPendingStatus) "⏳ Menunggu Persetujuan Admin" else "ℹ️ Informasi",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = if (isPendingStatus) Color(0xFF92400E) else Color(0xFFBE123C)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = statusMessage,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = AccountManager.newChooseAccountIntent(
                                    null, null, arrayOf("com.google"), null, null, null, null
                                )
                                googleAccountPickerLauncher.launch(intent)
                            } catch (e: Exception) {
                                statusMessage = "Silakan masukkan Email & Password pada kolom di bawah."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Masuk Cepat dengan Google (SSO)",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text("  atau via Email  ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nama Lengkap Anda") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Alamat Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldPrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            isLoading = true
                            isPendingStatus = false
                            CoroutineScope(Dispatchers.IO).launch {
                                if (isRegisterMode) {
                                    val msg = registerEmailToServer(nameInput, emailInput, passwordInput)
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        isPendingStatus = true
                                        statusMessage = msg
                                    }
                                } else {
                                    val res = loginEmailToServer(emailInput, passwordInput)
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        if (res.first != null) {
                                            Toast.makeText(context, "Alhamdulillah, selamat datang ${res.first!!.name}!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess(res.first!!)
                                        } else {
                                            isPendingStatus = res.second.contains("PENDING", ignoreCase = true) ||
                                                    res.second.contains("Approve", ignoreCase = true)
                                            statusMessage = res.second
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isLoading && emailInput.isNotBlank() && passwordInput.isNotBlank() && (!isRegisterMode || nameInput.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isLoading) "Menghubungi Server..."
                            else if (isRegisterMode) "Daftar & Ajukan Approve Admin 🚀"
                            else "Masuk ke Aplikasi 🕌",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    TextButton(
                        onClick = {
                            isRegisterMode = false
                            emailInput = "admin@pandamuslim.pandakode-dev.my.id"
                            passwordInput = "humairoh26"
                        }
                    ) {
                        Text(
                            text = "👑 Pemilik Aplikasi? Ketuk untuk isi otomatis Email Admin",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. DIALOG PROFIL PENGGUNA + AKSES RUANG DONATUR VIP & PANEL ADMIN
// =========================================================================
@Composable
fun AuthLoginRegisterDialog(
    currentUser: LoggedInUser?,
    onLoginSuccess: (LoggedInUser) -> Unit,
    onLogout: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDonaturVipModal by remember { mutableStateOf(false) }

    if (showDonaturVipModal && currentUser != null) {
        DonaturVipExclusiveDialog(
            user = currentUser,
            onDismiss = { showDonaturVipModal = false }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Profil Jamaah PandaMuslim 🐼",
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldDark,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (currentUser != null) {
                    val roleBadge = when (currentUser.role) {
                        "admin" -> "👑 Super Admin"
                        "donatur" -> "💎 Donatur VIP (Muhsinin)"
                        else -> "👤 Sahabat Jamaah"
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MintSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = currentUser.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = EmeraldDark
                            )
                            Text(text = currentUser.email, fontSize = 13.sp, color = EmeraldPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(color = GoldAccent, shape = RoundedCornerShape(50)) {
                                Text(
                                    text = roleBadge,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Menu Eksklusif jika Role = Donatur VIP atau Admin
                    if (currentUser.role == "donatur" || currentUser.role == "admin") {
                        Button(
                            onClick = { showDonaturVipModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka Ruang Eksklusif Donatur VIP 💎", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }

                    if (currentUser.role == "admin") {
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenAdminPanel()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka Panel Admin & Edit Role User 👑", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onLogout()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
            ) {
                Text("Keluar Akun (Logout)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

// =========================================================================
// 3. MENU EKSKLUSIF KHUSUS AKUN DONATUR VIP (PIAGAM WAKAF & DOA MUHSININ)
// =========================================================================
@Composable
fun DonaturVipExclusiveDialog(
    user: LoggedInUser,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("💎 Ruang Kehormatan Donatur VIP", fontWeight = FontWeight.Black, color = EmeraldDark, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sertifikat Wakaf Digital
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(EmeraldDark, EmeraldPrimary)))
                            .border(2.dp, GoldAccent, RoundedCornerShape(20.dp))
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏅 PIAGAM PENGHARGAAN MUHSININ", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(user.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        Text(user.email, color = MintSurface, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Terima kasih telah menjadi bagian dari Donatur VIP Penjaga Server Dakwah PandaMuslim. Setiap ayat Al-Quran dan huruf Iqro yang dibaca oleh ribuan pengguna aplikasi ini mengalirkan pahala jariyah abadi untuk Anda dan keluarga.",
                            color = Color.White,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Doa Khusus Keluarga Donatur
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🤲 Doa Khusus Keberkahan Harta & Keluarga", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = EmeraldDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "اللَّهُمَّ بَارِكْ لَهُمْ فِيمَا رَزَقْتَهُمْ، وَاغْفِرْ لَهُمْ وَارْحَمْهُمْ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"Ya Allah, berkahilah rezeki yang Engkau karuniakan kepada mereka, ampunilah dosa mereka, dan sayangilah keluarga mereka.\" (HR. Muslim)",
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF78350F),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Alhamdulillah, Terima Kasih 🌟", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// =========================================================================
// 4. LAYAR PANEL ADMIN DI HP ANDROID (APPROVE USER & EDIT ROLE USER/DONATUR/ADMIN)
// =========================================================================
data class AdminUserRow(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val status: String,
    val lastQuran: String,
    val lastIqro: String
)

data class AdminLogRow(
    val id: Int,
    val userName: String,
    val userEmail: String,
    val actionType: String,
    val description: String,
    val createdAt: String
)

data class AdminFeedbackRow(
    val id: Int,
    val userName: String,
    val userEmail: String,
    val message: String,
    val status: String,
    val createdAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMobilePanelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    var userList by remember { mutableStateOf<List<AdminUserRow>>(emptyList()) }
    var logList by remember { mutableStateOf<List<AdminLogRow>>(emptyList()) }
    var feedbackList by remember { mutableStateOf<List<AdminFeedbackRow>>(emptyList()) }

    fun refreshAdminData() {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            val data = fetchAdminDashboardData()
            withContext(Dispatchers.Main) {
                userList = data.first
                logList = data.second
                feedbackList = data.third
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshAdminData()
    }

    val pendingCount = userList.count { it.status == "pending" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("👑 Panel Admin PandaMuslim", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
                        Text(
                            text = "Edit Role (User/Donatur/Admin) • Approve ($pendingCount)",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { refreshAdminData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Segarkan", tint = GoldAccent)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf(
                    0 to "👥 Role & User (${userList.size})",
                    1 to "🔔 Log (${logList.size})",
                    2 to "📬 Masukan (${feedbackList.size})"
                )
                tabs.forEach { (idx, title) ->
                    val active = selectedTab == idx
                    Surface(
                        color = if (active) GoldAccent else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f).clickable { selectedTab = idx }
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (active) Color(0xFF0F172A) else Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 9.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (selectedTab) {
                        0 -> {
                            items(userList, key = { it.id }) { u ->
                                val isPending = u.status == "pending"
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isPending) Color(0xFFFEF9C3) else Color.White
                                    ),
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
                                                Text(u.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EmeraldDark)
                                                Text(u.email, fontSize = 12.sp, color = EmeraldPrimary)
                                            }
                                            Surface(
                                                color = when (u.status) {
                                                    "approved" -> Color(0xFFD1FAE5)
                                                    "pending" -> Color(0xFFFDE047)
                                                    else -> Color(0xFFFFE4E6)
                                                },
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Text(
                                                    text = "${u.role.uppercase()} • ${u.status.uppercase()}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF0F172A),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("📖 Mengaji Quran: ${u.lastQuran}", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                        Text("📗 Mengaji Iqro: ${u.lastIqro}", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("Ubah Role Pengguna:", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldDark)
                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Pilihan Role: User Biasa, Donatur VIP, atau Admin
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                            listOf(
                                                "user" to "👤 User",
                                                "donatur" to "💎 Donatur VIP",
                                                "admin" to "👑 Admin"
                                            ).forEach { (rKey, rLabel) ->
                                                val isCurrentRole = u.role == rKey
                                                Surface(
                                                    color = if (isCurrentRole) EmeraldDark else MintSurface,
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            CoroutineScope(Dispatchers.IO).launch {
                                                                approveUserOnServer(u.id, "approved", rKey)
                                                                withContext(Dispatchers.Main) {
                                                                    Toast.makeText(context, "Role ${u.name} diubah jadi $rLabel", Toast.LENGTH_SHORT).show()
                                                                    refreshAdminData()
                                                                }
                                                            }
                                                        }
                                                ) {
                                                    Text(
                                                        text = rLabel,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isCurrentRole) GoldAccent else EmeraldDark,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.padding(vertical = 8.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (u.status != "approved") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        approveUserOnServer(u.id, "approved", u.role)
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, "${u.name} berhasil di-Approve!", Toast.LENGTH_SHORT).show()
                                                            refreshAdminData()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Approve Pendaftaran Sekarang ✅", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            items(logList, key = { it.id }) { log ->
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "[${log.actionType}] ${log.userName}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp,
                                                color = EmeraldDark
                                            )
                                            Text(log.createdAt, fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(log.description, fontSize = 13.sp, color = Color(0xFF334155))
                                        Text("Email: ${log.userEmail}", fontSize = 11.sp, color = EmeraldPrimary)
                                    }
                                }
                            }
                        }

                        2 -> {
                            items(feedbackList, key = { it.id }) { fb ->
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("👤 Pengirim: ${fb.userName}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = EmeraldDark)
                                        Text("✉️ Email: ${fb.userEmail} • ${fb.createdAt}", fontSize = 11.sp, color = EmeraldPrimary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = OffWhiteBg,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = fb.message,
                                                fontSize = 14.sp,
                                                color = Color(0xFF1E293B),
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Fungsi Koneksi API
fun registerEmailToServer(name: String, email: String, pass: String): String {
    return try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=register_email")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true
        val json = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("password", pass)
        }
        OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
        val resp = JSONObject(conn.inputStream.bufferedReader().readText())
        resp.optString("message", "Pendaftaran terkirim! Menunggu Approve Admin.")
    } catch (e: Exception) {
        "Gagal terhubung ke server: ${e.localizedMessage}"
    }
}

fun loginEmailToServer(email: String, pass: String): Pair<LoggedInUser?, String> {
    return try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=login_email")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true
        val json = JSONObject().apply {
            put("email", email)
            put("password", pass)
        }
        OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
        val resp = JSONObject(conn.inputStream.bufferedReader().readText())
        if (resp.optString("status") == "success") {
            val u = resp.getJSONObject("user")
            Pair(
                LoggedInUser(
                    name = u.getString("name"),
                    email = u.getString("email"),
                    role = u.optString("role", "user"),
                    status = u.optString("status", "approved")
                ),
                "Berhasil masuk"
            )
        } else {
            Pair(null, resp.optString("message", "Login gagal"))
        }
    } catch (e: Exception) {
        Pair(null, "Pastikan internet aktif: ${e.localizedMessage}")
    }
}

fun loginGoogleToServer(name: String, email: String): Pair<LoggedInUser?, String> {
    return try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=login_google")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true
        val json = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("google_uid", email)
        }
        OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
        val resp = JSONObject(conn.inputStream.bufferedReader().readText())
        if (resp.optString("status") == "success") {
            val u = resp.getJSONObject("user")
            Pair(
                LoggedInUser(
                    name = u.getString("name"),
                    email = u.getString("email"),
                    role = u.optString("role", "user"),
                    status = u.optString("status", "approved")
                ),
                "Berhasil masuk via Google SSO"
            )
        } else {
            Pair(null, resp.optString("message", "Menunggu Approve Admin"))
        }
    } catch (e: Exception) {
        Pair(null, "Gagal sinkronisasi SSO: ${e.localizedMessage}")
    }
}

fun syncMengajiBookmarkToServer(context: Context, type: String, bookmarkText: String) {
    val prefs = context.getSharedPreferences("panda_auth_prefs", Context.MODE_PRIVATE)
    val email = prefs.getString("user_email", "") ?: ""
    val name = prefs.getString("user_name", "Sahabat Panda") ?: "Sahabat Panda"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=save_bookmark")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            val json = JSONObject().apply {
                put("email", email)
                put("name", name)
                put("type", type)
                put("bookmark", bookmarkText)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
            conn.responseCode
        } catch (_: Exception) {}
    }
}

fun fetchAdminDashboardData(): Triple<List<AdminUserRow>, List<AdminLogRow>, List<AdminFeedbackRow>> {
    val users = mutableListOf<AdminUserRow>()
    val logs = mutableListOf<AdminLogRow>()
    val feedbacks = mutableListOf<AdminFeedbackRow>()
    try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=admin_dashboard_data")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            val obj = JSONObject(conn.inputStream.bufferedReader().readText())
            val uArr = obj.optJSONArray("users")
            if (uArr != null) {
                for (i in 0 until uArr.length()) {
                    val o = uArr.getJSONObject(i)
                    users.add(
                        AdminUserRow(
                            id = o.getInt("id"),
                            name = o.getString("name"),
                            email = o.getString("email"),
                            role = o.optString("role", "user"),
                            status = o.optString("status", "pending"),
                            lastQuran = o.optString("last_quran", "-"),
                            lastIqro = o.optString("last_iqro", "-")
                        )
                    )
                }
            }
            val lArr = obj.optJSONArray("logs")
            if (lArr != null) {
                for (i in 0 until lArr.length()) {
                    val o = lArr.getJSONObject(i)
                    logs.add(
                        AdminLogRow(
                            id = o.getInt("id"),
                            userName = o.getString("user_name"),
                            userEmail = o.getString("user_email"),
                            actionType = o.getString("action_type"),
                            description = o.getString("description"),
                            createdAt = o.optString("created_at", "")
                        )
                    )
                }
            }
            val fArr = obj.optJSONArray("feedbacks")
            if (fArr != null) {
                for (i in 0 until fArr.length()) {
                    val o = fArr.getJSONObject(i)
                    feedbacks.add(
                        AdminFeedbackRow(
                            id = o.getInt("id"),
                            userName = o.getString("user_name"),
                            userEmail = o.optString("user_email", "-"),
                            message = o.getString("message"),
                            status = o.optString("status", "baru"),
                            createdAt = o.optString("created_at", "")
                        )
                    )
                }
            }
        }
    } catch (_: Exception) {}
    return Triple(users, logs, feedbacks)
}

fun approveUserOnServer(userId: Int, newStatus: String, newRole: String = "") {
    try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=admin_approve_user")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true
        val json = JSONObject().apply {
            put("user_id", userId)
            put("status", newStatus)
            if (newRole.isNotBlank()) put("role", newRole)
        }
        OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
        conn.responseCode
    } catch (_: Exception) {}
}