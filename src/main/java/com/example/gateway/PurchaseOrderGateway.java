package com.example.gateway;

import java.util.Date;
import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.PurchaseOrderInfoModel;

public class PurchaseOrderGateway
{
	public PurchaseOrderGateway()
	{ }

	public String getOrderId(String BranchId)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vOrderId, 7, 10) as int)),"+
					" 0)+1 from trans.tbPurchaseOrderInfo";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = BranchId+"O"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getOrderNo(String Date)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vOrderNo, 4, len(vOrderNo)) as int)), cast(SUBSTRING('"+Date+"', 1, 4) +"+
					" SUBSTRING('"+Date+"', 6, 2)+'000' as int)) + 1 from trans.tbPurchaseOrderInfo where"+
					" month(dOrderDate) = month('"+Date+"') and year(dOrderDate) = year('"+Date+"')";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "ORN"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "OrderNo"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getRevisionNo(String OrderId)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(vRevisionNo), 0)+1"+
					" from trans.tbPurchaseOrderInfo where vOrderId like '"+OrderId+"'";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "Id"); }
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
			System.out.println("Error insert: "+ exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean insertEditData(PurchaseOrderInfoModel pom, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into trans.tbPurchaseOrderInfo (vBranchId, vOrderId, vOrderNo, vRevisionNo, vOrderType,"+
						" vSupplierId, dOrderDate, dDeliveryDate, vReferenceNo, vRemarks, vStatusId, vApprovedBy, vApproveTime,"+
						" vCancelledBy, vCancelledTime, vCancelReason, iInvoiceType, iActive, vCreatedBy, dCreatedDate, vModifiedBy,"+
						" dModifiedDate, vRequisitionId, vReqBranchId) values (:branchId, :orderId, :orderNo, :revisionNo,"+
						" :orderType, :supplierId, :orderDate, :deliveryDate, :referenceNo, :remarks, :statusId, :approvedBy,"+
						" :approvedTime, :cancelledBy, :cancelledTime, :cancelReason, 0, 1, :createdBy, getDate(), :modifiedBy,"+
						" getDate(), :requisitionId, :reqBranchId)";

				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", pom.getBranchId());
				insert.setParameter("orderId", pom.getOrderId());
				insert.setParameter("orderNo", pom.getOrderNo());
				insert.setParameter("revisionNo", pom.getRevisionId());
				insert.setParameter("orderType", pom.getOrderType());
				insert.setParameter("orderDate", pom.getOrderDate());
				insert.setParameter("supplierId", pom.getSupplierId());
				insert.setParameter("deliveryDate", pom.getDeliveryDate());
				insert.setParameter("remarks", pom.getRemarks());
				insert.setParameter("createdBy", pom.getCreatedBy());
				insert.setParameter("modifiedBy", pom.getCreatedBy());
				insert.setParameter("referenceNo", pom.getReferenceNo());
				insert.setParameter("statusId", pom.getStatusId());
				insert.setParameter("approvedBy", pom.getApproveBy());
				insert.setParameter("cancelledBy", pom.getCancelBy());
				insert.setParameter("cancelledTime", "");
				insert.setParameter("approvedTime", "");
				insert.setParameter("cancelReason", pom.getCancelReason());
				insert.setParameter("requisitionId", pom.getRequisitionId());
				insert.setParameter("reqBranchId", pom.getReqBranchId());
				insert.executeUpdate();

				if (!pom.getDetailsSql().isEmpty() && pom.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(pom.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update trans.tbPurchaseOrderInfo set dOrderDate = :orderDate, vSupplierId = :supplierId,"+
						" dDeliveryDate = :deliveryDate, vRemarks = :remarks, vModifiedBy = :modifiedBy,"+
						" dModifiedDate = getDate(), vReferenceNo = :referenceNo, vRequisitionId = :requisitionId,"+
						" vReqBranchId = :reqBranchId where vOrderId = :orderId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("orderId", pom.getOrderId());
				insert.setParameter("orderDate", pom.getOrderDate());
				insert.setParameter("supplierId", pom.getSupplierId());
				insert.setParameter("deliveryDate", pom.getDeliveryDate());
				insert.setParameter("remarks", pom.getRemarks());
				insert.setParameter("modifiedBy", pom.getCreatedBy());
				insert.setParameter("referenceNo", pom.getReferenceNo());
				insert.setParameter("requisitionId", pom.getRequisitionId());
				insert.setParameter("reqBranchId", pom.getReqBranchId());
				insert.executeUpdate();

				if (!pom.getDetailsSql().isEmpty() && pom.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(pom.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert purchase order: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String orderId, String userId)
	{
		boolean ret = false;

		String sql = " update trans.tbPurchaseOrderDetails set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbPurchaseOrderInfo where vOrderId = '"+orderId+"')"+
				" where vOrderId = '"+orderId+"'";

		sql += "update trans.tbPurchaseOrderInfo set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbPurchaseOrderInfo where vOrderId = '"+orderId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate() where vOrderId = '"+orderId+"'";

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
			System.out.println("Error active purchase order: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(PurchaseOrderInfoModel pom, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select vBranchId, vOrderId, vOrderNo, vRevisionNo, dOrderDate, vSupplierId, vOrderType,"+
					" dDeliveryDate, vRemarks, vReferenceNo, vRequisitionId, vReqBranchId FROM trans.tbPurchaseOrderInfo"+
					" where vOrderId = :orderId ";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("orderId", idFind);
			//System.out.println(sql+idFind);
			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();

				pom.setBranchId(element[0].toString().trim());
				pom.setOrderId(element[1].toString());
				pom.setOrderNo(element[2].toString());
				pom.setRevisionId(element[3].toString());
				pom.setOrderDate((Date) element[4]);
				pom.setSupplierId(element[5].toString().trim());
				pom.setOrderType(element[6].toString().trim());
				pom.setDeliveryDate((Date) element[7]);
				pom.setRemarks(element[8].toString());
				pom.setReferenceNo(element[9].toString().trim());
				pom.setRequisitionId(element[10].toString().trim());
				pom.setReqBranchId(element[11].toString().trim());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean getOrderUse(String OrderId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			if (!OrderId.isEmpty())
			{
				String sql = "select vOrderId from trans.tbPurchaseInfo where vOrderId = '"+OrderId+"'";
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
