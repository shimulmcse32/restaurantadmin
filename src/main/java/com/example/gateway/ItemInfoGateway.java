package com.example.gateway;

import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.ItemInfoModel;

public class ItemInfoGateway
{
	public ItemInfoGateway()
	{ }

	public boolean checkExist(String ItemName, String ItemId)
	{
		boolean ret = false;
		String edit = (ItemId.isEmpty()?"":"and vItemId != '"+ItemId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vItemId from master.tbFinishedItemInfo where vItemName like '"+ItemName+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist Item"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean checkExistRaw(String ItemName, String ItemId)
	{
		boolean ret = false;
		String edit = (ItemId.isEmpty()?"":"and vItemId != '"+ItemId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vItemId from master.tbRawItemInfo where vItemName like '"+ItemName+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist Item"); }
		finally{ session.close(); }
		return ret;
	}

	public String getRawItemCode()
	{
		String code = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(dbo.funGetNumeric(vItemCode) as int)), 0)+1 from master.tbRawItemInfo";
			//System.out.println(query);
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ code = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Item Code"); }
		finally{ session.close(); };
		return code;
	}

	public String getRawItemUnit(String rawItemId)
	{
		String code = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select uni.vUnitName from master.tbRawItemInfo rii, master.tbUnitInfo uni"+
					" where rii.vUnitId = uni.iUnitId and rii.vItemId = '"+rawItemId+"'";
			//System.out.println(query);
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ code = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Item Code"); }
		finally{ session.close(); };
		return code;
	}

	public boolean checkExistRawCode(String ItemCode, String ItemId)
	{
		boolean ret = false;
		String edit = (ItemId.isEmpty()?"":"and vItemId != '"+ItemId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			if (!ItemCode.isEmpty())
			{
				String sql = "select vItemId from master.tbRawItemInfo where vItemCode like '"+ItemCode+"' "+edit+"";
				Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
				if (iter.hasNext())
				{ ret = true; }
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist Item"); }
		finally{ session.close(); }
		return ret;
	}

	public String getItemId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vItemId, 3, 10) as int)), 0)+1 from master.tbFinishedItemInfo";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "FI"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Item Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getItemIdRaw()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vItemId, 3, 10) as int)), 0)+1 from master.tbRawItemInfo";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "RI"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Item Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getReceipeId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vReceipeId, 2, 10) as int)), 0)+1 from master.tbFinishedReceipe";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "C"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Recipe Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getProfileId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vProfileId, 2, 10) as int)), 0)+1 from master.tbRawItemProfile";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "P"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Profile Id"); }
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
			System.out.println("Error insert: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean insertEditData(ItemInfoModel iim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbFinishedItemInfo(vBranchIds, vItemId, vItemType, vItemName, vItemNameAr,"+
						" vItemNameKitchen, vBarcode, vCategoryId, vSalesTypeIds, vVatCatId, vVatOption, vCompanyId, iOnlineMenu,"+
						" iInventory, vSupplierIds, vItemColor, vDescription, vItemImage, iActive, vCreatedBy, dCreatedDate, vModifiedBy,"+
						" dModifiedDate,"+
						" vUnitIds, vImagePath, vModifierId, iSynced, vSyncedMacId) values (:branchId, :itemId, :itemType, :itemName,"+
						" :itemNameArabic, :itemKitchenName, :barcode, :categoryId, :salesTypeId, :vatCatId, :vatOption, :itemCompanyId,"+
						" :onlineMenu, :inventory, :supplierId, :itemColor, :description, :image, 1, :createdBy, getDate(), :modifiedBy,"+
						" getDate(), :unitIds, :imagePath, :modifierId, 0, '')";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", iim.getBranchIds());
				insert.setParameter("itemId", iim.getItemId());
				insert.setParameter("itemType", iim.getItemType());
				insert.setParameter("itemName", iim.getItemName());
				insert.setParameter("itemNameArabic", iim.getItemNameArabic());
				insert.setParameter("itemKitchenName", iim.getKitchenName());
				insert.setParameter("unitIds", iim.getUnitIds());
				insert.setParameter("barcode", iim.getBarCode());
				insert.setParameter("categoryId", iim.getCategoryId());
				insert.setParameter("salesTypeId", iim.getSalesTypeIds());
				insert.setParameter("vatCatId", iim.getVatCategoryId());
				insert.setParameter("vatOption", iim.getVatOption());
				insert.setParameter("itemCompanyId", iim.getItemCompanyId());
				insert.setParameter("onlineMenu", iim.getOnlineMenu());
				insert.setParameter("inventory", iim.getItemRaw());
				insert.setParameter("supplierId", iim.getSupplierIds());
				insert.setParameter("itemColor", iim.getItemColor());
				insert.setParameter("description", iim.getDescription());
				insert.setParameter("imagePath", iim.getImagePath());
				insert.setParameter("image", iim.getItemImage());
				insert.setParameter("createdBy", iim.getCreatedBy());
				insert.setParameter("modifiedBy", iim.getCreatedBy());
				insert.setParameter("modifierId", iim.getModifier());
				insert.executeUpdate();

				if (!iim.getUnitRateSql().isEmpty() && iim.getUnitRateSql() != null)
				{
					SQLQuery insertUnit = session.createSQLQuery(iim.getUnitRateSql());
					insertUnit.executeUpdate();
				}
				if (!iim.getModifierSql().isEmpty() && iim.getModifierSql() != null)
				{
					SQLQuery insertModi = session.createSQLQuery(iim.getModifierSql());
					insertModi.executeUpdate();
				}
				if (iim.getItemRaw() == 1)
				{
					//System.out.println("Type: "+iim.getItemTypeRaw());
					String prc = "exec trans.prcRawItemFromMenu '"+iim.getItemTypeRaw()+"', '"+iim.getItemId()+"',"+
							" '"+iim.getCreatedBy()+"', '"+iim.getRawCategory()+"', '"+iim.getRawUnit()+"',"+
							" '"+iim.getItemName()+"', '"+iim.getCostMargin()+"', '"+iim.getIssueRate()+"'";
					SQLQuery insertRaw = session.createSQLQuery(prc);
					insertRaw.executeUpdate();
				}
				if (!iim.getReceipeSql().isEmpty() && iim.getReceipeSql() != null)
				{
					SQLQuery insertReceipe = session.createSQLQuery(iim.getReceipeSql());
					insertReceipe.executeUpdate();
				}
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update master.tbFinishedItemInfo set vBranchIds = :branchId, vItemName = :itemName,"+
						" vItemType = :itemType, vItemNameAr = :itemNameArabic, vItemNameKitchen = :itemKitchenName,"+
						" vBarcode = :barcode, vCategoryId = :categoryId, vSalesTypeIds = :salesTypeId, vVatCatId ="+
						" :vatCatId, vSupplierIds = :supplierId, vVatOption = :vatOption, vCompanyId = :itemCompanyId,"+
						" vUnitIds = :unitIds, vItemImage = :itemImage, vDescription = :description, vImagePath = :imagePath,"+
						" vModifiedBy = :modifiedBy, dModifiedDate = getDate(), vModifierId = :modifierId, iOnlineMenu ="+
						" :onlineMenu, iInventory = :inventory, iSynced = 0, vSyncedMacId = '', vItemColor = :itemColor where"+
						" vItemId = :itemId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", iim.getBranchIds());
				insert.setParameter("itemName", iim.getItemName());
				insert.setParameter("itemType", iim.getItemType());
				insert.setParameter("itemNameArabic", iim.getItemNameArabic());
				insert.setParameter("itemKitchenName", iim.getKitchenName());
				insert.setParameter("barcode", iim.getBarCode());
				insert.setParameter("categoryId", iim.getCategoryId());
				insert.setParameter("salesTypeId", iim.getSalesTypeIds());
				insert.setParameter("vatCatId", iim.getVatCategoryId());
				insert.setParameter("supplierId", iim.getSupplierIds());
				insert.setParameter("vatOption", iim.getVatOption());
				insert.setParameter("itemCompanyId", iim.getItemCompanyId());
				insert.setParameter("onlineMenu", iim.getOnlineMenu());
				insert.setParameter("inventory", iim.getItemRaw());
				insert.setParameter("unitIds", iim.getUnitIds());
				insert.setParameter("itemImage", iim.getItemImage());
				insert.setParameter("description", iim.getDescription());
				insert.setParameter("imagePath", iim.getImagePath());
				insert.setParameter("modifiedBy", iim.getCreatedBy());
				insert.setParameter("modifierId", iim.getModifier());
				insert.setParameter("itemColor", iim.getItemColor());
				//where clause
				insert.setParameter("itemId", iim.getItemId());
				insert.executeUpdate();

				if (!iim.getUnitRateSql().isEmpty() && iim.getUnitRateSql() != null && iim.getUnitRateChange())
				{
					SQLQuery insertUnit = session.createSQLQuery(iim.getUnitRateSql());
					insertUnit.executeUpdate();
				}
				if (!iim.getModifierSql().isEmpty() && iim.getModifierSql() != null && iim.getModifierChange())
				{
					SQLQuery insertModi = session.createSQLQuery(iim.getModifierSql());
					insertModi.executeUpdate();
				}
				if (iim.getItemRaw() == 1)
				{
					String prc = "exec trans.prcRawItemFromMenu '"+iim.getItemTypeRaw()+"', '"+iim.getItemId()+"',"+
							" '"+iim.getCreatedBy()+"', '"+iim.getRawCategory()+"', '"+iim.getRawUnit()+"',"+
							" '"+iim.getItemName()+"', '"+iim.getCostMargin()+"', '"+iim.getIssueRate()+"'";
					SQLQuery insertRaw = session.createSQLQuery(prc);
					insertRaw.executeUpdate();
				}
				if (!iim.getReceipeSql().isEmpty() && iim.getReceipeSql() != null && iim.getReceipeSqlChange())
				{
					SQLQuery insertReceipe = session.createSQLQuery(iim.getReceipeSql());
					insertReceipe.executeUpdate();
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

	public boolean insertEditDataRaw(ItemInfoModel iim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbRawItemInfo(vBranchId, vItemId, vItemCode, vItemType, vItemName, vItemNameAr,"+
						" vBarcode, vCategoryId, vVatCatId, vSupplierIds, vUnitId, mCostMargin, iActive, vCreatedBy, dCreatedDate,"+
						" vModifiedBy, dModifiedDate, mIssueRate) values (:branchId, :itemId, :itemCode, :itemType, :itemName,"+
						" :itemNameArabic, :barcode, :categoryId, :vatCatId, :supplierId, :unitId, :costMargin, 1, :createdBy,"+
						" getDate(), :modifiedBy, getDate(), :issueRate)";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("branchId", iim.getBranchIds());
				insert.setParameter("itemId", iim.getItemId());
				insert.setParameter("itemCode", iim.getKitchenName());
				insert.setParameter("itemType", iim.getItemType());
				insert.setParameter("itemName", iim.getItemName());
				insert.setParameter("itemNameArabic", iim.getItemNameArabic());
				insert.setParameter("barcode", iim.getBarCode());
				insert.setParameter("categoryId", iim.getRawCategory());
				insert.setParameter("vatCatId", iim.getVatCategoryId());
				insert.setParameter("supplierId", iim.getSupplierIds());
				insert.setParameter("unitId", iim.getRawUnit());
				insert.setParameter("issueRate", iim.getIssueRate());
				insert.setParameter("costMargin", iim.getCostMargin());
				insert.setParameter("createdBy", iim.getCreatedBy());
				insert.setParameter("modifiedBy", iim.getCreatedBy());
				insert.executeUpdate();

				/*if (!iim.getReceipeSql().isEmpty() && iim.getReceipeSql() != null)
				{
					SQLQuery insertReceipe = session.createSQLQuery(iim.getReceipeSql());
					insertReceipe.executeUpdate();
				}*/
			}
			else if (flag.equals("Edit"))
			{
				String sql = "update master.tbRawItemInfo set vItemCode = :itemCode, vItemType = :itemType, vItemName = :itemName,"+
						" vItemNameAr = :itemNameArabic, vBarcode = :barcode, vCategoryId = :categoryId, vVatCatId = :vatCatId,"+
						" vSupplierIds = :supplierId, vUnitId = :unitId, mCostMargin = :costMargin, mIssueRate = :issueRate,"+
						" vModifiedBy = :modifiedBy, dModifiedDate = getDate() where vItemId = :itemId";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("itemCode", iim.getKitchenName());
				insert.setParameter("itemType", iim.getItemType());
				insert.setParameter("itemName", iim.getItemName());
				insert.setParameter("itemNameArabic", iim.getItemNameArabic());
				insert.setParameter("barcode", iim.getBarCode());
				insert.setParameter("categoryId", iim.getRawCategory());
				insert.setParameter("vatCatId", iim.getVatCategoryId());
				insert.setParameter("supplierId", iim.getSupplierIds());
				insert.setParameter("unitId", iim.getRawUnit());
				insert.setParameter("issueRate", iim.getIssueRate());
				insert.setParameter("costMargin", iim.getCostMargin());
				insert.setParameter("modifiedBy", iim.getCreatedBy());
				//where clause
				insert.setParameter("itemId", iim.getItemId());
				insert.executeUpdate();

				/*if (!iim.getReceipeSql().isEmpty() && iim.getReceipeSql() != null && iim.getReceipeSqlChange())
				{
					SQLQuery insertReceipe = session.createSQLQuery(iim.getReceipeSql());
					insertReceipe.executeUpdate();
				}*/
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

	public boolean activeInactiveData(String ItemId, String userId)
	{
		boolean ret = false;
		String sql = "update master.tbFinishedItemInfo set iActive = (select case when iActive = 1"+
				" then 0 else 1 end from master.tbFinishedItemInfo where vItemId = '"+ItemId+"'),"+
				" vModifiedBy = '"+userId+"', dModifiedDate = getdate(), iSynced = 0, vSyncedMacId = ''"+
				" where vItemId = '"+ItemId+"'";
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
			System.out.println("Error active Item: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveDataRaw(String ItemId, String userId)
	{
		boolean ret = false;
		String sql = "update master.tbRawItemInfo set iActive = (select case when iActive = 1 then 0 else 1 end from master.tbRawItemInfo"+
				" where vItemId = '"+ItemId+"'), vModifiedBy = '"+userId+"', dModifiedDate = getdate() where vItemId = '"+ItemId+"'";
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
			System.out.println("Error active Item: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(ItemInfoModel iim, String idFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vItemType, iInventory, vBranchIds, vItemName, vUnitIds, vCategoryId, vSalesTypeIds, vVatCatId, vVatOption,"+
					" vItemNameAr, vItemNameKitchen, vModifierId, vBarcode, vCompanyId, vSupplierIds, vImagePath, iOnlineMenu, vItemColor,"+
					" vDescription from master.tbFinishedItemInfo where vItemId = :itemId";
			//System.out.println(sql);
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("itemId", idFind);
			for (Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				iim.setItemType(element[0].toString().trim());
				iim.setItemRaw(Integer.parseInt(element[1].toString()));
				iim.setBranchIds(element[2].toString().trim());
				iim.setItemName(element[3].toString());
				iim.setUnitIds(element[4].toString().trim());
				iim.setCategoryId(element[5].toString().trim());
				iim.setSalesTypeIds(element[6].toString().trim());
				iim.setVatCategoryId(element[7].toString().trim());
				iim.setVatOption(element[8].toString());
				iim.setItemNameArabic(element[9].toString().trim());
				iim.setKitchenName(element[10].toString());
				iim.setModifier(element[11].toString().trim());

				iim.setBarCode(element[12].toString());
				iim.setItemCompanyId(element[13].toString());
				iim.setSupplierIds(element[14].toString().trim());
				iim.setImagePath(element[15].toString().trim());
				iim.setOnlineMenu(Integer.parseInt(element[16].toString()));
				iim.setItemColor(element[17].toString());
				iim.setDescription(element[18].toString());
				iim.setItemId(idFind);
				selectEditRaw(iim, idFind);
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	private boolean selectEditRaw(ItemInfoModel iim, String itemId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vItemType, vCategoryId, vUnitId, mIssueRate, mCostMargin from"+
					" master.tbRawItemInfo where vItemId = :itemId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("itemId", itemId);
			for (Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				iim.setItemTypeRaw(element[0].toString());
				iim.setRawCategory(element[1].toString());
				iim.setRawUnit(element[2].toString());
				iim.setIssueRate(Double.parseDouble(element[3].toString()));
				iim.setCostMargin(Double.parseDouble(element[4].toString()));
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditDataRaw(ItemInfoModel iim, String itemId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vItemCode, vItemName, vItemNameAr, vBarcode, vCategoryId, vVatCatId, vSupplierIds,"+
					" vUnitId, mIssueRate, mCostMargin, vItemType from master.tbRawItemInfo where vItemId = :itemId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("itemId", itemId);
			for (Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				iim.setKitchenName(element[0].toString());
				iim.setItemName(element[1].toString());
				iim.setItemNameArabic(element[2].toString());
				iim.setBarCode(element[3].toString());
				iim.setRawCategory(element[4].toString());
				iim.setVatCategoryId(element[5].toString());
				iim.setSupplierIds(element[6].toString());
				iim.setRawUnit(element[7].toString());
				iim.setIssueRate(Double.parseDouble(element[8].toString()));
				iim.setCostMargin(Double.parseDouble(element[9].toString()));
				iim.setItemType(element[10].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}
}
