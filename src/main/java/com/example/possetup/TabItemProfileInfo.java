package com.example.possetup;

import java.util.ArrayList;
import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonMethod;
import com.common.share.SessionBean;
import com.example.gateway.ItemInfoGateway;
import com.example.model.ItemInfoModel;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TabItemProfileInfo extends HorizontalLayout
{
	private SessionBean sessionBean;
	private String flag = "", size1 = "", size2 = "", size3 = "", size4 = "",
			receipeId = "";

	private Table tblRecipeList;
	public ArrayList<Label> tbLblRecipeId = new ArrayList<Label>();
	private ArrayList<Button> tbAddRawItem = new ArrayList<Button>();
	public ArrayList<ComboBox> tbCmbItemName = new ArrayList<ComboBox>();
	public ArrayList<Label> tbLblItemUnit = new ArrayList<Label>();
	public ArrayList<Label> tbLblSize1Id = new ArrayList<Label>();
	public ArrayList<CommaField> tbTxtSize1Qty = new ArrayList<CommaField>();
	public ArrayList<Label> tbLblSize2Id = new ArrayList<Label>();
	public ArrayList<CommaField> tbTxtSize2Qty = new ArrayList<CommaField>();
	public ArrayList<Label> tbLblSize3Id = new ArrayList<Label>();
	public ArrayList<CommaField> tbTxtSize3Qty = new ArrayList<CommaField>();
	public ArrayList<Label> tbLblSize4Id = new ArrayList<Label>();
	public ArrayList<CommaField> tbTxtSize4Qty = new ArrayList<CommaField>();
	private ArrayList<Button> tbBtnRemove = new ArrayList<Button>();

	private CommonMethod cm;
	private ItemInfoGateway iig = new ItemInfoGateway();
	public boolean changes = false, action = false;

	public TabItemProfileInfo(SessionBean sessionBean, String flag)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		cm = new CommonMethod(this.sessionBean);
		setSizeFull();

		addComponent(buildComponent());
	}

	private Component buildComponent()
	{
		GridLayout glayout = new GridLayout(1, 1);
		glayout.setSpacing(true);
		glayout.setSizeFull();

		glayout.addComponent(buildRecipeTable(), 0, 0);

		return glayout;
	}

	private Table buildRecipeTable()
	{
		tblRecipeList = new Table();
		tblRecipeList.setFooterVisible(true);
		tblRecipeList.setSelectable(true);
		tblRecipeList.setColumnCollapsingAllowed(true);
		tblRecipeList.addStyleName(ValoTheme.TABLE_SMALL);
		tblRecipeList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblRecipeList.setWidth("100%");
		tblRecipeList.setPageLength(11);
		tblRecipeList.setDescription("Recipe Information");

		tblRecipeList.addContainerProperty("Recipe Id", Label.class, new Label(), null, null, Align.CENTER);
		tblRecipeList.setColumnCollapsed("Recipe Id", true);

		tblRecipeList.addContainerProperty("Add", Button.class, new Button(), null, null, Align.CENTER);
		tblRecipeList.setColumnWidth("Add", 60);

		tblRecipeList.addContainerProperty("Item Name(Raw)", ComboBox.class, new ComboBox(), null, null, Align.CENTER);

		tblRecipeList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);
		tblRecipeList.setColumnWidth("Unit", 100);

		tblRecipeList.addContainerProperty("Size1Id", Label.class, new Label(), null, null, Align.CENTER);
		tblRecipeList.setColumnCollapsed("Size1Id", true);

		tblRecipeList.addContainerProperty("Size 1", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblRecipeList.setColumnWidth("Size 1", 100);

		tblRecipeList.addContainerProperty("Size2Id", Label.class, new Label(), null, null, Align.CENTER);
		tblRecipeList.setColumnCollapsed("Size2Id", true);

		tblRecipeList.addContainerProperty("Size 2", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblRecipeList.setColumnWidth("Size 2", 100);

		tblRecipeList.addContainerProperty("Size3Id", Label.class, new Label(), null, null, Align.CENTER);
		tblRecipeList.setColumnCollapsed("Size3Id", true);

		tblRecipeList.addContainerProperty("Size 3", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblRecipeList.setColumnWidth("Size 3", 100);

		tblRecipeList.addContainerProperty("Size4Id", Label.class, new Label(), null, null, Align.CENTER);
		tblRecipeList.setColumnCollapsed("Size4Id", true);

		tblRecipeList.addContainerProperty("Size 4", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblRecipeList.setColumnWidth("Size 4", 100);

		tblRecipeList.addContainerProperty("Rem", Button.class, new Button(), null, null, Align.CENTER);
		tblRecipeList.setColumnWidth("Rem", 50);

		tableRowAdd(0);
		return tblRecipeList;
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblRecipeId.add(ar, new Label(receipeId));
			tbLblRecipeId.get(ar).setWidth("100%");
			tbLblRecipeId.get(ar).setImmediate(true);
			tbLblRecipeId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbAddRawItem.add(ar, new Button());
			tbAddRawItem.get(ar).setWidth("100%");
			tbAddRawItem.get(ar).setImmediate(true);
			tbAddRawItem.get(ar).addStyleName(ValoTheme.BUTTON_TINY);
			tbAddRawItem.get(ar).setIcon(FontAwesome.PLUS);
			tbAddRawItem.get(ar).setDescription("Add Item(Raw)");
			tbAddRawItem.get(ar).addClickListener(new ClickListener()
			{
				public void buttonClick(ClickEvent event)
				{ addEditRawItem(ar); }
			});
			cm.setAuthorize(sessionBean.getUserId(), "rawItem");
			tbAddRawItem.get(ar).setEnabled(cm.insert);

			tbCmbItemName.add(ar, new ComboBox());
			tbCmbItemName.get(ar).setWidth("100%");
			tbCmbItemName.get(ar).setImmediate(true);
			tbCmbItemName.get(ar).setFilteringMode(FilteringMode.CONTAINS);
			tbCmbItemName.get(ar).setRequired(true);
			tbCmbItemName.get(ar).setRequiredError("This field is required.");
			tbCmbItemName.get(ar).setInputPrompt("Select raw item");
			tbCmbItemName.get(ar).addStyleName(ValoTheme.COMBOBOX_TINY);
			loadComboData(ar);

			tbLblItemUnit.add(ar, new Label());
			tbLblItemUnit.get(ar).setWidth("100%");
			tbLblItemUnit.get(ar).setImmediate(true);
			tbLblItemUnit.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblSize1Id.add(ar, new Label(size1));
			tbLblSize1Id.get(ar).setWidth("100%");
			tbLblSize1Id.get(ar).setImmediate(true);
			tbLblSize1Id.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbTxtSize1Qty.add(ar, new CommaField());
			tbTxtSize1Qty.get(ar).setWidth("100%");
			tbTxtSize1Qty.get(ar).setImmediate(true);
			tbTxtSize1Qty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtSize1Qty.get(ar).setInputPrompt("Quantity");
			tbTxtSize1Qty.get(ar).setRequired(size1.isEmpty()?false:true);
			tbTxtSize1Qty.get(ar).setRequiredError(size1.isEmpty()?"":"This field is required.");
			tbTxtSize1Qty.get(ar).addValueChangeListener(new ValueChangeListener()
			{
				public void valueChange(ValueChangeEvent event)
				{
					if (tbCmbItemName.size() <= (ar+1))
					{ tableRowAdd(ar+1); }
					addChanges();
					tbCmbItemName.get(ar+1).focus();
				}
			});

			tbLblSize2Id.add(ar, new Label(size2));
			tbLblSize2Id.get(ar).setWidth("100%");
			tbLblSize2Id.get(ar).setImmediate(true);
			tbLblSize2Id.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbTxtSize2Qty.add(ar, new CommaField());
			tbTxtSize2Qty.get(ar).setWidth("100%");
			tbTxtSize2Qty.get(ar).setImmediate(true);
			tbTxtSize2Qty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtSize2Qty.get(ar).setInputPrompt("Quantity");
			tbTxtSize2Qty.get(ar).setRequired(size2.isEmpty()?false:true);
			tbTxtSize2Qty.get(ar).setRequiredError(size2.isEmpty()?"":"This field is required.");
			tbTxtSize2Qty.get(ar).addValueChangeListener(new ValueChangeListener()
			{
				public void valueChange(ValueChangeEvent event)
				{ addChanges(); }
			});

			tbLblSize3Id.add(ar, new Label(size3));
			tbLblSize3Id.get(ar).setWidth("100%");
			tbLblSize3Id.get(ar).setImmediate(true);
			tbLblSize3Id.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbTxtSize3Qty.add(ar, new CommaField());
			tbTxtSize3Qty.get(ar).setWidth("100%");
			tbTxtSize3Qty.get(ar).setImmediate(true);
			tbTxtSize3Qty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtSize3Qty.get(ar).setInputPrompt("Quantity");
			tbTxtSize3Qty.get(ar).setRequired(size3.isEmpty()?false:true);
			tbTxtSize3Qty.get(ar).setRequiredError(size3.isEmpty()?"":"This field is required.");
			tbTxtSize3Qty.get(ar).addValueChangeListener(new ValueChangeListener()
			{
				public void valueChange(ValueChangeEvent event)
				{ addChanges(); }
			});

			tbLblSize4Id.add(ar, new Label(size4));
			tbLblSize4Id.get(ar).setWidth("100%");
			tbLblSize4Id.get(ar).setImmediate(true);
			tbLblSize4Id.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbTxtSize4Qty.add(ar, new CommaField());
			tbTxtSize4Qty.get(ar).setWidth("100%");
			tbTxtSize4Qty.get(ar).setImmediate(true);
			tbTxtSize4Qty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtSize4Qty.get(ar).setInputPrompt("Quantity");
			tbTxtSize4Qty.get(ar).setRequired(size4.isEmpty()?false:true);
			tbTxtSize4Qty.get(ar).setRequiredError(size4.isEmpty()?"":"This field is required.");
			tbTxtSize4Qty.get(ar).addValueChangeListener(new ValueChangeListener()
			{
				public void valueChange(ValueChangeEvent event)
				{ addChanges(); }
			});

			tbBtnRemove.add(ar, new Button());
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
					if (tbCmbItemName.size() > 1)
					{
						removeRow(ar);
						addChanges();
					}
				}
			});

			tblRecipeList.addItem(new Object[]{tbLblRecipeId.get(ar), tbAddRawItem.get(ar), tbCmbItemName.get(ar),
					tbLblItemUnit.get(ar), tbLblSize1Id.get(ar), tbTxtSize1Qty.get(ar), tbLblSize2Id.get(ar),
					tbTxtSize2Qty.get(ar), tbLblSize3Id.get(ar), tbTxtSize3Qty.get(ar), tbLblSize4Id.get(ar),
					tbTxtSize4Qty.get(ar), tbBtnRemove.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); System.out.println(exp); }
	}

	private void removeRow(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null &&
				!tbTxtSize1Qty.get(ar).getValue().toString().isEmpty())
		{
			tbCmbItemName.get(ar).setValue(null);
			tblRecipeList.removeItem(ar);
			tbCmbItemName.get(ar).clear();
			tbTxtSize1Qty.get(ar).clear();
		}
	}

	private void loadComboData(int ar)
	{
		String sql = "select vItemId, vItemCode, vItemName, dbo.funGetNumeric(vItemCode) iCode from"+
				" master.tbRawItemInfo where iActive = 1 order by iCode, vItemName", caption = "";
		for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			caption = element[1].toString().isEmpty()? element[2].toString() :
				element[1].toString()+" - "+element[2].toString();
			tbCmbItemName.get(ar).addItem(element[0].toString());
			tbCmbItemName.get(ar).setItemCaption(element[0].toString(), caption);
		}
		actionTableCombo(ar);
	}

	private void actionTableCombo(int ar)
	{
		tbCmbItemName.get(ar).addValueChangeListener(new ValueChangeListener()
		{
			public void valueChange(ValueChangeEvent event)
			{
				if (tbCmbItemName.get(ar).getValue() != null)
				{
					String selectId = tbCmbItemName.get(ar).getValue().toString();
					if (!checkDuplicate(selectId, ar))
					{
						tbLblItemUnit.get(ar).setValue(iig.getRawItemUnit(selectId));
						tbTxtSize1Qty.get(ar).focus();
						addChanges();
					}
					else
					{
						tbCmbItemName.get(ar).setValue(null);
						tbLblItemUnit.get(ar).setValue("");
						tbTxtSize1Qty.get(ar).setValue("");
						cm.showNotification("warning", "Warning!", "Item already selected.");
					}
				}
			}
		});
	}

	private boolean checkDuplicate(String selectId, int ar)
	{
		boolean ret = false;
		for(int i=0; i<tbCmbItemName.size(); i++)
		{
			if (i != ar)
			{
				if (tbCmbItemName.get(i).getValue() != null)
				{
					if (selectId.equals(tbCmbItemName.get(i).getValue().toString()))
					{ ret = true; break; }
				}
			}
		}
		return ret;
	}

	public void setTableColumn(String sizes)
	{
		int i = 0;
		size1 = ""; size2 = ""; size3 = ""; size4 = "";

		//Clear table header
		tblRecipeList.setColumnHeader("Size 1", "Size 1");
		tblRecipeList.setColumnHeader("Size 2", "Size 2");
		tblRecipeList.setColumnHeader("Size 3", "Size 3");
		tblRecipeList.setColumnHeader("Size 4", "Size 4");

		String sql = "select uni.iUnitId, uni.vUnitName from dbo.Split('"+sizes+"') fun, master.tbUnitInfo"+
				" uni where fun.Item = uni.iUnitId and vUnitType = 'Finish' order by iUnitId";
		for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (i == 0)
			{
				size1 = element[0].toString();
				tblRecipeList.setColumnHeader("Size 1", element[1].toString());
				tblRecipeList.setColumnHeader("Size1Id", element[1].toString()+"Id");
			}
			else if (i == 1)
			{
				size2 = element[0].toString();
				tblRecipeList.setColumnHeader("Size 2", element[1].toString());
				tblRecipeList.setColumnHeader("Size2Id", element[1].toString()+"Id");
			}
			else if (i == 2)
			{
				size3 = element[0].toString();
				tblRecipeList.setColumnHeader("Size 3", element[1].toString());
				tblRecipeList.setColumnHeader("Size3Id", element[1].toString()+"Id");
			}
			else if (i == 3)
			{
				size4 = element[0].toString();
				tblRecipeList.setColumnHeader("Size 4", element[1].toString());
				tblRecipeList.setColumnHeader("Size4Id", element[1].toString()+"Id");
			}
			i++;
		}
		setSizeIdToTable();
	}

	private void setSizeIdToTable()
	{
		for(int i=0; i<tbCmbItemName.size(); i++)
		{
			clearTableData(i);
			if (!size1.isEmpty())
			{
				tbLblSize1Id.get(i).setValue(size1);
				tbTxtSize1Qty.get(i).setRequired(true);
				tbTxtSize1Qty.get(i).setRequiredError("This field is required.");
			}
			if (!size2.isEmpty())
			{
				tbLblSize2Id.get(i).setValue(size2);
				tbTxtSize2Qty.get(i).setRequired(true);
				tbTxtSize2Qty.get(i).setRequiredError("This field is required.");
			}
			if (!size3.isEmpty())
			{
				tbLblSize3Id.get(i).setValue(size3);
				tbTxtSize3Qty.get(i).setRequired(true);
				tbTxtSize3Qty.get(i).setRequiredError("This field is required.");
			}
			if (!size4.isEmpty())
			{
				tbLblSize4Id.get(i).setValue(size4);
				tbTxtSize4Qty.get(i).setRequired(true);
				tbTxtSize4Qty.get(i).setRequiredError("This field is required.");
			}
		}
	}

	private void clearTableData(int i)
	{
		tbLblSize1Id.get(i).setValue("");
		tbTxtSize1Qty.get(i).setRequired(false);
		tbTxtSize1Qty.get(i).setRequiredError("");

		tbLblSize2Id.get(i).setValue("");
		tbTxtSize2Qty.get(i).setRequired(false);
		tbTxtSize2Qty.get(i).setRequiredError("");

		tbLblSize3Id.get(i).setValue("");
		tbTxtSize3Qty.get(i).setRequired(false);
		tbTxtSize3Qty.get(i).setRequiredError("");

		tbLblSize4Id.get(i).setValue("");
		tbTxtSize4Qty.get(i).setRequired(false);
		tbTxtSize4Qty.get(i).setRequiredError("");
	}

	public void getValue(ItemInfoModel iim)
	{
		String receipeIdN = "", receip = iig.getReceipeId();
		String sqlDelete = "", sqlDetails = "", sqlTables = "", mergeSql = "";
		String unitId1 = "", unitId2 = "", unitId3 = "", unitId4 = "";
		double unitQty1 = 0, unitQty2 = 0, unitQty3 = 0, unitQty4 = 0;

		sqlDetails = " insert into master.tbFinishedReceipe(vBranchId, vReceipeId, vItemId, vItemIdReceipe,"+
				" iUnitId1, mUnitQty1, iUnitId2, mUnitQty2, iUnitId3, mUnitQty3, iUnitId4, mUnitQty4, iActive,"+
				" vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate) values";
		for(int i=0; i<tbCmbItemName.size(); i++)
		{
			if (tbCmbItemName.get(i).getValue() != null && cm.getAmtValue(tbTxtSize1Qty.get(i)) > 0)
			{
				receipeIdN = tbLblRecipeId.get(i).getValue().toString().isEmpty()?receip:
					tbLblRecipeId.get(i).getValue().toString();

				if (sqlDelete.isEmpty())
				{ sqlDelete = "delete from master.tbFinishedReceipe where vReceipeId = '"+receipeId+"'"; }

				unitId1 = tbLblSize1Id.get(i).getValue().toString();
				unitQty1 = cm.getAmtValue(tbTxtSize1Qty.get(i));
				unitId2 = tbLblSize2Id.get(i).getValue().toString();
				unitQty2 = cm.getAmtValue(tbTxtSize2Qty.get(i));
				unitId3 = tbLblSize3Id.get(i).getValue().toString();
				unitQty3 = cm.getAmtValue(tbTxtSize3Qty.get(i));
				unitId4 = tbLblSize4Id.get(i).getValue().toString();
				unitQty4 = cm.getAmtValue(tbTxtSize4Qty.get(i));

				sqlTables = sqlTables+ " ('"+sessionBean.getBranchId()+"', '"+receipeIdN+"', '"+iim.getItemId()+"',"+
						" '"+tbCmbItemName.get(i).getValue().toString()+"', '"+unitId1+"', '"+unitQty1+"',"+
						" '"+unitId2+"', '"+unitQty2+"', '"+unitId3+"', '"+unitQty3+"', '"+unitId4+"',"+
						" '"+unitQty4+"', 1, '"+sessionBean.getUserId()+"', getdate(),"+
						" '"+sessionBean.getUserId()+"', getdate()),";
			}
		}

		if (!sqlTables.isEmpty())
		{ mergeSql = sqlDelete + sqlDetails + sqlTables.substring(0, sqlTables.length()-1); }
		iim.setReceipeSql(mergeSql);
		iim.setReceipeSqlChange(changes);
	}

	public void setValue(String itemId)
	{
		int ar = 0;
		String sql = "select vReceipeId, vItemIdReceipe, iUnitId1, mUnitQty1, iUnitId2, mUnitQty2, iUnitId3, mUnitQty3,"+
				" iUnitId4, mUnitQty4 from master.tbFinishedReceipe where iActive = 1 and vItemId = '"+itemId+"'";
		for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			receipeId = element[0].toString();
			tbLblRecipeId.get(ar).setValue(element[0].toString());
			tbCmbItemName.get(ar).setValue(element[1].toString());

			tbLblSize1Id.get(ar).setValue(element[2].toString());
			tbTxtSize1Qty.get(ar).setValue(Double.parseDouble(element[3].toString()));

			tbLblSize2Id.get(ar).setValue(element[4].toString());
			tbTxtSize2Qty.get(ar).setValue(Double.parseDouble(element[5].toString()));

			tbLblSize3Id.get(ar).setValue(element[6].toString());
			tbTxtSize3Qty.get(ar).setValue(Double.parseDouble(element[7].toString()));

			tbLblSize4Id.get(ar).setValue(element[8].toString());
			tbTxtSize4Qty.get(ar).setValue(Double.parseDouble(element[9].toString()));

			ar++;
		}
	}

	private void addEditRawItem(int ar)
	{
		String itemId = tbCmbItemName.get(ar).getValue() != null? tbCmbItemName.get(ar).getValue().toString():"";
		String addEdit = itemId.isEmpty()? "Add":"Edit";

		AddEditRawItemInfo win = new AddEditRawItemInfo(sessionBean, addEdit, itemId);
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(new CloseListener()
		{
			public void windowClose(CloseEvent e)
			{ loadComboData(ar); }
		});
	}

	private void addChanges()
	{
		if(flag.equals("Edit") && action)
		{ changes = true; }
	}

	public void tableClear()
	{
		tblRecipeList.removeAllItems();
		tbCmbItemName.clear();
		tbTxtSize1Qty.clear();
		tableRowAdd(0);
	}
}
