package com.example.sams;

import java.io.Serializable;

public class User implements Serializable {
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_ORGANIZER = "ORGANIZER";
    public static final String ROLE_EVENT_COORDINATOR = "EVENT_COORDINATOR";
    public static final String ROLE_PROGRAMME_DIRECTOR = "PROGRAMME_DIRECTOR";

    public static final String INSTITUTION_UNIVERSITY = "University";
    public static final String INSTITUTION_COLLEGE = "College";
    public static final String INSTITUTION_HIGHER_SECONDARY = "Higher Secondary";
    public static final String INSTITUTION_SCHOOL = "School";

    private String id;
    private String fullName;
    private String email;
    private String password;
    private String role; // STUDENT, TEACHER, PROGRAMME_DIRECTOR
    private String department;
    private String programme;
    private String additionalInfo; // e.g., Semester/Year for student, Designation/Course for teacher
    private String phoneNumber;
    private String institutionType; // University, College, Higher Secondary
    private String dob; // e.g., "14/05/2008"
    private String yearOrClass; // e.g. "11th Standard", "2nd Year / Batch 2026"
    private String degree; // e.g. "MCA", "B.Tech", "Higher Secondary Certificate"
    private String subjects; // e.g. "Physics, Chemistry, Mathematics, English"
    private String address = ""; // Residential Address
    private boolean passwordChanged = false;

    // Academic Attendance Tracking
    private double attendancePercentage = 80.0;
    private int totalClasses = 60; // Standard term total lectures (60 lectures per term)
    private int attendedClasses = 48; // Default attended (80% of 60)
    private int dutyLeaveApprovedCount = 2;

    public User() {
        this.institutionType = INSTITUTION_UNIVERSITY;
    }

    public User(String id, String fullName, String email, String password, String role, 
                String department, String programme, String additionalInfo) {
        this(id, fullName, email, password, role, department, programme, additionalInfo, "", INSTITUTION_UNIVERSITY);
    }

    public User(String id, String fullName, String email, String password, String role, 
                String department, String programme, String additionalInfo, String phoneNumber) {
        this(id, fullName, email, password, role, department, programme, additionalInfo, phoneNumber, INSTITUTION_UNIVERSITY);
    }

    public User(String id, String fullName, String email, String password, String role, 
                String department, String programme, String additionalInfo, String phoneNumber, String institutionType) {
        this(id, fullName, email, password, role, department, programme, additionalInfo, phoneNumber, institutionType, "", "", "", "");
    }

    public User(String id, String fullName, String email, String password, String role, 
                String department, String programme, String additionalInfo, String phoneNumber, 
                String institutionType, String dob, String yearOrClass, String degree, String subjects) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.department = (department != null && department.trim().endsWith(" Stream")) ? department.trim().substring(0, department.trim().length() - 7).trim() : department;
        this.programme = programme;
        this.additionalInfo = additionalInfo;
        this.phoneNumber = phoneNumber;
        this.institutionType = institutionType != null && !institutionType.isEmpty() ? institutionType : INSTITUTION_UNIVERSITY;
        this.dob = dob != null ? dob : "";
        this.yearOrClass = yearOrClass != null ? yearOrClass : "";
        this.degree = degree != null ? degree : "";
        this.subjects = subjects != null ? subjects : "";
        this.address = "";
        this.passwordChanged = false;
    }

    public String getInstitutionType() {
        return institutionType != null ? institutionType : INSTITUTION_UNIVERSITY;
    }

    public void setInstitutionType(String institutionType) {
        this.institutionType = institutionType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() {
        if (department == null) return "";
        String d = department.trim();
        if (d.endsWith(" Stream")) {
            return d.substring(0, d.length() - 7).trim();
        }
        if (d.equalsIgnoreCase("Commerce Stream")) return "Commerce";
        if (d.equalsIgnoreCase("Science Stream")) return "Science";
        if (d.equalsIgnoreCase("Arts Stream")) return "Arts";
        return d;
    }

    public void setDepartment(String department) {
        if (department != null && department.trim().endsWith(" Stream")) {
            this.department = department.trim().substring(0, department.trim().length() - 7).trim();
        } else {
            this.department = department;
        }
    }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getDesignation() { return additionalInfo != null ? additionalInfo : ""; }
    public void setDesignation(String designation) { this.additionalInfo = designation; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDob() { return dob != null ? dob : ""; }
    public void setDob(String dob) { this.dob = dob; }

    public String getYearOrClass() { return yearOrClass != null ? yearOrClass : ""; }
    public void setYearOrClass(String yearOrClass) { this.yearOrClass = yearOrClass; }

    public String getDegree() { return degree != null ? degree : ""; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getSubjects() { return subjects != null ? subjects : ""; }
    public void setSubjects(String subjects) { this.subjects = subjects; }

    public java.util.List<String> getSubjectsList() {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (subjects == null || subjects.trim().isEmpty()) {
            return list;
        }
        String[] split = subjects.split("[,;•\n]");
        for (String s : split) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    public String getInitials() {
        if (fullName == null || fullName.trim().isEmpty()) return "EV";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
    }

    public boolean isSchoolOrHigherSec() {
        return INSTITUTION_HIGHER_SECONDARY.equalsIgnoreCase(institutionType) || 
               INSTITUTION_SCHOOL.equalsIgnoreCase(institutionType);
    }

    public boolean isCollegeOrUniversity() {
        return INSTITUTION_COLLEGE.equalsIgnoreCase(institutionType) || 
               INSTITUTION_UNIVERSITY.equalsIgnoreCase(institutionType);
    }

    public boolean isStudent() {
        return ROLE_STUDENT.equals(role);
    }

    public boolean isOrganizer() {
        return ROLE_ORGANIZER.equals(role);
    }

    public boolean isTeacherOrDirector() {
        return ROLE_TEACHER.equals(role) || ROLE_PROGRAMME_DIRECTOR.equals(role) || ROLE_EVENT_COORDINATOR.equals(role) || isPrincipal();
    }

    public boolean isPrincipal() {
        return "PRINCIPAL".equalsIgnoreCase(role) || ROLE_PROGRAMME_DIRECTOR.equalsIgnoreCase(role) ||
               (email != null && (email.toLowerCase().startsWith("principal@") || email.toLowerCase().contains("shashikant.mayekar"))) ||
               (additionalInfo != null && additionalInfo.toLowerCase().contains("principal")) ||
               (programme != null && programme.toLowerCase().contains("principal"));
    }

    public String getAddress() { return address != null ? address : ""; }
    public void setAddress(String address) { this.address = address; }

    public boolean isPasswordChanged() { return passwordChanged; }
    public void setPasswordChanged(boolean passwordChanged) { this.passwordChanged = passwordChanged; }

    public boolean isProfileComplete() {
        if (isPrincipal()) return true;
        return address != null && !address.trim().isEmpty()
                && phoneNumber != null && !phoneNumber.trim().isEmpty()
                && dob != null && !dob.trim().isEmpty();
    }

    public String getRoleDisplayName() {
        if (isPrincipal()) {
            return "Principal / Admin";
        } else if (isEventCoordinatorOrIncharge()) {
            return "Event Incharge";
        } else if (ROLE_STUDENT.equals(role)) {
            return "Student";
        } else if (ROLE_ORGANIZER.equals(role)) {
            return "Organizer";
        } else {
            return "Faculty";
        }
    }

    public double getAttendancePercentage() { 
        return attendancePercentage; 
    }

    public double calculateAttendance() {
        if (totalClasses <= 0) return 100.0;
        int effectiveAttended = attendedClasses + dutyLeaveApprovedCount;
        if (effectiveAttended > totalClasses) effectiveAttended = totalClasses;
        this.attendancePercentage = Math.min(100.0, ((double) effectiveAttended / totalClasses) * 100.0);
        return this.attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) { 
        this.attendancePercentage = attendancePercentage; 
        if (totalClasses > 0) {
            this.attendedClasses = (int) Math.round(totalClasses * (attendancePercentage / 100.0));
        }
    }

    public int getTotalClasses() { 
        return totalClasses; 
    }

    public void setTotalClasses(int totalClasses) { 
        this.totalClasses = totalClasses; 
    }

    public int getAttendedClasses() { 
        return attendedClasses; 
    }

    public void setAttendedClasses(int attendedClasses) { 
        this.attendedClasses = attendedClasses; 
    }

    public int getDutyLeaveApprovedCount() { 
        return dutyLeaveApprovedCount; 
    }

    public void setDutyLeaveApprovedCount(int dutyLeaveApprovedCount) { 
        this.dutyLeaveApprovedCount = dutyLeaveApprovedCount; 
    }

    public boolean isAttendanceBelow70() {
        return attendancePercentage < 70.0;
    }

    public boolean isAttendanceSafe() {
        return attendancePercentage >= 75.0;
    }

    public boolean isHOD() {
        if (additionalInfo != null && additionalInfo.toUpperCase().contains("HOD")) return true;
        if (yearOrClass != null && yearOrClass.toUpperCase().contains("HOD")) return true;
        if (programme != null && programme.toUpperCase().contains("HOD")) return true;
        if (email != null && email.toLowerCase().startsWith("hod.")) return true;
        return false;
    }

    /**
     * Identifies the academic stream of this user: "Science", "Commerce", or "Arts".
     */
    public String getStream() {
        if (department != null && !department.isEmpty()) {
            String d = department.toLowerCase();
            if (d.contains("science") || d.contains("sci")) return "Science";
            if (d.contains("commerce") || d.contains("com")) return "Commerce";
            if (d.contains("arts") || d.contains("art")) return "Arts";
        }
        if (degree != null && !degree.isEmpty()) {
            String dg = degree.toLowerCase();
            if (dg.contains("b.sc") || dg.contains("science")) return "Science";
            if (dg.contains("b.com") || dg.contains("commerce")) return "Commerce";
            if (dg.contains("b.a") || dg.contains("arts")) return "Arts";
        }
        if (programme != null && !programme.isEmpty()) {
            String p = programme.toLowerCase();
            if (p.contains("b.sc") || p.contains("science")) return "Science";
            if (p.contains("b.com") || p.contains("commerce")) return "Commerce";
            if (p.contains("b.a") || p.contains("arts")) return "Arts";
        }
        if (id != null) {
            String upper = id.toUpperCase();
            if (upper.startsWith("SCI") || upper.contains("-SCI-")) return "Science";
            if (upper.startsWith("COM") || upper.contains("-COM-")) return "Commerce";
            if (upper.startsWith("ART") || upper.contains("-ART-")) return "Arts";
        }
        if (additionalInfo != null) {
            String info = additionalInfo.toLowerCase();
            if (info.contains("science")) return "Science";
            if (info.contains("commerce")) return "Commerce";
            if (info.contains("arts")) return "Arts";
        }
        return "";
    }

    /**
     * Determines whether this faculty/HOD can access the given student.
     * Principal has full visibility; Teachers and HODs only see students from their own stream.
     */
    public boolean canAccessStudent(User student) {
        if (student == null) return false;
        if (isPrincipal()) return true;
        if (isTeacherOrDirector() || isHOD()) {
            String myStream = getStream();
            if (myStream.isEmpty()) return true;
            String studentStream = student.getStream();
            return myStream.equalsIgnoreCase(studentStream);
        }
        return this.id != null && this.id.equals(student.getId());
    }

    /**
     * Returns true if this user holds an Event Incharge, Sports Director, Coach, or Coordinator designation.
     */
    public boolean isEventCoordinatorOrIncharge() {
        if (ROLE_EVENT_COORDINATOR.equalsIgnoreCase(role) || ROLE_ORGANIZER.equalsIgnoreCase(role)) {
            return true;
        }
        if (additionalInfo != null) {
            String info = additionalInfo.toLowerCase();
            if (info.contains("coordinator") || info.contains("incharge") || info.contains("in-charge") ||
                info.contains("coach") || info.contains("sports director")) {
                return true;
            }
        }
        if (yearOrClass != null) {
            String yc = yearOrClass.toLowerCase();
            if (yc.contains("coordinator") || yc.contains("incharge") || yc.contains("coach")) {
                return true;
            }
        }
        if (department != null && department.toLowerCase().contains("sports")) {
            return true;
        }
        return false;
    }

    /**
     * Checks if this user is the designated coordinator or incharge for a specific practice session or event.
     */
    public boolean isCoordinatorForSession(PracticeSession session, Event event) {
        if (session == null && event == null) return false;
        if (ROLE_EVENT_COORDINATOR.equalsIgnoreCase(role) || ROLE_ORGANIZER.equalsIgnoreCase(role)) {
            if (degree != null && !degree.isEmpty()) {
                if (session != null && degree.equalsIgnoreCase(session.getEventId())) return true;
                if (event != null && degree.equalsIgnoreCase(event.getId())) return true;
            }
            if (programme != null && !programme.isEmpty()) {
                if (session != null && session.getEventName() != null && programme.toLowerCase().contains(session.getEventName().toLowerCase())) return true;
                if (event != null && event.getName() != null && programme.toLowerCase().contains(event.getName().toLowerCase())) return true;
            }
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                if (session != null && session.getEventName() != null && additionalInfo.toLowerCase().contains(session.getEventName().toLowerCase())) return true;
                if (event != null && event.getName() != null && additionalInfo.toLowerCase().contains(event.getName().toLowerCase())) return true;
            }
            return true;
        }
        String myName = fullName != null ? fullName.trim().toLowerCase() : "";
        if (!myName.isEmpty()) {
            String cleanMy = myName.replace("coach", "").replace("dr.", "").replace("prof.", "")
                    .replace("(event incharge)", "").replace("(student event incharge)", "").replace("(faculty event incharge)", "").trim();
            if (session != null && session.getCoachName() != null) {
                String coach = session.getCoachName().trim().toLowerCase();
                String cleanCoach = coach.replace("coach", "").replace("dr.", "").replace("prof.", "").trim();
                if (coach.contains(myName) || coach.contains(cleanMy) || myName.contains(cleanCoach) || cleanMy.contains(cleanCoach)) {
                    return true;
                }
            }
            if (event != null && event.getCoordinatorName() != null) {
                String coord = event.getCoordinatorName().trim().toLowerCase();
                String cleanCoord = coord.replace("coach", "").replace("dr.", "").replace("prof.", "").trim();
                if (coord.contains(myName) || coord.contains(cleanMy) || myName.contains(cleanCoord) || cleanMy.contains(cleanCoord)) {
                    return true;
                }
            }
        }
        return isEventCoordinatorOrIncharge();
    }

    public String getAttendanceStatusBadgeText() {
        if (attendancePercentage < 70.0) {
            return "⚠️ " + String.format(java.util.Locale.US, "%.1f%% (Low Attendance)", attendancePercentage);
        } else if (attendancePercentage < 75.0) {
            return "⚡ " + String.format(java.util.Locale.US, "%.1f%% (Borderline)", attendancePercentage);
        } else {
            return "✅ " + String.format(java.util.Locale.US, "%.1f%% (Safe)", attendancePercentage);
        }
    }
}
