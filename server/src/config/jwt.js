const jwt = require('jsonwebtoken');
const crypto = require('crypto');

const JWT_SECRET = process.env.JWT_SECRET || 'malsz_cyber_jwt_secret_change_in_production_key_2026';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'malsz_cyber_jwt_refresh_secret_change_in_production_2026';
const ACCESS_EXPIRY = '60m';
const REFRESH_EXPIRY_DAYS = 30;

function generateAccessToken(user) {
  return jwt.sign(
    {
      id: user.id,
      username: user.username,
      role: user.role
    },
    JWT_SECRET,
    { expiresIn: ACCESS_EXPIRY }
  );
}

function generateRefreshToken() {
  const token = crypto.randomBytes(40).toString('hex');
  const tokenHash = crypto.createHash('sha256').update(token).digest('hex');
  const expiresAt = new Date(Date.now() + REFRESH_EXPIRY_DAYS * 24 * 60 * 60 * 1000).toISOString();
  return { token, tokenHash, expiresAt };
}

function verifyAccessToken(token) {
  try {
    return jwt.verify(token, JWT_SECRET);
  } catch (err) {
    return null;
  }
}

function hashToken(token) {
  return crypto.createHash('sha256').update(token).digest('hex');
}

module.exports = {
  generateAccessToken,
  generateRefreshToken,
  verifyAccessToken,
  hashToken,
  ACCESS_EXPIRY
};
