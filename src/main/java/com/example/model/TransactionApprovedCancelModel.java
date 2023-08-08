package com.example.model;

public class TransactionApprovedCancelModel
{
	private String  transId, statusId, modifyBy, approveBy, cancelBy, cancelReason;
	private boolean bDetailsSql;

	public TransactionApprovedCancelModel()
	{}
	
	public void setTransactionId(String transId)
	{ this.transId = transId; }
	public String getTransactionId()
	{ return transId; }
	
	public void setModifyBy(String modifyBy)
	{ this.modifyBy = modifyBy; }
	public String getModifyBy()
	{ return modifyBy; }

	public void setDetailsChange(boolean bDetailsSql)
	{ this.bDetailsSql = bDetailsSql; }
	public boolean getDetailsChange()
	{ return bDetailsSql; }

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
