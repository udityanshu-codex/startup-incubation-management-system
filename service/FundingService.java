package service;

import dao.FundingDAO;
import model.FundingApplication;
import model.FundingOpportunity;

import java.sql.SQLException;
import java.util.List;

public class FundingService {

    private final FundingDAO fundingDAO = new FundingDAO();

    public List<FundingOpportunity> getFundingOpportunities() throws SQLException {
        return fundingDAO.getAllOpportunities();
    }

    public FundingOpportunity createOpportunity(String title, double maxAmount, String deadline, String desc) throws Exception {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title is required.");
        if (maxAmount <= 0) throw new IllegalArgumentException("Maximum grant amount must be positive.");

        FundingOpportunity opp = new FundingOpportunity(title.trim(), maxAmount, deadline != null ? deadline.trim() : "", desc != null ? desc.trim() : "");
        return fundingDAO.createOpportunity(opp);
    }

    public FundingApplication applyForFunding(int startupId, Integer oppId, double amount, String proposal) throws Exception {
        if (startupId <= 0) throw new IllegalArgumentException("Valid startup ID required.");
        if (amount <= 0) throw new IllegalArgumentException("Requested funding amount must be greater than zero.");
        if (proposal == null || proposal.trim().isEmpty()) throw new IllegalArgumentException("Pitch proposal & fund utilization plan required.");

        FundingApplication app = new FundingApplication();
        app.setStartupId(startupId);
        app.setOpportunityId(oppId);
        app.setAmountRequested(amount);
        app.setProposal(proposal.trim());
        app.setStatus("Under Review");

        return fundingDAO.createApplication(app);
    }

    public List<FundingApplication> getApplicationsForFounder(int founderId) throws SQLException {
        return fundingDAO.getApplicationsByFounderId(founderId);
    }

    public List<FundingApplication> getAllApplications() throws SQLException {
        return fundingDAO.getAllApplications();
    }

    public boolean decideApplication(int applicationId, String status, double amountApproved, String feedback) throws SQLException {
        return fundingDAO.updateApplicationDecision(applicationId, status, amountApproved, feedback != null ? feedback.trim() : "");
    }

    public double getTotalFundingApproved() throws SQLException {
        return fundingDAO.getTotalFundingApproved();
    }
}
