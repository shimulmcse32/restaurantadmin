package com.example.gateway;

import java.util.Date;
import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.RequisitionInfoModel;

public class RequisitionInfoGateway
{
	public RequisitionInfoGateway()
	{ }

	public String getRequisitionId(String BranchId)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vRequisitionId, 7, 10) as int)),"+
					" 0)+1 from trans.tbRequisitionInfo";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = BranchId+"R"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getRequisitionNo(String Date)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull( max( cast( SUBSTRING(vRequisitionNo, 4, len(vRequisitionNo)) as int)), cast( SUBSTRING('"+Date+"', 1, 4) +"+
					" SUBSTRING('"+Date+"', 6, 2)+'000' as int)) + 1 from trans.tbRequisitionInfo where"+
					" month(dRequisitionDate) = month('"+Date+"') and year(dRequisitionDate) = year('"+Date+"')";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "RQN"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "RequisitionNo"); }
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

	public boolean insertEditData(RequisitionInfoModel pom, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into trans.tbRequisitionInfo (vBranchId, vRequisitionId, vRequisitionNo, vReqBranchId,"+
						" dRequisitionDate, dDeliveryDate, vReferenceNo, vRemarks, vStatusId, vApprovedBy, vApproveTime, vCancelledBy,"+
						" vCancelledTime, vCancelReason, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate)"+
						" values (:branchId, :requisitionId, :requisitionNo, :reqBranchId, :requisitionDate, :deliveryDate,"+
						" :referenceNo, :remarks, :statusId, :approvedBy, :approvedTime, :cancelledBy, :cancelledTime, :cancelReason,"+
						" 1, :createdBy, getDate(), :modifiedBy, getDate())";

				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", pom.getBranchId());
				insert.setParameter("requisitionId", pom.getRequisitionId());
				insert.setParameter("requisitionNo", pom.getRequisitionNo());
				insert.setParameter("requisitionDate", pom.getRequisitionDate());
				insert.setParameter("reqBranchId", pom.getReqBranchId());
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
				insert.executeUpdate();

				if (!pom.getDetailsSql().isEmpty() && pom.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(pom.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update trans.tbRequisitionInfo set dRequisitionDate = :requisitionDate, vReqBranchId = :reqBranchId,"+
						" dDeliveryDate = :deliveryDate, vRemarks = :remarks, vModifiedBy = :modifiedBy,"+
						" dModifiedDate = getDate(), vReferenceNo = :referenceNo where vRequisitionId = :requisitionId";

				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("requisitionId", pom.getRequisitionId());
				insert.setParameter("requisitionDate", pom.getRequisitionDate());
				insert.setParameter("reqBranchId", pom.getReqBranchId());
				insert.setParameter("deliveryDate", pom.getDeliveryDate());
				insert.setParameter("remarks", pom.getRemarks());
				insert.setParameter("modifiedBy", pom.getCreatedBy());
				insert.setParameter("referenceNo", pom.getReferenceNo());
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
			System.out.println("Error insert Requisition: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String RequisitionId, String userId)
	{
		boolean ret = false;

		String sql = " update trans.tbRequisitionDetails set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbRequisitionInfo where vRequisitionId = '"+RequisitionId+"')"+
				" where vRequisitionId = '"+RequisitionId+"'";

		sql += "update trans.tbRequisitionInfo set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbRequisitionInfo where vRequisitionId = '"+RequisitionId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate() where vRequisitionId = '"+RequisitionId+"'";

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
			System.out.println("Error active Requisition: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean RequisitionCancel(RequisitionInfoModel pom, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String update = "";
			update = " update trans.tbRequisitionInfo set vCancelledBy = :cancelledBy,"+
					" vCancelledTime = getdate(), vCancelReason = :cancelReason, vModifiedBy = :modifiedBy,"+
					" dModifiedDate = getdate(), vStatusId = :statusId where vRequisitionId = :requisitionId";
			SQLQuery updateSql = session.createSQLQuery(update);
			updateSql.setParameter("cancelledBy", pom.getCancelBy());
			updateSql.setParameter("cancelReason", pom.getCancelReason());
			updateSql.setParameter("modifiedBy", pom.getCreatedBy());
			updateSql.setParameter("statusId", pom.getStatusId());

			//Where clause
			updateSql.setParameter("requisitionId", pom.getRequisitionId());
			updateSql.executeUpdate();			
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update/insert: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean RequisitionApprove(String reqDate, String narration, String RequisitionId, String userId,
			String branchId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sqlApprove = "update trans.tbRequisitionInfo set vStatusId = 'S6', vApprovedBy = '"+userId+"',"+
					" vApproveTime = '"+reqDate+"', vModifiedBy = '"+userId+"', dModifiedDate = getDate(),"+
					" vRemarks = '"+narration+"' where vRequisitionId like '"+RequisitionId+"'";

			SQLQuery insertMain = session.createSQLQuery(sqlApprove);
			insertMain.executeUpdate();
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update/insert: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(RequisitionInfoModel pom, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select vBranchId, vRequisitionId, vRequisitionNo, dRequisitionDate, vReqBranchId,"+
					" dDeliveryDate, vRemarks, vReferenceNo FROM trans.tbRequisitionInfo where vRequisitionId = :requisitionId ";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("requisitionId", idFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();

				pom.setBranchId(element[0].toString().trim());
				pom.setRequisitionId(element[1].toString());
				pom.setRequisitionNo(element[2].toString());
				pom.setRequisitionDate((Date) element[3]);
				pom.setReqBranchId(element[4].toString().trim());
				pom.setDeliveryDate((Date) element[5]);
				pom.setRemarks(element[6].toString());
				pom.setReferenceNo(element[7].toString().trim());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}
}
