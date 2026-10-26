// Eventra State Management
let currentRole = 'STUDENT'; // 'STUDENT' | 'TEACHER'
let currentMode = 'REGISTER'; // 'REGISTER' | 'LOGIN'
let teacherSubRole = 'DIRECTOR'; // 'DIRECTOR' | 'FACULTY'
let selectedStudentInst = 'UNIVERSITY'; // 'UNIVERSITY' | 'COLLEGE' | 'HIGHER_SEC' | 'SCHOOL'
let selectedTeacherInst = 'UNIVERSITY';
let lastRegisteredUser = null;

// Mock Local User Database with unified multi-buyer format
let usersDatabase = [
  // DEFAULT / UNIVERSITY BUYER
  {
    id: "MCA_2026_2650",
    fullName: "Esther Howard",
    email: "student@eventra.ac.in",
    phoneNumber: "9876543210",
    password: "123456789",
    role: "STUDENT",
    roleDisplayName: "Student",
    department: "Department of Computer Applications",
    institutionType: "UNIVERSITY",
    dob: "19/08/2002",
    yearOrClass: "2nd Year (Semester 4)",
    degree: "Master of Computer Applications (MCA)",
    subjects: "Cloud Computing, Distributed Databases, Advanced Java, Mobile Application Development, Web Frameworks",
    extra: "MCA (Batch 2026 | Roll: 2650)"
  },
  {
    id: "CSC_Snithi",
    fullName: "Snithi Sharma",
    email: "snithi.sharma@eventra.ac.in",
    phoneNumber: "9876512345",
    password: "123456789",
    role: "FACULTY",
    roleDisplayName: "Faculty Lecturer",
    department: "Computer Science (CSC)",
    institutionType: "UNIVERSITY",
    dob: "12/03/1990",
    yearOrClass: "Assistant Professor",
    degree: "M.Tech, Ph.D (Computer Science)",
    subjects: "Website Design & Systems, UX Architecture, Front-end Systems",
    extra: "Website Design & Systems (CSC_Snithi)"
  },
  {
    id: "CSC_Robert",
    fullName: "Dr. Robert Vance",
    email: "robert.vance@eventra.ac.in",
    phoneNumber: "9876523456",
    password: "123456789",
    role: "DIRECTOR",
    roleDisplayName: "Programme Director",
    department: "Computer Science (CSC)",
    institutionType: "UNIVERSITY",
    dob: "24/11/1982",
    yearOrClass: "Programme Director",
    degree: "Ph.D in Software Systems",
    subjects: "Project Leadership, Software Engineering & Research",
    extra: "Mastering Projects & Leadership"
  },
  {
    id: "ORG_2026_01",
    fullName: "Kavita Rao",
    email: "organizer@eventra.ac.in",
    phoneNumber: "9823000099",
    password: "123456789",
    role: "ORGANIZER",
    roleDisplayName: "Event Organizer",
    department: "Event Management",
    institutionType: "UNIVERSITY",
    dob: "14/04/2001",
    yearOrClass: "Lead Coordinator",
    degree: "Master of Business Administration (MBA)",
    subjects: "Campus Event Operations, Duty Leave Verification, Spot Pass Governance",
    extra: "Chief Event Coordinator"
  },

  // WAKANDA HIGHER SECONDARY SCHOOL (Wakanda HSS)
  // Admin-provisioned @eventra.ac.in accounts with default password 123456789
  {
    id: "SCI_25_001",
    fullName: "Aditya Kamat",
    email: "aditya.kamat@eventra.ac.in",
    phoneNumber: "9823000001",
    password: "123456789",
    role: "STUDENT",
    roleDisplayName: "Student (Science Stream)",
    department: "Science Division",
    institutionType: "HIGHER_SEC",
    dob: "05/01/2008",
    yearOrClass: "11th Standard (Class XI)",
    degree: "Higher Secondary Certificate (HSC)",
    subjects: "Physics, Chemistry, Mathematics, English, NSS Unit",
    extra: "Roll: SCI-25-001 | NSS Unit | Physics, Chem, Maths"
  },
  {
    id: "HSS_Shashikant",
    fullName: "Dr. Shashikant V. Mayekar",
    email: "shashikant.mayekar@eventra.ac.in",
    phoneNumber: "9823000001",
    password: "123456789",
    role: "DIRECTOR",
    roleDisplayName: "Principal (Higher Sec)",
    department: "Executive Administration",
    institutionType: "HIGHER_SEC",
    dob: "15/06/1972",
    yearOrClass: "Principal",
    degree: "Ph.D in Applied Mathematics",
    subjects: "Mathematics, Institutional Governance",
    extra: "Principal (Wakanda Higher Secondary School)"
  },
  {
    id: "HSS_Pramod",
    fullName: "Pramod Naik",
    email: "pramod.naik@eventra.ac.in",
    phoneNumber: "9823000003",
    password: "123456789",
    role: "DIRECTOR",
    roleDisplayName: "HOD Science",
    department: "Department of Science",
    institutionType: "HIGHER_SEC",
    dob: "08/09/1978",
    yearOrClass: "HOD Science",
    degree: "M.Sc Physics, B.Ed",
    subjects: "Physics, Science Laboratory",
    extra: "Head of Science Stream & Physics Lead"
  },

  // COLLISION RESOLUTION DEMOS:
  // Rule: If a person has the same name and surname (or duplicate collision),
  // emails are formatted as: rollno.name@eventra.ac.in
  {
    id: "SCI_25_055",
    fullName: "Kiran Kiran",
    email: "055.kiran@eventra.ac.in",
    phoneNumber: "9823000055",
    password: "123456789",
    role: "STUDENT",
    roleDisplayName: "Student (Identical Name & Surname)",
    department: "Science Division",
    institutionType: "HIGHER_SEC",
    dob: "22/07/2008",
    yearOrClass: "11th Standard (Class XI)",
    degree: "Higher Secondary Certificate (HSC)",
    subjects: "Physics, Chemistry, Biology, English",
    extra: "Collision Rule: 055.kiran@eventra.ac.in (First name & surname are identical 'Kiran Kiran')"
  }
];

// Institutional Email Generator with Collision Resolution
function generateInstitutionalEmail(fullName, rollOrId) {
  if (!fullName || !fullName.trim()) {
    const r = (rollOrId || '').toLowerCase().replace(/[^a-z0-9]/g, '') || '101';
    return `${r}.student@eventra.ac.in`;
  }

  const parts = fullName.trim().toLowerCase().replace(/[^a-z0-9\s]/g, '').split(/\s+/).filter(Boolean);
  const first = parts[0] || 'student';
  const last = parts.length > 1 ? parts[parts.length - 1] : '';
  const cleanRoll = (rollOrId || '').toLowerCase().replace(/[^a-z0-9]/g, '') || String(Math.floor(1000 + Math.random() * 9000));

  // Condition 1: Same name and surname (e.g. "Kiran Kiran" or single name)
  if (!last || first === last) {
    return `${cleanRoll}.${first}@eventra.ac.in`;
  }

  const standard = `${first}.${last}@eventra.ac.in`;

  // Condition 2: If another user already has this firstname.lastname@eventra.ac.in (collision)
  const hasCollision = usersDatabase.some(u => u.email && u.email.toLowerCase() === standard);
  if (hasCollision) {
    return `${cleanRoll}.${first}@eventra.ac.in`;
  }

  return standard;
}

function sanitizeCourse(c) {
  if (!c || !c.trim()) return "MCA";
  const str = c.trim();
  const match = str.match(/\(([^)]+)\)/);
  if (match && match[1]) return match[1].replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
  const words = str.replace(/[^a-zA-Z0-9\s]/g, '').trim().split(/\s+/);
  if (words.length === 1) return words[0].toUpperCase();
  if (words[0].length <= 5 && !['BACHELOR','MASTER'].includes(words[0].toUpperCase())) return words[0].toUpperCase();
  const acronym = words.filter(w => !['of','and','in','the'].includes(w.toLowerCase())).map(w => w[0].toUpperCase()).join('');
  return acronym || "MCA";
}

function sanitizeStream(s) {
  if (!s || !s.trim()) return "SCIENCE";
  const str = s.trim().toLowerCase();
  if (str.includes("sci")) return "SCIENCE";
  if (str.includes("comm")) return "COMMERCE";
  if (str.includes("art")) return "ARTS";
  if (str.includes("voc")) return "VOCATIONAL";
  const clean = s.replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
  return clean || "SCIENCE";
}

function sanitizeSchoolClass(c) {
  if (!c || !c.trim()) return "CLASS10";
  const digits = c.replace(/[^0-9]/g, '');
  if (digits) return "CLASS" + digits;
  const clean = c.replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
  return clean || "CLASS10";
}

function sanitizeYear(y) {
  if (!y) return "2026";
  const match = y.match(/\b(20\d{2})\b/);
  if (match) return match[1];
  const digits = y.replace(/[^0-9]/g, '');
  if (digits.length === 4) return digits;
  if (digits.length === 2) return "20" + digits;
  return "2026";
}

function sanitizeRoll(r) {
  if (!r) return String(Math.floor(1000 + Math.random() * 9000));
  const cleaned = r.replace(/[^a-zA-Z0-9]/g, '');
  return cleaned || String(Math.floor(1000 + Math.random() * 9000));
}

function generateStudentId(instType, courseOrClass, year, roll) {
  let prefix;
  if (instType === 'HIGHER_SEC') {
    prefix = sanitizeStream(courseOrClass);
  } else if (instType === 'SCHOOL') {
    prefix = sanitizeSchoolClass(courseOrClass);
  } else {
    prefix = sanitizeCourse(courseOrClass);
  }
  const y = sanitizeYear(year);
  const r = sanitizeRoll(roll);
  let candidate = `${prefix}_${y}_${r}`;
  let suffix = 1;
  while (usersDatabase.some(u => u.id.toLowerCase() === candidate.toLowerCase())) {
    candidate = `${prefix}_${y}_${r}_${suffix}`;
    suffix++;
  }
  return candidate;
}

function sanitizeTeacherDept(dept) {
  if (!dept || !dept.trim()) return "CSC";
  const trimmed = dept.trim();
  const match = trimmed.match(/\(([^)]+)\)/);
  if (match && match[1]) return match[1].replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
  const lower = trimmed.toLowerCase();
  if (lower.includes("computer science") || lower.includes("computing") || lower.includes("mca") || lower.includes("bca") || lower.includes("cse")) return "CSC";
  if (lower.includes("math")) return "MATH";
  if (lower.includes("physic")) return "PHY";
  if (lower.includes("chem")) return "CHEM";
  if (lower.includes("bio")) return "BIO";
  if (lower.includes("english")) return "ENG";
  if (lower.includes("commerce")) return "COMM";
  const words = trimmed.replace(/[^a-zA-Z0-9\s]/g, '').trim().split(/\s+/);
  if (words.length === 1) return words[0].toUpperCase();
  const acronym = words.filter(w => !['of','and','in','department','school'].includes(w.toLowerCase())).map(w => w[0].toUpperCase()).join('');
  return acronym || "CSC";
}

function sanitizeTeacherName(fullName) {
  if (!fullName || !fullName.trim()) return "Snithi";
  const cleaned = fullName.trim().replace(/^(Dr\.|Prof\.|Mr\.|Mrs\.|Ms\.)\s*/i, '');
  const words = cleaned.trim().split(/\s+/);
  const first = words[0].replace(/[^a-zA-Z0-9]/g, '');
  if (!first) return "Snithi";
  return first.charAt(0).toUpperCase() + first.substring(1).toLowerCase();
}

function generateTeacherId(dept, fullName) {
  const d = sanitizeTeacherDept(dept);
  const n = sanitizeTeacherName(fullName);
  let candidate = `${d}_${n}`;
  let suffix = 2;
  while (usersDatabase.some(u => u.id.toLowerCase() === candidate.toLowerCase())) {
    candidate = `${d}_${n}_${suffix}`;
    suffix++;
  }
  return candidate;
}

// Institution selection for Student
function setStudentInstitution(type) {
  selectedStudentInst = type;
  document.querySelectorAll('.student-inst-chip').forEach(el => el.classList.remove('active'));
  const activeEl = document.getElementById('chipInst_' + type);
  if (activeEl) activeEl.classList.add('active');

  const labelCourse = document.getElementById('labelStudentCourse');
  const inputCourse = document.getElementById('regStudentCourse');
  const chip1 = document.getElementById('formulaChipStudent1');
  const formulaPreview = document.getElementById('studentFormulaPreview');

  if (type === 'COLLEGE') {
    labelCourse.textContent = "Degree / Course (Prefix for ID)";
    inputCourse.placeholder = "e.g. BCA, B.Sc, B.Com, BA";
    chip1.textContent = "Course";
    formulaPreview.textContent = "➔ BCA_2026_1045";
  } else if (type === 'HIGHER_SEC') {
    labelCourse.textContent = "Stream / Major (Prefix for ID)";
    inputCourse.placeholder = "e.g. Science, Commerce, Arts";
    chip1.textContent = "Stream";
    formulaPreview.textContent = "➔ SCIENCE_2026_1204";
  } else if (type === 'SCHOOL') {
    labelCourse.textContent = "Class / Standard (Prefix for ID)";
    inputCourse.placeholder = "e.g. Class 10, Class 8, 10A";
    chip1.textContent = "Class";
    formulaPreview.textContent = "➔ CLASS10_2026_42";
  } else {
    labelCourse.textContent = "Course / Programme (Prefix for ID)";
    inputCourse.placeholder = "e.g. MCA, BCA, B.Tech, MBA";
    chip1.textContent = "Course";
    formulaPreview.textContent = "➔ MCA_2026_2650";
  }
}

// Institution selection for Teacher
function setTeacherInstitution(type) {
  selectedTeacherInst = type;
  document.querySelectorAll('.teacher-inst-chip').forEach(el => el.classList.remove('active'));
  const activeEl = document.getElementById('chipTeacherInst_' + type);
  if (activeEl) activeEl.classList.add('active');

  const inputDept = document.getElementById('regTeacherDept');
  if (type === 'COLLEGE') {
    inputDept.placeholder = "e.g. Computer Science, Economics";
  } else if (type === 'HIGHER_SEC') {
    inputDept.placeholder = "e.g. Physics Dept, Commerce";
  } else if (type === 'SCHOOL') {
    inputDept.placeholder = "e.g. Mathematics, Science, English";
  } else {
    inputDept.placeholder = "e.g. Department of Computer Science (CSC)";
  }
}

// Switch Role
function switchRole(role) {
  currentRole = role;
  document.getElementById('btnRoleStudent').classList.toggle('active', role === 'STUDENT');
  document.getElementById('btnRoleTeacher').classList.toggle('active', role === 'TEACHER');
  document.getElementById('appTabStudent').classList.toggle('active', role === 'STUDENT');
  document.getElementById('appTabTeacher').classList.toggle('active', role === 'TEACHER');
  updateFormVisibility();
  if (role === 'TEACHER') {
    switchMainPortalView('TEACHER_PORTAL');
  } else {
    switchMainPortalView('MOBILE_APP');
  }
}

// Switch Mode
function switchMode(mode) {
  currentMode = mode;
  document.getElementById('btnModeRegister').classList.toggle('active', mode === 'REGISTER');
  document.getElementById('btnModeLogIn').classList.toggle('active', mode === 'LOGIN');
  document.getElementById('appTabRegister').classList.toggle('active', mode === 'REGISTER');
  document.getElementById('appTabLogIn').classList.toggle('active', mode === 'LOGIN');
  updateFormVisibility();
}

// Sub-role toggle for Teacher vs Programme Director
function setTeacherSubRole(subRole) {
  teacherSubRole = subRole;
  document.getElementById('toggleDirector').classList.toggle('active', subRole === 'DIRECTOR');
  document.getElementById('toggleFaculty').classList.toggle('active', subRole === 'FACULTY');

  const labelField = document.getElementById('labelProgrammeField');
  const inputProg = document.getElementById('regTeacherProgramme');
  const submitBtn = document.getElementById('btnSubmitTeacherReg');

  if (subRole === 'DIRECTOR') {
    labelField.textContent = "Supervised / Directed Programme";
    inputProg.placeholder = "e.g. Mastering Projects & Leadership";
    submitBtn.innerHTML = 'Create account & Get Director ID <span class="arrow-icon">→</span>';
  } else {
    labelField.textContent = "Teaching Modules / Specialization";
    inputProg.placeholder = "e.g. OOP, Data Structures, Web Systems";
    submitBtn.innerHTML = 'Create account & Get Faculty ID <span class="arrow-icon">→</span>';
  }
}

function updateFormVisibility() {
  document.getElementById('formStudentRegister').classList.remove('active');
  document.getElementById('formTeacherRegister').classList.remove('active');
  document.getElementById('formUnifiedSignIn').classList.remove('active');

  if (currentMode === 'LOGIN') {
    document.getElementById('formUnifiedSignIn').classList.add('active');
  } else {
    if (currentRole === 'STUDENT') {
      document.getElementById('formStudentRegister').classList.add('active');
    } else {
      document.getElementById('formTeacherRegister').classList.add('active');
    }
  }
}

function handleStudentRegister(event) {
  event.preventDefault();

  const name = document.getElementById('regStudentName').value.trim();
  const phone = document.getElementById('regStudentPhone').value.trim();
  const courseInput = document.getElementById('regStudentCourse') ? document.getElementById('regStudentCourse').value.trim() : "MCA";
  const roll = document.getElementById('regStudentRoll').value.trim();
  const year = document.getElementById('regStudentYear').value.trim();
  const email = document.getElementById('regStudentEmail').value.trim();
  const dept = document.getElementById('regStudentDept').value.trim();
  const password = document.getElementById('regStudentPassword').value;

  if (usersDatabase.some(u => u.email.toLowerCase() === email.toLowerCase())) {
    showToast("An account with this email already exists!");
    return;
  }

  const generatedId = generateStudentId(selectedStudentInst, courseInput, year, roll);
  const newUser = {
    id: generatedId,
    fullName: name,
    email: email,
    phoneNumber: phone,
    password: password,
    role: "STUDENT",
    roleDisplayName: "Student",
    department: dept || "Department of Computer Applications",
    institutionType: selectedStudentInst,
    extra: `${courseInput} (Batch ${sanitizeYear(year)} | Roll: ${sanitizeRoll(roll)})`
  };

  usersDatabase.push(newUser);
  lastRegisteredUser = newUser;
  showModal(newUser);
  event.target.reset();
}

function handleTeacherRegister(event) {
  event.preventDefault();

  const name = document.getElementById('regTeacherName').value.trim();
  const phone = document.getElementById('regTeacherPhone').value.trim();
  const empId = document.getElementById('regTeacherEmpId').value.trim();
  const dept = document.getElementById('regTeacherDept').value.trim();
  const email = document.getElementById('regTeacherEmail').value.trim();
  const programme = document.getElementById('regTeacherProgramme').value.trim();
  const password = document.getElementById('regTeacherPassword').value;

  if (usersDatabase.some(u => u.email.toLowerCase() === email.toLowerCase())) {
    showToast("An account with this email already exists!");
    return;
  }

  const generatedId = generateTeacherId(dept, name);
  const roleName = teacherSubRole === 'DIRECTOR' ? "Programme Director" : "Faculty Lecturer";

  const newUser = {
    id: generatedId,
    fullName: name,
    email: email,
    phoneNumber: phone,
    password: password,
    role: teacherSubRole,
    roleDisplayName: roleName,
    department: dept || "Computer Science (CSC)",
    institutionType: selectedTeacherInst,
    extra: `${roleName} (${empId || 'EMP-3042'})`
  };

  usersDatabase.push(newUser);
  lastRegisteredUser = newUser;
  showModal(newUser);
  event.target.reset();
}

// Unified Authentication: checks University Email, Phone Number, or ID
function handleUnifiedSignIn(event) {
  event.preventDefault();

  const query = document.getElementById('loginUnifiedIdentifier').value.trim().toLowerCase();
  const password = document.getElementById('loginUnifiedPassword').value;
  const cleanPhone = query.replace(/[^0-9]/g, '');

  const found = usersDatabase.find(u => {
    const matchEmail = u.email && u.email.toLowerCase() === query;
    const matchId = u.id && u.id.toLowerCase() === query;
    let matchPhone = false;
    if (u.phoneNumber && cleanPhone) {
      const uPhoneClean = u.phoneNumber.replace(/[^0-9]/g, '');
      matchPhone = uPhoneClean === cleanPhone || (cleanPhone.length >= 10 && uPhoneClean.endsWith(cleanPhone));
    }
    return (matchEmail || matchId || matchPhone) && u.password === password;
  });

  if (found) {
    showToast(`Welcome to Eventra, ${found.fullName}!`);
    if (found.role === 'DIRECTOR' || found.role === 'FACULTY') {
      switchMainPortalView('TEACHER_PORTAL');
      const avatar = document.getElementById('tTopAvatar');
      const name = document.getElementById('tTopName');
      const role = document.getElementById('tTopRole');
      if (avatar) avatar.textContent = found.fullName.split(' ').map(p => p[0]).slice(0, 2).join('');
      if (name) name.textContent = found.fullName;
      if (role) role.textContent = `${found.roleDisplayName} • @eventra.ac.in`;
    } else {
      switchMainPortalView('MOBILE_APP');
      renderDashboard(found);
    }
  } else {
    showToast("Invalid credentials. Please enter your registered Email, Phone, or ID.");
  }
}

function autofillDemo(type) {
  const inputId = document.getElementById('loginUnifiedIdentifier');
  const inputPass = document.getElementById('loginUnifiedPassword');
  inputPass.value = "123456789";

  if (type === 'HSS_STUDENT' || type === 'SCI') {
    inputId.value = "aditya.kamat@eventra.ac.in";
    showToast("🔬 Wakanda HSS Student: aditya.kamat@eventra.ac.in (Default pass: 123456789)");
  } else if (type === 'HSS_PRINCIPAL') {
    inputId.value = "shashikant.mayekar@eventra.ac.in";
    showToast("🏛️ Wakanda HSS Principal: shashikant.mayekar@eventra.ac.in (Default pass: 123456789)");
  } else if (type === 'HSS_HOD') {
    inputId.value = "pramod.naik@eventra.ac.in";
    showToast("🔬 HOD Science: pramod.naik@eventra.ac.in (Default pass: 123456789)");
  } else if (type === 'STUDENT' || type === 'UNIVERSITY_STUDENT') {
    inputId.value = "student@eventra.ac.in";
    showToast("🎓 University Demo: student@eventra.ac.in (Default pass: 123456789)");
  } else if (type === 'SAME_NAME_COLLISION') {
    inputId.value = "42.ananya@eventra.ac.in";
    showToast("⚡ Collision Demo (Duplicate Name): 42.ananya@eventra.ac.in (Pass: 123456789)");
  } else if (type === 'IDENTICAL_NAME_SURNAME') {
    inputId.value = "055.kiran@eventra.ac.in";
    showToast("⚡ Collision Demo (Same Name & Surname): 055.kiran@eventra.ac.in (Pass: 123456789)");
  }
}

// Moodle-style Password Reset Modal Handlers
function openResetModal() {
  const currentLoginInput = document.getElementById('loginUnifiedIdentifier').value.trim();
  const resetIdInput = document.getElementById('resetIdentifier');
  if (resetIdInput && currentLoginInput) {
    resetIdInput.value = currentLoginInput;
  }
  const modal = document.getElementById('passwordResetModal');
  if (modal) modal.classList.add('active');
}

function closeResetModal() {
  const modal = document.getElementById('passwordResetModal');
  if (modal) modal.classList.remove('active');
}

function handlePasswordResetSubmit(event) {
  event.preventDefault();
  const identifier = document.getElementById('resetIdentifier').value.trim().toLowerCase();
  const currentPass = document.getElementById('resetCurrentPassword').value;
  const newPass = document.getElementById('resetNewPassword').value;

  const found = usersDatabase.find(u => 
    (u.email && u.email.toLowerCase() === identifier) ||
    (u.id && u.id.toLowerCase() === identifier)
  );

  if (!found) {
    showToast("❌ User not found with this email (@eventra.ac.in) or ID.");
    return;
  }

  if (found.password !== currentPass && currentPass !== '123456789') {
    showToast("❌ Current password incorrect. Default password is 123456789.");
    return;
  }

  found.password = newPass;
  closeResetModal();
  document.getElementById('loginUnifiedIdentifier').value = found.email;
  document.getElementById('loginUnifiedPassword').value = newPass;
  showToast(`✅ Password successfully updated for ${found.fullName}! Please sign in.`);
}

function selectBuyerPersona(buyer) {
  const regCourse = document.getElementById('regStudentCourse');
  const regRoll = document.getElementById('regStudentRoll');
  const regName = document.getElementById('regStudentName');
  const regDept = document.getElementById('regStudentDept');
  const phoneLabel = document.getElementById('phoneInstLabel');

  // Update top buyer pill buttons
  const pills = document.querySelectorAll('.buyer-pill');
  pills.forEach(p => p.classList.remove('active'));

  setStudentInstitution('HIGHER_SEC');
  setTeacherInstitution('HIGHER_SEC');
  if (regCourse) regCourse.value = "Science";
  if (regRoll) regRoll.value = "001";
  if (regName) regName.value = "Aditya Kamat";
  if (regDept) regDept.value = "Department of Science";
  if (phoneLabel) phoneLabel.innerHTML = "🔬 <strong>Wakanda Higher Secondary</strong>";
  showToast("🔬 Activated: Wakanda Higher Secondary School (Wakanda HSS)");
  } else {
    setStudentInstitution('UNIVERSITY');
    setTeacherInstitution('UNIVERSITY');
    if (regCourse) regCourse.value = "MCA";
    if (regRoll) regRoll.value = "2650";
    if (regName) regName.value = "Esther Howard";
    if (regDept) regDept.value = "Department of Computer Applications";
    if (phoneLabel) phoneLabel.innerHTML = "🏛️ <strong>University Campus</strong>";
    if (pills[0]) pills[0].classList.add('active');
    showToast("🎓 Activated University / Engineering College");
  }
}

// Architecture Workbench Tab Switcher
function switchWorkbenchTab(tab) {
  const tabs = {
    'ROSTER': { btn: 'wbTabRoster', view: 'wbViewRoster' },
    'BUYERS': { btn: 'wbTabBuyers', view: 'wbViewBuyers' },
    'SCHEMA': { btn: 'wbTabSchema', view: 'wbViewSchema' }
  };

  Object.keys(tabs).forEach(key => {
    const btn = document.getElementById(tabs[key].btn);
    const view = document.getElementById(tabs[key].view);
    if (btn) btn.classList.toggle('active', key === tab);
    if (view) view.classList.toggle('active', key === tab);
  });
}

// Live Real-Time Password Strength Meter
function updatePasswordStrength(val) {
  const bar1 = document.getElementById('strBar1');
  const bar2 = document.getElementById('strBar2');
  const bar3 = document.getElementById('strBar3');
  const text = document.getElementById('strengthText');
  if (!bar1 || !bar2 || !bar3 || !text) return;

  bar1.className = 'strength-bar';
  bar2.className = 'strength-bar';
  bar3.className = 'strength-bar';

  if (!val || val.length === 0) {
    text.textContent = 'Password Strength: -';
    text.style.color = '#64748B';
    return;
  }

  let score = 0;
  if (val.length >= 6) score++;
  if (val.length >= 8 && (/[0-9]/.test(val) || /[^a-zA-Z0-9]/.test(val))) score++;
  if (val.length >= 10 && /[A-Z]/.test(val) && /[0-9]/.test(val) && /[^a-zA-Z0-9]/.test(val)) score++;

  if (score === 1) {
    bar1.className = 'strength-bar weak';
    text.textContent = 'Password Strength: Weak';
    text.style.color = '#EF4444';
  } else if (score === 2) {
    bar1.className = 'strength-bar medium';
    bar2.className = 'strength-bar medium';
    text.textContent = 'Password Strength: Moderate';
    text.style.color = '#F59E0B';
  } else if (score >= 3) {
    bar1.className = 'strength-bar strong';
    bar2.className = 'strength-bar strong';
    bar3.className = 'strength-bar strong';
    text.textContent = 'Password Strength: Strong & Secure';
    text.style.color = '#10B981';
  }
}

// Modal navigation helpers
function modalGoToDashboard() {
  proceedToDashboardFromModal();
}

function modalGoToLogIn() {
  proceedToSignInFromModal();
}

// Background status check for Node.js API
async function checkBackendStatus() {
  const statusBadge = document.getElementById('backendStatusBadge');
  if (!statusBadge) return;
  try {
    const res = await fetch('http://localhost:3000/api/health');
    if (res.ok) {
      const data = await res.json();
      statusBadge.innerHTML = '🟢 <strong>Node.js Backend Online</strong> (Port 3000: higher_secondary_db connected - Wakanda HSS)';
      statusBadge.className = 'backend-badge online';
    } else {
      statusBadge.innerHTML = '🟡 <strong>Backend Standby</strong> (Running in offline preview mode)';
      statusBadge.className = 'backend-badge standby';
    }
  } catch (err) {
    statusBadge.innerHTML = '⚡ <strong>Frontend Preview Mode</strong> (Ready to connect with <code>npm start</code> in backend/)';
    statusBadge.className = 'backend-badge preview';
  }
}
setTimeout(checkBackendStatus, 1000);

function showModal(user) {
  document.getElementById('modalGeneratedId').textContent = user.id;
  document.getElementById('modalUserName').textContent = user.fullName;
  document.getElementById('modalUserDept').textContent = user.department;
  document.getElementById('modalRoleChip').textContent = user.roleDisplayName.toUpperCase();
  document.getElementById('idModal').classList.add('active');
}

function hideModal() {
  document.getElementById('idModal').classList.remove('active');
}

function copyModalId() {
  if (!lastRegisteredUser) return;
  navigator.clipboard.writeText(lastRegisteredUser.id).then(() => {
    showToast(`Copied ID: ${lastRegisteredUser.id}`);
  }).catch(() => {
    showToast(`ID: ${lastRegisteredUser.id}`);
  });
}

function proceedToSignInFromModal() {
  hideModal();
  switchMode('LOGIN');
  if (lastRegisteredUser) {
    document.getElementById('loginUnifiedIdentifier').value = lastRegisteredUser.email;
  }
}

function proceedToDashboardFromModal() {
  hideModal();
  if (lastRegisteredUser) {
    renderDashboard(lastRegisteredUser);
  }
}

function renderDashboard(user) {
  document.getElementById('authView').style.display = 'none';
  document.getElementById('dashboardView').style.display = 'block';

  document.getElementById('dashAvatarInitial').textContent = user.fullName.charAt(0).toUpperCase();
  document.getElementById('dashUserName').textContent = user.fullName;
  document.getElementById('dashUserIdBadge').textContent = user.id;
  document.getElementById('dashRoleBadge').textContent = user.roleDisplayName;

  const heroTitle = document.getElementById('dashHeroTitle');
  if (user.role === 'STUDENT') {
    heroTitle.textContent = "Mastering projects with management";
  } else if (user.role === 'DIRECTOR') {
    heroTitle.textContent = "Programme Oversight & Module Reviews";
  } else {
    heroTitle.textContent = "Curriculum Delivery & Student Projects";
  }
}

function logoutDashboard() {
  document.getElementById('dashboardView').style.display = 'none';
  document.getElementById('authView').style.display = 'block';
  showToast("Logged out successfully");
}

function showToast(msg) {
  const toast = document.getElementById('toast');
  toast.textContent = msg;
  toast.classList.add('active');
  setTimeout(() => toast.classList.remove('active'), 3200);
}

/* ========================================================================== */
/* TEACHER PORTAL STATE, DATA MODELS & CONTROLLERS (WAKANDA HSS)              */
/* ========================================================================== */

let teacherCurrentStandard = '11th';
let currentFacultyDrillStream = 'Arts';
let currentStudentStream = 'Arts';
let currentTeacherPersonaIndex = 0; // 0 = Pramod Naik (HOD), 1 = Dr. Shashikant Mayekar (Principal)

const teacherPersonas = [
  {
    name: "Pramod Naik",
    role: "HOD Science Stream • @eventra.ac.in",
    avatar: "PN"
  },
  {
    name: "Dr. Shashikant V. Mayekar",
    role: "Principal (Executive Head) • @eventra.ac.in",
    avatar: "SM"
  }
];

// Authentic Wakanda HSS Faculty Data for 11th & 12th standards
const hssFacultyData = {
  Arts: {
    '11th': [
      { sr: 1, name: "Geeta Mehta", subjects: "Marathi", phone: "9370178529" },
      { sr: 2, name: "Anita Verlekar", subjects: "English", phone: "9822587410" },
      { sr: 3, name: "Ramesh Naik", subjects: "History", phone: "9012345678" },
      { sr: 4, name: "Shruti Kamat", subjects: "Economics", phone: "7887465123" },
      { sr: 5, name: "Pradnya Sawant", subjects: "Political Science", phone: "9156784320" },
      { sr: 6, name: "Sanjay Bandodkar", subjects: "HOD Arts / Geography", phone: "9823000005" }
    ],
    '12th': [
      { sr: 1, name: "Geeta Mehta", subjects: "Advanced Marathi Literature", phone: "9370178529" },
      { sr: 2, name: "Anita Verlekar", subjects: "English Special & Composition", phone: "9822587410" },
      { sr: 3, name: "Ramesh Naik", subjects: "Modern World & Indian History", phone: "9012345678" },
      { sr: 4, name: "Shruti Kamat", subjects: "Macro Economics & Development", phone: "7887465123" },
      { sr: 5, name: "Pradnya Sawant", subjects: "Political Science & Constitution", phone: "9156784320" },
      { sr: 6, name: "Maria Rodrigues", subjects: "Vice Principal / Functional English", phone: "9823000002" }
    ]
  },
  Commerce: {
    '11th': [
      { sr: 1, name: "Sujata Kamat", subjects: "HOD / Book Keeping & Accountancy", phone: "9823000004" },
      { sr: 2, name: "Aarti Faldesai", subjects: "Business Studies & Organization", phone: "9823000009" },
      { sr: 3, name: "Nitin Shenvi", subjects: "Economics & Banking Principles", phone: "9823000010" },
      { sr: 4, name: "Dr. Shashikant V. Mayekar", subjects: "Principal / Commercial Mathematics", phone: "9823000001" },
      { sr: 5, name: "Maria Rodrigues", subjects: "English & Secretarial Practice", phone: "9823000002" },
      { sr: 6, name: "Vinayak Borkar", subjects: "Physical Education & NSS", phone: "9823000014" }
    ],
    '12th': [
      { sr: 1, name: "Sujata Kamat", subjects: "HOD / Corporate Accounting & Partnerships", phone: "9823000004" },
      { sr: 2, name: "Aarti Faldesai", subjects: "Business Finance & Marketing", phone: "9823000009" },
      { sr: 3, name: "Nitin Shenvi", subjects: "Applied Economics & Auditing", phone: "9823000010" },
      { sr: 4, name: "Dr. Shashikant V. Mayekar", subjects: "Principal / Statistics & Business Maths", phone: "9823000001" },
      { sr: 5, name: "Maria Rodrigues", subjects: "Business Communication & English", phone: "9823000002" },
      { sr: 6, name: "Vinayak Borkar", subjects: "Health & Sports Science", phone: "9823000014" }
    ]
  },
  Science: {
    '11th': [
      { sr: 1, name: "Dr. Neha Parab", subjects: "Physics Mechanics & Thermodynamics", phone: "9023456781" },
      { sr: 2, name: "Vikram Bhat", subjects: "Inorganic & Organic Chemistry", phone: "8855667788" },
      { sr: 3, name: "Sangeeta Dessai", subjects: "Botany & Cell Biology", phone: "9321123344" },
      { sr: 4, name: "Pranav Kolekar", subjects: "Algebra & Coordinate Geometry", phone: "8765432109" },
      { sr: 5, name: "Amit Gaonkar", subjects: "Computer Science (Python & SQL)", phone: "8899001122" },
      { sr: 6, name: "Pramod Naik", subjects: "HOD Science / Physics Practicals", phone: "9823000003" }
    ],
    '12th': [
      { sr: 1, name: "Dr. Neha Parab", subjects: "Physics Optics & Modern Electrodynamics", phone: "9023456781" },
      { sr: 2, name: "Vikram Bhat", subjects: "Physical Chemistry & Reaction Kinetics", phone: "8855667788" },
      { sr: 3, name: "Sangeeta Dessai", subjects: "Zoology, Genetics & Biotechnology", phone: "9321123344" },
      { sr: 4, name: "Pranav Kolekar", subjects: "Calculus, Vectors & Probability", phone: "8765432109" },
      { sr: 5, name: "Amit Gaonkar", subjects: "Computer Science (Data Structures & OOP)", phone: "8899001122" },
      { sr: 6, name: "Pramod Naik", subjects: "HOD Science / Advanced Physics Lab", phone: "9823000003" }
    ]
  },
  Vocational: {
    '11th': [
      { sr: 1, name: "Francis D'Costa", subjects: "HOD / Office Management & Secretarial Practice", phone: "9823000006" },
      { sr: 2, name: "Clifford Fernandes", subjects: "Electronic Technology & Hardware Maintenance", phone: "9823000013" },
      { sr: 3, name: "Nitin Shenvi", subjects: "Accountancy & Costing (Vocational)", phone: "9823000010" },
      { sr: 4, name: "Amit Gaonkar", subjects: "Desktop Publishing & Computer Applications", phone: "8899001122" },
      { sr: 5, name: "Maria Rodrigues", subjects: "Functional & Workplace English", phone: "9823000002" },
      { sr: 6, name: "Vinayak Borkar", subjects: "Physical Fitness & Industrial Safety", phone: "9823000014" }
    ],
    '12th': [
      { sr: 1, name: "Francis D'Costa", subjects: "HOD / Advanced Office Administration & CRM", phone: "9823000006" },
      { sr: 2, name: "Clifford Fernandes", subjects: "Microcontrollers & Network Maintenance", phone: "9823000013" },
      { sr: 3, name: "Nitin Shenvi", subjects: "Auditing & Taxation Practice", phone: "9823000010" },
      { sr: 4, name: "Amit Gaonkar", subjects: "Web Graphics, Typography & DTP Lab", phone: "8899001122" },
      { sr: 5, name: "Maria Rodrigues", subjects: "Business Presentation & Communication", phone: "9823000002" },
      { sr: 6, name: "Vinayak Borkar", subjects: "First Aid & Community NSS Projects", phone: "9823000014" }
    ]
  }
};

// Authentic Wakanda HSS Student Directory (Image 2 Structure)
const hssStudentsData = {
  Arts: [
    { roll: "AR01", year: "2024", name: "Aditi Sharma", email: "aditi.sharma@eventra.ac.in", pass: "123456789" },
    { roll: "AR02", year: "2024", name: "Rohan Patil", email: "rohan.patil@eventra.ac.in", pass: "123456789" },
    { roll: "AR03", year: "2025", name: "Sneha Desai", email: "sneha.desai@eventra.ac.in", pass: "123456789" },
    { roll: "AR04", year: "2025", name: "Pratik Kulkarni", email: "pratik.kulkarni@eventra.ac.in", pass: "123456789" },
    { roll: "AR05", year: "2025", name: "Neha Iyer", email: "neha.iyer@eventra.ac.in", pass: "123456789" },
    { roll: "AR06", year: "2025", name: "Rutuja More", email: "rutuja.more@eventra.ac.in", pass: "123456789" }
  ],
  Commerce: [
    { roll: "CO001", year: "2024", name: "Anil Shah", email: "anil.shah@eventra.ac.in", pass: "123456789" },
    { roll: "CO002", year: "2024", name: "Pooja Mehta", email: "pooja.mehta@eventra.ac.in", pass: "123456789" },
    { roll: "CO003", year: "2024", name: "Karan Singh", email: "karan.singh@eventra.ac.in", pass: "123456789" },
    { roll: "CO004", year: "2025", name: "Isha Patel", email: "isha.patel@eventra.ac.in", pass: "123456789" },
    { roll: "CO005", year: "2025", name: "Sahil Gupta", email: "sahil.gupta@eventra.ac.in", pass: "123456789" },
    { roll: "CO006", year: "2025", name: "Tanvi Joshi", email: "tanvi.joshi@eventra.ac.in", pass: "123456789" }
  ],
  Science: [
    { roll: "SC001", year: "2024", name: "Aditya Kamat", email: "aditya.kamat@eventra.ac.in", pass: "123456789" },
    { roll: "SC002", year: "2024", name: "Riya Patil", email: "riya.patil@eventra.ac.in", pass: "123456789" },
    { roll: "SC003", year: "2025", name: "Pallavi Deshmukh", email: "pallavi.deshmukh@eventra.ac.in", pass: "123456789" },
    { roll: "SC004", year: "2025", name: "Siddharth Kulkarni", email: "siddharth.kulkarni@eventra.ac.in", pass: "123456789" },
    { roll: "SC005", year: "2025", name: "Shruti Nair", email: "shruti.nair@eventra.ac.in", pass: "123456789" },
    { roll: "SC006", year: "2025", name: "Omkar Bhosale", email: "omkar.bhosale@eventra.ac.in", pass: "123456789" },
    // Collision resolution rule demo student:
    { roll: "SC055", year: "2025", name: "Kiran Kiran", email: "055.kiran@eventra.ac.in", pass: "123456789", isCollisionDemo: true }
  ],
  Vocational: [
    { roll: "VO001", year: "2024", name: "Sahil Khan", email: "sahil.khan@eventra.ac.in", pass: "123456789" },
    { roll: "VO002", year: "2024", name: "Pooja Nair", email: "pooja.nair@eventra.ac.in", pass: "123456789" },
    { roll: "VO003", year: "2025", name: "Arjun Shetty", email: "arjun.shetty@eventra.ac.in", pass: "123456789" },
    { roll: "VO004", year: "2025", name: "Meera Iyer", email: "meera.iyer@eventra.ac.in", pass: "123456789" },
    { roll: "VO005", year: "2025", name: "Rohit Yadav", email: "rohit.yadav@eventra.ac.in", pass: "123456789" },
    { roll: "VO006", year: "2025", name: "Snehal Patil", email: "snehal.patil@eventra.ac.in", pass: "123456789" }
  ]
};

// Switch Master View (Teacher Portal vs Mobile View)
function switchMainPortalView(view) {
  const teacherContainer = document.getElementById('teacherPortalContainer');
  const mobileContainer = document.getElementById('mobileAppContainer');
  const btnTeacher = document.getElementById('btnSwitchTeacherPortal');
  const btnMobile = document.getElementById('btnSwitchMobileApp');

  if (view === 'TEACHER_PORTAL') {
    if (teacherContainer) teacherContainer.style.display = 'flex';
    if (mobileContainer) mobileContainer.style.display = 'none';
    if (btnTeacher) btnTeacher.classList.add('active');
    if (btnMobile) btnMobile.classList.remove('active');
    showTeacherTab('departments');
    showToast("👨‍🏫 Switched to Wakanda HSS Teacher Interface");
  } else {
    if (teacherContainer) teacherContainer.style.display = 'none';
    if (mobileContainer) mobileContainer.style.display = 'flex';
    if (btnTeacher) btnTeacher.classList.remove('active');
    if (btnMobile) btnMobile.classList.add('active');
    showToast("📱 Switched to Student Mobile App View");
  }
}

// Switch Teacher Sidebar Nav Tabs
function showTeacherTab(tabName) {
  const tabIds = ['dashboard', 'departments', 'faculty', 'students', 'timetable', 'announcements', 'contact'];
  
  tabIds.forEach(id => {
    const navBtn = document.getElementById('tNav_' + id);
    if (navBtn) navBtn.classList.toggle('active', id === tabName);

    const viewSec = document.getElementById('tView_' + id);
    if (viewSec) viewSec.classList.remove('active');
  });

  const drillSec = document.getElementById('tView_facultyDrilldown');
  if (drillSec) drillSec.classList.remove('active');

  const activeSec = document.getElementById('tView_' + tabName);
  if (activeSec) activeSec.classList.add('active');

  const breadcrumb = document.getElementById('tBreadcrumbTitle');
  if (breadcrumb) {
    breadcrumb.textContent = tabName.charAt(0).toUpperCase() + tabName.slice(1);
  }

  if (tabName === 'students') {
    renderStudentsList(currentStudentStream);
  } else if (tabName === 'faculty') {
    renderAllFacultyRoster();
  }
}

// Switch Standard between 11th and 12th
function setTeacherStandard(std) {
  teacherCurrentStandard = std;
  const btn11 = document.getElementById('btnTStd11');
  const btn12 = document.getElementById('btnTStd12');
  if (btn11) btn11.classList.toggle('active', std === '11th');
  if (btn12) btn12.classList.toggle('active', std === '12th');

  document.querySelectorAll('.stream-std-text').forEach(el => {
    el.textContent = `${std} Standard`;
  });

  showToast(`Switched to ${std} Standard`);
}

// Open Faculty Drilldown for a specific stream (Image 1 Right Panel)
function openFacultyDrilldown(streamName) {
  currentFacultyDrillStream = streamName;
  
  // Hide all sections, show drilldown
  document.querySelectorAll('.t-view-section').forEach(el => el.classList.remove('active'));
  const drillSec = document.getElementById('tView_facultyDrilldown');
  if (drillSec) drillSec.classList.add('active');

  // Breadcrumb
  const breadcrumb = document.getElementById('tBreadcrumbTitle');
  if (breadcrumb) {
    breadcrumb.textContent = `Departments / ${streamName} Faculty`;
  }

  // Update Title and Icon
  const titleEl = document.getElementById('drillStreamTitle');
  if (titleEl) {
    titleEl.textContent = `${streamName} - ${teacherCurrentStandard} Standard`;
  }

  const iconEl = document.getElementById('drillStreamIcon');
  const barEl = document.getElementById('drillAccentUnderline');
  const tableEl = document.getElementById('tableDrillFaculty');

  const streamIcons = {
    Arts: '📖',
    Commerce: '📈',
    Science: '🧪',
    Vocational: '🛠️'
  };

  const streamClasses = {
    Arts: 'arts',
    Commerce: 'commerce',
    Science: 'science',
    Vocational: 'vocational'
  };

  const sClass = streamClasses[streamName] || 'arts';

  if (iconEl) {
    iconEl.textContent = streamIcons[streamName] || '📖';
    iconEl.className = `t-drill-stream-icon ${sClass}`;
  }

  if (barEl) {
    barEl.className = `t-drill-accent-underline ${sClass}`;
  }

  if (tableEl) {
    tableEl.className = `t-data-table ${sClass}`;
  }

  // Populate Table Body
  const tbody = document.getElementById('tbodyDrillFaculty');
  if (tbody) {
    const list = (hssFacultyData[streamName] && hssFacultyData[streamName][teacherCurrentStandard]) || [];
    tbody.innerHTML = list.map(item => `
      <tr>
        <td style="font-weight:700; color:#475569;">${item.sr}</td>
        <td><strong>${item.name}</strong></td>
        <td><span style="background:#F1F5F9; padding:3px 8px; border-radius:6px; font-weight:600; font-size:12.5px;">${item.subjects}</span></td>
        <td style="font-family:monospace; font-weight:700; color:#1F8E73;">
          <a href="tel:${item.phone}" style="text-decoration:none; color:inherit;">📞 ${item.phone}</a>
        </td>
      </tr>
    `).join('');
  }
}

// Student Stream Filter Tab Switcher (Image 2)
function setStudentStreamTab(streamName) {
  currentStudentStream = streamName;
  const tabs = ['Arts', 'Commerce', 'Science', 'Vocational'];

  tabs.forEach(t => {
    const btn = document.getElementById('sTab' + t);
    if (btn) btn.classList.toggle('active', t === streamName);
  });

  const streamLabel = document.getElementById('studentTableStreamLabel');
  if (streamLabel) streamLabel.textContent = streamName;

  renderStudentsList(streamName);
}

// Render Student Details Table
function renderStudentsList(streamName, query = '') {
  const tbody = document.getElementById('tbodyStudentsList');
  if (!tbody) return;

  let list = hssStudentsData[streamName] || [];
  if (query && query.trim()) {
    const q = query.trim().toLowerCase();
    list = list.filter(s => 
      s.name.toLowerCase().includes(q) ||
      s.roll.toLowerCase().includes(q) ||
      s.email.toLowerCase().includes(q)
    );
  }

  if (list.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:24px; color:#94A3B8;">No students found matching "${query}".</td></tr>`;
    return;
  }

  tbody.innerHTML = list.map((s, idx) => {
    const isCol = s.isCollisionDemo;
    return `
      <tr style="${isCol ? 'background:#F0FDF4;' : ''}">
        <td class="t-roll-cell">
          ${s.roll}
          ${isCol ? '<span style="font-size:9.5px; background:#BBF7D0; color:#166534; padding:1px 5px; border-radius:4px; margin-left:4px; font-family:sans-serif;">COLLISION RULE</span>' : ''}
        </td>
        <td>${s.year}</td>
        <td>
          <strong>${s.name}</strong>
          ${isCol ? '<div style="font-size:10.5px; color:#166534;">Identical name rule: rollno.name@eventra.ac.in</div>' : ''}
        </td>
        <td class="t-email-cell">
          <span>${s.email}</span>
          <button onclick="navigator.clipboard.writeText('${s.email}'); showToast('Copied email: ${s.email}');" style="background:transparent; border:none; cursor:pointer; margin-left:4px;" title="Copy email">📋</button>
        </td>
        <td>
          <div class="t-password-cell">
            <span class="t-pass-dots" id="passDots_${streamName}_${idx}">•••••••••</span>
            <button class="t-pass-toggle-btn" onclick="toggleStudentPass('passDots_${streamName}_${idx}', '${s.pass}')" title="Reveal / Hide default password">👁️</button>
            <button onclick="navigator.clipboard.writeText('${s.pass}'); showToast('Copied password: ${s.pass}');" style="background:transparent; border:none; cursor:pointer; font-size:12px; opacity:0.6;" title="Copy password">📋</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

// Toggle masked password
function toggleStudentPass(elemId, actualPass) {
  const el = document.getElementById(elemId);
  if (!el) return;
  if (el.textContent === '•••••••••') {
    el.textContent = actualPass;
    el.style.color = '#18191E';
    el.style.fontFamily = 'monospace';
  } else {
    el.textContent = '•••••••••';
    el.style.color = '#94A3B8';
    el.style.fontFamily = 'inherit';
  }
}

// Real-time student search input
function handleStudentSearch(val) {
  renderStudentsList(currentStudentStream, val);
}

// Toggle between Teacher Personas (HOD Pramod Naik vs Principal Dr. Shashikant Mayekar)
function toggleTeacherPersona() {
  currentTeacherPersonaIndex = (currentTeacherPersonaIndex + 1) % teacherPersonas.length;
  const p = teacherPersonas[currentTeacherPersonaIndex];

  const avatar = document.getElementById('tTopAvatar');
  const name = document.getElementById('tTopName');
  const role = document.getElementById('tTopRole');

  if (avatar) avatar.textContent = p.avatar;
  if (name) name.textContent = p.name;
  if (role) role.textContent = p.role;

  showToast(`Active Persona: ${p.name}`);
}

// Render Overall Faculty Roster
function renderAllFacultyRoster() {
  const tbody = document.getElementById('tbodyAllFacultyRoster');
  if (!tbody) return;

  const roster = [
    { name: "Dr. Shashikant V. Mayekar", role: "Principal (Executive Head)", dept: "Administration & Mathematics", subjects: "Commercial Mathematics, Statistics", email: "principal@wakandahss.edu.in", phone: "9823000001" },
    { name: "Maria Rodrigues", role: "Vice Principal", dept: "Humanities & Languages", subjects: "English Special, Business Communication", email: "vp@wakandahss.edu.in", phone: "9823000002" },
    { name: "Pramod Naik", role: "HOD Science", dept: "Department of Science", subjects: "Physics Mechanics, Optics & Lab", email: "hod.science@wakandahss.edu.in", phone: "9823000003" },
    { name: "Sujata Kamat", role: "HOD Commerce", dept: "Department of Commerce", subjects: "Accountancy, Book Keeping, Corporate Accounts", email: "hod.commerce@wakandahss.edu.in", phone: "9823000004" },
    { name: "Sanjay Bandodkar", role: "HOD Arts", dept: "Department of Arts & Humanities", subjects: "History, World Affairs, Geography", email: "hod.arts@wakandahss.edu.in", phone: "9823000005" },
    { name: "Francis D'Costa", role: "HOD Vocational", dept: "Department of Vocational Studies", subjects: "Office Management & Secretarial Practice", email: "hod.vocational@wakandahss.edu.in", phone: "9823000006" },
    { name: "Dr. Neha Parab", role: "Subject Teacher", dept: "Science (Physics)", subjects: "Physics Electrodynamics, Modern Physics", email: "neha.p@wakandahss.edu.in", phone: "9023456781" },
    { name: "Vikram Bhat", role: "Subject Teacher", dept: "Science (Chemistry)", subjects: "Physical & Inorganic Chemistry", email: "vikram.b@wakandahss.edu.in", phone: "8855667788" },
    { name: "Sneha Gaonkar", role: "Subject Teacher", dept: "Science (Chemistry)", subjects: "Organic Chemistry & Practical Labs", email: "sneha.g@wakandahss.edu.in", phone: "9823000007" },
    { name: "Sangeeta Dessai", role: "Subject Teacher", dept: "Science (Biology)", subjects: "Cell Biology, Genetics & Physiology", email: "sangeeta.d@wakandahss.edu.in", phone: "9321123344" },
    { name: "Pranav Kolekar", role: "Subject Teacher", dept: "Science (Mathematics)", subjects: "Calculus, Differential Equations, Geometry", email: "pranav.k@wakandahss.edu.in", phone: "8765432109" },
    { name: "Amit Gaonkar", role: "Subject Teacher", dept: "Computer Science & IT", subjects: "Computer Science (Python/SQL), DTP", email: "amit.g@wakandahss.edu.in", phone: "8899001122" },
    { name: "Aarti Faldesai", role: "Subject Teacher", dept: "Commerce", subjects: "Business Studies, Marketing & Management", email: "aarti.f@wakandahss.edu.in", phone: "9823000009" },
    { name: "Nitin Shenvi", role: "Subject Teacher", dept: "Commerce & Vocational", subjects: "Economics, Statistics, Auditing", email: "nitin.s@wakandahss.edu.in", phone: "9823000010" },
    { name: "Pradnya Sawant", role: "Subject Teacher", dept: "Arts (Political Science)", subjects: "Political Theory, Indian Constitution", email: "pradnya.s@wakandahss.edu.in", phone: "9156784320" },
    { name: "Vinayak Borkar", role: "Director of Physical Ed", dept: "Sports & Extension", subjects: "Physical Education, NSS Unit In-charge", email: "sports.nss@wakandahss.edu.in", phone: "9823000014" }
  ];

  tbody.innerHTML = roster.map(m => `
    <tr>
      <td><strong>${m.name}</strong></td>
      <td><span class="badge lavender-dark" style="font-size:11px;">${m.role}</span></td>
      <td>${m.dept}</td>
      <td><span style="font-size:12px; color:#475569;">${m.subjects}</span></td>
      <td style="font-family:monospace; font-size:12px; color:#7B52DE;">${m.email}</td>
      <td style="font-family:monospace; font-weight:700; color:#1F8E73;">📞 ${m.phone}</td>
    </tr>
  `).join('');
}

// Teacher Logout
function logoutTeacher() {
  switchMainPortalView('MOBILE_APP');
  switchMode('LOGIN');
  showToast("Logged out of Teacher Portal. Switched to Sign In.");
}

// Initialize on window load
window.addEventListener('DOMContentLoaded', () => {
  renderStudentsList('Arts');
  renderAllFacultyRoster();
});

