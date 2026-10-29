const mysql = require('mysql2/promise');
require('dotenv').config();

// Yashoda College of Arts, Science and Commerce (YCASC) Database Connection Pool
const collegePool = mysql.createPool({
  host: process.env.COLLEGE_DB_HOST || process.env.HSS_DB_HOST || 'localhost',
  user: process.env.COLLEGE_DB_USER || process.env.HSS_DB_USER || 'root',
  password: process.env.COLLEGE_DB_PASS !== undefined ? process.env.COLLEGE_DB_PASS : (process.env.HSS_DB_PASS !== undefined ? process.env.HSS_DB_PASS : ''),
  database: process.env.COLLEGE_DB_NAME || 'yashoda_college_db',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

module.exports = { collegePool, hssPool: collegePool, pool: collegePool };
