const express = require('express');
const cors = require('cors');
const { collegePool } = require('./db');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Use collegePool as the primary database connection
const db = collegePool;

// ===================================================================
// SYSTEM HEALTH & INSTITUTIONAL META ROUTES
// ===================================================================

app.get('/api/health', async (req, res) => {
  let dbStatus = 'unknown';

  try {
    const [rows] = await db.query('SELECT 1 as connected');
    dbStatus = rows && rows.length ? 'connected' : 'error';
  } catch (err) {
    dbStatus = `disconnected (${err.message})`;
  }

  res.json({
    status: 'online',
    institution: 'Yashoda College of Arts, Science and Commerce',
    acronym: 'YCASC',
    timestamp: new Date().toISOString(),
    database: {
      name: process.env.COLLEGE_DB_NAME || 'yashoda_college_db',
      status: dbStatus
    }
  });
});

app.get('/api/buyer-info', (req, res) => {
  res.json({
    institution: {
      id: 'yashoda_college',
      institutionName: 'Yashoda College of Arts, Science and Commerce',
      type: 'Degree College (UG & PG Programs)',
      database: 'yashoda_college_db',
      identifierType: 'College Stream Roll Number (e.g. SCI-26-001, COM-26-001)',
      features: [
        '3 Academic Streams: Science (B.Sc.), Commerce (B.Com), Arts (B.A.)',
        '15 Students per stream demo cohort (45 total active students)',
        'Faculty Hierarchy: Principal -> Vice Principal -> HODs (1 per stream) -> Assistant/Associate Professors',
        'Automated Course Enrollment & Attendance Tracking',
        'Campus Councils: NSS, NCC Army Wing, Cultural & Dramatics, Sports Council, Innovation Cell'
      ],
      sampleStudents: [
        'SCI-26-001 (Akash Kamat - FY B.Sc. Physics)',
        'COM-26-001 (Rohan Phadte - FY B.Com Accountancy)',
        'ART-26-001 (Kunal Shirodkar - FY B.A. History)'
      ],
      sampleTeachers: [
        'Dr. Shashikant V. Mayekar (Principal & Professor of Mathematics)',
        'Dr. Ramakant Deshmukh (Vice Principal & Dean of Academics)',
        'Prof. Pramod Naik (HOD Science & Associate Professor)',
        'Prof. Sujata Kamat (HOD Commerce & Associate Professor)',
        'Dr. Robert Vance (HOD Arts & Associate Professor)'
      ]
    }
  });
});

const passwordOverrides = new Map();

// Indian standard 10-digit default phone numbers per account
const defaultPhoneMap = {
  'ycasc_principal': '9823000001',
  'principal@ycasc.com': '9823000001',
  'ycasc_vp': '9823000002',
  'ramakant.deshmukh@ycasc.com': '9823000002',
  't-sci-101': '9823000003',
  'pramod.naik@ycasc.com': '9823000003',
  't-com-101': '9823000004',
  'hod.commerce@ycasc': '9823000004',
  't-art-101': '9823000005',
  'hod.arts@ycasc': '9823000005',
  'sci-26-001': '9823222001',
  'akash.kamat@ycasc.com': '9823222001',
  'com-26-001': '9823222016',
  'rohan.phadte@ycasc': '9823222016',
  'art-26-001': '9823222031',
  'kunal.shirodkar@ycasc': '9823222031'
};

function getDefaultPasswordForUser(cleanId) {
  if (defaultPhoneMap[cleanId]) return defaultPhoneMap[cleanId];
  return '9823222001';
}

app.post('/api/auth/login', async (req, res) => {
  const { identifier, password } = req.body;
  if (!identifier || !password) {
    return res.status(400).json({ error: 'Identifier and password are required.' });
  }

  const cleanId = identifier.trim().toLowerCase();
  const defaultPass = getDefaultPasswordForUser(cleanId);
  const effectivePassword = passwordOverrides.get(cleanId) || defaultPass;

  if (password !== effectivePassword && password !== defaultPass) {
    return res.status(401).json({ error: `Invalid password. (Default is user's 10-digit phone number: ${defaultPass})` });
  }

  res.json({
    message: 'Authentication successful',
    institution: 'Yashoda College of Arts, Science and Commerce',
    identifier: cleanId,
    emailExtension: '@ycasc',
    isDefaultPassword: effectivePassword === defaultPass
  });
});

app.post('/api/auth/reset-password', (req, res) => {
  const { identifier, currentPassword, newPassword } = req.body;
  if (!identifier || !currentPassword || !newPassword) {
    return res.status(400).json({ error: 'All fields (identifier, currentPassword, newPassword) are required.' });
  }

  const cleanId = identifier.trim().toLowerCase();
  const defaultPass = getDefaultPasswordForUser(cleanId);
  const effectivePassword = passwordOverrides.get(cleanId) || defaultPass;

  if (currentPassword !== effectivePassword && currentPassword !== defaultPass) {
    return res.status(401).json({ error: `Current password does not match. Default is 10-digit phone number: ${defaultPass}.` });
  }

  passwordOverrides.set(cleanId, newPassword);
  res.json({
    message: 'Password successfully updated for ' + cleanId,
    resetLink: 'https://ycasc.ac.in/reset-password'
  });
});

// Institutional Email Generator for College SIS
function generateOfficialEmail(firstName, lastName, rollNumber, existingEmails = new Set()) {
  const cleanFirst = (firstName || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  const cleanLast = (lastName || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  const cleanRoll = (rollNumber || '').toLowerCase().replace(/[^a-z0-9]/g, '');

  const standard = `${cleanFirst}.${cleanLast}@ycasc`;
  if (!cleanLast || cleanFirst === cleanLast || existingEmails.has(standard)) {
    if (cleanRoll) {
      return `${cleanRoll}.${cleanFirst}@ycasc`;
    }
  }
  return standard;
}

app.get('/api/admin/provisioned-accounts', async (req, res) => {
  res.json({
    provisioningPolicy: {
      institution: 'Yashoda College of Arts, Science and Commerce',
      database: 'yashoda_college_db',
      domain: '@ycasc',
      defaultPassword: 'User 10-digit Phone Number (Indian standard)',
      namingStandard: 'firstname.lastname@ycasc',
      mechanism: 'College SIS Database Provisioning'
    },
    sampleCredentials: [
      { role: 'Principal', name: 'Dr. Shashikant V. Mayekar', email: 'principal@ycasc.com', id: 'YCASC_Principal', phone: '9823000001', defaultPass: '9823000001' },
      { role: 'Vice Principal', name: 'Dr. Ramakant Deshmukh', email: 'ramakant.deshmukh@ycasc.com', id: 'YCASC_VP', phone: '9823000002', defaultPass: '9823000002' },
      { role: 'HOD Science', name: 'Prof. Pramod Naik', email: 'pramod.naik@ycasc.com', id: 'T-SCI-101', phone: '9823000003', defaultPass: '9823000003' },
      { role: 'HOD Commerce', name: 'Prof. Sujata Kamat', email: 'hod.commerce@ycasc', id: 'T-COM-101', phone: '9823000004', defaultPass: '9823000004' },
      { role: 'HOD Arts', name: 'Dr. Robert Vance', email: 'hod.arts@ycasc', id: 'T-ART-101', phone: '9823000005', defaultPass: '9823000005' },
      { role: 'Student (Science)', name: 'Akash Kamat', email: 'akash.kamat@ycasc.com', id: 'SCI-26-001', phone: '9823222001', defaultPass: '9823222001' },
      { role: 'Student (Commerce)', name: 'Rohan Phadte', email: 'rohan.phadte@ycasc', id: 'COM-26-001', phone: '9823222016', defaultPass: '9823222016' },
      { role: 'Student (Arts)', name: 'Kunal Shirodkar', email: 'kunal.shirodkar@ycasc', id: 'ART-26-001', phone: '9823222031', defaultPass: '9823222031' }
    ]
  });
});

// ===================================================================
// YASHODA COLLEGE DEGREE ROUTES
// ===================================================================

app.post('/api/students', async (req, res) => {
  const { roll_number, first_name, last_name, gender, dob, stream_id, class_year, email, phone } = req.body;

  if (!roll_number || !first_name || !last_name || !gender || !dob || !stream_id) {
    return res.status(400).json({ error: 'roll_number, first_name, last_name, gender, dob, stream_id are required.' });
  }

  const connection = await db.getConnection();
  try {
    await connection.beginTransaction();

    const studentEmail = email || generateOfficialEmail(first_name, last_name, roll_number);
    const yr = class_year || 'FY Undergraduate';

    const [studentResult] = await connection.execute(
      `INSERT INTO students (roll_number, first_name, last_name, gender, dob, stream_id, class_year, email, phone)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [roll_number, first_name, last_name, gender, dob, stream_id, yr, studentEmail, phone || '']
    );

    const studentId = studentResult.insertId;

    const [compulsorySubjects] = await connection.execute(
      `SELECT subject_id FROM subjects WHERE stream_id = ? AND is_compulsory = TRUE`,
      [stream_id]
    );

    for (const sub of compulsorySubjects) {
      await connection.execute(
        `INSERT INTO student_course_enrollments (student_id, subject_id) VALUES (?, ?)`,
        [studentId, sub.subject_id]
      );
    }

    await connection.commit();
    res.status(201).json({
      message: 'College student registered and auto-enrolled in compulsory courses successfully',
      studentId: studentId,
      email: studentEmail,
      autoEnrolledSubjectsCount: compulsorySubjects.length
    });
  } catch (err) {
    await connection.rollback();
    console.error('Error inserting Yashoda College student:', err.message);
    if (err.code === 'ER_DUP_ENTRY') {
      return res.status(409).json({ error: 'Roll number or email already exists.' });
    }
    res.status(500).json({ error: err.message });
  } finally {
    connection.release();
  }
});

app.get('/api/students', async (req, res) => {
  const { stream_id } = req.query;

  try {
    let query = `
      SELECT 
        s.student_id, s.roll_number, s.first_name, s.last_name, 
        s.gender, s.dob, s.class_year, s.email, s.phone, st.stream_name, st.degree_name
      FROM students s
      JOIN streams st ON s.stream_id = st.stream_id
    `;
    const params = [];

    if (stream_id) {
      query += ` WHERE s.stream_id = ?`;
      params.push(stream_id);
    }

    query += ` ORDER BY s.stream_id ASC, s.roll_number ASC`;

    const [rows] = await db.execute(query, params);
    res.json(rows);
  } catch (err) {
    console.error('Error fetching college students:', err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/college/students-detailed', async (req, res) => {
  try {
    const [rows] = await db.execute(`
      SELECT 
        s.student_id, s.roll_number, s.first_name, s.last_name, 
        s.gender, s.dob, s.class_year, s.email, s.phone,
        st.stream_name, st.degree_name,
        GROUP_CONCAT(sub.subject_name SEPARATOR ', ') AS enrolled_subjects
      FROM students s
      JOIN streams st ON s.stream_id = st.stream_id
      LEFT JOIN student_course_enrollments sce ON s.student_id = sce.student_id
      LEFT JOIN subjects sub ON sce.subject_id = sub.subject_id
      GROUP BY s.student_id
      ORDER BY s.stream_id ASC, s.roll_number ASC
    `);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/teachers', async (req, res) => {
  const { stream_id } = req.query;

  try {
    let query = `
      SELECT 
        t.teacher_id, t.name, t.gender, t.role, t.designation, t.phone, t.email,
        st.stream_name,
        rep.name AS reports_to_name
      FROM teachers t
      LEFT JOIN streams st ON t.stream_id = st.stream_id
      LEFT JOIN teachers rep ON t.reports_to = rep.teacher_id
    `;
    const params = [];

    if (stream_id) {
      query += ` WHERE t.stream_id = ?`;
      params.push(stream_id);
    }

    query += ` ORDER BY t.teacher_id ASC`;

    const [rows] = await db.execute(query, params);
    res.json(rows);
  } catch (err) {
    console.error('Error fetching teachers:', err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/subjects', async (req, res) => {
  const { stream_id } = req.query;

  try {
    let query = `
      SELECT 
        sub.subject_id, sub.subject_name, sub.is_compulsory, sub.credits,
        st.stream_name, t.name AS faculty_name, t.designation AS faculty_designation
      FROM subjects sub
      JOIN streams st ON sub.stream_id = st.stream_id
      JOIN teachers t ON sub.teacher_id = t.teacher_id
    `;
    const params = [];

    if (stream_id) {
      query += ` WHERE sub.stream_id = ?`;
      params.push(stream_id);
    }

    query += ` ORDER BY sub.stream_id ASC, sub.is_compulsory DESC`;

    const [rows] = await db.execute(query, params);
    res.json(rows);
  } catch (err) {
    console.error('Error fetching subjects:', err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/streams', async (req, res) => {
  try {
    const [rows] = await db.execute(`SELECT * FROM streams ORDER BY stream_id ASC`);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/clubs', async (req, res) => {
  try {
    const [rows] = await db.execute(`
      SELECT 
        c.club_id, c.club_name, t.name AS teacher_in_charge,
        COUNT(sc.student_id) AS total_members
      FROM clubs c
      LEFT JOIN teachers t ON c.teacher_in_charge = t.teacher_id
      LEFT JOIN student_clubs sc ON c.club_id = sc.club_id
      GROUP BY c.club_id
      ORDER BY c.club_id ASC
    `);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Official Notices Endpoints (Empty by default; populated when teachers / administration post notices)
app.get('/api/notices', async (req, res) => {
  try {
    const [rows] = await db.execute(`SELECT * FROM notices ORDER BY created_at DESC`);
    res.json(rows);
  } catch (err) {
    // If table doesn't exist yet, return empty list gracefully
    res.json([]);
  }
});

app.post('/api/notices', async (req, res) => {
  try {
    const { title, category, author, target_audience, content, event_time } = req.body;
    if (!title || !content) {
      return res.status(400).json({ error: 'Title and content are required' });
    }
    const notice_ref = `NOT-2026-${Math.floor(100 + Math.random() * 900)}`;
    const [result] = await db.execute(
      `INSERT INTO notices (notice_ref, title, category, author, target_audience, content, event_time)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [notice_ref, title, category || 'General Administration', author || 'Faculty Member', target_audience || 'All Undergraduate Students & Faculty', content, event_time || null]
    );
    res.status(201).json({
      message: 'Notice successfully published',
      notice_id: result.insertId,
      notice_ref
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Node.js Yashoda College of Arts, Science and Commerce Admin server running on http://localhost:${PORT}`);
});
