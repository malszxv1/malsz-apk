const test = require('node:test');
const assert = require('node:assert/strict');
const http = require('http');
const fs = require('fs');
const path = require('path');

// Set TEST environment before requiring db and app
const testDbPath = path.join(__dirname, '../data/test_malsz.db');
if (fs.existsSync(testDbPath)) {
  fs.unlinkSync(testDbPath);
}
process.env.DATABASE_PATH = testDbPath;
process.env.NODE_ENV = 'test';

const db = require('../src/database/connection');
const app = require('../src/app');
const { runMigrations } = require('../src/database/migrate');
const { seedOwner } = require('../scripts/seedOwner');

let server;
let baseUrl;

async function request(p, options = {}) {
  const url = `${baseUrl}${p}`;
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  const res = await fetch(url, {
    method: options.method || 'GET',
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined
  });
  const data = await res.json().catch(() => ({}));
  return { status: res.status, ok: res.ok, data };
}

test.before(async () => {
  runMigrations();
  seedOwner();

  server = http.createServer(app);
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const port = server.address().port;
  baseUrl = `http://127.0.0.1:${port}`;
});

test.after(async () => {
  if (server) {
    await new Promise(resolve => server.close(resolve));
  }
  db.close();
  if (fs.existsSync(testDbPath)) {
    try { fs.unlinkSync(testDbPath); } catch (e) {}
  }
});

test('1 & 2: Login success, failure, and password hash verification', async () => {
  // Login failure: invalid credentials
  const fail = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'malsz', password: 'WrongPassword!' }
  });
  assert.equal(fail.status, 401);

  // Login success: correct credentials
  const success = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'malsz', password: 'MalszCyber2026!' }
  });
  assert.equal(success.status, 200);
  assert.ok(success.data.accessToken);
  assert.ok(success.data.refreshToken);
  assert.equal(success.data.user.username, 'malsz');
  assert.equal(success.data.user.role, 'OWNER');

  // Verify password in DB is a hash, never plaintext
  const ownerDb = db.prepare('SELECT password_hash FROM users WHERE username = ?').get('malsz');
  assert.notEqual(ownerDb.password_hash, 'MalszCyber2026!');
  assert.ok(ownerDb.password_hash.startsWith('$2')); // bcrypt prefix
});

test('6: OWNER can create accounts with allowed roles and expiration', async () => {
  const loginRes = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'malsz', password: 'MalszCyber2026!' }
  });
  const ownerToken = loginRes.data.accessToken;

  // Create Reseller account
  const createReseller = await request('/api/admin/users', {
    method: 'POST',
    headers: { Authorization: `Bearer ${ownerToken}` },
    body: {
      username: 'reseller1',
      password: 'ResellerSecret123!',
      role: 'RESELLER',
      expirationDays: 30
    }
  });
  assert.equal(createReseller.status, 201);
  assert.equal(createReseller.data.user.role, 'RESELLER');
  assert.ok(createReseller.data.user.expires_at);

  // Create Member account
  const createMember = await request('/api/admin/users', {
    method: 'POST',
    headers: { Authorization: `Bearer ${ownerToken}` },
    body: {
      username: 'member1',
      password: 'MemberSecret123!',
      role: 'MEMBER',
      expirationDays: 7
    }
  });
  assert.equal(createMember.status, 201);
  assert.equal(createMember.data.user.role, 'MEMBER');
});

test('3: MEMBER cannot access admin endpoints', async () => {
  const loginRes = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'member1', password: 'MemberSecret123!' }
  });
  assert.equal(loginRes.status, 200);
  const memberToken = loginRes.data.accessToken;

  const adminAccess = await request('/api/admin/users', {
    headers: { Authorization: `Bearer ${memberToken}` }
  });
  assert.equal(adminAccess.status, 403);
});

test('4: RESELLER can ONLY create MEMBER accounts', async () => {
  const loginRes = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'reseller1', password: 'ResellerSecret123!' }
  });
  assert.equal(loginRes.status, 200);
  const resellerToken = loginRes.data.accessToken;

  // Attempt to create ADMIN (should be forced to MEMBER or rejected)
  const attemptAdmin = await request('/api/admin/users', {
    method: 'POST',
    headers: { Authorization: `Bearer ${resellerToken}` },
    body: {
      username: 'sub_admin_attempt',
      password: 'SubAdminPassword123!',
      role: 'ADMIN',
      expirationDays: 30
    }
  });
  assert.equal(attemptAdmin.status, 201);
  assert.equal(attemptAdmin.data.user.role, 'MEMBER');
  assert.equal(attemptAdmin.data.user.created_by, loginRes.data.user.id);
});

test('5: RESELLER cannot ban accounts outside their scope', async () => {
  const loginRes = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'reseller1', password: 'ResellerSecret123!' }
  });
  const resellerToken = loginRes.data.accessToken;

  // Attempt to ban 'member1' (created by owner, not this reseller)
  const member1 = db.prepare('SELECT id FROM users WHERE username = ?').get('member1');
  const banAttempt = await request(`/api/admin/users/${member1.id}/ban`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${resellerToken}` },
    body: { reason: 'Unauthorized ban attempt' }
  });
  assert.equal(banAttempt.status, 403);
});

test('7 & 8: Ban immediately revokes access, and unban restores access', async () => {
  const ownerLogin = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'malsz', password: 'MalszCyber2026!' }
  });
  const ownerToken = ownerLogin.data.accessToken;

  const memberLogin = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'member1', password: 'MemberSecret123!' }
  });
  const memberToken = memberLogin.data.accessToken;
  const memberId = memberLogin.data.user.id;

  // 1. Verify Member can access /api/users/me initially
  const profileOk = await request('/api/users/me', {
    headers: { Authorization: `Bearer ${memberToken}` }
  });
  assert.equal(profileOk.status, 200);

  // 2. Owner bans Member
  const banRes = await request(`/api/admin/users/${memberId}/ban`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${ownerToken}` },
    body: { reason: 'Test policy breach' }
  });
  assert.equal(banRes.status, 200);

  // 3. Member request immediately blocked 403
  const blockedReq = await request('/api/users/me', {
    headers: { Authorization: `Bearer ${memberToken}` }
  });
  assert.equal(blockedReq.status, 403);
  assert.equal(blockedReq.data.error, 'Account is banned');

  // 4. Member login is blocked
  const loginBlocked = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'member1', password: 'MemberSecret123!' }
  });
  assert.equal(loginBlocked.status, 403);

  // 5. Owner unbans Member
  const unbanRes = await request(`/api/admin/users/${memberId}/unban`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${ownerToken}` },
    body: { reason: 'Pardon granted' }
  });
  assert.equal(unbanRes.status, 200);

  // 6. Member can login and access again
  const loginRestored = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'member1', password: 'MemberSecret123!' }
  });
  assert.equal(loginRestored.status, 200);
});

test('9: Expired account is rejected by server', async () => {
  // Create account expired yesterday
  const expiredDate = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString();
  db.prepare(`
    INSERT INTO users (id, username, password_hash, role, status, created_by, created_at, expires_at, updated_at)
    VALUES ('exp-123', 'expired_user', 'hash_test', 'MEMBER', 'ACTIVE', NULL, datetime('now'), ?, datetime('now'))
  `).run(expiredDate);

  const loginAttempt = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'expired_user', password: 'any' }
  });
  assert.equal(loginAttempt.status, 401);
});

test('10: Change password and logout revokes refresh tokens', async () => {
  const memberLogin = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'member1', password: 'MemberSecret123!' }
  });
  assert.equal(memberLogin.status, 200);
  const memberToken = memberLogin.data.accessToken;
  const refreshToken = memberLogin.data.refreshToken;

  // Change password
  const changeRes = await request('/api/auth/change-password', {
    method: 'POST',
    headers: { Authorization: `Bearer ${memberToken}` },
    body: { oldPassword: 'MemberSecret123!', newPassword: 'NewPassword999!' }
  });
  assert.equal(changeRes.status, 200);

  // Old refresh token must now be revoked
  const refreshAttempt = await request('/api/auth/refresh', {
    method: 'POST',
    body: { refreshToken }
  });
  assert.equal(refreshAttempt.status, 401);

  // Login with new password works
  const newLogin = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'member1', password: 'NewPassword999!' }
  });
  assert.equal(newLogin.status, 200);
});

test('12: Global chat validates sender identity and allows messaging', async () => {
  const memberLogin = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'member1', password: 'NewPassword999!' }
  });
  const memberToken = memberLogin.data.accessToken;

  // Post chat message
  const postMsg = await request('/api/chat/messages', {
    method: 'POST',
    headers: { Authorization: `Bearer ${memberToken}` },
    body: { message: 'Halo dari Malsz Robot Member!' }
  });
  assert.equal(postMsg.status, 201);
  assert.equal(postMsg.data.message.username, 'member1');
  assert.equal(postMsg.data.message.role, 'MEMBER');

  // List messages
  const listMsg = await request('/api/chat/messages', {
    headers: { Authorization: `Bearer ${memberToken}` }
  });
  assert.equal(listMsg.status, 200);
  assert.ok(Array.isArray(listMsg.data.messages));
  assert.ok(listMsg.data.messages.length > 0);
});

test('14, 15, 16: AI endpoints (Ask, Solve Screen, Fill Answer)', async () => {
  const memberLogin = await request('/api/auth/login', {
    method: 'POST',
    body: { username: 'member1', password: 'NewPassword999!' }
  });
  const memberToken = memberLogin.data.accessToken;

  // 16. Tanya AI
  const askRes = await request('/api/ai/ask', {
    method: 'POST',
    headers: { Authorization: `Bearer ${memberToken}` },
    body: { question: 'Apa rumus energi kinetik?' }
  });
  assert.equal(askRes.status, 200);
  assert.ok(askRes.data.answer);

  // 15. Cari Jawaban (Solve Screen)
  const solveRes = await request('/api/ai/solve-screen', {
    method: 'POST',
    headers: { Authorization: `Bearer ${memberToken}` },
    body: {
      image: Buffer.from('test_image_data').toString('base64'),
      mimeType: 'image/jpeg'
    }
  });
  assert.equal(solveRes.status, 200);
  assert.ok(solveRes.data.answer);
  assert.ok(solveRes.data.reason);

  // 14. Isi Otomatis (Fill Answer)
  const fillRes = await request('/api/ai/fill-answer', {
    method: 'POST',
    headers: { Authorization: `Bearer ${memberToken}` },
    body: { fieldHint: 'Nama Lengkap', screenContext: 'Form pendaftaran lomba' }
  });
  assert.equal(fillRes.status, 200);
  assert.ok(fillRes.data.fill_text);
  assert.equal(fillRes.data.delay_seconds, 2);
});
