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
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.MultiComboBox;
import com.common.share.ReportViewer;
import com.example.gateway.PurchaseOrderGateway;
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
public class PurchaseOrderInfo extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");
	private TablePaged tblOrderList;
	private ArrayList<Label> tbLblOrderId = new ArrayList<Label>();
	private ArrayList<Label> tbLblOrderNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblOrderDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblSupplierName = new ArrayList<Label>();
	private ArrayList<Label> tbLblDeliveryDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblStatus = new ArrayList<Label>();
	private ArrayList<Label> tbLblAmount = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();

	private SessionBean sessionBean;
	private TextField txtSearch;
	private ComboBox cmbSupplierName, cmbStatus;
	private PopupDateField txtFromDate, txtToDate;
	private CommonButton cBtnS = new CommonButton("", "", "", "", "", "Search", "", "", "");

	private CommonMethod cm;
	private PurchaseOrderGateway pog = new PurchaseOrderGateway();
	private String formId;

	//Purchase order report
	private PopupDateField txtFromDateReport, txtToDateReport;
	private MultiComboBox cmbSupplierReport, cmbOrderNoReport;
	private CommonButton cBtnV = new CommonButton("", "", "", "", "", "", "", "View", "");

	public PurchaseOrderInfo(SessionBean sessionBean, String formId)
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
		{ addValidation(); });

		txtFromDateReport.addValueChangeListener(event ->
		{ loadReportSupplier(); });

		txtToDateReport.addValueChangeListener(event ->
		{ loadReportSupplier(); });

		txtFromDate.addValueChangeListener(event ->
		{ loadSupplier(); });

		txtToDate.addValueChangeListener(event ->
		{ loadSupplier(); });

		cmbSupplierReport.addValueChangeListener(event ->
		{ loadOrderNo(); });

		loadSupplier();
		loadReportSupplier();
	}

	private void addEditWindow(String addEdit, String orderId, String ar)
	{
		AddEditPurchaseOrder win = new AddEditPurchaseOrder(sessionBean, addEdit, orderId);
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
			loadReportSupplier();
		});
	}

	private void loadSupplier()
	{
		cmbSupplierName.removeAllItems();
		String fmDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());

		String sql = "select distinct sup.vSupplierId, sup.vSupplierCode, sup.vSupplierName, dbo.funGetNumeric(sup.vSupplierCode) iCode"+
				" from trans.tbPurchaseOrderInfo poi, master.tbSupplierMaster sup where poi.vSupplierId = sup.vSupplierId and poi.dOrderDate"+
				" between '"+fmDate+"' and '"+toDate+"' order by iCode asc";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupplierName.addItem(element[0].toString());
			cmbSupplierName.setItemCaption(element[0].toString(), element[1].toString()+" - "+element[2].toString());
		}

		String sqlStatus = "select vStatusId, vStatusName from master.tbAllStatus where iActive = 1 and vFlag = 'st' order by vStatusName";
		for (Iterator<?> iter = cm.selectSql(sqlStatus).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbStatus.addItem(element[0].toString());
			cmbStatus.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void ActiveInactiveSelectOrder(String orderId, int ar)
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
					if (pog.activeInactiveData(orderId, sessionBean.getUserId()))
					{
						tbChkActive.get(ar).setValue(!tbChkActive.get(ar).getValue().booleanValue());
						cm.showNotification("success", "Successfull!", "All information updated successfully.");
						loadTableInfo();
					}
					else
					{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
				}
			}
		});
	}

	private void PurchaseOrderPurchaseWindow(String orderId, String ar)
	{
		OrderToPurchase win = new OrderToPurchase(sessionBean, orderId);
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
		});
	}

	private void TransCancelWindow(String orderId, String ar)
	{
		TransactionCancel win = new TransactionCancel(sessionBean, orderId, "Purchase Order");
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

	private void TransApproveWindow(String orderId, String ar)
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
					if (tacm.TransactionApprove(orderId, sessionBean.getUserId(), "Purchase Order"))
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

	private boolean checkOrder(String orderId)
	{
		boolean ret = false;
		if (!pog.getOrderUse(orderId))
		{ ret = true; }
		else
		{ cm.showNotification("warning", "Warning!", "Order is in use in purchase information."); }
		return ret;
	}

	private void loadReportSupplier()
	{
		cmbSupplierReport.removeAllItems(); 
		String fmDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue());
		String branId = sessionBean.getBranchId();

		String sql = "select distinct poi.vSupplierId, sup.vSupplierCode, sup.vSupplierName, dbo.funGetNumeric(sup.vSupplierCode) iCode from"+
				" trans.tbPurchaseOrderInfo poi, master.tbSupplierMaster sup where poi.vSupplierId = sup.vSupplierId and dOrderDate between"+
				" '"+fmDate+"' and '"+toDate+"' and poi.vBranchId = '"+branId+"' order by iCode asc";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupplierReport.addItem(element[0].toString());
			cmbSupplierReport.setItemCaption(element[0].toString(), element[1].toString()+" - "+element[2].toString());
		}
	}

	private void loadOrderNo()
	{
		cmbOrderNoReport.removeAllItems();
		String fmDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue());
		String suppId = cm.getMultiComboValue(cmbSupplierReport);
		String branId = sessionBean.getBranchId();

		String sql = "select poi.vOrderId, poi.vOrderNo, convert(varchar(10), poi.dOrderDate, 105) dDate from trans.tbPurchaseOrderInfo poi,"+
				" master.tbAllStatus ast where poi.vStatusId = ast.vStatusId and poi.vSupplierId in (select Item from dbo.Split('"+suppId+"'))"+
				" and dOrderDate between '"+fmDate+"' and '"+toDate+"' and vBranchId = '"+branId+"' order by vOrderNo, dOrderDate desc";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			cmbOrderNoReport.addItem(element[0].toString());
			cmbOrderNoReport.setItemCaption(element[0].toString(),
					element[1].toString()+" ("+element[2].toString()+")"+" ("+element[3].toString()+")");
		}
	}

	private void addValidation()
	{
		if (txtFromDateReport.getValue() != null)
		{
			if (txtToDateReport.getValue() != null)
			{ viewReport(""); }
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

	public void viewReport(String orderIds)
	{
		String reportSource = "", sql = "";
		orderIds = orderIds.isEmpty()? cm.getMultiComboValue(cmbOrderNoReport):orderIds;
		try
		{
			HashMap<String, Object> hm = new HashMap<String, Object>();
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getBranchAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", sessionBean.getUserIp());

			sql = "select poi.vOrderNo, pod.vVatOption, pod.mVatAmount, poi.dOrderDate, poi.dDeliveryDate, poi.vRemarks,"+
					" poi.vReferenceNo, pod.vDescription, pod.mQuantity, pod.mUnitRate, vat.vVatCatName, pod.mVatAmount,"+
					" pod.mAmount, pod.mDiscount, pod.mNetAmount, poi.vOrderType, ast.vStatusName, poi.vRevisionNo,"+
					" pod.mVatPercent, sup.vSupplierCode, sup.vSupplierName, sup.vAddress, sup.vPhone, sup.vContactMobile,"+
					" ri.vItemName, cat.vCategoryName, uni.vUnitName, req.vRequisitionId, req.vRequisitionNo, req.dRequisitionDate,"+
					" br.vBranchName vReqFrom, bm.vBranchName vOrderFrom, bm.vAddress vOrdAdd, bm.vMobileNo, ui.vFullName,"+
					" ISNULL((select ui.vFullName from master.tbUserInfo ui where ui.vUserId = poi.vApprovedBy), '') vApprovedBy,"+
					" ISNULL((select ui.vFullName from master.tbUserInfo ui where ui.vUserId = poi.vCancelledBy), '') vCancelledBy"+
					" from trans.tbPurchaseOrderInfo poi inner join trans.tbPurchaseOrderDetails pod on poi.vOrderId = pod.vOrderId"+
					" inner join master.tbSupplierMaster sup on poi.vSupplierId = sup.vSupplierId inner join master.tbRawItemInfo ri"+
					" on pod.vItemId = ri.vItemId inner join master.tbItemCategory cat on ri.vCategoryId = cat.vCategoryId inner join"+
					" master.tbVatCatMaster vat on pod.vVatCatId = vat.vVatCatId inner join master.tbAllStatus ast on poi.vStatusId ="+
					" ast.vStatusId left join trans.tbRequisitionInfo req on req.vRequisitionId = poi.vRequisitionId left join"+
					" master.tbBranchMaster br on br.vBranchId = poi.vReqBranchId inner join master.tbBranchMaster bm on poi.vBranchId"+
					" = bm.vBranchId inner join master.tbUserInfo ui on ui.vUserId = poi.vModifiedBy inner join master.tbUnitInfo uni"+
					" on pod.vUnitId = convert(varchar(10), uni.iUnitId) where poi.vOrderId in (select Item from dbo.Split"+
					" ('"+orderIds+"')) order by poi.vOrderId, pod.iAutoId";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptPurchaseOrder.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private Panel addPanel()
	{
		Panel pnlTable = new Panel("Purchase Order List :: "+sessionBean.getCompanyName()+
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

		cmbSupplierName = new ComboBox();
		cmbSupplierName.setInputPrompt("Select Supplier Name");
		cmbSupplierName.setWidth("100%");
		cmbSupplierName.setDescription("Select one Supplier");
		cmbSupplierName.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbSupplierName.setFilteringMode(FilteringMode.CONTAINS);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Order");
		txtSearch.setDescription("Search by order number");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		cmbStatus = new ComboBox();
		cmbStatus.setImmediate(true);
		cmbStatus.setFilteringMode(FilteringMode.CONTAINS);
		cmbStatus.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbStatus.setInputPrompt("Select status");
		cmbStatus.setWidth("115px");

		cBtnS.btnSearch.setStyleName(ValoTheme.BUTTON_TINY);
		hori.addComponents(txtFromDate, txtToDate, cmbSupplierName, txtSearch, cmbStatus, cBtnS);

		buildTable();
		content.addComponents(hori, tblOrderList, tblOrderList.createControls());
		pnlTable.setContent(content);
		return pnlTable;
	}

	private void buildTable()
	{
		tblOrderList = new TablePaged();
		tblOrderList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String orderId = tbLblOrderId.get(ar).getValue().toString();
				if (checkOrder(orderId))
				{ addEditWindow("Edit", orderId, ar+""); }
			}
		});

		tblOrderList.addContainerProperty("Order Id", Label.class, new Label(), null, null, Align.CENTER);
		tblOrderList.setColumnCollapsed("Order Id", true);

		tblOrderList.addContainerProperty("Order No", Label.class, new Label(), null, null, Align.CENTER);

		tblOrderList.addContainerProperty("Order Date", Label.class, new Label(), null, null, Align.CENTER);

		tblOrderList.addContainerProperty("Supplier Name", Label.class, new Label(), null, null, Align.CENTER);

		tblOrderList.addContainerProperty("Delivery Date", Label.class, new Label(), null, null, Align.CENTER);

		tblOrderList.addContainerProperty("Amount", Label.class, new Label(), null, null, Align.RIGHT); 

		tblOrderList.addContainerProperty("Status", Label.class, new Label(), null, null, Align.CENTER);

		tblOrderList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblOrderList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblOrderList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblOrderId.add(ar, new Label());
			tbLblOrderId.get(ar).setWidth("100%");
			tbLblOrderId.get(ar).setImmediate(true);
			tbLblOrderId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblOrderNo.add(ar, new Label());
			tbLblOrderNo.get(ar).setWidth("100%");
			tbLblOrderNo.get(ar).setImmediate(true);
			tbLblOrderNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblOrderDate.add(ar, new Label());
			tbLblOrderDate.get(ar).setWidth("100%");
			tbLblOrderDate.get(ar).setImmediate(true);
			tbLblOrderDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblSupplierName.add(ar, new Label());
			tbLblSupplierName.get(ar).setWidth("100%");
			tbLblSupplierName.get(ar).setImmediate(true);
			tbLblSupplierName.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblDeliveryDate.add(ar, new Label());
			tbLblDeliveryDate.get(ar).setWidth("100%");
			tbLblDeliveryDate.get(ar).setImmediate(true);
			tbLblDeliveryDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblAmount.add(ar, new Label());
			tbLblAmount.get(ar).setWidth("100%");
			tbLblAmount.get(ar).setImmediate(true);
			tbLblAmount.get(ar).addStyleName(ValoTheme.LABEL_TINY);
			tbLblAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbLblStatus.add(ar, new Label());
			tbLblStatus.get(ar).setWidth("100%");
			tbLblStatus.get(ar).setImmediate(true);
			tbLblStatus.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbChkActive.add(ar, new CheckBox());
			tbChkActive.get(ar).setWidth("100%");
			tbChkActive.get(ar).setImmediate(true);
			tbChkActive.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbChkActive.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

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

			//Authorization check for purchase
			cm.setAuthorize(sessionBean.getUserId(), "purInfo");
			if (cm.insert)
			{
				tbCmbAction.get(ar).addItem("Purchase");
				tbCmbAction.get(ar).setItemIcon("Purchase", FontAwesome.SHOPPING_BASKET);
			}

			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String orderId = tbLblOrderId.get(ar).getValue().toString();
				if (!orderId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ if (checkOrder(orderId)) { addEditWindow("Edit", orderId, ar+""); } }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Active/Inactive"))
					{ if (checkOrder(orderId)) { ActiveInactiveSelectOrder(orderId, ar); } }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Preview"))
					{ viewReport(orderId); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Purchase"))
					{ if (checkOrder(orderId)) { PurchaseOrderPurchaseWindow(orderId, ar+""); } }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Cancel"))
					{ if (checkOrder(orderId)) { TransCancelWindow(orderId, ar+""); } }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Approve"))
					{ if (checkOrder(orderId)) { TransApproveWindow(orderId, ar+""); } }
				}
				tbCmbAction.get(ar).select(null);
			});

			tblOrderList.addItem(new Object[]{tbLblOrderId.get(ar), tbLblOrderNo.get(ar),tbLblOrderDate.get(ar), tbLblSupplierName.get(ar),
					tbLblDeliveryDate.get(ar), tbLblAmount.get(ar), tbLblStatus.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private Panel addReport()
	{
		Panel panelReport = new Panel("Purchase Order Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 5);
		lay.setSpacing(true);

		txtFromDateReport  = new PopupDateField();
		txtFromDateReport.setImmediate(true);
		txtFromDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateReport.setValue(new Date());
		txtFromDateReport.setWidth("110px");
		txtFromDateReport.setDateFormat("dd-MM-yyyy");
		txtFromDateReport.setRequired(true);
		txtFromDateReport.setRequiredError("This field is required");
		lay.addComponent(new Label("From Date: "), 0, 0);
		lay.addComponent(txtFromDateReport, 1, 0);

		txtToDateReport  = new PopupDateField();
		txtToDateReport.setImmediate(true);
		txtToDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateReport.setValue(new Date());
		txtToDateReport.setWidth("110px");
		txtToDateReport.setDateFormat("dd-MM-yyyy");
		txtToDateReport.setRequired(true);
		txtToDateReport.setRequiredError("This field is required");
		lay.addComponent(new Label("To Date: "), 0, 1);
		lay.addComponent(txtToDateReport, 1, 1);

		cmbSupplierReport = new MultiComboBox();
		cmbSupplierReport.setWidth("450px");
		cmbSupplierReport.setInputPrompt("Select supplier name");
		cmbSupplierReport.setRequired(true);
		cmbSupplierReport.setRequiredError("This field is required");
		lay.addComponent(new Label("Supplier Name: "), 0, 2);
		lay.addComponent(cmbSupplierReport, 1, 2);

		cmbOrderNoReport = new MultiComboBox();
		cmbOrderNoReport.setWidth("450px");
		cmbOrderNoReport.setInputPrompt("Select order no");
		cmbOrderNoReport.setRequired(true);
		cmbOrderNoReport.setRequiredError("This field is required");
		lay.addComponent(new Label("Order No: "), 0, 3);
		lay.addComponent(cmbOrderNoReport, 1, 3);

		lay.addComponent(cBtnV, 1, 4);
		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelReport.setContent(content);

		return panelReport;
	}

	private void loadTableInfo()
	{
		String search = "%"+txtSearch.getValue().toString()+"%", statId = "", statNm = "";;
		String suppId = cmbSupplierName.getValue() != null? cmbSupplierName.getValue().toString():"%";
		String status = cmbStatus.getValue() != null? cmbStatus.getValue().toString():"%";
		String fmDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		String branId = sessionBean.getBranchId();

		tableClear();
		int i = 0;
		try
		{
			String sql = "select poi.vOrderId, poi.vOrderNo, poi.dOrderDate, poi.dDeliveryDate, sup.vSupplierCode+' - '+sup.vSupplierName"+
					" vSupDetails, poi.vRemarks, SUM(pod.mAmount) mAmount, poi.iActive, poi.vStatusId, ast.vStatusName, poi.iAutoId from"+
					" trans.tbPurchaseOrderInfo poi, trans.tbPurchaseOrderDetails pod, master.tbSupplierMaster sup, master.tbAllStatus ast"+
					" where poi.vOrderId = pod.vOrderId and poi.vSupplierId = sup.vSupplierId and poi.vStatusId = ast.vStatusId and poi.vOrderNo"+
					" like '"+search+"' and poi.dOrderDate between '"+fmDate+"' and '"+toDate+"' and poi.vStatusId like '"+status+"' and"+
					" poi.vBranchId = '"+branId+"' and poi.vSupplierId = '"+suppId+"' group by poi.vOrderId, poi.vOrderNo, poi.dOrderDate,"+
					" poi.dDeliveryDate, sup.vSupplierCode+' - '+sup.vSupplierName, poi.vRemarks, poi.iActive, poi.vStatusId, ast.vStatusName,"+
					" poi.iAutoId order by poi.dOrderDate, poi.iAutoId desc";
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblOrderId.size() <= i)
				{ tableRowAdd(i); }

				tbLblOrderId.get(i).setValue(element[0].toString());
				tbLblOrderNo.get(i).setValue(element[1].toString());
				tbLblOrderDate.get(i).setValue(cm.dfBd.format(element[2]));
				tbLblDeliveryDate.get(i).setValue(cm.dfBd.format(element[3]));
				tbLblSupplierName.get(i).setValue(element[4].toString());
				tbLblAmount.get(i).setValue(cm.setComma(Double.parseDouble(element[6].toString())));
				tbChkActive.get(i).setValue((element[7].toString().equals("1")? true:false));
				tbChkActive.get(i).setEnabled(false);

				statId = element[8].toString();
				statNm = element[9].toString();

				if (statId.equals("S5"))
				{ status = "<b style=\"color:orange\">"+statNm+"</b>"; }
				else if (statId.equals("S6"))
				{ status = "<b style=\"color:green\">"+statNm+"</b>"; }
				else if (statId.equals("S7"))
				{ status = "<b style=\"color:red\">"+statNm+"</b>"; }
				tbLblStatus.get(i).setValue(status);

				if (statId.equals("S6"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Approve");
					tbCmbAction.get(i).removeItem("Purchase");
				}
				if (statId.equals("S7"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Cancel");
					tbCmbAction.get(i).removeItem("Approve");
					tbCmbAction.get(i).removeItem("Purchase");
				}
				i++;
			}
			tblOrderList.nextPage();
			tblOrderList.previousPage();

			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			totalAmount();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblOrderList, tbLblOrderId); }

	private double totalAmount()
	{
		double amt = 0;
		for (int i=0; i<tbLblOrderId.size(); i++)
		{ amt += cm.getAmtValue(tbLblAmount.get(i)); }
		tblOrderList.setColumnFooter("Amount", cm.setComma(amt));
		tblOrderList.setColumnAlignment("Amount", Align.RIGHT);		
		return amt;
	}

	public void enter(ViewChangeEvent event)
	{
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
		loadTableInfo();

		loadReportSupplier();
	}
}
