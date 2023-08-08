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
import com.example.gateway.UserInfoGateway;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class UserInfo extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblUserList;
	private ArrayList<Label> tbLblUserId = new ArrayList<Label>();
	private ArrayList<Label> tbLblRoleName = new ArrayList<Label>();
	private ArrayList<Label> tbLblUserName = new ArrayList<Label>();
	private ArrayList<Label> tbLblFullName = new ArrayList<Label>();
	private ArrayList<Label> tbLblExpiryDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblUserType = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;
	private Panel pnlTable;

	private TextField txtSearch;
	private OptionGroup ogFilter;

	private CommonMethod cm;
	private UserInfoGateway uig = new UserInfoGateway();

	public UserInfo(SessionBean sessionBean, String formId)
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

		cBtn.btnRefresh.addClickListener(event -> loadTableInfo());

		txtSearch.addValueChangeListener(event -> loadTableInfo());

		ogFilter.addValueChangeListener(event -> loadTableInfo());
	}

	private void addEditWindow(String addEdit, String userId, String ar)
	{
		AddEditUser win = new AddEditUser(sessionBean, addEdit, userId);
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
		pnlTable = new Panel("User List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout lay = new HorizontalLayout();
		lay.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search User");
		txtSearch.setDescription("Search by user name");
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

		content.addComponents(lay, tblUserList, tblUserList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblUserList = new TablePaged();
		tblUserList.addItemClickListener(event ->
		{
			if (event.isDoubleClick())
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblUserId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblUserList.addContainerProperty("User Id", Label.class, new Label(), null, null, Align.CENTER);

		tblUserList.addContainerProperty("Role Name", Label.class, new Label(), null, null, Align.CENTER);

		tblUserList.addContainerProperty("User Name", Label.class, new Label(), null, null, Align.CENTER);

		tblUserList.addContainerProperty("Full Name", Label.class, new Label(), null, null, Align.CENTER);

		tblUserList.addContainerProperty("Expiry Date", Label.class, new Label(), null, null, Align.CENTER);

		tblUserList.addContainerProperty("User Type", Label.class, new Label(), null, null, Align.CENTER);

		tblUserList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblUserList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblUserList.setColumnWidth("Action", 100);

		tblUserList.setColumnCollapsed("User Id", true);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblUserId.add(ar, new Label());
			tbLblUserId.get(ar).setWidth("100%");
			tbLblUserId.get(ar).setImmediate(true);
			tbLblUserId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblRoleName.add(ar, new Label());
			tbLblRoleName.get(ar).setWidth("100%");
			tbLblRoleName.get(ar).setImmediate(true);
			tbLblRoleName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblUserName.add(ar, new Label());
			tbLblUserName.get(ar).setWidth("100%");
			tbLblUserName.get(ar).setImmediate(true);
			tbLblUserName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblFullName.add(ar, new Label());
			tbLblFullName.get(ar).setWidth("100%");
			tbLblFullName.get(ar).setImmediate(true);
			tbLblFullName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblExpiryDate.add(ar, new Label());
			tbLblExpiryDate.get(ar).setWidth("100%");
			tbLblExpiryDate.get(ar).setImmediate(true);
			tbLblExpiryDate.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblUserType.add(ar, new Label());
			tbLblUserType.get(ar).setWidth("100%");
			tbLblUserType.get(ar).setImmediate(true);
			tbLblUserType.get(ar).setStyleName(ValoTheme.LABEL_TINY);

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
				String uId = tbLblUserId.get(ar).getValue().toString();
				if (!uId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					tbCmbAction.get(ar).setEnabled(false);
					String action = tbCmbAction.get(ar).getValue().toString();

					if (action.equals("Edit"))
					{ addEditWindow("Edit", uId, ar+""); }
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
									if (uig.activeInactiveData(uId, sessionBean.getUserId()))
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

			tblUserList.addItem(new Object[]{tbLblUserId.get(ar), tbLblRoleName.get(ar), tbLblUserName.get(ar),
					tbLblFullName.get(ar), tbLblExpiryDate.get(ar), tbLblUserType.get(ar), tbChkActive.get(ar),
					tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void loadTableInfo()
	{
		String branch = (sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin())?"%":sessionBean.getBranchId();
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
			String sql = " select ui.vUserId, uri.vRoleName, ui.vUserName, ui.vFullName, ui.dExpiryDate, ut.vUserTypeName,"+
					" ui.iActive from master.tbUserInfo ui, master.tbUserRoleInfo uri, master.tbUserType ut where ui.vRoleId"+
					" = uri.vRoleId and ut.iUserTypeId = ui.iUserTypeId and ui.vUserId != 'U1' and (ui.vUserName like '"+search+"'"+
					" or ui.vFullName like '"+search+"') and ui.vUserId != '"+sessionBean.getUserId()+"'and ui.iActive like"+
					" '"+status+"' and vBranchId like '"+branch+"' order by uri.vRoleName, ui.vUserName ";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblUserId.size() <= i)
				{ tableRowAdd(i); }
				tbLblUserId.get(i).setValue(element[0].toString());
				tbLblRoleName.get(i).setValue(element[1].toString());
				tbLblUserName.get(i).setValue(element[2].toString());
				tbLblFullName.get(i).setValue(element[3].toString());
				tbLblExpiryDate.get(i).setValue(cm.dfBd.format(element[4]));
				tbLblUserType.get(i).setValue(element[5].toString());
				tbChkActive.get(i).setValue((element[6].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);
				i++;
			}
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			tblUserList.nextPage();
			tblUserList.previousPage();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblUserList, tbLblUserId); }

	public void enter(ViewChangeEvent event)
	{ loadTableInfo(); }
}
