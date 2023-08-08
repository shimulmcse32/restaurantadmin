package com.example.model;

import java.util.Date;

public class PhysicalStockModel
{
	private String branchId, physicalStockId, physicalStockNo, physicalStockmentFrom, itemId, remarks, 
	active, createdBy, detailsSql, referenceNo, statusId, approveBy, cancelBy, cancelReason;
	private Date physicalStockDate;
	private boolean bDetailsSql;

	public PhysicalStockModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setItemId(String itemId)
	{ this.itemId = itemId; }
	public String getItemId()
	{ return itemId; }

	public void setPhysicalStockId(String physicalStockId)
	{ this.physicalStockId = physicalStockId; }
	public String getPhysicalStockId()
	{ return physicalStockId; }

	public void setPhysicalStockNo(String physicalStockNo)
	{ this.physicalStockNo = physicalStockNo; }
	public String getPhysicalStockNo()
	{ return physicalStockNo; }

	public void setPhysicalStockDate(Date physicalStockDate)
	{ this.physicalStockDate = physicalStockDate; }
	public Date getPhysicalStockDate()
	{ return physicalStockDate; }

	public void setPhysicalStockmentFrom(String physicalStockmentFrom)
	{ this.physicalStockmentFrom = physicalStockmentFrom; }
	public String getPhysicalStockmentFrom()
	{ return physicalStockmentFrom; }

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
