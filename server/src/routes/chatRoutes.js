const express = require('express');
const router = express.Router();
const chatController = require('../controllers/chatController');
const { authenticate } = require('../middleware/auth');

router.use(authenticate);

router.get('/messages', chatController.listMessages);
router.post('/messages', chatController.createMessage);
router.delete('/messages/:id', chatController.deleteMessage);
router.post('/messages/:id/report', chatController.reportMessage);

module.exports = router;
