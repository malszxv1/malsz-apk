# MALSZ AI ROBOT — PREMIUM CYBER COMPANION

Malsz AI Robot adalah aplikasi Android native yang didukung backend Node.js + Express + SQLite, menghadirkan asisten robot siber pintar (*Cute Cyber Companion*) dengan antarmuka Holographic Sci-Fi, sistem autentikasi bertingkat, panel manajemen lisensi (Owner, Admin, Reseller, Member), obrolan global, serta **Floating AI** yang dapat berinteraksi di atas aplikasi lain.

---

## 🎨 Identitas Visual & Maskot

- **Maskot**: Robot bulat menggemaskan berjubah navy dengan aksen emas, armor silver mengkilap, dan mata visor neon biru/cyan (`app/src/main/res/drawable/img_malsz_robot.jpg`).
- **Palet Warna**:
  - Obsidian Navy: `#080D18`
  - Midnight Blue: `#111D35`
  - Electric Blue: `#168BFF`
  - Hologram Cyan: `#58E7FF`
  - Robot Silver: `#D9E3F0`
  - Armor Gold: `#D9B778`

---

## 🚀 Fitur Utama

1. **Autentikasi & Keamanan Tingkat Lanjut**:
   - Sistem login berbasis JWT access token dan revocable refresh token.
   - Password di-hash menggunakan **bcrypt**.
   - Penegakan masa aktif (hari / lifetime) dan status ban secara real-time pada level server.
2. **Sistem Role & Hierarki**:
   - **OWNER**: Akses absolut, seed bootstrap aman, pembuatan akun Admin, Reseller, dan Member, ban/unban, audit log.
   - **ADMIN**: Moderasi akun dan pesan chat sesuai wewenang.
   - **RESELLER**: Terkunci hanya dapat membuat dan mengelola akun role `MEMBER` miliknya sendiri.
   - **MEMBER**: Penggunaan Floating AI, Chat Global, pengelolaan profil & password sendiri.
3. **Floating AI Assistant (WindowManager Overlay)**:
   - **1. Isi Otomatis**: Mendeteksi kolom aktif via `AccessibilityService`, menunggu verifikasi 2 detik, dan menginjeksi teks jawaban secara aman (tidak menyentuh kolom password).
   - **2. Cari Jawaban (Screenshot AI)**: Mengambil tangkapan layar melalui `MediaProjection`, dikirimkan ke model AI multimodal Gemini di server, dan menyajikan jawaban terstruktur (Jawaban & Alasan singkat).
   - **3. Tanya AI**: Panel percakapan mini mengambang tanpa perlu meninggalkan aplikasi aktif.
   - **4. Sembunyikan AI**: Sakelar ON/OFF untuk meminimalkan robot ke pojok layar.
4. **Chat Global**:
   - Ruang obrolan real-time antar pengguna aktif dengan badge role pengirim, timestamp, dan fitur moderasi pesan.
5. **Panel Admin & Reseller**:
   - Pencarian username, filter role & status, statistik akun, serta aksi ban/unban dengan catatan alasan.

---

## 📁 Struktur Monorepo Proyek

```
MalszAIRobot/
├── app/                           # Android Native Application
│   ├── src/main/java/com/example/
│   │   ├── data/
│   │   │   ├── api/               # Retrofit MalszApiService & ApiClient
│   │   │   ├── local/             # TokenManager (SharedPreferences/Session)
│   │   │   ├── model/             # Auth, User, Chat, and AI DTO models
│   │   │   └── repository/        # Auth, User, Chat, and AI Repositories
│   │   ├── service/
│   │   │   ├── AIAccessibilityService.kt   # Safe auto text injection
│   │   │   ├── FloatingAIService.kt        # Draggable WindowManager overlay
│   │   │   └── ScreenCaptureManager.kt     # MediaProjection screenshot engine
│   │   ├── ui/
│   │   │   ├── components/        # CyberCard, CyberButton, RobotAvatar, Badges
│   │   │   ├── navigation/        # MalszNavGraph & Screen routes
│   │   │   ├── screens/           # Splash, Login, Dashboard, Chat, Settings, Admin
│   │   │   └── theme/             # Cyber companion dark theme & colors
│   │   └── MainActivity.kt
│   └── src/main/res/              # Drawables, mipmaps, XML configs
├── server/                        # Node.js + Express Backend
│   ├── src/
│   │   ├── config/                # JWT & security configs
│   │   ├── database/              # SQLite connection & migrations
│   │   ├── middleware/            # Auth, RBAC permissions, error handler
│   │   ├── routes/                # Auth, Users, Admin, Chat, AI routes
│   │   ├── controllers/           # Endpoint controllers
│   │   ├── services/              # Gemini multimodal AI & account logic
│   │   ├── repositories/          # SQLite database query repositories
│   │   ├── app.js                 # Express application setup
│   │   └── server.js              # Server entry point
│   ├── scripts/seedOwner.js       # Idempotent owner bootstrap script
│   ├── tests/server.test.js       # Automated backend integration test suite
│   ├── owner.json                 # Non-secret bootstrap configuration template
│   ├── .env.example
│   └── package.json
├── docs/
│   ├── INSTALLATION.md
│   ├── API.md
│   ├── SECURITY.md
│   └── TESTING.md
└── README.md
```

---

## ⚡ Cara Menjalankan Proyek

### 1. Menjalankan Backend Server
```bash
cd server
npm install
cp .env.example .env
npm run migrate
npm run seed:owner
npm start
```
Server akan aktif di `http://0.0.0.0:5000`.

### 2. Akun Owner Awal (Bawaan Bootstrap)
- **Username**: `malsz`
- **Password**: `MalszCyber2026!` (atau sesuai `OWNER_PASSWORD` di `.env`)

### 3. Menjalankan Pengujian
- **Server Tests**: `cd server && npm test`
- **Android Tests**: `gradle :app:testDebugUnitTest`

### 4. Menjalankan Aplikasi Android
Jalankan di emulator Android. URL server bawaan adalah `http://10.0.2.2:5000/`. Anda juga dapat mengubah URL server langsung dari tombol **Konfigurasi Server** pada layar Login atau Settings aplikasi.
