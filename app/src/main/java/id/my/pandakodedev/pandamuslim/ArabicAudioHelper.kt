package id.my.pandakodedev.pandamuslim

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import java.net.URLEncoder
import java.util.Locale

/**
 * MESIN AUDIO TAJWID, MAKHRAJ IQRO & WAQAF ARAB ASLI (ANTI-LATIN & ANTI-CEPAT)
 * - Mengatur panjang pendek 2, 4, hingga 6 harakat (Mad Thabi'i, Mad Wajib, Mad Lazim)
 * - Memastikan makhraj huruf tebal (Kho, Sho, Dho, Tho, Zho, Gho, Qo) tepat 100%
 * - Memiliki Fallback Cloud Native Arabic Audio jika HP user tidak memiliki TTS Bahasa Arab
 */
object ArabicWaqafPhoneticEngine {

    /**
     * Fungsi Utama: Memutar Suara Arab (Makhraj Iqro, Doa, Hadits, Tajwid)
     * TIDAK PERNAH membaca teks Latin!
     */
    fun speakAuthenticArabic(
        arabicText: String,
        rawMediaUrl: String = "",
        tts: TextToSpeech?,
        mediaPlayer: MediaPlayer,
        onStartStatus: (String) -> Unit = {}
    ) {
        if (arabicText.isBlank()) return

        // 1. Cek apakah ada file MP3 Qari Asli (EveryAyah / Server Admin)
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
                    // Jika gagal memuat MP3, beralih ke Mesin Fonetik Tajwid Arab
                    speakWithTajwidEngineOrCloud(arabicText, tts, mediaPlayer, onStartStatus)
                    true
                }
                return
            } catch (_: Exception) {}
        }

        // 2. Jika tidak ada MP3 khusus, gunakan Mesin Tajwid & Makhraj Arab Asli
        speakWithTajwidEngineOrCloud(arabicText, tts, mediaPlayer, onStartStatus)
    }

    private fun speakWithTajwidEngineOrCloud(
        arabicText: String,
        tts: TextToSpeech?,
        mediaPlayer: MediaPlayer,
        onStartStatus: (String) -> Unit
    ) {
        // Olah teks Arab dengan aturan Panjang-Pendek Harakat (2, 4, 6 ketukan) & Waqaf
        val tajwidProcessedText = toTajwidAndWaqafRecitationText(arabicText)
        onStartStatus("🔊 Melafalkan Makhraj Arab: $arabicText")

        // Cek apakah TTS di HP pengguna benar-benar mendukung Voice Bahasa Arab Asli
        val isNativeArabicReady = tts != null && configureStrictArabicVoice(tts, speechRate = 0.62f)

        if (isNativeArabicReady && tts != null) {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
            } catch (_: Exception) {}
            tts.speak(tajwidProcessedText, TextToSpeech.QUEUE_FLUSH, null, "arab_tajwid_${System.currentTimeMillis()}")
        } else {
            // FALLBACK CLOUD ARABIC NATIVE STREAM:
            // Jika HP user tidak terinstal Voice Arab, jangan gunakan suara Indonesia!
            // Langsung stream suara Native Arab (ar) dari Cloud TTS Server!
            playCloudNativeArabicStream(tajwidProcessedText, mediaPlayer)
        }
    }

    /**
     * Memutar Suara Native Arab (ar-SA) langsung via Streaming jika HP user tidak punya paket suara Arab
     */
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

    fun configureBestArabicVoice(tts: TextToSpeech, speechRate: Float = 0.62f): Boolean {
        return configureStrictArabicVoice(tts, speechRate)
    }

    /**
     * Memaksa TTS menggunakan Voice Bahasa Arab Murni (Melepas kuncian Voice Bahasa Indonesia!)
     * Mengembalikan true jika Voice Arab tersedia di HP, false jika tidak ada.
     */
    fun configureStrictArabicVoice(tts: TextToSpeech, speechRate: Float = 0.62f): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val allVoices = tts.voices
                val arabicVoices = allVoices?.filter {
                    it.locale.language.equals("ar", ignoreCase = true)
                }.orEmpty()

                if (arabicVoices.isNotEmpty()) {
                    val bestArabVoice = arabicVoices.maxByOrNull { v ->
                        val n = v.name.lowercase()
                        var score = v.quality
                        if (n.contains("network") || n.contains("wavenet") || n.contains("neural")) score += 100
                        if (n.contains("ar-xa") || n.contains("ar-sa")) score += 50
                        score
                    }
                    if (bestArabVoice != null) {
                        tts.voice = bestArabVoice
                        tts.setPitch(0.96f)
                        tts.setSpeechRate(speechRate)
                        return true
                    }
                }
            }

            // Coba setLanguage standar
            val saudiLocale = Locale("ar", "SA")
            val res = tts.setLanguage(saudiLocale)
            if (res >= TextToSpeech.LANG_AVAILABLE) {
                tts.setPitch(0.96f)
                tts.setSpeechRate(speechRate)
                true
            } else {
                val genArab = Locale("ar")
                val res2 = tts.setLanguage(genArab)
                if (res2 >= TextToSpeech.LANG_AVAILABLE) {
                    tts.setPitch(0.96f)
                    tts.setSpeechRate(speechRate)
                    true
                } else {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * MESIN PENGOLAH PANJANG PENDEK HARAKAT (2, 4, 6 KETUKAN), MAKHRAJ IQRO & HUKUM WAQAF
     */
    fun toTajwidAndWaqafRecitationText(rawArabic: String): String {
        if (rawArabic.isBlank()) return ""

        // 1. Cek jika input berupa Huruf Muqaththa'ah (Pembuka Surah Mad Lazim 6 Harakat)
        val trimmedRaw = rawArabic.trim()
        when (trimmedRaw) {
            "الم", "الم ۝" -> return "أَلِفْ ، لَاااامْ ، مِييييمْ"
            "يس", "يس ۝" -> return "يَااااسِيييينْ"
            "طه", "طه ۝" -> return "طَااااهَاااا"
            "حم", "حم ۝" -> return "حَاااامِييييمْ"
            "كهيعص" -> return "كَاااافْ هَااااءْ يَااااءْ عَينْ صَاااادْ"
        }

        // 2. Pisahkan spasi ganda atau tanda pisah Iqro menjadi jeda koma Arab (،) agar tidak dibaca ngebut
        var text = trimmedRaw
            .replace("۝", " ، ")
            .replace("—", " ، ")
            .replace("-", " ، ")
            .replace("•", " ، ")
            .replace(Regex("\\s{2,}"), " ، ")

        // 3. Olah per segmen jeda koma
        val segments = text.split(Regex("[،.؛!؟]+")).map { it.trim() }.filter { it.isNotEmpty() }

        val processedSegments = segments.map { seg ->
            processSegmentMakhrajAndMad(seg)
        }

        // Gabungkan dengan koma Arab ganda agar ada jeda napas tartil yang jelas antar kata Iqro/Doa
        return processedSegments.joinToString(" ،  ")
    }

    private fun processSegmentMakhrajAndMad(segment: String): String {
        val cleanSeg = segment.trim()

        // A. KHUSUS HURUF TUNGGAL IQRO BERHARAKAT (Misal: خَ ، صَ ، ضَ ، طَ ، ظَ ، قَ ، بَ)
        // Agar makhrajnya keluar fasih (Kho, Sho, Dho, Tho, Zho, Qo) dan pendek 1 harakat tegas!
        val singleLetterMakhrajMap = mapOf(
            // Fathah (A - Ba - Ta - Kho - Sho...)
            "أَ" to "أَهْ", "اَ" to "أَهْ", "ءَ" to "أَهْ",
            "بَ" to "بَهْ", "تَ" to "تَهْ", "ثَ" to "ثَهْ",
            "جَ" to "جَهْ", "حَ" to "حَهْ", "خَ" to "خَوْهْ", // Kho tebal tenggorokan
            "دَ" to "دَهْ", "ذَ" to "ذَهْ", "رَ" to "رَوْهْ",
            "زَ" to "زَهْ", "سَ" to "سَهْ", "شَ" to "شَهْ",
            "صَ" to "صَوْهْ", "ضَ" to "ضَوْهْ", "طَ" to "طَوْهْ", "ظَ" to "ظَوْهْ",
            "عَ" to "عَهْ", "غَ" to "غَوْهْ", "فَ" to "فَهْ", "قَ" to "قَوْهْ",
            "كَ" to "كَهْ", "لَ" to "لَهْ", "مَ" to "مَهْ", "نَ" to "نَهْ",
            "وَ" to "وَهْ", "هَ" to "هَهْ", "هـَ" to "هَهْ", "يَ" to "يَهْ",
            // Kasrah (I - Bi - Ti...)
            "إِ" to "إِهْ", "اِ" to "إِهْ", "بِ" to "بِهْ", "تِ" to "تِهْ", "ثِ" to "ثِهْ",
            "جِ" to "جِهْ", "حِ" to "حِهْ", "خِ" to "خِهْ", "دِ" to "دِهْ", "ذِ" to "ذِهْ",
            "رِ" to "رِهْ", "زِ" to "زِهْ", "سِ" to "سِهْ", "شِ" to "شِهْ", "صِ" to "صِهْ",
            "ضِ" to "ضِهْ", "طِ" to "طِهْ", "ظِ" to "ظِهْ", "عِ" to "عِهْ", "غِ" to "غِهْ",
            "فِ" to "فِهْ", "قِ" to "قِهْ", "كِ" to "كِهْ", "لِ" to "لِهْ", "مِ" to "مِهْ",
            "نِ" to "نِهْ", "وِ" to "وِهْ", "هِ" to "هِهْ", "يِ" to "يِهْ",
            // Dhammah (U - Bu - Tu...)
            "أُ" to "أُهْ", "اُ" to "أُهْ", "بُ" to "بُهْ", "تُ" to "تُهْ", "ثُ" to "ثُهْ",
            "جُ" to "جُهْ", "حُ" to "حُهْ", "خُ" to "خُهْ", "دُ" to "دُهْ", "ذُ" to "ذُهْ",
            "رُ" to "رُهْ", "زُ" to "زُهْ", "سُ" to "سُهْ", "شُ" to "شُهْ", "صُ" to "صُهْ",
            "ضُ" to "ضُهْ", "طُ" to "طُهْ", "ظُ" to "ظُهْ", "عُ" to "عُهْ", "غُ" to "غُهْ",
            "فُ" to "فُهْ", "قُ" to "قُهْ", "كُ" to "كُهْ", "لُ" to "لُهْ", "مُ" to "مُهْ",
            "نُ" to "نُهْ", "وُ" to "وُهْ", "هُ" to "هُهْ", "يُ" to "يُهْ"
        )

        if (singleLetterMakhrajMap.containsKey(cleanSeg)) {
            return singleLetterMakhrajMap[cleanSeg]!!
        }

        // Jika segmen terdiri dari beberapa huruf tunggal yang dipisah spasi (contoh: "بَ تَ ثَ")
        val words = cleanSeg.split(Regex("\\s+")).toMutableList()
        if (words.all { singleLetterMakhrajMap.containsKey(it) }) {
            return words.joinToString(" ، ") { singleLetterMakhrajMap[it] ?: it }
        }

        // B. PROSES TARIKAN MAD PANJANG (2, 4, DAN 6 HARAKAT) PADA KATA/KALIMAT
        var s = cleanSeg

        // 1. Mad Lazim 6 Harakat (Tanda Bendera ~ diikuti huruf bertasydid, contoh: الضَّالِّينَ)
        s = s.replace(Regex("\u0627\u0653(.)\u0651"), "اااا$1\u0651")

        // 2. Mad Wajib Muttashil & Mad Jaiz Munfashil (4–5 Harakat: Tanda Bendera ~ atau Mad bertemu Hamzah ء/أ/إ)
        s = s.replace("\u0627\u0653", "اااا") // Alif + Bendera Mad -> tarik 4 harakat
        s = s.replace("آ", "أَااا") // Alif Maddah -> tarik 3-4 harakat
        s = s.replace("اء", "اااء") // Mad Alif bertemu Hamzah (contoh: جَاءَ -> جَاااءَ)
        s = s.replace("ِيء", "ِيييء") // Mad Ya bertemu Hamzah (contoh: سِيءَ)
        s = s.replace("ُوء", "ُوووء") // Mad Wawu bertemu Hamzah (contoh: سُوءَ)

        // 3. Fathah Tegak (\u0670), Kasrah Tegak (\u0656), Dhammah Terbalik (\u0657) -> Tarik 2 Harakat Penuh
        s = s.replace("\u0670", "\u064Eاا") // Fathah berdiri (contoh: هَٰذَا , الرَّحْمَٰنِ)
        s = s.replace("ۦ", "يي") // Ya kecil panjang 2 harakat (contoh: بِهِۦ)
        s = s.replace("ۥ", "وو") // Wawu kecil panjang 2 harakat (contoh: لَهُۥ)

        // 4. Ghunnah Nun & Mim Bertasydid (نّ / مّ) -> Tahan Dengung 2-3 Harakat
        s = s.replace("إِنَّ", "إِنْنَّ")
        s = s.replace("عَمَّ", "عَمْمَّ")
        s = s.replace("ثُمَّ", "ثُمْمَّ")

        // C. TERAPKAN ATURAN WAQAF (MATIKAN HARAKAT HURUF TERAKHIR)
        return applyWaqafRuleToPhraseEnd(s)
    }

    fun toAsmaulHusnaWaqaf(rawName: String): String {
        return toTajwidAndWaqafRecitationText(rawName)
    }

    fun toWaqafRecitationText(rawArabic: String): String {
        return toTajwidAndWaqafRecitationText(rawArabic)
    }

    private fun applyWaqafRuleToPhraseEnd(phrase: String): String {
        val words = phrase.split(Regex("\\s+")).toMutableList()
        if (words.isEmpty()) return phrase

        var lastWord = words.last()

        // 1. Aturan Waqaf Mad 'Iwadh: Fathatain + Alif (ـًا) di akhir kata dibaca panjang 2 harakat (ـَاا)
        if (lastWord.endsWith("\u064B\u0627") || lastWord.endsWith("\u0627\u064B")) {
            lastWord = lastWord.dropLast(2) + "\u064Eاا"
            words[words.lastIndex] = lastWord
            return words.joinToString(" ")
        }

        // 2. Aturan Waqaf Ta Marbuthah (ة): Semua harakat di atas 'ة' berubah menjadi Ha Sukun (هْ)
        val strippedLast = lastWord.replace(Regex("[\u064B-\u0652]+$"), "")
        if (strippedLast.endsWith("ة") || strippedLast.endsWith("ۀ")) {
            lastWord = strippedLast.dropLast(1) + "هْ"
            words[words.lastIndex] = lastWord
            return words.joinToString(" ")
        }

        // 3. Jika diakhiri huruf Mad (Alif ا / Wawu و / Ya ي / Alif Maqshurah ى) tanpa harakat hidup, tarik 2 harakat
        if (lastWord.endsWith("ا") || lastWord.endsWith("ى")) {
            return words.joinToString(" ")
        }

        // 4. Aturan Waqaf Umum (Sukun Aridh): Hapus harakat hidup paling ujung (-u, -a, -i, -un, -in) lalu pasang Sukun (ـْ)
        lastWord = strippedLast + "\u0652"
        words[words.lastIndex] = lastWord

        return words.joinToString(" ")
    }
}