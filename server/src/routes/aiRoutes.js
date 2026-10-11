const express = require('express');
const router = express.Router();
const multer = require('multer');
const aiController = require('../controllers/aiController');
const { authenticate } = require('../middleware/auth');

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 15 * 1024 * 1024 } // 15MB limit
});

router.use(authenticate);

router.post('/ask', aiController.ask);
router.post('/solve-screen', upload.single('screenshot'), aiController.solveScreen);
router.post('/fill-answer', aiController.fillAnswer);

module.exports = router;
