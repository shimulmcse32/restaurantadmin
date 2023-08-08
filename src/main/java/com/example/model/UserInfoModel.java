package com.example.model;

import java.util.Date;

public class UserInfoModel
{
	private String companyId, companyName, roleId, branchId, fullName,
	userId, userName, passWord, profilePic, mobileNumber, emailId,
	userType, employeeId, posAccess, appAccess, createdBy;
	private boolean isEmailNotification;
	private Date expiryDate;

	public UserInfoModel()
	{}

	public void setCompanyId(String companyId)
	{ this.companyId = companyId; }
	public String getCompanyId()
	{ return companyId; }

	public void setRoleId(String roleId)
	{ this.roleId = roleId; }
	public String getRoleId()
	{ return roleId; }

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setFullName(String fullName)
	{ this.fullName = fullName; }
	public String getFullName()
	{ return fullName; }

	public void setCompanyName(String companyName)
	{ this.companyName = companyName; }
	public String getCompanyName()
	{ return companyName; }

	public void setUserId(String userId)
	{ this.userId = userId; }
	public String getUserId()
	{ return userId; }

	public void setUserName(String userName)
	{ this.userName = userName; }
	public String getUserName()
	{ return userName; }

	public void setPassWord(String passWord)
	{ this.passWord = passWord; }
	public String getPassWord()
	{ return passWord; }

	public void setExpiryDate(Date expiryDate)
	{ this.expiryDate = expiryDate; }
	public Date getExpiryDate()
	{ return expiryDate; }

	public void setMobileNumber(String mobileNumber)
	{ this.mobileNumber = mobileNumber; }
	public String getMobileNumber()
	{ return mobileNumber; }

	public void setEmailId(String emailId)
	{ this.emailId = emailId; }
	public String getEmailId()
	{ return emailId; }

	public void setUserType(String userType)
	{ this.userType = userType; }
	public String getUserType()
	{ return userType; }

	public void setUserPicture(String profilePic)
	{ this.profilePic = profilePic; }
	public String getUserPicture()
	{ return profilePic; }

	public void setEmployeeId(String employeeId)
	{ this.employeeId = employeeId; }
	public String getEmployeeId()
	{ return employeeId; }

	public void setPosAccess(String posAccess)
	{ this.posAccess = posAccess; }
	public String getPosAccess()
	{ return posAccess; }

	public void setAppAccess(String appAccess)
	{ this.appAccess = appAccess; }
	public String getAppAccess()
	{ return appAccess; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }
	
	public void setIsEmailNotification(boolean isEmailNotification)
	{ this.isEmailNotification = isEmailNotification; }
	public boolean getIsEmailNotification()
	{ return isEmailNotification; }
}
