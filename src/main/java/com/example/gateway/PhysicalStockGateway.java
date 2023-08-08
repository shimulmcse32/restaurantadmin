package com.example.gateway;

import java.util.Date;
import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.PhysicalStockModel;

public class PhysicalStockGateway
{
	public PhysicalStockGateway()
	{ }

	public String getPhysicalStockId(String BranchId)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vPhysicalStockId, 8, 10) as int)),"+
					" 0)+1 from trans.tbPhysicalStockInfo";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = BranchId+"PS"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getPhysicalStockNo(String Date)
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull( max( cast( SUBSTRING(vPhysicalStockNo, 4, len(vPhysicalStockNo)) as int)), cast( SUBSTRING('"+Date+"', 1, 4) +"+
					" SUBSTRING('"+Date+"', 6, 2)+'000' as int)) + 1 from trans.tbPhysicalStockInfo where month(dPhysicalStockDate) = month('"+Date+"')"+
					" and year(dPhysicalStockDate) = year('"+Date+"')";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "PST"+ iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex + "PhysicalStockNo"); }
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

	public boolean insertEditData(PhysicalStockModel psm, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into trans.tbPhysicalStockInfo (vBranchId, vPhysicalStockId, vPhysicalStockNo,"+
						" dPhysicalStockDate, vReferenceNo, vRemarks, vStatusId, vApprovedBy, vApproveTime, vCancelledBy,"+
						" vCancelledTime, vCancelReason, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate)"+
						" values (:branchId, :physicalStockId, :physicalStockNo, :physicalStockDate, :referenceNo, :remarks,"+
						" :statusId, :approvedBy, :approvedTime, :cancelledBy, :cancelledTime, :cancelReason, 1, :createdBy,"+
						" getDate(), :modifiedBy, getDate())";

				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", psm.getBranchId());
				insert.setParameter("physicalStockId", psm.getPhysicalStockId());
				insert.setParameter("physicalStockNo", psm.getPhysicalStockNo());
				insert.setParameter("physicalStockDate", psm.getPhysicalStockDate());
				insert.setParameter("remarks", psm.getRemarks());
				insert.setParameter("createdBy", psm.getCreatedBy());
				insert.setParameter("modifiedBy", psm.getCreatedBy());
				insert.setParameter("referenceNo", psm.getReferenceNo());
				insert.setParameter("statusId", psm.getStatusId());
				insert.setParameter("approvedBy", psm.getApproveBy());
				insert.setParameter("cancelledBy", psm.getCancelBy());
				insert.setParameter("cancelledTime", "");
				insert.setParameter("approvedTime", "");
				insert.setParameter("cancelReason", psm.getCancelReason());
				insert.executeUpdate();

				if (!psm.getDetailsSql().isEmpty() && psm.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(psm.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update trans.tbPhysicalStockInfo set dPhysicalStockDate = :physicalStockDate,"+
						" vRemarks = :remarks, vModifiedBy = :modifiedBy, dModifiedDate = getDate(),"+
						" vReferenceNo = :referenceNo where vPhysicalStockId = :physicalStockId";

				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("physicalStockId", psm.getPhysicalStockId());
				insert.setParameter("physicalStockDate", psm.getPhysicalStockDate());
				insert.setParameter("remarks", psm.getRemarks());
				insert.setParameter("modifiedBy", psm.getCreatedBy());
				insert.setParameter("referenceNo", psm.getReferenceNo());
				insert.executeUpdate();

				if (!psm.getDetailsSql().isEmpty() && psm.getDetailsSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(psm.getDetailsSql());
					insertUnit.executeUpdate();
				}
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert stock Physical: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(PhysicalStockModel psm, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select vBranchId, vPhysicalStockId, vPhysicalStockNo, dPhysicalStockDate, vRemarks, vReferenceNo"+
					" FROM trans.tbPhysicalStockInfo where vPhysicalStockId = :physicalStockId ";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("physicalStockId", idFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();

				psm.setBranchId(element[0].toString().trim());
				psm.setPhysicalStockId(element[1].toString());
				psm.setPhysicalStockNo(element[2].toString());
				psm.setPhysicalStockDate((Date) element[3]);
				psm.setRemarks(element[4].toString());
				psm.setReferenceNo(element[5].toString().trim());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean getPhysicalStockUse(String physicalStockId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			if (!physicalStockId.isEmpty())
			{
				String sql = "select vPhysicalStockId from trans.tbPhysicalStockInfo where vPhysicalStockId = '"+physicalStockId+"' and iActive = 0";
				Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
				if (iter.hasNext())
				{ ret = true; }		
			}
		}
		catch (Exception e)
		{ System.out.println(e); }
		return ret;
	} 

	public boolean PhysicalStockProcess(String formDate, String toDate, String userName, String userId,String branchId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sqlProcess = "exec prcGeneratePhysicalStock '"+branchId+"', '"+formDate+"', '"+toDate+"', '"+userName+"', '"+userId+"'";

			SQLQuery process = session.createSQLQuery(sqlProcess);
			process.executeUpdate();
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error Process: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}
}
