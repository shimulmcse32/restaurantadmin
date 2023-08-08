package com.example.postrans;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.MessageBox;
import com.example.gateway.PhysicalStockGateway;
import com.example.model.PhysicalStockModel;
import com.example.possetup.AddEditRawItemInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditPhysicalStock extends Window
{
	private SessionBean sessionBean;
	private String flag, PhysicalStockId;
	private boolean tick = false;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "", "Exit");
	private PhysicalStockGateway psg = new PhysicalStockGateway();

	private TextField txtPhysicalStockNo, txtRemarks, txtReferenceNo;
	private PopupDateField txtPhysicalStockDate;
	//Consume,Expried,Demage,Lost.
	private Table tblPhysicalStockDetailsList;
	private ArrayList<Button> tbBtnAddItem  = new ArrayList<Button>();
	private ArrayList<ComboBox> tbCmbItemName = new ArrayList<ComboBox>();
	private ArrayList<Label> tblblUnitName = new ArrayList<Label>();
	private ArrayList<Label> tblblCatName = new ArrayList<Label>();
	private ArrayList<TextField> tbTxtRemarks = new ArrayList<TextField>();
	private ArrayList<CommaField> tbAmtSoftQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtPhyQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtDiffQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtPurchaseRate = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtTotalAmount = new ArrayList<CommaField>();
	private ArrayList<Label> tblblUnitId  = new ArrayList<Label>();
	private ArrayList<Label> tblblCatId  = new ArrayList<Label>();
	private ArrayList<Button> tbBtnRemove  = new ArrayList<Button>();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private CommonMethod cm;
	private boolean changes = false, action = false;

	public AddEditPhysicalStock(SessionBean sessionBean, String flag, String PhysicalStockId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.PhysicalStockId = PhysicalStockId;
		this.setCaption(flag+" Physical Stock :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);
		setWidth("100%");
		setHeight("600px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
		{ masterValidation(); });

		cBtn.btnExit.addClickListener(event ->
		{ close(); });

		txtPhysicalStockDate.addValueChangeListener(event ->
		{ loadPhysicalStockNo(); });

		tblPhysicalStockDetailsList.addHeaderClickListener(event -> 
		{
			if (event.getPropertyId().toString().equals("Item Name"))
			{ selectAll(); }
		});

		loadPhysicalStockNo();
		if (flag.equals("Edit"))
		{setEditData();}
		focusEnter();
	}

	private void addEditItem(int ar)
	{
		String itemId = tbCmbItemName.get(ar).getValue() != null? tbCmbItemName.get(ar).getValue().toString():"";
		String addEdit = itemId.isEmpty()? "Add":"Edit";

		AddEditRawItemInfo win = new AddEditRawItemInfo(sessionBean, addEdit, itemId);
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();

		win.addCloseListener(event ->
		{ 
			for (int i=0; i<tbCmbItemName.size(); i++)
			{ loadtableComboData(ar); } 
		});
	}

	private void loadtableComboData(int ar)
	{
		tbCmbItemName.get(ar).removeAllItems();
		String sql = "";

		sql = "select vItemId, vItemName, vItemCode, dbo.funGetNumeric(vItemCode) iCode"+
				" from master.tbRawItemInfo where iActive = 1 order by iCode asc";

		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tbCmbItemName.get(ar).addItem(element[0].toString());
			tbCmbItemName.get(ar).setItemCaption(element[0].toString(), element[2].toString()+" - "+element[1].toString());
		}
	}

	private void selectAll()
	{
		if (tick)
		{tick = false;}
		else
		{tick = true;}
		selectAlltable();
	}

	private void selectAlltable()
	{
		if (tick == true)
		{ loadMenuList(); } 
		else
		{ tableClear(); }
	}

	private void loadPhysicalStockNo()
	{
		if (!flag.equals("Edit"))
		{
			txtPhysicalStockNo.setReadOnly(false);
			txtPhysicalStockNo.setValue(psg.getPhysicalStockNo(cm.dfDb.format(txtPhysicalStockDate.getValue())));
			txtPhysicalStockNo.setReadOnly(true);
		}
	}

	private void QtyValidation(int ar)
	{
		if (tbCmbItemName.get(ar).getValue()!=null)
		{
			if (cm.getAmtValue(tbAmtPhyQty.get(ar)) > 0)
			{ calculateVatData(ar); }				
			else
			{
				tbAmtPhyQty.get(ar).focus();
				calculateVatData(ar);
				cm.showNotification("warning", "Warning!", "Provide Qty");
			}				
		}
		else
		{ cm.showNotification("warning", "Warning!", "Select Item"); }
	}

	private void masterValidation()
	{
		if (!txtPhysicalStockNo.getValue().toString().trim().isEmpty())
		{
			if (txtPhysicalStockDate.getValue() != null)
			{
				if (tbCmbItemName.get(0).getValue()!=null)
				{
					if (totalAmount("Total") > 0)
					{
						cBtn.btnSave.setEnabled(false);
						insertEditData();
					}
					else
					{ cm.showNotification("warning", "Warning!", "provide valid data in table."); }
				}
				else
				{
					tbCmbItemName.get(0).focus();
					cm.showNotification("warning", "Warning!", "Select item name from table.");
				}
			}
			else
			{
				txtPhysicalStockDate.focus();
				cm.showNotification("warning", "Warning!", "provide physical stock date.");
			}
		}
		else
		{
			txtPhysicalStockNo.focus();
			cm.showNotification("warning", "Warning!", "physical stock number not found.");
		}
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
					{ insertData(); }
					catch(Exception ex)
					{ System.out.println(ex); }
				}
				else if (buttonType == ButtonType.NO)
				{ cBtn.btnSave.setEnabled(true); }
			}
		});
	}

	private void insertData()
	{		
		PhysicalStockModel psm = new PhysicalStockModel();
		setValueForSave(psm);
		if (psg.insertEditData(psm, flag))
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

	private void setValueForSave(PhysicalStockModel psm)
	{
		psm.setBranchId(sessionBean.getBranchId());
		psm.setPhysicalStockId(flag.equals("Add")? psg.getPhysicalStockId(sessionBean.getBranchId()):PhysicalStockId);
		psm.setPhysicalStockNo(flag.equals("Add")? psg.getPhysicalStockNo(cm.dfDb.format(txtPhysicalStockDate.getValue())).toString():
			txtPhysicalStockNo.getValue().toString());
		psm.setPhysicalStockDate(txtPhysicalStockDate.getValue());
		psm.setRemarks(txtRemarks.getValue().toString().trim());
		psm.setReferenceNo(txtReferenceNo.getValue().toString());
		psm.setCreatedBy(sessionBean.getUserId());
		psm.setStatusId("S6");
		psm.setApproveBy("");
		psm.setCancelBy("");
		psm.setCancelReason("");
		psm.setDetailsSql(SaveDetails(psm.getPhysicalStockId()));
		psm.setDetailsChange(changes);
	}

	private String SaveDetails(String PhysicalStockId)
	{
		String sql="";
		//String sql = flag.equals("Add") ? "" : " delete trans.tbPhysicalStockDetails where vPhysicalStockId = '"+PhysicalStockId+"' ";
		for (int i = 0; i < tbCmbItemName.size(); i++)
		{
			if (i == 0)
			{
				sql += " insert into trans.tbPhysicalStockDetails(vPhysicalStockId, vCategoryId, vCategoryName, vItemId, vRemarks,"+
						" vUnitId, vUnitName, vPhysicalStockStatus, mPhysicalStockQty, mSoftwareStockQty, mPurchaseRate, mSaleRate,"+
						" mTotalAmount, iActive) values ";
			}
			if (tbCmbItemName.get(0).getValue()!=null && cm.getAmtValue(tbAmtTotalAmount.get(i)) > 0)
			{
				sql += " ('"+PhysicalStockId+"', '"+tblblCatId.get(i).getValue().toString()+"',"+
						" '"+tblblCatName.get(i).getValue().toString()+"',"+
						" '"+tbCmbItemName.get(i).getValue().toString()+"',"+
						" '"+tbTxtRemarks.get(i).getValue().toString()+"',"+
						" '"+tblblUnitId.get(i).getValue().toString()+"',"+
						" '"+tblblUnitName.get(i).getValue().toString()+"',"+
						" '',"+
						" '"+cm.getAmtValue(tbAmtPhyQty.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtSoftQty.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtPurchaseRate.get(i))+"', 0,"+
						" '"+cm.getAmtValue(tbAmtTotalAmount.get(i))+"', 1),";
			}
		}
		return sql.substring(0, sql.length()-1);
	}

	public void setValue(String PhysicalStockId)
	{
		int ar = 0;
		String sql = "select vPhysicalStockId, vItemId, vUnitName, vPhysicalStockStatus, mPhysicalStockQty, mPurchaseRate,"+
				" mTotalAmount, iActive ,vRemarks, (select [dbo].[funcStockQty](a.vItemId ,'"+sessionBean.getBranchId()+"'))"+
				" mStockQty from trans.tbPhysicalStockDetails a where vPhysicalStockId = '"+PhysicalStockId+"'";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (tbCmbItemName.size() <= ar)
			{ tableRowAdd(tbCmbItemName.size()); }

			tbCmbItemName.get(ar).setValue(element[1].toString());
			tbAmtPhyQty.get(ar).setValue(Double.parseDouble(element[4].toString()));
			tbAmtPurchaseRate.get(ar).setReadOnly(false);
			tbAmtPurchaseRate.get(ar).setValue(Double.parseDouble(element[5].toString()));
			tbAmtPurchaseRate.get(ar).setReadOnly(true);
			tbAmtTotalAmount.get(ar).setReadOnly(false);
			tbAmtTotalAmount.get(ar).setValue(Double.parseDouble(element[6].toString()));
			tbAmtTotalAmount.get(ar).setReadOnly(true);			
			tbTxtRemarks.get(ar).setValue(element[8].toString());
			tbAmtSoftQty.get(ar).setReadOnly(false);
			tbAmtSoftQty.get(ar).setValue(Double.parseDouble(element[9].toString()));
			tbAmtSoftQty.get(ar).setReadOnly(true);	
			totalAmount("Total");
			ar++;
		}
		action = true;
	}

	private void setEditData()
	{
		PhysicalStockModel psm = new PhysicalStockModel();
		try
		{
			if (psg.selectEditData(psm, PhysicalStockId))
			{				
				txtPhysicalStockDate.setValue((Date) psm.getPhysicalStockDate());
				txtRemarks.setValue(psm.getRemarks());
				txtReferenceNo.setValue(psm.getReferenceNo());
				txtPhysicalStockNo.setReadOnly(false);
				txtPhysicalStockNo.setValue(psm.getPhysicalStockNo());
				txtPhysicalStockNo.setReadOnly(true);
				setValue(PhysicalStockId);
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private void loadMenuList()
	{
		tableClear();
		int i = 0;
		String sql = "select I.vItemId, I.vItemName, U.vUnitName, C.vCategoryName, I.vUnitId, I.vCategoryId, (select "+
				" [dbo].[funcItemRate](I.vItemId, '"+sessionBean.getBranchId()+"')) mPurchaseRate, (select [dbo].[funcStockQty]"+
				" (I.vItemId, '"+sessionBean.getBranchId()+"')) mStockQty from master.tbRawItemInfo I inner join master.tbUnitInfo"+
				" U on I.vUnitId = U.iUnitId inner join master.tbItemCategory C on I.vCategoryId = C.vCategoryId where I.iActive = 1"+
				" order by I.vItemName";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			if (tbCmbItemName.size() <= i)
			{ tableRowAdd(i); }

			tbCmbItemName.get(i).setValue(element[0].toString());
			tblblUnitName.get(i).setValue(element[2].toString());
			tblblCatName.get(i).setValue(element[3].toString());
			tblblUnitId.get(i).setValue(element[4].toString());
			tblblCatId.get(i).setValue(element[5].toString());
			tbAmtPurchaseRate.get(i).setReadOnly(false);
			tbAmtPurchaseRate.get(i).setValue(Double.parseDouble(element[6].toString()));
			tbAmtPurchaseRate.get(i).setReadOnly(true);
			tbAmtSoftQty.get(i).setReadOnly(false);
			tbAmtSoftQty.get(i).setValue(Double.parseDouble(element[7].toString()));
			tbAmtSoftQty.get(i).setReadOnly(true);
			totalAmount("Total");
			i++;
		}
	}

	private void setItemDetails(int ar, String ItemId)
	{
		try
		{
			String sql = "select I.vItemId, I.vItemName, U.vUnitName, C.vCategoryName, I.vUnitId, I.vCategoryId, (select [dbo].[funcItemRate]"+
					" (I.vItemId, '"+sessionBean.getBranchId()+"')) mPurchaseRate, (select [dbo].[funcStockQty] (I.vItemId,"+
					" '"+sessionBean.getBranchId()+"')) mStockQty from master.tbRawItemInfo I inner join master.tbUnitInfo U on"+
					" I.vUnitId = U.iUnitId inner join master.tbItemCategory C on I.vCategoryId = C.vCategoryId where I.iActive=1"+ 
					" and I.vItemId = '"+ItemId+"'";

			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				tblblUnitName.get(ar).setValue(element[2].toString());
				tblblCatName.get(ar).setValue(element[3].toString());
				tblblUnitId.get(ar).setValue(element[4].toString());
				tblblCatId.get(ar).setValue(element[5].toString());
				tbAmtPurchaseRate.get(ar).setReadOnly(false);
				tbAmtPurchaseRate.get(ar).setValue(Double.parseDouble(element[6].toString()));
				tbAmtPurchaseRate.get(ar).setReadOnly(true);
				tbAmtSoftQty.get(ar).setReadOnly(false);
				tbAmtSoftQty.get(ar).setValue(Double.parseDouble(element[7].toString()));
				tbAmtSoftQty.get(ar).setReadOnly(true);
			}
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void calculateVatData(int ar)
	{
		double TotalSoftQty = cm.getAmtValue(tbAmtSoftQty.get(ar));
		double TotalPhyQty = cm.getAmtValue(tbAmtPhyQty.get(ar));
		double Rate = cm.getAmtValue(tbAmtPurchaseRate.get(ar));
		double TotalAmountCal = cm.getRound(TotalPhyQty * Rate);
		double TotalDiff = cm.getRound(TotalSoftQty - TotalPhyQty);

		tbAmtTotalAmount.get(ar).setReadOnly(false);
		tbAmtTotalAmount.get(ar).setValue(TotalAmountCal);
		tbAmtTotalAmount.get(ar).setReadOnly(true);

		tbAmtDiffQty.get(ar).setReadOnly(false);
		tbAmtDiffQty.get(ar).setValue(TotalDiff);
		tbAmtDiffQty.get(ar).setReadOnly(true);
		totalAmount("Total");
		addChanges();
	}

	private void addChanges()
	{
		if (flag.equals("Edit") && action)
		{ changes = true; }
	}

	public void tableClear()
	{
		tblPhysicalStockDetailsList.removeAllItems();
		tbCmbItemName.clear();
		tableRowAdd(0);
	}

	private void focusEnter()
	{
		allComp.add(txtReferenceNo);
		allComp.add(txtRemarks);
		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}

	public void txtClear()
	{
		txtPhysicalStockNo.setReadOnly(false);
		txtPhysicalStockNo.setValue(psg.getPhysicalStockNo(cm.dfDb.format(txtPhysicalStockDate.getValue())));
		txtPhysicalStockNo.setReadOnly(true);
		txtRemarks.setValue("");
		txtReferenceNo.setValue("");
		txtPhysicalStockDate.setValue(new Date());
		tableClear();
		totalAmount("");
	}

	private void removeRow(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null && tbCmbItemName.size() > 0 &&
				!tbAmtTotalAmount.get(ar).getValue().toString().isEmpty())
		{
			tbCmbItemName.get(ar).setValue(null);
			tblPhysicalStockDetailsList.removeItem(ar);
			tbCmbItemName.get(ar).clear();
			tbAmtTotalAmount.get(ar).setReadOnly(false);
			tbAmtTotalAmount.get(ar).clear();
		}
		totalAmount("Total");
	}

	//Master Component Set
	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(8, 3);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		CssLayout groupP = new CssLayout();
		groupP.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		txtPhysicalStockDate  = new PopupDateField();
		txtPhysicalStockDate.setImmediate(true);
		txtPhysicalStockDate.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPhysicalStockDate.setValue(new Date());
		txtPhysicalStockDate.setWidth("120px");
		txtPhysicalStockDate.setDateFormat("dd-MM-yyyy");
		txtPhysicalStockDate.setDescription("Physical Stock Date");
		txtPhysicalStockDate.setRequired(true);
		txtPhysicalStockDate.setRequiredError("This field is required");
		Label tblstock = new Label("Reference Details: ");
		tblstock.setWidth("150px");
		grid.addComponent(tblstock, 0, 0);

		txtPhysicalStockNo = new TextField();
		txtPhysicalStockNo.setImmediate(true);
		txtPhysicalStockNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPhysicalStockNo.setWidth("130px");
		txtPhysicalStockNo.setInputPrompt("Physical Stock No");
		txtPhysicalStockNo.setDescription("Physical Stock No");
		txtPhysicalStockNo.setRequired(true);
		txtPhysicalStockNo.setRequiredError("This field is required.");
		groupP.addComponents(txtPhysicalStockNo, txtPhysicalStockDate);
		groupP.setWidth("250px");
		grid.addComponent(groupP, 1, 0);

		txtReferenceNo = new TextField();
		txtReferenceNo.setImmediate(true);
		txtReferenceNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReferenceNo.setWidth("250px");
		txtReferenceNo.setInputPrompt("Reference No");
		Label tblRef = new Label("Ref. No: ");
		tblRef.setWidth("60px");
		grid.addComponent(tblRef, 2, 0);
		grid.addComponent(txtReferenceNo, 3, 0, 4, 0);

		txtRemarks = new TextField();
		txtRemarks.setImmediate(true);
		txtRemarks.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRemarks.setWidth("250px");
		txtRemarks.setInputPrompt("Remarks");
		Label tblRem = new Label("Remarks: ");
		tblRem.setWidth("60px");
		grid.addComponent(tblRem, 5, 0);
		grid.addComponent(txtRemarks, 6, 0, 7, 0);

		grid.addComponent(buildTable(), 0, 1, 7, 1);

		grid.addComponent(cBtn, 0, 2, 7, 2);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return grid;
	}

	//Table Component Set
	private Table buildTable()
	{
		tblPhysicalStockDetailsList = new Table();
		tblPhysicalStockDetailsList.setSelectable(true);
		tblPhysicalStockDetailsList.setFooterVisible(true);
		tblPhysicalStockDetailsList.setColumnCollapsingAllowed(true);
		tblPhysicalStockDetailsList.addStyleName(ValoTheme.TABLE_SMALL);
		tblPhysicalStockDetailsList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblPhysicalStockDetailsList.setPageLength(10);
		tblPhysicalStockDetailsList.setWidth("100%");

		tblPhysicalStockDetailsList.addContainerProperty("Add", Button.class, new Button(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnWidth("Add", 50);

		tblPhysicalStockDetailsList.addContainerProperty("Item Name", ComboBox.class, new ComboBox(), null, null, Align.CENTER);

		tblPhysicalStockDetailsList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);

		tblPhysicalStockDetailsList.addContainerProperty("Category", Label.class, new Label(), null, null, Align.CENTER);

		tblPhysicalStockDetailsList.addContainerProperty("Remarks", TextField.class, new TextField(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnCollapsed("Remarks", true);

		tblPhysicalStockDetailsList.addContainerProperty("Soft.Stk Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnWidth("Soft.Stk Qty", 100);

		tblPhysicalStockDetailsList.addContainerProperty("Phy.Stk Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnWidth("Phy.Stk Qty", 100);

		tblPhysicalStockDetailsList.addContainerProperty("Difference", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnWidth("Difference", 120);

		tblPhysicalStockDetailsList.addContainerProperty("Rate", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblPhysicalStockDetailsList.setColumnWidth("Rate", 100);

		tblPhysicalStockDetailsList.addContainerProperty("Total Amount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblPhysicalStockDetailsList.setColumnWidth("Total Amount", 140);

		tblPhysicalStockDetailsList.addContainerProperty("Unit ID", Label.class, new Label(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnCollapsed("Unit ID", true);

		tblPhysicalStockDetailsList.addContainerProperty("Cat. ID", Label.class, new Label(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnCollapsed("Cat. ID", true);

		tblPhysicalStockDetailsList.addContainerProperty("Rem", Button.class, new Button(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnWidth("Rem", 50);
		tableRowAdd(0);
		return tblPhysicalStockDetailsList;
	}

	public void tableRowAdd(int ar)
	{
		try
		{
			tbBtnAddItem.add(ar, new Button(""));
			tbBtnAddItem.get(ar).setWidth("100%");
			tbBtnAddItem.get(ar).setImmediate(true);
			tbBtnAddItem.get(ar).setIcon(FontAwesome.PLUS);
			tbBtnAddItem.get(ar).setStyleName(ValoTheme.BUTTON_TINY);
			tbBtnAddItem.get(ar).setDescription("Add Item");
			cm.setAuthorize(sessionBean.getUserId(), "rawItem");
			tbBtnAddItem.get(ar).setEnabled(cm.insert);
			tbBtnAddItem.get(ar).addClickListener(event ->
			{ addEditItem(ar); });

			tbCmbItemName.add(ar, new ComboBox());
			tbCmbItemName.get(ar).setWidth("100%");
			tbCmbItemName.get(ar).setImmediate(true);
			tbCmbItemName.get(ar).setDescription("Click table header '"+"Item Name"+"' to select all.");
			tbCmbItemName.get(ar).setStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbItemName.get(ar).setFilteringMode(FilteringMode.CONTAINS);
			tbCmbItemName.get(ar).setInputPrompt("Item Name");
			tbCmbItemName.get(ar).setRequired(true);
			tbCmbItemName.get(ar).setRequiredError("This field is required");
			loadtableComboData(ar);
			tbCmbItemName.get(ar).addValueChangeListener(event ->
			{ ItemValidation(ar); });

			tblblUnitName.add(ar, new Label());
			tblblUnitName.get(ar).setWidth("100%");
			tblblUnitName.get(ar).setImmediate(true);
			tblblUnitName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tblblCatName.add(ar, new Label());
			tblblCatName.get(ar).setWidth("100%");
			tblblCatName.get(ar).setImmediate(true);
			tblblCatName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbTxtRemarks.add(ar, new TextField());
			tbTxtRemarks.get(ar).setWidth("100%");
			tbTxtRemarks.get(ar).setImmediate(true);
			tbTxtRemarks.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);

			tbAmtSoftQty.add(ar, new CommaField());
			tbAmtSoftQty.get(ar).setWidth("100%");
			tbAmtSoftQty.get(ar).setImmediate(true);
			tbAmtSoftQty.get(ar).setReadOnly(true);
			tbAmtSoftQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtSoftQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtSoftQty.get(ar).setDescription("Software Stock Qty");
			tbAmtSoftQty.get(ar).addValueChangeListener(event ->
			{ calculateVatData(ar); });

			tbAmtPhyQty.add(ar, new CommaField());
			tbAmtPhyQty.get(ar).setWidth("100%");
			tbAmtPhyQty.get(ar).setImmediate(true);
			tbAmtPhyQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtPhyQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtPhyQty.get(ar).setDescription("Physical Stock Qty");
			tbAmtPhyQty.get(ar).setInputPrompt("Phy.Stk Qty");
			tbAmtPhyQty.get(ar).setRequired(true);
			tbAmtPhyQty.get(ar).setRequiredError("This field is required.");
			tbAmtPhyQty.get(ar).addValueChangeListener(event ->
			{ 
				QtyValidation(ar);
				if ((ar+1) == tbCmbItemName.size())
				{ tableRowAdd(tbCmbItemName.size()); }
				tbAmtPhyQty.get(ar+1).focus();
			});

			tbAmtDiffQty.add(ar, new CommaField());
			tbAmtDiffQty.get(ar).setWidth("100%");
			tbAmtDiffQty.get(ar).setImmediate(true);
			tbAmtDiffQty.get(ar).setReadOnly(true);
			tbAmtDiffQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtDiffQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

			tbAmtPurchaseRate.add(ar, new CommaField());
			tbAmtPurchaseRate.get(ar).setWidth("100%");
			tbAmtPurchaseRate.get(ar).setImmediate(true);
			tbAmtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtPurchaseRate.get(ar).setReadOnly(true);

			tbAmtTotalAmount.add(ar, new CommaField());
			tbAmtTotalAmount.get(ar).setWidth("100%");
			tbAmtTotalAmount.get(ar).setImmediate(true);
			tbAmtTotalAmount.get(ar).setReadOnly(true);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tblblUnitId.add(ar, new Label());
			tblblUnitId.get(ar).setWidth("100%");
			tblblUnitId.get(ar).setImmediate(true);
			tblblUnitId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tblblCatId.add(ar, new Label());
			tblblCatId.get(ar).setWidth("100%");
			tblblCatId.get(ar).setImmediate(true);
			tblblCatId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbBtnRemove.add(ar, new Button(""));
			tbBtnRemove.get(ar).setWidth("100%");
			tbBtnRemove.get(ar).setImmediate(true);
			tbBtnRemove.get(ar).setIcon(FontAwesome.REMOVE);
			tbBtnRemove.get(ar).setStyleName(ValoTheme.BUTTON_TINY);
			tbBtnRemove.get(ar).addStyleName(ValoTheme.BUTTON_DANGER);
			tbBtnRemove.get(ar).setDescription("Remove");
			tbBtnRemove.get(ar).addClickListener(event ->
			{ removeRow(ar); });

			tblPhysicalStockDetailsList.addItem(new Object[]{tbBtnAddItem.get(ar), tbCmbItemName.get(ar), tblblUnitName.get(ar),tblblCatName.get(ar),
					tbTxtRemarks.get(ar), tbAmtSoftQty.get(ar), tbAmtPhyQty.get(ar), tbAmtDiffQty.get(ar), tbAmtPurchaseRate.get(ar), tbAmtTotalAmount.get(ar), 
					tblblUnitId.get(ar), tblblCatId.get(ar), tbBtnRemove.get(ar)}, ar);
		}
		catch(Exception exp)
		{
			cm.showNotification("failure", "Error!", "Can't add rows to table.");
		}
	}

	private void ItemValidation(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null)
		{
			addChanges();
			String selectId = tbCmbItemName.get(ar).getValue().toString();
			if (!checkDuplicate(selectId, ar))
			{ 
				setItemDetails(ar, selectId); 
				tbAmtPhyQty.get(ar).focus();
			}
			else
			{
				tbCmbItemName.get(ar).setValue(null);
				cm.showNotification("warning", "Warning!", "Duplicate item selected.");
				tbCmbItemName.get(ar).focus();
			}
		}
	}

	private boolean checkDuplicate(String selectId, int ar)
	{
		boolean ret = false;
		for (int i=0; i<tbCmbItemName.size(); i++)
		{
			if (i != ar)
			{
				if (tbCmbItemName.get(i).getValue() != null)
				{
					if (selectId.equals(tbCmbItemName.get(i).getValue().toString()))
					{
						ret = true;
						break;
					}
				}
			}
		}
		return ret;
	}

	public double totalAmount(String flag)
	{
		double ret = 0, totalamt = 0;

		for (int i=0; i<tbCmbItemName.size(); i++)
		{ totalamt += cm.getAmtValue(tbAmtTotalAmount.get(i)); }

		tblPhysicalStockDetailsList.setColumnFooter("Total Amount", cm.setComma(totalamt));
		tblPhysicalStockDetailsList.setColumnAlignment("Total Amount", Align.RIGHT);
		tblPhysicalStockDetailsList.setColumnFooter("Item Name", "Click table header '"+"Item Name"+"' to select all.");
		tblPhysicalStockDetailsList.setColumnAlignment("Item Name", Align.CENTER);

		if (flag.equals("Total")) { ret = totalamt; }
		return ret;
	}
}
