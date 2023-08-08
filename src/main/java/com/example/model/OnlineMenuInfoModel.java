package com.example.model;

public class OnlineMenuInfoModel
{
	private String onlineMenuId, branchId, onlineMenuName, address, phone,
	fax, email, license, vatRegNo, webSite, logo, surName, createdBy, url;
	private int emailNotification, stockZeroSale;
	private double miniOrderAmt;
	private byte[] logoBytes;

	public OnlineMenuInfoModel()
	{}

	public void setOnlineMenuId(String onlineMenuId)
	{ this.onlineMenuId = onlineMenuId; }
	public String getOnlineMenuId()
	{ return onlineMenuId; }

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setOnlineMenuLogo(byte[] logoBytes)
	{ this.logoBytes = logoBytes; }
	public byte[] getOnlineMenuLogo()
	{ return logoBytes; }

	public void setOnlineMenuName(String onlineMenuName)
	{ this.onlineMenuName = onlineMenuName; }
	public String getOnlineMenuName()
	{ return onlineMenuName; }

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

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }
	
	public void setEmailNotification(int emailNotification)
	{ this.emailNotification = emailNotification; }
	public int getEmailNotification()
	{ return emailNotification; }
	
	public void setURL(String url)
	{ this.url = url; }
	public String getURL()
	{ return url; }
	
	public void setMiniOrderAmt(Double miniOrderAmt)
	{ this.miniOrderAmt = miniOrderAmt; }
	public Double getMiniOrderAmt()
	{ return miniOrderAmt; }
	
	public void setStockZeroSale(int stockZeroSale)
	{ this.stockZeroSale = stockZeroSale; }
	public int getStockZeroSale()
	{ return stockZeroSale; }
	
}
