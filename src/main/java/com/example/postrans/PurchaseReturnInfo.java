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
import com.example.gateway.PurchaseReturnGateway;
import com.example.gateway.TransAppCanGateway;
import com.common.share.ReportViewer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class PurchaseReturnInfo extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblPurchaseReturnList;
	private ArrayList<Label> tbLblReturnId = new ArrayList<Label>();
	private ArrayList<Label> tbLblReturnNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblReturnDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblPurchaseNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblSupplierName = new ArrayList<Label>();
	private ArrayList<Label> tbLblItemNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblAmount = new ArrayList<Label>();
	private ArrayList<Label> tbLblStatus = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	
	private SessionBean sessionBean;
	private TextField txtSearch;
	private ComboBox cmbSupplierName;
	private PopupDateField txtFromDate, txtToDate;
	private CommonButton cBtnS = new CommonButton("", "", "", "", "", "Search", "", "", "");

	private CommonMethod cm;
	private PurchaseReturnGateway prg = new PurchaseReturnGateway();
	private String formId;

	///Report panel  purchases
	private PopupDateField txtFromDateReport, txtToDateReport;
	private ComboBox cmbSupplierForReport;
	private MultiComboBox cmbReturnNo;
	private CommonButton cBtnReport = new CommonButton("", "", "", "", "", "", "", "View", "");

	public PurchaseReturnInfo(SessionBean sessionBean, String formId)
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

		cBtnReport.btnPreview.addClickListener(event ->
		{ addValidationReport(); });

		txtFromDate.addValueChangeListener(event ->
		{ checkSearchSupplierLoad(); });

		txtToDate.addValueChangeListener(event ->
		{ checkSearchSupplierLoad(); });

		txtFromDateReport.addValueChangeListener(event ->
		{ checkReportSupplierLoad(); });

		txtToDateReport.addValueChangeListener(event ->
		{ checkReportSupplierLoad(); });

		cmbSupplierForReport.addValueChangeListener(event ->
		{ loadReturnNo(); });

		loadSupplier();
		checkReportSupplierLoad();
	}

	//Table Method
	private void checkSearchSupplierLoad()
	{
		if (txtFromDate.getValue() != null)
		{
			if (txtToDate.getValue() != null)
			{ loadSupplier(); }
			else
			{
				txtToDate.focus();
				cm.showNotification("warning", "Warning!", "Select to date.");
			}
		}
		else
		{
			txtFromDate.focus();
			cm.showNotification("warning", "Warning!", "Select from date.");
		}
	}

	private void loadSupplier()
	{
		cmbSupplierName.removeAllItems();
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		String sql = "select sm.vSupplierId, sm.vSupplierCode, sm.vSupplierName, dbo.funGetNumeric(sm.vSupplierCode) iCode"+
				" from master.tbSupplierMaster sm where vSupplierId in (select pui.vSupplierId from trans.tbPurchaseInfo"+
				" pui where pui.vPurchaseId in (select prd.vPurchaseId from trans.tbPurchaseReturnInfo prd where"+
				" prd.dReturnDate between '"+fromDate+"' and '"+toDate+"')) order by iCode";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupplierName.addItem(element[0].toString());
			cmbSupplierName.setItemCaption(element[0].toString(), element[1].toString()+" - "+element[2].toString());
		}
	}

	private void checkReportSupplierLoad()
	{
		if (txtFromDateReport.getValue() != null)
		{
			if (txtToDateReport.getValue() != null)
			{ loadReportSupplier(); }
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

	private void loadReportSupplier()
	{
		cmbSupplierForReport.removeAllItems(); 
		String fromDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue());
		String sql = "select sm.vSupplierId, sm.vSupplierCode, sm.vSupplierName, dbo.funGetNumeric(sm.vSupplierCode) iCode"+
				" from master.tbSupplierMaster sm where vSupplierId in (select pui.vSupplierId from trans.tbPurchaseInfo"+
				" pui where pui.vPurchaseId in (select prd.vPurchaseId from trans.tbPurchaseReturnInfo prd where"+
				" prd.dReturnDate between '"+fromDate+"' and '"+toDate+"')) order by iCode";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupplierForReport.addItem(element[0].toString());
			cmbSupplierForReport.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadReturnNo()
	{
		cmbReturnNo.removeAllItems();
		String fromDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue());
		String supplierId = cmbSupplierForReport.getValue() == null? "0":cmbSupplierForReport.getValue().toString();
		String sql = "select vReturnId, vReturnNo from trans.tbPurchaseReturnInfo where vSupplierId like '"+supplierId+"'"+
				" and dReturnDate between '"+fromDate+"' and '"+toDate+"' and a.vBranchId = '"+sessionBean.getBranchId()+"'"+
				" order by vReturnNo";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbReturnNo.addItem(element[0].toString());
			cmbReturnNo.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void addEditWindow(String addEdit, String itemId, String ar)
	{
		AddEditPurchaseReturn win = new AddEditPurchaseReturn(sessionBean, addEdit, itemId);
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
		String supplier = cmbSupplierName.getValue() == null? "%":cmbSupplierName.getValue().toString();
		String search = "%"+txtSearch.getValue().toString()+"%";
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());

		tableClear();
		int i = 0;
		try
		{
			String sql = "select pin.vReturnId, pin.vReturnNo, pin.dReturnDate, (select vSupplierName from"+
					" master.tbSupplierMaster where vSupplierId = pin.vSupplierId) vSupplierName, pin.vRemarks,"+
					" (select isnull(sum(mNetAmount),0) from trans.tbPurchaseReturnDetails where vReturnId ="+
					" pin.vReturnId)Amount, (select isnull(count(vReturnId),0) from trans.tbPurchaseReturnDetails"+
					" where vReturnId = pin.vReturnId)ItemCount, pin.iActive, pin.vStatusId, ast.vStatusName, (select"+
					" vPurchaseNo from trans.tbPurchaseInfo where vPurchaseId = pin.vPurchaseId)vPurchaseNo from"+
					" trans.tbPurchaseReturnInfo pin, master.tbAllStatus ast where pin.vStatusId = ast.vStatusId"+
					" and pin.vReturnNo like '"+search+"' and pin.vSupplierId like '"+supplier+"' and pin.dReturnDate"+
					" between '"+fromDate+"' and '"+toDate+"' and pin.vBranchId = '"+sessionBean.getBranchId()+"'"+
					" order by pin.dReturnDate desc";
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				if (tbLblReturnId.size() <= i)
				{ tableRowAdd(i); }
				tbLblReturnId.get(i).setValue(element[0].toString());
				tbLblReturnNo.get(i).setValue(element[1].toString());
				tbLblReturnDate.get(i).setValue(cm.dfBd.format(element[2]));
				tbLblSupplierName.get(i).setValue(element[3].toString());
				tbLblPurchaseNo.get(i).setValue(element[10].toString());
				tbLblItemNo.get(i).setValue(element[6].toString());
				tbLblAmount.get(i).setValue(cm.setComma(Double.parseDouble(element[5].toString())));
				tbLblStatus.get(i).setValue(element[9].toString());
				tbChkActive.get(i).setValue((element[7].toString().equals("1")? true:false));
				tbChkActive.get(i).setEnabled(false);
				if (element[8].toString().equals("S6"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Approve");
				}
				if (element[8].toString().equals("S7"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Cancel");
					tbCmbAction.get(i).removeItem("Approve");
				}
				i++;
			}
			tblPurchaseReturnList.nextPage();
			tblPurchaseReturnList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			totalAmount();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	public double totalAmount()
	{
		double amt = 0;
		for (int i=0; i<tbLblReturnId.size(); i++)
		{ amt += cm.getAmtValue(tbLblAmount.get(i)); }
		tblPurchaseReturnList.setColumnFooter("Amount", cm.setComma(amt));
		tblPurchaseReturnList.setColumnAlignment("Amount", Align.RIGHT);		
		return amt;
	}

	private void ActiveInactiveSelectReturn(String returnId, int ar)
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
					if (prg.activeInactiveData(returnId, sessionBean.getUserId()))
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

	public void viewReportSingle(String returnId)
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
			hm.put("userIp", sessionBean.getUserIp());

			sql = "select a.vReturnNo, a.dReturnDate, O.vPurchaseNo, b.vVatOption, b.mVatAmount, O.dPurchaseDate, a.vRemarks,"+
					" a.vReferenceNo, b.vDescription, b.mVatPercent, b.mQuantity, b.mUnitRate, V.vVatCatName, b.mVatAmount,"+
					" b.mAmount, b.mDiscount, b.mNetAmount, a.vReturnType, ast.vStatusName, s.vSupplierName, s.vAddress,"+
					" s.vPhone, s.vFax, s.vEmail, I.vItemName, C.vCategoryName, b.vUnitName, s.vContactMobile from"+
					" trans.tbPurchaseReturnInfo a inner join trans.tbPurchaseReturnDetails b on a.vReturnId = b.vReturnId"+
					" inner join master.tbSupplierMaster s on a.vSupplierId = s.vSupplierId inner join master.tbRawItemInfo I"+
					" on b.vItemId = I.vItemId inner join master.tbItemCategory C on I.vCategoryId = C.vCategoryId inner join"+
					" master.tbVatCatMaster V on b.vVatCatId = v.vVatCatId left join trans.tbPurchaseInfo O on a.vPurchaseId"+
					" = O.vPurchaseId inner join master.tbAllStatus ast on a.vStatusId = ast.vStatusId where a.vReturnId ="+
					" '"+returnId+"' order by a.vReturnId,b.iAutoId";

			reportSource = "com/jasper/postransaction/rptPurchaseReturn.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	public void viewReport()
	{
		String reportSource = "", sql = "";
		String returnId = cmbReturnNo.getValue().toString().replace("]", "").replace("[", "").trim();
		String returnIds = returnId.isEmpty()? "%":returnId;
		String supplierIds = cmbSupplierForReport.getValue() == null? "%":cmbSupplierForReport.getValue().toString();
		String fromDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue());
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

			sql = "select a.vReturnNo, a.dReturnDate, O.vPurchaseNo, b.vVatOption, b.mVatAmount, O.dPurchaseDate, a.vRemarks,"+
					" a.vReferenceNo, b.vDescription, b.mVatPercent, b.mQuantity, b.mUnitRate, V.vVatCatName, b.mVatAmount,"+
					" b.mAmount, b.mDiscount, b.mNetAmount, a.vReturnType, ast.vStatusName, s.vSupplierName, s.vAddress, s.vPhone,"+
					" s.vFax, s.vEmail, I.vItemName, C.vCategoryName, b.vUnitName, s.vContactMobile from trans.tbPurchaseReturnInfo"+
					" a inner join trans.tbPurchaseReturnDetails b on a.vReturnId = b.vReturnId inner join master.tbSupplierMaster"+
					" s on a.vSupplierId = s.vSupplierId inner join master.tbRawItemInfo I on b.vItemId = I.vItemId inner join"+
					" master.tbItemCategory C on I.vCategoryId = C.vCategoryId inner join master.tbVatCatMaster V on b.vVatCatId"+
					" = v.vVatCatId left join trans.tbPurchaseInfo O on a.vPurchaseId = O.vPurchaseId inner join master.tbAllStatus"+
					" ast on a.vStatusId = ast.vStatusId where a.vReturnId in (select Item from dbo.Split('"+returnIds+"')) and"+
					" a.vSupplierId like '"+supplierIds+"' and a.dReturnDate between '"+fromDate+"' and '"+toDate+"' and"+
					" a.vBranchId = '"+sessionBean.getBranchId()+"' order by a.vReturnId, b.iAutoId";

			reportSource = "com/jasper/postransaction/rptPurchaseReturn.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private void tableClear()
	{ cm.tableClear(tblPurchaseReturnList, tbLblReturnId); }

	private void addValidationReport()
	{
		if (txtFromDateReport.getValue() != null)
		{
			if (txtToDateReport.getValue() != null)
			{ viewReport(); }
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

	private void TransactionCancelWindow(String transId, String ar)
	{
		TransactionCancel win = new TransactionCancel(sessionBean, transId, "Purchase Return");
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
					if (tacm.TransactionApprove(orderId, sessionBean.getUserId(), "Purchase Return"))
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

	//Table Panel start 
	private Panel addPanel()
	{
		Panel pnlTable = new Panel("Purchase Return List :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout hori = new HorizontalLayout();
		hori.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search by Return No");
		txtSearch.setDescription("Search by");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		cmbSupplierName = new ComboBox();
		cmbSupplierName.setInputPrompt("Select Supplier Name");
		cmbSupplierName.setWidth("100%");
		cmbSupplierName.setDescription("Select one Supplier");
		cmbSupplierName.addStyleName(ValoTheme.COMBOBOX_TINY);
		/*cmbSupplierName.setRequired(true);*/
		cmbSupplierName.setFilteringMode(FilteringMode.CONTAINS);

		txtFromDate  = new PopupDateField();
		txtFromDate.setImmediate(true);
		txtFromDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDate.setValue(new Date());
		txtFromDate.setWidth("110px");
		txtFromDate.setDateFormat("dd-MM-yyyy");
		txtFromDate.setDescription("From Date");
		txtFromDate.setRequired(true);

		txtToDate  = new PopupDateField();
		txtToDate.setImmediate(true);
		txtToDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDate.setValue(new Date());
		txtToDate.setWidth("110px");
		txtToDate.setDateFormat("dd-MM-yyyy");
		txtToDate.setDescription("To Date");
		txtToDate.setRequired(true);

		cBtnS.btnSearch.setStyleName(ValoTheme.BUTTON_TINY);
		hori.addComponents(txtFromDate, txtToDate, cmbSupplierName, txtSearch, cBtnS);

		buildTable();
		content.addComponents(hori, tblPurchaseReturnList, tblPurchaseReturnList.createControls());
		pnlTable.setContent(content);
		return pnlTable;
	}

	private void buildTable()
	{
		tblPurchaseReturnList = new TablePaged();
		tblPurchaseReturnList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblReturnId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblPurchaseReturnList.addContainerProperty("Return Id", Label.class, new Label(), null, null, Align.CENTER);
		tblPurchaseReturnList.setColumnCollapsed("Return Id", true);

		tblPurchaseReturnList.addContainerProperty("Return No", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseReturnList.addContainerProperty("Return Date", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseReturnList.addContainerProperty("Purchase No", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseReturnList.addContainerProperty("Supplier Name", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseReturnList.addContainerProperty("Item No", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseReturnList.addContainerProperty("Amount", Label.class, new Label(), null, null, Align.RIGHT);

		tblPurchaseReturnList.addContainerProperty("Status", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseReturnList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblPurchaseReturnList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblPurchaseReturnList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblReturnId.add(ar, new Label());
			tbLblReturnId.get(ar).setWidth("100%");
			tbLblReturnId.get(ar).setImmediate(true);
			tbLblReturnId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblReturnNo.add(ar, new Label());
			tbLblReturnNo.get(ar).setWidth("100%");
			tbLblReturnNo.get(ar).setImmediate(true);
			tbLblReturnNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblReturnDate.add(ar, new Label());
			tbLblReturnDate.get(ar).setWidth("100%");
			tbLblReturnDate.get(ar).setImmediate(true);
			tbLblReturnDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblPurchaseNo.add(ar, new Label());
			tbLblPurchaseNo.get(ar).setWidth("100%");
			tbLblPurchaseNo.get(ar).setImmediate(true);
			tbLblPurchaseNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblSupplierName.add(ar, new Label());
			tbLblSupplierName.get(ar).setWidth("100%");
			tbLblSupplierName.get(ar).setImmediate(true);
			tbLblSupplierName.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblItemNo.add(ar, new Label());
			tbLblItemNo.get(ar).setWidth("100%");
			tbLblItemNo.get(ar).setImmediate(true);
			tbLblItemNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

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

			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String returnId = tbLblReturnId.get(ar).getValue().toString();
				if (!returnId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ addEditWindow("Edit", returnId, ar+""); }	

					else if (tbCmbAction.get(ar).getValue().toString().equals("Active/Inactive"))
					{ ActiveInactiveSelectReturn(returnId, ar); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Preview"))
					{ viewReportSingle(returnId); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Cancel"))
					{ TransactionCancelWindow(returnId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Approve"))
					{ TransactionApproveWindow(returnId, ar+""); }
				}
				tbCmbAction.get(ar).select(null);
			});
			tblPurchaseReturnList.addItem(new Object[]{tbLblReturnId.get(ar), tbLblReturnNo.get(ar),
					tbLblReturnDate.get(ar), tbLblPurchaseNo.get(ar), tbLblSupplierName.get(ar), tbLblItemNo.get(ar),
					tbLblAmount.get(ar), tbLblStatus.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private HorizontalLayout addReportPanel()
	{
		HorizontalLayout lay = new HorizontalLayout();
		lay.setSizeFull();
		lay.setSpacing(true);
		lay.addComponents(addPurchasePanel());
		return lay;
	}

	//Report Panel Purchase return Start
	private Panel addPurchasePanel()
	{
		Panel panelReport = new Panel("Purchase Return Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		panelReport.setHeight("300px");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(3, 6);
		lay.setSpacing(true);

		txtFromDateReport = new PopupDateField();
		txtFromDateReport.setImmediate(true);
		txtFromDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateReport.setValue(new Date());
		txtFromDateReport.setWidth("-1px");
		txtFromDateReport.setDateFormat("dd-MM-yyyy");
		txtFromDateReport.setDescription("Return Date From");
		txtFromDateReport.setInputPrompt("From Date");
		txtFromDateReport.setRequired(true);
		txtFromDateReport.setRequiredError("This field is required");
		lay.addComponent(new Label("From Date: "), 0, 0);
		lay.addComponent(txtFromDateReport, 1, 0);

		txtToDateReport = new PopupDateField();
		txtToDateReport.setImmediate(true);
		txtToDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateReport.setValue(new Date());
		txtToDateReport.setWidth("-1px");
		txtToDateReport.setDateFormat("dd-MM-yyyy");
		txtToDateReport.setDescription("Return Date To");
		txtToDateReport.setInputPrompt("To Date");
		txtToDateReport.setRequired(true);
		txtToDateReport.setRequiredError("This field is required");
		lay.addComponent(new Label("To Date: "), 0, 1);
		lay.addComponent(txtToDateReport, 1, 1);

		cmbSupplierForReport = new ComboBox();
		cmbSupplierForReport.setWidth("350px");
		cmbSupplierForReport.setInputPrompt("Select Supplier Name");
		cmbSupplierForReport.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbSupplierForReport.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Supplier Name: "), 0, 2);
		lay.addComponent(cmbSupplierForReport, 1, 2);

		cmbReturnNo = new MultiComboBox();
		cmbReturnNo.setWidth("350px");
		cmbReturnNo.setInputPrompt("Select Order No");
		cmbReturnNo.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbReturnNo.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Return No: "), 0, 3);
		lay.addComponent(cmbReturnNo, 1, 3);

		lay.addComponent(cBtnReport, 1, 5);

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
	}
}
