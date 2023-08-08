package com.example.model;

import java.util.Date;

public class EmployeeInfoModel
{
	//Officials Informations
	private String branchId, employeeId, employeeCode, employeeName, gender, maritialStatus,
	religion, contactNo, emailId, employeeTypeId, nationality, visaTypeId, employeeStatusId,
	deptId, desigId, employeeType, employeePhoto, salaryPayType, createdBy, eduExperinceSql,
	empTypeDate, visaTypeDate, empStatusDate;
	private Date dateOfBirth, dateOfInterview, dateOfJoin, dateOfLeasePay;
	private int otEnable, confidential;

	//Legal Informations
	private String contractId, cprNo, passportNo, residentPermit, drivingLicense, insurance;
	private Date contStart, contEnd, cprIssue, cprExpiry, passIssue, passExpiry, residentIssue,
	residentExpiry, drivingIssue, drivingExpiry, insuranceIssue, insuranceExpiry;

	//Personal Informations
	private String fatherName, motherName, spouseName, bahrainAddress, countryAddress,
	bloodGroup, nomineeSql;

	//Payroll Informations
	private double basicSalary, indemPercent, intaxPercent, gosiPercentEmp, gosiPercentCom;
	private String bankId, accountNo, iBanNo, salaryId, allowDeductionSql;

	//Leave Information
	private String leaveList, leaveBalanceSql, leaveBalanceId, attachId;

	//Dependents Information
	private String dependentsSql, attachSql;

	private String nomineeId, eduExpId, dependentsId;

	//Action on changes
	private boolean salaryChange, allowDedChange, nomineeChange, leaveChange,
	eduExpChange, dependChange, attachChange;

	public EmployeeInfoModel()
	{}

	//Officials Informations
	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setEmployeeId(String employeeId)
	{ this.employeeId = employeeId; }
	public String getEmployeeId()
	{ return employeeId; }

	public void setEmployeeCode(String employeeCode)
	{ this.employeeCode = employeeCode; }
	public String getEmployeeCode()
	{ return employeeCode; }

	public void setEmployeeName(String employeeName)
	{ this.employeeName = employeeName; }
	public String getEmployeeName()
	{ return employeeName; }

	public void setGender(String gender)
	{ this.gender = gender; }
	public String getGender()
	{ return gender; }

	public void setMaritialStatus(String maritialStatus)
	{ this.maritialStatus = maritialStatus; }
	public String getMaritialStatus()
	{ return maritialStatus; }

	public void setReligion(String religion)
	{ this.religion = religion; }
	public String getReligion()
	{ return religion; }

	public void setContactNo(String contactNo)
	{ this.contactNo = contactNo; }
	public String getContactNo()
	{ return contactNo; }

	public void setEmailId(String emailId)
	{ this.emailId = emailId; }
	public String getEmailId()
	{ return emailId; }

	public void setEmployeeTypeId(String employeeTypeId)
	{ this.employeeTypeId = employeeTypeId; }
	public String getEmployeeTypeId()
	{ return employeeTypeId; }

	public void setNationality(String nationality)
	{ this.nationality = nationality; }
	public String getNationality()
	{ return nationality; }

	public void setVisaTypeId(String visaTypeId)
	{ this.visaTypeId = visaTypeId; }
	public String getVisaTypeId()
	{ return visaTypeId; }

	public void setDeptId(String deptId)
	{ this.deptId = deptId; }
	public String getDeptId()
	{ return deptId; }

	public void setDesigId(String desigId)
	{ this.desigId = desigId; }
	public String getDesigId()
	{ return desigId; }

	public void setEmployeeStatusId(String employeeStatusId)
	{ this.employeeStatusId = employeeStatusId; }
	public String getEmployeeStatusId()
	{ return employeeStatusId; }

	public void setEmpTypeDate(String empTypeDate)
	{ this.empTypeDate = empTypeDate; }
	public String getEmpTypeDate()
	{ return empTypeDate; }

	public void setVisaTypeDate(String visaTypeDate)
	{ this.visaTypeDate = visaTypeDate; }
	public String getVisaTypeDate()
	{ return visaTypeDate; }

	public void setEmpStatusDate(String empStatusDate)
	{ this.empStatusDate = empStatusDate; }
	public String getEmpStatusDate()
	{ return empStatusDate; }

	public void setEmpType(String employeeType)
	{ this.employeeType = employeeType; }
	public String getEmpType()
	{ return employeeType; }

	public void setOtEnable(int otEnable)
	{ this.otEnable = otEnable; }
	public int getOtEnable()
	{ return otEnable; }

	public void setConfidential(int confidential)
	{ this.confidential = confidential; }
	public int getConfidential()
	{ return confidential; }

	public void setEmployeePhoto(String employeePhoto)
	{ this.employeePhoto = employeePhoto; }
	public String getEmployeePhoto()
	{ return employeePhoto; }

	public void setDateOfBirth(Date dateOfBirth)
	{ this.dateOfBirth = dateOfBirth; }
	public Date getDateOfBirth()
	{ return dateOfBirth; }

	public void setDateOfInterview(Date dateOfInterview)
	{ this.dateOfInterview = dateOfInterview; }
	public Date getDateOfInterview()
	{ return dateOfInterview; }

	public void setDateOfJoin(Date dateOfJoin)
	{ this.dateOfJoin = dateOfJoin; }
	public Date getDateOfJoin()
	{ return dateOfJoin; }

	public void setDateOfLease(Date dateOfLeasePay)
	{ this.dateOfLeasePay = dateOfLeasePay; }
	public Date getDateOfLease()
	{ return dateOfLeasePay; }

	//Legal Informations
	public void setContractId(String contractId)
	{ this.contractId = contractId; }
	public String getContractId()
	{ return contractId; }

	public void setDateContStart(Date contStart)
	{ this.contStart = contStart; }
	public Date getDateContStart()
	{ return contStart; }

	public void setDateContEnd(Date contEnd)
	{ this.contEnd = contEnd; }
	public Date getDateContEnd()
	{ return contEnd; }

	public void setCPRNo(String cprNo)
	{ this.cprNo = cprNo; }
	public String getCPRNo()
	{ return cprNo; }

	public void setDateCPRIssue(Date cprIssue)
	{ this.cprIssue = cprIssue; }
	public Date getDateCPRIssue()
	{ return cprIssue; }

	public void setDateCPRExpiry(Date cprExpiry)
	{ this.cprExpiry = cprExpiry; }
	public Date getDateCPRExpiry()
	{ return cprExpiry; }

	public void setPassport(String passportNo)
	{ this.passportNo = passportNo; }
	public String getPassport()
	{ return passportNo; }

	public void setDatePPIssue(Date passIssue)
	{ this.passIssue = passIssue; }
	public Date getDatePPIssue()
	{ return passIssue; }

	public void setDatePPExpiry(Date passExpiry)
	{ this.passExpiry = passExpiry; }
	public Date getDatePPExpiry()
	{ return passExpiry; }

	public void setResidentPermit(String residentPermit)
	{ this.residentPermit = residentPermit; }
	public String getResidentPermit()
	{ return residentPermit; }

	public void setDateRPIssue(Date residentIssue)
	{ this.residentIssue = residentIssue; }
	public Date getDateRPIssue()
	{ return residentIssue; }

	public void setDateRPExpiry(Date residentExpiry)
	{ this.residentExpiry = residentExpiry; }
	public Date getDateRPExpiry()
	{ return residentExpiry; }

	public void setDrivingLicense(String drivingLicense)
	{ this.drivingLicense = drivingLicense; }
	public String getDrivingLicense()
	{ return drivingLicense; }

	public void setDateDLIssue(Date drivingIssue)
	{ this.drivingIssue = drivingIssue; }
	public Date getDateDLIssue()
	{ return drivingIssue; }

	public void setDateDLExpiry(Date drivingExpiry)
	{ this.drivingExpiry = drivingExpiry; }
	public Date getDateDLExpiry()
	{ return drivingExpiry; }

	public void setInsurance(String insurance)
	{ this.insurance = insurance; }
	public String getInsurance()
	{ return insurance; }

	public void setDateInsIssue(Date insuranceIssue)
	{ this.insuranceIssue = insuranceIssue; }
	public Date getDateInsIssue()
	{ return insuranceIssue; }

	public void setDateInsExpiry(Date insuranceExpiry)
	{ this.insuranceExpiry = insuranceExpiry; }
	public Date getDateInsExpiry()
	{ return insuranceExpiry; }

	//Personal Informations
	public void setFatherName(String fatherName)
	{ this.fatherName = fatherName; }
	public String getFatherName()
	{ return fatherName; }

	public void setMotherName(String motherName)
	{ this.motherName = motherName; }
	public String getMotherName()
	{ return motherName; }

	public void setSpouseName(String spouseName)
	{ this.spouseName = spouseName; }
	public String getSpouseName()
	{ return spouseName; }

	public void setBahrainAddress(String bahrainAddress)
	{ this.bahrainAddress = bahrainAddress; }
	public String getBahrainAddress()
	{ return bahrainAddress; }

	public void setCountryAddress(String countryAddress)
	{ this.countryAddress = countryAddress; }
	public String getCountryAddress()
	{ return countryAddress; }

	public void setBloodGroup(String bloodGroup)
	{ this.bloodGroup = bloodGroup; }
	public String getBloodGroup()
	{ return bloodGroup; }

	//Payroll Informations
	public void setBasicSalary(double basicSalary)
	{ this.basicSalary = basicSalary; }
	public Double getBasicSalary()
	{ return basicSalary; }

	public void setIndemPercent(double indemPercent)
	{ this.indemPercent = indemPercent; }
	public Double getIndemPercent()
	{ return indemPercent; }

	public void setIntaxPercent(double intaxPercent)
	{ this.intaxPercent = intaxPercent; }
	public Double getIntaxPercent()
	{ return intaxPercent; }

	public void setGosiPercentEmp(double gosiPercentEmp)
	{ this.gosiPercentEmp = gosiPercentEmp; }
	public Double getGosiPercentEmp()
	{ return gosiPercentEmp; }

	public void setGosiPercentCom(double gosiPercentCom)
	{ this.gosiPercentCom = gosiPercentCom; }
	public Double getGosiPercentCom()
	{ return gosiPercentCom; }

	public void setSalaryPayType(String salaryPayType)
	{ this.salaryPayType = salaryPayType; }
	public String getSalaryPayType()
	{ return salaryPayType; }

	public void setBankId(String bankId)
	{ this.bankId = bankId; }
	public String getBankId()
	{ return bankId; }

	public void setAccountNo(String accountNo)
	{ this.accountNo = accountNo; }
	public String getAccountNo()
	{ return accountNo; }

	public void setIBanNo(String iBanNo)
	{ this.iBanNo = iBanNo; }
	public String getIBanNo()
	{ return iBanNo; }

	//Leave Information
	public void setLeaveList(String leaveList)
	{ this.leaveList = leaveList; }
	public String getLeaveList()
	{ return leaveList; }

	public void setLeaveBalanceId(String leaveBalanceId)
	{ this.leaveBalanceId = leaveBalanceId; }
	public String getLeaveBalanceId()
	{ return leaveBalanceId; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }

	public void setNomineeId(String nomineeId)
	{ this.nomineeId = nomineeId; }
	public String getNomineeId()
	{ return nomineeId; }

	public void setSalaryId(String salaryId)
	{ this.salaryId = salaryId; }
	public String getSalaryId()
	{ return salaryId; }

	public void setEduExpId(String eduExpId)
	{ this.eduExpId = eduExpId; }
	public String getEduExpId()
	{ return eduExpId; }

	public void setDependentsId(String dependentsId)
	{ this.dependentsId = dependentsId; }
	public String getDependentsId()
	{ return dependentsId; }

	public void setAttachId(String attachId)
	{ this.attachId = attachId; }
	public String getAttachId()
	{ return attachId; }

	public void setAllowDeductionSql(String allowDeductionSql)
	{ this.allowDeductionSql = allowDeductionSql; }
	public String getAllowDeductionSql()
	{ return allowDeductionSql; }

	public void setNomineeSql(String nomineeSql)
	{ this.nomineeSql = nomineeSql; }
	public String getNomineeSql()
	{ return nomineeSql; }

	public void setEduExpSql(String eduExperinceSql)
	{ this.eduExperinceSql = eduExperinceSql; }
	public String getEduExpSql()
	{ return eduExperinceSql; }

	public void setLeaveBalanceSql(String leaveBalanceSql)
	{ this.leaveBalanceSql = leaveBalanceSql; }
	public String getLeaveBalanceSql()
	{ return leaveBalanceSql; }

	public void setDependentsSql(String dependentsSql)
	{ this.dependentsSql = dependentsSql; }
	public String getDependentsSql()
	{ return dependentsSql; }

	public void setAttachSql(String attachSql)
	{ this.attachSql = attachSql; }
	public String getAttachSql()
	{ return attachSql; }

	public void setSalaryChange(boolean salaryChange)
	{ this.salaryChange = salaryChange; }
	public boolean getSalaryChange()
	{ return salaryChange; }

	public void setNomineeChange(boolean nomineeChange)
	{ this.nomineeChange = nomineeChange; }
	public boolean getNomineeChange()
	{ return nomineeChange; }

	public void setAllowDedChange(boolean allowDedChange)
	{ this.allowDedChange = allowDedChange; }
	public boolean getAllowDedChange()
	{ return allowDedChange; }

	public void setLeaveChange(boolean leaveChange)
	{ this.leaveChange = leaveChange; }
	public boolean getLeaveChange()
	{ return leaveChange; }

	public void setEduExpChange(boolean eduExpChange)
	{ this.eduExpChange = eduExpChange; }
	public boolean getEduExpChange()
	{ return eduExpChange; }

	public void setDependChange(boolean dependChange)
	{ this.dependChange = dependChange; }
	public boolean getDependChange()
	{ return dependChange; }

	public void setAttachChange(boolean attachChange)
	{ this.attachChange = attachChange; }
	public boolean getAttachChange()
	{ return attachChange; }
}
