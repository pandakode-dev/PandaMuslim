package id.my.pandakodedev.pandamuslim

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ServerContent(
    val id: Int,
    val title: String,
    val arabicText: String,
    val latinText: String,
    val description: String,
    val mediaUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicContentScreen(
    menuTitle: String,
    categoryKey: String, // "doa", "tajwid", "video", "belajar", "iqro"
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var items by remember { mutableStateOf<List<ServerContent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(categoryKey) {
        items = fetchContentsFromServer(categoryKey)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(menuTitle, fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBg)
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = EmeraldPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (items.isEmpty()) {
                Text(
                    text = "Belum ada materi di kategori $menuTitle.\nSilakan tambahkan melalui Web Admin.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = EmeraldDark
                                )
                                if (item.arabicText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = item.arabicText,
                                        fontSize = 24.sp,
                                        color = Color(0xFF065F46),
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (item.latinText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.latinText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 14.sp,
                                    color = Color(0xFF334155)
                                )
                                if (item.mediaUrl.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.mediaUrl))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Putar Video / Buka Materi")
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

suspend fun fetchContentsFromServer(category: String): List<ServerContent> = withContext(Dispatchers.IO) {
    val resultList = mutableListOf<ServerContent>()
    try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=get_contents&category=$category")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
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
                        mediaUrl = obj.optString("media_url", "")
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    resultList
}