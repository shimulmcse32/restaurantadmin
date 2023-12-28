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
import com.common.share.ExcelGenerator;
import com.common.share.MessageBox;
import com.common.share.ReportViewer;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.TransAppCanGateway;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class ReceiptAgainstPurchase extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");
	private CommonButton cBtnS = new CommonButton("", "", "", "", "", "Search", "", "", "");

	private TablePaged tblReceiptList;
	private ArrayList<Label> tbLblReceiptId = new ArrayList<Label>();
	private ArrayList<Label> tbLblReceiptNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblSupplierName = new ArrayList<Label>();
	private ArrayList<Label> tbLblReceiptType = new ArrayList<Label>();
	private ArrayList<Label> tbLblReceiptDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblNoOfItem = new ArrayList<Label>();
	private ArrayList<Label> tbLblAmount = new ArrayList<Label>();
	private ArrayList<Label> tbLblStatus = new ArrayList<Label>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;

	private PopupDateField txtFromDate, txtToDate;
	private ComboBox cmbSupplier, cmbStatus;
	private TextField txtSearch;
	private OptionGroup ogReceiptType;

	private CommonMethod cm;
	private String formId;

	//Report panel
	private PopupDateField txtFromDateReport, txtToDateReport;
	private CommonButton cBtnReport = new CommonButton("", "", "", "", "", "", "", "View", "");
	private OptionGroup ogReportTypeReport, ogReportFormatReport;
	private ComboBox cmbStatusReport, cmbSupReport;

	public ReceiptAgainstPurchase(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		this.formId = formId;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		addComponents(cBtn, addPanel(), addReportPanel());

		addActions();
	}

	private void addActions()
	{
		cBtn.btnNew.addClickListener(event ->
		{
			addEditWindow("Add", "", "");
			event.getButton().setEnabled(false);
		});

		cBtn.btnRefresh.addClickListener(event ->
		{ loadTableInfo(); });

		cBtnS.btnSearch.addClickListener(event ->
		{ loadTableInfo(); });

		txtFromDate.addValueChangeListener(event ->
		{ loadComboData(); });

		txtToDate.addValueChangeListener(event ->
		{ loadComboData(); });

		cBtnReport.btnPreview.addClickListener(event ->
		{ addValidationReport(); });

		ogReportTypeReport.addValueChangeListener(event ->
		{ loadSupplierData(); });

		txtFromDateReport.addValueChangeListener(event ->
		{ loadSupplierData(); });

		txtToDateReport.addValueChangeListener(event ->
		{ loadSupplierData(); });
	}

	private void addEditWindow(String addEdit, String receiptId, String ar)
	{
		AddEditReceiptAgainstPurchase win = new AddEditReceiptAgainstPurchase(sessionBean, addEdit, receiptId);
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

	private void loadComboData()
	{
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		cmbSupplier.removeAllItems();

		String sql = "select distinct rp.vSupplierId, sm.vSupplierName, sm.vSupplierCode from trans.tbReceiptPurchaseInfo rp,"+
				" master.tbSupplierMaster sm where rp.vSupplierId = sm.vSupplierId and rp.dReceiptDate between '"+fromDate+"'"+
				" and '"+toDate+"' and rp.vBranchId = '"+sessionBean.getBranchId()+"' order by sm.vSupplierName";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupplier.addItem(element[0].toString());
			cmbSupplier.setItemCaption(element[0].toString(), element[2].toString()+" - "+element[1].toString());
		}

		String sqlStatus = "select vStatusId, vStatusName from master.tbAllStatus where iActive = 1 and"+
				" vFlag = 'st' order by vStatusName";
		for (Iterator<?> iter = cm.selectSql(sqlStatus).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbStatus.addItem(element[0].toString());
			cmbStatus.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadTableInfo()
	{
		String type = ogReceiptType.getValue().toString();
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		String supplierId = cmbSupplier.getValue() != null? cmbSupplier.getValue().toString():"%";
		String search = "%"+txtSearch.getValue().toString().replaceAll("'", "")+"%";
		String status = cmbStatus.getValue() != null? cmbStatus.getValue().toString():"%";
		tableClear();
		try
		{
			String sql = "select ri.vReceiptId, ri.vReceiptNo, ld.vSupplierName, ri.vReceiptFor, ri.dReceiptDate, ri.vStatusId,"+
					" ri.vStatusId vStatus, ri.iActive, (select count(vPurchaseId) from trans.tbReceiptPurchaseDetails od where"+
					" od.vReceiptId = ri.vReceiptId and od.iActive = 1) iTotalItem, (select SUM(rec.mReceiptAmount) from"+
					" trans.tbReceiptPurchaseDetails rec where rec.vReceiptId = ri.vReceiptId and rec.iActive = 1) mNetAmount,"+
					" ri.vStatusId, ast.vStatusName from trans.tbReceiptPurchaseInfo ri, master.tbSupplierMaster ld, master.tbAllStatus"+
					" ast where ld.vSupplierId = ri.vSupplierId and ri.dReceiptDate between '"+fromDate+"' and '"+toDate+"' and"+
					" vReceiptNo like '"+search+"' and ri.vSupplierId like '"+supplierId+"' and ri.vReceiptFor = '"+type+"' and"+
					" ri.vStatusId like '"+status+"' and ri.vStatusId = ast.vStatusId and ri.vBranchId = '"+sessionBean.getBranchId()+
					"' order by ri.dReceiptDate desc";
			int i = 0;
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblReceiptId.size() <= i)
				{ tableRowAdd(i); }

				tbLblReceiptId.get(i).setValue(element[0].toString());
				tbLblReceiptNo.get(i).setValue(element[1].toString());
				tbLblSupplierName.get(i).setValue(element[2].toString());
				tbLblReceiptType.get(i).setValue(element[3].toString());
				tbLblReceiptDate.get(i).setValue(cm.dfBd.format(element[4]));
				tbLblNoOfItem.get(i).setValue(cm.deciFloat.format(element[8]));
				tbLblAmount.get(i).setValue(cm.setComma(Double.parseDouble(element[9].toString())));
				tbLblStatus.get(i).setValue(element[11].toString());
				if (element[10].toString().equals("S6"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Approve");
				}
				if (element[10].toString().equals("S7"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Cancel");
					tbCmbAction.get(i).removeItem("Approve");
				}
				i++;
			}
			tblReceiptList.nextPage();
			tblReceiptList.previousPage();

			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			totalAmount();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblReceiptList, tbLblReceiptId); }

	private void loadSupplierData()
	{
		String type = ogReportTypeReport.getValue().toString();
		String fromDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue());
		cmbSupReport.removeAllItems();

		String sql = "select distinct oi.vSupplierId, ld.vSupplierName, dbo.funGetNumeric(ld.vSupplierCode) iCode"+
				" from trans.tbReceiptPurchaseInfo oi, master.tbSupplierMaster ld where oi.vSupplierId = ld.vSupplierId"+
				" and oi.dReceiptDate between '"+fromDate+"' and '"+toDate+"' and oi.vReceiptFor = '"+type+"'and"+
				" oi.vBranchId = '"+sessionBean.getBranchId()+"' order by iCode asc";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			cmbSupReport.addItem("%");
			cmbSupReport.setItemCaption("%", "ALL");

			cmbSupReport.addItem(element[0].toString());
			cmbSupReport.setItemCaption(element[0].toString(), element[2].toString()+" - "+element[1].toString());
		}
	}

	private void TransactionCancelWindow(String receiptId, String ar)
	{
		TransactionCancel win = new TransactionCancel(sessionBean, receiptId, "Purchase Receipt");
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

	private void TransactionApproveWindow(String orderId, String ar)
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
					if (tacm.TransactionApprove(orderId, sessionBean.getUserId(), "Purchase Receipt"))
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

	private void addValidationReport()
	{
		if (txtFromDateReport.getValue() != null)
		{
			if (txtToDateReport.getValue() != null)
			{
				if (cmbSupReport.getValue() != null)
				{ viewReport(); }
				else
				{
					cmbSupReport.focus();
					cm.showNotification("warning", "Warning!", "Select Supplier name.");
				}
			}
			else
			{
				txtToDateReport.focus();
				cm.showNotification("warning", "Warning!", "Select to date.");
			}
		}
		else
		{
			txtFromDateReport.focus();
			cm.showNotification("warning", "Warning!", "Select from date.");
		}
	}

	@SuppressWarnings("deprecation")
	public void viewReport()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", xsql = "", fileName = "";

		String fromDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue());
		String status = cmbStatusReport.getValue().toString();
		String type = ogReportTypeReport.getValue().toString();
		String supCus = cmbSupReport.getValue().toString();
		String datePara = "From: "+cm.dfBd.format(txtFromDateReport.getValue())+" To "+cm.dfBd.format(txtToDateReport.getValue());
		try
		{
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", sessionBean.getUserIp());
			hm.put("reportType", type.toUpperCase());
			hm.put("fromToDate", datePara);

			sql = " select * from (select vLedgerDetails, vPurchaseNo, dPurchaseDate, SUM(mDiscount) mDiscount, SUM(mAmount) mAmount, SUM(mVatAmount)"+
					" mVatAmount, SUM(mNetAmount)mNetAmount, (mReceivedAmount)mReceivedAmount, SUM(mNetAmount) - (mReceivedAmount + mReturnAmount)"+
					" mBalance,mReturnAmount, case when (SUM(mNetAmount) - (mReceivedAmount + mReturnAmount)) = 0 then 'Settled' else 'Pending' end"+
					" vStatus from (select ld.vSupplierId, ld.vSupplierName vLedgerDetails, oii.vPurchaseNo, oii.dPurchaseDate, oid.mDiscount, oid.mAmount,"+
					" oid.mVatAmount, oid.mNetAmount, ISNULL((select SUM(rid.mReceiptAmount) from trans.tbReceiptPurchaseDetails rid where rid.vPurchaseId ="+
					" oii.vPurchaseId and rid.iActive = 1), 0) mReceivedAmount,ISNULL((select SUM(prd.mNetAmount) from trans.tbPurchaseReturnDetails prd"+
					" where prd.vPurchaseId = oii.vPurchaseId and  prd.iActive = 1), 0) mReturnAmount from trans.tbPurchaseInfo oii inner join"+
					" trans.tbPurchaseDetails oid on oii.vPurchaseId = oid.vPurchaseId and oid.iActive = 1 inner join master.tbSupplierMaster ld on"+
					" oii.vSupplierId = ld.vSupplierId where oii.dPurchaseDate between '"+fromDate+"' and '"+toDate+"' and ld.vSupplierId like '"+supCus+"'"+
					" and oii.vBranchId = '"+sessionBean.getBranchId()+"') as tbTempTable group by vLedgerDetails, vPurchaseNo, dPurchaseDate, mReceivedAmount,"+
					" mReturnAmount) as tbtemp where vStatus like '"+status+"' order by vLedgerDetails, vPurchaseNo, dPurchaseDate";

			if (ogReportFormatReport.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				reportSource = "com/jasper/postransaction/rptSalesInvoiceStatus.jasper";
				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = sql;
				fileName = "From_"+cm.dfBd.format(txtFromDateReport.getValue())+"_To_"+cm.dfBd.format(txtToDateReport.getValue());
				hm.put("parameters", datePara);

				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	public void viewReportMoneyReceipt(String receiptId)
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "";

		try
		{
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", sessionBean.getUserIp());
			hm.put("surName", sessionBean.getSurName());

			sql = "select * from funMoneyReceipt('"+receiptId+"')";

			hm.put("sql", sql);
			reportSource = "com/jasper/postransaction/rptMoneyReceipt.jasper";

			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	public double totalAmount()
	{
		double amt = 0;
		for (int i=0; i<tblReceiptList.size(); i++)
		{ amt += cm.getAmtValue(tbLblAmount.get(i)); }
		tblReceiptList.setColumnFooter("Net Amount", cm.setComma(amt));
		tblReceiptList.setColumnAlignment("Net Amount", Align.RIGHT);		
		return amt;
	}

	private Panel addPanel()
	{
		Panel pnlTable = new Panel("Receipt List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");

		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout hori = new HorizontalLayout();
		hori.setSpacing(true);

		ogReceiptType = new OptionGroup();
		ogReceiptType.setImmediate(true);
		ogReceiptType.setWidth("-1px");
		ogReceiptType.addItem("Purchase");
		ogReceiptType.setValue("Purchase");
		ogReceiptType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);

		txtFromDate = new PopupDateField();
		txtFromDate.setImmediate(true);
		txtFromDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDate.setValue(new Date());
		txtFromDate.setDateFormat("dd-MM-yyyy");
		txtFromDate.setWidth("110px");
		txtFromDate.setDescription("From Date");
		txtFromDate.setInputPrompt("From Date");

		txtToDate = new PopupDateField();
		txtToDate.setImmediate(true);
		txtToDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDate.setValue(new Date());
		txtToDate.setDateFormat("dd-MM-yyyy");
		txtToDate.setWidth("110px");
		txtToDate.setDescription("To Date"); 
		txtToDate.setInputPrompt("To Date");

		cmbSupplier = new ComboBox();
		cmbSupplier.setImmediate(true);
		cmbSupplier.setFilteringMode(FilteringMode.CONTAINS);
		cmbSupplier.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbSupplier.setInputPrompt("Select supplier");
		cmbSupplier.setWidth("170px");

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Receipt");
		txtSearch.setDescription("Search by receipt number");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtSearch.setWidth("130px");

		cmbStatus = new ComboBox();
		cmbStatus.setImmediate(true);
		cmbStatus.setFilteringMode(FilteringMode.CONTAINS);
		cmbStatus.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbStatus.setInputPrompt("Select status");
		cmbStatus.setWidth("115px");

		cBtnS.btnSearch.setStyleName(ValoTheme.BUTTON_TINY);
		hori.addComponents(ogReceiptType, txtFromDate, txtToDate, cmbSupplier,
				txtSearch, cmbStatus, cBtnS);

		buildTable();
		content.addComponents(hori, tblReceiptList, tblReceiptList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblReceiptList = new TablePaged();
		tblReceiptList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblReceiptId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblReceiptList.addContainerProperty("Receipt Id", Label.class, new Label(), null, null, Align.CENTER);
		tblReceiptList.setColumnCollapsed("Receipt Id", true);

		tblReceiptList.addContainerProperty("Receipt No", Label.class, new Label(), null, null, Align.CENTER);

		tblReceiptList.addContainerProperty("Supplier Name", Label.class, new Label(), null, null, Align.CENTER);

		tblReceiptList.addContainerProperty("Receipt Type", Label.class, new Label(), null, null, Align.CENTER);

		tblReceiptList.addContainerProperty("Receipt Date", Label.class, new Label(), null, null, Align.CENTER);

		tblReceiptList.addContainerProperty("Total Item", Label.class, new Label(), null, null, Align.CENTER);

		tblReceiptList.addContainerProperty("Net Amount", Label.class, new Label(), null, null, Align.CENTER);

		tblReceiptList.addContainerProperty("Status", Label.class, new Label(), null, null, Align.CENTER);

		tblReceiptList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblReceiptList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblReceiptId.add(ar, new Label());
			tbLblReceiptId.get(ar).setWidth("100%");
			tbLblReceiptId.get(ar).setImmediate(true);

			tbLblReceiptNo.add(ar, new Label());
			tbLblReceiptNo.get(ar).setWidth("100%");
			tbLblReceiptNo.get(ar).setImmediate(true);

			tbLblSupplierName.add(ar, new Label());
			tbLblSupplierName.get(ar).setWidth("100%");
			tbLblSupplierName.get(ar).setImmediate(true);

			tbLblReceiptType.add(ar, new Label());
			tbLblReceiptType.get(ar).setWidth("100%");
			tbLblReceiptType.get(ar).setImmediate(true);

			tbLblReceiptDate.add(ar, new Label());
			tbLblReceiptDate.get(ar).setWidth("100%");
			tbLblReceiptDate.get(ar).setImmediate(true);

			tbLblNoOfItem.add(ar, new Label());
			tbLblNoOfItem.get(ar).setWidth("100%");
			tbLblNoOfItem.get(ar).setImmediate(true);
			tbLblNoOfItem.get(ar).setStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbLblAmount.add(ar, new Label());
			tbLblAmount.get(ar).setWidth("100%");
			tbLblAmount.get(ar).setImmediate(true);
			tbLblAmount.get(ar).setStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbLblStatus.add(ar, new Label());
			tbLblStatus.get(ar).setWidth("100%");
			tbLblStatus.get(ar).setImmediate(true);

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
				tbCmbAction.get(ar).addItem("Edit");
				tbCmbAction.get(ar).setItemIcon("Edit", FontAwesome.EDIT);
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
				String reciptId = tbLblReceiptId.get(ar).getValue().toString();
				if (!reciptId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ addEditWindow("Edit", reciptId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Preview"))
					{ viewReportMoneyReceipt(reciptId); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Cancel"))
					{ TransactionCancelWindow(reciptId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Approve"))
					{ TransactionApproveWindow(reciptId, ar+""); }
				}
				tbCmbAction.get(ar).select(null);
			});
			tblReceiptList.addItem(new Object[]{tbLblReceiptId.get(ar), tbLblReceiptNo.get(ar), tbLblSupplierName.get(ar),
					tbLblReceiptType.get(ar), tbLblReceiptDate.get(ar), tbLblNoOfItem.get(ar), tbLblAmount.get(ar),
					tbLblStatus.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	//Report Panel Start
	private Panel addReportPanel()
	{
		Panel panelReport = new Panel("Purchase Status Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");

		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(3, 7);
		lay.setSpacing(true);

		ogReportTypeReport = new OptionGroup();
		ogReportTypeReport.addItem("Purchase");
		ogReportTypeReport.select("Purchase");
		ogReportTypeReport.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportTypeReport.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Report Type: "), 0, 0);
		lay.addComponent(ogReportTypeReport, 1, 0);

		txtFromDateReport = new PopupDateField();
		txtFromDateReport.setImmediate(true);
		txtFromDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateReport.setValue(new Date());
		txtFromDateReport.setWidth("-1px");
		txtFromDateReport.setDateFormat("dd-MM-yyyy");
		txtFromDateReport.setDescription("Invoice Date From");
		txtFromDateReport.setInputPrompt("From Date");
		txtFromDateReport.setRequired(true);
		txtFromDateReport.setRequiredError("This field is required");
		lay.addComponent(new Label("From Date: "), 0, 1);
		lay.addComponent(txtFromDateReport, 1, 1);

		txtToDateReport = new PopupDateField();
		txtToDateReport.setImmediate(true);
		txtToDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateReport.setValue(new Date());
		txtToDateReport.setWidth("-1px");
		txtToDateReport.setDateFormat("dd-MM-yyyy");
		txtToDateReport.setDescription("Invoice Date To");
		txtToDateReport.setInputPrompt("To Date");
		txtToDateReport.setRequired(true);
		txtToDateReport.setRequiredError("This field is required");
		lay.addComponent(new Label("To Date: "), 0, 2);
		lay.addComponent(txtToDateReport, 1, 2);

		cmbSupReport = new ComboBox();
		cmbSupReport.setImmediate(true);
		cmbSupReport.setNullSelectionAllowed(false);
		cmbSupReport.setRequired(true);
		cmbSupReport.setWidth("300px");
		cmbSupReport.setRequiredError("This field is required.");
		cmbSupReport.addStyleName(ValoTheme.COMBOBOX_TINY);
		lay.addComponent(new Label("Supplier Name: "), 0, 3);
		lay.addComponent(cmbSupReport, 1, 3);

		cmbStatusReport = new ComboBox();
		cmbStatusReport.setNullSelectionAllowed(false);
		cmbStatusReport.addItem("%");
		cmbStatusReport.setItemCaption("%", "ALL");
		cmbStatusReport.addItem("Pending");
		cmbStatusReport.addItem("Settled");
		cmbStatusReport.select("Pending");
		cmbStatusReport.addStyleName(ValoTheme.COMBOBOX_TINY);
		lay.addComponent(new Label("Status: "), 0, 4);
		lay.addComponent(cmbStatusReport, 1, 4);

		ogReportFormatReport = new OptionGroup();
		ogReportFormatReport.addItem("PDF");
		ogReportFormatReport.addItem("XLS");
		ogReportFormatReport.select("PDF");
		ogReportFormatReport.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormatReport.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogReportFormatReport, 1, 5);

		lay.addComponent(cBtnReport, 1, 6);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelReport.setContent(content);

		loadSupplierData();
		return panelReport;
	}

	public void enter(ViewChangeEvent event)
	{
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
		loadTableInfo();

		loadSupplierData();
		loadComboData();
	}
}
