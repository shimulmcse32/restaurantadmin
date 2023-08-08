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
public class TabRawItemProfile extends HorizontalLayout
{
	private SessionBean sessionBean;
	private String flag = "", size1 = "", receipeId = "";

	private Table tblProfileList;
	public ArrayList<Label> tbLblPrfileId = new ArrayList<Label>();
	private ArrayList<Button> tbAddRawItem = new ArrayList<Button>();
	public ArrayList<ComboBox> tbCmbItemName = new ArrayList<ComboBox>();
	public ArrayList<Label> tbLblItemUnit = new ArrayList<Label>();
	public ArrayList<CommaField> tbTxtSize1Qty = new ArrayList<CommaField>();
	private ArrayList<Button> tbBtnRemove = new ArrayList<Button>();

	private CommonMethod cm;
	private ItemInfoGateway iig = new ItemInfoGateway();
	public boolean changes = false, action = false;

	public TabRawItemProfile(SessionBean sessionBean, String flag)
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

		glayout.addComponent(buildPrfileTable(), 0, 0);

		return glayout;
	}

	private Table buildPrfileTable()
	{
		tblProfileList = new Table();
		tblProfileList.setFooterVisible(true);
		tblProfileList.setSelectable(true);
		tblProfileList.setColumnCollapsingAllowed(true);
		tblProfileList.addStyleName(ValoTheme.TABLE_SMALL);
		tblProfileList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblProfileList.setWidth("100%");
		tblProfileList.setPageLength(11);
		tblProfileList.setDescription("Profile Information");

		tblProfileList.addContainerProperty("Prfile Id", Label.class, new Label(), null, null, Align.CENTER);
		tblProfileList.setColumnCollapsed("Prfile Id", true);

		tblProfileList.addContainerProperty("Add", Button.class, new Button(), null, null, Align.CENTER);
		tblProfileList.setColumnWidth("Add", 60);

		tblProfileList.addContainerProperty("Item Name(Raw)", ComboBox.class, new ComboBox(), null, null, Align.CENTER);

		tblProfileList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);
		tblProfileList.setColumnWidth("Unit", 100);

		tblProfileList.addContainerProperty("Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblProfileList.setColumnWidth("Qty", 100);

		tblProfileList.addContainerProperty("Rem", Button.class, new Button(), null, null, Align.CENTER);
		tblProfileList.setColumnWidth("Rem", 50);

		tableRowAdd(0);
		return tblProfileList;
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblPrfileId.add(ar, new Label(receipeId));
			tbLblPrfileId.get(ar).setWidth("100%");
			tbLblPrfileId.get(ar).setImmediate(true);
			tbLblPrfileId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

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

			tblProfileList.addItem(new Object[]{tbLblPrfileId.get(ar), tbAddRawItem.get(ar), tbCmbItemName.get(ar),
					tbLblItemUnit.get(ar), tbTxtSize1Qty.get(ar), tbBtnRemove.get(ar)}, ar);
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
			tblProfileList.removeItem(ar);
			tbCmbItemName.get(ar).clear();
			tbTxtSize1Qty.get(ar).clear();
		}
	}

	private void loadComboData(int ar)
	{
		String sql = "select vItemId, vItemCode, vItemName, dbo.funGetNumeric(vItemCode) iCode from"+
				" master.tbRawItemInfo where /*vItemType = 'Raw' and*/ iActive = 1 order by iCode,"+
				" vItemName", caption = "";
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
		size1 = "";

		//Clear table header
		tblProfileList.setColumnHeader("Size 1", "Size 1");

		String sql = "select uni.iUnitId, uni.vUnitName from dbo.Split('"+sizes+"') fun, master.tbUnitInfo"+
				" uni where fun.Item = uni.iUnitId and vUnitType = 'Finish' order by iUnitId";
		for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (i == 0)
			{
				size1 = element[0].toString();
				tblProfileList.setColumnHeader("Size 1", element[1].toString());
				tblProfileList.setColumnHeader("Size1Id", element[1].toString()+"Id");
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
				tbTxtSize1Qty.get(i).setRequired(true);
				tbTxtSize1Qty.get(i).setRequiredError("This field is required.");
			}
		}
	}

	private void clearTableData(int i)
	{
		tbTxtSize1Qty.get(i).setRequired(false);
		tbTxtSize1Qty.get(i).setRequiredError("");
	}

	public void getValue(ItemInfoModel iim, String flag)
	{
		String profileIdN = "", receip = iig.getProfileId();
		String sqlDelete = "", sqlDetails = " ", mergeSql = "";
		double unitQty1 = 0, got = 0;

		if (flag.equals("Edit"))
		{ sqlDelete = " delete from master.tbRawItemProfile where vItemId = '"+iim.getItemId()+"' "; }

		for(int i=0; i<tbCmbItemName.size(); i++)
		{
			unitQty1 = cm.getAmtValue(tbTxtSize1Qty.get(i));
			if (tbCmbItemName.get(i).getValue() != null && unitQty1 > 0)
			{
				if (got == 0)
				{
					sqlDetails = " insert into master.tbRawItemProfile(vBranchId, vProfileId, vItemId, vItemIdProfile,"+
							" mUnitQty1, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate) values";
					got = 1;
				}
				profileIdN = tbLblPrfileId.get(i).getValue().toString().isEmpty()?receip:
					tbLblPrfileId.get(i).getValue().toString();

				sqlDetails += " ('"+sessionBean.getBranchId()+"', '"+profileIdN+"', '"+iim.getItemId()+"',"+
						" '"+tbCmbItemName.get(i).getValue().toString()+"', '"+unitQty1+"', 1,"+
						" '"+sessionBean.getUserId()+"', getdate(), '"+sessionBean.getUserId()+"', getdate()),";
			}
		}

		mergeSql = sqlDelete;
		//if (!sqlTables.isEmpty())
		{ mergeSql += sqlDetails.substring(0, sqlDetails.length()-1); }
		iim.setReceipeSql(mergeSql);
		iim.setReceipeSqlChange(changes);
		//System.out.println(mergeSql);
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
		tblProfileList.removeAllItems();
		tbCmbItemName.clear();
		tbTxtSize1Qty.clear();
		tableRowAdd(0);
	}
}
