-- ============================================================================
-- EVENTRA DATABASE: YASHODA COLLEGE OF ARTS, SCIENCE AND COMMERCE (YCASC)
-- Database: yashoda_college_db
-- ============================================================================
CREATE DATABASE IF NOT EXISTS yashoda_college_db;
USE yashoda_college_db;

DROP TABLE IF EXISTS student_clubs;
DROP TABLE IF EXISTS clubs;
DROP TABLE IF EXISTS student_course_enrollments;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS teachers;
DROP TABLE IF EXISTS streams;

-- 1. Streams Table
CREATE TABLE streams (
    stream_id INT AUTO_INCREMENT PRIMARY KEY,
    stream_name VARCHAR(50) NOT NULL UNIQUE,
    degree_name VARCHAR(100) NOT NULL,
    target_student_count INT NOT NULL
) ENGINE=InnoDB;

-- 2. Teachers Hierarchy (Principal -> Vice Principal -> HOD -> Assistant / Associate Professor)
CREATE TABLE teachers (
    teacher_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    gender ENUM('Male', 'Female') NOT NULL,
    role ENUM('Principal', 'Vice Principal', 'HOD', 'Professor', 'Staff') NOT NULL,
    stream_id INT NULL,
    reports_to INT NULL,
    phone VARCHAR(15),
    email VARCHAR(100) UNIQUE,
    designation VARCHAR(100),
    FOREIGN KEY (stream_id) REFERENCES streams(stream_id),
    FOREIGN KEY (reports_to) REFERENCES teachers(teacher_id)
) ENGINE=InnoDB;

-- 3. Subjects Table
CREATE TABLE subjects (
    subject_id INT AUTO_INCREMENT PRIMARY KEY,
    subject_name VARCHAR(100) NOT NULL,
    stream_id INT NOT NULL,
    is_compulsory BOOLEAN DEFAULT FALSE,
    teacher_id INT NOT NULL,
    credits INT DEFAULT 4,
    FOREIGN KEY (stream_id) REFERENCES streams(stream_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)
) ENGINE=InnoDB;

-- 4. Students Table
CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    gender ENUM('Male', 'Female') NOT NULL,
    dob DATE NOT NULL,
    stream_id INT NOT NULL,
    class_year VARCHAR(30) NOT NULL, -- FY, SY, TY
    academic_year VARCHAR(10) DEFAULT '2025-2026',
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    attendance_percentage DECIMAL(5,2) DEFAULT 75.0,
    total_classes INT DEFAULT 120,
    attended_classes INT DEFAULT 90,
    duty_leaves INT DEFAULT 2,
    FOREIGN KEY (stream_id) REFERENCES streams(stream_id)
) ENGINE=InnoDB;

-- 5. Student Subject Enrollments
CREATE TABLE student_course_enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    UNIQUE KEY (student_id, subject_id)
) ENGINE=InnoDB;

-- 6. Clubs & Co-Curricular Councils
CREATE TABLE clubs (
    club_id INT AUTO_INCREMENT PRIMARY KEY,
    club_name VARCHAR(100) NOT NULL UNIQUE,
    teacher_in_charge INT,
    FOREIGN KEY (teacher_in_charge) REFERENCES teachers(teacher_id)
) ENGINE=InnoDB;

-- 7. Student Club Memberships
CREATE TABLE student_clubs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    club_id INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (club_id) REFERENCES clubs(club_id)
) ENGINE=InnoDB;

-- ============================================================================
-- SEED DATA: YASHODA COLLEGE OF ARTS, SCIENCE AND COMMERCE
-- ============================================================================

-- 1. Streams
INSERT INTO streams (stream_id, stream_name, degree_name, target_student_count) VALUES
(1, 'Science Stream', 'Bachelor of Science (B.Sc.)', 60),
(2, 'Commerce Stream', 'Bachelor of Commerce (B.Com)', 60),
(3, 'Arts Stream', 'Bachelor of Arts (B.A.)', 60);

-- 2. Faculty & Administration
-- Hierarchy: Principal -> Vice Principal -> HODs (1 per stream) -> Professors (2 per stream)
INSERT INTO teachers (teacher_id, name, gender, role, stream_id, reports_to, phone, email, designation) VALUES
(1, 'Dr. Shashikant V. Mayekar', 'Male', 'Principal', NULL, NULL, '9823000001', 'principal@ycasc.com', 'Principal & Professor of Mathematics'),
(2, 'Dr. Ramakant Deshmukh', 'Male', 'Vice Principal', NULL, 1, '9823000002', 'ramakant.deshmukh@ycasc.com', 'Vice Principal & Dean of Academics'),

-- Science Stream Faculty (HOD + 2 Teachers)
(3, 'Prof. Pramod Naik', 'Male', 'HOD', 1, 2, '9823000003', 'pramod.naik@ycasc.com', 'HOD Science & Associate Professor (Physics)'),
(4, 'Dr. Sneha Gaonkar', 'Female', 'Professor', 1, 3, '9823000004', 'sneha.gaonkar@ycasc.com', 'Assistant Professor (Organic Chemistry)'),
(5, 'Prof. Vikram Dessai', 'Male', 'Professor', 1, 3, '9823000005', 'vikram.dessai@ycasc.com', 'Assistant Professor (Mathematics & Comp Science)'),

-- Commerce Stream Faculty (HOD + 2 Teachers)
(6, 'Prof. Sujata Kamat', 'Female', 'HOD', 2, 2, '9823000006', 'hod.commerce@ycasc.com', 'HOD Commerce & Associate Professor (Financial Accounting)'),
(7, 'Prof. Aarti Faldesai', 'Female', 'Professor', 2, 6, '9823000007', 'aarti.faldesai@ycasc.com', 'Assistant Professor (Business Economics & Banking)'),
(8, 'Prof. Nitin Shenvi', 'Male', 'Professor', 2, 6, '9823000008', 'nitin.shenvi@ycasc.com', 'Assistant Professor (Commerce & Business Law)'),

-- Arts Stream Faculty (HOD + 2 Teachers)
(9, 'Dr. Robert Vance', 'Male', 'HOD', 3, 2, '9823000009', 'hod.arts@ycasc.com', 'HOD Arts & Associate Professor (English & Psychology)'),
(10, 'Prof. Sanjay Bandodkar', 'Male', 'Professor', 3, 9, '9823000010', 'sanjay.bandodkar@ycasc.com', 'Assistant Professor (History & Heritage Studies)'),
(11, 'Prof. Meera Chodankar', 'Female', 'Professor', 3, 9, '9823000011', 'meera.chodankar@ycasc.com', 'Assistant Professor (Political Science & Sociology)'),

-- Non-Teaching Administration
(15, 'Mr. Rajesh Shirodkar', 'Male', 'Staff', NULL, 1, '9823000015', 'registrar@ycasc.com', 'College Registrar & Examination Superintendent');

-- 3. Subjects Table
INSERT INTO subjects (subject_id, subject_name, stream_id, is_compulsory, teacher_id, credits) VALUES
-- Science Subjects
(1, 'Biology', 1, TRUE, 1, 4),
(2, 'Physics', 1, TRUE, 5, 4),
(3, 'Chemistry', 1, FALSE, 3, 2),
(4, 'Mathematics', 1, TRUE, 5, 4),
(5, 'Computer Science', 1, TRUE, 5, 4),
(6, 'Geography', 1, FALSE, 3, 2),
(7, 'English', 1, TRUE, 9, 2),

-- Commerce Subjects
(8, 'Business Studies', 2, FALSE, 7, 3),
(9, 'Accountancy', 2, FALSE, 7, 3),
(10, 'Secretarial Practice', 2, FALSE, 7, 2),
(11, 'Economics', 2, TRUE, 8, 4),
(12, 'Business Law', 2, FALSE, 4, 2),
(13, 'Taxation', 2, FALSE, 6, 3),
(14, 'English', 2, TRUE, 9, 2),

-- Arts Subjects
(15, 'Contemporary World History', 3, TRUE, 10, 4),
(16, 'Modern English Literature & Criticism', 3, TRUE, 9, 4),
(17, 'Political Science', 3, TRUE, 2, 4),
(18, 'Psychology', 3, FALSE, 4, 3),
(19, 'Sociology', 3, FALSE, 8, 3),
(20, 'Geography', 3, FALSE, 3, 2),
(21, 'Environmental Studies', 3, FALSE, 3, 2);


-- ============================================================================
-- 5. STUDENTS SEED DATA: 15 STUDENTS PER STREAM (TOTAL 45 STUDENTS)
-- Includes realistic attendance distribution (some below 70%, some borderline, rest >=75%)
-- ============================================================================

-- A. Science Stream (15 Students: SCI-26-001 to SCI-26-015)
INSERT INTO students (roll_number, first_name, last_name, gender, stream_id, class_year, email, attendance_percentage, total_classes, attended_classes, duty_leaves) VALUES
('SCI-26-001', 'Akash', 'Kamat', 'Male', 1, 'First Year (Batch 2026)', 'akash.kamat@ycasc.com', 82.5, 120, 99, 3),
('SCI-26-002', 'Priya', 'Vernekar', 'Female', 1, 'SY B.Sc. (Chemistry)', 'priya.vernekar@ycasc.com', 64.0, 120, 77, 1), -- Below 70%
('SCI-26-003', 'Sahil', 'Fernandes', 'Male', 1, 'FY B.Sc. (Comp Sci)', 'sahil.fernandes@ycasc.com', 91.5, 120, 110, 3),
('SCI-26-004', 'Shruti', 'Dessai', 'Female', 1, 'TY B.Sc. (Mathematics)', 'shruti.dessai@ycasc.com', 88.0, 120, 106, 3),
('SCI-26-005', 'Varun', 'Rane', 'Male', 1, 'FY B.Sc. (Physics)', 'varun.rane@ycasc.com', 58.5, 120, 70, 1), -- Below 70%
('SCI-26-006', 'Neha', 'Chari', 'Female', 1, 'SY B.Sc. (Chemistry)', 'neha.chari@ycasc.com', 73.0, 120, 88, 2), -- Borderline
('SCI-26-007', 'Pranav', 'Naik', 'Male', 1, 'FY B.Sc. (Comp Sci)', 'pranav.naik@ycasc.com', 67.5, 120, 81, 1), -- Below 70%
('SCI-26-008', 'Ananya', 'Gawde', 'Female', 1, 'TY B.Sc. (Physics)', 'ananya.gawde@ycasc.com', 94.0, 120, 113, 3),
('SCI-26-009', 'Siddharth', 'Borkar', 'Male', 1, 'FY B.Sc. (Mathematics)', 'siddharth.borkar@ycasc.com', 61.2, 120, 73, 1), -- Below 70%
('SCI-26-010', 'Tanvi', 'Shirodkar', 'Female', 1, 'SY B.Sc. (Comp Sci)', 'tanvi.shirodkar@ycasc.com', 85.5, 120, 103, 3),
('SCI-26-011', 'Gaurav', 'Sawant', 'Male', 1, 'FY B.Sc. (Chemistry)', 'gaurav.sawant@ycasc.com', 76.0, 120, 91, 2),
('SCI-26-012', 'Rashmi', 'Kudchadkar', 'Female', 1, 'TY B.Sc. (Physics)', 'rashmi.kudchadkar@ycasc.com', 69.0, 120, 83, 1), -- Below 70%
('SCI-26-013', 'Manish', 'Tari', 'Male', 1, 'FY B.Sc. (Comp Sci)', 'manish.tari@ycasc.com', 89.2, 120, 107, 3),
('SCI-26-014', 'Isha', 'Prabhu', 'Female', 1, 'SY B.Sc. (Mathematics)', 'isha.prabhu@ycasc.com', 71.5, 120, 86, 2), -- Borderline
('SCI-26-015', 'Kiran M', 'Nair', 'Male', 1, 'First Year', 'kiran.m.nair@ycasc.com', 78.4, 120, 94, 2);

-- B. Commerce Stream (15 Students: COM-26-001 to COM-26-015)
INSERT INTO students (roll_number, first_name, last_name, gender, stream_id, class_year, email, attendance_percentage, total_classes, attended_classes, duty_leaves) VALUES
('COM-26-001', 'Rohan', 'Phadte', 'Male', 2, 'FY B.Com (Accountancy)', 'rohan.phadte@ycasc.com', 86.0, 120, 103, 3),
('COM-26-002', 'Meera', 'Deshmukh', 'Female', 2, 'TY B.Com (Banking & Finance)', 'meera.deshmukh@ycasc.com', 95.0, 120, 114, 3),
('COM-26-003', 'Rohan', 'Kamat', 'Male', 2, 'FY B.Com (Auditing)', 'rohan.kamat@ycasc.com', 63.5, 120, 76, 1), -- Below 70%
('COM-26-004', 'Tanvi', 'Shenvi', 'Female', 2, 'SY B.Com (Corporate Law)', 'tanvi.shenvi@ycasc.com', 77.5, 120, 93, 2),
('COM-26-005', 'Gaurav', 'Borkar', 'Male', 2, 'FY B.Com (Accountancy)', 'gaurav.borkar@ycasc.com', 59.0, 120, 71, 1), -- Below 70%
('COM-26-006', 'Divya', 'Velip', 'Female', 2, 'SY B.Com (Economics)', 'divya.velip@ycasc.com', 72.5, 120, 87, 2), -- Borderline
('COM-26-007', 'Sanket', 'Prabhu', 'Male', 2, 'FY B.Com (Taxation)', 'sanket.prabhu@ycasc.com', 66.0, 120, 79, 1), -- Below 70%
('COM-26-008', 'Rhea', 'Kakodkar', 'Female', 2, 'TY B.Com (Finance)', 'rhea.kakodkar@ycasc.com', 84.0, 120, 101, 3),
('COM-26-009', 'Abhishek', 'Gauns', 'Male', 2, 'FY B.Com (Business Law)', 'abhishek.gauns@ycasc.com', 68.5, 120, 82, 1), -- Below 70%
('COM-26-010', 'Pooja', 'Mandrekar', 'Female', 2, 'SY B.Com (Accountancy)', 'pooja.mandrekar@ycasc.com', 79.5, 120, 95, 2),
('COM-26-011', 'Ketan', 'Salgaonkar', 'Male', 2, 'FY B.Com (Auditing)', 'ketan.salgaonkar@ycasc.com', 62.0, 120, 74, 1), -- Below 70%
('COM-26-012', 'Snehal', 'Tirodkar', 'Female', 2, 'TY B.Com (Banking)', 'snehal.tirodkar@ycasc.com', 92.5, 120, 111, 3),
('COM-26-013', 'Yash', 'Dharwadkar', 'Male', 2, 'FY B.Com (Accountancy)', 'yash.dharwadkar@ycasc.com', 74.0, 120, 89, 2), -- Borderline
('COM-26-014', 'Simran', 'Kadam', 'Female', 2, 'SY B.Com (Economics)', 'simran.kadam@ycasc.com', 81.0, 120, 97, 3),
('COM-26-015', 'Omkar', 'Lotlikar', 'Male', 2, 'FY B.Com (Finance)', 'omkar.lotlikar@ycasc.com', 87.5, 120, 105, 3);

-- C. Arts Stream (15 Students: ART-26-001 to ART-26-015)
INSERT INTO students (roll_number, first_name, last_name, gender, stream_id, class_year, email, attendance_percentage, total_classes, attended_classes, duty_leaves) VALUES
('ART-26-001', 'Kunal', 'Shirodkar', 'Male', 3, 'FY B.A. (History)', 'kunal.shirodkar@ycasc.com', 78.0, 120, 94, 2),
('ART-26-002', 'Ananya', 'Naik', 'Female', 3, 'TY B.A. (Psychology)', 'ananya.naik@ycasc.com', 89.0, 120, 107, 3),
('ART-26-003', 'Sanika', 'Bandodkar', 'Female', 3, 'FY B.A. (English Lit)', 'sanika.bandodkar@ycasc.com', 65.5, 120, 79, 1), -- Below 70%
('ART-26-004', 'Nilesh', 'Sawant', 'Male', 3, 'SY B.A. (Political Science)', 'nilesh.sawant@ycasc.com', 71.0, 120, 85, 2), -- Borderline
('ART-26-005', 'Rhea', 'Tari', 'Female', 3, 'FY B.A. (Psychology)', 'rhea.tari@ycasc.com', 83.0, 120, 100, 3),
('ART-26-006', 'Gautam', 'Vernekar', 'Male', 3, 'SY B.A. (History)', 'gautam.vernekar@ycasc.com', 57.5, 120, 69, 1), -- Below 70%
('ART-26-007', 'Maitreyi', 'Kulkarni', 'Female', 3, 'FY B.A. (Sociology)', 'maitreyi.kulkarni@ycasc.com', 90.5, 120, 109, 3),
('ART-26-008', 'Sameer', 'Khan', 'Male', 3, 'TY B.A. (English Lit)', 'sameer.khan@ycasc.com', 68.0, 120, 82, 1), -- Below 70%
('ART-26-009', 'Aparna', 'Joshi', 'Female', 3, 'FY B.A. (Political Science)', 'aparna.joshi@ycasc.com', 76.5, 120, 92, 2),
('ART-26-010', 'Chinmay', 'Nadkarni', 'Male', 3, 'SY B.A. (Psychology)', 'chinmay.nadkarni@ycasc.com', 60.5, 120, 73, 1), -- Below 70%
('ART-26-011', 'Devika', 'Shet', 'Female', 3, 'FY B.A. (English Lit)', 'devika.shet@ycasc.com', 85.0, 120, 102, 3),
('ART-26-012', 'Akshay', 'Redkar', 'Male', 3, 'TY B.A. (History)', 'akshay.redkar@ycasc.com', 73.5, 120, 88, 2), -- Borderline
('ART-26-013', 'Pallavi', 'Chopdekar', 'Female', 3, 'FY B.A. (Sociology)', 'pallavi.chopdekar@ycasc.com', 93.0, 120, 112, 3),
('ART-26-014', 'Tejas', 'Madkaikar', 'Male', 3, 'SY B.A. (Political Science)', 'tejas.madkaikar@ycasc.com', 67.0, 120, 80, 1), -- Below 70%
('ART-26-015', 'Kavya', 'Sukhtankar', 'Female', 3, 'FY B.A. (Psychology)', 'kavya.sukhtankar@ycasc.com', 82.0, 120, 98, 3);

-- Auto-enroll students into compulsory subjects for their stream
INSERT INTO student_course_enrollments (student_id, subject_id)
SELECT s.student_id, sub.subject_id
FROM students s
JOIN subjects sub ON s.stream_id = sub.stream_id
WHERE sub.is_compulsory = TRUE;

-- Assign students to campus clubs
INSERT INTO student_clubs (student_id, club_id)
SELECT student_id, ((student_id - 1) % 6) + 1 FROM students;

-- 8. Official Notices & Circulars Table
-- (Kept empty for now; populated when teachers / administration publish notices)
DROP TABLE IF EXISTS notices;
CREATE TABLE notices (
    notice_id INT AUTO_INCREMENT PRIMARY KEY,
    notice_ref VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL DEFAULT 'General Administration',
    author VARCHAR(150) NOT NULL,
    target_audience VARCHAR(150) NOT NULL DEFAULT 'All Undergraduate Students & Faculty',
    content TEXT NOT NULL,
    event_time VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================================
-- 9. ACADEMIC TIMETABLE SLOTS & INTERCHANGE
-- Lecture Timings:
-- Slot 1: 08:00 AM - 09:00 AM
-- Slot 2: 09:00 AM - 10:00 AM
-- Slot 3: 10:00 AM - 11:00 AM
-- Recess / Break: 11:00 AM - 11:30 AM
-- Slot 4: 11:30 AM - 12:30 PM
-- Slot 5: 12:30 PM - 01:30 PM
-- ============================================================================
DROP TABLE IF EXISTS timetable_slots;
CREATE TABLE timetable_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    stream_id INT NOT NULL,
    day_of_week VARCHAR(15) NOT NULL,
    slot_number INT NOT NULL,
    start_time VARCHAR(20) NOT NULL,
    end_time VARCHAR(20) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    lecture_title VARCHAR(150) NOT NULL,
    teacher_name VARCHAR(150) NOT NULL,
    room VARCHAR(50) NOT NULL,
    is_interchanged BOOLEAN DEFAULT FALSE,
    interchanged_with VARCHAR(150) NULL,
    interchange_note TEXT NULL,
    FOREIGN KEY (stream_id) REFERENCES streams(stream_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Seed Timetable Slots for Monday to Saturday
-- Stream 1: Science (Physics, Chemistry, Mathematics, Computer Science)
INSERT INTO timetable_slots (stream_id, day_of_week, slot_number, start_time, end_time, subject, lecture_title, teacher_name, room) VALUES
-- Monday (Science)
(1, 'Monday', 1, '08:00 AM', '09:00 AM', 'Physics', 'Physics · Lecture 1', 'Dr. Pramod Naik', 'Lab 1'),
(1, 'Monday', 2, '09:00 AM', '10:00 AM', 'Chemistry', 'Chemistry · Lecture 2', 'Dr. Sneha Gaonkar', 'Room 204'),
(1, 'Monday', 3, '10:00 AM', '11:00 AM', 'Mathematics', 'Mathematics · Lecture 3', 'Dr. Shashikant Mayekar', 'Room 204'),
(1, 'Monday', 4, '11:30 AM', '12:30 PM', 'Computer Science', 'Computer Science · Lecture 4', 'Prof. Vikram Dessai', 'Computer Lab A'),
(1, 'Monday', 5, '12:30 PM', '01:30 PM', 'Physics', 'Applied Physics · Lecture 5', 'Dr. Pramod Naik', 'Lab 1'),

-- Tuesday (Science)
(1, 'Tuesday', 1, '08:00 AM', '09:00 AM', 'Chemistry', 'Chemistry · Lecture 1', 'Dr. Sneha Gaonkar', 'Room 204'),
(1, 'Tuesday', 2, '09:00 AM', '10:00 AM', 'Physics', 'Physics · Lecture 2', 'Dr. Pramod Naik', 'Lab 1'),
(1, 'Tuesday', 3, '10:00 AM', '11:00 AM', 'Mathematics', 'Mathematics · Lecture 3', 'Dr. Shashikant Mayekar', 'Room 204'),
(1, 'Tuesday', 4, '11:30 AM', '12:30 PM', 'Computer Science', 'Computer Science · Lecture 4', 'Prof. Vikram Dessai', 'Computer Lab A'),
(1, 'Tuesday', 5, '12:30 PM', '01:30 PM', 'Chemistry', 'Organic Chemistry · Lecture 5', 'Dr. Sneha Gaonkar', 'Room 204'),

-- Wednesday (Science)
(1, 'Wednesday', 1, '08:00 AM', '09:00 AM', 'Mathematics', 'Mathematics · Lecture 1', 'Dr. Shashikant Mayekar', 'Room 204'),
(1, 'Wednesday', 2, '09:00 AM', '10:00 AM', 'Computer Science', 'Computer Science · Lecture 2', 'Prof. Vikram Dessai', 'Computer Lab A'),
(1, 'Wednesday', 3, '10:00 AM', '11:00 AM', 'Physics', 'Physics · Lecture 3', 'Dr. Pramod Naik', 'Lab 1'),
(1, 'Wednesday', 4, '11:30 AM', '12:30 PM', 'Chemistry', 'Chemistry · Lecture 4', 'Dr. Sneha Gaonkar', 'Room 204'),
(1, 'Wednesday', 5, '12:30 PM', '01:30 PM', 'Mathematics', 'Discrete Mathematics · Lecture 5', 'Dr. Shashikant Mayekar', 'Room 204'),

-- Thursday (Science)
(1, 'Thursday', 1, '08:00 AM', '09:00 AM', 'Computer Science', 'Computer Science · Lecture 1', 'Prof. Vikram Dessai', 'Computer Lab A'),
(1, 'Thursday', 2, '09:00 AM', '10:00 AM', 'Physics', 'Physics · Lecture 2', 'Dr. Pramod Naik', 'Lab 1'),
(1, 'Thursday', 3, '10:00 AM', '11:00 AM', 'Chemistry', 'Chemistry · Lecture 3', 'Dr. Sneha Gaonkar', 'Room 204'),
(1, 'Thursday', 4, '11:30 AM', '12:30 PM', 'Mathematics', 'Mathematics · Lecture 4', 'Dr. Shashikant Mayekar', 'Room 204'),
(1, 'Thursday', 5, '12:30 PM', '01:30 PM', 'Computer Science', 'Software Lab · Lecture 5', 'Prof. Vikram Dessai', 'Computer Lab B'),

-- Friday (Science)
(1, 'Friday', 1, '08:00 AM', '09:00 AM', 'Chemistry', 'Chemistry · Lecture 1', 'Dr. Sneha Gaonkar', 'Room 204'),
(1, 'Friday', 2, '09:00 AM', '10:00 AM', 'Mathematics', 'Mathematics · Lecture 2', 'Dr. Shashikant Mayekar', 'Room 204'),
(1, 'Friday', 3, '10:00 AM', '11:00 AM', 'Physics', 'Physics · Lecture 3', 'Dr. Pramod Naik', 'Lab 1'),
(1, 'Friday', 4, '11:30 AM', '12:30 PM', 'Computer Science', 'Computer Science · Lecture 4', 'Prof. Vikram Dessai', 'Computer Lab A'),
(1, 'Friday', 5, '12:30 PM', '01:30 PM', 'Physics', 'Experimental Physics · Lecture 5', 'Dr. Pramod Naik', 'Lab 1'),

-- Saturday (Science)
(1, 'Saturday', 1, '08:00 AM', '09:00 AM', 'Physics', 'Physics · Lecture 1', 'Dr. Pramod Naik', 'Lab 1'),
(1, 'Saturday', 2, '09:00 AM', '10:00 AM', 'Chemistry', 'Chemistry · Lecture 2', 'Dr. Sneha Gaonkar', 'Room 204'),
(1, 'Saturday', 3, '10:00 AM', '11:00 AM', 'Mathematics', 'Mathematics · Lecture 3', 'Dr. Shashikant Mayekar', 'Room 204'),
(1, 'Saturday', 4, '11:30 AM', '12:30 PM', 'Computer Science', 'Computer Science · Lecture 4', 'Prof. Vikram Dessai', 'Computer Lab A'),
(1, 'Saturday', 5, '12:30 PM', '01:30 PM', 'Chemistry', 'Chemistry Practical Review', 'Dr. Sneha Gaonkar', 'Room 204'),

-- Stream 2: Commerce (Financial Accounting, Business Economics, Business Law, Cost Accounting, Banking)
-- Monday (Commerce)
(2, 'Monday', 1, '08:00 AM', '09:00 AM', 'Financial Accounting', 'Financial Accounting · Lecture 1', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Monday', 2, '09:00 AM', '10:00 AM', 'Business Economics', 'Business Economics · Lecture 2', 'Prof. Aarti Faldesai', 'Room 101'),
(2, 'Monday', 3, '10:00 AM', '11:00 AM', 'Business Law', 'Business Law · Lecture 3', 'Prof. Nitin Shenvi', 'Room 102'),
(2, 'Monday', 4, '11:30 AM', '12:30 PM', 'Cost Accounting', 'Cost Accounting · Lecture 4', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Monday', 5, '12:30 PM', '01:30 PM', 'Banking & Finance', 'Banking Operations · Lecture 5', 'Prof. Nitin Shenvi', 'Room 102'),

-- Tuesday (Commerce)
(2, 'Tuesday', 1, '08:00 AM', '09:00 AM', 'Business Economics', 'Business Economics · Lecture 1', 'Prof. Aarti Faldesai', 'Room 101'),
(2, 'Tuesday', 2, '09:00 AM', '10:00 AM', 'Financial Accounting', 'Financial Accounting · Lecture 2', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Tuesday', 3, '10:00 AM', '11:00 AM', 'Cost Accounting', 'Cost Accounting · Lecture 3', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Tuesday', 4, '11:30 AM', '12:30 PM', 'Business Law', 'Corporate Law · Lecture 4', 'Prof. Nitin Shenvi', 'Room 102'),
(2, 'Tuesday', 5, '12:30 PM', '01:30 PM', 'Banking & Finance', 'Financial Services · Lecture 5', 'Prof. Aarti Faldesai', 'Room 101'),

-- Wednesday (Commerce)
(2, 'Wednesday', 1, '08:00 AM', '09:00 AM', 'Financial Accounting', 'Financial Accounting · Lecture 1', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Wednesday', 2, '09:00 AM', '10:00 AM', 'Business Law', 'Business Law · Lecture 2', 'Prof. Nitin Shenvi', 'Room 102'),
(2, 'Wednesday', 3, '10:00 AM', '11:00 AM', 'Business Economics', 'Macro Economics · Lecture 3', 'Prof. Aarti Faldesai', 'Room 101'),
(2, 'Wednesday', 4, '11:30 AM', '12:30 PM', 'Cost Accounting', 'Cost Accounting · Lecture 4', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Wednesday', 5, '12:30 PM', '01:30 PM', 'Banking & Finance', 'Banking Regulations · Lecture 5', 'Prof. Nitin Shenvi', 'Room 102'),

-- Thursday (Commerce)
(2, 'Thursday', 1, '08:00 AM', '09:00 AM', 'Cost Accounting', 'Cost Accounting · Lecture 1', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Thursday', 2, '09:00 AM', '10:00 AM', 'Business Economics', 'Business Economics · Lecture 2', 'Prof. Aarti Faldesai', 'Room 101'),
(2, 'Thursday', 3, '10:00 AM', '11:00 AM', 'Financial Accounting', 'Financial Accounting · Lecture 3', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Thursday', 4, '11:30 AM', '12:30 PM', 'Business Law', 'Business Law · Lecture 4', 'Prof. Nitin Shenvi', 'Room 102'),
(2, 'Thursday', 5, '12:30 PM', '01:30 PM', 'Banking & Finance', 'Investment Analysis · Lecture 5', 'Prof. Aarti Faldesai', 'Room 101'),

-- Friday (Commerce)
(2, 'Friday', 1, '08:00 AM', '09:00 AM', 'Financial Accounting', 'Auditing & Accounting · Lecture 1', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Friday', 2, '09:00 AM', '10:00 AM', 'Business Law', 'Business Law · Lecture 2', 'Prof. Nitin Shenvi', 'Room 102'),
(2, 'Friday', 3, '10:00 AM', '11:00 AM', 'Business Economics', 'Economic Policy · Lecture 3', 'Prof. Aarti Faldesai', 'Room 101'),
(2, 'Friday', 4, '11:30 AM', '12:30 PM', 'Cost Accounting', 'Cost Management · Lecture 4', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Friday', 5, '12:30 PM', '01:30 PM', 'Banking & Finance', 'Banking Operations · Lecture 5', 'Prof. Nitin Shenvi', 'Room 102'),

-- Saturday (Commerce)
(2, 'Saturday', 1, '08:00 AM', '09:00 AM', 'Business Economics', 'Managerial Economics · Lecture 1', 'Prof. Aarti Faldesai', 'Room 101'),
(2, 'Saturday', 2, '09:00 AM', '10:00 AM', 'Financial Accounting', 'Company Accounts · Lecture 2', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Saturday', 3, '10:00 AM', '11:00 AM', 'Business Law', 'Contract Act · Lecture 3', 'Prof. Nitin Shenvi', 'Room 102'),
(2, 'Saturday', 4, '11:30 AM', '12:30 PM', 'Cost Accounting', 'Cost Sheet Preparation · Lecture 4', 'Prof. Sujata Kamat', 'Room 101'),
(2, 'Saturday', 5, '12:30 PM', '01:30 PM', 'Banking & Finance', 'Case Study Seminar · Lecture 5', 'Prof. Aarti Faldesai', 'Room 101'),

-- Stream 3: Arts (English Literature, History, Political Science, Psychology, Sociology)
-- Monday (Arts)
(3, 'Monday', 1, '08:00 AM', '09:00 AM', 'English Literature', 'Modern English Literature · Lecture 1', 'Dr. Robert Vance', 'Room 301'),
(3, 'Monday', 2, '09:00 AM', '10:00 AM', 'History', 'Contemporary World History · Lecture 2', 'Prof. Sanjay Bandodkar', 'Room 301'),
(3, 'Monday', 3, '10:00 AM', '11:00 AM', 'Political Science', 'Political Theory · Lecture 3', 'Prof. Meera Chodankar', 'Room 302'),
(3, 'Monday', 4, '11:30 AM', '12:30 PM', 'Psychology', 'General Psychology · Lecture 4', 'Dr. Anita Monteiro', 'Room 303'),
(3, 'Monday', 5, '12:30 PM', '01:30 PM', 'Sociology', 'Indian Society & Culture · Lecture 5', 'Prof. Sanjay Bandodkar', 'Room 301'),

-- Tuesday (Arts)
(3, 'Tuesday', 1, '08:00 AM', '09:00 AM', 'History', 'Indian National Movement · Lecture 1', 'Prof. Sanjay Bandodkar', 'Room 301'),
(3, 'Tuesday', 2, '09:00 AM', '10:00 AM', 'English Literature', 'Post-Colonial Literature · Lecture 2', 'Dr. Robert Vance', 'Room 301'),
(3, 'Tuesday', 3, '10:00 AM', '11:00 AM', 'Psychology', 'Developmental Psychology · Lecture 3', 'Dr. Anita Monteiro', 'Room 303'),
(3, 'Tuesday', 4, '11:30 AM', '12:30 PM', 'Political Science', 'International Relations · Lecture 4', 'Prof. Meera Chodankar', 'Room 302'),
(3, 'Tuesday', 5, '12:30 PM', '01:30 PM', 'Sociology', 'Sociological Theories · Lecture 5', 'Prof. Sanjay Bandodkar', 'Room 301'),

-- Wednesday (Arts)
(3, 'Wednesday', 1, '08:00 AM', '09:00 AM', 'Political Science', 'Public Administration · Lecture 1', 'Prof. Meera Chodankar', 'Room 302'),
(3, 'Wednesday', 2, '09:00 AM', '10:00 AM', 'History', 'World Civilization · Lecture 2', 'Prof. Sanjay Bandodkar', 'Room 301'),
(3, 'Wednesday', 3, '10:00 AM', '11:00 AM', 'English Literature', 'Literary Criticism · Lecture 3', 'Dr. Robert Vance', 'Room 301'),
(3, 'Wednesday', 4, '11:30 AM', '12:30 PM', 'Psychology', 'Social Psychology · Lecture 4', 'Dr. Anita Monteiro', 'Room 303'),
(3, 'Wednesday', 5, '12:30 PM', '01:30 PM', 'Sociology', 'Research Methods · Lecture 5', 'Prof. Sanjay Bandodkar', 'Room 301'),

-- Thursday (Arts)
(3, 'Thursday', 1, '08:00 AM', '09:00 AM', 'Psychology', 'Cognitive Psychology · Lecture 1', 'Dr. Anita Monteiro', 'Room 303'),
(3, 'Thursday', 2, '09:00 AM', '10:00 AM', 'English Literature', 'Drama & Theatre Studies · Lecture 2', 'Dr. Robert Vance', 'Room 301'),
(3, 'Thursday', 3, '10:00 AM', '11:00 AM', 'History', 'Modern Goa History · Lecture 3', 'Prof. Sanjay Bandodkar', 'Room 301'),
(3, 'Thursday', 4, '11:30 AM', '12:30 PM', 'Political Science', 'Comparative Politics · Lecture 4', 'Prof. Meera Chodankar', 'Room 302'),
(3, 'Thursday', 5, '12:30 PM', '01:30 PM', 'Sociology', 'Urban Sociology · Lecture 5', 'Prof. Sanjay Bandodkar', 'Room 301'),

-- Friday (Arts)
(3, 'Friday', 1, '08:00 AM', '09:00 AM', 'English Literature', 'Poetry Through Ages · Lecture 1', 'Dr. Robert Vance', 'Room 301'),
(3, 'Friday', 2, '09:00 AM', '10:00 AM', 'Political Science', 'Indian Constitution · Lecture 2', 'Prof. Meera Chodankar', 'Room 302'),
(3, 'Friday', 3, '10:00 AM', '11:00 AM', 'History', 'Historiography · Lecture 3', 'Prof. Sanjay Bandodkar', 'Room 301'),
(3, 'Friday', 4, '11:30 AM', '12:30 PM', 'Sociology', 'Social Stratification · Lecture 4', 'Prof. Sanjay Bandodkar', 'Room 301'),
(3, 'Friday', 5, '12:30 PM', '01:30 PM', 'Psychology', 'Abnormal Psychology · Lecture 5', 'Dr. Anita Monteiro', 'Room 303'),

-- Saturday (Arts)
(3, 'Saturday', 1, '08:00 AM', '09:00 AM', 'History', 'Asian History Seminar · Lecture 1', 'Prof. Sanjay Bandodkar', 'Room 301'),
(3, 'Saturday', 2, '09:00 AM', '10:00 AM', 'English Literature', 'Creative Writing Workshop · Lecture 2', 'Dr. Robert Vance', 'Room 301'),
(3, 'Saturday', 3, '10:00 AM', '11:00 AM', 'Political Science', 'Political Ideologies · Lecture 3', 'Prof. Meera Chodankar', 'Room 302'),
(3, 'Saturday', 4, '11:30 AM', '12:30 PM', 'Psychology', 'Practical Experimental Session', 'Dr. Anita Monteiro', 'Room 303'),
(3, 'Saturday', 5, '12:30 PM', '01:30 PM', 'Sociology', 'Community Outreach & Fieldwork', 'Prof. Sanjay Bandodkar', 'Room 301');

