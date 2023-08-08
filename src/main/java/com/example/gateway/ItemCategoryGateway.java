package com.example.gateway;

import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.ItemCategoryModel;

public class ItemCategoryGateway
{
	public ItemCategoryGateway()
	{ }

	public boolean checkExist(String CategoryName, String CategoryId, String CategoryType)
	{
		boolean ret = false;
		String edit = (CategoryId.isEmpty()?"":"and vCategoryId != '"+CategoryId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vCategoryId from master.tbItemCategory where vCategoryName"+
					" like '"+CategoryName+"' "+edit+" and vCategoryType = '"+CategoryType+"'";
			//System.out.println(sql);
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist Category"); }
		finally{ session.close(); }
		return ret;
	}

	public String getCategoryId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vCategoryId, 2, 10) as int)),"+
					" 0)+1 from master.tbItemCategory";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "C"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Category Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public boolean insertEditData(ItemCategoryModel cim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbItemCategory(vBranchId, vCategoryId, vCategoryType, vCategoryName,"+
						" vCategoryDescription, vCategoryColor, iShowOnline, iActive,  iSynced, vSyncedMacId, vCreatedBy,"+
						" dCreatedDate, vModifiedBy, dModifiedDate ) values (:branchId, :categoryId, :catType, :categoryName,"+
						" :description, :catColor, :showOnline, 1, 0, '', :createdBy, getDate(), :modifiedBy, getDate())";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", cim.getBranchId());
				insert.setParameter("categoryId", cim.getCategoryId());
				insert.setParameter("catType", cim.getCategoryType());
				insert.setParameter("categoryName", cim.getCategoryName());
				insert.setParameter("description", cim.getCatDesc());
				insert.setParameter("catColor", cim.getCatColor());
				insert.setParameter("showOnline", cim.getShowOnline());
				insert.setParameter("createdBy", cim.getCreatedBy());
				insert.setParameter("modifiedBy", cim.getCreatedBy());
				insert.executeUpdate();
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update master.tbItemCategory set vCategoryName = :categoryName, vCategoryDescription = :catDesc,"+
						" vCategoryColor = :catColor, iShowOnline = :showOnline,  iSynced = 0, vSyncedMacId = '',"+
						" vModifiedBy = :modifiedBy, dModifiedDate = getDate(), vCategoryType = :catType, vBranchId = :branchId"+
						" where vCategoryId = :categoryId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("categoryName", cim.getCategoryName());
				insert.setParameter("catDesc", cim.getCatDesc());
				insert.setParameter("catColor", cim.getCatColor());
				insert.setParameter("modifiedBy", cim.getCreatedBy());
				insert.setParameter("catType", cim.getCategoryType());
				insert.setParameter("showOnline", cim.getShowOnline());
				insert.setParameter("branchId", cim.getBranchId());
				//where clause
				insert.setParameter("categoryId", cim.getCategoryId());
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

	public boolean activeInactiveData(String CategoryId, String userId)
	{
		String sql = "update master.tbItemCategory set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from master.tbItemCategory where vCategoryId = '"+CategoryId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate(), iSynced = 0, vSyncedMacId = ''"+
				" where vCategoryId = '"+CategoryId+"'";
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
			System.out.println("Error update category: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(ItemCategoryModel cim, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vBranchId, vCategoryType, vCategoryName, vCategoryDescription, vCategoryColor,"+
					" iShowOnline from master.tbItemCategory where vCategoryId = :categoryId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("categoryId", idFind);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				cim.setBranchId(element[0].toString());
				cim.setCategoryType(element[1].toString());
				cim.setCategoryName(element[2].toString());
				cim.setCatDesc(element[3].toString());
				cim.setCatColor(element[4].toString());
				cim.setShowOnline(Integer.parseInt(element[5].toString()));
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}
}
