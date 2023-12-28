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

import com.common.share.CommonMethod;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class CompanyInfo extends VerticalLayout implements View
{
	private TablePaged tblCompanyList;
	private ArrayList<Label> tbLblCompanyId = new ArrayList<Label>();
	private ArrayList<Label> tbLblCompanyName = new ArrayList<Label>();
	private ArrayList<Label> tbLblAddress = new ArrayList<Label>();
	private ArrayList<Label> tbLblLicense = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;
	private TextField txtSearch;

	private CommonMethod cm;
	//private String formId;

	public CompanyInfo(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		//this.formId = formId;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		addComponents(addPanel());

		addActions();
	}

	private void addActions()
	{
		txtSearch.addValueChangeListener(event -> loadTableInfo());
	}

	private void addEditWindow(String addEdit, String companyId, String ar)
	{
		AddEditCompany win = new AddEditCompany(sessionBean, addEdit, companyId);
		getUI().addWindow(win);
		win.center();
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(event ->
		{
			if (!ar.isEmpty())
			{ tbCmbAction.get(Integer.parseInt(ar)).setEnabled(true); }
			loadTableInfo();
		});
	}

	private Panel addPanel()
	{
		Panel pnlTable = new Panel("Company Information :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Company");
		txtSearch.setDescription("Search by company name");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		buildTable();

		content.addComponents(txtSearch, tblCompanyList, tblCompanyList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblCompanyList = new TablePaged();
		tblCompanyList.addItemClickListener(event ->
		{
			if (event.isDoubleClick())
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblCompanyId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblCompanyList.addContainerProperty("Company Id", Label.class, new Label(), null, null, Align.CENTER);
		tblCompanyList.setColumnCollapsed("Company Id", true);

		tblCompanyList.addContainerProperty("Company Name", Label.class, new Label(), null, null, Align.CENTER);

		tblCompanyList.addContainerProperty("Address", Label.class, new Label(), null, null, Align.CENTER);

		tblCompanyList.addContainerProperty("CR No", Label.class, new Label(), null, null, Align.CENTER);

		tblCompanyList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblCompanyList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblCompanyList.setColumnWidth("Action", 110);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblCompanyId.add(ar, new Label());
			tbLblCompanyId.get(ar).setWidth("100%");
			tbLblCompanyId.get(ar).setImmediate(true);
			tbLblCompanyId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblCompanyName.add(ar, new Label());
			tbLblCompanyName.get(ar).setWidth("100%");
			tbLblCompanyName.get(ar).setImmediate(true);
			tbLblCompanyName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblAddress.add(ar, new Label());
			tbLblAddress.get(ar).setWidth("100%");
			tbLblAddress.get(ar).setImmediate(true);
			tbLblAddress.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblLicense.add(ar, new Label());
			tbLblLicense.get(ar).setWidth("100%");
			tbLblLicense.get(ar).setImmediate(true);
			tbLblLicense.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbChkActive.add(ar, new CheckBox());
			tbChkActive.get(ar).setWidth("100%");
			tbChkActive.get(ar).setImmediate(true);
			tbChkActive.get(ar).setStyleName(ValoTheme.CHECKBOX_SMALL);

			tbCmbAction.add(ar, new ComboBox());
			tbCmbAction.get(ar).setWidth("100%");
			tbCmbAction.get(ar).setImmediate(true);
			tbCmbAction.get(ar).setInputPrompt("Action");
			tbCmbAction.get(ar).addStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbAction.get(ar).setTextInputAllowed(false);
			tbCmbAction.get(ar).setNullSelectionAllowed(false);
			if (cm.update)
			{
				tbCmbAction.get(ar).addItem("Edit");
				tbCmbAction.get(ar).setItemIcon("Edit", FontAwesome.EDIT);
			}
			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String comId = tbLblCompanyId.get(ar).getValue().toString();
				if (!comId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					tbCmbAction.get(ar).setEnabled(false);
					String action = tbCmbAction.get(ar).getValue().toString();
					if (action.equals("Edit"))
					{ addEditWindow("Edit", comId, ar+""); }
				}
				tbCmbAction.get(ar).select(null);
			});
			tblCompanyList.addItem(new Object[]{tbLblCompanyId.get(ar), tbLblCompanyName.get(ar),
					tbLblAddress.get(ar), tbLblLicense.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table"); }
	}

	private void loadTableInfo()
	{
		String search = "%"+txtSearch.getValue().toString().replaceAll("'", "")+"%";
		tableClear();
		int i = 0;
		try
		{
			String sql = " select vCompanyId, vCompanyName, vAddress, vLicenceNo, iActive from"+
					" master.tbCompanyMaster where vCompanyName like '"+search+"' order by vCompanyName ";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblCompanyId.size() <= i)
				{ tableRowAdd(i); }

				tbLblCompanyId.get(i).setValue(element[0].toString());
				tbLblCompanyName.get(i).setValue(element[1].toString());
				tbLblAddress.get(i).setValue(element[2].toString());
				tbLblLicense.get(i).setValue(element[3].toString());
				tbChkActive.get(i).setValue((element[4].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);
				i++;
			}
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			tblCompanyList.nextPage();
			tblCompanyList.previousPage();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblCompanyList, tbLblCompanyId); }

	public void enter(ViewChangeEvent event)
	{ loadTableInfo(); }
}
