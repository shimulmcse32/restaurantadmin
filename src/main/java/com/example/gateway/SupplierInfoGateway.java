package com.example.gateway;

import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.SupplierInfoModel;

public class SupplierInfoGateway
{
	public SupplierInfoGateway()
	{ }

	public boolean checkExist(String supplierName, String supplierId)
	{
		boolean ret = false;
		String edit = (supplierId.isEmpty()?"":"and vSupplierId != '"+supplierId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vSupplierId from master.tbSupplierMaster"+
					" where vSupplierName like '"+supplierName+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean checkExistCust(String customerName, String customerId)
	{
		boolean ret = false;
		String edit = (customerId.isEmpty()?"":"and vCustomerId != '"+customerId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vCustomerId from master.tbCustomerInfo"+
					" where vCustomerName like '"+customerName+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist"); }
		finally{ session.close(); }
		return ret;
	}

	public String getSupplierId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vSupplierId, 2, 10) as int)),"+
					" 0)+1 from master.tbSupplierMaster";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "S"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" supplierId"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getCustomerId(String branchId)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vCustomerId, 7, 10) as int)),"+
					" 0)+1 from master.tbCustomerInfo where vBranchId = '"+branchId+"'";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = branchId+"W"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" branchId"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getSupCode()
	{
		String code = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(dbo.funGetNumeric(vSupplierCode) as int)),"+
					" 0)+1 from master.tbSupplierMaster";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ code = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Sup Code"); }
		finally{ session.close(); };
		return code;
	}

	public String getCustCode()
	{
		String code = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(dbo.funGetNumeric(vCustomerCode) as int)),"+
					" 0)+1 from master.tbCustomerInfo";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ code = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Cust Code"); }
		finally{ session.close(); };
		return code;
	}

	public boolean checkExistCodeCust(String custCode, String custId)
	{
		boolean ret = false;
		String edit = (custId.isEmpty()?"":"and vCustomerId != '"+custId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			if (!custCode.isEmpty())
			{
				String sql = "select vCustomerCode from master.tbCustomerInfo"+
						" where vCustomerCode like '"+custCode+"' "+edit+"";
				//System.out.println(sql);
				Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
				if (iter.hasNext())
				{ ret = true; }
			}
		}
		catch (Exception e)
		{
			System.out.println(e+" Check exist");
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean checkExistCode(String supCode, String supId)
	{
		boolean ret = false;
		String edit = (supId.isEmpty()?"":"and vSupplierId != '"+supId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			if (!supCode.isEmpty())
			{
				String sql = "select vSupplierCode from master.tbSupplierMaster"+
						" where vSupplierCode like '"+supCode+"' "+edit+"";
				//System.out.println(sql);
				Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
				if (iter.hasNext())
				{ ret = true; }
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean insertEditData(SupplierInfoModel sim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbSupplierMaster(vBranchId, vLedgerId, vSupplierId, vSupplierName,"+
						" vAddress, vPhone, vFax, vEmail, vLicenceNo, vVatRegNo, vContactPerson, vContactMobile,"+
						" vContactEmail, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate, vSupplierCode,"+
						" iCreditLimit, mDiscount, iSynced, vSyncedMacId) values (:branchId, '', :supplierId,"+
						" :supplierName, :address, :phone, :fax, :email, :license, :vatRegNo, '', '', '', 1, :createdBy,"+
						" getDate(), :modifiedBy, getDate(), :supplierCode, :creditLimit, :discount, 0, '')";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", sim.getBranchId());
				insert.setParameter("supplierId", sim.getSupplierId());
				insert.setParameter("supplierName", sim.getSupplierName());
				insert.setParameter("address", sim.getAddress());
				insert.setParameter("phone", sim.getPhone());
				insert.setParameter("fax", sim.getFax());
				insert.setParameter("email", sim.getEmail());
				insert.setParameter("license", sim.getLicense());
				insert.setParameter("vatRegNo", sim.getVatRegNo());
				insert.setParameter("createdBy", sim.getCreatedBy());
				insert.setParameter("modifiedBy", sim.getCreatedBy());
				insert.setParameter("supplierCode", sim.getSupplierCode());
				insert.setParameter("creditLimit", sim.getCreditLimit());
				insert.setParameter("discount", sim.getDiscount());
				insert.executeUpdate();
			}
			else if (flag.equals("Edit"))
			{
				String sql = " update master.tbSupplierMaster set vSupplierName = :supplierName, vAddress = :address,"+
						" vPhone = :phone, vFax = :fax, vEmail = :email, vLicenceNo = :license, vVatRegNo = :vatRegNo,"+
						" iCreditLimit = :creditLimit, mDiscount = :discount, vModifiedBy = :modifiedBy, dModifiedDate"+
						" = getDate(), vSupplierCode = :supplierCode, iSynced = 0, vSyncedMacId = '' where vSupplierId = :supplierId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("supplierName", sim.getSupplierName());
				insert.setParameter("address", sim.getAddress());
				insert.setParameter("phone", sim.getPhone());
				insert.setParameter("fax", sim.getFax());
				insert.setParameter("email", sim.getEmail());
				insert.setParameter("license", sim.getLicense());
				insert.setParameter("vatRegNo", sim.getVatRegNo());
				insert.setParameter("modifiedBy", sim.getCreatedBy());
				insert.setParameter("supplierCode", sim.getSupplierCode());
				insert.setParameter("creditLimit", sim.getCreditLimit());
				insert.setParameter("discount", sim.getDiscount());
				//where clause
				insert.setParameter("supplierId", sim.getSupplierId());
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

	public boolean insertEditDataCust(SupplierInfoModel sim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbCustomerInfo(vBranchId, vCustomerId, vCustomerCode, vCustomerName,"+
						" vVatRegNo, vMobileNo, vEmailId, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate,"+
						" iCreditLimit, iSynced, vSyncedMacId) values (:branchId, :customerId, :customerCode, :customerName,"+
						" :vatRegNo, :mobileNo, :email, 1, :createdBy, getDate(), :modifiedBy, getDate(), :creditLimit, 0, '')";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", sim.getBranchId());
				insert.setParameter("customerId", sim.getSupplierId());
				insert.setParameter("customerCode", sim.getSupplierCode());
				insert.setParameter("customerName", sim.getSupplierName());
				insert.setParameter("vatRegNo", sim.getVatRegNo());
				insert.setParameter("mobileNo", sim.getPhone());
				insert.setParameter("email", sim.getEmail());
				insert.setParameter("createdBy", sim.getCreatedBy());
				insert.setParameter("modifiedBy", sim.getCreatedBy());
				insert.setParameter("creditLimit", sim.getCreditLimit());
				insert.executeUpdate();

				if (!sim.getDetailsSql().isEmpty() && sim.getDetailsSql() != null)
				{
					SQLQuery insertDetails = session.createSQLQuery(sim.getDetailsSql());
					insertDetails.executeUpdate();
				}
			}
			else if (flag.equals("Edit"))
			{
				String sql = " update master.tbCustomerInfo set vCustomerCode = :customerCode, vCustomerName = :customerName,"+
						" vMobileNo = :mobileNo, vEmailId = :email, iCreditLimit = :creditLimit, vModifiedBy = :modifiedBy,"+
						" dModifiedDate = getDate(), vVatRegNo = :vatRegNo, iSynced = 0, vSyncedMacId = '' where"+
						" vCustomerId = :customerId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("customerCode", sim.getSupplierCode());
				insert.setParameter("customerName", sim.getSupplierName());
				insert.setParameter("mobileNo", sim.getPhone());
				insert.setParameter("email", sim.getEmail());
				insert.setParameter("creditLimit", sim.getCreditLimit());
				insert.setParameter("modifiedBy", sim.getCreatedBy());
				insert.setParameter("vatRegNo", sim.getVatRegNo());
				//where clause
				insert.setParameter("customerId", sim.getSupplierId());
				insert.executeUpdate();

				if (!sim.getDetailsSql().isEmpty() && sim.getDetailsSql() != null)
				{
					SQLQuery insertDetails = session.createSQLQuery(sim.getDetailsSql());
					insertDetails.executeUpdate();
				}
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

	public boolean activeInactiveSupplier(String supId, String modifiedId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sql = "update master.tbSupplierMaster set iActive = (select case when iActive = 1"+
					" then 0 else 1 end from master.tbSupplierMaster where vSupplierId = :para1),"+
					" vModifiedBy = :modifiedBy, dModifiedDate = CURRENT_TIMESTAMP, iSynced = 0,"+
					" vSyncedMacId = '' where vSupplierId = :para2";

			Query insert = session.createSQLQuery(sql);
			insert.setParameter("para1", supId);
			insert.setParameter("para2", supId);
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

	public boolean activeInactiveCustomer(String cusId, String modifiedId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sql = "update master.tbCustomerInfo set iActive = (select case when iActive = 1"+
					" then 0 else 1 end from master.tbCustomerInfo where vCustomerId = :para1),"+
					" vModifiedBy = :modifiedBy, dModifiedDate = getdate(), iSynced = 0,"+
					" vSyncedMacId = '' where vCustomerId = :para2";

			Query insert = session.createSQLQuery(sql);
			insert.setParameter("para1", cusId);
			insert.setParameter("para2", cusId);
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

	public boolean selectEditData(SupplierInfoModel sim, String supplierIdFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vSupplierName, vAddress, vPhone, vFax, vEmail, vLicenceNo, vVatRegNo, vSupplierCode,"+
					" iCreditLimit, mDiscount from master.tbSupplierMaster where vSupplierId = :supplierId";
			//System.out.println(sql);
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("supplierId", supplierIdFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				sim.setSupplierName(element[0].toString());
				sim.setAddress(element[1].toString());
				sim.setPhone(element[2].toString());
				sim.setFax(element[3].toString());
				sim.setEmail(element[4].toString());
				sim.setLicense(element[5].toString());
				sim.setVatRegNo(element[6].toString());
				sim.setSupplierCode(element[7].toString());
				sim.setCreditLimit(element[8].toString());
				sim.setDiscount(Double.parseDouble(element[9].toString()));
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}
}
