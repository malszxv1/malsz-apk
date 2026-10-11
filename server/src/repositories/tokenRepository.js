const db = require('../database/connection');
const crypto = require('crypto');

const tokenRepository = {
  createRefreshToken({ userId, tokenHash, expiresAt }) {
    const id = crypto.randomUUID();
    const now = new Date().toISOString();
    db.prepare(`
      INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, created_at)
      VALUES (?, ?, ?, ?, ?)
    `).run(id, userId, tokenHash, expiresAt, now);
    return id;
  },

  findValidToken(tokenHash) {
    const now = new Date().toISOString();
    return db.prepare(`
      SELECT * FROM refresh_tokens
      WHERE token_hash = ? AND revoked_at IS NULL AND expires_at > ?
    `).get(tokenHash, now);
  },

  revokeToken(tokenHash) {
    const now = new Date().toISOString();
    return db.prepare(`
      UPDATE refresh_tokens
      SET revoked_at = ?
      WHERE token_hash = ?
    `).run(now, tokenHash);
  },

  revokeAllForUser(userId) {
    const now = new Date().toISOString();
    return db.prepare(`
      UPDATE refresh_tokens
      SET revoked_at = ?
      WHERE user_id = ? AND revoked_at IS NULL
    `).run(now, userId);
  }
};

module.exports = tokenRepository;
