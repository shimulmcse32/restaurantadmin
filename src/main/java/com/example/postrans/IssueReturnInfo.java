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
import com.example.gateway.IssueReturnGateway;
import com.example.gateway.TransAppCanGateway;
import com.common.share.ReportViewer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class IssueReturnInfo extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblIssueReturnList;
	private ArrayList<Label> tbLblReturnId = new ArrayList<Label>();
	private ArrayList<Label> tbLblReturnNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblReturnDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblIssueNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblIssueDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblItemNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblAmount = new ArrayList<Label>();
	private ArrayList<Label> tbLblReturnTo = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();

	private SessionBean sessionBean;
	private TextField txtSearch;
	private PopupDateField txtFromDate, txtToDate;
	private CommonButton cBtnS = new CommonButton("", "", "", "", "", "Search", "", "", "");

	private CommonMethod cm;
	private IssueReturnGateway prg = new IssueReturnGateway();
	private String formId;

	public IssueReturnInfo(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		this.formId = formId;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		addComponents(cBtn, addPanel());

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
	}

	private void addEditWindow(String addEdit, String itemId, String ar)
	{
		AddEditIssueReturn win = new AddEditIssueReturn(sessionBean, addEdit, itemId);
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
		String search = "%"+txtSearch.getValue().toString()+"%";
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());

		tableClear();
		int i = 0;
		try
		{
			String sql = "select a.vIssueReturnId, a.vIssueReturnNo, a.vIssueId, b.vIssueNo, a.dReturnDate, b.dIssueDate,"+
					" (select vBranchName from master.tbBranchMaster bm where bm.vBranchId = a.vReturnTo) vReturnTo,"+
					" a.mTotalAmount, (select count(vItemId) from trans.tbIssueReturnDetails where vIssueReturnId"+
					" = a.vIssueReturnId)Item, a.iActive, a.vStatusId from trans.tbissueReturnInfo a inner join trans.tbIssueInfo"+
					" b on a.vIssueId = b.vIssueId where (a.vIssueReturnNo like '"+search+"' or b.vIssueNo like '"+search+"'"+
					" or a.vReturnTo like '"+search+"') and a.dReturnDate between '"+fromDate+"' and '"+toDate+"' and"+
					" a.vBranchId = '"+sessionBean.getBranchId()+"' order by a.dReturnDate, a.iAutoId desc";
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblReturnId.size() <= i)
				{ tableRowAdd(i); }
				tbLblReturnId.get(i).setValue(element[0].toString());
				tbLblReturnNo.get(i).setValue(element[1].toString());
				tbLblIssueNo.get(i).setValue(element[3].toString());
				tbLblReturnDate.get(i).setValue(cm.dfBd.format(element[4]));
				tbLblIssueDate.get(i).setValue(cm.dfBd.format(element[5]));
				tbLblReturnTo.get(i).setValue(element[6].toString());
				tbLblAmount.get(i).setValue(cm.setComma(Double.parseDouble(element[7].toString())));
				tbLblItemNo.get(i).setValue(element[8].toString());
				tbChkActive.get(i).setValue((element[9].toString().equals("1")? true:false));
				tbChkActive.get(i).setEnabled(false);

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
			tblIssueReturnList.nextPage();
			tblIssueReturnList.previousPage();
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
		tblIssueReturnList.setColumnFooter("Amount", cm.setComma(amt));
		tblIssueReturnList.setColumnAlignment("Amount", Align.RIGHT);		
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

			sql = "select bmf.vBranchName vFrom, bmt.vBranchName vTo, iri.vIssueReturnNo,"+
					" iri.dReturnDate, isu.vIssueNo, isu.dIssueDate, rii.vItemCode, rii.vItemName, uni.vUnitName,"+
					" cat.vCategoryName, (select isd.mIssueQty from trans.tbIssueDetails isd where isd.vIssueId = ird.vIssueId and"+
					" isd.vItemId = ird.vItemId) mIssueQty, ird.mReturnedQty, ird.mIssueRate, ird.mAmount, ui.vFullName, ISNULL((select"+
					" ui.vFullName from master.tbUserInfo ui where ui.vUserId = iri.vApprovedBy), '') vApprovedBy, ISNULL((select"+
					" ui.vFullName from master.tbUserInfo ui where ui.vUserId = iri.vCancelledBy), '') vCancelledBy from"+
					" trans.tbIssueReturnInfo iri, trans.tbIssueReturnDetails ird, trans.tbIssueInfo isu, master.tbBranchMaster bmf,"+
					" master.tbBranchMaster bmt, master.tbRawItemInfo rii, master.tbItemCategory cat, master.tbUnitInfo uni,"+
					" master.tbUserInfo ui where iri.vIssueReturnId = ird.vIssueReturnId and iri.vIssueId = isu.vIssueId and"+
					" iri.vReturnFrom = bmf.vBranchId and iri.vReturnTo = bmt.vBranchId and ird.vItemId = rii.vItemId and"+
					" rii.vCategoryId = cat.vCategoryId and ird.vUnitId = convert(varchar(10), uni.iUnitId) and iri.vModifiedBy"+
					" = ui.vUserId and iri.vIssueReturnId = '"+returnId+"' order by iri.iAutoId, ird.iAutoId";
			reportSource = "com/jasper/postransaction/rptIssueReturnInfo.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private void tableClear()
	{ cm.tableClear(tblIssueReturnList, tbLblReturnId); }

	private void TransactionCancelWindow(String returnId, String ar)
	{
		TransactionCancel win = new TransactionCancel(sessionBean, returnId, "Issue Return");
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
					if (tacm.TransactionApprove(orderId, sessionBean.getUserId(), "Issue Return"))
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
		Panel pnlTable = new Panel("Issue Return List :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");

		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout hori = new HorizontalLayout();
		hori.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search by Return/Issue No, Return To");
		txtSearch.setDescription("Search by");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

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
		hori.addComponents(txtFromDate, txtToDate, txtSearch, cBtnS);

		buildTable();
		content.addComponents(hori, tblIssueReturnList, tblIssueReturnList.createControls());
		pnlTable.setContent(content);
		return pnlTable;
	}

	private void buildTable()
	{
		tblIssueReturnList = new TablePaged();
		tblIssueReturnList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblReturnId.get(ar).getValue().toString();
				if (!prg.getIssueUse(id))
				{ addEditWindow("Edit", id, ar+""); }
				else
				{
					cm.showNotification("failure", "Error!", "Issue is in use.");
					tbCmbAction.get(ar).setEnabled(true);
				}
			}
		});

		tblIssueReturnList.addContainerProperty("Return Id", Label.class, new Label(), null, null, Align.CENTER);
		tblIssueReturnList.setColumnCollapsed("Return Id", true);

		tblIssueReturnList.addContainerProperty("Return No", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueReturnList.addContainerProperty("Return Date", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueReturnList.addContainerProperty("Issue No", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueReturnList.addContainerProperty("Issue Date", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueReturnList.addContainerProperty("Return To", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueReturnList.addContainerProperty("Item No", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueReturnList.addContainerProperty("Amount", Label.class, new Label(), null, null, Align.RIGHT);

		tblIssueReturnList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblIssueReturnList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblIssueReturnList.setColumnWidth("Action", 100);
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

			tbLblIssueNo.add(ar, new Label());
			tbLblIssueNo.get(ar).setWidth("100%");
			tbLblIssueNo.get(ar).setImmediate(true);
			tbLblIssueNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblIssueDate.add(ar, new Label());
			tbLblIssueDate.get(ar).setWidth("100%");
			tbLblIssueDate.get(ar).setImmediate(true);
			tbLblIssueDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblReturnTo.add(ar, new Label());
			tbLblReturnTo.get(ar).setWidth("100%");
			tbLblReturnTo.get(ar).setImmediate(true);
			tbLblReturnTo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblItemNo.add(ar, new Label());
			tbLblItemNo.get(ar).setWidth("100%");
			tbLblItemNo.get(ar).setImmediate(true);
			tbLblItemNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblAmount.add(ar, new Label());
			tbLblAmount.get(ar).setWidth("100%");
			tbLblAmount.get(ar).setImmediate(true);
			tbLblAmount.get(ar).addStyleName(ValoTheme.LABEL_TINY);
			tbLblAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

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
					{ 
						if (!prg.getIssueUse(returnId))
						{ addEditWindow("Edit", returnId, ar+""); }
						else
						{
							cm.showNotification("failure", "Error!", "Issue is in use.");
							tbCmbAction.get(ar).setEnabled(true);
						}
					}	

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

			tblIssueReturnList.addItem(new Object[]{tbLblReturnId.get(ar), tbLblReturnNo.get(ar), tbLblReturnDate.get(ar),
					tbLblIssueNo.get(ar), tbLblIssueDate.get(ar), tbLblReturnTo.get(ar), tbLblItemNo.get(ar),
					tbLblAmount.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	public void enter(ViewChangeEvent event)
	{
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
		loadTableInfo();
	}
}
