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
public class SupplierInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblSupplierList;
	private ArrayList<Label> tbLblSupplierId = new ArrayList<Label>();
	private ArrayList<Label> tbLblSupplierName = new ArrayList<Label>();
	private ArrayList<Label> tbLblSupplierAddress = new ArrayList<Label>();
	private ArrayList<Label> tbLblVatRegNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblCreditLimit = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;

	private TextField txtSearch;
	private OptionGroup ogFilter;

	private CommonMethod cm;
	private SupplierInfoGateway sig = new SupplierInfoGateway();
	private String formId;

	//Report panel
	private CommonButton cBtnView = new CommonButton("", "", "", "", "", "", "", "View", "");
	private OptionGroup ogSupplierStaus, ogReportFormat;

	public SupplierInformation(SessionBean sessionBean, String formId)
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

		txtSearch.addValueChangeListener(event ->
		{ loadTableInfo(); 
		});

		cBtnView.btnPreview.addClickListener(event ->
		{ addValidation(); });

		ogFilter.addValueChangeListener(event ->
		{ loadTableInfo(); });
	}

	private void addEditWindow(String addEdit, String supplierId, String ar)
	{
		AddEditSupplierInfo win = new AddEditSupplierInfo(sessionBean, addEdit, supplierId);
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
		Panel pnlTable = new Panel("Supplier List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout lay = new HorizontalLayout();
		lay.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Supplier Name");
		txtSearch.setDescription("Search by supplier name");
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

		content.addComponents(lay, tblSupplierList, tblSupplierList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblSupplierList = new TablePaged();
		tblSupplierList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblSupplierId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblSupplierList.addContainerProperty("Supplier Id", Label.class, new Label(), null, null, Align.CENTER);
		tblSupplierList.setColumnCollapsed("Supplier Id", true);

		tblSupplierList.addContainerProperty("Supplier Details", Label.class, new Label(), null, null, Align.CENTER);

		tblSupplierList.addContainerProperty("Supplier Address", Label.class, new Label(), null, null, Align.CENTER);

		tblSupplierList.addContainerProperty("Vat Reg No", Label.class, new Label(), null, null, Align.CENTER);

		tblSupplierList.addContainerProperty("Credit Limit", Label.class, new Label(), null, null, Align.CENTER);

		tblSupplierList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblSupplierList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblSupplierList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblSupplierId.add(ar, new Label());
			tbLblSupplierId.get(ar).setWidth("100%");
			tbLblSupplierId.get(ar).setImmediate(true);
			tbLblSupplierId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblSupplierName.add(ar, new Label());
			tbLblSupplierName.get(ar).setWidth("100%");
			tbLblSupplierName.get(ar).setImmediate(true);
			tbLblSupplierName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblSupplierAddress.add(ar, new Label());
			tbLblSupplierAddress.get(ar).setWidth("100%");
			tbLblSupplierAddress.get(ar).setImmediate(true);
			tbLblSupplierAddress.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblVatRegNo.add(ar, new Label());
			tbLblVatRegNo.get(ar).setWidth("100%");
			tbLblVatRegNo.get(ar).setImmediate(true);
			tbLblVatRegNo.get(ar).setStyleName(ValoTheme.LABEL_TINY);

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
				tbCmbAction.get(ar).setItemIcon("Active/Inactive", FontAwesome.LOCK);
				tbCmbAction.get(ar).addItem("Active/Inactive");
				tbCmbAction.get(ar).setItemIcon("Edit", FontAwesome.EDIT);
				tbCmbAction.get(ar).addItem("Edit");
			}
			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String id = tbLblSupplierId.get(ar).getValue().toString();
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
									if (sig.activeInactiveSupplier(id, sessionBean.getUserId()))
									{
										tbChkActive.get(ar).setValue(!tbChkActive.get(ar).getValue().booleanValue());
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
			tblSupplierList.addItem(new Object[]{tbLblSupplierId.get(ar), tbLblSupplierName.get(ar), tbLblSupplierAddress.get(ar),
					tbLblVatRegNo.get(ar), tbLblCreditLimit.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
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
		if (ogFilter.getValue().toString().equals("Inactive"))
		{ status = "0"; }
		if (ogFilter.getValue().toString().equals("All"))
		{ status = "%"; }

		tableClear();
		int i = 0;
		try
		{
			String sql = "select vSupplierId, vSupplierName, vAddress, vVatRegNo, iActive, vSupplierCode, iCreditLimit,"+
					" dbo.funGetNumeric(vSupplierCode) iSerial from master.tbSupplierMaster where (vSupplierName like"+
					" '"+search+"' or vSupplierCode like '"+search+"' or vVatRegNo like '"+search+"') and iActive like"+
					" '"+status+"' order by iSerial desc";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblSupplierId.size() <= i)
				{ tableRowAdd(i); }

				tbLblSupplierId.get(i).setValue(element[0].toString());
				tbLblSupplierName.get(i).setValue(element[5].toString()+" - "+element[1].toString());
				tbLblSupplierAddress.get(i).setValue(element[2].toString());
				tbLblVatRegNo.get(i).setValue(element[3].toString());
				tbChkActive.get(i).setValue((element[4].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);
				tbLblCreditLimit.get(i).setValue(element[6].toString()+" Days");

				i++;
			}
			tblSupplierList.nextPage();
			tblSupplierList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
		}
		catch (Exception e)
		{ System.out.println("Table Query "+e); }
	}

	private void tableClear()
	{ cm.tableClear(tblSupplierList, tbLblSupplierId); }

	//Report Panel Start
	private Panel addReportPanel()
	{
		Panel panelReport = new Panel("Supplier Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 3);
		lay.setSpacing(true);

		ogSupplierStaus = new OptionGroup();
		ogSupplierStaus.addItem("All");
		ogSupplierStaus.addItem("Active");
		ogSupplierStaus.addItem("Inactive");
		ogSupplierStaus.select("All");
		ogSupplierStaus.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogSupplierStaus.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Supplier Status: "), 0, 0);
		lay.addComponent(ogSupplierStaus, 1, 0);

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

	private void addValidation()
	{ viewReport(); }

	@SuppressWarnings("deprecation")
	public void viewReport()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", xsql = "", fileName = "";

		try
		{
			String status = ogSupplierStaus.getValue().toString();
			String stat = "";
			if (status.equals("All")) {stat = "%";}
			else if (status.equals("Active")) {stat = "1";}
			else {stat = "0";}

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			if (ogReportFormat.getValue().toString().equals("PDF"))
			{
				sql = "select vSupplierCode, vSupplierName, vAddress, vPhone, vFax, vEmail, vLicenceNo, vVatRegNo,"+
						" vContactPerson, vContactMobile, vContactEmail, iCreditLimit, mDiscount, iActive"+
						" from master.tbSupplierMaster where iActive like '"+stat+"' order by iActive,"+
						" vSupplierName desc";
				reportSource = "com/jasper/possetup/rptSupplierInformation.jasper";

				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("logo", sessionBean.getCompanyLogo());
				hm.put("userIp", sessionBean.getUserIp());

				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = "select vSupplierCode, vSupplierName, vAddress, vPhone, vFax, vEmail, vLicenceNo, vVatRegNo,"+
						" vContactPerson, vContactMobile, vContactEmail, iCreditLimit, mDiscount, case when iActive = 0"+
						" then 'Inactive' else 'Active' end iStatus from master.tbSupplierMaster where iActive like"+
						" '"+stat+"' order by iActive, vSupplierName desc";
				fileName = "Supplier_List_";

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
	{
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
		loadTableInfo();
	}
}
