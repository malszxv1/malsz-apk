const db = require('../database/connection');
const crypto = require('crypto');

const chatRepository = {
  createMessage({ userId, message }) {
    const id = crypto.randomUUID();
    const now = new Date().toISOString();
    db.prepare(`
      INSERT INTO global_messages (id, user_id, message, created_at)
      VALUES (?, ?, ?, ?)
    `).run(id, userId, message, now);
    return this.findById(id);
  },

  findById(id) {
    return db.prepare(`
      SELECT m.id, m.user_id, m.message, m.created_at, m.deleted_at,
             u.username, u.role, u.status
      FROM global_messages m
      JOIN users u ON m.user_id = u.id
      WHERE m.id = ?
    `).get(id);
  },

  listMessages({ limit = 50, before = null }) {
    let query = `
      SELECT m.id, m.user_id, m.message, m.created_at, m.deleted_at,
             u.username, u.role, u.status
      FROM global_messages m
      JOIN users u ON m.user_id = u.id
      WHERE m.deleted_at IS NULL
    `;
    const params = [];

    if (before) {
      query += ' AND m.created_at < ?';
      params.push(before);
    }

    query += ' ORDER BY m.created_at DESC LIMIT ?';
    params.push(limit);

    const rows = db.prepare(query).all(...params);
    return rows.reverse(); // Return in chronological order
  },

  softDelete(id) {
    const now = new Date().toISOString();
    return db.prepare(`
      UPDATE global_messages
      SET deleted_at = ?
      WHERE id = ?
    `).run(now, id);
  }
};

module.exports = chatRepository;
