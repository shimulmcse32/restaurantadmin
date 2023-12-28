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
package com.example.config;

import java.util.ArrayList;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.BranchInfoGateway;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class BranchInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblBranchList;
	private ArrayList<Label> tbLblBranchId = new ArrayList<Label>();
	private ArrayList<Label> tbLblBranchName = new ArrayList<Label>();
	private ArrayList<Label> tbLblAddress = new ArrayList<Label>();
	private ArrayList<Label> tbLblLicense = new ArrayList<Label>();
	private ArrayList<Label> tbLblBranchType = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;

	private TextField txtSearch;
	private OptionGroup ogFilter;

	private CommonMethod cm;
	private BranchInfoGateway big = new BranchInfoGateway();
	private String formId;

	public BranchInformation(SessionBean sessionBean, String formId)
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

		cBtn.btnRefresh.addClickListener(event -> loadTableInfo());

		txtSearch.addValueChangeListener(event -> loadTableInfo());

		ogFilter.addValueChangeListener(event -> loadTableInfo());
	}

	private void addEditWindow(String addEdit, String companyId, String ar)
	{
		AddEditBranch win = new AddEditBranch(sessionBean, addEdit, companyId);
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
		Panel pnlTable = new Panel("Branch List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout lay = new HorizontalLayout();
		lay.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Branch");
		txtSearch.setDescription("Search by branch name");
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

		content.addComponents(lay, tblBranchList, tblBranchList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblBranchList = new TablePaged();
		tblBranchList.addItemClickListener(event ->
		{
			if (event.isDoubleClick())
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblBranchId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblBranchList.addContainerProperty("Branch Id", Label.class, new Label(), null, null, Align.CENTER);
		tblBranchList.setColumnCollapsed("Branch Id", true);

		tblBranchList.addContainerProperty("Branch Name", Label.class, new Label(), null, null, Align.CENTER);

		tblBranchList.addContainerProperty("Address", Label.class, new Label(), null, null, Align.CENTER);

		tblBranchList.addContainerProperty("CR No", Label.class, new Label(), null, null, Align.CENTER);

		tblBranchList.addContainerProperty("Branch Type", Label.class, new Label(), null, null, Align.CENTER);

		tblBranchList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblBranchList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblBranchList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblBranchId.add(ar, new Label());
			tbLblBranchId.get(ar).setWidth("100%");
			tbLblBranchId.get(ar).setImmediate(true);
			tbLblBranchId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblBranchName.add(ar, new Label());
			tbLblBranchName.get(ar).setWidth("100%");
			tbLblBranchName.get(ar).setImmediate(true);
			tbLblBranchName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblAddress.add(ar, new Label());
			tbLblAddress.get(ar).setWidth("100%");
			tbLblAddress.get(ar).setImmediate(true);
			tbLblAddress.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblLicense.add(ar, new Label());
			tbLblLicense.get(ar).setWidth("100%");
			tbLblLicense.get(ar).setImmediate(true);
			tbLblLicense.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblBranchType.add(ar, new Label());
			tbLblBranchType.get(ar).setWidth("100%");
			tbLblBranchType.get(ar).setImmediate(true);
			tbLblBranchType.get(ar).setStyleName(ValoTheme.LABEL_TINY);

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
				tbCmbAction.get(ar).addItem("Edit");
				tbCmbAction.get(ar).setItemIcon("Active/Inactive", FontAwesome.LOCK);
				tbCmbAction.get(ar).setItemIcon("Edit", FontAwesome.EDIT);
			}
			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String brId = tbLblBranchId.get(ar).getValue().toString();
				if (!brId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					tbCmbAction.get(ar).setEnabled(false);
					String action = tbCmbAction.get(ar).getValue().toString();
					if (action.equals("Edit"))
					{ addEditWindow("Edit", brId, ar+""); }
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
									if (big.activeInactiveData(brId, sessionBean.getUserId()))
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
			tblBranchList.addItem(new Object[]{tbLblBranchId.get(ar), tbLblBranchName.get(ar), tbLblAddress.get(ar),
					tbLblLicense.get(ar), tbLblBranchType.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
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
			String sql = " select vBranchId, vBranchName, vAddress, vLicenceNo, vBranchType, iActive from"+
					" master.tbBranchMaster bm, master.tbBranchType bt where bm.iBranchTypeId = bt.iBranchTypeId"+
					" and vBranchName like '"+search+"' and vCompanyId = '"+sessionBean.getCompanyId()+"'"+
					" and iActive like '"+status+"' order by vBranchName ";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblBranchId.size() <= i)
				{ tableRowAdd(i); }

				tbLblBranchId.get(i).setValue(element[0].toString());
				tbLblBranchName.get(i).setValue(element[1].toString());
				tbLblAddress.get(i).setValue(element[2].toString());
				tbLblLicense.get(i).setValue(element[3].toString());
				tbLblBranchType.get(i).setValue(element[4].toString());
				tbChkActive.get(i).setValue((element[5].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);
				i++;
			}
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			tblBranchList.nextPage();
			tblBranchList.previousPage();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblBranchList, tbLblBranchId); }

	public void enter(ViewChangeEvent event)
	{
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
		loadTableInfo();
	}
}
