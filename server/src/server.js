require('dotenv').config();
const app = require('./app');
const { runMigrations } = require('./database/migrate');
const { seedOwner } = require('../scripts/seedOwner');

const PORT = process.env.APP_PORT || 5000;

try {
  // 1. Ensure migrations are up-to-date
  runMigrations();
  // 2. Ensure initial owner is bootstrapped
  seedOwner();

  const server = app.listen(PORT, '0.0.0.0', () => {
    console.log(`[MALSZ SERVER] Server running on http://0.0.0.0:${PORT}`);
    console.log(`[MALSZ SERVER] Status: ONLINE | Environment: ${process.env.NODE_ENV || 'development'}`);
  });

  const shutdown = () => {
    console.log('[MALSZ SERVER] Gracefully shutting down...');
    server.close(() => {
      console.log('[MALSZ SERVER] HTTP server closed.');
      process.exit(0);
    });
  };

  process.on('SIGINT', shutdown);
  process.on('SIGTERM', shutdown);
} catch (err) {
  console.error('[MALSZ SERVER FATAL]', err);
  process.exit(1);
}
