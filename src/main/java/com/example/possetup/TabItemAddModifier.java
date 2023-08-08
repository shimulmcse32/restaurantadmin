package com.example.possetup;

import java.util.ArrayList;
import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonMethod;
import com.common.share.MultiComboBox;
import com.common.share.SessionBean;
import com.example.model.ItemInfoModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TabItemAddModifier extends HorizontalLayout
{
	private SessionBean sessionBean;

	public TextField txtArabicName, txtKitchenName, txtBarcode;

	public ComboBox cmbItemCompany, cmbModifier;
	public MultiComboBox cmbSupplierName;
	public Button btnAddModifier, btnAddSupplier, btnAddCompany;

	private Table tblModifierList;
	private ArrayList<Label> tbLblModiItemId = new ArrayList<Label>();
	private ArrayList<Label> tbLblModiItemName = new ArrayList<Label>();
	private ArrayList<Label> tbLblVatDetails = new ArrayList<Label>();
	private ArrayList<CommaField> tbTxtMainPrice = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtQuantity = new ArrayList<CommaField>();
	private ArrayList<TextField> tbTxtActualPrice = new ArrayList<TextField>();
	private ArrayList<TextField> tbTxtVatAmount = new ArrayList<TextField>();
	private ArrayList<TextField> tbTxtFinalPrice = new ArrayList<TextField>();
	//private ArrayList<Button> tbBtnRemove = new ArrayList<Button>();

	private CommonMethod cm;
	private String flag = "";
	public boolean changes = false, action = false;

	public TabItemAddModifier(SessionBean sessionBean, String flag)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		cm = new CommonMethod(sessionBean);
		setSizeFull();

		addComponent(buildComponent());

		addActions();
	}

	private void addActions()
	{
		btnAddModifier.addClickListener(event -> addEditModifier());

		btnAddCompany.addClickListener(event -> addEditCompany());

		btnAddSupplier.addClickListener(event -> addEditSupplier());

		cmbModifier.addValueChangeListener(event ->
		{ setModifierDetails(); addChanges(); });
	}

	private void loadCombos()
	{
		loadSupplier();
		loadItemComapny();
		loadModifier();
	}

	private void loadSupplier()
	{
		String sqls = "select vSupplierId, vSupplierCode, vSupplierName, dbo.funGetNumeric(vSupplierCode)"+
				" iCode from master.tbSupplierMaster where iActive = 1 order by iCode asc";
		for (Iterator<?> iter = cm.selectSql(sqls).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupplierName.addItem(element[0].toString());
			cmbSupplierName.setItemCaption(element[0].toString(), element[1].toString()+" - "+element[2].toString());
		}
	}

	private void loadItemComapny()
	{
		String sqlC = "select vCompanyId, vCompanyName from master.tbItemCompanyMaster"+
				" where iActive = 1 order by vCompanyName";
		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbItemCompany.addItem(element[0].toString());
			cmbItemCompany.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadModifier()
	{
		String sqls = "select distinct vModifierId, vModifierName from master.tbModifierMaster"+
				" where iActive = 1 order by vModifierName";
		for (Iterator<?> iter = cm.selectSql(sqls).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbModifier.addItem(element[0].toString());
			cmbModifier.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void setModifierDetails()
	{
		int ar = 0;
		String modId = cmbModifier.getValue() != null? cmbModifier.getValue().toString():"";
		String sql = "select mm.vItemId, fi.vItemName, mMainPrice, mQuantity from master.tbModifierMaster mm,"+
				" master.tbFinishedItemInfo fi where mm.vItemId = fi.vItemId and mm.vModifierId = '"+modId+"'"+
				" and mm.iActive = 1";
		//System.out.println(sql);
		tableClear();
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (tbLblModiItemId.size() <= ar)
			{ tableRowAdd(ar); }

			tbLblModiItemId.get(ar).setValue(element[0].toString());
			tbLblModiItemName.get(ar).setValue(element[1].toString());
			tbTxtMainPrice.get(ar).setValue(Double.parseDouble(element[2].toString()));
			tbTxtQuantity.get(ar).setValue(Double.parseDouble(element[3].toString()));
			setVatDetails(ar, element[0].toString());
			calculateVatData(ar);

			ar++;
		}
	}

	private void addEditModifier()
	{
		String modiId = cmbModifier.getValue() != null? cmbModifier.getValue().toString():"";
		String addEdit = modiId.isEmpty()? "Add":"Edit";

		AddEditModifier win = new AddEditModifier(sessionBean, addEdit, modiId, "Shortcut");
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(event -> loadModifier());
	}

	private void addEditSupplier()
	{
		String suppId = cm.getMultiComboValue(cmbSupplierName);
		String addEdit = suppId.isEmpty()? "Add":"Edit";

		AddEditSupplierInfo win = new AddEditSupplierInfo(sessionBean, addEdit, suppId);
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(event -> loadSupplier());
	}

	private void addEditCompany()
	{
		String companyId = cmbItemCompany.getValue() != null? cmbItemCompany.getValue().toString():"";
		String addEdit = companyId.isEmpty()? "Add":"Edit";

		AddEditItemCompany win = new AddEditItemCompany(sessionBean, addEdit, companyId);
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(event -> loadItemComapny());
	}

	private Component buildComponent()
	{
		GridLayout grid = new GridLayout(4, 4);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtArabicName = new TextField();
		txtArabicName.setImmediate(true);
		txtArabicName.setStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
		txtArabicName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtArabicName.setWidth("330px");
		txtArabicName.setInputPrompt("Menu Name (Arabic)");
		grid.addComponent(new Label("Menu Name (Arabic): "), 0, 0);
		grid.addComponent(txtArabicName, 1, 0);

		txtKitchenName = new TextField();
		txtKitchenName.setImmediate(true);
		txtKitchenName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtKitchenName.setWidth("100%");
		txtKitchenName.setInputPrompt("Menu Name (Kitchen)");
		Label lblk = new Label("Menu Name (Kitchen): ");
		lblk.setWidth("-1px");
		grid.addComponent(lblk, 0, 1);
		grid.addComponent(txtKitchenName, 1, 1);

		CssLayout group = new CssLayout();
		group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		cmbModifier = new ComboBox();
		cmbModifier.setFilteringMode(FilteringMode.CONTAINS);
		cmbModifier.setInputPrompt("Select Modifier(s) Set");
		cmbModifier.setWidth("290px");
		cmbModifier.setDescription("Select modifier(s) set");
		cmbModifier.addStyleName(ValoTheme.COMBOBOX_TINY);
		btnAddModifier = new Button();
		btnAddModifier.setIcon(FontAwesome.PLUS);
		btnAddModifier.setStyleName(ValoTheme.BUTTON_TINY);
		btnAddModifier.setDescription("Add Modifier(s) Set");
		cm.setAuthorize(sessionBean.getUserId(), "modiMas");
		btnAddModifier.setEnabled(cm.insert);
		group.addComponents(cmbModifier, btnAddModifier);
		grid.addComponent(new Label("Modifier(s) Set: "), 0, 2);
		grid.addComponent(group, 1, 2);

		txtBarcode = new TextField();
		txtBarcode.setImmediate(true);
		txtBarcode.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtBarcode.setWidth("100%");
		txtBarcode.setInputPrompt("Barcode");
		grid.addComponent(new Label("Barcode: "), 2, 0);
		grid.addComponent(txtBarcode, 3, 0);

		group = new CssLayout();
		group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		cmbItemCompany = new ComboBox();
		cmbItemCompany.setWidth("300px");
		cmbItemCompany.setInputPrompt("Select Menu Company Name");
		cmbItemCompany.setImmediate(true);
		cmbItemCompany.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbItemCompany.setFilteringMode(FilteringMode.CONTAINS);
		Label lbl = new Label("Company Name: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 2, 1);
		btnAddCompany = new Button();
		btnAddCompany.setIcon(FontAwesome.PLUS);
		btnAddCompany.setStyleName(ValoTheme.BUTTON_TINY);
		btnAddCompany.setDescription("Add Company(Menu)");
		group.addComponents(cmbItemCompany, btnAddCompany);
		grid.addComponent(group, 3, 1);

		CssLayout groups = new CssLayout();
		groups.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		cmbSupplierName = new MultiComboBox();
		cmbSupplierName.setInputPrompt("Select Supplier Name");
		cmbSupplierName.setWidth("300px");
		cmbSupplierName.setDescription("Select supplier name");
		grid.addComponent(new Label("Supplier Name: "), 2, 2);
		btnAddSupplier = new Button();
		btnAddSupplier.setIcon(FontAwesome.PLUS);
		btnAddSupplier.setStyleName(ValoTheme.BUTTON_TINY);
		cm.setAuthorize(sessionBean.getUserId(), "suppInfo");
		btnAddSupplier.setEnabled(cm.insert);
		btnAddSupplier.setDescription("Add Supplier");
		groups.addComponents(cmbSupplierName, btnAddSupplier);
		grid.addComponent(groups, 3, 2);

		grid.addComponent(buildTable(), 0, 3, 3, 3);

		loadCombos();
		return grid;
	}

	private Table buildTable()
	{
		tblModifierList = new Table();
		tblModifierList.setSelectable(true);
		tblModifierList.setColumnCollapsingAllowed(true);
		tblModifierList.addStyleName(ValoTheme.TABLE_SMALL);
		tblModifierList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblModifierList.setPageLength(6);
		tblModifierList.setWidth("100%");

		tblModifierList.addContainerProperty("Menu Id", Label.class, new Label(), null, null, Align.CENTER);
		tblModifierList.setColumnCollapsed("Menu Id", true);

		tblModifierList.addContainerProperty("Modifier Menu", Label.class, new Label(), null, null, Align.CENTER);

		tblModifierList.addContainerProperty("VAT Details", Label.class, new Label(), null, null, Align.CENTER);

		tblModifierList.addContainerProperty("Main Price", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Main Price", 80);

		tblModifierList.addContainerProperty("Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Qty", 60);

		tblModifierList.addContainerProperty("Actual Price", TextField.class, new TextField(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Actual Price", 80);

		tblModifierList.addContainerProperty("VAT Amount", TextField.class, new TextField(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("VAT Amount", 80);

		tblModifierList.addContainerProperty("Final Price", TextField.class, new TextField(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Final Price", 80);

		/*tblModifierList.addContainerProperty("Rem", Button.class, new Button(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Rem", 50);*/

		return tblModifierList;
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblModiItemId.add(ar, new Label());
			tbLblModiItemId.get(ar).setWidth("100%");
			tbLblModiItemId.get(ar).setImmediate(true);
			tbLblModiItemId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblModiItemName.add(ar, new Label());
			tbLblModiItemName.get(ar).setWidth("100%");
			tbLblModiItemName.get(ar).setImmediate(true);
			tbLblModiItemName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblVatDetails.add(ar, new Label());
			tbLblVatDetails.get(ar).setWidth("100%");
			tbLblVatDetails.get(ar).setImmediate(true);
			tbLblVatDetails.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbTxtMainPrice.add(ar, new CommaField());
			tbTxtMainPrice.get(ar).setWidth("100%");
			tbTxtMainPrice.get(ar).setImmediate(true);
			tbTxtMainPrice.get(ar).setEnabled(false);
			tbTxtMainPrice.get(ar).setInputPrompt("Main Price");
			tbTxtMainPrice.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtMainPrice.get(ar).addValueChangeListener(event ->
			{
				calculateVatData(ar);
				addChanges();
			});

			tbTxtQuantity.add(ar, new CommaField());
			tbTxtQuantity.get(ar).setWidth("100%");
			tbTxtQuantity.get(ar).setImmediate(true);
			tbTxtQuantity.get(ar).setRequired(true);
			tbTxtQuantity.get(ar).setEnabled(false);
			tbTxtQuantity.get(ar).setRequiredError("This field is required.");
			tbTxtQuantity.get(ar).setInputPrompt("Quantity");
			tbTxtQuantity.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtQuantity.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbTxtQuantity.get(ar).addValueChangeListener(event ->
			{
				calculateVatData(ar);
				addChanges();
			});

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

			/*tbBtnRemove.add(ar, new Button());
			tbBtnRemove.get(ar).setWidth("100%");
			tbBtnRemove.get(ar).setImmediate(true);
			tbBtnRemove.get(ar).setIcon(FontAwesome.REMOVE);
			tbBtnRemove.get(ar).setStyleName(ValoTheme.BUTTON_TINY);
			tbBtnRemove.get(ar).addStyleName(ValoTheme.BUTTON_DANGER);
			tbBtnRemove.get(ar).setDescription("Remove row");
			tbBtnRemove.get(ar).addClickListener(new ClickListener()
			{
				public void buttonClick(ClickEvent event)
				{
					if (tbLblModiItemId.size() > 1)
					{
						removeRow(ar);
						addChanges();
					}
				}
			});*/

			tblModifierList.addItem(new Object[]{tbLblModiItemId.get(ar), tbLblModiItemName.get(ar),
					tbLblVatDetails.get(ar), tbTxtMainPrice.get(ar), tbTxtQuantity.get(ar), tbTxtActualPrice.get(ar),
					tbTxtVatAmount.get(ar), tbTxtFinalPrice.get(ar)/*, tbBtnRemove.get(ar)*/}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void calculateVatData(int ar)
	{
		if (!tbLblModiItemId.get(ar).getValue().toString().isEmpty() && 
				!tbTxtMainPrice.get(ar).getValue().toString().isEmpty())
		{
			String vatRule = tbLblVatDetails.get(ar).getValue().toString().contains("Inc")?"Inclusive":"Exclusive";
			double vatPecent = tbLblVatDetails.get(ar).getValue().toString().contains("5")?5:0;

			double qty = Double.parseDouble(tbTxtQuantity.get(ar).getValue().toString().isEmpty()?"0":
				tbTxtQuantity.get(ar).getValue().toString().replaceAll(",", ""));

			double mainRate = Double.parseDouble(tbTxtMainPrice.get(ar).getValue().toString().isEmpty()?
					"0":tbTxtMainPrice.get(ar).getValue().toString());

			double mainAmount = (qty * mainRate);

			double vatAmount = cm.getRound((mainAmount*vatPecent) / (vatRule.equals("Inclusive")?(100+vatPecent):100));

			double actualRate = 0;
			if (vatRule.equals("Inclusive"))
			{ actualRate = mainAmount - vatAmount; }
			else
			{ actualRate = mainAmount; }

			tbTxtActualPrice.get(ar).setReadOnly(false);
			tbTxtActualPrice.get(ar).setValue(cm.deciMn.format(actualRate));
			tbTxtActualPrice.get(ar).setReadOnly(true);

			tbTxtVatAmount.get(ar).setReadOnly(false);
			tbTxtVatAmount.get(ar).setValue(cm.deciMn.format(vatAmount));
			tbTxtVatAmount.get(ar).setReadOnly(true);

			tbTxtFinalPrice.get(ar).setReadOnly(false);
			tbTxtFinalPrice.get(ar).setValue(cm.deciMn.format(actualRate + vatAmount));
			tbTxtFinalPrice.get(ar).setReadOnly(true);
		}
	}

	/*private void removeRow(int ar)
	{ tblModifierList.removeItem(ar); tbLblModiItemId.clear(); }*/

	public void txtClear()
	{
		txtKitchenName.setValue("");
		txtArabicName.setValue("");
		txtBarcode.setValue("");
		cmbItemCompany.setValue(null);
		cmbSupplierName.setValue(null);
		cmbModifier.setValue(null);
	}

	public void getValue(ItemInfoModel iim)
	{
		iim.setItemNameArabic(txtArabicName.getValue().toString());
		iim.setKitchenName(txtKitchenName.getValue().toString().trim());
		iim.setBarCode(txtBarcode.getValue().toString().trim());
		String supplier = cm.getMultiComboValue(cmbSupplierName);
		iim.setSupplierIds(supplier);
		iim.setItemCompanyId(cm.getComboValue(cmbItemCompany));

		iim.setModifier(cm.getComboValue(cmbModifier));
		getModifiers(iim);
	}

	public void getModifiers(ItemInfoModel iim)
	{
		String sql = " ";
		if (flag.equals("Edit") && changes)
		{ sql += "update master.tbFinishedModifier set iActive = 0 where vItemId = '"+iim.getItemId()+"' "; }
		for (int i = 0; i < tbLblModiItemId.size(); i++)
		{
			double qty = cm.getAmtValue(tbTxtQuantity.get(i));
			if (qty > 0)
			{
				if (tbLblModiItemId.get(i).getValue() != null && !tbTxtMainPrice.get(i).getValue().toString().isEmpty())
				{
					if (i == 0)
					{
						sql += " insert into master.tbFinishedModifier(vModifierId, vItemId, vItemIdModifier, mMainPrice,"+
								" mQuantity, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate) values ";
					}
					sql += "('"+iim.getModifier()+"', '"+iim.getItemId()+"', '"+tbLblModiItemId.get(i).getValue().toString()+"',"+
							" '"+tbTxtMainPrice.get(i).getValue().toString()+"', '"+qty+"', 1, '"+sessionBean.getUserId()+"',"+
							" getdate(), '"+sessionBean.getUserId()+"', getdate()),";
				}
			}
		}
		iim.setModifierSql(sql.substring(0, sql.length()-1));
		iim.setModifierChange(changes);
		//System.out.println(sql.substring(0, sql.length()-1));
	}

	public void setValue(ItemInfoModel iim)
	{
		txtArabicName.setValue(iim.getItemNameArabic());
		txtKitchenName.setValue(iim.getKitchenName());
		cmbModifier.setValue(iim.getModifier());
		txtBarcode.setValue(iim.getBarCode());
		cmbItemCompany.setValue(iim.getItemCompanyId());

		if (!iim.getSupplierIds().isEmpty())
		{ setSupplier(iim.getSupplierIds()); }

		setModifier(iim.getItemId(), iim.getModifier());
	}

	private void setSupplier(String supplierIds)
	{
		String sqls = "select Item, 0 xero from dbo.Split('"+supplierIds+"')";
		for (Iterator<?> iter = cm.selectSql(sqls).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupplierName.select(element[0].toString());
		}
	}

	private void setModifier(String ItemId, String ModifierId)
	{
		int ar = 0;
		String sql = "select fm.vItemIdModifier, fi.vItemName, fm.mMainPrice, fm.mQuantity from master.tbFinishedModifier"+
				" fm, master.tbFinishedItemInfo fi where fm.vItemIdModifier = fi.vItemId and fm.vItemId = '"+ItemId+"'"+
				" and fm.vModifierId = '"+ModifierId+"' and fm.iActive = 1";
		//System.out.println(sql);
		tableClear();
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (tbLblModiItemId.size() <= ar)
			{ tableRowAdd(ar); }

			tbLblModiItemId.get(ar).setValue(element[0].toString());
			tbLblModiItemName.get(ar).setValue(element[1].toString());
			tbTxtMainPrice.get(ar).setValue(Double.parseDouble(element[2].toString()));
			tbTxtQuantity.get(ar).setValue(Double.parseDouble(element[3].toString()));
			setVatDetails(ar, element[0].toString());
			calculateVatData(ar);

			ar++;
		}
		action = true;
	}

	private void setVatDetails(int ar, String ItemId)
	{
		if (!ItemId.isEmpty())
		{
			String sql = "select fin.vVatOption, vcm.mPercentage, vcm.vVatCatName, convert(varchar(500),"+
					" (SELECT top 1 STUFF((SELECT ', ' + ic.vCategoryName FROM master.tbItemCategory ic where"+
					" ic.vCategoryId in (select rtrim(ltrim(Item)) collate SQL_Latin1_General_CP1_CI_AS"+
					" Item from dbo.Split(fin.vCategoryId)) FOR XML PATH('')), 1, 1, '') FROM"+
					" master.tbItemCategory ic)) vCategoryName from master.tbFinishedItemInfo fin,"+
					" master.tbVatCatMaster vcm where fin.vVatCatId = vcm.vVatCatId and vItemId = '"+ItemId+"'";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				tbLblVatDetails.get(ar).setValue(element[2].toString()+" "+element[0].toString().substring(0, 3));
			}
		}
	}

	public boolean getTableData()
	{
		boolean ret = false;
		for (int i = 0; i < tbLblModiItemId.size(); i++)
		{
			if (!tbLblModiItemId.get(i).getValue().toString().isEmpty() &&
					(tbTxtMainPrice.get(i).getValue().toString().equals("0") ||
							Double.parseDouble(tbTxtMainPrice.get(i).getValue().toString().isEmpty()?
									"0":tbTxtMainPrice.get(i).getValue().toString())>=0))
			{ ret = true; break; }
		}
		return ret;
	}

	private void addChanges()
	{
		if(flag.equals("Edit") && action)
		{ changes = true; }
	}

	public void tableClear()
	{ tblModifierList.removeAllItems(); tbLblModiItemId.clear(); }
}
