package com.example.gateway;

import java.util.Date;
import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.common.share.SessionFactoryUtil;
import com.example.model.IssueInfoModel;

public class IssueInfoGateway
{
	public IssueInfoGateway()
	{ }

	public String getIssueId(String BranchId)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vIssueId, 8, 10) as int)),"+
					" 0)+1 from trans.tbIssueInfo";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = BranchId+"IS"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getIssueNo(String Date)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull( max( cast( SUBSTRING(vIssueNo, 4, len(vIssueNo)) as int)), cast( SUBSTRING('"+Date+"', 1, 4) +"+
					" SUBSTRING('"+Date+"', 6, 2)+'000' as int)) + 1 from trans.tbIssueInfo where month(dIssueDate) = month('"+Date+"')"+
					" and year(dIssueDate) = year('"+Date+"')";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "ISU"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "IssueNo"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getStatus(String issueID)
	{
		String status = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select iStatus from trans.tbIssueInfo where vIssueId like '"+issueID+"'";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ status = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "Status"); }
		finally{ session.close(); };
		return status;
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

	public boolean insertEditData(IssueInfoModel isum, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into trans.tbIssueInfo (vBranchId, vIssueId, vIssueNo, vReferenceNo,"+
						" vBranchFrom, vBranchTo, dIssueDate, vReceivedBy, mTotalAmount, vRemarks, vStatusId,"+
						" vApprovedBy, vApproveTime, vCancelledBy, vCancelledTime, vCancelReason, vCreatedBy,"+
						" dCreatedDate, vModifiedBy, dModifiedDate, iStatus, iActive, iCostMargin,"+
						" vRequisitionId) values (:branchId, :issueId, :issueNo, :referenceNo, :branchFrom,"+
						" :branchTo, :issueDate, :receivedBy, :totalAmount, :remarks, :statusMainId, :approvedBy,"+
						" :approvedTime, :cancelledBy, :cancelledTime, :cancelReason, :createdBy, getDate(),"+
						" :modifiedBy, getDate(), :statusId, 1, :costMargin, :requisitionId)";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", isum.getBranchId());
				insert.setParameter("issueId", isum.getIssueId());
				insert.setParameter("issueNo", isum.getIssueNo());
				insert.setParameter("issueDate", isum.getIssueDate());
				insert.setParameter("remarks", isum.getRemarks());
				insert.setParameter("createdBy", isum.getCreatedBy());
				insert.setParameter("modifiedBy", isum.getCreatedBy());
				insert.setParameter("referenceNo", isum.getReferenceNo());
				insert.setParameter("statusId", isum.getStatusId());
				insert.setParameter("cancelledBy", isum.getCancelBy());
				insert.setParameter("cancelledTime", "");
				insert.setParameter("cancelReason", isum.getCancelReason());
				insert.setParameter("branchFrom", isum.getBranchFrom());
				insert.setParameter("branchTo", isum.getBranchTo());
				insert.setParameter("receivedBy", isum.getReceivedBy());
				insert.setParameter("totalAmount", isum.getTotalAmount());
				insert.setParameter("costMargin", isum.getCostMargin());
				insert.setParameter("statusMainId", isum.getStatusMainId());
				insert.setParameter("approvedBy", isum.getApproveBy());
				insert.setParameter("approvedTime", "");
				insert.setParameter("requisitionId", isum.getRequisitionId());
				insert.executeUpdate();
				if (!isum.getDetailsSql().isEmpty() && isum.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(isum.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update trans.tbIssueInfo set dIssueDate = :issueDate, vBranchFrom = :branchFrom,"+
						" vBranchTo = :branchTo, vReceivedBy = :receivedBy, mTotalAmount = :totalAmount, vRemarks = :remarks,"+
						" vModifiedBy = :modifiedBy, dModifiedDate = getDate(), vReferenceNo = :referenceNo,"+
						" iStatus = :statusId, iCostMargin = :costMargin, vRequisitionId = :requisitionId"+
						" where vIssueId = :issueId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("issueId", isum.getIssueId());
				insert.setParameter("issueDate", isum.getIssueDate());
				insert.setParameter("remarks", isum.getRemarks());
				insert.setParameter("modifiedBy", isum.getCreatedBy());
				insert.setParameter("referenceNo", isum.getReferenceNo());
				insert.setParameter("branchFrom", isum.getBranchFrom());
				insert.setParameter("branchTo", isum.getBranchTo());
				insert.setParameter("receivedBy", isum.getReceivedBy());
				insert.setParameter("totalAmount", isum.getTotalAmount());
				insert.setParameter("statusId", isum.getStatusId());
				insert.setParameter("costMargin", isum.getCostMargin());
				insert.setParameter("requisitionId", isum.getRequisitionId());
				insert.executeUpdate();

				if (!isum.getDetailsSql().isEmpty() && isum.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(isum.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert Issue: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String IssueId, String userId)
	{
		boolean ret = false;

		String sql = " update trans.tbIssueDetails set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbIssueInfo where vIssueId = '"+IssueId+"')"+
				" where vIssueId = '"+IssueId+"'";

		sql += "update trans.tbIssueInfo set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbIssueInfo where vIssueId = '"+IssueId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate() where vIssueId = '"+IssueId+"'";

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
			System.out.println("Error active Issue: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean ReceivedData(String IssueId, String userId)
	{
		boolean ret = false;

		String sql = "update trans.tbIssueInfo set iStatus = (select case when iStatus = 1"+
				" then 0 else 1 end from trans.tbIssueInfo where vIssueId = '"+IssueId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate() where vIssueId = '"+IssueId+"'";

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
			System.out.println("Error active Issue: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean IssueFromReceivedData(String IssueId, String userId)
	{
		boolean ret = false;
		String sql = "update trans.tbIssueInfo set iStatus = 1, vModifiedBy = '"+userId+"',"+
				" dModifiedDate = getdate() where vIssueId = '"+IssueId+"'";
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
			System.out.println("Error active Issue: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(IssueInfoModel isum, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select vBranchId, vIssueId, vIssueNo, dIssueDate, vRemarks, vReferenceNo, vBranchFrom,"+
					" vBranchTo, vReceivedBy, mTotalAmount, iStatus, iCostMargin, isnull(vRequisitionId,0)"+
					" vRequisitionId FROM trans.tbIssueInfo where vIssueId = :issueId ";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("issueId", idFind);
			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();

				isum.setBranchId(element[0].toString().trim());
				isum.setIssueId(element[1].toString());
				isum.setIssueNo(element[2].toString());
				isum.setIssueDate((Date) element[3]);
				isum.setRemarks(element[4].toString());
				isum.setReferenceNo(element[5].toString().trim());
				isum.setBranchFrom(element[6].toString());
				isum.setBranchTo(element[7].toString());
				isum.setReceivedBy(element[8].toString());
				//isum.setTotalAmount((Double) element[9]);
				isum.setStatusId(element[10].toString());
				isum.setCostMargin(element[11].toString());
				isum.setRequisitionId(element[12].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean getIssueUse(String IssueId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			if (!IssueId.isEmpty())
			{
				String sql = "select vIssueId from trans.tbIssueInfo where vIssueId = '"+IssueId+"'";
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
