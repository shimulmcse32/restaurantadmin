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
public class PaymentMethodInfo extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblPaymentList;
	private ArrayList<Label> tbLblPayId = new ArrayList<Label>();
	private ArrayList<Label> tbLblPayMethod = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;

	private TextField txtSearch;
	private OptionGroup ogActive;

	private CommonMethod cm;
	private ItemUnitGateway iug = new ItemUnitGateway();
	private String formId;

	public PaymentMethodInfo(SessionBean sessionBean, String formId)
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

		txtSearch.addValueChangeListener(event ->
		{ loadTableInfo(); });
	}

	private void addEditWindow(String addEdit, String payId, String ar)
	{
		AddEditPayMethod win = new AddEditPayMethod(sessionBean, addEdit, payId);
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
		Panel pnlTable = new Panel("Payment Method List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout lay = new HorizontalLayout();
		lay.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Payment Method");
		txtSearch.setDescription("Search by payment method");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		ogActive = new OptionGroup();
		ogActive.addItem("Active");
		ogActive.addItem("Inactive");
		ogActive.addItem("All");
		ogActive.select("Active");
		ogActive.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogActive.addStyleName(ValoTheme.OPTIONGROUP_SMALL);

		lay.addComponents(txtSearch, ogActive);

		buildTable();

		content.addComponents(lay, tblPaymentList, tblPaymentList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblPaymentList = new TablePaged();
		tblPaymentList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblPayId.get(ar).getValue().toString();
				if (!id.equals("1") && !id.equals("2"))
				{ addEditWindow("Edit", id, ar+""); }
			}
		});

		tblPaymentList.addContainerProperty("Pay Id", Label.class, new Label(), null, null, Align.CENTER);
		tblPaymentList.setColumnCollapsed("Pay Id", true);

		tblPaymentList.addContainerProperty("Payment Method(s)", Label.class, new Label(), null, null, Align.CENTER);

		tblPaymentList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);
		tblPaymentList.setColumnWidth("Active", 80);

		tblPaymentList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblPaymentList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblPayId.add(ar, new Label());
			tbLblPayId.get(ar).setWidth("100%");
			tbLblPayId.get(ar).setImmediate(true);
			tbLblPayId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblPayMethod.add(ar, new Label());
			tbLblPayMethod.get(ar).setWidth("100%");
			tbLblPayMethod.get(ar).setImmediate(true);
			tbLblPayMethod.get(ar).setStyleName(ValoTheme.LABEL_TINY);

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
				String payId = tbLblPayId.get(ar).getValue().toString();
				if (!payId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					tbCmbAction.get(ar).setEnabled(false);
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ addEditWindow("Edit", payId, ar+""); }
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
									if (iug.activeInactiveDataMethod(payId, sessionBean.getUserId()))
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
			tblPaymentList.addItem(new Object[]{tbLblPayId.get(ar), tbLblPayMethod.get(ar),
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

		tableClear();
		int i = 0;
		try
		{
			String sql = "select iMethodId, vMethodName, iActive from master.tbPaymentMethods"+
					" where vMethodName like '"+search+"' and iActive like '"+status+"' order"+
					" by vMethodName";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblPayId.size() <= i)
				{ tableRowAdd(i); }

				tbLblPayId.get(i).setValue(element[0].toString());
				tbLblPayMethod.get(i).setValue(element[1].toString());
				tbChkActive.get(i).setValue((element[2].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);

				if (element[0].toString().equals("1") || element[0].toString().equals("2"))
				{ tbCmbAction.get(i).setEnabled(false); }
				i++;
			}
			tblPaymentList.nextPage();
			tblPaymentList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblPaymentList, tbLblPayId); }

	public void enter(ViewChangeEvent event)
	{
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
		loadTableInfo();
	}
}
