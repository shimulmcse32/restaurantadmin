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
package com.example.possetup;

import java.util.ArrayList;
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
import com.example.gateway.SupplierInfoGateway;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
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
public class CustomerInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblCustomerList;
	private ArrayList<Label> tbLblCustomerId = new ArrayList<Label>();
	private ArrayList<Label> tbLblCustomerDetails = new ArrayList<Label>();
	private ArrayList<Label> tbLblCustomerMobile = new ArrayList<Label>();
	private ArrayList<Label> tbLblEmail = new ArrayList<Label>();
	private ArrayList<Label> tbLblCreditLimit = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;
	private Panel pnlTable;

	private TextField txtSearch;
	private OptionGroup ogFilter;

	private CommonMethod cm;
	private SupplierInfoGateway sig = new SupplierInfoGateway();

	//Report panel
	private Panel panelReport;
	private CommonButton cBtnView = new CommonButton("", "", "", "", "", "", "", "View", "");
	private ComboBox cmbCustomer;
	private OptionGroup ogReportFormat;

	public CustomerInformation(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		//Check authorisation
		cm.setAuthorize(sessionBean.getUserId(), formId);
		addComponents(cBtn, addPanel(), addReportPanel());
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

		cBtn.btnRefresh.addClickListener(event -> loadTableInfo());

		txtSearch.addValueChangeListener(event -> loadTableInfo());

		cBtnView.btnPreview.addClickListener(event -> addValidation());

		ogFilter.addValueChangeListener(event -> loadTableInfo());
	}

	private void addEditWindow(String addEdit, String customerId, String ar)
	{
		AddEditCustomerInfo win = new AddEditCustomerInfo(sessionBean, addEdit, customerId);
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

	private Panel addPanel()
	{
		pnlTable = new Panel("Customer List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout lay = new HorizontalLayout();
		lay.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Customer Name");
		txtSearch.setDescription("Search by customer name");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		ogFilter = new OptionGroup();
		ogFilter.addItem("Active");
		ogFilter.addItem("Inactive");
		ogFilter.addItem("All");
		ogFilter.select("Active");
		ogFilter.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogFilter.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		ogFilter.setDescription("User Status");

		lay.addComponents(txtSearch, ogFilter);

		buildTable();

		content.addComponents(lay, tblCustomerList, tblCustomerList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblCustomerList = new TablePaged();
		tblCustomerList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblCustomerId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblCustomerList.addContainerProperty("Customer Id", Label.class, new Label(), null, null, Align.CENTER);
		tblCustomerList.setColumnCollapsed("Customer Id", true);

		tblCustomerList.addContainerProperty("Customer Details", Label.class, new Label(), null, null, Align.CENTER);

		tblCustomerList.addContainerProperty("Mobile No", Label.class, new Label(), null, null, Align.CENTER);

		tblCustomerList.addContainerProperty("Email Address", Label.class, new Label(), null, null, Align.CENTER);

		tblCustomerList.addContainerProperty("Credit Limit", Label.class, new Label(), null, null, Align.CENTER);

		tblCustomerList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblCustomerList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblCustomerList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblCustomerId.add(ar, new Label());
			tbLblCustomerId.get(ar).setWidth("100%");
			tbLblCustomerId.get(ar).setImmediate(true);
			tbLblCustomerId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblCustomerDetails.add(ar, new Label());
			tbLblCustomerDetails.get(ar).setWidth("100%");
			tbLblCustomerDetails.get(ar).setImmediate(true);
			tbLblCustomerDetails.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblCustomerMobile.add(ar, new Label());
			tbLblCustomerMobile.get(ar).setWidth("100%");
			tbLblCustomerMobile.get(ar).setImmediate(true);
			tbLblCustomerMobile.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblEmail.add(ar, new Label());
			tbLblEmail.get(ar).setWidth("100%");
			tbLblEmail.get(ar).setImmediate(true);
			tbLblEmail.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblCreditLimit.add(ar, new Label());
			tbLblCreditLimit.get(ar).setWidth("100%");
			tbLblCreditLimit.get(ar).setImmediate(true);
			tbLblCreditLimit.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbChkActive.add(ar, new CheckBox());
			tbChkActive.get(ar).setWidth("100%");
			tbChkActive.get(ar).setImmediate(true);
			tbChkActive.get(ar).setStyleName(ValoTheme.CHECKBOX_SMALL);

			tbCmbAction.add(ar, new ComboBox());
			tbCmbAction.get(ar).setWidth("100%");
			tbCmbAction.get(ar).setImmediate(true);
			tbCmbAction.get(ar).addStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbAction.get(ar).setTextInputAllowed(false);
			tbCmbAction.get(ar).setNullSelectionAllowed(false);
			tbCmbAction.get(ar).setInputPrompt("Action");
			if (cm.update)
			{
				tbCmbAction.get(ar).addItem("Active/Inactive");
				tbCmbAction.get(ar).setItemIcon("Active/Inactive", FontAwesome.LOCK);
				tbCmbAction.get(ar).addItem("Edit");
				tbCmbAction.get(ar).setItemIcon("Edit", FontAwesome.EDIT);
			}
			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String id = tbLblCustomerId.get(ar).getValue();
				if (!id.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{
						tbCmbAction.get(ar).setEnabled(false);
						addEditWindow("Edit", id, ar+"");
					}
					else if (tbCmbAction.get(ar).getValue().toString().equals("Active/Inactive"))
					{
						tbCmbAction.get(ar).setEnabled(false);
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
									if (sig.activeInactiveCustomer(id, sessionBean.getUserId()))
									{
										cm.showNotification("success", "Successfull!", "All information updated successfully.");
										tbCmbAction.get(ar).setEnabled(true);
									}
									else
									{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
								}
								else if (buttonType == ButtonType.NO)
								{ tbCmbAction.get(ar).setEnabled(true); }
							}
						});
					}
				}
				tbCmbAction.get(ar).select(null);
			});
			tblCustomerList.addItem(new Object[]{tbLblCustomerId.get(ar), tbLblCustomerDetails.get(ar), tbLblCustomerMobile.get(ar),
					tbLblEmail.get(ar), tbLblCreditLimit.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void loadTableInfo()
	{
		String search = "%"+txtSearch.getValue().toString().replaceAll("'", "")+"%";
		String status = "";
		if (ogFilter.getValue().toString().equals("Active"))
		{ status = "1"; }
		else if (ogFilter.getValue().toString().equals("Inactive"))
		{ status = "0"; }
		else if (ogFilter.getValue().toString().equals("All"))
		{ status = "%"; }

		tableClear();
		int i = 0;
		try
		{
			String sql = "select vCustomerId, vCustomerCode, vCustomerName, vMobileNo, vEmailId, iCreditLimit, iActive,"+
					" dbo.funGetNumeric(vCustomerCode) iSerial from master.tbCustomerInfo where (vCustomerName like"+
					" '"+search+"' or vCustomerCode like '"+search+"' or vMobileNo like '"+search+"') and iActive like"+
					" '"+status+"' order by iSerial desc";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblCustomerId.size() <= i)
				{ tableRowAdd(i); }

				tbLblCustomerId.get(i).setValue(element[0].toString());
				tbLblCustomerDetails.get(i).setValue(element[1].toString()+" - "+element[2].toString());
				tbLblCustomerMobile.get(i).setValue(element[3].toString());
				tbLblEmail.get(i).setValue(element[4].toString());
				tbLblCreditLimit.get(i).setValue(element[5].toString()+" Days");
				tbChkActive.get(i).setValue((element[6].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);
				i++;
			}
			tblCustomerList.nextPage();
			tblCustomerList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
		}
		catch (Exception e)
		{ System.out.println("Table Query "+e); }
	}

	private void tableClear()
	{ cm.tableClear(tblCustomerList, tbLblCustomerId); }

	//Report Panel Start
	private Panel addReportPanel()
	{
		panelReport = new Panel("Customer Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 3);
		lay.setSpacing(true);

		cmbCustomer = new ComboBox();
		cmbCustomer.setInputPrompt("Select Customer");
		cmbCustomer.setImmediate(true);
		cmbCustomer.setWidth("350px");
		cmbCustomer.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbCustomer.setFilteringMode(FilteringMode.CONTAINS);
		cmbCustomer.setRequired(true);
		cmbCustomer.setRequiredError("This field is required.");
		lay.addComponent(new Label("Customer: "), 0, 0);
		lay.addComponent(cmbCustomer, 1, 0);
		loadCustomer();

		ogReportFormat = new OptionGroup();
		ogReportFormat.addItem("PDF");
		ogReportFormat.addItem("XLS");
		ogReportFormat.select("PDF");
		ogReportFormat.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormat.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Format: "), 0, 1);
		lay.addComponent(ogReportFormat, 1, 1);

		lay.addComponent(cBtnView, 1, 2);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelReport.setContent(content);

		return panelReport;
	}

	private void loadCustomer()
	{
		String sql = "select vCustomerId, vCustomerCode, vCustomerName from master.tbCustomerInfo"+
				" order by vCustomerCode, vCustomerName", caption = "";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (caption.isEmpty())
			{
				cmbCustomer.addItem("%");
				cmbCustomer.setItemCaption("%", "ALL");
			}
			caption = element[1].toString().isEmpty()?element[2].toString():element[1].toString()+"-"+
					element[2].toString();
			cmbCustomer.addItem(element[0].toString());
			cmbCustomer.setItemCaption(element[0].toString(), caption);
		}
		cmbCustomer.select("%");
	}

	private void addValidation()
	{
		if (cmbCustomer.getValue() != null)
		{ viewReport(); }
		else
		{
			cmbCustomer.focus();
			cm.showNotification("warning", "Warning!", "Select customer."); 
		}
	}

	@SuppressWarnings("deprecation")
	public void viewReport()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", xsql = "", fileName = "";

		try
		{
			String customer = cmbCustomer.getValue().toString();
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			if (ogReportFormat.getValue().toString().equals("PDF"))
			{
				sql = "select vCustomerCode, vCustomerName, vVatRegNo, vMobileNo, vEmailId, iCreditLimit, iActive,"+
						" vArea, vBuildingNo, vFlatNo, vBlockNo, vRoadNo, dbo.funGetNumeric(vCustomerCode) iCode from"+
						" master.tbCustomerInfo cui, master.tbCustomerAddresses cad where cui.vCustomerId = cad.vCustomerId"+
						" and cui.vCustomerId like '"+customer+"' order by iCode, cui.vCustomerName";
				reportSource = "com/jasper/possetup/rptCustomerInformation.jasper";

				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("logo", sessionBean.getCompanyLogo());
				hm.put("userIp", sessionBean.getUserIp());

				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = "select vCustomerCode, vCustomerName, vVatRegNo, vMobileNo, vEmailId, iCreditLimit, case when"+
						" iActive = 0 then 'Inactive' else 'Active' end vStatus, vArea, vBuildingNo, vFlatNo, vBlockNo, vRoadNo,"+
						" dbo.funGetNumeric(vCustomerCode) iCode from master.tbCustomerInfo cui, master.tbCustomerAddresses"+
						" cad where cui.vCustomerId = cad.vCustomerId and cui.vCustomerId like '"+customer+"' order"+
						" by iCode, cui.vCustomerName";
				fileName = "Customer_List_";

				hm.put("parameters", "");
				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	public void enter(ViewChangeEvent event)
	{ loadTableInfo(); loadCustomer(); }
}
