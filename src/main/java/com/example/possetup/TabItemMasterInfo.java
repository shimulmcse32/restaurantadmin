package com.example.possetup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonMethod;
import com.common.share.ImageResizer;
import com.common.share.ImageUpload;
import com.common.share.MultiComboBox;
import com.common.share.SessionBean;
import com.example.model.ItemInfoModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TabItemMasterInfo extends HorizontalLayout
{
	private SessionBean sessionBean;

	public TextField txtItemName, txtDescription;
	public ComboBox cmbVatCategory, cmbFinishCategory, cmbColor;
	public OptionGroup ogVatOption, ogItemType;
	public CheckBox chkOnlineMenu, chkInventory;

	private ImageUpload Image;
	public MultiComboBox cmbBranchName, cmbSalesType, cmbPackageName;
	public Button btnClearImg, btnAddPackage;
	private Button btnAddCategory;

	public Table tblUnitPriceList;
	public ArrayList<Label> tbLblPackageId = new ArrayList<Label>();
	public ArrayList<Label> tbLblPackageName = new ArrayList<Label>();
	public ArrayList<CommaField> tbTxtMainPrice = new ArrayList<CommaField>();
	public ArrayList<TextField> tbTxtActualPrice = new ArrayList<TextField>();
	public ArrayList<TextField> tbTxtVatAmount = new ArrayList<TextField>();
	public ArrayList<TextField> tbTxtFinalPrice = new ArrayList<TextField>();
	public ArrayList<CommaField> tbTxtFixedCost = new ArrayList<CommaField>();
	private ArrayList<Button> tbBtnRemove = new ArrayList<Button>();

	private CommonMethod cm;
	private String itemImage = "0", editImage = "0", flag = "";
	public HashMap<String, Double> hmVat = new HashMap<String, Double>();
	private HashMap<String, String> hmPrice = new HashMap<String, String>();

	public boolean changes = false, action = false;
	private byte[] imageInBytes = null;

	public TabItemMasterInfo(SessionBean sessionBean, String flag)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		cm = new CommonMethod(sessionBean);
		setSizeFull();

		itemImage = sessionBean.itemPhoto;

		addComponent(buildComponent());
		addActions();
	}

	private void addActions()
	{
		btnAddCategory.addClickListener(event -> addEditCategory());

		ogVatOption.addValueChangeListener(event -> setVatAmount());

		cmbVatCategory.addValueChangeListener(event -> setVatAmount());

		btnAddPackage.addClickListener(event -> addEditUnit());

		btnClearImg.addClickListener(event ->
		{
			Image.image.removeAllComponents();
			Image.status.setValue("");
			editImage = "0";
			imageInBytes = null;
			Image.fileName = "";
		});
		loadCombos();
	}

	public void setHashmapValue()
	{
		if (!hmPrice.isEmpty())
		{
			for (int i = 0; i < tbLblPackageId.size(); i++)
			{
				String id = tbLblPackageId.get(i).getValue().toString();
				if (hmPrice.get(id) != null)
				{
					String fullData = hmPrice.get(id);
					double price = Double.parseDouble(fullData.substring(0, fullData.indexOf("#")));
					double cost = Double.parseDouble(fullData.substring(fullData.indexOf("#")+1, fullData.length()));
					tbTxtMainPrice.get(i).setValue(price);
					tbTxtFixedCost.get(i).setValue(cost);
				}
			}
		}
	}

	private void setVatAmount()
	{
		for (int i = 0; i < tbLblPackageId.size(); i++)
		{ calculateVatData(i); }
	}

	private void calculateVatData(int ar)
	{
		if (!tbLblPackageId.get(ar).getValue().toString().isEmpty() && 
				!tbTxtMainPrice.get(ar).getValue().toString().isEmpty())
		{
			String vatRule = ogVatOption.getValue().toString();
			double vatPecent = hmVat.get(cm.getComboValue(cmbVatCategory));
			double mainRate = cm.getAmtValue(tbTxtMainPrice.get(ar));
			double vatAmount = cm.getRound((mainRate*vatPecent)/(vatRule.equals("Inclusive")?(100+vatPecent):100));

			double cost = cm.getAmtValue(tbTxtFixedCost.get(ar));

			hmPrice.put(tbLblPackageId.get(ar).getValue().toString(), mainRate+"#"+cost);

			double actualRate = 0;
			if (vatRule.equals("Inclusive"))
			{ actualRate = mainRate - vatAmount; }
			else
			{ actualRate = mainRate; }

			tbTxtActualPrice.get(ar).setReadOnly(false);
			tbTxtActualPrice.get(ar).setValue(cm.setComma(actualRate));
			tbTxtActualPrice.get(ar).setReadOnly(true);

			tbTxtVatAmount.get(ar).setReadOnly(false);
			tbTxtVatAmount.get(ar).setValue(cm.setComma(vatAmount));
			tbTxtVatAmount.get(ar).setReadOnly(true);

			tbTxtFinalPrice.get(ar).setReadOnly(false);
			tbTxtFinalPrice.get(ar).setValue(cm.setComma(actualRate+vatAmount));
			tbTxtFinalPrice.get(ar).setReadOnly(true);
		}
	}

	private void loadCombos()
	{
		String sqlB = "select vBranchId, vBranchName from master.tbBranchMaster where iActive = 1"+
				" order by vBranchName";
		for (Iterator<?> iter = cm.selectSql(sqlB).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbBranchName.addItem(element[0].toString());
			cmbBranchName.setItemCaption(element[0].toString(), element[1].toString());
			cmbBranchName.select(element[0].toString());
		}

		String sqlS = "select iSalesTypeId, vSalesType from master.tbSalesType where vFlag = 'POS'"+
				" and iActive = 1 order by vSalesType";
		for (Iterator<?> iter = cm.selectSql(sqlS).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSalesType.addItem(element[0].toString());
			cmbSalesType.setItemCaption(element[0].toString(), element[1].toString());
			cmbSalesType.select(element[0].toString());
		}

		String sqlV = "select vVatCatId, vVatCatName, mPercentage from master.tbVatCatMaster"+
				" where iActive = 1";
		for (Iterator<?> iter = cm.selectSql(sqlV).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbVatCategory.addItem(element[0].toString());
			cmbVatCategory.setItemCaption(element[0].toString(), element[1].toString());
			hmVat.put(element[0].toString(), Double.parseDouble(element[2].toString()));
		}
		cmbVatCategory.select("V2");

		loadCategory();
		loadPackage();
	}

	public void loadCategory()
	{
		String type = ogItemType.getValue().toString();
		type = type.equals("Both")?" vCategoryType not in ('Raw', 'Modifier')":" vCategoryType = '"+type+"'";
		String sqlC = "select vCategoryId, vCategoryName, vCategoryType, vCategoryColor+'.gif'vColor from master.tbItemCategory"+
				" where "+type+" and iActive = 1 order by vCategoryType, vCategoryName";
		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbFinishCategory.addItem(element[0].toString());
			cmbFinishCategory.setItemCaption(element[0].toString(), element[1].toString()+
					" ("+element[2].toString()+")");
		}
		/*cmbFinishCategory.setValue("C2");//Default category for modifier
		cmbFinishCategory.setEnabled(ogItemType.getValue().toString().equals("Modifier")?false:true);
		btnAddCategory.setEnabled(ogItemType.getValue().toString().equals("Modifier")?false:true);
		cmbFinishCategory.setRequired(ogItemType.getValue().toString().equals("Modifier")?false:true);
		cmbFinishCategory.setRequiredError(ogItemType.getValue().toString().equals("Modifier")?"":"This field is required.");*/
	}

	private void loadPackage()
	{
		String sqlC = "select iUnitId, vUnitName from master.tbUnitInfo where"+
				" vUnitType = 'Finish' and iActive = 1 order by vUnitName";
		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbPackageName.addItem(element[0].toString());
			cmbPackageName.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void addEditUnit()
	{
		String unitId = cm.getMultiComboValue(cmbPackageName);
		String addEdit = unitId.isEmpty()? "Add":"Edit";

		AddEditItemUnit win = new AddEditItemUnit(sessionBean, addEdit, unitId, "Finish");
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event -> loadPackage());
	}

	private void addEditCategory()
	{
		String catId = cm.getComboValue(cmbFinishCategory);
		String addEdit = catId.isEmpty()? "Add":"Edit";
		String type = ogItemType.getValue().toString();

		AddEditItemCategory win = new AddEditItemCategory(sessionBean, addEdit, catId, type);
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event -> loadCategory());
	}

	private Component buildComponent()
	{
		GridLayout grid = new GridLayout(8, 9);
		grid.setMargin(new MarginInfo(true, true, false, true));
		grid.setSpacing(true);
		grid.setSizeFull();

		ogItemType = new OptionGroup();
		ogItemType.addItem("Menu");
		ogItemType.addItem("Modifier");
		ogItemType.addItem("Both");
		ogItemType.select("Menu");
		ogItemType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		ogItemType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		grid.addComponent(new Label("Type: "), 0, 0);
		grid.addComponent(ogItemType, 1, 0, 3, 0);

		cmbBranchName = new MultiComboBox();
		cmbBranchName.setWidth("350px");
		cmbBranchName.setInputPrompt("Select Branch Name");
		cmbBranchName.setRequired(true);
		cmbBranchName.setRequiredError("This field is required.");
		Label lbl = new Label("Branch Name: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 1);
		grid.addComponent(cmbBranchName, 1, 1, 3, 1);

		txtItemName = new TextField();
		txtItemName.setImmediate(true);
		txtItemName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtItemName.setWidth("350px");
		txtItemName.setInputPrompt("Menu Name");
		txtItemName.setRequired(true);
		txtItemName.setRequiredError("This field is required.");
		grid.addComponent(new Label("Menu Name: "), 0, 2);
		grid.addComponent(txtItemName, 1, 2, 3, 2);

		CssLayout group = new CssLayout();
		group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		cmbPackageName = new MultiComboBox();
		cmbPackageName.setWidth("310px");
		cmbPackageName.setInputPrompt("Select Package Name");
		cmbPackageName.setDescription("Select one item to edit");
		cmbPackageName.setRequired(true);
		cmbPackageName.setRequiredError("This field is required.");
		btnAddPackage = new Button();
		btnAddPackage.setIcon(FontAwesome.PLUS);
		btnAddPackage.setStyleName(ValoTheme.BUTTON_TINY);
		btnAddPackage.setDescription("Add Package Name");
		group.addComponents(cmbPackageName, btnAddPackage);
		grid.addComponent(new Label("Package(s): "), 0, 3);
		grid.addComponent(group, 1, 3, 3, 3);

		grid.addComponent(buildTable(), 0, 4, 4, 4);

		cmbColor = new ComboBox();
		cmbColor.setImmediate(true);
		cmbColor.setNullSelectionAllowed(false);
		cmbColor.setFilteringMode(FilteringMode.CONTAINS);
		cmbColor.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbColor.setInputPrompt("Select menu color");
		cmbColor.setWidth("200px");
		grid.addComponent(new Label("Back Color: "), 0, 5);
		grid.addComponent(cmbColor, 1, 5, 3, 5);
		loadColors();

		txtDescription = new TextField();
		txtDescription.setImmediate(true);
		txtDescription.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDescription.setWidth("650px");
		txtDescription.setInputPrompt("Description");
		grid.addComponent(new Label("Description: "), 0, 6);
		grid.addComponent(txtDescription, 1, 6, 4, 6);

		chkOnlineMenu = new CheckBox("Online Menu");
		chkOnlineMenu.setImmediate(true);
		chkOnlineMenu.setValue(true);
		chkOnlineMenu.addStyleName(ValoTheme.CHECKBOX_SMALL);
		chkOnlineMenu.setDescription("Save as an online menu.");
		//grid.addComponent(chkOnlineMenu, 0, 5);
		//grid.setComponentAlignment(chkOnlineMenu, Alignment.MIDDLE_CENTER);

		chkInventory = new CheckBox("Inventory");
		chkInventory.setImmediate(true);
		chkInventory.setValue(true);
		chkInventory.addStyleName(ValoTheme.CHECKBOX_SMALL);
		chkInventory.setDescription("Save as an inventory item.");

		Label lblRaw = new Label(" ");
		lblRaw.setWidth("20px");

		CssLayout cssRawCat = new CssLayout();
		cssRawCat.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		cssRawCat.addComponents(chkOnlineMenu, lblRaw, chkInventory);
		grid.addComponent(cssRawCat, 0, 7, 4, 7);

		group = new CssLayout();
		group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		cmbFinishCategory = new ComboBox();
		cmbFinishCategory.setImmediate(true);
		cmbFinishCategory.setFilteringMode(FilteringMode.CONTAINS);
		cmbFinishCategory.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbFinishCategory.setInputPrompt("Select Category");
		cmbFinishCategory.setWidth("280px");
		cmbFinishCategory.setDescription("Select one item to edit");
		cmbFinishCategory.setRequired(true);
		cmbFinishCategory.setRequiredError("This field is required.");
		Label lblc = new Label("Menu Category: ");
		lblc.setWidth("-1px");
		grid.addComponent(lblc, 4, 0);
		btnAddCategory = new Button();
		btnAddCategory.setIcon(FontAwesome.PLUS);
		btnAddCategory.setStyleName(ValoTheme.BUTTON_TINY);
		btnAddCategory.setDescription("Add Category");
		cm.setAuthorize(sessionBean.getUserId(), "categoryInfo");
		btnAddCategory.setEnabled(cm.insert);
		group.addComponents(cmbFinishCategory, btnAddCategory);
		grid.addComponent(group, 5, 0, 7, 0);

		cmbSalesType = new MultiComboBox();
		cmbSalesType.setInputPrompt("Select Sales Type");
		cmbSalesType.setFilteringMode(FilteringMode.CONTAINS);
		cmbSalesType.setWidth("300px");
		cmbSalesType.setRequired(true);
		cmbSalesType.setRequiredError("This field is required.");
		grid.addComponent(new Label("Sales Type: "), 4, 1);
		grid.addComponent(cmbSalesType, 5, 1, 7, 1);

		cmbVatCategory = new ComboBox();
		cmbVatCategory.setImmediate(true);
		cmbVatCategory.setFilteringMode(FilteringMode.CONTAINS);
		cmbVatCategory.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbVatCategory.setInputPrompt("Select VAT Category");
		cmbVatCategory.setWidth("50%");
		cmbVatCategory.setRequired(true);
		cmbVatCategory.setRequiredError("This field is required.");
		lbl = new Label("VAT Category: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 4, 2);
		grid.addComponent(cmbVatCategory, 5, 2, 7, 2);

		ogVatOption = new OptionGroup();
		ogVatOption.addItem("Inclusive");
		ogVatOption.addItem("Exclusive");
		ogVatOption.select("Inclusive");
		ogVatOption.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		ogVatOption.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		grid.addComponent(new Label("VAT Option:"), 4, 3);
		grid.addComponent(ogVatOption, 5, 3, 7, 3);

		VerticalLayout vert = new VerticalLayout();

		Image = new ImageUpload("Item Image");
		Image.upload.setButtonCaption("Image");

		btnClearImg = new Button("Clear Photo");
		btnClearImg.setStyleName(ValoTheme.BUTTON_TINY);
		vert.addComponents(Image, btnClearImg);
		grid.addComponent(vert, 5, 4, 7, 8);

		return grid;
	}

	private void loadColors()
	{
		cmbColor.addItem("#0049b6");
		cmbColor.setItemIcon("#0049b6", new ThemeResource("../dashboard/colors/0049b6.gif"));
		cmbColor.setItemCaption("#0049b6", "Deep Blue");

		cmbColor.addItem("#6da8ff");
		cmbColor.setItemIcon("#6da8ff", new ThemeResource("../dashboard/colors/6da8ff.gif"));
		cmbColor.setItemCaption("#6da8ff", "Blue");

		cmbColor.addItem("#1a1a1a");
		cmbColor.setItemIcon("#1a1a1a", new ThemeResource("../dashboard/colors/1a1a1a.gif"));
		cmbColor.setItemCaption("#1a1a1a", "Black");

		cmbColor.addItem("#24b600");		cmbColor.setItemIcon("#24b600", new ThemeResource("../dashboard/colors/24b600.gif"));
		cmbColor.setItemCaption("#24b600", "Green");

		cmbColor.addItem("#9200b6");
		cmbColor.setItemIcon("#9200b6", new ThemeResource("../dashboard/colors/9200b6.gif"));
		cmbColor.setItemCaption("#9200b6", "Purple");

		cmbColor.addItem("#d324ff");
		cmbColor.setItemIcon("#d324ff", new ThemeResource("../dashboard/colors/d324ff.gif"));
		cmbColor.setItemCaption("#d324ff", "Pink");

		cmbColor.addItem("#db0083");
		cmbColor.setItemIcon("#db0083", new ThemeResource("../dashboard/colors/db0083.gif"));
		cmbColor.setItemCaption("#db0083", "Deep Pink");

		cmbColor.addItem("#ff0000");
		cmbColor.setItemIcon("#ff0000", new ThemeResource("../dashboard/colors/ff0000.gif"));
		cmbColor.setItemCaption("#ff0000", "Red");

		cmbColor.addItem("#ff9900");
		cmbColor.setItemIcon("#ff9900", new ThemeResource("../dashboard/colors/ff9900.gif"));
		cmbColor.setItemCaption("#ff9900", "Orange");

		cmbColor.select("#1a1a1a");
	}

	private Table buildTable()
	{
		tblUnitPriceList = new Table();
		tblUnitPriceList.setSelectable(true);
		tblUnitPriceList.setColumnCollapsingAllowed(true);
		tblUnitPriceList.addStyleName(ValoTheme.TABLE_SMALL);
		tblUnitPriceList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblUnitPriceList.setPageLength(4);
		tblUnitPriceList.setWidth("650px");

		tblUnitPriceList.addContainerProperty("Package Id", Label.class, new Label(), null, null, Align.CENTER);
		tblUnitPriceList.setColumnCollapsed("Package Id", true);

		tblUnitPriceList.addContainerProperty("Package Name", Label.class, new Label(), null, null, Align.CENTER);

		tblUnitPriceList.addContainerProperty("Main Price", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblUnitPriceList.setColumnWidth("Main Price", 100);

		tblUnitPriceList.addContainerProperty("Actual Price", TextField.class, new TextField(), null, null, Align.CENTER);
		tblUnitPriceList.setColumnWidth("Actual Price", 100);

		tblUnitPriceList.addContainerProperty("VAT Amount", TextField.class, new TextField(), null, null, Align.CENTER);
		tblUnitPriceList.setColumnWidth("VAT Amount", 100);

		tblUnitPriceList.addContainerProperty("Final Price", TextField.class, new TextField(), null, null, Align.CENTER);
		tblUnitPriceList.setColumnWidth("Final Price", 100);

		tblUnitPriceList.addContainerProperty("Fixed Cost", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblUnitPriceList.setColumnWidth("Final Price", 80);

		tblUnitPriceList.addContainerProperty("Remove", Button.class, new Button(), null, null, Align.CENTER);
		tblUnitPriceList.setColumnCollapsed("Remove", true);

		return tblUnitPriceList;
	}

	public void tableRowAdd(int ar)
	{
		try
		{
			tbLblPackageId.add(ar, new Label());
			tbLblPackageId.get(ar).setWidth("100%");
			tbLblPackageId.get(ar).setImmediate(true);

			tbLblPackageName.add(ar, new Label());
			tbLblPackageName.get(ar).setWidth("100%");
			tbLblPackageName.get(ar).setImmediate(true);

			tbTxtMainPrice.add(ar, new CommaField());
			tbTxtMainPrice.get(ar).setWidth("100%");
			tbTxtMainPrice.get(ar).setImmediate(true);
			tbTxtMainPrice.get(ar).setRequired(true);
			tbTxtMainPrice.get(ar).setInputPrompt("Main Price");
			tbTxtMainPrice.get(ar).setRequiredError("This field is required.");
			tbTxtMainPrice.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);

			tbTxtActualPrice.add(ar, new TextField());
			tbTxtActualPrice.get(ar).setWidth("100%");
			tbTxtActualPrice.get(ar).setImmediate(true);
			tbTxtActualPrice.get(ar).setReadOnly(true);
			tbTxtActualPrice.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtActualPrice.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbTxtVatAmount.add(ar, new TextField());
			tbTxtVatAmount.get(ar).setWidth("100%");
			tbTxtVatAmount.get(ar).setImmediate(true);
			tbTxtVatAmount.get(ar).setReadOnly(true);
			tbTxtVatAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtVatAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbTxtFinalPrice.add(ar, new TextField());
			tbTxtFinalPrice.get(ar).setWidth("100%");
			tbTxtFinalPrice.get(ar).setImmediate(true);
			tbTxtFinalPrice.get(ar).setReadOnly(true);
			tbTxtFinalPrice.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtFinalPrice.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbTxtFixedCost.add(ar, new CommaField());
			tbTxtFixedCost.get(ar).setWidth("100%");
			tbTxtFixedCost.get(ar).setImmediate(true);
			tbTxtFixedCost.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtFixedCost.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbTxtFixedCost.get(ar).addValueChangeListener(event -> addChanges());

			tbBtnRemove.add(ar, new Button());
			tbBtnRemove.get(ar).setWidth("100%");
			tbBtnRemove.get(ar).setImmediate(true);
			tbBtnRemove.get(ar).setIcon(FontAwesome.REMOVE);
			tbBtnRemove.get(ar).setStyleName(ValoTheme.BUTTON_TINY);
			tbBtnRemove.get(ar).setDescription("Remove employee");
			tbBtnRemove.get(ar).addClickListener(event -> clearRow(ar));

			addMainAction(ar);

			tblUnitPriceList.addItem(new Object[]{tbLblPackageId.get(ar), tbLblPackageName.get(ar),
					tbTxtMainPrice.get(ar), tbTxtActualPrice.get(ar), tbTxtVatAmount.get(ar),
					tbTxtFinalPrice.get(ar), tbTxtFixedCost.get(ar), tbBtnRemove.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void addMainAction(int ar)
	{
		tbTxtMainPrice.get(ar).addValueChangeListener(event ->
		{ calculateVatData(ar); addChanges(); });

		tbTxtFixedCost.get(ar).addValueChangeListener(event ->
		{ calculateVatData(ar); addChanges(); });
	}

	private void clearRow(int ar)
	{
		if (!tbLblPackageId.get(ar).getValue().toString().isEmpty() &&
				!tbTxtMainPrice.get(ar).getValue().toString().isEmpty())
		{
			tblUnitPriceList.removeItem(ar);

			tbLblPackageId.get(ar).setValue("");
			tbLblPackageName.get(ar).setValue("");
			tbTxtMainPrice.get(ar).setValue("");

			tbTxtActualPrice.get(ar).setReadOnly(false);
			tbTxtActualPrice.get(ar).setValue("");
			tbTxtActualPrice.get(ar).setReadOnly(true);

			tbTxtVatAmount.get(ar).setReadOnly(false);
			tbTxtVatAmount.get(ar).setValue("");
			tbTxtVatAmount.get(ar).setReadOnly(true);

			tbTxtFinalPrice.get(ar).setReadOnly(false);
			tbTxtFinalPrice.get(ar).setValue("");
			tbTxtFinalPrice.get(ar).setReadOnly(true);

			tbTxtFixedCost.get(ar).setValue("");
		}
	}

	public boolean getTableData()
	{
		boolean ret = false;
		for (int i = 0; i < tbLblPackageId.size(); i++)
		{
			if (!tbLblPackageId.get(i).getValue().toString().isEmpty() &&
					!tbTxtMainPrice.get(i).getValue().toString().isEmpty())
			{ ret = true; break; }
		}
		return ret;
	}

	private String detailsSql(String itemId)
	{
		String sql = " ";
		try
		{
			if (flag.equals("Edit") && changes)
			{ sql += "update master.tbFinishedItemPrice set iActive = 0 where vItemId = '"+itemId+"'"; }
			for (int i = 0; i < tbLblPackageId.size(); i++)
			{
				if (!tbLblPackageId.get(i).getValue().toString().isEmpty() &&
						cm.getAmtValue(tbTxtMainPrice.get(i)) >= 0)
				{
					if (i == 0)
					{
						sql += " insert into master.tbFinishedItemPrice(vItemId, iUnitId, mMainPrice, mFixedCost,"+
								" iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate) values ";
					}
					sql += "('"+itemId+"', '"+tbLblPackageId.get(i).getValue().toString()+"',"+
							" '"+cm.getAmtValue(tbTxtMainPrice.get(i))+"', '"+cm.getAmtValue(tbTxtFixedCost.get(i))+"', 1,"+
							" '"+sessionBean.getUserId()+"', getdate(), '"+sessionBean.getUserId()+"', getdate()),";
				}
			}
		}
		catch (Exception e)
		{ System.out.println("Here sql can't generate: "+e); }
		return sql.substring(0, sql.length()-1);
	}

	private String imagePath(int flag, String imageId)
	{
		String basePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"".replace("\\","/")+"/VAADIN/rpttmp/";
		String ReturnImagePath = "0";

		if (flag == 1)
		{
			// image move
			if (Image.fileName.trim().length()>0)
			{
				try 
				{
					if (Image.fileName.toString().endsWith(".jpg"))
					{
						String Origin = basePath+Image.fileName.trim();
						String Destin = itemImage+imageId+".jpg";
						//System.out.println(Origin+"\n"+Destin);

						//Copying file
						fileCopy(Origin, Destin);
						ReturnImagePath = Destin;
					}
					else
					{
						Image.upload.focus();
						cm.showNotification("warning", "Warning!", "Invalid image format(jpg only).");
					}
				}
				catch(IOException e) 
				{ e.printStackTrace(); }
			}
			return ReturnImagePath;
		}
		return null;
	}

	private void fileCopy(String Origin, String Destin) throws IOException
	{
		ImageResizer.resize(Origin, Destin, .8);
		/*try
		{
			File f1 = new File(Destin);
			if (f1.isFile())
			{ f1.delete(); }
		}
		catch(Exception exp)
		{ System.out.println("Photo move: "+exp); }

		FileInputStream ff = new FileInputStream(Origin);

		File ft = new File(Destin);
		FileOutputStream fos = new FileOutputStream(ft);

		while (ff.available() != 0)
		{ fos.write(ff.read()); }
		fos.close();
		ff.close();*/
	}

	private void addChanges()
	{
		if (flag.equals("Edit") && action)
		{ changes = true; }
	}

	public void txtClear()
	{
		txtItemName.setValue("");
		txtDescription.setValue("");
		cmbPackageName.unselectAll();
		hmPrice.clear();
		tableClear();
		//cmbPackageName.select("1");
	}

	public void tableClear()
	{
		tblUnitPriceList.removeAllItems();
		tbLblPackageId.clear();
		Image.image.removeAllComponents();
		Image.status.setValue("");
	}

	public void getValue(ItemInfoModel iim)
	{
		iim.setItemType(ogItemType.getValue().toString());
		String branchIds = cm.getMultiComboValue(cmbBranchName);
		String unitIds = cm.getMultiComboValue(cmbPackageName);
		String saleTypeIds = cm.getMultiComboValue(cmbSalesType);

		iim.setBranchIds(branchIds);
		iim.setItemName(txtItemName.getValue().toString().trim());
		iim.setUnitIds(unitIds);
		iim.setCategoryId(cm.getComboValue(cmbFinishCategory));
		iim.setSalesTypeIds(saleTypeIds);
		iim.setVatCategoryId(cmbVatCategory.getValue().toString());
		iim.setVatOption(ogVatOption.getValue().toString());
		iim.setCreatedBy(sessionBean.getUserId());
		iim.setOnlineMenu(chkOnlineMenu.getValue().booleanValue()? 1:0);
		iim.setItemColor(cm.getComboValue(cmbColor));
		iim.setItemRaw(chkInventory.getValue().booleanValue()? 1:0);
		iim.setDescription(txtDescription.getValue().toString().trim());

		//Attachment
		File imagePath = null;
		String imagePathItem = Image.success ? (imagePath(1, iim.getItemId()) == null ? "0" :
			imagePath(1, iim.getItemId())) : editImage;
		//System.out.println(imagePathItem);
		if (!imagePathItem.equals("0") && !imagePathItem.isEmpty() && imagePathItem != null)
		{
			try
			{
				imagePath = new File(imagePathItem);
				imageInBytes = new byte[(int)imagePath.length()];
				FileInputStream inputStream = new FileInputStream(imagePath); //input stream object create to read the file
				inputStream.read(imageInBytes); //here input stream object read the file
				inputStream.close();
			}
			catch (Exception e)
			{ System.out.println("Here image can't read: "+e); }
		}

		iim.setImagePath(imagePathItem);
		iim.setItemImage(imageInBytes);

		iim.setUnitRateSql(detailsSql(iim.getItemId()));
		iim.setUnitRateChange(changes);
	}

	public void setValue(ItemInfoModel iim)
	{
		ogItemType.setValue(iim.getItemType());
		txtItemName.setValue(iim.getItemName());

		cmbPackageName.unselectAll();
		cmbFinishCategory.setValue(iim.getCategoryId());
		cmbVatCategory.setValue(iim.getVatCategoryId());
		ogVatOption.setValue(iim.getVatOption());
		cmbColor.setValue(iim.getItemColor());
		txtDescription.setValue(iim.getDescription());

		chkInventory.setValue(iim.getItemRaw()==1? true:false);

		chkOnlineMenu.setValue(iim.getOnlineMenu()==1? true:false);

		//System.out.println(iim.getImagePath());
		Image.setImage(iim.getImagePath());
		editImage = iim.getImagePath();

		setMultiCombo(iim);
		setValueUnit(iim);
	}

	private void setValueUnit(ItemInfoModel iim)
	{
		String sql = "select iUnitId, mMainPrice, mFixedCost from master.tbFinishedItemPrice where"+
				" vItemId = '"+iim.getItemId()+"' and iActive = 1 order by iUnitId";
		//System.out.println(sql);
		int ar = 0;
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbPackageName.select(element[0].toString());
			if (tbLblPackageId.get(ar).getValue().toString().equals(element[0].toString()))
			{
				tbTxtMainPrice.get(ar).setValue(Double.parseDouble(element[1].toString()));
				tbTxtFixedCost.get(ar).setValue(Double.parseDouble(element[2].toString()));
			}
			ar++;
		}
		action = true;
	}

	private void setMultiCombo(ItemInfoModel iim)
	{
		String sql = "select Item, 'Branch' vValue from dbo.Split('"+iim.getBranchIds().trim()+"') union all"+
				" select Item, 'Sales' vValue from dbo.Split('"+iim.getSalesTypeIds().trim()+"')";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			if (element[1].toString().equals("Branch"))
			{ cmbBranchName.select(element[0].toString().trim()); }
			else if (element[1].toString().equals("Sales"))
			{ cmbSalesType.select(element[0].toString().trim()); }
		}
	}
}
