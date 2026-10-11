const db = require('../database/connection');
const crypto = require('crypto');

const auditRepository = {
  log({ actorId, targetUserId, action, reason, metadata = {} }) {
    const id = crypto.randomUUID();
    const now = new Date().toISOString();
    db.prepare(`
      INSERT INTO audit_logs (id, actor_id, target_user_id, action, reason, metadata_json, created_at)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `).run(id, actorId, targetUserId, action, reason, JSON.stringify(metadata), now);
    return id;
  },

  listLogs({ limit = 50, offset = 0, actorId = null }) {
    let query = `
      SELECT a.id, a.actor_id, a.target_user_id, a.action, a.reason, a.metadata_json, a.created_at,
             u.username as actor_username, t.username as target_username
      FROM audit_logs a
      LEFT JOIN users u ON a.actor_id = u.id
      LEFT JOIN users t ON a.target_user_id = t.id
      WHERE 1=1
    `;
    const params = [];

    if (actorId) {
      query += ' AND a.actor_id = ?';
      params.push(actorId);
    }

    query += ' ORDER BY a.created_at DESC LIMIT ? OFFSET ?';
    params.push(limit, offset);

    return db.prepare(query).all(...params);
  }
};

module.exports = auditRepository;
