package com.example.gateway;

import java.util.Date;
import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.PurchaseReturnModel;

public class PurchaseReturnGateway
{
	public PurchaseReturnGateway()
	{ }

	public String getReturnId(String BranchId)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull( max( cast( SUBSTRING( vReturnId, 8, 10) as int)),"+
					" 0)+1 from trans.tbPurchaseReturnInfo";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = BranchId+"PR"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+ "Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getReturnNo(String Date)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull( max( cast( SUBSTRING( vReturnNo, 4, len(vReturnNo)) as int)), cast( SUBSTRING"+
					" ('"+Date+"', 1, 4) + SUBSTRING('"+Date+"', 6, 2)+'000' as int)) + 1 from trans.tbPurchaseReturnInfo where"+
					" month(dReturnDate) = month('"+Date+"') and year(dReturnDate) = year('"+Date+"')";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "PRE"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+ "ReturnNo"); }
		finally{ session.close(); };
		return maxId;
	}

	public boolean insertData(String sql)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			session.createSQLQuery(sql).executeUpdate();
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean insertEditData(PurchaseReturnModel pim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into trans.tbPurchaseReturnInfo(vBranchId, vReturnId, vReturnNo, vPurchaseId,"+
						" dReturnDate, vReferenceNo, vRemarks, vStatusId, vApprovedBy, vApproveTime, vCancelledBy,"+
						" vCancelledTime, vCancelReason, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate)"+
						" values (:branchId, :returnId, :returnNo, :purchaseId, :returnDate, :referenceNo, :remarks,"+
						" :statusId, :approvedBy, getdate(), :cancelledBy, :cancelledTime, :cancelReason, 1,"+
						" :createdBy, getDate(), :modifiedBy, getDate())";

				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", pim.getBranchId());
				insert.setParameter("returnId", pim.getReturnId());
				insert.setParameter("returnNo", pim.getReturnNo());
				insert.setParameter("purchaseId", pim.getPurchaseId());
				insert.setParameter("returnDate", pim.getReturnDate());
				insert.setParameter("referenceNo", pim.getReferenceNo());
				insert.setParameter("remarks", pim.getRemarks());
				insert.setParameter("statusId", pim.getStatusId());
				insert.setParameter("createdBy", pim.getCreatedBy());
				insert.setParameter("modifiedBy", pim.getCreatedBy());
				insert.setParameter("approvedBy", pim.getApproveBy());
				insert.setParameter("cancelledBy", pim.getCancelBy());
				insert.setParameter("cancelledTime", "");
				insert.setParameter("cancelReason", pim.getCancelReason());
				insert.executeUpdate();

				if (!pim.getDetailsSql().isEmpty() && pim.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(pim.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update trans.tbPurchaseReturnInfo set vPurchaseId = :purchaseId, dReturnDate = :returnDate,"+
						" vSupplierId = :supplierId, vRemarks = :remarks, vModifiedBy = :modifiedBy, dModifiedDate = getDate(),"+
						" vReferenceNo = :referenceNo, vReturnType = :returnType where vReturnId = :returnId";

				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("purchaseId", pim.getPurchaseId());
				insert.setParameter("returnDate", pim.getReturnDate());
				insert.setParameter("supplierId", pim.getSupplierId());
				insert.setParameter("remarks", pim.getRemarks());
				insert.setParameter("modifiedBy", pim.getCreatedBy());
				insert.setParameter("referenceNo", pim.getReferenceNo());
				insert.setParameter("returnType", pim.getReturnType());
				insert.setParameter("remarks", pim.getRemarks());
				insert.setParameter("returnId", pim.getReturnId());
				insert.executeUpdate();

				if (!pim.getDetailsSql().isEmpty() && pim.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(pim.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert purchase: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String ItemId, String userId)
	{
		boolean ret = false;

		String sql = " update trans.tbPurchaseReturnDetails set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbPurchaseReturnInfo where vReturnId = '"+ItemId+"')"+
				" where vReturnId = '"+ItemId+"'";

		sql += "update trans.tbPurchaseReturnInfo set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbPurchaseReturnInfo where vReturnId = '"+ItemId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate() where vReturnId = '"+ItemId+"'";

		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			session.createSQLQuery(sql).executeUpdate();
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error active purchase return: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditPurchaseData(PurchaseReturnModel pim, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select vBranchId, vPurchaseId, vPurchaseNo, dPurchaseDate, vSupplierId, dDeliveryDate,"+
					" vRemarks, vReferenceNo FROM trans.tbPurchaseInfo where vPurchaseId = :purchaseId ";

			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("purchaseId", idFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();

				pim.setBranchId(element[0].toString().trim());
				pim.setPurchaseId(element[1].toString());
				pim.setPurchaseNo(element[2].toString());
				pim.setPurchaseDate((Date) element[3]);
				pim.setSupplierId(element[4].toString().trim());
				pim.setRemarks(element[6].toString());
				pim.setReferenceNo(element[7].toString().trim());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(PurchaseReturnModel pim, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select vBranchId, isnull(vPurchaseId,0)vPurchaseId, vReturnId, vReturnNo, dReturnDate, vSupplierId,"+
					" vRemarks, vReferenceNo, vReturnType, isnull((select vPurchaseNO +' ('+ convert(varchar,dPurchaseDate,105) +')'" + 
					" from trans.tbPurchaseInfo where vPurchaseId = a.vPurchaseId),'') vPurchaseNO FROM trans.tbPurchaseReturnInfo a"+
					" where vReturnId = :returnId";

			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("returnId", idFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();

				pim.setBranchId(element[0].toString().trim());
				pim.setPurchaseId(element[1].toString());
				pim.setReturnId(element[2].toString());
				pim.setReturnNo(element[3].toString());
				pim.setReturnDate((Date) element[4]);
				pim.setSupplierId(element[5].toString().trim());
				pim.setRemarks(element[6].toString());
				pim.setReferenceNo(element[7].toString().trim());
				pim.setReturnType(element[8].toString().trim());
				pim.setPurchaseNo(element[9].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}
}
