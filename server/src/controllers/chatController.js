const chatRepository = require('../repositories/chatRepository');
const auditRepository = require('../repositories/auditRepository');

const chatController = {
  async listMessages(req, res, next) {
    try {
      const { limit = 50, before = null } = req.query;
      const parsedLimit = Math.min(Math.max(parseInt(limit, 10) || 50, 1), 100);
      const messages = chatRepository.listMessages({ limit: parsedLimit, before });
      return res.json({ messages });
    } catch (err) {
      next(err);
    }
  },

  async createMessage(req, res, next) {
    try {
      const { message } = req.body;
      if (!message || typeof message !== 'string' || message.trim().length === 0) {
        return res.status(400).json({ error: 'Message cannot be empty' });
      }
      if (message.trim().length > 500) {
        return res.status(400).json({ error: 'Message exceeds maximum limit of 500 characters' });
      }

      const created = chatRepository.createMessage({
        userId: req.user.id,
        message: message.trim()
      });

      return res.status(201).json({
        message: created
      });
    } catch (err) {
      next(err);
    }
  },

  async deleteMessage(req, res, next) {
    try {
      const msgId = req.params.id;
      const msg = chatRepository.findById(msgId);
      if (!msg) {
        return res.status(404).json({ error: 'Message not found' });
      }

      // Can delete if owner, admin, or author
      const canDelete = ['OWNER', 'ADMIN'].includes(req.user.role) || msg.user_id === req.user.id;
      if (!canDelete) {
        return res.status(403).json({ error: 'Not authorized to delete this message' });
      }

      chatRepository.softDelete(msgId);

      auditRepository.log({
        actorId: req.user.id,
        targetUserId: msg.user_id,
        action: 'CHAT_MESSAGE_DELETED',
        reason: 'Moderation or author deletion',
        metadata: { messageId: msgId }
      });

      return res.json({ message: 'Message deleted' });
    } catch (err) {
      next(err);
    }
  },

  async reportMessage(req, res, next) {
    try {
      const msgId = req.params.id;
      const { reason = 'Inappropriate content' } = req.body;
      const msg = chatRepository.findById(msgId);
      if (!msg) {
        return res.status(404).json({ error: 'Message not found' });
      }

      auditRepository.log({
        actorId: req.user.id,
        targetUserId: msg.user_id,
        action: 'CHAT_MESSAGE_REPORTED',
        reason,
        metadata: { messageId: msgId, snippet: msg.message.slice(0, 50) }
      });

      return res.json({ message: 'Message reported for moderation review' });
    } catch (err) {
      next(err);
    }
  }
};

module.exports = chatController;
