package dao;

import utils.PasswordUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Manages database connectivity for the Startup Incubation System.
 * Supports MySQL as the primary database with an automatic persistent
 * embedded fallback (H2 in MySQL mode) when MySQL is not running locally.
 */
public class DatabaseConnection {

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static String dbDriver;
    private static boolean fallbackEnabled = true;

    private static boolean isFallback = false;
    private static boolean initialized = false;
    private static String activeDatabaseName = "Unknown";

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        Properties props = new Properties();

        // 1. Try to read from db.properties in working directory
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
            } catch (Exception e) {
                System.err.println("Notice: Could not read db.properties: " + e.getMessage());
            }
        }

        // 2. Read properties with environment variable overrides
        dbDriver = System.getenv("DB_DRIVER");
        if (dbDriver == null || dbDriver.isEmpty()) {
            dbDriver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        }

        dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = props.getProperty("db.url",
                    "jdbc:mysql://localhost:3306/startup_incubation?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        }

        dbUser = System.getenv("DB_USER");
        if (dbUser == null) {
            dbUser = props.getProperty("db.user", "root");
        }

        dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null) {
            dbPassword = props.getProperty("db.password", "");
        }

        String fallbackProp = props.getProperty("db.fallback.enabled", "true");
        fallbackEnabled = Boolean.parseBoolean(fallbackProp);
    }

    /**
     * Obtains an active connection to the database.
     * Initializes tables and default records on first call.
     */
    public static synchronized Connection getConnection() throws SQLException {
        Connection conn = null;

        // Try MySQL first
        if (!isFallback) {
            try {
                Class.forName(dbDriver);
                conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                activeDatabaseName = "MySQL (" + dbUrl + ")";
            } catch (Exception ex) {
                if (fallbackEnabled) {
                    System.out.println("------------------------------------------------------------------");
                    System.out.println("[DatabaseConnection] MySQL not reachable: " + ex.getMessage());
                    System.out.println("[DatabaseConnection] Switching to persistent local storage (H2 in MySQL mode)...");
                    System.out.println("------------------------------------------------------------------");
                    isFallback = true;
                } else {
                    throw new SQLException("Failed to connect to MySQL database at " + dbUrl +
                            ". Ensure MySQL is running on port 3306 and check db.properties credentials.", ex);
                }
            }
        }

        // Fallback to embedded persistent H2 database in MySQL mode
        if (isFallback) {
            try {
                Class.forName("org.h2.Driver");
                File dataDir = new File("data");
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                }
                String fallbackUrl = "jdbc:h2:./data/startup_incubation;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE";
                conn = DriverManager.getConnection(fallbackUrl, "sa", "");
                activeDatabaseName = "Embedded Local Database (H2 MySQL Compatibility Mode)";
            } catch (Exception e) {
                throw new SQLException("Failed to initialize embedded fallback database: " + e.getMessage(), e);
            }
        }

        if (!initialized && conn != null) {
            initializeDatabase(conn);
            initialized = true;
        }

        return conn;
    }

    public static boolean isFallbackMode() {
        return isFallback;
    }

    public static String getActiveDatabaseName() {
        return activeDatabaseName;
    }

    /**
     * Creates all necessary tables and inserts seed data if not present.
     */
    public static synchronized void initializeDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            // 1. Users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(150) NOT NULL UNIQUE, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "phone VARCHAR(20), " +
                    "role VARCHAR(20) NOT NULL DEFAULT 'FOUNDER', " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 2. Startups
            stmt.execute("CREATE TABLE IF NOT EXISTS startups (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "founder_id INT NOT NULL, " +
                    "name VARCHAR(150) NOT NULL, " +
                    "category VARCHAR(50) NOT NULL, " +
                    "description TEXT, " +
                    "stage VARCHAR(50) DEFAULT 'Ideation', " +
                    "status VARCHAR(30) DEFAULT 'Under Review', " +
                    "reviewed_by_admin_id INT NULL, " +
                    "reviewed_by_admin_name VARCHAR(150) NULL, " +
                    "admin_remarks TEXT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (founder_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");

            // Migration check: ensure review columns exist if table was already created
            try { stmt.execute("ALTER TABLE startups ADD COLUMN reviewed_by_admin_id INT NULL"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE startups ADD COLUMN reviewed_by_admin_name VARCHAR(150) NULL"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE startups ADD COLUMN admin_remarks TEXT NULL"); } catch (Exception ignored) {}

            // 3. Incubation Programs
            stmt.execute("CREATE TABLE IF NOT EXISTS incubation_programs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(150) NOT NULL, " +
                    "duration VARCHAR(50), " +
                    "description TEXT, " +
                    "eligibility VARCHAR(255), " +
                    "status VARCHAR(20) DEFAULT 'ACTIVE'" +
                    ")");

            // 4. Incubation Applications
            stmt.execute("CREATE TABLE IF NOT EXISTS incubation_applications (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "startup_id INT NOT NULL, " +
                    "program_id INT, " +
                    "pitch_deck_url VARCHAR(255), " +
                    "objectives TEXT, " +
                    "status VARCHAR(30) DEFAULT 'Under Review', " +
                    "admin_feedback TEXT, " +
                    "reviewed_by_admin_id INT NULL, " +
                    "reviewed_by_admin_name VARCHAR(150) NULL, " +
                    "applied_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "decided_date TIMESTAMP NULL, " +
                    "FOREIGN KEY (startup_id) REFERENCES startups(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (program_id) REFERENCES incubation_programs(id) ON DELETE SET NULL" +
                    ")");

            // Migration check: ensure review columns exist if table was already created
            try { stmt.execute("ALTER TABLE incubation_applications ADD COLUMN reviewed_by_admin_id INT NULL"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE incubation_applications ADD COLUMN reviewed_by_admin_name VARCHAR(150) NULL"); } catch (Exception ignored) {}

            // 5. Mentors
            stmt.execute("CREATE TABLE IF NOT EXISTS mentors (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "expertise VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(150) NOT NULL, " +
                    "phone VARCHAR(20), " +
                    "bio TEXT, " +
                    "status VARCHAR(20) DEFAULT 'Active', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 6. Mentorship Requests
            stmt.execute("CREATE TABLE IF NOT EXISTS mentorship_requests (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "startup_id INT NOT NULL, " +
                    "mentor_id INT, " +
                    "topic VARCHAR(200) NOT NULL, " +
                    "notes TEXT, " +
                    "status VARCHAR(30) DEFAULT 'Pending', " +
                    "admin_remarks TEXT, " +
                    "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (startup_id) REFERENCES startups(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (mentor_id) REFERENCES mentors(id) ON DELETE SET NULL" +
                    ")");

            // 7. Events
            stmt.execute("CREATE TABLE IF NOT EXISTS events (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(150) NOT NULL, " +
                    "event_date VARCHAR(50) NOT NULL, " +
                    "venue VARCHAR(150) NOT NULL, " +
                    "capacity INT DEFAULT 50, " +
                    "description TEXT, " +
                    "status VARCHAR(20) DEFAULT 'UPCOMING', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 8. Event Registrations
            stmt.execute("CREATE TABLE IF NOT EXISTS event_registrations (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "event_id INT NOT NULL, " +
                    "user_id INT NOT NULL, " +
                    "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE (event_id, user_id), " +
                    "FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");

            // 9. Funding Opportunities
            stmt.execute("CREATE TABLE IF NOT EXISTS funding_opportunities (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(150) NOT NULL, " +
                    "max_amount DOUBLE NOT NULL, " +
                    "deadline VARCHAR(50), " +
                    "description TEXT, " +
                    "status VARCHAR(20) DEFAULT 'OPEN', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 10. Funding Applications
            stmt.execute("CREATE TABLE IF NOT EXISTS funding_applications (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "startup_id INT NOT NULL, " +
                    "opportunity_id INT, " +
                    "amount_requested DOUBLE NOT NULL, " +
                    "amount_approved DOUBLE DEFAULT 0.0, " +
                    "proposal TEXT, " +
                    "status VARCHAR(30) DEFAULT 'Under Review', " +
                    "admin_feedback TEXT, " +
                    "applied_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "decided_date TIMESTAMP NULL, " +
                    "FOREIGN KEY (startup_id) REFERENCES startups(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (opportunity_id) REFERENCES funding_opportunities(id) ON DELETE SET NULL" +
                    ")");

            // Insert Default Seed Data if tables are empty
            seedInitialData(conn);

        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Error during schema initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn) {
        try {
            // Check if users exist
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("[DatabaseConnection] Seeding initial users and records...");

                    // 1. Admin
                    String adminPassHash = PasswordUtil.hashPassword("admin123");
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO users (full_name, email, password_hash, phone, role, status) VALUES (?, ?, ?, ?, ?, ?)")) {
                        ps.setString(1, "System Administrator");
                        ps.setString(2, "admin@gmail.com");
                        ps.setString(3, adminPassHash);
                        ps.setString(4, "9876543210");
                        ps.setString(5, "ADMIN");
                        ps.setString(6, "ACTIVE");
                        ps.executeUpdate();
                    }

                    // 2. Demo Founder
                    String founderPassHash = PasswordUtil.hashPassword("founder123");
                    int founderId = 0;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO users (full_name, email, password_hash, phone, role, status) VALUES (?, ?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, "Udit Sharma");
                        ps.setString(2, "founder@gmail.com");
                        ps.setString(3, founderPassHash);
                        ps.setString(4, "+91 98765 43210");
                        ps.setString(5, "FOUNDER");
                        ps.setString(6, "ACTIVE");
                        ps.executeUpdate();
                        try (ResultSet gk = ps.getGeneratedKeys()) {
                            if (gk.next()) founderId = gk.getInt(1);
                        }
                    }

                    // 3. Demo Startup for founder
                    int startupId = 0;
                    if (founderId > 0) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO startups (founder_id, name, category, description, stage, status) VALUES (?, ?, ?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS)) {
                            ps.setInt(1, founderId);
                            ps.setString(2, "TechNova");
                            ps.setString(3, "Technology");
                            ps.setString(4, "AI-based smart farming solutions for small farmers.");
                            ps.setString(5, "Early Traction");
                            ps.setString(6, "Under Review");
                            ps.executeUpdate();
                            try (ResultSet gk = ps.getGeneratedKeys()) {
                                if (gk.next()) startupId = gk.getInt(1);
                            }
                        }
                    }

                    // 4. Incubation Programs
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO incubation_programs (title, duration, description, eligibility, status) VALUES (?, ?, ?, ?, ?)")) {
                        ps.setString(1, "Seed Accelerator Cohort 2026");
                        ps.setString(2, "6 Months");
                        ps.setString(3, "Intensive acceleration program with seed funding, co-working space, and masterclasses.");
                        ps.setString(4, "Early stage tech & product startups");
                        ps.setString(5, "ACTIVE");
                        ps.executeUpdate();

                        ps.setString(1, "DeepTech & AI Incubation");
                        ps.setString(2, "12 Months");
                        ps.setString(3, "Advanced incubation support with high-performance compute labs and IP guidance.");
                        ps.setString(4, "Startups working in AI, Robotics, Biotech");
                        ps.setString(5, "ACTIVE");
                        ps.executeUpdate();

                        ps.setString(1, "Women In Innovation Fellowship");
                        ps.setString(2, "4 Months");
                        ps.setString(3, "Tailored mentorship and founder grants for women-led startups.");
                        ps.setString(4, "At least 50% women co-founders");
                        ps.setString(5, "ACTIVE");
                        ps.executeUpdate();
                    }

                    // 5. Sample Incubation Application for TechNova
                    if (startupId > 0) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO incubation_applications (startup_id, program_id, pitch_deck_url, objectives, status) VALUES (?, 1, ?, ?, ?)")) {
                            ps.setInt(1, startupId);
                            ps.setString(2, "https://technova.io/pitch.pdf");
                            ps.setString(3, "Scale IoT sensor network across 500 farms in North India.");
                            ps.setString(4, "Under Review");
                            ps.executeUpdate();
                        }
                    }

                    // 6. Mentors
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO mentors (name, expertise, email, phone, bio, status) VALUES (?, ?, ?, ?, ?, ?)")) {
                        ps.setString(1, "Rahul Sharma");
                        ps.setString(2, "Startup Strategy & GTM");
                        ps.setString(3, "mentor@example.com");
                        ps.setString(4, "+91 98111 22233");
                        ps.setString(5, "Ex-Founder with 12+ years experience in B2B SaaS scaling.");
                        ps.setString(6, "Active");
                        ps.executeUpdate();

                        ps.setString(1, "Sneha Kapoor");
                        ps.setString(2, "Digital Marketing & Growth");
                        ps.setString(3, "sneha@example.com");
                        ps.setString(4, "+91 98222 33344");
                        ps.setString(5, "Growth strategist helping early stage startups reach PMF.");
                        ps.setString(6, "Active");
                        ps.executeUpdate();

                        ps.setString(1, "Vikram Rao");
                        ps.setString(2, "Finance & Pitch Deck");
                        ps.setString(3, "vikram@example.com");
                        ps.setString(4, "+91 98333 44455");
                        ps.setString(5, "Angel investor and chartered accountant with 15+ exits.");
                        ps.setString(6, "Active");
                        ps.executeUpdate();
                    }

                    // 7. Sample Mentorship Request
                    if (startupId > 0) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO mentorship_requests (startup_id, mentor_id, topic, notes, status, admin_remarks) VALUES (?, 1, ?, ?, ?, ?)")) {
                            ps.setInt(1, startupId);
                            ps.setString(2, "Go-To-Market & Pricing Strategy");
                            ps.setString(3, "Need guidance on enterprise pilot pricing for farmer producer organizations.");
                            ps.setString(4, "Assigned");
                            ps.setString(5, "Assigned to Rahul Sharma for initial 4-week advisory sprint.");
                            ps.executeUpdate();
                        }
                    }

                    // 8. Events
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO events (title, event_date, venue, capacity, description, status) VALUES (?, ?, ?, ?, ?, ?)")) {
                        ps.setString(1, "Startup Pitch Competition");
                        ps.setString(2, "25 Aug 2026");
                        ps.setString(3, "Innovation Hall");
                        ps.setInt(4, 80);
                        ps.setString(5, "Present your startup to institutional angel investors and win seed grants.");
                        ps.setString(6, "UPCOMING");
                        ps.executeUpdate();

                        ps.setString(1, "Innovation Workshop: Zero to One");
                        ps.setString(2, "02 Sep 2026");
                        ps.setString(3, "Seminar Hall B");
                        ps.setInt(4, 50);
                        ps.setString(5, "Hands-on product validation and MVP architecture bootcamp.");
                        ps.setString(6, "UPCOMING");
                        ps.executeUpdate();

                        ps.setString(1, "Entrepreneurship Seminar");
                        ps.setString(2, "10 Sep 2026");
                        ps.setString(3, "Auditorium");
                        ps.setInt(4, 150);
                        ps.setString(5, "Keynote session by industry leaders on navigating venture capital.");
                        ps.setString(6, "UPCOMING");
                        ps.executeUpdate();

                        ps.setString(1, "Hackathon 2026");
                        ps.setString(2, "20 Sep 2026");
                        ps.setString(3, "Computer Lab 3");
                        ps.setInt(4, 60);
                        ps.setString(5, "24-hour sprint to build innovative solutions for sustainability.");
                        ps.setString(6, "UPCOMING");
                        ps.executeUpdate();

                        ps.setString(1, "Investor Meet & Networking Dinner");
                        ps.setString(2, "30 Sep 2026");
                        ps.setString(3, "Conference Room");
                        ps.setInt(4, 40);
                        ps.setString(5, "Curated one-on-one sessions between incubated founders and VC analysts.");
                        ps.setString(6, "UPCOMING");
                        ps.executeUpdate();
                    }

                    // 9. Funding Opportunities
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO funding_opportunities (title, max_amount, deadline, description, status) VALUES (?, ?, ?, ?, ?)")) {
                        ps.setString(1, "Seed Innovation Grant");
                        ps.setDouble(2, 500000.0);
                        ps.setString(3, "30 Sep 2026");
                        ps.setString(4, "Grant for early prototypes and patent applications. Zero equity taken.");
                        ps.setString(5, "OPEN");
                        ps.executeUpdate();

                        ps.setString(1, "Early-Stage Growth Capital");
                        ps.setDouble(2, 1500000.0);
                        ps.setString(3, "15 Oct 2026");
                        ps.setString(4, "Seed funding for startups with demonstrable initial user traction and revenue.");
                        ps.setString(5, "OPEN");
                        ps.executeUpdate();

                        ps.setString(1, "Women Techmakers Fund");
                        ps.setDouble(2, 1000000.0);
                        ps.setString(3, "30 Oct 2026");
                        ps.setString(4, "Dedicated capital allocation for women-founded technology ventures.");
                        ps.setString(5, "OPEN");
                        ps.executeUpdate();
                    }

                    // 10. Sample Funding Application
                    if (startupId > 0) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO funding_applications (startup_id, opportunity_id, amount_requested, amount_approved, proposal, status) VALUES (?, 1, ?, 0.0, ?, ?)")) {
                            ps.setInt(1, startupId);
                            ps.setDouble(2, 500000.0);
                            ps.setString(3, "Deployment of IoT soil health monitoring stations across 10 rural agricultural clusters.");
                            ps.setString(4, "Under Review");
                            ps.executeUpdate();
                        }
                    }

                    System.out.println("[DatabaseConnection] Initial database seeding completed successfully.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Warning during seed data insertion: " + e.getMessage());
        }
    }

    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
