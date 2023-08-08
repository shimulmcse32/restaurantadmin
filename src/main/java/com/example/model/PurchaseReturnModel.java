package com.example.model;

import java.util.Date;

public class PurchaseReturnModel
{
	private String branchId, returnId, returnNo, purchaseId, purchaseNo, supplierId, remarks, active, createdBy,
	detailsSql, referenceNo, returnType, statusId, approveBy, cancelBy, cancelReason;;

	private Date returnDate, purchaseDate;
	private boolean bDetailsSql;

	public PurchaseReturnModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setPurchaseId(String purchaseId)
	{ this.purchaseId = purchaseId; }
	public String getPurchaseId()
	{ return purchaseId; }

	public void setPurchaseNo(String purchaseNo)
	{ this.purchaseNo = purchaseNo; }
	public String getPurchaseNo()
	{ return purchaseNo; }
	
	public void setPurchaseDate(Date purchaseDate)
	{ this.purchaseDate = purchaseDate; }
	public Date getPurchaseDate()
	{ return purchaseDate; }

	public void setReturnDate(Date returnDate)
	{ this.returnDate = returnDate; }
	public Date getReturnDate()
	{ return returnDate; }

	public void setReturnId(String returnId)
	{ this.returnId = returnId; }
	public String getReturnId()
	{ return returnId; }

	public void setReturnNo(String returnNo)
	{ this.returnNo = returnNo; }
	public String getReturnNo()
	{ return returnNo; }

	public void setSupplierId(String supplierId)
	{ this.supplierId = supplierId; }
	public String getSupplierId()
	{ return supplierId; }

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

	public void setReturnType(String returnType)
	{ this.returnType = returnType; }
	public String getReturnType()
	{ return returnType; }
	
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
