package com.example.model;

public class ItemCategoryModel
{
	private String branchId, categoryId, categoryName, itemIds,
	categoryDesc, createdBy, catColor, typeName, detailsSql;
	private boolean modChange;
	private int showOnline;

	public ItemCategoryModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setCategoryId(String categoryId)
	{ this.categoryId = categoryId; }
	public String getCategoryId()
	{ return categoryId; }

	public void setCategoryType(String typeName)
	{ this.typeName = typeName; }
	public String getCategoryType()
	{ return typeName; }

	public void setCategoryName(String categoryName)
	{ this.categoryName = categoryName; }
	public String getCategoryName()
	{ return categoryName; }

	public void setCatDesc(String categoryDesc)
	{ this.categoryDesc = categoryDesc; }
	public String getCatDesc()
	{ return categoryDesc; }

	public void setCatColor(String catColor)
	{ this.catColor = catColor; }
	public String getCatColor()
	{ return catColor; }

	public void setDetailsSql(String detailsSql)
	{ this.detailsSql = detailsSql; }
	public String getDetailsSql()
	{ return detailsSql; }

	public void setItemIds(String itemIds)
	{ this.itemIds = itemIds; }
	public String getItemIds()
	{ return itemIds; }

	public void setModChange(boolean modChange)
	{ this.modChange = modChange; }
	public boolean getModChange()
	{ return modChange; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }
	
	public void setShowOnline(int showOnline)
	{ this.showOnline = showOnline; }
	public int getShowOnline()
	{ return showOnline; }
}
