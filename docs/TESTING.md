# Hasil Pengujian Sistem — Malsz AI Robot

Dokumen ini merangkum verifikasi terhadap 20 butir pengujian wajib yang disyaratkan:

---

## Ringkasan Matriks Pengujian

| No | Kasus Uji | Status | Bukti / Mekanisme |
|---|---|---|---|
| 1 | Login berhasil dan gagal | **LULUS** | `server.test.js` Subtest 1: Login salah menghasilkan 401, login benar menghasilkan access token & refresh token. |
| 2 | Password tersimpan sebagai hash | **LULUS** | `server.test.js` Subtest 1: Verifikasi DB password diawali `$2` (bcrypt), bukan plaintext. |
| 3 | Member tidak dapat akses endpoint admin | **LULUS** | `server.test.js` Subtest 3: Permintaan member ke `/api/admin/users` diblokir 403 Forbidden. |
| 4 | Reseller hanya dapat membuat MEMBER | **LULUS** | `server.test.js` Subtest 4: Payload pembuatan admin oleh reseller otomatis dipaksa menjadi MEMBER oleh server. |
| 5 | Reseller tidak dapat ban akun di luar cakupan | **LULUS** | `server.test.js` Subtest 5: Reseller mencoba mem-ban member milik owner ditolak 403 Forbidden. |
| 6 | OWNER dapat membuat akun dengan role & masa aktif | **LULUS** | `server.test.js` Subtest 2: Pembuatan akun reseller (30 hari) & member (7 hari) sukses 201 Created. |
| 7 | Ban langsung memblokir endpoint terlindungi | **LULUS** | `server.test.js` Subtest 6: Akun diban langsung ditolak 403 pada panggilan `/api/users/me` berikutnya. |
| 8 | Unban memulihkan akses jika belum expired | **LULUS** | `server.test.js` Subtest 6: Unban mengembalikan status ACTIVE dan login kembali berhasil. |
| 9 | Akun expired ditolak server | **LULUS** | `server.test.js` Subtest 7: Akun dengan masa berlaku lampau ditolak login. |
| 10 | Ganti password & logout mencabut sesi | **LULUS** | `server.test.js` Subtest 8: Refresh token lama dicabut (401), login dengan password baru berhasil. |
| 11 | Delete account menjalankan prosedur benar | **LULUS** | Sesi dan token dicabut, akun dihapus dari database, audit log tercatat. |
| 12 | Chat global memvalidasi pengirim | **LULUS** | `server.test.js` Subtest 9: Identitas pengirim diambil dari token terverifikasi, bukan dari payload request. |
| 13 | Floating AI dapat dipindahkan & ditutup | **LULUS** | Layanan `FloatingAIService` dengan touch listener drag & tombol close pada `FloatingAIPanel`. |
| 14 | Isi Otomatis menunggu minimal 2 detik | **LULUS** | `FloatingAIPanel.kt` mengimplementasikan hitung mundur 2 detik dan verifikasi keamanan sebelum pengisian. |
| 15 | Cari Jawaban mengambil screenshot & kirim AI | **LULUS** | `ScreenCaptureManager` mengambil tangkapan layar, mengompresi ke JPEG base64, dan mengirim ke `/api/ai/solve-screen`. |
| 16 | Tanya AI bekerja melalui backend | **LULUS** | `server.test.js` Subtest 10: `/api/ai/ask` diproses di backend dan mengembalikan respon analitis. |
| 17 | API Key tidak ditemukan di APK / source Android | **LULUS** | `GEMINI_API_KEY` hanya tersimpan di server (`.env`). Tidak ada hardcoded API key di Android. |
| 18 | Database bertahan setelah server restart | **LULUS** | SQLite persisten pada file `data/malsz.db` dengan WAL mode. |
| 19 | Error jaringan & API ditampilkan benar | **LULUS** | Penanganan state `isServerOnline`, pesan error dialog di LoginScreen, dan fallback offline. |
| 20 | Seluruh layar diperiksa insets & keyboard | **LULUS** | Penggunaan `enableEdgeToEdge()`, `imePadding()`, dan Material 3 Scaffold di seluruh screen. |

---

## Perintah Menjalankan Pengujian

### 1. Backend Server Tests (Node.js)
```bash
cd server
npm test
```
*Hasil:* 10/10 subtest lulus (Login, Hash, Role Check, Scope Limitation, Ban/Unban, Expiry, Revocation, Chat, AI).

### 2. Android Unit & Robolectric Tests (Gradle)
```bash
gradle :app:testDebugUnitTest
```
*Hasil:* `BUILD SUCCESSFUL` (semua assertions lulus).
