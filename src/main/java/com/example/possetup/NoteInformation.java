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
import com.example.gateway.ItemUnitGateway;
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
public class NoteInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblNoteList;
	private ArrayList<Label> tbLblNoteId = new ArrayList<Label>();
	private ArrayList<Label> tbLblNoteType = new ArrayList<Label>();
	private ArrayList<Label> tbLblNoteDetails = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;

	private TextField txtSearch;
	private OptionGroup ogActive, ogType;

	private CommonMethod cm;
	private ItemUnitGateway iug = new ItemUnitGateway();
	private String formId;

	public NoteInformation(SessionBean sessionBean, String formId)
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

		cBtn.btnRefresh.addClickListener(event ->
		{ loadTableInfo(); });

		ogActive.addValueChangeListener(event ->
		{ loadTableInfo(); });

		ogType.addValueChangeListener(event ->
		{ loadTableInfo(); });

		txtSearch.addValueChangeListener(event ->
		{ loadTableInfo(); });
	}

	private void addEditWindow(String addEdit, String noteId, String ar)
	{
		AddEditNoteInfo win = new AddEditNoteInfo(sessionBean, addEdit, noteId);
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
		Panel pnlTable = new Panel("Note List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout lay = new HorizontalLayout();
		lay.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Note");
		txtSearch.setDescription("Search by note or details");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		ogType = new OptionGroup();
		ogType.addItem("Kitchen");
		ogType.addItem("Invoice");
		ogType.addItem("All");
		ogType.select("Kitchen");
		ogType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);

		ogActive = new OptionGroup();
		ogActive.addItem("Active");
		ogActive.addItem("Inactive");
		ogActive.addItem("All");
		ogActive.select("Active");
		ogActive.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogActive.addStyleName(ValoTheme.OPTIONGROUP_SMALL);

		lay.addComponents(txtSearch, ogType, ogActive);

		buildTable();

		content.addComponents(lay, tblNoteList, tblNoteList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblNoteList = new TablePaged();
		tblNoteList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblNoteId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblNoteList.addContainerProperty("Note Id", Label.class, new Label(), null, null, Align.CENTER);
		tblNoteList.setColumnCollapsed("Note Id", true);

		tblNoteList.addContainerProperty("Note Type", Label.class, new Label(), null, null, Align.CENTER);

		tblNoteList.addContainerProperty("Note Details", Label.class, new Label(), null, null, Align.CENTER);

		tblNoteList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);
		tblNoteList.setColumnWidth("Active", 80);

		tblNoteList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblNoteList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblNoteId.add(ar, new Label());
			tbLblNoteId.get(ar).setWidth("100%");
			tbLblNoteId.get(ar).setImmediate(true);
			tbLblNoteId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblNoteType.add(ar, new Label());
			tbLblNoteType.get(ar).setWidth("100%");
			tbLblNoteType.get(ar).setImmediate(true);
			tbLblNoteType.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblNoteDetails.add(ar, new Label());
			tbLblNoteDetails.get(ar).setWidth("100%");
			tbLblNoteDetails.get(ar).setImmediate(true);
			tbLblNoteDetails.get(ar).setStyleName(ValoTheme.LABEL_TINY);

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
				String noteId = tbLblNoteId.get(ar).getValue().toString();
				if (!noteId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					tbCmbAction.get(ar).setEnabled(false);
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ addEditWindow("Edit", noteId, ar+""); }
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
									if (iug.activeInactiveDataNote(noteId, sessionBean.getUserId()))
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
			tblNoteList.addItem(new Object[]{tbLblNoteId.get(ar), tbLblNoteType.get(ar), tbLblNoteDetails.get(ar),
					tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void loadTableInfo()
	{
		String search = "%"+txtSearch.getValue().toString()+"%";
		String status = "";
		if (ogActive.getValue().toString().equals("Active"))
		{ status = "1"; }
		else if (ogActive.getValue().toString().equals("Inactive"))
		{ status = "0"; }
		else if (ogActive.getValue().toString().equals("All"))
		{ status = "%"; }

		String type = ogType.getValue().toString().equals("All")?"%":ogType.getValue().toString();

		tableClear();
		int i = 0;
		try
		{
			String sql = "select vNoteId, vNoteType, vNoteDetails, iActive from master.tbNoteInformation"+
					" where (vNoteDetails like '"+search+"' or vNoteType like '"+search+"') and iActive"+
					" like '"+status+"' and vNoteType like '"+type+"' order by vNoteType, vNoteDetails";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblNoteId.size() <= i)
				{ tableRowAdd(i); }

				tbLblNoteId.get(i).setValue(element[0].toString());
				tbLblNoteType.get(i).setValue(element[1].toString());
				tbLblNoteDetails.get(i).setValue(element[2].toString());
				tbChkActive.get(i).setValue((element[3].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);
				i++;
			}
			tblNoteList.nextPage();
			tblNoteList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblNoteList, tbLblNoteId); }

	public void enter(ViewChangeEvent event)
	{
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
		loadTableInfo();
	}
}
