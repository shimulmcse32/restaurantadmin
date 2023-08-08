package com.example.model;

import java.util.Date;

public class StockAdjustmentModel
{
	private String branchId, adjustId, adjustNo, adjustmentFrom, itemId, remarks, 
	active, createdBy, detailsSql, referenceNo, statusId, approveBy, cancelBy, cancelReason;
	private Date adjustDate;
	private boolean bDetailsSql;

	public StockAdjustmentModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setItemId(String itemId)
	{ this.itemId = itemId; }
	public String getItemId()
	{ return itemId; }

	public void setAdjustId(String adjustId)
	{ this.adjustId = adjustId; }
	public String getAdjustId()
	{ return adjustId; }

	public void setAdjustNo(String adjustNo)
	{ this.adjustNo = adjustNo; }
	public String getAdjustNo()
	{ return adjustNo; }

	public void setAdjustDate(Date adjustDate)
	{ this.adjustDate = adjustDate; }
	public Date getAdjustDate()
	{ return adjustDate; }

	public void setAdjustmentFrom(String adjustmentFrom)
	{ this.adjustmentFrom = adjustmentFrom; }
	public String getAdjustmentFrom()
	{ return adjustmentFrom; }

	public void setRemarks(String remarks)
	{ this.remarks = remarks; }
	public String getRemarks()
	{ return remarks; }

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
