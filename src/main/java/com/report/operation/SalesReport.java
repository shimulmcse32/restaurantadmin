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
package com.report.operation;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.ExcelGenerator;
import com.common.share.MultiComboBox;
import com.common.share.ReportViewer;
import com.common.share.SessionBean;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
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
public class SalesReport extends VerticalLayout implements View
{
	private SessionBean sessionBean;

	//Closing Report
	private CommonButton cBtnClose = new CommonButton("", "", "", "", "", "", "", "View", "");
	private PopupDateField txtFromDateClose, txtToDateClose;
	private OptionGroup ogInvoiceTypeClose, ogReportFormatClose, ogInvoiceFrom;
	private ComboBox cmbBranchClose, cmbUserClose;
	private Panel panelClose;

	//Sales report invoice
	private CommonButton cBtnSales = new CommonButton("", "", "", "", "", "", "", "View", "");
	private PopupDateField txtFromDateSales, txtToDateSales;
	private OptionGroup ogInvoiceTypeSales, ogReportTypeSales, ogInvoiceFromS;
	private ComboBox cmbBranchSales, cmbUserSales, cmbSalesTypeSales;
	private Panel panelSales;

	//Sales report menu
	private CommonButton cBtnMenu = new CommonButton("", "", "", "", "", "", "", "View", "");
	private PopupDateField txtFromDateMenu, txtToDateMenu;
	private OptionGroup ogInvoiceFromM;
	private ComboBox cmbBranchMenu, cmbSalesTypeMenu;
	private Panel panelMenu;

	//Sales report invoice terminal wise
	private CommonButton cBtnTerminal = new CommonButton("", "", "", "", "", "", "", "View", "");
	private PopupDateField txtFromDateTerminal, txtToDateTerminal;
	private OptionGroup ogInvoiceTypeTerminal, ogReportTypeTerminal, ogInvoiceFromT;
	private ComboBox cmbBranchTerminal, cmbUserTerminal, cmbSalesTypeTerminal;
	private MultiComboBox cmbTerminal;
	private Panel panelTerminal;

	private CommonMethod cm;

	public SalesReport(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		addComponents(addClosingReport(), addSalesReport(), addSalesMenu(), addTerminalReport());

		addActionsReport();
	}

	//Sales Report Start
	private void addActionsReport()
	{
		loadComboData();
		cBtnClose.btnPreview.addClickListener(event -> addValidationClosing());

		cBtnSales.btnPreview.addClickListener(event -> addValidationSales());

		cBtnMenu.btnPreview.addClickListener(event -> addValidationMenu());

		cBtnTerminal.btnPreview.addClickListener(event -> addValidationTerminal());

		cmbBranchTerminal.addValueChangeListener(event ->
		{ 
			String branchId = cmbBranchTerminal.getValue().toString();
			if (branchId != null)
			{ loadComboTerminalData(branchId); }
		});
	}

	private void loadComboData()
	{
		String sqlBranch = sessionBean.isCentralbranch()?"select '%' vBranchId, 'ALL' vBranchName union all":"";
		sqlBranch += " select vBranchId, vBranchName from master.tbBranchMaster where vBranchId like '"+
				(sessionBean.isCentralbranch()?"%":sessionBean.getBranchId())+"' order by vBranchName";
		for(Iterator<?> iter = cm.selectSql(sqlBranch).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbBranchClose.addItem(element[0].toString());
			cmbBranchClose.setItemCaption(element[0].toString(), element[1].toString());

			cmbBranchSales.addItem(element[0].toString());
			cmbBranchSales.setItemCaption(element[0].toString(), element[1].toString());

			cmbBranchMenu.addItem(element[0].toString());
			cmbBranchMenu.setItemCaption(element[0].toString(), element[1].toString());

			cmbBranchTerminal.addItem(element[0].toString());
			cmbBranchTerminal.setItemCaption(element[0].toString(), element[1].toString());
		}
		cmbBranchClose.select(sessionBean.getBranchId());
		cmbBranchClose.setEnabled((sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()));

		cmbBranchSales.select(sessionBean.getBranchId());
		cmbBranchSales.setEnabled((sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()));

		cmbBranchMenu.select(sessionBean.getBranchId());
		cmbBranchMenu.setEnabled((sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()));

		cmbBranchTerminal.select(sessionBean.getBranchId());
		cmbBranchTerminal.setEnabled((sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()));

		String sqlUser = "select vUserId, vFullName from master.tbUserInfo order by vFullName";
		for(Iterator<?> iter = cm.selectSql(sqlUser).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbUserClose.addItem(element[0].toString());
			cmbUserClose.setItemCaption(element[0].toString(), element[1].toString());

			cmbUserSales.addItem(element[0].toString());
			cmbUserSales.setItemCaption(element[0].toString(), element[1].toString());

			cmbUserTerminal.addItem(element[0].toString());
			cmbUserTerminal.setItemCaption(element[0].toString(), element[1].toString());
		}

		String sqlSalesType = "select iSalesTypeId, vSalesType from master.tbSalesType where"+
				" vFlag = 'POS' order by iSalesTypeId";
		for(Iterator<?> iter = cm.selectSql(sqlSalesType).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSalesTypeSales.addItem(element[0].toString());
			cmbSalesTypeSales.setItemCaption(element[0].toString(), element[1].toString());

			cmbSalesTypeMenu.addItem(element[0].toString());
			cmbSalesTypeMenu.setItemCaption(element[0].toString(), element[1].toString());

			cmbSalesTypeTerminal.addItem(element[0].toString());
			cmbSalesTypeTerminal.setItemCaption(element[0].toString(), element[1].toString());
		}	
	}

	private void loadComboTerminalData(String BranchId)
	{
		cmbTerminal.removeAllItems();
		String sqlTerminal = "select distinct ti.vTerminalCode, ti.vTerminal from trans.tbInvoiceInfo ini"+
				" inner join master.tbTerminalConfig ti on ini.vTerminalName = ti.vTerminal where "+
				" ini.vBranchId like '"+BranchId+"' order by ti.vTerminal";
		for(Iterator<?> iter = cm.selectSql(sqlTerminal).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbTerminal.addItem(element[1].toString());
			cmbTerminal.setItemCaption(element[1].toString(), element[1].toString());
		}
	}

	//Closing report starts
	private void addValidationClosing()
	{
		if (txtFromDateClose.getValue() != null && txtToDateClose.getValue() != null)
		{ viewReportClosing(); }
		else
		{
			txtFromDateClose.focus();
			cm.showNotification("warning", "Warning!", "Invalid date selected.");
		}
	}

	@SuppressWarnings("deprecation")
	private void viewReportClosing()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", fileName = "", xsql = "";
		try
		{
			String fromDate = cm.dfDbHMA.format(txtFromDateClose.getValue());
			String toDate = cm.dfDbHMA.format(txtToDateClose.getValue());
			String userId = cmbUserClose.getValue().toString();
			String branch = cmbBranchClose.getValue().toString();
			String invType = ogInvoiceTypeClose.getValue().toString().equals("Pending")?"1":"2, 3, 4, 5, 6";
			String invFrom = ogInvoiceFrom.getValue().toString().equals("All")? "%":
				ogInvoiceFrom.getValue().toString().equals("POS")?"":"apps";
			String datePara = cm.dfBd.format(txtFromDateClose.getValue())+" to "+cm.dfBd.format(txtToDateClose.getValue());

			sql = "select * from funClosingReport('"+userId+"', '"+branch+"',"+
					" '"+fromDate+"', '"+toDate+"', '"+invType+"', '"+invFrom+"') order by iSerial, mNumber desc";
			reportSource = "com/jasper/operation/rptClosingReportPOS.jasper";
			//System.out.println(sql);
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("branchName", cmbBranchClose.getItemCaption(cmbBranchClose.getValue()));
			hm.put("invType", ogInvoiceTypeClose.getValue().toString());
			hm.put("invFrom", ogInvoiceFrom.getValue().toString().equals("All")? "Both POS & Apps":ogInvoiceFrom.getValue().toString());
			hm.put("dateTime", new Date());
			hm.put("fromDate", cm.dfBd.format(txtFromDateClose.getValue()));
			hm.put("toDate", cm.dfBd.format(txtToDateClose.getValue()));
			hm.put("fromTime", cm.dfT.format(txtFromDateClose.getValue()));
			hm.put("toTime", cm.dfT.format(txtToDateClose.getValue()));

			if (ogReportFormatClose.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("logo", sessionBean.getCompanyLogo());
				hm.put("userIp", sessionBean.getUserIp());

				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = sql;
				fileName = "Sales_Details_"+datePara;
				hm.put("parameters", "Sales From: "+datePara);

				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private Panel addClosingReport()
	{
		panelClose = new Panel("Closing Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(3, 7);
		lay.setSpacing(true);

		cmbBranchClose = new ComboBox();
		cmbBranchClose.setNullSelectionAllowed(false);
		cmbBranchClose.setWidth("300px");
		cmbBranchClose.setInputPrompt("Select branch name");
		cmbBranchClose.setImmediate(true);
		cmbBranchClose.setRequired(true);
		cmbBranchClose.setRequiredError("This field is required.");
		cmbBranchClose.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbBranchClose.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Branch Name: "), 0, 0);
		lay.addComponent(cmbBranchClose, 1, 0, 2, 0);

		txtFromDateClose = new PopupDateField();
		txtFromDateClose.setImmediate(true);
		txtFromDateClose.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateClose.setValue(new Date());
		txtFromDateClose.setResolution(Resolution.MINUTE);
		txtFromDateClose.setWidth("160px");
		txtFromDateClose.setDateFormat("dd-MM-yyyy hh:mm aa");
		txtFromDateClose.setDescription("From Date");
		txtFromDateClose.setInputPrompt("From Date");
		txtFromDateClose.setRequired(true);
		txtFromDateClose.setRequiredError("This field is required");
		lay.addComponent(new Label("Date(s): "), 0, 1);
		lay.addComponent(txtFromDateClose, 1, 1);

		txtToDateClose = new PopupDateField();
		txtToDateClose.setImmediate(true);
		txtToDateClose.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateClose.setValue(new Date());
		txtToDateClose.setResolution(Resolution.MINUTE);
		txtToDateClose.setWidth("160px");
		txtToDateClose.setDateFormat("dd-MM-yyyy hh:mm aa");
		txtToDateClose.setDescription("To Date");
		txtToDateClose.setInputPrompt("To Date");
		txtToDateClose.setRequired(true);
		txtToDateClose.setRequiredError("This field is required");
		lay.addComponent(txtToDateClose, 2, 1);

		ogInvoiceTypeClose = new OptionGroup();
		ogInvoiceTypeClose.addItem("Pending");
		ogInvoiceTypeClose.addItem("Settled");
		ogInvoiceTypeClose.select("Pending");
		ogInvoiceTypeClose.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogInvoiceTypeClose.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Invoice Type: "), 0, 2);
		lay.addComponent(ogInvoiceTypeClose, 1, 2, 2, 2);

		ogInvoiceFrom = new OptionGroup();
		ogInvoiceFrom.addItem("All");
		ogInvoiceFrom.addItem("POS");
		ogInvoiceFrom.addItem("Apps");
		ogInvoiceFrom.select("All");
		ogInvoiceFrom.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogInvoiceFrom.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Invoice From: "), 0, 3);
		lay.addComponent(ogInvoiceFrom, 1, 3, 2, 3);

		cmbUserClose = new ComboBox();
		cmbUserClose.setNullSelectionAllowed(false);
		cmbUserClose.addItem("%");
		cmbUserClose.setItemCaption("%", "ALL");
		cmbUserClose.select("%");
		cmbUserClose.setWidth("300px");
		cmbUserClose.setInputPrompt("Select user name");
		cmbUserClose.setImmediate(true);
		cmbUserClose.setRequired(true);
		cmbUserClose.setRequiredError("This field is required.");
		cmbUserClose.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbUserClose.setFilteringMode(FilteringMode.CONTAINS);
		Label lbl = new Label("Cashier Name: ");
		lbl.setWidth("-1px");
		lay.addComponent(lbl, 0, 4);
		lay.addComponent(cmbUserClose, 1, 4, 2, 4);

		ogReportFormatClose = new OptionGroup();
		ogReportFormatClose.addItem("PDF");
		ogReportFormatClose.addItem("XLS");
		ogReportFormatClose.select("PDF");
		ogReportFormatClose.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormatClose.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogReportFormatClose, 1, 5, 2, 5);

		lay.addComponent(cBtnClose, 0, 6, 2, 6);
		lay.setComponentAlignment(cBtnClose, Alignment.MIDDLE_CENTER);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelClose.setContent(content);

		return panelClose;
	}
	//Closing Report Ends

	//Sales report starts
	private void addValidationSales()
	{
		if (txtFromDateSales.getValue() != null && txtToDateSales.getValue() != null)
		{ viewReportSales(); }
		else
		{
			txtFromDateSales.focus();
			cm.showNotification("warning", "Warning!", "Invalid date selected.");
		}
	}

	private void viewReportSales()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateSales.getValue());
			String toDate = cm.dfDb.format(txtToDateSales.getValue());
			String branch = cmbBranchSales.getValue().toString();
			String saleType = cmbSalesTypeSales.getValue().toString();
			String userId = cmbUserSales.getValue().toString();
			String invType = ogInvoiceTypeSales.getValue().toString().equals("Pending")?"1":"2, 3, 4, 5, 6";
			String datePara = cm.dfBd.format(txtFromDateSales.getValue())+" to "+cm.dfBd.format(txtToDateSales.getValue());
			String reportType = ogReportTypeSales.getValue().toString();
			String invFrom = ogInvoiceFromS.getValue().toString().equals("All")? "%":
				ogInvoiceFromS.getValue().toString().equals("POS")?"":"apps";

			if (reportType.equals("Details(Invoice Wise)"))
			{
				sql = "select ini.vInvoiceNo, convert(date, ini.dSaveDate) dInvoiceDate, SUM(ind.mNetAmount) mAmount,"+
						" SUM(ind.mDisCalculated) mDiscount, SUM(ind.mVoidCalculated) mVoid, SUM(ind.mAmountWithoutVat)"+
						" mAmountWoVat, SUM(ind.mTotalVatAmount) mVatAmount, SUM(ind.mFinalAmount) mNetAmount, st.vSalesType,"+
						" ui.vFullName from trans.tbInvoiceInfo ini, trans.tbInvoiceDetails ind, master.tbUserInfo ui,"+
						" master.tbSalesType st where ini.vInvoiceId = ind.vInvoiceId and ini.vModifiedBy = ui.vUserId"+
						" and ini.iSalesTypeId = st.iSalesTypeId and convert(date, ini.dSaveDate) between '"+fromDate+"'"+
						" and '"+toDate+"' and ini.vModifiedBy like '"+userId+"' and ini.vBranchId like '"+branch+"' and"+
						" ini.iSalesTypeId like '"+saleType+"' and ini.vSplitTicketId like '"+invFrom+"' and ini.iStatusId"+
						" in ("+invType+") group by ini.vInvoiceNo, convert(date, ini.dSaveDate), st.vSalesType, ui.vFullName"+
						" order by st.vSalesType, dInvoiceDate, ui.vFullName";
				reportSource = "com/jasper/operation/rptSalesDetails.jasper";
			}
			else if (reportType.equals("Details(Menu Wise)"))
			{
				sql = "select ini.vInvoiceNo, convert(date, ini.dSaveDate) dInvoiceDate, fin.vItemName, SUM(ind.mNetAmount)"+
						" mAmount, SUM(ind.mDisCalculated) mDiscount, SUM(ind.mVoidCalculated) mVoid, SUM(ind.mAmountWithoutVat)"+
						" mAmountWoVat, SUM(ind.mTotalVatAmount) mVatAmount, SUM(ind.mFinalAmount) mNetAmount, st.vSalesType,"+
						" ui.vFullName from trans.tbInvoiceInfo ini, trans.tbInvoiceDetails ind, master.tbUserInfo ui,"+
						" master.tbSalesType st, master.tbFinishedItemInfo fin where ini.vInvoiceId = ind.vInvoiceId and"+
						" ini.vModifiedBy = ui.vUserId and ini.iSalesTypeId = st.iSalesTypeId and ind.vItemId = fin.vItemId"+
						" and convert(date, ini.dSaveDate) between '"+fromDate+"' and '"+toDate+"'"+
						" and ini.vModifiedBy like '"+userId+"' and ini.vBranchId like '"+branch+"' and ini.iSalesTypeId"+
						" like '"+saleType+"' and ini.vSplitTicketId like '"+invFrom+"' and ini.iStatusId in ("+invType+")"+
						" group by ini.vInvoiceNo, fin.vItemName, convert(date, ini.dSaveDate), st.vSalesType, ui.vFullName"+
						" order by st.vSalesType, dInvoiceDate, ui.vFullName";
				reportSource = "com/jasper/operation/rptSalesDetailsWithItems.jasper";
			}
			else
			{
				sql = "select convert(date, ini.dSaveDate) dInvoiceDate, SUM(ind.mNetAmount) mAmount, SUM(ind.mDisCalculated)"+
						" mDiscount, SUM(ind.mVoidCalculated) mVoid, SUM(ind.mAmountWithoutVat) mAmountWoVat, SUM(ind.mTotalVatAmount)"+
						" mVatAmount, SUM(ind.mFinalAmount) mNetAmount, st.vSalesType, ui.vFullName from trans.tbInvoiceInfo ini,"+
						" trans.tbInvoiceDetails ind, master.tbUserInfo ui, master.tbSalesType st where ini.vInvoiceId ="+
						" ind.vInvoiceId and ini.vModifiedBy = ui.vUserId and ini.iSalesTypeId = st.iSalesTypeId and convert(date,"+
						" ini.dSaveDate) between '"+fromDate+"' and '"+toDate+"' and ini.vModifiedBy like '"+userId+"' and"+
						" ini.vBranchId like '"+branch+"' and ini.iSalesTypeId like '"+saleType+"' and ini.vSplitTicketId like '"+invFrom+"' and"+
						" ini.iStatusId in ("+invType+") group by convert(date, ini.dSaveDate), st.vSalesType,"+
						" ui.vFullName order by st.vSalesType, dInvoiceDate, ui.vFullName";
				reportSource = "com/jasper/operation/rptSalesSummary.jasper";
			}
			System.out.println(sql);
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("branchName", cmbBranchSales.getItemCaption(cmbBranchSales.getValue()));

			hm.put("sql", sql);
			hm.put("userName", sessionBean.getFullName());
			hm.put("developerInfo", sessionBean.getDeveloper());
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", sessionBean.getUserIp());
			hm.put("fromToDate", datePara);

			hm.put("salesType", cmbSalesTypeSales.getItemCaption(cmbSalesTypeSales.getValue()));
			hm.put("invType", ogInvoiceTypeSales.getValue().toString());
			hm.put("invFrom", ogInvoiceFromS.getValue().toString().equals("All")? "Both POS & Apps":ogInvoiceFromS.getValue().toString());

			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private Panel addSalesReport()
	{
		panelSales = new Panel("Sales Report (Invoices) :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(5, 8);
		lay.setSpacing(true);

		cmbBranchSales = new ComboBox();
		cmbBranchSales.setNullSelectionAllowed(false);
		cmbBranchSales.setWidth("300px");
		cmbBranchSales.setInputPrompt("Select branch name");
		cmbBranchSales.setImmediate(true);
		cmbBranchSales.setRequired(true);
		cmbBranchSales.setRequiredError("This field is required.");
		cmbBranchSales.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbBranchSales.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Branch Name: "), 0, 0);
		lay.addComponent(cmbBranchSales, 1, 0, 4, 0);

		txtFromDateSales = new PopupDateField();
		txtFromDateSales.setImmediate(true);
		txtFromDateSales.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateSales.setValue(new Date());
		txtFromDateSales.setWidth("120px");
		txtFromDateSales.setDateFormat("dd-MM-yyyy");
		txtFromDateSales.setDescription("From Date");
		txtFromDateSales.setInputPrompt("From Date");
		txtFromDateSales.setRequired(true);
		txtFromDateSales.setRequiredError("This field is required");
		lay.addComponent(new Label("Date(s): "), 0, 1);
		lay.addComponent(txtFromDateSales, 1, 1);

		txtToDateSales = new PopupDateField();
		txtToDateSales.setImmediate(true);
		txtToDateSales.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateSales.setValue(new Date());
		txtToDateSales.setWidth("120px");
		txtToDateSales.setDateFormat("dd-MM-yyyy");
		txtToDateSales.setDescription("To Date");
		txtToDateSales.setInputPrompt("To Date");
		txtToDateSales.setRequired(true);
		txtToDateSales.setRequiredError("This field is required");
		lay.addComponent(txtToDateSales, 2, 1);

		cmbSalesTypeSales = new ComboBox();
		cmbSalesTypeSales.setNullSelectionAllowed(false);
		cmbSalesTypeSales.addItem("%");
		cmbSalesTypeSales.setItemCaption("%", "ALL");
		cmbSalesTypeSales.select("%");
		cmbSalesTypeSales.setWidth("180px");
		cmbSalesTypeSales.setInputPrompt("Select sales type");
		cmbSalesTypeSales.setImmediate(true);
		cmbSalesTypeSales.setRequired(true);
		cmbSalesTypeSales.setRequiredError("This field is required.");
		cmbSalesTypeSales.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbSalesTypeSales.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Sales Type: "), 0, 2);
		lay.addComponent(cmbSalesTypeSales, 1, 2, 4, 2);

		ogInvoiceTypeSales = new OptionGroup();
		ogInvoiceTypeSales.addItem("Pending");
		ogInvoiceTypeSales.addItem("Settled");
		ogInvoiceTypeSales.select("Pending");
		ogInvoiceTypeSales.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogInvoiceTypeSales.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Invoice Type: "), 0, 3);
		lay.addComponent(ogInvoiceTypeSales, 1, 3, 4, 3);

		ogInvoiceFromS = new OptionGroup();
		ogInvoiceFromS.addItem("All");
		ogInvoiceFromS.addItem("POS");
		ogInvoiceFromS.addItem("Apps");
		ogInvoiceFromS.select("All");
		ogInvoiceFromS.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogInvoiceFromS.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Invoice From: "), 0, 4);
		lay.addComponent(ogInvoiceFromS, 1, 4, 2, 4);

		ogReportTypeSales = new OptionGroup();
		ogReportTypeSales.addItem("Details(Invoice Wise)");
		ogReportTypeSales.addItem("Details(Menu Wise)");
		ogReportTypeSales.addItem("Summary(Date Wise)");
		ogReportTypeSales.select("Details(Invoice Wise)");
		ogReportTypeSales.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportTypeSales.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Report Type: "), 0, 5);
		lay.addComponent(ogReportTypeSales, 1, 5, 4, 5);

		cmbUserSales = new ComboBox();
		cmbUserSales.setNullSelectionAllowed(false);
		cmbUserSales.addItem("%");
		cmbUserSales.setItemCaption("%", "ALL");
		cmbUserSales.select("%");
		cmbUserSales.setWidth("300px");
		cmbUserSales.setInputPrompt("Select user name");
		cmbUserSales.setImmediate(true);
		cmbUserSales.setRequired(true);
		cmbUserSales.setRequiredError("This field is required.");
		cmbUserSales.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbUserSales.setFilteringMode(FilteringMode.CONTAINS);
		Label lbl = new Label("Cashier Name: ");
		lbl.setWidth("-1px");
		lay.addComponent(lbl, 0, 6);
		lay.addComponent(cmbUserSales, 1, 6, 4, 6);

		lay.addComponent(cBtnSales, 0, 7, 4, 7);
		lay.setComponentAlignment(cBtnSales, Alignment.MIDDLE_CENTER);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelSales.setContent(content);

		return panelSales;
	}
	//Sales report ends

	//Menu report starts
	private void addValidationMenu()
	{
		if (txtFromDateMenu.getValue() != null && txtToDateMenu.getValue() != null)
		{ viewReportMenu(); }
		else
		{
			txtFromDateMenu.focus();
			cm.showNotification("warning", "Warning!", "Invalid date selected.");
		}
	}

	private void viewReportMenu()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateMenu.getValue());
			String toDate = cm.dfDb.format(txtToDateMenu.getValue());
			String branch = cmbBranchMenu.getValue().toString();
			String datePara = cm.dfBd.format(txtFromDateMenu.getValue())+" to "+cm.dfBd.format(txtToDateMenu.getValue());
			String saleType = cmbSalesTypeMenu.getValue().toString();
			String invFrom = ogInvoiceFromM.getValue().toString().equals("All")? "%":
				ogInvoiceFromM.getValue().toString().equals("POS")?"":"apps";

			sql = "select ind.vItemId, fi.vItemName, SUM(ind.mQuantity) mQuantity, SUM(ind.mNetAmount) mAmount,"+
					" SUM(ind.mDisCalculated) mDiscount, SUM(ind.mVoidCalculated) mVoid, SUM(ind.mAmountWithoutVat)"+
					" mAmountWoVat, SUM(ind.mTotalVatAmount) mVatAmount, SUM(ind.mFinalAmount) mNetAmount,"+
					" SUM(ind.mFinalAmount) / SUM(ind.mQuantity) mPrice, st.vSalesType from trans.tbInvoiceInfo"+
					" ini, trans.tbInvoiceDetails ind, master.tbSalesType st, master.tbFinishedItemInfo fi where"+
					" ini.vInvoiceId = ind.vInvoiceId and ini.iSalesTypeId = st.iSalesTypeId and ind.vItemId"+
					" = fi.vItemId and convert(date, ini.dSaveDate) between '"+fromDate+"' and"+
					" '"+toDate+"' and ini.vBranchId like '"+branch+"' and ini.iSalesTypeId like '"+saleType+"' and"+
					" ini.vSplitTicketId like '"+invFrom+"' group by ind.vItemId, fi.vItemName, st.vSalesType order by"+
					" st.vSalesType, mQuantity desc";
			reportSource = "com/jasper/operation/rptSalesItems.jasper";
			//System.out.println(sql);

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("branchName", cmbBranchMenu.getItemCaption(cmbBranchMenu.getValue()));

			hm.put("sql", sql);
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", sessionBean.getUserIp());

			hm.put("fromToDate", datePara);
			hm.put("salesType", cmbSalesTypeMenu.getItemCaption(cmbSalesTypeMenu.getValue()));
			hm.put("invFrom", ogInvoiceFromM.getValue().toString().equals("All")? "Both POS & Apps":ogInvoiceFromM.getValue().toString());

			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private Panel addSalesMenu()
	{
		panelMenu = new Panel("Sales Report (Items) :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(3, 5);
		lay.setSpacing(true);

		cmbBranchMenu = new ComboBox();
		cmbBranchMenu.setNullSelectionAllowed(false);
		cmbBranchMenu.setWidth("300px");
		cmbBranchMenu.setInputPrompt("Select branch name");
		cmbBranchMenu.setImmediate(true);
		cmbBranchMenu.setRequired(true);
		cmbBranchMenu.setRequiredError("This field is required.");
		cmbBranchMenu.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbBranchMenu.setFilteringMode(FilteringMode.CONTAINS);
		Label lbl = new Label("Branch Name: ");
		lbl.setWidth("-1px");
		lay.addComponent(lbl, 0, 0);
		lay.addComponent(cmbBranchMenu, 1, 0, 2, 0);

		txtFromDateMenu = new PopupDateField();
		txtFromDateMenu.setImmediate(true);
		txtFromDateMenu.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateMenu.setValue(new Date());
		txtFromDateMenu.setWidth("120px");
		txtFromDateMenu.setDateFormat("dd-MM-yyyy");
		txtFromDateMenu.setDescription("From Date");
		txtFromDateMenu.setInputPrompt("From Date");
		txtFromDateMenu.setRequired(true);
		txtFromDateMenu.setRequiredError("This field is required");
		lay.addComponent(new Label("Date(s): "), 0, 1);
		lay.addComponent(txtFromDateMenu, 1, 1);

		txtToDateMenu = new PopupDateField();
		txtToDateMenu.setImmediate(true);
		txtToDateMenu.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateMenu.setValue(new Date());
		txtToDateMenu.setWidth("120px");
		txtToDateMenu.setDateFormat("dd-MM-yyyy");
		txtToDateMenu.setDescription("To Date");
		txtToDateMenu.setInputPrompt("To Date");
		txtToDateMenu.setRequired(true);
		txtToDateMenu.setRequiredError("This field is required");
		lay.addComponent(txtToDateMenu, 2, 1);

		cmbSalesTypeMenu = new ComboBox();
		cmbSalesTypeMenu.setNullSelectionAllowed(false);
		cmbSalesTypeMenu.addItem("%");
		cmbSalesTypeMenu.setItemCaption("%", "ALL");
		cmbSalesTypeMenu.select("%");
		cmbSalesTypeMenu.setWidth("150px");
		cmbSalesTypeMenu.setInputPrompt("Select user name");
		cmbSalesTypeMenu.setImmediate(true);
		cmbSalesTypeMenu.setRequired(true);
		cmbSalesTypeMenu.setRequiredError("This field is required.");
		cmbSalesTypeMenu.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbSalesTypeMenu.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Sales Type: "), 0, 2);
		lay.addComponent(cmbSalesTypeMenu, 1, 2, 2, 2);

		ogInvoiceFromM = new OptionGroup();
		ogInvoiceFromM.addItem("All");
		ogInvoiceFromM.addItem("POS");
		ogInvoiceFromM.addItem("Apps");
		ogInvoiceFromM.select("All");
		ogInvoiceFromM.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogInvoiceFromM.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Invoice From: "), 0, 3);
		lay.addComponent(ogInvoiceFromM, 1, 3, 2, 3);

		lay.addComponent(cBtnMenu, 0, 4, 2, 4);
		lay.setComponentAlignment(cBtnMenu, Alignment.MIDDLE_CENTER);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelMenu.setContent(content);

		return panelMenu;
	}

	//Terminal report starts
	private void addValidationTerminal()
	{
		if (cmbTerminal.getValue() != null)
		{
			if (txtFromDateTerminal.getValue() != null && txtToDateTerminal.getValue() != null)
			{ viewReportTerminal(); }
			else
			{
				txtFromDateTerminal.focus();
				cm.showNotification("warning", "Warning!", "Invalid date selected.");
			}
		}
		else
		{
			cmbTerminal.focus();
			cm.showNotification("warning", "Warning!", "Select Terminal.");
		}
	}

	private void viewReportTerminal()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateTerminal.getValue());
			String toDate = cm.dfDb.format(txtToDateTerminal.getValue());
			String branch = cmbBranchTerminal.getValue().toString();
			String TerminalId = cmbTerminal.getValue().toString().replace("]", "").replace("[", "").trim();
			String TerminalIds = TerminalId.isEmpty()? "%":TerminalId;
			String saleType = cmbSalesTypeTerminal.getValue().toString();
			String userId = cmbUserTerminal.getValue().toString();
			String invType = ogInvoiceTypeTerminal.getValue().toString().equals("Pending")?"1":"2, 3, 4, 5, 6";
			String datePara = cm.dfBd.format(txtFromDateTerminal.getValue())+" to "+cm.dfBd.format(txtToDateTerminal.getValue());
			String reportType = ogReportTypeTerminal.getValue().toString();
			String invFrom = ogInvoiceFromT.getValue().toString().equals("All")? "%":
				ogInvoiceFromT.getValue().toString().equals("POS")?"":"apps";

			if (reportType.equals("Details(Invoice Wise)"))
			{
				sql = "select ini.vInvoiceNo, convert(date, ini.dSaveDate) dInvoiceDate, SUM(ind.mNetAmount) mAmount,"+
						" SUM(ind.mDisCalculated) mDiscount, SUM(ind.mVoidCalculated) mVoid, SUM(ind.mAmountWithoutVat)"+
						" mAmountWoVat, SUM(ind.mTotalVatAmount) mVatAmount, SUM(ind.mFinalAmount) mNetAmount, st.vSalesType,"+
						" ui.vFullName, (select top 1 vTerminalName from master.tbTerminalInfo where vTerminalName = ini.vTerminalName)"+
						" vTerminalName from trans.tbInvoiceInfo ini, trans.tbInvoiceDetails ind, master.tbUserInfo ui,"+
						" master.tbSalesType st where ini.vInvoiceId = ind.vInvoiceId and ini.vModifiedBy = ui.vUserId"+
						" and ini.iSalesTypeId = st.iSalesTypeId and convert(date, ini.dSaveDate) between '"+fromDate+"'"+
						" and '"+toDate+"' and ini.vModifiedBy like '"+userId+"' and ini.vBranchId like '"+branch+"' and"+
						" ini.iSalesTypeId like '"+saleType+"' and ini.vSplitTicketId like '"+invFrom+"' and ini.iStatusId in ("+invType+") and"+
						" ini.vTerminalName in (select Item from dbo.Split('"+TerminalIds+"')) group by ini.vInvoiceNo,"+
						" convert(date, ini.dSaveDate), st.vSalesType, ui.vFullName, ini.vTerminalName order by ini.vTerminalName,"+
						" st.vSalesType, dInvoiceDate, ui.vFullName";
				reportSource = "com/jasper/operation/rptSalesDetailsTerminalWise.jasper";
			}
			else if (reportType.equals("Details(Menu Wise)"))
			{
				sql = "select ini.vInvoiceNo, convert(date, ini.dSaveDate) dInvoiceDate, fin.vItemName, SUM(ind.mNetAmount)"+
						" mAmount, SUM(ind.mDisCalculated) mDiscount, SUM(ind.mVoidCalculated) mVoid, SUM(ind.mAmountWithoutVat)"+
						" mAmountWoVat, SUM(ind.mTotalVatAmount) mVatAmount, SUM(ind.mFinalAmount) mNetAmount, st.vSalesType,"+
						" ui.vFullName, (select top 1 vTerminalName from master.tbTerminalInfo where vTerminalName = ini.vTerminalName)"+
						" vTerminalName from trans.tbInvoiceInfo ini, trans.tbInvoiceDetails ind, master.tbUserInfo ui,"+
						" master.tbSalesType st, master.tbFinishedItemInfo fin where ini.vInvoiceId = ind.vInvoiceId and"+
						" ini.vModifiedBy = ui.vUserId and ini.iSalesTypeId = st.iSalesTypeId and ind.vItemId = fin.vItemId"+
						" and convert(date, ini.dSaveDate) between '"+fromDate+"' and '"+toDate+"'"+
						" and ini.vModifiedBy like '"+userId+"' and ini.vBranchId like '"+branch+"' and ini.iSalesTypeId"+
						" like '"+saleType+"' and ini.vSplitTicketId like '"+invFrom+"' and ini.iStatusId in ("+invType+") and"+
						" ini.vTerminalName in (select Item from dbo.Split('"+TerminalIds+"')) group by ini.vInvoiceNo, fin.vItemName,"+
						" convert(date, ini.dSaveDate), st.vSalesType, ui.vFullName, ini.vTerminalName order by ini.vTerminalName,"+
						" st.vSalesType, dInvoiceDate, ui.vFullName";
				reportSource = "com/jasper/operation/rptSalesDetailsWithItemsTerminalWise.jasper";
			}
			else
			{
				sql = "select convert(date, ini.dSaveDate) dInvoiceDate, SUM(ind.mNetAmount) mAmount, SUM(ind.mDisCalculated)"+
						" mDiscount, SUM(ind.mVoidCalculated) mVoid, SUM(ind.mAmountWithoutVat) mAmountWoVat, SUM(ind.mTotalVatAmount)"+
						" mVatAmount, SUM(ind.mFinalAmount) mNetAmount, st.vSalesType, ui.vFullName, (select top 1 vTerminalName from"+
						" master.tbTerminalInfo where vTerminalName = ini.vTerminalName) vTerminalName from trans.tbInvoiceInfo ini,"+
						" trans.tbInvoiceDetails ind, master.tbUserInfo ui, master.tbSalesType st where ini.vInvoiceId ="+
						" ind.vInvoiceId and ini.vModifiedBy = ui.vUserId and ini.iSalesTypeId = st.iSalesTypeId and convert(date,"+
						" ini.dSaveDate) between '"+fromDate+"' and '"+toDate+"' and ini.vModifiedBy"+
						" like '"+userId+"' and ini.vBranchId like '"+branch+"' and ini.iSalesTypeId like '"+saleType+"' and"+
						" ini.iStatusId in ("+invType+") and ini.vSplitTicketId like '"+invFrom+"' and ini.vTerminalName in (select"+
						" Item from dbo.Split('"+TerminalIds+"')) group by convert(date, ini.dSaveDate), st.vSalesType, ui.vFullName,"+
						" ini.vTerminalName order by ini.vTerminalName, st.vSalesType, dInvoiceDate, ui.vFullName";
				reportSource = "com/jasper/operation/rptSalesSummaryTerminalWise.jasper";
			}
			//System.out.println(sql);
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("branchName", cmbBranchSales.getItemCaption(cmbBranchSales.getValue()));

			hm.put("sql", sql);
			hm.put("userName", sessionBean.getFullName());
			hm.put("developerInfo", sessionBean.getDeveloper());
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", sessionBean.getUserIp());
			hm.put("fromToDate", datePara);

			hm.put("salesType", cmbSalesTypeSales.getItemCaption(cmbSalesTypeSales.getValue()));
			hm.put("invType", ogInvoiceTypeSales.getValue().toString());
			hm.put("invFrom", ogInvoiceFromT.getValue().toString().equals("All")? "Both POS & Apps":ogInvoiceFromT.getValue().toString());

			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private Panel addTerminalReport()
	{
		panelTerminal = new Panel("Terminal Wise Sales Report (Invoices) :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(5, 9);
		lay.setSpacing(true);

		cmbBranchTerminal = new ComboBox();
		cmbBranchTerminal.setNullSelectionAllowed(false);
		cmbBranchTerminal.setWidth("300px");
		cmbBranchTerminal.setInputPrompt("Select branch name");
		cmbBranchTerminal.setImmediate(true);
		cmbBranchTerminal.setRequired(true);
		cmbBranchTerminal.setRequiredError("This field is required.");
		cmbBranchTerminal.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbBranchTerminal.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Branch Name: "), 0, 0);
		lay.addComponent(cmbBranchTerminal, 1, 0, 4, 0);

		cmbTerminal = new MultiComboBox();
		cmbTerminal.setWidth("350px");
		cmbTerminal.setInputPrompt("Select Terminal");
		cmbTerminal.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbTerminal.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Terminal: "), 0, 1);
		lay.addComponent(cmbTerminal, 1, 1, 4, 1);

		txtFromDateTerminal = new PopupDateField();
		txtFromDateTerminal.setImmediate(true);
		txtFromDateTerminal.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateTerminal.setValue(new Date());
		txtFromDateTerminal.setWidth("120px");
		txtFromDateTerminal.setDateFormat("dd-MM-yyyy");
		txtFromDateTerminal.setDescription("From Date");
		txtFromDateTerminal.setInputPrompt("From Date");
		txtFromDateTerminal.setRequired(true);
		txtFromDateTerminal.setRequiredError("This field is required");
		lay.addComponent(new Label("Date(s): "), 0, 2);
		lay.addComponent(txtFromDateTerminal, 1, 2);

		txtToDateTerminal = new PopupDateField();
		txtToDateTerminal.setImmediate(true);
		txtToDateTerminal.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateTerminal.setValue(new Date());
		txtToDateTerminal.setWidth("120px");
		txtToDateTerminal.setDateFormat("dd-MM-yyyy");
		txtToDateTerminal.setDescription("To Date");
		txtToDateTerminal.setInputPrompt("To Date");
		txtToDateTerminal.setRequired(true);
		txtToDateTerminal.setRequiredError("This field is required");
		lay.addComponent(txtToDateTerminal, 2, 2);

		cmbSalesTypeTerminal = new ComboBox();
		cmbSalesTypeTerminal.setNullSelectionAllowed(false);
		cmbSalesTypeTerminal.addItem("%");
		cmbSalesTypeTerminal.setItemCaption("%", "ALL");
		cmbSalesTypeTerminal.select("%");
		cmbSalesTypeTerminal.setWidth("180px");
		cmbSalesTypeTerminal.setInputPrompt("Select sales type");
		cmbSalesTypeTerminal.setImmediate(true);
		cmbSalesTypeTerminal.setRequired(true);
		cmbSalesTypeTerminal.setRequiredError("This field is required.");
		cmbSalesTypeTerminal.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbSalesTypeTerminal.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Sales Type: "), 0, 3);
		lay.addComponent(cmbSalesTypeTerminal, 1, 3, 4, 3);

		ogInvoiceTypeTerminal = new OptionGroup();
		ogInvoiceTypeTerminal.addItem("Pending");
		ogInvoiceTypeTerminal.addItem("Settled");
		ogInvoiceTypeTerminal.select("Pending");
		ogInvoiceTypeTerminal.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogInvoiceTypeTerminal.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Invoice Type: "), 0, 4);
		lay.addComponent(ogInvoiceTypeTerminal, 1, 4, 4, 4);

		ogInvoiceFromT = new OptionGroup();
		ogInvoiceFromT.addItem("All");
		ogInvoiceFromT.addItem("POS");
		ogInvoiceFromT.addItem("Apps");
		ogInvoiceFromT.select("All");
		ogInvoiceFromT.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogInvoiceFromT.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Invoice From: "), 0, 5);
		lay.addComponent(ogInvoiceFromT, 1, 5, 2, 5);

		ogReportTypeTerminal = new OptionGroup();
		ogReportTypeTerminal.addItem("Details(Invoice Wise)");
		ogReportTypeTerminal.addItem("Details(Menu Wise)");
		ogReportTypeTerminal.addItem("Summary(Date Wise)");
		ogReportTypeTerminal.select("Details(Invoice Wise)");
		ogReportTypeTerminal.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportTypeTerminal.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Report Type: "), 0, 6);
		lay.addComponent(ogReportTypeTerminal, 1, 6, 4, 6);

		cmbUserTerminal = new ComboBox();
		cmbUserTerminal.setNullSelectionAllowed(false);
		cmbUserTerminal.addItem("%");
		cmbUserTerminal.setItemCaption("%", "ALL");
		cmbUserTerminal.select("%");
		cmbUserTerminal.setWidth("300px");
		cmbUserTerminal.setInputPrompt("Select user name");
		cmbUserTerminal.setImmediate(true);
		cmbUserTerminal.setRequired(true);
		cmbUserTerminal.setRequiredError("This field is required.");
		cmbUserTerminal.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbUserTerminal.setFilteringMode(FilteringMode.CONTAINS);

		Label lbl = new Label("Cashier Name: "); lbl.setWidth("-1px");
		lay.addComponent(lbl, 0, 7); lay.addComponent(cmbUserTerminal, 1, 7, 4, 7);
		lay.addComponent(cBtnTerminal, 0, 8, 4, 8);
		lay.setComponentAlignment(cBtnTerminal, Alignment.MIDDLE_CENTER);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelTerminal.setContent(content);

		return panelTerminal;
	}
	//End Terminal Report

	public void enter(ViewChangeEvent event)
	{
		loadComboData();
		loadComboTerminalData(sessionBean.getBranchId());
	}
}
