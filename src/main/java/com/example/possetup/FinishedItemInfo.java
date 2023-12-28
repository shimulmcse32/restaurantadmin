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
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MultiComboBox;
import com.common.share.ReportViewer;
import com.example.gateway.ItemInfoGateway;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
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
public class FinishedItemInfo extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblMenuList;
	private ArrayList<Label> tbLblItemId = new ArrayList<Label>();
	private ArrayList<Label> tbLblItemName = new ArrayList<Label>();
	private ArrayList<Label> tbLblCategoryName = new ArrayList<Label>();
	private ArrayList<Label> tbLblUnits = new ArrayList<Label>();
	private ArrayList<Label> tbLblPrices = new ArrayList<Label>();
	private ArrayList<Label> tbLblVatDetails = new ArrayList<Label>();
	private ArrayList<Label> tbLblModifier = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;

	private TextField txtSearch;
	private OptionGroup ogItemType, ogFilter;
	private ComboBox cmbCategorySearch;

	private CommonMethod cm;
	private ItemInfoGateway iig = new ItemInfoGateway();
	private String formId;

	//Information Report
	private ComboBox cmbCategoryInfo, cmbVatCatInfo;
	private CommonButton cBtnInfo = new CommonButton("", "", "", "", "", "", "", "View", "");
	private OptionGroup ogMenuStausInfo, ogReportFormatInfo, ogMenuType;
	private CheckBox chkInventory;

	//Information Recipe
	private MultiComboBox cmbMenuRecipe;
	private CommonButton cBtnRecipe = new CommonButton("", "", "", "", "", "", "", "View", "");
	private OptionGroup ogReportTypeRecipe;

	//Information Recipe(Details)
	private MultiComboBox cmbCategoryInfoDe, cmbFinishedItemDe;
	private CommonButton cBtnInfoDe = new CommonButton("", "", "", "", "", "", "", "View", "");

	public FinishedItemInfo(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		this.formId = formId;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		addComponents(cBtn, addPanel(), addReportInfo(), addReportRecipe(), addReportReciped());

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

		cmbCategorySearch.addValueChangeListener(event -> loadTableInfo());

		ogItemType.addValueChangeListener(event -> loadTableInfo());

		ogFilter.addValueChangeListener(event -> loadTableInfo());

		cBtnInfo.btnPreview.addClickListener(event -> addValidationInfo());

		cBtnRecipe.btnPreview.addClickListener(event -> addValidationRecipe());

		cBtnInfoDe.btnPreview.addClickListener(event -> addValidationInfoDe());

		cmbCategoryInfoDe.addValueChangeListener(event ->
		{
			String catId = cm.getMultiComboValue(cmbCategoryInfoDe);
			loadItemDe(catId);
		});
	}

	private void addEditWindow(String addEdit, String itemId, String ar)
	{
		AddEditFinishedItem win = new AddEditFinishedItem(sessionBean, addEdit, itemId);
		getUI().addWindow(win);
		win.center();
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event ->
		{
			if (!ar.isEmpty())
			{ tbCmbAction.get(Integer.parseInt(ar)).setEnabled(true); }
			cBtn.btnNew.setEnabled(true);
			resetData();
		});
	}

	private Panel addPanel()
	{
		Panel pnlTable = new Panel("Menu List :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout lay = new HorizontalLayout();
		lay.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search Menu");
		txtSearch.setDescription("Search by menu name");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		ogItemType = new OptionGroup();
		ogItemType.addItem("Menu");
		ogItemType.addItem("Modifier");
		ogItemType.addItem("Both");
		ogItemType.select("Menu");
		ogItemType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		ogItemType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogItemType.setDescription("Menu Type");

		ogFilter = new OptionGroup();
		ogFilter.addItem("Active");
		ogFilter.addItem("Inactive");
		ogFilter.addItem("All");
		ogFilter.select("Active");
		ogFilter.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogFilter.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		ogFilter.setDescription("Menu Status");

		cmbCategorySearch = new ComboBox();
		cmbCategorySearch.setImmediate(true);
		cmbCategorySearch.setWidth("160px");
		cmbCategorySearch.setInputPrompt("Select Category");
		cmbCategorySearch.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbCategorySearch.setFilteringMode(FilteringMode.CONTAINS);

		lay.addComponents(txtSearch, cmbCategorySearch, ogItemType, ogFilter);

		buildTable();

		content.addComponents(lay, tblMenuList, tblMenuList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblMenuList = new TablePaged();
		tblMenuList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblItemId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblMenuList.addContainerProperty("Menu Id", Label.class, new Label(), null, null, Align.CENTER);
		tblMenuList.setColumnCollapsed("Menu Id", true);

		tblMenuList.addContainerProperty("Menu Name", Label.class, new Label(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("Category", Label.class, new Label(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("VAT Details", Label.class, new Label(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("Unit(s)", Label.class, new Label(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("Price(s)", Label.class, new Label(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("Modifier", Label.class, new Label(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblMenuList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblItemId.add(ar, new Label());
			tbLblItemId.get(ar).setWidth("100%");
			tbLblItemId.get(ar).setImmediate(true);
			tbLblItemId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblItemName.add(ar, new Label());
			tbLblItemName.get(ar).setWidth("100%");
			tbLblItemName.get(ar).setImmediate(true);
			tbLblItemName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblCategoryName.add(ar, new Label());
			tbLblCategoryName.get(ar).setWidth("100%");
			tbLblCategoryName.get(ar).setImmediate(true);
			tbLblCategoryName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblVatDetails.add(ar, new Label());
			tbLblVatDetails.get(ar).setWidth("100%");
			tbLblVatDetails.get(ar).setImmediate(true);
			tbLblVatDetails.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblUnits.add(ar, new Label());
			tbLblUnits.get(ar).setWidth("100%");
			tbLblUnits.get(ar).setImmediate(true);
			tbLblUnits.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblPrices.add(ar, new Label());
			tbLblPrices.get(ar).setWidth("100%");
			tbLblPrices.get(ar).setImmediate(true);
			tbLblPrices.get(ar).setDescription("Price Including VAT");
			tbLblPrices.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblModifier.add(ar, new Label());
			tbLblModifier.get(ar).setWidth("100%");
			tbLblModifier.get(ar).setImmediate(true);
			tbLblModifier.get(ar).setStyleName(ValoTheme.LABEL_TINY);

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
				String itemId = tbLblItemId.get(ar).getValue().toString();
				if (!itemId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					tbCmbAction.get(ar).setEnabled(false);
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ addEditWindow("Edit", itemId, ar+""); }
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
									if (iig.activeInactiveData(itemId, sessionBean.getUserId()))
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
			tblMenuList.addItem(new Object[]{tbLblItemId.get(ar), tbLblItemName.get(ar), tbLblCategoryName.get(ar),
					tbLblVatDetails.get(ar), tbLblUnits.get(ar), tbLblPrices.get(ar), tbLblModifier.get(ar),
					tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void loadTableInfo()
	{
		String itemName = "%"+txtSearch.getValue().toString()+"%";
		String cat = cmbCategorySearch.getValue() != null? cmbCategorySearch.getValue().toString():"%";
		String status = "";
		if (ogFilter.getValue().toString().equals("Active"))
		{ status = "1"; }
		if (ogFilter.getValue().toString().equals("Inactive"))
		{ status = "0"; }
		if (ogFilter.getValue().toString().equals("All"))
		{ status = "%"; }
		String type = ogItemType.getValue().toString();

		tableClear();
		int i = 0;
		try
		{
			String sql = "select vItemId, vItemName, convert(varchar(500), (SELECT top 1 STUFF((SELECT ', '+ic.vCategoryName"+
					" FROM master.tbItemCategory ic where ic.vCategoryId in (select rtrim(ltrim(Item)) Item from"+
					" dbo.Split(im.vCategoryId)) FOR XML PATH('')), 1, 1, '') FROM master.tbItemCategory ic)) vCategoryName,"+
					" convert(varchar(500), (SELECT top 1 STUFF((SELECT ', '+st.vSalesType FROM master.tbSalesType st where"+
					" st.iSalesTypeId in (select rtrim(ltrim(Item)) Item from dbo.Split(im.vSalesTypeIds)) FOR XML PATH('')),"+
					" 1, 1, '') FROM master.tbItemCategory ic)) vSalesType, vc.vVatCatName, vVatOption, convert(varchar(500),"+
					" (SELECT top 1 STUFF((SELECT ', '+uno.vUnitName FROM master.tbUnitInfo uno where iUnitId in(select"+
					" rtrim(ltrim(Item)) Item from dbo.Split(im.vUnitIds)) FOR XML PATH('')), 1, 1, '') FROM master.tbUnitInfo"+
					" un)) vUnitNames, im.iActive, (rtrim(ltrim(STUFF((SELECT ', '+convert(varchar(10), ip.mMainPrice + (case"+
					" when im.vVatOption = 'Exclusive' then (ip.mMainPrice * vc.mPercentage) / 100 else 0 end)) FROM"+
					" master.tbFinishedItemPrice ip where ip.vItemId = im.vItemId and ip.iActive = 1 order by iUnitId FOR XML"+
					" PATH('')), 1, 1, '')))) mPrices, ISNULL((select distinct vModifierName from master.tbModifierMaster"+
					" mm where mm.vModifierId = im.vModifierId), '') vModifier from master.tbFinishedItemInfo im,"+
					" master.tbVatCatMaster vc where im.vVatCatId = vc.vVatCatId and (vItemName like '"+itemName+"'"+
					" or vBarcode like '"+itemName+"') and im.iActive like '"+status+"' and vItemType = '"+type+"'"+
					" and im.vCategoryId like '"+cat+"' order by im.vItemName";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblItemId.size() <= i)
				{ tableRowAdd(i); }

				tbLblItemId.get(i).setValue(element[0].toString());
				tbLblItemName.get(i).setValue(element[1].toString());
				tbLblCategoryName.get(i).setValue(element[2].toString());
				tbLblVatDetails.get(i).setValue(element[4].toString()+" ("+element[5].toString()+")");
				tbLblUnits.get(i).setValue(element[6].toString());
				tbLblPrices.get(i).setValue(element[8].toString());
				tbLblModifier.get(i).setValue(element[9].toString());
				tbChkActive.get(i).setValue((element[7].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);

				i++;
			}
			tblMenuList.nextPage();
			tblMenuList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblMenuList, tbLblItemId); }

	//Finished Menu Report
	private Panel addReportInfo()
	{
		Panel panelInfo = new Panel("Menu Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(3, 7);
		lay.setSpacing(true);

		cmbCategoryInfo = new ComboBox();
		cmbCategoryInfo.setNullSelectionAllowed(false);
		cmbCategoryInfo.addItem("%");
		cmbCategoryInfo.setItemCaption("%", "ALL");
		cmbCategoryInfo.setWidth("350px");
		cmbCategoryInfo.setInputPrompt("Select Category");
		cmbCategoryInfo.setRequired(true);
		cmbCategoryInfo.setRequiredError("This field is required.");
		cmbCategoryInfo.setStyleName(ValoTheme.COMBOBOX_TINY);
		Label lbl = new Label("Category Name: ");
		lbl.setWidth("-1px");
		lay.addComponent(lbl, 0, 0);
		lay.addComponent(cmbCategoryInfo, 1, 0);

		cmbVatCatInfo = new ComboBox();
		cmbVatCatInfo.setNullSelectionAllowed(false);
		cmbVatCatInfo.addItem("%");
		cmbVatCatInfo.setItemCaption("%", "ALL");
		cmbVatCatInfo.setWidth("250px");
		cmbVatCatInfo.setInputPrompt("Select VAT Category");
		cmbVatCatInfo.setImmediate(true);
		cmbVatCatInfo.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbVatCatInfo.setFilteringMode(FilteringMode.CONTAINS);
		cmbVatCatInfo.setRequired(true);
		cmbVatCatInfo.setRequiredError("This field is required.");
		lay.addComponent(new Label("VAT Category: "), 0, 1);
		lay.addComponent(cmbVatCatInfo, 1, 1);

		ogMenuStausInfo = new OptionGroup();
		ogMenuStausInfo.addItem("All");
		ogMenuStausInfo.addItem("Active");
		ogMenuStausInfo.addItem("Inactive");
		ogMenuStausInfo.select("All");
		ogMenuStausInfo.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogMenuStausInfo.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Menu Status: "), 0, 2);
		lay.addComponent(ogMenuStausInfo, 1, 2);

		ogMenuType = new OptionGroup();
		ogMenuType.addItem("All");
		ogMenuType.addItem("Online Menu");
		ogMenuType.addItem("Offline Menu");
		ogMenuType.select("All");
		ogMenuType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogMenuType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Menu Type: "), 0, 3);
		lay.addComponent(ogMenuType, 1, 3);

		chkInventory = new CheckBox("Inventory");
		chkInventory.setImmediate(true);
		chkInventory.setValue(true);
		chkInventory.addStyleName(ValoTheme.CHECKBOX_SMALL);
		chkInventory.setDescription("Save as an inventory.");
		lay.addComponent(chkInventory, 1, 4);

		ogReportFormatInfo = new OptionGroup();
		ogReportFormatInfo.addItem("PDF");
		ogReportFormatInfo.addItem("XLS");
		ogReportFormatInfo.select("PDF");
		ogReportFormatInfo.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormatInfo.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogReportFormatInfo, 1, 5);

		lay.addComponent(cBtnInfo, 0, 6, 1, 6);
		lay.setComponentAlignment(cBtnInfo, Alignment.MIDDLE_CENTER);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelInfo.setContent(content);

		loadCategory();

		return panelInfo;
	}

	private void loadCategory()
	{
		String sqlC = "select vCategoryId, vCategoryName from master.tbItemCategory"+
				" where vCategoryType = 'Menu' order by vCategoryName";
		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbCategoryInfo.addItem(element[0].toString());
			cmbCategoryInfo.setItemCaption(element[0].toString(), element[1].toString());

			cmbCategorySearch.addItem(element[0].toString());
			cmbCategorySearch.setItemCaption(element[0].toString(), element[1].toString());
		}

		String sqlV = "select vVatCatId, vVatCatName from master.tbVatCatMaster where iActive = 1";
		for (Iterator<?> iter = cm.selectSql(sqlV).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbVatCatInfo.addItem(element[0].toString());
			cmbVatCatInfo.setItemCaption(element[0].toString(), element[1].toString());
		}
		cmbCategoryInfo.select("%");
		cmbVatCatInfo.select("%");
	}

	private void addValidationInfo()
	{
		if (cmbCategoryInfo.getValue() != null)
		{
			if (cmbVatCatInfo.getValue() != null)
			{ viewReportInfo(); }
			else
			{
				cmbVatCatInfo.focus();
				cm.showNotification("warning", "Warning!", "Select VAT category.");
			}
		}
		else
		{
			cmbCategoryInfo.focus();
			cm.showNotification("warning", "Warning!", "Select category.");
		}
	}

	@SuppressWarnings("deprecation")
	public void viewReportInfo()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", xsql = "", fileName = "";

		String status = ogMenuStausInfo.getValue().toString();
		String stat = "0";
		if (status.equals("All")) {stat = "%";}
		else if (status.equals("Active")) {stat = "1";}

		String catId = cmbCategoryInfo.getValue().toString();
		String vatId = cmbVatCatInfo.getValue().toString();
		String onOff = ogMenuType.getValue().toString();
		String chkIn = chkInventory.getValue().booleanValue()? "1":"%";
		String menuType = "%";
		if (onOff.equals("Online Menu")) { menuType = "1"; }
		else if (onOff.equals("Offline Menu")) { menuType = "0"; }
		try
		{
			sql = "select ic.vCategoryName, fin.vItemName, vc.vVatCatName, fin.vVatOption, uni.vUnitName, fip.mMainPrice, ISNULL(fip.mFixedCost, 0)mFixedCost,"+
					" (SELECT STUFF((SELECT ', '+sat.vSalesType FROM master.tbSalesType sat WHERE sat.iSalesTypeId in (select Item from"+
					" dbo.Split(fin.vSalesTypeIds)) FOR XML PATH ('')), 1, 2, '')) vSalesType from master.tbFinishedItemInfo fin,"+
					" master.tbFinishedItemPrice fip, master.tbItemCategory ic, master.tbVatCatMaster vc, master.tbUnitInfo uni where"+
					" fin.vItemId = fip.vItemId and fin.iActive = 1 and fip.iActive  = 1 and fin.vCategoryId = ic.vCategoryId and"+
					" vc.vVatCatId = fin.vVatCatId and fip.iUnitId = uni.iUnitId and fin.vCategoryId like '"+catId+"' and fin.iActive like '"+stat+"'"+
					" and fin.vVatCatId like '"+vatId+"'and fin.iOnlineMenu like '"+menuType+"' and fin.iInventory like '"+chkIn+"' order by"+
					" ic.vCategoryName, fin.vItemName";
			System.out.println(sql);
			reportSource = "com/jasper/possetup/rptMenuInformation.jasper";

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			if (ogReportFormatInfo.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("logo", sessionBean.getCompanyLogo());
				hm.put("userIp", sessionBean.getUserIp());
				hm.put("onOffMenu", onOff);

				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = "select ic.vCategoryName, fii.vItemType, fii.vItemName, vc.vVatCatName+' - '+SUBSTRING(fii.vVatOption, 0, 4)"+
						" vVatDetails, case when fii.iActive = 0 then 'Inactive' else 'Active' end iStatus, ISNULL(CONVERT(varchar(2000),"+
						" (SELECT STUFF((SELECT ', '+sup.vSupplierName FROM master.tbSupplierMaster sup WHERE sup.vSupplierId in"+
						" (select Item from dbo.Split(fii.vSupplierIds)) FOR XML PATH ('')), 1, 2, ''))), '') vSupplierDetails,"+
						" ISNULL(CONVERT(varchar(2000), (SELECT STUFF((SELECT ', '+sat.vSalesType FROM master.tbSalesType sat WHERE"+
						" sat.iSalesTypeId in (select Item from dbo.Split(fii.vSalesTypeIds)) FOR XML PATH ('')), 1, 2, ''))), '')"+
						" vSalesType, ISNULL(CONVERT(varchar(2000), (SELECT STUFF((SELECT ', '+ico.vCompanyName FROM"+
						" master.tbItemCompanyMaster ico WHERE ico.vCompanyId in (select Item from dbo.Split(fii.vCategoryId)) FOR XML PATH ('')),"+
						" 1, 2, ''))), '') vItemCompanyName, ISNULL(CONVERT(varchar(2000), (SELECT STUFF((SELECT ', '+uni.vUnitName FROM"+
						" master.tbUnitInfo uni WHERE uni.iUnitId in (select Item from dbo.Split(fii.vUnitIds)) order by iUnitId asc FOR XML PATH ('')),"+
						" 1, 2, ''))), '') vUnitName, ISNULL(CONVERT(varchar(2000), (SELECT STUFF((SELECT ', '+convert(varchar(10), fip.mMainPrice) FROM"+
						" master.tbFinishedItemPrice fip WHERE fip.iActive = 1 and fip.vItemId = fii.vItemId and fip.iUnitId in (select Item from"+
						" dbo.Split(fii.vUnitIds)) order by iUnitId asc FOR XML PATH ('')), 1, 2, ''))), '') vUnitPrice from master.tbFinishedItemInfo"+
						" fii, master.tbItemCategory ic, master.tbVatCatMaster vc where fii.vCategoryId = ic.vCategoryId and fii.vVatCatId = vc.vVatCatId"+
						" and fii.vCategoryId like '"+catId+"' and fii.iActive like '"+stat+"' and fii.vVatCatId like '"+vatId+"'"+
						" order by ic.vCategoryName, fii.vItemType, fii.vItemName";
				fileName = "Menu_Information_";
				//System.out.println(xsql);
				hm.put("parameters", "");
				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	//Report Recipe
	private Panel addReportRecipe()
	{
		Panel panelRecipe = new Panel("Menu Recipe :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(3, 3);
		lay.setSpacing(true);

		cmbMenuRecipe = new MultiComboBox();
		cmbMenuRecipe.setWidth("450px");
		cmbMenuRecipe.setInputPrompt("Select Menu(s)");
		cmbMenuRecipe.setRequired(true);
		cmbMenuRecipe.setRequiredError("This field is required.");
		Label lbl = new Label("Menu Name: ");
		lbl.setWidth("-1px");
		lay.addComponent(lbl, 0, 0);
		lay.addComponent(cmbMenuRecipe, 1, 0);

		ogReportTypeRecipe = new OptionGroup();
		ogReportTypeRecipe.addItem("Recipe");
		ogReportTypeRecipe.addItem("Modifier");
		ogReportTypeRecipe.select("Recipe");
		ogReportTypeRecipe.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportTypeRecipe.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Report Type: "), 0, 1);
		lay.addComponent(ogReportTypeRecipe, 1, 1);

		lay.addComponent(cBtnRecipe, 0, 2, 1, 2);
		lay.setComponentAlignment(cBtnRecipe, Alignment.MIDDLE_CENTER);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelRecipe.setContent(content);
		loadMenuName();

		return panelRecipe;
	}

	private void loadMenuName()
	{
		String sqlC = "select vItemId, vItemName from master.tbFinishedItemInfo"+
				" order by vItemName";
		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbMenuRecipe.addItem(element[0].toString());
			cmbMenuRecipe.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void addValidationRecipe()
	{
		if (!cmbMenuRecipe.getValue().toString().replace("[", "").replace("]", "").isEmpty())
		{ viewReportRecipe(); }
		else
		{
			cmbMenuRecipe.focus();
			cm.showNotification("warning", "Warning!", "Select Menu.");
		}
	}

	public void viewReportRecipe()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "";//, xsql = "", fileName = "";

		String menuIds = cmbMenuRecipe.getValue().toString().replace("[", "").replace("]", "");
		try
		{
			if (ogReportTypeRecipe.getValue().toString().equals("Recipe"))
			{
				sql = "select fii.vItemName vMainItem, ISNULL((select itc.vCategoryName from master.tbItemCategory itc"+
						" where itc.vCategoryId = fii.vCategoryId), '') vMenuCategory, ISNULL(CONVERT(varchar(2000),"+
						" (SELECT STUFF((SELECT ', '+uni.vUnitName FROM master.tbUnitInfo uni WHERE uni.iUnitId in"+
						" (select Item from dbo.Split(fii.vUnitIds)) order by iUnitId asc FOR XML PATH ('')),"+
						" 1, 2, ''))), '') vUnitName, ISNULL(CONVERT(varchar(2000), (SELECT STUFF((SELECT ', '+convert(varchar(10),"+
						" fip.mMainPrice) FROM master.tbFinishedItemPrice fip WHERE fip.iActive = 1 and fip.vItemId"+
						" = fii.vItemId and fip.iUnitId in (select Item from dbo.Split(fii.vUnitIds)) order by iUnitId"+
						" asc FOR XML PATH ('')), 1, 2, ''))), '') vUnitPrice, rii.vItemCode+' - '+rii.vItemName vRawItem,"+
						" ISNULL((select uni.vUnitName from master.tbUnitInfo uni where uni.iUnitId = rii.vUnitId), '') vRawUnit,"+
						" ISNULL((select uni.vUnitName from master.tbUnitInfo uni where uni.iUnitId = fr.iUnitId1), '') vUnitName1, fr.mUnitQty1,"+
						" ISNULL((select uni.vUnitName from master.tbUnitInfo uni where uni.iUnitId = fr.iUnitId2), '') vUnitName2, fr.mUnitQty2,"+
						" ISNULL((select uni.vUnitName from master.tbUnitInfo uni where uni.iUnitId = fr.iUnitId3), '') vUnitName3, fr.mUnitQty3,"+
						" ISNULL((select uni.vUnitName from master.tbUnitInfo uni where uni.iUnitId = fr.iUnitId4), '') vUnitName4, fr.mUnitQty4"+
						" from master.tbFinishedItemInfo fii, master.tbFinishedReceipe fr, master.tbRawItemInfo rii"+
						" where fii.vItemId = fr.vItemId and fr.vItemIdReceipe = rii.vItemId and fii.vItemId in (select"+
						" Item from dbo.Split('"+menuIds+"')) order by vMainItem";
				System.out.println(sql);
				reportSource = "com/jasper/possetup/rptMenuReceipeInformation.jasper";
			}
			else
			{
				sql = "select fin.vItemName vMainItem, ISNULL((select itc.vCategoryName from master.tbItemCategory itc"+
						" where itc.vCategoryId = fin.vCategoryId), '') vMenuCategory, ISNULL(CONVERT(varchar(2000),"+
						" (SELECT STUFF((SELECT ', '+uni.vUnitName FROM master.tbUnitInfo uni WHERE uni.iUnitId in"+
						" (select Item from dbo.Split(fin.vUnitIds)) order by iUnitId asc FOR XML PATH ('')), 1, 2, ''))),"+
						" '') vUnitName, ISNULL(CONVERT(varchar(2000), (SELECT STUFF((SELECT ', '+convert(varchar(10),"+
						" fip.mMainPrice) FROM master.tbFinishedItemPrice fip WHERE fip.iActive = 1 and fip.vItemId"+
						" = fin.vItemId and fip.iUnitId in (select Item from dbo.Split(fin.vUnitIds)) order by iUnitId"+
						" asc FOR XML PATH ('')), 1, 2, ''))), '') vUnitPrice, fii.vItemName vModifierName, uni.vUnitName"+
						" vModiUnitName, vc.vVatCatName+' - '+SUBSTRING(fii.vVatOption, 0, 4) vVatDetails, fim.mQuantity,"+
						" fim.mMainPrice, (fim.mQuantity * fim.mMainPrice) mAmount from master.tbFinishedItemInfo fin,"+
						" master.tbFinishedModifier fim, master.tbFinishedItemInfo fii, master.tbFinishedItemInfo fiim,"+
						" master.tbVatCatMaster vc, master.tbUnitInfo uni where fin.vItemId = fim.vItemId and fin.vModifierId"+
						" = fim.vModifierId and fim.vItemIdModifier = fiim.vItemId and fii.vVatCatId = vc.vVatCatId and"+
						" fii.vUnitIds = convert(varchar(50), uni.iUnitId) and fim.iActive = 1 and fim.vItemIdModifier ="+
						" fii.vItemId and fin.vItemId in (select Item from dbo.Split('"+menuIds+"')) order by vMainItem";
				//System.out.println(sql);
				reportSource = "com/jasper/possetup/rptMenuModifierInformation.jasper";
			}

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			//if (ogReportFormatInfo.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("logo", sessionBean.getCompanyLogo());
				hm.put("userIp", sessionBean.getUserIp());

				new ReportViewer(hm, reportSource);
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	//Menu Recipe Report
	private Panel addReportReciped()
	{
		Panel panelInfoDe = new Panel("Menu Recipe Details :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(3, 3);
		lay.setSpacing(true);

		cmbCategoryInfoDe = new MultiComboBox();
		cmbCategoryInfoDe.setWidth("450px");
		cmbCategoryInfoDe.setInputPrompt("Select Category");
		cmbCategoryInfoDe.setRequired(true);
		cmbCategoryInfoDe.setRequiredError("This field is required.");
		Label lbl = new Label("Category Name: ");
		lbl.setWidth("-1px");
		lay.addComponent(lbl, 0, 0);
		lay.addComponent(cmbCategoryInfoDe, 1, 0);

		cmbFinishedItemDe = new MultiComboBox();
		cmbFinishedItemDe.setWidth("450px");
		cmbFinishedItemDe.setInputPrompt("Select Finished Item");
		cmbFinishedItemDe.setRequired(true);
		cmbFinishedItemDe.setRequiredError("This field is required.");
		Label lblF = new Label("Finished Item: ");
		lbl.setWidth("-1px");
		lay.addComponent(lblF, 0, 1);
		lay.addComponent(cmbFinishedItemDe, 1, 1);

		lay.addComponent(cBtnInfoDe, 0, 2, 1, 2);
		lay.setComponentAlignment(cBtnInfoDe, Alignment.MIDDLE_CENTER);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelInfoDe.setContent(content);

		loadCategoryDe();

		return panelInfoDe;
	}

	private void loadCategoryDe()
	{
		cmbCategoryInfoDe.removeAllItems();
		String sqlC = "select vCategoryId, vCategoryName from master.tbItemCategory order by vCategoryName";
		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbCategoryInfoDe.addItem(element[0].toString());
			cmbCategoryInfoDe.setItemCaption(element[0].toString(), element[1].toString());
		}
		//cmbCategoryInfoDe.select("%");
		//cmbFinishedItemDe.select("%");
	}

	private void loadItemDe(String catIds)
	{
		cmbFinishedItemDe.removeAllItems();
		String sqlF = "select vItemId, vItemName from master.tbFinishedItemInfo where vCategoryId in (select"
				+ " item from dbo.Split('"+catIds+"')) order by vItemName";
		//System.out.println(sqlF);
		for (Iterator<?> iter = cm.selectSql(sqlF).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbFinishedItemDe.addItem(element[0].toString());
			cmbFinishedItemDe.setItemCaption(element[0].toString(), element[1].toString());
		}
		//cmbCategoryInfoDe.select("%");
		//cmbFinishedItemDe.select("%");
	}

	private void addValidationInfoDe()
	{
		if (!cmbCategoryInfoDe.getValue().toString().replace("]", "").replace("[", "").isEmpty())
		{
			if (!cmbFinishedItemDe.getValue().toString().replace("]", "").replace("[", "").isEmpty())
			{ viewReportInfoDe(); }
			else
			{
				cmbFinishedItemDe.focus();
				cm.showNotification("warning", "Warning!", "Select item name.");
			}
		}
		else
		{
			cmbCategoryInfoDe.focus();
			cm.showNotification("warning", "Warning!", "Select item category.");
		}
	}

	public void viewReportInfoDe()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "";//, xsql = "", fileName = "";

		String catId = cmbCategoryInfoDe.getValue().toString().replace("[", "").replace("]", "");
		String finId = cmbFinishedItemDe.getValue().toString().replace("[", "").replace("]", "");
		try
		{
			if (ogReportTypeRecipe.getValue().toString().equals("Recipe"))
			{
				sql = "select * from funItemRecipeReport('"+catId+"', '"+finId+"', '%') order by vCategoryName, vItemName, vSizeName, vRawItemName";
				//System.out.println(sql);
				reportSource = "com/jasper/postransaction/rptMenuRecipeReport.jasper";

				hm.put("sql", sql);
				hm.put("companyName", sessionBean.getCompanyName());
				hm.put("address", sessionBean.getCompanyAddress());
				hm.put("phoneFax", sessionBean.getCompanyContact());
				hm.put("branchName", sessionBean.getBranchName());

				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("logo", sessionBean.getCompanyLogo());
				hm.put("userIp", sessionBean.getUserIp());

				new ReportViewer(hm, reportSource);
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private void resetData()
	{ loadTableInfo(); loadCategory(); loadMenuName(); loadCategoryDe(); }

	public void enter(ViewChangeEvent event)
	{
		resetData();
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
	}
}
