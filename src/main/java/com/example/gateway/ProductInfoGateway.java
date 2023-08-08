package com.example.gateway;

import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionBean;
import com.common.share.SessionFactoryUtil;
import com.example.model.ProductInfoModel;

public class ProductInfoGateway
{
	private SessionBean sessionBean;

	public ProductInfoGateway(SessionBean sessionBean)
	{ this.sessionBean = sessionBean; }

	public boolean checkExist(String productName, String productId)
	{
		boolean ret = false;
		String edit = (productId.isEmpty()?"":"and vProductId != '"+productId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vProductId from "+sessionBean.acc_db+"master.tbProductInfo where"+
					" vProductName like '"+productName+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist"); }
		finally{ session.close(); }
		return ret;
	}

	public String getProductId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vProductId, 2, 10) as int)),"+
					" 0)+1 from "+sessionBean.acc_db+"master.tbProductInfo";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "P"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public boolean insertEditData(ProductInfoModel lim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sqlInfo = "insert into "+sessionBean.acc_db+"master.tbProductInfo(vBranchId, vProductId,"+
						" vProductName, vProductNameArabic, vBarcode, iUnitId, vProductCatId, vVatCatId, vVatOption,"+
						" mMainRate, mActualRate, mVatAmount, iSaleAble, mMinLevel, mMaxLevel, mReOrderLevel, vSupplierId,"+
						" iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate) values (:branchId, :productId,"+
						" :productName, :productNameArabic, :barcode, :unitId, :productCatId, :vatCatId, :vatOption,"+
						" :mainRate, :actualRate, :vatAmount, :saleAble, :minLevel, :maxLevel, :reOrderLevel, :supplierId,"+
						" 1, :createdBy, getDate(), :modifiedBy, getDate())";
				SQLQuery insertInfo = session.createSQLQuery(sqlInfo);
				insertInfo.setParameter("branchId", lim.getBranchId());
				insertInfo.setParameter("productId", lim.getProductId());
				insertInfo.setParameter("productName", lim.getProductName());
				insertInfo.setParameter("productNameArabic", lim.getProductNameArabic());
				insertInfo.setParameter("barcode", lim.getBarCode());
				insertInfo.setParameter("unitId", lim.getUnitId());
				insertInfo.setParameter("productCatId", lim.getCategoryId());
				insertInfo.setParameter("vatCatId", lim.getVatCategoryId());
				insertInfo.setParameter("vatOption", lim.getVatOption());
				insertInfo.setParameter("mainRate", lim.getMainRate());
				insertInfo.setParameter("actualRate", lim.getActualRate());
				insertInfo.setParameter("vatAmount", lim.getVatAmount());
				insertInfo.setParameter("saleAble", lim.getSaleAble());
				insertInfo.setParameter("minLevel", lim.getMinLevel());
				insertInfo.setParameter("maxLevel", lim.getMaxLevel());
				insertInfo.setParameter("reOrderLevel", lim.getReOrderLevel());
				insertInfo.setParameter("supplierId", lim.getSupplierId());
				insertInfo.setParameter("createdBy", lim.getCreatedBy());
				insertInfo.setParameter("modifiedBy", lim.getCreatedBy());
				insertInfo.executeUpdate();
			}
			else if (flag.equals("Edit"))
			{
				String sqlInfo = "update "+sessionBean.acc_db+"master.tbProductInfo set vProductName = :productName,"+
						" vProductNameArabic = :productNameArabic, vBarcode = :barcode, iUnitId = :unitId, vProductCatId"+
						" = :productCatId, vVatCatId = :vatCatId, vVatOption = :vatOption, mMainRate = :mainRate,"+
						" mActualRate = :actualRate, mVatAmount = :vatAmount, iSaleAble = :saleAble, mMinLevel = :minLevel,"+
						" mMaxLevel = :maxLevel, mReOrderLevel = :reOrderLevel, vSupplierId = :supplierId,"+
						" vModifiedBy = :modifiedBy, dModifiedDate = getDate() where vProductId = :productId";
				SQLQuery insertInfo = session.createSQLQuery(sqlInfo);
				insertInfo.setParameter("productName", lim.getProductName());
				insertInfo.setParameter("productNameArabic", lim.getProductNameArabic());
				insertInfo.setParameter("barcode", lim.getBarCode());
				insertInfo.setParameter("unitId", lim.getUnitId());
				insertInfo.setParameter("productCatId", lim.getCategoryId());
				insertInfo.setParameter("vatCatId", lim.getVatCategoryId());
				insertInfo.setParameter("vatOption", lim.getVatOption());
				insertInfo.setParameter("mainRate", lim.getMainRate());
				insertInfo.setParameter("actualRate", lim.getActualRate());
				insertInfo.setParameter("vatAmount", lim.getVatAmount());
				insertInfo.setParameter("saleAble", lim.getSaleAble());
				insertInfo.setParameter("minLevel", lim.getMinLevel());
				insertInfo.setParameter("maxLevel", lim.getMaxLevel());
				insertInfo.setParameter("reOrderLevel", lim.getReOrderLevel());
				insertInfo.setParameter("supplierId", lim.getSupplierId());
				insertInfo.setParameter("modifiedBy", lim.getCreatedBy());
				//Where clause
				insertInfo.setParameter("productId", lim.getProductId());
				insertInfo.executeUpdate();
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

	public boolean activeInactiveData(String productId, String modifiedId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sql = "update "+sessionBean.acc_db+"master.tbProductInfo set iActive = (select case when iActive = 1"+
					" then 0 else 1 end from "+sessionBean.acc_db+"master.tbProductInfo where vProductId = :para1),"+
					" vModifiedBy = :modifiedBy, dModifiedDate = CURRENT_TIMESTAMP where vProductId = :para2";

			Query insert = session.createSQLQuery(sql);
			insert.setParameter("para1", productId);
			insert.setParameter("para2", productId);
			insert.setParameter("modifiedBy", modifiedId);
			insert.executeUpdate();

			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error inactive: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(ProductInfoModel lim, String productId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "SELECT vProductName, vProductNameArabic, vBarcode, iUnitId, vProductCatId, vVatCatId,"+
					" vVatOption, mMainRate, mActualRate, mVatAmount, iSaleAble, mMinLevel, mMaxLevel, mReOrderLevel,"+
					" vSupplierId from "+sessionBean.acc_db+"master.tbProductInfo where vProductId = :productId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("productId", productId);
			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				lim.setProductName(element[0].toString());
				lim.setProductNameArabic(element[1].toString());
				lim.setBarCode(element[2].toString());
				lim.setUnitId(element[3].toString());
				lim.setCategoryId(element[4].toString());
				lim.setVatCategoryId(element[5].toString());
				lim.setVatOption(element[6].toString());
				lim.setMainRate(Double.parseDouble(element[7].toString()));
				lim.setActualRate(Double.parseDouble(element[8].toString()));
				lim.setVatAmount(Double.parseDouble(element[9].toString()));
				lim.setSaleAble(Integer.parseInt(element[10].toString()));
				lim.setMinLevel(Double.parseDouble(element[11].toString()));
				lim.setMaxLevel(Double.parseDouble(element[12].toString()));
				lim.setReOrderLevel(Double.parseDouble(element[13].toString()));
				lim.setSupplierId(element[14].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}
}
