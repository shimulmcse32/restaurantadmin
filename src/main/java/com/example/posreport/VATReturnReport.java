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

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.ExcelGenerator;
import com.common.share.ReportViewer;
import com.common.share.SessionBean;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
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
public class VATReturnReport extends VerticalLayout implements View
{
	private SessionBean sessionBean;

	//VAT Summary Report
	private CommonButton cBtnSum = new CommonButton("", "", "", "", "", "", "", "View", "");
	private PopupDateField txtFromDateSum, txtToDateSum;
	private OptionGroup ogReportFormatSum;

	//VAT Details Report
	private CommonButton cBtnDet = new CommonButton("", "", "", "", "", "", "", "View", "");
	private PopupDateField txtFromDateDet, txtToDateDet;
	private OptionGroup ogReportFormatDet, ogReportTypeDet;
	private CommonMethod cm;

	public VATReturnReport(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		addComponents(addVatReturnSum(), addVatReturnDet());

		addActionsVatSum();
		addActionsVatDet();
	}

	private boolean checkTwoDate(Object from, Object to)
	{
		boolean ret = true;
		/*if (!cm.checkFindDate(from, to).equals("0"))
		{ ret = true; }*/
		return ret;
	}

	//VAT Summary Report Start
	private Panel addVatReturnSum()
	{
		Panel panelSum = new Panel("VAT Return Report(Summary) :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 4);
		lay.setSpacing(true);

		txtFromDateSum = new PopupDateField();
		txtFromDateSum.setImmediate(true);
		txtFromDateSum.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateSum.setValue(new Date());
		txtFromDateSum.setWidth("125px");
		txtFromDateSum.setDateFormat("dd-MM-yyyy");
		txtFromDateSum.setDescription("From Date");
		txtFromDateSum.setInputPrompt("From Date");
		txtFromDateSum.setRequired(true);
		txtFromDateSum.setRequiredError("This field is required");
		lay.addComponent(new Label("From Date: "), 0, 0);
		lay.addComponent(txtFromDateSum, 1, 0);

		txtToDateSum = new PopupDateField();
		txtToDateSum.setImmediate(true);
		txtToDateSum.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateSum.setValue(new Date());
		txtToDateSum.setWidth("125px");
		txtToDateSum.setDateFormat("dd-MM-yyyy");
		txtToDateSum.setDescription("To Date");
		txtToDateSum.setInputPrompt("To Date");
		txtToDateSum.setRequired(true);
		txtToDateSum.setRequiredError("This field is required");
		lay.addComponent(new Label("To Date: "), 0, 1);
		lay.addComponent(txtToDateSum, 1, 1);

		ogReportFormatSum = new OptionGroup();
		ogReportFormatSum.addItem("PDF");
		ogReportFormatSum.addItem("XLS");
		ogReportFormatSum.select("PDF");
		ogReportFormatSum.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormatSum.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogReportFormatSum, 1, 2);

		lay.addComponent(cBtnSum, 1, 3);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelSum.setContent(content);

		return panelSum;
	}

	private void addActionsVatSum()
	{
		cBtnSum.btnPreview.addClickListener(event -> addValidationVatSum());
	}

	private void addValidationVatSum()
	{
		if (txtFromDateSum.getValue() != null && txtToDateSum.getValue() != null)
		{
			if (checkTwoDate(txtFromDateSum.getValue(), txtToDateSum.getValue()))
			{ viewReportVatSum(); }
			else
			{
				txtFromDateSum.focus();
				cm.showNotification("warning", "Warning!", "Invalid date selected.");
			}
		}
		else
		{
			txtFromDateSum.focus();
			cm.showNotification("warning", "Warning!", "Invalid date selected.");
		}
	}

	@SuppressWarnings("deprecation")
	private void viewReportVatSum()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", fileName = "", xsql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateSum.getValue());
			String toDate = cm.dfDb.format(txtToDateSum.getValue());
			String branch = sessionBean.getBranchId();
			String datePara = cm.dfBd.format(txtFromDateSum.getValue())+" to "+cm.dfBd.format(txtToDateSum.getValue());

			sql = "select * from [dbo].[funVatReturnSummary]('"+fromDate+"', '"+toDate+"', '"+branch+"')";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptVatReturnSummary.jasper";

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			if (ogReportFormatSum.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				//hm.put("logo", sessionBean.getCompanyLogo());
				hm.put("userIp", sessionBean.getUserIp());
				hm.put("fromToDate", datePara);

				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = "select vSerial, vVatCategory, mNetAmount, mAdjustAmount, mVatAmount from"+
						" [dbo].[funVatReturnSummary]('"+fromDate+"', '"+toDate+"', '"+branch+"')";
				fileName = "VAT_Report_"+datePara;
				hm.put("parameters", "VAT From: "+datePara);

				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
	//VAT Summary Report End

	//VAT Details Report Start
	private Panel addVatReturnDet()
	{
		Panel panelDet = new Panel("VAT Return Report(Details) :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 6);
		lay.setSpacing(true);

		txtFromDateDet = new PopupDateField();
		txtFromDateDet.setImmediate(true);
		txtFromDateDet.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateDet.setValue(new Date());
		txtFromDateDet.setWidth("125px");
		txtFromDateDet.setDateFormat("dd-MM-yyyy");
		txtFromDateDet.setDescription("From Date");
		txtFromDateDet.setInputPrompt("From Date");
		txtFromDateDet.setRequired(true);
		txtFromDateDet.setRequiredError("This field is required");
		lay.addComponent(new Label("From Date: "), 0, 0);
		lay.addComponent(txtFromDateDet, 1, 0);

		txtToDateDet = new PopupDateField();
		txtToDateDet.setImmediate(true);
		txtToDateDet.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateDet.setValue(new Date());
		txtToDateDet.setWidth("125px");
		txtToDateDet.setDateFormat("dd-MM-yyyy");
		txtToDateDet.setDescription("To Date");
		txtToDateDet.setInputPrompt("To Date");
		txtToDateDet.setRequired(true);
		txtToDateDet.setRequiredError("This field is required");
		lay.addComponent(new Label("To Date: "), 0, 1);
		lay.addComponent(txtToDateDet, 1, 1);

		ogReportTypeDet = new OptionGroup();
		ogReportTypeDet.addItem("PURCHASES");
		ogReportTypeDet.addItem("SALES");
		ogReportTypeDet.select("PURCHASES");
		ogReportTypeDet.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportTypeDet.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogReportTypeDet, 1, 3);

		ogReportFormatDet = new OptionGroup();
		ogReportFormatDet.addItem("PDF");
		ogReportFormatDet.addItem("XLS");
		ogReportFormatDet.select("PDF");
		ogReportFormatDet.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormatDet.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogReportFormatDet, 1, 4);

		lay.addComponent(cBtnDet, 1, 5);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelDet.setContent(content);

		return panelDet;
	}

	private void addActionsVatDet()
	{
		cBtnDet.btnPreview.addClickListener(event -> addValidationDet());
	}

	private void addValidationDet()
	{
		if (txtFromDateDet.getValue() != null && txtToDateDet.getValue() != null)
		{
			if (checkTwoDate(txtFromDateDet.getValue(), txtToDateDet.getValue()))
			{
				viewReportVatDet();
			}
			else
			{
				txtFromDateDet.focus();
				cm.showNotification("warning", "Warning!", "Invalid date selected.");
			}
		}
		else
		{
			txtFromDateDet.focus();
			cm.showNotification("warning", "Warning!", "Invalid date selected.");
		}
	}

	@SuppressWarnings("deprecation")
	private void viewReportVatDet()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", fileName = "", xsql = "";
		try
		{
			String fromDate = cm.dfDb.format(txtFromDateDet.getValue());
			String toDate = cm.dfDb.format(txtToDateDet.getValue());
			String branch = sessionBean.getBranchId();
			String datePara = " from "+cm.dfBd.format(txtFromDateDet.getValue())+" to "+cm.dfBd.format(txtToDateDet.getValue());
			String type = ogReportTypeDet.getValue().toString();
			sql = "select * from [dbo].[funVatReturnDetails]('"+fromDate+"', '"+toDate+"', '"+branch+"','"+type+"')";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptVatReturnDetails.jasper";

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			if (ogReportFormatDet.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				//hm.put("logo", sessionBean.getCompanyLogo());
				hm.put("userIp", sessionBean.getUserIp());
				hm.put("fromToDate", datePara);

				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = "select vSupCusName, vInvoiceNo, dInvoiceDate, vReferencNo, vVatRegNo, mAmount, mDiscount,"+
						" mTotalAmount mTaxableAmount, mVatAmount mTaxAmount, mNetAmount from"+
						" [dbo].[funVatReturnDetails]('"+fromDate+"', '"+toDate+"', '"+branch+"', '"+type+"')";
				fileName = "VAT_Report_"+datePara;
				hm.put("parameters", datePara);

				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
	//VAT Details Report End

	public void enter(ViewChangeEvent event)
	{ }
}
