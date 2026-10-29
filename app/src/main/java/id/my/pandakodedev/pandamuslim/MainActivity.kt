package id.my.pandakodedev.pandamuslim

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

var EmeraldDark by mutableStateOf(Color(0xFF004D40))
var EmeraldPrimary by mutableStateOf(Color(0xFF00897B))
var MintSurface by mutableStateOf(Color(0xFFE0F2F1))
var GoldAccent by mutableStateOf(Color(0xFFFFCA28))
var OffWhiteBg by mutableStateOf(Color(0xFFF4FBF9))

enum class KidsAppTheme(
    val label: String,
    val emoji: String,
    val headerBg: Color,
    val primaryColor: Color,
    val accentColor: Color,
    val surfaceColor: Color,
    val bodyBg: Color
) {
    ZAMRUD("Zamrud Nabawi", "🌿", Color(0xFF004D40), Color(0xFF00897B), Color(0xFFFFCA28), Color(0xFFE0F2F1), Color(0xFFF4FBF9)),
    CERIA_ANAK("Biru Ceria", "🎈", Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFFE0F2FE), Color(0xFFF0F9FF)),
    MALAM_BINTANG("Ungu Ramadhan", "🌙", Color(0xFF3B0764), Color(0xFF7E22CE), Color(0xFFFDE047), Color(0xFFF3E8FF), Color(0xFFFAF5FF)),
    PINK_MUSLIMAH("Pinky Solehah", "🌸", Color(0xFF831843), Color(0xFFDB2777), Color(0xFFFDE047), Color(0xFFFCE7F3), Color(0xFFFFF1F2)),
    EMAS_MAKKAH("Kiswah Makkah", "🕋", Color(0xFF1C1917), Color(0xFFB45309), Color(0xFFFDE047), Color(0xFFFEF3C7), Color(0xFFFFFBEB)),
    SAMUDRA_BIRU("Samudra Ilmu", "🌊", Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF38BDF8), Color(0xFFDBEAFE), Color(0xFFEFF6FF)),
    SENJA_MADINAH("Senja Madinah", "🌅", Color(0xFF7C2D12), Color(0xFFEA580C), Color(0xFFFDE047), Color(0xFFFFEDD5), Color(0xFFFFF7ED)),
    COKELAT_KURMA("Manis Kurma", "🌴", Color(0xFF451A03), Color(0xFF92400E), Color(0xFFFBBF24), Color(0xFFFEF3C7), Color(0xFFFFFBEB))
}

val LocalPandaTheme = compositionLocalOf { KidsAppTheme.ZAMRUD }

fun applyGlobalThemeColors(theme: KidsAppTheme) {
    EmeraldDark = theme.headerBg
    EmeraldPrimary = theme.primaryColor
    GoldAccent = theme.accentColor
    MintSurface = theme.surfaceColor
    OffWhiteBg = theme.bodyBg
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PandaMuslimAppRoot()
        }
    }
}

@Composable
fun PandaMuslimAppRoot() {
    val context = LocalContext.current
    val themePrefs = remember { context.getSharedPreferences("panda_theme_prefs", Context.MODE_PRIVATE) }
    val authPrefs = remember { context.getSharedPreferences("panda_auth_prefs", Context.MODE_PRIVATE) }

    var currentTheme by remember {
        val savedName = themePrefs.getString("selected_theme", KidsAppTheme.ZAMRUD.name)
        val initial = try { KidsAppTheme.valueOf(savedName!!) } catch (_: Exception) { KidsAppTheme.ZAMRUD }
        applyGlobalThemeColors(initial)
        mutableStateOf(initial)
    }

    var activeUser by remember {
        val savedEmail = authPrefs.getString("user_email", null)
        val savedStatus = authPrefs.getString("user_status", "")
        if (!savedEmail.isNullOrBlank() && savedStatus == "approved") {
            mutableStateOf<LoggedInUser?>(
                LoggedInUser(
                    name = authPrefs.getString("user_name", "Sahabat Panda") ?: "Sahabat Panda",
                    email = savedEmail,
                    role = authPrefs.getString("user_role", "user") ?: "user",
                    status = "approved"
                )
            )
        } else {
            mutableStateOf<LoggedInUser?>(null)
        }
    }

    LaunchedEffect(currentTheme) {
        applyGlobalThemeColors(currentTheme)
    }

    val dynamicColorScheme = lightColorScheme(
        primary = currentTheme.primaryColor,
        onPrimary = Color.White,
        primaryContainer = currentTheme.surfaceColor,
        secondary = currentTheme.accentColor,
        background = currentTheme.bodyBg,
        surface = Color.White
    )

    CompositionLocalProvider(LocalPandaTheme provides currentTheme) {
        MaterialTheme(colorScheme = dynamicColorScheme) {
            Surface(modifier = Modifier.fillMaxSize(), color = currentTheme.bodyBg) {
                if (activeUser == null) {
                    MandatoryLoginScreen(
                        onLoginSuccess = { loggedIn ->
                            authPrefs.edit()
                                .putString("user_name", loggedIn.name)
                                .putString("user_email", loggedIn.email)
                                .putString("user_role", loggedIn.role)
                                .putString("user_status", loggedIn.status)
                                .apply()
                            activeUser = loggedIn
                        }
                    )
                } else {
                    PandaMuslimAppNavigator(
                        currentTheme = currentTheme,
                        onThemeChange = { newTheme ->
                            currentTheme = newTheme
                            applyGlobalThemeColors(newTheme)
                            themePrefs.edit().putString("selected_theme", newTheme.name).apply()
                        },
                        onRequireLogout = {
                            authPrefs.edit().clear().apply()
                            activeUser = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PandaMuslimAppNavigator(
    currentTheme: KidsAppTheme,
    onThemeChange: (KidsAppTheme) -> Unit,
    onRequireLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("HOME") }

    BackHandler(enabled = currentScreen != "HOME") {
        currentScreen = "HOME"
    }

    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
            "HOME" -> {
                PandaMuslimHomeScreen(
                    currentTheme = currentTheme,
                    onSelectTheme = onThemeChange,
                    onMenuClick = { selectedMenuTitle ->
                        if (selectedMenuTitle == "LOGOUT") {
                            onRequireLogout()
                        } else {
                            currentScreen = selectedMenuTitle
                        }
                    }
                )
            }
            // Navigasi Admin & Donasi
            "AdminPanel" -> AdminMobilePanelScreen(onBack = { currentScreen = "HOME" })
            "Donasi" -> DonasiScreen(onBack = { currentScreen = "HOME" })

            // Navigasi 16 Menu Utama
            "Al-Quran" -> QuranScreen(onBack = { currentScreen = "HOME" })
            "Asmaul" -> AsmaulHusnaScreen(onBack = { currentScreen = "HOME" })
            "Tasbih" -> Tasbih3DScreen(onBack = { currentScreen = "HOME" })
            "Kiblat" -> KiblatScreen(onBack = { currentScreen = "HOME" })
            "Amalan" -> AmalanScreen(onBack = { currentScreen = "HOME" })
            "Kalkulator" -> KalkulatorZakatScreen(onBack = { currentScreen = "HOME" })
            "Khatam" -> KhatamTrackerScreen(onBack = { currentScreen = "HOME" })
            "Mewarnai" -> MewarnaiScreen(onBack = { currentScreen = "HOME" })
            "Komunitas" -> KomunitasScreen(onBack = { currentScreen = "HOME" })

            // Navigasi 6 Menu Inovatif Baru
            "KisahNabi" -> KisahNabiScreen(onBack = { currentScreen = "HOME" })
            "KuisIslami" -> KuisIslamiGameScreen(onBack = { currentScreen = "HOME" })
            "KalenderHijriah" -> KalenderHijriahScreen(onBack = { currentScreen = "HOME" })
            "KamusHalal" -> KamusHalalScreen(onBack = { currentScreen = "HOME" })
            "TanyaPanda" -> TanyaKakPandaScreen(onBack = { currentScreen = "HOME" })
            "PialaSertifikat" -> PialaSertifikatScreen(onBack = { currentScreen = "HOME" })

            else -> {
                val categoryMap = mapOf(
                    "Doa" to "doa",
                    "Tajwid" to "tajwid",
                    "Video" to "video",
                    "Belajar" to "belajar",
                    "Iqro" to "iqro",
                    "FAQ" to "faq"
                )
                val categoryKey = categoryMap[screen] ?: "belajar"
                DynamicContentScreen(
                    menuTitle = screen,
                    categoryKey = categoryKey,
                    onBack = { currentScreen = "HOME" }
                )
            }
        }
    }
}