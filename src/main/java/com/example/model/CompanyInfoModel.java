package com.example.model;

public class CompanyInfoModel
{
	private String companyId, branchId, companyName, address, phone,
	fax, email, license, vatRegNo, webSite, logo, surName, addOnlineMenuName,
	addAddress, addPhone, addFax, addEmail, addLicenseNo, addVatRegNo, addWebsite,
	addURL, addMinOrderAmount, createdBy;
	private int emailNotification, addEmailNotification, addStockZeroSale;
	private byte[] logoBytes;

	public CompanyInfoModel()
	{}

	public void setCompanyId(String companyId)
	{ this.companyId = companyId; }
	public String getCompanyId()
	{ return companyId; }

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setComLogo(byte[] logoBytes)
	{ this.logoBytes = logoBytes; }
	public byte[] getComLogo()
	{ return logoBytes; }

	public void setCompanyName(String companyName)
	{ this.companyName = companyName; }
	public String getCompanyName()
	{ return companyName; }

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

	public void setSurName(String surName)
	{ this.surName = surName; }
	public String getSurName()
	{ return surName; }

	public void setWebsite(String webSite)
	{ this.webSite = webSite; }
	public String getWebsite()
	{ return webSite; }

	public void setLogo(String logo)
	{ this.logo = logo; }
	public String getLogo()
	{ return logo; }

	public void setAddOnlineMenuName(String addOnlineMenuName)
	{ this.addOnlineMenuName = addOnlineMenuName; }
	public String getAddOnlineMenuName()
	{ return addOnlineMenuName; }

	public void setAddAddress(String addAddress)
	{ this.addAddress = addAddress; }
	public String getAddAddress()
	{ return addAddress; }

	public void setAddPhone(String addPhone)
	{ this.addPhone = addPhone; }
	public String getAddPhone()
	{ return addPhone; }

	public void setAddFax(String addFax)
	{ this.addFax = addFax; }
	public String getAddFax()
	{ return addFax; }

	public void setAddEmail(String addEmail)
	{ this.addEmail = addEmail; }
	public String getAddEmail()
	{ return addEmail; }

	public void setAddLicenseNo(String addLicenseNo)
	{ this.addLicenseNo = addLicenseNo; }
	public String getAddLicenseNo()
	{ return addLicenseNo; }

	public void setAddVatRegNo(String addVatRegNo)
	{ this.addVatRegNo = addVatRegNo; }
	public String getAddVatRegNo()
	{ return addVatRegNo; }

	public void setAddWebsite(String addWebsite)
	{ this.addWebsite = addWebsite; }
	public String getAddWebsite()
	{ return addWebsite; }

	public void setAddURL(String addURL)
	{ this.addURL = addURL; }
	public String getAddURL()
	{ return addURL; }

	public void setAddMinOrderAmount(String addMinOrderAmount)
	{ this.addMinOrderAmount = addMinOrderAmount; }
	public String getAddMinOrderAmount()
	{ return addMinOrderAmount; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }

	public void setEmailNotification(int emailNotification)
	{ this.emailNotification = emailNotification; }
	public int getEmailNotification()
	{ return emailNotification; }
	
	public void setAddEmailNotification(int addEmailNotification)
	{ this.addEmailNotification = addEmailNotification; }
	public int getAddEmailNotification()
	{ return addEmailNotification; }
	
	public void setaAddStockZeroSale(int addStockZeroSale)
	{ this.addStockZeroSale = addStockZeroSale; }
	public int getAddStockZeroSale()
	{ return addStockZeroSale; }
}
