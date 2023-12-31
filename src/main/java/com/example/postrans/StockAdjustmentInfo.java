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
import com.example.gateway.StockAdjustmentGateway;
import com.example.gateway.TransAppCanGateway;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
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
public class StockAdjustmentInfo extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");
	private TablePaged tblAdjustList;
	private ArrayList<Label> tbLblAdjustId = new ArrayList<Label>();
	private ArrayList<Label> tbLblAdjustNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblAdjustDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblTotalItem = new ArrayList<Label>();
	private ArrayList<Label> tbLblStatus = new ArrayList<Label>();
	private ArrayList<Label> tbLblAmount = new ArrayList<Label>();
	private ArrayList<Label> tbLblRemarks = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();

	private SessionBean sessionBean;
	private TextField txtSearch;
	private ComboBox cmbStatus;
	private PopupDateField txtFromDate, txtToDate;
	private CommonButton cBtnS = new CommonButton("", "", "", "", "", "Search", "", "", "");

	private CommonMethod cm;
	private StockAdjustmentGateway sag = new StockAdjustmentGateway();
	private String formId;

	//Adjustment report
	private PopupDateField txtFromDateReport, txtToDateReport;
	private MultiComboBox cmbItemNameReport, cmbAdjTypeReport;
	private CommonButton cBtnV = new CommonButton("", "", "", "", "", "", "", "View", "");

	public StockAdjustmentInfo(SessionBean sessionBean, String formId)
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
		{ addValidationRpt(); });

		txtFromDateReport.addValueChangeListener(event ->
		{ loadItemReport(); });

		txtToDateReport.addValueChangeListener(event ->
		{ loadItemReport(); });

		loadStatus();
	}

	private void loadStatus()
	{
		String sqlStatus = "select vStatusId, vStatusName from master.tbAllStatus where iActive = 1 and"+
				" vFlag = 'st' Order by vStatusName";
		for (Iterator<?> iter = cm.selectSql(sqlStatus).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbStatus.addItem(element[0].toString());
			cmbStatus.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void addEditWindow(String addEdit, String itemId, String ar)
	{
		AddEditStockAdjustment win = new AddEditStockAdjustment(sessionBean, addEdit, itemId);
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
			loadItemReport();
		});
	}

	public double totalAmount()
	{
		double amt = 0;
		for (int i=0; i<tbLblAdjustId.size(); i++)
		{ amt += cm.getAmtValue(tbLblAmount.get(i)); }
		tblAdjustList.setColumnFooter("Amount", cm.setComma(amt));
		tblAdjustList.setColumnAlignment("Amount", Align.RIGHT);
		return amt;
	}

	private void EditSelectOrder(String transId, int ar)
	{ addEditWindow("Edit", transId, ar+""); }

	private void ActiveInactiveSelectOrder(String transId, int ar)
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
					if (sag.activeInactiveData(transId, sessionBean.getUserId()))
					{
						tbChkActive.get(ar).setValue(!tbChkActive.get(ar).getValue().booleanValue());
						cm.showNotification("success", "Successfull!", "All information updated successfully.");
						tbCmbAction.get(ar).setEnabled(true);
						loadTableInfo();
					}
					else
					{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
				}
				else if (buttonType == ButtonType.NO)
				{ tbCmbAction.get(ar).setEnabled(true); }
			}
		});
	}

	private void AdjustOrderCancelWindow(String adjustId, String ar)
	{
		TransactionCancel win = new TransactionCancel(sessionBean, adjustId, "Adjustment");
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event ->
		{
			if (!ar.isEmpty())
			{ tbCmbAction.get(Integer.parseInt(ar)).setEnabled(true); }
			cBtn.btnNew.setEnabled(true);
			loadTableInfo();
			loadItemReport();
		});
	}

	private void TransactionApproveWindow(String transId, String ar)
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
					if (tacm.TransactionApprove(transId, sessionBean.getUserId(), "Adjustment"))
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

	public void viewReportSingle(String adjustIds)
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

			sql = "select bm.vBranchName, sai.vAdjustNo, sai.dAdjustDate, sai.vReferenceNo, sai.vRemarks, vItemCode, vItemName, sad.vRemarks"+
					" vDescription, uni.vUnitName, ss.vStatusName, ss.vFlag, sad.mQuantity, sad.mUnitRate, sad.mAmount, ui.vFullName,"+
					" ISNULL((select ui.vFullName from master.tbUserInfo ui where ui.vUserId = sai.vApprovedBy), '') vApprovedBy,"+
					" ISNULL((select ui.vFullName from master.tbUserInfo ui where ui.vUserId = sai.vCancelledBy), '') vCancelledBy,"+
					" sts.vStatusName vStat from trans.tbStockAdjustmentInfo sai, trans.tbStockAdjustmentDetails sad, master.tbRawItemInfo rii,"+
					" master.tbStockStatus ss, master.tbBranchMaster bm, master.tbUserInfo ui, master.tbUnitInfo uni, master.tbAllStatus sts"+
					" where sai.vAdjustId = sad.vAdjustId and sad.vItemId = rii.vItemId and ss.vStatusId = sad.vAdjustStatus and sai.vBranchId"+
					" = bm.vBranchId and sai.vModifiedBy = ui.vUserId and sai.vStatusId = sts.vStatusId and sad.vUnitId = convert(varchar(10),"+
					" uni.iUnitId) and sai.vAdjustId = '"+adjustIds+"' and sai.iActive = 1 order by ss.vStatusName, sad.iAutoId";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptStockAdjustment.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private void loadItemReport()
	{
		cmbItemNameReport.removeAllItems();
		String fmDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue()), branchId = sessionBean.getBranchId();

		String sql = "select distinct rin.vItemId, rin.vItemCode, rin.vItemName, dbo.funGetNumeric(rin.vItemCode) from trans.tbStockAdjustmentInfo"+
				" sai, trans.tbStockAdjustmentDetails sad, master.tbRawItemInfo rin where sai.vAdjustId = sad.vAdjustId and sad.vItemId = rin.vItemId"+
				" and sai.dAdjustDate between '"+fmDate+"' and '"+toDate+"' and sai.vBranchId = '"+branchId+"' and sai.vStatusId = 'S6' and"+
				" sai.iActive = 1 order by dbo.funGetNumeric(rin.vItemCode), rin.vItemName";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbItemNameReport.addItem(element[0].toString());
			cmbItemNameReport.setItemCaption(element[0].toString(), element[1].toString()+" - "+element[2].toString());
		}

		String sqlS = "select vStatusId, vStatusName, vFlag from master.tbStockStatus where iActive = 1 order by vStatusName";
		for (Iterator<?> iter = cm.selectSql(sqlS).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			cmbAdjTypeReport.addItem(element[0].toString());
			cmbAdjTypeReport.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void addValidationRpt()
	{
		if (txtFromDateReport.getValue() != null)
		{
			if (txtToDateReport.getValue() != null)
			{
				if (!cm.getMultiComboValue(cmbItemNameReport).isEmpty())
				{
					if (!cm.getMultiComboValue(cmbAdjTypeReport).isEmpty())
					{ viewReport(); }
					else
					{
						cmbAdjTypeReport.focus();
						cm.showNotification("warning", "Warning!", "Select adjust type.");
					}
				}
				else
				{
					cmbItemNameReport.focus();
					cm.showNotification("warning", "Warning!", "Select a raw item.");
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

	public void viewReport()
	{
		String reportSource = "", sql = "";
		String fmDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue());
		String itemId = cm.getMultiComboValue(cmbItemNameReport);
		String adjTyp = cm.getMultiComboValue(cmbAdjTypeReport);
		String branId = sessionBean.getBranchId();

		String dtPara = "From: "+cm.dfBd.format(txtFromDateReport.getValue())+" To "+cm.dfBd.format(txtToDateReport.getValue());
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
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("fromToDate", dtPara);

			sql = "select sai.vAdjustNo, sai.dAdjustDate, sai.vReferenceNo, sai.vRemarks, sad.vItemId, rin.vItemCode+' - '+rin.vItemName"+
					" vItemDetails, uni.vUnitName, sta.vStatusName, sad.mQuantity, sad.mUnitRate, sad.mAmount, cat.vCategoryName, sad.vRemarks"+
					" vDescription from trans.tbStockAdjustmentInfo sai, trans.tbStockAdjustmentDetails sad, master.tbRawItemInfo rin,"+
					" master.tbItemCategory cat, master.tbStockStatus sta, master.tbUnitInfo uni where sai.vAdjustId = sad.vAdjustId and"+
					" sad.vItemId = rin.vItemId and rin.vCategoryId = cat.vCategoryId and sad.vAdjustStatus = sta.vStatusId and rin.vUnitId"+
					" = uni.iUnitId and sad.vItemId in (select Item from dbo.Split('"+itemId+"')) and sai.vBranchId = '"+branId+"' and"+
					" convert(date, dAdjustDate, 105) between '"+fmDate+"' and '"+toDate+"' and sad.vAdjustStatus in (select Item from dbo.Split"+
					" ('"+adjTyp+"')) and sai.iActive = 1 order by cat.vCategoryName, dbo.funGetNumeric(rin.vItemCode), rin.vItemName";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptStockAdjustDateBetween.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private Panel addPanel()
	{
		Panel pnlTable = new Panel("Stock Adjustment List :: "+sessionBean.getCompanyName()+
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
		txtSearch.setInputPrompt("Search Adjustment");
		txtSearch.setDescription("Search by adjustment number");
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
		content.addComponents(hori, tblAdjustList, tblAdjustList.createControls());
		pnlTable.setContent(content);
		return pnlTable;
	}

	private void buildTable()
	{
		tblAdjustList = new TablePaged();
		tblAdjustList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update &&
					(sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()))
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblAdjustId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblAdjustList.addContainerProperty("Adjust Id", Label.class, new Label(), null, null, Align.CENTER);
		tblAdjustList.setColumnCollapsed("Adjust Id", true);

		tblAdjustList.addContainerProperty("Adjust No", Label.class, new Label(), null, null, Align.CENTER);

		tblAdjustList.addContainerProperty("Adjust Date", Label.class, new Label(), null, null, Align.CENTER);

		tblAdjustList.addContainerProperty("Total Item", Label.class, new Label(), null, null, Align.RIGHT);

		tblAdjustList.addContainerProperty("Amount", Label.class, new Label(), null, null, Align.RIGHT); 

		tblAdjustList.addContainerProperty("Remarks", Label.class, new Label(), null, null, Align.CENTER);

		tblAdjustList.addContainerProperty("Status", Label.class, new Label(), null, null, Align.CENTER);

		tblAdjustList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblAdjustList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblAdjustList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblAdjustId.add(ar, new Label());
			tbLblAdjustId.get(ar).setWidth("100%");
			tbLblAdjustId.get(ar).setImmediate(true);
			tbLblAdjustId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblAdjustNo.add(ar, new Label());
			tbLblAdjustNo.get(ar).setWidth("100%");
			tbLblAdjustNo.get(ar).setImmediate(true);
			tbLblAdjustNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblAdjustDate.add(ar, new Label());
			tbLblAdjustDate.get(ar).setWidth("100%");
			tbLblAdjustDate.get(ar).setImmediate(true);
			tbLblAdjustDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

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

			tbLblRemarks.add(ar, new Label());
			tbLblRemarks.get(ar).setWidth("100%");
			tbLblRemarks.get(ar).setImmediate(true);
			tbLblRemarks.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblStatus.add(ar, new Label("", ContentMode.HTML));
			tbLblStatus.get(ar).setWidth("100%");
			tbLblStatus.get(ar).setImmediate(true);
			tbLblStatus.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbChkActive.add(ar, new CheckBox());
			tbChkActive.get(ar).setWidth("100%");
			tbChkActive.get(ar).setImmediate(true);
			tbChkActive.get(ar).addStyleName(ValoTheme.CHECKBOX_SMALL);

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
				String adjustId = tbLblAdjustId.get(ar).getValue().toString();
				if (!adjustId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ EditSelectOrder(adjustId, ar); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Active/Inactive"))
					{ ActiveInactiveSelectOrder(adjustId, ar); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Preview"))
					{ viewReportSingle(adjustId); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Cancel"))
					{ AdjustOrderCancelWindow(adjustId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Approve"))
					{ TransactionApproveWindow(adjustId, ar+""); }
				}
				tbCmbAction.get(ar).select(null);
			});

			tblAdjustList.addItem(new Object[]{tbLblAdjustId.get(ar), tbLblAdjustNo.get(ar), tbLblAdjustDate.get(ar),
					tbLblTotalItem.get(ar), tbLblAmount.get(ar), tbLblRemarks.get(ar), tbLblStatus.get(ar),
					tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private Panel addReport()
	{
		Panel panelReport = new Panel("Stock Adjustment Report :: "+sessionBean.getCompanyName()+" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new  HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(5, 4);
		lay.setSpacing(true);

		txtFromDateReport  = new PopupDateField();
		txtFromDateReport.setImmediate(true);
		txtFromDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateReport.setValue(new Date());
		txtFromDateReport.setWidth("110px");
		txtFromDateReport.setDateFormat("dd-MM-yyyy");
		txtFromDateReport.setRequired(true);
		txtFromDateReport.setRequiredError("This field is required.");
		txtFromDateReport.setDescription("From Date");
		lay.addComponent(new Label("Date(s): "), 0, 0);
		lay.addComponent(txtFromDateReport, 1, 0);

		txtToDateReport  = new PopupDateField();
		txtToDateReport.setImmediate(true);
		txtToDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateReport.setValue(new Date());
		txtToDateReport.setWidth("110px");
		txtToDateReport.setDateFormat("dd-MM-yyyy");
		txtToDateReport.setRequired(true);
		txtToDateReport.setRequiredError("This field is required.");
		txtToDateReport.setDescription("To Date");
		lay.addComponent(txtToDateReport, 2, 0);

		cmbItemNameReport = new MultiComboBox();
		cmbItemNameReport.setWidth("400px");
		cmbItemNameReport.setInputPrompt("Select item name");
		cmbItemNameReport.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbItemNameReport.setFilteringMode(FilteringMode.CONTAINS);
		cmbItemNameReport.setRequired(true);
		cmbItemNameReport.setRequiredError("This field is required.");
		Label lbl = new Label("Item Name: ");
		lbl.setWidth("-1px");
		lay.addComponent(lbl, 0, 1);
		lay.addComponent(cmbItemNameReport, 1, 1, 4, 1); 

		cmbAdjTypeReport = new MultiComboBox();
		cmbAdjTypeReport.setWidth("300px");
		cmbAdjTypeReport.setInputPrompt("Select status");
		cmbAdjTypeReport.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbAdjTypeReport.setFilteringMode(FilteringMode.CONTAINS);
		cmbAdjTypeReport.setRequired(true);
		cmbAdjTypeReport.setRequiredError("This field is required.");
		lay.addComponent(new Label("Adj. Type: "), 0, 2);
		lay.addComponent(cmbAdjTypeReport, 1, 2, 4, 2); 

		lay.addComponent(cBtnV, 1, 3, 4, 3);
		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelReport.setContent(content);

		return panelReport;
	}

	private void loadTableInfo()
	{
		String search = "%"+txtSearch.getValue().toString()+"%", statId = "", statNm = "";
		String status = cmbStatus.getValue() != null? cmbStatus.getValue().toString():"%";
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		String branchId = sessionBean.getBranchId();
		tableClear();
		int i = 0;
		try
		{
			String sql = "select sa.vAdjustId, sa.vAdjustNo, sa.dAdjustDate, sa.vRemarks, (select isnull(sum(mAmount), 0) from"+
					" trans.tbStockAdjustmentDetails where vAdjustId = sa.vAdjustId) Amount, (select isnull(count(vItemId), 0) from"+
					" trans.tbStockAdjustmentDetails where vAdjustId = sa.vAdjustId) iItem, sa.iActive, sa.vStatusId, ast.vStatusName"+
					" from trans.tbStockAdjustmentInfo sa, master.tbAllStatus ast where vAdjustNo like '"+search+"' and sa.vStatusId"+
					" = ast.vStatusId and sa.dAdjustDate between '"+fromDate+"' and '"+toDate+"' and sa.vStatusId like '"+status+"'"+
					" and sa.vBranchId like '"+branchId+"' order by sa.dAdjustDate, sa.iAutoId desc";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblAdjustId.size() <= i)
				{ tableRowAdd(i); }

				tbLblAdjustId.get(i).setValue(element[0].toString());
				tbLblAdjustNo.get(i).setValue(element[1].toString());
				tbLblAdjustDate.get(i).setValue(cm.dfBd.format(element[2]));
				tbLblRemarks.get(i).setValue(element[3].toString());
				tbLblTotalItem.get(i).setValue(element[5].toString());
				tbLblStatus.get(i).setValue(element[8].toString());
				tbLblAmount.get(i).setValue(cm.setComma(Double.parseDouble(element[4].toString())));
				tbChkActive.get(i).setValue((element[6].toString().equals("1")? true:false));
				tbChkActive.get(i).setEnabled(false);

				statId = element[7].toString();
				statNm = element[8].toString();

				if (statId.equals("S5"))
				{ status = "<b style=\"color:orange\">"+statNm+"</b>"; }
				else if (statId.equals("S6"))
				{ status = "<b style=\"color:green\">"+statNm+"</b>"; }
				else if (statId.equals("S7"))
				{ status = "<b style=\"color:red\">"+statNm+"</b>"; }
				tbLblStatus.get(i).setValue(status);

				if (statId.equals("S7"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Cancel");
					tbCmbAction.get(i).removeItem("Approve");
				}
				if (statId.equals("S6"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Approve");
				}

				i++;
			}
			tblAdjustList.nextPage();
			tblAdjustList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			totalAmount();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblAdjustList, tbLblAdjustId); }

	public void enter(ViewChangeEvent event)
	{
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
		loadTableInfo();
		loadItemReport();
	}
}
