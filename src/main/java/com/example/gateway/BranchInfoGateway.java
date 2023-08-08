package com.example.gateway;

import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.BranchInfoModel;

public class BranchInfoGateway
{
	public BranchInfoGateway()
	{ }

	public boolean checkExist(String branchName, String branchId)
	{
		boolean ret = false;
		String edit = (branchId.isEmpty()?"":"and vBranchId != '"+branchId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vBranchId from master.tbBranchMaster where vBranchName"+
					" like '"+branchName+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist data"); }
		finally{session.close();}
		return ret;
	}

	public String getBranchId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select 'B'+right('0000'+Convert(varchar(10), ISNULL(MAX(SUBSTRING(vBranchId,"+
					" 2, 10)), 0) + 1), 4) vBranchId from master.tbBranchMaster";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" BranchId"); }
		finally{session.close();};
		return maxId;
	}

	public boolean insertEditData(BranchInfoModel cim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbBranchMaster(vCompanyId, vBranchId, vBranchName,"+
						" vAddress, vPhone, vFax, vEmail, vLicenceNo, vContactPerson, vMobileNo, vEmailId,"+
						" iBranchTypeId, vStartFrom, iActive, vCreatedBy, dCreatedDate, vModifiedBy,"+
						" dModifiedDate, iSessionTime, iSynced, vSyncedMacId) values ( :companyId, :branchId,"+
						" :branchName, :address, :phone, :fax, :email, :license, '', '', '', :branchTypeId,"+
						" '', 1, :createdBy, getDate(), :modifiedBy, getDate(), 30, 0, '')";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("companyId", cim.getCompanyId());
				insert.setParameter("branchId", cim.getBranchId());
				insert.setParameter("branchName", cim.getBranchName());
				insert.setParameter("address", cim.getAddress());
				insert.setParameter("phone", cim.getPhone());
				insert.setParameter("fax", cim.getFax());
				insert.setParameter("email", cim.getEmail());
				insert.setParameter("license", cim.getLicense());
				insert.setParameter("branchTypeId", cim.getBranchType());
				insert.setParameter("createdBy", cim.getCreatedBy());
				insert.setParameter("modifiedBy", cim.getCreatedBy());
				insert.executeUpdate();
			}
			else if (flag.equals("Edit"))
			{
				String sql = " update master.tbBranchMaster set vBranchName = :branchName, vAddress = :address,"+
						" vPhone = :phone, vFax = :fax, vEmail = :email, vLicenceNo = :license,"+
						" iBranchTypeId = :branchTypeId, vModifiedBy = :modifiedBy, dModifiedDate = getDate(),"+
						" iSynced = 0, vSyncedMacId = '' where vBranchId = :branchId ";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchName", cim.getBranchName());
				insert.setParameter("address", cim.getAddress());
				insert.setParameter("phone", cim.getPhone());
				insert.setParameter("fax", cim.getFax());
				insert.setParameter("email", cim.getEmail());
				insert.setParameter("license", cim.getLicense());
				insert.setParameter("branchTypeId", cim.getBranchType());
				insert.setParameter("modifiedBy", cim.getCreatedBy());
				//where clause
				insert.setParameter("branchId", cim.getBranchId());
				insert.executeUpdate();
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert company: "+exp);
			tx.rollback();
		}
		finally{session.close();}
		return ret;
	}

	public boolean activeInactiveData(String deptId, String modifiedId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sql = "update master.tbBranchMaster set iActive = (select case when iActive = 1"+
					" then 0 else 1 end from master.tbBranchMaster where vBranchId = :para1),"+
					" vModifiedBy = :modifiedBy, dModifiedDate = getdate(), iSynced = 0,"+
					" vSyncedMacId = '' where vBranchId = :para2";

			Query insert = session.createSQLQuery(sql);
			insert.setParameter("para1", deptId);
			insert.setParameter("para2", deptId);
			insert.setParameter("modifiedBy", modifiedId);
			insert.executeUpdate();

			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update branch: "+exp);
			tx.rollback();
		}
		finally{session.close();}
		return ret;
	}

	public boolean selectEditData(BranchInfoModel cim, String branchIdFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vBranchName, vAddress, vPhone, vFax, vEmail, vLicenceNo,"+
					" iBranchTypeId from master.tbBranchMaster where vBranchId = :branchId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("branchId", branchIdFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				cim.setBranchName(element[0].toString());
				cim.setAddress(element[1].toString());
				cim.setPhone(element[2].toString());
				cim.setFax(element[3].toString());
				cim.setEmail(element[4].toString());
				cim.setLicense(element[5].toString());
				cim.setBranchType(element[6].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{session.close();}
		return ret;
	}
}
