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
import com.example.gateway.RequisitionInfoGateway;
import com.example.model.RequisitionInfoModel;
import com.example.possetup.AddEditRawItemInfo;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditRequisition extends Window
{
	private SessionBean sessionBean;
	private String flag, reqId;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private RequisitionInfoGateway rig = new RequisitionInfoGateway();

	private TextField txtRequisitionNo, txtRemarks, txtReferenceNo;
	private ComboBox cmbRequestedBranch;
	private PopupDateField txtRequisitionDate, txtDeliveryDate;

	private Table tblRequisitionList;
	private ArrayList<ComboBox> tbCmbItemName = new ArrayList<ComboBox>();
	private ArrayList<Label> tbLblUnitName = new ArrayList<Label>();
	private ArrayList<Label> tbLblCatName = new ArrayList<Label>();
	private ArrayList<TextField> tbTxtDescription = new ArrayList<TextField>();
	private ArrayList<CommaField> tbAmtStockQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtRequestQty = new ArrayList<CommaField>();
	private ArrayList<Label> tbLblUnitId  = new ArrayList<Label>();
	private ArrayList<Label> tbLblCatId  = new ArrayList<Label>();
	private ArrayList<Button> tbBtnAddItem  = new ArrayList<Button>();
	private ArrayList<Button> tbBtnRemove  = new ArrayList<Button>();

	private ArrayList<Component> allComp = new ArrayList<Component>();
	private HashMap<String, String> hmBranch = new HashMap<String, String>();
	private CommonMethod cm;
	private boolean changes = false, action = false;

	public AddEditRequisition(SessionBean sessionBean, String flag, String reqId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.reqId = reqId;
		this.setCaption(flag+" Requisition :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);
		setWidth("100%");
		setHeight("610px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
		{ addValidation(); });

		cBtn.btnExit.addClickListener(event ->
		{ close(); });

		txtRequisitionDate.addValueChangeListener(event ->
		{ loadRequisitionNo(); });

		cmbRequestedBranch.addValueChangeListener(event ->
		{
			for (int i=0; i<tbCmbItemName.size(); i++)
			{ loadtableComboData(i); }
		});

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void addValidation()
	{
		if (cmbRequestedBranch.getValue() != null)
		{
			if (!txtRequisitionNo.getValue().toString().trim().isEmpty())
			{
				if (txtRequisitionDate.getValue() != null)
				{
					if (totalQty() > 0)
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
					txtRequisitionDate.focus();
					cm.showNotification("warning", "Warning!", "Provide requisition date.");
				}
			}
			else
			{
				txtRequisitionNo.focus();
				cm.showNotification("warning", "Warning!", "Requisition number not found.");
			}
		}
		else
		{
			cmbRequestedBranch.focus();
			cm.showNotification("warning", "Warning!", "Select Branch Name.");
		}
	}

	private double totalQty()
	{
		double ret = 0;
		for (int i=0; i<tbCmbItemName.size(); i++)
		{
			if (tbCmbItemName.get(i).getValue() != null && cm.getAmtValue(tbAmtRequestQty.get(i)) > 0)
			{ ret += cm.getAmtValue(tbAmtRequestQty.get(i)); }
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
		RequisitionInfoModel pom = new RequisitionInfoModel();
		getValue(pom);
		if (rig.insertEditData(pom, flag))
		{
			txtClear();
			cm.showNotification("success", "Successfull!", "All information saved successfully.");
			cBtn.btnSave.setEnabled(true);

			RequisitionInformation report = new RequisitionInformation(sessionBean, "");
			report.viewReport(pom.getRequisitionId());

			if (flag.equals("Edit"))
			{ close(); }
		}
		else
		{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
	}

	private void getValue(RequisitionInfoModel pom)
	{
		pom.setBranchId(sessionBean.getBranchId());
		pom.setRequisitionId(flag.equals("Add")? rig.getRequisitionId(sessionBean.getBranchId()):reqId);
		pom.setRequisitionNo(flag.equals("Add")? rig.getRequisitionNo(cm.dfDb.format(txtRequisitionDate.getValue())).toString():
			txtRequisitionNo.getValue().toString());
		pom.setReqBranchId((cmbRequestedBranch.getValue() == null) ? "":cmbRequestedBranch.getValue().toString());
		pom.setRequisitionDate(txtRequisitionDate.getValue());
		pom.setDeliveryDate(txtDeliveryDate.getValue());
		pom.setRemarks(txtRemarks.getValue().toString().trim());
		pom.setReferenceNo(txtReferenceNo.getValue().toString());
		pom.setCreatedBy(sessionBean.getUserId());
		pom.setStatusId("S5");
		pom.setApproveBy("");
		pom.setCancelBy("");
		pom.setCancelReason("");
		pom.setDetailsSql(getDetails(pom.getRequisitionId()));
		pom.setDetailsChange(changes);
	}

	private String getDetails(String reqId)
	{
		String sql = flag.equals("Add") ? "" : "delete trans.tbRequisitionDetails where vRequisitionId = '"+reqId+"' ";
		for (int i = 0; i < tbCmbItemName.size(); i++)
		{
			if (i == 0)
			{
				sql += "insert into trans.tbRequisitionDetails(vRequisitionId, vItemId, vDescription,"+
						" vUnitId, mQuantity, iActive) values ";
			}
			if (tbCmbItemName.get(i).getValue() != null && cm.getAmtValue(tbAmtRequestQty.get(i)) > 0)
			{
				sql += "('"+reqId+"', '"+tbCmbItemName.get(i).getValue().toString()+"',"+
						" '"+tbTxtDescription.get(i).getValue().toString()+"',"+
						" '"+tbLblUnitId.get(i).getValue().toString()+"',"+
						" '"+cm.getAmtValue(tbAmtRequestQty.get(i))+"', 1),";
			}
		}
		return sql.substring(0, sql.length()-1);
	}

	private void setEditData()
	{
		RequisitionInfoModel pom = new RequisitionInfoModel();
		try
		{
			if (rig.selectEditData(pom, reqId))
			{
				txtRequisitionDate.setValue((Date) pom.getRequisitionDate());
				cmbRequestedBranch.setValue(pom.getReqBranchId());
				txtDeliveryDate.setValue((Date) pom.getDeliveryDate());
				txtRemarks.setValue(pom.getRemarks());
				txtReferenceNo.setValue(pom.getReferenceNo());
				txtRequisitionNo.setReadOnly(false);
				txtRequisitionNo.setValue(pom.getRequisitionNo());
				txtRequisitionNo.setReadOnly(true);
				setValue(reqId);
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private void setValue(String reqId)
	{
		int ar = 0;
		String sql = "select red.vItemId, red.vUnitId, ui.vUnitName, red.mQuantity, red.vDescription from"+
				" trans.tbRequisitionDetails red, master.tbUnitInfo ui where red.vUnitId = ui.iUnitId"+
				" and vRequisitionId = '"+reqId+"'";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tbCmbItemName.get(ar).setValue(element[0].toString());
			tbLblUnitId.get(ar).setValue(element[1].toString());
			tbLblUnitName.get(ar).setValue(element[2].toString());
			tbAmtRequestQty.get(ar).setValue(Double.parseDouble(element[3].toString()));
			tbTxtDescription.get(ar).setValue(element[4].toString());
			ar++;
		}
		action = true;
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(8, 4);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtRequisitionNo = new TextField();
		txtRequisitionNo.setImmediate(true);
		txtRequisitionNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRequisitionNo.setWidth("-1px");
		txtRequisitionNo.setInputPrompt("Requisition No");
		txtRequisitionNo.setDescription("Requisition No");
		txtRequisitionNo.setRequired(true);
		txtRequisitionNo.setRequiredError("This field is required.");
		grid.addComponent(new Label("Requisition No: "), 0, 0);
		grid.addComponent(txtRequisitionNo, 1, 0);

		cmbRequestedBranch = new ComboBox();
		cmbRequestedBranch.setImmediate(true);
		cmbRequestedBranch.setWidth("300px");
		cmbRequestedBranch.setInputPrompt("Select Branch Name");
		cmbRequestedBranch.setDescription("Select branch name");
		cmbRequestedBranch.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbRequestedBranch.setFilteringMode(FilteringMode.CONTAINS);
		cmbRequestedBranch.setRequired(true);
		cmbRequestedBranch.setRequiredError("This field is required.");
		Label lbls = new Label("Request To: ");
		lbls.setWidth("-1px");
		grid.addComponent(lbls, 0, 1);
		grid.addComponent(cmbRequestedBranch, 1, 1);

		txtRequisitionDate  = new PopupDateField();
		txtRequisitionDate.setImmediate(true);
		txtRequisitionDate.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRequisitionDate.setValue(new Date());
		txtRequisitionDate.setWidth("110px");
		txtRequisitionDate.setDateFormat("dd-MM-yyyy");
		txtRequisitionDate.setDescription("Requisition Date");
		txtRequisitionDate.setRequired(true);
		txtRequisitionDate.setRequiredError("This field is required");
		Label lblReqd = new Label("Requisition Date: ");
		lblReqd.setWidth("-1px");
		grid.addComponent(lblReqd, 2, 0);
		grid.addComponent(txtRequisitionDate, 3, 0);

		txtDeliveryDate  = new PopupDateField();
		txtDeliveryDate.setImmediate(true);
		txtDeliveryDate.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDeliveryDate.setValue(new Date());
		txtDeliveryDate.setWidth("110px");
		txtDeliveryDate.setDateFormat("dd-MM-yyyy");
		grid.addComponent(new Label("Delivery Date: "), 2, 1);
		grid.addComponent(txtDeliveryDate, 3, 1);

		txtReferenceNo = new TextField();
		txtReferenceNo.setImmediate(true);
		txtReferenceNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReferenceNo.setWidth("250px");
		txtReferenceNo.setInputPrompt("Reference");
		Label lblRef = new Label("Reference: ");
		lblRef.setWidth("-1px");
		grid.addComponent(lblRef, 4, 0);
		grid.addComponent(txtReferenceNo, 5, 0, 7, 0);

		txtRemarks = new TextField();
		txtRemarks.setImmediate(true);
		txtRemarks.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRemarks.setWidth("250px");
		txtRemarks.setInputPrompt("Remarks");
		grid.addComponent(new Label("Remarks: "), 4, 1);
		grid.addComponent(txtRemarks, 5, 1, 7, 1);

		grid.addComponent(buildTable(), 0, 2, 7, 2);

		grid.addComponent(cBtn, 0, 3, 7, 3);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		loadSupplier();
		loadRequisitionNo();

		return grid;
	}

	private Table buildTable()
	{
		tblRequisitionList = new Table();
		tblRequisitionList.setSelectable(true);
		tblRequisitionList.setFooterVisible(true);
		tblRequisitionList.setColumnCollapsingAllowed(true);
		tblRequisitionList.addStyleName(ValoTheme.TABLE_SMALL);
		tblRequisitionList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblRequisitionList.setPageLength(10);
		tblRequisitionList.setWidth("100%");

		tblRequisitionList.addContainerProperty("Add", Button.class, new Button(), null, null, Align.CENTER);
		tblRequisitionList.setColumnWidth("Add", 50);

		tblRequisitionList.addContainerProperty("Item Name", ComboBox.class, new ComboBox(), null, null, Align.CENTER);

		tblRequisitionList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);

		tblRequisitionList.addContainerProperty("Category", Label.class, new Label(), null, null, Align.CENTER);

		tblRequisitionList.addContainerProperty("Description", TextField.class, new TextField(), null, null, Align.CENTER);

		tblRequisitionList.addContainerProperty("Cur. Stk.", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblRequisitionList.setColumnWidth("Cur. Stk.", 100);

		tblRequisitionList.addContainerProperty("Req. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblRequisitionList.setColumnWidth("Req. Qty", 100);

		tblRequisitionList.addContainerProperty("Unit Id", Label.class, new Label(), null, null, Align.CENTER);
		tblRequisitionList.setColumnCollapsed("Unit Id", true);

		tblRequisitionList.addContainerProperty("Category Id", Label.class, new Label(), null, null, Align.CENTER);
		tblRequisitionList.setColumnCollapsed("Category Id", true);

		tblRequisitionList.addContainerProperty("Rem", Button.class, new Button(), null, null, Align.CENTER);
		tblRequisitionList.setColumnWidth("Rem", 50);
		tableRowAdd(0);

		return tblRequisitionList;
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbBtnAddItem.add(ar, new Button());
			tbBtnAddItem.get(ar).setWidth("100%");
			tbBtnAddItem.get(ar).setImmediate(true);
			tbBtnAddItem.get(ar).setIcon(FontAwesome.PLUS);
			tbBtnAddItem.get(ar).setStyleName(ValoTheme.BUTTON_TINY);
			tbBtnAddItem.get(ar).setDescription("Add Item");
			cm.setAuthorize(sessionBean.getUserId(), "rawItem");
			tbBtnAddItem.get(ar).setEnabled(cm.insert);
			tbBtnAddItem.get(ar).addClickListener(event ->
			{ addEditItem(ar); addChanges(); });

			tbCmbItemName.add(ar, new ComboBox());
			tbCmbItemName.get(ar).setWidth("100%");
			tbCmbItemName.get(ar).setImmediate(true);
			tbCmbItemName.get(ar).setStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbItemName.get(ar).setFilteringMode(FilteringMode.CONTAINS);
			tbCmbItemName.get(ar).setRequired(true);
			tbCmbItemName.get(ar).setRequiredError("This field is required");
			tbCmbItemName.get(ar).setInputPrompt("Select Item Name");
			loadtableComboData(ar);
			tbCmbItemName.get(ar).addValueChangeListener(event ->
			{ setItemActions(ar); });

			tbLblUnitName.add(ar, new Label());
			tbLblUnitName.get(ar).setWidth("100%");
			tbLblUnitName.get(ar).setImmediate(true);
			tbLblUnitName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblCatName.add(ar, new Label());
			tbLblCatName.get(ar).setWidth("100%");
			tbLblCatName.get(ar).setImmediate(true);
			tbLblCatName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbTxtDescription.add(ar, new TextField());
			tbTxtDescription.get(ar).setWidth("100%");
			tbTxtDescription.get(ar).setImmediate(true);
			tbTxtDescription.get(ar).setInputPrompt("Description");
			tbTxtDescription.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);

			tbAmtStockQty.add(ar, new CommaField());
			tbAmtStockQty.get(ar).setWidth("100%");
			tbAmtStockQty.get(ar).setImmediate(true);
			tbAmtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtStockQty.get(ar).setDescription("Current Stock");
			tbAmtStockQty.get(ar).setInputPrompt("Current Stock");
			tbAmtStockQty.get(ar).setReadOnly(true);

			tbAmtRequestQty.add(ar, new CommaField());
			tbAmtRequestQty.get(ar).setWidth("100%");
			tbAmtRequestQty.get(ar).setImmediate(true);
			tbAmtRequestQty.get(ar).setRequired(true);
			tbAmtRequestQty.get(ar).setRequiredError("This field is required.");
			tbAmtRequestQty.get(ar).setDescription("Request Qty");
			tbAmtRequestQty.get(ar).setInputPrompt("Request Qty");
			tbAmtRequestQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtRequestQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtRequestQty.get(ar).addValueChangeListener(event ->
			{
				addChanges();
				if ((ar+1) == tbCmbItemName.size())
				{ 
					tableRowAdd(tbCmbItemName.size());
					tbCmbItemName.get(ar+1).focus();
				}
			});

			tbLblUnitId.add(ar, new Label());
			tbLblUnitId.get(ar).setWidth("100%");
			tbLblUnitId.get(ar).setImmediate(true);
			tbLblUnitId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblCatId.add(ar, new Label());
			tbLblCatId.get(ar).setWidth("100%");
			tbLblCatId.get(ar).setImmediate(true);
			tbLblCatId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbBtnRemove.add(ar, new Button());
			tbBtnRemove.get(ar).setWidth("100%");
			tbBtnRemove.get(ar).setIcon(FontAwesome.REMOVE);
			tbBtnRemove.get(ar).addStyleName(ValoTheme.BUTTON_TINY);
			tbBtnRemove.get(ar).addStyleName(ValoTheme.BUTTON_DANGER);
			tbBtnRemove.get(ar).setDescription("Remove Row");
			tbBtnRemove.get(ar).addClickListener(event ->
			{ removeRow(ar); addChanges(); });

			tblRequisitionList.addItem(new Object[]{tbBtnAddItem.get(ar), tbCmbItemName.get(ar), tbLblUnitName.get(ar),
					tbLblCatName.get(ar), tbTxtDescription.get(ar), tbAmtStockQty.get(ar), tbAmtRequestQty.get(ar),
					tbLblUnitId.get(ar), tbLblCatId.get(ar), tbBtnRemove.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
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
			{ loadtableComboData(i); }
		});
	}

	private void loadtableComboData(int ar)
	{
		tbCmbItemName.get(ar).removeAllItems();

		String sql = "", type = "", item = "Raw";

		if (cmbRequestedBranch.getValue() != null)
		{ type = hmBranch.get(cmbRequestedBranch.getValue().toString()); }

		if (type.equals("4"))
		{ item = "Semi-Cooked"; }

		sql = "select vItemId, vItemCode, vItemName, dbo.funGetNumeric(vItemCode)iSerial from"+
				" master.tbRawItemInfo where vItemType = '"+item+"' and iActive = 1 order by iSerial asc";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tbCmbItemName.get(ar).addItem(element[0].toString());
			tbCmbItemName.get(ar).setItemCaption(element[0].toString(),
					element[1].toString()+" - "+element[2].toString());
		}
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
				tbAmtRequestQty.get(ar).focus();
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
			String sql = "select U.vUnitName, C.vCategoryName, I.vUnitId, I.vCategoryId, (select dbo.funcStockQty"+
					" (I.vItemId ,'"+sessionBean.getBranchId()+"')) mStockQty from master.tbRawItemInfo I inner join"+
					" master.tbUnitInfo U on I.vUnitId = U.iUnitId inner join master.tbItemCategory C on I.vCategoryId"+
					" = C.vCategoryId where I.vItemId = '"+ItemId+"'";
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				tbLblUnitName.get(ar).setValue(element[0].toString());
				tbLblCatName.get(ar).setValue(element[1].toString());
				tbLblUnitId.get(ar).setValue(element[2].toString());
				tbLblCatId.get(ar).setValue(element[3].toString());

				tbAmtStockQty.get(ar).setReadOnly(false);
				tbAmtStockQty.get(ar).setValue(cm.deciMn.format(Double.parseDouble(element[4].toString())));
				tbAmtStockQty.get(ar).setReadOnly(true);
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
					{
						ret = true;
						break;
					}
				}
			}
		}
		return ret;
	}

	private void removeRow(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null && tbCmbItemName.size() > 0)
		{
			tbCmbItemName.get(ar).setValue(null);
			tblRequisitionList.removeItem(ar);
			tbCmbItemName.get(ar).clear();
		}
	}

	private void addChanges()
	{
		if (flag.equals("Edit") && action)
		{ changes = true; }
	}

	private void focusEnter()
	{
		allComp.add(cmbRequestedBranch);
		allComp.add(txtReferenceNo);
		allComp.add(txtRemarks);
		allComp.add(tbCmbItemName.get(0));
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}

	private void loadRequisitionNo()
	{
		if (!flag.equals("Edit"))
		{
			txtRequisitionNo.setReadOnly(false);
			txtRequisitionNo.setValue(rig.getRequisitionNo(cm.dfDb.format(txtRequisitionDate.getValue())));
			txtRequisitionNo.setReadOnly(true);
		}
	}

	private void loadSupplier()
	{
		cmbRequestedBranch.removeAllItems();
		String sqls = "select vBranchId, vBranchName, iBranchTypeId from master.tbBranchMaster where"+
				" iActive = 1 and vBranchId != '"+sessionBean.getBranchId()+"' order by vBranchName";
		for (Iterator<?> iter = cm.selectSql(sqls).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbRequestedBranch.addItem(element[0].toString());
			cmbRequestedBranch.setItemCaption(element[0].toString(), element[1].toString());
			hmBranch.put(element[0].toString(), element[2].toString());
		}
	}

	private void txtClear()
	{
		txtRequisitionNo.setReadOnly(false);
		txtRequisitionNo.setValue(rig.getRequisitionNo(cm.dfDb.format(txtRequisitionDate.getValue())));
		txtRequisitionNo.setReadOnly(true);

		txtRemarks.setValue("");
		txtReferenceNo.setValue("");
		txtDeliveryDate.setValue(new Date());
		txtRequisitionDate.setValue(new Date());
		tableClear();
	}

	private void tableClear()
	{
		tblRequisitionList.removeAllItems();
		tbCmbItemName.clear();
		tableRowAdd(0);
	}
}
