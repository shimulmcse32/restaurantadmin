package com.example.model;

public class TerminalInfoModel
{
	private String branchId, branchName, terminalId, terminalName, terminalCode, statusId, statusName, active, createdBy;

	public TerminalInfoModel()
	{}

	public void setBranchId(String branchId)
	{ this.branchId = branchId; }
	public String getBranchId()
	{ return branchId; }
	
	public void setBranchName(String branchName)
	{ this.branchName = branchName; }
	public String getBranchName()
	{ return branchName; }
	
	public void setTerminalId(String terminalId)
	{ this.terminalId = terminalId; }
	public String getTerminalId()
	{ return terminalId; }

	public void setTerminalName(String terminalName)
	{ this.terminalName = terminalName; }
	public String getTerminalName()
	{ return terminalName; }

	public void setTerminalCode(String terminalCode)
	{ this.terminalCode = terminalCode; }
	public String getTerminalCode()
	{ return terminalCode; }

	public void setStatusId(String statusId)
	{ this.statusId = statusId; }
	public String getStatusId()
	{ return statusId; }

	public void setStatusName(String statusName)
	{ this.statusName = statusName; }
	public String getStatusName()
	{ return statusName; }

	public void setActive(String active)
	{ this.active = active; }
	public String getActive()
	{ return active; }

	public void setCreatedBy(String createdBy)
	{ this.createdBy = createdBy; }
	public String getCreatedBy()
	{ return createdBy; }
}
