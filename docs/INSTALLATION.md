# Panduan Instalasi & Konfigurasi — Malsz AI Robot

Dokumen ini menjelaskan langkah-langkah instalasi, konfigurasi backend Node.js, migrasi database SQLite, serta koneksi aplikasi Android Malsz AI Robot ke server.

---

## 1. Persyaratan Sistem

- **Node.js**: Versi 18 LTS atau 20+ LTS
- **NPM**: Versi 9+
- **Android**: SDK 24+ (Android 7.0 Nougat ke atas), target SDK 34-36
- **Google Gemini API Key**: Dikonfigurasi pada lingkungan server (`GEMINI_API_KEY`)

---

## 2. Instalasi & Setup Server Node.js

### A. Masuk ke Direktori Server & Pasang Dependensi
```bash
cd server
npm install
```

### B. Konfigurasi Variabel Lingkungan (.env)
Salin berkas `.env.example` ke `.env`:
```bash
cp .env.example .env
```
Sesuaikan konfigurasi di dalam file `.env`:
```env
PORT=5000
NODE_ENV=production
DATABASE_PATH=data/malsz.db

# JWT Keys
JWT_SECRET=malsz_cyber_super_secret_jwt_key_2026
JWT_REFRESH_SECRET=malsz_cyber_refresh_super_secret_key_2026

# Password bootstrap owner awal (hanya dipakai saat pertama kali seed)
OWNER_PASSWORD=MalszOwner2026!

# Model & API Key Gemini untuk pemrosesan AI multimodal di server
GEMINI_API_KEY=AIzaSy...
GEMINI_MODEL=models/gemini-3.8-flash

# Keamanan Rate Limiting
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=300
```

### C. Menjalankan Migrasi Database SQLite
Jalankan migrasi skema tabel secara otomatis:
```bash
npm run migrate
```
Perintah ini akan membuat tabel `users`, `refresh_tokens`, `global_messages`, `audit_logs`, `ai_usage_logs`, dan tabel indeks yang diperlukan pada `data/malsz.db`.

### D. Melakukan Bootstrap Akun OWNER Pertama
Jalankan seed owner yang idempotent:
```bash
npm run seed:owner
```
- Username owner bawaan: `malsz`
- Password diambil dari `OWNER_PASSWORD` di `.env` (atau bawaan `MalszCyber2026!`), di-hash secara aman menggunakan bcrypt.
- Password tidak pernah disimpan dalam bentuk plaintext.

### E. Menjalankan Server
Untuk mode produksi:
```bash
npm start
```
Untuk mode pengembangan (auto-reload):
```bash
npm run dev
```
Server akan aktif di: `http://0.0.0.0:5000` (atau port yang ditentukan).

---

## 3. Konfigurasi Aplikasi Android

### A. Koneksi ke Backend
Aplikasi Android default menggunakan loopback Android Emulator:
`http://10.0.2.2:5000/`

Jika Anda menjalankan aplikasi pada perangkat fisik atau IP jaringan lokal yang berbeda:
1. Buka layar **Login** atau **Settings** di aplikasi.
2. Ketuk **Konfigurasi Server**.
3. Masukkan URL server Anda, misalnya: `http://192.168.1.100:5000/`.
4. Simpan perubahan. Aplikasi akan langsung terhubung ke server tersebut.

### B. Izin Sistem Android (Permissions)
Aplikasi Malsz AI Robot memerlukan izin berikut untuk fitur asisten:
1. **Izin Tampilkan di Atas Aplikasi Lain (SYSTEM_ALERT_WINDOW)**:
   - Diperlukan untuk Floating AI robot overlay.
   - Aktifkan melalui menu Pengaturan di aplikasi atau Pengaturan Sistem Android > Aplikasi > Malsz AI Robot > Tampilkan di atas aplikasi lain.
2. **Layanan Aksesibilitas (BIND_ACCESSIBILITY_SERVICE)**:
   - Diperlukan untuk fitur **Isi Otomatis**.
   - Aktifkan di Pengaturan Aksesibilitas Android > Malsz AI Robot Accessibility Service.
3. **Perekaman Layar (MediaProjection)**:
   - Diperlukan untuk fitur **Cari Jawaban (Screenshot AI)**.
   - Dialog konfirmasi sistem akan muncul saat pertama kali mengambil tangkapan layar.
