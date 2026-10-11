const aiService = require('../services/aiService');

const aiController = {
  async ask(req, res, next) {
    try {
      const { question, history } = req.body;
      if (!question || typeof question !== 'string') {
        return res.status(400).json({ error: 'Question is required' });
      }

      const result = await aiService.ask(req.user.id, question.trim(), history);
      return res.json(result);
    } catch (err) {
      next(err);
    }
  },

  async solveScreen(req, res, next) {
    try {
      let imageBase64 = '';
      let mimeType = 'image/jpeg';

      if (req.file) {
        imageBase64 = req.file.buffer.toString('base64');
        mimeType = req.file.mimetype || 'image/jpeg';
      } else if (req.body && req.body.image) {
        imageBase64 = req.body.image;
        if (req.body.mimeType) mimeType = req.body.mimeType;
      }

      if (!imageBase64) {
        return res.status(400).json({ error: 'Screenshot image data is required (file or base64)' });
      }

      const result = await aiService.solveScreen(req.user.id, imageBase64, mimeType);
      return res.json(result);
    } catch (err) {
      next(err);
    }
  },

  async fillAnswer(req, res, next) {
    try {
      const { fieldHint, screenContext } = req.body;
      const result = await aiService.fillAnswer(req.user.id, fieldHint, screenContext);
      return res.json(result);
    } catch (err) {
      next(err);
    }
  }
};

module.exports = aiController;
