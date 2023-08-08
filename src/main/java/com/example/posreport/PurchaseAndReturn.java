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
import com.common.share.ExcelGenerator;
import com.common.share.MultiComboBox;
import com.common.share.ReportViewer;
import com.common.share.SessionBean;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
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
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class PurchaseAndReturn extends VerticalLayout implements View
{
	private SessionBean sessionBean;

	//Item Wise Purchase Summary Report
	private CommonButton cBtnItemPurchase = new CommonButton("", "", "", "", "", "", "", "View", "");
	private PopupDateField txtFromDateItemPurchase, txtToDateItemPurchase;
	private MultiComboBox cmbItemPurchase; 
	private OptionGroup ogReportFormatItemPurchase, ogReportItemFlag;
	private Panel panelItemPurchase;

	//Supplier Wise Purchase Summary Report
	private CommonButton cBtnSupPurchase = new CommonButton("", "", "", "", "", "", "", "View", "");
	private PopupDateField txtFromDateSupPurchase, txtToDateSupPurchase;
	private MultiComboBox cmbSupPurchase; 
	private OptionGroup ogReportFormatSupPurchase, ogReportSupFlag;
	private Panel panelSupPurchase;

	//Purchase Summary Report
	private CommonButton cBtnSupPurchaseSummary = new CommonButton("", "", "", "", "", "", "", "View", "");
	private PopupDateField txtFromDateSupPurchaseSummary, txtToDateSupPurchaseSummary;
	private MultiComboBox cmbSupPurchaseSummary, cmbItemPurchaseSummary; 
	private OptionGroup ogReportFormatSupPurchaseSummary;
	private Panel panelSupPurchaseSummary;

	private CommonMethod cm;

	public PurchaseAndReturn(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		addComponents(addSupPurchase(), addItemPurchase(), addSupPurchaseSummary());

		addActionsSupPurchase();
		addActionsItemPurchase();
		addActionsSupPurchaseSummary();

		loadSupplierPurchase();
		loadItemPurchase();
		loadSupplierPurchaseSummary();
	}

	private boolean checkTwoDate(Object from, Object to)
	{
		boolean ret = true;
		/*if (!cm.checkFindDate(from, to).equals("0"))
		{ ret = true; }*/
		return ret;
	}

	private void loadSupplierPurchase()
	{
		cmbSupPurchase.removeAllItems();
		String fromDate = cm.dfDb.format(txtFromDateSupPurchase.getValue());
		String toDate = cm.dfDb.format(txtToDateSupPurchase.getValue());
		String sqlC = "";

		if(ogReportSupFlag.getValue().equals("Purchase"))
		{
			sqlC = "select distinct vSupplierId,(select vSupplierName from master.tbSupplierMaster where vSupplierId = a.vSupplierId)"+
					" vSupplierName from trans.tbPurchaseInfo a where dPurchaseDate between '"+fromDate+"' and '"+toDate+"' order by vSupplierName";
		}
		else
		{
			sqlC = "select distinct vSupplierId,(select vSupplierName from master.tbSupplierMaster where vSupplierId = a.vSupplierId)"+
					" vSupplierName from trans.tbPurchaseReturnInfo a where dReturnDate between '"+fromDate+"' and '"+toDate+"' order by vSupplierName";
		}

		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupPurchase.addItem(element[0].toString());
			cmbSupPurchase.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadItemPurchase()
	{
		cmbItemPurchase.removeAllItems();
		String fromDate = cm.dfDb.format(txtFromDateItemPurchase.getValue());
		String toDate = cm.dfDb.format(txtToDateItemPurchase.getValue());
		String sqlC = "";

		if(ogReportSupFlag.getValue().equals("Purchase"))
		{
			sqlC = "select distinct vItemId,(select vItemName from master.tbRawItemInfo where vItemId = b.vItemId) vItemName" + 
					" from trans.tbPurchaseInfo a inner join trans.tbPurchaseDetails b on a.vPurchaseId=b.vPurchaseId where dPurchaseDate"+
					" between '"+fromDate+"' and '"+toDate+"' order by vItemName";
		}
		else
		{
			sqlC = "select distinct vItemId,(select vItemName from master.tbRawItemInfo where vItemId = b.vItemId) vItemName" + 
					" from trans.tbPurchaseReturnInfo a inner join trans.tbPurchaseReturnDetails b on a.vReturnId=b.vReturnId where"+
					" dReturnDate between '"+fromDate+"' and '"+toDate+"' order by vItemName";
		}

		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbItemPurchase.addItem(element[0].toString());
			cmbItemPurchase.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadSupplierPurchaseSummary()
	{
		cmbSupPurchaseSummary.removeAllItems();
		String fromDate = cm.dfDb.format(txtFromDateSupPurchaseSummary.getValue());
		String toDate = cm.dfDb.format(txtToDateSupPurchaseSummary.getValue());

		String sqlC = "select distinct vSupplierId,(select vSupplierName from master.tbSupplierMaster where vSupplierId = a.vSupplierId)"+
				" vSupplierName from trans.tbPurchaseInfo a where dPurchaseDate between '"+fromDate+"' and '"+toDate+"' order by vSupplierName";

		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupPurchaseSummary.addItem(element[0].toString());
			cmbSupPurchaseSummary.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadItemPurchaseSummary()
	{
		cmbItemPurchaseSummary.removeAllItems();
		String fromDate = cm.dfDb.format(txtFromDateSupPurchaseSummary.getValue());
		String toDate = cm.dfDb.format(txtToDateSupPurchaseSummary.getValue());
		String supplierIds =  cmbSupPurchaseSummary.getValue().toString() == null? "%":cmbSupPurchaseSummary.getValue().toString().replace("]", "").replace("[", "").trim();

		String sqlC = "select distinct vItemId,(select vItemName from master.tbRawItemInfo where vItemId = b.vItemId) vItemName" + 
				" from trans.tbPurchaseInfo a inner join trans.tbPurchaseDetails b on a.vPurchaseId=b.vPurchaseId where dPurchaseDate"+
				" between '"+fromDate+"' and '"+toDate+"' and a.vSupplierId in (select Item from dbo.Split('"+supplierIds+"')) order by vItemName";

		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbItemPurchaseSummary.addItem(element[0].toString());
			cmbItemPurchaseSummary.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	//Supplier Wise Purchase Report Start
	private Panel addSupPurchase()
	{
		panelSupPurchase = new Panel("Supplier Wise Purchase/Return Details :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 6);
		lay.setSpacing(true);

		ogReportSupFlag = new OptionGroup();
		ogReportSupFlag.addItem("Purchase");
		ogReportSupFlag.addItem("Return");
		ogReportSupFlag.select("Purchase");
		ogReportSupFlag.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportSupFlag.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Type: "), 0, 0);
		lay.addComponent(ogReportSupFlag, 1, 0);

		txtFromDateSupPurchase = new PopupDateField();
		txtFromDateSupPurchase.setImmediate(true);
		txtFromDateSupPurchase.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateSupPurchase.setValue(new Date());
		txtFromDateSupPurchase.setWidth("125px");
		txtFromDateSupPurchase.setDateFormat("dd-MM-yyyy");
		txtFromDateSupPurchase.setDescription("From Date");
		txtFromDateSupPurchase.setInputPrompt("From Date");
		txtFromDateSupPurchase.setRequired(true);
		txtFromDateSupPurchase.setRequiredError("This field is required");
		lay.addComponent(new Label("From Date: "), 0, 1);
		lay.addComponent(txtFromDateSupPurchase, 1, 1);

		txtToDateSupPurchase = new PopupDateField();
		txtToDateSupPurchase.setImmediate(true);
		txtToDateSupPurchase.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateSupPurchase.setValue(new Date());
		txtToDateSupPurchase.setWidth("125px");
		txtToDateSupPurchase.setDateFormat("dd-MM-yyyy");
		txtToDateSupPurchase.setDescription("To Date");
		txtToDateSupPurchase.setInputPrompt("To Date");
		txtToDateSupPurchase.setRequired(true);
		txtToDateSupPurchase.setRequiredError("This field is required");
		lay.addComponent(new Label("To Date: "), 0, 2);
		lay.addComponent(txtToDateSupPurchase, 1, 2);

		cmbSupPurchase = new MultiComboBox();
		cmbSupPurchase.setWidth("350px");
		cmbSupPurchase.setInputPrompt("Select Supplier Name");
		cmbSupPurchase.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbSupPurchase.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Supplier Name : "), 0, 3);
		lay.addComponent(cmbSupPurchase, 1, 3);

		ogReportFormatSupPurchase = new OptionGroup();
		ogReportFormatSupPurchase.addItem("PDF");
		ogReportFormatSupPurchase.addItem("XLS");
		ogReportFormatSupPurchase.select("PDF");
		ogReportFormatSupPurchase.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormatSupPurchase.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogReportFormatSupPurchase, 1, 4);

		lay.addComponent(cBtnSupPurchase, 1, 5);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelSupPurchase.setContent(content);

		return panelSupPurchase;
	}

	private void addActionsSupPurchase()
	{
		txtFromDateSupPurchase.addValueChangeListener(event -> { loadSupplierPurchase(); });

		txtToDateSupPurchase.addValueChangeListener(event -> { loadSupplierPurchase(); });

		ogReportSupFlag.addValueChangeListener(event -> { loadSupplierPurchase(); });

		cBtnSupPurchase.btnPreview.addClickListener(event -> { addValidationSupPurchase(); });
	}

	private void addValidationSupPurchase()
	{
		if (txtFromDateSupPurchase.getValue() != null && txtToDateSupPurchase.getValue() != null)
		{
			if (checkTwoDate(txtFromDateSupPurchase.getValue(), txtToDateSupPurchase.getValue()))
			{ 
				if(cmbSupPurchase.getValue() != null)
				{ 
					if(ogReportSupFlag.getValue().toString().equalsIgnoreCase("Purchase"))
					{ viewReportSupPurchase(); }
					else
					{ viewReportSupReturn(); }
				}
				else
				{
					cmbSupPurchase.focus();
					cm.showNotification("warning", "Warning!", "Select Supplier.");
				}
			}
			else
			{
				txtFromDateSupPurchase.focus();
				cm.showNotification("warning", "Warning!", "Invalid date selected.");
			}
		}
		else
		{
			txtToDateSupPurchase.focus();
			cm.showNotification("warning", "Warning!", "Invalid date selected.");
		}
	}

	@SuppressWarnings("deprecation")
	private void viewReportSupPurchase()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", fileName = "", xsql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateSupPurchase.getValue());
			String toDate = cm.dfDb.format(txtToDateSupPurchase.getValue());
			String branch = sessionBean.getBranchId();
			String supplierIds =  cmbSupPurchase.getValue().toString() == null? "%":cmbSupPurchase.getValue().toString().replace("]", "").replace("[", "").trim();
			String datePara = cm.dfBd.format(txtFromDateSupPurchase.getValue())+" to "+cm.dfBd.format(txtToDateSupPurchase.getValue());

			sql = "select * from funSupplierWisePurchaseSummary('"+supplierIds+"', '"+fromDate+"', '"+toDate+"', '"+branch+"') order by vSupplierName, vItemName, dPurchaseDate, vPurchaseNo";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptSupplierWisePurchaseSummary.jasper";

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			if (ogReportFormatSupPurchase.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("url", Page.getCurrent().getLocation().toString().replaceAll("#!purchaseReport%239", "?"));
				hm.put("userIp", sessionBean.getUserIp());
				hm.put("fromToDate", datePara);

				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = "select vSupplierName, vItemName, vUnitName, vCategoryName, vPurchaseNo, dPurchaseDate, mTotalQty, mRate, mTotalAmount" + 						
						" mDiscountAmount, mVatAmount, mWithoutVatAmount, mNetAmount, vVatCatName, vVatOption, mVatPercentage from" + 
						" funSupplierWisePurchaseSummary('"+supplierIds+"', '"+fromDate+"', '"+toDate+"', '"+branch+"') order by vSupplierName,"+
						" vItemName, dPurchaseDate, vPurchaseNo";
				fileName = "Supplier_Purchase_Report_"+datePara;
				hm.put("parameters", "Purchase From: "+datePara);

				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
	//Supplier Wise Purchase Summary Report End

	//Supplier Wise Purchase Return Report start
	@SuppressWarnings("deprecation")
	private void viewReportSupReturn()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", fileName = "", xsql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateSupPurchase.getValue());
			String toDate = cm.dfDb.format(txtToDateSupPurchase.getValue());
			String branch = sessionBean.getBranchId();
			String supplierIds = cmbSupPurchase.getValue().toString() == null? "%":cmbSupPurchase.getValue().toString().replace("]", "").replace("[", "").trim();
			String datePara = cm.dfBd.format(txtFromDateSupPurchase.getValue())+" to "+cm.dfBd.format(txtToDateSupPurchase.getValue());

			sql = "select * from funSupplierWiseReturnSummary('"+supplierIds+"', '"+fromDate+"', '"+toDate+"', '"+branch+"') order by vSupplierName, vItemName, dReturnDate, vReturnNo";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptSupplierWiseReturnSummary.jasper";

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			if (ogReportFormatSupPurchase.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("url", Page.getCurrent().getLocation().toString().replaceAll("#!purchaseReport%239", "?"));
				hm.put("userIp", sessionBean.getUserIp());
				hm.put("fromToDate", datePara);

				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = "select vSupplierName, vItemName, vUnitName, vCategoryName, vReturnNo, dReturnDate, vPurchaseNo, mTotalQty, mRate, mTotalAmount" + 						
						" mDiscountAmount, mVatAmount, mWithoutVatAmount, mNetAmount, vVatCatName, vVatOption, mVatPercentage from" + 
						" funSupplierWiseReturnSummary('"+supplierIds+"', '"+fromDate+"', '"+toDate+"', '"+branch+"') order by vSupplierName, vItemName, dReturnDate, vReturnNo";
				fileName = "Supplier_Return_Report_"+datePara;
				hm.put("parameters", "Return From: "+datePara);

				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
	//Supplier Wise Purchase Return Report End

	//Item Wise Purchase Report Start
	private Panel addItemPurchase()
	{
		panelItemPurchase = new Panel("Item Wise Purchase/Return Details :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 6);
		lay.setSpacing(true);

		ogReportItemFlag = new OptionGroup();
		ogReportItemFlag.addItem("Purchase");
		ogReportItemFlag.addItem("Return");
		ogReportItemFlag.select("Purchase");
		ogReportItemFlag.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportItemFlag.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Type: "), 0, 0);
		lay.addComponent(ogReportItemFlag, 1, 0);

		txtFromDateItemPurchase = new PopupDateField();
		txtFromDateItemPurchase.setImmediate(true);
		txtFromDateItemPurchase.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateItemPurchase.setValue(new Date());
		txtFromDateItemPurchase.setWidth("125px");
		txtFromDateItemPurchase.setDateFormat("dd-MM-yyyy");
		txtFromDateItemPurchase.setDescription("From Date");
		txtFromDateItemPurchase.setInputPrompt("From Date");
		txtFromDateItemPurchase.setRequired(true);
		txtFromDateItemPurchase.setRequiredError("This field is required");
		lay.addComponent(new Label("From Date: "), 0, 1);
		lay.addComponent(txtFromDateItemPurchase, 1, 1);

		txtToDateItemPurchase = new PopupDateField();
		txtToDateItemPurchase.setImmediate(true);
		txtToDateItemPurchase.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateItemPurchase.setValue(new Date());
		txtToDateItemPurchase.setWidth("125px");
		txtToDateItemPurchase.setDateFormat("dd-MM-yyyy");
		txtToDateItemPurchase.setDescription("To Date");
		txtToDateItemPurchase.setInputPrompt("To Date");
		txtToDateItemPurchase.setRequired(true);
		txtToDateItemPurchase.setRequiredError("This field is required");
		lay.addComponent(new Label("To Date: "), 0, 2);
		lay.addComponent(txtToDateItemPurchase, 1, 2);

		cmbItemPurchase = new MultiComboBox();
		cmbItemPurchase.setWidth("350px");
		cmbItemPurchase.setInputPrompt("Select Item Name");
		cmbItemPurchase.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbItemPurchase.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Item Name : "), 0, 3);
		lay.addComponent(cmbItemPurchase, 1, 3);

		ogReportFormatItemPurchase = new OptionGroup();
		ogReportFormatItemPurchase.addItem("PDF");
		ogReportFormatItemPurchase.addItem("XLS");
		ogReportFormatItemPurchase.select("PDF");
		ogReportFormatItemPurchase.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormatItemPurchase.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogReportFormatItemPurchase, 1, 4);

		lay.addComponent(cBtnItemPurchase, 1, 5);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelItemPurchase.setContent(content);

		return panelItemPurchase;
	}

	private void addActionsItemPurchase()
	{
		txtFromDateItemPurchase.addValueChangeListener(event -> loadItemPurchase());

		txtToDateItemPurchase.addValueChangeListener(event -> loadItemPurchase());

		ogReportItemFlag.addValueChangeListener(event -> loadItemPurchase());

		cBtnItemPurchase.btnPreview.addClickListener(event -> addValidationItemPurchase());
	}

	private void addValidationItemPurchase()
	{
		if (txtFromDateItemPurchase.getValue() != null && txtToDateItemPurchase.getValue() != null)
		{
			if (checkTwoDate(txtFromDateItemPurchase.getValue(), txtToDateItemPurchase.getValue()))
			{
				if(cmbItemPurchase.getValue() != null)
				{ 
					if(ogReportItemFlag.getValue().toString().equalsIgnoreCase("Purchase"))
					{ viewReportItemPurchase(); }
					else
					{ viewReportItemReturn(); }
				}
				else
				{
					cmbItemPurchase.focus();
					cm.showNotification("warning", "Warning!", "Select Item.");
				}
			}
			else
			{
				txtFromDateItemPurchase.focus();
				cm.showNotification("warning", "Warning!", "Invalid date selected.");
			}
		}
		else
		{
			txtFromDateItemPurchase.focus();
			cm.showNotification("warning", "Warning!", "Invalid date selected.");
		}
	}

	@SuppressWarnings("deprecation")
	private void viewReportItemPurchase()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", fileName = "", xsql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateItemPurchase.getValue());
			String toDate = cm.dfDb.format(txtToDateItemPurchase.getValue());
			String branch = sessionBean.getBranchId();
			String ItemIds = cmbItemPurchase.getValue().toString() == null? "%":cmbItemPurchase.getValue().toString().replace("]", "").replace("[", "").trim();
			String datePara = " from "+cm.dfBd.format(txtFromDateItemPurchase.getValue())+" to "+cm.dfBd.format(txtToDateItemPurchase.getValue());

			sql = "select * from funItemWisePurchaseSummary('"+ItemIds+"', '"+fromDate+"', '"+toDate+"', '"+branch+"') order by vItemName, vSupplierName, dPurchaseDate, vPurchaseNo";

			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptItemWisePurchaseSummary.jasper";

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			if (ogReportFormatItemPurchase.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("userIp", sessionBean.getUserIp());
				hm.put("fromToDate", datePara);
				hm.put("url", Page.getCurrent().getLocation().toString().replaceAll("#!purchaseReport%239", "?"));

				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = "select vItemName, vUnitName, vCategoryName, vSupplierName, vPurchaseNo, dPurchaseDate, mTotalQty, mRate, mTotalAmount" + 						
						" mDiscountAmount, mVatAmount, mWithoutVatAmount, mNetAmount, vVatCatName, vVatOption, mVatPercentage from" + 
						" funItemWisePurchaseSummary('"+ItemIds+"', '"+fromDate+"', '"+toDate+"', '"+branch+"') order by vItemName,"+
						" vSupplierName, dPurchaseDate, vPurchaseNo";
				fileName = "Item_Purchase_Report_"+datePara;
				hm.put("parameters", "Purchase From: "+datePara);

				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
	//Item Wise Purchase Report End

	//Item Wise Purchase Return Report End
	@SuppressWarnings("deprecation")
	private void viewReportItemReturn()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", fileName = "", xsql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateItemPurchase.getValue());
			String toDate = cm.dfDb.format(txtToDateItemPurchase.getValue());
			String branch = sessionBean.getBranchId();
			String ItemIds = cmbItemPurchase.getValue().toString() == null? "%":cmbItemPurchase.getValue().toString().replace("]", "").replace("[", "").trim();
			String datePara = " from "+cm.dfBd.format(txtFromDateItemPurchase.getValue())+" to "+cm.dfBd.format(txtToDateItemPurchase.getValue());

			sql = "select * from funItemWiseReturnSummary('"+ItemIds+"', '"+fromDate+"', '"+toDate+"', '"+branch+"') order by vItemName, vSupplierName, dReturnDate, vReturnNo";

			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptItemWisePurchaseReturnSummary.jasper";

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			if (ogReportFormatItemPurchase.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("url", Page.getCurrent().getLocation().toString().replaceAll("#!purchaseReport%239", "?"));
				hm.put("userIp", sessionBean.getUserIp());
				hm.put("fromToDate", datePara);

				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = "select vItemName, vUnitName, vCategoryName, vSupplierName, vReturnNo, dReturnDate, vPurchaseNo, mTotalQty, mRate, mTotalAmount" + 						
						" mDiscountAmount, mVatAmount, mWithoutVatAmount, mNetAmount, vVatCatName, vVatOption, mVatPercentage from" + 
						" funItemWiseReturnSummary('"+ItemIds+"', '"+fromDate+"', '"+toDate+"', '"+branch+"') order by vItemName, vSupplierName, dReturnDate, vReturnNo";
				fileName = "Item_Return_Report_"+datePara;
				hm.put("parameters", "Return From: "+datePara);

				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
	//Item Wise Purchase Return Report End

	//Purchase Summary Start
	private Panel addSupPurchaseSummary()
	{
		panelSupPurchaseSummary = new Panel("Purchase Summary Report:: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 6);
		lay.setSpacing(true);

		txtFromDateSupPurchaseSummary = new PopupDateField();
		txtFromDateSupPurchaseSummary.setImmediate(true);
		txtFromDateSupPurchaseSummary.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateSupPurchaseSummary.setValue(new Date());
		txtFromDateSupPurchaseSummary.setWidth("125px");
		txtFromDateSupPurchaseSummary.setDateFormat("dd-MM-yyyy");
		txtFromDateSupPurchaseSummary.setDescription("From Date");
		txtFromDateSupPurchaseSummary.setInputPrompt("From Date");
		txtFromDateSupPurchaseSummary.setRequired(true);
		txtFromDateSupPurchaseSummary.setRequiredError("This field is required");
		lay.addComponent(new Label("From Date: "), 0, 0);
		lay.addComponent(txtFromDateSupPurchaseSummary, 1, 0);

		txtToDateSupPurchaseSummary = new PopupDateField();
		txtToDateSupPurchaseSummary.setImmediate(true);
		txtToDateSupPurchaseSummary.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateSupPurchaseSummary.setValue(new Date());
		txtToDateSupPurchaseSummary.setWidth("125px");
		txtToDateSupPurchaseSummary.setDateFormat("dd-MM-yyyy");
		txtToDateSupPurchaseSummary.setDescription("To Date");
		txtToDateSupPurchaseSummary.setInputPrompt("To Date");
		txtToDateSupPurchaseSummary.setRequired(true);
		txtToDateSupPurchaseSummary.setRequiredError("This field is required");
		lay.addComponent(new Label("To Date: "), 0, 1);
		lay.addComponent(txtToDateSupPurchaseSummary, 1, 1);

		cmbSupPurchaseSummary = new MultiComboBox();
		cmbSupPurchaseSummary.setWidth("350px");
		cmbSupPurchaseSummary.setInputPrompt("Select Supplier Name");
		cmbSupPurchaseSummary.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbSupPurchaseSummary.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Supplier Name : "), 0, 2);
		lay.addComponent(cmbSupPurchaseSummary, 1, 2);

		cmbItemPurchaseSummary = new MultiComboBox();
		cmbItemPurchaseSummary.setWidth("350px");
		cmbItemPurchaseSummary.setInputPrompt("Select Item Name");
		cmbItemPurchaseSummary.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbItemPurchaseSummary.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Item Name : "), 0, 3);
		lay.addComponent(cmbItemPurchaseSummary, 1, 3);

		ogReportFormatSupPurchaseSummary = new OptionGroup();
		ogReportFormatSupPurchaseSummary.addItem("PDF");
		ogReportFormatSupPurchaseSummary.addItem("XLS");
		ogReportFormatSupPurchaseSummary.select("PDF");
		ogReportFormatSupPurchaseSummary.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormatSupPurchaseSummary.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogReportFormatSupPurchaseSummary, 1, 4);

		lay.addComponent(cBtnSupPurchaseSummary, 1, 5);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelSupPurchaseSummary.setContent(content);

		return panelSupPurchaseSummary;
	}

	private void addActionsSupPurchaseSummary()
	{
		txtFromDateSupPurchaseSummary.addValueChangeListener(event -> loadSupplierPurchaseSummary());

		txtToDateSupPurchaseSummary.addValueChangeListener(event -> loadSupplierPurchaseSummary());

		cmbSupPurchaseSummary.addValueChangeListener(event -> loadItemPurchaseSummary());

		cBtnSupPurchaseSummary.btnPreview.addClickListener(event -> addValidationSupPurchaseSummary());
	}

	private void addValidationSupPurchaseSummary()
	{
		if (txtFromDateSupPurchaseSummary.getValue() != null && txtToDateSupPurchaseSummary.getValue() != null)
		{
			if (checkTwoDate(txtFromDateSupPurchaseSummary.getValue(), txtToDateSupPurchaseSummary.getValue()))
			{ 
				if(cmbSupPurchaseSummary.getValue() != null)
				{ 
					if(cmbItemPurchaseSummary.getValue() != null)
					{ viewReportSupPurchaseSummary(); }
					else
					{
						cmbItemPurchaseSummary.focus();
						cm.showNotification("warning", "Warning!", "Select Item.");
					}
				}
				else
				{
					cmbSupPurchaseSummary.focus();
					cm.showNotification("warning", "Warning!", "Select Supplier.");
				}
			}
			else
			{
				txtFromDateSupPurchaseSummary.focus();
				cm.showNotification("warning", "Warning!", "Invalid date selected.");
			}
		}
		else
		{
			txtToDateSupPurchaseSummary.focus();
			cm.showNotification("warning", "Warning!", "Invalid date selected.");
		}
	}

	@SuppressWarnings("deprecation")
	private void viewReportSupPurchaseSummary()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", fileName = "", xsql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateSupPurchaseSummary.getValue());
			String toDate = cm.dfDb.format(txtToDateSupPurchaseSummary.getValue());
			String branch = sessionBean.getBranchId();
			String supplierIds =  cmbSupPurchaseSummary.getValue().toString() == null? "%":cmbSupPurchaseSummary.getValue().toString().replace("]", "").replace("[", "").trim();
			String itemIds =  cmbItemPurchaseSummary.getValue().toString() == null? "%":cmbItemPurchaseSummary.getValue().toString().replace("]", "").replace("[", "").trim();
			String datePara = cm.dfBd.format(txtFromDateSupPurchaseSummary.getValue())+" to "+cm.dfBd.format(txtToDateSupPurchaseSummary.getValue());

			sql = "select vSupplierId, vSupplierName, vItemId, vItemName, vUnitName, vCategoryName, sum(mTotalQty+mReturnQty) mPurchaseQty, sum(mReturnQty)"+
					" mReturnQty, sum(mTotalQty)mTotalQty, sum(mNetAmount)/sum(mTotalQty)mRate, sum(mDiscountAmount)mDiscountAmount, sum(mVatAmount)mVatAmount," + 
					" sum(mWithoutVatAmount)mWithoutVatAmount, sum(mNetAmount) mNetAmount from funSupplierWisePurchaseSummary('"+supplierIds+"', '"+fromDate+"',"+
					" '"+toDate+"', '"+branch+"') where vItemId in (select Item from dbo.Split('"+itemIds+"')) group by vSupplierId, vSupplierName, vItemId,"+
					" vItemName, vUnitName, vCategoryName order by vSupplierName, vItemName";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptPurchaseSummary.jasper";

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			if (ogReportFormatSupPurchaseSummary.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("url", Page.getCurrent().getLocation().toString().replaceAll("#!purchaseReport%239", "?"));
				hm.put("userIp", sessionBean.getUserIp());
				hm.put("fromToDate", datePara);

				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = "select vSupplierId, vSupplierName, vItemId, vItemName, vUnitName, vCategoryName, sum(mTotalQty+mReturnQty) mPurchaseQty, sum(mReturnQty)" + 
						" mReturnQty, sum(mTotalQty)mTotalQty from funSupplierWisePurchaseSummary('"+supplierIds+"', '"+fromDate+"', '"+toDate+"', '"+branch+"')" + 
						" group by vSupplierId, vSupplierName, vItemId, vItemName, vUnitName, vCategoryName where order by vSupplierName, vItemName";

				fileName = "Purchase_Summary_Report_"+datePara;
				hm.put("parameters", "Purchase Summary From: "+datePara);

				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	//Purchase Summary Report End
	public void enter(ViewChangeEvent event)
	{
		loadSupplierPurchase();
		loadItemPurchase();
		loadSupplierPurchaseSummary();
	}
}
