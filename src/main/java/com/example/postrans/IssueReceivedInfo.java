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
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.IssueInfoGateway;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class IssueReceivedInfo extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("", "Save", "", "", "Refresh", "", "", "", "");
	private CommonButton cBtnS = new CommonButton("", "", "", "", "", "Search", "", "", "");

	private boolean tick = false;
	private TablePaged tblPendingIssueList;
	private ArrayList<Label> tbLblIssueId = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkSelect = new ArrayList<CheckBox>();
	private ArrayList<Label> tbLblIssueNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblIssueDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblAmount = new ArrayList<Label>();
	private ArrayList<Label> tbLblReceivedBy = new ArrayList<Label>();
	private ArrayList<Label> tbLblIssueBy = new ArrayList<Label>();
	private ArrayList<Label> tbLblIssueTo = new ArrayList<Label>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;
	private Panel pnlTable;

	private IssueInfoGateway iig = new IssueInfoGateway();

	private PopupDateField txtFromDate, txtToDate;
	private ComboBox cmbBranchName;
	private TextField txtSearch;

	private CommonMethod cm;

	public IssueReceivedInfo(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);

		buildTable();
		cBtn.btnSave.setCaption("Received");
		addComponents(addPanel(), cBtn);
		setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);
		cBtn.btnSave.setEnabled(cm.insert);

		loadComboData();
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
			{
				if (checkTable())
				{ receivedIssue(); }
				else
				{ cm.showNotification("warning", "Warning!", "Select Issue from the table."); }
			});

		cBtn.btnRefresh.addClickListener(event ->
			{ loadTableInfo(); });

		cBtnS.btnSearch.addClickListener(event ->
			{ loadTableInfo(); });

		txtFromDate.addValueChangeListener(event ->
			{ loadComboData(); });

		txtToDate.addValueChangeListener(event ->
			{ loadComboData(); });

		tblPendingIssueList.addHeaderClickListener(event -> 
			{
				if (event.getPropertyId().toString().equalsIgnoreCase("Select"))
				{ selectAll(); }
			});
	}

	private boolean checkTable()
	{
		boolean ret = false;
		for (int i=0; i<tbLblIssueId.size(); i++)
		{
			if (tbChkSelect.get(i).getValue().booleanValue())
			{ ret = true; break; }
		}
		return ret;
	}

	private void loadComboData()
	{
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		cmbBranchName.removeAllItems();
		String sql = "select distinct b.vBranchId, b.vBranchName from trans.tbIssueInfo a inner join master.tbBranchMaster b"+
				" on a.vBranchTo = b.vBranchId where a.vBranchFrom like '"+sessionBean.getBranchId()+"' and a.dIssueDate between"+
				" '"+fromDate+"' and '"+toDate+"' and a.iStatus = 0 and a.iActive = 1 order by b.vBranchName";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbBranchName.addItem(element[0].toString());
			cmbBranchName.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private Panel addPanel()
	{
		pnlTable = new Panel("Pending Issue List :: "+sessionBean.getCompanyName()+
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

		cmbBranchName = new ComboBox();
		cmbBranchName.setImmediate(true);
		cmbBranchName.setFilteringMode(FilteringMode.CONTAINS);
		cmbBranchName.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbBranchName.setInputPrompt("Select Branch Name");
		cmbBranchName.setWidth("280px");

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Issue");
		txtSearch.setDescription("Search by Issue number or Issue by");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		cBtnS.btnSearch.setStyleName(ValoTheme.BUTTON_TINY);
		hori.addComponents(txtFromDate, txtToDate, cmbBranchName, txtSearch, cBtnS);

		content.addComponents(hori, tblPendingIssueList, tblPendingIssueList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblPendingIssueList = new TablePaged();

		tblPendingIssueList.addContainerProperty("Issue Id", Label.class, new Label(), null, null, Align.CENTER);
		tblPendingIssueList.setColumnCollapsed("Issue Id", true);

		tblPendingIssueList.addContainerProperty("Select", CheckBox.class, new CheckBox(), null, null, Align.CENTER);
		tblPendingIssueList.setColumnWidth("Select", 50);

		tblPendingIssueList.addContainerProperty("Issue No", Label.class, new Label(), null, null, Align.CENTER);

		tblPendingIssueList.addContainerProperty("Issue Date", Label.class, new Label(), null, null, Align.CENTER);
		tblPendingIssueList.setColumnWidth("Issue Date", 100);

		tblPendingIssueList.addContainerProperty("Amount", Label.class, new Label(), null, null, Align.CENTER);
		tblPendingIssueList.setColumnWidth("Amount", 120);

		tblPendingIssueList.addContainerProperty("Received By", Label.class, new Label(), null, null, Align.CENTER);

		tblPendingIssueList.addContainerProperty("Issue By", Label.class, new Label(), null, null, Align.CENTER);

		tblPendingIssueList.addContainerProperty("Issue To", Label.class, new Label(), null, null, Align.CENTER);

		tblPendingIssueList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblPendingIssueList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblIssueId.add(ar, new Label());
			tbLblIssueId.get(ar).setWidth("100%");
			tbLblIssueId.get(ar).setImmediate(true);
			tbLblIssueId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbChkSelect.add(ar, new CheckBox());
			tbChkSelect.get(ar).setWidth("100%");
			tbChkSelect.get(ar).setImmediate(true);
			tbChkSelect.get(ar).setStyleName(ValoTheme.CHECKBOX_SMALL);
			tbChkSelect.get(ar).setDescription("Select to received");
			tbChkSelect.get(ar).addValueChangeListener(event ->
				{ setTotalAmount(); });

			tbLblIssueNo.add(ar, new Label());
			tbLblIssueNo.get(ar).setWidth("100%");
			tbLblIssueNo.get(ar).setImmediate(true);
			tbLblIssueNo.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblIssueDate.add(ar, new Label());
			tbLblIssueDate.get(ar).setWidth("100%");
			tbLblIssueDate.get(ar).setImmediate(true);
			tbLblIssueDate.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblAmount.add(ar, new Label());
			tbLblAmount.get(ar).setWidth("100%");
			tbLblAmount.get(ar).setImmediate(true);
			tbLblAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbLblAmount.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblReceivedBy.add(ar, new Label());
			tbLblReceivedBy.get(ar).setWidth("100%");
			tbLblReceivedBy.get(ar).setImmediate(true);
			tbLblReceivedBy.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblIssueBy.add(ar, new Label());
			tbLblIssueBy.get(ar).setWidth("100%");
			tbLblIssueBy.get(ar).setImmediate(true);
			tbLblIssueBy.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblIssueTo.add(ar, new Label());
			tbLblIssueTo.get(ar).setWidth("100%");
			tbLblIssueTo.get(ar).setImmediate(true);
			tbLblIssueTo.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbCmbAction.add(ar, new ComboBox());
			tbCmbAction.get(ar).setWidth("100%");
			tbCmbAction.get(ar).setImmediate(true);
			tbCmbAction.get(ar).addStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbAction.get(ar).addItem("Preview(PDF)");
			tbCmbAction.get(ar).setItemIcon("Preview(PDF)", FontAwesome.FILE_PDF_O);
			tbCmbAction.get(ar).setInputPrompt("Action");
			tbCmbAction.get(ar).setTextInputAllowed(false);
			tbCmbAction.get(ar).setNullSelectionAllowed(false);
			tbCmbAction.get(ar).addValueChangeListener(event ->
				{
					String vissueId = tbLblIssueId.get(ar).getValue().toString();
					if (!vissueId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
					{
						String action = tbCmbAction.get(ar).getValue().toString();
						if (action.equals("Preview(PDF)"))
						{ viewReportSingle(vissueId); }
					}
					tbCmbAction.get(ar).select(null);
			});
			tblPendingIssueList.addItem(new Object[]{tbLblIssueId.get(ar), tbChkSelect.get(ar), tbLblIssueNo.get(ar),
					tbLblIssueDate.get(ar), tbLblAmount.get(ar), tbLblReceivedBy.get(ar), tbLblIssueBy.get(ar),
					tbLblIssueTo.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void setTotalAmount()
	{
		double total = 0;
		for (int i = 0; i < tbLblIssueId.size(); i++)
		{
			if (tbChkSelect.get(i).getValue().booleanValue())
			{ total += cm.getAmtValue(tbLblAmount.get(i)); }
		}
		tblPendingIssueList.setColumnFooter("Amount", cm.setComma(total));
		tblPendingIssueList.setColumnAlignment("Amount", Align.RIGHT);
		tblPendingIssueList.setColumnFooter("Issue Date", "Total Amount:");
		tblPendingIssueList.setColumnAlignment("Issue Date", Align.RIGHT);
	}

	private void selectAll()
	{
		if (tick)
		{tick = false;}
		else
		{tick = true;}
		for (int i = 0; i < tbLblIssueId.size(); i++)
		{
			if (!tbLblIssueId.get(i).getValue().toString().isEmpty())
			{ tbChkSelect.get(i).setValue(tick); }
		}
	}

	private void receivedIssue()
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
					try
					{
						cBtn.btnSave.setEnabled(false);
						for (int i=0; i<tbLblIssueId.size(); i++)
						{
							if (tbChkSelect.get(i).getValue().booleanValue())
							{
								String id = tbLblIssueId.get(i).getValue().toString();
								iig.IssueFromReceivedData(id, sessionBean.getUserId());
							}
						}
						cm.showNotification("success", "Successfull!", "All information updated successfully.");
						cBtn.btnSave.setEnabled(true);
						loadTableInfo();
					}
					catch(Exception e)
					{ System.out.println(e); }
				}
				else if (buttonType == ButtonType.NO)
				{ cBtn.btnSave.setEnabled(true); }
			}
		});
	}

	private void loadTableInfo()
	{
		setTotalAmount();
		if (txtFromDate.getValue() != null && txtToDate.getValue() != null)
		{
			String fromDate = cm.dfDb.format(txtFromDate.getValue());
			String toDate = cm.dfDb.format(txtToDate.getValue());
			String branchId = cmbBranchName.getValue()!=null?cmbBranchName.getValue().toString():"%";
			String search = "%"+txtSearch.getValue().toString().replaceAll("'", "")+"%";
			tableClear();
			try
			{
				String sql = "select b.vBranchId, b.vBranchName, a.vIssueId, a.vIssueNo, a.dIssueDate, a.mTotalAmount,"+
						" a.vReceivedBy, c.vFullName from trans.tbIssueInfo a inner join master.tbBranchMaster b on"+
						" a.vBranchTo = b.vBranchId inner join master.tbUserInfo c on a.vCreatedBy = c.vUserId where"+
						" a.vBranchTo like '"+sessionBean.getBranchId()+"' and a.vBranchFrom like '"+branchId+"' and"+
						" a.dIssueDate between '"+fromDate+"' and '"+toDate+"' and a.vIssueNo like '"+search+"' and"+
						" a.iStatus = 0 and a.iActive = 1 order by a.dIssueDate, a.iAutoId desc";
				//System.out.println(sql);
				int i = 0;
				for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
				{
					Object[] element = (Object[]) iter.next();

					if (tbLblIssueId.size() <= i)
					{ tableRowAdd(i); }

					tbLblIssueId.get(i).setValue(element[2].toString());
					tbLblIssueNo.get(i).setValue(element[3].toString());
					tbLblIssueDate.get(i).setValue(cm.dfBd.format(element[4]));
					tbLblAmount.get(i).setValue(cm.setComma(element[5].toString()));
					tbLblReceivedBy.get(i).setValue(element[6].toString());
					tbLblIssueBy.get(i).setValue(element[7].toString());
					tbLblIssueTo.get(i).setValue(element[1].toString());
					i++;
				}
				if (i == 0)
				{ cm.showNotification("warning", "Sorry!", "No data found."); }
				tblPendingIssueList.nextPage();
				tblPendingIssueList.previousPage();
			}
			catch (Exception e)
			{ System.out.println(e); }
		}
		else
		{
			cm.showNotification("warning", "Warning!", "Check the date.");
			txtFromDate.focus();
		}
	}

	private void tableClear()
	{ cm.tableClear(tblPendingIssueList, tbLblIssueId); }

	public void viewReportSingle(String issueId)
	{
		IssueInformation report = new IssueInformation(sessionBean, "");
		report.viewReportSingle(issueId);
	}

	public void enter(ViewChangeEvent event)
	{ loadTableInfo(); }
}
