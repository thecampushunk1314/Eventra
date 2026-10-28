package com.example.sams;

import java.io.Serializable;

public class User implements Serializable {
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_ORGANIZER = "ORGANIZER";
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
    private String institutionType; // University, College, Higher Secondary, School
    private String dob; // e.g., "14/05/2008"
    private String yearOrClass; // e.g. "11th Standard", "2nd Year / Batch 2026"
    private String degree; // e.g. "MCA", "B.Tech", "Higher Secondary Certificate"
    private String subjects; // e.g. "Physics, Chemistry, Mathematics, English"
    private String address = ""; // Residential Address
    private boolean passwordChanged = false;

    // Academic Attendance Tracking
    private double attendancePercentage = 80.0;
    private int totalClasses = 120;
    private int attendedClasses = 96;
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
        this.department = department;
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

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

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
        return ROLE_TEACHER.equals(role) || ROLE_PROGRAMME_DIRECTOR.equals(role) || isPrincipal();
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
