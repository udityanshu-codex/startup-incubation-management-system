package model;

import java.sql.Timestamp;

public class FundingApplication {
    private int id;
    private int startupId;
    private String startupName;
    private Integer opportunityId;
    private String opportunityTitle;
    private double amountRequested;
    private double amountApproved;
    private String proposal;
    private String status; // "Under Review", "Approved", "Rejected"
    private String adminFeedback;
    private Timestamp appliedDate;
    private Timestamp decidedDate;

    public FundingApplication() {}

    public FundingApplication(int id, int startupId, String startupName, Integer opportunityId, String opportunityTitle,
                              double amountRequested, double amountApproved, String proposal, String status,
                              String adminFeedback, Timestamp appliedDate, Timestamp decidedDate) {
        this.id = id;
        this.startupId = startupId;
        this.startupName = startupName;
        this.opportunityId = opportunityId;
        this.opportunityTitle = opportunityTitle;
        this.amountRequested = amountRequested;
        this.amountApproved = amountApproved;
        this.proposal = proposal;
        this.status = status;
        this.adminFeedback = adminFeedback;
        this.appliedDate = appliedDate;
        this.decidedDate = decidedDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStartupId() { return startupId; }
    public void setStartupId(int startupId) { this.startupId = startupId; }

    public String getStartupName() { return startupName; }
    public void setStartupName(String startupName) { this.startupName = startupName; }

    public Integer getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Integer opportunityId) { this.opportunityId = opportunityId; }

    public String getOpportunityTitle() { return opportunityTitle; }
    public void setOpportunityTitle(String opportunityTitle) { this.opportunityTitle = opportunityTitle; }

    public double getAmountRequested() { return amountRequested; }
    public void setAmountRequested(double amountRequested) { this.amountRequested = amountRequested; }

    public double getAmountApproved() { return amountApproved; }
    public void setAmountApproved(double amountApproved) { this.amountApproved = amountApproved; }

    public String getProposal() { return proposal; }
    public void setProposal(String proposal) { this.proposal = proposal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminFeedback() { return adminFeedback; }
    public void setAdminFeedback(String adminFeedback) { this.adminFeedback = adminFeedback; }

    public Timestamp getAppliedDate() { return appliedDate; }
    public void setAppliedDate(Timestamp appliedDate) { this.appliedDate = appliedDate; }

    public Timestamp getDecidedDate() { return decidedDate; }
    public void setDecidedDate(Timestamp decidedDate) { this.decidedDate = decidedDate; }
}
