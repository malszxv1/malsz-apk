const { verifyAccessToken } = require('../config/jwt');
const userRepository = require('../repositories/userRepository');

function authenticate(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Missing or invalid authorization header' });
  }

  const token = authHeader.split(' ')[1];
  const payload = verifyAccessToken(token);
  if (!payload || !payload.id) {
    return res.status(401).json({ error: 'Invalid or expired access token' });
  }

  const user = userRepository.findById(payload.id);
  if (!user) {
    return res.status(401).json({ error: 'User account not found' });
  }

  // 1. Check if user is BANNED
  if (user.status === 'BANNED') {
    return res.status(403).json({
      error: 'Account is banned',
      banned_at: user.banned_at,
      reason: user.ban_reason || 'Violation of terms'
    });
  }

  // 2. Check if user has EXPIRED
  if (user.expires_at) {
    const expiryTime = new Date(user.expires_at).getTime();
    const nowTime = Date.now();
    if (expiryTime <= nowTime) {
      if (user.status !== 'EXPIRED') {
        userRepository.updateStatus(user.id, 'EXPIRED');
        user.status = 'EXPIRED';
      }
      return res.status(403).json({
        error: 'Account has expired',
        expires_at: user.expires_at
      });
    }
  }

  req.user = {
    id: user.id,
    username: user.username,
    role: user.role,
    status: user.status,
    created_by: user.created_by,
    created_at: user.created_at,
    expires_at: user.expires_at
  };

  next();
}

module.exports = { authenticate };
