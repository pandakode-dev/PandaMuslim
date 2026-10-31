package id.my.pandakodedev.pandamuslim

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

object ServerAudioAndFeatureConfig {
    var arabSpeechRate: Float = 0.64f
    var arabPitch: Float = 0.88f
    var indoSpeechRate: Float = 0.90f
    var indoPitch: Float = 1.04f
    var customPhoneticMap: MutableMap<String, String> = mutableMapOf()

    var enableMisiKebaikan: Boolean = true
    var enableKisahWanita: Boolean = true
    var enableTtsIslami: Boolean = true
    var enableJurnalSyukur: Boolean = true
    var enableLukisKaligrafi: Boolean = true
    var enableImsakiyah: Boolean = true

    suspend fun syncFromWebServer() = withContext(Dispatchers.IO) {
        try {
            val ts = System.currentTimeMillis()
            val url = URL("https://pandamuslim.pandakode-dev.my.id/api.php?action=get_app_config&_t=$ts")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            if (conn.responseCode == 200) {
                val cfg = JSONObject(conn.inputStream.bufferedReader().readText()).getJSONObject("config")
                arabSpeechRate = cfg.optDouble("arab_speech_rate", 0.64).toFloat()
                arabPitch = cfg.optDouble("arab_pitch", 0.88).toFloat()
                indoSpeechRate = cfg.optDouble("indo_speech_rate", 0.90).toFloat()
                indoPitch = cfg.optDouble("indo_pitch", 1.04).toFloat()

                enableMisiKebaikan = cfg.optBoolean("enable_misi", true)
                enableKisahWanita = cfg.optBoolean("enable_wanita", true)
                enableTtsIslami = cfg.optBoolean("enable_tts", true)
                enableJurnalSyukur = cfg.optBoolean("enable_jurnal", true)
                enableLukisKaligrafi = cfg.optBoolean("enable_kaligrafi", true)
                enableImsakiyah = cfg.optBoolean("enable_imsakiyah", true)

                val rawRules = cfg.optString("phonetic_overrides", "")
                if (rawRules.isNotBlank()) {
                    val newMap = mutableMapOf<String, String>()
                    rawRules.lines().forEach { line ->
                        val parts = line.split("=")
                        if (parts.size == 2) {
                            newMap[parts[0].trim()] = parts[1].trim()
                        }
                    }
                    customPhoneticMap = newMap
                }
            }
        } catch (_: Exception) {}
    }
}

enum class StorytellerVoiceStyle(
    val label: String,
    val basePitch: Float,
    val baseRate: Float,
    val preferFemale: Boolean
) {
    KAKAK_LEMBUT("🌸 Suara Lembut (Natural)", 1.04f, 0.90f, true),
    USTADZ_TEDUH("🎙️ Suara Teduh (Wibawa)", 0.92f, 0.88f, false),
    CERIA_EKSPRESIF("✨ Suara Ceria Anak", 1.12f, 0.94f, true)
}

/**
 * ============================================================================
 * BAGIAN 1: MESIN PENDONGENG BAHASA INDONESIA LEMBUT & NATURAL
 * ============================================================================
 */
object IndonesianStorytellerEngine {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentSentences: List<String> = emptyList()
    private var currentStyle: StorytellerVoiceStyle = StorytellerVoiceStyle.KAKAK_LEMBUT
    private var isCurrentlyPlaying: Boolean = false
    private var onSentenceChangedCallback: ((Int) -> Unit)? = null
    private var onStoryFinishedCallback: (() -> Unit)? = null

    fun splitIntoNaturalSentences(rawTitle: String, rawBody: String): List<String> {
        val cleanTitle = rawTitle.replace(Regex("\\[.*?\\]"), "").trim()
        val combined = if (cleanTitle.isNotBlank()) "$cleanTitle. $rawBody" else rawBody

        return combined
            .replace("\r\n", "\n")
            .replace(Regex("\n+"), ". ")
            .split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.length > 2 }
    }

    fun humanizeIndonesianIslamicText(rawSentence: String): String {
        var s = rawSentence
            .replace(Regex("[📌🔊📖🌟⚠️🛡️•➔✅❌🕌🤲💧☝️🤝👑🏰🕊️🌸📚🐪🌍✒️🚢🌪️🕋🌊🏛️🏔️🦅⛰️🐋🌙✨🎉]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        s = s.replace(Regex("\\bSAW\\b"), "Shallallaahu 'alaihi wa sallam")
        s = s.replace("ﷺ", "Shallallaahu 'alaihi wa sallam")
        s = s.replace(Regex("\\bSWT\\b"), "Subhaanahu wa Ta'aalaa")
        s = s.replace(Regex("\\bAS\\b"), "'Alaihis salaam")
        s = s.replace(Regex("\\bRA\\b"), "Radhiyallaahu 'anhaa")
        s = s.replace(Regex("\\bHR\\.\\s*"), "Hadits Riwayat ")
        s = s.replace(Regex("\\bQS\\.\\s*"), "Al-Quran Surah ")
        s = s.replace("sholat", "shalat", ignoreCase = true)

        val storytellingConnectors = listOf(
            "Dahulu kala" to "Dahulu kala,",
            "Pada suatu hari" to "Pada suatu hari,",
            "Ketika itu" to "Ketika itu,",
            "Namun" to "Namun,",
            "Akhirnya" to "Akhirnya,",
            "Oleh karena itu" to "Oleh karena itu,",
            "Artinya:" to "Artinya, "
        )
        for ((oldWord, newWord) in storytellingConnectors) {
            if (s.startsWith(oldWord, ignoreCase = true) && !s.startsWith(newWord, ignoreCase = true)) {
                s = newWord + s.substring(oldWord.length)
            }
        }

        return s.trim()
    }

    fun configureSoftIndonesianVoice(
        tts: TextToSpeech,
        style: StorytellerVoiceStyle = StorytellerVoiceStyle.KAKAK_LEMBUT
    ) {
        val indoLocale = Locale("id", "ID")
        tts.language = indoLocale

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val indoVoices = tts.voices?.filter {
                    it.locale.language.equals("id", ignoreCase = true) ||
                            it.locale.language.equals("in", ignoreCase = true)
                }.orEmpty()

                if (indoVoices.isNotEmpty()) {
                    val bestVoice: Voice? = indoVoices.maxByOrNull { v ->
                        val name = v.name.lowercase()
                        var score = v.quality
                        if (name.contains("network") || name.contains("wavenet") || name.contains("neural")) {
                            score += 200
                        }
                        if (style.preferFemale) {
                            if (name.contains("female") || name.contains("ide") || name.contains("gad")) score += 80
                        } else {
                            if (name.contains("male") || name.contains("dfz") || name.contains("idc")) score += 80
                        }
                        score
                    }
                    if (bestVoice != null) {
                        tts.voice = bestVoice
                    }
                }
            } catch (_: Exception) {}
        }

        tts.setPitch(ServerAudioAndFeatureConfig.indoPitch)
        tts.setSpeechRate(ServerAudioAndFeatureConfig.indoSpeechRate)
    }

    fun startExpressiveStorytelling(
        tts: TextToSpeech?,
        sentences: List<String>,
        style: StorytellerVoiceStyle = StorytellerVoiceStyle.KAKAK_LEMBUT,
        onSentenceHighlight: (Int) -> Unit,
        onFinished: () -> Unit
    ) {
        if (tts == null || sentences.isEmpty()) {
            onFinished()
            return
        }

        stopStorytelling(tts)

        currentSentences = sentences
        currentStyle = style
        isCurrentlyPlaying = true
        onSentenceChangedCallback = onSentenceHighlight
        onStoryFinishedCallback = onFinished

        configureSoftIndonesianVoice(tts, style)

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                val idx = utteranceId?.removePrefix("indo_story_")?.toIntOrNull() ?: return
                mainHandler.post {
                    if (isCurrentlyPlaying) {
                        onSentenceChangedCallback?.invoke(idx)
                    }
                }
            }

            override fun onDone(utteranceId: String?) {
                val idx = utteranceId?.removePrefix("indo_story_")?.toIntOrNull() ?: return
                val nextIdx = idx + 1
                if (isCurrentlyPlaying && nextIdx < currentSentences.size) {
                    mainHandler.postDelayed({
                        if (isCurrentlyPlaying) {
                            speakSentenceAtIndex(tts, nextIdx)
                        }
                    }, 320L)
                } else if (nextIdx >= currentSentences.size) {
                    mainHandler.post {
                        isCurrentlyPlaying = false
                        onStoryFinishedCallback?.invoke()
                    }
                }
            }

            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    isCurrentlyPlaying = false
                    onStoryFinishedCallback?.invoke()
                }
            }
        })

        speakSentenceAtIndex(tts, 0)
    }

    private fun speakSentenceAtIndex(tts: TextToSpeech, index: Int) {
        if (!isCurrentlyPlaying || index !in currentSentences.indices) return

        val rawSentence = currentSentences[index]
        val humanizedText = humanizeIndonesianIslamicText(rawSentence)

        val basePitch = if (currentStyle == StorytellerVoiceStyle.KAKAK_LEMBUT) {
            ServerAudioAndFeatureConfig.indoPitch
        } else {
            currentStyle.basePitch
        }
        val baseRate = ServerAudioAndFeatureConfig.indoSpeechRate

        val dynamicPitch = when {
            index == 0 -> basePitch + 0.04f
            rawSentence.endsWith("!") -> basePitch + 0.06f
            rawSentence.endsWith("?") -> basePitch + 0.08f
            rawSentence.contains("berdoa", ignoreCase = true) ||
                    rawSentence.contains("Artinya", ignoreCase = true) -> basePitch - 0.04f
            index % 2 == 0 -> basePitch + 0.02f
            else -> basePitch - 0.01f
        }

        tts.setPitch(dynamicPitch)
        tts.setSpeechRate(baseRate)
        tts.speak(humanizedText, TextToSpeech.QUEUE_FLUSH, null, "indo_story_$index")
    }

    fun stopStorytelling(tts: TextToSpeech?) {
        isCurrentlyPlaying = false
        mainHandler.removeCallbacksAndMessages(null)
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }
}

/**
 * ============================================================================
 * BAGIAN 2: MESIN AUDIO TAJWID ARAB, MODE IQRO (HIDUP UTUH) & MODE DOA (WAQAF)
 * - Jika isIqroMode = true: Semua harakat akhir (Fathah/Kasrah/Dhammah/Tanwin)
 *   DIBACA HIDUP UTUH (misal: كَتَبَ = Ka-Ta-Ba, bukan Katab!), KECUALI ada tanda
 *   lingkaran waqaf (۝) atau simbol panah berhenti (➔).
 * - Jika isIqroMode = false (Doa, Tasbih, Asmaul Husna): Huruf terakhir di setiap
 *   jeda waqaf otomatis dimatikan (Sukun).
 * ============================================================================
 */
object ArabicWaqafPhoneticEngine {

    fun speakAuthenticArabic(
        arabicText: String,
        rawMediaUrl: String = "",
        tts: TextToSpeech?,
        mediaPlayer: MediaPlayer,
        isIqroMode: Boolean = false,
        onStartStatus: (String) -> Unit = {}
    ) {
        if (arabicText.isBlank()) return

        IndonesianStorytellerEngine.stopStorytelling(tts)

        val parts = rawMediaUrl.split("|")
        val directMp3Url = parts.find {
            it.trim().matches(Regex(".*\\.(mp3|wav|ogg|m4a)(\\?.*)?$", RegexOption.IGNORE_CASE))
        }?.trim() ?: ""

        if (directMp3Url.isNotBlank()) {
            try {
                tts?.stop()
                val finalUrl = normalizeServerMediaUrl(directMp3Url)
                mediaPlayer.reset()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mediaPlayer.setDataSource(finalUrl)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener { mp ->
                    onStartStatus("🎙️ Memutar Rekaman Suara Qari...")
                    mp.start()
                }
                mediaPlayer.setOnErrorListener { _, _, _ ->
                    speakPerChunks(arabicText, tts, mediaPlayer, isIqroMode, onStartStatus)
                    true
                }
                return
            } catch (_: Exception) {}
        }

        speakPerChunks(arabicText, tts, mediaPlayer, isIqroMode, onStartStatus)
    }

    private fun speakPerChunks(
        rawArabic: String,
        tts: TextToSpeech?,
        mediaPlayer: MediaPlayer,
        isIqroMode: Boolean,
        onStartStatus: (String) -> Unit
    ) {
        onStartStatus("🔊 Melafalkan: $rawArabic")

        val chunks = buildPhoneticChunks(rawArabic, isIqroMode = isIqroMode)
        if (chunks.isEmpty()) return

        val isNativeArabicReady = tts != null && configureStrictArabicVoice(
            tts = tts,
            speechRate = ServerAudioAndFeatureConfig.arabSpeechRate,
            pitch = ServerAudioAndFeatureConfig.arabPitch
        )

        if (isNativeArabicReady && tts != null) {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
            } catch (_: Exception) {}

            tts.stop()

            chunks.forEachIndexed { index, chunk ->
                val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                tts.speak(chunk, queueMode, null, "arab_chunk_${System.currentTimeMillis()}_$index")

                if (index < chunks.lastIndex) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.playSilentUtterance(320L, TextToSpeech.QUEUE_ADD, "pause_$index")
                    }
                }
            }
        } else {
            playCloudNativeArabicStream(chunks.joinToString(" ، "), mediaPlayer)
        }
    }

    private fun playCloudNativeArabicStream(arabicText: String, mediaPlayer: MediaPlayer) {
        try {
            val encoded = URLEncoder.encode(arabicText.take(180), "UTF-8")
            val streamUrl = "https://translate.google.com/translate_tts?ie=UTF-8&q=$encoded&tl=ar&client=tw-ob&ttsspeed=0.65"
            mediaPlayer.reset()
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mediaPlayer.setDataSource(streamUrl)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { it.start() }
        } catch (_: Exception) {}
    }

    fun configureStrictArabicVoice(
        tts: TextToSpeech,
        speechRate: Float = ServerAudioAndFeatureConfig.arabSpeechRate,
        pitch: Float = ServerAudioAndFeatureConfig.arabPitch
    ): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val arabicVoices = tts.voices?.filter {
                    it.locale.language.equals("ar", ignoreCase = true)
                }.orEmpty()

                if (arabicVoices.isNotEmpty()) {
                    val bestArabVoice = arabicVoices.maxByOrNull { v ->
                        val n = v.name.lowercase()
                        var score = v.quality
                        if (n.contains("network") || n.contains("wavenet") || n.contains("neural")) score += 100
                        if (n.contains("male") || n.contains("wavenet-b") || n.contains("wavenet-c")) score += 60
                        if (n.contains("ar-sa") || n.contains("ar-xa")) score += 40
                        score
                    }
                    if (bestArabVoice != null) {
                        tts.voice = bestArabVoice
                        tts.setPitch(pitch)
                        tts.setSpeechRate(speechRate)
                        return true
                    }
                }
            }

            val saudiLocale = Locale("ar", "SA")
            val res = tts.setLanguage(saudiLocale)
            if (res >= TextToSpeech.LANG_AVAILABLE) {
                tts.setPitch(pitch)
                tts.setSpeechRate(speechRate)
                true
            } else {
                val res2 = tts.setLanguage(Locale("ar"))
                if (res2 >= TextToSpeech.LANG_AVAILABLE) {
                    tts.setPitch(pitch)
                    tts.setSpeechRate(speechRate)
                    true
                } else false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun configureBestArabicVoice(tts: TextToSpeech, speechRate: Float = ServerAudioAndFeatureConfig.arabSpeechRate) {
        configureStrictArabicVoice(tts, speechRate = speechRate, pitch = ServerAudioAndFeatureConfig.arabPitch)
    }

    fun buildPhoneticWaqafChunks(rawArabic: String): List<String> {
        return buildPhoneticChunks(rawArabic, isIqroMode = false)
    }

    /**
     * Memecah teks menjadi segmen-segmen bacaan.
     * - Jika isIqroMode == true: mempertahankan simbol '۝' di dalam segmen agar sistem tahu
     *   mana kata Iqro yang harakat akhirnya wajib dibaca hidup utuh (tanpa '۝') dan mana yang latihan waqaf (ada '۝').
     */
    fun buildPhoneticChunks(rawArabic: String, isIqroMode: Boolean = false): List<String> {
        if (rawArabic.isBlank()) return emptyList()

        val trimmed = rawArabic.trim()
        when (trimmed) {
            "الم", "الم ۝" -> return listOf("أَلِفْ", "لَاااامْ", "مِييييمْ")
            "يس", "يس ۝" -> return listOf("يَااااسِيييينْ")
            "طه", "طه ۝" -> return listOf("طَااااهَاااا")
            "حم", "حم ۝" -> return listOf("حَاااامِييييمْ")
            "كهيعص" -> return listOf("كَاااافْ", "هَااااءْ", "يَااااءْ", "عَيْنْ", "صَاااادْ")
        }

        val normalizedSeparators = if (isIqroMode) {
            // Pada Mode Iqro, pertahankan tanda ۝ di ujung kata sebagai penanda latihan Waqaf Iqro 5-6
            trimmed
                .replace("۝", "۝|")
                .replace("،", "|")
                .replace(",", "|")
                .replace("—", "|")
                .replace("-", "|")
                .replace("•", "|")
                .replace("؛", "|")
                .replace(Regex("\\s{2,}"), "|")
        } else {
            trimmed
                .replace("۝", "|")
                .replace("،", "|")
                .replace(",", "|")
                .replace("—", "|")
                .replace("-", "|")
                .replace("•", "|")
                .replace("؛", "|")
                .replace(".", "|")
                .replace(Regex("\\s{2,}"), "|")
        }

        val rawSegments = normalizedSeparators.split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return rawSegments.map { seg ->
            convertSingleChunkToPhoneticTajwid(seg, isIqroMode = isIqroMode)
        }
    }

    fun toTajwidAndWaqafRecitationText(rawArabic: String, isIqroMode: Boolean = false): String {
        return buildPhoneticChunks(rawArabic, isIqroMode = isIqroMode).joinToString(" . ")
    }

    fun toAsmaulHusnaWaqaf(rawName: String): String {
        return convertSingleChunkToPhoneticTajwid(rawName, isIqroMode = false)
    }

    fun toWaqafRecitationText(rawArabic: String): String {
        return buildPhoneticChunks(rawArabic, isIqroMode = false).joinToString(" . ")
    }

    private fun convertSingleChunkToPhoneticTajwid(segment: String, isIqroMode: Boolean = false): String {
        var cleanSeg = segment.trim()

        // Cek apakah segmen ini memiliki tanda berhenti eksplisit (۝ atau ➔)
        val hasExplicitWaqafSymbol = cleanSeg.contains("۝") || cleanSeg.contains("➔")
        if ( cleanSeg.contains("➔")) {
            // Jika ada tanda panah contoh waqaf (misal: جَنَّةٌ ➔ جَنَّهْ), baca keduanya dengan jeda
            val arrowParts = cleanSeg.split("➔").map { it.trim() }
            if (arrowParts.size == 2) {
                val p1 = preserveIqroFullEndingVowels(arrowParts[0])
                val p2 = forceKillLastVowelForWaqaf(arrowParts[1])
                return "$p1 ، $p2"
            }
        }
        cleanSeg = cleanSeg.replace("۝", "").trim()

        if (ServerAudioAndFeatureConfig.customPhoneticMap.containsKey(cleanSeg)) {
            return ServerAudioAndFeatureConfig.customPhoneticMap[cleanSeg]!!
        }

        // 1. KAMUS HURUF TUNGGAL IQRO BERHARAKAT (FATHAH, KASRAH, DHAMMAH, TANWIN)
        val singleLetterMakhrajMap = mapOf(
            "أَ" to "أَهْ", "اَ" to "أَهْ", "ءَ" to "أَهْ",
            "بَ" to "بَهْ", "تَ" to "تَهْ", "ثَ" to "ثَهْ",
            "جَ" to "جَهْ", "حَ" to "حَهْ", "خَ" to "خَوْهْ",
            "دَ" to "دَهْ", "ذَ" to "ذَهْ", "رَ" to "رَوْهْ",
            "زَ" to "زَهْ", "سَ" to "سَهْ", "شَ" to "شَهْ",
            "صَ" to "صَوْهْ", "ضَ" to "ضَوْهْ", "طَ" to "طَوْهْ", "ظَ" to "ظَوْهْ",
            "عَ" to "عَهْ", "غَ" to "غَوْهْ", "فَ" to "فَهْ", "قَ" to "قَوْهْ",
            "كَ" to "كَهْ", "لَ" to "لَهْ", "مَ" to "مَهْ", "نَ" to "نَهْ",
            "وَ" to "وَهْ", "هَ" to "هَهْ", "هـَ" to "هَهْ", "يَ" to "يَهْ",
            "إِ" to "إِهْ", "اِ" to "إِهْ", "بِ" to "بِهْ", "تِ" to "تِهْ", "ثِ" to "ثِهْ",
            "جِ" to "جِهْ", "حِ" to "حِهْ", "خِ" to "خِهْ", "دِ" to "دِهْ", "ذِ" to "ذِهْ",
            "رِ" to "رِهْ", "زِ" to "زِهْ", "سِ" to "سِهْ", "شِ" to "شِهْ", "صِ" to "صِهْ",
            "ضِ" to "ضِهْ", "طِ" to "طِهْ", "ظِ" to "ظِهْ", "عِ" to "عِهْ", "غِ" to "غِهْ",
            "فِ" to "فِهْ", "قِ" to "قِهْ", "كِ" to "كِهْ", "لِ" to "لِهْ", "مِ" to "مِهْ",
            "نِ" to "نِهْ", "وِ" to "وِهْ", "هِ" to "هِهْ", "يِ" to "يِهْ",
            "أُ" to "أُهْ", "اُ" to "أُهْ", "بُ" to "بُهْ", "تُ" to "تُهْ", "ثُ" to "ثُهْ",
            "جُ" to "جُهْ", "حُ" to "حُهْ", "خُ" to "خُهْ", "دُ" to "دُهْ", "ذُ" to "ذُهْ",
            "رُ" to "رُهْ", "زُ" to "زُهْ", "سُ" to "سُهْ", "شُ" to "شُهْ", "صُ" to "صُهْ",
            "ضُ" to "ضُهْ", "طُ" to "طُهْ", "ظُ" to "ظُهْ", "عُ" to "عُهْ", "غُ" to "غُهْ",
            "فُ" to "فُهْ", "قُ" to "قُهْ", "كُ" to "كُهْ", "لُ" to "لُهْ", "مُ" to "مُهْ",
            "نُ" to "نُهْ", "وُ" to "وُهْ", "هُ" to "هُهْ", "يُ" to "يُهْ",
            // Tanwin Tunggal (Ban - Bin - Bun)
            "أً" to "أَنْ", "بً" to "بَنْ", "تً" to "تَنْ", "ثً" to "ثَنْ", "جً" to "جَنْ",
            "حً" to "حَنْ", "خً" to "خَوْنْ", "دً" to "دَنْ", "ذً" to "ذَنْ", "رً" to "رَوْنْ",
            "زً" to "زَنْ", "سً" to "سَنْ", "شً" to "شَنْ", "صً" to "صَوْنْ", "ضً" to "ضَوْنْ",
            "طً" to "طَوْنْ", "ظً" to "ظَوْنْ", "عً" to "عَنْ", "غً" to "غَوْنْ", "فً" to "فَنْ",
            "قً" to "قَوْنْ", "كً" to "كَنْ", "لً" to "لَنْ", "مً" to "مَنْ", "نً" to "نَنْ",
            "وً" to "وَنْ", "هً" to "هَنْ", "يً" to "يَنْ",
            "بٍ" to "بِنْ", "تٍ" to "تِنْ", "بٌ" to "بُنْ", "تٌ" to "تُنْ"
        )

        if (singleLetterMakhrajMap.containsKey(cleanSeg)) {
            return singleLetterMakhrajMap[cleanSeg]!!
        }

        val splitSingleWords = cleanSeg.split(Regex("\\s+"))
        if (splitSingleWords.all { singleLetterMakhrajMap.containsKey(it) }) {
            return splitSingleWords.joinToString(" ، ") { singleLetterMakhrajMap[it] ?: it }
        }

        // 2. PEMISAHAN LOGIKA AKHIR KATA: MODE IQRO (HIDUP UTUH) vs MODE DOA/TASBIH (WAQAF MATI)
        var s = if (isIqroMode && !hasExplicitWaqafSymbol) {
            // KHUSUS IQRO TANPA SIMBOL WAQAF: Baca semua harakat akhir secara hidup & utuh!
            preserveIqroFullEndingVowels(cleanSeg)
        } else {
            // UNTUK DOA, TASBIH, ASMAUL HUSNA, ATAU IQRO YANG ADA SIMBOL ۝: Matikan huruf akhir!
            forceKillLastVowelForWaqaf(cleanSeg)
        }

        // 3. Terapkan difton fonetik "Aullooh, Subhaanaullooh, Illaullooh"
        s = injectHeavyAulloohPhonetics(s, isIqroMode = isIqroMode && !hasExplicitWaqafSymbol)

        // 4. Tarikan Mad Panjang (2, 4, 6 Harakat)
        s = s.replace(Regex("\u0627\u0653(.)\u0651"), "اااا$1\u0651")
        s = s.replace("\u0627\u0653", "اااا")
        s = s.replace("آ", "أَااا")
        s = s.replace("اء", "اااء")
        s = s.replace("ِيء", "ِيييء")
        s = s.replace("ُوء", "ُوووء")
        s = s.replace("\u0670", "\u064Eاا") // Fathah berdiri (هَٰذَا , تَعَالَىٰ) ditarik 2 ketukan
        s = s.replace("ۦ", "يي")
        s = s.replace("ۥ", "وو")

        // 5. Dengung Ghunnah
        s = s.replace("إِنَّ", "إِنْنَّ")
        s = s.replace("عَمَّ", "عَمْمَّ")
        s = s.replace("ثُمَّ", "ثُمْمَّ")

        return s
    }

    /**
     * KHUSUS MODE IQRO (JILID 1-6 TANPA TANDA WAQAF ۝):
     * Memastikan huruf paling akhir pada kata Iqro TIDAK DIMATIKAN oleh mesin TTS!
     * - Contoh Iqro 2: كَتَبَ -> dikunci menjadi كَتَبَهْ sehingga 100% berbunyi "Ka-Ta-Ba" (bukan "Katab")
     * - Contoh Iqro 3: كُتِبَ -> كُتِبَهْ ("Ku-Ti-Ba"), يَقُولُ -> يَقُولُهْ ("Ya-Quu-Lu")
     * - Contoh Iqro 4: كِتَابًا -> كِتَابَنْ ("Kitaaban"), عَلِيمٌ -> عَلِيمُنْ ("'Aliimun"), حَكِيمٍ -> حَكِيمِنْ ("Hakiimin")
     * - Jika huruf terakhir memang sudah bertanda Sukun (أَبْ / أَنْعَمْتَ / لَمْ يَلِدْ), biarkan tetap Sukun!
     */
    private fun preserveIqroFullEndingVowels(phrase: String): String {
        val words = phrase.split(Regex("\\s+")).toMutableList()
        if (words.isEmpty()) return phrase

        var lastWord = words.last()

        when {
            // 1. Jika diakhiri Fathatain + Alif (ـًا atau ًا, contoh: كِتَابًا / كُفُوًا) -> Baca "-an" jelas!
            lastWord.endsWith("\u064B\u0627") || lastWord.endsWith("\u0627\u064B") -> {
                lastWord = lastWord.dropLast(2) + "\u064Eنْ"
            }
            // 2. Jika diakhiri Fathatain + Alif Maqshurah (ـًى, contoh: هُدًى) -> Baca "-an" jelas!
            lastWord.endsWith("\u064B\u0649") || lastWord.endsWith("\u0649\u064B") -> {
                lastWord = lastWord.dropLast(2) + "\u064Eنْ"
            }
            // 3. Jika diakhiri Fathatain biasa (ـً, contoh: جَنَّةً) -> Baca "-an"
            lastWord.endsWith("\u064B") -> {
                lastWord = lastWord.dropLast(1) + "\u064Eنْ"
            }
            // 4. Jika diakhiri Dhammatain (ـٌ, contoh: أَحَدٌ / عَلِيمٌ / جَنَّةٌ) -> Baca "-un" jelas!
            lastWord.endsWith("\u064C") -> {
                lastWord = lastWord.dropLast(1) + "\u064Fنْ"
            }
            // 5. Jika diakhiri Kasratain (ـٍ, contoh: حَكِيمٍ / عَمَدٍ) -> Baca "-in" jelas!
            lastWord.endsWith("\u064D") -> {
                lastWord = lastWord.dropLast(1) + "\u0650نْ"
            }
            // 6. Jika diakhiri Fathah (ـَ), Dhammah (ـُ), atau Kasrah (ـِ) pendek (contoh: كَتَبَ / يَقُولُ / بِسْمِ اللَّهِ)
            // Tambahkan Ha السكت halus (هْ) agar TTS wajib menyuarakan vokal terakhir tersebut dan tidak mematikannya!
            lastWord.endsWith("\u064E") || lastWord.endsWith("\u064F") || lastWord.endsWith("\u0650") -> {
                // Jika huruf terakhir adalah Ta Marbuthah berharakat (ةَ / ةُ / ةِ), ubah ke تَ / تُ / تِ agar berbunyi Ta/Tu/Ti
                if (lastWord.length >= 2 && lastWord[lastWord.length - 2] == 'ة') {
                    val harakat = lastWord.last()
                    lastWord = lastWord.dropLast(2) + "ت" + harakat + "هْ"
                } else {
                    lastWord += "هْ"
                }
            }
            // 7. Jika diakhiri Sukun (ـْ) atau huruf Mad (ا / و / ي / ى), biarkan apa adanya!
        }

        words[words.lastIndex] = lastWord
        return words.joinToString(" ")
    }

    private fun injectHeavyAulloohPhonetics(input: String, isIqroMode: Boolean = false): String {
        var s = input

        for ((orig, repl) in ServerAudioAndFeatureConfig.customPhoneticMap) {
            if (s.contains(orig)) {
                s = s.replace(orig, repl)
            }
        }

        if (isIqroMode) {
            // Pada Mode Iqro, jaga harakat akhir Lafadz Allah tetap hidup sesuai tulisannya!
            s = s.replace("مِنَ اللَّهِهْ", "مِنَوْللَّوْهِهْ")
            s = s.replace("رَسُولُ اللَّهِهْ", "رَسُولُوْللَّوْهِهْ")
            s = s.replace("هُوَ اللَّهُهْ", "هُوَوْللَّوْهُهْ")
            s = s.replace("اللَّهُهْ", "أَوْللَّوْهُهْ")
            return s
        }

        val exactAulloohOverrides = mapOf(
            "اللَّهُ أَكْبَرُ" to "أَوْللَّوْهُ أَكْبَرْ",
            "اللَّهُ أَكْبَرْ" to "أَوْللَّوْهُ أَكْبَرْ",
            "وَاللَّهُ أَكْبَرُ" to "وَاَوْللَّوْهُ أَكْبَرْ",
            "وَاللَّهُ أَكْبَرْ" to "وَاَوْللَّوْهُ أَكْبَرْ",
            "سُبْحَانَ اللَّهِ" to "سُبْحَانَوْللَّوْهْ",
            "سُبْحَانَ اللَّهْ" to "سُبْحَانَوْللَّوْهْ",
            "سُبْحَانَ الله" to "سُبْحَانَوْللَّوْهْ",
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ" to "سُبْحَانَوْللَّوْهِ وَبِحَمْدِهْ",
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهْ" to "سُبْحَانَوْللَّوْهِ وَبِحَمْدِهْ",
            "سُبْحَانَ اللَّهِ الْعَظِيمِ" to "سُبْحَانَوْللَّوْهِ الْعَظِييمْ",
            "سُبْحَانَ اللَّهِ الْعَظِيمْ" to "سُبْحَانَوْللَّوْهِ الْعَظِييمْ",
            "لَا إِلَٰهَ إِلَّا اللَّهُ" to "لَاا إِلَاهَ إِلَّاوْللَّوْهْ",
            "لَا إِلَٰهَ إِلَّا اللَّهْ" to "لَاا إِلَاهَ إِلَّاوْللَّوْهْ",
            "وَلَا إِلَٰهَ إِلَّا اللَّهُ" to "وَلَاا إِلَاهَ إِلَّاوْللَّوْهْ",
            "وَلَا إِلَٰهَ إِلَّا اللَّهْ" to "وَلَاا إِلَاهَ إِلَّاوْللَّوْهْ",
            "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ" to "أَسْتَغْفِرُوْللَّوْهَ الْعَظِييمْ",
            "أَسْتَغْفِرُ اللَّهَ الْعَظِيمْ" to "أَسْتَغْفِرُوْللَّوْهَ الْعَظِييمْ",
            "أَسْتَغْفِرُ اللَّهَ" to "أَسْتَغْفِرُوْللَّوْهْ",
            "أَسْتَغْفِرُ اللَّهْ" to "أَسْتَغْفِرُوْللَّوْهْ",
            "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ" to "حَسْبُنَاوْللَّوْهُ وَنِعْمَ الْوَكِييلْ",
            "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلْ" to "حَسْبُنَاوْللَّوْهُ وَنِعْمَ الْوَكِييلْ",
            "الْحَمْدُ لِلَّهِ" to "الْحَمْدُ لِلَّاهْ",
            "الْحَمْدُ لِلَّهْ" to "الْحَمْدُ لِلَّاهْ",
            "وَالْحَمْدُ لِلَّهِ" to "وَالْحَمْدُ لِلَّاهْ",
            "وَالْحَمْدُ لِلَّهْ" to "وَالْحَمْدُ لِلَّاهْ",
            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ" to "لَاا حَوْلَ وَلَاا قُوَّةَ إِلَّا بِاللَّاهْ",
            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهْ" to "لَاا حَوْلَ وَلَاا قُوَّةَ إِلَّا بِاللَّاهْ"
        )

        if (exactAulloohOverrides.containsKey(s)) {
            return exactAulloohOverrides[s]!!
        }

        s = s.replace("سُبْحَانَ اللَّهِ", "سُبْحَانَوْللَّوْهِ")
        s = s.replace("أَسْتَغْفِرُ اللَّهَ", "أَسْتَغْفِرُوْللَّوْهَ")
        s = s.replace("إِلَّا اللَّهُ", "إِلَّاوْللَّوْهْ")
        s = s.replace("إِلَّا اللَّهْ", "إِلَّاوْللَّوْهْ")
        s = s.replace("اللَّهُمَّ", "أَوْللَّوْهُمَّ")
        s = s.replace("إِنْ شَاءَ اللَّهُ", "إِنْ شَاااءَوْللَّوْهْ")
        s = s.replace("إِنْ شَاءَ اللَّهْ", "إِنْ شَاااءَوْللَّوْهْ")
        s = s.replace("مَا شَاءَ اللَّهُ", "مَا شَاااءَوْللَّوْهْ")
        s = s.replace("رَسُولُ اللَّهِ", "رَسُولُوْللَّوْهْ")
        s = s.replace("رَسُولُ اللَّهْ", "رَسُولُوْللَّوْهْ")
        s = s.replace("عَلَى اللَّهِ", "عَلَوْللَّوْهْ")
        s = s.replace("عَلَى اللَّهْ", "عَلَوْللَّوْهْ")
        s = s.replace("هُوَ اللَّهُ", "هُوَوْللَّوْهُ")
        s = s.replace("اللَّهُ الصَّمَدُ", "أَوْللَّوْهُ الصَّمَدْ")
        s = s.replace("اللَّهُ الصَّمَدْ", "أَوْللَّوْهُ الصَّمَدْ")
        s = s.replace("وَبِحَمْدِهِ", "وَبِحَمْدِهْ")

        return s
    }

    private fun forceKillLastVowelForWaqaf(phrase: String): String {
        val words = phrase.split(Regex("\\s+")).toMutableList()
        if (words.isEmpty()) return phrase

        var lastWord = words.last()

        if (lastWord.endsWith("اللَّهِ") || lastWord.endsWith("اللَّهُ") || lastWord.endsWith("اللَّهَ")) {
            lastWord = lastWord.dropLast(1) + "\u0652"
            words[words.lastIndex] = lastWord
            return words.joinToString(" ")
        }

        if (lastWord.endsWith("\u064B\u0627") || lastWord.endsWith("\u0627\u064B")) {
            lastWord = lastWord.dropLast(2) + "\u064Eاا"
            words[words.lastIndex] = lastWord
            return words.joinToString(" ")
        }

        val strippedLast = lastWord.replace(Regex("[\u064B-\u0650\u0652\u0670]+$"), "")

        if (strippedLast.endsWith("ة") || strippedLast.endsWith("ۀ")) {
            lastWord = strippedLast.dropLast(1) + "هْ"
            words[words.lastIndex] = lastWord
            return words.joinToString(" ")
        }

        if (strippedLast.endsWith("ا") || strippedLast.endsWith("ى")) {
            words[words.lastIndex] = strippedLast
            return words.joinToString(" ")
        }

        lastWord = if (strippedLast.length >= 3) {
            val lastChar = strippedLast.last()
            val prevChar = strippedLast[strippedLast.length - 2]
            if (prevChar == 'ي' || prevChar == 'و') {
                strippedLast.dropLast(1) + prevChar + lastChar + "\u0652"
            } else {
                strippedLast + "\u0652"
            }
        } else {
            strippedLast + "\u0652"
        }

        words[words.lastIndex] = lastWord
        return words.joinToString(" ")
    }
}