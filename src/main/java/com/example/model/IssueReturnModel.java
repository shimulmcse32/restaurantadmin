package com.example.model;

import java.util.Date;

public class IssueReturnModel
{
	private String branchId, issueReturnId, issueReturnNo, issueId, branchFrom, branchTo, 
	active, createdBy, detailsSql, statusId, approveBy, cancelBy, cancelReason;
	private Date issueReturnDate;
	private Double totalAmount;
	private boolean detailsChange;

	public IssueReturnModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setIssueReturnId(String issueReturnId)
	{ this.issueReturnId = issueReturnId; }
	public String getIssueReturnId()
	{ return issueReturnId; }

	public void setIssueId(String issueId)
	{ this.issueId = issueId; }
	public String getIssueId()
	{ return issueId; }

	public void setIssueReturnNo(String issueReturnNo)
	{ this.issueReturnNo = issueReturnNo; }
	public String getIssueReturnNo()
	{ return issueReturnNo; }

	public void setIssueReturnDate(Date issueReturnDate)
	{ this.issueReturnDate = issueReturnDate; }
	public Date getIssueReturnDate()
	{ return issueReturnDate; }

	public void setBranchFrom(String branchFrom)
	{ this.branchFrom = branchFrom; }
	public String getBranchFrom()
	{ return branchFrom; }

	public void setBranchTo(String branchTo)
	{ this.branchTo = branchTo; }
	public String getBranchTo()
	{ return branchTo; }

	public void setTotalAmount(Double totalAmount)
	{ this.totalAmount = totalAmount; }
	public Double getTotalAmount()
	{ return totalAmount; }

	public void setActive(String active)
	{ this.active = active; }
	public String getActive()
	{ return active; }

	public void setDetailsChange(boolean detailsChange)
	{ this.detailsChange = detailsChange; }
	public boolean getDetailsChange()
	{ return detailsChange; }

	public void setDetailsSql(String detailsSql)
	{ this.detailsSql = detailsSql; }
	public String getDetailsSql()
	{ return detailsSql; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }

	public void setStatusId(String statusId)
	{ this.statusId = statusId; }
	public String getStatusId()
	{ return statusId; }

	public void setApproveBy(String approveBy)
	{ this.approveBy = approveBy; }
	public String getApproveBy()
	{ return approveBy; }

	public void setCancelBy(String cancelBy)
	{ this.cancelBy = cancelBy; }
	public String getCancelBy()
	{ return cancelBy; }

	public void setCancelReason(String cancelReason)
	{ this.cancelReason = cancelReason; }
	public String getCancelReason()
	{ return cancelReason; }
}
