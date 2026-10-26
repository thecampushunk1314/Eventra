const express = require('express');
const cors = require('cors');
const { hssPool } = require('./db');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// ===================================================================
// SYSTEM HEALTH & INSTITUTIONAL META ROUTES
// ===================================================================

app.get('/api/health', async (req, res) => {
  let hssStatus = 'unknown';

  try {
    const [hssRows] = await hssPool.query('SELECT 1 as connected');
    hssStatus = hssRows && hssRows.length ? 'connected' : 'error';
  } catch (err) {
    hssStatus = `disconnected (${err.message})`;
  }

  res.json({
    status: 'online',
    institution: 'Wakanda Higher Secondary School',
    timestamp: new Date().toISOString(),
    database: {
      name: 'higher_secondary_db',
      status: hssStatus
    }
  });
});

app.get('/api/buyer-info', (req, res) => {
  res.json({
    institution: {
      id: 'higher_secondary',
      institutionName: 'Wakanda Higher Secondary School',
      type: 'Higher Secondary (Grade 11 & 12)',
      database: 'higher_secondary_db',
      identifierType: 'Stream Roll Number (e.g. SCI-25-001)',
      features: [
        '4 Specialized Streams (Science, Commerce, Arts, Vocational)',
        'Automated Compulsory & Elective Subject Enrollment',
        'Principal -> Vice Principal -> HOD -> Subject Teacher Hierarchy',
        'Co-Curricular Units (NSS, NCC, Cultural & Arts, Sports Association)'
      ],
      sampleStudents: ['SCI-25-001 (Aditya Kamat)', 'COM-25-001 (Rohan Phadte)'],
      sampleTeachers: ['Dr. Shashikant V. Mayekar (Principal)', 'Pramod Naik (HOD Science)']
    }
  });
});



const passwordOverrides = new Map();

app.post('/api/auth/login', async (req, res) => {
  const { identifier, password } = req.body;
  if (!identifier || !password) {
    return res.status(400).json({ error: 'Identifier and password are required.' });
  }

  const cleanId = identifier.trim().toLowerCase();
  const effectivePassword = passwordOverrides.get(cleanId) || '123456789';

  if (password !== effectivePassword && password !== '123456789') {
    return res.status(401).json({ error: 'Invalid password. (Default is 123456789)' });
  }

  res.json({
    message: 'Authentication successful',
    identifier: cleanId,
    emailExtension: '@eventra.ac.in',
    isDefaultPassword: effectivePassword === '123456789'
  });
});

app.post('/api/auth/reset-password', (req, res) => {
  const { identifier, currentPassword, newPassword } = req.body;
  if (!identifier || !currentPassword || !newPassword) {
    return res.status(400).json({ error: 'All fields (identifier, currentPassword, newPassword) are required.' });
  }

  const cleanId = identifier.trim().toLowerCase();
  const effectivePassword = passwordOverrides.get(cleanId) || '123456789';

  if (currentPassword !== effectivePassword) {
    return res.status(401).json({ error: 'Current password does not match. Default is 123456789.' });
  }

  passwordOverrides.set(cleanId, newPassword);
  res.json({
    message: 'Password successfully updated for ' + cleanId,
    resetLink: 'https://eventra.ac.in/reset-password'
  });
});

// Institutional Email Generator with Moodle Collision Resolution
function generateOfficialEmail(firstName, lastName, rollNumber, existingEmails = new Set()) {
  const cleanFirst = (firstName || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  const cleanLast = (lastName || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  const cleanRoll = (rollNumber || '').toLowerCase().replace(/[^a-z0-9]/g, '');

  const standard = `${cleanFirst}.${cleanLast}@eventra.ac.in`;
  if (!cleanLast || cleanFirst === cleanLast || existingEmails.has(standard)) {
    if (cleanRoll) {
      return `${cleanRoll}.${cleanFirst}@eventra.ac.in`;
    }
  }
  return standard;
}

app.get('/api/admin/provisioned-accounts', async (req, res) => {
  res.json({
    provisioningPolicy: {
      institution: 'Wakanda Higher Secondary School',
      database: 'higher_secondary_db',
      domain: '@eventra.ac.in',
      defaultPassword: '123456789',
      namingStandard: 'firstname.lastname@eventra.ac.in',
      collisionRule: 'If a person has the same name and surname (or duplicate collision), email is provisioned as rollno.name@eventra.ac.in',
      mechanism: 'Admin batch import from institution SIS/DB (Moodle architecture)'
    },
    sampleCredentials: [
      { role: 'Student (Science)', name: 'Aditya Kamat', email: 'aditya.kamat@eventra.ac.in', id: 'SCI_25_001', defaultPass: '123456789' },
      { role: 'Student (Collision Demo)', name: 'Kiran Kiran', email: '055.kiran@eventra.ac.in', id: 'SCI_25_055', rollNo: '055', note: 'Same name and surname resolved to rollno.name', defaultPass: '123456789' },
      { role: 'Principal', name: 'Dr. Shashikant V. Mayekar', email: 'shashikant.mayekar@eventra.ac.in', id: 'HSS_Shashikant', defaultPass: '123456789' },
      { role: 'HOD Science', name: 'Pramod Naik', email: 'pramod.naik@eventra.ac.in', id: 'HSS_Pramod', defaultPass: '123456789' }
    ]
  });
});

// ===================================================================
// WAKANDA HIGHER SECONDARY SCHOOL (Wakanda HSS) ROUTES
// ===================================================================

app.post('/api/students', async (req, res) => {
  const { roll_number, first_name, last_name, gender, dob, stream_id } = req.body;

  if (!roll_number || !first_name || !last_name || !gender || !dob || !stream_id) {
    return res.status(400).json({ error: 'All fields are required.' });
  }

  const connection = await hssPool.getConnection();
  try {
    await connection.beginTransaction();

    const [studentResult] = await connection.execute(
      `INSERT INTO students (roll_number, first_name, last_name, gender, dob, stream_id)
       VALUES (?, ?, ?, ?, ?, ?)`,
      [roll_number, first_name, last_name, gender, dob, stream_id]
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
      message: 'Student registered and enrolled in compulsory courses successfully',
      studentId: studentId,
      autoEnrolledSubjectsCount: compulsorySubjects.length
    });
  } catch (err) {
    await connection.rollback();
    console.error('Error inserting Wakanda HSS student:', err.message);
    if (err.code === 'ER_DUP_ENTRY') {
      return res.status(409).json({ error: 'Roll number already exists.' });
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
        s.gender, s.dob, st.stream_name
      FROM students s
      JOIN streams st ON s.stream_id = st.stream_id
    `;
    const params = [];

    if (stream_id) {
      query += ` WHERE s.stream_id = ?`;
      params.push(stream_id);
    }

    query += ` ORDER BY s.roll_number ASC`;

    const [rows] = await hssPool.execute(query, params);
    res.json(rows);
  } catch (err) {
    console.error('Error fetching students:', err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/hss/students', async (req, res) => {
  try {
    const [rows] = await hssPool.execute(`
      SELECT 
        s.student_id, s.roll_number, s.first_name, s.last_name, 
        s.gender, s.dob, st.stream_name,
        GROUP_CONCAT(sub.subject_name SEPARATOR ', ') AS enrolled_subjects
      FROM students s
      JOIN streams st ON s.stream_id = st.stream_id
      LEFT JOIN student_course_enrollments sce ON s.student_id = sce.student_id
      LEFT JOIN subjects sub ON sce.subject_id = sub.subject_id
      GROUP BY s.student_id
      ORDER BY s.student_id ASC
      LIMIT 100
    `);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/teachers', async (req, res) => {
  try {
    const [rows] = await hssPool.execute(`
      SELECT 
        t.teacher_id, t.name, t.gender, t.role, t.phone, t.email,
        st.stream_name,
        rep.name AS reports_to_name
      FROM teachers t
      LEFT JOIN streams st ON t.stream_id = st.stream_id
      LEFT JOIN teachers rep ON t.reports_to = rep.teacher_id
      ORDER BY t.teacher_id ASC
    `);
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
        sub.subject_id, sub.subject_name, sub.is_compulsory,
        st.stream_name, t.name AS teacher_name
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

    const [rows] = await hssPool.execute(query, params);
    res.json(rows);
  } catch (err) {
    console.error('Error fetching subjects:', err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/streams', async (req, res) => {
  try {
    const [rows] = await hssPool.execute(`SELECT * FROM streams ORDER BY stream_id ASC`);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/clubs', async (req, res) => {
  try {
    const [rows] = await hssPool.execute(`
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

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Node.js Wakanda HSS Admin server running on http://localhost:${PORT}`);
});
