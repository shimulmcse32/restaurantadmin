package com.example.gateway;

import java.util.Date;
import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.StockAdjustmentModel;

public class StockAdjustmentGateway
{
	public StockAdjustmentGateway()
	{ }

	public String getAdjustId(String BranchId)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vAdjustId, 8, 10) as int)),"+
					" 0)+1 from trans.tbStockAdjustmentInfo";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = BranchId+"SA"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getAdjustNo(String Date)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull( max( cast( SUBSTRING(vAdjustNo, 4, len(vAdjustNo)) as int)), cast( SUBSTRING('"+Date+"', 1, 4) +"+
					" SUBSTRING('"+Date+"', 6, 2)+'000' as int)) + 1 from trans.tbStockAdjustmentInfo where month(dAdjustDate) = month('"+Date+"')"+
					" and year(dAdjustDate) = year('"+Date+"')";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "STA"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "AdjustNo"); }
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

	public boolean insertEditData(StockAdjustmentModel sam, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into trans.tbStockAdjustmentInfo (vBranchId, vAdjustId, vAdjustNo, dAdjustDate,"+
						" vReferenceNo, vRemarks, vStatusId, vApprovedBy, vApproveTime, vCancelledBy, vCancelledTime,"+
						" vCancelReason, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate)"+
						" values (:branchId, :adjustId, :adjustNo, :adjustDate, :referenceNo, :remarks, :statusId,"+
						" :approvedBy, :approvedTime, :cancelledBy, :cancelledTime, :cancelReason, 1, :createdBy,"+
						" getDate(), :modifiedBy, getDate())";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", sam.getBranchId());
				insert.setParameter("adjustId", sam.getAdjustId());
				insert.setParameter("adjustNo", sam.getAdjustNo());
				insert.setParameter("adjustDate", sam.getAdjustDate());
				insert.setParameter("remarks", sam.getRemarks());
				insert.setParameter("createdBy", sam.getCreatedBy());
				insert.setParameter("modifiedBy", sam.getCreatedBy());
				insert.setParameter("referenceNo", sam.getReferenceNo());
				insert.setParameter("statusId", sam.getStatusId());
				insert.setParameter("approvedBy", sam.getApproveBy());
				insert.setParameter("cancelledBy", sam.getCancelBy());
				insert.setParameter("cancelledTime", "");
				insert.setParameter("approvedTime", "");
				insert.setParameter("cancelReason", sam.getCancelReason());
				insert.executeUpdate();

				if (!sam.getDetailsSql().isEmpty() && sam.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(sam.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update trans.tbStockAdjustmentInfo set dAdjustDate = :adjustDate,"+
						" vRemarks = :remarks, vModifiedBy = :modifiedBy, dModifiedDate = getDate(),"+
						" vReferenceNo = :referenceNo where vAdjustId = :adjustId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("adjustId", sam.getAdjustId());
				insert.setParameter("adjustDate", sam.getAdjustDate());
				insert.setParameter("remarks", sam.getRemarks());
				insert.setParameter("modifiedBy", sam.getCreatedBy());
				insert.setParameter("referenceNo", sam.getReferenceNo());
				insert.executeUpdate();

				if (!sam.getDetailsSql().isEmpty() && sam.getDetailsSql() != null &&
						sam.getDetailsChange())
				{
					SQLQuery insertUnit = session.createSQLQuery(sam.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert stock adjustment: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String adjustId, String userId)
	{
		boolean ret = false;

		String sql = " update trans.tbStockAdjustmentDetails set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbStockAdjustmentInfo where vAdjustId = '"+adjustId+"')"+
				" where vAdjustId = '"+adjustId+"'";

		sql += "update trans.tbStockAdjustmentInfo set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from trans.tbStockAdjustmentInfo where vAdjustId = '"+adjustId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate() where vAdjustId = '"+adjustId+"'";

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
			System.out.println("Error active stock adjustment: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean StockAdjustmentCancel(StockAdjustmentModel sam, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String update = "";
			update = " update trans.tbStockAdjustmentInfo set vCancelledBy = :cancelledBy,"+
					" vCancelledTime = getdate(), vCancelReason = :cancelReason, vModifiedBy = :modifiedBy,"+
					" dModifiedDate = getdate(), vStatusId = :statusId where vAdjustId = :adjustId";
			SQLQuery updateSql = session.createSQLQuery(update);
			updateSql.setParameter("cancelledBy", sam.getCancelBy());
			updateSql.setParameter("cancelReason", sam.getCancelReason());
			updateSql.setParameter("modifiedBy", sam.getCreatedBy());
			updateSql.setParameter("statusId", sam.getStatusId());

			//Where clause
			updateSql.setParameter("adjustId", sam.getAdjustId());
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

	public boolean StockAdjustmentApprove(String purDate, String narration, String adjustId, String userId,
			String branchId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sqlApprove = "update trans.tbStockAdjustmentInfo set vStatusId = 'S6', vApprovedBy = '"+userId+"',"+
					" vApproveTime = '"+purDate+"', vModifiedBy = '"+userId+"', dModifiedDate = getDate(),"+
					" vRemarks = '"+narration+"' where vAdjustId like '"+adjustId+"'";

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

	public boolean selectEditData(StockAdjustmentModel sam, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select vBranchId, vAdjustId, vAdjustNo, dAdjustDate, vRemarks, vReferenceNo"+
					" FROM trans.tbStockAdjustmentInfo where vAdjustId = :adjustId ";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("adjustId", idFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();

				sam.setBranchId(element[0].toString().trim());
				sam.setAdjustId(element[1].toString());
				sam.setAdjustNo(element[2].toString());
				sam.setAdjustDate((Date) element[3]);
				sam.setRemarks(element[4].toString());
				sam.setReferenceNo(element[5].toString().trim());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean getAdjustUse(String adjustId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			if (!adjustId.isEmpty())
			{
				String sql = "select vAdjustId from trans.tbStockAdjustmentInfo where vAdjustId = '"+adjustId+"'";
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
