package com.example.gateway;

import java.util.Iterator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;

public class RoleInfoGateway
{
	public RoleInfoGateway()
	{ }

	public boolean checkExist(String roleName, String roleId)
	{
		boolean ret = false;
		String edit = (roleId.isEmpty()?"":"and vRoleId != '"+roleId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vRoleId from master.tbUserRoleInfo where vRoleName"+
					" like '"+roleName+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist data"); }
		finally{ session.close(); }
		return ret;
	}

	public String getRoleId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vRoleId, 2, 10) as int)),"+
					" 0)+1 from master.tbUserRoleInfo";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "R" + iter.next().toString(); }//Add branch to identify each transaction
		}
		catch (Exception ex)
		{ System.out.print(ex+" Data Id"); }
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
			System.out.println("Error insert : "+exp+"\n"+sql);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String roleId, String userId)
	{
		boolean ret = false;
		String sql = "update master.tbUserRoleInfo set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from master.tbUserRoleInfo where vRoleId = '" + roleId + "'),"+
				" vModifiedBy = '" + userId + "', dModifiedDate = CURRENT_TIMESTAMP, iSynced = 0,"+
				" vSyncedMacId = '' where vRoleId = '"+ roleId +"'";
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
			System.out.println("Error insert role: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public void updateNote(String notes, String userId)
	{
		String sql = "update tbUserNote set vNoteDetails = '"+notes.replaceAll("'", "''")+"',"+
				" dModifiedDate = getdate() where vUserId = '"+userId+"'";
		insertData(sql);
	}
}
