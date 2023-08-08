package com.example.gateway;

import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.ItemUnitModel;

public class ItemUnitGateway
{
	public ItemUnitGateway()
	{ }

	public boolean checkExist(String unitName, String unitId)
	{
		boolean ret = false;
		String edit = (unitId.isEmpty()?"":"and iUnitId != '"+unitId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select iUnitId from master.tbUnitInfo"+
					" where vUnitName like '"+unitName+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean checkExistNote(String noteDetails, String noteId)
	{
		boolean ret = false;
		String edit = (noteId.isEmpty()?"":"and vNoteId != '"+noteId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vNoteId from master.tbNoteInformation"+
					" where vNoteDetails like '"+noteDetails+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist note"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean checkExistPay(String payDetails, String payId)
	{
		boolean ret = false;
		String edit = (payId.isEmpty()?"":"and iMethodId != '"+payId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vMethodName from master.tbPaymentMethods"+
					" where vMethodName like '"+payDetails+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist pay"); }
		finally{ session.close(); }
		return ret;
	}

	public String getUnitId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(iUnitId), 0)+1 from master.tbUnitInfo";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" getUnitId"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getNoteId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(convert(int, vNoteId)), 0)+1 from master.tbNoteInformation";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" getNoteId"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getPayId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(iMethodId), 0)+1 from master.tbPaymentMethods";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" getPayId"); }
		finally{ session.close(); };
		return maxId;
	}

	public boolean insertEditData(ItemUnitModel ium, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbUnitInfo(vBranchId, iUnitId, vUnitType, vUnitName,"+
						" vUnitDescription, iActive, iSynced, vSyncedMacId, vCreatedBy, dCreatedDate, vModifiedBy,"+
						" dModifiedDate) values (:branchId, :unitId, :unitType, :unitName, :unitDesc, 1, 0, '',"+
						" :createdBy, getDate(), :modifiedBy, getDate())";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", ium.getBranchId());
				insert.setParameter("unitId", ium.getUnitId());
				insert.setParameter("unitType", ium.getType());
				insert.setParameter("unitName", ium.getUnitName());
				insert.setParameter("unitDesc", ium.getUnitDesc());
				insert.setParameter("createdBy", ium.getCreatedBy());
				insert.setParameter("modifiedBy", ium.getCreatedBy());
				insert.executeUpdate();
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update master.tbUnitInfo set vUnitName = :unitName,"+
						" vUnitDescription = :unitDesc, vModifiedBy = :modifiedBy, dModifiedDate"+
						" = getDate(), vUnitType = :unitType, vBranchId = :branchId, iSynced = 0,"+
						" vSyncedMacId = '' where iUnitId = :unitId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("unitName", ium.getUnitName());
				insert.setParameter("unitDesc", ium.getUnitDesc());
				insert.setParameter("modifiedBy", ium.getCreatedBy());
				insert.setParameter("unitType", ium.getType());
				insert.setParameter("branchId", ium.getBranchId());
				//where clause
				insert.setParameter("unitId", ium.getUnitId());
				insert.executeUpdate();
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert category: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean insertEditDataNote(ItemUnitModel ium, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbNoteInformation(vBranchId, vNoteId, vNoteType, vNoteDetails,"+
						" iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate, vSyncedMacId, iSynced)"+
						" values (:branchId, :noteId, :noteType, :noteDetails, 1, :createdBy, getDate(),"+
						" :modifiedBy, getDate(), '', 0)";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", ium.getBranchId());
				insert.setParameter("noteId", ium.getUnitId());
				insert.setParameter("noteType", ium.getType());
				insert.setParameter("noteDetails", ium.getUnitName());
				insert.setParameter("createdBy", ium.getCreatedBy());
				insert.setParameter("modifiedBy", ium.getCreatedBy());
				insert.executeUpdate();
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update master.tbNoteInformation set vNoteType = :noteType, vNoteDetails = :noteDetails,"+
						" vModifiedBy = :modifiedBy, dModifiedDate = getDate(), vSyncedMacId = '', iSynced = 0 where"+
						" vNoteId = :noteId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("noteType", ium.getType());
				insert.setParameter("noteDetails", ium.getUnitName());
				insert.setParameter("modifiedBy", ium.getCreatedBy());
				//where clause
				insert.setParameter("noteId", ium.getUnitId());
				insert.executeUpdate();
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert category: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean insertEditDataPay(ItemUnitModel ium, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbPaymentMethods(iMethodId, vMethodName, iActive,"+
						" vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate, iSynced, vSyncedMacId) values"+
						" (:methodId, :methodName, 1, :createdBy, getDate(), :modifiedBy, getDate(), 0, '')";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("methodId", ium.getUnitId());
				insert.setParameter("methodName", ium.getUnitName());
				insert.setParameter("createdBy", ium.getCreatedBy());
				insert.setParameter("modifiedBy", ium.getCreatedBy());
				insert.executeUpdate();
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update master.tbPaymentMethods set vMethodName = :methodName, vModifiedBy ="+
						" :modifiedBy, dModifiedDate = getDate(), vSyncedMacId = '', iSynced = 0 where"+
						" iMethodId = :methodId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("methodName", ium.getUnitName());
				insert.setParameter("modifiedBy", ium.getCreatedBy());
				//where clause
				insert.setParameter("methodId", ium.getUnitId());
				insert.executeUpdate();
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert category: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String unitId, String userId)
	{
		String sql = "update master.tbUnitInfo set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from master.tbUnitInfo where iUnitId = '"+unitId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = CURRENT_TIMESTAMP,"+
				" iSynced = 0, vSyncedMacId = '' where iUnitId = '"+unitId+"'";
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
			System.out.println("Error active: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveDataNote(String noteId, String userId)
	{
		String sql = "update master.tbNoteInformation set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from master.tbNoteInformation where vNoteId = '"+noteId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate(), iSynced = 0, vSyncedMacId = ''"+
				" where vNoteId = '"+noteId+"'";
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
			System.out.println("Error active: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveDataMethod(String noteId, String userId)
	{
		String sql = "update master.tbPaymentMethods set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from master.tbPaymentMethods where iMethodId = '"+noteId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate(), iSynced = 0,"+
				" vSyncedMacId = '' where iMethodId = '"+noteId+"'";
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
			System.out.println("Error active: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(ItemUnitModel ium, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vBranchId, vUnitType, vUnitName, vUnitDescription"+
					" from master.tbUnitInfo where iUnitId = :unitId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("unitId", idFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				ium.setBranchId(element[0].toString());
				ium.setType(element[1].toString());
				ium.setUnitName(element[2].toString());
				ium.setUnitDesc(element[3].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditDataNote(ItemUnitModel ium, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vNoteType, vNoteDetails from master.tbNoteInformation"+
					" where vNoteId = :noteId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("noteId", idFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				ium.setType(element[0].toString());
				ium.setUnitName(element[1].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditDataPay(ItemUnitModel ium, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select iMethodId, vMethodName from master.tbPaymentMethods"+
					" where iMethodId = :methodId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("methodId", idFind);
			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				ium.setUnitName(element[1].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}
}
