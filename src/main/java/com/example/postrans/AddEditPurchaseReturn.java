package com.example.postrans;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.PurchaseReturnGateway;
import com.example.model.PurchaseReturnModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditPurchaseReturn extends Window
{
	private SessionBean sessionBean;
	private String flag, returnId;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private PurchaseReturnGateway prg = new PurchaseReturnGateway();

	private TextField txtReturnNo, txtRemarks, txtReferenceNo;
	private ComboBox cmbInvoiceDetails;
	private PopupDateField txtReturnDate, txtFromDate, txtToDate;

	private Table tblReturnDetailsList;
	private ArrayList<ComboBox> tbCmbItemName = new ArrayList<ComboBox>();
	private ArrayList<Label> tbLblUnitName = new ArrayList<Label>();
	private ArrayList<Label> tbLblCatName = new ArrayList<Label>();
	private ArrayList<Label> tblblVatCatId = new ArrayList<Label>();
	private ArrayList<Label> tblblVatOption = new ArrayList<Label>();
	private ArrayList<Label> tblblVatRule = new ArrayList<Label>();
	private ArrayList<Label> tbLblUnitId = new ArrayList<Label>();
	private ArrayList<TextField> tbTxtDescription = new ArrayList<TextField>();
	private ArrayList<CommaField> tbAmtStockQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtPurchaseQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtReturnedQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtPurchaseRate = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtTotalAmount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtDiscount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtPerDiscount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtAmountDiscount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtVatAmount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtNetAmount = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtMainPurchaseRate = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtWithOutVat = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtCustomCost = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtFreightCost = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtVatPercent = new ArrayList<CommaField>();

	private ArrayList<Component> allComp = new ArrayList<Component>();
	private CommonMethod cm;
	private boolean changes = false, action = false;

	public AddEditPurchaseReturn(SessionBean sessionBean, String flag, String returnId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.returnId = returnId;
		this.setCaption(flag+" Purchase Return :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);
		setWidth("100%");
		setHeight("590px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
		{ masterValidation(); });

		cBtn.btnExit.addClickListener(event ->
		{ close(); });

		cmbInvoiceDetails.addValueChangeListener(event ->
		{ setPurchaseDetails(); });

		txtReturnDate.addValueChangeListener(event ->
		{ setReturnNo(); });

		txtFromDate.addValueChangeListener(event ->
		{ loadInvoiceDetails(); });

		txtToDate.addValueChangeListener(event ->
		{ loadInvoiceDetails(); });

		setReturnNo();
		focusEnter();
	}

	private void setReturnNo()
	{
		txtReturnNo.setReadOnly(false);
		txtReturnNo.setValue(prg.getReturnNo(cm.dfDb.format(txtReturnDate.getValue())));
		txtReturnNo.setReadOnly(true);
	}

	private void loadInvoiceDetails()
	{
		cmbInvoiceDetails.removeAllItems();
		String branchId = sessionBean.getBranchId();
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());

		String sql = "select pin.vPurchaseId, pin.vPurchaseNo, pin.dPurchaseDate, sm.vSupplierCode+' - '+sm.vSupplierName"+
				" vSupplierDetails, pin.vPurchaseType from trans.tbPurchaseInfo pin, master.tbSupplierMaster sm where"+
				" pin.vSupplierId = sm.vSupplierId and pin.vStatusId = 'S6' and pin.iActive = 1 and pin.vPurchaseId"+
				" not in (select vPurchaseId from trans.tbReceiptPurchaseDetails rpd where rpd.iActive = 1) and"+
				" pin.vPurchaseId not in (select vPurchaseId from trans.tbPurchaseReturnInfo pri where pri.iActive = 1"+
				" and vReturnId != '"+returnId+"') and pin.dPurchaseDate between '"+fromDate+"' and '"+toDate+"' and"+
				" pin.vBranchId = '"+branchId+"' order by pin.dPurchaseDate, pin.iAutoId desc";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbInvoiceDetails.addItem(element[0].toString());
			cmbInvoiceDetails.setItemCaption(element[0].toString(), element[3].toString()+" - "+element[1].toString()
					+" ("+cm.dfBd.format(element[2])+") "+element[4].toString());
		}
	}

	private void masterValidation()
	{
		if (cmbInvoiceDetails.getValue() != null)
		{
			if (!txtReturnNo.getValue().toString().trim().isEmpty())
			{				
				if (txtReturnDate.getValue() != null)
				{
					cBtn.btnSave.setEnabled(false);
					insertEditData();	
				}
				else
				{
					txtReturnDate.focus();
					cm.showNotification("warning", "Warning!", "Provide return date.");
				}			
			}
			else
			{
				txtReturnNo.focus();
				cm.showNotification("warning", "Warning!", "Return no not found.");
			}
		}
		else
		{
			cmbInvoiceDetails.focus();
			cm.showNotification("warning", "Warning!", "Select invoice details.");
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
		PurchaseReturnModel pim = new PurchaseReturnModel();
		setValueForSave(pim);
		if (prg.insertEditData(pim, flag))
		{
			txtClear();
			cm.showNotification("success", "Successfull!", "All information saved successfully.");
			cBtn.btnSave.setEnabled(true);

			PurchaseReturnInfo report = new PurchaseReturnInfo(sessionBean, "");
			report.viewReportSingle(pim.getReturnId());

			if (flag.equals("Edit"))
			{ close(); }
		}
		else
		{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
	}

	private void setValueForSave(PurchaseReturnModel pim)
	{
		pim.setBranchId(sessionBean.getBranchId());
		pim.setReturnId(flag.equals("Add")? prg.getReturnId(sessionBean.getBranchId()):returnId);
		pim.setReturnNo(flag.equals("Add")? prg.getReturnNo(cm.dfDb.format(txtReturnDate.getValue())).toString():
			txtReturnNo.getValue().toString());
		pim.setPurchaseId(cmbInvoiceDetails.getValue().toString());
		pim.setReturnDate(txtReturnDate.getValue());
		pim.setRemarks(txtRemarks.getValue().toString().trim());
		pim.setReferenceNo(txtReferenceNo.getValue().toString());
		pim.setCreatedBy(sessionBean.getUserId());
		pim.setStatusId("S6");
		pim.setApproveBy(sessionBean.getUserId());
		pim.setCancelBy("");
		pim.setCancelReason("");
		pim.setDetailsSql(getDetails(pim.getReturnId()));
		pim.setDetailsChange(changes);
	}

	private String getDetails(String returnId)
	{
		String purchaseId = cmbInvoiceDetails.getValue() == null? "":cmbInvoiceDetails.getValue().toString();
		String sql = flag.equals("Add")? "":" delete trans.tbPurchaseReturnDetails where vReturnId = '"+returnId+"'";

		for (int i=0; i < tbCmbItemName.size(); i++)
		{
			if (i == 0)
			{
				sql += " insert into trans.tbPurchaseReturnDetails(vReturnId, vPurchaseId, vItemId, vDescription,"+
						" vUnitId, vVatCatId, vVatOption, mVatPercent, mQuantity, mUnitRate, mDiscount, mAmount,"+
						" mVatAmount, mNetAmount, iActive) values";
			}
			if (tbCmbItemName.get(i).getValue() != null && cm.getAmtValue(tbAmtTotalAmount.get(i))>0)
			{
				sql +=  "('"+returnId+"', '"+purchaseId+"',"+
						" '"+tbCmbItemName.get(i).getValue().toString()+"',"+
						" '"+tbTxtDescription.get(i).getValue().toString()+"',"+
						" '"+tbLblUnitId.get(i).getValue().toString()+"',"+
						" '"+tblblVatCatId.get(i).getValue().toString()+"',"+
						" '"+tblblVatRule.get(i).getValue().toString()+"',"+
						" '"+cm.getAmtValue(tbAmtVatPercent.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtReturnedQty.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtPurchaseRate.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtDiscount.get(i))+"',"+ 
						" '"+cm.getAmtValue(tbAmtTotalAmount.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtVatAmount.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtNetAmount.get(i))+"', 1),";
			}
		}
		return sql.substring(0, sql.length()-1);
	}

	private void setPurchaseDetails()
	{
		String purchaseId = cmbInvoiceDetails.getValue() != null? cmbInvoiceDetails.getValue().toString():"";
		tableClear();
		String sql = "select pui.vPurchaseId, pui.vPurchaseNo, pui.vReferenceNo, pui.vRemarks, pud.vItemId, pud.vUnitId,"+
				" uni.vUnitName, pud.vDescription, pud.vVatCatId, vat.vVatCatName, pud.vVatOption, pud.mVatPercent,"+
				" pud.mUnitRate, pud.mQuantity, pud.mVatAmount, pud.mDiscount, pud.mAmount, pud.mNetAmount, ISNULL((select"+
				" prd.mQuantity from trans.tbPurchaseReturnDetails prd where prd.vReturnId = '"+returnId+"' and prd.vItemId"+
				" = pud.vItemId and prd.iActive = 1), 0) mReturnedQty from trans.tbPurchaseInfo pui, trans.tbPurchaseDetails"+
				" pud, master.tbRawItemInfo rii, master.tbVatCatMaster vat, master.tbUnitInfo uni where pui.vPurchaseId"+
				" = pud.vPurchaseId and pud.vItemId = rii.vItemId and pud.vVatCatId = vat.vVatCatId and pud.vUnitId"+
				" = convert(varchar(10), uni.iUnitId) and pui.vPurchaseId = '"+purchaseId+"' order by pud.iAutoId";
		//System.out.println(sql);
		int ar = 0;
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (tbCmbItemName.size() <= ar)
			{ tableRowAdd(tbCmbItemName.size()); }

			if (ar == 0)
			{
				txtReferenceNo.setValue(element[2].toString());
				txtRemarks.setValue(element[3].toString());
			}

			tbCmbItemName.get(ar).setValue(element[4].toString());
			tbLblUnitId.get(ar).setValue(element[5].toString());
			tbLblUnitName.get(ar).setValue(element[6].toString());
			tbTxtDescription.get(ar).setValue(element[7].toString());
			tblblVatCatId.get(ar).setValue(element[8].toString());
			tblblVatOption.get(ar).setValue(element[9].toString()+" ("+element[10].toString()+")");
			tblblVatRule.get(ar).setValue(element[10].toString());
			tbAmtVatPercent.get(ar).setValue(Double.parseDouble(element[11].toString()));

			tbAmtPurchaseRate.get(ar).setValue(Double.parseDouble(element[12].toString()));
			tbAmtPurchaseQty.get(ar).setValue(Double.parseDouble(element[13].toString()));
			tbAmtDiscount.get(ar).setValue(Double.parseDouble(element[15].toString()));

			if (Double.parseDouble(element[18].toString())>0)
			{ tbAmtReturnedQty.get(ar).setValue(Double.parseDouble(element[18].toString())); }

			double Qty = Double.parseDouble(element[13].toString());
			double DisCount = cm.getAmtValue(tbAmtDiscount.get(ar));
			double PerDiscount = cm.getRound(DisCount/Qty);
			tbAmtPerDiscount.get(ar).setValue(PerDiscount);

			setStockQty(ar, element[4].toString());
			ar++;
		}
		action = true;
	}

	private void setStockQty(int ar, String itemId)
	{
		try
		{
			String branchId = sessionBean.getBranchId();
			String sql = "select (select [dbo].[funcStockQty](rii.vItemId, '"+branchId+"')) mStockQty, cat.vCategoryName"+
					" from master.tbRawItemInfo rii, master.tbItemCategory cat where rii.vCategoryId = cat.vCategoryId"+
					" and rii.vItemId = '"+itemId+"'";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				tbAmtStockQty.get(ar).setReadOnly(false);
				tbAmtStockQty.get(ar).setValue(Double.parseDouble(element[0].toString()));
				tbAmtStockQty.get(ar).setReadOnly(true);
				tbLblCatName.get(ar).setValue(element[1].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void loadComboData(int ar)
	{
		String sql = "select vItemId, vItemName, vItemCode, dbo.funGetNumeric(vItemCode) iCode from"+
				" master.tbRawItemInfo where iActive = 1 order by iCode asc";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tbCmbItemName.get(ar).addItem(element[0].toString());
			tbCmbItemName.get(ar).setItemCaption(element[0].toString(), element[2].toString()+" - "+element[1].toString());
		}
	}

	private void calculateVatData(int ar)
	{
		double TotalQty = cm.getAmtValue(tbAmtReturnedQty.get(ar));
		double Rate = cm.getAmtValue(tbAmtPurchaseRate.get(ar));
		double TotalAmountCal = cm.getRound(TotalQty * Rate);
		double PerDisCount = cm.getAmtValue(tbAmtPerDiscount.get(ar));

		double Discount = cm.getRound(TotalQty * PerDisCount); 
		double WithoutDiscount = 0.0;

		if (TotalAmountCal > Discount)
		{ WithoutDiscount = cm.getRound(TotalAmountCal - Discount); }

		double PurchaseAmtCal = 0.0;
		double TotalVat = cm.getAmtValue(tbAmtVatPercent.get(ar));
		double TotalVatCal = 0.0;
		double NetAmtCal = 0.0;
		double WithoutVATAmtCal = 0.0;

		if (WithoutDiscount > 0)
		{
			if (tblblVatOption.get(ar).getValue().equals("Inclusive"))
			{
				TotalVatCal = cm.getRound((WithoutDiscount * TotalVat) / (100 + TotalVat));
				WithoutVATAmtCal = cm.getRound(WithoutDiscount - TotalVatCal);
				NetAmtCal = cm.getRound(WithoutVATAmtCal + TotalVatCal);
			}
			else
			{
				TotalVatCal = cm.getRound((WithoutDiscount * TotalVat) / 100);
				WithoutVATAmtCal = cm.getRound(WithoutDiscount);
				NetAmtCal = cm.getRound(WithoutDiscount + TotalVatCal);
			}

			if (TotalQty > 0)
			{ PurchaseAmtCal = cm.getRound(NetAmtCal / TotalQty); }
		}

		if (Discount>0)
		{ tbAmtDiscount.get(ar).setValue(Discount); }

		tbAmtTotalAmount.get(ar).setReadOnly(false);
		tbAmtTotalAmount.get(ar).setValue(TotalAmountCal);
		tbAmtTotalAmount.get(ar).setReadOnly(true);

		tbAmtAmountDiscount.get(ar).setReadOnly(false);
		tbAmtAmountDiscount.get(ar).setValue(WithoutDiscount);
		tbAmtAmountDiscount.get(ar).setReadOnly(true);

		tbAmtVatAmount.get(ar).setReadOnly(false);
		tbAmtVatAmount.get(ar).setValue(TotalVatCal);
		tbAmtVatAmount.get(ar).setReadOnly(true);

		tbAmtWithOutVat.get(ar).setReadOnly(false);
		tbAmtWithOutVat.get(ar).setValue(WithoutVATAmtCal);
		tbAmtWithOutVat.get(ar).setReadOnly(true);

		tbAmtNetAmount.get(ar).setReadOnly(false);
		tbAmtNetAmount.get(ar).setValue(NetAmtCal);
		tbAmtNetAmount.get(ar).setReadOnly(true);

		tbAmtMainPurchaseRate.get(ar).setReadOnly(false);
		tbAmtMainPurchaseRate.get(ar).setValue(PurchaseAmtCal);
		tbAmtMainPurchaseRate.get(ar).setReadOnly(true);
		totalAmount("Total");
		addChanges();
	}

	private void addChanges()
	{
		if (flag.equals("Edit") && action)
		{ changes = true; }
	}

	private void focusEnter()
	{
		allComp.add(txtReturnDate);
		allComp.add(txtFromDate);
		allComp.add(txtToDate);
		allComp.add(cmbInvoiceDetails);
		allComp.add(txtReferenceNo);
		allComp.add(txtRemarks);

		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}

	private void tableClear()
	{
		tblReturnDetailsList.removeAllItems();
		tbCmbItemName.clear();
	}

	private void txtClear()
	{
		txtReturnNo.setReadOnly(false);
		txtReturnNo.setValue(prg.getReturnNo(cm.dfDb.format(txtReturnDate.getValue())));
		txtReturnNo.setReadOnly(true);
		txtRemarks.setValue("");
		txtReferenceNo.setValue("");

		tableClear();
		totalAmount("");
	}

	private boolean checkQty(int ar)
	{
		boolean ret = true;
		if (cmbInvoiceDetails.getValue() != null)
		{
			if (cm.getAmtValue(tbAmtReturnedQty.get(ar)) > cm.getAmtValue(tbAmtPurchaseQty.get(ar)))
			{
				ret = false;
				tbAmtReturnedQty.get(ar).setValue(cm.getAmtValue(tbAmtPurchaseQty.get(ar)));
				tbAmtReturnedQty.get(ar).focus();
				cm.showNotification("warning", "Warning!", "Purchased qty exceeded.");
			}
		}
		return ret;
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(16, 4);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtReturnNo = new TextField();
		txtReturnNo.setImmediate(true);
		txtReturnNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReturnNo.setWidth("140px");
		txtReturnNo.setInputPrompt("Return No");
		txtReturnNo.setDescription("Return No");
		txtReturnNo.setRequired(true);
		txtReturnNo.setRequiredError("This field is required.");
		Label lblp = new Label("Return No: ");
		lblp.setWidth("-1px");
		grid.addComponent(lblp, 0, 0);
		grid.addComponent(txtReturnNo, 1, 0);

		txtReturnDate  = new PopupDateField();
		txtReturnDate.setImmediate(true);
		txtReturnDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtReturnDate.setValue(new Date());
		txtReturnDate.setWidth("140px");
		txtReturnDate.setDateFormat("dd-MM-yyyy");
		txtReturnDate.setDescription("Return Date");
		txtReturnDate.setRequired(true);
		txtReturnDate.setRequiredError("This field is required");
		Label lblpd = new Label("Return Date: ");
		lblpd.setWidth("-1px");
		grid.addComponent(lblpd, 0, 1);
		grid.addComponent(txtReturnDate, 1, 1);

		txtFromDate  = new PopupDateField();
		txtFromDate.setImmediate(true);
		txtFromDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDate.setValue(new Date());
		txtFromDate.setWidth("110px");
		txtFromDate.setDateFormat("dd-MM-yyyy");
		txtFromDate.setDescription("From Date For Search");
		Label ll = new Label("Purchase Date(s): ");
		ll.setWidth("-1px");
		grid.addComponent(ll, 2, 0);
		grid.addComponent(txtFromDate, 3, 0, 4, 0);

		txtToDate  = new PopupDateField();
		txtToDate.setImmediate(true);
		txtToDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDate.setValue(new Date());
		txtToDate.setWidth("110px");
		txtToDate.setDateFormat("dd-MM-yyyy");
		txtToDate.setDescription("To Date For Search");
		grid.addComponent(txtToDate, 5, 0, 6, 0);

		cmbInvoiceDetails = new ComboBox();
		cmbInvoiceDetails.setInputPrompt("Select Invoice Details");
		cmbInvoiceDetails.setWidth("100%");
		cmbInvoiceDetails.setDescription("Select Invoice Details");
		cmbInvoiceDetails.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbInvoiceDetails.setRequired(true);
		cmbInvoiceDetails.setRequiredError("This field is required");
		cmbInvoiceDetails.setFilteringMode(FilteringMode.CONTAINS);
		Label lbls = new Label("Invoice Details: ");
		lbls.setWidth("-1px");
		grid.addComponent(lbls, 2, 1);
		grid.addComponent(cmbInvoiceDetails, 3, 1, 6, 1);

		txtReferenceNo = new TextField();
		txtReferenceNo.setImmediate(true);
		txtReferenceNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReferenceNo.setWidth("100%");
		txtReferenceNo.setInputPrompt("Reference No");
		Label lblrf = new Label("Ref. No: ");
		lblrf.setWidth("-1px");
		grid.addComponent(lblrf, 7, 0);
		grid.addComponent(txtReferenceNo, 8, 0, 15, 0);

		txtRemarks = new TextField();
		txtRemarks.setImmediate(true);
		txtRemarks.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRemarks.setWidth("100%");
		txtRemarks.setInputPrompt("Remarks");
		Label lblr = new Label("Remarks: ");
		lblr.setWidth("-1px");
		grid.addComponent(lblr, 7, 1);
		grid.addComponent(txtRemarks, 8, 1, 15, 1);

		grid.addComponent(buildTable(), 0, 2, 15, 2);

		grid.addComponent(cBtn, 0, 3, 15, 3);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);
		loadInvoiceDetails();

		return grid;
	}

	private Table buildTable()
	{
		tblReturnDetailsList = new Table();
		tblReturnDetailsList.setSelectable(true);
		tblReturnDetailsList.setFooterVisible(true);
		tblReturnDetailsList.setColumnCollapsingAllowed(true);
		tblReturnDetailsList.addStyleName(ValoTheme.TABLE_SMALL);
		tblReturnDetailsList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblReturnDetailsList.setPageLength(9);
		tblReturnDetailsList.setWidth("100%");

		tblReturnDetailsList.addContainerProperty("Item Name", ComboBox.class, new ComboBox(), null, null, Align.CENTER);

		tblReturnDetailsList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnWidth("Unit", 85);

		tblReturnDetailsList.addContainerProperty("Category", Label.class, new Label(), null, null, Align.CENTER);

		tblReturnDetailsList.addContainerProperty("Description", TextField.class, new TextField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnCollapsed("Description", true);

		tblReturnDetailsList.addContainerProperty("VAT Id", Label.class, new Label(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnWidth("VAT Id", 130);
		tblReturnDetailsList.setColumnCollapsed("VAT Id", true);

		tblReturnDetailsList.addContainerProperty("VAT Rule", Label.class, new Label(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnCollapsed("VAT Rule", true);

		tblReturnDetailsList.addContainerProperty("VAT Option", Label.class, new Label(), null, null, Align.CENTER);

		tblReturnDetailsList.addContainerProperty("Cur. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnWidth("Cur. Qty", 60);

		tblReturnDetailsList.addContainerProperty("Pur. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnWidth("Pur. Qty", 60);

		tblReturnDetailsList.addContainerProperty("R. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnWidth("R. Qty", 60);

		tblReturnDetailsList.addContainerProperty("Rate", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblReturnDetailsList.setColumnWidth("Rate", 60);

		tblReturnDetailsList.addContainerProperty("Total Amount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblReturnDetailsList.setColumnWidth("Total Amount", 100);

		tblReturnDetailsList.addContainerProperty("Discount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblReturnDetailsList.setColumnWidth("Discount", 80);

		tblReturnDetailsList.addContainerProperty("Amt-Dis", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblReturnDetailsList.setColumnWidth("Amt-Dis", 100);
		tblReturnDetailsList.setColumnCollapsed("Amt-Dis", true);

		tblReturnDetailsList.addContainerProperty("Vat", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblReturnDetailsList.setColumnWidth("Vat", 80);

		tblReturnDetailsList.addContainerProperty("Without Vat", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblReturnDetailsList.setColumnWidth("Without Vat", 100);
		tblReturnDetailsList.setColumnCollapsed("Without Vat", true);

		tblReturnDetailsList.addContainerProperty("Net Amount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblReturnDetailsList.setColumnWidth("Net Amount", 100);

		tblReturnDetailsList.addContainerProperty("P. Rate", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblReturnDetailsList.setColumnWidth("P. Rate", 80);
		tblReturnDetailsList.setColumnCollapsed("P. Rate", true);

		tblReturnDetailsList.addContainerProperty("Unit Id", Label.class, new Label(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnCollapsed("Unit Id", true);

		tblReturnDetailsList.addContainerProperty("Freight Cost", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnCollapsed("Freight Cost", true);

		tblReturnDetailsList.addContainerProperty("Custom Cost", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnCollapsed("Custom Cost", true);

		tblReturnDetailsList.addContainerProperty("Vat Percent", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnCollapsed("Vat Percent", true);

		tblReturnDetailsList.addContainerProperty("Per Discount", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnCollapsed("Per Discount", true);

		return tblReturnDetailsList;
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbCmbItemName.add(ar, new ComboBox());
			tbCmbItemName.get(ar).setWidth("100%");
			tbCmbItemName.get(ar).setImmediate(true);
			tbCmbItemName.get(ar).setStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbItemName.get(ar).setFilteringMode(FilteringMode.CONTAINS);
			tbCmbItemName.get(ar).setEnabled(false);
			tbCmbItemName.get(ar).setRequired(true);
			tbCmbItemName.get(ar).setRequiredError("This field is required");
			loadComboData(ar);

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

			tblblVatCatId.add(ar, new Label());
			tblblVatCatId.get(ar).setWidth("100%");
			tblblVatCatId.get(ar).setImmediate(true);
			tblblVatCatId.get(ar).addStyleName(ValoTheme.LABEL_TINY);			

			tblblVatOption.add(ar, new Label());
			tblblVatOption.get(ar).setWidth("100%");
			tblblVatOption.get(ar).setImmediate(true);
			tblblVatOption.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tblblVatRule.add(ar, new Label());
			tblblVatRule.get(ar).setWidth("100%");
			tblblVatRule.get(ar).setImmediate(true);
			tblblVatRule.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbAmtStockQty.add(ar, new CommaField());
			tbAmtStockQty.get(ar).setWidth("100%");
			tbAmtStockQty.get(ar).setImmediate(true);
			tbAmtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtStockQty.get(ar).setDescription("Current Stock");
			tbAmtStockQty.get(ar).setReadOnly(true);

			tbAmtPurchaseQty.add(ar, new CommaField());
			tbAmtPurchaseQty.get(ar).setWidth("100%");
			tbAmtPurchaseQty.get(ar).setImmediate(true);
			tbAmtPurchaseQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtPurchaseQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtPurchaseQty.get(ar).setEnabled(false);
			tbAmtPurchaseQty.get(ar).setDescription("Purchased Qty");

			tbAmtReturnedQty.add(ar, new CommaField());
			tbAmtReturnedQty.get(ar).setWidth("100%");
			tbAmtReturnedQty.get(ar).setImmediate(true);
			tbAmtReturnedQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtReturnedQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtReturnedQty.get(ar).setDescription("Returned Qty");
			tbAmtReturnedQty.get(ar).setRequired(true);
			tbAmtReturnedQty.get(ar).setRequiredError("This field is required");
			tbAmtReturnedQty.get(ar).addValueChangeListener(event ->
			{
				if (checkQty(ar))
				{ calculateVatData(ar); }
			});

			tbAmtPurchaseRate.add(ar, new CommaField());
			tbAmtPurchaseRate.get(ar).setWidth("100%");
			tbAmtPurchaseRate.get(ar).setImmediate(true);
			tbAmtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtPurchaseRate.get(ar).setEnabled(false);
			tbAmtPurchaseRate.get(ar).setDescription("Purchase Rate");

			tbAmtTotalAmount.add(ar, new CommaField());
			tbAmtTotalAmount.get(ar).setWidth("100%");
			tbAmtTotalAmount.get(ar).setImmediate(true);
			tbAmtTotalAmount.get(ar).setReadOnly(true);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbAmtDiscount.add(ar, new CommaField());
			tbAmtDiscount.get(ar).setWidth("100%");
			tbAmtDiscount.get(ar).setImmediate(true);
			tbAmtDiscount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtDiscount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtDiscount.get(ar).setEnabled(false);

			tbAmtAmountDiscount.add(ar, new CommaField());
			tbAmtAmountDiscount.get(ar).setWidth("100%");
			tbAmtAmountDiscount.get(ar).setImmediate(true);
			tbAmtAmountDiscount.get(ar).setReadOnly(true);
			tbAmtAmountDiscount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtAmountDiscount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtAmountDiscount.get(ar).setDescription("Amount-Discount");

			tbAmtVatAmount.add(ar, new CommaField());
			tbAmtVatAmount.get(ar).setWidth("100%");
			tbAmtVatAmount.get(ar).setImmediate(true);
			tbAmtVatAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtVatAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbAmtWithOutVat.add(ar, new CommaField());
			tbAmtWithOutVat.get(ar).setWidth("100%");
			tbAmtWithOutVat.get(ar).setImmediate(true);
			tbAmtWithOutVat.get(ar).setReadOnly(true);
			tbAmtWithOutVat.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtWithOutVat.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbAmtNetAmount.add(ar, new CommaField());
			tbAmtNetAmount.get(ar).setWidth("100%");
			tbAmtNetAmount.get(ar).setImmediate(true);
			tbAmtNetAmount.get(ar).setReadOnly(true);
			tbAmtNetAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtNetAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

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

			tbAmtVatPercent.add(ar, new CommaField());
			tbAmtVatPercent.get(ar).setWidth("100%");
			tbAmtVatPercent.get(ar).setImmediate(true);
			tbAmtVatPercent.get(ar).setEnabled(false);
			tbAmtVatPercent.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtVatPercent.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

			tbAmtPerDiscount.add(ar, new CommaField());
			tbAmtPerDiscount.get(ar).setWidth("100%");
			tbAmtPerDiscount.get(ar).setImmediate(true);
			tbAmtPerDiscount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtPerDiscount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtPerDiscount.get(ar).setEnabled(false);

			tblReturnDetailsList.addItem(new Object[]{tbCmbItemName.get(ar), tbLblUnitName.get(ar), tbLblCatName.get(ar),
					tbTxtDescription.get(ar), tblblVatCatId.get(ar), tblblVatOption.get(ar), tblblVatRule.get(ar), tbAmtStockQty.get(ar),
					tbAmtPurchaseQty.get(ar), tbAmtReturnedQty.get(ar), tbAmtPurchaseRate.get(ar), tbAmtTotalAmount.get(ar),
					tbAmtDiscount.get(ar), tbAmtAmountDiscount.get(ar), tbAmtVatAmount.get(ar), tbAmtWithOutVat.get(ar),
					tbAmtNetAmount.get(ar), tbAmtMainPurchaseRate.get(ar), tbLblUnitId.get(ar), tbAmtFreightCost.get(ar),
					tbAmtCustomCost.get(ar), tbAmtVatPercent.get(ar), tbAmtPerDiscount.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private double totalAmount(String flag)
	{
		double ret = 0, totalamt = 0, grandtotal = 0, totalDiscount = 0,  totalVat = 0;

		for (int i=0; i<tbCmbItemName.size(); i++)
		{
			totalamt += cm.getAmtValue(tbAmtTotalAmount.get(i));
			grandtotal += cm.getAmtValue(tbAmtNetAmount.get(i));
			totalVat += cm.getAmtValue(tbAmtVatAmount.get(i));
			totalDiscount += cm.getAmtValue(tbAmtDiscount.get(i));
		}

		tblReturnDetailsList.setColumnFooter("Total Amount", cm.setComma(totalamt));
		tblReturnDetailsList.setColumnAlignment("Total Amount", Align.RIGHT);
		tblReturnDetailsList.setColumnFooter("Net Amount", cm.setComma(grandtotal));
		tblReturnDetailsList.setColumnAlignment("Net Amount", Align.RIGHT);
		tblReturnDetailsList.setColumnFooter("Vat", cm.setComma(totalVat));
		tblReturnDetailsList.setColumnAlignment("Vat", Align.RIGHT);
		tblReturnDetailsList.setColumnFooter("Discount", cm.setComma(totalDiscount));
		tblReturnDetailsList.setColumnAlignment("Discount", Align.RIGHT);

		if (flag.equals("Total")) { ret = totalamt; }
		if (flag.equals("Net")) { ret = grandtotal; }
		if (flag.equals("Discount")) { ret = totalDiscount; }

		return ret;
	}
}
