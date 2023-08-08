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
import com.example.gateway.ItemCategoryGateway;
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
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class CategoryInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblCategoryList;
	private ArrayList<Label> tbLblCategoryId = new ArrayList<Label>();
	private ArrayList<Label> tbLblCategoryName = new ArrayList<Label>();
	private ArrayList<Label> tbLblCategoryDesc = new ArrayList<Label>();
	private ArrayList<Label> tbLblColor = new ArrayList<Label>();
	private ArrayList<Label> tbLblOnline = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;
	private Panel pnlTable;

	private TextField txtSearch;
	private OptionGroup ogFilter;

	private CommonMethod cm;
	private ItemCategoryGateway cig = new ItemCategoryGateway();

	public CategoryInformation(SessionBean sessionBean, String formId)
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

	private void addEditWindow(String addEdit, String categoryId, String ar)
	{
		AddEditItemCategory win = new AddEditItemCategory(sessionBean, addEdit, categoryId, "Menu");
		getUI().addWindow(win);
		win.center();
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(event ->
		{
			if(!ar.isEmpty())
			{ tbCmbAction.get(Integer.parseInt(ar)).setEnabled(true); }
			cBtn.btnNew.setEnabled(true);
			loadTableInfo();
		});
	}

	private Panel addPanel()
	{
		pnlTable = new Panel("Menu Category List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout lay = new HorizontalLayout();
		lay.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Category Name");
		txtSearch.setDescription("Search by category name");
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

		content.addComponents(lay, tblCategoryList, tblCategoryList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblCategoryList = new TablePaged();
		tblCategoryList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblCategoryId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblCategoryList.addContainerProperty("Category Id", Label.class, new Label(), null, null, Align.CENTER);
		tblCategoryList.setColumnCollapsed("Category Id", true);

		tblCategoryList.addContainerProperty("Category Name", Label.class, new Label(), null, null, Align.CENTER);

		tblCategoryList.addContainerProperty("Category Description", Label.class, new Label(), null, null, Align.CENTER);

		tblCategoryList.addContainerProperty("Color", Label.class, new Label(), null, null, Align.CENTER);

		tblCategoryList.addContainerProperty("Show Online", Label.class, new Label(), null, null, Align.CENTER);

		tblCategoryList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblCategoryList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblCategoryList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblCategoryId.add(ar, new Label());
			tbLblCategoryId.get(ar).setWidth("100%");
			tbLblCategoryId.get(ar).setImmediate(true);
			tbLblCategoryId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblCategoryName.add(ar, new Label());
			tbLblCategoryName.get(ar).setWidth("100%");
			tbLblCategoryName.get(ar).setImmediate(true);
			tbLblCategoryName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblCategoryDesc.add(ar, new Label());
			tbLblCategoryDesc.get(ar).setWidth("100%");
			tbLblCategoryDesc.get(ar).setImmediate(true);
			tbLblCategoryDesc.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblColor.add(ar, new Label("", ContentMode.HTML));
			tbLblColor.get(ar).setWidth("100%");
			tbLblColor.get(ar).setImmediate(true);
			tbLblColor.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblOnline.add(ar, new Label());
			tbLblOnline.get(ar).setWidth("100%");
			tbLblOnline.get(ar).setImmediate(true);
			tbLblOnline.get(ar).setStyleName(ValoTheme.CHECKBOX_SMALL);

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
			if(cm.update)
			{
				tbCmbAction.get(ar).addItem("Active/Inactive");
				tbCmbAction.get(ar).setItemIcon("Active/Inactive", FontAwesome.LOCK);

				tbCmbAction.get(ar).addItem("Edit");
				tbCmbAction.get(ar).setItemIcon("Edit", FontAwesome.EDIT);
			}
			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String id = tbLblCategoryId.get(ar).getValue();
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
									if (cig.activeInactiveData(id, sessionBean.getUserId()))
									{
										tbChkActive.get(ar).setValue(!tbChkActive.get(ar).getValue().booleanValue());
										cm.showNotification("success", "Successfull!", "All information updated successfully.");
										tbCmbAction.get(ar).setEnabled(true);
										loadTableInfo();
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
			tblCategoryList.addItem(new Object[]{tbLblCategoryId.get(ar), tbLblCategoryName.get(ar), tbLblCategoryDesc.get(ar),
					tbLblColor.get(ar), tbLblOnline.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
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
		String type = "Menu";

		tableClear();
		int i = 0;
		try
		{
			String sql = "select vCategoryId, vCategoryName, vCategoryDescription, vCategoryColor, iActive,"+
					" ISNULL(iShowOnline, 0)iShowOnline from master.tbItemCategory where vCategoryType = '"+type+"'"+
					" and vCategoryName like '"+search+"' and iActive like '"+status+"' order by vCategoryName";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if(tbLblCategoryId.size() <= i)
				{ tableRowAdd(i); }

				tbLblCategoryId.get(i).setValue(element[0].toString());
				tbLblCategoryName.get(i).setValue(element[1].toString());
				tbLblCategoryDesc.get(i).setValue(element[2].toString());
				tbLblOnline.get(i).setValue(element[5].toString().equals("0")?"No":"Yes");
				if (!element[3].toString().isEmpty())
				{
					tbLblColor.get(i).setValue("<span style='color:"+element[3].toString()+"; font-weight: bold;'>Color</span>");
					//tbLblColor.get(i).setIcon(new ThemeResource("../dashboard/colors/"+element[3].toString()));
				}
				tbChkActive.get(i).setValue((element[4].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);

				i++;
			}
			tblCategoryList.nextPage();
			tblCategoryList.previousPage();
			if(i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
		}
		catch (Exception e)
		{ System.out.println("Table Query "+e); }
	}

	private void tableClear()
	{ cm.tableClear(tblCategoryList, tbLblCategoryId); }

	public void enter(ViewChangeEvent event)
	{ loadTableInfo(); }
}
