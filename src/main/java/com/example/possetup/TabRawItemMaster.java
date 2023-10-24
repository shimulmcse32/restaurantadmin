package com.example.possetup;

import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonMethod;
import com.common.share.MultiComboBox;
import com.common.share.SessionBean;
import com.example.gateway.ItemInfoGateway;
import com.example.model.ItemInfoModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TabRawItemMaster extends HorizontalLayout
{
	private SessionBean sessionBean;

	public OptionGroup ogRawItemType;
	public TextField txtItemCode, txtItemName, txtItemNameAr, txtBarcode;
	public CommaField txtCostMargin, txtIssueRate;
	public ComboBox cmbUnit, cmbCategory, cmbVatCategory;
	public MultiComboBox cmbSupplierName;
	private Button btnAddUnit, btnAddCategory, btnAddSupplier;

	private ItemInfoGateway iig = new ItemInfoGateway();

	private CommonMethod cm;

	public TabRawItemMaster(SessionBean sessionBean, String flag)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(sessionBean);
		setSizeFull();

		addComponent(buildComponent());
		addActions();
	}

	private void addActions()
	{
		btnAddCategory.addClickListener(event ->
		{ addEditCategory(); });

		btnAddSupplier.addClickListener(event ->
		{ addEditSupplier(); });

		btnAddUnit.addClickListener(event ->
		{ addEditUnit(); });

		loadComboData();
	}

	private void addEditSupplier()
	{
		String supplierId = cmbSupplierName.getValue().toString().replace("[", "").replace("]", "");
		String addEdit = supplierId.isEmpty()? "Add":"Edit";

		AddEditSupplierInfo win = new AddEditSupplierInfo(sessionBean, addEdit, supplierId);
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(event ->
		{ loadSupplier(); });
	}

	private void addEditCategory()
	{
		String categoryId = cmbCategory.getValue() != null? cmbCategory.getValue().toString():"";
		String addEdit = categoryId.isEmpty()? "Add":"Edit";

		AddEditItemCategory win = new AddEditItemCategory(sessionBean, addEdit, categoryId, "Raw");
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event ->
		{ loadCategory(); });
	}

	private void addEditUnit()
	{
		String unitId = cmbUnit.getValue() != null? cmbUnit.getValue().toString():"";
		String addEdit = unitId.isEmpty()? "Add":"Edit";

		AddEditItemUnit win = new AddEditItemUnit(sessionBean, addEdit, unitId, "Raw");
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event ->
		{ loadUnit(); });
	}

	private void loadComboData()
	{
		loadSupplier();
		loadCategory();
		loadUnit();
		loadVatCategory();
	}

	private void loadSupplier()
	{
		//cmbSupplierName.removeAllItems();
		String sqls = "select vSupplierId, vSupplierCode, vSupplierName, dbo.funGetNumeric(vSupplierCode)"+
				" iCode from master.tbSupplierMaster where iActive = 1 order by iCode asc";
		for(Iterator<?> iter = cm.selectSql(sqls).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupplierName.addItem(element[0].toString());
			cmbSupplierName.setItemCaption(element[0].toString(), element[1].toString()+" - "+element[2].toString());
		}
	}

	private void loadCategory()
	{
		//cmbCategory.removeAllItems();
		String sqlC = "select vCategoryId, vCategoryName from master.tbItemCategory"+
				" where vCategoryType = 'Raw' and iActive = 1 order by vCategoryName";
		for(Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbCategory.addItem(element[0].toString());
			cmbCategory.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadUnit()
	{
		//cmbUnit.removeAllItems();
		String sqlC = "select iUnitId, vUnitName from master.tbUnitInfo where"+
				" vUnitType = 'Raw' and iActive = 1 order by vUnitName";
		for(Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbUnit.addItem(element[0].toString());
			cmbUnit.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadVatCategory()
	{
		String sqlV = "select vVatCatId, vVatCatName, mPercentage from master.tbVatCatMaster where iActive = 1";
		for(Iterator<?> iter = cm.selectSql(sqlV).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbVatCategory.addItem(element[0].toString());
			cmbVatCategory.setItemCaption(element[0].toString(), element[1].toString());
		}
		cmbVatCategory.select("V1");
	}

	public void getValue(ItemInfoModel iim)
	{
		iim.setBranchIds(sessionBean.getBranchId());
		iim.setKitchenName(txtItemCode.getValue().toString().trim());
		iim.setItemType(ogRawItemType.getValue().toString().trim());
		iim.setItemName(txtItemName.getValue().toString().trim());
		iim.setItemNameArabic(txtItemNameAr.getValue().toString().trim());
		iim.setBarCode(txtBarcode.getValue().toString().trim());

		iim.setSupplierIds(cmbSupplierName.getValue().toString().isEmpty()? "":
			cmbSupplierName.getValue().toString().replace("[", "").replace("]", ""));
		iim.setRawCategory(cmbCategory.getValue().toString());
		iim.setRawUnit(cmbUnit.getValue().toString());
		iim.setVatCategoryId(cmbVatCategory.getValue().toString());
		iim.setIssueRate(cm.getAmtValue(txtIssueRate));
		iim.setCostMargin(cm.getAmtValue(txtCostMargin));
		iim.setCreatedBy(sessionBean.getUserId());
	}

	private Component buildComponent()
	{
		GridLayout grid = new GridLayout(8, 10);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtItemCode = new TextField();
		txtItemCode.setImmediate(true);
		txtItemCode.addStyleName(ValoTheme.TEXTAREA_TINY);
		txtItemCode.setWidth("100%");
		txtItemCode.setInputPrompt("Item Code");
		txtItemCode.setValue(iig.getRawItemCode());
		grid.addComponent(new Label("Item Code: "), 0, 0);
		grid.addComponent(txtItemCode, 1, 0, 7, 0);

		txtItemName = new TextField();
		txtItemName.setImmediate(true);
		txtItemName.addStyleName(ValoTheme.TEXTAREA_TINY);
		txtItemName.setWidth("100%");
		txtItemName.setInputPrompt("Item Name");
		txtItemName.setRequired(true);
		txtItemName.setRequiredError("This field is required");
		grid.addComponent(new Label("Item Name: "), 0, 1);
		grid.addComponent(txtItemName, 1, 1, 7, 1);

		txtItemNameAr = new TextField();
		txtItemNameAr.setImmediate(true);
		txtItemNameAr.setStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
		txtItemNameAr.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtItemNameAr.setWidth("100%");
		txtItemNameAr.setInputPrompt("Item Name (Arabic)");
		Label lbl = new Label("Item Name (Arabic): ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 2);
		grid.addComponent(txtItemNameAr, 1, 2, 7, 2);

		txtBarcode = new TextField();
		txtBarcode.setImmediate(true);
		txtBarcode.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtBarcode.setWidth("100%");
		txtBarcode.setInputPrompt("Barcode");
		grid.addComponent(new Label("Barcode: "), 0, 3);
		grid.addComponent(txtBarcode, 1, 3, 7, 3);

		CssLayout groups = new CssLayout();
		groups.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		cmbSupplierName = new MultiComboBox();
		cmbSupplierName.setInputPrompt("Select Supplier Name");
		cmbSupplierName.setWidth("315px");
		cmbSupplierName.setDescription("Select one item to edit");
		Label lbls = new Label("Supplier Name: ");
		lbls.setWidth("-1px");
		grid.addComponent(lbls, 0, 4);
		btnAddSupplier = new Button();
		btnAddSupplier.setIcon(FontAwesome.PLUS);
		btnAddSupplier.setStyleName(ValoTheme.BUTTON_TINY);
		btnAddSupplier.setDescription("Add Supplier");
		groups.addComponents(cmbSupplierName, btnAddSupplier);
		grid.addComponent(groups, 1, 4, 7, 4);

		CssLayout group = new CssLayout();
		group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		cmbCategory = new ComboBox();
		cmbCategory.setInputPrompt("Select Category");
		cmbCategory.setWidth("300px");
		cmbCategory.setDescription("Select one item to edit");
		cmbCategory.setRequired(true);
		cmbCategory.setRequiredError("This field is required.");
		cmbCategory.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbCategory.setFilteringMode(FilteringMode.CONTAINS);
		Label lblc = new Label("Category: ");
		lblc.setWidth("-1px");
		grid.addComponent(lblc, 0, 5);
		btnAddCategory = new Button();
		btnAddCategory.setIcon(FontAwesome.PLUS);
		btnAddCategory.setStyleName(ValoTheme.BUTTON_TINY);
		btnAddCategory.setDescription("Add Category");
		group.addComponents(cmbCategory, btnAddCategory);
		grid.addComponent(group, 1, 5, 7, 5);

		group = new CssLayout();
		group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		cmbUnit = new ComboBox();
		cmbUnit.setWidth("300px");
		cmbUnit.setInputPrompt("Select Unit");
		cmbUnit.setDescription("Select one item to edit");
		cmbUnit.setRequired(true);
		cmbUnit.setRequiredError("This field is required.");
		cmbUnit.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbUnit.setFilteringMode(FilteringMode.CONTAINS);
		btnAddUnit = new Button();
		btnAddUnit.setIcon(FontAwesome.PLUS);
		btnAddUnit.setStyleName(ValoTheme.BUTTON_TINY);
		btnAddUnit.setDescription("Add Unit");
		group.addComponents(cmbUnit, btnAddUnit);
		grid.addComponent(new Label("Unit Name: "), 0, 6);
		grid.addComponent(group, 1, 6, 7, 6);

		cmbVatCategory = new ComboBox();
		cmbVatCategory.setImmediate(true);
		cmbVatCategory.setFilteringMode(FilteringMode.CONTAINS);
		cmbVatCategory.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbVatCategory.setFilteringMode(FilteringMode.CONTAINS);
		cmbVatCategory.setInputPrompt("Select VAT Category");
		cmbVatCategory.setWidth("50%");
		cmbVatCategory.setRequired(true);
		cmbVatCategory.setRequiredError("This field is required.");
		grid.addComponent(new Label("VAT Category: "), 0, 7);
		grid.addComponent(cmbVatCategory, 1, 7, 7, 7);

		group = new CssLayout();
		group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		txtIssueRate = new CommaField();
		txtIssueRate.setImmediate(true);
		txtIssueRate.addStyleName(ValoTheme.TEXTAREA_TINY);
		txtIssueRate.setWidth("50%");
		txtIssueRate.setRequired(true);
		txtIssueRate.setRequiredError("This field is required.");
		txtIssueRate.setDescription("Cost Rate");
		txtIssueRate.setInputPrompt("Cost Rate");
		grid.addComponent(new Label("Cost Details: "), 0, 8);

		txtCostMargin = new CommaField();
		txtCostMargin.setImmediate(true);
		txtCostMargin.addStyleName(ValoTheme.TEXTAREA_TINY);
		txtCostMargin.setWidth("50%");
		txtCostMargin.setDescription("Cost Margin On Transfer");
		txtCostMargin.setMaxLength(2);
		txtCostMargin.setInputPrompt("Cost Margin");
		group.addComponents(txtIssueRate, txtCostMargin);
		grid.addComponent(group, 1, 8, 7, 8);

		ogRawItemType = new OptionGroup();
		ogRawItemType.addItem("Raw");
		ogRawItemType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogRawItemType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		ogRawItemType.setDescription("Item Type");
		grid.addComponent(new Label("Item Type: "), 0, 9);
		grid.addComponent(ogRawItemType, 1, 9, 7, 9);

		return grid;
	}

	public void txtClear()
	{
		txtItemCode.setValue(iig.getRawItemCode());
		txtItemName.setValue("");
		txtItemNameAr.setValue("");
		txtBarcode.setValue("");
		cmbSupplierName.setValue(null);
		cmbCategory.setValue(null);
		cmbUnit.setValue(null);
		cmbVatCategory.setValue(null);
		txtIssueRate.setValue("");
	}
}
