package id.my.pandakodedev.pandamuslim

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

data class CommunityPost(
    val id: Int,
    val userName: String,
    val userBadge: String,
    val message: String,
    val likes: Int,
    val createdAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KomunitasScreen(onBack: () -> Unit) {
    var posts by remember { mutableStateOf<List<CommunityPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var senderName by remember { mutableStateOf("") }
    var newMessage by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    fun reloadPosts() {
        CoroutineScope(Dispatchers.IO).launch {
            val loaded = fetchCommunityPosts()
            withContext(Dispatchers.Main) {
                posts = loaded
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        reloadPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_panda_komunitas),
                            contentDescription = "Komunitas Panda",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Komunitas Sahabat Panda", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 17.sp)
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
            // Daftar Obrolan & Doa Komunitas
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (isLoading) {
                    CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(posts, key = { it.id }) { post ->
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(MintSurface)
                                            ) {
                                                Text("🐼", fontSize = 18.sp)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(post.userName, fontWeight = FontWeight.ExtraBold, color = EmeraldDark, fontSize = 14.sp)
                                                Text(post.userBadge, color = EmeraldPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }

                                        FilledTonalButton(
                                            onClick = {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    likeCommunityPost(post.id)
                                                    reloadPosts()
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Default.Favorite, contentDescription = "Aamiin", tint = Color(0xFFE11D48), modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Aamiin (${post.likes})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(post.message, fontSize = 14.sp, color = Color(0xFF334155))
                                }
                            }
                        }
                    }
                }
            }

            // Form Kirim Pesan / Doa ke Komunitas
            Surface(
                color = Color.White,
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        placeholder = { Text("Nama Anda (contoh: Hamba Allah / Budi)", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = newMessage,
                            onValueChange = { newMessage = it },
                            placeholder = { Text("Tulis doa, salam, atau diskusi kebaikan...", fontSize = 13.sp) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                        FloatingActionButton(
                            onClick = {
                                if (newMessage.isNotBlank() && !isPosting) {
                                    isPosting = true
                                    CoroutineScope(Dispatchers.IO).launch {
                                        postToCommunity(
                                            name = if (senderName.isBlank()) "Sahabat Panda" else senderName,
                                            message = newMessage
                                        )
                                        withContext(Dispatchers.Main) {
                                            newMessage = ""
                                            isPosting = false
                                            reloadPosts()
                                        }
                                    }
                                }
                            },
                            containerColor = EmeraldPrimary
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

suspend fun fetchCommunityPosts(): List<CommunityPost> = withContext(Dispatchers.IO) {
    val list = mutableListOf<CommunityPost>()
    try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=get_community")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().readText()
            val arr = JSONObject(response).getJSONArray("data")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    CommunityPost(
                        id = obj.getInt("id"),
                        userName = obj.getString("user_name"),
                        userBadge = obj.optString("user_badge", "Sahabat Panda"),
                        message = obj.getString("message"),
                        likes = obj.optInt("likes", 0),
                        createdAt = obj.optString("created_at", "")
                    )
                )
            }
        }
    } catch (_: Exception) {}
    list
}

fun postToCommunity(name: String, message: String) {
    try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=post_community")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true
        val json = JSONObject().apply {
            put("user_name", name)
            put("user_badge", "Jamaah PandaMuslim")
            put("message", message)
        }
        OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
        conn.responseCode
    } catch (_: Exception) {}
}

fun likeCommunityPost(id: Int) {
    try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=like_community&id=$id")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.responseCode
    } catch (_: Exception) {}
}