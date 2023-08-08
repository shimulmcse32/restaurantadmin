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
package com.example.posreport;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.MultiComboBox;
import com.common.share.ReportViewer;
import com.common.share.SessionBean;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class IssueAndReturn extends VerticalLayout implements View
{
	private SessionBean sessionBean;

	//Date Wise Issue Summary Report
	private CommonButton cBtnIssue = new CommonButton("", "", "", "", "", "", "", "View", "");
	private PopupDateField txtFromDateIssue, txtToDateIssue;
	private OptionGroup ogReportType;
	private Panel panelIssue;

	//Item Wise Issue Summary Report
	private CommonButton cBtnItem = new CommonButton("", "", "", "", "", "", "", "View", "");
	private PopupDateField txtFromDateItem, txtToDateItem;
	private MultiComboBox cmbItemName;
	private ComboBox cmbBranchItem;
	private OptionGroup ogReportItemType;
	private Label lblType;
	private Panel panelItem;

	private CommonMethod cm;

	public IssueAndReturn(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		addComponents(addIssue(), addItemIssue());

		loadComboBoxs();
	}

	private void loadComboBoxs()
	{
		String sqlItem = "select vItemId, vItemCode, vItemName, dbo.funGetNumeric(vItemCode) iSerial"+
				" from master.tbRawItemInfo order by iSerial, vItemName", caption = "";
		for (Iterator<?> iter = cm.selectSql(sqlItem).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			caption = element[1].toString().isEmpty()? element[2].toString():element[1].toString()+" - "+element[2].toString();
			cmbItemName.addItem(element[0].toString());
			cmbItemName.setItemCaption(element[0].toString(), caption);
		}

		String sqlBranch = "select vBranchId, vBranchName from master.tbBranchMaster order by vBranchName";
		for (Iterator<?> iter = cm.selectSql(sqlBranch).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			cmbBranchItem.addItem("%");
			cmbBranchItem.setItemCaption("%", "ALL");
			cmbBranchItem.addItem(element[0].toString());
			cmbBranchItem.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	//Issue Report Start
	private Panel addIssue()
	{
		panelIssue = new Panel("Date Wise Issue/Return Details :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 4);
		lay.setSpacing(true);

		ogReportType = new OptionGroup();
		ogReportType.addItem("Issue");
		ogReportType.addItem("Return");
		ogReportType.select("Issue");
		ogReportType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Type: "), 0, 0);
		lay.addComponent(ogReportType, 1, 0);

		txtFromDateIssue = new PopupDateField();
		txtFromDateIssue.setImmediate(true);
		txtFromDateIssue.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateIssue.setValue(new Date());
		txtFromDateIssue.setWidth("125px");
		txtFromDateIssue.setDateFormat("dd-MM-yyyy");
		txtFromDateIssue.setDescription("From Date");
		txtFromDateIssue.setInputPrompt("From Date");
		txtFromDateIssue.setRequired(true);
		txtFromDateIssue.setRequiredError("This field is required");
		lay.addComponent(new Label("From Date: "), 0, 1);
		lay.addComponent(txtFromDateIssue, 1, 1);

		txtToDateIssue = new PopupDateField();
		txtToDateIssue.setImmediate(true);
		txtToDateIssue.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateIssue.setValue(new Date());
		txtToDateIssue.setWidth("125px");
		txtToDateIssue.setDateFormat("dd-MM-yyyy");
		txtToDateIssue.setDescription("To Date");
		txtToDateIssue.setInputPrompt("To Date");
		txtToDateIssue.setRequired(true);
		txtToDateIssue.setRequiredError("This field is required");
		lay.addComponent(new Label("To Date: "), 0, 2);
		lay.addComponent(txtToDateIssue, 1, 2);

		lay.addComponent(cBtnIssue, 1, 3);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelIssue.setContent(content);

		addActionsIssue();
		return panelIssue;
	}

	private void addActionsIssue()
	{
		cBtnIssue.btnPreview.addClickListener(event -> addValidationIssue());
	}

	private void addValidationIssue()
	{
		if (txtFromDateIssue.getValue() != null && txtToDateIssue.getValue() != null)
		{ 
			if (ogReportType.getValue().toString().equalsIgnoreCase("Issue"))
			{ viewReportIssue(); }
			else
			{ viewReportReturn(); }
		}
		else
		{
			txtFromDateIssue.focus();
			cm.showNotification("warning", "Warning!", "Invalid date selected.");
		}
	}

	private void viewReportIssue()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateIssue.getValue());
			String toDate = cm.dfDb.format(txtToDateIssue.getValue());
			String branch = sessionBean.getBranchId();
			String datePara = " from "+cm.dfBd.format(txtFromDateIssue.getValue())+" to "+cm.dfBd.format(txtToDateIssue.getValue());

			sql = "select * from funIssueSummary('"+fromDate+"', '"+toDate+"', '"+branch+"') order by dIssueDate, vItemName";
			reportSource = "com/jasper/postransaction/rptIssueDateBetween.jasper";

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			hm.put("sql", sql);
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("userIp", sessionBean.getUserIp());
			hm.put("fromToDate", datePara);
			hm.put("url", Page.getCurrent().getLocation().toString().replaceAll("#!IssueReport%239", "?"));

			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
	//Issue Report End

	//Issue Return Report End
	private void viewReportReturn()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateIssue.getValue());
			String toDate = cm.dfDb.format(txtToDateIssue.getValue());
			String branch = sessionBean.getBranchId();
			String datePara = " from "+cm.dfBd.format(txtFromDateIssue.getValue())+" to "+cm.dfBd.format(txtToDateIssue.getValue());

			sql = "select * from funIssueReturnSummary('"+fromDate+"', '"+toDate+"', '"+branch+"') order by dReturnDate, vItemName";
			reportSource = "com/jasper/postransaction/rptIssueReturnDateBetween.jasper";

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			hm.put("sql", sql);
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("url", Page.getCurrent().getLocation().toString().replaceAll("#!IssueReport%239", "?"));
			hm.put("userIp", sessionBean.getUserIp());
			hm.put("fromToDate", datePara);

			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
	//Issue Return Report End

	//Item Wise Issue Report Start
	private Panel addItemIssue()
	{
		panelItem = new Panel("Item Wise Issue/Return Details :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 6);
		lay.setSpacing(true);

		ogReportItemType = new OptionGroup();
		ogReportItemType.addItem("Issue");
		ogReportItemType.addItem("Return");
		ogReportItemType.select("Issue");
		ogReportItemType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportItemType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Type: "), 0, 0);
		lay.addComponent(ogReportItemType, 1, 0);

		txtFromDateItem = new PopupDateField();
		txtFromDateItem.setImmediate(true);
		txtFromDateItem.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateItem.setValue(new Date());
		txtFromDateItem.setWidth("125px");
		txtFromDateItem.setDateFormat("dd-MM-yyyy");
		txtFromDateItem.setDescription("From Date");
		txtFromDateItem.setInputPrompt("From Date");
		txtFromDateItem.setRequired(true);
		txtFromDateItem.setRequiredError("This field is required");
		lay.addComponent(new Label("From Date: "), 0, 1);
		lay.addComponent(txtFromDateItem, 1, 1);

		txtToDateItem = new PopupDateField();
		txtToDateItem.setImmediate(true);
		txtToDateItem.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateItem.setValue(new Date());
		txtToDateItem.setWidth("125px");
		txtToDateItem.setDateFormat("dd-MM-yyyy");
		txtToDateItem.setDescription("To Date");
		txtToDateItem.setInputPrompt("To Date");
		txtToDateItem.setRequired(true);
		txtToDateItem.setRequiredError("This field is required");
		lay.addComponent(new Label("To Date: "), 0, 2);
		lay.addComponent(txtToDateItem, 1, 2);

		cmbItemName = new MultiComboBox();
		cmbItemName.setWidth("450px");
		cmbItemName.setInputPrompt("Select Item Name");
		cmbItemName.setRequired(true);
		cmbItemName.setRequiredError("This field is required");
		lay.addComponent(new Label("Item Name: "), 0, 3);
		lay.addComponent(cmbItemName, 1, 3);

		cmbBranchItem = new ComboBox();
		cmbBranchItem.setWidth("300px");
		cmbBranchItem.setImmediate(true);
		cmbBranchItem.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbBranchItem.setFilteringMode(FilteringMode.CONTAINS);
		cmbBranchItem.setInputPrompt("Select issued branch name");
		cmbBranchItem.setRequired(true);
		cmbBranchItem.setRequiredError("This field is required");
		lblType = new Label("Issued To: ");
		lay.addComponent(lblType, 0, 4);
		lay.addComponent(cmbBranchItem, 1, 4);

		lay.addComponent(cBtnItem, 1, 5);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelItem.setContent(content);

		addActionsItemIssue();
		return panelItem;
	}

	private void addActionsItemIssue()
	{
		ogReportItemType.addValueChangeListener(event ->
		{
			String type = ogReportItemType.getValue().toString();
			lblType.setValue(type.equals("Issue")?"Issued To: ":"Returned From: ");
		});

		cBtnItem.btnPreview.addClickListener(event -> addValidationItemIssue());
	}

	private void addValidationItemIssue()
	{
		if (txtFromDateItem.getValue() != null && txtToDateItem.getValue() != null)
		{
			if (!cmbItemName.getValue().toString().replace("[", "").replace("]", "").isEmpty())
			{
				if (cmbBranchItem.getValue() != null)
				{ viewReportItem(); }
				else
				{
					cmbBranchItem.focus();
					cm.showNotification("warning", "Warning!", "Select issued branch name.");
				}
			}
			else
			{
				cmbItemName.focus();
				cm.showNotification("warning", "Warning!", "Select item name.");
			}
		}
		else
		{
			txtFromDateItem.focus();
			cm.showNotification("warning", "Warning!", "Invalid date selected.");
		}
	}

	private void viewReportItem()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateItem.getValue());
			String toDate = cm.dfDb.format(txtToDateItem.getValue());
			String branch = sessionBean.getBranchId();
			String ItemIds = cmbItemName.getValue().toString().replace("]", "").replace("[", "").trim();
			String datePara = cm.dfBd.format(txtFromDateItem.getValue())+" To "+
					cm.dfBd.format(txtToDateItem.getValue());
			if (ogReportItemType.getValue().toString().equals("Issue"))
			{
				sql = "select isd.vItemId, rii.vItemCode+' - '+rii.vItemName vItemDetails, uni.vUnitName, SUM(isd.mIssueQty)"+
						" mIssueQty, AVG(isd.mMainRate) mMainRate, isd.mCostMargin, AVG(isd.mIssueRate)mIssueRate, SUM(isd.mAmount)"+
						" mAmount, dbo.funGetNumeric(rii.vItemCode) iSerial from trans.tbIssueInfo isi, trans.tbIssueDetails"+
						" isd, master.tbRawItemInfo rii, master.tbUnitInfo uni where isi.vIssueId = isd.vIssueId and isd.vItemId"+
						" = rii.vItemId and isd.vUnitId = convert(varchar(10), uni.iUnitId) and isd.vItemId in (select Item"+
						" from dbo.Split('"+ItemIds+"')) and isi.dIssueDate between '"+fromDate+"' and '"+toDate+"' and isi.vBranchTo"+
						" like '"+cmbBranchItem.getValue().toString()+"' and isi.vBranchFrom = '"+branch+"' group by isd.vItemId,"+
						" rii.vItemCode+' - '+rii.vItemName, uni.vUnitName, isd.mCostMargin, dbo.funGetNumeric(rii.vItemCode)"+
						" order by iSerial";
				reportSource = "com/jasper/postransaction/rptItemWiseIssueSummary.jasper";
			}
			else
			{
				sql = "select ird.vItemId, rii.vItemCode+' - '+rii.vItemName vItemDetails, uni.vUnitName, SUM(ird.mIssueQty)"+
						" mIssueQty, SUM(ird.mReturnedQty)mReturnedQty, AVG(ird.mMainRate) mMainRate, ird.mCostMargin,"+
						" AVG(ird.mIssueRate)mIssueRate, SUM(ird.mAmount) mAmount, dbo.funGetNumeric(rii.vItemCode) iSerial"+
						" from trans.tbIssueReturnInfo iri, trans.tbIssueReturnDetails ird, master.tbRawItemInfo rii,"+
						" master.tbUnitInfo uni where iri.vIssueReturnId = ird.vIssueReturnId and ird.vItemId = rii.vItemId"+
						" and ird.vUnitId = convert(varchar(10), uni.iUnitId) and ird.vItemId in (select Item from"+
						" dbo.Split('"+ItemIds+"')) and iri.dReturnDate between '"+fromDate+"' and '"+toDate+"' and"+
						" iri.vReturnFrom like '"+cmbBranchItem.getValue().toString()+"' and iri.vReturnTo = '"+branch+"'"+
						" group by ird.vItemId, rii.vItemCode+' - '+rii.vItemName, uni.vUnitName, ird.mCostMargin,"+
						" dbo.funGetNumeric(rii.vItemCode) order by iSerial";
				reportSource = "com/jasper/postransaction/rptItemWiseReturnSummary.jasper";
			}

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("branchTo", cmbBranchItem.getItemCaption(cmbBranchItem.getValue()).toString());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			hm.put("sql", sql);
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("userIp", sessionBean.getUserIp());
			hm.put("fromToDate", datePara);

			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
	//Item Wise Issue Report End

	public void enter(ViewChangeEvent event)
	{ loadComboBoxs(); }
}
