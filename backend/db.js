const mysql = require('mysql2/promise');
require('dotenv').config();

// Wakanda Higher Secondary School Database Connection Pool
const hssPool = mysql.createPool({
  host: process.env.HSS_DB_HOST || 'localhost',
  user: process.env.HSS_DB_USER || 'root',
  password: process.env.HSS_DB_PASS !== undefined ? process.env.HSS_DB_PASS : '',
  database: process.env.HSS_DB_NAME || 'higher_secondary_db',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

module.exports = { hssPool, pool: hssPool };
