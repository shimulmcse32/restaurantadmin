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
package com.example.hrmaster;

import java.util.ArrayList;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.EmployeeInfoGateway;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class EmployeeInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblEmployeeList;
	private ArrayList<Label> tbLblEmployeeId = new ArrayList<Label>();
	private ArrayList<Label> tbLblEmployeeDetails = new ArrayList<Label>();
	private ArrayList<Label> tbLblContactNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblDepartment = new ArrayList<Label>();
	private ArrayList<Label> tbLblDesignation = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;
	private Panel pnlTable;

	private TextField txtSearch;
	private OptionGroup ogFilter;
	private EmployeeInfoGateway eig = new EmployeeInfoGateway();

	private CommonMethod cm;

	public EmployeeInformation(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);

		buildTable();
		addComponents(cBtn, addPanel());
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

		ogFilter.addValueChangeListener(event -> loadTableInfo());

		cBtn.btnRefresh.addClickListener(event -> loadTableInfo());

		txtSearch.addValueChangeListener(event -> loadTableInfo());
	}

	private void addEditWindow(String addEdit, String employeeId, String ar)
	{
		AddEditEmployee win = new AddEditEmployee(sessionBean, addEdit, employeeId);
		getUI().addWindow(win);
		win.center();
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(new CloseListener()
		{
			public void windowClose(CloseEvent e)
			{
				if (!ar.isEmpty())
				{ tbCmbAction.get(Integer.parseInt(ar)).setEnabled(true); }
				cBtn.btnNew.setEnabled(true);
				loadTableInfo();
			}
		});
	}

	private Panel addPanel()
	{
		pnlTable = new Panel("Employee List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout lay = new HorizontalLayout();
		lay.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Employee");
		txtSearch.setDescription("Search plate no by employee code or name or cpr");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		ogFilter = new OptionGroup();
		ogFilter.addItem("Active");
		ogFilter.addItem("Inactive");
		ogFilter.addItem("All");
		ogFilter.select("Active");
		ogFilter.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogFilter.addStyleName(ValoTheme.OPTIONGROUP_SMALL);

		lay.addComponents(txtSearch, ogFilter);

		content.addComponents(lay, tblEmployeeList, tblEmployeeList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblEmployeeList = new TablePaged();
		tblEmployeeList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblEmployeeId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblEmployeeList.addContainerProperty("Emp Id", Label.class, new Label(), null, null, Align.CENTER);
		tblEmployeeList.setColumnCollapsed("Emp Id", true);

		tblEmployeeList.addContainerProperty("Employee Details", Label.class, new Label(), null, null, Align.CENTER);

		tblEmployeeList.addContainerProperty("Contact No", Label.class, new Label(), null, null, Align.CENTER);

		tblEmployeeList.addContainerProperty("Department", Label.class, new Label(), null, null, Align.CENTER);
		tblEmployeeList.setColumnCollapsed("Department", true);

		tblEmployeeList.addContainerProperty("Designation", Label.class, new Label(), null, null, Align.CENTER);

		tblEmployeeList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblEmployeeList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblEmployeeList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblEmployeeId.add(ar, new Label());
			tbLblEmployeeId.get(ar).setWidth("100%");
			tbLblEmployeeId.get(ar).setImmediate(true);
			tbLblEmployeeId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblEmployeeDetails.add(ar, new Label("", ContentMode.HTML));
			tbLblEmployeeDetails.get(ar).setWidth("100%");
			tbLblEmployeeDetails.get(ar).setImmediate(true);
			tbLblEmployeeDetails.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblContactNo.add(ar, new Label());
			tbLblContactNo.get(ar).setWidth("100%");
			tbLblContactNo.get(ar).setImmediate(true);
			tbLblContactNo.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblDepartment.add(ar, new Label());
			tbLblDepartment.get(ar).setWidth("100%");
			tbLblDepartment.get(ar).setImmediate(true);
			tbLblDepartment.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblDesignation.add(ar, new Label());
			tbLblDesignation.get(ar).setWidth("100%");
			tbLblDesignation.get(ar).setImmediate(true);
			tbLblDesignation.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbChkActive.add(ar, new CheckBox());
			tbChkActive.get(ar).setWidth("100%");
			tbChkActive.get(ar).setImmediate(true);
			tbChkActive.get(ar).setStyleName(ValoTheme.CHECKBOX_SMALL);

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
				tbCmbAction.get(ar).addItem("Edit");
				tbCmbAction.get(ar).setItemIcon("Active/Inactive", FontAwesome.LOCK);
				tbCmbAction.get(ar).setItemIcon("Edit", FontAwesome.EDIT);
			}
			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String empId = tbLblEmployeeId.get(ar).getValue().toString();
				if (!empId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					String action = tbCmbAction.get(ar).getValue().toString();
					if (action.equals("Edit"))
					{
						tbCmbAction.get(ar).setEnabled(false);
						addEditWindow("Edit", empId, ar+"");
					}
					else if (action.equals("Active/Inactive"))
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
									if (eig.activeInactiveData(empId, sessionBean.getUserId()))
									{
										cm.showNotification("success", "Successfull!", "All information updated successfully.");
										tbCmbAction.get(ar).setEnabled(true);
										tbChkActive.get(ar).setValue(!tbChkActive.get(ar).getValue().booleanValue());
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
			tblEmployeeList.addItem(new Object[]{tbLblEmployeeId.get(ar), tbLblEmployeeDetails.get(ar),
					tbLblContactNo.get(ar), tbLblDepartment.get(ar), tbLblDesignation.get(ar),
					tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void loadTableInfo()
	{
		String search = "%"+txtSearch.getValue().toString().replaceAll("'", "")+"%";
		String status = "", empCaption = "";
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
			String sql = " select em.vEmployeeId, em.vEmployeeCode, em.vEmployeeName, em.vCPRNumber, dem.vDepartmentName,"+
					" dm.vDesignation, em.iActive, em.vContactNo, dbo.funGetNumeric(em.vEmployeeCode) iSerial from"+
					" master.tbEmployeeMaster em, master.tbDesignationMaster dm, master.tbDepartmentMaster dem where"+
					" em.vDesignationId = dm.vDesignationId and em.vDeparmentId = dem.vDepartmentId and (em.vEmployeeCode"+
					" like '"+search+"' or em.vEmployeeName like '"+search+"' or em.vCPRNumber like '"+search+"' or"+
					" vContactNo like '"+search+"') and em.vBranchId = '"+sessionBean.getBranchId()+"' and em.iActive"+
					" like '"+status+"' order by iSerial desc";
			//System.out.println(sql);
			for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblEmployeeId.size() <= i)
				{ tableRowAdd(i); }

				empCaption = element[1].toString()+" - "+element[2].toString()+" - "+element[3].toString();
				/*empDetails = element[11].toString().isEmpty()?"<b>"+empCaption+"</b>":
					"<b style='color:red'>"+empCaption+"</b>";*/

				tbLblEmployeeId.get(i).setValue(element[0].toString());
				tbLblEmployeeDetails.get(i).setValue(empCaption);
				tbLblContactNo.get(i).setValue(element[7].toString());
				tbLblDepartment.get(i).setValue(element[4].toString());
				tbLblDesignation.get(i).setValue(element[5].toString());
				tbChkActive.get(i).setValue((element[6].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);
				i++;
			}
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			tblEmployeeList.nextPage();
			tblEmployeeList.previousPage();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblEmployeeList, tbLblEmployeeId); }

	public void enter(ViewChangeEvent event)
	{ loadTableInfo(); }
}
