require('dotenv').config();
const fs = require('fs');
const path = require('path');
const db = require('./connection');

function runMigrations() {
  const migrationsDir = path.join(__dirname, 'migrations');
  const files = fs.readdirSync(migrationsDir).filter(f => f.endsWith('.sql')).sort();

  db.exec(`
    CREATE TABLE IF NOT EXISTS migrations (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT UNIQUE NOT NULL,
      applied_at TEXT NOT NULL
    );
  `);

  const appliedList = db.prepare('SELECT name FROM migrations').all().map(r => r.name);

  for (const file of files) {
    if (!appliedList.includes(file)) {
      console.log(`[MIGRATE] Applying migration: ${file}`);
      const sql = fs.readFileSync(path.join(migrationsDir, file), 'utf8');
      db.transaction(() => {
        db.exec(sql);
        db.prepare('INSERT INTO migrations (name, applied_at) VALUES (?, ?)').run(
          file,
          new Date().toISOString()
        );
      })();
      console.log(`[MIGRATE] Applied migration: ${file}`);
    }
  }
}

if (require.main === module) {
  try {
    runMigrations();
    console.log('[MIGRATE] All migrations applied successfully.');
    process.exit(0);
  } catch (err) {
    console.error('[MIGRATE ERROR]', err);
    process.exit(1);
  }
}

module.exports = { runMigrations };
