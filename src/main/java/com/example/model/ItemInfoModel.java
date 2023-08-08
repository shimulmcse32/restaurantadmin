package com.example.model;

public class ItemInfoModel
{
	private String branchIds, itemCompanyId, categoryId, barCode, itemId, modifier,
	itemName, itemNameArabic, kitchenName, unitIds, supplierIds, itemColor, description, modifierSql,
	createdBy, vatCategoryId, vatOption, unitRateSql, itemType, imagePath, salesTypes,
	receipeSql, rawCategory, rawUnit, itemTypeRaw;
	private Double vatPercent, minLevel, maxLevel, reOrderLevel, costMargin, issueRate;
	private int itemRaw, onlineMenu;
	private boolean unitRateChange, modifierSqlChange, recipeSqlChange;
	private byte[] itemImage;

	public ItemInfoModel()
	{}

	public void setBranchIds(String branchIds)
	{ this.branchIds = branchIds; }
	public String getBranchIds()
	{ return branchIds; }

	public void setItemCompanyId(String itemCompanyId)
	{ this.itemCompanyId = itemCompanyId; }
	public String getItemCompanyId()
	{ return itemCompanyId; }

	public void setCategoryId(String categoryId)
	{ this.categoryId = categoryId; }
	public String getCategoryId()
	{ return categoryId; }

	public void setBarCode(String barCode)
	{ this.barCode = barCode; }
	public String getBarCode()
	{ return barCode; }

	public void setItemId(String itemId)
	{ this.itemId = itemId; }
	public String getItemId()
	{ return itemId; }

	public void setItemName(String itemName)
	{ this.itemName = itemName; }
	public String getItemName()
	{ return itemName; }

	public void setItemNameArabic(String itemNameArabic)
	{ this.itemNameArabic = itemNameArabic; }
	public String getItemNameArabic()
	{ return itemNameArabic; }

	public void setKitchenName(String kitchenName)
	{ this.kitchenName = kitchenName; }
	public String getKitchenName()
	{ return kitchenName; }

	public void setUnitIds(String unitIds)
	{ this.unitIds = unitIds; }
	public String getUnitIds()
	{ return unitIds; }

	public void setSupplierIds(String supplierIds)
	{ this.supplierIds = supplierIds; }
	public String getSupplierIds()
	{ return supplierIds; }

	public void setSalesTypeIds(String salesTypes)
	{ this.salesTypes = salesTypes; }
	public String getSalesTypeIds()
	{ return salesTypes; }

	public void setItemType(String itemType)
	{ this.itemType = itemType; }
	public String getItemType()
	{ return itemType; }

	public void setItemTypeRaw(String itemTypeRaw)
	{ this.itemTypeRaw = itemTypeRaw; }
	public String getItemTypeRaw()
	{ return itemTypeRaw; }

	public void setVatCategoryId(String vatCategoryId)
	{ this.vatCategoryId = vatCategoryId; }
	public String getVatCategoryId()
	{ return vatCategoryId; }

	public void setVatOption(String vatOption)
	{ this.vatOption = vatOption; }
	public String getVatOption()
	{ return vatOption; }

	public void setImagePath(String imagePath)
	{ this.imagePath = imagePath; }
	public String getImagePath()
	{ return imagePath; }

	public void setItemImage(byte[] itemImage)
	{ this.itemImage = itemImage; }
	public byte[] getItemImage()
	{ return itemImage; }

	public void setItemColor(String itemColor)
	{ this.itemColor = itemColor; }
	public String getItemColor()
	{ return itemColor; }
	
	public void setDescription(String description)
	{ this.description = description; }
	public String getDescription()
	{ return description; }

	public void setUnitRateSql(String unitRateSql)
	{ this.unitRateSql = unitRateSql; }
	public String getUnitRateSql()
	{ return unitRateSql; }

	public void setModifier(String modifier)
	{ this.modifier = modifier; }
	public String getModifier()
	{ return modifier; }

	public void setVatPercent(double vatPercent)
	{ this.vatPercent = vatPercent; }
	public Double getVatPercent()
	{ return vatPercent; }

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

	public void setIssueRate(double issueRate)
	{ this.issueRate = issueRate; }
	public Double getIssueRate()
	{ return issueRate; }

	public void setCostMargin(double costMargin)
	{ this.costMargin = costMargin; }
	public Double getCostMargin()
	{ return costMargin; }
	
	public void setOnlineMenu(int onlineMenu)
	{ this.onlineMenu = onlineMenu; }
	public int getOnlineMenu()
	{ return onlineMenu; }

	public void setItemRaw(int itemRaw)
	{ this.itemRaw = itemRaw; }
	public int getItemRaw()
	{ return itemRaw; }

	public void setRawCategory(String rawCategory)
	{ this.rawCategory = rawCategory; }
	public String getRawCategory()
	{ return rawCategory; }

	public void setRawUnit(String rawUnit)
	{ this.rawUnit = rawUnit; }
	public String getRawUnit()
	{ return rawUnit; }

	public void setModifierSql(String modifierSql)
	{ this.modifierSql = modifierSql; }
	public String getModifierSql()
	{ return modifierSql; }

	public void setUnitRateChange(boolean unitRateChange)
	{ this.unitRateChange = unitRateChange; }
	public boolean getUnitRateChange()
	{ return unitRateChange; }

	public void setReceipeSql(String receipeSql)
	{ this.receipeSql = receipeSql; }
	public String getReceipeSql()
	{ return receipeSql; }

	public void setReceipeSqlChange(boolean recipeSqlChange)
	{ this.recipeSqlChange = recipeSqlChange; }
	public boolean getReceipeSqlChange()
	{ return recipeSqlChange; }

	public void setModifierChange(boolean modifierSqlChange)
	{ this.modifierSqlChange = modifierSqlChange; }
	public boolean getModifierChange()
	{ return modifierSqlChange; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }
}
