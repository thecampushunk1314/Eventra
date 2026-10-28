// Eventra State Management
let currentRole = 'STUDENT'; // 'STUDENT' | 'TEACHER'
let currentMode = 'LOGIN'; // 'LOGIN' (Registration handled centrally by Principal)
let teacherSubRole = 'DIRECTOR'; // 'DIRECTOR' | 'FACULTY'
let selectedStudentInst = 'COLLEGE'; // 'COLLEGE' | 'UNIVERSITY'
let selectedTeacherInst = 'COLLEGE';
let lastRegisteredUser = null;

// Mock Local User Database with unified multi-buyer format
let usersDatabase = [
  // YASHODA COLLEGE OF ARTS, SCIENCE AND COMMERCE (YCASC)
  // Provisioned @ycasc.com accounts with default password 123456789
  {
    id: "SCI-26-001",
    fullName: "Akash Kamat",
    email: "akash.kamat@ycasc.com",
    phoneNumber: "9823000001",
    password: "123456789",
    role: "STUDENT",
    roleDisplayName: "Student (B.Sc. Science)",
    department: "Science Division",
    institutionType: "COLLEGE",
    dob: "05/01/2006",
    yearOrClass: "First Year (Batch 2026)",
    degree: "Bachelor of Science (B.Sc.)",
    subjects: "Physics, Chemistry, Mathematics",
    extra: "Roll: SCI-26-001 | NSS Unit | Physics, Chemistry, Maths"
  },
  {
    id: "YCASC_Principal",
    fullName: "Dr. Shashikant V. Mayekar",
    email: "principal@ycasc.com",
    phoneNumber: "9823000001",
    password: "123456789",
    role: "DIRECTOR",
    roleDisplayName: "Principal",
    department: "Executive Administration",
    institutionType: "COLLEGE",
    dob: "15/06/1972",
    yearOrClass: "Principal",
    degree: "Ph.D in Business Administration",
    subjects: "Business, Management",
    extra: "Principal(YCASC)"
  },
  {
    id: "YCASC_VP",
    fullName: "Dr. Ramakant Deshmukh",
    email: "ramakant.deshmukh@ycasc.com",
    phoneNumber: "9823000002",
    password: "123456789",
    role: "DIRECTOR",
    roleDisplayName: "Vice Principal",
    department: "Executive Administration",
    institutionType: "COLLEGE",
    dob: "20/08/1975",
    yearOrClass: "Vice Principal",
    degree: "Ph.D in Organic Chemistry",
    subjects: "Chemistry",
    extra: "Vice Principal(YCASC)"
  },
  {
    id: "HOD_SCI",
    fullName: "Dr. Pramod Naik",
    email: "pramod.naik@ycasc.com",
    phoneNumber: "9823000003",
    password: "123456789",
    role: "DIRECTOR",
    roleDisplayName: "HOD Science",
    department: "Department of Science",
    institutionType: "COLLEGE",
    dob: "08/09/1978",
    yearOrClass: "HOD Science",
    degree: "Ph.D in Physics",
    subjects: "Physics, Mathematics",
    extra: "Head of Department - Science Stream"
  },

  // COLLISION RESOLUTION DEMOS:
  // Rule: If a person has the same name and surname (or duplicate collision),
  // emails are formatted as: rollno.name@ycasc.com
  {
    id: "SCI-26-015",
    fullName: "Kiran M Nair",
    email: "kiran.m.nair@ycasc.com",
    phoneNumber: "9823000015",
    password: "123456789",
    role: "STUDENT",
    roleDisplayName: "Student",
    department: "Science Division",
    institutionType: "COLLEGE",
    dob: "14/03/2006",
    yearOrClass: "First Year",
    degree: "Bachelor of Science (B.Sc.)",
    subjects: "Physics, Chemistry, Computer Science, English",
    extra: "Collision if name and surname already exists."
  }
];

// Institutional Email Generator with Collision Resolution
function generateInstitutionalEmail(fullName, rollOrId) {
  if (!fullName || !fullName.trim()) {
    const r = (rollOrId || '').toLowerCase().replace(/[^a-z0-9]/g, '') || '101';
    return `${r}.student@ycasc.com`;
  }

  const parts = fullName.trim().toLowerCase().replace(/[^a-z0-9\s]/g, '').split(/\s+/).filter(Boolean);
  const first = parts[0] || 'student';
  const last = parts.length > 1 ? parts[parts.length - 1] : '';
  const cleanRoll = (rollOrId || '').toLowerCase().replace(/[^a-z0-9]/g, '') || String(Math.floor(1000 + Math.random() * 9000));

  // Condition 1: Same name and surname (e.g. "Kiran Kiran" or single name)
  if (!last || first === last) {
    return `${cleanRoll}.${first}@ycasc.com`;
  }

  const standard = `${first}.${last}@ycasc.com`;

  // Condition 2: If another user already has this firstname.lastname@ycasc.com (collision)
  const hasCollision = usersDatabase.some(u => u.email && u.email.toLowerCase() === standard);
  if (hasCollision) {
    return `${cleanRoll}.${first}@ycasc.com`;
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
  if (words[0].length <= 5 && !['BACHELOR', 'MASTER'].includes(words[0].toUpperCase())) return words[0].toUpperCase();
  const acronym = words.filter(w => !['of', 'and', 'in', 'the'].includes(w.toLowerCase())).map(w => w[0].toUpperCase()).join('');
  return acronym || "MCA";
}

function sanitizeStream(s) {
  if (!s || !s.trim()) return "SCIENCE";
  const str = s.trim().toLowerCase();
  if (str.includes("sci")) return "SCIENCE";
  if (str.includes("comm")) return "COMMERCE";
  if (str.includes("art")) return "ARTS";
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
  const acronym = words.filter(w => !['of', 'and', 'in', 'department', 'school'].includes(w.toLowerCase())).map(w => w[0].toUpperCase()).join('');
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
  const btnReg = document.getElementById('btnModeRegister');
  if (btnReg) btnReg.classList.toggle('active', mode === 'REGISTER');
  const btnLog = document.getElementById('btnModeLogIn');
  if (btnLog) btnLog.classList.toggle('active', mode === 'LOGIN');
  const tabReg = document.getElementById('appTabRegister');
  if (tabReg) tabReg.classList.toggle('active', mode === 'REGISTER');
  const tabLog = document.getElementById('appTabLogIn');
  if (tabLog) tabLog.classList.toggle('active', mode === 'LOGIN');
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
    currentLoggedInUser = found;
    showToast(`Welcome to Eventra, ${found.fullName}!`);
    if (found.role === 'DIRECTOR' || found.role === 'FACULTY') {
      switchMainPortalView('TEACHER_PORTAL');
      const avatar = document.getElementById('tTopAvatar');
      const name = document.getElementById('tTopName');
      const role = document.getElementById('tTopRole');
      if (avatar) avatar.textContent = found.fullName.split(' ').map(p => p[0]).slice(0, 2).join('');
      if (name) name.textContent = found.fullName;
      if (role) role.textContent = `${found.roleDisplayName} • @ycasc.com`;
      renderTeacherTimetable();
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

  if (type === 'BSC' || type === 'HSS_STUDENT' || type === 'SCI' || type === 'STUDENT' || type === 'UNIVERSITY_STUDENT') {
    inputId.value = "akash.kamat@ycasc.com";
    showToast("🧪 B.Sc. Student: akash.kamat@ycasc.com (Default pass: 123456789)");
  } else if (type === 'BCOM') {
    inputId.value = "anil.shah@ycasc.com";
    showToast("📈 B.Com Student: anil.shah@ycasc.com (Default pass: 123456789)");
  } else if (type === 'BA') {
    inputId.value = "aditi.sharma@ycasc.com";
    showToast("🎨 B.A. Student: aditi.sharma@ycasc.com (Default pass: 123456789)");
  } else if (type === 'PRINCIPAL' || type === 'HSS_PRINCIPAL') {
    inputId.value = "principal@ycasc.com";
    showToast("🏛️ College Principal: principal@ycasc.com (Default pass: 123456789)");
  } else if (type === 'HOD_SCI' || type === 'HSS_HOD' || type === 'TEACHER') {
    inputId.value = "pramod.naik@ycasc.com";
    showToast("👨‍🏫 HOD Science: pramod.naik@ycasc.com (Default pass: 123456789)");
  } else if (type === 'SAME_NAME_COLLISION') {
    inputId.value = "015.ananya@ycasc.com";
    showToast("⚡ Collision Demo (Duplicate Name): 015.ananya@ycasc.com (Pass: 123456789)");
  } else if (type === 'IDENTICAL_NAME_SURNAME') {
    inputId.value = "015.kiran@ycasc.com";
    showToast("⚡ Collision Demo (Same Name & Surname): 015.kiran@ycasc.com (Pass: 123456789)");
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
    showToast("❌ User not found with this email (@ycasc) or ID.");
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

  if (buyer === 'COLLEGE' || buyer === 'HIGHER_SEC') {
    setStudentInstitution('COLLEGE');
    setTeacherInstitution('COLLEGE');
    if (regCourse) regCourse.value = "Science";
    if (regRoll) regRoll.value = "001";
    if (regName) regName.value = "Aditya Kamat";
    if (regDept) regDept.value = "Department of Science";
    if (phoneLabel) phoneLabel.innerHTML = "🏛️ <strong>Yashoda College (YCASC)</strong>";
    if (pills[0]) pills[0].classList.add('active');
    showToast("🏛️ Activated: Yashoda College of Arts, Science and Commerce");
  } else {
    setStudentInstitution('UNIVERSITY');
    setTeacherInstitution('UNIVERSITY');
    if (regCourse) regCourse.value = "MCA";
    if (regRoll) regRoll.value = "2650";
    if (regName) regName.value = "Esther Howard";
    if (regDept) regDept.value = "Department of Computer Applications";
    if (phoneLabel) phoneLabel.innerHTML = "🎓 <strong>University Campus</strong>";
    if (pills[1]) pills[1].classList.add('active');
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
      statusBadge.innerHTML = '🟢 <strong>Node.js Backend Online</strong> (Port 3000: yashoda_college_db connected - Yashoda College)';
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
    name: "Dr. Pramod Naik",
    role: "HOD Science Stream • @ycasc",
    avatar: "PN"
  },
  {
    name: "Dr. Shashikant V. Mayekar",
    role: "Principal (Executive Head) • @ycasc",
    avatar: "SM"
  }
];

// Authentic Yashoda College Faculty Data (3 teachers per stream with 1 HOD, plus Principal and VP)
const collegeFacultyData = {
  Arts: {
    FY: [
      { sr: 1, name: "Dr. Meenakshi Sundaram", subjects: "HOD Arts / History & World Civilization", phone: "9823000007" },
      { sr: 2, name: "Prof. Anthony D'Souza", subjects: "Associate Professor / English Literature & Linguistics", phone: "9823000008" },
      { sr: 3, name: "Prof. Geeta Sawant", subjects: "Assistant Professor / Political Science & Governance", phone: "9823000009" }
    ],
    SY: [
      { sr: 1, name: "Dr. Meenakshi Sundaram", subjects: "HOD Arts / Modern Indian Historiography", phone: "9823000007" },
      { sr: 2, name: "Prof. Anthony D'Souza", subjects: "Associate Professor / Contemporary World Literature", phone: "9823000008" },
      { sr: 3, name: "Prof. Geeta Sawant", subjects: "Assistant Professor / Public Policy & International Relations", phone: "9823000009" }
    ]
  },
  Commerce: {
    FY: [
      { sr: 1, name: "Dr. Anand Vernekar", subjects: "HOD Commerce / Advanced Financial Accounting", phone: "9823000004" },
      { sr: 2, name: "Prof. Kavita Shenvi", subjects: "Associate Professor / Corporate Accounting & Auditing", phone: "9823000005" },
      { sr: 3, name: "Prof. Suresh Lotlikar", subjects: "Assistant Professor / Principles of Business Management", phone: "9823000006" }
    ],
    SY: [
      { sr: 1, name: "Dr. Anand Vernekar", subjects: "HOD Commerce / Taxation & Business Law", phone: "9823000004" },
      { sr: 2, name: "Prof. Kavita Shenvi", subjects: "Associate Professor / Financial Markets & Banking", phone: "9823000005" },
      { sr: 3, name: "Prof. Suresh Lotlikar", subjects: "Assistant Professor / Costing & Forensic Accounting", phone: "9823000006" }
    ]
  },
  Science: {
    FY: [
      { sr: 1, name: "Dr. Pramod Naik", subjects: "HOD Science / Mechanics, Electrodynamics & Lab", phone: "9823000003" },
      { sr: 2, name: "Dr. Sunita Kulkarni", subjects: "Associate Professor / Organic & Physical Chemistry", phone: "9823000010" },
      { sr: 3, name: "Prof. Rajesh Haldankar", subjects: "Assistant Professor / Calculus, Differential Equations & Stats", phone: "9823000011" }
    ],
    SY: [
      { sr: 1, name: "Dr. Pramod Naik", subjects: "HOD Science / Quantum Mechanics & Modern Physics", phone: "9823000003" },
      { sr: 2, name: "Dr. Sunita Kulkarni", subjects: "Associate Professor / Spectroscopy & Inorganic Chemistry", phone: "9823000010" },
      { sr: 3, name: "Prof. Rajesh Haldankar", subjects: "Assistant Professor / Numerical Analysis & Linear Algebra", phone: "9823000011" }
    ]
  }
};
// Aliases for compatibility
collegeFacultyData.Arts['11th'] = collegeFacultyData.Arts.FY;
collegeFacultyData.Arts['12th'] = collegeFacultyData.Arts.SY;
collegeFacultyData.Commerce['11th'] = collegeFacultyData.Commerce.FY;
collegeFacultyData.Commerce['12th'] = collegeFacultyData.Commerce.SY;
collegeFacultyData.Science['11th'] = collegeFacultyData.Science.FY;
collegeFacultyData.Science['12th'] = collegeFacultyData.Science.SY;
const hssFacultyData = collegeFacultyData;

// Authentic Yashoda College Student Directory (15 students per general degree stream = 45 total)
const hssStudentsData = {
  Arts: [
    { roll: "ART-26-001", year: "2026", name: "Aditi Sharma", email: "aditi.sharma@ycasc", pass: "123456789" },
    { roll: "ART-26-002", year: "2026", name: "Rohan Patil", email: "rohan.patil@ycasc", pass: "123456789" },
    { roll: "ART-26-003", year: "2026", name: "Sneha Desai", email: "sneha.desai@ycasc", pass: "123456789" },
    { roll: "ART-26-004", year: "2026", name: "Pratik Kulkarni", email: "pratik.kulkarni@ycasc", pass: "123456789" },
    { roll: "ART-26-005", year: "2026", name: "Neha Iyer", email: "neha.iyer@ycasc", pass: "123456789" },
    { roll: "ART-26-006", year: "2026", name: "Rutuja More", email: "rutuja.more@ycasc", pass: "123456789" },
    { roll: "ART-26-007", year: "2026", name: "Devika Menon", email: "devika.menon@ycasc", pass: "123456789" },
    { roll: "ART-26-008", year: "2026", name: "Kabir Sen", email: "kabir.sen@ycasc", pass: "123456789" },
    { roll: "ART-26-009", year: "2026", name: "Ayesha Qureshi", email: "ayesha.qureshi@ycasc", pass: "123456789" },
    { roll: "ART-26-010", year: "2026", name: "Manasvi Gokhale", email: "manasvi.gokhale@ycasc", pass: "123456789" },
    { roll: "ART-26-011", year: "2026", name: "Nikhil Vartak", email: "nikhil.vartak@ycasc", pass: "123456789" },
    { roll: "ART-26-012", year: "2026", name: "Sharmila Tagore", email: "sharmila.tagore@ycasc", pass: "123456789" },
    { roll: "ART-26-013", year: "2026", name: "Farhan Akhtar", email: "farhan.akhtar@ycasc", pass: "123456789" },
    { roll: "ART-26-014", year: "2026", name: "Gayatri Devi", email: "gayatri.devi@ycasc", pass: "123456789" },
    { roll: "ART-26-015", year: "2026", name: "Rahul Dravid", email: "rahul.dravid@ycasc", pass: "123456789" }
  ],
  Commerce: [
    { roll: "COM-26-001", year: "2026", name: "Anil Shah", email: "anil.shah@ycasc", pass: "123456789" },
    { roll: "COM-26-002", year: "2026", name: "Pooja Mehta", email: "pooja.mehta@ycasc", pass: "123456789" },
    { roll: "COM-26-003", year: "2026", name: "Karan Singh", email: "karan.singh@ycasc", pass: "123456789" },
    { roll: "COM-26-004", year: "2026", name: "Isha Patel", email: "isha.patel@ycasc", pass: "123456789" },
    { roll: "COM-26-005", year: "2026", name: "Sahil Gupta", email: "sahil.gupta@ycasc", pass: "123456789" },
    { roll: "COM-26-006", year: "2026", name: "Tanvi Joshi", email: "tanvi.joshi@ycasc", pass: "123456789" },
    { roll: "COM-26-007", year: "2026", name: "Bhavik Agarwal", email: "bhavik.agarwal@ycasc", pass: "123456789" },
    { roll: "COM-26-008", year: "2026", name: "Megha Singhania", email: "megha.singhania@ycasc", pass: "123456789" },
    { roll: "COM-26-009", year: "2026", name: "Yashvardhan Goel", email: "yashvardhan.goel@ycasc", pass: "123456789" },
    { roll: "COM-26-010", year: "2026", name: "Ritu Maheshwari", email: "ritu.maheshwari@ycasc", pass: "123456789" },
    { roll: "COM-26-011", year: "2026", name: "Kunal Somani", email: "kunal.somani@ycasc", pass: "123456789" },
    { roll: "COM-26-012", year: "2026", name: "Simran Kaur", email: "simran.kaur@ycasc", pass: "123456789" },
    { roll: "COM-26-013", year: "2026", name: "Gaurav Biyani", email: "gaurav.biyani@ycasc", pass: "123456789" },
    { roll: "COM-26-014", year: "2026", name: "Nidhi Khandelwal", email: "nidhi.khandelwal@ycasc", pass: "123456789" },
    { roll: "COM-26-015", year: "2026", name: "Ananya Prabhu", email: "015.ananya@ycasc", pass: "123456789", isCollisionDemo: true }
  ],
  Science: [
    { roll: "SCI-26-001", year: "2026", name: "Aditya Kamat", email: "aditya.kamat@ycasc", pass: "123456789" },
    { roll: "SCI-26-002", year: "2026", name: "Riya Patil", email: "riya.patil@ycasc", pass: "123456789" },
    { roll: "SCI-26-003", year: "2026", name: "Pallavi Deshmukh", email: "pallavi.deshmukh@ycasc", pass: "123456789" },
    { roll: "SCI-26-004", year: "2026", name: "Siddharth Kulkarni", email: "siddharth.kulkarni@ycasc", pass: "123456789" },
    { roll: "SCI-26-005", year: "2026", name: "Shruti Nair", email: "shruti.nair@ycasc", pass: "123456789" },
    { roll: "SCI-26-006", year: "2026", name: "Omkar Bhosale", email: "omkar.bhosale@ycasc", pass: "123456789" },
    { roll: "SCI-26-007", year: "2026", name: "Tanvi Gawde", email: "tanvi.gawde@ycasc", pass: "123456789" },
    { roll: "SCI-26-008", year: "2026", name: "Harshvardhan Joshi", email: "harshvardhan.joshi@ycasc", pass: "123456789" },
    { roll: "SCI-26-009", year: "2026", name: "Ananya Prabhu", email: "ananya.prabhu@ycasc", pass: "123456789" },
    { roll: "SCI-26-010", year: "2026", name: "Varun Shenoy", email: "varun.shenoy@ycasc", pass: "123456789" },
    { roll: "SCI-26-011", year: "2026", name: "Sanika Shirodkar", email: "sanika.shirodkar@ycasc", pass: "123456789" },
    { roll: "SCI-26-012", year: "2026", name: "Prathamesh Naik", email: "prathamesh.naik@ycasc", pass: "123456789" },
    { roll: "SCI-26-013", year: "2026", name: "Divya Chodankar", email: "divya.chodankar@ycasc", pass: "123456789" },
    { roll: "SCI-26-014", year: "2026", name: "Chinmay Rane", email: "chinmay.rane@ycasc", pass: "123456789" },
    { roll: "SCI-26-015", year: "2026", name: "Kiran Kiran", email: "015.kiran@ycasc", pass: "123456789", isCollisionDemo: true }
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
    showToast("👨‍🏫 Switched to Yashoda College Teacher Interface");
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
  } else if (tabName === 'announcements') {
    renderAnnouncements();
  } else if (tabName === 'timetable') {
    renderTeacherTimetable();
  }
}

// Switch Collegiate Year between FY and SY
function setTeacherStandard(std) {
  teacherCurrentStandard = (std === '12th' || std === 'SY') ? 'SY' : 'FY';
  const btn11 = document.getElementById('btnTStd11');
  const btn12 = document.getElementById('btnTStd12');
  if (btn11) btn11.classList.toggle('active', teacherCurrentStandard === 'FY');
  if (btn12) btn12.classList.toggle('active', teacherCurrentStandard === 'SY');

  const label = teacherCurrentStandard === 'FY' ? 'First Year (FY)' : 'Second Year (SY)';
  document.querySelectorAll('.stream-std-text').forEach(el => {
    el.textContent = label;
  });

  showToast(`Switched to ${label}`);
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
    const yrLabel = teacherCurrentStandard === 'FY' ? 'First Year (FY)' : 'Second Year (SY)';
    titleEl.textContent = `${streamName} - ${yrLabel}`;
  }

  const iconEl = document.getElementById('drillStreamIcon');
  const barEl = document.getElementById('drillAccentUnderline');
  const tableEl = document.getElementById('tableDrillFaculty');

  const streamIcons = {
    Arts: '📖',
    Commerce: '📈',
    Science: '🧪'
  };

  const streamClasses = {
    Arts: 'arts',
    Commerce: 'commerce',
    Science: 'science'
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
  const tabs = ['Arts', 'Commerce', 'Science'];

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
          ${isCol ? '<div style="font-size:10.5px; color:#166534;">Identical name rule: rollno.name@ycasc</div>' : ''}
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
    { name: "Dr. Shashikant V. Mayekar", role: "Principal & Chief Executive", dept: "Administration & Mathematics", subjects: "Institutional Governance, Applied Mathematics", email: "principal@ycasc.com", phone: "9823000001" },
    { name: "Dr. Ramakant Deshmukh", role: "Vice Principal", dept: "Administration & Chemistry", subjects: "Academic Council, Organic Chemistry", email: "ramakant.deshmukh@ycasc.com", phone: "9823000002" },
    { name: "Mr. Rajesh Shirodkar", role: "Registrar", dept: "Registrar's Office", subjects: "Admissions & University Affiliation", email: "registrar@ycasc.com", phone: "9823000015" },
    { name: "Dr. Pramod Naik", role: "HOD Science", dept: "Department of Science", subjects: "Quantum Mechanics, Advanced Physics & Lab", email: "pramod.naik@ycasc.com", phone: "9823000003" },
    { name: "Dr. Sunita Kulkarni", role: "Associate Professor", dept: "Department of Science", subjects: "Physical Chemistry, Spectroscopy & Organic Analysis", email: "sunita.kulkarni@ycasc.com", phone: "9823000010" },
    { name: "Prof. Rajesh Haldankar", role: "Assistant Professor", dept: "Department of Science", subjects: "Calculus, Differential Equations & Probability", email: "rajesh.haldankar@ycasc.com", phone: "9823000011" },
    { name: "Dr. Anand Vernekar", role: "HOD Commerce", dept: "Department of Commerce", subjects: "Advanced Financial Accounting & Corporate Law", email: "anand.vernekar@ycasc.com", phone: "9823000004" },
    { name: "Prof. Kavita Shenvi", role: "Associate Professor", dept: "Department of Commerce", subjects: "Corporate Accounting, Auditing & Financial Markets", email: "kavita.shenvi@ycasc.com", phone: "9823000005" },
    { name: "Prof. Suresh Lotlikar", role: "Assistant Professor", dept: "Department of Commerce", subjects: "Principles of Business Management & Marketing", email: "suresh.lotlikar@ycasc.com", phone: "9823000006" },
    { name: "Dr. Meenakshi Sundaram", role: "HOD Arts", dept: "Department of Arts & Humanities", subjects: "Indian History, Historiography & Culture", email: "meenakshi.sundaram@ycasc.com", phone: "9823000007" },
    { name: "Prof. Anthony D'Souza", role: "Associate Professor", dept: "Department of Arts & Humanities", subjects: "English Literature, World Classics & Criticism", email: "anthony.dsouza@ycasc.com", phone: "9823000008" },
    { name: "Prof. Geeta Sawant", role: "Assistant Professor", dept: "Department of Arts & Humanities", subjects: "Political Science, Indian Constitution & Public Policy", email: "geeta.sawant@ycasc.com", phone: "9823000009" }
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

// Official Notices and Announcements (Kept empty for now; added by teachers/administration)
let collegeAnnouncements = [];

function toggleNoticePublisher() {
  const card = document.getElementById('noticePublisherCard');
  if (card) {
    const isHidden = card.style.display === 'none' || !card.style.display;
    card.style.display = isHidden ? 'block' : 'none';
    if (isHidden) {
      const input = document.getElementById('noticeInputTitle');
      if (input) input.focus();
    }
  }
}

function handlePublishNotice(e) {
  if (e) e.preventDefault();
  const title = (document.getElementById('noticeInputTitle')?.value || '').trim();
  const category = document.getElementById('noticeInputCategory')?.value || 'General Administration';
  const audience = document.getElementById('noticeInputAudience')?.value || 'All Undergraduate Students & Faculty';
  const content = (document.getElementById('noticeInputContent')?.value || '').trim();

  if (!title || !content) {
    showToast('⚠️ Please enter both notice title and content');
    return;
  }

  const refNum = Math.floor(100 + Math.random() * 900);
  const now = new Date();
  const timeStr = now.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });

  const newNotice = {
    ref: `NOT-2026-${refNum}`,
    title: title,
    category: category,
    audience: audience,
    content: content,
    author: "Faculty Member (Yashoda College)",
    date: `Today, ${now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
  };

  collegeAnnouncements.unshift(newNotice);
  renderAnnouncements();
  toggleNoticePublisher();

  const titleInput = document.getElementById('noticeInputTitle');
  const contentInput = document.getElementById('noticeInputContent');
  if (titleInput) titleInput.value = '';
  if (contentInput) contentInput.value = '';

  showToast(`📢 Official Notice [${newNotice.ref}] Published!`);
}

function renderAnnouncements() {
  const container = document.getElementById('announcementsListContainer');
  if (!container) return;

  if (collegeAnnouncements.length === 0) {
    container.innerHTML = `
      <div style="background:#FFFFFF; border:2px dashed #E2E8F0; border-radius:14px; padding:40px 24px; text-align:center; box-shadow:0 2px 8px rgba(0,0,0,0.02);">
        <div style="font-size:38px; margin-bottom:12px;">📢</div>
        <h3 style="font-size:16px; font-weight:800; color:#18191E; margin-bottom:6px;">No Official Notices Yet</h3>
        <p style="font-size:13px; color:#64748B; max-width:440px; margin:0 auto 16px; line-height:1.5;">
          Official announcements, exam schedules, and circulars to be added by teachers and administration. The notice board is currently empty.
        </p>
        <button class="t-btn-primary" onclick="toggleNoticePublisher()" style="font-size:12.5px; padding:8px 16px;">
          + Publish First Notice
        </button>
      </div>
    `;
    return;
  }

  container.innerHTML = collegeAnnouncements.map(n => `
    <div style="background:#FFFFFF; border:1px solid #E2E8F0; border-radius:14px; padding:18px 22px; box-shadow:0 2px 10px rgba(0,0,0,0.03);">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; flex-wrap:wrap; gap:8px;">
        <div style="display:flex; gap:8px; align-items:center;">
          <span class="badge lavender-dark">${n.ref}</span>
          <span class="badge mint-dark" style="font-size:11px;">${n.category}</span>
          <span style="font-size:11px; color:#64748B;">• Target: ${n.audience}</span>
        </div>
        <span style="font-size:11px; color:#94A3B8;">${n.date}</span>
      </div>
      <h3 style="font-size:16px; font-weight:800; color:#18191E; margin-bottom:6px;">${n.title}</h3>
      <p style="font-size:13px; color:#475569; line-height:1.5; white-space:pre-wrap;">${n.content}</p>
      <div style="margin-top:10px; font-size:11.5px; color:#7B52DE; font-weight:700;">By: ${n.author}</div>
    </div>
  `).join('');
}

// Teacher Logout
function logoutTeacher() {
  switchMainPortalView('MOBILE_APP');
  switchMode('LOGIN');
  showToast("Logged out of Teacher Portal. Switched to Sign In.");
}

// Student Mobile Logout
function logout() {
  currentLoggedInUser = null;
  switchMode('LOGIN');
  showToast("Signed out successfully.");
}

// ============================================================================
// ACADEMIC TIMETABLE DATA ENGINE & TEACHER INTERCHANGE (WEB PREVIEW)
// Timings:
// Slot 1: 08:00 AM - 09:00 AM
// Slot 2: 09:00 AM - 10:00 AM
// Slot 3: 10:00 AM - 11:00 AM
// Recess: 11:00 AM - 11:30 AM (Break)
// Slot 4: 11:30 AM - 12:30 PM
// Slot 5: 12:30 PM - 01:30 PM
// ============================================================================

let currentLoggedInUser = null;
let activeWebTimetableStream = 'SCIENCE';
let activeWebTimetableDay = 'Monday';
let activeStudentTimetableDay = 'Monday';

const timetableDatabase = {
  SCIENCE: {
    name: 'Science (B.Sc.)',
    Monday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Physics', title: 'Physics · Lecture 1', teacher: 'Dr. Pramod Naik', room: 'Lab 1', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Chemistry', title: 'Chemistry · Lecture 2', teacher: 'Dr. Sneha Gaonkar', room: 'Room 204', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Mathematics', title: 'Mathematics · Lecture 3', teacher: 'Dr. Shashikant Mayekar', room: 'Room 204', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Computer Science', title: 'Computer Science · Lecture 4', teacher: 'Prof. Vikram Dessai', room: 'Computer Lab A', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Physics', title: 'Applied Physics · Lecture 5', teacher: 'Dr. Pramod Naik', room: 'Lab 1', swapped: false }
    ],
    Tuesday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Chemistry', title: 'Chemistry · Lecture 1', teacher: 'Dr. Sneha Gaonkar', room: 'Room 204', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Physics', title: 'Physics · Lecture 2', teacher: 'Dr. Pramod Naik', room: 'Lab 1', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Mathematics', title: 'Mathematics · Lecture 3', teacher: 'Dr. Shashikant Mayekar', room: 'Room 204', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Computer Science', title: 'Computer Science · Lecture 4', teacher: 'Prof. Vikram Dessai', room: 'Computer Lab A', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Chemistry', title: 'Organic Chemistry · Lecture 5', teacher: 'Dr. Sneha Gaonkar', room: 'Room 204', swapped: false }
    ],
    Wednesday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Mathematics', title: 'Mathematics · Lecture 1', teacher: 'Dr. Shashikant Mayekar', room: 'Room 204', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Computer Science', title: 'Computer Science · Lecture 2', teacher: 'Prof. Vikram Dessai', room: 'Computer Lab A', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Physics', title: 'Physics · Lecture 3', teacher: 'Dr. Pramod Naik', room: 'Lab 1', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Chemistry', title: 'Chemistry · Lecture 4', teacher: 'Dr. Sneha Gaonkar', room: 'Room 204', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Mathematics', title: 'Discrete Mathematics · Lecture 5', teacher: 'Dr. Shashikant Mayekar', room: 'Room 204', swapped: false }
    ],
    Thursday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Computer Science', title: 'Computer Science · Lecture 1', teacher: 'Prof. Vikram Dessai', room: 'Computer Lab A', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Physics', title: 'Physics · Lecture 2', teacher: 'Dr. Pramod Naik', room: 'Lab 1', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Chemistry', title: 'Chemistry · Lecture 3', teacher: 'Dr. Sneha Gaonkar', room: 'Room 204', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Mathematics', title: 'Mathematics · Lecture 4', teacher: 'Dr. Shashikant Mayekar', room: 'Room 204', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Computer Science', title: 'Software Lab · Lecture 5', teacher: 'Prof. Vikram Dessai', room: 'Computer Lab B', swapped: false }
    ],
    Friday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Chemistry', title: 'Chemistry · Lecture 1', teacher: 'Dr. Sneha Gaonkar', room: 'Room 204', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Mathematics', title: 'Mathematics · Lecture 2', teacher: 'Dr. Shashikant Mayekar', room: 'Room 204', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Physics', title: 'Physics · Lecture 3', teacher: 'Dr. Pramod Naik', room: 'Lab 1', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Computer Science', title: 'Computer Science · Lecture 4', teacher: 'Prof. Vikram Dessai', room: 'Computer Lab A', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Physics', title: 'Experimental Physics · Lecture 5', teacher: 'Dr. Pramod Naik', room: 'Lab 1', swapped: false }
    ],
    Saturday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Physics', title: 'Physics · Lecture 1', teacher: 'Dr. Pramod Naik', room: 'Lab 1', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Chemistry', title: 'Chemistry · Lecture 2', teacher: 'Dr. Sneha Gaonkar', room: 'Room 204', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Mathematics', title: 'Mathematics · Lecture 3', teacher: 'Dr. Shashikant Mayekar', room: 'Room 204', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Computer Science', title: 'Computer Science · Lecture 4', teacher: 'Prof. Vikram Dessai', room: 'Computer Lab A', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Chemistry', title: 'Chemistry Practical Review', teacher: 'Dr. Sneha Gaonkar', room: 'Room 204', swapped: false }
    ]
  },
  COMMERCE: {
    name: 'Commerce (B.Com)',
    Monday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Financial Accounting', title: 'Financial Accounting · Lecture 1', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Business Economics', title: 'Business Economics · Lecture 2', teacher: 'Prof. Aarti Faldesai', room: 'Room 101', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Business Law', title: 'Business Law · Lecture 3', teacher: 'Prof. Nitin Shenvi', room: 'Room 102', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Cost Accounting', title: 'Cost Accounting · Lecture 4', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Banking & Finance', title: 'Banking Operations · Lecture 5', teacher: 'Prof. Nitin Shenvi', room: 'Room 102', swapped: false }
    ],
    Tuesday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Business Economics', title: 'Business Economics · Lecture 1', teacher: 'Prof. Aarti Faldesai', room: 'Room 101', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Financial Accounting', title: 'Financial Accounting · Lecture 2', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Cost Accounting', title: 'Cost Accounting · Lecture 3', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Business Law', title: 'Corporate Law · Lecture 4', teacher: 'Prof. Nitin Shenvi', room: 'Room 102', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Banking & Finance', title: 'Financial Services · Lecture 5', teacher: 'Prof. Aarti Faldesai', room: 'Room 101', swapped: false }
    ],
    Wednesday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Financial Accounting', title: 'Financial Accounting · Lecture 1', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Business Law', title: 'Business Law · Lecture 2', teacher: 'Prof. Nitin Shenvi', room: 'Room 102', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Business Economics', title: 'Macro Economics · Lecture 3', teacher: 'Prof. Aarti Faldesai', room: 'Room 101', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Cost Accounting', title: 'Cost Accounting · Lecture 4', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Banking & Finance', title: 'Banking Regulations · Lecture 5', teacher: 'Prof. Nitin Shenvi', room: 'Room 102', swapped: false }
    ],
    Thursday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Cost Accounting', title: 'Cost Accounting · Lecture 1', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Business Economics', title: 'Business Economics · Lecture 2', teacher: 'Prof. Aarti Faldesai', room: 'Room 101', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Financial Accounting', title: 'Financial Accounting · Lecture 3', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Business Law', title: 'Business Law · Lecture 4', teacher: 'Prof. Nitin Shenvi', room: 'Room 102', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Banking & Finance', title: 'Investment Analysis · Lecture 5', teacher: 'Prof. Aarti Faldesai', room: 'Room 101', swapped: false }
    ],
    Friday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Financial Accounting', title: 'Auditing & Accounting · Lecture 1', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Business Law', title: 'Business Law · Lecture 2', teacher: 'Prof. Nitin Shenvi', room: 'Room 102', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Business Economics', title: 'Economic Policy · Lecture 3', teacher: 'Prof. Aarti Faldesai', room: 'Room 101', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Cost Accounting', title: 'Cost Management · Lecture 4', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Banking & Finance', title: 'Banking Operations · Lecture 5', teacher: 'Prof. Nitin Shenvi', room: 'Room 102', swapped: false }
    ],
    Saturday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Business Economics', title: 'Managerial Economics · Lecture 1', teacher: 'Prof. Aarti Faldesai', room: 'Room 101', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Financial Accounting', title: 'Company Accounts · Lecture 2', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Business Law', title: 'Contract Act · Lecture 3', teacher: 'Prof. Nitin Shenvi', room: 'Room 102', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Cost Accounting', title: 'Cost Sheet Preparation · Lecture 4', teacher: 'Prof. Sujata Kamat', room: 'Room 101', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Banking & Finance', title: 'Case Study Seminar · Lecture 5', teacher: 'Prof. Aarti Faldesai', room: 'Room 101', swapped: false }
    ]
  },
  ARTS: {
    name: 'Arts (B.A.)',
    Monday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'English Literature', title: 'Modern English Literature · Lecture 1', teacher: 'Dr. Robert Vance', room: 'Room 301', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'History', title: 'Contemporary World History · Lecture 2', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Political Science', title: 'Political Theory · Lecture 3', teacher: 'Prof. Meera Chodankar', room: 'Room 302', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Psychology', title: 'General Psychology · Lecture 4', teacher: 'Dr. Anita Monteiro', room: 'Room 303', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Sociology', title: 'Indian Society & Culture · Lecture 5', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false }
    ],
    Tuesday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'History', title: 'Indian National Movement · Lecture 1', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'English Literature', title: 'Post-Colonial Literature · Lecture 2', teacher: 'Dr. Robert Vance', room: 'Room 301', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Psychology', title: 'Developmental Psychology · Lecture 3', teacher: 'Dr. Anita Monteiro', room: 'Room 303', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Political Science', title: 'International Relations · Lecture 4', teacher: 'Prof. Meera Chodankar', room: 'Room 302', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Sociology', title: 'Sociological Theories · Lecture 5', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false }
    ],
    Wednesday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Political Science', title: 'Public Administration · Lecture 1', teacher: 'Prof. Meera Chodankar', room: 'Room 302', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'History', title: 'World Civilization · Lecture 2', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'English Literature', title: 'Literary Criticism · Lecture 3', teacher: 'Dr. Robert Vance', room: 'Room 301', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Psychology', title: 'Social Psychology · Lecture 4', teacher: 'Dr. Anita Monteiro', room: 'Room 303', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Sociology', title: 'Research Methods · Lecture 5', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false }
    ],
    Thursday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'Psychology', title: 'Cognitive Psychology · Lecture 1', teacher: 'Dr. Anita Monteiro', room: 'Room 303', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'English Literature', title: 'Drama & Theatre Studies · Lecture 2', teacher: 'Dr. Robert Vance', room: 'Room 301', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'History', title: 'Modern Goa History · Lecture 3', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Political Science', title: 'Comparative Politics · Lecture 4', teacher: 'Prof. Meera Chodankar', room: 'Room 302', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Sociology', title: 'Urban Sociology · Lecture 5', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false }
    ],
    Friday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'English Literature', title: 'Poetry Through Ages · Lecture 1', teacher: 'Dr. Robert Vance', room: 'Room 301', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'Political Science', title: 'Indian Constitution · Lecture 2', teacher: 'Prof. Meera Chodankar', room: 'Room 302', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'History', title: 'Historiography · Lecture 3', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Sociology', title: 'Social Stratification · Lecture 4', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Psychology', title: 'Abnormal Psychology · Lecture 5', teacher: 'Dr. Anita Monteiro', room: 'Room 303', swapped: false }
    ],
    Saturday: [
      { slot: 1, time: '08:00 AM - 09:00 AM', start: '08:00 AM', subject: 'History', title: 'Asian History Seminar · Lecture 1', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false },
      { slot: 2, time: '09:00 AM - 10:00 AM', start: '09:00 AM', subject: 'English Literature', title: 'Creative Writing Workshop · Lecture 2', teacher: 'Dr. Robert Vance', room: 'Room 301', swapped: false },
      { slot: 3, time: '10:00 AM - 11:00 AM', start: '10:00 AM', subject: 'Political Science', title: 'Political Ideologies · Lecture 3', teacher: 'Prof. Meera Chodankar', room: 'Room 302', swapped: false },
      { slot: 4, time: '11:30 AM - 12:30 PM', start: '11:30 AM', subject: 'Psychology', title: 'Practical Experimental Session', teacher: 'Dr. Anita Monteiro', room: 'Room 303', swapped: false },
      { slot: 5, time: '12:30 PM - 01:30 PM', start: '12:30 PM', subject: 'Sociology', title: 'Community Outreach & Fieldwork', teacher: 'Prof. Sanjay Bandodkar', room: 'Room 301', swapped: false }
    ]
  }
};

// Render Mobile App Student Dashboard
function renderDashboard(user) {
  if (!user) return;
  currentLoggedInUser = user;

  const avatar = document.getElementById('dashAvatar');
  const name = document.getElementById('dashUserName');
  const idEl = document.getElementById('dashUserId');
  const roleEl = document.getElementById('dashUserRole');
  const heroTitle = document.getElementById('dashHeroTitle');

  if (avatar) avatar.textContent = user.fullName.split(' ').map(p => p[0]).slice(0, 2).join('');
  if (name) name.textContent = user.fullName;
  if (idEl) idEl.textContent = user.id || user.email;
  if (roleEl) roleEl.textContent = user.roleDisplayName || user.role || 'Student';

  // Determine user's stream
  let stream = 'SCIENCE';
  const checkStr = ((user.department || '') + ' ' + (user.course || '') + ' ' + (user.id || '') + ' ' + (user.email || '')).toUpperCase();
  if (checkStr.includes('COMMERCE') || checkStr.includes('B.COM') || checkStr.includes('COM-') || checkStr.includes('SHAH')) {
    stream = 'COMMERCE';
  } else if (checkStr.includes('ARTS') || checkStr.includes('B.A') || checkStr.includes('ART-') || checkStr.includes('SHARMA')) {
    stream = 'ARTS';
  }

  activeWebTimetableStream = stream;
  if (heroTitle) {
    heroTitle.textContent = `${timetableDatabase[stream].name} • Academic Schedule`;
  }

  renderStudentTimetable();
}

// Render Student Timetable for Active Day
function renderStudentTimetable() {
  const container = document.getElementById('studentTimetableSlotsList');
  const subtitle = document.getElementById('studentTimetableSubtitle');
  if (!container) return;

  const streamData = timetableDatabase[activeWebTimetableStream] || timetableDatabase.SCIENCE;
  const slots = streamData[activeStudentTimetableDay] || streamData.Monday;

  if (subtitle) {
    subtitle.textContent = `${streamData.name} • ${activeStudentTimetableDay} Timetable`;
  }

  let html = '';
  slots.forEach((s, idx) => {
    const isSwapped = s.swapped;
    const badgeText = isSwapped ? '🔄 Swapped' : 'Regular';
    const badgeColor = isSwapped ? 'background:#FFF4D4; color:#B45309;' : 'background:#FFFFFF; color:#222;';

    html += `
      <div style="display:flex; align-items:center; justify-content:space-between; padding:10px 12px; background:#F8FAFC; border-radius:10px; border:1px solid #E2E8F0;">
        <div style="display:flex; align-items:center; gap:10px;">
          <div style="font-size:11px; font-weight:800; color:#7B52DE; min-width:62px;">${s.start}</div>
          <div>
            <div style="font-size:12.5px; font-weight:800; color:#18191E;">${s.title}</div>
            <div style="font-size:11px; color:#64748B;">${s.teacher} • ${s.room}</div>
            ${isSwapped ? `<div style="font-size:10.5px; color:#B45309; font-style:italic;">🔄 ${s.swapNote || 'Mutually interchanged'}</div>` : ''}
          </div>
        </div>
        <span style="font-size:10px; font-weight:700; padding:3px 8px; border-radius:100px; ${badgeColor}">${badgeText}</span>
      </div>
    `;

    // Recess / Break (11:00 AM - 11:30 AM) after slot 3
    if (s.slot === 3) {
      html += `
        <div style="display:flex; align-items:center; justify-content:space-between; padding:8px 12px; background:#D9F3EC; border-radius:10px; border:1px solid #B8E8DC;">
          <div style="display:flex; align-items:center; gap:10px;">
            <div style="font-size:11px; font-weight:800; color:#0D7058; min-width:62px;">11:00 AM</div>
            <div>
              <div style="font-size:12px; font-weight:800; color:#0D7058;">🥪 Recess / Break (11:00 AM - 11:30 AM)</div>
              <div style="font-size:10.5px; color:#1F8E73;">College Canteen &amp; Campus Common Rooms</div>
            </div>
          </div>
          <span style="font-size:10px; font-weight:700; padding:2px 8px; border-radius:100px; background:#FFF; color:#0D7058;">Recess</span>
        </div>
      `;
    }
  });

  container.innerHTML = html;
}

// Student Switch Day
function switchStudentTimetableDay(day, btn) {
  activeStudentTimetableDay = day;
  document.querySelectorAll('.s-tt-day-chip').forEach(b => {
    b.classList.remove('active');
    b.style.background = '#FFF';
    b.style.color = '#555';
    b.style.border = '1px solid #DDD';
  });
  if (btn) {
    btn.classList.add('active');
    btn.style.background = '#7B52DE';
    btn.style.color = '#FFF';
    btn.style.border = 'none';
  }
  renderStudentTimetable();
}

function toggleStudentTimetableFull() {
  showToast(`📅 Showing full ${activeWebTimetableStream} weekly timetable`);
  const modal = document.getElementById('webInterchangeModal');
  if (modal) {
    // If teacher, allow interchange; if student, prompt
    if (currentLoggedInUser && (currentLoggedInUser.role === 'DIRECTOR' || currentLoggedInUser.role === 'FACULTY')) {
      openWebInterchangeModal();
    } else {
      showToast(`Showing timetable for ${timetableDatabase[activeWebTimetableStream].name}. Tap any day chip to view lectures.`);
    }
  }
}

// Render Master Timetable for Teacher Portal
function renderTeacherTimetable() {
  const container = document.getElementById('teacherTimetableCardContainer');
  if (!container) return;

  const streamData = timetableDatabase[activeWebTimetableStream] || timetableDatabase.SCIENCE;
  const slots = streamData[activeWebTimetableDay] || streamData.Monday;

  let rowsHtml = '';
  slots.forEach(s => {
    const isSwapped = s.swapped;
    const badgeText = isSwapped ? '🔄 Swapped' : 'Regular';
    const badgeStyle = isSwapped
      ? 'background:#FFF4D4; color:#B45309; border:1px solid #FCD34D;'
      : 'background:#F1EEFD; color:#5B32BE; border:1px solid #E2DBFA;';

    rowsHtml += `
      <tr>
        <td style="font-weight:700; color:#5B32BE; width:130px;">
          Slot ${s.slot}<br>
          <span style="font-size:11px; color:#64748B; font-weight:normal;">${s.time}</span>
        </td>
        <td>
          <div style="font-weight:700; color:#18191E; font-size:13.5px;">${s.title}</div>
          <div style="font-size:12px; color:#64748B;">Faculty: <strong>${s.teacher}</strong> • Room / Hall: <strong>${s.room}</strong></div>
          ${isSwapped ? `<div style="font-size:11.5px; color:#B45309; margin-top:3px;">🔄 <em>${s.swapNote || 'Mutually interchanged'}</em></div>` : ''}
        </td>
        <td style="width:130px; text-align:right;">
          <span style="font-size:11px; font-weight:700; padding:4px 10px; border-radius:100px; ${badgeStyle}">${badgeText}</span>
        </td>
      </tr>
    `;

    // Recess Row after slot 3
    if (s.slot === 3) {
      rowsHtml += `
        <tr style="background:#F0FAF7;">
          <td style="font-weight:800; color:#0D7058;">
            🥪 Recess<br>
            <span style="font-size:11px; color:#1F8E73; font-weight:normal;">11:00 AM - 11:30 AM</span>
          </td>
          <td colspan="2">
            <div style="font-weight:800; color:#0D7058; font-size:13px;">Recess / Mid-Day Academic Break (30 Mins)</div>
            <div style="font-size:11.5px; color:#1F8E73;">College Canteen, Faculty Common Room &amp; Library Period</div>
          </td>
        </tr>
      `;
    }
  });

  container.innerHTML = `
    <table class="t-data-table" style="width:100%;">
      <thead>
        <tr>
          <th>Slot / Timing</th>
          <th>Subject, Faculty &amp; Room Details (${streamData.name} • ${activeWebTimetableDay})</th>
          <th style="text-align:right;">Status</th>
        </tr>
      </thead>
      <tbody>
        ${rowsHtml}
      </tbody>
    </table>
  `;
}

// Teacher Portal Stream Switcher
function switchWebTimetableStream(stream) {
  activeWebTimetableStream = stream;
  ['SCIENCE', 'COMMERCE', 'ARTS'].forEach(s => {
    const btn = document.getElementById('ttStream_' + s);
    if (btn) {
      const active = s === stream;
      btn.classList.toggle('active', active);
      btn.style.background = active ? '#7B52DE' : 'transparent';
      btn.style.color = active ? '#FFF' : '#555';
    }
  });
  renderTeacherTimetable();
}

// Teacher Portal Day Switcher
function switchWebTimetableDay(day, btn) {
  activeWebTimetableDay = day;
  document.querySelectorAll('.t-tt-day-btn').forEach(b => {
    b.classList.remove('active');
    b.style.background = '#FFF';
    b.style.color = '#555';
    b.style.border = '1px solid #DDD';
  });
  if (btn) {
    btn.classList.add('active');
    btn.style.background = '#7B52DE';
    btn.style.color = '#FFF';
    btn.style.border = '1px solid #7B52DE';
  }
  renderTeacherTimetable();
}

// Modal Handlers for Lecture Interchange
function openWebInterchangeModal() {
  const streamSelect = document.getElementById('interchangeStreamSelect');
  const daySelect = document.getElementById('interchangeDaySelect');
  if (streamSelect) streamSelect.value = activeWebTimetableStream;
  if (daySelect) daySelect.value = activeWebTimetableDay;

  populateInterchangeSlots();
  const modal = document.getElementById('webInterchangeModal');
  if (modal) modal.classList.add('active');
}

function closeWebInterchangeModal() {
  const modal = document.getElementById('webInterchangeModal');
  if (modal) modal.classList.remove('active');
}

function populateInterchangeSlots() {
  const stream = document.getElementById('interchangeStreamSelect').value;
  const day = document.getElementById('interchangeDaySelect').value;
  const s1 = document.getElementById('interchangeSlot1Select');
  const s2 = document.getElementById('interchangeSlot2Select');
  if (!s1 || !s2) return;

  const slots = timetableDatabase[stream][day];
  s1.innerHTML = slots.map(s => `<option value="${s.slot}">Slot ${s.slot} (${s.time}): ${s.subject} - ${s.teacher}</option>`).join('');
  s2.innerHTML = slots.map(s => `<option value="${s.slot}">Slot ${s.slot} (${s.time}): ${s.subject} - ${s.teacher}</option>`).join('');

  if (slots.length > 1) {
    s1.selectedIndex = 0;
    s2.selectedIndex = 1;
  }
}

function handleWebInterchangeSubmit(event) {
  event.preventDefault();
  const stream = document.getElementById('interchangeStreamSelect').value;
  const day = document.getElementById('interchangeDaySelect').value;
  const slot1Num = parseInt(document.getElementById('interchangeSlot1Select').value, 10);
  const slot2Num = parseInt(document.getElementById('interchangeSlot2Select').value, 10);
  const reason = document.getElementById('interchangeReasonInput').value.trim();
  const broadcast = document.getElementById('interchangeBroadcastCheckbox').checked;

  if (slot1Num === slot2Num) {
    showToast("⚠️ Please select two different lecture slots to interchange.");
    return;
  }

  const slots = timetableDatabase[stream][day];
  const slotA = slots.find(s => s.slot === slot1Num);
  const slotB = slots.find(s => s.slot === slot2Num);

  if (!slotA || !slotB) {
    showToast("❌ Could not find selected slots.");
    return;
  }

  // Swap subjects, titles, teachers, and rooms
  const tempSubject = slotA.subject;
  const tempTitle = slotA.title;
  const tempTeacher = slotA.teacher;
  const tempRoom = slotA.room;

  slotA.subject = slotB.subject;
  slotA.title = slotB.title.replace(/Lecture \d+/, `Lecture ${slotA.slot}`);
  slotA.teacher = slotB.teacher;
  slotA.room = slotB.room;
  slotA.swapped = true;
  slotA.swapNote = `Swapped with Slot ${slotB.slot}: ${reason}`;

  slotB.subject = tempSubject;
  slotB.title = tempTitle.replace(/Lecture \d+/, `Lecture ${slotB.slot}`);
  slotB.teacher = tempTeacher;
  slotB.room = tempRoom;
  slotB.swapped = true;
  slotB.swapNote = `Swapped with Slot ${slotA.slot}: ${reason}`;

  // If broadcast checked, add notice to Announcements
  if (broadcast) {
    const authorName = currentLoggedInUser ? currentLoggedInUser.fullName : 'Faculty Coordination Committee';
    const newNotice = {
      id: Date.now(),
      ref: `NOT-SWAP-${Date.now().toString().slice(-4)}`,
      title: `📢 Timetable Update: ${day} Lectures Interchanged (${timetableDatabase[stream].name})`,
      category: 'Academic Timetable',
      audience: timetableDatabase[stream].name,
      content: `Please note that Slot ${slotA.slot} (${slotA.time} - ${slotA.subject}) and Slot ${slotB.slot} (${slotB.time} - ${slotB.subject}) have been interchanged for ${day}.\n\nReason: ${reason}`,
      author: `${authorName} (Faculty)`,
      date: 'Today'
    };
    collegeAnnouncements.unshift(newNotice);
    renderAnnouncements();
  }

  closeWebInterchangeModal();
  renderTeacherTimetable();
  renderStudentTimetable();
  showToast("🎉 Lectures interchanged successfully! Students' timetable updated.");
}

// Dismiss Splash Overlay (Click or 2.4s Timer)
let splashDismissed = false;
function dismissSplash() {
  if (splashDismissed) return;
  splashDismissed = true;
  const splash = document.getElementById('appSplashScreen');
  if (splash) {
    splash.classList.add('hidden');
    setTimeout(() => {
      splash.style.display = 'none';
    }, 450);
  }
}

// Initialize on window load
window.addEventListener('DOMContentLoaded', () => {
  switchMode('LOGIN');
  renderStudentsList('Arts');
  renderAllFacultyRoster();
  renderAnnouncements();
  renderTeacherTimetable();
  renderStudentTimetable();

  // 3s delay matching Android SplashActivity
  setTimeout(() => {
    dismissSplash();
  }, 3000);
});



