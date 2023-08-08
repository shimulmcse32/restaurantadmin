package com.example.postrans;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.MultiComboBox;
import com.example.gateway.StockAdjustmentGateway;
import com.example.model.StockAdjustmentModel;
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
public class AddEditStockAdjustment extends Window
{
	private SessionBean sessionBean;
	private String flag, adjustId;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private StockAdjustmentGateway sag = new StockAdjustmentGateway();

	private TextField txtAdjustNo, txtRemarks, txtReferenceNo;
	private PopupDateField txtAdjustDate;
	private ComboBox cmbStatus;
	private MultiComboBox cmbItemName;
	private Button btnItem;

	//Consume, Expired, Damage, Lost
	private Table tblAdjustDetailsList;
	private ArrayList<ComboBox> tbCmbItemName = new ArrayList<ComboBox>();
	private ArrayList<Label> tbLblUnitName = new ArrayList<Label>();
	private ArrayList<Label> tbLblCatName = new ArrayList<Label>();
	private ArrayList<ComboBox> tbCmbAdjType = new ArrayList<ComboBox>();
	private ArrayList<TextField> tbTxtRemarks = new ArrayList<TextField>();
	private ArrayList<CommaField> tbAmtStockQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtAdjustQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtPurchaseRate = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtTotalAmount = new ArrayList<CommaField>();
	private ArrayList<Label> tbLblUnitId  = new ArrayList<Label>();
	private ArrayList<Button> tbBtnAddItem = new ArrayList<Button>();
	private ArrayList<Button> tbBtnRemove  = new ArrayList<Button>();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private HashMap<String, String> hmStatus = new HashMap<String, String>();

	private CommonMethod cm;
	private boolean changes = false, action = false;

	public AddEditStockAdjustment(SessionBean sessionBean, String flag, String adjustId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.adjustId = adjustId;
		this.setCaption(flag+" Stock Adjustment :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);
		setWidth("100%");
		setHeight("580px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
		{ masterValidation(); });

		cBtn.btnExit.addClickListener(event ->
		{ close(); });

		txtAdjustDate.addValueChangeListener(event ->
		{ loadAdjustNo(); });

		btnItem.addClickListener(event ->
		{ addEditItem(); });

		cmbStatus.addValueChangeListener(event ->
		{ changeStatusData(); });

		cmbItemName.addValueChangeListener(event ->
		{ setItemToTable(); });

		loadAdjustNo();

		if (flag.equals("Edit"))
		{ setEditData(); }

		focusEnter();
	}

	private void loadAdjustNo()
	{
		if (!flag.equals("Edit"))
		{
			txtAdjustNo.setReadOnly(false);
			txtAdjustNo.setValue(sag.getAdjustNo(cm.dfDb.format(txtAdjustDate.getValue())));
			txtAdjustNo.setReadOnly(true);
		}
	}

	private void loadTableComboData(int ar)
	{
		String sql = "select vItemId, vItemName, vItemCode, dbo.funGetNumeric(vItemCode) iCode"+
				" from master.tbRawItemInfo where iActive = 1 order by iCode asc";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			if (ar == 0)
			{
				cmbItemName.addItem(element[0].toString());
				cmbItemName.setItemCaption(element[0].toString(),
						element[2].toString()+" - "+element[1].toString());
			}
			tbCmbItemName.get(ar).addItem(element[0].toString());
			tbCmbItemName.get(ar).setItemCaption(element[0].toString(),
					element[2].toString()+" - "+element[1].toString());
		}

		String sqlStatus = "select vStatusId, vStatusName, vFlag from master.tbStockStatus where iActive = 1"+
				" order by vStatusName";
		for (Iterator<?> iter = cm.selectSql(sqlStatus).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			if (ar == 0)
			{
				cmbStatus.addItem(element[0].toString());
				cmbStatus.setItemCaption(element[0].toString(), element[1].toString());
				hmStatus.put(element[0].toString(), element[2].toString());
			}
			tbCmbAdjType.get(ar).addItem(element[0].toString());
			tbCmbAdjType.get(ar).setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void changeStatusData()
	{
		for (int i=0; i<tbCmbItemName.size(); i++)
		{
			tbCmbAdjType.get(i).setEnabled(cmbStatus.getValue() == null);
			tbCmbAdjType.get(i).setValue(cmbStatus.getValue() == null? "":cmbStatus.getValue().toString());
		}
	}

	private void masterValidation()
	{
		if (!txtAdjustNo.getValue().toString().trim().isEmpty())
		{
			if (txtAdjustDate.getValue() != null)
			{
				if (totalAmount("Total") > 0)
				{
					cBtn.btnSave.setEnabled(false);
					insertEditData();
				}
				else
				{
					tbCmbItemName.get(0).focus();
					cm.showNotification("warning", "Warning!", "Provide valid data in the table.");
				}
			}
			else
			{
				txtAdjustDate.focus();
				cm.showNotification("warning", "Warning!", "Provide Adjustmant date.");
			}
		}
		else
		{
			txtAdjustNo.focus();
			cm.showNotification("warning", "Warning!", "Adjustment number not found.");
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
		StockAdjustmentModel sam = new StockAdjustmentModel();
		setValueForSave(sam);
		if (sag.insertEditData(sam, flag))
		{
			txtClear();
			cm.showNotification("success", "Successfull!", "All information saved successfully.");
			cBtn.btnSave.setEnabled(true);

			StockAdjustmentInfo report = new StockAdjustmentInfo(sessionBean, "");
			report.viewReportSingle(sam.getAdjustId());

			if (flag.equals("Edit"))
			{ close(); }
		}
		else
		{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
	}

	private void setValueForSave(StockAdjustmentModel sam)
	{
		sam.setBranchId(sessionBean.getBranchId());
		sam.setAdjustId(flag.equals("Add")? sag.getAdjustId(sessionBean.getBranchId()):adjustId);
		sam.setAdjustNo(flag.equals("Add")? sag.getAdjustNo(cm.dfDb.format(txtAdjustDate.getValue())).toString():
			txtAdjustNo.getValue().toString());
		sam.setAdjustDate(txtAdjustDate.getValue());
		sam.setRemarks(txtRemarks.getValue().toString().trim());
		sam.setReferenceNo(txtReferenceNo.getValue().toString());
		sam.setCreatedBy(sessionBean.getUserId());
		sam.setStatusId("S6");
		sam.setApproveBy(sessionBean.getUserId());
		sam.setCancelBy("");
		sam.setCancelReason("");
		sam.setDetailsSql(getDetails(sam.getAdjustId()));
		sam.setDetailsChange(changes);
	}

	private String getDetails(String adjustId)
	{
		String sql = flag.equals("Add") ? "" : "delete trans.tbStockAdjustmentDetails where vAdjustId = '"+adjustId+"' ";
		for (int i = 0; i < tbCmbItemName.size(); i++)
		{
			if (i == 0)
			{
				sql += "insert into trans.tbStockAdjustmentDetails(vAdjustId, vItemId, vRemarks,"+
						" vUnitId, vAdjustStatus, mQuantity, mUnitRate, mAmount, iActive) values ";
			}
			if (tbCmbItemName.get(i).getValue() != null && tbCmbAdjType.get(i).getValue() != null &&
					cm.getAmtValue(tbAmtAdjustQty.get(i)) > 0)
			{
				sql += "('"+adjustId+"', '"+tbCmbItemName.get(i).getValue().toString()+"',"+
						" '"+tbTxtRemarks.get(i).getValue().toString()+"',"+
						" '"+tbLblUnitId.get(i).getValue().toString()+"',"+
						" '"+tbCmbAdjType.get(i).getValue().toString()+"',"+
						" '"+cm.getAmtValue(tbAmtAdjustQty.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtPurchaseRate.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtTotalAmount.get(i))+"', 1),";
			}
		}
		return sql.substring(0, sql.length()-1);
	}

	private void setEditData()
	{
		StockAdjustmentModel sam = new StockAdjustmentModel();
		try
		{
			if (sag.selectEditData(sam, adjustId))
			{
				txtAdjustNo.setReadOnly(false);
				txtAdjustNo.setValue(sam.getAdjustNo());
				txtAdjustNo.setReadOnly(true);
				txtAdjustDate.setValue((Date) sam.getAdjustDate());
				txtRemarks.setValue(sam.getRemarks());
				txtReferenceNo.setValue(sam.getReferenceNo());
				setEditDetailsValue(adjustId);
				cmbItemName.setEnabled(false);
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private void setEditDetailsValue(String adjustId)
	{
		int ar = 0;
		double stock = 0;
		String sql = "select sd.vAdjustId, sd.vItemId, sd.vUnitId, sd.vAdjustStatus, sd.mQuantity, sd.mUnitRate,"+
				" sd.mAmount, sd.iActive, sd.vRemarks, ui.vUnitName from trans.tbStockAdjustmentDetails sd,"+
				" master.tbUnitInfo ui where sd.vUnitId = ui.iUnitId and sd.vAdjustId = '"+adjustId+"' and sd.iActive = 1";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tbCmbItemName.get(ar).setValue(element[1].toString());
			tbLblUnitId.get(ar).setValue(element[2].toString());
			tbCmbAdjType.get(ar).setValue(element[3].toString());
			tbAmtAdjustQty.get(ar).setValue(cm.deciMn.format(Double.parseDouble(element[4].toString())));

			tbAmtPurchaseRate.get(ar).setReadOnly(false);
			tbAmtPurchaseRate.get(ar).setValue(cm.deciMn.format(Double.parseDouble(element[5].toString())));
			tbAmtPurchaseRate.get(ar).setReadOnly(true);

			stock = cm.getAmtValue(tbAmtStockQty.get(ar));
			tbAmtStockQty.get(ar).setReadOnly(false);
			tbAmtStockQty.get(ar).setValue(cm.deciFloat.format(stock - Double.parseDouble(element[4].toString())));
			tbAmtStockQty.get(ar).setReadOnly(true);

			tbTxtRemarks.get(ar).setValue(element[8].toString());
			tbLblUnitName.get(ar).setValue(element[9].toString());
			ar++;
		}
		action = true;
	}

	private void setItemToTable()
	{
		tableClear();
		String ids = cmbItemName.getValue().toString().replace("]", "").replace("[", "").trim();
		int ar = 0;
		try
		{
			String sql = "select vItemId, vItemName, vItemCode, dbo.funGetNumeric(vItemCode) iCode"+
					" from master.tbRawItemInfo where iActive = 1 and vItemId in (select Item from"+
					" dbo.Split('"+ids+"')) order by iCode asc";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				if (tbCmbItemName.size() <= ar)
				{ tableRowAdd(ar); }
				tbCmbItemName.get(ar).setValue(element[0].toString());
				ar++;
			}
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void addEditItem()
	{
		String itemId = cmbItemName.getValue().toString().replace("[", "").replace("]", "").trim();
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
			{ loadTableComboData(i); } 
		});
	}

	private void addChanges()
	{
		if (flag.equals("Edit") && action)
		{ changes = true; }
	}

	private void tableClear()
	{
		tblAdjustDetailsList.removeAllItems();
		tbCmbItemName.clear();
		tableRowAdd(0);
	}

	private void focusEnter()
	{
		allComp.add(txtReferenceNo);
		allComp.add(txtRemarks);
		allComp.add(tbCmbItemName.get(0));

		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		txtAdjustNo.setReadOnly(false);
		txtAdjustNo.setValue(sag.getAdjustNo(cm.dfDb.format(txtAdjustDate.getValue())));
		txtAdjustNo.setReadOnly(true);
		txtRemarks.setValue("");
		txtReferenceNo.setValue("");
		txtAdjustDate.setValue(new Date());
		tableClear();
		totalAmount("");
	}

	//Master Component Set
	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(16, 4);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtAdjustNo = new TextField();
		txtAdjustNo.setImmediate(true);
		txtAdjustNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtAdjustNo.setWidth("200px");
		txtAdjustNo.setInputPrompt("Adjust No");
		txtAdjustNo.setDescription("Adjust No");
		txtAdjustNo.setRequired(true);
		txtAdjustNo.setRequiredError("This field is required.");
		Label lblAdj = new Label("Adjustment No: ");
		lblAdj.setWidth("-1px");
		grid.addComponent(lblAdj, 0, 0);
		grid.addComponent(txtAdjustNo, 1, 0, 2, 0);

		txtAdjustDate  = new PopupDateField();
		txtAdjustDate.setImmediate(true);
		txtAdjustDate.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtAdjustDate.setValue(new Date());
		txtAdjustDate.setWidth("110");
		txtAdjustDate.setDateFormat("dd-MM-yyyy");
		txtAdjustDate.setDescription("Adjust Date");
		txtAdjustDate.setRequired(true);
		txtAdjustDate.setRequiredError("This field is required");
		grid.addComponent(new Label("Date: "), 3, 0);
		grid.addComponent(txtAdjustDate, 4, 0);

		Label lblref = new Label("Reference: ");
		lblref.setWidth("-1px");
		grid.addComponent(lblref, 5, 0);

		txtReferenceNo = new TextField();
		txtReferenceNo.setImmediate(true);
		txtReferenceNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReferenceNo.setWidth("100%");
		txtReferenceNo.setInputPrompt("Reference No");
		grid.addComponent(txtReferenceNo, 6, 0, 15, 0);

		Label lblitem = new Label("Select Item(s): ");
		lblitem.setWidth("-1px");
		grid.addComponent(lblitem, 0, 1);

		CssLayout group = new CssLayout();
		group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		cmbItemName = new MultiComboBox();
		cmbItemName.setInputPrompt("Select Item Name");
		cmbItemName.setWidth("350px");
		cmbItemName.setDescription("Select item to load in table");
		btnItem = new Button();
		btnItem.setIcon(FontAwesome.PLUS);
		btnItem.setStyleName(ValoTheme.BUTTON_TINY);
		btnItem.setDescription("Add Item Name");
		group.addComponents(cmbItemName, btnItem);
		grid.addComponent(cmbItemName, 1, 1, 2, 1);

		cmbStatus = new ComboBox();
		cmbStatus.setImmediate(true);
		cmbStatus.setWidth("140px");
		cmbStatus.setInputPrompt("Select Status");
		cmbStatus.setDescription("Select Status");
		cmbStatus.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbStatus.setFilteringMode(FilteringMode.CONTAINS);
		Label lblSt = new Label("Status: ");
		lblSt.setWidth("-1px");
		grid.addComponent(lblSt, 3, 1);
		grid.addComponent(cmbStatus, 4, 1);

		Label lblrem = new Label("Remarks: ");
		lblrem.setWidth("-1px");
		grid.addComponent(lblrem, 5, 1);

		txtRemarks = new TextField();
		txtRemarks.setImmediate(true);
		txtRemarks.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRemarks.setWidth("100%");
		txtRemarks.setInputPrompt("Remarks");
		grid.addComponent(txtRemarks, 6, 1, 15, 1);

		grid.addComponent(buildTable(), 0, 2, 15, 2);

		grid.addComponent(cBtn, 0, 3, 15, 3);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);
		return grid;
	}

	//Table Component Set
	private Table buildTable()
	{
		tblAdjustDetailsList = new Table();
		tblAdjustDetailsList.setSelectable(true);
		tblAdjustDetailsList.setFooterVisible(true);
		tblAdjustDetailsList.setColumnCollapsingAllowed(true);
		tblAdjustDetailsList.addStyleName(ValoTheme.TABLE_SMALL);
		tblAdjustDetailsList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblAdjustDetailsList.setPageLength(9);
		tblAdjustDetailsList.setWidth("100%");

		tblAdjustDetailsList.addContainerProperty("Add", Button.class, new Button(), null, null, Align.CENTER);
		tblAdjustDetailsList.setColumnWidth("Add", 50);

		tblAdjustDetailsList.addContainerProperty("Item Name", ComboBox.class, new ComboBox(), null, null, Align.CENTER);

		tblAdjustDetailsList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);

		tblAdjustDetailsList.addContainerProperty("Category", Label.class, new Label(), null, null, Align.CENTER);

		tblAdjustDetailsList.addContainerProperty("Remarks", TextField.class, new TextField(), null, null, Align.CENTER);
		tblAdjustDetailsList.setColumnWidth("Remarks", 200);
		tblAdjustDetailsList.setColumnCollapsed("Remarks", true);

		tblAdjustDetailsList.addContainerProperty("Adj. Type", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblAdjustDetailsList.setColumnWidth("Adj. Type", 130);

		tblAdjustDetailsList.addContainerProperty("Cur. Stk.", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblAdjustDetailsList.setColumnWidth("Cur. Stk.", 100);

		tblAdjustDetailsList.addContainerProperty("Adj. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblAdjustDetailsList.setColumnWidth("Adj. Qty", 100);

		tblAdjustDetailsList.addContainerProperty("Rate", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblAdjustDetailsList.setColumnWidth("Rate", 100);

		tblAdjustDetailsList.addContainerProperty("Total Amount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblAdjustDetailsList.setColumnWidth("Total Amount", 120);

		tblAdjustDetailsList.addContainerProperty("Unit Id", Label.class, new Label(), null, null, Align.CENTER);
		tblAdjustDetailsList.setColumnCollapsed("Unit Id", true);

		tblAdjustDetailsList.addContainerProperty("Rem", Button.class, new Button(), null, null, Align.CENTER);
		tblAdjustDetailsList.setColumnWidth("Rem", 50);
		tableRowAdd(0);
		return tblAdjustDetailsList;
	}

	private void tableRowAdd(int ar)
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
			tbCmbItemName.get(ar).setStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbItemName.get(ar).setFilteringMode(FilteringMode.CONTAINS);
			tbCmbItemName.get(ar).setRequired(true);
			tbCmbItemName.get(ar).setRequiredError("This field is required");
			tbCmbItemName.get(ar).setInputPrompt("Select item name");
			tbCmbItemName.get(ar).addValueChangeListener(event -> setItemActions(ar));

			tbLblUnitName.add(ar, new Label());
			tbLblUnitName.get(ar).setWidth("100%");
			tbLblUnitName.get(ar).setImmediate(true);
			tbLblUnitName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblCatName.add(ar, new Label());
			tbLblCatName.get(ar).setWidth("100%");
			tbLblCatName.get(ar).setImmediate(true);
			tbLblCatName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbTxtRemarks.add(ar, new TextField());
			tbTxtRemarks.get(ar).setWidth("100%");
			tbTxtRemarks.get(ar).setImmediate(true);
			tbTxtRemarks.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);

			tbCmbAdjType.add(ar, new ComboBox());
			tbCmbAdjType.get(ar).setWidth("100%");
			tbCmbAdjType.get(ar).setImmediate(true);
			tbCmbAdjType.get(ar).setNullSelectionAllowed(false);
			tbCmbAdjType.get(ar).setFilteringMode(FilteringMode.CONTAINS);
			tbCmbAdjType.get(ar).addStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbAdjType.get(ar).setDescription("Adjustment Type");
			tbCmbAdjType.get(ar).setInputPrompt("Adjustment Type");
			tbCmbAdjType.get(ar).setRequired(true);
			tbCmbAdjType.get(ar).setRequiredError("This field is required");
			tbCmbAdjType.get(ar).addValueChangeListener(event ->
			{ setStatusActions(ar); });
			loadTableComboData(ar);

			tbAmtStockQty.add(ar, new CommaField());
			tbAmtStockQty.get(ar).setWidth("100%");
			tbAmtStockQty.get(ar).setImmediate(true);
			tbAmtStockQty.get(ar).setReadOnly(true);
			tbAmtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtStockQty.get(ar).setDescription("Current Stock");

			tbAmtAdjustQty.add(ar, new CommaField());
			tbAmtAdjustQty.get(ar).setWidth("100%");
			tbAmtAdjustQty.get(ar).setImmediate(true);
			tbAmtAdjustQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtAdjustQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtAdjustQty.get(ar).setRequired(true);
			tbAmtAdjustQty.get(ar).setRequiredError("This field is required");
			tbAmtAdjustQty.get(ar).setDescription("Adjust Qty");
			tbAmtAdjustQty.get(ar).setInputPrompt("Adjust Qty");
			tbAmtAdjustQty.get(ar).addValueChangeListener(event ->
			{ setRowAmount(ar); });

			tbAmtPurchaseRate.add(ar, new CommaField());
			tbAmtPurchaseRate.get(ar).setWidth("100%");
			tbAmtPurchaseRate.get(ar).setImmediate(true);
			tbAmtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtPurchaseRate.get(ar).setReadOnly(true);
			tbAmtPurchaseRate.get(ar).addValueChangeListener(event ->
			{ setRowAmount(ar); });

			tbAmtTotalAmount.add(ar, new CommaField());
			tbAmtTotalAmount.get(ar).setWidth("100%");
			tbAmtTotalAmount.get(ar).setImmediate(true);
			tbAmtTotalAmount.get(ar).setReadOnly(true);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbLblUnitId.add(ar, new Label());
			tbLblUnitId.get(ar).setWidth("100%");
			tbLblUnitId.get(ar).setImmediate(true);
			tbLblUnitId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbBtnRemove.add(ar, new Button(""));
			tbBtnRemove.get(ar).setWidth("100%");
			tbBtnRemove.get(ar).setImmediate(true);
			tbBtnRemove.get(ar).setIcon(FontAwesome.REMOVE);
			tbBtnRemove.get(ar).setStyleName(ValoTheme.BUTTON_TINY);
			tbBtnRemove.get(ar).addStyleName(ValoTheme.BUTTON_DANGER);
			tbBtnRemove.get(ar).setDescription("Remove");
			tbBtnRemove.get(ar).addClickListener(event ->
			{ removeRow(ar); });

			tblAdjustDetailsList.addItem(new Object[]{tbBtnAddItem.get(ar), tbCmbItemName.get(ar),
					tbLblUnitName.get(ar), tbLblCatName.get(ar), tbTxtRemarks.get(ar), tbCmbAdjType.get(ar),
					tbAmtStockQty.get(ar), tbAmtAdjustQty.get(ar), tbAmtPurchaseRate.get(ar),
					tbAmtTotalAmount.get(ar), tbLblUnitId.get(ar), tbBtnRemove.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void setStatusActions(int ar)
	{
		String status = "";
		if (tbCmbAdjType.get(ar).getValue() != null)
		{
			status = hmStatus.get(tbCmbAdjType.get(ar).getValue().toString()) != null?
					hmStatus.get(tbCmbAdjType.get(ar).getValue().toString()):"";
		}
		if (status.equals("Deduction"))
		{
			if (cm.getAmtValue(tbAmtAdjustQty.get(ar)) > cm.getAmtValue(tbAmtStockQty.get(ar)))
			{
				tbAmtAdjustQty.get(ar).setValue("0");
				cm.showNotification("warning", "Warning!", "Deduction cann't be greater than stock qty.");
			}
		}
		tbAmtAdjustQty.get(ar).focus();
	}

	private void setRowAmount(int ar)
	{
		setStatusActions(ar);
		totalAmountRow(ar);
		if ((ar+1) == tbCmbItemName.size())
		{ tableRowAdd(tbCmbItemName.size()); }
		tbCmbItemName.get(ar+1).focus();
	}

	private void totalAmountRow(int ar)
	{
		double TotalQty = cm.getAmtValue(tbAmtAdjustQty.get(ar));
		double Rate = cm.getAmtValue(tbAmtPurchaseRate.get(ar));
		double TotalAmountCal = cm.getRound(TotalQty * Rate);

		tbAmtTotalAmount.get(ar).setReadOnly(false);
		tbAmtTotalAmount.get(ar).setValue(cm.setComma(TotalAmountCal));
		tbAmtTotalAmount.get(ar).setReadOnly(true);
		totalAmount("Total");
		addChanges();
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
			{ loadTableComboData(ar); } 
		});
	}

	private void removeRow(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null && tbCmbItemName.size() > 0 &&
				!tbAmtTotalAmount.get(ar).getValue().toString().isEmpty())
		{
			tbCmbItemName.get(ar).setValue(null);
			tblAdjustDetailsList.removeItem(ar);
			tbCmbItemName.get(ar).clear();
			tbAmtTotalAmount.get(ar).setReadOnly(false);
			tbAmtTotalAmount.get(ar).clear();
		}
		totalAmount("Total");
	}

	private void setItemActions(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null)
		{
			addChanges();
			String selectId = tbCmbItemName.get(ar).getValue().toString();
			if (!checkDuplicate(selectId, ar))
			{
				setItemDetails(ar, selectId);
				tbAmtAdjustQty.get(ar).focus();
			}
			else
			{
				tbCmbItemName.get(ar).setValue(null);
				cm.showNotification("warning", "Warning!", "Duplicate item selected.");
				tbCmbItemName.get(ar).focus();
			}
		}
	}

	private void setItemDetails(int ar, String ItemId)
	{
		try
		{
			String sql = "select U.vUnitName, C.vCategoryName, I.vUnitId, I.vCategoryId, (select [dbo].[funcItemRate]"+
					" ('"+ItemId+"', '"+sessionBean.getBranchId()+"')) mPurchaseRate, (select [dbo].[funcStockQty]"+
					" ('"+ItemId+"', '"+sessionBean.getBranchId()+"')) mStockQty from master.tbRawItemInfo I inner"+
					" join master.tbUnitInfo U on I.vUnitId = U.iUnitId inner join master.tbItemCategory C on"+
					" I.vCategoryId = C.vCategoryId where I.vItemId = '"+ItemId+"'";

			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				tbLblUnitName.get(ar).setValue(element[0].toString());
				tbLblCatName.get(ar).setValue(element[1].toString());
				tbLblUnitId.get(ar).setValue(element[2].toString());

				tbAmtStockQty.get(ar).setReadOnly(false);
				tbAmtStockQty.get(ar).setValue(cm.deciMn.format(Double.parseDouble(element[5].toString())));
				tbAmtStockQty.get(ar).setReadOnly(true);

				tbAmtPurchaseRate.get(ar).setReadOnly(false);
				tbAmtPurchaseRate.get(ar).setValue(cm.deciMn.format(Double.parseDouble(element[4].toString())));
				tbAmtPurchaseRate.get(ar).setReadOnly(true);
			}
		}
		catch (Exception e)
		{ System.out.println(e); }
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
					{ ret = true; break; }
				}
			}
		}
		return ret;
	}

	private double totalAmount(String flag)
	{
		double ret = 0, totalAmt = 0;

		for (int i=0; i<tbCmbItemName.size(); i++)
		{
			if (tbCmbItemName.get(i).getValue() != null && tbCmbAdjType.get(i).getValue() != null &&
					cm.getAmtValue(tbAmtAdjustQty.get(i)) > 0)
			{ totalAmt += cm.getAmtValue(tbAmtTotalAmount.get(i)); }
		}
		tblAdjustDetailsList.setColumnFooter("Total Amount", cm.setComma(totalAmt));
		tblAdjustDetailsList.setColumnAlignment("Total Amount", Align.RIGHT);

		if (flag.equals("Total")) { ret = totalAmt; }
		return ret;
	}
}
