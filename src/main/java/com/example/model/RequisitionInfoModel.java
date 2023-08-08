package com.example.model;

import java.util.Date;

public class RequisitionInfoModel
{
	private String branchId, requisitionId, requisitionNo, reqBranchId, itemId, remarks, 
	active, createdBy, detailsSql, referenceNo, statusId, approveBy, cancelBy, cancelReason;
	private Date deliveryDate,requisitionDate;
	private boolean bDetailsSql;

	public RequisitionInfoModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setItemId(String itemId)
	{ this.itemId = itemId; }
	public String getItemId()
	{ return itemId; }

	public void setRequisitionId(String requisitionId)
	{ this.requisitionId = requisitionId; }
	public String getRequisitionId()
	{ return requisitionId; }

	public void setRequisitionNo(String requisitionNo)
	{ this.requisitionNo = requisitionNo; }
	public String getRequisitionNo()
	{ return requisitionNo; }

	public void setRequisitionDate(Date requisitionDate)
	{ this.requisitionDate = requisitionDate; }
	public Date getRequisitionDate()
	{ return requisitionDate; }

	public void setReqBranchId(String reqBranchId)
	{ this.reqBranchId = reqBranchId; }
	public String getReqBranchId()
	{ return reqBranchId; }

	public void setDeliveryDate(Date deliveryDate)
	{ this.deliveryDate = deliveryDate; }
	public Date getDeliveryDate()
	{ return deliveryDate; }

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
