package id.my.pandakodedev.pandamuslim

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

@Composable
fun FeedbackDialog(onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var responseText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Saran & Laporan Bug 🐼", fontWeight = FontWeight.ExtraBold, color = EmeraldDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (responseText.isNotEmpty()) {
                    Text(responseText, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                } else {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Anda") },
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
                            val result = sendFeedbackToServer(name, message)
                            withContext(Dispatchers.Main) {
                                isSending = false
                                responseText = result
                            }
                        }
                    },
                    enabled = !isSending && message.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isSending) "Mengirim..." else "Kirim ke Server")
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

fun sendFeedbackToServer(name: String, message: String): String {
    return try {
        val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=send_feedback")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true

        val jsonParam = JSONObject().apply {
            put("name", if (name.isBlank()) "Sahabat Panda" else name)
            put("email", "user@android.app")
            put("message", message)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonParam.toString()) }

        if (conn.responseCode == 200) {
            "Alhamdulillah! Masukan Anda sudah masuk ke Dashboard Admin."
        } else {
            "Gagal mengirim (Kode: ${conn.responseCode})"
        }
    } catch (e: Exception) {
        "Pastikan internet menyala: ${e.localizedMessage}"
    }
}
