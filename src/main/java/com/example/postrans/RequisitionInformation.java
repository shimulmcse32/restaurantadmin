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
import com.example.gateway.RequisitionInfoGateway;
import com.example.gateway.TransAppCanGateway;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.MultiComboBox;
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
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class RequisitionInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");
	private TablePaged tblRequisitionList;
	private ArrayList<Label> tbLblRequisitionId = new ArrayList<Label>();
	private ArrayList<Label> tbLblRequisitionNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblRequisitionDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblBranchName = new ArrayList<Label>();
	private ArrayList<Label> tbLblDeliveryDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblStatus = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();

	private SessionBean sessionBean;
	private Panel pnlTable;
	private TextField txtSearch;
	private ComboBox cmbBranchName, cmbStatus;
	private PopupDateField txtFromDate, txtToDate;
	private CommonButton cBtnS = new CommonButton("", "", "", "", "", "Search", "", "", "");

	private CommonMethod cm;
	private RequisitionInfoGateway rig = new RequisitionInfoGateway();

	// Requisition report
	private Panel panelReport;
	private PopupDateField txtReportFromDate, txtReportToDate;
	private ComboBox cmbBranchForReport;
	private MultiComboBox cmbRequisitionNo;
	private CommonButton cBtnV = new CommonButton("", "", "", "", "", "", "", "View", "");

	public RequisitionInformation(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		addComponents(cBtn, addPanel(), addReport());
		cBtn.btnNew.setEnabled(cm.insert);

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

		txtReportFromDate.addValueChangeListener(event ->
		{ checkReportSupplierLoad(); });

		txtReportToDate.addValueChangeListener(event ->
		{ checkReportSupplierLoad(); });

		txtFromDate.addValueChangeListener(event ->
		{ checkSearchSupplierLoad(); });

		txtToDate.addValueChangeListener(event ->
		{ checkSearchSupplierLoad(); });

		cmbBranchForReport.addValueChangeListener(event ->
		{ loadRequisitionNo(); });
		loadBranchList();
	}

	private void checkSearchSupplierLoad()
	{
		if (txtFromDate.getValue() != null)
		{
			if (txtToDate.getValue() != null)
			{ loadBranchList(); }
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

	private void loadBranchList()
	{
		cmbBranchName.removeAllItems();
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());

		String sqlC = "select distinct a.vReqBranchId, b.vBranchName from trans.tbRequisitionInfo a inner join"+
				" master.tbBranchMaster b on b.vBranchId = a.vReqBranchId where dRequisitionDate between '"+fromDate+"'"+
				" and '"+toDate+"' and a.vBranchId = '"+sessionBean.getBranchId()+"' Order by b.vBranchName";
		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbBranchName.addItem(element[0].toString());
			cmbBranchName.setItemCaption(element[0].toString(), element[1].toString());
		}

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
		AddEditRequisition win = new AddEditRequisition(sessionBean, addEdit, itemId);
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

	private void loadTableInfo()
	{
		String branch = cmbBranchName.getValue() == null? "%":cmbBranchName.getValue().toString();
		String search = "%"+txtSearch.getValue().toString()+"%";
		String status = cmbStatus.getValue() != null? cmbStatus.getValue().toString():"%";
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		tableClear();
		int i = 0;
		try
		{
			String sql = "select pin.vRequisitionId, pin.vRequisitionNo, pin.dRequisitionDate, pin.dDeliveryDate,"+
					" (select sm.vBranchName from master.tbBranchMaster sm where sm.vBranchId = pin.vReqBranchId)"+
					" vBranchName, pin.vRemarks, pin.iActive, pin.vStatusId, ast.vStatusName from trans.tbRequisitionInfo"+
					" pin, master.tbAllStatus ast where vRequisitionNo like '"+search+"' and vReqBranchId like"+
					" '"+branch+"' and pin.vStatusId = ast.vStatusId and pin.dRequisitionDate between '"+fromDate+"'"+
					" and '"+toDate+"' and pin.vStatusId like '"+status+"' and pin.vBranchId = '"+sessionBean.getBranchId()+"'"+
					" Order by pin.dRequisitionDate, pin.iAutoId desc";
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblRequisitionId.size() <= i)
				{ tableRowAdd(i); }
				tbLblRequisitionId.get(i).setValue(element[0].toString());
				tbLblRequisitionNo.get(i).setValue(element[1].toString());
				tbLblRequisitionDate.get(i).setValue(cm.dfBd.format(element[2]));
				tbLblDeliveryDate.get(i).setValue(cm.dfBd.format(element[3]));
				tbLblBranchName.get(i).setValue(element[4].toString());
				tbLblStatus.get(i).setValue(element[8].toString());
				tbChkActive.get(i).setValue((element[6].toString().equals("1")? true:false));
				tbChkActive.get(i).setEnabled(false);
				if (element[7].toString().equals("S6"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Approve");
				}
				if (element[7].toString().equals("S7"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Cancel");
					tbCmbAction.get(i).removeItem("Approve");
				}
				i++;
			}
			tblRequisitionList.nextPage();
			tblRequisitionList.previousPage();

			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblRequisitionList, tbLblRequisitionId); }

	private void EditSelectRequisition(String requisitionId, int ar)
	{ addEditWindow("Edit", requisitionId, ar+""); }

	private void ActiveInactiveSelectRequisition(String requisitionId, int ar)
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
					if (rig.activeInactiveData(requisitionId, sessionBean.getUserId()))
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

	private void TransactionCancelWindow(String transId, String ar)
	{
		TransactionCancel win = new TransactionCancel(sessionBean, transId, "Requisition");
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
					if (tacm.TransactionApprove(transId, sessionBean.getUserId(), "Requisition"))
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

	private void checkReportSupplierLoad()
	{
		if (txtReportFromDate.getValue() != null)
		{
			if (txtReportToDate.getValue() != null)
			{ loadReportSupplier(); }
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

	private void loadReportSupplier()
	{
		cmbBranchForReport.removeAllItems(); 
		String fromDate = cm.dfDb.format(txtReportFromDate.getValue());
		String toDate = cm.dfDb.format(txtReportToDate.getValue());

		String sqlC = "select distinct a.vReqBranchId, b.vBranchName from trans.tbRequisitionInfo a inner join"+
				" master.tbBranchMaster b on b.vBranchId = a.vReqBranchId where dRequisitionDate between '"+fromDate+"'"+
				" and '"+toDate+"' and a.vBranchId = '"+sessionBean.getBranchId()+"' Order by b.vBranchName";

		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbBranchForReport.addItem(element[0].toString());
			cmbBranchForReport.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadRequisitionNo()
	{
		cmbRequisitionNo.removeAllItems();

		String fromDate = cm.dfDb.format(txtReportFromDate.getValue());
		String toDate = cm.dfDb.format(txtReportToDate.getValue());
		String branchIds = cmbBranchForReport.getValue() == null? "0":cmbBranchForReport.getValue().toString();

		String sqlC = "select vRequisitionId, vRequisitionNo from trans.tbRequisitionInfo where vReqBranchId like"+
				" '"+branchIds+"' and dRequisitionDate between '"+fromDate+"' and '"+toDate+"' and a.vBranchId"+
				" = '"+sessionBean.getBranchId()+"' Order by vRequisitionNo";
		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbRequisitionNo.addItem(element[0].toString());
			cmbRequisitionNo.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void addValidation()
	{
		if (txtReportFromDate.getValue() != null)
		{
			if (txtReportToDate.getValue() != null)
			{ viewReport(""); }
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

	public void viewReport(String reqIds)
	{
		String reportSource = "", sql = "";
		reqIds = reqIds.isEmpty()?cmbRequisitionNo.getValue().toString().replace("]", "").replace("[", "").trim():reqIds;
		/*String branchIds = cmbBranchForReport.getValue() == null? "%":cmbBranchForReport.getValue().toString();
		String fromDate = cm.dfDb.format(txtReportFromDate.getValue());
		String toDate = cm.dfDb.format(txtReportToDate.getValue());*/
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
			sql = "select rei.vRequisitionNo, rei.dRequisitionDate, rei.dDeliveryDate, rei.vRemarks, rei.vReferenceNo,"+
					" red.vDescription, red.mQuantity, ast.vStatusName, br.vBranchName vToBranch, br.vAddress vToAddress,"+
					" bm.vBranchName vFromBranch, bm.vAddress vFromAddress, ri.vItemCode, ri.vItemName, C.vCategoryName,"+
					" uni.vUnitName, br.vMobileNo, ui.vFullName, ISNULL((select ui.vFullName from master.tbUserInfo ui"+
					" where ui.vUserId = rei.vApprovedBy), '') vApprovedBy, ISNULL((select ui.vFullName from master.tbUserInfo"+
					" ui where ui.vUserId = rei.vCancelledBy), '') vCancelledBy from trans.tbRequisitionInfo rei,"+
					" trans.tbRequisitionDetails red, master.tbBranchMaster br, master.tbRawItemInfo ri, master.tbItemCategory C,"+
					" master.tbAllStatus ast, master.tbUserInfo ui, master.tbBranchMaster bm, master.tbUnitInfo uni where"+
					" rei.vRequisitionId = red.vRequisitionId and rei.vReqBranchId = br.vBranchId and red.vItemId = ri.vItemId"+
					" and ri.vCategoryId = C.vCategoryId and rei.vStatusId = ast.vStatusId and rei.vModifiedBy = ui.vUserId"+
					" and bm.vBranchId = rei.vBranchId and red.vUnitId = convert(varchar(10), uni.iUnitId) and rei.vRequisitionId in"+
					"(select Item from dbo.Split('"+reqIds+"'))order by rei.vRequisitionNo, rei.dRequisitionDate, red.iAutoId";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptRequisition.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private Panel addPanel()
	{
		pnlTable = new Panel("Requisition List :: "+sessionBean.getCompanyName()+
				"("+this.sessionBean.getBranchName()+")");
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

		cmbBranchName = new ComboBox();
		cmbBranchName.setInputPrompt("Select Branch Name");
		cmbBranchName.setWidth("100%");
		cmbBranchName.setDescription("Select one Branch");
		cmbBranchName.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbBranchName.setFilteringMode(FilteringMode.CONTAINS);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Requisition");
		txtSearch.setDescription("Search by Requisition number");
		txtSearch.setWidth("120px");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		cmbStatus = new ComboBox();
		cmbStatus.setImmediate(true);
		cmbStatus.setFilteringMode(FilteringMode.CONTAINS);
		cmbStatus.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbStatus.setInputPrompt("Select status");
		cmbStatus.setWidth("115px");

		cBtnS.btnSearch.setStyleName(ValoTheme.BUTTON_TINY);
		hori.addComponents(txtFromDate, txtToDate, cmbBranchName, txtSearch, cmbStatus, cBtnS);

		buildTable();
		content.addComponents(hori, tblRequisitionList, tblRequisitionList.createControls());
		pnlTable.setContent(content);
		return pnlTable;
	}

	private void buildTable()
	{
		tblRequisitionList = new TablePaged();
		tblRequisitionList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblRequisitionId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblRequisitionList.addContainerProperty("Requisition Id", Label.class, new Label(), null, null, Align.CENTER);
		tblRequisitionList.setColumnCollapsed("Requisition Id", true);

		tblRequisitionList.addContainerProperty("Requisition No", Label.class, new Label(), null, null, Align.CENTER);

		tblRequisitionList.addContainerProperty("Requisition Date", Label.class, new Label(), null, null, Align.CENTER);

		tblRequisitionList.addContainerProperty("Requested To", Label.class, new Label(), null, null, Align.CENTER);

		tblRequisitionList.addContainerProperty("Delivery Date", Label.class, new Label(), null, null, Align.CENTER);

		tblRequisitionList.addContainerProperty("Status", Label.class, new Label(), null, null, Align.CENTER);

		tblRequisitionList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblRequisitionList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblRequisitionList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblRequisitionId.add(ar, new Label());
			tbLblRequisitionId.get(ar).setWidth("100%");
			tbLblRequisitionId.get(ar).setImmediate(true);
			tbLblRequisitionId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblRequisitionNo.add(ar, new Label());
			tbLblRequisitionNo.get(ar).setWidth("100%");
			tbLblRequisitionNo.get(ar).setImmediate(true);
			tbLblRequisitionNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblRequisitionDate.add(ar, new Label());
			tbLblRequisitionDate.get(ar).setWidth("100%");
			tbLblRequisitionDate.get(ar).setImmediate(true);
			tbLblRequisitionDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblBranchName.add(ar, new Label());
			tbLblBranchName.get(ar).setWidth("100%");
			tbLblBranchName.get(ar).setImmediate(true);
			tbLblBranchName.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblDeliveryDate.add(ar, new Label());
			tbLblDeliveryDate.get(ar).setWidth("100%");
			tbLblDeliveryDate.get(ar).setImmediate(true);
			tbLblDeliveryDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

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

			//Authorization check for
			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String requisitionId = tbLblRequisitionId.get(ar).getValue().toString();
				if (!requisitionId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ EditSelectRequisition(requisitionId, ar); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Active/Inactive"))
					{ ActiveInactiveSelectRequisition(requisitionId, ar); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Preview"))
					{ viewReport(requisitionId); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Cancel"))
					{ TransactionCancelWindow(requisitionId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Approve"))
					{ TransactionApproveWindow(requisitionId, ar+""); }
				}
				tbCmbAction.get(ar).select(null);
			});

			tblRequisitionList.addItem(new Object[]{tbLblRequisitionId.get(ar), tbLblRequisitionNo.get(ar),
					tbLblRequisitionDate.get(ar), tbLblBranchName.get(ar), tbLblDeliveryDate.get(ar),
					tbLblStatus.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private Panel addReport()
	{
		panelReport = new Panel("Requisition Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(3, 4);
		lay.setSpacing(true);

		txtReportFromDate  = new PopupDateField();
		txtReportFromDate.setImmediate(true);
		txtReportFromDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtReportFromDate.setValue(new Date());
		txtReportFromDate.setWidth("110px");
		txtReportFromDate.setDescription("From Date");
		txtReportFromDate.setDateFormat("dd-MM-yyyy");
		lay.addComponent(new Label("Dates: "), 0, 0);
		lay.addComponent(txtReportFromDate, 1, 0);

		txtReportToDate  = new PopupDateField();
		txtReportToDate.setImmediate(true);
		txtReportToDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtReportToDate.setValue(new Date());
		txtReportToDate.setWidth("110px");
		txtReportToDate.setDescription("To Date");
		txtReportToDate.setDateFormat("dd-MM-yyyy");
		lay.addComponent(txtReportToDate, 2, 0);

		cmbBranchForReport = new ComboBox();
		cmbBranchForReport.setWidth("350px");
		cmbBranchForReport.setInputPrompt("Select Branch Name");
		cmbBranchForReport.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbBranchForReport.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Branch Name: "), 0, 1);
		lay.addComponent(cmbBranchForReport, 1, 1, 2, 1);

		cmbRequisitionNo = new MultiComboBox();
		cmbRequisitionNo.setWidth("350px");
		cmbRequisitionNo.setInputPrompt("Select Requisition No");
		cmbRequisitionNo.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbRequisitionNo.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Requisition No: "), 0, 2);
		lay.addComponent(cmbRequisitionNo, 1, 2, 2, 2);

		lay.addComponent(cBtnV, 1, 3);
		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelReport.setContent(content);

		loadReportSupplier();
		return panelReport;
	}

	public void enter(ViewChangeEvent event)
	{
		loadTableInfo();
		loadReportSupplier();
	}
}
