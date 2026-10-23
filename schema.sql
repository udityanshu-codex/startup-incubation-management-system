-- ====================================================================
-- STARTUP INCUBATION & INNOVATION MANAGEMENT SYSTEM
-- Course: Object Oriented Programming using Java (CCSE0355)
-- Institute: NIET Greater Noida
-- Database Schema & Initial Seed Data
-- ====================================================================

CREATE DATABASE IF NOT EXISTS startup_incubation;
USE startup_incubation;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'FOUNDER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Startups Table
CREATE TABLE IF NOT EXISTS startups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    founder_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    stage VARCHAR(50) DEFAULT 'Ideation',
    status VARCHAR(30) DEFAULT 'Under Review',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (founder_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Incubation Programs Table
CREATE TABLE IF NOT EXISTS incubation_programs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    duration VARCHAR(50),
    description TEXT,
    eligibility VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- 4. Incubation Applications Table
CREATE TABLE IF NOT EXISTS incubation_applications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    startup_id INT NOT NULL,
    program_id INT,
    pitch_deck_url VARCHAR(255),
    objectives TEXT,
    status VARCHAR(30) DEFAULT 'Under Review',
    admin_feedback TEXT,
    applied_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    decided_date TIMESTAMP NULL,
    FOREIGN KEY (startup_id) REFERENCES startups(id) ON DELETE CASCADE,
    FOREIGN KEY (program_id) REFERENCES incubation_programs(id) ON DELETE SET NULL
);

-- 5. Mentors Table
CREATE TABLE IF NOT EXISTS mentors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    expertise VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    bio TEXT,
    status VARCHAR(20) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Mentorship Requests Table
CREATE TABLE IF NOT EXISTS mentorship_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    startup_id INT NOT NULL,
    mentor_id INT,
    topic VARCHAR(200) NOT NULL,
    notes TEXT,
    status VARCHAR(30) DEFAULT 'Pending',
    admin_remarks TEXT,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (startup_id) REFERENCES startups(id) ON DELETE CASCADE,
    FOREIGN KEY (mentor_id) REFERENCES mentors(id) ON DELETE SET NULL
);

-- 7. Events Table
CREATE TABLE IF NOT EXISTS events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    event_date VARCHAR(50) NOT NULL,
    venue VARCHAR(150) NOT NULL,
    capacity INT DEFAULT 50,
    description TEXT,
    status VARCHAR(20) DEFAULT 'UPCOMING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Event Registrations Table
CREATE TABLE IF NOT EXISTS event_registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_event_user (event_id, user_id),
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 9. Funding Opportunities Table
CREATE TABLE IF NOT EXISTS funding_opportunities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    max_amount DOUBLE NOT NULL,
    deadline VARCHAR(50),
    description TEXT,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. Funding Applications Table
CREATE TABLE IF NOT EXISTS funding_applications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    startup_id INT NOT NULL,
    opportunity_id INT,
    amount_requested DOUBLE NOT NULL,
    amount_approved DOUBLE DEFAULT 0.0,
    proposal TEXT,
    status VARCHAR(30) DEFAULT 'Under Review',
    admin_feedback TEXT,
    applied_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    decided_date TIMESTAMP NULL,
    FOREIGN KEY (startup_id) REFERENCES startups(id) ON DELETE CASCADE,
    FOREIGN KEY (opportunity_id) REFERENCES funding_opportunities(id) ON DELETE SET NULL
);

-- ====================================================================
-- SEED DATA
-- Default Passwords:
-- Admin: admin@gmail.com / admin123
-- Founder: founder@gmail.com / founder123
-- ====================================================================

-- Users
INSERT INTO users (id, full_name, email, password_hash, phone, role, status) VALUES
(1, 'System Administrator', 'admin@gmail.com', '$2a$10$e8T8Wc49R0qXk6Z0oU9m/OD9R51.9G3tQkU34v13k7rZ6zQh6H4qK', '9876543210', 'ADMIN', 'ACTIVE'),
(2, 'Udit Sharma', 'founder@gmail.com', '$2a$10$w4rU8L1zZ36B/gL6XkR5fecy2W2f7r0Q9m2R4q8rZ36B/gL6XkR5f', '+91 98765 43210', 'FOUNDER', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

-- Startups
INSERT INTO startups (id, founder_id, name, category, description, stage, status) VALUES
(1, 2, 'TechNova', 'Technology', 'AI-based smart farming solutions for small farmers.', 'Early Traction', 'Under Review')
ON DUPLICATE KEY UPDATE id=id;

-- Incubation Programs
INSERT INTO incubation_programs (id, title, duration, description, eligibility, status) VALUES
(1, 'Seed Accelerator Cohort 2026', '6 Months', 'Intensive acceleration program with seed funding, co-working space, and masterclasses.', 'Early stage tech & product startups', 'ACTIVE'),
(2, 'DeepTech & AI Incubation', '12 Months', 'Advanced incubation support with high-performance compute labs and IP guidance.', 'Startups working in AI, Robotics, Biotech', 'ACTIVE'),
(3, 'Women In Innovation Fellowship', '4 Months', 'Tailored mentorship and founder grants for women-led startups.', 'At least 50% women co-founders', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

-- Mentors
INSERT INTO mentors (id, name, expertise, email, phone, bio, status) VALUES
(1, 'Rahul Sharma', 'Startup Strategy & GTM', 'mentor@example.com', '+91 98111 22233', 'Ex-Founder with 12+ years experience in B2B SaaS scaling.', 'Active'),
(2, 'Sneha Kapoor', 'Digital Marketing & Growth', 'sneha@example.com', '+91 98222 33344', 'Growth strategist helping early stage startups reach PMF.', 'Active'),
(3, 'Vikram Rao', 'Finance & Pitch Deck', 'vikram@example.com', '+91 98333 44455', 'Angel investor and chartered accountant with 15+ exits.', 'Active')
ON DUPLICATE KEY UPDATE id=id;

-- Events
INSERT INTO events (id, title, event_date, venue, capacity, description, status) VALUES
(1, 'Startup Pitch Competition', '25 Aug 2026', 'Innovation Hall', 80, 'Present your startup to institutional angel investors and win seed grants.', 'UPCOMING'),
(2, 'Innovation Workshop: Zero to One', '02 Sep 2026', 'Seminar Hall B', 50, 'Hands-on product validation and MVP architecture bootcamp.', 'UPCOMING'),
(3, 'Entrepreneurship Seminar', '10 Sep 2026', 'Auditorium', 150, 'Keynote session by industry leaders on navigating venture capital.', 'UPCOMING'),
(4, 'Hackathon 2026', '20 Sep 2026', 'Computer Lab 3', 60, '24-hour sprint to build innovative solutions for sustainability.', 'UPCOMING'),
(5, 'Investor Meet & Networking Dinner', '30 Sep 2026', 'Conference Room', 40, 'Curated one-on-one sessions between incubated founders and VC analysts.', 'UPCOMING')
ON DUPLICATE KEY UPDATE id=id;

-- Funding Opportunities
INSERT INTO funding_opportunities (id, title, max_amount, deadline, description, status) VALUES
(1, 'Seed Innovation Grant', 500000.0, '30 Sep 2026', 'Grant for early prototypes and patent applications. Zero equity taken.', 'OPEN'),
(2, 'Early-Stage Growth Capital', 1500000.0, '15 Oct 2026', 'Seed funding for startups with demonstrable initial user traction and revenue.', 'OPEN'),
(3, 'Women Techmakers Fund', 1000000.0, '30 Oct 2026', 'Dedicated capital allocation for women-founded technology ventures.', 'OPEN')
ON DUPLICATE KEY UPDATE id=id;
