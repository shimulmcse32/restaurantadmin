package com.example.model;

public class ProductInfoModel
{
	private String branchId, productId, productName, productNameArabic,
	barCode, unitId, categoryId, vatCategoryId, vatOption, supplierId, createdBy;
	private Double mainRate, actualRate, vatAmount, minLevel, maxLevel, reOrderLevel;
	private int saleAble;

	public ProductInfoModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setProductId(String productId)
	{ this.productId = productId; }
	public String getProductId()
	{ return productId; }

	public void setProductName(String productName)
	{ this.productName = productName; }
	public String getProductName()
	{ return productName; }

	public void setProductNameArabic(String productNameArabic)
	{ this.productNameArabic = productNameArabic; }
	public String getProductNameArabic()
	{ return productNameArabic; }

	public void setCategoryId(String categoryId)
	{ this.categoryId = categoryId; }
	public String getCategoryId()
	{ return categoryId; }

	public void setBarCode(String barCode)
	{ this.barCode = barCode; }
	public String getBarCode()
	{ return barCode; }

	public void setUnitId(String unitId)
	{ this.unitId = unitId; }
	public String getUnitId()
	{ return unitId; }

	public void setSupplierId(String supplierId)
	{ this.supplierId = supplierId; }
	public String getSupplierId()
	{ return supplierId; }

	public void setVatCategoryId(String vatCategoryId)
	{ this.vatCategoryId = vatCategoryId; }
	public String getVatCategoryId()
	{ return vatCategoryId; }

	public void setVatOption(String vatOption)
	{ this.vatOption = vatOption; }
	public String getVatOption()
	{ return vatOption; }

	public void setMainRate(double mainRate)
	{ this.mainRate = mainRate; }
	public Double getMainRate()
	{ return mainRate; }

	public void setActualRate(double actualRate)
	{ this.actualRate = actualRate; }
	public Double getActualRate()
	{ return actualRate; }

	public void setVatAmount(double vatAmount)
	{ this.vatAmount = vatAmount; }
	public Double getVatAmount()
	{ return vatAmount; }

	public void setMinLevel(double minLevel)
	{ this.minLevel = minLevel; }
	public Double getMinLevel()
	{ return minLevel; }

	public void setMaxLevel(double maxLevel)
	{ this.maxLevel = maxLevel; }
	public Double getMaxLevel()
	{ return maxLevel; }

	public void setReOrderLevel(double reOrderLevel)
	{ this.reOrderLevel = reOrderLevel; }
	public Double getReOrderLevel()
	{ return reOrderLevel; }

	public void setSaleAble(int saleAble)
	{ this.saleAble = saleAble; }
	public int getSaleAble()
	{ return saleAble; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }
}
