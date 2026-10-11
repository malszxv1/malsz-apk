require('dotenv').config();
const crypto = require('crypto');
const bcrypt = require('bcryptjs');
const db = require('../src/database/connection');
const { runMigrations } = require('../src/database/migrate');
const ownerConfig = require('../owner.json');

function seedOwner() {
  runMigrations();

  const ownerUsername = ownerConfig.bootstrap?.defaultOwnerUsername || 'malsz';
  const existingOwner = db.prepare('SELECT id, username, role, status FROM users WHERE username = ?').get(ownerUsername);

  if (existingOwner) {
    console.log(`[BOOTSTRAP] Owner '${ownerUsername}' already exists with role ${existingOwner.role}. Skipping creation.`);
    return existingOwner;
  }

  const rawPassword = process.env.OWNER_PASSWORD || 'MalszCyber2026!';
  const salt = bcrypt.genSaltSync(10);
  const passwordHash = bcrypt.hashSync(rawPassword, salt);
  const now = new Date().toISOString();
  const id = crypto.randomUUID();

  db.prepare(`
    INSERT INTO users (id, username, password_hash, role, status, created_by, created_at, expires_at, updated_at)
    VALUES (?, ?, ?, 'OWNER', 'ACTIVE', NULL, ?, NULL, ?)
  `).run(id, ownerUsername, passwordHash, now, now);

  db.prepare(`
    INSERT INTO audit_logs (id, actor_id, target_user_id, action, reason, metadata_json, created_at)
    VALUES (?, ?, ?, 'SYSTEM_BOOTSTRAP', 'Initial system owner initialized', ?, ?)
  `).run(
    crypto.randomUUID(),
    id,
    id,
    JSON.stringify({ username: ownerUsername, role: 'OWNER' }),
    now
  );

  console.log(`[BOOTSTRAP] Successfully created initial OWNER: '${ownerUsername}' (id: ${id})`);
  return { id, username: ownerUsername, role: 'OWNER' };
}

if (require.main === module) {
  try {
    seedOwner();
    process.exit(0);
  } catch (err) {
    console.error('[BOOTSTRAP ERROR]', err);
    process.exit(1);
  }
}

module.exports = { seedOwner };
