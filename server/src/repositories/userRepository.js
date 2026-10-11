const db = require('../database/connection');

const userRepository = {
  findById(id) {
    return db.prepare('SELECT * FROM users WHERE id = ?').get(id);
  },

  findByUsername(username) {
    return db.prepare('SELECT * FROM users WHERE username = ? COLLATE NOCASE').get(username);
  },

  createUser({ id, username, passwordHash, role, status, createdBy, expiresAt }) {
    const now = new Date().toISOString();
    db.prepare(`
      INSERT INTO users (id, username, password_hash, role, status, created_by, created_at, expires_at, updated_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).run(id, username, passwordHash, role, status, createdBy, now, expiresAt, now);
    return this.findById(id);
  },

  updateStatus(id, status, bannedAt = null, banReason = null) {
    const now = new Date().toISOString();
    db.prepare(`
      UPDATE users
      SET status = ?, banned_at = ?, ban_reason = ?, updated_at = ?
      WHERE id = ?
    `).run(status, bannedAt, banReason, now, id);
    return this.findById(id);
  },

  updatePassword(id, passwordHash) {
    const now = new Date().toISOString();
    db.prepare(`
      UPDATE users
      SET password_hash = ?, updated_at = ?
      WHERE id = ?
    `).run(passwordHash, now, id);
    return this.findById(id);
  },

  updateUser(id, updates) {
    const fields = [];
    const values = [];
    for (const [key, val] of Object.entries(updates)) {
      if (['role', 'status', 'expires_at'].includes(key)) {
        fields.push(`${key} = ?`);
        values.push(val);
      }
    }
    if (fields.length === 0) return this.findById(id);

    fields.push('updated_at = ?');
    values.push(new Date().toISOString());
    values.push(id);

    db.prepare(`UPDATE users SET ${fields.join(', ')} WHERE id = ?`).run(...values);
    return this.findById(id);
  },

  deleteUser(id) {
    return db.prepare('DELETE FROM users WHERE id = ?').run(id);
  },

  listUsers({ search, role, status, createdBy, limit = 50, offset = 0 }) {
    let query = `
      SELECT u.id, u.username, u.role, u.status, u.created_by, u.created_at, u.expires_at, u.banned_at, u.ban_reason, u.updated_at,
             c.username as creator_username
      FROM users u
      LEFT JOIN users c ON u.created_by = c.id
      WHERE 1=1
    `;
    const params = [];

    if (search) {
      query += ' AND u.username LIKE ?';
      params.push(`%${search}%`);
    }
    if (role) {
      query += ' AND u.role = ?';
      params.push(role);
    }
    if (status) {
      query += ' AND u.status = ?';
      params.push(status);
    }
    if (createdBy) {
      query += ' AND u.created_by = ?';
      params.push(createdBy);
    }

    query += ' ORDER BY u.created_at DESC LIMIT ? OFFSET ?';
    params.push(limit, offset);

    const rows = db.prepare(query).all(...params);

    // Count query
    let countQuery = 'SELECT COUNT(*) as total FROM users u WHERE 1=1';
    const countParams = [];
    if (search) {
      countQuery += ' AND u.username LIKE ?';
      countParams.push(`%${search}%`);
    }
    if (role) {
      countQuery += ' AND u.role = ?';
      countParams.push(role);
    }
    if (status) {
      countQuery += ' AND u.status = ?';
      countParams.push(status);
    }
    if (createdBy) {
      countQuery += ' AND u.created_by = ?';
      countParams.push(createdBy);
    }

    const total = db.prepare(countQuery).get(...countParams).total;

    return { users: rows, total, limit, offset };
  },

  getStats(createdBy = null) {
    let whereClause = createdBy ? 'WHERE created_by = ?' : '';
    const params = createdBy ? [createdBy] : [];

    const totalUsers = db.prepare(`SELECT COUNT(*) as count FROM users ${whereClause}`).get(...params).count;
    const activeUsers = db.prepare(`SELECT COUNT(*) as count FROM users ${whereClause ? whereClause + ' AND' : 'WHERE'} status = 'ACTIVE'`).get(...params).count;
    const bannedUsers = db.prepare(`SELECT COUNT(*) as count FROM users ${whereClause ? whereClause + ' AND' : 'WHERE'} status = 'BANNED'`).get(...params).count;
    const expiredUsers = db.prepare(`SELECT COUNT(*) as count FROM users ${whereClause ? whereClause + ' AND' : 'WHERE'} status = 'EXPIRED'`).get(...params).count;

    return { totalUsers, activeUsers, bannedUsers, expiredUsers };
  }
};

module.exports = userRepository;
