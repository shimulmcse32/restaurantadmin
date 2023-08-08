package com.example.model;

import java.util.Date;

public class PurchaseInfoModel
{
	private String branchId, orderId, orderNo, purchaseId, purchaseNo, supplierId,itemId, remarks, active, createdBy,
	detailsSql, referenceNo, totalDiscount, paymentType, statusId, approveBy, cancelBy, cancelReason;;

	private Date deliveryDate,purchaseDate;
	private boolean bDetailsSql;

	public PurchaseInfoModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setItemId(String itemId)
	{ this.itemId = itemId; }
	public String getItemId()
	{ return itemId; }

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

	public void setOrderId(String orderId)
	{ this.orderId = orderId; }
	public String getOrderId()
	{ return orderId; }

	public void setOrderNo(String orderNo)
	{ this.orderNo = orderNo; }
	public String getOrderNo()
	{ return orderNo; }

	public void setSupplierId(String supplierId)
	{ this.supplierId = supplierId; }
	public String getSupplierId()
	{ return supplierId; }

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

	public void setTotalDiscount(String totalDiscount)
	{ this.totalDiscount = totalDiscount; }
	public String getTotalDiscount()
	{ return totalDiscount; }

	public void setPaymentType(String paymentType)
	{ this.paymentType = paymentType; }
	public String getPaymentType()
	{ return paymentType; }
	
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
