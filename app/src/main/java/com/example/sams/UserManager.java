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

    private void seedDefaultAccountsIfEmpty() {
        List<User> list = prefs.contains(KEY_USERS) ? getAllUsers() : new ArrayList<>();
        boolean changed = false;

        // Upgrade any existing demo accounts with @eventra.ac.in and default 123456789
        for (User u : list) {
            if ("password123".equals(u.getPassword())) {
                u.setPassword("123456789");
                changed = true;
            }
            if (u.getEmail() != null && !u.getEmail().endsWith("@eventra.ac.in")) {
                String prefix = u.getEmail().split("@")[0].replace(".edu.in", "").replace(".edu", "");
                u.setEmail(prefix + "@eventra.ac.in");
                changed = true;
            }
        }

        // ====================================================================
        // BASELINE / UNIVERSITY DEMO ACCOUNTS (@eventra.ac.in / 123456789)
        // ====================================================================
        if (getUserFromList(list, "MCA_2026_2650") == null) {
            list.add(new User(
                    "MCA_2026_2650",
                    "Esther Howard",
                    "student@eventra.ac.in",
                    "123456789",
                    User.ROLE_STUDENT,
                    "Department of Computer Applications",
                    "Master of Computer Applications",
                    "Batch 2026 | Roll: 2650",
                    "9876543210",
                    User.INSTITUTION_UNIVERSITY,
                    "19/08/2002",
                    "2nd Year (Semester 4)",
                    "Master of Computer Applications (MCA)",
                    "Cloud Computing, Distributed Databases, Advanced Java, Mobile Application Development, Web Frameworks"
            ));
            changed = true;
        }

        if (getUserFromList(list, "CSC_Snithi") == null) {
            list.add(new User(
                    "CSC_Snithi",
                    "Snithi Sharma",
                    "snithi.sharma@eventra.ac.in",
                    "123456789",
                    User.ROLE_TEACHER,
                    "Computer Science (CSC)",
                    "Website Design & Systems",
                    "Faculty Lecturer",
                    "9876512345",
                    User.INSTITUTION_UNIVERSITY,
                    "12/03/1990",
                    "Assistant Professor",
                    "M.Tech, Ph.D (Computer Science)",
                    "Website Design & Systems, UX Architecture, Front-end Systems"
            ));
            changed = true;
        }

        if (getUserFromList(list, "CSC_Robert") == null) {
            list.add(new User(
                    "CSC_Robert",
                    "Dr. Robert Vance",
                    "robert.vance@eventra.ac.in",
                    "123456789",
                    User.ROLE_PROGRAMME_DIRECTOR,
                    "Computer Science (CSC)",
                    "Mastering Projects & Leadership",
                    "Programme Director",
                    "9876523456",
                    User.INSTITUTION_UNIVERSITY,
                    "24/11/1982",
                    "Programme Director",
                    "Ph.D in Software Systems",
                    "Project Leadership, Software Engineering & Research"
            ));
            changed = true;
        }

        // ====================================================================
        // ORGANIZER DEMO ACCOUNT (@eventra.ac.in / 123456789)
        // ====================================================================
        if (getUserFromList(list, "ORG_2026_01") == null && getUserFromList(list, "organizer@eventra.ac.in") == null) {
            list.add(new User(
                    "ORG_2026_01",
                    "Kavita Rao",
                    "organizer@eventra.ac.in",
                    "123456789",
                    User.ROLE_ORGANIZER,
                    "Event Management",
                    "Organizer Committee",
                    "Chief Event Coordinator",
                    "9823000099",
                    User.INSTITUTION_UNIVERSITY,
                    "14/04/2001",
                    "Lead Coordinator",
                    "Master of Business Administration (MBA)",
                    "Campus Event Operations, Duty Leave Verification, Spot Pass Governance"
            ));
            changed = true;
        }

        // ====================================================================
        // WAKANDA HIGHER SECONDARY SCHOOL (Wakanda HSS)
        // Admin-provisioned @eventra.ac.in accounts (Default password: 123456789)
        // ====================================================================
        if (getUserFromList(list, "SCI_25_001") == null && getUserFromList(list, "SCI-25-001") == null) {
            list.add(new User(
                    "SCI_25_001",
                    "Aditya Kamat",
                    "aditya.kamat@eventra.ac.in",
                    "123456789",
                    User.ROLE_STUDENT,
                    "Science Stream",
                    "Higher Secondary",
                    "Roll: SCI-25-001 | NSS Unit",
                    "9823000001",
                    User.INSTITUTION_HIGHER_SECONDARY,
                    "05/01/2008",
                    "11th Standard (Class XI)",
                    "Higher Secondary Certificate (HSC)",
                    "Physics, Chemistry, Mathematics, English, NSS Unit"
            ));
            changed = true;
        }

        if (getUserFromList(list, "HSS_Shashikant") == null) {
            list.add(new User(
                    "HSS_Shashikant",
                    "Dr. Shashikant V. Mayekar",
                    "shashikant.mayekar@eventra.ac.in",
                    "123456789",
                    User.ROLE_PROGRAMME_DIRECTOR,
                    "Administration & Mathematics",
                    "Principal",
                    "Principal (Wakanda Higher Secondary School)",
                    "9823000001",
                    User.INSTITUTION_HIGHER_SECONDARY,
                    "15/06/1972",
                    "Principal",
                    "Ph.D in Applied Mathematics",
                    "Mathematics, Institutional Governance"
            ));
            changed = true;
        }

        if (getUserFromList(list, "HSS_Pramod") == null) {
            list.add(new User(
                    "HSS_Pramod",
                    "Pramod Naik",
                    "pramod.naik@eventra.ac.in",
                    "123456789",
                    User.ROLE_PROGRAMME_DIRECTOR,
                    "Science Stream",
                    "Physics",
                    "HOD Science (Wakanda HSS)",
                    "9823000003",
                    User.INSTITUTION_HIGHER_SECONDARY,
                    "08/09/1978",
                    "HOD Science",
                    "M.Sc Physics, B.Ed",
                    "Physics, Science Laboratory"
            ));
            changed = true;
        }

        // ====================================================================
        // COLLISION RESOLUTION DEMO ACCOUNT:
        // Rule: If a person has the same name and surname (or duplicate name collision),
        // email is provisioned as: rollno.name@eventra.ac.in
        // ====================================================================

        if (getUserFromList(list, "SCI_25_055") == null) {
            list.add(new User(
                    "SCI_25_055",
                    "Kiran Kiran",
                    "055.kiran@eventra.ac.in",
                    "123456789",
                    User.ROLE_STUDENT,
                    "Science Stream",
                    "Higher Secondary",
                    "Roll: 055 (Same name and surname resolved to rollno.name)",
                    "9823000055",
                    User.INSTITUTION_HIGHER_SECONDARY,
                    "22/07/2008",
                    "11th Standard (Class XI)",
                    "Higher Secondary Certificate (HSC)",
                    "Physics, Chemistry, Biology, English"
            ));
            changed = true;
        }

        if (changed || !prefs.contains(KEY_USERS)) {
            saveUsers(list);
        }
    }

    private User getUserFromList(List<User> list, String id) {
        if (id == null) return null;
        for (User u : list) {
            if (id.equalsIgnoreCase(u.getId()) || (u.getEmail() != null && id.equalsIgnoreCase(u.getEmail()))) return u;
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
                list.add(new User(
                        obj.optString("id"),
                        obj.optString("fullName"),
                        obj.optString("email"),
                        obj.optString("password"),
                        obj.optString("role"),
                        obj.optString("department"),
                        obj.optString("programme"),
                        obj.optString("additionalInfo"),
                        obj.optString("phoneNumber"),
                        obj.optString("institutionType", User.INSTITUTION_UNIVERSITY),
                        obj.optString("dob", ""),
                        obj.optString("yearOrClass", ""),
                        obj.optString("degree", ""),
                        obj.optString("subjects", "")
                ));
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
                obj.put("email", u.getEmail());
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
                arr.put(obj);
            }
            prefs.edit().putString(KEY_USERS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (s.contains("voc")) return "VOCATIONAL";
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

    public String generateTeacherId(String dept, String fullName) {
        String d = sanitizeTeacherDept(dept);
        String n = sanitizeTeacherName(fullName);
        String candidate = d + "_" + n;

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

        int suffix = 2;
        while (true) {
            String alt = d + "_" + n + "_" + suffix;
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

    public String generateUniqueId(String role) {
        if (User.ROLE_STUDENT.equals(role)) {
            return generateStudentId(User.INSTITUTION_UNIVERSITY, "MCA", "2026", String.valueOf(1000 + random.nextInt(9000)));
        }
        return generateTeacherId("CSC", "Snithi");
    }

    /**
     * Moodle-style Institutional Email Generator with Collision Resolution:
     * Standard: firstname.lastname@eventra.ac.in
     * If a person has the same first name and surname (e.g. "Kiran Kiran") OR if another
     * student in the institution already has that firstname.lastname,
     * the email is formatted as: rollno.name@eventra.ac.in
     */
    public String generateInstitutionalEmail(String fullName, String rollOrId) {
        if (fullName == null || fullName.trim().isEmpty()) {
            String r = (rollOrId != null && !rollOrId.trim().isEmpty())
                    ? rollOrId.trim().toLowerCase().replaceAll("[^a-z0-9]", "")
                    : "101";
            return r + ".student@eventra.ac.in";
        }

        String[] parts = fullName.trim().toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+");
        String first = parts.length > 0 ? parts[0] : "student";
        String last = parts.length > 1 ? parts[parts.length - 1] : "";
        String cleanRoll = (rollOrId != null && !rollOrId.trim().isEmpty())
                ? rollOrId.trim().toLowerCase().replaceAll("[^a-z0-9]", "")
                : String.valueOf(1000 + random.nextInt(9000));

        // Condition 1: Same name and surname (or single name)
        if (last.isEmpty() || first.equalsIgnoreCase(last)) {
            return cleanRoll + "." + first + "@eventra.ac.in";
        }

        String standard = first + "." + last + "@eventra.ac.in";

        // Condition 2: Check if another user already has this standard email
        for (User u : getAllUsers()) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(standard)) {
                return cleanRoll + "." + first + "@eventra.ac.in";
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

        User newUser = new User(generatedId, fullName.trim(), email.trim().toLowerCase(),
                password, User.ROLE_STUDENT, cleanDept, cleanCourse, additionalInfo,
                phoneNumber != null ? phoneNumber.trim() : "", instType);

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

        User newUser = new User(generatedId, fullName.trim(), email.trim().toLowerCase(),
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
        List<User> users = getAllUsers();
        for (User u : users) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(email.trim())) {
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
        String inst = institutionType != null && !institutionType.isEmpty() ? institutionType : User.INSTITUTION_UNIVERSITY;

        User newUser = new User(cleanId, fullName.trim(), email.trim().toLowerCase(),
                password, role, finalDept, prog, additionalInfo, 
                phoneNumber != null ? phoneNumber.trim() : "", 
                inst,
                dob != null ? dob.trim() : "",
                yearOrClass != null ? yearOrClass.trim() : "",
                degree != null ? degree.trim() : "",
                subjects != null ? subjects.trim() : "");
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

            if ((matchesEmail || matchesPhone || matchesId || matchesNormId || matchesInfo) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
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
            List<User> all = getAllUsers();
            for (User u : all) {
                if (u.getId().equalsIgnoreCase(user.getId())) {
                    u.setPassword(newPassword.trim());
                    break;
                }
            }
            saveUsers(all);
            return true;
        }
        return false;
    }

    public synchronized void logout() {
        prefs.edit().remove(KEY_LOGGED_IN_ID).apply();
    }
}
