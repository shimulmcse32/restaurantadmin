package com.example.gateway;

import java.util.Date;
import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.PurchaseInfoModel;

public class PurchaseInfoGateway
{
	public PurchaseInfoGateway()
	{ }

	public String getPurchaseId(String BranchId)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull( max( cast( SUBSTRING( vPurchaseId, 7, 10) as int)),"+
					" 0)+1 from trans.tbPurchaseInfo";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = BranchId+"P"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+ "Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getPurchaseNo(String Date)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull( max( cast( SUBSTRING( vPurchaseNo, 4, len(vPurchaseNo)) as int)), cast( SUBSTRING"+
					" ('"+Date+"', 1, 4) + SUBSTRING('"+Date+"', 6, 2)+'000' as int)) + 1 from trans.tbPurchaseInfo where"+
					" month(dPurchaseDate) = month('"+Date+"') and year(dPurchaseDate) = year('"+Date+"')";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "PUR"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+"PurchaseNo"); }
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

	public boolean insertEditData(PurchaseInfoModel pim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into trans.tbPurchaseInfo (vBranchId, vPurchaseId, vPurchaseNo, vPurchaseType, vSupplierId, vOrderId,"+
						" dPurchaseDate, vReferenceNo, dDeliveryDate, vRemarks, vStatusId, vApprovedBy, vApproveTime, vCancelledBy," + 
						" vCancelledTime, vCancelReason, iInvoiceType, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate)"+
						" values (:branchId, :purchaseId, :purchaseNo, :paymentType, :supplierId, :orderId, :purchaseDate, :referenceNo,"+
						" :deliveryDate, :remarks, :statusId, :approvedBy, :approvedTime, :cancelledBy, :cancelledTime, :cancelReason, 0, 1,"+
						" :createdBy, getDate(), :modifiedBy, getDate())";

				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", pim.getBranchId());
				insert.setParameter("orderId", pim.getOrderId());
				insert.setParameter("purchaseId", pim.getPurchaseId());
				insert.setParameter("purchaseNo", pim.getPurchaseNo());
				insert.setParameter("purchaseDate", pim.getPurchaseDate());
				insert.setParameter("supplierId", pim.getSupplierId());
				insert.setParameter("deliveryDate", pim.getDeliveryDate());
				insert.setParameter("remarks", pim.getRemarks());
				insert.setParameter("createdBy", pim.getCreatedBy());
				insert.setParameter("modifiedBy", pim.getCreatedBy());
				insert.setParameter("referenceNo", pim.getReferenceNo());
				insert.setParameter("paymentType", pim.getPaymentType());
				insert.setParameter("statusId", pim.getStatusId());
				insert.setParameter("approvedBy", pim.getApproveBy());
				insert.setParameter("cancelledBy", pim.getCancelBy());
				insert.setParameter("cancelledTime", "");
				insert.setParameter("approvedTime", "");
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
				String sql = "update trans.tbPurchaseInfo set vOrderId = :orderId, dPurchaseDate = :purchaseDate, vSupplierId = :supplierId,"+
						" dDeliveryDate = :deliveryDate, vRemarks = :remarks, vModifiedBy = :modifiedBy, dModifiedDate = getDate(),"+
						" vReferenceNo =:referenceNo, vPurchaseType =:paymentType where vPurchaseId = :purchaseId";

				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("purchaseId", pim.getPurchaseId());
				insert.setParameter("orderId", pim.getOrderId());
				insert.setParameter("purchaseDate", pim.getPurchaseDate());
				insert.setParameter("supplierId", pim.getSupplierId());
				insert.setParameter("deliveryDate", pim.getDeliveryDate());
				insert.setParameter("remarks", pim.getRemarks());
				insert.setParameter("modifiedBy", pim.getCreatedBy());
				insert.setParameter("referenceNo", pim.getReferenceNo());
				insert.setParameter("paymentType", pim.getPaymentType());
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
		String sql = " update trans.tbPurchaseDetails set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbPurchaseInfo where vPurchaseId = '"+ItemId+"')"+
				" where vPurchaseId = '"+ItemId+"'";

		sql += "update trans.tbPurchaseInfo set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbPurchaseInfo where vPurchaseId = '"+ItemId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate() where vPurchaseId = '"+ItemId+"'";

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
			System.out.println("Error active purchase: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(PurchaseInfoModel pim, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select vBranchId, a.vOrderId, vPurchaseId, vPurchaseNo, dPurchaseDate, vSupplierId, dDeliveryDate,"+
					" vRemarks, vReferenceNo, vPurchaseType FROM trans.tbPurchaseInfo a where vPurchaseId = :purchaseId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("purchaseId", idFind);
			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();

				pim.setBranchId(element[0].toString());
				pim.setOrderId(element[1].toString());
				pim.setPurchaseId(element[2].toString());
				pim.setPurchaseNo(element[3].toString());
				pim.setPurchaseDate((Date) element[4]);
				pim.setSupplierId(element[5].toString());
				pim.setDeliveryDate((Date) element[6]);
				pim.setRemarks(element[7].toString());
				pim.setReferenceNo(element[8].toString());
				pim.setPaymentType(element[9].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean getPurchaseUse(String PurchaseId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			if (!PurchaseId.isEmpty())
			{
				String sql = "select vPurchaseId from trans.tbReceiptPurchaseDetails where vPurchaseId = '"+PurchaseId+"'";
				Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
				if (iter.hasNext())
				{ ret = true; }		
			}
		}
		catch (Exception e)
		{ System.out.println(e); }
		return ret;
	}

	public boolean getPurchaseUseEdit(String PurchaseId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			if (!PurchaseId.isEmpty())
			{
				String sql = "select * from trans.tbPurchaseInfo where vOrderId like (select vOrderId from trans.tbPurchaseInfo a where" + 
						" vPurchaseId like '"+PurchaseId+"' and isnull(vOrderId,'') != '') and dPurchaseDate > (select dPurchaseDate"+
						" from trans.tbPurchaseInfo where vPurchaseId like '"+PurchaseId+"' and isnull(vOrderId,'') != '')";
				//System.out.println(sql);
				Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
				if (iter.hasNext())
				{ ret = true; }		
			}
		}
		catch (Exception e)
		{ System.out.println(e); }
		return ret;
	}
}
