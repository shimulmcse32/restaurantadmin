package com.common.share;

import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;

public class SessionBean 
{
	private String userId, userType, password, profilePic;
	private String userName;
	private String fullName;
	private boolean isAdmin, isSuperAdmin;
	private String employeeId;

	private String companyId;
	private String companyName;
	private String companyLogo;
	private String companyAddress;
	private String phonefaxemail;
	private String companyEmail;
	private String vatRegNo;
	private String surName;

	private String branchId;
	private String branchName;
	private boolean isCentralBranch;
	private String branchLogo;
	private String branchAddress;
	private String branchPhoneFaxemail;
	private String branchType;

	public String war = "";
	private String moduleId;

	public String employeePhoto, imagePathDoc, imagePathAtt, imagePathAcc, imagePathAss,
	imagePathUser, imagePathTmp, imagePathLogo, itemPhoto, commonFiles, emailPath, acc_db;

	public SessionBean()
	{}

	//Company Informations
	public void setCompanyId(String companyId)
	{ this.companyId = companyId; }
	public String getCompanyId()
	{ return companyId; }

	public void setCompanyName(String companyname)
	{ this.companyName = companyname; }
	public String getCompanyName()
	{ return companyName; }

	public void setCompanyLogo(String companyLogo)
	{ this.companyLogo = companyLogo; }
	public String getCompanyLogo()
	{ return companyLogo; }

	public void setCompanyAddress(String companyAddress)
	{ this.companyAddress = companyAddress; }
	public String getCompanyAddress()
	{ return companyAddress; }

	public void setCompanyContact(String phonefaxemail)
	{ this.phonefaxemail = phonefaxemail; }
	public String getCompanyContact()
	{ return phonefaxemail; }

	public void setCompanyEmail(String companyEmail)
	{ this.companyEmail = companyEmail; }
	public String getCompanyEmail()
	{ return companyEmail; }

	public void setVatRegNo(String vatRegNo)
	{ this.vatRegNo = vatRegNo; }
	public String getVatRegNo()
	{ return vatRegNo; }

	//branch Informations
	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setBranchName(String branchname)
	{ this.branchName = branchname; }
	public String getBranchName()
	{ return branchName; }

	public void setBranchType(String branchType)
	{ this.branchType = branchType; }
	public String getBranchType()
	{ return branchType; }

	public void setSurName(String surName)
	{ this.surName = surName; }
	public String getSurName()
	{ return surName; }

	public void setCentralBranch(boolean branchType)
	{ this.isCentralBranch = branchType; }
	public boolean isCentralbranch()
	{ return isCentralBranch; }

	public void setBranchLogo(String branchLogo)
	{ this.branchLogo = branchLogo; }
	public String getBranchLogo()
	{ return branchLogo; }

	public void setBranchAddress(String branchAddress)
	{ this.branchAddress = branchAddress; }
	public String getBranchAddress()
	{ return branchAddress; }

	public void setBranchContact(String branchPhoneFaxemail)
	{ this.branchPhoneFaxemail = branchPhoneFaxemail; }
	public String getBranchContact()
	{ return branchPhoneFaxemail; }

	public void setModuleId(String moduleId)
	{ this.moduleId = moduleId; }
	public String getModuleId()
	{ return moduleId; }

	public void setUserId(String uid)
	{ userId = uid; }
	public String getUserId()
	{ return userId; }

	public void setPassword(String password)
	{ this.password = password; }
	public String getPassword()
	{ return password; }

	public void setIsAdmin(boolean iAdmin)
	{ isAdmin = iAdmin; }
	public boolean getIsAdmin()
	{ return isAdmin; }

	public void setIsSuperAdmin(boolean iSuperAdmin)
	{ isSuperAdmin = iSuperAdmin; }
	public boolean getIsSuperAdmin()
	{ return isSuperAdmin; }

	public void setEmployeeId(String empId)
	{ employeeId = empId; }
	public String getEmployeeId()
	{ return employeeId; }

	public void setUserName(String uname)
	{ userName = uname; }
	public String getUserName()
	{ return userName; }

	public void setFullName(String fName)
	{ fullName = fName; }
	public String getFullName()
	{ return fullName; }

	/*public void setUserIp(String userIp)
	{ this.userIp = userIp; }*/
	public String getUserIp()
	{
		WebBrowser webBrowser = Page.getCurrent().getWebBrowser();
		String ipAddress = webBrowser.getAddress();
		return ipAddress;
	}

	public String getDeveloper()
	{
		String developer = "Tonmoy";
		return developer;
	}

	public void setUserType(String userType)
	{ this.userType = userType; }
	public String getUserType()
	{ return userType; }

	public void setUserPicture(String profilePic)
	{ this.profilePic = profilePic; }
	public String getUserPicture()
	{ return profilePic; }
}