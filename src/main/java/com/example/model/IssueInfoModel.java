package com.example.model;

import java.util.Date;

public class IssueInfoModel
{
	private String branchId, issueId, issueNo, branchFrom, branchTo, remarks, receivedBy, requisitionId, requisitionNo,
	active, createdBy, detailsSql, referenceNo, statusId, cancelBy, cancelReason, costMargin, statusMainId, approveBy;
	private Date issueDate;
	private Double totalAmount;
	private boolean bDetailsSql;

	public IssueInfoModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setIssueId(String issueId)
	{ this.issueId = issueId; }
	public String getIssueId()
	{ return issueId; }

	public void setIssueNo(String issueNo)
	{ this.issueNo = issueNo; }
	public String getIssueNo()
	{ return issueNo; }

	public void setIssueDate(Date issueDate)
	{ this.issueDate = issueDate; }
	public Date getIssueDate()
	{ return issueDate; }

	public void setBranchFrom(String branchFrom)
	{ this.branchFrom = branchFrom; }
	public String getBranchFrom()
	{ return branchFrom; }
	
	public void setBranchTo(String branchTo)
	{ this.branchTo = branchTo; }
	public String getBranchTo()
	{ return branchTo; }
	
	public void setReceivedBy(String receivedBy)
	{ this.receivedBy = receivedBy; }
	public String getReceivedBy()
	{ return receivedBy; }
	
	public void setRemarks(String remarks)
	{ this.remarks = remarks; }
	public String getRemarks()
	{ return remarks; }
	
	public void setTotalAmount(Double totalAmount)
	{ this.totalAmount = totalAmount; }
	public Double getTotalAmount()
	{ return totalAmount; }
	

	public void setActive(String active)
	{ this.active = active; }
	public String getActive()
	{ return active; }

	public void setDetailsChange(boolean bDetailsSql)
	{ this.bDetailsSql = bDetailsSql; }
	public boolean getDetailsChange()
	{ return bDetailsSql; }

	public void setDetailsSql(String detailsSql)
	{ this.detailsSql = detailsSql; }
	public String getDetailsSql()
	{ return detailsSql; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }

	public void setReferenceNo(String referenceNo)
	{ this.referenceNo = referenceNo; }
	public String getReferenceNo()
	{ return referenceNo; }
	
	public void setStatusId(String statusId)
	{ this.statusId = statusId; }
	public String getStatusId()
	{ return statusId; }
	
	public void setCostMargin(String costMargin)
	{ this.costMargin = costMargin; }
	public String getCostMargin()
	{ return costMargin; }

	public void setStatusMainId(String statusMainId)
	{ this.statusMainId = statusMainId; }
	public String getStatusMainId()
	{ return statusMainId; }

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
	
	public void setRequisitionId(String requisitionId)
	{ this.requisitionId = requisitionId; }
	public String getRequisitionId()
	{ return requisitionId; }
	
	public void setRequisitionNo(String requisitionNo)
	{ this.requisitionNo = requisitionNo; }
	public String getRequisitionNo()
	{ return requisitionNo; }
}
