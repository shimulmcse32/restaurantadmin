package com.example.gateway;

import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.OnlineMenuInfoModel;

public class OnlineMenuInfoGateway
{
	public OnlineMenuInfoGateway()
	{ }

	public boolean checkExist(String onlineMenuName, String onlineMenuId)
	{
		boolean ret = false;
		String edit = (onlineMenuId.isEmpty()?"":"and vOnlineMenuId != '"+onlineMenuId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vOnlineMenuId from master.tbOnlineMenuMaster where vOnlineMenuName like '"+onlineMenuName+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist Online Menu"); }
		finally{ session.close(); }
		return ret;
	}

	public String getOnlineMenuId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vOnlineMenuId, 2, 10) as int)),"+
					" 0)+1 from master.tbOnlineMenuMaster";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "C"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Online Menu Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public boolean insertEditData(OnlineMenuInfoModel omim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbOnlineMenuMaster(vOnlineMenuId, vOnlineMenuName, vAddress,"+
						" vPhone, vFax, vEmail, vLicenceNo, vWebSite, vURL, mMiniOrderAmt, vLogo, iLogo, iEmailNotification, iActive,"+
						" vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate, vVatRegNo, vSurName, iStockZeroSale) values (:onlineMenuId,"+
						" :onlineMenuName, :address, :phone, :fax, :email, :license, :website, :url, :miniOrderAmt, :logo, :ilogo,"+
						" :emailNotification, 1, :createdBy, getDate(), :modifiedBy, getDate(), :vatRegNo, :surName, :stockZeroSale)";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("onlineMenuId", omim.getOnlineMenuId());
				insert.setParameter("onlineMenuName", omim.getOnlineMenuName());
				insert.setParameter("address", omim.getAddress());
				insert.setParameter("phone", omim.getPhone());
				insert.setParameter("fax", omim.getFax());
				insert.setParameter("email", omim.getEmail());
				insert.setParameter("license", omim.getLicense());
				insert.setParameter("website", omim.getWebsite());
				insert.setParameter("url", omim.getURL());
				insert.setParameter("miniOrderAmt", omim.getMiniOrderAmt());
				insert.setParameter("logo", omim.getLogo());
				insert.setParameter("ilogo", omim.getOnlineMenuLogo());
				insert.setParameter("createdBy", omim.getCreatedBy());
				insert.setParameter("modifiedBy", omim.getCreatedBy());
				insert.setParameter("vatRegNo", omim.getVatRegNo());
				insert.setParameter("emailNotification", omim.getEmailNotification());
				insert.setParameter("surName", omim.getSurName());
				insert.setParameter("stockZeroSale", omim.getStockZeroSale());
				
				//insert.setParameter("invoiceType", omim.getInvoiceType());
				insert.executeUpdate();
			}
			else if (flag.equals("Edit"))
			{
				String sql = " update master.tbOnlineMenuMaster set vOnlineMenuName = :onlineMenuName, vAddress = :address,"+
						" vPhone = :phone, vFax = :fax, vEmail = :email, vLicenceNo = :license, vVatRegNo = :vatRegNo,"+
						" vWebSite = :website, vURL = :url, mMiniOrderAmt = :miniOrderAmt, vModifiedBy = :modifiedBy,"+
						" dModifiedDate = getDate(), vLogo = :logo, vSurName = :surName, iLogo = :ilogo,"+
						" iEmailNotification = :emailNotification, iStockZeroSale = :stockZeroSale"+
						" where vOnlineMenuId = :onlineMenuId ";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("onlineMenuName", omim.getOnlineMenuName());
				insert.setParameter("address", omim.getAddress());
				insert.setParameter("phone", omim.getPhone());
				insert.setParameter("fax", omim.getFax());
				insert.setParameter("email", omim.getEmail());
				insert.setParameter("license", omim.getLicense());
				insert.setParameter("website", omim.getWebsite());
				insert.setParameter("miniOrderAmt", omim.getMiniOrderAmt());
				insert.setParameter("logo", omim.getLogo());
				insert.setParameter("ilogo", omim.getOnlineMenuLogo());
				insert.setParameter("vatRegNo", omim.getVatRegNo());
				insert.setParameter("modifiedBy", omim.getCreatedBy());
				insert.setParameter("surName", omim.getSurName());
				insert.setParameter("emailNotification", omim.getEmailNotification());
				insert.setParameter("url", omim.getURL());
				insert.setParameter("stockZeroSale", omim.getStockZeroSale());
				//insert.setParameter("invoiceType", omim.getInvoiceType());
				//where clause
				insert.setParameter("onlineMenuId", omim.getOnlineMenuId());
				insert.executeUpdate();
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert OnlineMenu: "+exp);
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
			String sql = "update master.tbOnlineMenuMaster set iActive = (select case when iActive = 1"+
					" then 0 else 1 end from master.tbOnlineMenuMaster where vUserId = :user1),"+
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

	public boolean selectEditData(OnlineMenuInfoModel omim, String OnlineMenuIdFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vOnlineMenuName, vAddress, vPhone, vFax, vEmail, vLicenceNo, vWebSite, vURL, mMiniOrderAmt,"+
					" vVatRegNo, vLogo, iEmailNotification, iStockZeroSale from master.tbOnlineMenuMaster where vOnlineMenuId = :onlineMenuId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("onlineMenuId", OnlineMenuIdFind);
			System.out.println(sql+" Select edit data"+OnlineMenuIdFind);
			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				omim.setOnlineMenuName(element[0].toString());
				omim.setAddress(element[1].toString());
				omim.setPhone(element[2].toString());
				omim.setFax(element[3].toString());
				omim.setEmail(element[4].toString());
				omim.setLicense(element[5].toString());
				omim.setWebsite(element[6].toString());
				omim.setURL(element[7].toString());
				
				omim.setMiniOrderAmt(Double.parseDouble(element[8].toString()));
				omim.setVatRegNo(element[9].toString());
				omim.setLogo(element[10].toString());
				omim.setEmailNotification(Integer.parseInt(element[11].toString()));
				omim.setStockZeroSale(Integer.parseInt(element[12].toString()));
				System.out.println(sql+" Select edit data");
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}
}
