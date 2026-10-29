package com.example.sams;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserManager {
    private static final String PREF_NAME = "eventra_user_prefs";
    private static final String KEY_USERS = "saved_users";
    private static final String KEY_LOGGED_IN_ID = "logged_in_user_id";

    private static UserManager instance;
    private final SharedPreferences prefs;
    private final Random random = new Random();

    private UserManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        seedDefaultAccountsIfEmpty();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }

    /**
     * Normalizes any input phone number to a 10-digit Indian standard mobile number.
     * (e.g. "+91 9823222001" -> "9823222001", "09823222001" -> "9823222001").
     * Falls back to a deterministic 10-digit number if empty.
     */
    public static String normalizePhone(String rawPhone) {
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            return "9823" + String.format(java.util.Locale.US, "%06d", (int)(Math.random() * 900000 + 100000));
        }
        String digits = rawPhone.replaceAll("[^0-9]", "");
        if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        } else if (digits.length() == 11 && digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        if (digits.length() == 10) {
            return digits;
        }
        if (digits.length() > 10) {
            return digits.substring(digits.length() - 10);
        }
        String fallback = "9823000000" + digits;
        return fallback.substring(fallback.length() - 10);
    }

    private void seedDefaultAccountsIfEmpty() {
        List<User> list = prefs.contains(KEY_USERS) ? getAllUsers() : new ArrayList<>();

        // Auto-migration check: If existing database is from old Higher Secondary school (@whss.com),
        // or has incomplete cohort, or has old emails without .com, or has unseeded attendance
        boolean isOldHssData = false;
        boolean needsEmailOrAttendanceUpgrade = false;
        int studentCount = 0;
        for (User u : list) {
            if (u.getEmail() != null && u.getEmail().toLowerCase().contains("@whss.com")) {
                isOldHssData = true;
                break;
            }
            if (u.getEmail() != null && u.getEmail().endsWith("@ycasc") && !u.getEmail().endsWith("@ycasc.com")) {
                needsEmailOrAttendanceUpgrade = true;
            }
            if (u.isStudent()) {
                studentCount++;
                if (u.getAttendancePercentage() <= 0.1 || !prefs.getBoolean("has_seeded_attendance_v3", false)) {
                    needsEmailOrAttendanceUpgrade = true;
                }
            }
        }

        boolean needsPhonePasswordUpgrade = !prefs.getBoolean("has_seeded_phone_passwords_v5", false);
        boolean needs60LecturesUpgrade = !prefs.getBoolean("has_seeded_60_lectures_v6", false);
        if (list.isEmpty() || isOldHssData || studentCount < 40 || needsEmailOrAttendanceUpgrade || needsPhonePasswordUpgrade || needs60LecturesUpgrade) {
            list.clear();
            list.add(getPrincipalUser());
            seedInstitutionalFacultyAndStudents(list);
            prefs.edit()
                    .putBoolean("has_seeded_attendance_v3", true)
                    .putBoolean("has_seeded_phone_passwords_v5", true)
                    .putBoolean("has_seeded_60_lectures_v6", true)
                    .apply();
            saveUsers(list);
            return;
        }

        // Ensure Principal always exists in the system
        boolean hasPrincipal = false;
        for (User u : list) {
            if (u.isPrincipal()) {
                hasPrincipal = true;
                break;
            }
        }
        if (!hasPrincipal) {
            list.add(0, getPrincipalUser());
            saveUsers(list);
        }
    }

    private User createStudent(String id, String name, String email, String stream, String degree, 
                              String rollInfo, String phone, String dob, String yearOrClass, 
                              String subjects, double attendancePct) {
        String cleanEmail = email != null ? email.trim() : "";
        if (!cleanEmail.endsWith("@ycasc.com")) {
            cleanEmail = cleanEmail.split("@")[0] + "@ycasc.com";
        }
        String cleanPhone = normalizePhone(phone);
        // Default password is their 10-digit Indian standard phone number
        User u = new User(id, name, cleanEmail, cleanPhone, User.ROLE_STUDENT, stream, degree, 
                rollInfo, cleanPhone, User.INSTITUTION_COLLEGE, dob, yearOrClass, degree, subjects);
        u.setAttendancePercentage(attendancePct);
        u.setTotalClasses(60); // 60 standard lectures for the term
        u.setAttendedClasses((int) Math.round(60 * (attendancePct / 100.0)));
        u.setDutyLeaveApprovedCount(attendancePct > 80.0 ? 3 : (attendancePct > 70.0 ? 2 : 1));
        return u;
    }

    private void seedInstitutionalFacultyAndStudents(List<User> list) {
        // =========================================================================
        // FACULTY & ADMINISTRATION HIERARCHY
        // Principal -> Vice Principal -> Stream HODs -> Assistant / Associate Professors
        // =========================================================================

        // 1. Vice Principal & Dean of Academics
        list.add(new User(
                "YCASC_VP",
                "Dr. Ramakant Deshmukh",
                "ramakant.deshmukh@ycasc.com",
                "9823000002",
                User.ROLE_PROGRAMME_DIRECTOR,
                "Executive Administration",
                "Dean of Academic Affairs",
                "Vice Principal (YCASC)",
                "9823000002",
                User.INSTITUTION_COLLEGE,
                "20/08/1975",
                "Vice Principal",
                "Ph.D in Organic Chemistry",
                "Chemistry"
        ));

        // 2. Science Stream Faculty (HOD + 2 Teachers)
        list.add(new User(
                "T-SCI-101",
                "Dr. Pramod Naik",
                "pramod.naik@ycasc.com",
                "9823000003",
                User.ROLE_TEACHER,
                "Science Stream",
                "Bachelor of Science (B.Sc.)",
                "HOD Science & Associate Professor",
                "9823000003",
                User.INSTITUTION_COLLEGE,
                "08/09/1978",
                "HOD Science",
                "Ph.D in Physics",
                "Physics, Mathematics"
        ));
        list.add(new User(
                "T-SCI-102",
                "Dr. Sneha Gaonkar",
                "sneha.gaonkar@ycasc.com",
                "9823000004",
                User.ROLE_TEACHER,
                "Science",
                "Bachelor of Science (B.Sc.)",
                "Assistant Professor of Chemistry",
                "9823000004",
                User.INSTITUTION_COLLEGE,
                "12/03/1985",
                "Faculty",
                "Ph.D in Organic Chemistry",
                "Chemistry, Organic Chemistry, Analytical Chemistry"
        ));
        list.add(new User(
                "T-SCI-103",
                "Prof. Vikram Dessai",
                "vikram.dessai@ycasc.com",
                "9823000005",
                User.ROLE_TEACHER,
                "Science",
                "Bachelor of Science (B.Sc.)",
                "Assistant Professor of Computer Science & Mathematics",
                "9823000005",
                User.INSTITUTION_COLLEGE,
                "25/07/1988",
                "Faculty",
                "M.Sc in Applied Mathematics, M.Tech CS",
                "Mathematics, Computer Science, Data Structures, Discrete Math"
        ));

        // 3. Commerce Faculty (HOD + 2 Teachers)
        list.add(new User(
                "T-COM-101",
                "Prof. Sujata Kamat",
                "hod.commerce@ycasc.com",
                "9823000006",
                User.ROLE_TEACHER,
                "Commerce",
                "Bachelor of Commerce (B.Com)",
                "HOD Commerce & Associate Professor",
                "9823000006",
                User.INSTITUTION_COLLEGE,
                "03/01/1982",
                "Faculty",
                "M.Com, FCA, Ph.D Scholar",
                "Accountancy, Financial Accounting, Cost Accounting, Auditing"
        ));
        list.add(new User(
                "T-COM-102",
                "Prof. Aarti Faldesai",
                "aarti.faldesai@ycasc.com",
                "9823000007",
                User.ROLE_TEACHER,
                "Commerce",
                "Bachelor of Commerce (B.Com)",
                "Assistant Professor of Economics & Banking",
                "9823000007",
                User.INSTITUTION_COLLEGE,
                "17/09/1986",
                "Faculty",
                "M.A in Economics, M.Phil",
                "Business Economics, Banking & Financial Institutions, Microeconomics"
        ));
        list.add(new User(
                "T-COM-103",
                "Prof. Nitin Shenvi",
                "nitin.shenvi@ycasc.com",
                "9823000008",
                User.ROLE_TEACHER,
                "Commerce",
                "Bachelor of Commerce (B.Com)",
                "Assistant Professor of Commerce & Business Law",
                "9823000008",
                User.INSTITUTION_COLLEGE,
                "14/05/1987",
                "Faculty",
                "M.Com, LL.M",
                "Business Law, Corporate Governance, Marketing & International Trade"
        ));

        // 4. Arts Faculty (HOD + 2 Teachers)
        list.add(new User(
                "T-ART-101",
                "Dr. Robert Vance",
                "hod.arts@ycasc.com",
                "9823000009",
                User.ROLE_TEACHER,
                "Arts",
                "Bachelor of Arts (B.A.)",
                "HOD Arts & Associate Professor",
                "9823000009",
                User.INSTITUTION_COLLEGE,
                "18/06/1978",
                "Faculty",
                "Ph.D in English & Cognitive Psychology",
                "English Literature, Literary Theory, Psychology, Communication"
        ));
        list.add(new User(
                "T-ART-102",
                "Prof. Sanjay Bandodkar",
                "sanjay.bandodkar@ycasc.com",
                "9823000010",
                User.ROLE_TEACHER,
                "Arts",
                "Bachelor of Arts (B.A.)",
                "Assistant Professor of History & Heritage",
                "9823000010",
                User.INSTITUTION_COLLEGE,
                "22/08/1982",
                "Faculty",
                "M.A in Ancient & Modern World History",
                "History, World Civilization, Archaeology, Heritage Tourism"
        ));
        list.add(new User(
                "T-ART-103",
                "Prof. Meera Chodankar",
                "meera.chodankar@ycasc.com",
                "9823000011",
                User.ROLE_TEACHER,
                "Arts",
                "Bachelor of Arts (B.A.)",
                "Assistant Professor of Political Science & Sociology",
                "9823000011",
                User.INSTITUTION_COLLEGE,
                "11/11/1989",
                "Faculty",
                "M.A in Political Science, NET-JRF",
                "Political Science, Constitutional Law, Sociology, Public Administration"
        ));

        // 5. Non-Teaching Administration
        list.add(new User(
                "NT-OFF-201",
                "Mr. Rajesh Shirodkar",
                "registrar@ycasc.com",
                "9823000015",
                User.ROLE_TEACHER,
                "Administration",
                "Superintendent Office",
                "College Registrar & Exam Logistics Superintendent",
                "9823000015",
                User.INSTITUTION_COLLEGE,
                "10/02/1979",
                "Staff",
                "B.Com, PGDHRM",
                "Examination Logistics, University Transcripts, Institutional Records"
        ));

        // 6. Sports Department & Event Incharges / Coordinators
        list.add(new User(
                "COACH-101",
                "Coach Rajesh Parab",
                "rajesh.parab@ycasc.com",
                "9823000016",
                User.ROLE_EVENT_COORDINATOR,
                "Physical Education & Sports",
                "Collegiate Sports Directorate",
                "Head Coach & Inter-Collegiate Football Coordinator",
                "9823000016",
                User.INSTITUTION_COLLEGE,
                "15/04/1983",
                "Event Incharge & Head Coach",
                "M.P.Ed (Master of Physical Education), NIS Certified",
                "Physical Education, Football, Athletics, Field Drills"
        ));
        list.add(new User(
                "SPORTS-DIR-101",
                "Maria Rodrigues",
                "sports.director@ycasc.com",
                "9823000017",
                User.ROLE_EVENT_COORDINATOR,
                "Physical Education & Sports",
                "Collegiate Sports Directorate",
                "Sports Director & Annual Athletics Incharge",
                "9823000017",
                User.INSTITUTION_COLLEGE,
                "20/09/1980",
                "Sports Director & Event Incharge",
                "Ph.D in Sports Science & Physical Training",
                "Athletics, Track & Field, Sports Administration"
        ));
        list.add(new User(
                "T-ART-HOD",
                "Dr. Meenakshi Sundaram",
                "meenakshi.sundaram@ycasc.com",
                "9823000012",
                User.ROLE_TEACHER,
                "Arts",
                "Bachelor of Arts (B.A.)",
                "HOD Arts & Cultural Incharge",
                "9823000012",
                User.INSTITUTION_COLLEGE,
                "14/07/1979",
                "HOD Arts",
                "Ph.D in Performing Arts & Literature",
                "Drama, Literature, Performing Arts, Cultural Studies"
        ));

        // =========================================================================
        // STUDENTS DEMO DATA: EXACTLY 15 STUDENTS PER STREAM (TOTAL 45 STUDENTS)
        // Realistic Random Attendance: Some below 70% (defaulter), some borderline, rest above 75%
        // =========================================================================

        // A. Science Stream (15 Students: SCI-26-001 to SCI-26-015)
        String sciDeg = "Bachelor of Science (B.Sc.)";
        list.add(createStudent("SCI-26-001", "Akash Kamat", "akash.kamat@ycasc.com", "Science Stream", sciDeg, "Roll SCI-001 • Football Captain", "9823222001", "05/01/2006", "First Year (Batch 2026)", "Physics, Chemistry, Mathematics", 82.5));
        list.add(createStudent("SCI-26-002", "Priya Vernekar", "priya.vernekar@ycasc.com", "Science Stream", sciDeg, "Roll SCI-002 • Science Society Lead", "9823222002", "20/09/2004", "SY B.Sc. (Chemistry)", "Chemistry, Organic Chemistry, Analytical Chemistry, Biology", 64.0)); // Below 70%
        list.add(createStudent("SCI-26-003", "Sahil Fernandes", "sahil.fernandes@ycasc.com", "Science Stream", sciDeg, "Roll SCI-003 • Coding Club Lead", "9823222003", "05/03/2005", "FY B.Sc. (Comp Sci)", "Computer Science, Data Structures, Mathematics, Physics", 91.5));
        list.add(createStudent("SCI-26-004", "Shruti Dessai", "shruti.dessai@ycasc.com", "Science Stream", sciDeg, "Roll SCI-004 • Math Olympiad Winner", "9823222004", "12/11/2004", "TY B.Sc. (Mathematics)", "Advanced Calculus, Linear Algebra, Statistics, Physics", 88.0));
        list.add(createStudent("SCI-26-005", "Varun Rane", "varun.rane@ycasc.com", "Science Stream", sciDeg, "Roll SCI-005 • NCC Cadet Corps", "9823222005", "28/01/2005", "FY B.Sc. (Physics)", "Physics, Chemistry, Electronics, Mathematics", 58.5)); // Below 70%
        list.add(createStudent("SCI-26-006", "Neha Chari", "neha.chari@ycasc.com", "Science Stream", sciDeg, "Roll SCI-006 • Environmental Cell", "9823222006", "19/07/2004", "SY B.Sc. (Chemistry)", "Chemistry, Biochemistry, Physics, English", 73.0)); // Borderline
        list.add(createStudent("SCI-26-007", "Pranav Naik", "pranav.naik@ycasc.com", "Science Stream", sciDeg, "Roll SCI-007 • Robotics Cell", "9823222007", "22/08/2005", "FY B.Sc. (Comp Sci)", "Computer Science, Mathematics, Electronics, Physics", 67.5)); // Below 70%
        list.add(createStudent("SCI-26-008", "Ananya Gawde", "ananya.gawde@ycasc.com", "Science Stream", sciDeg, "Roll SCI-008 • Astronomy Club", "9823222008", "15/04/2004", "TY B.Sc. (Physics)", "Astrophysics, Quantum Mechanics, Classical Mechanics", 94.0));
        list.add(createStudent("SCI-26-009", "Siddharth Borkar", "siddharth.borkar@ycasc.com", "Science Stream", sciDeg, "Roll SCI-009 • Athletics Team", "9823222009", "30/10/2005", "FY B.Sc. (Mathematics)", "Calculus, Discrete Mathematics, Statistics, Computer Science", 61.2)); // Below 70%
        list.add(createStudent("SCI-26-010", "Tanvi Shirodkar", "tanvi.shirodkar@ycasc.com", "Science Stream", sciDeg, "Roll SCI-010 • Hackathon Finalist", "9823222010", "18/02/2004", "SY B.Sc. (Comp Sci)", "Database Systems, Computer Science, Algorithms, Math", 85.5));
        list.add(createStudent("SCI-26-011", "Gaurav Sawant", "gaurav.sawant@ycasc.com", "Science Stream", sciDeg, "Roll SCI-011 • Chemistry Lab Tech", "9823222011", "11/06/2005", "FY B.Sc. (Chemistry)", "Organic Chemistry, Inorganic Chemistry, Mathematics", 76.0));
        list.add(createStudent("SCI-26-012", "Rashmi Kudchadkar", "rashmi.kudchadkar@ycasc.com", "Science Stream", sciDeg, "Roll SCI-012 • Optics Researcher", "9823222012", "04/12/2004", "TY B.Sc. (Physics)", "Optics & Laser Physics, Thermodynamics, Physics", 69.0)); // Below 70%
        list.add(createStudent("SCI-26-013", "Manish Tari", "manish.tari@ycasc.com", "Science Stream", sciDeg, "Roll SCI-013 • Cyber Security Lead", "9823222013", "17/09/2005", "FY B.Sc. (Comp Sci)", "Computer Networks, Operating Systems, Computer Science", 89.2));
        list.add(createStudent("SCI-26-014", "Isha Prabhu", "isha.prabhu@ycasc.com", "Science Stream", sciDeg, "Roll SCI-014 • Statistics Representative", "9823222014", "25/08/2004", "SY B.Sc. (Mathematics)", "Mathematics, Applied Statistics, Linear Algebra", 71.5)); // Borderline
        list.add(createStudent("SCI-26-015", "Kiran M Nair", "kiran.m.nair@ycasc.com", "Science Stream", sciDeg, "Collision if name and surname already exists.", "9823222015", "14/03/2006", "First Year", "Physics, Chemistry, Computer Science, English", 78.4));

        // B. Commerce Stream (15 Students: COM-26-001 to COM-26-015)
        String comDeg = "Bachelor of Commerce (B.Com)";
        list.add(createStudent("COM-26-001", "Rohan Phadte", "rohan.phadte@ycasc.com", "Commerce Stream", comDeg, "Roll COM-001 • Class Representative", "9823222016", "18/04/2005", "FY B.Com (Accountancy)", "Financial Accounting, Business Economics, Corporate Law, Business Math", 86.0));
        list.add(createStudent("COM-26-002", "Meera Deshmukh", "meera.deshmukh@ycasc.com", "Commerce Stream", comDeg, "Roll COM-002 • Finance Club Head", "9823222017", "28/01/2004", "TY B.Com (Banking & Finance)", "Financial Markets, Investment Analysis, Banking, Business Economics", 95.0));
        list.add(createStudent("COM-26-003", "Rohan Kamat", "rohan.kamat@ycasc.com", "Commerce Stream", comDeg, "Roll COM-003 • Debate Society Secretary", "9823222018", "17/12/2005", "FY B.Com (Auditing)", "Cost Accounting, Auditing, Corporate Law, Business Economics", 63.5)); // Below 70%
        list.add(createStudent("COM-26-004", "Tanvi Shenvi", "tanvi.shenvi@ycasc.com", "Commerce Stream", comDeg, "Roll COM-004 • Taxation Cell Head", "9823222019", "22/06/2004", "SY B.Com (Corporate Law)", "Corporate Law, Direct & Indirect Tax, Financial Accounting", 77.5));
        list.add(createStudent("COM-26-005", "Gaurav Borkar", "gaurav.borkar@ycasc.com", "Commerce Stream", comDeg, "Roll COM-005 • Commerce Forum Lead", "9823222020", "14/03/2005", "FY B.Com (Accountancy)", "Accountancy, Business Law, Economics, Environmental Commerce", 59.0)); // Below 70%
        list.add(createStudent("COM-26-006", "Divya Velip", "divya.velip@ycasc.com", "Commerce Stream", comDeg, "Roll COM-006 • NSS Volunteer", "9823222021", "09/10/2004", "SY B.Com (Economics)", "Macroeconomics, Public Finance, Accountancy, Banking", 72.5)); // Borderline
        list.add(createStudent("COM-26-007", "Sanket Prabhu", "sanket.prabhu@ycasc.com", "Commerce Stream", comDeg, "Roll COM-007 • Cricket Team Vice-Captain", "9823222022", "03/07/2005", "FY B.Com (Taxation)", "Income Tax, Auditing, Business Mathematics, Economics", 66.0)); // Below 70%
        list.add(createStudent("COM-26-008", "Rhea Kakodkar", "rhea.kakodkar@ycasc.com", "Commerce Stream", comDeg, "Roll COM-008 • Stock Trading Lead", "9823222023", "29/05/2004", "TY B.Com (Finance)", "Financial Management, International Banking, Cost Accounting", 84.0));
        list.add(createStudent("COM-26-009", "Abhishek Gauns", "abhishek.gauns@ycasc.com", "Commerce Stream", comDeg, "Roll COM-009 • Entrepreneurship Cell", "9823222024", "21/11/2005", "FY B.Com (Business Law)", "Business Law, Accounting, Business Communication, Economics", 68.5)); // Below 70%
        list.add(createStudent("COM-26-010", "Pooja Mandrekar", "pooja.mandrekar@ycasc.com", "Commerce Stream", comDeg, "Roll COM-010 • Annual Fest Treasurer", "9823222025", "16/03/2004", "SY B.Com (Accountancy)", "Cost Accounting, Corporate Accounting, Business Law, Banking", 79.5));
        list.add(createStudent("COM-26-011", "Ketan Salgaonkar", "ketan.salgaonkar@ycasc.com", "Commerce Stream", comDeg, "Roll COM-011 • Table Tennis Lead", "9823222026", "07/08/2005", "FY B.Com (Auditing)", "Financial Accounting, Economics, Secretarial Practice, English", 62.0)); // Below 70%
        list.add(createStudent("COM-26-012", "Snehal Tirodkar", "snehal.tirodkar@ycasc.com", "Commerce Stream", comDeg, "Roll COM-012 • Gold Medalist Aspirant", "9823222027", "30/09/2004", "TY B.Com (Banking)", "Banking, Insurance, Auditing, Corporate Accounting", 92.5));
        list.add(createStudent("COM-26-013", "Yash Dharwadkar", "yash.dharwadkar@ycasc.com", "Commerce Stream", comDeg, "Roll COM-013 • E-Commerce Cell", "9823222028", "12/02/2005", "FY B.Com (Accountancy)", "Accounting, Digital Marketing, Business Economics, Math", 74.0)); // Borderline
        list.add(createStudent("COM-26-014", "Simran Kadam", "simran.kadam@ycasc.com", "Commerce Stream", comDeg, "Roll COM-014 • Women Empowerment Cell", "9823222029", "27/07/2004", "SY B.Com (Economics)", "Business Economics, Financial Management, Direct Tax, Law", 81.0));
        list.add(createStudent("COM-26-015", "Omkar Lotlikar", "omkar.lotlikar@ycasc.com", "Commerce Stream", comDeg, "Roll COM-015 • Youth Parliamentarian", "9823222030", "05/01/2005", "FY B.Com (Finance)", "Corporate Finance, Financial Accounting, Economics, Law", 87.5));

        // C. Arts Stream (15 Students: ART-26-001 to ART-26-015)
        String artDeg = "Bachelor of Arts (B.A.)";
        list.add(createStudent("ART-26-001", "Kunal Shirodkar", "kunal.shirodkar@ycasc.com", "Arts Stream", artDeg, "Roll ART-001 • History Society Head", "9823222031", "25/06/2005", "FY B.A. (History)", "History, Western Civilization, Political Science, English", 78.0));
        list.add(createStudent("ART-26-002", "Ananya Naik", "ananya.naik@ycasc.com", "Arts Stream", artDeg, "Roll ART-002 • Annual Drama Director", "9823222032", "11/06/2004", "TY B.A. (Psychology)", "Psychology, Cognitive Psychology, Social Psychology, Sociology", 89.0));
        list.add(createStudent("ART-26-003", "Sanika Bandodkar", "sanika.bandodkar@ycasc.com", "Arts Stream", artDeg, "Roll ART-003 • Literary Magazine Editor", "9823222033", "19/02/2005", "FY B.A. (English Lit)", "English Literature, Creative Writing, History, Sociology", 65.5)); // Below 70%
        list.add(createStudent("ART-26-004", "Nilesh Sawant", "nilesh.sawant@ycasc.com", "Arts Stream", artDeg, "Roll ART-004 • Youth Leadership Cell", "9823222034", "14/08/2004", "SY B.A. (Political Science)", "Political Science, Western Political Thought, History, English", 71.0)); // Borderline
        list.add(createStudent("ART-26-005", "Rhea Tari", "rhea.tari@ycasc.com", "Arts Stream", artDeg, "Roll ART-005 • Campus Counselor Peer", "9823222035", "03/04/2005", "FY B.A. (Psychology)", "Psychology, Child Development, English, History", 83.0));
        list.add(createStudent("ART-26-006", "Gautam Vernekar", "gautam.vernekar@ycasc.com", "Arts Stream", artDeg, "Roll ART-006 • Archaeology Enthusiast", "9823222036", "08/12/2004", "SY B.A. (History)", "History, World Civilization, Heritage Conservation, Sociology", 57.5)); // Below 70%
        list.add(createStudent("ART-26-007", "Maitreyi Kulkarni", "maitreyi.kulkarni@ycasc.com", "Arts Stream", artDeg, "Roll ART-007 • Social Research Cell", "9823222037", "21/07/2005", "FY B.A. (Sociology)", "Sociology, Indian Society, Political Science, English", 90.5));
        list.add(createStudent("ART-26-008", "Sameer Khan", "sameer.khan@ycasc.com", "Arts Stream", artDeg, "Roll ART-008 • Poetry Club Head", "9823222038", "17/05/2004", "TY B.A. (English Lit)", "Modern English Literature, Postcolonial Studies, Philosophy", 68.0)); // Below 70%
        list.add(createStudent("ART-26-009", "Aparna Joshi", "aparna.joshi@ycasc.com", "Arts Stream", artDeg, "Roll ART-009 • Model UN Secretary", "9823222039", "10/10/2005", "FY B.A. (Political Science)", "Political Science, International Relations, History, English", 76.5));
        list.add(createStudent("ART-26-010", "Chinmay Nadkarni", "chinmay.nadkarni@ycasc.com", "Arts Stream", artDeg, "Roll ART-010 • Behavioral Science Lab", "9823222040", "24/03/2004", "SY B.A. (Psychology)", "Psychology, Research Methodology, Statistics for Social Sci", 60.5)); // Below 70%
        list.add(createStudent("ART-26-011", "Devika Shet", "devika.shet@ycasc.com", "Arts Stream", artDeg, "Roll ART-011 • Classical Literature Club", "9823222041", "02/09/2005", "FY B.A. (English Lit)", "English Literature, Elizabethan Drama, Cultural Studies", 85.0));
        list.add(createStudent("ART-26-012", "Akshay Redkar", "akshay.redkar@ycasc.com", "Arts Stream", artDeg, "Roll ART-012 • Museum Studies Intern", "9823222042", "13/01/2004", "TY B.A. (History)", "Modern History, Indian Freedom Movement, Epigraphy", 73.5)); // Borderline
        list.add(createStudent("ART-26-013", "Pallavi Chopdekar", "pallavi.chopdekar@ycasc.com", "Arts Stream", artDeg, "Roll ART-013 • Rural Development Cell", "9823222043", "31/08/2005", "FY B.A. (Sociology)", "Sociology, Social Psychology, Human Geography, English", 93.0));
        list.add(createStudent("ART-26-014", "Tejas Madkaikar", "tejas.madkaikar@ycasc.com", "Arts Stream", artDeg, "Roll ART-014 • Political Analysis Lead", "9823222044", "26/11/2004", "SY B.A. (Political Science)", "Political Science, Public Administration, Constitutional Law", 67.0)); // Below 70%
        list.add(createStudent("ART-26-015", "Kavya Sukhtankar", "kavya.sukhtankar@ycasc.com", "Arts Stream", artDeg, "Roll ART-015 • Fine Arts Coordinator", "9823222045", "08/03/2005", "FY B.A. (Psychology)", "Psychology, English Literature, Sociology, Philosophy", 82.0));
    }

    public synchronized List<User> getStudents() {
        List<User> students = new ArrayList<>();
        for (User u : getAllUsers()) {
            if (u.isStudent()) {
                students.add(u);
            }
        }
        return students;
    }

    /**
     * Returns student roster filtered by the viewer's role and stream.
     * Principals see all students; Teachers and HODs only see students from their own stream.
     */
    public synchronized List<User> getStudentsForUser(User viewer) {
        List<User> all = getStudents();
        if (viewer == null || viewer.isPrincipal()) {
            return all;
        }
        String viewerStream = viewer.getStream();
        if (viewerStream.isEmpty()) {
            return all;
        }
        List<User> filtered = new ArrayList<>();
        for (User u : all) {
            if (viewer.canAccessStudent(u)) {
                filtered.add(u);
            }
        }
        return filtered;
    }

    public synchronized List<User> getFacultyAndStaff() {
        List<User> faculty = new ArrayList<>();
        for (User u : getAllUsers()) {
            if (u.isTeacherOrDirector() || u.isOrganizer()) {
                faculty.add(u);
            }
        }
        return faculty;
    }

    public synchronized List<User> filterStudents(String stream, String subject) {
        return filterStudents(getCurrentUser(), stream, subject);
    }

    public synchronized List<User> filterStudents(User viewer, String stream, String subject) {
        List<User> results = new ArrayList<>();
        List<User> sourceList = getStudentsForUser(viewer);
        String streamQuery = stream != null ? stream.trim().toLowerCase() : "";
        String subQuery = subject != null ? subject.trim().toLowerCase() : "";

        String streamKey = streamQuery.replace("stream", "").replace("division", "")
                .replace("b.sc.", "").replace("b.com", "").replace("b.a.", "")
                .replace("(", "").replace(")", "").trim();

        for (User s : sourceList) {
            boolean streamMatches = true;
            if (!streamKey.isEmpty() && !streamKey.contains("all")) {
                String sDept = s.getDepartment() != null ? s.getDepartment().toLowerCase() : "";
                String sYear = s.getYearOrClass() != null ? s.getYearOrClass().toLowerCase() : "";
                String sDeg = s.getDegree() != null ? s.getDegree().toLowerCase() : "";
                String sId = s.getId() != null ? s.getId().toLowerCase() : "";
                streamMatches = sDept.contains(streamKey) || sYear.contains(streamKey) || sDeg.contains(streamKey) || sId.contains(streamKey);
            }

            boolean subMatches = true;
            if (!subQuery.isEmpty() && !subQuery.contains("all")) {
                String sSubs = s.getSubjects() != null ? s.getSubjects().toLowerCase() : "";
                subMatches = sSubs.contains(subQuery);
            }

            if (streamMatches && subMatches) {
                results.add(s);
            }
        }
        return results;
    }

    public synchronized List<User> filterTeachers(String subject) {
        List<User> results = new ArrayList<>();
        String subQuery = subject != null ? subject.trim().toLowerCase() : "";

        for (User t : getFacultyAndStaff()) {
            if (subQuery.isEmpty() || subQuery.contains("all")) {
                results.add(t);
            } else {
                String tSubs = t.getSubjects() != null ? t.getSubjects().toLowerCase() : "";
                String tDept = t.getDepartment() != null ? t.getDepartment().toLowerCase() : "";
                String tInfo = t.getAdditionalInfo() != null ? t.getAdditionalInfo().toLowerCase() : "";
                if (tSubs.contains(subQuery) || tDept.contains(subQuery) || tInfo.contains(subQuery)) {
                    results.add(t);
                }
            }
        }
        return results;
    }

    public static User getPrincipalUser() {
        return new User(
                "YCASC_Principal",
                "Dr. Shashikant V. Mayekar",
                "principal@ycasc.com",
                "9823000001",
                User.ROLE_PROGRAMME_DIRECTOR,
                "Executive Administration",
                "Principal Office",
                "Principal (YCASC)",
                "9823000001",
                User.INSTITUTION_COLLEGE,
                "15/06/1972",
                "Principal",
                "Ph.D in Business Administration",
                "Business, Management"
        );
    }

    /**
     * Resets the entire institutional database so ONLY the Principal account is present.
     * The Principal then works as the administrator to manually add teachers and students.
     */
    public synchronized void resetToPrincipalOnly() {
        List<User> list = new ArrayList<>();
        list.add(getPrincipalUser());
        saveUsers(list);
    }

    private User getUserFromList(List<User> list, String id) {
        if (id == null) return null;
        for (User u : list) {
            if (id.equalsIgnoreCase(u.getId()) || (u.getEmail() != null && id.equalsIgnoreCase(u.getEmail()))) return u;
            // Backward compatibility alias for principal and VP IDs
            if (("YCASC_Principal".equalsIgnoreCase(id) || "WCASC_Principal".equalsIgnoreCase(id)) && u.isPrincipal()) return u;
            if (("YCASC_VP".equalsIgnoreCase(id) || "WCASC_VP".equalsIgnoreCase(id)) && "YCASC_VP".equalsIgnoreCase(u.getId())) return u;
        }
        return null;
    }

    public synchronized List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String json = prefs.getString(KEY_USERS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String email = obj.optString("email", "");
                if (!email.isEmpty()) {
                    if (email.contains("@wcasc") || email.contains("@whss") || email.contains("@wcase") || email.contains("@wsss") || !email.endsWith("@ycasc.com")) {
                        email = email.split("@")[0] + "@ycasc.com";
                    }
                }
                String phone = normalizePhone(obj.optString("phoneNumber"));
                String pass = obj.optString("password");
                if (pass.isEmpty() || pass.equals("123456789")) {
                    pass = phone;
                }
                User u = new User(
                        obj.optString("id"),
                        obj.optString("fullName"),
                        email,
                        pass,
                        obj.optString("role"),
                        obj.optString("department"),
                        obj.optString("programme"),
                        obj.optString("additionalInfo"),
                        phone,
                        obj.optString("institutionType", User.INSTITUTION_UNIVERSITY),
                        obj.optString("dob", ""),
                        obj.optString("yearOrClass", ""),
                        obj.optString("degree", ""),
                        obj.optString("subjects", "")
                );
                u.setAddress(obj.optString("address", ""));
                u.setPasswordChanged(obj.optBoolean("passwordChanged", false));

                // Restore academic attendance tracking data
                if (obj.has("attendancePercentage")) {
                    u.setAttendancePercentage(obj.optDouble("attendancePercentage", 80.0));
                    int tc = obj.optInt("totalClasses", 60);
                    if (tc == 120 || tc <= 0) {
                        tc = 60;
                    }
                    u.setTotalClasses(tc);
                    int defaultAttended = (int) Math.round(tc * (u.getAttendancePercentage() / 100.0));
                    int savedAttended = obj.optInt("attendedClasses", defaultAttended);
                    if (tc == 60 && savedAttended > 60) {
                        savedAttended = (int) Math.round(60 * (u.getAttendancePercentage() / 100.0));
                    }
                    u.setAttendedClasses(savedAttended);
                    u.setDutyLeaveApprovedCount(obj.optInt("dutyLeaveApprovedCount", 2));
                }

                list.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private synchronized void saveUsers(List<User> list) {
        try {
            JSONArray arr = new JSONArray();
            for (User u : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", u.getId());
                obj.put("fullName", u.getFullName());
                // Guarantee @ycasc.com email format
                String mail = u.getEmail() != null ? u.getEmail().trim() : "";
                if (!mail.isEmpty() && !mail.endsWith("@ycasc.com")) {
                    mail = mail.split("@")[0] + "@ycasc.com";
                }
                obj.put("email", mail);
                obj.put("password", u.getPassword());
                obj.put("role", u.getRole());
                obj.put("department", u.getDepartment());
                obj.put("programme", u.getProgramme());
                obj.put("additionalInfo", u.getAdditionalInfo());
                obj.put("phoneNumber", u.getPhoneNumber());
                obj.put("institutionType", u.getInstitutionType());
                obj.put("dob", u.getDob());
                obj.put("yearOrClass", u.getYearOrClass());
                obj.put("degree", u.getDegree());
                obj.put("subjects", u.getSubjects());
                obj.put("address", u.getAddress());
                obj.put("passwordChanged", u.isPasswordChanged());

                // Persist attendance stats
                obj.put("attendancePercentage", u.getAttendancePercentage());
                obj.put("totalClasses", u.getTotalClasses());
                obj.put("attendedClasses", u.getAttendedClasses());
                obj.put("dutyLeaveApprovedCount", u.getDutyLeaveApprovedCount());

                arr.put(obj);
            }
            prefs.edit().putString(KEY_USERS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double calculateAverageAttendance(List<User> students) {
        if (students == null || students.isEmpty()) return 0.0;
        double sum = 0;
        int count = 0;
        for (User u : students) {
            if (u.isStudent()) {
                sum += u.getAttendancePercentage();
                count++;
            }
        }
        return count == 0 ? 0.0 : (sum / count);
    }

    public int getStudentsBelowAttendanceThreshold(List<User> students, double threshold) {
        if (students == null) return 0;
        int count = 0;
        for (User u : students) {
            if (u.isStudent() && u.getAttendancePercentage() < threshold) {
                count++;
            }
        }
        return count;
    }

    public int getStudentsAboveAttendanceThreshold(List<User> students, double threshold) {
        if (students == null) return 0;
        int count = 0;
        for (User u : students) {
            if (u.isStudent() && u.getAttendancePercentage() >= threshold) {
                count++;
            }
        }
        return count;
    }

    public String sanitizeCourse(String course) {
        if (course == null || course.trim().isEmpty()) {
            return "MCA";
        }
        String trimmed = course.trim();
        // Check if there is an acronym inside parentheses e.g. "Master of Computer Applications (MCA)"
        if (trimmed.contains("(") && trimmed.contains(")")) {
            int start = trimmed.indexOf("(") + 1;
            int end = trimmed.indexOf(")");
            if (end > start) {
                String inside = trimmed.substring(start, end).replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                if (!inside.isEmpty() && inside.length() <= 8) {
                    return inside;
                }
            }
        }
        // Single word or simple abbreviation
        String cleaned = trimmed.replaceAll("[^a-zA-Z0-9\\s]", " ").trim();
        String[] parts = cleaned.split("\\s+");
        if (parts.length == 1) {
            return parts[0].toUpperCase();
        }
        // If first word is short like MCA, BCA, BTECH, BSC
        if (parts[0].length() <= 5 && !parts[0].equalsIgnoreCase("Bachelor") && !parts[0].equalsIgnoreCase("Master") && !parts[0].equalsIgnoreCase("School")) {
            return parts[0].toUpperCase();
        }
        // If "B Tech" or "M Tech"
        if (parts.length >= 2 && parts[0].length() <= 2 && parts[1].length() <= 5) {
            return (parts[0] + parts[1]).toUpperCase();
        }
        // Acronym from initial letters
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty() && !part.equalsIgnoreCase("of") && !part.equalsIgnoreCase("and") && !part.equalsIgnoreCase("in") && !part.equalsIgnoreCase("the")) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        String acronym = sb.toString();
        if (acronym.isEmpty()) return "MCA";
        return acronym.length() > 6 ? acronym.substring(0, 6) : acronym;
    }

    public String sanitizeYear(String year) {
        if (year == null || year.trim().isEmpty()) {
            return "2026";
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\b(20\\d{2})\\b").matcher(year);
        if (m.find()) {
            return m.group(1);
        }
        String digits = year.replaceAll("[^0-9]", "");
        if (digits.length() == 4) {
            return digits;
        } else if (digits.length() == 2) {
            return "20" + digits;
        }
        return "2026";
    }

    public String sanitizeRollNo(String rollNo) {
        if (rollNo == null || rollNo.trim().isEmpty()) {
            return String.valueOf(1000 + random.nextInt(9000));
        }
        String cleaned = rollNo.trim().replaceAll("[^a-zA-Z0-9]", "");
        if (cleaned.isEmpty()) {
            return String.valueOf(1000 + random.nextInt(9000));
        }
        return cleaned;
    }

    public String sanitizeStream(String stream) {
        if (stream == null || stream.trim().isEmpty()) return "SCIENCE";
        String s = stream.trim().toLowerCase();
        if (s.contains("sci")) return "SCIENCE";
        if (s.contains("comm")) return "COMMERCE";
        if (s.contains("art")) return "ARTS";
        String clean = stream.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        return clean.isEmpty() ? "SCIENCE" : (clean.length() > 8 ? clean.substring(0, 8) : clean);
    }

    public String sanitizeSchoolClass(String className) {
        if (className == null || className.trim().isEmpty()) return "CLASS10";
        String digits = className.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            return "CLASS" + digits;
        }
        String clean = className.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        return clean.isEmpty() ? "CLASS10" : (clean.length() > 8 ? clean.substring(0, 8) : clean);
    }

    public String generateStudentId(String course, String year, String rollNo) {
        return generateStudentId(User.INSTITUTION_UNIVERSITY, course, year, rollNo);
    }

    public String generateStudentId(String institutionType, String courseOrClass, String year, String rollNo) {
        String type = institutionType != null ? institutionType : User.INSTITUTION_UNIVERSITY;
        String y = sanitizeYear(year);
        String r = sanitizeRollNo(rollNo);
        String prefix;

        if (User.INSTITUTION_HIGHER_SECONDARY.equalsIgnoreCase(type)) {
            prefix = sanitizeStream(courseOrClass);
        } else if (User.INSTITUTION_SCHOOL.equalsIgnoreCase(type)) {
            prefix = sanitizeSchoolClass(courseOrClass);
        } else {
            prefix = sanitizeCourse(courseOrClass);
        }

        String candidate = prefix + "_" + y + "_" + r;
        List<User> users = getAllUsers();
        boolean exists = false;
        for (User u : users) {
            if (candidate.equalsIgnoreCase(u.getId())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            return candidate;
        }

        int suffix = 1;
        while (true) {
            String alt = prefix + "_" + y + "_" + r + "_" + suffix;
            boolean match = false;
            for (User u : users) {
                if (alt.equalsIgnoreCase(u.getId())) {
                    match = true;
                    break;
                }
            }
            if (!match) return alt;
            suffix++;
        }
    }

    public String sanitizeTeacherDept(String dept) {
        if (dept == null || dept.trim().isEmpty()) {
            return "CSC";
        }
        String trimmed = dept.trim();
        // Check for acronym inside parentheses e.g. "Computer Science (CSC)"
        if (trimmed.contains("(") && trimmed.contains(")")) {
            int start = trimmed.indexOf("(") + 1;
            int end = trimmed.indexOf(")");
            if (end > start) {
                String inside = trimmed.substring(start, end).replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                if (!inside.isEmpty() && inside.length() <= 8) {
                    return inside;
                }
            }
        }
        String lower = trimmed.toLowerCase();
        if (lower.contains("computer science") || lower.contains("computing") || lower.contains("mca") || lower.contains("bca") || lower.contains("cse")) {
            return "CSC";
        } else if (lower.contains("information tech") || lower.equals("it")) {
            return "IT";
        } else if (lower.contains("math")) {
            return "MATH";
        } else if (lower.contains("physic")) {
            return "PHY";
        } else if (lower.contains("chem")) {
            return "CHEM";
        } else if (lower.contains("bio")) {
            return "BIO";
        } else if (lower.contains("english")) {
            return "ENG";
        } else if (lower.contains("electron")) {
            return "ECE";
        } else if (lower.contains("mechanic")) {
            return "MECH";
        } else if (lower.contains("civil")) {
            return "CIVIL";
        } else if (lower.contains("commerce")) {
            return "COMM";
        } else if (lower.contains("econom")) {
            return "ECON";
        } else if (lower.contains("manage") || lower.contains("business")) {
            return "MGMT";
        }

        String cleaned = trimmed.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
        String[] parts = cleaned.split("\\s+");
        if (parts.length == 1) {
            return parts[0].toUpperCase();
        }
        if (parts[0].length() <= 5 && !parts[0].equalsIgnoreCase("Department") && !parts[0].equalsIgnoreCase("School")) {
            return parts[0].toUpperCase();
        }
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty() && !part.equalsIgnoreCase("of") && !part.equalsIgnoreCase("and") && !part.equalsIgnoreCase("in") && !part.equalsIgnoreCase("department") && !part.equalsIgnoreCase("school")) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        String acronym = sb.toString();
        if (acronym.isEmpty()) return "CSC";
        return acronym.length() > 6 ? acronym.substring(0, 6) : acronym;
    }

    public String sanitizeTeacherName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Snithi";
        }
        String cleaned = fullName.trim()
                .replaceAll("(?i)^(Dr\\.|Prof\\.|Mr\\.|Mrs\\.|Ms\\.)\\s*", "")
                .trim();
        String[] parts = cleaned.split("\\s+");
        String first = parts[0].replaceAll("[^a-zA-Z0-9]", "");
        if (first.isEmpty()) {
            return "Snithi";
        }
        return Character.toUpperCase(first.charAt(0)) + (first.length() > 1 ? first.substring(1).toLowerCase() : "");
    }

    public String generateTeacherId(String deptOrDegree, String fullName) {
        String n = sanitizeTeacherName(fullName);
        String deg = sanitizeCourse(deptOrDegree);
        int num = 101;
        String candidate = n + "_" + deg + "_" + num;

        List<User> users = getAllUsers();
        while (true) {
            boolean exists = false;
            for (User u : users) {
                if (candidate.equalsIgnoreCase(u.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) return candidate;
            num++;
            candidate = n + "_" + deg + "_" + num;
        }
    }

    public String generateUniqueId(String role) {
        if (User.ROLE_STUDENT.equals(role)) {
            return generateStudentId(User.INSTITUTION_UNIVERSITY, "MCA", "2026", String.valueOf(1000 + random.nextInt(9000)));
        }
        return generateTeacherId("CSC", "Snithi");
    }

    /**
     * Institutional Email Generator with Collision Resolution:
     * Standard: firstname.lastname@ycasc.com
     * If a person has the same first name and surname (e.g. "Kiran Kiran") OR if another
     * student in the institution already has that firstname.lastname,
     * the email is formatted as: rollno.name@ycasc.com
     */
    public String generateInstitutionalEmail(String fullName, String rollOrId) {
        if (fullName == null || fullName.trim().isEmpty()) {
            String r = (rollOrId != null && !rollOrId.trim().isEmpty())
                    ? rollOrId.trim().toLowerCase().replaceAll("[^a-z0-9]", "")
                    : "101";
            return r + ".student@ycasc.com";
        }

        String[] parts = fullName.trim().toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+");
        String first = parts.length > 0 ? parts[0] : "student";
        String last = parts.length > 1 ? parts[parts.length - 1] : "";
        String cleanRoll = (rollOrId != null && !rollOrId.trim().isEmpty())
                ? rollOrId.trim().toLowerCase().replaceAll("[^a-z0-9]", "")
                : String.valueOf(1000 + random.nextInt(9000));

        // Condition 1: Same name and surname (or single name)
        if (last.isEmpty() || first.equalsIgnoreCase(last)) {
            return cleanRoll + "." + first + "@ycasc.com";
        }

        String standard = first + "." + last + "@ycasc.com";

        // Condition 2: Check if another user already has this standard email
        for (User u : getAllUsers()) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(standard)) {
                return cleanRoll + "." + first + "@ycasc.com";
            }
        }

        return standard;
    }

    public synchronized User registerStudent(String fullName, String email, String password,
                                             String department, String course, String year,
                                             String rollNo, String phoneNumber) {
        return registerStudent(fullName, email, password, department, course, year, rollNo, phoneNumber, User.INSTITUTION_UNIVERSITY);
    }

    public synchronized User registerStudent(String fullName, String email, String password,
                                             String department, String course, String year,
                                             String rollNo, String phoneNumber, String institutionType) {
        List<User> users = getAllUsers();
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) {
                return null; // Already exists
            }
        }

        String instType = institutionType != null ? institutionType : User.INSTITUTION_UNIVERSITY;
        String generatedId = generateStudentId(instType, course, year, rollNo);
        String cleanDept = department != null && !department.trim().isEmpty() ? department.trim() : "Department of Computer Applications";
        String cleanCourse = course != null && !course.trim().isEmpty() ? course.trim() : "MCA";
        String cleanYear = sanitizeYear(year);
        String cleanRoll = sanitizeRollNo(rollNo);
        String additionalInfo = "Batch " + cleanYear + " (Roll: " + cleanRoll + ")";

        String cleanMail = email != null ? email.trim().toLowerCase() : generateInstitutionalEmail(fullName, cleanRoll);
        if (!cleanMail.endsWith("@ycasc.com")) {
            cleanMail = cleanMail.split("@")[0] + "@ycasc.com";
        }

        User newUser = new User(generatedId, fullName.trim(), cleanMail,
                password, User.ROLE_STUDENT, cleanDept, cleanCourse, additionalInfo,
                phoneNumber != null ? phoneNumber.trim() : "", instType);
        newUser.setAttendancePercentage(78.5);
        newUser.setTotalClasses(60);
        newUser.setAttendedClasses(47);
        newUser.setDutyLeaveApprovedCount(2);

        users.add(newUser);
        saveUsers(users);
        return newUser;
    }

    public synchronized User registerTeacher(String fullName, String email, String password,
                                             String role, String department, String programme,
                                             String additionalInfo, String phoneNumber, String institutionType) {
        List<User> users = getAllUsers();
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) {
                return null; // Already exists
            }
        }

        String generatedId = generateTeacherId(department, fullName);
        String cleanRole = role != null ? role : User.ROLE_TEACHER;
        String instType = institutionType != null ? institutionType : User.INSTITUTION_UNIVERSITY;

        String cleanMail = email != null ? email.trim().toLowerCase() : generateInstitutionalEmail(fullName, generatedId);
        if (!cleanMail.endsWith("@ycasc.com")) {
            cleanMail = cleanMail.split("@")[0] + "@ycasc.com";
        }

        User newUser = new User(generatedId, fullName.trim(), cleanMail,
                password, cleanRole, department != null ? department.trim() : "Computer Science (CSC)",
                programme != null ? programme.trim() : "",
                additionalInfo != null ? additionalInfo.trim() : "Faculty",
                phoneNumber != null ? phoneNumber.trim() : "",
                instType);

        users.add(newUser);
        saveUsers(users);
        return newUser;
    }

    public synchronized User registerUser(String fullName, String email, String password,
                                          String role, String department, String programme,
                                          String additionalInfo) {
        return registerUser(fullName, email, password, role, department, programme, additionalInfo, "");
    }

    public synchronized User registerUser(String fullName, String email, String password,
                                          String role, String department, String programme,
                                          String additionalInfo, String phoneNumber) {
        if (User.ROLE_STUDENT.equals(role)) {
            return registerStudent(fullName, email, password, department, programme, "2026", "2650", phoneNumber);
        } else {
            return registerTeacher(fullName, email, password, role, department, programme, additionalInfo, phoneNumber, User.INSTITUTION_UNIVERSITY);
        }
    }

    public synchronized User registerCleanUser(String fullName, String rollNoOrId, String email, String password, String role) {
        return registerCleanUser(fullName, rollNoOrId, email, password, role, User.INSTITUTION_UNIVERSITY, "", "", "", "", "", "");
    }

    public synchronized User registerCleanUser(String fullName, String rollNoOrId, String email, String password, 
                                               String role, String institutionType, String phoneNumber, 
                                               String dob, String yearOrClass, String degree, String subjects, String department) {
        String cleanMail = email != null ? email.trim().toLowerCase() : "";
        if (!cleanMail.endsWith("@ycasc.com")) {
            cleanMail = cleanMail.split("@")[0] + "@ycasc.com";
        }

        List<User> users = getAllUsers();
        for (User u : users) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(cleanMail)) {
                return null; // Already exists
            }
        }
        String cleanId = (rollNoOrId != null && !rollNoOrId.trim().isEmpty()) ? rollNoOrId.trim().toUpperCase() : "EV_" + (System.currentTimeMillis() % 100000);
        
        String defaultDept = User.ROLE_STUDENT.equals(role) ? "Computer Applications" : 
                            (User.ROLE_ORGANIZER.equals(role) ? "Event Management" : "Academics");
        String finalDept = (department != null && !department.trim().isEmpty()) ? department.trim() : defaultDept;

        String prog = User.ROLE_STUDENT.equals(role) ? 
                (degree != null && !degree.isEmpty() ? degree : "Student") : 
                (User.ROLE_ORGANIZER.equals(role) ? "Organizer Committee" : "Faculty");

        String additionalInfo = "Roll / ID: " + cleanId;
        String inst = institutionType != null && !institutionType.isEmpty() ? institutionType : User.INSTITUTION_COLLEGE;

        User newUser = new User(cleanId, fullName.trim(), cleanMail,
                password, role, finalDept, prog, additionalInfo, 
                phoneNumber != null ? phoneNumber.trim() : "", 
                inst,
                dob != null ? dob.trim() : "",
                yearOrClass != null ? yearOrClass.trim() : "",
                degree != null ? degree.trim() : "",
                subjects != null ? subjects.trim() : "");
        newUser.setPasswordChanged(false);

        if (User.ROLE_STUDENT.equals(role)) {
            newUser.setAttendancePercentage(78.5);
            newUser.setTotalClasses(60);
            newUser.setAttendedClasses(47);
            newUser.setDutyLeaveApprovedCount(2);
        }

        users.add(newUser);
        saveUsers(users);
        return newUser;
    }

    public synchronized boolean updateUser(User updatedUser) {
        if (updatedUser == null || updatedUser.getId() == null) return false;
        List<User> list = getAllUsers();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (updatedUser.getId().equalsIgnoreCase(list.get(i).getId())) {
                list.set(i, updatedUser);
                found = true;
                break;
            }
        }
        if (found) {
            saveUsers(list);
            User current = getCurrentUser();
            if (current != null && current.getId().equalsIgnoreCase(updatedUser.getId())) {
                setCurrentUser(updatedUser);
            }
            return true;
        }
        return false;
    }

    /**
     * Unified Login: authenticates both Students and Teachers through
     * registered University Email, Phone Number, or generated ID.
     */
    public synchronized User authenticate(String identifier, String password) {
        if (identifier == null || password == null) return null;
        String q = identifier.trim().toLowerCase();
        String cleanPhone = q.replaceAll("[^0-9]", "");

        List<User> users = getAllUsers();
        for (User u : users) {
            boolean matchesEmail = u.getEmail() != null && u.getEmail().trim().equalsIgnoreCase(q);
            boolean matchesId = u.getId() != null && u.getId().trim().equalsIgnoreCase(q);
            
            // Allow matching with hyphens converted to underscores or vice versa
            String normUser = u.getId() != null ? u.getId().replace("-", "_").toLowerCase() : "";
            String normQuery = q.replace("-", "_");
            boolean matchesNormId = !normUser.isEmpty() && normUser.equalsIgnoreCase(normQuery);

            // Match against GR / Roll number inside additional info
            boolean matchesInfo = u.getAdditionalInfo() != null && u.getAdditionalInfo().toLowerCase().contains(q);

            boolean matchesPhone = false;
            if (u.getPhoneNumber() != null && !u.getPhoneNumber().trim().isEmpty()) {
                String uPhoneClean = u.getPhoneNumber().replaceAll("[^0-9]", "");
                if (!cleanPhone.isEmpty() && !uPhoneClean.isEmpty()) {
                    matchesPhone = uPhoneClean.equals(cleanPhone) || 
                                   (cleanPhone.length() >= 10 && uPhoneClean.endsWith(cleanPhone)) ||
                                   (uPhoneClean.length() >= 10 && cleanPhone.endsWith(uPhoneClean));
                }
            }

            boolean matchesPrincipalAlias = u.isPrincipal() && 
                    (q.equals("principal") || q.equals("admin") || q.equals("principal@ycasc.com") || q.equals("principal@ycasc") || q.contains("shashikant") || q.equalsIgnoreCase("ycasc_principal") || q.equalsIgnoreCase("wcasc_principal"));

            if ((matchesEmail || matchesPhone || matchesId || matchesNormId || matchesInfo || matchesPrincipalAlias) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Enrolls a student directly into the institutional system.
     * Callable by Teachers and Principal.
     * The student immediately gets access to the app's student interface.
     */
    public synchronized User enrollStudent(String fullName, String rollNo, String className, 
                                           String stream, String subjects, String email, 
                                           String password, String phone) {
        String cleanName = fullName != null ? fullName.trim() : "";
        String cleanRoll = rollNo != null && !rollNo.trim().isEmpty() ? rollNo.trim().toUpperCase() : "STU_" + (1000 + random.nextInt(9000));
        String cleanClass = className != null && !className.trim().isEmpty() ? className.trim() : "First Year (Batch 2026)";
        String cleanStream = stream != null && !stream.trim().isEmpty() ? stream.trim() : "Science";
        String cleanSubjects = subjects != null && !subjects.trim().isEmpty() ? subjects.trim() : "General Degree Academics";

        String cleanEmail = email != null && !email.trim().isEmpty() ? email.trim().toLowerCase() : 
                            generateInstitutionalEmail(cleanName, cleanRoll);
        if (!cleanEmail.endsWith("@ycasc.com")) {
            cleanEmail = cleanEmail.split("@")[0] + "@ycasc.com";
        }

        String cleanPhone = normalizePhone(phone);
        String cleanPass = password != null && !password.trim().isEmpty() && !password.equals("123456789") ? password.trim() : cleanPhone;
        String degree = cleanStream.contains("Science") ? "Bachelor of Science (B.Sc.)" :
                (cleanStream.contains("Commerce") ? "Bachelor of Commerce (B.Com)" :
                (cleanStream.contains("Arts") ? "Bachelor of Arts (B.A.)" : "Bachelor of Vocation (B.Voc)"));

        return registerCleanUser(
                cleanName,
                cleanRoll,
                cleanEmail,
                cleanPass,
                User.ROLE_STUDENT,
                User.INSTITUTION_COLLEGE,
                cleanPhone,
                "",
                cleanClass,
                degree,
                cleanSubjects,
                cleanStream
        );
    }

    /**
     * Provisions a faculty / teacher member into the institutional system.
     * Callable by Principal.
     */
    public synchronized User provisionTeacher(String fullName, String teacherId, String designation, 
                                              String department, String subjects, String email, 
                                              String password, String phone) {
        String cleanName = fullName != null ? fullName.trim() : "";
        String cleanId = teacherId != null && !teacherId.trim().isEmpty() ? teacherId.trim() : generateTeacherId(department, cleanName);
        String cleanDept = department != null && !department.trim().isEmpty() ? department.trim() : "Department of Science";
        String cleanDesig = designation != null && !designation.trim().isEmpty() ? designation.trim() : "Assistant Professor & Lecturer";
        String cleanSubs = subjects != null && !subjects.trim().isEmpty() ? subjects.trim() : "General";

        String cleanEmail = email != null && !email.trim().isEmpty() ? email.trim().toLowerCase() : 
                            generateInstitutionalEmail(cleanName, cleanId);
        if (!cleanEmail.endsWith("@ycasc.com")) {
            cleanEmail = cleanEmail.split("@")[0] + "@ycasc.com";
        }

        String cleanPhone = normalizePhone(phone);
        String cleanPass = password != null && !password.trim().isEmpty() && !password.equals("123456789") ? password.trim() : cleanPhone;

        return registerCleanUser(
                cleanName,
                cleanId,
                cleanEmail,
                cleanPass,
                User.ROLE_TEACHER,
                User.INSTITUTION_COLLEGE,
                cleanPhone,
                "",
                cleanDesig,
                "Faculty Degree",
                cleanSubs,
                cleanDept
        );
    }

    public synchronized User authenticate(String idOrEmail, String password, boolean isTeacherDirectorTarget) {
        return authenticate(idOrEmail, password);
    }

    public synchronized User getUserById(String id) {
        if (id == null) return null;
        for (User u : getAllUsers()) {
            if (id.equalsIgnoreCase(u.getId())) return u;
        }
        return null;
    }

    public synchronized User findUserByEmailOrId(String idOrEmail) {
        if (idOrEmail == null) return null;
        for (User u : getAllUsers()) {
            if (idOrEmail.equalsIgnoreCase(u.getId()) || idOrEmail.equalsIgnoreCase(u.getEmail())) return u;
        }
        return null;
    }

    public synchronized void setCurrentUser(User user) {
        if (user == null) {
            prefs.edit().remove(KEY_LOGGED_IN_ID).apply();
        } else {
            prefs.edit().putString(KEY_LOGGED_IN_ID, user.getId()).apply();
        }
    }

    public synchronized User getCurrentUser() {
        String id = prefs.getString(KEY_LOGGED_IN_ID, null);
        return getUserById(id);
    }

    /**
     * Moodle-style password reset:
     * User verifies their current/default password (123456789) and sets a new one.
     */
    public synchronized boolean resetPassword(String identifier, String currentPassword, String newPassword) {
        if (identifier == null || currentPassword == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        User user = authenticate(identifier, currentPassword);
        if (user != null) {
            user.setPassword(newPassword.trim());
            user.setPasswordChanged(true);
            List<User> all = getAllUsers();
            for (User u : all) {
                if (u.getId().equalsIgnoreCase(user.getId())) {
                    u.setPassword(newPassword.trim());
                    u.setPasswordChanged(true);
                    break;
                }
            }
            saveUsers(all);
            return true;
        }
        return false;
    }

    /**
     * Recovery password reset for when a user forgets their password after changing demo pass.
     * Verifies registered User ID or Email.
     */
    public synchronized boolean forgotPasswordReset(String identifier, String newPassword) {
        if (identifier == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        String q = identifier.trim().toLowerCase();
        List<User> all = getAllUsers();
        for (User u : all) {
            if (q.equalsIgnoreCase(u.getId()) || (u.getEmail() != null && q.equalsIgnoreCase(u.getEmail()))) {
                u.setPassword(newPassword.trim());
                u.setPasswordChanged(true);
                saveUsers(all);
                return true;
            }
        }
        return false;
    }

    public synchronized void logout() {
        prefs.edit().remove(KEY_LOGGED_IN_ID).apply();
    }

    /**
     * Dynamically finds the assigned teacher for a given subject.
     * Queries database/registered accounts first, and falls back to curriculum defaults.
     * When new teachers are added to the database by the administrator, they will automatically appear here.
     */
    public synchronized String getTeacherForSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return "Maria Rodrigues (Subject Teacher)";
        }
        String clean = subject.trim().toLowerCase();

        // 1. Search registered teachers and directors in database
        for (User u : getAllUsers()) {
            if (u.isTeacherOrDirector()) {
                String dept = u.getDepartment() != null ? u.getDepartment().toLowerCase() : "";
                String prog = u.getProgramme() != null ? u.getProgramme().toLowerCase() : "";
                String subs = u.getSubjects() != null ? u.getSubjects().toLowerCase() : "";
                String info = u.getAdditionalInfo() != null ? u.getAdditionalInfo().toLowerCase() : "";

                if (subs.contains(clean) || dept.contains(clean) || prog.contains(clean) || info.contains(clean)) {
                    return u.getFullName() + " (" + u.getRoleDisplayName() + ")";
                }
            }
        }

        // 2. Realistic curriculum fallback mapping
        if (clean.contains("chem")) {
            return "Sneha Gaonkar (Chemistry Subject Teacher)";
        } else if (clean.contains("phys")) {
            return "Pramod Naik (Physics Subject Teacher / HOD)";
        } else if (clean.contains("math")) {
            return "Dr. Shashikant Mayekar (Mathematics Teacher)";
        } else if (clean.contains("bio")) {
            return "Vikram Dessai (Biology Subject Teacher)";
        } else if (clean.contains("computer") || clean.contains("cloud") || clean.contains("java") || clean.contains("web")) {
            return "Snithi Sharma (Computer Applications Lecturer)";
        } else if (clean.contains("project") || clean.contains("software")) {
            return "Dr. Robert Vance (Programme Director)";
        } else if (clean.contains("eng")) {
            return "Cheryl Pinto (English Subject Teacher)";
        } else if (clean.contains("account") || clean.contains("commerce") || clean.contains("business")) {
            return "Aarti Faldesai (Commerce Subject Teacher)";
        } else if (clean.contains("art") || clean.contains("history")) {
            return "Sanjay Bandodkar (Arts Subject Teacher)";
        }
        return "Maria Rodrigues (" + subject.trim() + " Teacher)";
    }

    public synchronized boolean changePassword(String identifier, String newPassword) {
        if (identifier == null || newPassword == null) return false;
        List<User> list = getAllUsers();
        for (int i = 0; i < list.size(); i++) {
            User u = list.get(i);
            if (identifier.equalsIgnoreCase(u.getId()) || identifier.equalsIgnoreCase(u.getEmail())) {
                u.setPassword(newPassword);
                u.setPasswordChanged(true);
                saveUsers(list);
                return true;
            }
        }
        return false;
    }

    /**
     * Provisions a dedicated Event Incharge user account created solely for managing
     * attendance and permissions for an event.
     * Can be appointed for a Teacher or a Student.
     */
    public synchronized User appointEventIncharge(Event event, User appointedPerson,
                                                  String inchargeEmail, String inchargePassword,
                                                  boolean isStudent) {
        if (event == null || appointedPerson == null) return null;

        String personName = appointedPerson.getFullName();
        String eventSlug = event.getName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (eventSlug.length() > 8) eventSlug = eventSlug.substring(0, 8);
        if (eventSlug.isEmpty()) eventSlug = "event";

        String id = "INC-" + (event.getId() != null ? event.getId().replace("EV-", "") : "EV") + "-" + (System.currentTimeMillis() % 10000);
        String name = personName + (isStudent ? " (Student Event Incharge)" : " (Faculty Event Incharge)");

        String email = inchargeEmail != null && !inchargeEmail.trim().isEmpty() ? inchargeEmail.trim().toLowerCase() : ("incharge." + eventSlug + "@ycasc.com");
        if (!email.contains("@")) email = email + "@ycasc.com";

        String password = inchargePassword != null && !inchargePassword.trim().isEmpty() ? inchargePassword.trim() : "event123";
        String designation = isStudent ? ("Student Incharge • " + event.getName()) : ("Faculty Incharge • " + event.getName());
        String info = "Event Incharge for " + event.getName() + " | Appointed by Principal";

        User inchargeUser = new User(
                id,
                name,
                email,
                password,
                User.ROLE_EVENT_COORDINATOR,
                appointedPerson.getDepartment(),
                "Event Incharge - " + event.getName(),
                info,
                appointedPerson.getPhoneNumber(),
                User.INSTITUTION_COLLEGE,
                appointedPerson.getDob() != null ? appointedPerson.getDob() : "01/01/2000",
                designation,
                event.getId(), // links to event ID
                "Ground Roll-Call & Duty Leave Redirection"
        );
        inchargeUser.setPasswordChanged(true);
        inchargeUser.setAddress("Yashoda College Campus, Margao Goa");

        // Check if account already exists with this email or id, update or add
        List<User> list = getAllUsers();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getEmail() != null && list.get(i).getEmail().equalsIgnoreCase(email)) {
                list.set(i, inchargeUser);
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(inchargeUser);
        }
        saveUsers(list);
        return inchargeUser;
    }

    public synchronized List<User> getEventIncharges() {
        List<User> incharges = new ArrayList<>();
        for (User u : getAllUsers()) {
            if (u.isEventCoordinatorOrIncharge() || User.ROLE_EVENT_COORDINATOR.equalsIgnoreCase(u.getRole())) {
                incharges.add(u);
            }
        }
        return incharges;
    }
}
