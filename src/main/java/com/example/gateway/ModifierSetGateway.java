package com.example.gateway;

import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.ItemCategoryModel;

public class ModifierSetGateway
{
	public ModifierSetGateway()
	{ }

	public boolean checkExist(String ModifierName, String ModifierId)
	{
		boolean ret = false;
		String edit = (ModifierId.isEmpty()?"":"and vModifierId != '"+ModifierId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vModifierId from master.tbModifierMaster where vModifierName"+
					" like '"+ModifierName+"' "+edit+"";
			//System.out.println(sql);
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist"); }
		finally{ session.close(); }
		return ret;
	}

	public String getModifierId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vModifierId, 2, 10) as int)),"+
					" 0)+1 from master.tbModifierMaster";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "M"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Category Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public boolean insertEditData(ItemCategoryModel mim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				if (!mim.getDetailsSql().isEmpty() && mim.getDetailsSql() != null)
				{
					SQLQuery insertModi = session.createSQLQuery(mim.getDetailsSql());
					insertModi.executeUpdate();

					if (!mim.getItemIds().isEmpty())
					{
						String prc = "exec trans.prcModifierMapping '"+mim.getCategoryId()+"',"+
								" '"+mim.getItemIds()+"', '"+mim.getCreatedBy()+"'";
						//System.out.println(prc);
						SQLQuery insertItem = session.createSQLQuery(prc);
						insertItem.executeUpdate();
					}
				}
			}
			else
			{
				if (!mim.getDetailsSql().isEmpty() && mim.getDetailsSql() != null)
				{
					if (mim.getModChange())
					{
						SQLQuery insertModi = session.createSQLQuery(mim.getDetailsSql());
						insertModi.executeUpdate();
					}
					if (!mim.getItemIds().isEmpty())
					{
						String prc = "exec trans.prcModifierMapping '"+mim.getCategoryId()+"',"+
								" '"+mim.getItemIds()+"', '"+mim.getCreatedBy()+"'";
						//System.out.println(prc);
						SQLQuery insertItem = session.createSQLQuery(prc);
						insertItem.executeUpdate();
					}
				}
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert/update: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String CategoryId, String userId)
	{
		String sql = "update master.tbModifierMaster set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from master.tbModifierMaster where vModifierId = '"+CategoryId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate(), iSynced = 0, vSyncedMacId = ''"+
				" where vModifierId = '"+CategoryId+"'";
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

	public boolean selectEditData(ItemCategoryModel mim, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vBranchId, vCategoryType, vCategoryName, vCategoryDescription, vCategoryColor"+
					" from master.tbItemCategory where vCategoryId = :categoryId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("categoryId", idFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				mim.setBranchId(element[0].toString());
				mim.setCategoryType(element[1].toString());
				mim.setCategoryName(element[2].toString());
				mim.setCatDesc(element[3].toString());
				mim.setCatColor(element[4].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}
}
