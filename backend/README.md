# Eventra Backend — Wakanda Higher Secondary School

Backend API server for **Wakanda Higher Secondary School (Wakanda HSS)** with dedicated MySQL database schema (`higher_secondary_db`).

---


When an institution installs Eventra, the **Institution Administrator** integrates the institutional database into Eventra:
- **No public self-registration**: Like Moodle/Blackboard/Canvas, user accounts are centrally provisioned from the institution's student information system.
- **Institutional Email Domain**: All provisioned accounts receive an official email ending in `@eventra.ac.in` (e.g. `aditya.kamat@eventra.ac.in`, `shashikant.mayekar@eventra.ac.in`, `pramod.naik@eventra.ac.in`).
- **Collision Resolution (`rollno.name@eventra.ac.in`)**: If a person has the same first name and surname (e.g. `Kiran Kiran`), or if two students share the identical name and surname in the institution, their official email is generated as `rollno.name@eventra.ac.in` (e.g. `055.kiran@eventra.ac.in`) to ensure zero email conflicts.
- **Standard Default Password**: Every account is initially created with the default password `123456789`.
- **First-Time Activation & Password Reset**: Users log in using their `@eventra.ac.in` email and `123456789`, or click the provided Eventra reset password link (`/api/auth/reset-password` or interactive modal) to choose their permanent password.

### Institutional Specifications: Wakanda HSS
* **Institution Name**: Wakanda Higher Secondary School (Wakanda HSS)
* **Database**: `higher_secondary_db`
* **Grade Levels**: Higher Secondary (Grades 11 & 12)
* **Academic Streams**: 4 Specialized Streams (Science, Commerce, Arts, Vocational)
* **Student Identifier**: Stream Roll Number (`SCI-25-001`, `COM-25-001`, `ART-25-001`, `VOC-25-001`)
* **Hierarchy**: Principal -> Vice Principal -> Stream HOD -> Subject Teacher
* **Course Enrollment**: Automated compulsory courses + stream elective enrollments
* **Co-Curriculars**: NSS Unit, NCC Cadet Corps, Cultural & Arts Club, Sports Association

---

## 🚀 Setup & Execution

### 1. Database Initialization (MySQL)
Run the bundled migration script to create the database and seed the Wakanda HSS cohorts:
```bash
mysql -u root -p < database/schema_and_seed.sql
```

### 2. Configure Environment
Update `.env` with your local MySQL credentials:
```env
PORT=3000
HSS_DB_HOST=localhost
HSS_DB_USER=root
HSS_DB_PASS=your_password
HSS_DB_NAME=higher_secondary_db
```

### 3. Install & Start Server
```bash
cd backend
npm install
npm run dev   # or npm start
```

---

## 📡 API Endpoints

### Moodle-Style Authentication & Provisioning
* `POST /api/auth/login`: Authenticate with `@eventra.ac.in` email or ID + password (default `123456789`).
* `POST /api/auth/reset-password`: Activate and set custom password.
* `GET /api/admin/provisioned-accounts`: Inspect administrator-provisioned roster with collision rules.
* `GET /api/health`: Check server and MySQL connection health.

### Wakanda HSS Academic Endpoints
* `GET /api/students`: List students (filter by `?stream_id=1`).
* `GET /api/hss/students`: Full cohort data with enrolled subjects.
* `POST /api/students`: Register student and auto-enroll in compulsory stream subjects.
* `GET /api/teachers`: Full hierarchy (Principal, VP, HODs, Teachers).
* `GET /api/subjects`: Subjects grouped by stream.
* `GET /api/streams`: Available streams (Science, Commerce, Arts, Vocational).
* `GET /api/clubs`: Co-curricular clubs & membership counts.
