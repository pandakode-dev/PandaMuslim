package id.my.pandakodedev.pandamuslim

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// =========================================================================
// 1. ALIAS TIPE DATA
// =========================================================================
typealias ServerContentItem = ServerContent

// =========================================================================
// 2. FUNGSI FETCH DATA DARI SERVER API (Mengembalikan List<ServerContent>)
// =========================================================================
fun fetchContentsFromServer(categoryKey: String = ""): List<ServerContent> {
    val result = mutableListOf<ServerContent>()
    try {
        val encodedCat = java.net.URLEncoder.encode(categoryKey, "UTF-8")
        val endpoint = if (categoryKey.isBlank()) {
            "https://pandamuslim.pandakode-dev.my.id/api.php?action=get_contents"
        } else {
            "https://pandamuslim.pandakode-dev.my.id/api.php?action=get_contents&category=$encodedCat"
        }
        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
        }
        if (conn.responseCode in 200..299) {
            val raw = conn.inputStream.bufferedReader().readText().trim()
            val arr: JSONArray? = when {
                raw.startsWith("[") -> JSONArray(raw)
                raw.startsWith("{") -> {
                    val obj = JSONObject(raw)
                    obj.optJSONArray("data") ?: obj.optJSONArray("contents") ?: obj.optJSONArray("items")
                }
                else -> null
            }
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val media = o.optString("media_url", o.optString("mediaUrl", o.optString("image_url", "")))
                    result.add(
                        ServerContent(
                            id = o.optInt("id", i + 1),
                            title = o.optString("title", ""),
                            arabicText = o.optString("arabic_text", o.optString("arabicText", o.optString("arabic", ""))),
                            latinText = o.optString("latin_text", o.optString("latinText", o.optString("latin", ""))),
                            description = o.optString("description", o.optString("meaning", o.optString("content", ""))),
                            mediaUrl = media
                        )
                    )
                }
            }
        }
    } catch (_: Exception) {
        // Fallback ke data lokal jika koneksi gagal
    }
    return result
}

// =========================================================================
// 3. HELPER NORMALISASI URL MEDIA & YOUTUBE
// =========================================================================
fun normalizeServerMediaUrl(rawUrl: String?): String {
    if (rawUrl.isNullOrBlank()) return ""
    val clean = rawUrl.trim()
    val formatted = if (clean.startsWith("http://") || clean.startsWith("https://")) {
        clean
    } else {
        "https://pandamuslim.pandakode-dev.my.id/${clean.trimStart('/')}"
    }
    return formatted.replace(" ", "%20")
}

fun extractYoutubeVideoId(url: String?): String? {
    if (url.isNullOrBlank()) return null
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

fun extractMediaPreviewUrl(url: String?): String {
    if (url.isNullOrBlank()) return ""
    val parts = url.split("|")

    // 1. Cek jika ada gambar langsung (.jpg / .png / .webp / .gif)
    for (part in parts) {
        val clean = part.trim()
        if (clean.matches(Regex(".*\\.(jpg|jpeg|png|webp|gif)(\\?.*)?$", RegexOption.IGNORE_CASE))) {
            return normalizeServerMediaUrl(clean)
        }
    }

    // 2. Cek jika mengandung Link YouTube -> ambil Thumbnail HQ otomatis!
    for (part in parts) {
        val ytId = extractYoutubeVideoId(part.trim())
        if (!ytId.isNullOrBlank()) {
            return "https://img.youtube.com/vi/$ytId/hqdefault.jpg"
        }
    }

    val directYt = extractYoutubeVideoId(url)
    if (!directYt.isNullOrBlank()) {
        return "https://img.youtube.com/vi/$directYt/hqdefault.jpg"
    }

    return normalizeServerMediaUrl(url)
}

fun extractImageUrl(rawMediaUrl: String?): String = extractMediaPreviewUrl(rawMediaUrl)

// =========================================================================
// 4. KOMPONEN UI GAMBAR JARINGAN (Mendukung parameter imageUrl & url)
// =========================================================================
@Composable
fun NetworkImageLoader(
    imageUrl: String? = null,
    url: String? = null,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val targetUrl = imageUrl ?: url
    val normalizedUrl = remember(targetUrl) { normalizeServerMediaUrl(targetUrl) }
    var imageBitmap by remember(normalizedUrl) { mutableStateOf<ImageBitmap?>(null) }
    var isFailed by remember(normalizedUrl) { mutableStateOf(false) }

    LaunchedEffect(normalizedUrl) {
        if (normalizedUrl.isBlank()) {
            isFailed = true
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val conn = (URL(normalizedUrl).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Android; PandaMuslimApp)")
                    doInput = true
                }
                conn.connect()
                if (conn.responseCode in 200..299) {
                    val bmp = BitmapFactory.decodeStream(conn.inputStream)
                    if (bmp != null) {
                        imageBitmap = bmp.asImageBitmap()
                    } else {
                        isFailed = true
                    }
                } else {
                    isFailed = true
                }
            } catch (_: Exception) {
                isFailed = true
            }
        }
    }

    Box(
        modifier = modifier.background(EmeraldDark),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageBitmap != null -> {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = contentDescription,
                    contentScale = contentScale,
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

// =========================================================================
// 5. KOMPONEN ORNAMEN SILUET MASJID (Mendukung parameter accentColor & tintColor)
// =========================================================================
@Composable
fun MosqueSilhouetteOrnamentCanvas(
    accentColor: Color = GoldAccent,
    modifier: Modifier = Modifier,
    tintColor: Color = accentColor
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = w * 0.45f,
            center = Offset(w * 0.85f, h * 0.2f)
        )

        val lanternColor = tintColor.copy(alpha = 0.28f)
        val lanternPositions = listOf(w * 0.88f to h * 0.22f, w * 0.76f to h * 0.16f)
        lanternPositions.forEach { (lx, ly) ->
            drawLine(color = lanternColor, start = Offset(lx, 0f), end = Offset(lx, ly), strokeWidth = 3f)
            drawCircle(color = lanternColor, radius = 16f, center = Offset(lx, ly + 16f), style = Stroke(width = 3f))
            drawCircle(color = tintColor.copy(alpha = 0.45f), radius = 6f, center = Offset(lx, ly + 16f))
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
            color = tintColor.copy(alpha = 0.35f),
            start = Offset(w * 0.50f, baseLineY - 65f),
            end = Offset(w * 0.50f, baseLineY - 92f),
            strokeWidth = 4f
        )
        drawCircle(color = tintColor.copy(alpha = 0.40f), radius = 8f, center = Offset(w * 0.50f, baseLineY - 100f))

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
