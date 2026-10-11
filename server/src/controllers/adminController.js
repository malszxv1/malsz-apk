const userRepository = require('../repositories/userRepository');
const accountService = require('../services/accountService');
const auditRepository = require('../repositories/auditRepository');

const adminController = {
  async createUser(req, res, next) {
    try {
      const { username, password, role, expirationDays } = req.body;
      const created = await accountService.createUser(req.user, {
        username,
        password,
        role,
        expirationDays
      });
      return res.status(201).json({
        message: 'Account created successfully',
        user: created
      });
    } catch (err) {
      next(err);
    }
  },

  async listUsers(req, res, next) {
    try {
      const { search, role, status, page = 1, limit = 20 } = req.query;
      const parsedLimit = Math.min(Math.max(parseInt(limit, 10) || 20, 1), 100);
      const parsedPage = Math.max(parseInt(page, 10) || 1, 1);
      const offset = (parsedPage - 1) * parsedLimit;

      // Resellers can only see users created by themselves
      let createdBy = null;
      if (req.user.role === 'RESELLER') {
        createdBy = req.user.id;
      }

      const result = userRepository.listUsers({
        search,
        role,
        status,
        createdBy,
        limit: parsedLimit,
        offset
      });

      return res.json({
        users: result.users,
        pagination: {
          total: result.total,
          page: parsedPage,
          limit: parsedLimit,
          totalPages: Math.ceil(result.total / parsedLimit)
        }
      });
    } catch (err) {
      next(err);
    }
  },

  async getUser(req, res, next) {
    try {
      const user = req.targetUser;
      return res.json({
        id: user.id,
        username: user.username,
        role: user.role,
        status: user.status,
        created_by: user.created_by,
        created_at: user.created_at,
        expires_at: user.expires_at,
        banned_at: user.banned_at,
        ban_reason: user.ban_reason,
        updated_at: user.updated_at
      });
    } catch (err) {
      next(err);
    }
  },

  async banUser(req, res, next) {
    try {
      const { reason } = req.body;
      const updated = await accountService.banUser(req.user, req.targetUser, reason);
      return res.json({
        message: `User ${req.targetUser.username} has been banned`,
        user: {
          id: updated.id,
          username: updated.username,
          status: updated.status,
          banned_at: updated.banned_at,
          ban_reason: updated.ban_reason
        }
      });
    } catch (err) {
      next(err);
    }
  },

  async unbanUser(req, res, next) {
    try {
      const { reason } = req.body;
      const updated = await accountService.unbanUser(req.user, req.targetUser, reason);
      return res.json({
        message: `User ${req.targetUser.username} unbanned`,
        user: {
          id: updated.id,
          username: updated.username,
          status: updated.status,
          expires_at: updated.expires_at
        }
      });
    } catch (err) {
      next(err);
    }
  },

  async updateUser(req, res, next) {
    try {
      const { role, status, expires_at } = req.body;
      if (req.user.role === 'RESELLER' && role && role !== 'MEMBER') {
        return res.status(403).json({ error: 'Resellers cannot change role' });
      }

      const updated = userRepository.updateUser(req.targetUser.id, { role, status, expires_at });
      return res.json({
        message: 'User updated',
        user: updated
      });
    } catch (err) {
      next(err);
    }
  },

  async deleteUser(req, res, next) {
    try {
      await accountService.deleteUser(req.user, req.targetUser);
      return res.json({ message: `User ${req.targetUser.username} deleted` });
    } catch (err) {
      next(err);
    }
  },

  async getStats(req, res, next) {
    try {
      const createdBy = req.user.role === 'RESELLER' ? req.user.id : null;
      const stats = userRepository.getStats(createdBy);
      return res.json(stats);
    } catch (err) {
      next(err);
    }
  },

  async getAuditLogs(req, res, next) {
    try {
      if (req.user.role !== 'OWNER' && req.user.role !== 'ADMIN') {
        return res.status(403).json({ error: 'Only Owner and Admin can view audit logs' });
      }
      const logs = auditRepository.listLogs({ limit: 100 });
      return res.json({ logs });
    } catch (err) {
      next(err);
    }
  }
};

module.exports = adminController;
