package com.example.possetup;

import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonMethod;
import com.common.share.SessionBean;
import com.example.model.ItemInfoModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TabItemInventory extends HorizontalLayout
{
	private SessionBean sessionBean;

	public ComboBox cmbRawCategory, cmbRawUnit;
	public OptionGroup ogRawItemType;
	public CommaField txtCostMargin;

	private Button btnAddRawUnit, btnAddRawCat;

	private CommonMethod cm;

	public TabItemInventory(SessionBean sessionBean)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(sessionBean);
		setSizeFull();

		addComponent(buildComponent());
		addActions();
	}

	private void addActions()
	{
		loadRawCombo();

		btnAddRawCat.addClickListener(event -> addEditRawCategory());

		btnAddRawUnit.addClickListener(event -> addEditRawUnit());
	}

	private void addEditRawCategory()
	{
		String categoryId = cm.getComboValue(cmbRawCategory);
		String addEdit = categoryId.isEmpty()? "Add":"Edit";

		AddEditItemCategory win = new AddEditItemCategory(sessionBean, addEdit, categoryId, "Raw");
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event -> loadRawCombo());
	}

	private void addEditRawUnit()
	{
		String unitId = cm.getComboValue(cmbRawUnit);
		String addEdit = unitId.isEmpty()? "Add":"Edit";

		AddEditItemUnit win = new AddEditItemUnit(sessionBean, addEdit, unitId, "Raw");
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event -> loadRawCombo());
	}

	public void loadRawCombo()
	{
		String sqlR = "select vCategoryId, vCategoryName from master.tbItemCategory where"+
				" vCategoryType = 'Raw' and iActive = 1 order by vCategoryType, vCategoryName";
		for (Iterator<?> iter = cm.selectSql(sqlR).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbRawCategory.addItem(element[0].toString());
			cmbRawCategory.setItemCaption(element[0].toString(), element[1].toString());
		}

		String sqlU = "select iUnitId, vUnitName from master.tbUnitInfo where"+
				" vUnitType = 'Raw' and iActive = 1 order by vUnitName";
		for (Iterator<?> iter = cm.selectSql(sqlU).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbRawUnit.addItem(element[0].toString());
			cmbRawUnit.setItemCaption(element[0].toString(), element[1].toString());
		}
		if (cmbRawCategory.getValue() == null)
		{ cmbRawCategory.select("C1"); }
		if (cmbRawUnit.getValue() == null)
		{ cmbRawUnit.select("5"); }
	}

	private Component buildComponent()
	{
		GridLayout grid = new GridLayout(8, 9);
		grid.setMargin(new MarginInfo(true, true, false, true));
		grid.setSpacing(true);
		grid.setSizeFull();

		ogRawItemType = new OptionGroup();
		ogRawItemType.addItem("Raw");
		//ogRawItemType.addItem("Semi-Cooked");
		ogRawItemType.select("Raw");
		ogRawItemType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogRawItemType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		grid.addComponent(new Label("Type:"), 0, 1);
		grid.addComponent(ogRawItemType, 1, 1, 3, 1);

		CssLayout groups = new CssLayout();
		groups.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		cmbRawCategory = new ComboBox();
		cmbRawCategory.setImmediate(true);
		cmbRawCategory.setFilteringMode(FilteringMode.CONTAINS);
		cmbRawCategory.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbRawCategory.setInputPrompt("Select Item Category");
		cmbRawCategory.setDescription("Item Category");
		cmbRawCategory.setWidth("300px");
		Label lblCa = new Label("Item Category:");
		lblCa.setWidth("-1px");
		grid.addComponent(lblCa, 0, 2);
		btnAddRawCat = new Button();
		btnAddRawCat.setIcon(FontAwesome.PLUS);
		btnAddRawCat.setStyleName(ValoTheme.BUTTON_TINY);
		btnAddRawCat.setDescription("Add Inventory Category");
		groups.addComponents(cmbRawCategory, btnAddRawCat);
		grid.addComponent(groups, 1, 2, 3, 2);

		CssLayout group = new CssLayout();
		group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		cmbRawUnit = new ComboBox();
		cmbRawUnit.setImmediate(true);
		cmbRawUnit.setNullSelectionAllowed(false);
		cmbRawUnit.setFilteringMode(FilteringMode.CONTAINS);
		cmbRawUnit.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbRawUnit.setInputPrompt("Select Item Unit");
		cmbRawUnit.setDescription("Item Unit");
		cmbRawUnit.setWidth("300px");
		Label lblRa = new Label("Item Unit:");
		lblRa.setWidth("-1px");
		grid.addComponent(lblRa, 0, 3);
		btnAddRawUnit = new Button();
		btnAddRawUnit.setIcon(FontAwesome.PLUS);
		btnAddRawUnit.setStyleName(ValoTheme.BUTTON_TINY);
		btnAddRawUnit.setDescription("Add Unit");
		group.addComponents(cmbRawUnit, btnAddRawUnit);
		grid.addComponent(group, 1, 3, 3, 3);

		/*Label lblCostPrice = new Label("Cost Price : ");
		lblCostPrice.setWidth("100px");

		txtCostPrice = new CommaField();
		txtCostPrice.setWidth("70px");
		txtCostPrice.setValue("0");
		txtCostPrice.setImmediate(true);
		txtCostPrice.setInputPrompt("Cost Price");
		txtCostPrice.setDescription("Cost Price");
		txtCostPrice.addStyleName(ValoTheme.TEXTFIELD_TINY);

		CssLayout cssCostPrice = new CssLayout();
		cssCostPrice.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		cssCostPrice.addComponents(lblCostPrice, txtCostPrice);
		grid.addComponent(cssCostPrice, 0, 4, 4, 4);*/

		txtCostMargin = new CommaField();
		txtCostMargin.setWidth("80px");
		txtCostMargin.setValue("0");
		txtCostMargin.setMaxValue(100);
		txtCostMargin.setImmediate(true);
		txtCostMargin.setInputPrompt("%");
		txtCostMargin.setDescription("Cost Margin(%)");
		txtCostMargin.addStyleName(ValoTheme.TEXTFIELD_TINY);
		grid.addComponent(new Label("Cost Margin(%): "), 0, 4);
		grid.addComponent(txtCostMargin, 1, 4, 3, 4);

		return grid;
	}

	public void getValue(ItemInfoModel iim)
	{
		//Inventory item information
		iim.setItemTypeRaw(ogRawItemType.getValue() != null? ogRawItemType.getValue().toString():"");
		iim.setRawCategory(cm.getComboValue(cmbRawCategory));
		iim.setRawUnit(cm.getComboValue(cmbRawUnit));
		iim.setCostMargin(cm.getAmtValue(txtCostMargin));
		//iim.setIssueRate(txtCostPrice.getValue() != null? cm.getAmtValue(txtCostPrice):0);
		iim.setIssueRate(0);
	}

	public void setValue(ItemInfoModel iim)
	{
		ogRawItemType.setValue(iim.getItemTypeRaw());
		cmbRawCategory.setValue(iim.getRawCategory());
		cmbRawUnit.setValue(iim.getRawUnit());
		//txtCostPrice.setValue(iim.getIssueRate());
		txtCostMargin.setValue(iim.getCostMargin());
	}

	public void txtClear()
	{
		ogRawItemType.setValue(null);
		cmbRawCategory.setValue(null);
		cmbRawUnit.setValue(null);
		txtCostMargin.setValue("");
	}
}
