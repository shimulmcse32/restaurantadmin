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
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.ModifierSetGateway;
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
public class ModifierInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblModifierList;
	private ArrayList<Label> tbLblModifierId = new ArrayList<Label>();
	private ArrayList<Label> tbLblModifierName = new ArrayList<Label>();
	private ArrayList<Label> tbLblModifierItem = new ArrayList<Label>();
	private ArrayList<Label> tbLblTaggedItem = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;
	private Panel pnlTable;

	private TextField txtSearch;
	private OptionGroup ogFilter;

	private CommonMethod cm;
	private ModifierSetGateway msg = new ModifierSetGateway();

	public ModifierInformation(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		addComponents(cBtn, addPanel());//, addReport());

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
		{ loadTableInfo(); /*viewReport("");*/});

		ogFilter.addValueChangeListener(event ->
		{ loadTableInfo(); });

		txtSearch.addValueChangeListener(event ->
		{ loadTableInfo(); });
	}

	private void addEditWindow(String addEdit, String modifierId, String ar)
	{
		AddEditModifier win = new AddEditModifier(sessionBean, addEdit, modifierId, "");
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
		pnlTable = new Panel("Modifier's Set List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout lay = new HorizontalLayout();
		lay.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Item");
		txtSearch.setDescription("Search by item name");
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

		buildTable();

		content.addComponents(lay, tblModifierList, tblModifierList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblModifierList = new TablePaged();
		tblModifierList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblModifierId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblModifierList.addContainerProperty("Modifier Id", Label.class, new Label(), null, null, Align.CENTER);
		tblModifierList.setColumnCollapsed("Modifier Id", true);

		tblModifierList.addContainerProperty("Modifier Name", Label.class, new Label(), null, null, Align.CENTER);

		tblModifierList.addContainerProperty("Modifier Menus", Label.class, new Label(), null, null, Align.CENTER);

		tblModifierList.addContainerProperty("Tagged Menus", Label.class, new Label(), null, null, Align.CENTER);

		tblModifierList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Active", 80);

		tblModifierList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblModifierId.add(ar, new Label());
			tbLblModifierId.get(ar).setWidth("100%");
			tbLblModifierId.get(ar).setImmediate(true);
			tbLblModifierId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblModifierName.add(ar, new Label());
			tbLblModifierName.get(ar).setWidth("100%");
			tbLblModifierName.get(ar).setImmediate(true);
			tbLblModifierName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblModifierItem.add(ar, new Label());
			tbLblModifierItem.get(ar).setWidth("100%");
			tbLblModifierItem.get(ar).setImmediate(true);
			tbLblModifierItem.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblTaggedItem.add(ar, new Label());
			tbLblTaggedItem.get(ar).setWidth("100%");
			tbLblTaggedItem.get(ar).setImmediate(true);
			tbLblTaggedItem.get(ar).setStyleName(ValoTheme.LABEL_TINY);

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
				String modId = tbLblModifierId.get(ar).getValue().toString();
				if (!modId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					tbCmbAction.get(ar).setEnabled(false);
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ addEditWindow("Edit", modId, ar+""); }
					else if (tbCmbAction.get(ar).getValue().toString().equals("Active/Inactive"))
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
									if (msg.activeInactiveData(modId, sessionBean.getUserId()))
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
			tblModifierList.addItem(new Object[]{tbLblModifierId.get(ar), tbLblModifierName.get(ar), tbLblModifierItem.get(ar),
					tbLblTaggedItem.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void loadTableInfo()
	{
		String search = "%"+txtSearch.getValue().toString()+"%";
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
			String sql = "select distinct mm.vModifierId, mm.vModifierName, ISNULL(STUFF((SELECT ', ' + ic.vItemName FROM"+
					" master.tbFinishedItemInfo ic where ic.vItemId in (select fm.vItemId from master.tbFinishedModifier"+
					" fm where fm.vModifierId = mm.vModifierId) FOR XML PATH('')), 1, 1, ''), '') vTaggedMenus, ISNULL(STUFF"+
					" ((SELECT ', ' + ic.vItemName FROM master.tbFinishedItemInfo ic where ic.vItemId in (select fm.vItemId"+
					" from master.tbModifierMaster fm where fm.vModifierId = mm.vModifierId) FOR XML PATH('')), 1, 1, ''), '')"+
					" vModifierItems, mm.iActive from master.tbModifierMaster mm where vModifierName like '"+search+"'"+
					" and mm.iActive like '"+status+"' order by vModifierName";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblModifierId.size() <= i)
				{ tableRowAdd(i); }

				tbLblModifierId.get(i).setValue(element[0].toString());
				tbLblModifierName.get(i).setValue(element[1].toString());
				tbLblModifierItem.get(i).setValue(element[3].toString());
				tbLblTaggedItem.get(i).setValue(element[2].toString());
				tbChkActive.get(i).setValue((element[4].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);
				i++;
			}
			tblModifierList.nextPage();
			tblModifierList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblModifierList, tbLblModifierId); }

	public void enter(ViewChangeEvent event)
	{ loadTableInfo(); }
}
