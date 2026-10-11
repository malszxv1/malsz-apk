const userRepository = require('../repositories/userRepository');
const accountService = require('../services/accountService');

const userController = {
  async getMe(req, res, next) {
    try {
      const user = userRepository.findById(req.user.id);
      if (!user) return res.status(404).json({ error: 'User not found' });

      return res.json({
        id: user.id,
        username: user.username,
        role: user.role,
        status: user.status,
        created_at: user.created_at,
        expires_at: user.expires_at,
        banned_at: user.banned_at,
        ban_reason: user.ban_reason
      });
    } catch (err) {
      next(err);
    }
  },

  async updateMe(req, res, next) {
    try {
      return res.json({ message: 'Profile updated' });
    } catch (err) {
      next(err);
    }
  },

  async deleteMe(req, res, next) {
    try {
      const user = userRepository.findById(req.user.id);
      if (!user) return res.status(404).json({ error: 'User not found' });

      await accountService.deleteUser(req.user, user);
      return res.json({ message: 'Account deleted successfully' });
    } catch (err) {
      next(err);
    }
  }
};

module.exports = userController;
