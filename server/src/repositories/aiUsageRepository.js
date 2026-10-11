const db = require('../database/connection');
const crypto = require('crypto');

const aiUsageRepository = {
  log({ userId, actionType, provider, status, metadata = {} }) {
    const id = crypto.randomUUID();
    const now = new Date().toISOString();
    db.prepare(`
      INSERT INTO ai_usage_logs (id, user_id, action_type, provider, status, usage_metadata, created_at)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `).run(id, userId, actionType, provider, status, JSON.stringify(metadata), now);
    return id;
  },

  getRecentLogs(userId, limit = 20) {
    return db.prepare(`
      SELECT * FROM ai_usage_logs
      WHERE user_id = ?
      ORDER BY created_at DESC
      LIMIT ?
    `).all(userId, limit);
  }
};

module.exports = aiUsageRepository;
