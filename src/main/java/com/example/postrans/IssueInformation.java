/*
 * Copyright 2000-2014 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.postrans;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.MessageBox;
import com.common.share.MultiComboBox;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.ReportViewer;
import com.example.gateway.IssueInfoGateway;
import com.example.gateway.TransAppCanGateway;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class IssueInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");
	private TablePaged tblIssueList;
	private ArrayList<Label> tbLblIssueId = new ArrayList<Label>();
	private ArrayList<Label> tbLblIssueNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblIssueDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblTotalItem = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkStatus = new ArrayList<CheckBox>();
	private ArrayList<Label> tbLblAmount = new ArrayList<Label>();
	private ArrayList<Label> tbLblBranchFrom = new ArrayList<Label>();
	private ArrayList<Label> tbLblBranchTo = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();

	private SessionBean sessionBean;
	private TextField txtSearch;
	private ComboBox cmbStatus;
	private PopupDateField txtFromDate, txtToDate;
	private CommonButton cBtnS = new CommonButton("", "", "", "", "", "Search", "", "", "");

	private CommonMethod cm;
	private IssueInfoGateway iig = new IssueInfoGateway();
	private String formId;

	//Issue report
	private PopupDateField txtReportFromDate, txtReportToDate;
	private MultiComboBox cmbItemNameForReport;
	private CommonButton cBtnV = new CommonButton("", "", "", "", "", "", "", "View", "");

	public IssueInformation(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		this.formId = formId;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		addComponents(cBtn, addPanel(), addReport());

		addActions();
	}

	private void addActions()
	{
		loadStatus();
		cBtn.btnNew.addClickListener(event ->
		{
			addEditWindow("Add", "", "");
			event.getButton().setEnabled(false);
		});

		cBtn.btnRefresh.addClickListener(event ->
		{ loadTableInfo(); });

		cBtnS.btnSearch.addClickListener(event ->
		{ loadTableInfo(); });

		cBtnV.btnPreview.addClickListener(event ->
		{ addValidation(); });

		txtReportFromDate.addValueChangeListener(event ->
		{ loadItemReport(); });

		txtReportToDate.addValueChangeListener(event ->
		{ loadItemReport(); });
	}

	private void loadStatus()
	{
		String sqlStatus = "select vStatusId, vStatusName from master.tbAllStatus where iActive = 1 and"+
				" vFlag = 'st' order by vStatusName";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sqlStatus).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbStatus.addItem(element[0].toString());
			cmbStatus.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void addEditWindow(String addEdit, String itemId, String ar)
	{
		AddEditIssueInfo win = new AddEditIssueInfo(sessionBean, addEdit, itemId);
		getUI().addWindow(win);
		win.center();
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event ->
		{
			if (!ar.isEmpty())
			{ tbCmbAction.get(Integer.parseInt(ar)).setEnabled(true); }
			cBtn.btnNew.setEnabled(true);
			loadTableInfo();
		});
	}

	private void loadTableInfo()
	{
		String search = "%"+txtSearch.getValue().toString()+"%";
		String status = cmbStatus.getValue() != null? cmbStatus.getValue().toString():"%";
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		tableClear();
		int i = 0;
		try
		{
			String sql = "select vIssueId, vIssueNo, dIssueDate, mTotalAmount, item, iActive, vBranchFrom, vBranchFromId,"+
					" vBranchTo, vBranchToId, iStatus, vStatusId, iAutoId, vReqNo from (select isu.iAutoId, isu.vIssueId,"+
					" isu.vIssueNo, isu.dIssueDate, mTotalAmount, (select isnull(count(vItemId),0) from trans.tbIssueDetails"+
					" where vIssueId = isu.vIssueId) item, isu.iActive, (select vBranchName from master.tbBranchMaster"+
					" where vBranchId = isu.vBranchFrom) vBranchFrom, isu.vBranchFrom vBranchFromId, (select vBranchName"+
					" from master.tbBranchMaster where vBranchId = isu.vBranchTo)vBranchTo, isu.vBranchTo vBranchToId,"+
					" isu.iStatus, isu.vCancelledBy, isu.vStatusId, (select vRequisitionNo from trans.tbRequisitionInfo req"+
					" where req.vRequisitionId = isu.vRequisitionId) vReqNo from trans.tbIssueInfo isu where isu.vBranchId ="+
					" '"+sessionBean.getBranchId()+"') xyz where (vIssueNo like '"+search+"' or vBranchFrom like '"+search+"'"+
					" or vBranchTo like '"+search+"') and dIssueDate between '"+fromDate+"' and '"+toDate+"' and iStatus"+
					" like '"+status+"' order by dIssueDate, iAutoId desc";
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblIssueId.size() <= i)
				{ tableRowAdd(i); }

				tbLblIssueId.get(i).setValue(element[0].toString());
				tbLblIssueNo.get(i).setValue(element[1].toString()+" ("+element[13].toString()+")");
				tbLblIssueDate.get(i).setValue(cm.dfBd.format(element[2]));
				tbLblBranchFrom.get(i).setValue(element[6].toString());
				tbLblBranchTo.get(i).setValue(element[8].toString());
				tbLblAmount.get(i).setValue(cm.setComma(Double.parseDouble(element[3].toString())));
				tbLblTotalItem.get(i).setValue(element[4].toString());
				tbChkActive.get(i).setValue((element[5].toString().equals("1")? true:false));
				tbChkActive.get(i).setEnabled(false);
				tbChkStatus.get(i).setValue((element[10].toString().equals("1")? true:false));
				tbChkStatus.get(i).setEnabled(false);

				if (element[10].toString().equals("1"))
				{ tbCmbAction.get(i).removeItem("Received"); }
				if (element[11].toString().equals("S6"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Approve");
					tbCmbAction.get(i).removeItem("Received");
				}
				if (element[11].toString().equals("S7"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Cancel");
					tbCmbAction.get(i).removeItem("Approve");
					tbCmbAction.get(i).removeItem("Received");
				}
				i++;
			}
			tblIssueList.nextPage();
			tblIssueList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			totalAmount();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblIssueList, tbLblIssueId); }

	public double totalAmount()
	{
		double amt = 0;
		for (int i=0; i<tbLblIssueId.size(); i++)
		{ amt += cm.getAmtValue(tbLblAmount.get(i)); }
		tblIssueList.setColumnFooter("Amount", cm.setComma(amt));
		tblIssueList.setColumnAlignment("Amount", Align.RIGHT);		
		return amt;
	}

	private void ActiveInactiveSelectIssue(String issueId, int ar)
	{
		MessageBox mb = new MessageBox(getUI(), "Are you sure?",
				MessageBox.Icon.QUESTION, "Do you want to save information?",
				new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
				new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
		mb.show(new EventListener()
		{
			public void buttonClicked(ButtonType buttonType)
			{
				if (buttonType == ButtonType.YES)
				{
					if (iig.activeInactiveData(issueId, sessionBean.getUserId()))
					{
						tbChkActive.get(ar).setValue(!tbChkActive.get(ar).getValue().booleanValue());
						cm.showNotification("success", "Successfull!", "All information updated successfully.");
					}
					else
					{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
				}
			}
		});
	}

	private void ReceivedSelectIssue(String issueId, int ar)
	{
		MessageBox mb = new MessageBox(getUI(), "Are you sure?",
				MessageBox.Icon.QUESTION, "Do you want to save information?",
				new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
				new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
		mb.show(new EventListener()
		{
			public void buttonClicked(ButtonType buttonType)
			{
				if (buttonType == ButtonType.YES)
				{
					if (iig.ReceivedData(issueId, sessionBean.getUserId()))
					{
						tbChkStatus.get(ar).setValue(!tbChkStatus.get(ar).getValue().booleanValue());
						cm.showNotification("success", "Successfull!", "All information updated successfully.");
						tbChkStatus.get(ar).setEnabled(true);
					}
					else
					{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
				}
				else if (buttonType == ButtonType.NO)
				{ tbChkStatus.get(ar).setEnabled(true); }
			}
		});
	}

	private void TransactionCancelWindow(String issueId, String ar)
	{
		TransactionCancel win = new TransactionCancel(sessionBean, issueId, "Issue");
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event ->
		{
			if (!ar.isEmpty())
			{ tbCmbAction.get(Integer.parseInt(ar)).setEnabled(true); }
			loadTableInfo();
		});
	}

	private void TransactionApproveWindow(String issueId, String ar)
	{
		MessageBox mb = new MessageBox(getUI(), "Are you sure?",
				MessageBox.Icon.QUESTION, "Do you want to approve information?",
				new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
				new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
		mb.show(new EventListener()
		{
			public void buttonClicked(ButtonType buttonType)
			{
				if (buttonType == ButtonType.YES)
				{
					TransAppCanGateway tacm = new TransAppCanGateway();
					if (tacm.TransactionApprove(issueId, sessionBean.getUserId(), "Issue"))
					{
						cm.showNotification("success", "Successfull!", "All information saved successfully.");
						loadTableInfo();
					}
					else
					{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
				}
			}
		});
	}

	public void viewReportSingle(String issueId)
	{
		String reportSource = "", sql = "";
		try
		{
			HashMap<String, Object> hm = new HashMap<String, Object>();
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", sessionBean.getUserIp());

			sql = "select isu.vIssueId, isu.vIssueNo, isu.dIssueDate, isu.iActive, (select vBranchName from master.tbBranchMaster"+
					" where vBranchId = isu.vBranchFrom)vBranchFrom, isu.vBranchFrom vBranchFromId, (select vBranchName from"+
					" master.tbBranchMaster where vBranchId = isu.vBranchTo)vBranchTo, isu.vBranchTo vBranchToId, isu.iStatus,"+
					" isd.vItemId, rii.vItemName, uni.vUnitName, cat.vCategoryName, isd.mStockQty, isd.mIssueQty, isd.mIssueRate,"+
					" isd.mAmount, isu.vReferenceNo, isu.vRemarks, isnull(isd.vRemarks,'') vDescription, isd.mCostmargin, ui.vFullName,"+
					" isd.mIssueRate, isu.vReceivedBy, isu.vRequisitionId, rqi.vRequisitionNo, rqi.dRequisitionDate, ISNULL((select"+
					" ui.vFullName from master.tbUserInfo ui where ui.vUserId = isu.vApprovedBy), '') vApprovedBy, ISNULL((select"+
					" ui.vFullName from master.tbUserInfo ui where ui.vUserId = isu.vCancelledBy), '') vCancelledBy from trans.tbIssueInfo"+
					" isu inner join trans.tbIssueDetails isd on isu.vIssueId = isd.vIssueId inner join master.tbRawItemInfo rii on"+
					" rii.vItemId = isd.vItemId left join trans.tbRequisitionInfo rqi on rqi.vRequisitionId = isu.vRequisitionId"+
					" inner join master.tbUnitInfo uni on isd.vUnitId = convert(varchar(10), uni.iUnitId) inner join master.tbItemCategory"+
					" cat on isd.vCategoryId = cat.vCategoryId inner join master.tbUserInfo ui on isu.vModifiedBy = ui.vUserId"+
					" where isu.vIssueId = '"+issueId+"' order by isd.iAutoId";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptIssueInfo.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private void loadItemReport()
	{
		cmbItemNameForReport.removeAllItems();
		String fromDate = cm.dfDb.format(txtReportFromDate.getValue());
		String toDate = cm.dfDb.format(txtReportToDate.getValue());
		String sqlC = "";

		sqlC = "Select distinct r.vItemId, r.vItemName, dbo.funGetNumeric(r.vItemCode) iCode from trans.tbIssueDetails abc"+
				" inner join trans.tbIssueinfo xyz on abc.vIssueId = xyz.vIssueId inner join master.tbRawItemInfo r on"+
				" abc.vItemId = r.vItemId where convert(date,xyz.dIssueDate,105) between '"+fromDate+"' and '"+toDate+"'"+
				" and xyz.vBranchId = '"+sessionBean.getBranchId()+"' order by iCode asc";
		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbItemNameForReport.addItem(element[0].toString());
			cmbItemNameForReport.setItemCaption(element[0].toString(), element[2].toString()+" - "+element[1].toString());
		}
	}

	private void addValidation()
	{
		if (txtReportFromDate.getValue() != null)
		{
			if (txtReportToDate.getValue() != null)
			{ viewReport(); }
			else
			{
				txtReportToDate.focus();
				cm.showNotification("warning", "Warning!", "Select to date.");
			}
		}
		else
		{
			txtReportFromDate.focus();
			cm.showNotification("warning", "Warning!", "Select from date.");
		}
	}

	public void viewReport()
	{
		String reportSource = "", sql = "";
		String fromDate = cm.dfDb.format(txtReportFromDate.getValue());
		String toDate = cm.dfDb.format(txtReportToDate.getValue());
		String itemId = cmbItemNameForReport.getValue().toString().replace("]", "").replace("[", "").trim();
		String itemIds = itemId.isEmpty()? "%":itemId;
		String datePara = "From: "+cm.dfBd.format(txtReportFromDate.getValue())+" To "+cm.dfBd.format(txtReportToDate.getValue());
		try
		{
			HashMap<String, Object> hm = new HashMap<String, Object>();
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getBranchAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("userIp", sessionBean.getUserIp());
			hm.put("logo", sessionBean.getBranchLogo());
			hm.put("fromToDate", datePara);

			sql = "select isu.vIssueId, isu.vIssueNo, isu.dIssueDate, isu.iActive, (select vBranchName from master.tbBranchMaster where"+
					" vBranchId = isu.vBranchFrom) vBranchFrom, isu.vBranchFrom vBranchFromId, (select vBranchName from master.tbBranchMaster"+
					" where vBranchId = isu.vBranchTo)vBranchTo, isu.vBranchTo vBranchToId, isu.iStatus, isd.vItemId, rii.vItemName,"+
					" uni.vUnitName, cat.vCategoryName, isd.mStockQty, isd.mIssueQty, isd.mIssueRate, 0 mSalesRate, isd.mAmount, ui.vFullName,"+
					" isu.vReferenceNo, isu.vRemarks, isnull(isd.vRemarks,'') vdescription, isd.mCostmargin, isd.mIssueRate mAverageRate,"+
					" isu.vReceivedBy, isu.vRequisitionId, rqi.vRequisitionNo, rqi.dRequisitionDate, ISNULL((select ui.vFullName from"+
					" master.tbUserInfo ui where ui.vUserId = isu.vApprovedBy), '') vApprovedBy, ISNULL((select ui.vFullName from"+
					" master.tbUserInfo ui where ui.vUserId = isu.vCancelledBy), '') vCancelledBy from trans.tbIssueInfo isu inner join"+
					" trans.tbIssueDetails isd on isu.vIssueId = isd.vIssueId inner join master.tbRawItemInfo rii on rii.vItemId = isd.vItemId"+
					" left join trans.tbRequisitionInfo rqi on rqi.vRequisitionId = isu.vRequisitionId inner join master.tbUnitInfo uni on"+
					" isd.vUnitId = convert(varchar(10), uni.iUnitId) inner join master.tbItemCategory cat on rii.vCategoryId = cat.vCategoryId"+
					" inner join master.tbUserInfo ui on isu.vModifiedBy = ui.vUserId where isd.vItemId in (select Item from dbo.Split"+
					"('"+itemIds+"')) and convert(date,isu.dIssueDate,105) between '"+fromDate+"' and '"+toDate+"' and isu.vBranchId"+
					" = '"+sessionBean.getBranchId()+"' order by isd.iAutoId";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptIssueInfo.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private Panel addPanel()
	{
		Panel pnlTable = new Panel("Issue List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");

		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout hori = new HorizontalLayout();
		hori.setSpacing(true);

		txtFromDate = new PopupDateField();
		txtFromDate.setImmediate(true);
		txtFromDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDate.setValue(new Date());
		txtFromDate.setDateFormat("dd-MM-yyyy");
		txtFromDate.setWidth("110px");
		txtFromDate.setDescription("From Date");
		txtFromDate.setInputPrompt("From Date");
		txtFromDate.setRequired(true);
		txtFromDate.setRequiredError("This field is required");

		txtToDate = new PopupDateField();
		txtToDate.setImmediate(true);
		txtToDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDate.setValue(new Date());
		txtToDate.setDateFormat("dd-MM-yyyy");
		txtToDate.setWidth("110px");
		txtToDate.setDescription("To Date"); 
		txtToDate.setInputPrompt("To Date");
		txtToDate.setRequired(true);
		txtToDate.setRequiredError("This field is required");

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Branch and Issue No");
		txtSearch.setDescription("Search by Branch and Issue No");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		cmbStatus = new ComboBox();
		cmbStatus.setImmediate(true);
		cmbStatus.setFilteringMode(FilteringMode.CONTAINS);
		cmbStatus.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbStatus.setInputPrompt("Select status");
		cmbStatus.setWidth("115px");

		cBtnS.btnSearch.setStyleName(ValoTheme.BUTTON_TINY);
		hori.addComponents(txtFromDate, txtToDate, txtSearch, cmbStatus, cBtnS);

		buildTable();
		content.addComponents(hori, tblIssueList, tblIssueList.createControls());
		pnlTable.setContent(content);
		return pnlTable;
	}

	private void buildTable()
	{
		tblIssueList = new TablePaged();
		tblIssueList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblIssueId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblIssueList.addContainerProperty("Issue Id", Label.class, new Label(), null, null, Align.CENTER);
		tblIssueList.setColumnCollapsed("Issue Id", true);

		tblIssueList.addContainerProperty("Issue No(Req. No)", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueList.addContainerProperty("Issue Date", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueList.addContainerProperty("Total Item", Label.class, new Label(), null, null, Align.RIGHT);

		tblIssueList.addContainerProperty("Amount", Label.class, new Label(), null, null, Align.RIGHT); 

		tblIssueList.addContainerProperty("Branch From", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueList.addContainerProperty("Branch To", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueList.addContainerProperty("Received", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblIssueList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblIssueList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblIssueList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblIssueId.add(ar, new Label());
			tbLblIssueId.get(ar).setWidth("100%");
			tbLblIssueId.get(ar).setImmediate(true);
			tbLblIssueId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblIssueNo.add(ar, new Label());
			tbLblIssueNo.get(ar).setWidth("100%");
			tbLblIssueNo.get(ar).setImmediate(true);
			tbLblIssueNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);
			tbLblIssueNo.get(ar).setDescription("Issue No(Requsition No)");

			tbLblIssueDate.add(ar, new Label());
			tbLblIssueDate.get(ar).setWidth("100%");
			tbLblIssueDate.get(ar).setImmediate(true);
			tbLblIssueDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblTotalItem.add(ar, new Label());
			tbLblTotalItem.get(ar).setWidth("100%");
			tbLblTotalItem.get(ar).setImmediate(true);
			tbLblTotalItem.get(ar).addStyleName(ValoTheme.LABEL_TINY);
			tbLblTotalItem.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbLblAmount.add(ar, new Label());
			tbLblAmount.get(ar).setWidth("100%");
			tbLblAmount.get(ar).setImmediate(true);
			tbLblAmount.get(ar).addStyleName(ValoTheme.LABEL_TINY);
			tbLblAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbLblBranchFrom.add(ar, new Label());
			tbLblBranchFrom.get(ar).setWidth("100%");
			tbLblBranchFrom.get(ar).setImmediate(true);
			tbLblBranchFrom.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblBranchTo.add(ar, new Label());
			tbLblBranchTo.get(ar).setWidth("100%");
			tbLblBranchTo.get(ar).setImmediate(true);
			tbLblBranchTo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbChkActive.add(ar, new CheckBox());
			tbChkActive.get(ar).setWidth("100%");
			tbChkActive.get(ar).setImmediate(true);
			tbChkActive.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbChkActive.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);


			tbChkStatus.add(ar, new CheckBox());
			tbChkStatus.get(ar).setWidth("100%");
			tbChkStatus.get(ar).setImmediate(true);
			tbChkStatus.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbChkStatus.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

			tbCmbAction.add(ar, new ComboBox());
			tbCmbAction.get(ar).setWidth("100%");
			tbCmbAction.get(ar).setImmediate(true);
			tbCmbAction.get(ar).addStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbAction.get(ar).addItem("Preview");
			tbCmbAction.get(ar).setItemIcon("Preview", FontAwesome.FILE_PDF_O);
			tbCmbAction.get(ar).setTextInputAllowed(false);
			tbCmbAction.get(ar).setNullSelectionAllowed(false);
			tbCmbAction.get(ar).setInputPrompt("Action");
			if (cm.update)
			{
				tbCmbAction.get(ar).addItem("Active/Inactive");
				tbCmbAction.get(ar).setItemIcon("Active/Inactive", FontAwesome.LOCK);
				tbCmbAction.get(ar).addItem("Edit");
				tbCmbAction.get(ar).setItemIcon("Edit", FontAwesome.EDIT);
				tbCmbAction.get(ar).addItem("Received");
				tbCmbAction.get(ar).setItemIcon("Received", FontAwesome.RECYCLE);

				if (sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin())
				{
					tbCmbAction.get(ar).addItem("Cancel");
					tbCmbAction.get(ar).setItemIcon("Cancel", FontAwesome.CROSSHAIRS);

					tbCmbAction.get(ar).addItem("Approve");
					tbCmbAction.get(ar).setItemIcon("Approve", FontAwesome.CHECK);
				}
			}

			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String issueId = tbLblIssueId.get(ar).getValue().toString();
				if (!issueId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ addEditWindow("Edit", issueId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Active/Inactive"))
					{ ActiveInactiveSelectIssue(issueId, ar); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Preview"))
					{ viewReportSingle(issueId); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Cancel"))
					{ TransactionCancelWindow(issueId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Approve"))
					{ TransactionApproveWindow(issueId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Received"))
					{ ReceivedSelectIssue(issueId, ar); }
				}
				tbCmbAction.get(ar).select(null);
			});

			tblIssueList.addItem(new Object[]{tbLblIssueId.get(ar), tbLblIssueNo.get(ar),tbLblIssueDate.get(ar),
					tbLblTotalItem.get(ar), tbLblAmount.get(ar), tbLblBranchFrom.get(ar), tbLblBranchTo.get(ar),
					tbChkStatus.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private Panel addReport()
	{
		Panel panelReport = new Panel("Issue Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");

		HorizontalLayout content = new  HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 4);
		lay.setSpacing(true);

		txtReportFromDate  = new PopupDateField();
		txtReportFromDate.setImmediate(true);
		txtReportFromDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtReportFromDate.setValue(new Date());
		txtReportFromDate.setWidth("110px");
		txtReportFromDate.setDateFormat("dd-MM-yyyy");
		txtReportFromDate.setRequired(true);
		txtReportFromDate.setRequiredError("This field is required.");
		lay.addComponent(new Label("From Date: "), 0, 0);
		lay.addComponent(txtReportFromDate, 1, 0);

		txtReportToDate  = new PopupDateField();
		txtReportToDate.setImmediate(true);
		txtReportToDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtReportToDate.setValue(new Date());
		txtReportToDate.setWidth("110px");
		txtReportToDate.setDateFormat("dd-MM-yyyy");
		txtReportToDate.setRequired(true);
		txtReportToDate.setRequiredError("This field is required.");
		lay.addComponent(new Label("To Date: "), 0, 1);
		lay.addComponent(txtReportToDate, 1, 1);

		cmbItemNameForReport = new MultiComboBox();
		cmbItemNameForReport.setWidth("350px");
		cmbItemNameForReport.setInputPrompt("Select Item Name");
		cmbItemNameForReport.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbItemNameForReport.setFilteringMode(FilteringMode.CONTAINS);
		cmbItemNameForReport.setRequired(true);
		cmbItemNameForReport.setRequiredError("This field is required.");
		lay.addComponent(new Label("Item Name : "), 0, 2);
		lay.addComponent(cmbItemNameForReport, 1, 2);

		lay.addComponent(cBtnV, 1, 3);
		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelReport.setContent(content);

		return panelReport;
	}

	public void enter(ViewChangeEvent event)
	{
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
		loadTableInfo();

		loadItemReport();
	}
}
