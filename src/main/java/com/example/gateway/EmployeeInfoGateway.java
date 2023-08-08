package com.example.gateway;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.EmployeeInfoModel;

public class EmployeeInfoGateway
{
	public SimpleDateFormat dfDb = new SimpleDateFormat("yyyy-MM-dd");

	public EmployeeInfoGateway()
	{ }

	public boolean checkExistCode(String userCode, String employeeId)
	{
		boolean ret = false;
		String edit = (employeeId.isEmpty()?"":"and vEmployeeId != '"+employeeId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vEmployeeId from master.tbEmployeeMaster where"+
					" vEmployeeCode like '"+userCode+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist code"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean checkExistCpr(String userCpr, String employeeId)
	{
		boolean ret = false;
		String edit = (employeeId.isEmpty()?"":"and vEmployeeId != '"+employeeId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vEmployeeId from master.tbEmployeeMaster where"+
					" vCPRNumber like '"+userCpr+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist cpr"); }
		finally{ session.close(); }
		return ret;
	}

	public String getEmployeeId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vEmployeeId, 2, 10) as int)), 0)+1"+
					" from master.tbEmployeeMaster";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "E"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Employee Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getEmployeeCode()
	{
		String code = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(vEmployeeCode as int)), 0)+1 from"+
					" master.tbEmployeeMaster";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ code = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Employee Code"); }
		finally{ session.close(); };
		return code;
	}

	public String getNomineeId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vNomineeId, 2, 10) as int)), 0)+1"+
					" from master.tbEmployeeNominee";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "N"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Nominee Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getSalaryId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vSalaryId, 2, 10) as int)), 0)+1"+
					" from master.tbEmployeeSalary";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "S"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Salary Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getLeaveBalanceId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vBalanceId, 2, 10) as int)), 0)+1"+
					" from master.tbEmployeeLeaveTaken";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "W"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Balance Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getEduExpId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vPrimaryId, 2, 10) as int)), 0)+1"+
					" from master.tbEmployeeEducationExperince";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "X"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" PrimaryId Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getDependentsId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vDependentsId, 2, 10) as int)), 0)+1"+
					" from master.tbEmployeeDependents";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "M"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" EmployeeDependents Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getAttachId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vAttachId, 2, 10) as int)), 0)+1"+
					" from master.tbEmployeeAttachment";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "A"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Attach Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public boolean updateCountryList(String countryIds)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sqlUpdate = " update master.tbCountryList set iActive = 0 update master.tbCountryList"+
					" set iActive = 1 where vCountryId in ("+countryIds+")";
			SQLQuery insertUpdate = session.createSQLQuery(sqlUpdate);
			insertUpdate.executeUpdate();
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			tx.rollback();
			System.out.println("Error update insert: "+exp);
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean insertEditData(EmployeeInfoModel eim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sqlMain = "insert into master.tbEmployeeMaster(vBranchId, vEmployeeId, vEmployeeCode, vEmployeeName, vGender,"+
						" vMaritialStatus, vReligion, vContactNo, vEmailAddress, vTypeId, vNationality, vVisaTypeId, vStatusId,"+
						" dDateOfBirth, dDateOfInterview, dDateOfJoin, vDeparmentId, vDesignationId, vEmployeeType, iOtEnable,"+
						" vEmployeePhoto, vContractRefId, dContractStartDate, dContractEndDate, vCPRNumber, dCPRIssueDate,"+
						" dCPRExpiryDate, vPassportNo, dPassportIssueDate, dPassportExpiryDate, vResidentPermitNo, iConfidential,"+
						" dResidentPermitIssueDate, dResidentPermitExpiryDate, vDrivingLicenseNo, dDrivingLicenseIssueDate,"+
						" dDrivingLicenseExpiryDate, vInsuranceNo, dInsuranceIssueDate, dInsuranceExpiryDate, vFatherName,"+
						" vMotherName, vSpouseName, vBloodGroup, vAddressInBahrain, vAddressInCountry, vEnjoyableLeave, vSalaryPayType,"+
						" vBankId, vIbanNumber, vAccountNumber, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate, "+
						" vTypeDate, vVisaTypeDate, vStatusDate, iSynced, vSyncedMacId) values (:branchId, :employeeId, :employeeCode,"+ 
						" :employeeName, :gender, :maritialStatus, :religion, :contactNo, :emailAddress, :typeId, :nationality, :visaTypeId,"+
						" :statusId, :dateOfBirth, :dateOfInterview, :dateOfJoin, :deparmentId, :designationId, :employeeType, :otEnable,"+
						" :employeePhoto, :contractRefId, :contractStartDate, :contractEndDate, :cPRNumber, :cPRIssueDate,"+
						" :cPRExpiryDate, :passportNo, :passportIssueDate, :passportExpiryDate, :residentPermitNo, :confidential,"+
						" :residentPermitIssueDate, :residentPermitExpiryDate, :drivingLicenseNo, :drivingLicenseIssueDate,"+
						" :drivingLicenseExpiryDate, :insuranceNo, :insuranceIssueDate, :insuranceExpiryDate, :fatherName,"+
						" :motherName, :spouseName, :bloodGroup, :addressInBahrain, :addressInCountry, :enjoyableLeave,"+
						" :salaryPayType, :bankId, :iBanNo, :accountNo, 1, :createdBy, getDate(), :modifiedBy, getDate(),"+
						" :typeDate, :visaTypeDate, :statusDate, 0, '')";
				SQLQuery insertMain = session.createSQLQuery(sqlMain);
				//System.out.println(eim.getDateOfBirth()+"\n"+eim.getDateOfInterview()+"\n"+eim.getDateOfJoin());
				//Official Info
				insertMain.setParameter("branchId", eim.getBranchId());
				insertMain.setParameter("employeeId", eim.getEmployeeId());
				insertMain.setParameter("employeeCode", eim.getEmployeeCode());
				insertMain.setParameter("employeeName", eim.getEmployeeName());
				insertMain.setParameter("gender", eim.getGender());
				insertMain.setParameter("maritialStatus", eim.getMaritialStatus());
				insertMain.setParameter("religion", eim.getReligion());
				insertMain.setParameter("contactNo", eim.getContactNo());
				insertMain.setParameter("emailAddress", eim.getEmailId());
				insertMain.setParameter("typeId", eim.getEmployeeTypeId());
				insertMain.setParameter("nationality", eim.getNationality());
				insertMain.setParameter("visaTypeId", eim.getVisaTypeId());
				insertMain.setParameter("statusId", eim.getEmployeeStatusId());
				insertMain.setParameter("dateOfBirth", dfDb.format(eim.getDateOfBirth()));
				insertMain.setParameter("dateOfInterview", dfDb.format(eim.getDateOfInterview()));
				insertMain.setParameter("dateOfJoin", dfDb.format(eim.getDateOfJoin()));
				insertMain.setParameter("deparmentId", eim.getDeptId());
				insertMain.setParameter("designationId", eim.getDesigId());
				insertMain.setParameter("employeeType", eim.getEmpType());
				insertMain.setParameter("otEnable", eim.getOtEnable());
				insertMain.setParameter("confidential", eim.getConfidential());
				insertMain.setParameter("employeePhoto", eim.getEmployeePhoto());
				//Legal Info
				insertMain.setParameter("contractRefId", eim.getContractId());
				insertMain.setParameter("contractStartDate", eim.getContractId().isEmpty()?"":dfDb.format(eim.getDateContStart()));
				insertMain.setParameter("contractEndDate", eim.getContractId().isEmpty()?"":dfDb.format(eim.getDateContEnd()));

				insertMain.setParameter("cPRNumber", eim.getCPRNo());
				insertMain.setParameter("cPRIssueDate", dfDb.format(eim.getDateCPRIssue()));
				insertMain.setParameter("cPRExpiryDate", dfDb.format(eim.getDateCPRExpiry()));
				insertMain.setParameter("passportNo", eim.getPassport());
				insertMain.setParameter("passportIssueDate", dfDb.format(eim.getDatePPIssue()));
				insertMain.setParameter("passportExpiryDate", dfDb.format(eim.getDatePPExpiry()));

				insertMain.setParameter("residentPermitNo", eim.getResidentPermit());
				insertMain.setParameter("residentPermitIssueDate", eim.getResidentPermit().isEmpty()?"":dfDb.format(eim.getDateRPIssue()));
				insertMain.setParameter("residentPermitExpiryDate", eim.getResidentPermit().isEmpty()?"":dfDb.format(eim.getDateRPExpiry()));

				insertMain.setParameter("drivingLicenseNo", eim.getDrivingLicense());
				insertMain.setParameter("drivingLicenseIssueDate", eim.getDrivingLicense().isEmpty()?"":dfDb.format(eim.getDateDLIssue()));
				insertMain.setParameter("drivingLicenseExpiryDate", eim.getDrivingLicense().isEmpty()?"":dfDb.format(eim.getDateDLExpiry()));

				insertMain.setParameter("insuranceNo", eim.getInsurance());
				insertMain.setParameter("insuranceIssueDate", eim.getInsurance().isEmpty()?"":dfDb.format(eim.getDateInsIssue()));
				insertMain.setParameter("insuranceExpiryDate", eim.getInsurance().isEmpty()?"":dfDb.format(eim.getDateInsExpiry()));
				//Personal Info
				insertMain.setParameter("fatherName", eim.getFatherName());
				insertMain.setParameter("motherName", eim.getMotherName());
				insertMain.setParameter("spouseName", eim.getSpouseName());
				insertMain.setParameter("bloodGroup", eim.getBloodGroup());
				insertMain.setParameter("addressInBahrain", eim.getBahrainAddress());
				insertMain.setParameter("addressInCountry", eim.getCountryAddress());
				//Leave Info
				insertMain.setParameter("enjoyableLeave", eim.getLeaveList());
				//Account Info
				insertMain.setParameter("salaryPayType", eim.getSalaryPayType());
				insertMain.setParameter("bankId", eim.getBankId());
				insertMain.setParameter("iBanNo", eim.getIBanNo());
				insertMain.setParameter("accountNo", eim.getAccountNo());
				insertMain.setParameter("createdBy", eim.getCreatedBy());
				insertMain.setParameter("modifiedBy", eim.getCreatedBy());
				insertMain.setParameter("typeDate", eim.getEmpTypeDate());
				insertMain.setParameter("visaTypeDate", eim.getVisaTypeDate());
				insertMain.setParameter("statusDate", eim.getEmpStatusDate());
				insertMain.executeUpdate();

				//Nominee Info
				//System.out.println(eim.getNomineeSql());
				if (!eim.getNomineeSql().trim().isEmpty())
				{ session.createSQLQuery(eim.getNomineeSql()).executeUpdate(); }

				//Education Experience Info
				if (!eim.getEduExpSql().trim().isEmpty())
				{ session.createSQLQuery(eim.getEduExpSql()).executeUpdate(); }

				//Dependents Info
				if (!eim.getDependentsSql().trim().isEmpty())
				{ session.createSQLQuery(eim.getDependentsSql()).executeUpdate(); }

				/*//Salary Information
				String sqlSalary = "insert into master.tbEmployeeSalary(vSalaryId, vEmployeeId, mBasicAmount, mIndemnityPercent, mIncomeTaxPercent,"+
						" mGosiEmployeePercent, mGosiCompanyPercent, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate) values "+
						" (:salaryId, :employeeId, :basicAmount, :indemnityPercent, :incomeTaxPercent, :gosiEmployeePercent, :gosiCompanyPercent,"+
						" 1, :createdBy, getDate(), :modifiedBy, getDate())";
				SQLQuery insertSalary = session.createSQLQuery(sqlSalary);
				insertSalary.setParameter("salaryId", eim.getSalaryId());
				insertSalary.setParameter("employeeId", eim.getEmployeeId());
				insertSalary.setParameter("basicAmount", eim.getBasicSalary());
				insertSalary.setParameter("indemnityPercent", eim.getIndemPercent());
				insertSalary.setParameter("incomeTaxPercent", eim.getIntaxPercent());
				insertSalary.setParameter("gosiEmployeePercent", eim.getGosiPercentEmp());
				insertSalary.setParameter("gosiCompanyPercent", eim.getGosiPercentCom());
				insertSalary.setParameter("createdBy", eim.getCreatedBy());
				insertSalary.setParameter("modifiedBy", eim.getCreatedBy());
				insertSalary.executeUpdate();
				if (!eim.getAllowDeductionSql().trim().isEmpty())
				{ session.createSQLQuery(eim.getAllowDeductionSql()).executeUpdate(); }

				//Leave Information
				if (!eim.getLeaveBalanceSql().trim().isEmpty())
				{ session.createSQLQuery(eim.getLeaveBalanceSql()).executeUpdate(); }

				//Attachment Information
				if (!eim.getAttachSql().trim().isEmpty())
				{ session.createSQLQuery(eim.getAttachSql()).executeUpdate(); }*/
			}
			else if (flag.equals("Edit"))
			{
				String sqlMain = "update master.tbEmployeeMaster set vEmployeeCode = :employeeCode, vEmployeeName = :employeeName,"+
						" vGender = :gender, vMaritialStatus = :maritialStatus, vReligion = :religion, vContactNo = :contactNo,"+
						" vEmailAddress = :emailAddress, vTypeId = :typeId, vNationality = :nationality, vVisaTypeId = :visaTypeId,"+
						" vStatusId = :statusId, dDateOfBirth = :dateOfBirth, dDateOfInterview = :dateOfInterview, dDateOfJoin ="+
						" :dateOfJoin, vDeparmentId = :deparmentId, vDesignationId = :designationId, vEmployeeType = :employeeType,"+
						" iOtEnable = :otEnable, vEmployeePhoto = :employeePhoto, vContractRefId = :contractRefId, dContractStartDate"+
						" = :contractStartDate, dContractEndDate = :contractEndDate, vCPRNumber = :cPRNumber, dCPRIssueDate = :cPRIssueDate,"+
						" dCPRExpiryDate = :cPRExpiryDate, vPassportNo = :passportNo, dPassportIssueDate = :passportIssueDate,"+
						" dPassportExpiryDate = :passportExpiryDate, vResidentPermitNo = :residentPermitNo, dResidentPermitIssueDate = "+
						" :residentPermitIssueDate, dResidentPermitExpiryDate = :residentPermitExpiryDate, vDrivingLicenseNo = :drivingLicenseNo,"+
						" dDrivingLicenseIssueDate = :drivingLicenseIssueDate, dDrivingLicenseExpiryDate = :drivingLicenseExpiryDate,"+
						" vInsuranceNo = :insuranceNo, dInsuranceIssueDate = :insuranceIssueDate, dInsuranceExpiryDate = :insuranceExpiryDate,"+
						" vFatherName = :fatherName, vMotherName = :motherName, vSpouseName = :spouseName, vBloodGroup = :bloodGroup,"+
						" vAddressInBahrain = :addressInBahrain, vAddressInCountry = :addressInCountry, vEnjoyableLeave = :enjoyableLeave,"+
						" vSalaryPayType = :salaryPayType, vBankId = :bankId, vIbanNumber = :iBanNo, vAccountNumber = :accountNo,"+
						" vModifiedBy = :modifiedBy, dModifiedDate = getDate(), vTypeDate = :typeDate, vVisaTypeDate =  :visaTypeDate,"+
						" vStatusDate = :statusDate, iConfidential = :confidential, iSynced = 0, vSyncedMacId = '' where vEmployeeId = :employeeId";
				SQLQuery insertMain = session.createSQLQuery(sqlMain);
				insertMain.setParameter("employeeCode", eim.getEmployeeCode());
				insertMain.setParameter("employeeName", eim.getEmployeeName());
				insertMain.setParameter("gender", eim.getGender());
				insertMain.setParameter("maritialStatus", eim.getMaritialStatus());
				insertMain.setParameter("religion", eim.getReligion());
				insertMain.setParameter("contactNo", eim.getContactNo());
				insertMain.setParameter("emailAddress", eim.getEmailId());
				insertMain.setParameter("typeId", eim.getEmployeeTypeId());
				insertMain.setParameter("nationality", eim.getNationality());
				insertMain.setParameter("visaTypeId", eim.getVisaTypeId());
				insertMain.setParameter("statusId", eim.getEmployeeStatusId());
				insertMain.setParameter("dateOfBirth", dfDb.format(eim.getDateOfBirth()));
				insertMain.setParameter("dateOfInterview", dfDb.format(eim.getDateOfInterview()));
				insertMain.setParameter("dateOfJoin", dfDb.format(eim.getDateOfJoin()));
				insertMain.setParameter("deparmentId", eim.getDeptId());
				insertMain.setParameter("designationId", eim.getDesigId());
				insertMain.setParameter("employeeType", eim.getEmpType());
				insertMain.setParameter("otEnable", eim.getOtEnable());
				insertMain.setParameter("confidential", eim.getConfidential());
				insertMain.setParameter("employeePhoto", eim.getEmployeePhoto());
				//Legal Info
				insertMain.setParameter("contractRefId", eim.getContractId());
				insertMain.setParameter("contractStartDate", eim.getContractId().isEmpty()?"":dfDb.format(eim.getDateContStart()));
				insertMain.setParameter("contractEndDate", eim.getContractId().isEmpty()?"":dfDb.format(eim.getDateContEnd()));

				insertMain.setParameter("cPRNumber", eim.getCPRNo());
				insertMain.setParameter("cPRIssueDate", dfDb.format(eim.getDateCPRIssue()));
				insertMain.setParameter("cPRExpiryDate", dfDb.format(eim.getDateCPRExpiry()));
				insertMain.setParameter("passportNo", eim.getPassport());
				insertMain.setParameter("passportIssueDate", dfDb.format(eim.getDatePPIssue()));
				insertMain.setParameter("passportExpiryDate", dfDb.format(eim.getDatePPExpiry()));

				insertMain.setParameter("residentPermitNo", eim.getResidentPermit());
				insertMain.setParameter("residentPermitIssueDate", eim.getResidentPermit().isEmpty()?"":dfDb.format(eim.getDateRPIssue()));
				insertMain.setParameter("residentPermitExpiryDate", eim.getResidentPermit().isEmpty()?"":dfDb.format(eim.getDateRPExpiry()));

				insertMain.setParameter("drivingLicenseNo", eim.getDrivingLicense());
				insertMain.setParameter("drivingLicenseIssueDate", eim.getDrivingLicense().isEmpty()?"":dfDb.format(eim.getDateDLIssue()));
				insertMain.setParameter("drivingLicenseExpiryDate", eim.getDrivingLicense().isEmpty()?"":dfDb.format(eim.getDateDLExpiry()));

				insertMain.setParameter("insuranceNo", eim.getInsurance());
				insertMain.setParameter("insuranceIssueDate", eim.getInsurance().isEmpty()?"":dfDb.format(eim.getDateInsIssue()));
				insertMain.setParameter("insuranceExpiryDate", eim.getInsurance().isEmpty()?"":dfDb.format(eim.getDateInsExpiry()));
				//Personal Info
				insertMain.setParameter("fatherName", eim.getFatherName());
				insertMain.setParameter("motherName", eim.getMotherName());
				insertMain.setParameter("spouseName", eim.getSpouseName());
				insertMain.setParameter("bloodGroup", eim.getBloodGroup());
				insertMain.setParameter("addressInBahrain", eim.getBahrainAddress());
				insertMain.setParameter("addressInCountry", eim.getCountryAddress());
				//Leave Info
				insertMain.setParameter("enjoyableLeave", eim.getLeaveList());
				//Account Info
				insertMain.setParameter("salaryPayType", eim.getSalaryPayType());
				insertMain.setParameter("bankId", eim.getBankId());
				insertMain.setParameter("iBanNo", eim.getIBanNo());
				insertMain.setParameter("accountNo", eim.getAccountNo());
				insertMain.setParameter("modifiedBy", eim.getCreatedBy());
				insertMain.setParameter("typeDate", eim.getEmpTypeDate());
				insertMain.setParameter("visaTypeDate", eim.getVisaTypeDate());
				insertMain.setParameter("statusDate", eim.getEmpStatusDate());
				//Where
				insertMain.setParameter("employeeId", eim.getEmployeeId());
				insertMain.executeUpdate();

				if (eim.getSalaryChange())
				{
					//Salary Information
					String sqlSalaryUpdate = "update master.tbEmployeeSalary set iActive = 0, vModifiedBy = :modifiedBy,"+
							" dModifiedDate = getDate() where vEmployeeId = :employeeId and vSalaryId = :salaryId";
					SQLQuery insertSalaryUpdate = session.createSQLQuery(sqlSalaryUpdate);
					insertSalaryUpdate.setParameter("modifiedBy", eim.getCreatedBy());
					//Where
					insertSalaryUpdate.setParameter("employeeId", eim.getEmployeeId());
					insertSalaryUpdate.setParameter("salaryId", eim.getSalaryId());
					insertSalaryUpdate.executeUpdate();

					String sqlSalary = "insert into master.tbEmployeeSalary(vSalaryId, vEmployeeId, mBasicAmount, mIndemnityPercent, mIncomeTaxPercent,"+
							" mGosiEmployeePercent, mGosiCompanyPercent, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate) values "+
							" (:salaryId, :employeeId, :basicAmount, :indemnityPercent, :incomeTaxPercent, :gosiEmployeePercent, :gosiCompanyPercent,"+
							" 1, :createdBy, getDate(), :modifiedBy, getDate())";
					SQLQuery insertSalary = session.createSQLQuery(sqlSalary);
					insertSalary.setParameter("salaryId", eim.getSalaryId());
					insertSalary.setParameter("employeeId", eim.getEmployeeId());
					insertSalary.setParameter("basicAmount", eim.getBasicSalary());
					insertSalary.setParameter("indemnityPercent", eim.getIndemPercent());
					insertSalary.setParameter("incomeTaxPercent", eim.getIntaxPercent());
					insertSalary.setParameter("gosiEmployeePercent", eim.getGosiPercentEmp());
					insertSalary.setParameter("gosiCompanyPercent", eim.getGosiPercentCom());
					insertSalary.setParameter("createdBy", eim.getCreatedBy());
					insertSalary.setParameter("modifiedBy", eim.getCreatedBy());
					insertSalary.executeUpdate();
				}

				if (eim.getAllowDedChange())
				{
					if (!eim.getAllowDeductionSql().trim().isEmpty())
					{ session.createSQLQuery(eim.getAllowDeductionSql()).executeUpdate(); }
				}

				if (eim.getNomineeChange())
				{
					//Nominee Info
					//System.out.println(eim.getNomineeSql());
					if (!eim.getNomineeSql().trim().isEmpty())
					{ session.createSQLQuery(eim.getNomineeSql()).executeUpdate(); }
				}

				if (eim.getEduExpChange())
				{
					//Education Experience Info
					if (!eim.getEduExpSql().trim().isEmpty())
					{ session.createSQLQuery(eim.getEduExpSql()).executeUpdate(); }
				}

				if (eim.getDependChange())
				{
					//Dependents Info
					if (!eim.getDependentsSql().trim().isEmpty())
					{ session.createSQLQuery(eim.getDependentsSql()).executeUpdate(); }
				}

				if (eim.getLeaveChange())
				{
					//Leave Information
					if (!eim.getLeaveBalanceSql().trim().isEmpty())
					{ session.createSQLQuery(eim.getLeaveBalanceSql()).executeUpdate(); }
				}

				if (eim.getAttachChange())
				{
					//Attachment Information
					if (!eim.getAttachSql().trim().isEmpty())
					{ session.createSQLQuery(eim.getAttachSql()).executeUpdate(); }
				}

				/*System.out.println("Salary: "+eim.getSalaryChange());
				System.out.println("Allow: "+eim.getAllowDedChange());
				System.out.println("Nominee: "+eim.getNomineeChange());
				System.out.println("Eduexp: "+eim.getEduExpChange());
				System.out.println("Depend: "+eim.getDependChange());
				System.out.println("Leave: "+eim.getLeaveChange());
				System.out.println("Attach: "+eim.getAttachChange());

				System.out.println("Salary Id: "+eim.getSalaryId());
				System.out.println("Nominee Id: "+eim.getNomineeId());
				System.out.println("Eduexp Id: "+eim.getEduExpId());
				System.out.println("Depend Id: "+eim.getDependentsId());
				System.out.println("Leave Id: "+eim.getLeaveBalanceId());
				System.out.println("Attach Id: "+eim.getAttachId());*/
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert Employee: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String employeeId, String modifiedId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sql = "update master.tbEmployeeMaster set iActive = (select case when iActive = 1"+
					" then 0 else 1 end from master.tbEmployeeMaster where vEmployeeId = :user1),"+
					" vModifiedBy = :modifiedBy, dModifiedDate = getdate(), iSynced = 0,"+
					" vSyncedMacId = '' where vEmployeeId = :user2";

			Query insert = session.createSQLQuery(sql);
			insert.setParameter("user1", employeeId);
			insert.setParameter("user2", employeeId);
			insert.setParameter("modifiedBy", modifiedId);
			insert.executeUpdate();

			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update Employee"+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(EmployeeInfoModel eim, String employeeIdFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select em.vEmployeeCode, em.vEmployeeName, em.vGender, em.vMaritialStatus, em.vReligion,"+
					" em.vContactNo, em.vEmailAddress, em.vTypeId, em.vNationality, em.vVisaTypeId, em.vStatusId,"+
					" em.dDateOfBirth, em.dDateOfInterview, em.dDateOfJoin, em.vDeparmentId, em.vDesignationId,"+
					" em.vEmployeeType, em.iOtEnable, em.vEmployeePhoto, em.vContractRefId, em.dContractStartDate,"+
					" em.dContractEndDate, em.vCPRNumber, em.dCPRIssueDate, em.dCPRExpiryDate, em.vPassportNo,"+
					" em.dPassportIssueDate, em.dPassportExpiryDate, em.vResidentPermitNo, em.dResidentPermitIssueDate,"+
					" em.dResidentPermitExpiryDate, em.vDrivingLicenseNo, em.dDrivingLicenseIssueDate,"+
					" em.dDrivingLicenseExpiryDate, em.vInsuranceNo, em.dInsuranceIssueDate, em.dInsuranceExpiryDate,"+
					" em.vFatherName, em.vMotherName, em.vSpouseName, em.vBloodGroup, em.vAddressInBahrain,"+
					" em.vAddressInCountry, em.vEnjoyableLeave, em.vSalaryPayType, em.vBankId, em.vIbanNumber,"+
					" em.vAccountNumber, em.vTypeDate, em.vVisaTypeDate, em.vStatusDate, em.iConfidential"+
					" from master.tbEmployeeMaster em where em.vEmployeeId = '"+employeeIdFind+"'";
			//System.out.println(sql);
			SQLQuery select = session.createSQLQuery(sql);
			//select.setParameter("employeeId", employeeIdFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();

				eim.setEmployeeId(employeeIdFind);
				eim.setEmployeeCode(element[0].toString());
				eim.setEmployeeName(element[1].toString());
				eim.setGender(element[2].toString());
				eim.setMaritialStatus(element[3].toString());
				eim.setReligion(element[4].toString());
				eim.setContactNo(element[5].toString());
				eim.setEmailId(element[6].toString());
				eim.setEmployeeTypeId(element[7].toString());
				eim.setNationality(element[8].toString());
				eim.setVisaTypeId(element[9].toString());
				eim.setEmployeeStatusId(element[10].toString());
				eim.setDateOfBirth((Date) element[11]);
				eim.setDateOfInterview((Date) element[12]);
				eim.setDateOfJoin((Date) element[13]);
				//eim.setDateOfLease(tabOfficials.txtDateOfLease.getValue());
				eim.setDeptId(element[14].toString());
				eim.setDesigId(element[15].toString());
				eim.setEmpType(element[16].toString());
				eim.setOtEnable(element[17].toString().equals("1")?1:0);
				eim.setEmployeePhoto(element[18].toString());

				//Legal Informations
				eim.setContractId(element[19].toString());
				eim.setDateContStart(element[20].toString().isEmpty()?new Date():(Date) dfDb.parse(element[20].toString()));
				eim.setDateContEnd(element[21].toString().isEmpty()?new Date():(Date) dfDb.parse(element[21].toString()));

				eim.setCPRNo(element[22].toString());
				eim.setDateCPRIssue(element[23].toString().isEmpty()?new Date():(Date) element[23]);
				eim.setDateCPRExpiry(element[24].toString().isEmpty()?new Date():(Date) element[24]);

				eim.setPassport(element[25].toString());
				eim.setDatePPIssue(element[26].toString().isEmpty()?new Date():(Date) element[26]);
				eim.setDatePPExpiry(element[27].toString().isEmpty()?new Date():(Date) element[27]);

				eim.setResidentPermit(element[28].toString());
				eim.setDateRPIssue(element[29].toString().isEmpty()?new Date():(Date) dfDb.parse(element[29].toString()));
				eim.setDateRPExpiry(element[30].toString().isEmpty()?new Date():(Date) dfDb.parse(element[30].toString()));

				eim.setDrivingLicense(element[31].toString());
				eim.setDateDLIssue(element[32].toString().isEmpty()?new Date():(Date) dfDb.parse(element[32].toString()));
				eim.setDateDLExpiry(element[33].toString().isEmpty()?new Date():(Date) dfDb.parse(element[33].toString()));

				eim.setInsurance(element[34].toString());
				eim.setDateInsIssue(element[35].toString().isEmpty()?new Date():(Date) dfDb.parse(element[35].toString()));
				eim.setDateInsExpiry(element[36].toString().isEmpty()?new Date():(Date) dfDb.parse(element[36].toString()));

				//Personal Information
				eim.setFatherName(element[37].toString());
				eim.setMotherName(element[38].toString());
				eim.setSpouseName(element[39].toString());
				eim.setBloodGroup((element[40].toString()));
				eim.setBahrainAddress(element[41].toString());
				eim.setCountryAddress(element[42].toString());

				//Leave Information
				eim.setLeaveList(element[43].toString());

				//Payroll Information
				eim.setSalaryPayType(element[44].toString());
				eim.setBankId(element[45].toString());
				eim.setIBanNo(element[46].toString());
				eim.setAccountNo(element[47].toString());

				/*eim.setBasicSalary(Double.parseDouble(element[48].toString()));
				eim.setIndemPercent(Double.parseDouble(element[49].toString()));
				eim.setIntaxPercent(Double.parseDouble(element[50].toString()));
				eim.setGosiPercentEmp(Double.parseDouble(element[51].toString()));
				eim.setGosiPercentCom(Double.parseDouble(element[52].toString()));
				eim.setSalaryId(element[53].toString());
				eim.setEmpTypeDate(element[54].toString());
				eim.setVisaTypeDate(element[55].toString());
				eim.setEmpStatusDate(element[56].toString());
				eim.setConfidential(element[57].toString().equals("1")?1:0);

				setOtherIds(eim);*/
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	/*private void setOtherIds(EmployeeInfoModel eim)
	{
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select"+
					" isnull((select top 1 vNomineeId from master.tbEmployeeNominee where vEmployeeId = :employeeId and iActive = 1), '') vNomineeId,"+
					" isnull((select top 1 vPrimaryId from master.tbEmployeeEducationExperince where vEmployeeId = :employeeId and iActive = 1), '') vPrimaryId,"+
					" isnull((select top 1 vDependentsId from master.tbEmployeeDependents where vEmployeeId = :employeeId and iActive = 1), '') vDependentsId,"+
					" isnull((select top 1 vBalanceId from master.tbEmployeeLeaveTaken where vEmployeeId = :employeeId and iActive = 1), '') vBalanceId,"+
					" isnull((select top 1 vAttachId from master.tbEmployeeAttachment where vEmployeeId = :employeeId and iActive = 1), '') vAttachId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("employeeId", eim.getEmployeeId());

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				eim.setNomineeId(element[0].toString());
				eim.setEduExpId(element[1].toString());
				eim.setDependentsId(element[2].toString());
				eim.setLeaveBalanceId(element[3].toString());
				eim.setAttachId(element[4].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
	}*/

	public String getAttendanceId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vAttendanceId, 2, 10) as int)), 0)+1"+
					" from trans.tbAttendanceDetails";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "A"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Attendance Id"); }
		finally{ session.close(); };
		return maxId;
	}
}
