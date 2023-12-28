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
import com.example.gateway.PurchaseOrderGateway;
import com.example.model.PurchaseOrderInfoModel;
import com.example.possetup.AddEditRawItemInfo;
import com.example.possetup.AddEditSupplierInfo;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditPurchaseOrder extends Window
{
	private SessionBean sessionBean;
	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private String flag, orderId;

	private TextField txtOrderNo, txtRemarks, txtReferenceNo;
	private ComboBox cmbSupplierName, cmbRequestedFrom, cmbRequisition;;
	private CommaField txtDiscount;
	private Button btnAddSupplier;
	private PopupDateField txtOrderDate, txtDeliveryDate;

	private Table tblOrderDetailsList;
	private ArrayList<ComboBox> tbCmbItemName = new ArrayList<ComboBox>();
	private ArrayList<Label> tbLblUnitName = new ArrayList<Label>();
	private ArrayList<Label> tbLblCatName = new ArrayList<Label>();
	private ArrayList<ComboBox> tbCmbVatCatId = new ArrayList<ComboBox>();
	private ArrayList<OptionGroup> tbOgVatOption = new ArrayList<OptionGroup>();
	private ArrayList<TextField> tbTxtDescription = new ArrayList<TextField>();
	private ArrayList<CommaField> tbAmtStockQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtReqQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtPendingQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtOrdQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtPurchaseRate = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtTotalAmount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtDiscount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtVatAmount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtNetAmount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtMainPurchaseRate = new ArrayList<CommaField>();
	private ArrayList<Label> tbLblUnitId  = new ArrayList<Label>();
	private ArrayList<Label> tbLblVatCatId  = new ArrayList<Label>();
	private ArrayList<CommaField> tbAmtCustomCost = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtFreightCost  = new ArrayList<CommaField>();
	private ArrayList<Button> tbBtnAddItem  = new ArrayList<Button>();
	private ArrayList<Button> tbBtnRemove  = new ArrayList<Button>();

	private ArrayList<Component> allComp = new ArrayList<Component>();
	private HashMap<String, Double> hmVat = new HashMap<String, Double>();
	private HashMap<String, Double> hmSupDisc = new HashMap<String, Double>();
	private PurchaseOrderGateway pog = new PurchaseOrderGateway();
	private CommonMethod cm;
	private boolean changes = false, action = false;

	public AddEditPurchaseOrder(SessionBean sessionBean, String flag, String orderId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.orderId = orderId;
		this.setCaption(flag+" Purchase Order Information :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(this.sessionBean);
		setWidth("100%");
		setHeight("610px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
		{ masterValidation(); });

		cBtn.btnExit.addClickListener(event ->
		{ close(); });

		btnAddSupplier.addClickListener(event ->
		{ addEditSupplier(); });

		txtOrderDate.addValueChangeListener(event ->
		{ loadOrderNo(); });

		cmbSupplierName.addValueChangeListener(event ->
		{ setSupplierDiscount(); });

		txtDiscount.addValueChangeListener(event ->
		{ discountAction(); });

		cmbRequestedFrom.addValueChangeListener(event ->
		{ loadRequisitionData(); });

		cmbRequisition.addValueChangeListener(event ->
		{ setRequisitionDetails(); });

		loadBranchData();
		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void loadOrderNo()
	{
		if (!flag.equals("Edit"))
		{
			txtOrderNo.setReadOnly(false);
			txtOrderNo.setValue(pog.getOrderNo(cm.dfDb.format(txtOrderDate.getValue())));
			txtOrderNo.setReadOnly(true);
		}
	}

	private void loadRequisitionData()
	{
		String reqBrnId = cm.getComboValue(cmbRequestedFrom);
		String branchId = sessionBean.getBranchId();
		String sql = "select vRequisitionId, vRequisitionNo+' ('+CONVERT(varchar(20), dRequisitionDate)+')' vReqDetails from"+
				" (select rei.vRequisitionId, rei.vRequisitionNo, rei.dRequisitionDate, sum(red.mQuantity) mReqQty, isnull((select"+
				" sum(pod.mQuantity) from trans.tbPurchaseOrderDetails pod where pod.vRequisitionId = rei.vRequisitionId and pod.iActive = 1"+
				" and pod.vOrderId != '"+orderId+"'), 0) mOrdQty from trans.tbRequisitionInfo rei, trans.tbRequisitionDetails red where"+
				" rei.vRequisitionId = red.vRequisitionId and rei.iActive = 1 and rei.vBranchId = '"+reqBrnId+"' and rei.vReqBranchId ="+
				" '"+branchId+"' and rei.vStatusId = 'S6' group by rei.vRequisitionId, rei.vRequisitionNo, rei.dRequisitionDate) temp"+
				" order by dRequisitionDate asc";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbRequisition.addItem(element[0].toString());
			cmbRequisition.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadBranchData()
	{
		String sql = "select vBranchId, vBranchName from master.tbBranchMaster where iActive = 1 and iBranchTypeId = 3 and"+
				" vBranchId != '"+sessionBean.getBranchId()+"' order by vBranchName";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbRequestedFrom.addItem(element[0].toString());
			cmbRequestedFrom.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void setRequisitionDetails()
	{
		String reqId = cmbRequisition.getValue() != null? cmbRequisition.getValue().toString():"";
		if (!reqId.isEmpty())
		{
			tblOrderDetailsList.setColumnCollapsed("Pen. Qty.", false);
			tblOrderDetailsList.setColumnCollapsed("Req. Qty", false);
		}
		else
		{
			tblOrderDetailsList.setColumnCollapsed("Pen. Qty.", true);
			tblOrderDetailsList.setColumnCollapsed("Req. Qty", true);
		}
		tableClear();
		String sql = "select vItemId, mQuantity, mBalance from (select vItemId, mQuantity, (select (select isnull(sum(mQuantity)"+
				" , 0) from trans.tbRequisitionDetails where vRequisitionId = '"+reqId+"' and vItemId = req.vItemId) -"+
				" isnull(sum(pod.mQuantity), 0) from trans.tbPurchaseOrderDetails pod where pod.vRequisitionId = '"+reqId+"'"+
				" and pod.vOrderId != '"+orderId+"' and pod.vItemId = req.vItemId) mBalance from trans.tbRequisitionDetails"+
				" req where vRequisitionId = '"+reqId+"') as tbTemp where mBalance > 0";
		//System.out.println(sql);
		int ar = 0;
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (tbCmbItemName.size() <= ar)
			{ tableRowAdd(tbCmbItemName.size()); }

			tbCmbItemName.get(ar).setValue(element[0].toString());
			tbAmtReqQty.get(ar).setReadOnly(false);
			tbAmtReqQty.get(ar).setValue(Double.parseDouble(element[1].toString()));
			tbAmtReqQty.get(ar).setReadOnly(true);

			tbAmtPendingQty.get(ar).setReadOnly(false);
			tbAmtPendingQty.get(ar).setValue(Double.parseDouble(element[2].toString()));
			tbAmtPendingQty.get(ar).setReadOnly(true);
			ar++;
		}
		action = true;
	}

	//Method For Master data
	private void discountAction()
	{
		if (cm.getAmtValue(txtDiscount) > 0)
		{
			for (int i=0; i<tbCmbItemName.size(); i++)
			{
				if (cm.getAmtValue(txtDiscount) <= totalAmount("Total"))
				{
					if (tbCmbItemName.get(i).getValue() != null)
					{ calculateAmtChangeData(i); }
				}
			}
		}
		else
		{
			for (int i=0; i<tbCmbItemName.size(); i++)
			{ tbTxtDiscount.get(i).setValue("0"); }
			totalAmount("Total");
		}
	}

	private void setSupplierDiscount()
	{
		String supplierId = cmbSupplierName.getValue() != null? cmbSupplierName.getValue().toString():"";
		if (!supplierId.isEmpty()) { txtDiscount.setValue(hmSupDisc.get(supplierId)); }
		else { txtDiscount.setValue("0");}
	}

	private void masterValidation()
	{
		if (cmbSupplierName.getValue() != null)
		{
			if (!txtOrderNo.getValue().toString().trim().isEmpty())
			{
				if (!txtDiscount.getValue().toString().trim().isEmpty())
				{
					if (txtOrderDate.getValue() != null)
					{
						if (txtDeliveryDate.getValue() != null)
						{
							if (totalAmount("Total") > 0)
							{
								if (cm.getAmtValue(txtDiscount) <= totalAmount("Total"))
								{
									cBtn.btnSave.setEnabled(false);
									insertEditData();
								}
								else
								{ cm.showNotification("warning", "Warning!", "Discount limit exceeded."); }
							}
							else
							{
								tbCmbItemName.get(0).focus();
								cm.showNotification("warning", "Warning!", "Provide valid data in table.");
							}
						}
						else
						{
							txtDeliveryDate.focus();
							cm.showNotification("warning", "Warning!", "Provide delivery date.");
						}
					}
					else
					{
						txtOrderDate.focus();
						cm.showNotification("warning", "Warning!", "Provide order date.");
					}
				}
				else
				{
					txtDiscount.focus();
					cm.showNotification("warning", "Warning!", "Provide discount amount.");
				}
			}
			else
			{
				txtOrderNo.focus();
				cm.showNotification("warning", "Warning!", "Order number not found.");
			}
		}
		else
		{
			cmbSupplierName.focus();
			cm.showNotification("warning", "Warning!", "Select supplier name.");
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
		PurchaseOrderInfoModel pom = new PurchaseOrderInfoModel();
		getValue(pom);
		if (pog.insertEditData(pom, flag))
		{
			txtClear();
			cm.showNotification("success", "Successfull!", "All information saved successfully.");
			cBtn.btnSave.setEnabled(true);

			PurchaseOrderInfo report = new PurchaseOrderInfo(sessionBean, "");
			report.viewReport(pom.getOrderId());

			if (flag.equals("Edit"))
			{ close(); }
		}
		else
		{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
	}

	private void getValue(PurchaseOrderInfoModel pom)
	{
		pom.setBranchId(sessionBean.getBranchId());
		pom.setOrderId(flag.equals("Add")? pog.getOrderId(sessionBean.getBranchId()):orderId);
		pom.setOrderNo(flag.equals("Add")? pog.getOrderNo(cm.dfDb.format(txtOrderDate.getValue())).toString():
			txtOrderNo.getValue().toString());
		pom.setOrderType("Purchase");
		pom.setRevisionId("");
		pom.setSupplierId((cmbSupplierName.getValue() == null)? "":cmbSupplierName.getValue().toString());
		pom.setOrderDate(txtOrderDate.getValue());
		pom.setDeliveryDate(txtDeliveryDate.getValue());
		pom.setRemarks(txtRemarks.getValue().toString().trim());
		pom.setReferenceNo(txtReferenceNo.getValue().toString());
		pom.setCreatedBy(sessionBean.getUserId());
		pom.setStatusId("S5");
		pom.setApproveBy("");
		pom.setCancelBy("");
		pom.setCancelReason("");
		pom.setRequisitionId((cmbRequisition.getValue() == null)? "":cmbRequisition.getValue().toString());
		pom.setReqBranchId((cmbRequestedFrom.getValue() == null)? "":cmbRequestedFrom.getValue().toString());
		pom.setDetailsSql(getDetailsSql(pom.getOrderId(), pom.getRequisitionId()));
		pom.setDetailsChange(changes);
	}

	private String getDetailsSql(String orderId, String requisId)
	{
		String sql = flag.equals("Add")? "":"delete trans.tbPurchaseOrderDetails where vOrderId = '"+orderId+"' ";
		for (int i = 0; i < tbCmbItemName.size(); i++)
		{
			if (i == 0)
			{
				sql += "insert into trans.tbPurchaseOrderDetails(vOrderId, vItemId, vDescription, vUnitId,"+
						" vVatCatId, vVatOption, mVatPercent, mQuantity, mUnitRate, mDiscount, mAmount,"+
						" mVatAmount, mNetAmount, iActive, vRequisitionId) values ";
			}
			if (tbCmbItemName.get(i).getValue() != null && cm.getAmtValue(tbTxtTotalAmount.get(i)) > 0)
			{
				sql += "('"+orderId+"', '"+tbCmbItemName.get(i).getValue().toString()+"',"+
						" '"+tbTxtDescription.get(i).getValue().toString()+"',"+
						" '"+tbLblUnitId.get(i).getValue().toString()+"',"+
						" '"+tbCmbVatCatId.get(i).getValue().toString()+"',"+
						" '"+tbOgVatOption.get(i).getValue().toString()+"',"+
						" '"+hmVat.get(tbCmbVatCatId.get(i).getValue().toString())+"',"+
						" '"+cm.getAmtValue(tbTxtOrdQty.get(i))+"',"+
						" '"+cm.getAmtValue(tbTxtPurchaseRate.get(i))+"',"+
						" '"+cm.getAmtValue(tbTxtDiscount.get(i))+"',"+
						" '"+cm.getAmtValue(tbTxtTotalAmount.get(i))+"',"+
						" '"+cm.getAmtValue(tbTxtVatAmount.get(i))+"',"+
						" '"+cm.getAmtValue(tbTxtNetAmount.get(i))+"', 1, '"+requisId+"'),";
			}
		}
		return sql.substring(0, sql.length()-1);
	}

	private void setEditData()
	{
		PurchaseOrderInfoModel pom = new PurchaseOrderInfoModel();
		try
		{
			if (pog.selectEditData(pom, orderId))
			{
				txtOrderDate.setValue((Date) pom.getOrderDate());
				cmbSupplierName.setValue(pom.getSupplierId());
				txtDeliveryDate.setValue((Date) pom.getDeliveryDate());
				txtRemarks.setValue(pom.getRemarks());
				txtReferenceNo.setValue(pom.getReferenceNo());

				txtOrderNo.setReadOnly(false);
				txtOrderNo.setValue(pom.getOrderNo());
				txtOrderNo.setReadOnly(true);

				cmbRequestedFrom.setValue(pom.getReqBranchId());
				cmbRequisition.setValue(pom.getRequisitionId());

				cmbRequisition.setEnabled(pom.getReqBranchId().isEmpty());
				cmbRequestedFrom.setEnabled(pom.getRequisitionId().isEmpty());

				setTableValue(pom.getRequisitionId());
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private void setTableValue(String reqId)
	{
		int ar = 0;
		String sql = "select pod.vItemId, pod.vUnitId, uni.vUnitName, pod.vVatCatId, pod.vVatOption, pod.vDescription,"+
				" pod.mQuantity, pod.mUnitRate, pod.mDiscount from trans.tbPurchaseOrderDetails pod, master.tbUnitInfo"+
				" uni where vOrderId = '"+orderId+"' and pod.vUnitId = convert(varchar(10), iUnitId)";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			tbLblUnitId.get(ar).setValue(element[1].toString());
			tbLblUnitName.get(ar).setValue(element[2].toString());
			tbCmbVatCatId.get(ar).setValue(element[3].toString());
			tbOgVatOption.get(ar).setValue(element[4].toString());
			tbTxtDescription.get(ar).setValue(element[5].toString());

			if (reqId.isEmpty())
			{
				tbCmbItemName.get(ar).setValue(element[0].toString());
				tbTxtOrdQty.get(ar).setValue(Double.parseDouble(element[6].toString()));
				tbTxtPurchaseRate.get(ar).setValue(Double.parseDouble(element[7].toString()));
				tbTxtDiscount.get(ar).setValue(Double.parseDouble(element[8].toString()));
			}
			else
			{ setTableEditQty(element[0].toString(), Double.parseDouble(element[6].toString()),
					Double.parseDouble(element[7].toString()), Double.parseDouble(element[8].toString())); }

			//totalAmount("Total");
			ar++;
		}
		action = true;
	}

	private void setTableEditQty(String itemIdEdit, double qty, double purRate, double disCount)
	{
		for (int ar = 0; ar < tbCmbItemName.size(); ar++)
		{
			String itemId = tbCmbItemName.get(ar).getValue() != null? tbCmbItemName.get(ar).getValue().toString():"";
			if (itemId.equals(itemIdEdit))
			{
				tbTxtOrdQty.get(ar).setValue(qty);
				tbTxtPurchaseRate.get(ar).setValue(purRate);
				tbTxtDiscount.get(ar).setValue(disCount);
				break;
			}
		}
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(8, 4);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		CssLayout groupO = new CssLayout();
		groupO.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		txtOrderNo = new TextField();
		txtOrderNo.setImmediate(true);
		txtOrderNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtOrderNo.setWidth("120px");
		txtOrderNo.setInputPrompt("Order No");
		txtOrderNo.setDescription("Order No");
		txtOrderNo.setRequired(true);
		txtOrderNo.setRequiredError("This field is required.");
		grid.addComponent(new Label("Order Details: "), 0, 0);

		txtOrderDate  = new PopupDateField();
		txtOrderDate.setImmediate(true);
		txtOrderDate.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtOrderDate.setValue(new Date());
		txtOrderDate.setWidth("110px");
		txtOrderDate.setDateFormat("dd-MM-yyyy");
		txtOrderDate.setDescription("Order Date");
		txtOrderDate.setRequired(true);
		txtOrderDate.setRequiredError("This field is required");

		groupO.addComponents(txtOrderNo, txtOrderDate);
		grid.addComponent(groupO, 1, 0);

		CssLayout groups = new CssLayout();
		groups.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		cm.setAuthorize(sessionBean.getUserId(), "suppInfo");

		cmbSupplierName = new ComboBox();
		cmbSupplierName.setImmediate(true);
		cmbSupplierName.setWidth("200px");
		cmbSupplierName.setInputPrompt("Select Supplier Name");
		cmbSupplierName.setDescription("Select supplier name");
		cmbSupplierName.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbSupplierName.setFilteringMode(FilteringMode.CONTAINS);
		cmbSupplierName.setRequired(true);
		cmbSupplierName.setRequiredError("This field is required.");

		btnAddSupplier = new Button();
		btnAddSupplier.setIcon(FontAwesome.PLUS);
		btnAddSupplier.setStyleName(ValoTheme.BUTTON_TINY);
		btnAddSupplier.setDescription("Add Supplier");
		btnAddSupplier.setEnabled(cm.insert);

		Label lbls = new Label("Supplier Name: ");
		lbls.setWidth("-1px");
		grid.addComponent(lbls, 0, 1);

		groups.addComponents(cmbSupplierName, btnAddSupplier);
		grid.addComponent(groups, 1, 1);

		cmbRequestedFrom = new ComboBox();
		cmbRequestedFrom.setImmediate(true);
		cmbRequestedFrom.setWidth("200px");
		cmbRequestedFrom.setInputPrompt("Select Branch");
		cmbRequestedFrom.setDescription("Select Branch");
		cmbRequestedFrom.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbRequestedFrom.setFilteringMode(FilteringMode.CONTAINS);
		Label lblBranchTo = new Label("Requested From: ");
		lblBranchTo.setWidth("-1px");
		grid.addComponent(lblBranchTo, 2, 0);
		grid.addComponent(cmbRequestedFrom, 3, 0); 

		cmbRequisition = new ComboBox();
		cmbRequisition.setImmediate(true);
		cmbRequisition.setWidth("200px");
		cmbRequisition.setInputPrompt("Select Requisition No");
		cmbRequisition.setDescription("Select Requisition No");
		cmbRequisition.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbRequisition.setFilteringMode(FilteringMode.CONTAINS);
		grid.addComponent(new Label("Requisition No: "), 2, 1);
		grid.addComponent(cmbRequisition, 3, 1);

		txtDeliveryDate  = new PopupDateField();
		txtDeliveryDate.setImmediate(true);
		txtDeliveryDate.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDeliveryDate.setValue(new Date());
		txtDeliveryDate.setWidth("110px");
		txtDeliveryDate.setDateFormat("dd-MM-yyyy");
		txtDeliveryDate.setRequiredError("This field is required");
		Label lblDvd = new Label("Delivery Date: ");
		lblDvd.setWidth("-1px");
		grid.addComponent(lblDvd, 4, 0);
		grid.addComponent(txtDeliveryDate, 5, 0);

		txtDiscount = new CommaField();
		txtDiscount.setImmediate(true);
		txtDiscount.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDiscount.setWidth("110px");
		txtDiscount.setValue("0");
		txtDiscount.setEnabled(false);
		grid.addComponent(new Label("Discount: "), 4, 1);
		grid.addComponent(txtDiscount, 5, 1);

		txtReferenceNo = new TextField();
		txtReferenceNo.setImmediate(true);
		txtReferenceNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReferenceNo.setWidth("120px");
		txtReferenceNo.setInputPrompt("Reference No");
		Label lblRef = new Label("Reference No: ");
		lblRef.setWidth("-1px");
		grid.addComponent(lblRef, 6, 0);
		grid.addComponent(txtReferenceNo, 7, 0);

		txtRemarks = new TextField();
		txtRemarks.setImmediate(true);
		txtRemarks.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRemarks.setWidth("120px");
		txtRemarks.setInputPrompt("Remarks");
		grid.addComponent(new Label("Remarks: "), 6, 1);
		grid.addComponent(txtRemarks, 7, 1);

		grid.addComponent(buildTable(), 0, 2, 7, 2);

		grid.addComponent(cBtn, 0, 3, 7, 3);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		loadSupplier();
		loadOrderNo();

		return grid;
	}

	private void addEditSupplier()
	{
		String supplierId = cmbSupplierName.getValue() != null? cmbSupplierName.getValue().toString():"";
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

	private void loadSupplier()
	{
		String sqls = "select vSupplierId, vSupplierCode, vSupplierName, dbo.funGetNumeric(vSupplierCode)"+
				" iCode, mDiscount from master.tbSupplierMaster where iActive = 1 order by iCode asc";
		for (Iterator<?> iter = cm.selectSql(sqls).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupplierName.addItem(element[0].toString());
			cmbSupplierName.setItemCaption(element[0].toString(), element[1].toString()+" - "+element[2].toString());
			hmSupDisc.put(element[0].toString(), Double.parseDouble(element[4].toString()));
		}
	}

	private Table buildTable()
	{
		tblOrderDetailsList = new Table();
		tblOrderDetailsList.setSelectable(true);
		tblOrderDetailsList.setFooterVisible(true);
		tblOrderDetailsList.setColumnCollapsingAllowed(true);
		tblOrderDetailsList.addStyleName(ValoTheme.TABLE_SMALL);
		tblOrderDetailsList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblOrderDetailsList.setPageLength(6);
		tblOrderDetailsList.setWidth("100%");

		tblOrderDetailsList.addContainerProperty("Add", Button.class, new Button(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnWidth("Add", 50);

		tblOrderDetailsList.addContainerProperty("Item Name", ComboBox.class, new ComboBox(), null, null, Align.CENTER);

		tblOrderDetailsList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);

		tblOrderDetailsList.addContainerProperty("Category", Label.class, new Label(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnCollapsed("Category", true);

		tblOrderDetailsList.addContainerProperty("Description", TextField.class, new TextField(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnCollapsed("Description", true);

		tblOrderDetailsList.addContainerProperty("VAT Cat", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnWidth("VAT Cat", 130);

		tblOrderDetailsList.addContainerProperty("VAT Option", OptionGroup.class, new OptionGroup(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnWidth("VAT Option", 85);

		tblOrderDetailsList.addContainerProperty("Cur. Stk.", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnWidth("Cur. Stk.", 60);

		tblOrderDetailsList.addContainerProperty("Req. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnWidth("Req. Qty", 60);
		tblOrderDetailsList.setColumnCollapsed("Req. Qty", true);

		tblOrderDetailsList.addContainerProperty("Pen. Qty.", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnWidth("Pen. Qty.", 60);
		tblOrderDetailsList.setColumnCollapsed("Pen. Qty.", true);

		tblOrderDetailsList.addContainerProperty("Ord. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnWidth("Ord. Qty", 60);

		tblOrderDetailsList.addContainerProperty("Rate", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblOrderDetailsList.setColumnWidth("Rate", 70);

		tblOrderDetailsList.addContainerProperty("Total Amount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblOrderDetailsList.setColumnWidth("Total Amount", 100);

		tblOrderDetailsList.addContainerProperty("Discount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblOrderDetailsList.setColumnWidth("Discount", 80);

		tblOrderDetailsList.addContainerProperty("Vat", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblOrderDetailsList.setColumnWidth("Vat", 80);

		tblOrderDetailsList.addContainerProperty("Net Amount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblOrderDetailsList.setColumnWidth("Net Amount", 120);

		tblOrderDetailsList.addContainerProperty("P. Rate", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblOrderDetailsList.setColumnCollapsed("P. Rate", true);

		tblOrderDetailsList.addContainerProperty("Unit ID", Label.class, new Label(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnCollapsed("Unit ID", true);

		tblOrderDetailsList.addContainerProperty("Category ID", Label.class, new Label(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnCollapsed("Category ID", true);

		tblOrderDetailsList.addContainerProperty("Freight Cost", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnCollapsed("Freight Cost", true);

		tblOrderDetailsList.addContainerProperty("Custom Cost", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnCollapsed("Custom Cost", true);

		tblOrderDetailsList.addContainerProperty("Rem", Button.class, new Button(), null, null, Align.CENTER);
		tblOrderDetailsList.setColumnWidth("Rem", 50);

		tableRowAdd(0);

		return tblOrderDetailsList;
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
			tbCmbItemName.get(ar).setInputPrompt("Select Item Name");
			tbCmbItemName.get(ar).setEnabled(cmbRequisition.getValue()==null);
			tbCmbItemName.get(ar).addValueChangeListener(event ->
			{ itemActions(ar); });

			tbLblUnitName.add(ar, new Label());
			tbLblUnitName.get(ar).setWidth("100%");
			tbLblUnitName.get(ar).setImmediate(true);
			tbLblUnitName.get(ar).setStyleName(ValoTheme.LABEL_TINY);
			tbLblUnitName.get(ar).setDescription("Unit Name");

			tbLblCatName.add(ar, new Label());
			tbLblCatName.get(ar).setWidth("100%");
			tbLblCatName.get(ar).setImmediate(true);
			tbLblCatName.get(ar).setStyleName(ValoTheme.LABEL_TINY);
			tbLblCatName.get(ar).setDescription("Category Name");

			tbTxtDescription.add(ar, new TextField());
			tbTxtDescription.get(ar).setWidth("100%");
			tbTxtDescription.get(ar).setImmediate(true);
			tbTxtDescription.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtDescription.get(ar).setDescription("Description");

			tbCmbVatCatId.add(ar, new ComboBox());
			tbCmbVatCatId.get(ar).setWidth("100%");
			tbCmbVatCatId.get(ar).setImmediate(true);
			tbCmbVatCatId.get(ar).setNullSelectionAllowed(false);
			tbCmbVatCatId.get(ar).setFilteringMode(FilteringMode.CONTAINS);
			tbCmbVatCatId.get(ar).addStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbVatCatId.get(ar).setInputPrompt("Select VAT Category");
			tbCmbVatCatId.get(ar).setRequired(true);
			tbCmbVatCatId.get(ar).setRequiredError("This field is required");
			loadTableComboData(ar);
			tbCmbVatCatId.get(ar).addValueChangeListener(event ->
			{ calculateVatDisChange(ar); });

			tbOgVatOption.add(ar, new OptionGroup());
			tbOgVatOption.get(ar).setWidth("100%");
			tbOgVatOption.get(ar).setImmediate(true);
			tbOgVatOption.get(ar).addItem("Inclusive");
			tbOgVatOption.get(ar).addItem("Exclusive");
			tbOgVatOption.get(ar).select("Inclusive");
			tbOgVatOption.get(ar).setRequired(true);
			tbOgVatOption.get(ar).setRequiredError("This field is required.");
			tbOgVatOption.get(ar).addStyleName(ValoTheme.OPTIONGROUP_SMALL);
			tbOgVatOption.get(ar).addValueChangeListener(event ->
			{ calculateVatDisChange(ar); });

			tbAmtStockQty.add(ar, new CommaField());
			tbAmtStockQty.get(ar).setWidth("100%");
			tbAmtStockQty.get(ar).setImmediate(true);
			tbAmtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtStockQty.get(ar).setDescription("Current Stock");
			tbAmtStockQty.get(ar).setInputPrompt("Current Stock");
			tbAmtStockQty.get(ar).setReadOnly(true);

			tbAmtReqQty.add(ar, new CommaField());
			tbAmtReqQty.get(ar).setWidth("100%");
			tbAmtReqQty.get(ar).setImmediate(true);
			tbAmtReqQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtReqQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtReqQty.get(ar).setDescription("Requisition Qty");
			tbAmtReqQty.get(ar).setInputPrompt("Requisition Qty");
			tbAmtReqQty.get(ar).setReadOnly(true);

			tbAmtPendingQty.add(ar, new CommaField());
			tbAmtPendingQty.get(ar).setWidth("100%");
			tbAmtPendingQty.get(ar).setImmediate(true);
			tbAmtPendingQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtPendingQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtPendingQty.get(ar).setDescription("Pending Qty");
			tbAmtPendingQty.get(ar).setInputPrompt("Pending Qty");
			tbAmtPendingQty.get(ar).setReadOnly(true);

			tbTxtOrdQty.add(ar, new CommaField());
			tbTxtOrdQty.get(ar).setWidth("100%");
			tbTxtOrdQty.get(ar).setImmediate(true);
			tbTxtOrdQty.get(ar).setRequired(true);
			tbTxtOrdQty.get(ar).setRequiredError("This field is required.");
			tbTxtOrdQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtOrdQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbTxtOrdQty.get(ar).setDescription("Order Qty");
			tbTxtOrdQty.get(ar).setInputPrompt("Order Qty");
			tbTxtOrdQty.get(ar).addValueChangeListener(event ->
			{
				if (checkQty(ar))
				{
					calculateVatDisChange(ar);
					if (cmbRequisition.getValue() == null)
					{
						if ((ar+1) == tbCmbItemName.size())
						{ tableRowAdd(tbCmbItemName.size()); }
					}
					tbTxtPurchaseRate.get(ar).focus();
				}
			});

			tbTxtPurchaseRate.add(ar, new CommaField());
			tbTxtPurchaseRate.get(ar).setWidth("100%");
			tbTxtPurchaseRate.get(ar).setImmediate(true);
			tbTxtPurchaseRate.get(ar).setRequired(true);
			tbTxtPurchaseRate.get(ar).setRequiredError("This field is required.");
			tbTxtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbTxtPurchaseRate.get(ar).setInputPrompt("Purchase Rate/Issue Rate");
			tbTxtPurchaseRate.get(ar).setDescription("Purchase Rate/Issue Rate");
			tbTxtPurchaseRate.get(ar).setEnabled(false);
			tbTxtPurchaseRate.get(ar).addValueChangeListener(event ->
			{
				calculateVatDisChange(ar);
				if (cmbRequisition.getValue() == null)
				{
					if ((ar+1) == tbCmbItemName.size())
					{ tableRowAdd(tbCmbItemName.size()); }
					tbCmbItemName.get(ar+1).focus();
				}
			});

			tbTxtTotalAmount.add(ar, new CommaField());
			tbTxtTotalAmount.get(ar).setWidth("100%");
			tbTxtTotalAmount.get(ar).setImmediate(true);
			tbTxtTotalAmount.get(ar).setReadOnly(true);
			tbTxtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbTxtTotalAmount.get(ar).setInputPrompt("Total Amount");
			tbTxtTotalAmount.get(ar).setDescription("Total Amount");
			tbTxtTotalAmount.get(ar).addValueChangeListener(event ->
			{
				calculateAmtChangeData(ar);
				DiscountCheck(ar);
			});

			tbTxtDiscount.add(ar, new CommaField());
			tbTxtDiscount.get(ar).setWidth("100%");
			tbTxtDiscount.get(ar).setImmediate(true);
			tbTxtDiscount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtDiscount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			//tbTxtDiscount.get(ar).setEnabled(false);
			tbTxtDiscount.get(ar).setInputPrompt("Discount");
			tbTxtDiscount.get(ar).setDescription("Discount");
			tbTxtDiscount.get(ar).addValueChangeListener(event ->
			{ DiscountCheck(ar); });

			tbTxtVatAmount.add(ar, new CommaField());
			tbTxtVatAmount.get(ar).setWidth("100%");
			tbTxtVatAmount.get(ar).setImmediate(true);
			tbTxtVatAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtVatAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbTxtVatAmount.get(ar).setInputPrompt("VAT");
			tbTxtVatAmount.get(ar).setDescription("VAT");

			tbTxtNetAmount.add(ar, new CommaField());
			tbTxtNetAmount.get(ar).setWidth("100%");
			tbTxtNetAmount.get(ar).setImmediate(true);
			tbTxtNetAmount.get(ar).setReadOnly(true);
			tbTxtNetAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtNetAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbTxtNetAmount.get(ar).setInputPrompt("Net Amount");
			tbTxtNetAmount.get(ar).setDescription("Net Amount");

			tbAmtMainPurchaseRate.add(ar, new CommaField());
			tbAmtMainPurchaseRate.get(ar).setWidth("100%");
			tbAmtMainPurchaseRate.get(ar).setImmediate(true);
			tbAmtMainPurchaseRate.get(ar).setReadOnly(true);
			tbAmtMainPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtMainPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtMainPurchaseRate.get(ar).setDescription("Purchase Rate");

			tbLblUnitId.add(ar, new Label());
			tbLblUnitId.get(ar).setWidth("100%");
			tbLblUnitId.get(ar).setImmediate(true);
			tbLblUnitId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblVatCatId.add(ar, new Label());
			tbLblVatCatId.get(ar).setWidth("100%");
			tbLblVatCatId.get(ar).setImmediate(true);
			tbLblVatCatId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbAmtCustomCost.add(ar, new CommaField());
			tbAmtCustomCost.get(ar).setWidth("100%");
			tbAmtCustomCost.get(ar).setImmediate(true);
			tbAmtCustomCost.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtCustomCost.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

			tbAmtFreightCost.add(ar, new CommaField());
			tbAmtFreightCost.get(ar).setWidth("100%");
			tbAmtFreightCost.get(ar).setImmediate(true);
			tbAmtFreightCost.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtFreightCost.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

			tbBtnRemove.add(ar, new Button(""));
			tbBtnRemove.get(ar).setWidth("100%");
			tbBtnRemove.get(ar).setImmediate(true);
			tbBtnRemove.get(ar).setIcon(FontAwesome.REMOVE);
			tbBtnRemove.get(ar).addStyleName(ValoTheme.BUTTON_TINY);
			tbBtnRemove.get(ar).addStyleName(ValoTheme.BUTTON_DANGER);
			tbBtnRemove.get(ar).setDescription("Remove");
			tbBtnRemove.get(ar).addClickListener(event ->
			{ removeRow(ar); });

			tblOrderDetailsList.addItem(new Object[]{tbBtnAddItem.get(ar), tbCmbItemName.get(ar), tbLblUnitName.get(ar),
					tbLblCatName.get(ar), tbTxtDescription.get(ar), tbCmbVatCatId.get(ar), tbOgVatOption.get(ar),
					tbAmtStockQty.get(ar), tbAmtReqQty.get(ar), tbAmtPendingQty.get(ar), tbTxtOrdQty.get(ar),
					tbTxtPurchaseRate.get(ar), tbTxtTotalAmount.get(ar), tbTxtDiscount.get(ar), tbTxtVatAmount.get(ar),
					tbTxtNetAmount.get(ar), tbAmtMainPurchaseRate.get(ar), tbLblUnitId.get(ar),
					tbLblVatCatId.get(ar), tbAmtFreightCost.get(ar), tbAmtCustomCost.get(ar), tbBtnRemove.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private boolean checkQty(int ar)
	{
		boolean ret = true;
		if (cmbRequisition.getValue() != null)
		{
			if (cm.getAmtValue(tbTxtOrdQty.get(ar)) > cm.getAmtValue(tbAmtPendingQty.get(ar)))
			{
				ret = false;
				tbTxtOrdQty.get(ar).setValue(cm.getAmtValue(tbAmtPendingQty.get(ar)));
				tbTxtOrdQty.get(ar).focus();
				cm.showNotification("warning", "Warning!", "Pending qty exceeded.");
			}
		}
		return ret;
	}

	private void itemActions(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null)
		{
			addChanges();
			String selectId = tbCmbItemName.get(ar).getValue().toString();
			if (!checkDuplicate(selectId, ar))
			{
				setItemDetails(ar, selectId);
				totalAmount("Total");
				tbTxtOrdQty.get(ar).focus();
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
					{ ret = true; break; }
				}
			}
		}
		return ret;
	}

	private void setItemDetails(int ar, String itemId)
	{
		try
		{
			String sql = "select uni.vUnitName, cat.vCategoryName, ri.vUnitId, ri.vCategoryId, (select dbo.funcStockQty"+
					" (ri.vItemId ,'"+sessionBean.getBranchId()+"')) mStockQty, ri.mIssueRate from master.tbRawItemInfo"+
					" ri inner join master.tbUnitInfo uni on ri.vUnitId = uni.iUnitId inner join master.tbItemCategory"+
					" cat on ri.vCategoryId = cat.vCategoryId where ri.vItemId = '"+itemId+"'";
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				tbLblUnitName.get(ar).setValue(element[0].toString());
				tbLblCatName.get(ar).setValue(element[1].toString());
				tbLblUnitId.get(ar).setValue(element[2].toString());
				tbLblVatCatId.get(ar).setValue(element[3].toString());
				tbAmtStockQty.get(ar).setReadOnly(false);
				tbAmtStockQty.get(ar).setValue(Double.parseDouble(element[4].toString()));
				tbAmtStockQty.get(ar).setReadOnly(true);

				tbTxtPurchaseRate.get(ar).setValue(Double.parseDouble(element[5].toString()));
			}
		}
		catch (Exception e)
		{ System.out.println(e); }
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

	private void loadTableComboData(int ar)
	{
		String sql = "select vItemId, vItemName, vItemCode, dbo.funGetNumeric(vItemCode) iCode"+
				" from master.tbRawItemInfo where iActive = 1 order by iCode asc";

		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tbCmbItemName.get(ar).addItem(element[0].toString());
			tbCmbItemName.get(ar).setItemCaption(element[0].toString(), element[2].toString()+" - "+element[1].toString());
		}

		//VAT Category
		String sqlV = "select vVatCatId, vVatCatName, mPercentage from master.tbVatCatMaster where iActive = 1";

		for (Iterator<?> iter = cm.selectSql(sqlV).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tbCmbVatCatId.get(ar).addItem(element[0].toString());
			tbCmbVatCatId.get(ar).setItemCaption(element[0].toString(), element[1].toString());
			hmVat.put(element[0].toString(), Double.parseDouble(element[2].toString()));
		}
		tbCmbVatCatId.get(ar).select("V1");
	}

	private void removeRow(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null && tbCmbItemName.size() > 0 &&
				!tbTxtTotalAmount.get(ar).getValue().toString().isEmpty())
		{
			tbCmbItemName.get(ar).setValue(null);
			tblOrderDetailsList.removeItem(ar);
			tbCmbItemName.get(ar).clear();
			tbTxtTotalAmount.get(ar).setReadOnly(false);
			tbTxtTotalAmount.get(ar).clear();
		}
		totalAmount("Total");
	}

	private void DiscountCheck(int ar)
	{
		if (cm.getAmtValue(tbTxtTotalAmount.get(ar)) >= cm.getAmtValue(tbTxtDiscount.get(ar)))
		{
			calculateVatDisChange(ar);
			addChanges();
		}
		else
		{
			tbTxtDiscount.get(ar).focus();
			tbTxtDiscount.get(ar).setValue("0");
			cm.showNotification("warning", "Warning!", "Discount limit exceeded.");	
		}
	}

	private double totalAmount(String flag)
	{
		double ret = 0, totalamt = 0, grandtotal = 0, totalDiscount = 0,  totalVat = 0;
		for (int i=0; i<tbCmbItemName.size(); i++)
		{
			if (tbCmbItemName.get(i).getValue() != null && cm.getAmtValue(tbTxtTotalAmount.get(i)) > 0)
			{
				totalamt += cm.getAmtValue(tbTxtTotalAmount.get(i));
				grandtotal += cm.getAmtValue(tbTxtNetAmount.get(i));
				totalVat += cm.getAmtValue(tbTxtVatAmount.get(i));
				totalDiscount += cm.getAmtValue(tbTxtDiscount.get(i));
			}
		}

		tblOrderDetailsList.setColumnFooter("Total Amount", cm.setComma(totalamt));
		tblOrderDetailsList.setColumnAlignment("Total Amount", Align.RIGHT);
		tblOrderDetailsList.setColumnFooter("Net Amount", cm.setComma(grandtotal));
		tblOrderDetailsList.setColumnAlignment("Net Amount", Align.RIGHT);
		tblOrderDetailsList.setColumnFooter("Vat", cm.setComma(totalVat));
		tblOrderDetailsList.setColumnAlignment("Vat", Align.RIGHT);
		tblOrderDetailsList.setColumnFooter("Discount", cm.setComma(totalDiscount));
		tblOrderDetailsList.setColumnAlignment("Discount", Align.RIGHT);

		if (flag.equals("Total")) { ret = totalamt; }
		if (flag.equals("Net")) { ret = grandtotal; }
		if (flag.equals("Discount")) { ret = totalDiscount; }
		return ret;
	}

	private void calculateVatDisChange(int ar)
	{
		double orderQty = cm.getAmtValue(tbTxtOrdQty.get(ar));
		double unitRate = cm.getAmtValue(tbTxtPurchaseRate.get(ar));
		String vatRule = tbOgVatOption.get(ar).getValue().toString();
		String vatCat = tbCmbVatCatId.get(ar).getValue() != null? tbCmbVatCatId.get(ar).getValue().toString():"";
		double vatPecent = hmVat.get(vatCat) != null? hmVat.get(vatCat):0;
		double actualRate = 0, vatAmount = 0;

		double discount = cm.getAmtValue(tbTxtDiscount.get(ar));

		double mainAmount = (orderQty * unitRate) - discount;

		if (vatRule.equals("Inclusive"))
		{
			vatAmount = cm.getRound((mainAmount * vatPecent) / (100 + vatPecent));
			actualRate = mainAmount - vatAmount;
		}
		else
		{
			vatAmount = cm.getRound((mainAmount * vatPecent) / 100);
			actualRate = mainAmount;
		}

		tbTxtTotalAmount.get(ar).setReadOnly(false);
		tbTxtTotalAmount.get(ar).setValue(orderQty * unitRate);
		tbTxtTotalAmount.get(ar).setReadOnly(true);

		tbTxtVatAmount.get(ar).setReadOnly(false);
		tbTxtVatAmount.get(ar).setValue(vatAmount);
		tbTxtVatAmount.get(ar).setReadOnly(true);

		tbTxtNetAmount.get(ar).setReadOnly(false);
		tbTxtNetAmount.get(ar).setValue((actualRate+vatAmount));
		tbTxtNetAmount.get(ar).setReadOnly(true);

		double purchaseRate = 0;
		if (orderQty > 0)
		{ purchaseRate = cm.getRound((actualRate+vatAmount) / orderQty); }

		tbAmtMainPurchaseRate.get(ar).setReadOnly(false);
		tbAmtMainPurchaseRate.get(ar).setValue(purchaseRate);
		tbAmtMainPurchaseRate.get(ar).setReadOnly(true);
		totalAmount("Total");
	}

	private void calculateAmtChangeData(int ar)
	{
		double TotalDiscount = cm.getAmtValue(txtDiscount);
		if (TotalDiscount > 0)
		{
			for (int i=0; i<tbCmbItemName.size(); i++)
			{
				if (tbCmbItemName.get(i).getValue() != null)
				{
					double TotalAmountCal = cm.getAmtValue(tbTxtTotalAmount.get(i));
					double TotalAmounttable = totalAmount("Total");
					double DiscountPerItem = 0.0;

					if (TotalAmounttable > 0)
					{ DiscountPerItem = cm.getRound((TotalAmountCal * TotalDiscount) / TotalAmounttable); }
					tbTxtDiscount.get(i).setValue(DiscountPerItem);
				}
			}
		}
	}

	private void addChanges()
	{
		if (flag.equals("Edit") && action)
		{ changes = true; }
	}

	private void txtClear()
	{
		txtOrderNo.setReadOnly(false);
		txtOrderNo.setValue(pog.getOrderNo(cm.dfDb.format(txtOrderDate.getValue())));
		txtOrderNo.setReadOnly(true);
		txtRemarks.setValue("");
		txtReferenceNo.setValue("");
		txtDiscount.setValue("0");
		txtDeliveryDate.setValue(new Date());
		txtOrderDate.setValue(new Date());
		cmbRequestedFrom.setValue(null);
		cmbRequisition.setValue(null);
		tblOrderDetailsList.setColumnCollapsed("Pen. Qty.", true);
		tableClear();
		totalAmount("");
	}

	private void tableClear()
	{
		tblOrderDetailsList.removeAllItems();
		tbCmbItemName.clear();
		tableRowAdd(0);
	}

	private void focusEnter()
	{
		allComp.add(cmbSupplierName);
		allComp.add(txtReferenceNo);
		allComp.add(txtRemarks);
		allComp.add(txtDiscount);
		allComp.add(tbCmbItemName.get(0));

		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}
}
