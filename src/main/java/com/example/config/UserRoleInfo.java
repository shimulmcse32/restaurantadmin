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
import com.example.gateway.RoleInfoGateway;
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
public class UserRoleInfo extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblRoleList;
	private ArrayList<Label> tbLblRoleId = new ArrayList<Label>();
	private ArrayList<Label> tbLblRoleName = new ArrayList<Label>();
	private ArrayList<Label> tbLblUserName = new ArrayList<Label>();
	private ArrayList<Label> tbLblDate = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;
	private Panel pnlTable;
	private TextField txtSearch;

	private CommonMethod cm;
	private RoleInfoGateway rig = new RoleInfoGateway();

	public UserRoleInfo(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;		
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);

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

		cBtn.btnRefresh.addClickListener(event -> loadRoleInfo());

		txtSearch.addValueChangeListener(event -> loadRoleInfo());
	}

	private void addEditWindow(String addEdit, String roleId, String ar)
	{
		AddEditRole win = new AddEditRole(sessionBean, addEdit, roleId);
		getUI().addWindow(win);
		win.center();
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(event ->
		{
			if (!ar.isEmpty())
			{ tbCmbAction.get(Integer.parseInt(ar)).setEnabled(true); }
			cBtn.btnNew.setEnabled(true);
			loadRoleInfo();
		});
	}

	private Panel addPanel()
	{
		pnlTable = new Panel("Role List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Role");
		txtSearch.setDescription("Search by role name");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		buildTable();

		content.addComponents(txtSearch, tblRoleList, tblRoleList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblRoleList = new TablePaged();
		tblRoleList.addItemClickListener(event ->
		{
			if (event.isDoubleClick())
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblRoleId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblRoleList.addContainerProperty("Role Id", Label.class, new Label(), null, null, Align.CENTER);
		tblRoleList.setColumnCollapsed("Role Id", true);

		tblRoleList.addContainerProperty("Role Name", Label.class, new Label(), null, null, Align.CENTER);

		tblRoleList.addContainerProperty("Created By", Label.class, new Label(), null, null, Align.CENTER);

		tblRoleList.addContainerProperty("Created Date", Label.class, new Label(), null, null, Align.CENTER);

		tblRoleList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblRoleList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblRoleList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblRoleId.add(ar, new Label());
			tbLblRoleId.get(ar).setWidth("100%");
			tbLblRoleId.get(ar).setImmediate(true);
			tbLblRoleId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblRoleName.add(ar, new Label());
			tbLblRoleName.get(ar).setWidth("100%");
			tbLblRoleName.get(ar).setImmediate(true);
			tbLblRoleName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblUserName.add(ar, new Label());
			tbLblUserName.get(ar).setWidth("100%");
			tbLblUserName.get(ar).setImmediate(true);
			tbLblUserName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblDate.add(ar, new Label());
			tbLblDate.get(ar).setWidth("100%");
			tbLblDate.get(ar).setImmediate(true);
			tbLblDate.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbChkActive.add(ar, new CheckBox());
			tbChkActive.get(ar).setWidth("100%");
			tbChkActive.get(ar).setImmediate(true);
			tbChkActive.get(ar).setStyleName(ValoTheme.CHECKBOX_SMALL);

			tbCmbAction.add(ar, new ComboBox());
			tbCmbAction.get(ar).setWidth("100%");
			tbCmbAction.get(ar).setImmediate(true);
			tbCmbAction.get(ar).addStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbAction.get(ar).setInputPrompt("Action");
			tbCmbAction.get(ar).setTextInputAllowed(false);
			tbCmbAction.get(ar).setNullSelectionAllowed(false);
			if (cm.update)
			{
				tbCmbAction.get(ar).addItem("Active/Inactive");
				tbCmbAction.get(ar).addItem("Edit");
				tbCmbAction.get(ar).setItemIcon("Active/Inactive", FontAwesome.LOCK);
				tbCmbAction.get(ar).setItemIcon("Edit", FontAwesome.EDIT);
			}
			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String rId = tbLblRoleId.get(ar).getValue().toString();
				if (!rId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					tbCmbAction.get(ar).setEnabled(false);
					String action = tbCmbAction.get(ar).getValue().toString();
					if (action.equals("Edit"))
					{ addEditWindow("Edit", rId, ar+""); }
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
									if (rig.activeInactiveData(rId, sessionBean.getUserId()))
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
			tblRoleList.addItem(new Object[]{tbLblRoleId.get(ar), tbLblRoleName.get(ar), tbLblUserName.get(ar),
					tbLblDate.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void loadRoleInfo()
	{
		String search = "%"+txtSearch.getValue().toString().replaceAll("'", "")+"%";
		tableClear();
		int i = 0;
		try
		{
			String sql = " select ri.vRoleId, vRoleName, vFullName, ri.dModifiedDate,"+
					" ri.iActive from master.tbUserRoleInfo ri, master.tbUserInfo"+
					" ui where ri.vModifiedBy = ui.vUserId and vRoleName like '"+search+"' ";
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblRoleId.size() <= i)
				{ tableRowAdd(i); }

				tbLblRoleId.get(i).setValue(element[0].toString());
				tbLblRoleName.get(i).setValue(element[1].toString());
				tbLblUserName.get(i).setValue(element[2].toString());
				tbLblDate.get(i).setValue(cm.dfBdHMA.format(element[3]));
				tbChkActive.get(i).setValue((element[4].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);
				i++;
			}
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			tblRoleList.nextPage();
			tblRoleList.previousPage();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblRoleList, tbLblRoleId); }

	public void enter(ViewChangeEvent event)
	{ loadRoleInfo(); }
}
