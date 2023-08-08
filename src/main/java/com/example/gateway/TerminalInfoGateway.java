package com.example.gateway;

import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.TerminalInfoModel;

public class TerminalInfoGateway
{
	public TerminalInfoGateway()
	{ }

	public boolean checkExist(String terminalCode,String terminalName, String terminalId)
	{
		boolean ret = false;
		String edit = (terminalId.isEmpty()?"":"and vTerminalId != '"+terminalId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vTerminalId from master.tbTerminalInfo where (vTerminalCode"+
					" like '"+terminalCode+"' or vTerminalName like '"+terminalName+"') "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist data"); }
		finally{session.close();}
		return ret;
	}

	public int CountTerminal(String flag)
	{
		int ret = 0;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select ((select [dbo].[funcTerminalTest]()) - Count(vTerminalCode)) countTerminal,"+
					" 0 id from master.tbTerminalInfo where iActive = 1";
			SQLQuery select = session.createSQLQuery(sql);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				if (flag.equals("Add"))
				{ ret = Integer.parseInt(element[0].toString()); }
				else
				{ ret = 1; }	
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Count Terminal data"); }
		finally{session.close();}
		return ret;
	}


	public String getTerminalId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select 'T'+right('0000'+Convert(varchar(10), ISNULL(MAX(SUBSTRING(vTerminalId,"+
					" 2, 10)), 0) + 1), 4) vTerminalId from master.tbTerminalInfo";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" TerminalId"); }
		finally{session.close();};
		return maxId;
	}

	public boolean insertEditData(TerminalInfoModel cim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbTerminalInfo(vBranchId, vTerminalId, vTerminalName, vTerminalCode,"+
						" vStatusId, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate, vMBSerial) values"+
						" (:branchId, :terminalId, :terminalName, :terminalCode, :statusId, 1, :createdBy, getDate(),"+
						" :modifiedBy, getDate(), '')";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("terminalId", cim.getTerminalId());
				insert.setParameter("branchId", cim.getBranchId());
				insert.setParameter("terminalName", cim.getTerminalName());
				insert.setParameter("terminalCode", cim.getTerminalCode());
				insert.setParameter("statusId", cim.getStatusId());
				insert.setParameter("createdBy", cim.getCreatedBy());
				insert.setParameter("modifiedBy", cim.getCreatedBy());
				insert.executeUpdate();
			}
			else if (flag.equals("Edit"))
			{
				String sql = " update master.tbTerminalInfo set vBranchId = :branchId, vTerminalName = :terminalName,"+
						" vTerminalCode = :terminalCode, vStatusId = :statusId, vModifiedBy = :modifiedBy, dModifiedDate = getDate()"+
						" where vTerminalId = :terminalId ";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", cim.getBranchId());
				insert.setParameter("terminalName", cim.getTerminalName());
				insert.setParameter("terminalCode", cim.getTerminalCode());
				insert.setParameter("statusId", cim.getStatusId());
				insert.setParameter("modifiedBy", cim.getCreatedBy());
				//where clause
				insert.setParameter("terminalId", cim.getTerminalId());
				insert.executeUpdate();
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert terminal: "+exp);
			tx.rollback();
		}
		finally{session.close();}
		return ret;
	}

	public boolean activeInactiveData(String terminalId, String modifiedId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sql = "update master.tbTerminalInfo set iActive = (select case when iActive = 1"+
					" then 0 else 1 end from master.tbTerminalInfo where vTerminalId = :terminalId),"+
					" vModifiedBy = :modifiedBy, dModifiedDate = CURRENT_TIMESTAMP where"+
					" vTerminalId = :terminalId";

			Query insert = session.createSQLQuery(sql);
			insert.setParameter("terminalId", terminalId);
			insert.setParameter("modifiedBy", modifiedId);
			insert.executeUpdate();

			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update terminal: "+exp);
			tx.rollback();
		}
		finally{session.close();}
		return ret;
	}

	public boolean selectEditData(TerminalInfoModel cim, String terminalIdFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vBranchId, vTerminalId, vTerminalName, vTerminalCode,"+
					" vStatusId from master.tbTerminalInfo where vTerminalId = :terminalId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("terminalId", terminalIdFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				cim.setBranchId(element[0].toString());
				cim.setTerminalId(element[1].toString());
				cim.setTerminalName(element[2].toString());
				cim.setTerminalCode(element[3].toString());
				cim.setStatusId(element[4].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{session.close();}
		return ret;
	}
}
