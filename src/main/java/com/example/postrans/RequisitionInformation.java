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
public class RequisitionInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");
	private TablePaged tblRequisitionList;
	private ArrayList<Label> tbLblRequisitionId = new ArrayList<Label>();
	private ArrayList<Label> tbLblRequisitionNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblRequisitionDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblBranchName = new ArrayList<Label>();
	private ArrayList<Label> tbLblDeliveryDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblNoOfItem = new ArrayList<Label>();
	private ArrayList<Label> tbLblStatus = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();

	private SessionBean sessionBean;
	private TextField txtSearch;
	private ComboBox cmbBranchName, cmbStatus;
	private PopupDateField txtFromDate, txtToDate;
	private CommonButton cBtnS = new CommonButton("", "", "", "", "", "Search", "", "", "");

	private CommonMethod cm;
	private RequisitionInfoGateway rig = new RequisitionInfoGateway();
	private String formId;

	// Requisition report
	private PopupDateField txtFromDateReport, txtToDateReport;
	private MultiComboBox cmbBranchReport, cmbReqNoReport;
	private CommonButton cBtnV = new CommonButton("", "", "", "", "", "", "", "View", "");

	public RequisitionInformation(SessionBean sessionBean, String formId)
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
		{ checkReportSupplierLoad(); });

		txtToDateReport.addValueChangeListener(event ->
		{ checkReportSupplierLoad(); });

		txtFromDate.addValueChangeListener(event ->
		{ loadBranchList(); });

		txtToDate.addValueChangeListener(event ->
		{ loadBranchList(); });

		cmbBranchReport.addValueChangeListener(event ->
		{ loadRequisitionNo(); });

		loadBranchList();
	}

	private void loadBranchList()
	{
		cmbBranchName.removeAllItems();
		String fmDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		String branId = sessionBean.getBranchId();

		String sqlB = "select distinct rqi.vReqBranchId, bnm.vBranchName from trans.tbRequisitionInfo rqi, master.tbBranchMaster"+
				" bnm where rqi.vReqBranchId = bnm.vBranchId and rqi.dRequisitionDate between '"+fmDate+"' and '"+toDate+"' and"+
				" rqi.vBranchId = '"+branId+"' order by bnm.vBranchName";
		for (Iterator<?> iter = cm.selectSql(sqlB).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbBranchName.addItem(element[0].toString());
			cmbBranchName.setItemCaption(element[0].toString(), element[1].toString());
		}

		String sqlS = "select vStatusId, vStatusName from master.tbAllStatus where iActive = 1 and vFlag = 'st' Order by vStatusName";
		for (Iterator<?> iter = cm.selectSql(sqlS).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbStatus.addItem(element[0].toString());
			cmbStatus.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void addEditWindow(String addEdit, String transId, String ar)
	{
		AddEditRequisition win = new AddEditRequisition(sessionBean, addEdit, transId);
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

	private void TransCancelWindow(String transId, String ar)
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

	private void TransApproveWindow(String transId, String ar)
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

	public void viewReport(String reqIds)
	{
		String reportSource = "", sql = "";
		reqIds = reqIds.isEmpty()? cm.getMultiComboValue(cmbReqNoReport):reqIds;
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

			sql = "select rei.vRequisitionNo, rei.dRequisitionDate, rei.dDeliveryDate, rei.vRemarks, rei.vReferenceNo, red.vDescription,"+
					" red.mQuantity, ast.vStatusName, tbm.vBranchName vToBranch, tbm.vAddress vToAddress, fbm.vBranchName vFromBranch,"+
					" fbm.vAddress vFromAddress, rin.vItemCode, rin.vItemName, cat.vCategoryName, uni.vUnitName, tbm.vMobileNo, pre.vFullName,"+
					" isnull(app.vFullName, '') vApprovedBy, isnull(can.vFullName, '') vCancelledBy from trans.tbRequisitionInfo rei inner"+
					" join trans.tbRequisitionDetails red on rei.vRequisitionId = red.vRequisitionId inner join master.tbRawItemInfo rin"+
					" on red.vItemId = rin.vItemId inner join master.tbUnitInfo uni on rin.vUnitId = uni.iUnitId inner join master.tbItemCategory"+
					" cat on rin.vCategoryId = cat.vCategoryId inner join master.tbAllStatus ast on rei.vStatusId = ast.vStatusId inner join"+
					" master.tbBranchMaster fbm on rei.vBranchId = fbm.vBranchId inner join master.tbBranchMaster tbm on rei.vReqBranchId = "+
					" tbm.vBranchId inner join master.tbUserInfo pre on rei.vModifiedBy = pre.vUserId left join master.tbUserInfo app on"+
					" rei.vApprovedBy = app.vUserId left join master.tbUserInfo can on rei.vCancelledBy = can.vUserId where rei.vRequisitionId"+
					" in (select Item from dbo.Split('"+reqIds+"')) order by rei.vRequisitionNo, rei.dRequisitionDate, red.iAutoId";
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
		Panel pnlTable = new Panel("Requisition List :: "+sessionBean.getCompanyName()+
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
		cmbBranchName.setInputPrompt("Select branch name");
		cmbBranchName.setWidth("100%");
		cmbBranchName.setDescription("Select requested branch name");
		cmbBranchName.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbBranchName.setFilteringMode(FilteringMode.CONTAINS);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search requisition");
		txtSearch.setDescription("Search by requisition number");
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

		tblRequisitionList.addContainerProperty("No Of Item", Label.class, new Label(), null, null, Align.CENTER);

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

			tbLblNoOfItem.add(ar, new Label());
			tbLblNoOfItem.get(ar).setWidth("100%");
			tbLblNoOfItem.get(ar).setImmediate(true);
			tbLblNoOfItem.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblStatus.add(ar, new Label("", ContentMode.HTML));
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
					{ addEditWindow("Edit", requisitionId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Active/Inactive"))
					{ ActiveInactiveSelectRequisition(requisitionId, ar); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Preview"))
					{ viewReport(requisitionId); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Cancel"))
					{ TransCancelWindow(requisitionId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Approve"))
					{ TransApproveWindow(requisitionId, ar+""); }
				}
				tbCmbAction.get(ar).select(null);
			});

			tblRequisitionList.addItem(new Object[]{tbLblRequisitionId.get(ar), tbLblRequisitionNo.get(ar), tbLblRequisitionDate.get(ar),
					tbLblBranchName.get(ar), tbLblDeliveryDate.get(ar), tbLblNoOfItem.get(ar), tbLblStatus.get(ar), tbChkActive.get(ar),
					tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private Panel addReport()
	{
		Panel panelReport = new Panel("Requisition Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(4, 4);
		lay.setSpacing(true);

		txtFromDateReport  = new PopupDateField();
		txtFromDateReport.setImmediate(true);
		txtFromDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateReport.setValue(new Date());
		txtFromDateReport.setWidth("110px");
		txtFromDateReport.setDescription("From date");
		txtFromDateReport.setDateFormat("dd-MM-yyyy");
		lay.addComponent(new Label("Date(s): "), 0, 0);
		lay.addComponent(txtFromDateReport, 1, 0);

		txtToDateReport  = new PopupDateField();
		txtToDateReport.setImmediate(true);
		txtToDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateReport.setValue(new Date());
		txtToDateReport.setWidth("110px");
		txtToDateReport.setDescription("To date");
		txtToDateReport.setDateFormat("dd-MM-yyyy");
		lay.addComponent(txtToDateReport, 2, 0);

		cmbBranchReport = new MultiComboBox();
		cmbBranchReport.setWidth("450px");
		cmbBranchReport.setInputPrompt("Select branch name");
		cmbBranchReport.setDescription("Select branch name");
		cmbBranchReport.setRequired(true);
		cmbBranchReport.setRequiredError("This field is required");
		lay.addComponent(new Label("Branch Name: "), 0, 1);
		lay.addComponent(cmbBranchReport, 1, 1, 3, 1);

		cmbReqNoReport = new MultiComboBox();
		cmbReqNoReport.setWidth("450px");
		cmbReqNoReport.setInputPrompt("Select requisition no");
		cmbReqNoReport.setDescription("Select requisition no");
		cmbReqNoReport.setRequired(true);
		cmbReqNoReport.setRequiredError("This field is required");
		lay.addComponent(new Label("Requisition No: "), 0, 2);
		lay.addComponent(cmbReqNoReport, 1, 2, 3, 2);

		lay.addComponent(cBtnV, 1, 3);
		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelReport.setContent(content);

		loadReportSupplier();
		return panelReport;
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
		cmbBranchReport.removeAllItems(); 
		String fmDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue());
		String branId = sessionBean.getBranchId();

		String sql = "select distinct rei.vReqBranchId, brm.vBranchName from trans.tbRequisitionInfo rei inner join master.tbBranchMaster"+
				" brm on brm.vBranchId = rei.vReqBranchId where rei.dRequisitionDate between '"+fmDate+"' and '"+toDate+"' and rei.vBranchId"+
				" = '"+branId+"' order by brm.vBranchName";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbBranchReport.addItem(element[0].toString());
			cmbBranchReport.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadRequisitionNo()
	{
		cmbReqNoReport.removeAllItems();
		String fmDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue());
		String branId = cm.getMultiComboValue(cmbBranchReport);
		String branIs = sessionBean.getBranchId();

		String sql = "select vRequisitionId, vRequisitionNo, CONVERT(varchar(20), dRequisitionDate, 105) vReqDate, ast.vStatusName from"+
				" trans.tbRequisitionInfo rei, master.tbAllStatus ast where rei.vStatusId = ast.vStatusId and vReqBranchId in (select Item"+
				" from dbo.Split('"+branId+"')) and dRequisitionDate between '"+fmDate+"' and '"+toDate+"' and vBranchId = '"+branIs+"'"+
				" order by vRequisitionNo";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			cmbReqNoReport.addItem(element[0].toString());
			cmbReqNoReport.setItemCaption(element[0].toString(),
					element[1].toString()+" ("+element[2].toString()+")"+" ("+element[3].toString()+")");
		}
	}

	private void addValidation()
	{
		if (txtFromDateReport.getValue() != null)
		{
			if (txtToDateReport.getValue() != null)
			{
				if (!cm.getMultiComboValue(cmbBranchReport).isEmpty())
				{
					if (!cm.getMultiComboValue(cmbReqNoReport).isEmpty())
					{  viewReport(""); }
					else
					{
						cmbReqNoReport.focus();
						cm.showNotification("warning", "Warning!", "Select requisition number.");
					}
				}
				else
				{
					cmbBranchReport.focus();
					cm.showNotification("warning", "Warning!", "Select branch name.");
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

	private void loadTableInfo()
	{
		String search = "%"+txtSearch.getValue().toString().trim()+"%", statId = "", statNm = "";
		String branch = cmbBranchName.getValue() == null? "%":cmbBranchName.getValue().toString();
		String status = cmbStatus.getValue() != null? cmbStatus.getValue().toString():"%";
		String fmDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		String branId = sessionBean.getBranchId();

		tableClear();
		int i = 0;
		try
		{
			String sql = "select ren.vRequisitionId, ren.vRequisitionNo, ren.dRequisitionDate, brm.vBranchName, ren.dDeliveryDate, ren.vRemarks,"+
					" ren.iActive, ren.vStatusId, ast.vStatusName, COUNT(red.vItemId) iNumber from trans.tbRequisitionInfo ren,"+
					" trans.tbRequisitionDetails red, master.tbAllStatus ast, master.tbBranchMaster brm where ren.vRequisitionId = red.vRequisitionId"+
					" and ren.vStatusId = ast.vStatusId and brm.vBranchId = ren.vReqBranchId and vRequisitionNo like '"+search+"' and vReqBranchId"+
					" like '"+branch+"' and ren.dRequisitionDate between '"+fmDate+"' and '"+toDate+"' and ren.vStatusId like '"+status+"' and"+
					" ren.vBranchId = '"+branId+"' group by ren.vRequisitionId, ren.vRequisitionNo, ren.dRequisitionDate, brm.vBranchName,"+
					" ren.dDeliveryDate, ren.vRemarks, ren.iActive, ren.vStatusId, ast.vStatusName order by ren.dRequisitionDate desc";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblRequisitionId.size() <= i)
				{ tableRowAdd(i); }

				tbLblRequisitionId.get(i).setValue(element[0].toString());
				tbLblRequisitionNo.get(i).setValue(element[1].toString());
				tbLblRequisitionDate.get(i).setValue(cm.dfBd.format(element[2]));
				tbLblBranchName.get(i).setValue(element[3].toString());
				tbLblDeliveryDate.get(i).setValue(cm.dfBd.format(element[4]));
				tbLblNoOfItem.get(i).setValue(cm.deciInt.format(element[9]));
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

				if (statId.equals("S6"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Approve");
				}
				if (statId.equals("S7"))
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

	public void enter(ViewChangeEvent event)
	{
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
		loadTableInfo();

		loadReportSupplier();
	}
}
