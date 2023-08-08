package com.example.model;

import java.util.Date;

public class PurchaseReceiptModel
{
	private String branchId, orderType, supCusId, orderId, invoiceId, invoiceType, orderNo, invoiceNo, revision,
	referenceNo, referenceDate, remarks, termsCondition, statusId, approveBy, cancelBy, cancelReason, detailsSql,
	createdBy, attachId, attachSql, plateNumber, narration;
	private double discount, vatAmount;
	private Date orderDate, invoiceDate, deliveryDate, receiptDate;
	private boolean orderChangeDetails, attachChange;

	private String receiptId, receiptNo, receiptFor, receiptType, payType, chequeNo, chequeDate, ledgerId, invType;

	public PurchaseReceiptModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setOrderType(String orderType)
	{ this.orderType = orderType; }
	public String getOrderType()
	{ return orderType; }

	public void setInvoiceType(String invoiceType)
	{ this.invoiceType = invoiceType; }
	public String getInvoiceType()
	{ return invoiceType; }

	public void setSupCusId(String supCusId)
	{ this.supCusId = supCusId; }
	public String getSupCusId()
	{ return supCusId; }

	public void setOrderDate(Date orderDate)
	{ this.orderDate = orderDate; }
	public Date getOrderDate()
	{ return orderDate; }

	public void setInvoiceDate(Date invoiceDate)
	{ this.invoiceDate = invoiceDate; }
	public Date getInvoiceDate()
	{ return invoiceDate; }

	public void setDeliveryDate(Date deliveryDate)
	{ this.deliveryDate = deliveryDate; }
	public Date getDeliveryDate()
	{ return deliveryDate; }

	public void setOrderId(String orderId)
	{ this.orderId = orderId; }
	public String getOrderId()
	{ return orderId; }

	public void setInvoiceId(String invoiceId)
	{ this.invoiceId = invoiceId; }
	public String getInvoiceId()
	{ return invoiceId; }

	public void setReceiptId(String receiptId)
	{ this.receiptId = receiptId; }
	public String getReceiptId()
	{ return receiptId; }

	public void setOrderNo(String orderNo)
	{ this.orderNo = orderNo; }
	public String getOrderNo()
	{ return orderNo; }

	public void setReceiptNo(String receiptNo)
	{ this.receiptNo = receiptNo; }
	public String getReceiptNo()
	{ return receiptNo; }

	public void setNarration(String narration)
	{ this.narration = narration; }
	public String getNarration()
	{ return narration; }

	public void setPlateNumber(String plateNumber)
	{ this.plateNumber = plateNumber; }
	public String getPlateNumber()
	{ return plateNumber; }

	public void setReceiptFor(String receiptFor)
	{ this.receiptFor = receiptFor; }
	public String getReceiptFor()
	{ return receiptFor; }

	public void setReceiptType(String receiptType)
	{ this.receiptType = receiptType; }
	public String getReceiptType()
	{ return receiptType; }

	public void setPayType(String payType)
	{ this.payType = payType; }
	public String getPayType()
	{ return payType; }

	public void setChequeNo(String chequeNo)
	{ this.chequeNo = chequeNo; }
	public String getChequeNo()
	{ return chequeNo; }

	public void setChequeDate(String chequeDate)
	{ this.chequeDate = chequeDate; }
	public String getChequeDate()
	{ return chequeDate; }

	public void setLedgerId(String ledgerId)
	{ this.ledgerId = ledgerId; }
	public String getLedgerId()
	{ return ledgerId; }

	public void setReceiptDate(Date receiptDate)
	{ this.receiptDate = receiptDate; }
	public Date getReceiptDate()
	{ return receiptDate; }

	public void setInvoiceNo(String invoiceNo)
	{ this.invoiceNo = invoiceNo; }
	public String getInvoiceNo()
	{ return invoiceNo; }

	public void setReferenceNo(String referenceNo)
	{ this.referenceNo = referenceNo; }
	public String getReferenceNo()
	{ return referenceNo; }

	public void setReferenceDate(String referenceDate)
	{ this.referenceDate = referenceDate; }
	public String getReferenceDate()
	{ return referenceDate; }

	public void setRemarks(String remarks)
	{ this.remarks = remarks; }
	public String getRemarks()
	{ return remarks; }

	public void setRevision(String revision)
	{ this.revision = revision; }
	public String getRevision()
	{ return revision; }

	public void setTermsCons(String termsCondition)
	{ this.termsCondition = termsCondition; }
	public String getTermsCons()
	{ return termsCondition; }

	public void setDiscount(double discount)
	{ this.discount = discount; }
	public Double getDiscount()
	{ return discount; }

	public void setVAT(double vatAmount)
	{ this.vatAmount = vatAmount; }
	public Double getVAT()
	{ return vatAmount; }

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

	public void setDetailsSql(String detailsSql)
	{ this.detailsSql = detailsSql; }
	public String getDetailsSql()
	{ return detailsSql; }

	public void setOrderChangeDetails(boolean orderChangeDetails)
	{ this.orderChangeDetails = orderChangeDetails; }
	public boolean getOrderChangeDetails()
	{ return orderChangeDetails; }

	public void setInvType(String invType)
	{ this.invType = invType; }
	public String getInvType()
	{ return invType; }

	public void setAttachSql(String attachSql)
	{ this.attachSql = attachSql; }
	public String getAttachSql()
	{ return attachSql; }

	public void setAttachId(String attachId)
	{ this.attachId = attachId; }
	public String getAttachId()
	{ return attachId; }

	public void setAttachChange(boolean attachChange)
	{ this.attachChange = attachChange; }
	public boolean getAttachChange()
	{ return attachChange; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }
}
