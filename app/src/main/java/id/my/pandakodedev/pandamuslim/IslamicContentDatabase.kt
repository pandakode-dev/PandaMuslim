package id.my.pandakodedev.pandamuslim

// =========================================================================
// PUSAT DATABASE MATERI BUILT-IN PANDAMUSLIM
// Jika nanti ingin menambah materi baru, cukup tambahkan di file ini saja!
// =========================================================================
fun getBuiltInFallbackContents(category: String): List<ServerContent> {
    return when (category) {
        "iqro" -> getBuiltInIqroCurriculum()
        "belajar" -> getBuiltInBelajarCurriculum()
        "tajwid" -> getBuiltInTajwidCurriculum()
        "video" -> getBuiltInVideoCurriculum()
        "faq" -> getBuiltInFaqCurriculum()
        else -> getBuiltInDoaAndHaditsCurriculum()
    }
}

fun getBuiltInIqroCurriculum(): List<ServerContent> = listOf(
    ServerContent(901, "Iqro 1 • Hal 1 (Alif - Ba)", "أَ   بَ", "A  —  Ba", "Mulut terbuka saat 'A' dan bibir merapat saat 'Ba'.", "https://everyayah.com/data/Husary_Muallim_128kbps/001001.mp3", 1),
    ServerContent(902, "Iqro 1 • Hal 2 (Ba - Ta - Tsa)", "بَ   تَ   ثَ", "Ba — Ta — Tsa", "Titik satu di bawah Ba, titik dua di atas Ta, titik tiga di atas Tsa.", "", 1),
    ServerContent(903, "Iqro 1 • Hal 3 (Jim - Ha - Kho)", "جَ   حَ   خَ", "Ja — Ha — Kho", "Jim bertitik di tengah, Ha bersih di tenggorokan, Kho sedikit bergetar.", "", 1),
    ServerContent(904, "Iqro 1 • Hal 4 (Dal - Dzal)", "دَ   ذَ   جَدَ", "Da — Dza — Ja-Da", "Sentuhkan ujung lidah ke gigi seri atas untuk huruf Dza.", "", 1),
    ServerContent(905, "Iqro 1 • Hal 5 (Ro - Za)", "رَ   زَ   رَزَ", "Ro — Za — Ro-Za", "Huruf Ro bergetar tebal, sedangkan Za berdesis halus.", "", 1),
    ServerContent(906, "Iqro 1 • Hal 6 (Sin - Syin)", "سَ   شَ   سَحَ", "Sa — Sya — Sa-Ha", "Bedakan desis tipis Sin (Sa) dan hembusan lebar Syin (Sya).", "", 1),
    ServerContent(907, "Iqro 1 • Hal 7 (Shod - Dhod - Tho - Zho)", "صَ   ضَ   طَ   ظَ", "Sho — Dho — Tho — Zho", "Keempat huruf ini dibaca tebal (Isti'la) memenuhi rongga mulut.", "", 1),
    ServerContent(908, "Iqro 1 • Hal 8 (Ain s/d Ya)", "عَ  غَ  فَ  قَ  كَ  لَ  مَ  نَ  وَ  هَ  يَ", "'A - Gho - Fa - Qo - Ka - La - Ma - Na - Wa - Ha - Ya", "Penutup Iqro Jilid 1! Pastikan semua huruf Fathah terbaca tegas.", "", 1),
    ServerContent(921, "Iqro 2 • Hal 1 (Sambung Ba-Ta)", "بَتَ   تَبَ", "Ba-Ta  —  Ta-Ba", "Mengenal bentuk huruf Ba dan Ta ketika disambung.", "", 2),
    ServerContent(922, "Iqro 2 • Hal 2 (Sambung 3 Huruf)", "نَبَتَ   ثَبَتَ", "Na-Ba-Ta — Tsa-Ba-Ta", "Bacalah pendek setiap huruf yang disambung.", "", 2),
    ServerContent(923, "Iqro 2 • Hal 3 (Sambung Jim-Ha-Kho)", "جَحَدَ   خَرَجَ", "Ja-Ha-Da — Kho-Ro-Ja", "Kepala huruf Jim, Ha, Kho tetap terlihat jelas saat disambung.", "", 2),
    ServerContent(924, "Iqro 2 • Hal 4 (Sambung Ain-Ghain-Fa-Qof)", "عَمَلَ   فَتَحَ", "'A-Ma-La — Fa-Ta-Ha", "Perhatikan bentuk huruf Ain dan Fa ketika berada di awal kata.", "", 2),
    ServerContent(925, "Iqro 2 • Hal 5 (Mad Panjang Alif)", "بَا   تَا   مَا   لاَ", "Baa — Taa — Maa — Laa", "Setiap Fathah diikuti Alif mati wajib dipanjangkan 2 harakat.", "", 2),
    ServerContent(926, "Iqro 2 • Hal 6 (Ayat Pendek vs Panjang)", "كَتَبَ   كَاتَبَ   صَدَقَ   صَادَقَ", "Ka-Ta-Ba  vs  Kaa-Ta-Ba", "Ujian Iqro 2: Jangan memanjangkan huruf yang tidak memiliki Alif!", "", 2),
    ServerContent(931, "Iqro 3 • Hal 1 (Baris Kasrah i-bi-ti)", "إِ   بِ   تِ   ثِ", "I — Bi — Ti — Tsi", "Garis di bawah huruf menghasilkan bunyi vokal 'i' pendek.", "", 3),
    ServerContent(932, "Iqro 3 • Hal 2 (Latihan Kata Kasrah)", "عَمِلَ   سَمِعَ   حَمِدَ", "'A-Mi-La — Sa-Mi-'A — Ha-Mi-Da", "Jaga agar huruf Kasrah tidak memantul atau miring ke bunyi 'e'.", "", 3),
    ServerContent(933, "Iqro 3 • Hal 3 (Mad Ya Sukun Panjang)", "بِي   تِي   فِيهِ   دِينِي", "Bii — Tii — Fii-Hi — Dii-Nii", "Kasrah diikuti Ya sukun dibaca panjang 2 harakat.", "", 3),
    ServerContent(934, "Iqro 3 • Hal 4 (Baris Dhammah u-bu-tu)", "أُ   بُ   تُ   ثُ", "U — Bu — Tu — Tsu", "Bibirmu maju membulat saat mengucapkan baris Dhammah (u).", "", 3),
    ServerContent(935, "Iqro 3 • Hal 5 (Kata Gabungan 3 Harakat)", "كُتِبَ   قُرِئَ   فُتِحَ", "Ku-Ti-Ba — Qu-Ri-A — Fu-Ti-Ha", "Gabungan Dhammah, Kasrah, dan Fathah dalam satu kata.", "", 3),
    ServerContent(936, "Iqro 3 • Hal 6 (Mad Wawu Sukun Panjang)", "بُو   تُو   يَقُولُ   رَسُولُ", "Buu — Tuu — Ya-Quu-Lu — Ro-Suu-Lu", "Dhammah diikuti Wawu sukun dibaca panjang 2 harakat.", "", 3),
    ServerContent(941, "Iqro 4 • Hal 1 (Fathatain -an)", "بً   تً   كِتَابًا   هُدًى", "Ban — Tan — Ki-Taa-Ban — Hu-Dan", "Baris dua di atas berbunyi '-an'.", "", 4),
    ServerContent(942, "Iqro 4 • Hal 2 (Kasratain -in)", "بٍ   تٍ   عَلِيمٍ   حَكِيمٍ", "Bin — Tin — 'A-Lii-Min — Ha-Kii-Min", "Baris dua di bawah berbunyi '-in'.", "", 4),
    ServerContent(943, "Iqro 4 • Hal 3 (Dhammatain -un)", "بٌ   تٌ   أَحَدٌ   صَمَدٌ", "Bun — Tun — A-Ha-Dun — Sho-Ma-Dun", "Dhammah rangkap di atas berbunyi '-un'.", "", 4),
    ServerContent(944, "Iqro 4 • Hal 4 (Huruf Sukun / Mati)", "أَمْ   أَنْ   يَعْلَمُونَ   مَسْجِدٌ", "Am — An — Ya'lamuuna — Masjidun", "Huruf bertanda Sukun dimatikan tanpa memantul (kecuali Qalqalah).", "", 4),
    ServerContent(945, "Iqro 4 • Hal 5 (Pantulan Qalqalah)", "أَبْ   أَجْ   أَدْ   أَقْ   أَطْ", "Ab' — Aj' — Ad' — Aq' — Ath'", "5 Huruf Qalqalah (ق ط ب ج د) wajib dipantulkan saat sukun.", "", 4),
    ServerContent(946, "Iqro 4 • Hal 6 (Ujian Evaluasi Jilid 4)", "أَنْعَمْتَ   الْمَغْضُوبِ   يَكْسِبُونَ", "An-'Am-Ta — Al-Magh-Dhuu-Bi", "Pastikan Sukun dan Qalqalah terdengar beda dengan jelas!", "", 4),
    ServerContent(951, "Iqro 5 • Hal 1 (Alif Lam Qamariyah)", "الْقَمَرُ   الْحَمْدُ   الْعَالَمِينَ", "Al-Qamaru — Al-Hamdu — Al-'Aalamiina", "Huruf Lam sukun dibaca jelas.", "", 5),
    ServerContent(952, "Iqro 5 • Hal 2 (Alif Lam Syamsiyah)", "الشَّمْسُ   الرَّحْمَنُ   النَّاسِ", "Asy-Syamsu — Ar-Rahmaanu — An-Naasi", "Huruf Lam lebur langsung ke huruf bertasydid di depannya.", "", 5),
    ServerContent(953, "Iqro 5 • Hal 3 (Huruf Bertasydid)", "رَبِّ   قُدِّرَ   سُبِّحَ   إِيَّاكَ", "Rab-bi — Qud-dira — Sub-biha — Iy-yaaka", "Tahan dan tekan kuat huruf yang memiliki tanda Tasydid (ّ).", "", 5),
    ServerContent(954, "Iqro 5 • Hal 4 (Ghunnah Nun & Mim Tasydid)", "إِنَّ   عَمَّ   جَنَّةٌ   ثُمَّ", "In-na — 'Am-ma — Jan-natun — Tsum-ma", "Wajib ditahan dengungnya selama 2 harakat penuh!", "", 5),
    ServerContent(955, "Iqro 5 • Hal 5 (Lafadz Allah / Jalalah)", "بِسْمِ اللَّهِ   مِنَ اللَّهِ   رَسُولُ اللَّهِ", "Bismillaahi (Tipis) vs Minallaahi (Tebal)", "Dibaca tipis (Lillaah) setelah Kasrah, dan tebal (Allaah) setelah Fathah/Dhammah.", "https://everyayah.com/data/Alafasy_128kbps/001001.mp3", 5),
    ServerContent(956, "Iqro 5 • Hal 6 (Cara Waqaf / Berhenti)", "تَعْلَمُونَ ۝   أَحَدٌ ۝   الْفَلَقِ ۝", "Ta'lamuun — Ahad' — Al-Falaq'", "Huruf terakhir dimatikan ketika berhenti di akhir ayat.", "", 5),
    ServerContent(961, "Iqro 6 • Hal 1 (Idgham Bighunnah)", "مَنْ يَقُولُ   مِنْ مَالٍ   مِنْ نِعْمَةٍ", "May-yaquulu — Mim-maalin — Min-ni'matin", "Nun mati/Tanwin bertemu ي ن م و dileburkan disertai dengung.", "", 6),
    ServerContent(962, "Iqro 6 • Hal 2 (Idgham Bilaghunnah)", "مِنْ رَبِّهِمْ   غَفُورٌ رَحِيمٌ   مِنْ لَدُنْهُ", "Mir-rabbihim — Ghafuurur-rahiim", "Nun mati/Tanwin bertemu Lam (ل) atau Ro (ر) melebur jelas tanpa dengung.", "", 6),
    ServerContent(963, "Iqro 6 • Hal 3 (Hukum Iqlab)", "مِنْ بَعْدِ   سَمِيعٌ بَصِيرٌ   لَيُنْبَذَنَّ", "Mim-ba'di — Samii'um-bashiir", "Nun mati/Tanwin bertemu Ba (ب) berubah menjadi bunyi Mim dengung.", "", 6),
    ServerContent(964, "Iqro 6 • Hal 4 (Hukum Ikhfa Haqiqi)", "مِنْ شَرِّ   أَنْفُسِهِمْ   مَنْصُورًا", "Min-syarri — An-fusihim — Man-shuuran", "Nun mati/Tanwin bertemu 15 huruf Ikhfa dibaca samar disertai dengung.", "https://everyayah.com/data/Alafasy_128kbps/113002.mp3", 6),
    ServerContent(965, "Iqro 6 • Hal 5 (Huruf Muqaththa'ah Awal Surah)", "الم   يس   طه   حم", "Alif-Laam-Miim — Yaa-Siin — Thoo-Haa", "Dibaca nama asli hurufnya dengan panjang Mad Lazim (6 harakat).", "https://everyayah.com/data/Alafasy_128kbps/002001.mp3", 6),
    ServerContent(966, "Iqro 6 • Hal 6 (Kelulusan Iqro 6)", "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝", "Qul huwallaahu ahad' — Allaahush-shomad'", "Alhamdulillah! Anda telah menamatkan seluruh kurikulum Iqro 1–6.", "https://everyayah.com/data/Alafasy_128kbps/112001.mp3", 6)
)

fun getBuiltInBelajarCurriculum(): List<ServerContent> = listOf(
    // 1. KATEGORI ADAB, AKHLAK & BENTENG ANTI-ZINA / PERGAULAN BEBAS
    ServerContent(
        140,
        "🛡️ [Adab] 1. Larangan Keras Mendekati Zina & Pergaulan Bebas (Pacaran)",
        "وَلَا تَقْرَبُوا الزِّنَا ۖ إِنَّهُ كَانَ فَاحِشَةً وَسَاءَ سَبِيلًا",
        "Wa laa taqrabuz-zinaa, innahuu kaana faahisyatan wa saa'a sabiilaa (QS. Al-Isra: 32)",
        "⚠️ Artinya: \"Dan janganlah kamu mendekati zina; sesungguhnya zina itu adalah suatu perbuatan yang keji dan suatu jalan yang buruk.\" (QS. Al-Isra: 32)\n\n🛡️ Mengapa Allah berfirman 'Jangan Mendekati Zina'?\nKarena zina selalu diawali dari hal-hal kecil yang dianggap sepele oleh anak zaman sekarang, seperti:\n• Pacaran, berduaan di tempat sepi (Khalwat), atau boncengan bukan mahram\n• Chatting mesra atau mengirim foto yang membuka aurat\n• Bersentuhan fisik antara laki-laki dan perempuan yang bukan mahram\n\n🌟 Nasihat Emas:\nAnak dan remaja muslim yang mulia menjaga kehormatan dirinya sampai tiba waktunya menikah secara halal. Menjaga kesucian diri akan membuat hidupmu diberkahi Allah dan membanggakan orang tua di dunia & akhirat!",
        "https://everyayah.com/data/Alafasy_128kbps/017032.mp3"
    ),
    ServerContent(
        141,
        "🛡️ [Adab] 2. Menjaga Pandangan Mata & Kehormatan Diri (Ghadhul Bashar)",
        "قُلْ لِلْمُؤْمِنِينَ يَغُضُّوا مِنْ أَبْصَارِهِمْ وَيَحْفَظُوا فُرُوجَهُمْ ۚ ذَٰلِكَ أَزْكَىٰ لَهُمْ",
        "Qul lil-mu'miniina yaghudhuu min abshaarihim wa yahfazhuu furuujahum, dzaalika azkaa lahum (QS. An-Nur: 30)",
        "📌 Artinya: \"Katakanlah kepada orang laki-laki yang beriman: Hendaklah mereka menahan pandangannya, dan memelihara kemaluannya; yang demikian itu adalah lebih suci bagi mereka.\" (QS. An-Nur: 30).\n\n🛡️ Cara Menjaga Pandangan di Era Digital:\n• Tidak menonton video atau gambar yang tidak senonoh / pornografi ketika sedang sendirian di kamar\n• Ingatlah bahwa Allah Maha Melihat (Al-Bashir) meskipun pintu kamar terkunci rapat\n• Segera palingkan pandangan dan ucapkan 'Astaghfirullah' jika tidak sengaja melihat aurat di layar HP.",
        "https://everyayah.com/data/Alafasy_128kbps/024030.mp3"
    ),
    ServerContent(
        142,
        "🛡️ [Adab] 3. Batasan Pergaulan Laki-Laki & Perempuan (Bukan Mahram)",
        "لَا يَخْلُوَنَّ رَجُلٌ بِامْرَأَةٍ إِلَّا كَانَ ثَالِثَهُمَا الشَّيْطَانُ",
        "Laa yakhluwanna rajulun bimra'atin illaa kaana tsaalitsahumasy-syaithaan",
        "📌 Artinya: \"Janganlah sekali-kali seorang laki-laki berduaan dengan seorang perempuan (yang bukan mahramnya), karena yang ketiga di antara mereka berdua adalah setan.\" (HR. Tirmidzi).\n\n🛡️ Aturan Pergaulan Anak & Remaja Sholeh:\n1. Tidak berduaan (Khalwat) baik di ruangan tertutup, di kendaraan, maupun lewat video call pribadi larut malam\n2. Tidak bersentuhan fisik dengan lawan jenis yang bukan mahram\n3. Berbicara seperlunya dengan sopan, tegas, dan tidak mendayu-dayu\n4. Menjaga rasa malu (Al-Haya'), karena rasa malu adalah cabang dari keimanan.",
        ""
    ),
    ServerContent(
        143,
        "🛡️ [Adab] 4. Kewajiban Menutup Aurat & Menjaga Rasa Malu (Al-Haya')",
        "الْحَيَاءُ مِنَ الْإِيمَانِ",
        "Al-Hayaa'u minal iimaan (Rasa malu itu adalah bagian dari iman — HR. Bukhari & Muslim)",
        "📌 Mengapa Kita Wajib Menutup Aurat?\nMenutup aurat bukan sekadar aturan pakaian, melainkan perisai kemuliaan agar seorang muslim & muslimah dihormati dan terlindung dari gangguan.\n\n• Batas Aurat Laki-Laki: Dari pusar hingga menutupi lutut (serta berpakaian sopan dan rapi).\n• Batas Aurat Perempuan: Seluruh tubuh kecuali wajah dan telapak tangan (mengenakan jilbab/hijab yang longgar, tidak transparan, dan tidak membentuk lekuk tubuh).\n• Jangan mengunggah foto/video yang memperlihatkan aurat ke media sosial karena akan menjadi dosa jariyah yang terus mengalir.",
        ""
    ),
    ServerContent(
        144,
        "🛡️ [Adab] 5. Berbakti kepada Ayah Bunda & Bahaya Durhaka (Jangan Berkata 'Ah'!)",
        "فَلَا تَقُلْ لَهُمَا أُفٍّ وَلَا تَنْهَرْهُمَا وَقُلْ لَهُمَا قَوْلًا كَرِيمًا",
        "Falaa taqul lahumaa uffin wa laa tanharhumaa wa qul lahumaa qaulan kariimaa (QS. Al-Isra: 23)",
        "⚠️ Artinya: \"Maka sekali-kali janganlah engkau mengatakan kepada keduanya perkataan 'ah' dan janganlah engkau membentak keduanya, dan ucapkanlah kepada keduanya perkataan yang mulia.\" (QS. Al-Isra: 23)\n\n🛡️ Adab Anak Sholeh kepada Orang Tua:\n1. Dilarang keras membentak, memelototi, atau meninggikan suara di hadapan Ayah dan Bunda\n2. Ketika dipanggil saat sedang bermain HP/game, segera letakkan HP dan datangi orang tua dengan wajah senyum\n3. Mencium tangan dan meminta doa restu sebelum berangkat sekolah\n4. Mendoakan ampunan bagi keduanya setiap selesai sholat 5 waktu.",
        "https://everyayah.com/data/Alafasy_128kbps/017023.mp3"
    ),
    ServerContent(
        145,
        "🛡️ [Adab] 6. Larangan Bullying, Mengejek Teman & Memanggil dengan Julukan Buruk",
        "لَا يَسْخَرْ قَوْمٌ مِنْ قَوْمٍ عَسَىٰ أَنْ يَكُونُوا خَيْرًا مِنْهُمْ",
        "Laa yaskhar qaumun min qaumin 'asaa ay-yakuunuu khairan minhum (QS. Al-Hujurat: 11)",
        "⚠️ Artinya: \"Janganlah suatu kaum mengolok-olok kaum yang lain, karena boleh jadi mereka yang diolok-olok itu lebih baik di sisi Allah daripada mereka yang mengolok-olok.\" (QS. Al-Hujurat: 11)\n\n🛡️ Adab Berteman di Sekolah & Lingkungan:\n1. Dilarang mem-bully, merendahkan fisik teman, atau memanggil teman dengan nama panggilan yang buruk\n2. Dilarang mengejek nama orang tua teman\n3. Bela dan tolonglah teman yang sedang dijahili atau dikucilkan\n4. Tebarkan salam, senyum, dan sapaan yang ramah kepada sesama muslim.",
        "https://everyayah.com/data/Alafasy_128kbps/049011.mp3"
    ),
    ServerContent(
        146,
        "🛡️ [Adab] 7. Adab Bermedia Sosial, Bermain HP & Menjaga Lisan/Jari",
        "مَا يَلْفِظُ مِنْ قَوْلٍ إِلَّا لَدَيْهِ رَقِيبٌ عَتِيدٌ",
        "Maa yalfizhu min qaulin illaa ladaihi Raqiibun 'Atiid (QS. Qaf: 18)",
        "📌 Artinya: \"Tiada suatu ucapan pun yang diucapkannya melainkan ada di dekatnya Malaikat Pengawas yang selalu hadir (Raqib & Atid).\" (QS. Qaf: 18)\n\n🛡️ 5 Aturan Emas Anak Muslim Saat Memegang HP:\n1. Apa yang diketik oleh jari di kolom komentar sama hukumnya dengan ucapan lisan yang dicatat Malaikat\n2. Jangan pernah menulis komentar kasar, kata-kata kotor/jorok, atau menyebarkan fitnah/gosip (Ghibah)\n3. Segera hentikan bermain HP ketika terdengar kumandang Adzan\n4. Gunakan HP untuk menambah ilmu, menghafal Al-Quran, dan menyebarkan kebaikan.",
        "https://everyayah.com/data/Alafasy_128kbps/050018.mp3"
    ),
    ServerContent(
        147,
        "🛡️ [Adab] 8. Adab Menghormati Guru & Orang yang Lebih Tua",
        "لَيْسَ مِنَّا مَنْ لَمْ يَرْحَمْ صَغِيرَنَا وَيُوَقِّرْ كَبِيرَنَا",
        "Laisa minnaa man lam yarham shaghiiranaa wa yuwaqqir kabiiranaa",
        "📌 Artinya: \"Bukan termasuk golongan kami (umat Nabi SAW), orang yang tidak menyayangi yang lebih muda dan tidak menghormati yang lebih tua.\" (HR. Tirmidzi).\n\n🛡️ Cara Mengamalkan:\n• Mendahulukan adab dan kesopanan sebelum mempelajari ilmu\n• Mengucapkan salam, menundukkan badan sedikit saat berjalan melewati orang tua/guru, dan berbicara dengan bahasa yang santun\n• Mendengarkan penjelasan guru di kelas dengan tenang dan tidak membuat gaduh.",
        ""
    ),
    ServerContent(
        148,
        "🛡️ [Adab] 9. Memilih Teman Pergaulan yang Sholeh (Penjual Minyak Wangi)",
        "مَثَلُ الْجَلِيسِ الصَّالِحِ وَالسَّوْءِ كَحَامِلِ الْمِسْكِ وَنَافِخِ الْكِيرِ",
        "Matsalul jaliisish-shaalihi was-sau'i kahaamilil miski wa naafikhil kiir (HR. Bukhari & Muslim)",
        "📌 Artinya: \"Permisalan teman duduk yang sholeh dan teman yang buruk adalah seperti penjual minyak wangi dan pandai besi.\" (HR. Bukhari & Muslim).\n\n🌟 Pelajaran Penting:\n• Berteman dengan anak yang rajin sholat, jujur, dan sopan akan membuat kita ikut tertular wangi kebaikannya\n• Sebaliknya, bergaul bebas dengan lingkungan yang suka berkata kasar, meninggalkan sholat, atau merokok/maksiat akan menyeret kita ke dalam keburukan. Pilihlah sahabat yang mengajakmu ke Surga!",
        ""
    ),
    ServerContent(
        149,
        "🛡️ [Adab] 10. Adab Makan, Minum & Berpakaian Sesuai Sunnah",
        "يَا غُلَامُ سَمِّ اللَّهَ ، وَكُلْ بِيَمِينِكَ ، وَكُلْ مِمَّا يَلِيكَ",
        "Yaa ghulaamu sammillaah, wa kul biyamiinika, wa kul mimmaa yaliik",
        "📌 Artinya: \"Wahai anak kecil, bacalah Bismillah, makanlah dengan tangan kananmu, dan makanlah makanan yang terdekat darimu.\" (HR. Bukhari & Muslim).\n\n1. Selalu makan dan minum menggunakan TANGAN KANAN sambil DUDUK\n2. Tidak mencela makanan yang telah dimasak oleh Ibu\n3. Ketika memakai baju atau sandal, dahulukan anggota tubuh sebelah KANAN, dan ketika melepas dahulukan sebelah KIRI.",
        ""
    ),

    // 2. KATEGORI PANDUAN & RUKUN SHOLAT 5 WAKTU
    ServerContent(
        101,
        "🕌 [Sholat] Mengenal 5 Waktu Sholat Wajib & Jumlah Rakaatnya",
        "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَوْقُوتًا",
        "Innash-shalaata kaanat 'alal mu'miniina kitaabam mauquutaa",
        "Umat Islam wajib mendirikan sholat 5 waktu sehari semalam dengan total 17 rakaat:\n\n1. Sholat Subuh: 2 Rakaat (Saat fajar menyingsing)\n2. Sholat Dzuhur: 4 Rakaat (Saat matahari tergelincir di siang hari)\n3. Sholat Ashar: 4 Rakaat (Sore hari)\n4. Sholat Maghrib: 3 Rakaat (Saat matahari terbenam)\n5. Sholat Isya: 4 Rakaat (Malam hari)\n\nSholat adalah tiang agama dan amalan pertama yang akan dihisab pada hari kiamat.",
        ""
    ),
    ServerContent(
        102,
        "🕌 [Sholat] 13 Rukun Sholat yang Wajib Dilakukan Berurutan",
        "صَلُّوا كَمَا رَأَيْتُمُونِي أُصَلِّي",
        "Shalluu kamaa ra'aitumuunii ushallii (Sholatlah sebagaimana kalian melihatku sholat)",
        "Urutan Rukun Sholat:\n1. Niat di dalam hati\n2. Berdiri bagi yang mampu\n3. Membaca Takbiratul Ihram (Allahu Akbar)\n4. Membaca Surah Al-Fatihah di setiap rakaat\n5. Ruku' dengan thuma'ninah (tenang sejenak)\n6. I'tidal (bangkit dari ruku') dengan thuma'ninah\n7. Sujud dua kali dengan thuma'ninah\n8. Duduk di antara dua sujud dengan thuma'ninah\n9. Duduk Tasyahud Akhir\n10. Membaca Tasyahud Akhir\n11. Membaca Sholawat Nabi pada Tasyahud Akhir\n12. Mengucapkan Salam pertama (menoleh ke kanan)\n13. Tertib (berurutan).",
        ""
    ),
    ServerContent(
        103,
        "🕌 [Sholat] Hal-Hal yang Membatalkan Sholat",
        "",
        "Perkara yang wajib dihindari ketika sedang sholat",
        "Sholat menjadi batal apabila:\n1. Berhadats kecil atau besar (buang angin/kentut, buang air) sebelum salam\n2. Terkena najis pada badan, pakaian, atau tempat sholat\n3. Terbuka aurat dengan sengaja\n4. Berbicara atau mengeluarkan kata-kata selain bacaan sholat\n5. Makan atau minum\n6. Bergerak besar berturut-turut 3 kali di luar gerakan sholat\n7. Tertawa terbahak-bahak.",
        ""
    ),

    // 3. KATEGORI BACAAN SHOLAT LENGKAP
    ServerContent(
        111,
        "🤲 [Bacaan Sholat] 1. Lafadz Niat Sholat 5 Waktu",
        "أُصَلِّي فَرْضَ الصُّبْحِ رَكْعَتَيْنِ مُسْتَقْبِلَ الْقِبْلَةِ أَدَاءً لِلَّهِ تَعَالَى",
        "Ushallii fardhash-Shubhi rak'ataini mustaqbilal qiblati adaa'an lillaahi Ta'aalaa",
        "Artinya: \"Aku berniat sholat fardhu Subuh dua rakaat menghadap kiblat karena Allah Ta'ala.\"\n\n• Untuk Dzuhur (4 Rakaat): Ushallii fardhazh-Zhuhri arba'a raka'aatin...\n• Untuk Ashar (4 Rakaat): Ushallii fardhal 'Ashri arba'a raka'aatin...\n• Untuk Maghrib (3 Rakaat): Ushallii fardhal Maghribi tsalaatsa raka'aatin...\n• Untuk Isya (4 Rakaat): Ushallii fardhal 'Isyaa'i arba'a raka'aatin...",
        ""
    ),
    ServerContent(
        112,
        "🤲 [Bacaan Sholat] 2. Takbiratul Ihram & Doa Iftitah",
        "اللَّهُ أَكْبَرُ كَبِيرًا ، وَالْحَمْدُ لِلَّهِ كَثِيرًا ، وَسُبْحَانَ اللَّهِ بُكْرَةً وَأَصِيلًا",
        "Allaahu Akbaru kabiiraa, walhamdulillaahi katsiiraa, wa subhaanallaahi bukratan wa ashiilaa...",
        "Lanjutan Doa Iftitah:\n\"Innii wajjahtu wajhiya lilladzii fatharas-samaawaati wal ardha haniifan musliman wa maa ana minal musyrikiin. Inna shalaatii wa nusukii wa mahyaaya wa mamaatii lillaahi Rabbil 'aalamiin.\"\n\nArtinya: \"Allah Maha Besar dengan sebesar-besarnya, segala puji bagi Allah sebanyak-banyaknya, dan Maha Suci Allah pada waktu pagi dan petang. Sesungguhnya sholatku, ibadahku, hidupku, dan matiku hanyalah untuk Allah Tuhan semesta alam.\"",
        ""
    ),
    ServerContent(
        113,
        "🤲 [Bacaan Sholat] 3. Bacaan Ketika Ruku' (3 Kali)",
        "سُبْحَانَ رَبِّيَ الْعَظِيمِ وَبِحَمْدِهِ",
        "Subhaana Rabbiyal 'Azhiimi wa bihamdih (Dibaca 3x)",
        "Artinya: \"Maha Suci Tuhanku Yang Maha Agung dan dengan memuji-Nya.\"",
        ""
    ),
    ServerContent(
        114,
        "🤲 [Bacaan Sholat] 4. Bacaan Bangkit Ruku' (I'tidal)",
        "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ  —  رَبَّنَا وَلَكَ الْحَمْدُ مِلْءَ السَّمَاوَاتِ وَمِلْءَ الْأَرْضِ",
        "Sami'allaahu liman hamidah — Rabbanaa wa lakal hamdu mil'as-samaawaati wa mil'al ardh",
        "Artinya:\n\"Allah mendengar orang yang memuji-Nya. Ya Tuhan kami, bagi-Mu segala puji sepenuh langit dan sepenuh bumi serta sepenuh apa saja yang Engkau kehendaki setelah itu.\"",
        ""
    ),
    ServerContent(
        115,
        "🤲 [Bacaan Sholat] 5. Bacaan Ketika Sujud (3 Kali)",
        "سُبْحَانَ رَبِّيَ الْأَعْلَىٰ وَبِحَمْدِهِ",
        "Subhaana Rabbiyal A'laa wa bihamdih (Dibaca 3x)",
        "Artinya: \"Maha Suci Tuhanku Yang Maha Tinggi dan dengan memuji-Nya.\"\n\n💡 Saat sujud adalah posisi seorang hamba paling dekat dengan Allah, maka perbanyaklah doa di dalamnya!",
        ""
    ),
    ServerContent(
        116,
        "🤲 [Bacaan Sholat] 6. Doa Duduk di Antara Dua Sujud",
        "رَبِّ اغْفِرْ لِي وَارْحَمْنِي وَاجْبُرْنِي وَارْفَعْنِي وَارْزُقْنِي وَاهْدِنِي وَعَافِنِي وَاعْفُ عَنِّي",
        "Rabbighfir lii, warhamnii, wajburnii, warfa'nii, warzuqnii, wahdinii, wa 'aafinii, wa'fu 'annii",
        "Artinya: \"Ya Tuhanku, ampunilah aku, sayangilah aku, cukupkanlah kekuranganku, angkatlah derajatku, berilah aku rezeki, berilah aku petunjuk, berilah aku kesehatan, dan maafkanlah kesalahanku.\"",
        ""
    ),
    ServerContent(
        117,
        "🤲 [Bacaan Sholat] 7. Bacaan Tasyahud / Tahiyat Awal & Akhir",
        "التَّحِيَّاتُ الْمُبَارَكَاتُ الصَّلَوَاتُ الطَّيِّبَاتُ لِلَّهِ ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ",
        "At-tahiyyaatul mubaarakaatush-shalawaatuth-thayyibaatu lillaah, as-salaamu 'alaika ayyuhan-Nabiyyu wa rahmatullaahi wa barakaatuh...",
        "Lanjutan Tahiyat & Syahadat:\n\"As-salaamu 'alainaa wa 'alaa 'ibaadillaahish-shaalihiin. Asyhadu allaa ilaaha illallaah, wa asyhadu anna Muhammadar Rasuulullaah. Allaahumma shalli 'alaa Sayyidinaa Muhammad wa 'alaa aali Sayyidinaa Muhammad.\"\n\nArtinya: \"Segala kehormatan, keberkahan, kebahagiaan dan kebaikan adalah milik Allah. Semoga keselamatan, rahmat Allah dan berkah-Nya tetap tercurah atasmu wahai Nabi...\"",
        ""
    ),
    ServerContent(
        118,
        "🤲 [Bacaan Sholat] 8. Salam Penutup Sholat (Menoleh Kanan & Kiri)",
        "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
        "As-salaamu 'alaikum wa rahmatullaah",
        "Artinya: \"Semoga keselamatan dan rahmat Allah tercurah kepada kalian.\"\n\nMenoleh ke kanan sambil mengucapkan salam pertama, lalu menoleh ke kiri sambil mengucapkan salam kedua.",
        ""
    ),

    // 4. KATEGORI PANDUAN WUDHU & THAHARAH
    ServerContent(
        121,
        "💧 [Wudhu] Urutan Tata Cara Wudhu Sesuai Sunnah Nabi SAW",
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        "Bismillaahir-rahmaanir-rahiim",
        "Urutan Wudhu yang benar:\n1. Berniat di dalam hati & mengucapkan 'Bismillah'\n2. Mencuci kedua telapak tangan hingga pergelangan (3 kali)\n3. Berkumur-kumur & menghirup air ke hidung lalu mengeluarkannya (3 kali)\n4. Membasuh seluruh permukaan wajah dari batas tumbuh rambut hingga dagu (3 kali)\n5. Membasuh kedua tangan dari ujung jari hingga melewati siku, dahulukan tangan kanan (3 kali)\n6. Mengusap sebagian atau seluruh kepala beserta kedua telinga (1 kali)\n7. Mencuci kedua kaki hingga melewati mata kaki, dahulukan kaki kanan (3 kali)\n8. Tertib (berurutan).",
        "https://everyayah.com/data/Alafasy_128kbps/001001.mp3"
    ),
    ServerContent(
        122,
        "💧 [Wudhu] Doa Sesudah Wudhu (Membuka 8 Pintu Surga)",
        "أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ ، اللَّهُمَّ اجْعَلْنِي مِنَ التَّوَّابِينَ وَاجْعَلْنِي مِنَ الْمُتَطَهِّرِينَ",
        "Asyhadu allaa ilaaha illallaahu wahdahuu laa syariika lah, wa asyhadu anna Muhammadan 'abduhuu wa Rasuuluh. Allaahummaj'alnii minat-tawwaabiina waj'alnii minal mutathahhiriin",
        "Artinya: \"Aku bersaksi bahwa tiada Tuhan selain Allah Yang Maha Esa, tiada sekutu bagi-Nya, dan aku bersaksi bahwa Nabi Muhammad adalah hamba dan utusan-Nya. Ya Allah, jadikanlah aku termasuk golongan orang-orang yang bertaubat dan jadikanlah aku termasuk golongan orang-orang yang menjaga kesucian.\"\n\n🌟 Keutamaan: Barangsiapa membacanya selesai wudhu, dibukakan untuknya 8 pintu Surga! (HR. Muslim & Tirmidzi)",
        ""
    ),

    // 5. KATEGORI AQIDAH
    ServerContent(
        131,
        "☝️ [Aqidah] 5 Rukun Islam & Penjelasannya",
        "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ",
        "Buniyal islaamu 'alaa khamsin",
        "Lima Pondasi Rukun Islam:\n1. Mengucapkan Dua Kalimat Syahadat\n2. Mendirikan Sholat 5 Waktu sehari semalam\n3. Menunaikan Zakat bagi yang telah mencapai nishab\n4. Berpuasa di Bulan Suci Ramadhan\n5. Menunaikan Ibadah Haji ke Baitullah bagi yang mampu.",
        ""
    ),
    ServerContent(
        132,
        "☝️ [Aqidah] 6 Rukun Iman yang Wajib Diyakini",
        "أَنْ تُؤْمِنَ بِاللَّهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ وَالْيَوْمِ الْآخِرِ وَتُؤْمِنَ بِالْقَدَرِ خَيْرِهِ وَشَرِّهِ",
        "An tu'mina billaahi wa malaa'ikatihi wa kutubihi wa rusulihi...",
        "Enam Pilar Keimanan Seorang Muslim:\n1. Iman kepada Allah SWT\n2. Iman kepada Malaikat-Malaikat Allah (10 Malaikat wajib diketahui)\n3. Iman kepada Kitab-Kitab Suci Allah (Taurat, Zabur, Injil, Al-Quran)\n4. Iman kepada Rasul-Rasul Allah (25 Nabi & Rasul)\n5. Iman kepada Hari Kiamat (Hari Akhir)\n6. Iman kepada Qada dan Qadar (Takdir baik dan buruk dari Allah).",
        ""
    )
)

fun getBuiltInTajwidCurriculum(): List<ServerContent> = listOf(
    ServerContent(
        301,
        "1. Izhar Halqi (Nun Mati / Tanwin Dibaca Jelas)",
        "مَنْ آمَنَ  —  عَذَابٌ أَلِيمٌ  —  مِنْ خَوْفٍ",
        "Man aamana — 'Adzaabun aliim — Min khaufin",
        "📌 Pengertian:\nApabila Nun Sukun (نْ) atau Tanwin (ـً ـٍ ـٌ) bertemu dengan salah satu dari 6 Huruf Tenggorokan (Halqi): ء هـ ع ح غ خ.\n\n🔊 Cara Membaca:\nHarus dibaca JELAS dan terang bunyi 'N'-nya, tanpa mendengung.\n\n📖 Contoh Lengkap:\n• مَنْ آمَنَ (Nun mati bertemu Hamzah)\n• مِنْ هَادٍ (Nun mati bertemu Ha)\n• سَمِيعٌ عَلِيمٌ (Tanwin bertemu 'Ain)\n• عَلِيمٌ حَكِيمٌ (Tanwin bertemu Ha')\n• قَوْمًا غَيْرَكُمْ (Tanwin bertemu Ghain)\n• مِنْ خَوْفٍ (Nun mati bertemu Kha)",
        "https://everyayah.com/data/Alafasy_128kbps/106004.mp3"
    ),
    ServerContent(
        302,
        "2. Idgham Bighunnah (Melebur Disertai Dengung)",
        "مَنْ يَقُولُ  —  مِنْ نِعْمَةٍ  —  مِنْ مَالٍ  —  مِنْ وَرَائِهِمْ",
        "May-yaquulu — Min-ni'matin — Mim-maalin — Miw-waraa'ihim",
        "📌 Pengertian:\nApabila Nun Sukun (نْ) atau Tanwin bertemu dengan salah satu dari 4 huruf YANMU (ي ن م و) dalam dua kata yang terpisah.\n\n🔊 Cara Membaca:\nSuara Nun/Tanwin dileburkan masuk ke huruf berikutnya disertai DENGUNG di hidung selama 2–3 harakat.\n\n📖 Contoh Lengkap:\n• فَمَنْ يَعْمَلْ (Bertemu Ya ➔ Famay-ya'mal)\n• مِنْ نِعْمَةٍ (Bertemu Nun ➔ Min-ni'matin)\n• فِي عَمَدٍ مُمَدَّدَةٍ (Bertemu Mim ➔ 'Amadim-mumaddadah)\n• مِنْ وَالٍ (Bertemu Wawu ➔ Miw-waalin)",
        "https://everyayah.com/data/Alafasy_128kbps/099007.mp3"
    ),
    ServerContent(
        303,
        "3. Idgham Bilaghunnah (Melebur Tanpa Dengung)",
        "مِنْ رَبِّهِمْ  —  غَفُورٌ رَحِيمٌ  —  هُدًى لِلْمُتَّقِينَ",
        "Mir-rabbihim — Ghafuurur-rahiim — Hudal-lilmuttaqiin",
        "📌 Pengertian:\nApabila Nun Sukun (نْ) atau Tanwin bertemu dengan huruf Lam (ل) atau Ro (ر).\n\n🔊 Cara Membaca:\nSuara Nun/Tanwin langsung melebur jelas ke huruf Lam atau Ro TANPA berdengung.\n\n📖 Contoh Lengkap:\n• مِنْ رَبِّهِمْ (Dibaca: Mir-rabbihim)\n• غَفُورٌ رَحِيمٌ (Dibaca: Ghafuurur-rahiim)\n• هُدًى لِلْمُتَّقِينَ (Dibaca: Hudal-lilmuttaqiin)\n• وَيْلٌ لِكُلِّ هُمَزَةٍ (Dibaca: Wailul-likulli)",
        "https://everyayah.com/data/Alafasy_128kbps/104001.mp3"
    ),
    ServerContent(
        304,
        "4. Iqlab (Membalik Suara Nun Menjadi Mim)",
        "مِنْ بَعْدِ  —  سَمِيعٌ بَصِيرٌ  —  لَيُنْبَذَنَّ",
        "Mim-ba'di — Samii'um-bashiir — Layumbadzanna",
        "📌 Pengertian:\nApabila Nun Sukun (نْ) atau Tanwin bertemu dengan huruf Ba (ب). Biasanya ditandai dengan huruf Mim kecil (م) di atasnya.\n\n🔊 Cara Membaca:\nBunyi 'N' dibalik menjadi bunyi 'M' (Mim) dengan merapatkan bibir secara lembut disertai dengung 2 harakat.\n\n📖 Contoh Lengkap:\n• مِنْ بَعْدِ (Dibaca: Mim-ba'di)\n• سَمِيعٌ بَصِيرٌ (Dibaca: Samii'um-bashiir)\n• كَلَّا لَيُنْبَذَنَّ (Dibaca: Kallaa layumbadzanna)",
        "https://everyayah.com/data/Alafasy_128kbps/104004.mp3"
    ),
    ServerContent(
        305,
        "5. Ikhfa Haqiqi (Samar-Samar Disertai Dengung)",
        "مِنْ شَرِّ  —  أَنْفُسِهِمْ  —  مَنْصُورًا  —  مِنْكُمْ",
        "Min-syarri — An-fusihim — Man-shuuran — Ming-kum",
        "📌 Pengertian:\nApabila Nun Sukun (نْ) atau Tanwin bertemu salah satu dari 15 huruf Ikhfa: ت ث ج د ذ ز س ش ص ض ط ظ ف ق ك.\n\n🔊 Cara Membaca:\nSuara Nun/Tanwin dibaca SAMAR (antara jelas dan melebur) sambil bersiap mengucapkan huruf di depannya, ditahan dengung 2 harakat.\n\n📖 Contoh Lengkap:\n• مِنْ شَرِّ مَا خَلَقَ (Bertemu Syin)\n• مِنْ تَحْتِهَا (Bertemu Ta)\n• إِنْ كُنْتُمْ (Bertemu Kaf & Ta)\n• صَعِيدًا طَيِّبًا (Bertemu Tho)",
        "https://everyayah.com/data/Alafasy_128kbps/113002.mp3"
    ),
    ServerContent(
        306,
        "6. Ghunnah Musyaddadah (Nun & Mim Bertasydid)",
        "إِنَّ  —  عَمَّ يَتَسَاءَلُونَ  —  مِنَ الْجِنَّةِ وَالنَّاسِ",
        "In-na — 'Am-ma yatasaa'aluun — Minal jin-nati wan-naas",
        "📌 Pengertian:\nSetiap huruf Nun yang bertasydid (نّ) dan Mim yang bertasydid (مّ).\n\n🔊 Cara Membaca:\nWAJIB ditahan dan didengungkan dengan kuat ke pangkal hidung selama 2–3 harakat.\n\n📖 Contoh Lengkap:\n• إِنَّا أَعْطَيْنَاكَ (In-naa)\n• عَمَّ يَتَسَاءَلُونَ ('Am-ma)\n• ثُمَّ كَلَّا سَوْفَ تَعْلَمُونَ (Tsum-ma)\n• مَلِكِ النَّاسِ (An-naas)",
        "https://everyayah.com/data/Alafasy_128kbps/108001.mp3"
    ),
    ServerContent(
        307,
        "7. Ikhfa Syafawi (Mim Mati Bertemu Ba)",
        "تَرْمِيهِمْ بِحِجَارَةٍ  —  وَمَا هُمْ بِمُؤْمِنِينَ",
        "Tarmiihim-bihijaarah — Wa maa hum-bimu'miniin",
        "📌 Pengertian:\nApabila Mim Sukun (مْ) bertemu dengan huruf Ba (ب).\n\n🔊 Cara Membaca:\nDibaca samar di bibir disertai dengung selama 2 harakat.",
        "https://everyayah.com/data/Alafasy_128kbps/105004.mp3"
    ),
    ServerContent(
        308,
        "8. Idgham Mimi / Mutamatsilain (Mim Mati Bertemu Mim)",
        "أَطْعَمَهُمْ مِنْ جُوعٍ  —  لَهُمْ مَا يَشَاءُونَ",
        "Ath'amahum-min juu' — Lahum-maa yasyaa'uun",
        "📌 Pengertian:\nApabila Mim Sukun (مْ) bertemu dengan huruf Mim berharakat (م).\n\n🔊 Cara Membaca:\nDileburkan menjadi satu Mim bertasydid disertai dengung 2 harakat.",
        "https://everyayah.com/data/Alafasy_128kbps/106004.mp3"
    ),
    ServerContent(
        309,
        "9. Izhar Syafawi (Mim Mati Dibaca Jelas)",
        "أَلَمْ تَرَ  —  لَكُمْ دِينُكُمْ  —  هُمْ فِيهَا",
        "Alam tara — Lakum diinukum — Hum fiihaa",
        "📌 Pengertian:\nApabila Mim Sukun (مْ) bertemu dengan huruf hijaiyah selain Mim (م) dan Ba (ب).\n\n🔊 Cara Membaca:\nBunyi 'M' di bibir dibaca JELAS tanpa mendengung.",
        "https://everyayah.com/data/Alafasy_128kbps/105001.mp3"
    ),
    ServerContent(
        310,
        "10. Qalqalah Sugra (Pantulan Kecil di Tengah Kata)",
        "يَدْخُلُونَ  —  أَلَمْ يَجْعَلْ  —  إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ",
        "Yad'-khuluuna — Alam yaj'-'al — Ab'-tar",
        "📌 Pengertian:\nApabila salah satu dari 5 huruf Qalqalah (ق ط ب ج د — disingkat: Baju Di Toko) mati asli (sukun) di tengah kalimat.\n\n🔊 Cara Membaca:\nDipantulkan secara ringan dan mengalir ke huruf berikutnya.",
        "https://everyayah.com/data/Alafasy_128kbps/105002.mp3"
    ),
    ServerContent(
        311,
        "11. Qalqalah Kubra (Pantulan Besar di Akhir Ayat)",
        "قُلْ هُوَ اللَّهُ أَحَدٌ ۝  —  بِرَبِّ الْفَلَقِ ۝  —  ذَاتِ اللَّهَبِ ۝",
        "Qul huwallaahu Ahad' — Birabbil Falaq' — Dzaatil Lahab'",
        "📌 Pengertian:\nApabila salah satu dari 5 huruf Qalqalah (ق ط ب ج د) berada di akhir kalimat dan dimatikan karena berhenti (Waqaf).\n\n🔊 Cara Membaca:\nDipantulkan dengan KUAT dan jelas di akhir bacaan.",
        "https://everyayah.com/data/Alafasy_128kbps/112001.mp3"
    ),
    ServerContent(
        312,
        "12. Alif Lam Qamariyah (Lam Dibaca Jelas)",
        "الْحَمْدُ  —  الْقَمَرُ  —  الْعَالَمِينَ  —  الْكَوْثَرَ",
        "Al-Hamdu — Al-Qamaru — Al-'Aalamiin — Al-Kautsar",
        "📌 Pengertian:\nApabila Alif Lam (ال) bertemu 14 huruf Qamariyah (ء ب غ ح ج ك و خ ف ع ق ي م هـ). Biasanya terdapat tanda Sukun di atas huruf Lam (الْ).\n\n🔊 Cara Membaca:\nHuruf Lam dibaca JELAS ('Al').",
        "https://everyayah.com/data/Alafasy_128kbps/001002.mp3"
    ),
    ServerContent(
        313,
        "13. Alif Lam Syamsiyah (Lam Lebur ke Huruf Depannya)",
        "الشَّمْسُ  —  الرَّحْمَٰنِ الرَّحِيمِ  —  النَّاسِ  —  الضُّحَىٰ",
        "Asy-Syamsu — Ar-Rahmaanir-Rahiim — An-Naas — Adh-Dhuhaa",
        "📌 Pengertian:\nApabila Alif Lam (ال) bertemu 14 huruf Syamsiyah (ت ث د ذ ر ز س ش ص ض ط ظ ل ن). Ditandai dengan Tasydid pada huruf setelah Lam.\n\n🔊 Cara Membaca:\nHuruf Lam TIDAK dibaca, melainkan langsung melebur masuk ke huruf bertasydid di depannya.",
        "https://everyayah.com/data/Alafasy_128kbps/001003.mp3"
    ),
    ServerContent(
        314,
        "14. Lafadz Jalalah / Allah (Tafkhim Tebal & Tarqiq Tipis)",
        "هُوَ اللَّهُ (Tebal)  —  بِسْمِ اللَّهِ (Tipis)",
        "Huwallaahu (Tebal) — Bismillaahi (Tipis)",
        "📌 Pengertian & Cara Membaca:\n• Dibaca TEBAL (Tafkhim ➔ 'Allaah') apabila didahului baris Fathah atau Dhammah (contoh: هُوَ اللَّهُ , نَصْرُ اللَّهِ).\n• Dibaca TIPIS (Tarqiq ➔ 'Lillaah') apabila didahului baris Kasrah (contoh: بِسْمِ اللَّهِ , الْحَمْدُ لِلَّهِ).",
        "https://everyayah.com/data/Alafasy_128kbps/110001.mp3"
    ),
    ServerContent(
        315,
        "15. Mad Thabi'i / Mad Asli (Panjang 2 Harakat)",
        "قَالَ  —  قِيلَ  —  يَقُولُ  —  نُوحِيهَا",
        "Qaala — Qiila — Yaquulu — Nuuhiihaa",
        "📌 Pengertian:\nApabila Fathah diikuti Alif (ـَا), Kasrah diikuti Ya Sukun (ـِي), atau Dhammah diikuti Wawu Sukun (ـُو).\n\n🔊 Cara Membaca:\nDipanjangkan secara rata selama 2 harakat (2 ketukan).",
        "https://everyayah.com/data/Alafasy_128kbps/001004.mp3"
    ),
    ServerContent(
        316,
        "16. Mad Wajib Muttashil (Panjang 4–5 Harakat dalam 1 Kata)",
        "جَاءَ  —  إِذَا جَاءَ نَصْرُ اللَّهِ  —  السَّمَاءِ  —  سُوءَ",
        "Jaa'a — Idzaa jaa'a nashrullaah — As-samaa'i",
        "📌 Pengertian:\nApabila Mad Thabi'i bertemu dengan huruf Hamzah (ء) di dalam SATU kata yang sama (ditandai garis lengkung panjang ~).\n\n🔊 Cara Membaca:\nWajib dipanjangkan selama 4 sampai 5 harakat.",
        "https://everyayah.com/data/Alafasy_128kbps/110001.mp3"
    ),
    ServerContent(
        317,
        "17. Mad Jaiz Munfashil (Panjang 4–5 Harakat Beda Kata)",
        "إِنَّا أَعْطَيْنَاكَ  —  قُلْ يَا أَيُّهَا الْكَافِرُونَ",
        "Innaa a'thainaaka — Qul yaa ayyuhal kaafiruun",
        "📌 Pengertian:\nApabila Mad Thabi'i di akhir kata bertemu dengan Hamzah (أ / إ) di awal kata berikutnya.\n\n🔊 Cara Membaca:\nBoleh dipanjangkan 2, 4, atau 5 harakat.",
        "https://everyayah.com/data/Alafasy_128kbps/109001.mp3"
    ),
    ServerContent(
        318,
        "18. Mengenal Tanda-Tanda Waqaf (Berhenti) di Mushaf",
        "مـ (Wajib)  •  لا (Jangan)  •  ج (Boleh)  •  قلى (Utama Berhenti)  •  صلى (Utama Lanjut)",
        "Panduan Tanda Berhenti dalam Al-Quran",
        "📌 Arti Simbol Waqaf di Mushaf:\n• مـ (Waqaf Lazim): Wajib berhenti.\n• لا (Laa Waqfa): Dilarang berhenti di tengah ayat (kecuali di akhir ayat).\n• ج (Waqaf Jaiz): Boleh berhenti dan boleh lanjut.\n• قلى (Al-Waqfu Aula): Lebih utama berhenti.\n• صلى (Al-Washlu Aula): Lebih utama terus menyambung bacaan.\n• ∴ ... ∴ (Mu'anaqah): Berhenti di salah satu titik tiga saja.",
        "https://everyayah.com/data/Alafasy_128kbps/002002.mp3"
    )
)

fun getBuiltInDoaAndHaditsCurriculum(): List<ServerContent> = listOf(
    // KOLEKSI 18 DOA HARIAN POPULER
    ServerContent(501, "🤲 Doa Sapu Jagad (Kebaikan Dunia & Akhirat)", "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ", "Rabbanaa aatinaa fid-dunyaa hasanatan wa fil aakhirati hasanatan wa qinaa 'adzaaban-naar", "Artinya: \"Ya Tuhan kami, berilah kami kebaikan di dunia dan kebaikan di akhirat, dan lindungilah kami dari siksa neraka.\" (QS. Al-Baqarah: 201)", "https://everyayah.com/data/Alafasy_128kbps/002201.mp3"),
    ServerContent(502, "🤲 Doa Untuk Kedua Orang Tua (Ayah & Bunda)", "رَبِّ اغْفِرْ لِي وَلِوَالِدَيَّ وَارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا", "Rabbighfir lii waliwaalidayya warhamhumaa kamaa rabbayaanii shaghiiraa", "Artinya: \"Ya Tuhanku, ampunilah dosaku dan dosa kedua orang tuaku, dan sayangilah mereka sebagaimana mereka menyayangiku di waktu kecil.\" (QS. Al-Isra: 24)", "https://everyayah.com/data/Alafasy_128kbps/017024.mp3"),
    ServerContent(503, "🤲 Doa Sebelum Belajar & Tambah Ilmu", "رَبِّ زِدْنِي عِلْمًا وَارْزُقْنِي فَهِمًا", "Rabbi zidnii 'ilman warzuqnii fahman", "Artinya: \"Ya Tuhanku, tambahkanlah ilmu kepadaku dan karuniakanlah kepadaku pemahaman yang baik.\" (QS. Thaha: 114)", "https://everyayah.com/data/Alafasy_128kbps/020114.mp3"),
    ServerContent(504, "🤲 Doa Kelapangan Hati & Kemudahan Bicara (Doa Nabi Musa)", "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي وَاحْلُلْ عُقْدَةً مِنْ لِسَانِي يَفْقَهُوا قَوْلِي", "Rabbisyrah lii shadrii wa yassir lii amrii wahlul 'uqdatan min lisaanii yafqahuu qaulii", "Artinya: \"Ya Tuhanku, lapangkanlah dadaku, mudahkanlah urusanku, dan lepaskanlah kekakuan dari lidahku agar mereka mengerti perkataanku.\" (QS. Thaha: 25-28)", "https://everyayah.com/data/Alafasy_128kbps/020025.mp3"),
    ServerContent(505, "🤲 Doa Naik Kendaraan Darat / Laut / Udara", "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنْقَلِبُونَ", "Subhaanalladzii sakh-khara lanaa haadzaa wa maa kunnaa lahuu muqriniin, wa innaa ilaa rabbinaa lamunqalibuun", "Artinya: \"Maha Suci Allah yang telah menundukkan kendaraan ini bagi kami padahal kami sebelumnya tidak mampu menguasainya, dan sesungguhnya kami akan kembali kepada Tuhan kami.\" (QS. Az-Zukhruf: 13-14)", "https://everyayah.com/data/Alafasy_128kbps/043013.mp3"),
    ServerContent(506, "🤲 Doa Saat Mengalami Kesulitan (Doa Nabi Yunus AS)", "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ", "Laa ilaaha illaa Anta subhaanaka innii kuntu minazh-zhaalimiin", "Artinya: \"Tidak ada Tuhan selain Engkau, Maha Suci Engkau, sesungguhnya aku termasuk orang-orang yang zalim.\" (QS. Al-Anbiya: 87)", "https://everyayah.com/data/Alafasy_128kbps/021087.mp3"),
    ServerContent(507, "🤲 Doa Sebelum Makan & Minum", "بِسْمِ اللَّهِ ، اللَّهُمَّ بَارِكْ لَنَا فِيمَا رَزَقْتَنَا وَقِنَا عَذَابَ النَّارِ", "Bismillaah, Allaahumma baarik lanaa fiimaa razaqtanaa wa qinaa 'adzaaban-naar", "Artinya: \"Dengan menyebut nama Allah. Ya Allah, berkahilah rezeki yang telah Engkau berikan kepada kami dan peliharalah kami dari siksa api neraka.\"", ""),
    ServerContent(508, "🤲 Doa Sesudah Makan & Minum", "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ", "Alhamdulillaahilladzii ath'amanaa wa saqaanaa wa ja'alanaa muslimiin", "Artinya: \"Segala puji bagi Allah yang telah memberi kami makan dan minum serta menjadikan kami termasuk golongan orang-orang muslim.\"", ""),
    ServerContent(509, "🤲 Doa Sebelum Tidur", "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا", "Bismika Allaahumma amuutu wa ahyaa", "Artinya: \"Dengan menyebut nama-Mu ya Allah, aku mati (tidur) dan aku hidup.\" (HR. Bukhari)", ""),
    ServerContent(510, "🤲 Doa Bangun Tidur", "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ", "Alhamdulillaahilladzii ahyaanaa ba'da maa amaatanaa wa ilaihin-nusyuur", "Artinya: \"Segala puji bagi Allah yang telah menghidupkan kami kembali setelah mematikan (menidurkan) kami, dan kepada-Nya kami dibangkitkan.\" (HR. Bukhari)", ""),
    ServerContent(511, "🤲 Doa Masuk Kamar Mandi / WC", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ", "Allaahumma innii a'uudzu bika minal khubutsi wal khabaa'its", "Artinya: \"Ya Allah, sesungguhnya aku berlindung kepada-Mu dari godaan setan laki-laki dan setan perempuan.\" (HR. Bukhari & Muslim)", ""),
    ServerContent(512, "🤲 Doa Keluar Kamar Mandi / WC", "غُفْرَانَكَ ، الْحَمْدُ لِلَّهِ الَّذِي أَذْهَبَ عَنِّي الْأَذَى وَعَافَانِي", "Ghufraanaka, alhamdulillaahilladzii adzhaba 'annil adzaa wa 'aafaanii", "Artinya: \"Aku memohon ampunan-Mu. Segala puji bagi Allah yang telah menghilangkan kotoran penyakit dariku dan menyehatkanku.\"", ""),
    ServerContent(513, "🤲 Doa Keluar Rumah", "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ ، لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "Bismillaahi tawakkaltu 'alallaah, laa haula wa laa quwwata illaa billaah", "Artinya: \"Dengan nama Allah, aku bertawakkal kepada Allah. Tiada daya dan kekuatan kecuali dengan pertolongan Allah.\" (HR. Abu Dawud & Tirmidzi)", ""),
    ServerContent(514, "🤲 Doa Masuk Masjid", "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ", "Allaahummaftah lii abwaaba rahmatik", "Artinya: \"Ya Allah, bukakanlah untukku pintu-pintu rahmat-Mu.\" (HR. Muslim)", ""),
    ServerContent(515, "🤲 Doa Keluar Masjid", "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ", "Allaahumma innii as'aluka min fadhlik", "Artinya: \"Ya Allah, sesungguhnya aku memohon karunia-Mu.\" (HR. Muslim)", ""),
    ServerContent(516, "🤲 Doa Ketika Turun Hujan", "اللَّهُمَّ صَيِّبًا نَافِعًا", "Allaahumma shayyiban naafi'aa", "Artinya: \"Ya Allah, turunkanlah hujan yang membawa manfaat dan keberkahan.\" (HR. Bukhari)", ""),
    ServerContent(517, "🤲 Doa Ketika Bercermin", "اللَّهُمَّ كَمَا حَسَّنْتَ خَلْقِي فَحَسِّنْ خُلُقِي", "Allaahumma kamaa hassanta khalqii fahassin khuluqii", "Artinya: \"Ya Allah, sebagaimana Engkau telah memperindah rupaku, maka perindahlah pula akhlakku.\" (HR. Ahmad)", ""),
    ServerContent(518, "🤲 Doa Berbuka Puasa", "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ", "Dzahabadz zhama'u wabtallatil 'uruqu wa tsabatal ajru insyaa Allaah", "Artinya: \"Telah hilang rasa haus, telah basah urat-urat, dan telah tetap pahala, insya Allah.\" (HR. Abu Dawud)", ""),

    // KOLEKSI 10 HADITS PENDEK SHAHIH PILIHAN
    ServerContent(601, "📜 Hadits 1: Keutamaan Belajar & Mengajarkan Al-Quran", "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ", "Khairukum man ta'allamal Qur'aana wa 'allamah", "Artinya: \"Sebaik-baik kalian adalah orang yang mempelajari Al-Quran dan mengajarkannya.\" (HR. Bukhari)", ""),
    ServerContent(602, "📜 Hadits 2: Senyum kepada Saudara adalah Sedekah", "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ", "Tabassumuka fii wajhi akhiika laka shadaqah", "Artinya: \"Senyummu di hadapan saudaramu adalah sedekah bagimu.\" (HR. Tirmidzi)", ""),
    ServerContent(603, "📜 Hadits 3: Kebersihan adalah Sebagian dari Iman", "الطُّهُورُ شَطْرُ الْإِيمَانِ", "Ath-thuhuuru syathrul iimaan", "Artinya: \"Kesucian dan kebersihan itu adalah sebagian dari iman.\" (HR. Muslim)", ""),
    ServerContent(604, "📜 Hadits 4: Larangan Marah & Hadiah Surga", "لَا تَغْضَبْ وَلَكَ الْجَنَّةُ", "Laa taghdhab wa lakal jannah", "Artinya: \"Janganlah kamu marah, maka bagimu adalah Surga.\" (HR. Thabrani)", ""),
    ServerContent(605, "📜 Hadits 5: Kasih Sayang kepada Sesama Makhluk", "مَنْ لَا يَرْحَمْ لَا يُرْحَمْ", "Man laa yarham laa yurham", "Artinya: \"Barangsiapa yang tidak menyayangi, maka ia tidak akan disayangi.\" (HR. Bukhari & Muslim)", ""),
    ServerContent(606, "📜 Hadits 6: Adab Makan dengan Tangan Kanan", "يَا غُلَامُ سَمِّ اللَّهَ ، وَكُلْ بِيَمِينِكَ ، وَكُلْ مِمَّا يَلِيكَ", "Yaa ghulaamu sammillaah, wa kul biyamiinika, wa kul mimmaa yaliik", "Artinya: \"Wahai anak kecil, bacalah Bismillah, makanlah dengan tangan kananmu, dan makanlah makanan yang terdekat darimu.\" (HR. Bukhari & Muslim)", ""),
    ServerContent(607, "📜 Hadits 7: Menjaga Lisan & Berkata yang Baik", "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الْآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ", "Man kaana yu'minu billaahi wal yaumil aakhiri fal-yaqul khairan au liyashmut", "Artinya: \"Barangsiapa beriman kepada Allah dan Hari Akhir, hendaklah ia berkata yang baik atau diam.\" (HR. Bukhari & Muslim)", ""),
    ServerContent(608, "📜 Hadits 8: Kewajiban Menuntut Ilmu", "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ", "Thalabul 'ilmi fariidhatun 'alaa kulli muslim", "Artinya: \"Menuntut ilmu itu wajib atas setiap muslim.\" (HR. Ibnu Majah)", ""),
    ServerContent(609, "📜 Hadits 9: Setiap Kebaikan adalah Sedekah", "كُلُّ مَعْرُوفٍ صَدَقَةٌ", "Kullu ma'ruufin shadaqah", "Artinya: \"Setiap perbuatan baik itu adalah sedekah.\" (HR. Bukhari & Muslim)", ""),
    ServerContent(610, "📜 Hadits 10: Keutamaan Menyebarkan Salam", "أَفْشُوا السَّلَامَ بَيْنَكُمْ", "Afsyus-salaama bainakum", "Artinya: \"Tebarkanlah salam di antara kalian (agar kalian saling menyayangi).\" (HR. Muslim)", "")
)

fun getBuiltInVideoCurriculum(): List<ServerContent> = listOf(
    ServerContent(401, "Belajar Huruf Hijaiyah & Makhraj Ceria Bersama Anak", "", "Animasi Edukasi Anak Sholeh", "Video belajar melafalkan 28 huruf Hijaiyah dengan lagu dan animasi yang mudah diingat anak-anak.", "https://www.youtube.com/watch?v=1t4sM2xefCc"),
    ServerContent(402, "Kisah Teladan Nabi Ibrahim AS & Burung Pipit", "", "Kisah 25 Nabi & Rasul", "Kisah inspiratif keberanian Nabi Ibrahim AS ketika menghadapi Raja Namrud.", "https://www.youtube.com/watch?v=aqz-KE-bpKQ")
)

fun getBuiltInFaqCurriculum(): List<ServerContent> = listOf(
    ServerContent(201, "❓ Bagaimana cara mendengarkan suara penjelasan materi di menu Belajar?", "", "", "Buka menu Belajar -> Ketuk tombol '📖 Buka Arti & Penjelasan' pada materi mana saja -> Lalu ketuk tombol kuning '🔊 Putar Suara Penjelasan & Nasihat Ini'.", ""),
    ServerContent(202, "❓ Bagaimana cara mengaktifkan Mode Mushaf Al-Quran Full Arab?", "", "", "Buka menu Al-Quran -> Pilih Surah -> Ketuk tombol tab '📖 Mushaf Arab' di bagian atas layar. Seluruh ayat akan menyambung utuh seperti Al-Quran cetak!", "")
)