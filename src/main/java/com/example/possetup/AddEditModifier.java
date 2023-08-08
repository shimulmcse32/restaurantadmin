package com.example.possetup;

import java.util.ArrayList;
import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.MultiComboBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.ModifierSetGateway;
import com.example.model.ItemCategoryModel;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditModifier extends Window
{
	private SessionBean sessionBean;
	private String flag, modiId, shortCut;

	private TextField txtModifierName;
	private MultiComboBox cmbMenuItems;

	private Table tblModifierList;
	private ArrayList<Button> tbAddItem = new ArrayList<Button>();
	private ArrayList<ComboBox> tbCmbItemName = new ArrayList<ComboBox>();
	private ArrayList<Label> tbLblVatDetails = new ArrayList<Label>();
	private ArrayList<CommaField> tbTxtMainPrice = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtQuantity = new ArrayList<CommaField>();
	private ArrayList<TextField> tbTxtActualPrice = new ArrayList<TextField>();
	private ArrayList<TextField> tbTxtVatAmount = new ArrayList<TextField>();
	private ArrayList<TextField> tbTxtFinalPrice = new ArrayList<TextField>();
	private ArrayList<Button> tbBtnRemove = new ArrayList<Button>();

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private ModifierSetGateway msg = new ModifierSetGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private CommonMethod cm;
	private boolean changes = false, action = false;

	public AddEditModifier(SessionBean sessionBean, String flag, String modiId, String shortCut)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.modiId = modiId;
		this.shortCut = shortCut;
		this.setCaption(flag+" Modifier's Set :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(this.sessionBean);
		setWidth("950px");
		setHeight("520px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
		{ addValidation(); });

		cBtn.btnExit.addClickListener(event ->
		{ close(); });

		txtModifierName.addTextChangeListener(event ->
		{ addChanges(); });

		cmbMenuItems.addValueChangeListener(event ->
		{ addChanges(); });

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();

		loadMenuItems();
	}

	private void loadMenuItems()
	{
		String sql = "select vItemId, vItemName from master.tbFinishedItemInfo where vItemType in"+
				" ('Menu', 'Both') and iActive = 1 order by vItemName";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbMenuItems.addItem(element[0].toString());
			cmbMenuItems.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void addValidation()
	{
		if (!txtModifierName.getValue().toString().trim().isEmpty())
		{
			if (!msg.checkExist(txtModifierName.getValue().toString().replaceAll("'", "''").trim(), modiId))
			{
				if (getTableData())
				{
					cBtn.btnSave.setEnabled(false);
					insertEditData();
				}
				else
				{
					tbCmbItemName.get(0).focus();
					cm.showNotification("warning", "Warning!", "Provide data on the table.");
				}
			}
			else
			{
				txtModifierName.focus();
				cm.showNotification("warning", "Warning!", "Modifier's name already exist.");
			}
		}
		else
		{
			txtModifierName.focus();
			cm.showNotification("warning", "Warning!", "Provide modifier's name.");
		}
	}

	public boolean getTableData()
	{
		boolean ret = false;
		for (int i = 0; i < tbCmbItemName.size(); i++)
		{
			String qty = tbTxtQuantity.get(i).getValue().toString();
			if (tbCmbItemName.get(i).getValue() != null &&
					!tbTxtMainPrice.get(i).getValue().toString().isEmpty() &&
					Double.parseDouble(qty.isEmpty()?"0":qty) > 0)
			{ ret = true; break; }
		}
		return ret;
	}

	private void insertEditData()
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
					try
					{
						ItemCategoryModel mim = new ItemCategoryModel();
						String modifierIdN = flag.equals("Add")?msg.getModifierId():modiId;
						mim.setCategoryId(modifierIdN);
						mim.setCategoryName(txtModifierName.getValue().toString().replaceAll("'", "''").trim());
						mim.setDetailsSql(getDetailsSql(mim));
						mim.setItemIds(cmbMenuItems.getValue().toString().replace("[", "").replace("]", "").trim());
						mim.setModChange(changes);
						mim.setCreatedBy(sessionBean.getUserId());
						//System.out.println(changes);
						if (msg.insertEditData(mim, flag))
						{
							txtClear();
							cm.showNotification("success", "Successfull!", "All information saved successfully.");
							cBtn.btnSave.setEnabled(true);

							if (flag.equals("Edit"))
							{ close(); }
						}
						else
						{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
					}
					catch(Exception ex)
					{ System.out.println(ex); }
				}
				else if (buttonType == ButtonType.NO)
				{ cBtn.btnSave.setEnabled(true); }
			}
		});
	}

	private String getDetailsSql(ItemCategoryModel mim)
	{
		String sql = " ";
		for (int ar = 0; ar < tbCmbItemName.size(); ar++)
		{
			double qty = Double.parseDouble(tbTxtQuantity.get(ar).getValue().toString().isEmpty()?"0":
				tbTxtQuantity.get(ar).getValue().toString().replaceAll(",", "").trim());
			if (qty > 0)
			{
				if (tbCmbItemName.get(ar).getValue() != null &&
						!tbTxtMainPrice.get(ar).getValue().toString().isEmpty())
				{
					if (ar == 0)
					{
						sql = "delete from master.tbModifierMaster where vModifierId = '"+mim.getCategoryId()+"'"+
								" insert into master.tbModifierMaster(vBranchId, vModifierId, vModifierName,"+
								" vItemId, mMainPrice, mQuantity, iActive, vCreatedBy, dCreatedDate, vModifiedBy,"+
								" dModifiedDate, iSynced, vSyncedMacId) values ";
					}
					sql = sql + "('"+sessionBean.getBranchId()+"', '"+mim.getCategoryId()+"', '"+mim.getCategoryName()+"',"+
							" '"+tbCmbItemName.get(ar).getValue().toString().replaceAll("'", "''").trim()+"',"+
							" '"+tbTxtMainPrice.get(ar).getValue().toString()+"', '"+qty+"', 1, '"+sessionBean.getUserId()+"',"+
							" getdate(), '"+sessionBean.getUserId()+"', getdate(), 0, ''),";
				}
			}
		}
		return sql.substring(0, sql.length()-1);
	}

	private void setEditData()
	{
		int ar = 0;
		try
		{
			String sql = "select vModifierName, vItemId, mMainPrice, mQuantity from master.tbModifierMaster"+
					" where vModifierId = '"+modiId+"'";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				if (ar == 0)
				{ txtModifierName.setValue(element[0].toString()); }

				tbCmbItemName.get(ar).setValue(element[1].toString());
				tbTxtMainPrice.get(ar).setValue(Double.parseDouble(element[2].toString()));
				tbTxtQuantity.get(ar).setValue(Double.parseDouble(element[3].toString()));
				ar++;
			}
			setMenus();//Select menus which are having the edit modifier linked
			action = true;
			if (ar == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
		}
		catch (Exception e)
		{ System.out.println("Table Query "+e); }
	}

	private void setMenus()
	{
		try
		{
			String sql = "select vItemId, vItemName from master.tbFinishedItemInfo where vModifierId"+
					" = '"+modiId+"' and vItemType in ('Menu', 'Both') order by vItemName";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				cmbMenuItems.addItem(element[0].toString());
				cmbMenuItems.setItemCaption(element[0].toString(), element[1].toString());
				cmbMenuItems.select(element[0].toString());
			}
		}
		catch (Exception e)
		{ System.out.println("Query "+e); }
	}

	private VerticalLayout buildLayout()
	{
		VerticalLayout lay = new VerticalLayout();
		lay.setMargin(true);
		lay.setSpacing(true);

		HorizontalLayout hori = new HorizontalLayout();
		hori.setSizeFull();

		txtModifierName = new TextField();
		txtModifierName.setImmediate(true);
		txtModifierName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtModifierName.setWidth("350px");
		txtModifierName.setRequired(true);
		txtModifierName.setRequiredError("This field is required.");
		txtModifierName.setInputPrompt("Modifier's Set Name");
		txtModifierName.setInputPrompt("Modifier's set name");

		cmbMenuItems = new MultiComboBox();
		cmbMenuItems.setWidth("450px");
		cmbMenuItems.setInputPrompt("Select menu name.");
		cmbMenuItems.setDescription("Select menu to tag with modifier");
		cmbMenuItems.setEnabled(shortCut.equals("Shortcut")? false:true);

		hori.addComponents(txtModifierName, cmbMenuItems);

		lay.addComponents(hori, buildTable());

		lay.addComponent(cBtn);
		lay.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return lay;
	}

	private Table buildTable()
	{
		tblModifierList = new Table();
		tblModifierList.setSelectable(true);
		tblModifierList.setColumnCollapsingAllowed(true);
		tblModifierList.addStyleName(ValoTheme.TABLE_SMALL);
		tblModifierList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblModifierList.setPageLength(10);
		tblModifierList.setWidth("100%");

		tblModifierList.addContainerProperty("Add", Button.class, new Button(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Add", 60);

		tblModifierList.addContainerProperty("Modifier Menu", ComboBox.class, new ComboBox(), null, null, Align.CENTER);

		tblModifierList.addContainerProperty("VAT Details", Label.class, new Label(), null, null, Align.CENTER);

		tblModifierList.addContainerProperty("Main Price", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Main Price", 80);

		tblModifierList.addContainerProperty("Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Qty", 80);

		tblModifierList.addContainerProperty("Actual Price", TextField.class, new TextField(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Actual Price", 80);

		tblModifierList.addContainerProperty("VAT Amount", TextField.class, new TextField(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("VAT Amount", 80);

		tblModifierList.addContainerProperty("Price", TextField.class, new TextField(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Price", 80);

		tblModifierList.addContainerProperty("Rem", Button.class, new Button(), null, null, Align.CENTER);
		tblModifierList.setColumnWidth("Rem", 50);

		tableRowAdd(0);

		return tblModifierList;
	}

	public void tableRowAdd(int ar)
	{
		try
		{
			tbAddItem.add(ar, new Button());
			tbAddItem.get(ar).setWidth("100%");
			tbAddItem.get(ar).setImmediate(true);
			tbAddItem.get(ar).addStyleName(ValoTheme.BUTTON_TINY);
			tbAddItem.get(ar).setIcon(FontAwesome.PLUS);
			tbAddItem.get(ar).setDescription("Add Modifier Item");
			tbAddItem.get(ar).addClickListener(event -> addEditItem(ar+""));

			tbCmbItemName.add(ar, new ComboBox());
			tbCmbItemName.get(ar).setWidth("100%");
			tbCmbItemName.get(ar).setImmediate(true);
			tbCmbItemName.get(ar).setRequired(true);
			tbCmbItemName.get(ar).setRequiredError("This field is required.");
			tbCmbItemName.get(ar).setInputPrompt("Select Modifier Menu");
			tbCmbItemName.get(ar).setFilteringMode(FilteringMode.CONTAINS);
			tbCmbItemName.get(ar).setStyleName(ValoTheme.COMBOBOX_TINY);
			loadComboData(ar);
			actionTableCombo(ar);

			tbLblVatDetails.add(ar, new Label());
			tbLblVatDetails.get(ar).setWidth("100%");
			tbLblVatDetails.get(ar).setImmediate(true);

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
				if ((ar+1) == tbCmbItemName.size())
				{ tableRowAdd(tbCmbItemName.size()); }
			});

			tbTxtQuantity.add(ar, new CommaField());
			tbTxtQuantity.get(ar).setWidth("100%");
			tbTxtQuantity.get(ar).setImmediate(true);
			tbTxtQuantity.get(ar).setRequired(true);
			tbTxtQuantity.get(ar).setRequiredError("This field is required.");
			tbTxtQuantity.get(ar).setInputPrompt("Qty");
			tbTxtQuantity.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtQuantity.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbTxtQuantity.get(ar).setValue("1");
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

			tbBtnRemove.add(ar, new Button());
			tbBtnRemove.get(ar).setWidth("100%");
			tbBtnRemove.get(ar).setImmediate(true);
			tbBtnRemove.get(ar).setIcon(FontAwesome.REMOVE);
			tbBtnRemove.get(ar).setStyleName(ValoTheme.BUTTON_TINY);
			tbBtnRemove.get(ar).addStyleName(ValoTheme.BUTTON_DANGER);
			tbBtnRemove.get(ar).setDescription("Remove row");
			tbBtnRemove.get(ar).addClickListener(event ->
			{
				removeRow(ar);
				addChanges();
			});

			tblModifierList.addItem(new Object[]{tbAddItem.get(ar), tbCmbItemName.get(ar),tbLblVatDetails.get(ar),
					tbTxtMainPrice.get(ar), tbTxtQuantity.get(ar), tbTxtActualPrice.get(ar), tbTxtVatAmount.get(ar),
					tbTxtFinalPrice.get(ar), tbBtnRemove.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void addEditItem(final String ar)
	{
		String itemId = "";
		if (!ar.isEmpty())
		{
			itemId = tbCmbItemName.get(Integer.parseInt(ar)).getValue() != null?
					tbCmbItemName.get(Integer.parseInt(ar)).getValue().toString():"";
		}
		String addEdit = itemId.isEmpty()? "Add":"Edit";

		AddEditFinishedItem win = new AddEditFinishedItem(sessionBean, addEdit, itemId);
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(event ->
		{
			if (!ar.isEmpty())
			{ loadComboData(Integer.parseInt(ar)); }
		});
	}

	private void removeRow(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null &&
				!tbTxtMainPrice.get(ar).getValue().toString().isEmpty())
		{
			tbCmbItemName.get(ar).setValue(null);
			tblModifierList.removeItem(ar);
			tbCmbItemName.get(ar).clear();
			tbTxtMainPrice.get(ar).clear();
		}
	}

	private void loadComboData(int ar)
	{
		/*String sqlI = "select vItemId, vItemName from master.tbFinishedItemInfo where vCategoryId in"+
				" (select vCategoryId from master.tbItemCategory where vCategoryType = 'Modifier') and"+
				" iActive = 1 order by vItemName";*/
		String sql = "select vItemId, vItemName from master.tbFinishedItemInfo where vItemType in"+
				" ('Modifier', 'Both') order by vItemName";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tbCmbItemName.get(ar).addItem(element[0].toString());
			tbCmbItemName.get(ar).setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void actionTableCombo(int ar)
	{
		tbCmbItemName.get(ar).addValueChangeListener(event ->
		{
			if (tbCmbItemName.get(ar).getValue() != null)
			{
				addChanges();
				clearRow(ar);
				setItemDetails(ar, tbCmbItemName.get(ar).getValue().toString());
				tbTxtMainPrice.get(ar).focus();
			}
		});
	}

	private void setItemDetails(int ar, String ItemId)
	{
		String sql = "select fin.vVatOption, vcm.mPercentage, vcm.vVatCatName, fip.mMainPrice from"+
				" master.tbFinishedItemInfo fin, master.tbFinishedItemPrice fip, master.tbVatCatMaster"+
				" vcm where fin.vItemId = fip.vItemId and fin.vUnitIds = fip.iUnitId and fin.vVatCatId"+
				" = vcm.vVatCatId and fin.vItemId = '"+ItemId+"'";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tbLblVatDetails.get(ar).setValue(element[2].toString()+" "+element[0].toString().substring(0, 3));
			tbTxtMainPrice.get(ar).setValue(Double.parseDouble(element[3].toString()));
		}
	}

	private void calculateVatData(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null && 
				!tbTxtMainPrice.get(ar).getValue().toString().isEmpty())
		{
			String vatRule = (tbLblVatDetails.get(ar).getValue().toString().contains("Inc")?"Inclusive":"Exclusive");
			double vatPecent = (tbLblVatDetails.get(ar).getValue().toString().contains("5")?5:0);

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

	private void clearRow(int ar)
	{
		tbLblVatDetails.get(ar).setValue("");
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
	}

	private void addChanges()
	{
		if (flag.equals("Edit") && action)
		{ changes = true; }
	}

	private void focusEnter()
	{
		allComp.add(txtModifierName);
		allComp.add(cmbMenuItems);
		allComp.add(tbCmbItemName.get(0));
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		txtModifierName.setValue("");
		cmbMenuItems.setValue(null);
		tableClear();
	}

	private void tableClear()
	{
		tblModifierList.removeAllItems();
		tbCmbItemName.clear();
		tableRowAdd(0);
	}
}
