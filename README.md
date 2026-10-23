# Startup Incubation & Innovation Management System

**Course:** Object Oriented Programming using Java (CCSE0355)  
**Institute:** NIET Greater Noida  
**Technologies:** Java, Swing, JDBC, MySQL, BCrypt  
**Entry Point:** `Main.java`

---

## 1. System Overview

The **Startup Incubation & Innovation Management System** is a full-featured Java Swing desktop application connected via a clean 4-tier object-oriented architecture:

```
Swing GUI  ──►  Service Layer  ──►  DAO Layer  ──►  JDBC (MySQL / Embedded Local)
```

The system provides complete separation of concerns with role-based access control for **Founders** and **Administrators**.

---

## 2. Key Modules & Features

### 🔐 Authentication & Security
- **Founder Registration**: Registers new founder accounts with full validation, unique email enforcement, and BCrypt password hashing. Automatically initializes the founder's startup profile.
- **Secure Login**: Verifies credentials against the database using BCrypt. Opens role-appropriate dashboards (`FounderDashboard` or `AdminDashboard`).
- **Session Management**: Tracks logged-in user in memory with role checks (`isAdmin()`, `isFounder()`) and clear logout.

### 🚀 Founder Dashboard Modules
1. **Dashboard Home**: Live database-backed metrics (Startup status, Incubation status, Funding status, Assigned Mentor), upcoming events list, and working quick-action navigation.
2. **My Startup**: Complete ownership enforcement (founders view and manage only their own startup). Interactive dialog to edit name, category, stage, and value proposition.
3. **Incubation**: Browse active incubation cohort programs, submit new applications with pitch deck and milestone objectives, view review decisions and committee feedback.
4. **Mentorship**: Explore active mentors, request mentorship sessions on specific topics, track committee assignment and mentor remarks.
5. **Events & Innovation**: Browse published incubation events, register with duplicate registration prevention and capacity enforcement.
6. **Funding Grants**: Browse grant schemes, submit funding applications with milestone utilization plans, review sanctioned amounts and committee feedback.
7. **Profile Settings**: View account information, update name & phone, and securely change password with old password verification.

### 🛡️ Administrator Command Center Modules
1. **Executive Dashboard**: Real-time aggregate counts for startups, pending applications, approved cohorts, active mentors, funding applications, and upcoming events.
2. **Startup Directory**: Review all registered startups, inspect full venture profiles, approve or reject startups with immediate database synchronization.
3. **Incubation Review**: Review applications from founders, record approval or rejection with customized committee feedback visible to founders.
4. **Mentor Management**: Onboard new industry mentors (name, expertise, email, phone, bio), toggle active/inactive status, review and assign mentorship requests submitted by founders.
5. **Event Management**: Publish new events (title, date, venue, capacity, description), edit existing events, and cancel/delete events.
6. **Funding Capital Decisions**: Review funding applications, sanction grant amounts, provide disbursement terms, or reject applications with reasons. Publish new grant schemes.
7. **User Directory & Access Control**: View all registered accounts, audit roles, and toggle active/disabled status (with primary admin protection).
8. **Reports & Audit**: View comprehensive metrics, preview formatted incubation audit reports, and export reports to disk via `JFileChooser`.

---

## 3. Database Integration & Setup

### Database Configuration (`db.properties`)
Database configuration is loaded from `db.properties` (with environment variable override support):

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/startup_incubation?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=
db.fallback.enabled=true
```

### Option A: Running with MySQL (Recommended for Production / Labs)
1. Ensure your MySQL server (or XAMPP MySQL) is running on port 3306.
2. Open MySQL CLI or phpMyAdmin and import the schema:
   ```powershell
   mysql -u root -p < schema.sql
   ```
3. Update `db.properties` with your MySQL user and password if different from defaults.

### Option B: Automatic Local Fallback (Works Out-of-the-Box)
If MySQL is not installed or currently stopped, `DatabaseConnection` automatically falls back to an embedded persistent local database (`./data/startup_incubation`) running in full MySQL compatibility mode. The application will initialize tables and seed data automatically and remain fully functional with complete data persistence across restarts!

---

## 4. Default Demo Accounts

| Role | Email | Password |
|---|---|---|
| **Founder** | `founder@gmail.com` | `founder123` |
| **Admin** | `admin@gmail.com` | `admin123` |

*(New founders can also be registered at any time from the Registration screen)*

---

## 5. Build and Execution Instructions

### Using Windows PowerShell / Terminal:

#### 1. Compile the Project:
```powershell
javac -cp "lib/*;src" -d bin (Get-ChildItem -Path src -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName)
```

#### 2. Run the Automated End-to-End Test Suite:
```powershell
java -cp "lib/*;bin" test.EndToEndTests
```
*(Runs comprehensive tests for Workflows A, B, C, D, E, and F)*

#### 3. Launch the Application:
```powershell
java -cp "lib/*;bin" Main
```

### Quick 1-Click Launchers:
- Double-click **`run.bat`** to compile and start the application.
- Double-click **`test.bat`** to run the test suite.

---

## 6. End-to-End Test Results

All 6 required workflows have been rigorously verified:
- **Workflow A (Authentication)**: Founder registration, BCrypt password hashing, credential verification, duplicate rejection — **PASSED**
- **Workflow B (Startup & Incubation)**: Startup profile creation, admin visibility, incubation application submission, admin approval with feedback, founder synchronization — **PASSED**
- **Workflow C (Mentorship)**: Mentor onboarding, active listing, session request, admin assignment, founder synchronization — **PASSED**
- **Workflow D (Events)**: Event publishing, registration, duplicate blocking, capacity limit enforcement — **PASSED**
- **Workflow E (Funding)**: Grant opportunity creation, funding proposal submission, admin sanction with approved amount and feedback, founder verification — **PASSED**
- **Workflow F (Persistence & Authorization)**: Fresh connection reconnection, credential persistence, founder data isolation, admin account protection — **PASSED**
