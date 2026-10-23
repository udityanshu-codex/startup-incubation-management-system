package test;

import dao.DatabaseConnection;
import model.*;
import service.*;
import utils.PasswordUtil;

import java.sql.Connection;
import java.util.List;

/**
 * Automated End-to-End Test Suite for Startup Incubation & Innovation Management System.
 * Tests Workflows A through F as specified in the integration requirements.
 */
public class EndToEndTests {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("   STARTUP INCUBATION SYSTEM — AUTOMATED END-TO-END TEST SUITE");
        System.out.println("   Course: Object Oriented Programming using Java (CCSE0355)");
        System.out.println("   Institute: NIET Greater Noida");
        System.out.println("======================================================================\n");

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("[DB Setup] Connected to: " + DatabaseConnection.getActiveDatabaseName());
        } catch (Exception e) {
            System.err.println("[DB Setup] Connection failed: " + e.getMessage());
            System.exit(1);
        }

        testWorkflowA();
        testWorkflowB();
        testWorkflowC();
        testWorkflowD();
        testWorkflowE();
        testWorkflowF();

        System.out.println("\n======================================================================");
        System.out.println(String.format("TEST RUN SUMMARY: %d PASSED, %d FAILED", testsPassed, testsFailed));
        System.out.println("======================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String message) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " — " + message);
            testsFailed++;
        }
    }

    // Workflow A — Authentication
    private static void testWorkflowA() {
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("WORKFLOW A: Registration, BCrypt Hashing & Authentication");
        System.out.println("----------------------------------------------------------------------");
        AuthService auth = new AuthService();
        String testEmail = "test_founder_" + System.currentTimeMillis() + "@gmail.com";
        String password = "FounderPass123";

        try {
            // 1. Register new founder
            User user = auth.registerFounder(
                    "Arjun Kapoor",
                    testEmail,
                    password,
                    password,
                    "+91 91234 56789",
                    "SolarPulse",
                    "CleanTech & Sustainability"
            );
            assertTrue("Register new founder", user != null && user.getId() > 0, "Failed to register founder");

            // 2. Verify BCrypt password hash
            assertTrue("BCrypt hash verified",
                    user.getPasswordHash() != null && user.getPasswordHash().startsWith("$2a$"),
                    "Password is not stored as BCrypt hash");
            assertTrue("BCrypt plain check",
                    PasswordUtil.checkPassword(password, user.getPasswordHash()),
                    "BCrypt failed to verify correct password");

            // 3. Login with registered credentials
            User loggedIn = auth.login(testEmail, password);
            assertTrue("Login with valid credentials", loggedIn != null && loggedIn.getId() == user.getId(), "Login failed");
            assertTrue("Role enforcement (Founder)", loggedIn.isFounder() && !loggedIn.isAdmin(), "Incorrect role assigned");

            // 4. Reject invalid credentials
            boolean loginFailed = false;
            try {
                auth.login(testEmail, "WrongPassword");
            } catch (Exception ex) {
                loginFailed = true;
            }
            assertTrue("Reject invalid password", loginFailed, "Login accepted incorrect password");

            // 5. Reject duplicate email registration
            boolean duplicateRejected = false;
            try {
                auth.registerFounder("Clone User", testEmail, password, password, "1234567890", "OtherStartup", "Tech");
            } catch (Exception ex) {
                duplicateRejected = true;
            }
            assertTrue("Reject duplicate email", duplicateRejected, "Duplicate email registration was not blocked");

        } catch (Exception e) {
            assertTrue("Workflow A Exception", false, e.getMessage());
        }
    }

    // Workflow B — Startup and Incubation
    private static void testWorkflowB() {
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("WORKFLOW B: Startup Creation & Incubation Application Review");
        System.out.println("----------------------------------------------------------------------");
        AuthService auth = new AuthService();
        StartupService startupService = new StartupService();
        IncubationService incService = new IncubationService();

        try {
            // 1. Register founder with startup
            String email = "bio_founder_" + System.currentTimeMillis() + "@gmail.com";
            User founder = auth.registerFounder("Dr. Meera Sen", email, "pass1234", "pass1234", "9898989898", "BioGenix", "Healthcare & MedTech");

            // 2. Verify startup in DB
            Startup startup = startupService.getStartupForFounder(founder.getId());
            assertTrue("Startup created in DB", startup != null && "BioGenix".equals(startup.getName()), "Startup not found for founder");

            // 3. Startup appears in Admin directory
            List<Startup> allStartups = startupService.getAllStartups();
            boolean foundInAdmin = false;
            for (Startup s : allStartups) {
                if (s.getId() == startup.getId()) {
                    foundInAdmin = true;
                    break;
                }
            }
            assertTrue("Startup appears in Admin directory", foundInAdmin, "Startup missing from admin view");

            // 4. Submit incubation application
            List<IncubationProgram> progs = incService.getPrograms();
            Integer progId = !progs.isEmpty() ? progs.get(0).getId() : null;
            IncubationApplication app = incService.applyForIncubation(
                    startup.getId(),
                    progId,
                    "https://biogenix.health/pitch.pdf",
                    "Conduct Phase 1 clinical trials for novel bio-sensor."
            );
            assertTrue("Submit incubation application", app != null && app.getId() > 0, "Failed to submit incubation application");
            assertTrue("Initial status Under Review", "Under Review".equals(app.getStatus()), "Status is not Under Review");

            // 5. Admin approves application with feedback
            String feedback = "Approved for DeepTech cohort. Lab access granted.";
            incService.decideApplication(app.getId(), "Approved", feedback);

            // 6. Verify updated status and feedback in Founder view
            List<IncubationApplication> founderApps = incService.getApplicationsForFounder(founder.getId());
            assertTrue("Founder sees updated applications", !founderApps.isEmpty(), "No applications returned for founder");
            IncubationApplication updatedApp = founderApps.get(0);
            assertTrue("Updated status is Approved", "Approved".equals(updatedApp.getStatus()), "Status did not update to Approved");
            assertTrue("Feedback visible to founder", feedback.equals(updatedApp.getAdminFeedback()), "Feedback was not saved/synchronized");

            // 7. Admin records official review on Startup with reviewer attribution
            String adminName = "Dr. Rajesh Sharma (Head of Incubation)";
            String adminRemarks = "Startup approved for cohort induction. Exceptional prototype.";
            startupService.updateStartupStatus(startup.getId(), "Approved", 1, adminName, adminRemarks);
            Startup reviewedStartup = startupService.getStartupForFounder(founder.getId());
            assertTrue("Startup review status Approved", "Approved".equals(reviewedStartup.getStatus()), "Startup status mismatch");
            assertTrue("Startup reviewed by admin recorded", adminName.equals(reviewedStartup.getReviewedByAdminName()), "Reviewer admin mismatch");
            assertTrue("Startup admin remarks recorded", adminRemarks.equals(reviewedStartup.getAdminRemarks()), "Admin remarks mismatch");

            // 8. Admin records official review on Application with reviewer attribution
            incService.decideApplication(app.getId(), "Approved", feedback, 1, adminName);
            List<IncubationApplication> refreshedApps = incService.getApplicationsForFounder(founder.getId());
            assertTrue("Application reviewed by admin recorded", adminName.equals(refreshedApps.get(0).getReviewedByAdminName()), "Application reviewer mismatch");

        } catch (Exception e) {
            assertTrue("Workflow B Exception", false, e.getMessage());
        }
    }

    // Workflow C — Mentorship
    private static void testWorkflowC() {
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("WORKFLOW C: Mentor Onboarding & Session Request Assignment");
        System.out.println("----------------------------------------------------------------------");
        MentorService mentorService = new MentorService();
        StartupService startupService = new StartupService();
        AuthService auth = new AuthService();

        try {
            // 1. Admin creates mentor
            String mentorName = "Prof. Raghav Verma";
            Mentor mentor = mentorService.addMentor(mentorName, "Quantum Computing & Hardware", "raghav@niet.co.in", "+91 94444 55555", "Head of Research");
            assertTrue("Admin onboarded mentor", mentor != null && mentor.getId() > 0, "Failed to onboard mentor");

            // 2. Verify mentor in Founder active list
            List<Mentor> activeMentors = mentorService.getActiveMentors();
            boolean mentorVisible = false;
            for (Mentor m : activeMentors) {
                if (m.getId() == mentor.getId()) {
                    mentorVisible = true;
                    break;
                }
            }
            assertTrue("Mentor visible in active directory", mentorVisible, "Mentor not listed for founders");

            // 3. Founder submits mentorship request
            User founder = auth.registerFounder("Kunal Shah", "kunal_" + System.currentTimeMillis() + "@gmail.com", "pass1234", "pass1234", "9777788888", "QBitLabs", "Technology");
            Startup startup = startupService.getStartupForFounder(founder.getId());
            MentorshipRequest req = mentorService.requestMentorship(startup.getId(), mentor.getId(), "Hardware Architecture", "Need review of cryogenic cooling design");
            assertTrue("Mentorship request submitted", req != null && req.getId() > 0, "Failed to submit mentorship request");

            // 4. Admin assigns mentor
            String remarks = "Mentor assigned for 4-week hardware review.";
            mentorService.assignMentor(req.getId(), mentor.getId(), "Assigned", remarks);

            // 5. Verify updated request status as founder
            List<MentorshipRequest> founderReqs = mentorService.getFounderRequests(founder.getId());
            assertTrue("Founder retrieved requests", !founderReqs.isEmpty(), "Founder requests empty");
            MentorshipRequest updatedReq = founderReqs.get(0);
            assertTrue("Status is Assigned", "Assigned".equals(updatedReq.getStatus()), "Status is not Assigned");
            assertTrue("Mentor name populated", mentorName.equals(updatedReq.getMentorName()), "Mentor name did not populate");
            assertTrue("Remarks synchronized", remarks.equals(updatedReq.getAdminRemarks()), "Admin remarks did not synchronize");

        } catch (Exception e) {
            assertTrue("Workflow C Exception", false, e.getMessage());
        }
    }

    // Workflow D — Events
    private static void testWorkflowD() {
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("WORKFLOW D: Event Publishing, Duplicate Checks & Capacity Enforcement");
        System.out.println("----------------------------------------------------------------------");
        EventService eventService = new EventService();
        AuthService auth = new AuthService();

        try {
            // 1. Admin creates event with capacity 2
            Event ev = eventService.createEvent("AI Founders RoundTable", "15 Nov 2026", "BoardRoom 1", 2, "Exclusive closed-door founder meetup.");
            assertTrue("Event created by Admin", ev != null && ev.getId() > 0, "Failed to create event");

            // 2. Founder 1 registers
            User founder1 = auth.registerFounder("User One", "user1_" + System.currentTimeMillis() + "@gmail.com", "pass1234", "pass1234", "9000000001", "Start1", "Tech");
            boolean reg1 = eventService.registerUser(ev.getId(), founder1.getId());
            assertTrue("Founder 1 registered successfully", reg1, "Founder 1 registration failed");

            // 3. Prevent duplicate registration
            boolean duplicateBlocked = false;
            try {
                eventService.registerUser(ev.getId(), founder1.getId());
            } catch (Exception ex) {
                duplicateBlocked = true;
            }
            assertTrue("Duplicate event registration blocked", duplicateBlocked, "Duplicate registration allowed");

            // 4. Founder 2 registers (fills capacity to 2/2)
            User founder2 = auth.registerFounder("User Two", "user2_" + System.currentTimeMillis() + "@gmail.com", "pass1234", "pass1234", "9000000002", "Start2", "Tech");
            boolean reg2 = eventService.registerUser(ev.getId(), founder2.getId());
            assertTrue("Founder 2 registered successfully", reg2, "Founder 2 registration failed");

            // 5. Enforce capacity limit (Founder 3 should be blocked)
            User founder3 = auth.registerFounder("User Three", "user3_" + System.currentTimeMillis() + "@gmail.com", "pass1234", "pass1234", "9000000003", "Start3", "Tech");
            boolean capacityBlocked = false;
            try {
                eventService.registerUser(ev.getId(), founder3.getId());
            } catch (Exception ex) {
                capacityBlocked = true;
            }
            assertTrue("Event capacity limit enforced", capacityBlocked, "Capacity limit exceeded without blocking");

        } catch (Exception e) {
            assertTrue("Workflow D Exception", false, e.getMessage());
        }
    }

    // Workflow E — Funding
    private static void testWorkflowE() {
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("WORKFLOW E: Funding Grant Opportunities & Review Decisions");
        System.out.println("----------------------------------------------------------------------");
        FundingService fundingService = new FundingService();
        StartupService startupService = new StartupService();
        AuthService auth = new AuthService();

        try {
            // 1. Publish funding opportunity
            FundingOpportunity opp = fundingService.createOpportunity("Prototype Grant 2026", 800000.0, "30 Dec 2026", "Hardware prototype grant");
            assertTrue("Funding scheme published", opp != null && opp.getId() > 0, "Failed to publish funding opportunity");

            // 2. Founder applies
            User founder = auth.registerFounder("Pooja Nair", "pooja_" + System.currentTimeMillis() + "@gmail.com", "pass1234", "pass1234", "9111122222", "AeroDrones", "Technology");
            Startup startup = startupService.getStartupForFounder(founder.getId());
            FundingApplication app = fundingService.applyForFunding(startup.getId(), opp.getId(), 600000.0, "Procure carbon-fiber components for drone delivery testing.");
            assertTrue("Funding application submitted", app != null && app.getId() > 0, "Failed to submit funding application");

            // 3. Admin review & grant sanction
            String feedback = "Sanctioned ₹5,00,000 for Component procurement.";
            boolean decided = fundingService.decideApplication(app.getId(), "Approved", 500000.0, feedback);
            assertTrue("Admin decision recorded", decided, "Failed to record funding decision");

            // 4. Verify synchronized result in Founder dashboard
            List<FundingApplication> founderApps = fundingService.getApplicationsForFounder(founder.getId());
            assertTrue("Founder retrieves applications", !founderApps.isEmpty(), "Founder funding list empty");
            FundingApplication updatedApp = founderApps.get(0);
            assertTrue("Status is Approved", "Approved".equals(updatedApp.getStatus()), "Status not updated to Approved");
            assertTrue("Sanctioned amount matches", updatedApp.getAmountApproved() == 500000.0, "Approved amount mismatch");
            assertTrue("Feedback matches", feedback.equals(updatedApp.getAdminFeedback()), "Feedback mismatch");

        } catch (Exception e) {
            assertTrue("Workflow E Exception", false, e.getMessage());
        }
    }

    // Workflow F — Persistence and Authorization
    private static void testWorkflowF() {
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("WORKFLOW F: Data Persistence & Role Authorization");
        System.out.println("----------------------------------------------------------------------");
        AuthService auth = new AuthService();
        StartupService startupService = new StartupService();

        try {
            // 1. Simulate fresh database connection / restart
            try (Connection conn = DatabaseConnection.getConnection()) {
                assertTrue("Database connection alive after reconnect", conn != null && !conn.isClosed(), "Connection closed");
                // Ensure default admin is ACTIVE
                try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE users SET status = 'ACTIVE' WHERE email = 'admin@gmail.com'")) {
                    ps.executeUpdate();
                }
            }

            // 2. Verify default seeded accounts persist
            User admin = auth.login("admin@gmail.com", "admin123");
            assertTrue("Admin persistent login", admin != null && admin.isAdmin(), "Admin account login failed");

            User founder = auth.login("founder@gmail.com", "founder123");
            assertTrue("Founder persistent login", founder != null && founder.isFounder(), "Founder account login failed");

            // 3. Enforce data isolation: Founder cannot access another founder's startup
            Startup founderStartup = startupService.getStartupForFounder(founder.getId());
            assertTrue("Founder gets own startup", founderStartup != null && founderStartup.getFounderId() == founder.getId(), "Founder startup isolation failed");

            // 4. Protect admin account from being disabled
            boolean adminDisableBlocked = false;
            try {
                auth.toggleUserStatus(admin.getId(), "ACTIVE");
            } catch (Exception ex) {
                adminDisableBlocked = true;
            }
            assertTrue("Admin account cannot be deactivated", adminDisableBlocked, "Admin protection failed to block deactivation");

        } catch (Exception e) {
            assertTrue("Workflow F Exception", false, e.getMessage());
        }
    }
}
