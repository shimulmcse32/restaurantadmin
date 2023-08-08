package com.example.model;

public class SupplierInfoModel
{
	private String branchId, supplierId, supplierName, address, phone, fax,
	email, license, vatRegNo, createdBy, supplierCode, creditLimit, detailsSql;
	
	private double discount;

	public SupplierInfoModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setSupplierId(String supplierId)
	{ this.supplierId = supplierId; }
	public String getSupplierId()
	{ return supplierId; }

	public void setSupplierName(String supplierName)
	{ this.supplierName = supplierName; }
	public String getSupplierName()
	{ return supplierName; }

	public void setAddress(String address)
	{ this.address = address; }
	public String getAddress()
	{ return address; }

	public void setPhone(String phone)
	{ this.phone = phone; }
	public String getPhone()
	{ return phone; }

	public void setFax(String fax)
	{ this.fax = fax; }
	public String getFax()
	{ return fax; }

	public void setEmail(String email)
	{ this.email = email; }
	public String getEmail()
	{ return email; }

	public void setLicense(String license)
	{ this.license = license; }
	public String getLicense()
	{ return license; }

	public void setVatRegNo(String vatRegNo)
	{ this.vatRegNo = vatRegNo; }
	public String getVatRegNo()
	{ return vatRegNo; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }

	public void setSupplierCode(String supplierCode)
	{ this.supplierCode = supplierCode; }
	public String getSupplierCode()
	{ return supplierCode; }

	public void setCreditLimit(String creditLimit)
	{ this.creditLimit = creditLimit; }
	public String getCreditLimit()
	{ return creditLimit; }

	public void setDiscount(double discount)
	{ this.discount = discount; }
	public double getDiscount()
	{ return discount; }

	public void setDetailsSql(String detailsSql)
	{ this.detailsSql = detailsSql; }
	public String getDetailsSql()
	{ return detailsSql; }
}
