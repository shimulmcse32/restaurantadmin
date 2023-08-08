package com.example.model;

public class BranchInfoModel
{
	private String companyId, branchId, branchName, address, phone,
	fax, email, license, branchType, createdBy;

	public BranchInfoModel()
	{}

	public void setCompanyId(String companyId)
	{ this.companyId = companyId; }
	public String getCompanyId()
	{ return companyId; }
	
	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setBranchName(String branchName)
	{ this.branchName = branchName; }
	public String getBranchName()
	{ return branchName; }

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

	public void setBranchType(String branchType)
	{ this.branchType = branchType; }
	public String getBranchType()
	{ return branchType; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }
}
