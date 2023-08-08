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
import com.example.gateway.PurchaseInfoGateway;
import com.example.model.PurchaseInfoModel;
import com.example.possetup.AddEditRawItemInfo;
import com.example.possetup.AddEditSupplierInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
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
public class AddEditPurchaseInfo extends Window
{
	private SessionBean sessionBean;
	private String flag, purchaseId;

	private boolean changes = false, action = false;
	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private PurchaseInfoGateway pig = new PurchaseInfoGateway();

	private ArrayList<Component> allComp = new ArrayList<Component>();
	private TextField txtPurchaseNo, txtRemarks, txtReferenceNo;
	private CommaField txtDiscount;
	private ComboBox cmbSupplierName, cmbOrderNo;
	private PopupDateField dPurchaseDate, dDeliveryDate;
	private Button btnAddSupplier;
	private OptionGroup ogPaymentType;

	private Table tblPurchaseDetailsList;
	private ArrayList<ComboBox> tbCmbItemName = new ArrayList<ComboBox>();
	private ArrayList<Label> tbLblUnitName = new ArrayList<Label>();
	private ArrayList<Label> tbLblCatName = new ArrayList<Label>();
	private ArrayList<ComboBox> tbCmbVatCatId = new ArrayList<ComboBox>();
	private ArrayList<OptionGroup> tbOgVatOption = new ArrayList<OptionGroup>();
	private ArrayList<TextField> tbTxtDescription = new ArrayList<TextField>();
	private ArrayList<CommaField> tbTxtStockQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtPendingQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtOrderQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtReceivedQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtPurchaseRate = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtTotalAmount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtDiscount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtVatAmount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtNetAmount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtMainPurchaseRate = new ArrayList<CommaField>();
	private ArrayList<Label> tbLblUnitId = new ArrayList<Label>();
	private ArrayList<Label> tbLblVatCatId = new ArrayList<Label>();
	private ArrayList<Label> tbLblDiscount = new ArrayList<Label>();
	private ArrayList<CommaField> tbTxtCustomCost = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbTxtFreightCost = new ArrayList<CommaField>();
	private ArrayList<Button> tbBtnAddItem = new ArrayList<Button>();
	private ArrayList<Button> tbBtnRemove  = new ArrayList<Button>();

	private HashMap<String, Double> hmSupDisc = new HashMap<String, Double>();
	private HashMap<String, Double> hmVat = new HashMap<String, Double>();
	private CommonMethod cm;

	public AddEditPurchaseInfo(SessionBean sessionBean, String flag, String purchaseId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.purchaseId = purchaseId;
		this.setCaption(flag+" Purchase Information :: "+this.sessionBean.getCompanyName() +
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

		btnAddSupplier.addClickListener(event ->
		{ addEditSupplier(); });

		cmbSupplierName.addValueChangeListener(event ->
		{ supplierDiscount(); loadOrderList(); });

		/*txtDiscount.addValueChangeListener(new ValueChangeListener()
		{
			public void valueChange(ValueChangeEvent event)
			{ discountAction(); }
		});*/

		cmbOrderNo.addValueChangeListener(event ->
		{ setOrderDetails(); });

		dPurchaseDate.addValueChangeListener(event ->
		{ setPurchaseNo(); });

		loadSupplier();
		setPurchaseNo();

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void setPurchaseNo()
	{
		if (!flag.equals("Edit"))
		{
			txtPurchaseNo.setReadOnly(false);
			txtPurchaseNo.setValue(pig.getPurchaseNo(cm.dfDb.format(dPurchaseDate.getValue())));
			txtPurchaseNo.setReadOnly(true);
		}
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
		String sqls = "select vSupplierId, vSupplierName, vSupplierCode, dbo.funGetNumeric(vSupplierCode)"+
				" iCode, mDiscount from master.tbSupplierMaster where iActive = 1 order by iCode asc";
		for (Iterator<?> iter = cm.selectSql(sqls).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupplierName.addItem(element[0].toString());
			cmbSupplierName.setItemCaption(element[0].toString(), element[2].toString()+" - "+element[1].toString());
			hmSupDisc.put(element[0].toString(), Double.parseDouble(element[4].toString()));
		}
	}

	private void loadOrderList()
	{
		cmbOrderNo.removeAllItems();
		String supplierId = cmbSupplierName.getValue() == null? "":cmbSupplierName.getValue().toString();
		String sqls = "select vOrderId, vOrderNo, dOrderDate from trans.tbPurchaseOrderInfo c where iActive = 1 and"+
				" vStatusId = 'S6' and vSupplierId = '"+supplierId+"' and (select (select isnull (sum (mQuantity),0)"+
				" from trans.tbPurchaseOrderDetails where vOrderId = c.vOrderId) - isnull(sum(mQuantity),0) from"+
				" trans.tbPurchaseDetails a where a.vOrderId = c.vOrderId and vPurchaseId != '"+purchaseId+"') > 0"+
				" order by dOrderDate desc";
		for (Iterator<?> iter = cm.selectSql(sqls).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbOrderNo.addItem(element[0].toString());
			cmbOrderNo.setItemCaption(element[0].toString(), element[1].toString()+" ("+cm.dfBd.format(element[2])+")");
		}
	}

	private void supplierDiscount()
	{
		String supplierId = cmbSupplierName.getValue() != null? cmbSupplierName.getValue().toString():"";
		if (!supplierId.isEmpty()) { txtDiscount.setValue(hmSupDisc.get(supplierId)); }
		else { txtDiscount.setValue("0");}
	}

	private void masterValidation()
	{
		if (cmbSupplierName.getValue() != null)
		{
			if (!txtPurchaseNo.getValue().toString().trim().isEmpty())
			{
				if (!txtDiscount.getValue().toString().trim().isEmpty())
				{
					if (dPurchaseDate.getValue() != null)
					{
						if (dDeliveryDate.getValue() != null)
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
							dDeliveryDate.focus();
							cm.showNotification("warning", "Warning!", "Provide Delivery Date");
						}
					}
					else
					{
						dPurchaseDate.focus();
						cm.showNotification("warning", "Warning!", "Provide Purchase Date");
					}
				}
				else
				{
					txtDiscount.focus();
					cm.showNotification("warning", "Warning!", "Provide Discount Amount");
				}
			}
			else
			{
				txtPurchaseNo.focus();
				cm.showNotification("warning", "Warning!", "Purchase No not found");
			}
		}
		else
		{
			cmbSupplierName.focus();
			cm.showNotification("warning", "Warning!", "Select supplier name.");
		}
	}

	private void removeRow(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null && tbCmbItemName.size() > 0 &&
				!tbTxtTotalAmount.get(ar).getValue().toString().isEmpty())
		{
			tbCmbItemName.get(ar).setValue(null);
			tblPurchaseDetailsList.removeItem(ar);
			tbCmbItemName.get(ar).clear();
			tbTxtTotalAmount.get(ar).setReadOnly(false);
			tbTxtTotalAmount.get(ar).clear();
		}
		totalAmount("Total");
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
					catch (Exception ex)
					{ System.out.println(ex); }
				}
				else if (buttonType == ButtonType.NO)
				{ cBtn.btnSave.setEnabled(true); }
			}
		});
	}

	private void insertData()
	{
		PurchaseInfoModel pim = new PurchaseInfoModel();
		setValueForSave(pim);
		if (pig.insertEditData(pim, flag))
		{
			txtClear();
			cm.showNotification("success", "Successfull!", "All information saved successfully.");
			cBtn.btnSave.setEnabled(true);

			PurchaseInformation report = new PurchaseInformation(sessionBean, "");
			report.viewReportInvoice(pim.getPurchaseId());

			if (flag.equals("Edit"))
			{ close(); }
		}
		else
		{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
	}

	private void setValueForSave(PurchaseInfoModel pim)
	{
		pim.setBranchId(sessionBean.getBranchId());
		pim.setPurchaseId(flag.equals("Add")? pig.getPurchaseId(sessionBean.getBranchId()):purchaseId);
		pim.setPurchaseNo(flag.equals("Add")? pig.getPurchaseNo(cm.dfDb.format(dPurchaseDate.getValue())).toString():
			txtPurchaseNo.getValue().toString());
		pim.setPaymentType(ogPaymentType.getValue().toString());
		pim.setSupplierId((cmbSupplierName.getValue() == null) ? "":cmbSupplierName.getValue().toString());
		pim.setOrderId((cmbOrderNo.getValue() == null) ? "":cmbOrderNo.getValue().toString());
		pim.setPurchaseDate(dPurchaseDate.getValue());
		pim.setDeliveryDate(dDeliveryDate.getValue());
		pim.setRemarks(txtRemarks.getValue().toString().trim());
		pim.setReferenceNo(txtReferenceNo.getValue().toString());
		pim.setCreatedBy(sessionBean.getUserId());
		pim.setStatusId("S5");
		pim.setApproveBy("");
		pim.setCancelBy("");
		pim.setCancelReason("");
		pim.setDetailsSql(getDetails(pim.getPurchaseId(), pim.getOrderId()));
		pim.setDetailsChange(changes);
	}

	private String getDetails(String purchaseId, String orderId)
	{
		int ar = 0;
		String sql = flag.equals("Add")? "":" delete trans.tbPurchaseDetails where vPurchaseId = '"+purchaseId+"'";
		for (int i=0; i < tbCmbItemName.size(); i++)
		{
			if (tbCmbItemName.get(i).getValue() != null && cm.getAmtValue(tbTxtTotalAmount.get(i))>0)
			{
				if (ar == 0)
				{
					sql += " insert into trans.tbPurchaseDetails(vPurchaseId, vOrderId, vItemId, vDescription,"+
							" vUnitId, vVatCatId, vVatOption, mVatPercent, mQuantity, mUnitRate, mDiscount,"+
							" mAmount, mVatAmount, mNetAmount, iActive) values";
					ar++;
				}
				sql += "('"+purchaseId+"', '"+orderId+"',"+
						" '"+tbCmbItemName.get(i).getValue().toString()+"',"+
						" '"+tbTxtDescription.get(i).getValue().toString().trim()+"',"+
						" '"+tbLblUnitId.get(i).getValue().toString()+"',"+
						" '"+tbCmbVatCatId.get(i).getValue().toString()+"',"+
						" '"+tbOgVatOption.get(i).getValue().toString()+"',"+
						" '"+hmVat.get(tbCmbVatCatId.get(i).getValue().toString())+"',"+
						" '"+cm.getAmtValue(tbTxtReceivedQty.get(i))+"',"+
						" '"+cm.getAmtValue(tbTxtPurchaseRate.get(i))+"',"+
						" '"+cm.getAmtValue(tbTxtDiscount.get(i))+"',"+ 
						" '"+cm.getAmtValue(tbTxtTotalAmount.get(i))+"',"+
						" '"+cm.getAmtValue(tbTxtVatAmount.get(i))+"',"+
						" '"+cm.getAmtValue(tbTxtNetAmount.get(i))+"', 1),";
			}
		}
		//System.out.println(sql);
		return sql.substring(0, sql.length()-1);
	}

	private void setEditData()
	{
		PurchaseInfoModel pim = new PurchaseInfoModel();
		try
		{
			if (pig.selectEditData(pim, purchaseId))
			{
				ogPaymentType.setValue(pim.getPaymentType());
				cmbSupplierName.setValue(pim.getSupplierId());
				cmbOrderNo.setValue(pim.getOrderId());
				txtRemarks.setValue(pim.getRemarks());
				txtReferenceNo.setValue(pim.getReferenceNo());
				txtDiscount.setValue("0");
				dDeliveryDate.setValue((Date) pim.getDeliveryDate());	
				dPurchaseDate.setValue((Date) pim.getPurchaseDate());

				txtPurchaseNo.setReadOnly(false);
				txtPurchaseNo.setValue(pim.getPurchaseNo());
				txtPurchaseNo.setReadOnly(true);

				setTableValue(pim.getOrderId());
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch (Exception ex)
		{ System.out.println(ex); }
	}

	private void setTableValue(String orderId)
	{
		int ar = 0;
		tblPurchaseDetailsList.setColumnCollapsed("P. Qty", orderId.isEmpty());

		String sql = "select pd.vItemId, pd.vDescription, pd.vUnitId, uni.vUnitName, pd.vVatCatId, pd.vVatOption,"+
				" pd.mVatPercent, pd.mQuantity, pd.mUnitRate, pd.mDiscount, (select (select isnull(sum(pod.mQuantity), 0)"+
				" from trans.tbPurchaseOrderDetails pod where pod.vOrderId = '"+orderId+"' and pod.vItemId = pd.vItemId)-"+
				" isnull(sum(pdd.mQuantity), 0) from trans.tbPurchaseDetails pdd where pdd.vItemId = pd.vItemId and"+
				" pdd.vOrderId = '"+orderId+"' and vPurchaseId != '"+purchaseId+"') mBalance from trans.tbPurchaseDetails"+
				" pd, master.tbUnitInfo uni where pd.vUnitId = convert(varchar(10), uni.iUnitId) and vPurchaseId = '"+purchaseId+"'";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (tbCmbItemName.size() <= ar)
			{ tableRowAdd(tbCmbItemName.size()); }

			tbCmbVatCatId.get(ar).setEnabled(orderId.isEmpty());
			tbOgVatOption.get(ar).setEnabled(orderId.isEmpty());

			tbTxtDescription.get(ar).setValue(element[1].toString());
			tbLblUnitId.get(ar).setValue(element[2].toString());
			tbLblUnitName.get(ar).setValue(element[3].toString());
			tbCmbVatCatId.get(ar).setValue(element[4].toString());
			tbOgVatOption.get(ar).setValue(element[5].toString());

			if (orderId.isEmpty())
			{
				tbCmbItemName.get(ar).setValue(element[0].toString());
				tbTxtReceivedQty.get(ar).setValue(Double.parseDouble(element[7].toString()));
				//tbTxtReceivedQty.get(ar).setValue(Double.parseDouble(element[7].toString()));
				tbTxtPurchaseRate.get(ar).setValue(Double.parseDouble(element[8].toString()));
				tbTxtDiscount.get(ar).setValue(Double.parseDouble(element[9].toString()));
			}
			else
			{ setTableEditQty(element[0].toString(), Double.parseDouble(element[7].toString()),
					Double.parseDouble(element[8].toString()), Double.parseDouble(element[9].toString())); }


			ar++;
		}
		action = true;
	}

	private void setTableEditQty(String itemIdEdit, double qty, double purRate, double disc)
	{
		for (int ar = 0; ar < tbCmbItemName.size(); ar++)
		{
			String itemId = tbCmbItemName.get(ar).getValue() != null? tbCmbItemName.get(ar).getValue().toString():"";
			if (itemId.equals(itemIdEdit))
			{
				tbTxtReceivedQty.get(ar).setValue(qty);
				//tbTxtReceivedQty.get(ar).setValue(Double.parseDouble(element[7].toString()));
				tbTxtPurchaseRate.get(ar).setValue(purRate);
				tbTxtDiscount.get(ar).setValue(disc);
				break;
			}
		}
	}

	//Detail Part Method
	private void setOrderDetails()
	{
		String orderId = cmbOrderNo.getValue() != null? cmbOrderNo.getValue().toString():"";
		if (!orderId.isEmpty())
		{
			tableClear();
			tblPurchaseDetailsList.setColumnCollapsed("P. Qty", orderId.isEmpty());
			tblPurchaseDetailsList.setColumnCollapsed("Or. Qty", orderId.isEmpty());

			String sql = "select vItemId, vUnitId, vUnitName, vVatCatId, vVatOption, mQuantity, mUnitRate, mVatAmount,"+
					" mDiscount, mNetAmount, vDescription, mBalance from (select pod.vItemId, pod.vUnitId, uni.vUnitName,"+
					" pod.vVatCatId, pod.vVatOption, pod.mQuantity, pod.mUnitRate, pod.mVatAmount, pod.mDiscount, pod.mNetAmount,"+
					" pod.vDescription, (select (select isnull(sum(mQuantity),0) from trans.tbPurchaseOrderDetails podd where"+
					" vOrderId = '"+orderId+"' and podd.vItemId = pod.vItemId) - isnull(sum(mQuantity),0) from trans.tbPurchaseDetails"+
					" pd where pd.vOrderId = '"+orderId+"' and pd.vItemId = pod.vItemId and pd.vPurchaseId != '"+purchaseId+"')"+
					" mBalance from trans.tbPurchaseOrderDetails pod, master.tbUnitInfo uni where pod.vUnitId = uni.iUnitId"+
					" and pod.vOrderId = '"+orderId+"') as tbTemp";
			//System.out.println(sql);
			int ar = 0;
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbCmbItemName.size() <= ar)
				{ tableRowAdd(tbCmbItemName.size()); }

				tbCmbVatCatId.get(ar).setEnabled(orderId.isEmpty());
				tbOgVatOption.get(ar).setEnabled(orderId.isEmpty());
				tbTxtDiscount.get(ar).setEnabled(orderId.isEmpty());

				tbCmbItemName.get(ar).setValue(element[0].toString());
				tbLblUnitId.get(ar).setValue(element[1].toString());
				tbLblUnitName.get(ar).setValue(element[2].toString());
				tbCmbVatCatId.get(ar).setValue(element[3].toString());
				tbOgVatOption.get(ar).setValue(element[4].toString());

				tbTxtPurchaseRate.get(ar).setValue(Double.parseDouble(element[6].toString()));
				tbTxtVatAmount.get(ar).setValue(Double.parseDouble(element[7].toString()));
				tbLblDiscount.get(ar).setValue(element[8].toString());
				tbTxtOrderQty.get(ar).setValue(Double.parseDouble(element[5].toString()));
				tbTxtPendingQty.get(ar).setValue(Double.parseDouble(element[11].toString()));

				//tbTxtReceivedQty.get(ar).setValue(Double.parseDouble(element[11].toString()));
				//tbTxtDiscount.get(ar).setValue(Double.parseDouble(element[6].toString()));
				tbTxtDescription.get(ar).setValue(element[10].toString());

				ar++;
			}
			action = true;
		}
	}

	/*private void vatDisCalculate(int i)
	{
		if (cmbOrderNo.getValue() != null)
		{
			if (cm.getAmtValue(txtDiscount) >= totalAmount("Total"))
			{
				if (tbCmbItemName.get(i).getValue() != null)
				{ calculateVatData(i); }
			}
		}
		else
		{
			if (cm.getAmtValue(txtDiscount) <= totalAmount("Total"))
			{
				if (tbCmbItemName.get(i).getValue() != null)
				{ calculateVatData(i); }
			}
		}
	}*/

	/*private void discountAction()
	{
		if (calculateVatDataValue())
		{
			if (cm.getAmtValue(txtDiscount) > 0)
			{
				for (int i=0; i<tbCmbItemName.size(); i++)
				{ vatDisCalculate(i); }
			}
			else
			{
				for (int i=0; i<tbCmbItemName.size(); i++)
				{ tbTxtDiscount.get(i).setValue("0"); }
			}
		}
	}*/

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
			{ loadComboData(ar); } 
		});
	}

	private void loadComboData(int ar)
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

	private void setItemDetails(int ar, String ItemId)
	{
		try
		{
			String sql = "select uni.vUnitName, cat.vCategoryName, ri.vUnitId, ri.vCategoryId, ri.mIssueRate,"+
					" (select [dbo].[funcStockQty](ri.vItemId, '"+sessionBean.getBranchId()+"')) mStockQty from"+
					" master.tbRawItemInfo ri, master.tbUnitInfo uni, master.tbItemCategory cat where"+
					" vItemId = '"+ItemId+"' and ri.vUnitId = uni.iUnitId and ri.vCategoryId = cat.vCategoryId";
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				tbLblUnitName.get(ar).setValue(element[0].toString());
				tbLblCatName.get(ar).setValue(element[1].toString());
				tbLblUnitId.get(ar).setValue(element[2].toString());
				tbLblVatCatId.get(ar).setValue(element[3].toString());

				tbTxtPurchaseRate.get(ar).setValue(Double.parseDouble(element[4].toString()));
				tbTxtPurchaseRate.get(ar).setEnabled(false);

				tbTxtStockQty.get(ar).setReadOnly(false);
				tbTxtStockQty.get(ar).setValue(Double.parseDouble(element[5].toString()));
				tbTxtStockQty.get(ar).setReadOnly(true);
			}
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	/*private boolean calculateVatDataValue()
	{
		boolean ret = false;
		try
		{
			for (int ar=0; ar<tbCmbItemName.size(); ar++)
			{
				if (tbCmbItemName.get(ar).getValue() != null)
				{
					double TotalQty = cm.getAmtValue(tbTxtReceivedQty.get(ar));
					double Rate = cm.getAmtValue(tbTxtPurchaseRate.get(ar));
					double TotalAmountCal = cm.getRound(TotalQty * Rate);

					tbTxtTotalAmount.get(ar).setReadOnly(false);
					tbTxtTotalAmount.get(ar).setValue(TotalAmountCal);
					tbTxtTotalAmount.get(ar).setReadOnly(true);
				}
			}
			for (int ar=0; ar<tbCmbItemName.size(); ar++)
			{
				if (tbCmbItemName.get(ar).getValue() != null)
				{
					double TotalAmounttable = totalAmount("Total");
					double TotalQty = cm.getAmtValue(tbTxtReceivedQty.get(ar));
					double Rate = cm.getAmtValue(tbTxtPurchaseRate.get(ar));
					double TotalAmountCal = cm.getRound(TotalQty * Rate);
					double TotalDiscount = cm.getAmtValue(txtDiscount);
					double DiscountPerItem = 0.0;

					if (TotalAmounttable > 0)
					{ DiscountPerItem = cm.getRound((TotalAmountCal * TotalDiscount) / TotalAmounttable); }

					if (TotalDiscount > 0)
					{ tbTxtDiscount.get(ar).setValue(DiscountPerItem); }
				}
			}
			ret = true;
		}
		catch (Exception e)
		{ System.out.println(e); }
		return ret;
	}*/

	private void calculateVatData(int ar)
	{
		double delivQty = cm.getAmtValue(tbTxtOrderQty.get(ar));
		double invoicQty = cm.getAmtValue(tbTxtReceivedQty.get(ar));
		double disCount = cm.getAmtValue(tbLblDiscount.get(ar));
		String vatRule = tbOgVatOption.get(ar).getValue().toString();
		double vatPecent = hmVat.get(tbCmbVatCatId.get(ar).getValue().toString());
		double unitRate = cm.getAmtValue(tbTxtPurchaseRate.get(ar));

		if (disCount > 0)
		{
			double disPerItem = disCount / delivQty;
			tbTxtDiscount.get(ar).setReadOnly(false);
			tbTxtDiscount.get(ar).setValue(invoicQty * disPerItem);
			tbTxtDiscount.get(ar).setReadOnly(cmbOrderNo.getValue()!=null? true:false);
		}
		double discount = cm.getAmtValue(tbTxtDiscount.get(ar));
		double mainAmount = (invoicQty * unitRate) - discount, vatAmount = 0;

		if (vatRule.equals("Inclusive"))
		{
			vatAmount = cm.getRound((mainAmount * vatPecent) / (100 + vatPecent));
			mainAmount = mainAmount - vatAmount;
		}
		else
		{ vatAmount = cm.getRound((mainAmount * vatPecent) / 100); }

		tbTxtTotalAmount.get(ar).setReadOnly(false);
		tbTxtTotalAmount.get(ar).setValue((invoicQty * unitRate));
		tbTxtTotalAmount.get(ar).setReadOnly(true);

		tbTxtVatAmount.get(ar).setReadOnly(false);
		tbTxtVatAmount.get(ar).setValue(vatAmount>0?vatAmount:0);
		tbTxtVatAmount.get(ar).setReadOnly(true);

		tbTxtNetAmount.get(ar).setReadOnly(false);
		tbTxtNetAmount.get(ar).setValue((mainAmount + vatAmount)>0?mainAmount + vatAmount:0);
		tbTxtNetAmount.get(ar).setReadOnly(true);

		tbAmtMainPurchaseRate.get(ar).setReadOnly(false);
		tbAmtMainPurchaseRate.get(ar).setValue(cm.getRound((mainAmount + vatAmount) / invoicQty));
		tbAmtMainPurchaseRate.get(ar).setReadOnly(true);

		totalAmount("Amount");
		addChanges();
	}

	private void addChanges()
	{
		if (flag.equals("Edit") && action)
		{ changes = true; }
	}

	private void tableClear()
	{
		totalAmount("");
		tblPurchaseDetailsList.removeAllItems();
		tbCmbItemName.clear();
		tableRowAdd(tbCmbItemName.size());
	}

	private void focusEnter()
	{
		allComp.add(cmbSupplierName);
		allComp.add(cmbOrderNo);
		allComp.add(txtReferenceNo);
		allComp.add(txtRemarks);
		allComp.add(txtDiscount);
		allComp.add(tbCmbItemName.get(0));

		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		txtPurchaseNo.setReadOnly(false);
		txtPurchaseNo.setValue(pig.getPurchaseNo(cm.dfDb.format(dPurchaseDate.getValue())));
		txtPurchaseNo.setReadOnly(true);
		txtRemarks.setValue("");
		txtReferenceNo.setValue("");
		ogPaymentType.select("Cash");
		txtDiscount.setValue("0");
		cmbSupplierName.setValue(null);
		tableClear();
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(6, 5);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		ogPaymentType = new OptionGroup();
		ogPaymentType.addItem("Cash");
		ogPaymentType.addItem("Credit");
		ogPaymentType.select("Cash");
		ogPaymentType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		ogPaymentType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		grid.addComponent(new Label("Payment Type:"), 0, 0);
		grid.addComponent(ogPaymentType, 1, 0);

		txtPurchaseNo = new TextField();
		txtPurchaseNo.setImmediate(true);
		txtPurchaseNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPurchaseNo.setWidth("120px");
		txtPurchaseNo.setInputPrompt("Purchase No");
		txtPurchaseNo.setDescription("Purchase No");
		txtPurchaseNo.setRequired(true);
		txtPurchaseNo.setRequiredError("This field is required.");
		grid.addComponent(new Label("Purchase No:"), 0, 1);
		grid.addComponent(txtPurchaseNo, 1, 1);

		dPurchaseDate  = new PopupDateField();
		dPurchaseDate.setImmediate(true);
		dPurchaseDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		dPurchaseDate.setValue(new Date());
		dPurchaseDate.setWidth("120px");
		dPurchaseDate.setDateFormat("dd-MM-yyyy");
		dPurchaseDate.setDescription("Purchase Date");
		dPurchaseDate.setRequired(true);
		dPurchaseDate.setRequiredError("This field is required");
		Label lblpd = new Label("Purchase Date:");
		lblpd.setWidth("-1");
		grid.addComponent(lblpd, 0, 2);
		grid.addComponent(dPurchaseDate, 1, 2);

		CssLayout groups = new CssLayout();
		groups.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		cm.setAuthorize(sessionBean.getUserId(), "suppInfo");

		Label lbls = new Label("Supplier Name:");
		lbls.setWidth("-1");
		grid.addComponent(lbls, 2, 0);

		cmbSupplierName = new ComboBox();
		cmbSupplierName.setInputPrompt("Select Supplier Name");
		cmbSupplierName.setWidth("250px");
		cmbSupplierName.setDescription("Select Supplier Name");
		cmbSupplierName.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbSupplierName.setRequired(true);
		cmbSupplierName.setRequiredError("This field is required");
		cmbSupplierName.setFilteringMode(FilteringMode.CONTAINS);

		btnAddSupplier = new Button("");
		btnAddSupplier.setIcon(FontAwesome.PLUS);
		btnAddSupplier.setStyleName(ValoTheme.BUTTON_TINY);
		btnAddSupplier.setDescription("Add Supplier");
		btnAddSupplier.setEnabled(cm.insert);

		groups.addComponents(cmbSupplierName, btnAddSupplier);
		grid.addComponent(groups, 3, 0);

		CssLayout groupO = new CssLayout();
		groupO.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		cmbOrderNo = new ComboBox();
		cmbOrderNo.setInputPrompt("Select Order No");
		cmbOrderNo.setWidth("280px");
		cmbOrderNo.setDescription("Select Order No");
		cmbOrderNo.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbOrderNo.setFilteringMode(FilteringMode.CONTAINS);
		grid.addComponent(new Label("Order Details:"), 2, 1);
		grid.addComponent(cmbOrderNo, 3, 1);

		txtDiscount = new CommaField();
		txtDiscount.setImmediate(true);
		txtDiscount.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDiscount.setWidth("120px");
		txtDiscount.setValue("0");
		txtDiscount.setEnabled(false);
		txtDiscount.setDescription("Total Discount Amount");
		grid.addComponent(new Label("Total Discount:"), 2, 2);
		grid.addComponent(txtDiscount, 3, 2);

		dDeliveryDate  = new PopupDateField();
		dDeliveryDate.setImmediate(true);
		dDeliveryDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		dDeliveryDate.setValue(new Date());
		dDeliveryDate.setWidth("110px");
		dDeliveryDate.setDateFormat("dd-MM-yyyy");
		dDeliveryDate.setRequiredError("This field is required");
		Label lbldv = new Label("Delivery Date:");
		lbldv.setWidth("-1");
		grid.addComponent(lbldv, 4, 0);
		grid.addComponent(dDeliveryDate, 5, 0);

		txtReferenceNo = new TextField();
		txtReferenceNo.setImmediate(true);
		txtReferenceNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReferenceNo.setWidth("200px");
		txtReferenceNo.setInputPrompt("Reference No");
		grid.addComponent(new Label("Reference"), 4, 1);
		grid.addComponent(txtReferenceNo, 5, 1);

		txtRemarks = new TextField();
		txtRemarks.setImmediate(true);
		txtRemarks.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRemarks.setWidth("200px");
		txtRemarks.setInputPrompt("Remarks");
		grid.addComponent(new Label("Remarks:"), 4, 2);
		grid.addComponent(txtRemarks, 5, 2);

		grid.addComponent(buildTable(), 0, 3, 5, 3);

		grid.addComponent(cBtn, 0, 4, 5, 4);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		loadSupplier();
		return grid;
	}

	private Table buildTable()
	{
		tblPurchaseDetailsList = new Table();
		tblPurchaseDetailsList.setSelectable(true);
		tblPurchaseDetailsList.setFooterVisible(true);
		tblPurchaseDetailsList.setColumnCollapsingAllowed(true);
		tblPurchaseDetailsList.addStyleName(ValoTheme.TABLE_SMALL);
		tblPurchaseDetailsList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblPurchaseDetailsList.setPageLength(5);
		tblPurchaseDetailsList.setWidth("100%");

		tblPurchaseDetailsList.addContainerProperty("Add", Button.class, new Button(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnWidth("Add", 50);

		tblPurchaseDetailsList.addContainerProperty("Item Name", ComboBox.class, new ComboBox(), null, null, Align.CENTER);

		tblPurchaseDetailsList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseDetailsList.addContainerProperty("Category", Label.class, new Label(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnCollapsed("Category", true);

		tblPurchaseDetailsList.addContainerProperty("Description", TextField.class, new TextField(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnCollapsed("Description", true);

		tblPurchaseDetailsList.addContainerProperty("VAT Cat", ComboBox.class, new ComboBox(), null, null, Align.CENTER);

		tblPurchaseDetailsList.addContainerProperty("VAT Option", OptionGroup.class, new OptionGroup(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnWidth("VAT Option", 85);

		tblPurchaseDetailsList.addContainerProperty("Cur. Stock", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnWidth("Cur. Stock", 60);

		tblPurchaseDetailsList.addContainerProperty("Or. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnWidth("Or. Qty", 60);
		tblPurchaseDetailsList.setColumnCollapsed("Or. Qty", true);

		tblPurchaseDetailsList.addContainerProperty("P. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnWidth("P. Qty", 60);
		tblPurchaseDetailsList.setColumnCollapsed("P. Qty", true);

		tblPurchaseDetailsList.addContainerProperty("R. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnWidth("R. Qty", 60);

		tblPurchaseDetailsList.addContainerProperty("Rate", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblPurchaseDetailsList.setColumnWidth("Rate", 80);

		tblPurchaseDetailsList.addContainerProperty("Total Amount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblPurchaseDetailsList.setColumnWidth("Total Amount", 100);

		tblPurchaseDetailsList.addContainerProperty("Discount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblPurchaseDetailsList.setColumnWidth("Discount", 80);

		tblPurchaseDetailsList.addContainerProperty("Vat", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblPurchaseDetailsList.setColumnWidth("Vat", 80);

		tblPurchaseDetailsList.addContainerProperty("Net Amount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblPurchaseDetailsList.setColumnWidth("Net Amount", 100);

		tblPurchaseDetailsList.addContainerProperty("P. Rate", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblPurchaseDetailsList.setColumnWidth("P. Rate", 80);
		tblPurchaseDetailsList.setColumnCollapsed("P. Rate", true);

		tblPurchaseDetailsList.addContainerProperty("Unit Id", Label.class, new Label(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnCollapsed("Unit Id", true);

		tblPurchaseDetailsList.addContainerProperty("Category Id", Label.class, new Label(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnCollapsed("Category Id", true);

		tblPurchaseDetailsList.addContainerProperty("Freight Cost", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnCollapsed("Freight Cost", true);

		tblPurchaseDetailsList.addContainerProperty("Custom Cost", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnCollapsed("Custom Cost", true);

		tblPurchaseDetailsList.addContainerProperty("Item Discount", Label.class, new Label(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnCollapsed("Item Discount", true);

		tblPurchaseDetailsList.addContainerProperty("Rem", Button.class, new Button(), null, null, Align.CENTER);
		tblPurchaseDetailsList.setColumnWidth("Rem", 50);
		tableRowAdd(0);
		return tblPurchaseDetailsList;
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
			tbCmbItemName.get(ar).setEnabled(cmbOrderNo.getValue()==null);
			tbCmbItemName.get(ar).addValueChangeListener(event ->
			{ itemActions(ar); });

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
			tbTxtDescription.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);

			tbCmbVatCatId.add(ar, new ComboBox());
			tbCmbVatCatId.get(ar).setWidth("100%");
			tbCmbVatCatId.get(ar).setImmediate(true);
			tbCmbVatCatId.get(ar).setNullSelectionAllowed(false);
			tbCmbVatCatId.get(ar).setFilteringMode(FilteringMode.CONTAINS);
			tbCmbVatCatId.get(ar).addStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbVatCatId.get(ar).setInputPrompt("Select VAT Category");
			tbCmbVatCatId.get(ar).setRequired(true);
			tbCmbVatCatId.get(ar).setRequiredError("This field is required");
			loadComboData(ar);
			tbCmbVatCatId.get(ar).addValueChangeListener(event ->
			{ 
				if (cmbOrderNo.getValue() == null)
				{ calculateVatData(ar); }
			});

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
			{ 
				if (cmbOrderNo.getValue() == null)
				{ calculateVatData(ar); }
			});

			tbTxtStockQty.add(ar, new CommaField());
			tbTxtStockQty.get(ar).setWidth("100%");
			tbTxtStockQty.get(ar).setImmediate(true);
			tbTxtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbTxtStockQty.get(ar).setDescription("Current Stock");
			tbTxtStockQty.get(ar).setReadOnly(true);

			tbTxtOrderQty.add(ar, new CommaField());
			tbTxtOrderQty.get(ar).setWidth("100%");
			tbTxtOrderQty.get(ar).setImmediate(true);
			tbTxtOrderQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtOrderQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbTxtOrderQty.get(ar).setEnabled(false);
			tbTxtOrderQty.get(ar).setDescription("Order Qty");

			tbTxtPendingQty.add(ar, new CommaField());
			tbTxtPendingQty.get(ar).setWidth("100%");
			tbTxtPendingQty.get(ar).setImmediate(true);
			tbTxtPendingQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtPendingQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbTxtPendingQty.get(ar).setEnabled(false);
			tbTxtPendingQty.get(ar).setDescription("Pending Qty");

			tbTxtReceivedQty.add(ar, new CommaField());
			tbTxtReceivedQty.get(ar).setWidth("100%");
			tbTxtReceivedQty.get(ar).setImmediate(true);
			tbTxtReceivedQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtReceivedQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbTxtReceivedQty.get(ar).setDescription("Received Qty");
			tbTxtReceivedQty.get(ar).setInputPrompt("Received Qty");
			tbTxtReceivedQty.get(ar).setRequired(true);
			tbTxtReceivedQty.get(ar).setRequiredError("This field is required.");
			tbTxtReceivedQty.get(ar).addValueChangeListener(event ->
			{
				if (checkQty(ar))
				{
					calculateVatData(ar);
					if (cmbOrderNo.getValue() == null)
					{
						if ((ar+1) == tbCmbItemName.size())
						{ tableRowAdd(tbCmbItemName.size()); }
						tbTxtReceivedQty.get(ar+1).focus();
					}
				}
			});

			tbTxtPurchaseRate.add(ar, new CommaField());
			tbTxtPurchaseRate.get(ar).setWidth("100%");
			tbTxtPurchaseRate.get(ar).setImmediate(true);
			tbTxtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbTxtPurchaseRate.get(ar).setRequired(true);
			tbTxtPurchaseRate.get(ar).setRequiredError("This field is required.");
			tbTxtPurchaseRate.get(ar).setDescription("Purchase Rate");
			tbTxtPurchaseRate.get(ar).setInputPrompt("Rate");

			tbTxtTotalAmount.add(ar, new CommaField());
			tbTxtTotalAmount.get(ar).setWidth("100%");
			tbTxtTotalAmount.get(ar).setImmediate(true);
			tbTxtTotalAmount.get(ar).setReadOnly(true);
			tbTxtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbTxtTotalAmount.get(ar).addValueChangeListener(event ->
			{
				if (cmbOrderNo.getValue() == null)
				{ discountCheck(ar); }
			});

			tbTxtDiscount.add(ar, new CommaField());
			tbTxtDiscount.get(ar).setWidth("100%");
			tbTxtDiscount.get(ar).setImmediate(true);
			tbTxtDiscount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtDiscount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbTxtDiscount.get(ar).addValueChangeListener(event ->
			{
				if (cmbOrderNo.getValue() == null)
				{ discountCheck(ar); }
			});

			tbTxtVatAmount.add(ar, new CommaField());
			tbTxtVatAmount.get(ar).setWidth("100%");
			tbTxtVatAmount.get(ar).setImmediate(true);
			tbTxtVatAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtVatAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbTxtNetAmount.add(ar, new CommaField());
			tbTxtNetAmount.get(ar).setWidth("100%");
			tbTxtNetAmount.get(ar).setImmediate(true);
			tbTxtNetAmount.get(ar).setReadOnly(true);
			tbTxtNetAmount.get(ar).setInputPrompt("Net Amount");
			tbTxtNetAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtNetAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

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

			tbTxtFreightCost.add(ar, new CommaField());
			tbTxtFreightCost.get(ar).setWidth("100%");
			tbTxtFreightCost.get(ar).setImmediate(true);
			tbTxtFreightCost.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtFreightCost.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

			tbTxtCustomCost.add(ar, new CommaField());
			tbTxtCustomCost.get(ar).setWidth("100%");
			tbTxtCustomCost.get(ar).setImmediate(true);
			tbTxtCustomCost.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtCustomCost.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

			tbLblDiscount.add(ar, new Label());
			tbLblDiscount.get(ar).setWidth("100%");
			tbLblDiscount.get(ar).setImmediate(true);
			tbLblDiscount.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbBtnRemove.add(ar, new Button(""));
			tbBtnRemove.get(ar).setWidth("100%");
			tbBtnRemove.get(ar).setImmediate(true);
			tbBtnRemove.get(ar).setIcon(FontAwesome.REMOVE);
			tbBtnRemove.get(ar).setStyleName(ValoTheme.BUTTON_TINY);
			tbBtnRemove.get(ar).addStyleName(ValoTheme.BUTTON_DANGER);
			tbBtnRemove.get(ar).setDescription("Remove");
			tbBtnRemove.get(ar).addClickListener(event ->
			{ removeRow(ar); });

			tblPurchaseDetailsList.addItem(new Object[]{tbBtnAddItem.get(ar), tbCmbItemName.get(ar), tbLblUnitName.get(ar),
					tbLblCatName.get(ar), tbTxtDescription.get(ar), tbCmbVatCatId.get(ar), tbOgVatOption.get(ar),
					tbTxtStockQty.get(ar), tbTxtOrderQty.get(ar), tbTxtPendingQty.get(ar), tbTxtReceivedQty.get(ar),
					tbTxtPurchaseRate.get(ar), tbTxtTotalAmount.get(ar), tbTxtDiscount.get(ar), tbTxtVatAmount.get(ar),
					tbTxtNetAmount.get(ar), tbAmtMainPurchaseRate.get(ar), tbLblUnitId.get(ar), tbLblVatCatId.get(ar),
					tbTxtFreightCost.get(ar), tbTxtCustomCost.get(ar), tbLblDiscount.get(ar), tbBtnRemove.get(ar)}, ar);
		}
		catch (Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private boolean checkQty(int ar)
	{
		boolean ret = true;
		if (cmbOrderNo.getValue() != null)
		{
			if (cm.getAmtValue(tbTxtReceivedQty.get(ar)) > cm.getAmtValue(tbTxtPendingQty.get(ar)))
			{
				ret = false;
				tbTxtReceivedQty.get(ar).setValue(cm.getAmtValue(tbTxtPendingQty.get(ar)));
				tbTxtReceivedQty.get(ar).focus();
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
				tbTxtReceivedQty.get(ar).focus();
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

	private void discountCheck(int ar)
	{
		if (cmbOrderNo.getValue() == null)
		{
			if (cm.getAmtValue(tbTxtDiscount.get(ar)) <= cm.getAmtValue(tbTxtTotalAmount.get(ar)))
			{
				calculateVatData(ar);
				addChanges();
			}
			else
			{
				tbTxtDiscount.get(ar).focus();
				tbTxtDiscount.get(ar).setValue("0");
				cm.showNotification("warning", "Warning!", "Discount limit exceeded.");	
			}	
		}
		else
		{
			if (cm.getAmtValue(tbTxtTotalAmount.get(ar)) <= cm.getAmtValue(tbTxtDiscount.get(ar)))
			{
				calculateVatData(ar);
				addChanges();
			}
			else
			{
				tbTxtDiscount.get(ar).focus();
				tbTxtDiscount.get(ar).setValue("0");
				cm.showNotification("warning", "Warning!", "Discount limit exceeded.");	
			}
		}
	}

	private double totalAmount(String flag)
	{
		double ret = 0, totalamt = 0, grandtotal = 0, totalDiscount = 0,  totalVat = 0;
		for (int i=0; i<tbCmbItemName.size(); i++)
		{
			totalamt += cm.getAmtValue(tbTxtTotalAmount.get(i));
			grandtotal += cm.getAmtValue(tbTxtNetAmount.get(i));
			totalVat += cm.getAmtValue(tbTxtVatAmount.get(i));
			totalDiscount += cm.getAmtValue(tbTxtDiscount.get(i));
		}

		tblPurchaseDetailsList.setColumnFooter("Total Amount", cm.setComma(totalamt));
		tblPurchaseDetailsList.setColumnAlignment("Total Amount", Align.RIGHT);
		tblPurchaseDetailsList.setColumnFooter("Net Amount", cm.setComma(grandtotal));
		tblPurchaseDetailsList.setColumnAlignment("Net Amount", Align.RIGHT);
		tblPurchaseDetailsList.setColumnFooter("Vat", cm.setComma(totalVat));
		tblPurchaseDetailsList.setColumnAlignment("Vat", Align.RIGHT);
		tblPurchaseDetailsList.setColumnFooter("Discount", cm.setComma(totalDiscount));
		tblPurchaseDetailsList.setColumnAlignment("Discount", Align.RIGHT);

		if (flag.equals("Total")) { ret = totalamt; }
		if (flag.equals("Net")) { ret = grandtotal; }
		if (flag.equals("Discount")) { ret = totalDiscount; }
		return ret;
	}
}
