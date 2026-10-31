package id.my.pandakodedev.pandamuslim

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.net.URLEncoder
import java.util.Locale

/**
 * MESIN AUDIO TAJWID GENERASI BARU (ANTI-TIPIS "ALLAH" & 100% MATI SAAT WAQAF)
 * 1. Mengubah fonetik Lafadz Jalalah setelah Fathah/Dhammah agar keluar suara bulat "Allooh"
 * 2. Memecah setiap frasa Waqaf menjadi antrean suara terpisah (QUEUE_ADD) + menghapus vokal akhir
 *    agar TTS 100% mematikan huruf terakhir di setiap jeda waqaf dan akhir kalimat!
 */
object ArabicWaqafPhoneticEngine {

    fun speakAuthenticArabic(
        arabicText: String,
        rawMediaUrl: String = "",
        tts: TextToSpeech?,
        mediaPlayer: MediaPlayer,
        onStartStatus: (String) -> Unit = {}
    ) {
        if (arabicText.isBlank()) return

        // 1. Prioritaskan MP3 Qari Asli jika tersedia di URL
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
                    speakPerWaqafChunks(arabicText, tts, mediaPlayer, onStartStatus)
                    true
                }
                return
            } catch (_: Exception) {}
        }

        // 2. Gunakan Mesin Pemecah Antrean Waqaf & Fonetik Tafkhim "Allooh"
        speakPerWaqafChunks(arabicText, tts, mediaPlayer, onStartStatus)
    }

    /**
     * Memecah bacaan per titik Waqaf (koma/tanda berhenti) lalu mengirimnya satu per satu
     * ke antrean TTS dengan jeda napas (Silent Pause), sehingga setiap ujung Waqaf PASTI MATI (Sukun)!
     */
    private fun speakPerWaqafChunks(
        rawArabic: String,
        tts: TextToSpeech?,
        mediaPlayer: MediaPlayer,
        onStartStatus: (String) -> Unit
    ) {
        onStartStatus("🔊 Melafalkan: $rawArabic")

        // Pecah kalimat menjadi daftar potongan Waqaf yang sudah diolah fonetik tebal & sukunnya
        val waqafChunks = buildPhoneticWaqafChunks(rawArabic)
        if (waqafChunks.isEmpty()) return

        val isNativeArabicReady = tts != null && configureStrictArabicVoice(tts, speechRate = 0.65f, pitch = 0.88f)

        if (isNativeArabicReady && tts != null) {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
            } catch (_: Exception) {}

            tts.stop()

            waqafChunks.forEachIndexed { index, chunk ->
                val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                tts.speak(chunk, queueMode, null, "waqaf_chunk_${System.currentTimeMillis()}_$index")

                // Tambahkan jeda napas (Waqaf) 320ms antar potongan kalimat agar tidak disambung oleh TTS!
                if (index < waqafChunks.lastIndex) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.playSilentUtterance(320L, TextToSpeech.QUEUE_ADD, "pause_$index")
                    }
                }
            }
        } else {
            // Fallback Cloud Native Arabic Stream
            playCloudNativeArabicStream(waqafChunks.joinToString(" . "), mediaPlayer)
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
        speechRate: Float = 0.65f,
        pitch: Float = 0.88f
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

    /**
     * Memecah teks Arab menjadi potongan-potongan frasa per tanda Waqaf (، / - / ۝ / .)
     * dan mengubah setiap potongan menjadi ejaan fonetik suara yang 100% tepat makhraj,
     * tebal pada "Allooh", dan MATI (Sukun) pada huruf paling ujung.
     */
    fun buildPhoneticWaqafChunks(rawArabic: String): List<String> {
        if (rawArabic.isBlank()) return emptyList()

        val trimmed = rawArabic.trim()
        when (trimmed) {
            "الم", "الم ۝" -> return listOf("أَلِفْ", "لَاااامْ", "مِييييمْ")
            "يس", "يس ۝" -> return listOf("يَااااسِيييينْ")
            "طه", "طه ۝" -> return listOf("طَااااهَاااا")
            "حم", "حم ۝" -> return listOf("حَاااامِييييمْ")
            "كهيعص" -> return listOf("كَاااافْ", "هَااااءْ", "يَااااءْ", "عَيْنْ", "صَاااادْ")
        }

        // Normalisasi semua tanda pemisah waqaf menjadi karakter '|'
        val normalizedSeparators = trimmed
            .replace("۝", "|")
            .replace("،", "|")
            .replace(",", "|")
            .replace("—", "|")
            .replace("-", "|")
            .replace("•", "|")
            .replace("؛", "|")
            .replace(".", "|")
            .replace(Regex("\\s{2,}"), "|")

        val rawSegments = normalizedSeparators.split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return rawSegments.map { seg ->
            convertSingleChunkToPhoneticTajwid(seg)
        }
    }

    fun toTajwidAndWaqafRecitationText(rawArabic: String): String {
        return buildPhoneticWaqafChunks(rawArabic).joinToString(" . ")
    }

    fun toAsmaulHusnaWaqaf(rawName: String): String {
        return convertSingleChunkToPhoneticTajwid(rawName)
    }

    fun toWaqafRecitationText(rawArabic: String): String {
        return buildPhoneticWaqafChunks(rawArabic).joinToString(" . ")
    }

    /**
     * Mengolah 1 potongan frasa Waqaf:
     * 1. Cek kamus emas Dzikir/Tasbih agar "Subhaanallooh", "Astaghfirullooh", "wa bihamdih" 100% akurat
     * 2. Suntikkan fonetik tebal "Allooh" (اللَّـاه)
     * 3. Matikan (Sukunkan) huruf paling ujung dari potongan ini!
     */
    private fun convertSingleChunkToPhoneticTajwid(segment: String): String {
        val cleanSeg = segment.trim()

        // 1. KAMUS HURUF TUNGGAL IQRO BERHARAKAT
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
            "نُ" to "نُهْ", "وُ" to "وُهْ", "هُ" to "هُهْ", "يُ" to "يُهْ"
        )

        if (singleLetterMakhrajMap.containsKey(cleanSeg)) {
            return singleLetterMakhrajMap[cleanSeg]!!
        }

        val splitSingleWords = cleanSeg.split(Regex("\\s+"))
        if (splitSingleWords.all { singleLetterMakhrajMap.containsKey(it) }) {
            return splitSingleWords.joinToString(" . ") { singleLetterMakhrajMap[it] ?: it }
        }

        // 2. MATIKAN HURUF TERAKHIR PADA POTONGAN WAQAF INI TERLEBIH DAHULU
        var s = forceKillLastVowelForWaqaf(cleanSeg)

        // 3. INJEKSI FONETIK TAFKHIM "ALLOOH" (MEMAKSA TTS MEMBACA TEBAL "LOH" BUKAN "LAH"!)
        // Rahasia Teknis: TTS Google Arab membaca "اللَّه" sebagai "Allah" tipis.
        // Tetapi jika setelah Fathah/Dhammah kita ubah fonetik suaranya menjadi "اللَّـاهْ" / "اللَّوْه"
        // dengan vokal pengunci Dhod/Wawu halus, TTS membulatkan suara menjadi "Allooh" tebal!
        s = injectHeavyAlloohPhonetics(s)

        // 4. TARIKAN MAD PANJANG (2, 4, 6 HARAKAT)
        s = s.replace(Regex("\u0627\u0653(.)\u0651"), "اااا$1\u0651")
        s = s.replace("\u0627\u0653", "اااا")
        s = s.replace("آ", "أَااا")
        s = s.replace("اء", "اااء")
        s = s.replace("ِيء", "ِيييء")
        s = s.replace("ُوء", "ُوووء")
        s = s.replace("ۦ", "يي")
        s = s.replace("ۥ", "وو")

        // 5. DENGUNG GHUNNAH
        s = s.replace("إِنَّ", "إِنْنَّ")
        s = s.replace("عَمَّ", "عَمْمَّ")
        s = s.replace("ثُمَّ", "ثُمْمَّ")

        return s
    }

    /**
     * Memaksa mesin TTS mengucapkan "Allooh" (tebal/bulat) ketika Lafadz Allah
     * didahului oleh Fathah atau Dhammah, dan tetap tipis "Lillaah" ketika didahului Kasrah.
     */
    private fun injectHeavyAlloohPhonetics(input: String): String {
        var s = input

        // Kamus Fonetik Dzikir Tasbih & Doa:
        // Menggunakan ejaan fonetik khusus TTS Arab agar keluar bunyi bulat "Allooh" & mati di ujung waqaf!
        val exactPhoneticOverrides = mapOf(
            // 1. Subhaanallooh
            "سُبْحَانَ اللَّهِ" to "سُبْحَانَ اللَّوْهْ",
            "سُبْحَانَ اللَّهْ" to "سُبْحَانَ اللَّوْهْ",
            "سُبْحَانَ الله" to "سُبْحَانَ اللَّوْهْ",
            // 2. Subhaanalloohi wa bihamdih (Mati di huruf Ha!)
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ" to "سُبْحَانَ اللَّوْهِ وَبِحَمْدِهْ",
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهْ" to "سُبْحَانَ اللَّوْهِ وَبِحَمْدِهْ",
            // 3. Subhaanalloohil 'Azhiim (Mati di huruf Mim!)
            "سُبْحَانَ اللَّهِ الْعَظِيمِ" to "سُبْحَانَ اللَّوْهِ الْعَظِييمْ",
            "سُبْحَانَ اللَّهِ الْعَظِيمْ" to "سُبْحَانَ اللَّوْهِ الْعَظِييمْ",
            // 4. Astaghfirulloohal 'Azhiim (Dibaca Astaghfirullooh, bukan Astaghfirullah!)
            "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ" to "أَسْتَغْفِرُ اللَّوْهَ الْعَظِييمْ",
            "أَسْتَغْفِرُ اللَّهَ الْعَظِيمْ" to "أَسْتَغْفِرُ اللَّوْهَ الْعَظِييمْ",
            "أَسْتَغْفِرُ اللَّهَ" to "أَسْتَغْفِرُ اللَّوْهْ",
            "أَسْتَغْفِرُ اللَّهْ" to "أَسْتَغْفِرُ اللَّوْهْ",
            // 5. Alloohu Akbar
            "اللَّهُ أَكْبَرُ" to "أَللَّوْهُ أَكْبَرْ",
            "اللَّهُ أَكْبَرْ" to "أَللَّوْهُ أَكْبَرْ",
            "وَاللَّهُ أَكْبَرُ" to "وَاللَّوْهُ أَكْبَرْ",
            "وَاللَّهُ أَكْبَرْ" to "وَاللَّوْهُ أَكْبَرْ",
            // 6. Laa ilaaha illallooh
            "لَا إِلَٰهَ إِلَّا اللَّهُ" to "لَاا إِلَاهَ إِلَّا اللَّوْهْ",
            "لَا إِلَٰهَ إِلَّا اللَّهْ" to "لَاا إِلَاهَ إِلَّا اللَّوْهْ",
            "وَلَا إِلَٰهَ إِلَّا اللَّهُ" to "وَلَاا إِلَاهَ إِلَّا اللَّوْهْ",
            "وَلَا إِلَٰهَ إِلَّا اللَّهْ" to "وَلَاا إِلَاهَ إِلَّا اللَّوْهْ",
            // 7. Laa haula wa laa quwwata illaa billaah (Kasrah = tipis billaah)
            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ" to "لَاا حَوْلَ وَلَاا قُوَّةَ إِلَّا بِاللَّاهْ",
            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهْ" to "لَاا حَوْلَ وَلَاا قُوَّةَ إِلَّا بِاللَّاهْ",
            // 8. Hasbunalloohu wa ni'mal wakiil
            "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ" to "حَسْبُنَا اللَّوْهُ وَنِعْمَ الْوَكِييلْ",
            "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلْ" to "حَسْبُنَا اللَّوْهُ وَنِعْمَ الْوَكِييلْ",
            // 9. Alhamdulillaah (Kasrah = tipis lillaah, mati di ujung)
            "الْحَمْدُ لِلَّهِ" to "الْحَمْدُ لِلَّاهْ",
            "الْحَمْدُ لِلَّهْ" to "الْحَمْدُ لِلَّاهْ",
            "وَالْحَمْدُ لِلَّهِ" to "وَالْحَمْدُ لِلَّاهْ",
            "وَالْحَمْدُ لِلَّهْ" to "وَالْحَمْدُ لِلَّاهْ"
        )

        if (exactPhoneticOverrides.containsKey(s)) {
            return exactPhoneticOverrides[s]!!
        }

        // Aturan Umum di Dalam Kalimat Panjang:
        // Jika اللَّه didahului Fathah atau Dhammah, ubah ke fonetik bulat اللَّوْه
        s = s.replace("سُبْحَانَ اللَّهِ", "سُبْحَانَ اللَّوْهِ")
        s = s.replace("أَسْتَغْفِرُ اللَّهَ", "أَسْتَغْفِرُ اللَّوْهَ")
        s = s.replace("اللَّهُمَّ", "أَللَّوْهُمَّ")
        s = s.replace("إِنْ شَاءَ اللَّهُ", "إِنْ شَاااءَ اللَّوْهْ")
        s = s.replace("إِنْ شَاءَ اللَّهْ", "إِنْ شَاااءَ اللَّوْهْ")
        s = s.replace("رَسُولُ اللَّهِ", "رَسُولُ اللَّوْهْ")
        s = s.replace("رَسُولُ اللَّهْ", "رَسُولُ اللَّوْهْ")
        s = s.replace("عَلَى اللَّهِ", "عَلَى اللَّوْهْ")
        s = s.replace("عَلَى اللَّهْ", "عَلَى اللَّوْهْ")
        s = s.replace("هُوَ اللَّهُ", "هُوَ اللَّوْهُ")
        s = s.replace("اللَّهُ الصَّمَدُ", "أَللَّوْهُ الصَّمَدْ")
        s = s.replace("اللَّهُ الصَّمَدْ", "أَللَّوْهُ الصَّمَدْ")

        // Pastikan kata وَبِحَمْدِهِ di mana saja selalu mati Ha-nya ("wa bihamdih")
        s = s.replace("وَبِحَمْدِهِ", "وَبِحَمْدِهْ")

        return s
    }

    /**
     * Menghapus total harakat hidup (-u, -a, -i, -un, -in) pada huruf paling ujung
     * dari setiap potongan Waqaf dan menguncinya dengan Sukun (ـْ), sehingga TTS
     * tidak mungkin lagi membacanya hidup!
     */
    private fun forceKillLastVowelForWaqaf(phrase: String): String {
        val words = phrase.split(Regex("\\s+")).toMutableList()
        if (words.isEmpty()) return phrase

        var lastWord = words.last()

        // 1. Jika kata terakhir adalah Lafadz Allah (اللَّهِ / اللَّهُ / اللَّهَ), matikan menjadi اللَّهْ
        if (lastWord.endsWith("اللَّهِ") || lastWord.endsWith("اللَّهُ") || lastWord.endsWith("اللَّهَ")) {
            lastWord = lastWord.dropLast(1) + "\u0652"
            words[words.lastIndex] = lastWord
            return words.joinToString(" ")
        }

        // 2. Aturan Waqaf Mad 'Iwadh: Fathatain + Alif (ـًا) di akhir kata dibaca panjang 2 harakat (ـَاا)
        if (lastWord.endsWith("\u064B\u0627") || lastWord.endsWith("\u0627\u064B")) {
            lastWord = lastWord.dropLast(2) + "\u064Eاا"
            words[words.lastIndex] = lastWord
            return words.joinToString(" ")
        }

        // 3. Kupas semua harakat vokal di ujung kata (Fathah \u064E, Dhammah \u064F, Kasrah \u0650, Tanwin \u064B-\u064D, Sukun \u0652)
        // TANPA menghapus Tasydid (\u0651)
        val strippedLast = lastWord.replace(Regex("[\u064B-\u0650\u0652\u0670]+$"), "")

        // 4. Aturan Waqaf Ta Marbuthah (ة): Semua 'ة' di akhir waqaf berubah menjadi Ha Sukun (هْ)
        if (strippedLast.endsWith("ة") || strippedLast.endsWith("ۀ")) {
            lastWord = strippedLast.dropLast(1) + "هْ"
            words[words.lastIndex] = lastWord
            return words.joinToString(" ")
        }

        // 5. Jika berakhir dengan huruf Mad (Alif ا / Alif Maqshurah ى), biarkan panjang
        if (strippedLast.endsWith("ا") || strippedLast.endsWith("ى")) {
            words[words.lastIndex] = strippedLast
            return words.joinToString(" ")
        }

        // 6. Jika sebelum huruf terakhir adalah Ya/Wawu Mad (contoh: الْعَظِيمِ / الْوَكِيلُ / الرَّحِيمِ),
        // gandakan huruf Mad-nya (الْعَظِييمْ) agar TTS membaca Mad Aridh Lissukun 2-4 harakat lalu berhenti MATI di huruf terakhir!
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