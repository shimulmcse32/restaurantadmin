package com.example.model;

public class ItemUnitModel
{
	private String branchId, unitId, unitName,
	unitDesc, createdBy, typeName;

	public ItemUnitModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }

	public void setUnitId(String unitId)
	{ this.unitId = unitId; }
	public String getUnitId()
	{ return unitId; }

	public void setType(String typeName)
	{ this.typeName = typeName; }
	public String getType()
	{ return typeName; }

	public void setUnitName(String unitName)
	{ this.unitName = unitName; }
	public String getUnitName()
	{ return unitName; }

	public void setUnitDesc(String unitDesc)
	{ this.unitDesc = unitDesc; }
	public String getUnitDesc()
	{ return unitDesc; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }
}
