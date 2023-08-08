package com.example.gateway;

import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.IssueReturnModel;

public class IssueReturnGateway
{
	public IssueReturnGateway()
	{ }

	public String getIssueReturnId(String BranchId)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vIssueReturnId, 8, 10) as int)),"+
					" 0)+1 from trans.tbIssueReturnInfo";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = BranchId+"IR"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getIssueReturnNo(String Date)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull( max( cast( SUBSTRING(vIssueReturnNo, 4, len(vIssueReturnNo)) as int)),"+
					" cast( SUBSTRING('"+Date+"', 1, 4) + SUBSTRING('"+Date+"', 6, 2)+'000' as int)) + 1 from"+
					" trans.tbIssueReturnInfo where month(dReturnDate) = month('"+Date+"') and year(dReturnDate)"+
					" = year('"+Date+"')";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "ISR"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "IssueReturnNo"); }
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

	public boolean insertEditData(IssueReturnModel irm, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into trans.tbIssueReturnInfo(vBranchId, vIssueReturnId, vIssueReturnNo, vIssueId, dReturnDate,"+
						" vReturnFrom, vReturnTo, mTotalAmount, iActive, vStatusId, vApprovedBy, vApproveTime, vCancelledBy,"+
						" vCancelledTime, vCancelReason, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate) values (:branchId,"+
						" :issueReturnId, :issueReturnNo, :issueId, :returnDate, :returnFrom, :returnTo, :totalAmount, 1, :statusId,"+
						" :approvedBy, :approveTime, :cancelledBy, :cancelledTime, :cancelReason, :createdBy, getdate(), :modifiedBy, getdate())";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", irm.getBranchId());
				insert.setParameter("issueReturnId", irm.getIssueReturnId());
				insert.setParameter("issueReturnNo", irm.getIssueReturnNo());
				insert.setParameter("issueId", irm.getIssueId());
				insert.setParameter("returnDate", irm.getIssueReturnDate());
				insert.setParameter("returnFrom", irm.getBranchFrom());
				insert.setParameter("returnTo", irm.getBranchTo());
				insert.setParameter("totalAmount", irm.getTotalAmount());
				insert.setParameter("statusId", irm.getStatusId());
				insert.setParameter("approvedBy", irm.getApproveBy());
				insert.setParameter("approveTime", "");
				insert.setParameter("cancelledBy", irm.getCancelBy());
				insert.setParameter("cancelledTime", "");
				insert.setParameter("cancelReason", irm.getCancelReason());
				insert.setParameter("createdBy", irm.getCreatedBy());
				insert.setParameter("modifiedBy", irm.getCreatedBy());

				insert.executeUpdate();
				//System.out.println(sql);
				if (!irm.getDetailsSql().isEmpty() && irm.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(irm.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update trans.tbIssueReturnInfo set dReturnDate = :issueReturnDate, vReturnFrom = :branchFrom,"+
						" vReturnTo = :branchTo, vIssueId = :issueId, mTotalAmount = :totalAmount, vModifiedBy = :modifiedBy,"+
						" dModifiedDate = getDate() where vIssueReturnId = :issueReturnId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("issueReturnDate", irm.getIssueReturnDate());
				insert.setParameter("branchFrom", irm.getBranchFrom());
				insert.setParameter("branchTo", irm.getBranchTo());
				insert.setParameter("issueId", irm.getIssueId());
				insert.setParameter("totalAmount", irm.getTotalAmount());
				insert.setParameter("modifiedBy", irm.getCreatedBy());
				insert.setParameter("issueReturnId", irm.getIssueReturnId());
				insert.executeUpdate();
				if (!irm.getDetailsSql().isEmpty() && irm.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(irm.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert return: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String IssueReturnId, String userId)
	{
		boolean ret = false;
		String sql = " update trans.tbIssueReturnDetails set iActive = (select case when iActive = 1 then 0"+
				" else 1 end from trans.tbIssueReturnInfo where vIssueReturnId = '"+IssueReturnId+"')"+
				" where vIssueReturnId = '"+IssueReturnId+"'";

		sql += "update trans.tbIssueReturnInfo set iActive = (select case when iActive = 1 then 0 else 1 end"+
				" from trans.tbIssueReturnInfo where vIssueReturnId = '"+IssueReturnId+"'), vModifiedBy ="+
				" '"+userId+"', dModifiedDate = getdate() where vIssueReturnId = '"+IssueReturnId+"'";
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

	public boolean getIssueUse(String issueId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			if (!issueId.isEmpty())
			{
				String sql = "select vIssueId from trans.tbIssueInfo where vIssueId = '"+issueId+"' and iStatus = 1";
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
