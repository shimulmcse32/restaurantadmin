package com.example.gateway;

import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.CompanyInfoModel;

public class CompanyInfoGateway
{
	public CompanyInfoGateway()
	{ }

	public boolean checkExist(String companyName, String companyId)
	{
		boolean ret = false;
		String edit = (companyId.isEmpty()?"":"and vCompanyId != '"+companyId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vCompanyId from master.tbCompanyMaster where vUserName like '"+companyName+"' "+edit+"";
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
					" 0)+1 from master.tbCompanyMaster";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "C"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" User Id"); }
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
				/*String sql = "insert into master.tbCompanyMaster(vCompanyId, vCompanyName, vAddress, vPhone,"+
						" vFax, vEmail, vLicenceNo, vWebSite, vLogo, iLogo, iEmailNotification, iActive, vCreatedBy,"+
						" dCreatedDate, vModifiedBy, dModifiedDate, vVatRegNo, vSurName, iSynced, vSyncedMacId) values"+
						" (:companyId, :companyName, :address, :phone, :fax, :email, :license, :website, :logo, :ilogo,"+
						" :emailNotification, 1, :createdBy, getDate(), :modifiedBy, getDate(), :vatRegNo, :surName,"+
						" 0, '')";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("companyId", cim.getCompanyId());
				insert.setParameter("companyName", cim.getCompanyName());
				insert.setParameter("address", cim.getAddress());
				insert.setParameter("phone", cim.getPhone());
				insert.setParameter("fax", cim.getFax());
				insert.setParameter("email", cim.getEmail());
				insert.setParameter("license", cim.getLicense());
				insert.setParameter("website", cim.getWebsite());
				insert.setParameter("logo", cim.getLogo());
				insert.setParameter("ilogo", cim.getComLogo());
				insert.setParameter("createdBy", cim.getCreatedBy());
				insert.setParameter("modifiedBy", cim.getCreatedBy());
				insert.setParameter("vatRegNo", cim.getVatRegNo());
				insert.setParameter("emailNotification", cim.getEmailNotification());
				insert.setParameter("surName", cim.getSurName());
				//insert.setParameter("invoiceType", cim.getInvoiceType());
				insert.executeUpdate();*/
			}
			else if (flag.equals("Edit"))
			{
				String sql = " update master.tbCompanyMaster set vCompanyName = :companyName, vAddress = :address,"+
						" vPhone = :phone, vFax = :fax, vEmail = :email, vLicenceNo = :license, vVatRegNo = :vatRegNo,"+
						" vWebSite = :website, vModifiedBy = :modifiedBy, dModifiedDate = getDate(), vLogo = :logo,"+
						" iLogo = :ilogo, iEmailNotification = :emailNotification, iSynced = 0,"+
						" vSyncedMacId = '', vAddOnlineMenuName = :addOnlineMenu, vAddAddress = :addAddress,"+
						" vAddPhone = :addPhone, vAddFax = :addFax, vAddEmail = :addEmail, vAddLicenseNo = :addLicenseNo,"+
						" vAddVatRegNo = :addVatRegNo, vAddWebsite = :addWebsite, vAddURL = :addURL, mAddMinOrderAmount"+
						" = :addMinOrderAmount, iAddEmailNotification = :addEmailNotification, iAddStockZeroSale= :addStockZeroSale"+
						" where vCompanyId = :companyId ";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("companyName", cim.getCompanyName());
				insert.setParameter("address", cim.getAddress());
				insert.setParameter("phone", cim.getPhone());
				insert.setParameter("fax", cim.getFax());
				insert.setParameter("email", cim.getEmail());
				insert.setParameter("license", cim.getLicense());
				insert.setParameter("website", cim.getWebsite());
				insert.setParameter("logo", cim.getLogo());
				insert.setParameter("ilogo", cim.getComLogo());
				insert.setParameter("vatRegNo", cim.getVatRegNo());
				insert.setParameter("modifiedBy", cim.getCreatedBy());
				//insert.setParameter("surName", cim.getSurName());
				insert.setParameter("emailNotification", cim.getEmailNotification());
				
				insert.setParameter("addOnlineMenu", cim.getAddOnlineMenuName());
				insert.setParameter("addAddress", cim.getAddAddress());
				insert.setParameter("addPhone", cim.getAddPhone());
				insert.setParameter("addFax", cim.getAddFax());
				insert.setParameter("addEmail", cim.getAddEmail());
				insert.setParameter("addLicenseNo", cim.getAddLicenseNo());
				insert.setParameter("addVatRegNo", cim.getAddVatRegNo());
				insert.setParameter("addWebsite", cim.getAddWebsite());
				insert.setParameter("addURL", cim.getAddURL());
				insert.setParameter("addMinOrderAmount", cim.getAddMinOrderAmount());
				//insert.setParameter("invoiceType", cim.getInvoiceType());
				insert.setParameter("addEmailNotification", cim.getAddEmailNotification());
				insert.setParameter("addStockZeroSale", cim.getAddStockZeroSale());
				//where clause
				insert.setParameter("companyId", cim.getCompanyId());
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
			String sql = "update master.tbCompanyMaster set iActive = (select case when iActive = 1"+
					" then 0 else 1 end from master.tbCompanyMaster where vUserId = :user1),"+
					" vModifiedBy = :modifiedBy, dModifiedDate = CURRENT_TIMESTAMP where"+
					" vUserId = :user2";

			Query insert = session.createSQLQuery(sql);
			insert.setParameter("user1", userId);
			insert.setParameter("user2", userId);
			insert.setParameter("modifiedBy", modifiedId);
			insert.executeUpdate();

			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update user: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(CompanyInfoModel cim, String companyIdFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vCompanyName, vAddress, vPhone, vFax, vEmail, vLicenceNo, vWebSite,"+
					" vVatRegNo, vLogo, iEmailNotification, vAddOnlineMenuName, vAddAddress, vAddPhone,"+
					" vAddFax, vAddEmail, vAddLicenseNo, vAddVatRegNo, vAddWebsite, vAddURL, mAddMinOrderAmount,"+
					" iAddEmailNotification, iAddStockZeroSale from master.tbCompanyMaster where "+
					" vCompanyId = :companyId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("companyId", companyIdFind);

			for (Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				cim.setCompanyName(element[0].toString());
				cim.setAddress(element[1].toString());
				cim.setPhone(element[2].toString());
				cim.setFax(element[3].toString());
				cim.setEmail(element[4].toString());
				cim.setLicense(element[5].toString());
				cim.setWebsite(element[6].toString());
				cim.setVatRegNo(element[7].toString());
				cim.setLogo(element[8].toString());
				cim.setEmailNotification(Integer.parseInt(element[9].toString()));

				cim.setAddOnlineMenuName(element[10].toString());
				cim.setAddAddress(element[11].toString());
				cim.setAddPhone(element[12].toString());
				cim.setAddFax(element[13].toString());
				cim.setAddEmail(element[14].toString());
				cim.setAddLicenseNo(element[15].toString());
				cim.setAddVatRegNo(element[16].toString());
				cim.setAddWebsite(element[17].toString());
				cim.setAddURL(element[18].toString());
				cim.setAddMinOrderAmount(element[19].toString());
				cim.setAddEmailNotification(Integer.parseInt(element[20].toString()));
				cim.setaAddStockZeroSale(Integer.parseInt(element[21].toString()));
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}
}
