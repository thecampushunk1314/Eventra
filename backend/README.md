# Eventra Backend — Yashoda College of Arts, Science and Commerce

Backend API server for **Yashoda College of Arts, Science and Commerce (YCASC)** with dedicated MySQL database schema (`yashoda_college_db`).

---

When an institution installs Eventra, the **Institution Administrator** integrates the institutional database into Eventra:
- **No public self-registration**: Like Moodle/Blackboard/Canvas, user accounts are centrally provisioned from the institution's collegiate student information system.
- **Institutional Email Domain**: All provisioned accounts receive an official email ending in `@ycasc` (e.g. `aditya.kamat@ycasc`, `principal@ycasc`, `pramod.naik@ycasc`).
- **Collision Resolution (`rollno.name@ycasc`)**: If a person has the same first name and surname (e.g. `Kiran Kiran`), or if two students share the identical name and surname in the institution, their official email is generated as `rollno.name@ycasc` (e.g. `015.kiran@ycasc`) to ensure zero email conflicts.
- **Standard Default Password**: Every account is initially created with the default password `123456789`.
- **First-Time Activation & Password Reset**: Users log in using their `@ycasc` email and `123456789`, or click the provided Eventra reset password link (`/api/auth/reset-password` or interactive modal) to choose their permanent password.

### Institutional Specifications: Yashoda College (YCASC)
* **Institution Name**: Yashoda College of Arts, Science and Commerce
* **Database**: `yashoda_college_db`
* **Undergraduate Streams**: 3 Academic Degree Streams:
  - **Science**: Bachelor of Science (B.Sc.)
  - **Commerce**: Bachelor of Commerce (B.Com)
  - **Arts**: Bachelor of Arts (B.A.)
* **Demo Cohort**: Exactly 15 students per stream (45 total) with complete profiles, DOB, phones, roll numbers (`SCI-26-001..015`, `COM-26-001..015`, `ART-26-001..015`), course enrollments, and clubs.
* **Faculty Hierarchy**:
  - Principal: Dr. Shashikant V. Mayekar
  - Vice Principal: Dr. Ramakant Deshmukh
  - Registrar: Mr. Rajesh Shirodkar
  - 3 teachers per stream (9 teachers total), where 1 teacher in each stream serves as Head of Department (HOD).
* **Course Enrollment**: Automated degree core courses + specialized stream lab enrollments
* **Co-Curriculars**: NSS Collegiate Unit, NCC Cadet Corps, Cultural & Arts Club, Collegiate Sports Association

---

## 🚀 Setup & Execution

### 1. Database Initialization (MySQL)
Run the bundled migration script to create the database and seed the Yashoda College cohorts:
```bash
mysql -u root -p < database/schema_and_seed.sql
```

### 2. Configure Environment
Update `.env` with your local MySQL credentials:
```env
PORT=3000
YCASC_DB_HOST=localhost
YCASC_DB_USER=root
YCASC_DB_PASS=your_password
YCASC_DB_NAME=yashoda_college_db
```

### 3. Install & Start Server
```bash
cd backend
npm install
npm run dev   # or npm start
```

---

## 📡 API Endpoints

### Institutional Authentication & Provisioning
* `POST /api/auth/login`: Authenticate with `@ycasc` email or ID + password (default `123456789`).
* `POST /api/auth/reset-password`: Activate and set custom password.
* `GET /api/admin/provisioned-accounts`: Inspect administrator-provisioned roster with collision rules.
* `GET /api/health`: Check server and MySQL connection health.

### Academic Endpoints
* `GET /api/students`: List students (filter by `?stream_id=1`).
* `GET /api/college/students`: Full cohort data with enrolled subjects.
* `POST /api/students`: Register student and auto-enroll in compulsory stream subjects.
* `GET /api/teachers`: Full hierarchy (Principal, VP, HODs, Teachers).
* `GET /api/subjects`: Subjects grouped by stream.
* `GET /api/streams`: Available streams (Science, Commerce, Arts).
* `GET /api/clubs`: Co-curricular clubs & membership counts.
