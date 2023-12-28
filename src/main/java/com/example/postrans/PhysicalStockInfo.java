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
import com.common.share.ReportViewer;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.PhysicalStockGateway;
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class PhysicalStockInfo extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "Save", "", "", "Refresh", "", "", "", "");
	private TablePaged tblPhysicalStockList;
	private ArrayList<Label> tbLblPhysicalStockId = new ArrayList<Label>();
	private ArrayList<Label> tbLblPhysicalStockNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblPhysicalStockDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblTotalItem = new ArrayList<Label>();
	private ArrayList<Label> tbLblAmount = new ArrayList<Label>();
	private ArrayList<Label> tbLblRemarks = new ArrayList<Label>();
	private ArrayList<Label> tbLblActive = new ArrayList<Label>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();

	private SessionBean sessionBean;
	private TextField txtSearch;
	private ComboBox cmbStatus;
	private PopupDateField txtFromDate, txtToDate;
	private CommonButton cBtnS = new CommonButton("", "", "", "", "", "Search", "", "", "");

	private CommonMethod cm;
	private PhysicalStockGateway pog = new PhysicalStockGateway();
	private String formId;

	//PhysicalStock report
	private PopupDateField txtReportFromDate, txtReportToDate;
	private MultiComboBox cmbItemNameForReport;
	private CommonButton cBtnV = new CommonButton("", "", "", "", "", "", "", "View", "");

	public PhysicalStockInfo(SessionBean sessionBean, String formId)
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

		cBtn.btnSave.addClickListener(event ->
		{
			processWindow();
			event.getButton().setEnabled(false);
		});

		cBtn.btnRefresh.addClickListener(event ->
		{ loadTableInfo(); });

		cBtnS.btnSearch.addClickListener(event ->
		{ loadTableInfo(); });

		cBtnV.btnPreview.addClickListener(event ->
		{ addValidation(); });

		txtReportFromDate.addValueChangeListener(event ->
		{ loadItemReport(); });

		txtReportToDate.addValueChangeListener(event ->
		{ loadItemReport(); });
	}

	private void addEditWindow(String addEdit, String itemId, String ar)
	{
		AddEditPhysicalStock win = new AddEditPhysicalStock(sessionBean, addEdit, itemId);
		getUI().addWindow(win);
		win.center();
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(event ->
		{
			cBtn.btnNew.setEnabled(true);
			loadTableInfo();
		});
	}

	private void EditSelectPhysicalStock(String physicalStockId, int ar)
	{
		if (!pog.getPhysicalStockUse(physicalStockId))
		{ addEditWindow("Edit", physicalStockId, ar+""); }
		else
		{
			cm.showNotification("failure", "Error!", "Stock Already Processed");
			tbCmbAction.get(ar).setEnabled(true);
		}
	}

	private void processWindow()
	{
		StockProcess win = new StockProcess(sessionBean);
		getUI().addWindow(win);
		win.center();
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(event ->
		{ 
			cBtn.btnSave.setEnabled(true);
			loadTableInfo(); 
		});
	}

	private void loadItemReport()
	{
		cmbItemNameForReport.removeAllItems();
		String fmDate = cm.dfDb.format(txtReportFromDate.getValue());
		String toDate = cm.dfDb.format(txtReportToDate.getValue());
		String branId = sessionBean.getBranchId();

		String sqlC= "Select distinct r.vItemId, r.vItemName, dbo.funGetNumeric(r.vItemCode) iCode from (select vItemId from trans.tbLostAddStockInfo"+
				" where convert(date,dLastStockDate,105) between '"+fmDate+"' and '"+toDate+"' and vBranchId = '"+branId+"' union select vItemId"+
				" from trans.tbLostAddStockInfo where convert(date,dLastStockDate,105) between '"+fmDate+"' and '"+toDate+"' and vBranchId ="+
				" '"+sessionBean.getBranchId()+"') abc inner join master.tbRawItemInfo r on abc.vItemId = r.vItemId order by iCode asc";
		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbItemNameForReport.addItem(element[0].toString());
			cmbItemNameForReport.setItemCaption(element[0].toString(), element[2].toString()+" - "+element[1].toString());
		}
	}

	private void loadTableInfo()
	{
		String search = "%"+txtSearch.getValue().toString()+"%";
		String status = cmbStatus.getValue() != null? cmbStatus.getValue().toString():"%";
		String fmDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		String branId = sessionBean.getBranchId();

		tableClear();
		int i = 0;
		try
		{
			String sql = "select pin.vPhysicalStockId, pin.vPhysicalStockNo, pin.dPhysicalStockDate, pin.vRemarks, (select isnull(sum(mTotalAmount), 0)"+
					" from trans.tbPhysicalStockDetails where vPhysicalStockId = pin.vPhysicalStockId)Amount, (select isnull(count(vItemId),0) from"+
					" trans.tbPhysicalStockDetails where vPhysicalStockId = pin.vPhysicalStockId)item, pin.iActive, pin.vStatusId, ast.vStatusName"+
					" from trans.tbPhysicalStockInfo pin, master.tbAllStatus ast where vPhysicalStockNo like '"+search+"' and pin.vStatusId = ast.vStatusId"+
					" and pin.dPhysicalStockDate between '"+fmDate+"' and '"+toDate+"' and pin.vStatusId like '"+status+"' and pin.vBranchId = '"+branId+"'"+
					" order by pin.dPhysicalStockDate desc";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblPhysicalStockId.size() <= i)
				{ tableRowAdd(i); }

				tbLblPhysicalStockId.get(i).setValue(element[0].toString());
				tbLblPhysicalStockNo.get(i).setValue(element[1].toString());
				tbLblPhysicalStockDate.get(i).setValue(cm.dfBd.format(element[2]));
				tbLblRemarks.get(i).setValue(element[3].toString());
				tbLblTotalItem.get(i).setValue(element[5].toString());
				tbLblAmount.get(i).setValue(cm.setComma(Double.parseDouble(element[4].toString())));
				tbLblActive.get(i).setValue((element[6].toString().equals("1")? "Prepared":"Processed"));

				if (element[6].toString().equals("0"))
				{ tbCmbAction.get(i).removeItem("Edit"); }
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
			tblPhysicalStockList.nextPage();
			tblPhysicalStockList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			totalAmount();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblPhysicalStockList, tbLblPhysicalStockId); }

	public double totalAmount()
	{
		double amt = 0;
		for (int i=0; i<tbLblPhysicalStockId.size(); i++)
		{ amt += cm.getAmtValue(tbLblAmount.get(i)); }
		tblPhysicalStockList.setColumnFooter("Amount", cm.setComma(amt));
		tblPhysicalStockList.setColumnAlignment("Amount", Align.RIGHT);		
		return amt;
	}

	public void viewReportSingle(String phyStockId)
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

			sql = "select a.vPhysicalStockId, a.vPhysicalStockNo, a.dPhysicalStockDate, a.vReferenceNo, a.vRemarks, a.iActive,"+
					" b.vItemId, c.vItemName, b.vUnitName, b.vCategoryName, b.vRemarks vDescription, b.mPhysicalStockQty,"+
					" b.mSoftwareStockQty, b.mPurchaseRate, b.mTotalAmount from trans.tbPhysicalStockInfo a inner join"+
					" trans.tbPhysicalStockDetails b on a.vPhysicalStockId = b.vPhysicalStockId inner join master.tbRawItemInfo"+
					" c on c.vItemId = b.vItemId where a.vPhysicalStockId like '"+phyStockId+"' order by b.iAutoId";

			reportSource = "com/jasper/postransaction/rptPhysicalStock.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private void addValidation()
	{
		if (txtReportFromDate.getValue() != null)
		{
			if (txtReportToDate.getValue() != null)
			{ viewReport(); }
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

	public void viewReport()
	{
		String reportSource = "", sql = "";
		String fromDate = cm.dfDb.format(txtReportFromDate.getValue());
		String toDate = cm.dfDb.format(txtReportToDate.getValue());
		String itemId = cmbItemNameForReport.getValue().toString().replace("]", "").replace("[", "").trim();
		String itemIds = itemId.isEmpty()? "%":itemId;
		String datePara = "From: "+cm.dfBd.format(txtReportFromDate.getValue())+" To "+cm.dfBd.format(txtReportToDate.getValue());
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
			hm.put("fromToDate", datePara);

			sql =   "Select vStockNo, dLastStockDate, abc.vItemId, r.vItemName, vUnitName, vCategoryName, mSoftwareStockQty,"+
					" mPhysicalStockQty, mStockQty, mPurchaseRate, mTotalAmount, vStatus, vUserName from (select vRefernceNo,"+
					" vStockNo, dLastStockDate, vItemId, vUnitName, vCategoryName, mSoftwareStockQty, mPhysicalStockQty,"+
					" mAddStockQty mStockQty, mPurchaseRate, mTotalAmount, vFlag vStatus, vUserName from trans.tbLostAddStockInfo"+
					" where convert(date,dLastStockDate,105) between '"+fromDate+"' and '"+toDate+"' and vBranchId ="+
					" '"+sessionBean.getBranchId()+"') abc inner join"+
					" master.tbRawItemInfo r on abc.vItemId = r.vItemId where r.vItemId in (select Item from dbo.Split('"+itemIds+"'))"+
					" order by vItemName, dLastStockDate";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptPhysicalStockProcessedDateBetween.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private void TransactionCancelWindow(String phyStockId, String ar)
	{
		TransactionCancel win = new TransactionCancel(sessionBean, phyStockId, "Physical Stock");
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

	private void TransactionApproveWindow(String phyStockId, String ar)
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
					if (tacm.TransactionApprove(phyStockId, sessionBean.getUserId(), "Physical Stock"))
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

	private Panel addPanel()
	{
		Panel pnlTable = new Panel("Physical Stock List :: "+sessionBean.getCompanyName()+
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
		txtSearch.setInputPrompt("Search Physical Stock number");
		txtSearch.setDescription("Search by Physical Stock number");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		cmbStatus = new ComboBox();
		cmbStatus.setImmediate(true);
		cmbStatus.setFilteringMode(FilteringMode.CONTAINS);
		cmbStatus.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbStatus.setInputPrompt("Select status");
		cmbStatus.setWidth("115px");

		cBtn.btnSave.setCaption("Process");
		cBtn.btnSave.setIcon(FontAwesome.COG);
		cBtnS.btnSearch.setStyleName(ValoTheme.BUTTON_TINY);
		hori.addComponents(txtFromDate, txtToDate, txtSearch, cmbStatus, cBtnS);

		buildTable();
		content.addComponents(hori, tblPhysicalStockList, tblPhysicalStockList.createControls());
		pnlTable.setContent(content);
		return pnlTable;
	}

	private void buildTable()
	{
		tblPhysicalStockList = new TablePaged();
		tblPhysicalStockList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblPhysicalStockId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblPhysicalStockList.addContainerProperty("Physical Stock Id", Label.class, new Label(), null, null, Align.CENTER);
		tblPhysicalStockList.setColumnCollapsed("Physical Stock Id", true);

		tblPhysicalStockList.addContainerProperty("Physical Stock No", Label.class, new Label(), null, null, Align.CENTER);

		tblPhysicalStockList.addContainerProperty("Physical Stock Date", Label.class, new Label(), null, null, Align.CENTER);

		tblPhysicalStockList.addContainerProperty("Total Item", Label.class, new Label(), null, null, Align.RIGHT);

		tblPhysicalStockList.addContainerProperty("Amount", Label.class, new Label(), null, null, Align.RIGHT); 

		tblPhysicalStockList.addContainerProperty("Remarks", Label.class, new Label(), null, null, Align.CENTER);

		tblPhysicalStockList.addContainerProperty("Status", Label.class, new Label(), null, null, Align.CENTER);

		tblPhysicalStockList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblPhysicalStockList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblPhysicalStockId.add(ar, new Label());
			tbLblPhysicalStockId.get(ar).setWidth("100%");
			tbLblPhysicalStockId.get(ar).setImmediate(true);
			tbLblPhysicalStockId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblPhysicalStockNo.add(ar, new Label());
			tbLblPhysicalStockNo.get(ar).setWidth("100%");
			tbLblPhysicalStockNo.get(ar).setImmediate(true);
			tbLblPhysicalStockNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblPhysicalStockDate.add(ar, new Label());
			tbLblPhysicalStockDate.get(ar).setWidth("100%");
			tbLblPhysicalStockDate.get(ar).setImmediate(true);
			tbLblPhysicalStockDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

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

			tbLblActive.add(ar, new Label());
			tbLblActive.get(ar).setWidth("100%");
			tbLblActive.get(ar).setImmediate(true);

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
				String phyStockId = tbLblPhysicalStockId.get(ar).getValue().toString();
				if (!phyStockId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ EditSelectPhysicalStock(phyStockId, ar); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Preview"))
					{ viewReportSingle(phyStockId); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Cancel"))
					{ TransactionCancelWindow(phyStockId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Approve"))
					{ TransactionApproveWindow(phyStockId, ar+""); }
				}
				tbCmbAction.get(ar).select(null);
			});

			tblPhysicalStockList.addItem(new Object[]{tbLblPhysicalStockId.get(ar), tbLblPhysicalStockNo.get(ar),
					tbLblPhysicalStockDate.get(ar), tbLblTotalItem.get(ar), tbLblAmount.get(ar), tbLblRemarks.get(ar),
					tbLblActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private Panel addReport()
	{
		Panel panelReport = new Panel("Processed Stock Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 5);
		lay.setSpacing(true);

		txtReportFromDate  = new PopupDateField();
		txtReportFromDate.setImmediate(true);
		txtReportFromDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtReportFromDate.setValue(new Date());
		txtReportFromDate.setWidth("110px");
		txtReportFromDate.setDateFormat("dd-MM-yyyy");
		lay.addComponent(new Label("From Date: "), 0, 0);
		lay.addComponent(txtReportFromDate, 1, 0);

		txtReportToDate  = new PopupDateField();
		txtReportToDate.setImmediate(true);
		txtReportToDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtReportToDate.setValue(new Date());
		txtReportToDate.setWidth("110px");
		txtReportToDate.setDateFormat("dd-MM-yyyy");
		lay.addComponent(new Label("To Date: "), 0, 1);
		lay.addComponent(txtReportToDate, 1, 1);

		cmbItemNameForReport = new MultiComboBox();
		cmbItemNameForReport.setWidth("350px");
		cmbItemNameForReport.setInputPrompt("Select Item Name");
		cmbItemNameForReport.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbItemNameForReport.setFilteringMode(FilteringMode.CONTAINS);
		lay.addComponent(new Label("Item Name : "), 0, 2);
		lay.addComponent(cmbItemNameForReport, 1, 2);

		lay.addComponent(cBtnV, 1, 3);
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

		loadItemReport();
	}
}
