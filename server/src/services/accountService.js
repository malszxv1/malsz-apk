const crypto = require('crypto');
const bcrypt = require('bcryptjs');
const userRepository = require('../repositories/userRepository');
const tokenRepository = require('../repositories/tokenRepository');
const auditRepository = require('../repositories/auditRepository');
const ownerConfig = require('../../owner.json');

const accountService = {
  async createUser(actor, { username, password, role, expirationDays }) {
    // 1. Validate username
    if (!username || typeof username !== 'string' || username.trim().length < 3 || username.trim().length > 32) {
      const err = new Error('Username must be 3 to 32 alphanumeric characters');
      err.statusCode = 400;
      throw err;
    }
    const sanitizedUsername = username.trim();

    // 2. Validate password
    if (!password || typeof password !== 'string' || password.length < 6) {
      const err = new Error('Password must be at least 6 characters');
      err.statusCode = 400;
      throw err;
    }

    // 3. Validate Role based on Actor
    let assignedRole = role ? role.toUpperCase() : 'MEMBER';
    if (actor.role === 'RESELLER') {
      // RESELLER cannot choose role; always MEMBER
      assignedRole = 'MEMBER';
    } else if (actor.role === 'ADMIN') {
      if (!['RESELLER', 'MEMBER'].includes(assignedRole)) {
        const err = new Error('Admins can only create MEMBER or RESELLER accounts');
        err.statusCode = 403;
        throw err;
      }
    } else if (actor.role === 'OWNER') {
      if (!['ADMIN', 'RESELLER', 'MEMBER'].includes(assignedRole)) {
        const err = new Error('Owner can create ADMIN, RESELLER, or MEMBER');
        err.statusCode = 400;
        throw err;
      }
    } else {
      const err = new Error('Unauthorized to create accounts');
      err.statusCode = 403;
      throw err;
    }

    // 4. Check unique username
    const existing = userRepository.findByUsername(sanitizedUsername);
    if (existing) {
      const err = new Error('Username already exists');
      err.statusCode = 409;
      throw err;
    }

    // 5. Calculate expiration
    let expiresAt = null;
    const maxResellerDays = ownerConfig.bootstrap?.maxResellerAccountDays || 365;

    if (expirationDays === 'LIFETIME' || expirationDays === 0 || expirationDays === null || expirationDays === 'lifetime') {
      if (actor.role === 'RESELLER' && !ownerConfig.bootstrap?.allowLifetimeAccounts) {
        const err = new Error('Resellers are not permitted to create lifetime accounts');
        err.statusCode = 403;
        throw err;
      }
      expiresAt = null; // Lifetime
    } else {
      const days = parseInt(expirationDays, 10);
      if (isNaN(days) || days <= 0) {
        const err = new Error('Invalid expiration days value');
        err.statusCode = 400;
        throw err;
      }
      if (actor.role === 'RESELLER' && days > maxResellerDays) {
        const err = new Error(`Reseller cannot grant more than ${maxResellerDays} days`);
        err.statusCode = 403;
        throw err;
      }
      const expDate = new Date();
      expDate.setDate(expDate.getDate() + days);
      expiresAt = expDate.toISOString();
    }

    // 6. Hash password
    const salt = bcrypt.genSaltSync(10);
    const passwordHash = bcrypt.hashSync(password, salt);
    const newId = crypto.randomUUID();

    const created = userRepository.createUser({
      id: newId,
      username: sanitizedUsername,
      passwordHash,
      role: assignedRole,
      status: 'ACTIVE',
      createdBy: actor.id,
      expiresAt
    });

    // 7. Audit log
    auditRepository.log({
      actorId: actor.id,
      targetUserId: newId,
      action: 'USER_CREATED',
      reason: `Created ${assignedRole} account`,
      metadata: { role: assignedRole, expires_at: expiresAt, created_by: actor.username }
    });

    return {
      id: created.id,
      username: created.username,
      role: created.role,
      status: created.status,
      created_by: created.created_by,
      created_at: created.created_at,
      expires_at: created.expires_at
    };
  },

  async banUser(actor, targetUser, reason) {
    if (targetUser.role === 'OWNER') {
      const err = new Error('Owner accounts cannot be banned');
      err.statusCode = 403;
      throw err;
    }
    if (targetUser.id === actor.id) {
      const err = new Error('Cannot ban your own account');
      err.statusCode = 400;
      throw err;
    }

    const banReason = reason || 'Violation of service terms';
    const now = new Date().toISOString();

    const updated = userRepository.updateStatus(targetUser.id, 'BANNED', now, banReason);
    tokenRepository.revokeAllForUser(targetUser.id);

    auditRepository.log({
      actorId: actor.id,
      targetUserId: targetUser.id,
      action: 'USER_BANNED',
      reason: banReason,
      metadata: { previousStatus: targetUser.status }
    });

    return updated;
  },

  async unbanUser(actor, targetUser, reason) {
    if (targetUser.status !== 'BANNED') {
      const err = new Error('User is not banned');
      err.statusCode = 400;
      throw err;
    }

    let nextStatus = 'ACTIVE';
    // Check if expired
    if (targetUser.expires_at && new Date(targetUser.expires_at).getTime() <= Date.now()) {
      nextStatus = 'EXPIRED';
    }

    const updated = userRepository.updateStatus(targetUser.id, nextStatus, null, null);

    auditRepository.log({
      actorId: actor.id,
      targetUserId: targetUser.id,
      action: 'USER_UNBANNED',
      reason: reason || 'Account unbanned by administrator',
      metadata: { restoredStatus: nextStatus }
    });

    return updated;
  },

  async changePassword(userId, oldPassword, newPassword) {
    if (!newPassword || newPassword.length < 6) {
      const err = new Error('New password must be at least 6 characters long');
      err.statusCode = 400;
      throw err;
    }

    const user = userRepository.findById(userId);
    if (!user) {
      const err = new Error('User not found');
      err.statusCode = 404;
      throw err;
    }

    const isValid = bcrypt.compareSync(oldPassword, user.password_hash);
    if (!isValid) {
      const err = new Error('Incorrect current password');
      err.statusCode = 401;
      throw err;
    }

    const salt = bcrypt.genSaltSync(10);
    const newHash = bcrypt.hashSync(newPassword, salt);
    userRepository.updatePassword(userId, newHash);
    tokenRepository.revokeAllForUser(userId);

    auditRepository.log({
      actorId: userId,
      targetUserId: userId,
      action: 'PASSWORD_CHANGED',
      reason: 'User self password reset',
      metadata: {}
    });

    return true;
  },

  async deleteUser(actor, targetUser) {
    if (targetUser.role === 'OWNER') {
      const allOwners = userRepository.listUsers({ role: 'OWNER' }).users;
      if (allOwners.length <= 1) {
        const err = new Error('Cannot delete sole system OWNER');
        err.statusCode = 403;
        throw err;
      }
    }

    tokenRepository.revokeAllForUser(targetUser.id);
    userRepository.deleteUser(targetUser.id);

    auditRepository.log({
      actorId: actor.id,
      targetUserId: targetUser.id,
      action: 'USER_DELETED',
      reason: `Account removed by ${actor.username}`,
      metadata: { deletedUsername: targetUser.username, deletedRole: targetUser.role }
    });

    return true;
  }
};

module.exports = accountService;
