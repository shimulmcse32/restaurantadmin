package com.example.model;

import java.util.Date;

public class InvoiceInfoModel
{
	private String branchId, orderId, orderNo, supplierId,itemId, remarks, active,createdBy,detailsSql,referenceNo,totalVat,totalDiscount,vatOption;
	private Date deliveryDate,orderDate;
	private boolean bDetailsSql;
	
	public InvoiceInfoModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }
	
	public void setItemId(String itemId)
	{ this.itemId = itemId; }
	public String getItemId()
	{ return itemId; }

	public void setOrderId(String orderId)
	{ this.orderId = orderId; }
	public String getOrderId()
	{ return orderId; }

	public void setOrderNo(String orderNo)
	{ this.orderNo = orderNo; }
	public String getOrderNo()
	{ return orderNo; }

	public void setOrderDate(Date orderDate)
	{ this.orderDate = orderDate; }
	public Date getOrderDate()
	{ return orderDate; }
	
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
	
	public void setVatOption(String vatOption)
	{ this.vatOption = vatOption; }
	public String getVatOption()
	{ return vatOption; }
	
	public void setTotalVat(String totalVat)
	{ this.totalVat = totalVat; }
	public String getTotalVat()
	{ return totalVat; }
	
	public void setTotalDiscount(String totalDiscount)
	{ this.totalDiscount = totalDiscount; }
	public String getTotalDiscount()
	{ return totalDiscount; }
}
