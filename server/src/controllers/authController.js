const bcrypt = require('bcryptjs');
const userRepository = require('../repositories/userRepository');
const tokenRepository = require('../repositories/tokenRepository');
const { generateAccessToken, generateRefreshToken, hashToken } = require('../config/jwt');
const accountService = require('../services/accountService');

const authController = {
  async login(req, res, next) {
    try {
      const { username, password } = req.body;
      if (!username || !password) {
        return res.status(400).json({ error: 'Username and password are required' });
      }

      const user = userRepository.findByUsername(username);
      if (!user) {
        return res.status(401).json({ error: 'Invalid username or password' });
      }

      const match = bcrypt.compareSync(password, user.password_hash);
      if (!match) {
        return res.status(401).json({ error: 'Invalid username or password' });
      }

      // 1. Check if BANNED
      if (user.status === 'BANNED') {
        return res.status(403).json({
          error: 'Your account has been banned',
          banned_at: user.banned_at,
          reason: user.ban_reason || 'Violation of service terms'
        });
      }

      // 2. Check if EXPIRED
      if (user.expires_at) {
        if (new Date(user.expires_at).getTime() <= Date.now()) {
          userRepository.updateStatus(user.id, 'EXPIRED');
          return res.status(403).json({
            error: 'Your account active period has expired',
            expires_at: user.expires_at
          });
        }
      }

      const accessToken = generateAccessToken(user);
      const { token: refreshToken, tokenHash, expiresAt } = generateRefreshToken();

      tokenRepository.createRefreshToken({
        userId: user.id,
        tokenHash,
        expiresAt
      });

      return res.json({
        message: 'Login successful',
        accessToken,
        refreshToken,
        user: {
          id: user.id,
          username: user.username,
          role: user.role,
          status: user.status,
          created_at: user.created_at,
          expires_at: user.expires_at
        }
      });
    } catch (err) {
      next(err);
    }
  },

  async refresh(req, res, next) {
    try {
      const { refreshToken } = req.body;
      if (!refreshToken) {
        return res.status(400).json({ error: 'Refresh token is required' });
      }

      const tokenHash = hashToken(refreshToken);
      const validToken = tokenRepository.findValidToken(tokenHash);
      if (!validToken) {
        return res.status(401).json({ error: 'Invalid, revoked, or expired refresh token' });
      }

      const user = userRepository.findById(validToken.user_id);
      if (!user || user.status === 'BANNED') {
        return res.status(403).json({ error: 'Account cannot be refreshed' });
      }

      if (user.expires_at && new Date(user.expires_at).getTime() <= Date.now()) {
        userRepository.updateStatus(user.id, 'EXPIRED');
        return res.status(403).json({ error: 'Account has expired' });
      }

      const newAccessToken = generateAccessToken(user);
      return res.json({
        accessToken: newAccessToken
      });
    } catch (err) {
      next(err);
    }
  },

  async logout(req, res, next) {
    try {
      const { refreshToken } = req.body;
      if (refreshToken) {
        const tokenHash = hashToken(refreshToken);
        tokenRepository.revokeToken(tokenHash);
      }
      return res.json({ message: 'Successfully logged out' });
    } catch (err) {
      next(err);
    }
  },

  async changePassword(req, res, next) {
    try {
      const { oldPassword, newPassword } = req.body;
      if (!oldPassword || !newPassword) {
        return res.status(400).json({ error: 'Old password and new password are required' });
      }

      await accountService.changePassword(req.user.id, oldPassword, newPassword);
      return res.json({ message: 'Password changed successfully. Please log in again.' });
    } catch (err) {
      next(err);
    }
  }
};

module.exports = authController;
