package com.example.gateway;

import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.CompanyInfoModel;

public class ItemCompanyInfoGateway
{
	public ItemCompanyInfoGateway()
	{ }

	public boolean checkExist(String companyName, String companyId)
	{
		boolean ret = false;
		String edit = (companyId.isEmpty()?"":"and vCompanyId != '"+companyId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vCompanyId from master.tbItemCompanyMaster where"+
					" vCompanyName like '"+companyName+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist company"); }
		finally{ session.close(); }
		return ret;
	}

	public String getCompanyId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vCompanyId, 2, 10) as int)),"+
					" 0)+1 from master.tbItemCompanyMaster";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "S"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public boolean insertEditData(CompanyInfoModel cim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbItemCompanyMaster(vBranchId, vCompanyId, vCompanyName,"+
						" vAddress, vPhone, vFax, vEmail, vLicenceNo, vContactPerson, vMobileNo, vEmailId,"+
						" iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate) values ("+
						" :branchId, :companyId, :companyName, :address, :phone, :fax, :email, :license, '',"+
						" '', '', 1, :createdBy, getDate(), :modifiedBy, getDate())";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", cim.getBranchId());
				insert.setParameter("companyId", cim.getCompanyId());
				insert.setParameter("companyName", cim.getCompanyName());
				insert.setParameter("address", cim.getAddress());
				insert.setParameter("phone", cim.getPhone());
				insert.setParameter("fax", cim.getFax());
				insert.setParameter("email", cim.getEmail());
				insert.setParameter("license", cim.getLicense());
				insert.setParameter("createdBy", cim.getCreatedBy());
				insert.setParameter("modifiedBy", cim.getCreatedBy());
				insert.executeUpdate();
			}
			else if (flag.equals("Edit"))
			{
				String sql = " update master.tbItemCompanyMaster set vCompanyName = :companyName,"+
						" vAddress = :address, vPhone = :phone, vFax = :fax, vEmail = :email, vLicenceNo"+
						" = :license, vModifiedBy = :modifiedBy, dModifiedDate = getDate()"+
						" where vCompanyId = :companyId ";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("companyName", cim.getCompanyName());
				insert.setParameter("address", cim.getAddress());
				insert.setParameter("phone", cim.getPhone());
				insert.setParameter("fax", cim.getFax());
				insert.setParameter("email", cim.getEmail());
				insert.setParameter("license", cim.getLicense());
				insert.setParameter("modifiedBy", cim.getCreatedBy());
				//where clause
				insert.setParameter("companyId", cim.getCompanyId());
				insert.executeUpdate();
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert update: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String userId, String modifiedId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sql = "update master.tbItemCompanyMaster set iActive = (select case when iActive = 1"+
					" then 0 else 1 end from master.tbItemCompanyMaster where vCompanyId = :subId1),"+
					" vModifiedBy = :modifiedBy, dModifiedDate = CURRENT_TIMESTAMP where"+
					" vCompanyId = :subId2";

			Query insert = session.createSQLQuery(sql);
			insert.setParameter("subId1", userId);
			insert.setParameter("subId2", userId);
			insert.setParameter("modifiedBy", modifiedId);
			insert.executeUpdate();

			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(CompanyInfoModel cim, String subBranchId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vBranchId, vCompanyName, vAddress, vPhone, vFax, vEmail, vLicenceNo"+
					" from master.tbItemCompanyMaster where vCompanyId = :subBranchId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("subBranchId", subBranchId);
			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				cim.setBranchId(element[0].toString());
				cim.setCompanyName(element[1].toString());
				cim.setAddress(element[2].toString());
				cim.setPhone(element[3].toString());
				cim.setFax(element[4].toString());
				cim.setEmail(element[5].toString());
				cim.setLicense(element[6].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}
}
