# Dokumen Keamanan — Malsz AI Robot

Dokumen ini memaparkan model ancaman, perlindungan autentikasi, enkripsi password, dan kebijakan privasi yang diimplementasikan dalam arsitektur Malsz AI Robot.

---

## 1. Zero Plaintext Passwords
- Semua password pengguna di-hash menggunakan algoritma **bcrypt** dengan salt rounds = 10 (`bcrypt.genSaltSync(10)`).
- Tidak ada password plaintext yang disimpan di database, di-cache di memori, dicetak pada log, atau dikembalikan dalam respon API JSON.

## 2. Autentikasi JWT & Manajemen Token
- **Access Token**: JWT bertanda tangan digital dengan masa berlaku terbatas (60 menit). Memuat `id`, `username`, dan `role`.
- **Refresh Token**: String acak kriptografis (40 bytes hex) yang disimpan di database dalam bentuk **SHA-256 hash**.
- **Pencabutan Sesi**: Logout atau perubahan password akan langsung mencabut (*revoke*) seluruh refresh token aktif pengguna.

## 3. Penegakan Otorisasi Server-Side (RBAC)
- Status akun (`ACTIVE`, `BANNED`, `EXPIRED`) diverifikasi pada **setiap** permintaan terproteksi di tingkat middleware server (`middleware/auth.js`).
- Manipulasi nilai role atau status pada sisi klien (Android) tidak dapat menembus validasi database backend.
- Role Hierarchy:
  - **OWNER**: Memiliki wewenang penuh, bootstrap melalui seed script, tidak dapat di-ban atau di-delete oleh akun biasa.
  - **ADMIN**: Mengelola reseller dan member dalam batas kewenangan.
  - **RESELLER**: Terkunci hanya bisa membuat akun role `MEMBER` dan hanya bisa mengelola member yang dibuat oleh akunnya sendiri.
  - **MEMBER**: Hanya memiliki akses ke fitur pengguna (Floating AI, Chat Global, Profil, Ganti Password).

## 4. Keamanan API Key & Proteksi Multimodal AI
- **Tidak ada API Key pada APK**: `GEMINI_API_KEY` dikonfigurasi secara eksklusif pada variabel lingkungan server (`.env`).
- Tangkapan layar yang dikirimkan untuk analisis visual tidak disimpan secara permanen di server secara default, melainkan diproses langsung dalam memori buffer dan dicatat metadata pemakaian saja (`ai_usage_logs`).
- Jika API key diganti, administrator cukup memperbarui `.env` server tanpa perlu merilis ulang APK ke pengguna.

## 5. Keamanan Android Floating AI & Aksesibilitas
- Layanan Aksesibilitas (`AIAccessibilityService`) dilindungi pemeriksaan keamanan:
  - Tidak membaca atau memodifikasi kolom berkategori password (`isPassword == true`).
  - Mengimplementasikan jeda minimal 2 detik sebelum injeksi teks dan menyediakan tombol Batal.
  - Tidak melakukan aksi submit, pembelian, atau transfer data tanpa tindakan sadar dari pengguna.
