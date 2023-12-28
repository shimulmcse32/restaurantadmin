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
import com.common.share.ReportViewer;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.MultiComboBox;
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
public class RawItemInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblRawItemList;
	private ArrayList<Label> tbLblItemId = new ArrayList<Label>();
	private ArrayList<Label> tbLblItemType = new ArrayList<Label>();
	private ArrayList<Label> tbLblItemDetails = new ArrayList<Label>();
	private ArrayList<Label> tbLblCategoryName = new ArrayList<Label>();
	private ArrayList<Label> tbLblUnit = new ArrayList<Label>();
	private ArrayList<Label> tbLblVat = new ArrayList<Label>();
	private ArrayList<Label> tbLblIssueRate = new ArrayList<Label>();
	private ArrayList<Label> tbLblCostMargin = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();
	private SessionBean sessionBean;

	private TextField txtSearch;
	private OptionGroup ogStatus, ogType;

	private CommonMethod cm;
	private ItemInfoGateway iig = new ItemInfoGateway();

	//Report panel master
	private CommonButton cBtnView = new CommonButton("", "", "", "", "", "", "", "View", "");
	private ComboBox cmbCategory;
	private OptionGroup ogItemStaus, ogItemType, ogReportFormat;
	private String formId;

	//Report panel profile
	private CommonButton cBtnViewPro = new CommonButton("", "", "", "", "", "", "", "View", "");
	private MultiComboBox cmbItemName;

	public RawItemInformation(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		this.formId = formId;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		addComponents(cBtn, addPanel(), addReportMaster(), addReportProfile());

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

		txtSearch.addValueChangeListener(event ->
		{ loadTableInfo(); });

		cBtnView.btnPreview.addClickListener(event ->
		{ addValidation(); });

		cBtnViewPro.btnPreview.addClickListener(event ->
		{ addValidationPro(); });

		ogStatus.addValueChangeListener(event ->
		{ loadTableInfo(); });

		ogType.addValueChangeListener(event ->
		{ loadTableInfo(); });
	}

	private void addEditWindow(String addEdit, String rawItemId, String ar)
	{
		AddEditRawItemInfo win = new AddEditRawItemInfo(sessionBean, addEdit, rawItemId);
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
		Panel pnlTable = new Panel("Inventory Item List :: "+sessionBean.getCompanyName()+
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

		ogType = new OptionGroup();
		ogType.addItem("All");
		ogType.addItem("Raw");
		ogType.addItem("Semi-Cooked");
		ogType.select("All");
		ogType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		ogType.setDescription("Item Type");

		ogStatus = new OptionGroup();
		ogStatus.addItem("All");
		ogStatus.addItem("Active");
		ogStatus.addItem("Inactive");
		ogStatus.select("Active");
		ogStatus.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogStatus.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		ogStatus.setDescription("Item Status");

		lay.addComponents(txtSearch, ogType, ogStatus);

		buildTable();

		content.addComponents(lay, tblRawItemList, tblRawItemList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblRawItemList = new TablePaged();
		tblRawItemList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblItemId.get(ar).getValue().toString();
				addEditWindow("Edit", id, ar+"");
			}
		});

		tblRawItemList.addContainerProperty("Item Id", Label.class, new Label(), null, null, Align.CENTER);
		tblRawItemList.setColumnCollapsed("Item Id", true);

		tblRawItemList.addContainerProperty("Item Type", Label.class, new Label(), null, null, Align.CENTER);

		tblRawItemList.addContainerProperty("Item Details", Label.class, new Label(), null, null, Align.CENTER);

		tblRawItemList.addContainerProperty("Category", Label.class, new Label(), null, null, Align.CENTER);

		tblRawItemList.addContainerProperty("VAT Category", Label.class, new Label(), null, null, Align.CENTER);

		tblRawItemList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);

		tblRawItemList.addContainerProperty("Issue Rate", Label.class, new Label(), null, null, Align.CENTER);

		tblRawItemList.addContainerProperty("Cost Margin", Label.class, new Label(), null, null, Align.CENTER);

		tblRawItemList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblRawItemList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblRawItemList.setColumnWidth("Action", 90);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblItemId.add(ar, new Label());
			tbLblItemId.get(ar).setWidth("100%");
			tbLblItemId.get(ar).setImmediate(true);
			tbLblItemId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblItemType.add(ar, new Label());
			tbLblItemType.get(ar).setWidth("100%");
			tbLblItemType.get(ar).setImmediate(true);
			tbLblItemType.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblItemDetails.add(ar, new Label());
			tbLblItemDetails.get(ar).setWidth("100%");
			tbLblItemDetails.get(ar).setImmediate(true);
			tbLblItemDetails.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblCategoryName.add(ar, new Label());
			tbLblCategoryName.get(ar).setWidth("100%");
			tbLblCategoryName.get(ar).setImmediate(true);
			tbLblCategoryName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblVat.add(ar, new Label());
			tbLblVat.get(ar).setWidth("100%");
			tbLblVat.get(ar).setImmediate(true);
			tbLblVat.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblUnit.add(ar, new Label());
			tbLblUnit.get(ar).setWidth("100%");
			tbLblUnit.get(ar).setImmediate(true);
			tbLblUnit.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblIssueRate.add(ar, new Label());
			tbLblIssueRate.get(ar).setWidth("100%");
			tbLblIssueRate.get(ar).setImmediate(true);
			tbLblIssueRate.get(ar).setStyleName(ValoTheme.LABEL_TINY);
			tbLblIssueRate.get(ar).setStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbLblCostMargin.add(ar, new Label());
			tbLblCostMargin.get(ar).setWidth("100%");
			tbLblCostMargin.get(ar).setImmediate(true);
			tbLblCostMargin.get(ar).setStyleName(ValoTheme.LABEL_TINY);
			tbLblCostMargin.get(ar).setStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbChkActive.add(ar, new CheckBox());
			tbChkActive.get(ar).setWidth("100%");
			tbChkActive.get(ar).setImmediate(true);

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
				String id = tbLblItemId.get(ar).getValue().toString();
				if (!id.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					tbCmbAction.get(ar).setEnabled(false);
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ addEditWindow("Edit", id, ar+""); }
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
									if (iig.activeInactiveDataRaw(id, sessionBean.getUserId()))
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
			tblRawItemList.addItem(new Object[]{tbLblItemId.get(ar), tbLblItemType.get(ar), tbLblItemDetails.get(ar),
					tbLblCategoryName.get(ar), tbLblVat.get(ar), tbLblUnit.get(ar), tbLblIssueRate.get(ar),
					tbLblCostMargin.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void loadTableInfo()
	{
		String search = "%"+txtSearch.getValue().toString()+"%";
		String type = "", status = "", details = "";
		if (ogType.getValue().toString().equals("All"))
		{ type = "%"; }
		else
		{ type = ogType.getValue().toString().toString(); }

		if (ogStatus.getValue().toString().equals("Active"))
		{ status = "1"; }
		else if (ogStatus.getValue().toString().equals("Inactive"))
		{ status = "0"; }
		else if (ogStatus.getValue().toString().equals("All"))
		{ status = "%"; }

		tableClear();
		int i = 0;
		try
		{
			String sql = "select rin.vItemId, rin.vItemType, rin.vItemCode, rin.vItemName, ic.vCategoryName, uni.vUnitName,"+
					" vcm.vVatCatName, rin.iActive, rin.mCostMargin, rin.mIssueRate, dbo.funGetNumeric(rin.vItemCode) iCode"+
					" from master.tbRawItemInfo rin inner join master.tbItemCategory ic on rin.vCategoryId = ic.vCategoryId"+
					" inner join master.tbUnitInfo uni on rin.vUnitId = uni.iUnitId inner join master.tbVatCatMaster vcm"+
					" on rin.vVatCatId = vcm.vVatCatId where (rin.vItemCode like '"+search+"' or rin.vItemName like '"+search+"')"+
					" and rin.iActive like '"+status+"' and rin.vItemType like '"+type+"' order by iCode desc";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblItemId.size() <= i)
				{ tableRowAdd(i); }

				details = element[2].toString().isEmpty()?element[3].toString():element[2].toString()+" - "+element[3].toString();

				tbLblItemId.get(i).setValue(element[0].toString());
				tbLblItemType.get(i).setValue(element[1].toString());
				tbLblItemDetails.get(i).setValue(details);
				tbLblCategoryName.get(i).setValue(element[4].toString());
				tbLblUnit.get(i).setValue(element[5].toString());
				tbLblVat.get(i).setValue(element[6].toString());
				tbLblCostMargin.get(i).setValue(cm.deciFloat.format(element[8])+" %");
				tbLblIssueRate.get(i).setValue(cm.deciMn.format(element[9]));
				tbChkActive.get(i).setValue((element[7].toString().equals("1")?true:false));
				tbChkActive.get(i).setEnabled(false);
				i++;
			}
			tblRawItemList.nextPage();
			tblRawItemList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void tableClear()
	{ cm.tableClear(tblRawItemList, tbLblItemId); }

	//Report Panel Master Start
	private Panel addReportMaster()
	{
		Panel panelReport = new Panel("Inventory Item(s) Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 5);
		lay.setSpacing(true);

		cmbCategory = new ComboBox();
		cmbCategory.setInputPrompt("Select Category");
		cmbCategory.setImmediate(true);
		cmbCategory.setWidth("350px");
		cmbCategory.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbCategory.setFilteringMode(FilteringMode.CONTAINS);
		cmbCategory.setRequired(true);
		cmbCategory.setRequiredError("This field is required.");
		lay.addComponent(new Label("Category: "), 0, 0);
		lay.addComponent(cmbCategory, 1, 0);
		loadCategory();

		ogItemStaus = new OptionGroup();
		ogItemStaus.addItem("All");
		ogItemStaus.addItem("Active");
		ogItemStaus.addItem("Inactive");
		ogItemStaus.select("All");
		ogItemStaus.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogItemStaus.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Item Status: "), 0, 1);
		lay.addComponent(ogItemStaus, 1, 1);

		ogItemType = new OptionGroup();
		ogItemType.addItem("All");
		ogItemType.addItem("Raw");
		ogItemType.addItem("Semi-Cooked");
		ogItemType.select("All");
		ogItemType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogItemType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Item Type: "), 0, 2);
		lay.addComponent(ogItemType, 1, 2);

		ogReportFormat = new OptionGroup();
		ogReportFormat.addItem("PDF");
		ogReportFormat.addItem("XLS");
		ogReportFormat.select("PDF");
		ogReportFormat.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormat.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Format: "), 0, 3);
		lay.addComponent(ogReportFormat, 1, 3);

		lay.addComponent(cBtnView, 1, 4);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelReport.setContent(content);

		return panelReport;
	}

	private void loadCategory()
	{
		String sql = "select vCategoryId, vCategoryName from master.tbItemCategory"+
				" where vCategoryType = 'Raw' order by vCategoryName", caption = "";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			if (caption.isEmpty())
			{
				cmbCategory.addItem("%");
				cmbCategory.setItemCaption("%", "ALL");
			}
			caption = element[1].toString();
			cmbCategory.addItem(element[0].toString());
			cmbCategory.setItemCaption(element[0].toString(), caption);
		}
		cmbCategory.select("%");
	}

	private void addValidation()
	{
		if (cmbCategory.getValue() != null)
		{ viewReport(); }
		else
		{
			cmbCategory.focus();
			cm.showNotification("warning", "Warning!", "Select category."); 
		}
	}

	@SuppressWarnings("deprecation")
	public void viewReport()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", xsql = "", fileName = "";

		try
		{
			String status = ogItemStaus.getValue().toString();
			String types = ogItemType.getValue().toString();
			String stat = "", type = "";

			if (status.equals("All")) {stat = "%";}
			else if (status.equals("Active")) {stat = "1";}
			else { stat = "0"; }

			if (types.equals("All")) {type = "%";}
			else { type = types; }

			String cate = cmbCategory.getValue().toString();

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			if (ogReportFormat.getValue().toString().equals("PDF"))
			{
				sql = "select ic.vCategoryName, rii.vItemCode, rii.vItemName, rii.mIssueRate, rii.mCostMargin, ISNULL(CONVERT"+
						"(varchar(2000), (SELECT STUFF((SELECT ', '+sup.vSupplierName FROM master.tbSupplierMaster sup WHERE"+
						" sup.vSupplierId in (select Item from dbo.Split(rii.vSupplierIds)) FOR XML PATH ('')) , 1, 2, ''))), '')"+
						" vSupplierName, vc.vVatCatName, ui.vUnitName, rii.iActive, dbo.funGetNumeric(rii.vItemCode) iCode,"+
						" rii.vItemType from master.tbRawItemInfo rii, master.tbItemCategory ic, master.tbVatCatMaster vc,"+
						" master.tbUnitInfo ui where rii.vCategoryId = ic.vCategoryId and rii.vVatCatId = vc.vVatCatId and"+
						" rii.vUnitId = ui.iUnitId and rii.vCategoryId like '"+cate+"' and rii.iActive like '"+stat+"' and"+
						" rii.vItemType like '"+type+"' order by ic.vCategoryName, rii.vItemType, iCode";
				reportSource = "com/jasper/possetup/rptRawItemInformation.jasper";

				hm.put("sql", sql);
				hm.put("userName", sessionBean.getFullName());
				hm.put("devloperInfo", sessionBean.getDeveloper());
				hm.put("logo", sessionBean.getCompanyLogo());
				hm.put("userIp", sessionBean.getUserIp());
				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = "select ic.vCategoryName, rii.vItemCode, rii.vItemName, rii.mIssueRate, rii.mCostMargin, ISNULL(CONVERT("+
						"varchar(2000), (SELECT STUFF((SELECT ', '+sup.vSupplierName FROM master.tbSupplierMaster sup WHERE"+
						" sup.vSupplierId in (select Item from dbo.Split(rii.vSupplierIds)) FOR XML PATH ('')) , 1, 2, ''))), '')"+
						" vSupplierName, vc.vVatCatName, ui.vUnitName, case when rii.iActive = 0 then 'Inactive' else 'Active'"+
						" end iStatus, dbo.funGetNumeric(rii.vItemCode) iCode, rii.vItemType from master.tbRawItemInfo rii,"+
						" master.tbItemCategory ic, master.tbVatCatMaster vc, master.tbUnitInfo ui where rii.vCategoryId ="+
						" ic.vCategoryId and rii.vVatCatId = vc.vVatCatId and rii.vUnitId = ui.iUnitId and rii.vCategoryId like"+
						" '"+cate+"' and rii.iActive like '"+stat+"' and rii.vItemType like '"+type+"' order by ic.vCategoryName,"+
						" rii.vItemType, iCode";
				fileName = "Inventory_Item_List_";

				hm.put("parameters", "");
				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
			//System.out.println(sql);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
	//Report Panel Master End

	//Report Panel Master Start
	private Panel addReportProfile()
	{
		Panel panelProfile = new Panel("Inventory Item(s) Profile :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(2, 2);
		lay.setSpacing(true);

		cmbItemName = new MultiComboBox();
		cmbItemName.setInputPrompt("Select Item Name");
		cmbItemName.setWidth("400px");
		cmbItemName.setDescription("Select item name");
		cmbItemName.setRequired(true);
		cmbItemName.setRequiredError("This field is required.");
		lay.addComponent(new Label("Semi-Cooked Item: "), 0, 0);
		lay.addComponent(cmbItemName, 1, 0);
		loadSemiCookedItem();

		lay.addComponent(cBtnViewPro, 1, 1);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelProfile.setContent(content);

		return panelProfile;
	}

	private void loadSemiCookedItem()
	{
		String sql = "select vItemId, vItemName, vItemCode, dbo.funGetNumeric(vItemCode) iCode from master.tbRawItemInfo"+
				" where vItemType = 'Semi-Cooked' order by iCode asc";

		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbItemName.addItem(element[0].toString());
			cmbItemName.setItemCaption(element[0].toString(),
					element[2].toString()+" - "+element[1].toString());
		}
	}

	private void addValidationPro()
	{
		if (!cmbItemName.getValue().toString().replace("[", "").replace("]", "").trim().isEmpty())
		{ viewReportPro(); }
		else
		{
			cmbItemName.focus();
			cm.showNotification("warning", "Warning!", "Select item name."); 
		}
	}

	public void viewReportPro()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "";

		try
		{
			String items = cmbItemName.getValue().toString().replace("[", "").replace("]", "").trim();
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());

			sql = "select rii.vItemCode+' - '+rii.vItemName vMainItem, CONVERT(DECIMAL(10,3),rii.mIssueRate) mMainRate,"+
					" (select vUnitName from master.tbUnitInfo where iUnitId = rii.vUnitId) vMainUnit, riip.vItemCode+'"+
					" - '+riip.vItemName vProfileItem, (select vUnitName from master.tbUnitInfo where iUnitId = riip.vUnitId)"+
					" vProfileUnit, rip.mUnitQty1, riip.mIssueRate, riip.mCostMargin, dbo.funGetNumeric(rii.vItemCode)"+
					" iCode from master.tbRawItemInfo rii left join master.tbRawItemProfile rip on rii.vItemId = rip.vItemId"+
					" and rip.iActive = 1 inner join master.tbRawItemInfo riip on rip.vItemIdProfile = riip.vItemId where"+
					" rii.vItemType = 'Semi-Cooked' and rii.vItemId in (select Item from dbo.Split('"+items+"')) order by iCode";
			reportSource = "com/jasper/possetup/rptRawItemProfile.jasper";

			hm.put("sql", sql);
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", sessionBean.getUserIp());
			new ReportViewer(hm, reportSource);
			//System.out.println(sql);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
	//Report Panel Master End*/

	public void enter(ViewChangeEvent event)
	{
		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		cBtn.btnNew.setEnabled(cm.insert);
		loadTableInfo();
		loadCategory();
	}
}
