-- ============================================================================
-- EVENTRA DATABASE: WAKANDA HIGHER SECONDARY SCHOOL (Wakanda HSS)
-- Database: higher_secondary_db
-- ============================================================================
CREATE DATABASE IF NOT EXISTS higher_secondary_db;
USE higher_secondary_db;

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
    target_student_count INT NOT NULL
) ENGINE=InnoDB;

-- 2. Teachers Hierarchy (Principal -> Vice Principal -> HOD -> Subject Teacher)
CREATE TABLE teachers (
    teacher_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    gender ENUM('Male', 'Female') NOT NULL,
    role ENUM('Principal', 'Vice Principal', 'HOD', 'Subject Teacher') NOT NULL,
    stream_id INT NULL,
    reports_to INT NULL,
    phone VARCHAR(15),
    email VARCHAR(100) UNIQUE,
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
    academic_year VARCHAR(10) DEFAULT '2025-2026',
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

-- 6. Clubs
CREATE TABLE clubs (
    club_id INT AUTO_INCREMENT PRIMARY KEY,
    club_name VARCHAR(50) NOT NULL UNIQUE,
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
-- SEED DATA: WAKANDA HIGHER SECONDARY SCHOOL
-- ============================================================================
INSERT INTO streams (stream_id, stream_name, target_student_count) VALUES
(1, 'Science', 50),
(2, 'Commerce', 45),
(3, 'Arts', 35),
(4, 'Vocational', 40);

INSERT INTO teachers (teacher_id, name, gender, role, stream_id, reports_to, phone, email) VALUES
(1, 'Dr. Shashikant V. Mayekar', 'Male', 'Principal', NULL, NULL, '9823000001', 'principal@wakandahss.edu.in'),
(2, 'Maria Rodrigues', 'Female', 'Vice Principal', NULL, 1, '9823000002', 'vp@wakandahss.edu.in'),
(3, 'Pramod Naik', 'Male', 'HOD', 1, 2, '9823000003', 'hod.science@wakandahss.edu.in'),
(4, 'Sujata Kamat', 'Female', 'HOD', 2, 2, '9823000004', 'hod.commerce@wakandahss.edu.in'),
(5, 'Sanjay Bandodkar', 'Male', 'HOD', 3, 2, '9823000005', 'hod.arts@wakandahss.edu.in'),
(6, 'Francis D\'Costa', 'Male', 'HOD', 4, 2, '9823000006', 'hod.vocational@wakandahss.edu.in'),
(7, 'Sneha Gaonkar', 'Female', 'Subject Teacher', 1, 3, '9823000007', 'sneha.g@wakandahss.edu.in'),
(8, 'Vikram Dessai', 'Male', 'Subject Teacher', 1, 3, '9823000008', 'vikram.d@wakandahss.edu.in'),
(9, 'Aarti Faldesai', 'Female', 'Subject Teacher', 2, 4, '9823000009', 'aarti.f@wakandahss.edu.in'),
(10, 'Nitin Shenvi', 'Male', 'Subject Teacher', 2, 4, '9823000010', 'nitin.s@wakandahss.edu.in'),
(11, 'Meera Chodankar', 'Female', 'Subject Teacher', 3, 5, '9823000011', 'meera.c@wakandahss.edu.in'),
(12, 'Prasad Sawant', 'Male', 'Subject Teacher', 3, 5, '9823000012', 'prasad.s@wakandahss.edu.in'),
(13, 'Clifford Fernandes', 'Male', 'Subject Teacher', 4, 6, '9823000013', 'clifford.f@wakandahss.edu.in'),
(14, 'Vinayak Borkar', 'Male', 'Subject Teacher', NULL, 2, '9823000014', 'sports.nss@wakandahss.edu.in');

INSERT INTO subjects (subject_id, subject_name, stream_id, is_compulsory, teacher_id) VALUES
-- SCIENCE
(1, 'Physics', 1, TRUE, 3),
(2, 'Chemistry', 1, TRUE, 7),
(3, 'Mathematics', 1, TRUE, 1),
(4, 'Biology', 1, FALSE, 8),
(5, 'Computer Science', 1, FALSE, 8),
(6, 'Psychology', 1, FALSE, 11),
(7, 'Geography (Sci)', 1, FALSE, 12),
(8, 'English (Sci)', 1, TRUE, 2),
(9, 'Second Language (Konkani)', 1, TRUE, 11),
(10, 'Physical Education (Sci)', 1, TRUE, 14),
(11, 'NSS (Sci)', 1, TRUE, 14),

-- COMMERCE
(12, 'Accountancy', 2, TRUE, 4),
(13, 'Business Studies', 2, TRUE, 9),
(14, 'Secretarial Practice', 2, FALSE, 9),
(15, 'Book Keeping', 2, TRUE, 4),
(16, 'Economics (Comm)', 2, TRUE, 10),
(17, 'Mathematics (Comm)', 2, FALSE, 1),
(18, 'English (Comm)', 2, TRUE, 2),
(19, 'Second Language (Hindi)', 2, TRUE, 11),
(20, 'Physical Education (Comm)', 2, TRUE, 14),
(21, 'NSS (Comm)', 2, TRUE, 14),

-- ARTS
(22, 'History', 3, TRUE, 5),
(23, 'Political Science', 3, TRUE, 12),
(24, 'Economics (Arts)', 3, TRUE, 10),
(25, 'Geography (Arts)', 3, TRUE, 12),
(26, 'Hindi', 3, FALSE, 11),
(27, 'Marathi', 3, FALSE, 11),
(28, 'Konkani', 3, FALSE, 11),
(29, 'English (Arts)', 3, TRUE, 2),
(30, 'Physical Education (Arts)', 3, TRUE, 14),
(31, 'NSS (Arts)', 3, TRUE, 14),

-- VOCATIONAL
(32, 'Office Management & Secretarial Practice', 4, TRUE, 6),
(33, 'Electronic Technology & Maintenance', 4, TRUE, 13),
(34, 'Accountancy & Auditing (Voc)', 4, TRUE, 10),
(35, 'Computer Applications & DTP', 4, TRUE, 13),
(36, 'English (Voc)', 4, TRUE, 2),
(37, 'Physical Education (Voc)', 4, TRUE, 14),
(38, 'NSS (Voc)', 4, TRUE, 14);

INSERT INTO clubs (club_id, club_name, teacher_in_charge) VALUES
(1, 'NSS Unit', 14),
(2, 'NCC Cadet Corps', 8),
(3, 'Cultural & Arts Club', 11),
(4, 'Sports Association', 14);

-- Population Procedure for Wakanda HSS Cohort
DELIMITER $$
CREATE PROCEDURE PopulateHigherSecondaryCohort()
BEGIN
    DECLARE i INT;
    
    SET i = 1;
    WHILE i <= 50 DO
        INSERT INTO students (roll_number, first_name, last_name, gender, dob, stream_id)
        VALUES (
            CONCAT('SCI-25-', LPAD(i, 3, '0')),
            ELT(1 + (i % 5), 'Aditya', 'Shruti', 'Varun', 'Neha', 'Pranav'),
            ELT(1 + (i % 5), 'Kamat', 'Dessai', 'Rane', 'Chari', 'Naik'),
            IF(i % 2 = 0, 'Female', 'Male'),
            DATE_ADD('2008-01-01', INTERVAL (i * 5) DAY),
            1
        );
        SET i = i + 1;
    END WHILE;

    SET i = 1;
    WHILE i <= 45 DO
        INSERT INTO students (roll_number, first_name, last_name, gender, dob, stream_id)
        VALUES (
            CONCAT('COM-25-', LPAD(i, 3, '0')),
            ELT(1 + (i % 5), 'Rohan', 'Tanvi', 'Gaurav', 'Divya', 'Sanket'),
            ELT(1 + (i % 5), 'Phadte', 'Shenvi', 'Borkar', 'Velip', 'Prabhu'),
            IF(i % 2 = 0, 'Female', 'Male'),
            DATE_ADD('2008-02-01', INTERVAL (i * 6) DAY),
            2
        );
        SET i = i + 1;
    END WHILE;

    SET i = 1;
    WHILE i <= 35 DO
        INSERT INTO students (roll_number, first_name, last_name, gender, dob, stream_id)
        VALUES (
            CONCAT('ART-25-', LPAD(i, 3, '0')),
            ELT(1 + (i % 5), 'Kunal', 'Sanika', 'Nilesh', 'Rhea', 'Gautam'),
            ELT(1 + (i % 5), 'Shirodkar', 'Bandodkar', 'Sawant', 'Tari', 'Vernekar'),
            IF(i % 2 = 0, 'Female', 'Male'),
            DATE_ADD('2008-03-01', INTERVAL (i * 7) DAY),
            3
        );
        SET i = i + 1;
    END WHILE;

    SET i = 1;
    WHILE i <= 40 DO
        INSERT INTO students (roll_number, first_name, last_name, gender, dob, stream_id)
        VALUES (
            CONCAT('VOC-25-', LPAD(i, 3, '0')),
            ELT(1 + (i % 5), 'Ashwin', 'Shreya', 'Chetan', 'Ankita', 'Manoj'),
            ELT(1 + (i % 5), 'Gaonkar', 'D\'Costa', 'Fernandes', 'Tirodkar', 'Pai'),
            IF(i % 2 = 0, 'Female', 'Male'),
            DATE_ADD('2008-04-01', INTERVAL (i * 6) DAY),
            4
        );
        SET i = i + 1;
    END WHILE;

    INSERT INTO student_course_enrollments (student_id, subject_id)
    SELECT s.student_id, sub.subject_id
    FROM students s
    JOIN subjects sub ON s.stream_id = sub.stream_id
    WHERE sub.is_compulsory = TRUE;

    INSERT INTO student_clubs (student_id, club_id)
    SELECT student_id, (student_id % 4) + 1 FROM students;

END$$
DELIMITER ;

CALL PopulateHigherSecondaryCohort();
DROP PROCEDURE PopulateHigherSecondaryCohort;
