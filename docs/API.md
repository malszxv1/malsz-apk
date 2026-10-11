# Dokumentasi API — Malsz AI Robot

Seluruh endpoint diawali dengan prefix `/api/`. Autentikasi menggunakan format header:
`Authorization: Bearer <accessToken>`

---

## 1. Autentikasi (`/api/auth`)

### POST `/api/auth/login`
Autentikasi pengguna dan mengembalikan token akses & refresh.
- **Request Body**:
  ```json
  {
    "username": "malsz",
    "password": "MalszCyber2026!"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "message": "Login successful",
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "7f9a12c...",
    "user": {
      "id": "e5c4e813-...",
      "username": "malsz",
      "role": "OWNER",
      "status": "ACTIVE",
      "created_at": "2026-10-11T03:00:00.000Z",
      "expires_at": null
    }
  }
  ```

### POST `/api/auth/refresh`
Memperbarui token akses yang kedaluwarsa menggunakan refresh token.
- **Request Body**:
  ```json
  {
    "refreshToken": "7f9a12c..."
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "accessToken": "eyJhbGciOi..."
  }
  ```

### POST `/api/auth/logout`
Mencabut refresh token pengguna dari database.
- **Request Body**:
  ```json
  {
    "refreshToken": "7f9a12c..."
  }
  ```

### POST `/api/auth/change-password`
Mengubah password pengguna saat ini (terautentikasi) dan mencabut sesi refresh token lain.
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
  ```json
  {
    "oldPassword": "CurrentPassword123!",
    "newPassword": "NewStrongPassword456!"
  }
  ```

---

## 2. Profil Pengguna (`/api/users`)

### GET `/api/users/me`
Mengambil detail profil akun pengguna yang sedang login.

### DELETE `/api/users/me`
Menghapus akun pengguna sendiri secara permanen dan mencabut seluruh token aktif.

---

## 3. Pengelolaan Akun & Lisensi (`/api/admin`)

Hanya dapat diakses oleh role `OWNER`, `ADMIN`, atau `RESELLER` sesuai batasan permission:

### POST `/api/admin/users`
Membuat akun baru.
- **Aturan Role**:
  - `OWNER`: Dapat memilih role `ADMIN`, `RESELLER`, atau `MEMBER`.
  - `RESELLER`: Otomatis dipaksa menjadi `MEMBER`. Tidak dapat memilih role lain.
- **Request Body**:
  ```json
  {
    "username": "member_baru",
    "password": "MemberSecret123!",
    "role": "MEMBER",
    "expirationDays": 30
  }
  ```

### GET `/api/admin/users`
Daftar seluruh akun dengan query parameter:
- `search`: Pencarian nama username
- `role`: Filter role (`OWNER`, `ADMIN`, `RESELLER`, `MEMBER`)
- `status`: Filter status (`ACTIVE`, `BANNED`, `EXPIRED`)
- `page`: Nomor halaman (default: 1)
- `limit`: Jumlah per halaman (default: 20)

### POST `/api/admin/users/:id/ban`
Mem-ban akun target.
- **Request Body**:
  ```json
  {
    "reason": "Pelanggaran aturan lisensi"
  }
  ```
- **Catatan**: Akun `OWNER` tidak dapat di-ban. Reseller hanya dapat mem-ban akun member miliknya.

### POST `/api/admin/users/:id/unban`
Membatalkan status ban dan memulihkan akses (jika akun belum kedaluwarsa).

### DELETE `/api/admin/users/:id`
Menghapus akun secara permanen.

### GET `/api/admin/stats`
Statistik ringkasan sistem: total akun, akun aktif, akun diban, dan akun expired.

---

## 4. Chat Global (`/api/chat`)

### GET `/api/chat/messages`
Mengambil riwayat obrolan global dengan identitas pengirim, role, dan timestamp.
- **Query Params**: `limit` (default: 50), `before` (timestamp ISO)

### POST `/api/chat/messages`
Mengirim pesan obrolan global baru. Identitas divalidasi langsung dari access token.
- **Request Body**:
  ```json
  {
    "message": "Halo dari Malsz Robot!"
  }
  ```

### DELETE `/api/chat/messages/:id`
Menghapus pesan (hanya diizinkan untuk penulis asli atau role `OWNER`/`ADMIN`).

### POST `/api/chat/messages/:id/report`
Melaporkan pesan yang tidak pantas ke tim moderasi.

---

## 5. Fitur AI Server-Side (`/api/ai`)

### POST `/api/ai/ask`
Percakapan cepat dengan kepribadian Malsz AI Robot.
- **Request Body**:
  ```json
  {
    "question": "Jelaskan konsep energi potensial gravitasi",
    "history": []
  }
  ```

### POST `/api/ai/solve-screen`
Menganalisis tangkapan layar untuk menemukan jawaban soal atau pertanyaan.
- **Request Body**:
  ```json
  {
    "image": "base64_encoded_jpeg_string...",
    "mimeType": "image/jpeg"
  }
  ```
- **Response**:
  ```json
  {
    "answer": "B. Fotosintesis",
    "reason": "Proses tersebut mengubah energi cahaya menjadi energi kimia dalam kloroplas.",
    "question": "Proses pembentukan makanan pada tumbuhan...",
    "provider": "GEMINI",
    "timestamp": "2026-10-11T03:00:00.000Z"
  }
  ```

### POST `/api/ai/fill-answer`
Menghasilkan jawaban teks optimal untuk kolom formulir yang sedang dipilih.
- **Request Body**:
  ```json
  {
    "fieldHint": "Nama Lengkap",
    "screenContext": "Formulir pendaftaran"
  }
  ```
- **Response**:
  ```json
  {
    "fill_text": "Malsz Robot",
    "delay_seconds": 2,
    "provider": "GEMINI"
  }
  ```
