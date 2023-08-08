package com.example.postrans;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonMethod;
import com.common.share.SessionBean;
import com.example.gateway.ReceiptAgainstPurcGateway;
import com.example.model.PurchaseReceiptModel;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TabReceiptAgainstPurchase extends VerticalLayout
{
	private SessionBean sessionBean;
	private CommonMethod cm;

	public OptionGroup ogInvoiceType, ogReceiptType, ogPaymentType;
	public PopupDateField txtFromDate, txtToDate, txtReceiptDate, txtChequeDate;
	public TextField txtReceiptNo, txtChequeNo;

	public ComboBox cmbSupName, cmbChequeNo, cmbNarration;
	private Label lblSupCus;

	private Table tblPurchaseDetails;
	public ArrayList<CheckBox> tbChkSelect = new ArrayList<CheckBox>();
	private ArrayList<Label> tbLblPurchaseId = new ArrayList<Label>();
	private ArrayList<Label> tbLblPurchaseNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblPurchaseDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblPurchaseRef = new ArrayList<Label>();
	private ArrayList<Label> tbLblNoOfItem = new ArrayList<Label>();
	private ArrayList<Label> tbLblBalance = new ArrayList<Label>();
	private ArrayList<CommaField> tbTxtAmount = new ArrayList<CommaField>();
	private ArrayList<Label> tbLblRemain = new ArrayList<Label>();
	private ArrayList<Label> tbLblStatus = new ArrayList<Label>();
	private ArrayList<Button> tbBtnRemove  = new ArrayList<Button>();

	private ReceiptAgainstPurcGateway pog = new ReceiptAgainstPurcGateway();
	public boolean changesDetails = false, actionDetails = false, tick = false;
	public String flag = "", chequeNoOld = "";

	public TabReceiptAgainstPurchase(SessionBean sessionBean, String flag, String receiptId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		cm = new CommonMethod(this.sessionBean);
		setSizeFull();

		addComponent(buildComponent());
		loadComboData();
		setReceiptNo();

		addActions();
	}

	private void addActions()
	{
		ogInvoiceType.addValueChangeListener(event ->
		{
			loadComboData();
			checkRecPay();
			setPurchaseData();
			setReceiptNo();
		});

		cmbSupName.addValueChangeListener(event ->
		{
			if (flag.equals("Add"))
			{ setPurchaseData(); }
		});

		txtFromDate.addValueChangeListener(event ->
		{
			if (flag.equals("Add"))
			{ setPurchaseData(); }
		});

		txtToDate.addValueChangeListener(event ->
		{
			if (flag.equals("Add"))
			{ setPurchaseData(); }
		});

		ogReceiptType.addValueChangeListener(event -> checkReceipt());

		ogPaymentType.addValueChangeListener(event -> checkCheque());

		tblPurchaseDetails.addHeaderClickListener(event ->
		{
			if (event.getPropertyId().toString().equals("Select"))
			{ selectAll(); }
		});

		txtReceiptDate.addValueChangeListener(event -> setReceiptNo());
	}

	private void loadComboData()
	{
		cmbSupName.removeAllItems();
		String sql = "select distinct oi.vSupplierId, ld.vSupplierName, ld.vSupplierCode, dbo.funGetNumeric(ld.vSupplierCode) iCode from" + 
				" trans.tbPurchaseInfo oi, master.tbSupplierMaster ld where oi.vSupplierId = ld.vSupplierId and ld.iActive = 1 and"+
				" oi.vBranchId = '"+sessionBean.getBranchId()+"' and oi.iActive = 1 order by iCode asc";
		for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupName.addItem(element[0].toString());
			cmbSupName.setItemCaption(element[0].toString(), element[2].toString()+" - "+element[1].toString());
		}
	}

	private void checkRecPay()
	{
		String type = ogInvoiceType.getValue().toString();
		if (type.equals("Purchase"))
		{
			txtChequeNo.setVisible(false);
			cmbChequeNo.setVisible(true);
		}
		else
		{
			txtChequeNo.setVisible(true);
			cmbChequeNo.setVisible(false);
		}
	}

	private void checkReceipt()
	{
		String str = ogReceiptType.getValue().toString();
		if (str.equals("Cash"))
		{
			ogPaymentType.setValue(null);
			ogPaymentType.setEnabled(false);
			enableField(false, "");
		}
		else
		{
			ogPaymentType.setValue("Cheque");
			ogPaymentType.setEnabled(true);
			enableField(true, "This field is required");
		}
	}

	private void checkCheque()
	{
		String str = ogPaymentType.getValue() != null? ogPaymentType.getValue().toString():"";
		if (str.equals("Online"))
		{ enableField(false, ""); }
		else
		{ enableField(true, "This field is required"); }
	}

	private void selectAll()
	{
		if (tick)
		{tick = false;}
		else
		{tick = true;}
		for(int i = 0; i < tbLblPurchaseId.size(); i++)
		{
			if (!tbLblPurchaseId.get(i).getValue().toString().isEmpty())
			{ tbChkSelect.get(i).setValue(tick); }
		}
	}

	private void enableField(boolean bo, String msg)
	{
		txtChequeNo.setValue("");
		txtChequeNo.setRequired(bo);
		txtChequeNo.setRequiredError(msg);
		txtChequeNo.setEnabled(bo);
		cmbChequeNo.setRequired(bo);
		cmbChequeNo.setRequiredError(msg);
		cmbChequeNo.setEnabled(bo);
		txtChequeDate.setRequired(bo);
		txtChequeDate.setRequiredError(msg);
		txtChequeDate.setEnabled(bo);
	}

	private void setReceiptNo()
	{
		txtReceiptNo.setReadOnly(false);
		txtReceiptNo.setValue(pog.getReceiptNo(sessionBean.getBranchId(),
				txtReceiptDate.getValue(), ogInvoiceType.getValue().toString()));
		txtReceiptNo.setReadOnly(true);
	}

	private void setBalanceAmount(int ar)
	{
		tbTxtAmount.get(ar).setValue("");
		tbTxtAmount.get(ar).setEnabled(tbChkSelect.get(ar).getValue().booleanValue());
		if (!tbLblPurchaseNo.get(ar).getValue().toString().isEmpty())
		{
			if (tbChkSelect.get(ar).getValue().booleanValue())
			{
				double balance = Double.parseDouble(tbLblBalance.get(ar).getValue().toString().isEmpty()?"0":
					tbLblBalance.get(ar).getValue().toString().replaceAll(",", ""));
				tbTxtAmount.get(ar).setValue(cm.deciMn.format(balance));
			}
		}
		/*
		 * else { cm.showNotification("warning", "Warning!", "Invoice not found."); }
		 */
	}

	private void setRemainAmount(int ar)
	{
		double bal = Double.parseDouble(tbLblBalance.get(ar).getValue().toString().isEmpty()?"0":
			tbLblBalance.get(ar).getValue().toString().replaceAll(",", ""));
		double amt = Double.parseDouble(tbTxtAmount.get(ar).getValue().toString().isEmpty()?"0":
			tbTxtAmount.get(ar).getValue().toString().replaceAll(",", ""));

		if (amt <= bal)
		{
			tbLblRemain.get(ar).setValue(cm.deciMn.format(bal-amt));
			if (amt == 0)
			{ tbLblStatus.get(ar).setValue("Not Paid"); }
			else if ((bal-amt) == 0)
			{ tbLblStatus.get(ar).setValue("Full Paid"); }
			else
			{ tbLblStatus.get(ar).setValue("Partial"); }
		}
		else
		{
			tbTxtAmount.get(ar).setValue(bal);
			cm.showNotification("warning", "Warning!", "Balance limit exceeded.");
			tbTxtAmount.get(ar).focus();
		}
		totalAmount();
	}

	public double totalAmount()
	{
		double bala = 0, amnto = 0, reamino = 0, count = 0;
		for (int i=0; i<tbLblPurchaseId.size(); i++)
		{
			double bal = Double.parseDouble(tbLblBalance.get(i).getValue().toString().isEmpty()? "0":
				tbLblBalance.get(i).getValue().toString().replaceAll(",", ""));
			double amnt = Double.parseDouble(tbTxtAmount.get(i).getValue().toString().isEmpty()? "0":
				tbTxtAmount.get(i).getValue().toString().replaceAll(",", ""));
			double reamin = Double.parseDouble(tbLblRemain.get(i).getValue().toString().isEmpty()? "0":
				tbLblRemain.get(i).getValue().toString().replaceAll(",", ""));
			if (!tbLblPurchaseId.get(i).getValue().toString().isEmpty())
			{
				bala += bal;
				amnto += amnt;
				reamino += reamin;
				count++;
			}
		}
		tblPurchaseDetails.setColumnFooter("Balance", cm.deciMn.format(bala));
		tblPurchaseDetails.setColumnAlignment("Balance", Align.RIGHT);

		tblPurchaseDetails.setColumnFooter("Amount", cm.deciMn.format(amnto));
		tblPurchaseDetails.setColumnAlignment("Amount", Align.RIGHT);

		tblPurchaseDetails.setColumnFooter("Remain", cm.deciMn.format(reamino));
		tblPurchaseDetails.setColumnAlignment("Remain", Align.RIGHT);

		tblPurchaseDetails.setColumnFooter("Invoice No", "Total Invoice: "+cm.deciInt.format(count));
		return amnto;
	}

	public String getDeleteInsertDetailsSql(PurchaseReceiptModel pom)
	{
		String detailsSql = " update trans.tbReceiptPurchaseDetails set iActive = 0 where"+
				" vReceiptId = '"+pom.getReceiptId()+"'";

		int i = 0;
		for (int ar = 0; ar < tbLblPurchaseId.size(); ar++)
		{
			double amnt = Double.parseDouble(tbTxtAmount.get(ar).getValue().toString().isEmpty()?"0":
				tbTxtAmount.get(ar).getValue().toString().replaceAll(",", ""));
			if (amnt > 0)
			{
				if (i == 0)
				{
					detailsSql += " insert into trans.tbReceiptPurchaseDetails(vReceiptId,"+
							" vPurchaseId, mReceiptAmount, iActive) values ";
				}

				i = 1;
				detailsSql += " ('"+pom.getReceiptId()+"', '"+tbLblPurchaseId.get(ar).getValue().toString()+"',"+
						" '"+tbTxtAmount.get(ar).getValue().toString()+"', 1),";
			}
		}
		return detailsSql.substring(0, detailsSql.length()-1);
	}

	public void setValue(String receiptId)
	{
		String sql = "select *,(select top 1 vReceiptId from trans.tbReceiptPurchaseInfo vu where vu.vReceiptId = vReceiptId)" + 
				" vVoucherNo from (select rii.vReceiptFor, rii.vReceiptType, rii.vPaymentType, rii.vSupplierId, rii.vRecLedgerId," + 
				" rii.dReceiptDate, rii.vReceiptNo,  rii.vChequeNo, rii.vChequeDate, rid.vPurchaseId, rid.mReceiptAmount," + 
				" (SUM(oid.mNetAmount) - (select ISNULL(SUM(rid.mReceiptAmount), 0) from trans.tbReceiptPurchaseDetails rid" + 
				" where rid.vReceiptId != '"+receiptId+"' and rid.iActive = 1 and rid.vPurchaseId = oid.vPurchaseId)) mBalanceAmount"+
				" from trans.tbReceiptPurchaseInfo rii, trans.tbReceiptPurchaseDetails rid, trans.tbPurchaseInfo oii, trans.tbPurchaseDetails"+
				" oid where rii.vReceiptId = rid.vReceiptId and rid.vPurchaseId = oii.vPurchaseId and oii.vPurchaseId = oid.vPurchaseId"+
				" and rii.vReceiptId = '"+receiptId+"' and oid.iActive = 1 and rid.iActive = 1 group by rii.vReceiptFor, rii.vReceiptType,"+
				" rii.vPaymentType, rii.vSupplierId, rii.vRecLedgerId, rii.dReceiptDate, rii.vReceiptNo, rii.vChequeNo, rii.vChequeDate,"+
				" rid.vPurchaseId, oid.vPurchaseId, rid.mReceiptAmount) as tbTemp";
		//System.out.println(sql);
		int ar = 0, ft = 0;

		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			if (ft == 0)
			{
				ogInvoiceType.setValue(element[0].toString());
				ogReceiptType.setValue(element[1].toString());
				ogPaymentType.setValue(element[2].toString());
				cmbSupName.setValue(element[3].toString());
				txtReceiptDate.setValue((Date) element[5]);
				txtReceiptNo.setReadOnly(false);
				txtReceiptNo.setValue(element[6].toString());
				txtReceiptNo.setReadOnly(true);

				if (element[2].toString().equals("Cheque"))
				{
					txtChequeNo.setVisible(false);
					cmbChequeNo.setVisible(true);
					cmbChequeNo.addItem(element[7].toString());
					cmbChequeNo.setValue(element[7].toString());
					chequeNoOld = element[7].toString();
					try
					{
						txtChequeDate.setValue((Date) cm.dfBd.parse(element[8].toString()));
					}
					catch (ReadOnlyException e) { } catch (ConversionException e) { } catch (ParseException e) { }
				}
				ft = 1;
			}
			tableRowAdd(ar);

			tbChkSelect.get(ar).setValue(true);
			tbLblPurchaseId.get(ar).setValue(element[9].toString());
			setValueTable(ar, element[9].toString());//Others information
			tbLblBalance.get(ar).setValue(cm.deciMn.format(element[11]));
			tbTxtAmount.get(ar).setValue(Double.parseDouble(element[10].toString()));

			if (tbLblPurchaseId.size() <= ar)
			{ tableRowAdd(tbLblPurchaseId.size()); }
			ar++;
		}
		actionDetails = true;
	}

	private void setValueTable(int ar, String PurchaseId)
	{
		String sql = "select oii.vPurchaseNo, oii.dPurchaseDate, oii.vReferenceNo, COUNT(oid.vItemId) iCount from"+
				" trans.tbPurchaseInfo oii, trans.tbPurchaseDetails oid where oii.vPurchaseId = oid.vPurchaseId"+
				" and oii.vPurchaseId = '"+PurchaseId+"' and oid.iActive = 1 and oii.vBranchId = '"+sessionBean.getBranchId()+"'"+
				" group by oii.vPurchaseNo, oii.dPurchaseDate, oii.vReferenceNo, oii.vPurchaseId order by oii.dPurchaseDate desc";

		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tbLblPurchaseNo.get(ar).setValue(element[0].toString());
			tbLblPurchaseDate.get(ar).setValue(element[1].toString());
			tbLblPurchaseRef.get(ar).setValue(element[2].toString());
			tbLblNoOfItem.get(ar).setValue(element[3].toString());
		}
	}

	private void setPurchaseData()
	{
		tableClear();
		String supId = cmbSupName.getValue() != null? cmbSupName.getValue().toString():"%";
		String fromDate = txtFromDate.getValue() != null? cm.dfDb.format(txtFromDate.getValue()):"";
		String toDate = txtToDate.getValue() != null? cm.dfDb.format(txtToDate.getValue()):"";

		String sql = "select vPurchaseId, vPurchaseNo, dPurchaseDate, vReferenceNo, SUM(mBalanceQty) mTotalItem, SUM(mNetAmount)mNetAmount from"+
				" (select pui.vPurchaseId, pui.vPurchaseNo, pui.dPurchaseDate, pui.vReferenceNo, case when (pud.mQuantity - ISNULL(prd.mQuantity, 0)) > 0"+
				" then 1 else 0 end mBalanceQty, (pud.mNetAmount - ISNULL(prd.mNetAmount, 0)) - ISNULL(prc.mReceiptAmount, 0) mNetAmount from"+
				" trans.tbPurchaseInfo pui inner join trans.tbPurchaseDetails pud on pui.vPurchaseId = pud.vPurchaseId and pui.vStatusId = 'S6' and pui.iActive = 1 and"+
				" pud.iActive = 1 left join trans.tbPurchaseReturnInfo pri on pui.vPurchaseId = pri.vPurchaseId and pri.iActive = 1 left join"+
				" trans.tbPurchaseReturnDetails prd on pri.vReturnId = prd.vReturnId and pud.vItemId = prd.vItemId and prd.iActive = 1 left join"+
				" trans.tbReceiptPurchaseDetails prc on pui.vPurchaseId = prc.vPurchaseId and prc.iActive = 1  where pui.vSupplierId like '"+supId+"'"+
				" and pui.dPurchaseDate between '"+fromDate+"' and '"+toDate+"' and pui.vBranchId = '"+sessionBean.getBranchId()+"') as tbTemp where mNetAmount>0"+
				" group by vPurchaseId, vPurchaseNo, dPurchaseDate, vReferenceNo order by dPurchaseDate desc";

		int ar = 0;
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (tbLblPurchaseId.size() <= ar)
			{ tableRowAdd(tbLblPurchaseId.size()); }

			tbLblPurchaseId.get(ar).setValue(element[0].toString());
			tbLblPurchaseNo.get(ar).setValue(element[1].toString());
			tbLblPurchaseDate.get(ar).setValue(cm.dfBd.format(element[2]));
			tbLblPurchaseRef.get(ar).setValue(element[3].toString());
			tbLblNoOfItem.get(ar).setValue(element[4].toString());
			tbLblBalance.get(ar).setValue(cm.deciMn.format(element[5]));
			tbLblStatus.get(ar).setValue("Not Paid");
			tbLblRemain.get(ar).setValue(cm.deciMn.format(element[5]));
			ar++;
		}
		if (ar == 0)
		{ cm.showNotification("warning", "Warning!", "No invoice found."); }
	}

	private void removeRow(int ar)
	{
		if (!tbLblPurchaseId.get(ar).getValue().toString().isEmpty() && tbLblPurchaseId.size() > 0 &&
				!tbTxtAmount.get(ar).getValue().toString().isEmpty())
		{
			tbLblPurchaseId.get(ar).setValue("");
			tblPurchaseDetails.removeItem(ar);
			tbTxtAmount.get(ar).clear();
		}
		totalAmount();
	}

	public void txtClear()
	{
		cmbSupName.setValue(null);
		txtReceiptNo.setReadOnly(false);
		txtReceiptNo.setValue("");
		txtReceiptNo.setReadOnly(true);
		txtChequeNo.setValue("");
		tableClear();
	}

	public void tableClear()
	{
		tblPurchaseDetails.removeAllItems();
		tbLblPurchaseId.clear();
	}

	private void addChangesDetails()
	{
		if (flag.equals("Edit") && actionDetails)
		{ changesDetails = true; }
	}

	private Component buildComponent()
	{
		GridLayout glayout = new GridLayout(7, 4);
		glayout.setSpacing(true);
		glayout.setMargin(true);
		glayout.setSizeFull();
		glayout.setWidth("100%");

		ogInvoiceType = new OptionGroup();
		ogInvoiceType.setImmediate(true);
		ogInvoiceType.setWidth("-1px");
		ogInvoiceType.addItem("Purchase");
		ogInvoiceType.setValue("Purchase");
		ogInvoiceType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogInvoiceType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		glayout.addComponent(new Label("Receipt For: "), 0, 0);
		glayout.addComponent(ogInvoiceType, 1, 0);

		ogReceiptType = new OptionGroup();
		ogReceiptType.setImmediate(true);
		ogReceiptType.setWidth("-1px");
		ogReceiptType.addItem("Bank");
		ogReceiptType.addItem("Cash");
		ogReceiptType.setValue("Bank");
		ogReceiptType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReceiptType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		glayout.addComponent(new Label("Payment Type: "), 0, 1);
		glayout.addComponent(ogReceiptType, 1, 1);

		ogPaymentType = new OptionGroup();
		ogPaymentType.setImmediate(true);
		ogPaymentType.setWidth("-1px");
		ogPaymentType.addItem("Cheque");
		ogPaymentType.addItem("Online");
		ogPaymentType.setValue("Cheque");
		ogPaymentType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogPaymentType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		Label lbll = new Label("Payment Mode: ");
		lbll.setWidth("-1px");
		glayout.addComponent(lbll, 0, 2);
		glayout.addComponent(ogPaymentType, 1, 2);

		txtFromDate = new PopupDateField();
		txtFromDate.setImmediate(true);
		txtFromDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDate.setValue(new Date());
		txtFromDate.setWidth("105px");
		txtFromDate.setDateFormat("dd-MM-yyyy");
		txtFromDate.setDescription("From Date(To search list of Purchase)");
		txtFromDate.setInputPrompt("From Date");
		txtFromDate.setRequired(true);
		txtFromDate.setRequiredError("This field is required");
		Label ll = new Label("Purchase Date(Search): ");
		ll.setWidth("-1px");
		glayout.addComponent(ll, 2, 0);
		glayout.addComponent(txtFromDate, 3, 0);

		txtToDate = new PopupDateField();
		txtToDate.setImmediate(true);
		txtToDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDate.setValue(new Date());
		txtToDate.setWidth("105px");
		txtToDate.setDateFormat("dd-MM-yyyy");
		txtToDate.setDescription("To Date(To search list of Purchase)");
		txtToDate.setInputPrompt("To Date");
		txtToDate.setRequired(true);
		txtToDate.setRequiredError("This field is required");
		glayout.addComponent(txtToDate, 4, 0);

		cmbSupName = new ComboBox();
		cmbSupName.setImmediate(true);
		cmbSupName.setFilteringMode(FilteringMode.CONTAINS);
		cmbSupName.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbSupName.setWidth("100%");
		cmbSupName.setRequired(true);
		cmbSupName.setRequiredError("This field is required");
		cmbSupName.setInputPrompt("Select Supplier Name");
		lblSupCus = new Label("Supplier Name: ");
		lblSupCus.setWidth("-1px");
		glayout.addComponent(lblSupCus, 2, 1);
		glayout.addComponent(cmbSupName, 3, 1, 4, 1);

		txtReceiptDate = new PopupDateField();
		txtReceiptDate.setImmediate(true);
		txtReceiptDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtReceiptDate.setValue(new Date());
		txtReceiptDate.setWidth("105px");
		txtReceiptDate.setDateFormat("dd-MM-yyyy");
		txtReceiptDate.setDescription("Receipt Date");
		txtReceiptDate.setInputPrompt("Receipt Date");
		txtReceiptDate.setRequired(true);
		txtReceiptDate.setRequiredError("This field is required");
		Label llb = new Label("Receipt Date: ");
		llb.setWidth("-1px");
		glayout.addComponent(llb, 5, 0);
		glayout.addComponent(txtReceiptDate, 6, 0);

		txtReceiptNo = new TextField();
		txtReceiptNo.setImmediate(true);
		txtReceiptNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReceiptNo.setWidth("-1px");
		txtReceiptNo.setDescription("Receipt number may not be same after saved.");
		txtReceiptNo.setInputPrompt("Receipt No");
		txtReceiptNo.setReadOnly(true);
		txtReceiptNo.setRequired(true);
		txtReceiptNo.setRequiredError("This field is required");
		Label llR = new Label("Receipt No: ");
		llR.setWidth("-1px");
		glayout.addComponent(llR, 5, 1);
		glayout.addComponent(txtReceiptNo, 6, 1);

		CssLayout groupl = new CssLayout();
		groupl.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		txtChequeNo = new TextField();
		txtChequeNo.setImmediate(true);
		txtChequeNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtChequeNo.setWidth("-1px");
		txtChequeNo.setDescription("Cheque Number");
		txtChequeNo.setInputPrompt("Cheque Number");
		txtChequeNo.setRequired(true);
		txtChequeNo.setRequiredError("This field is required");

		cmbChequeNo = new ComboBox();
		cmbChequeNo.setImmediate(true);
		cmbChequeNo.setFilteringMode(FilteringMode.CONTAINS);
		cmbChequeNo.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbChequeNo.setWidth("-1px");
		cmbChequeNo.setRequired(true);
		cmbChequeNo.setVisible(false);
		cmbChequeNo.setRequiredError("This field is required");
		cmbChequeNo.setDescription("Cheque Number");
		cmbChequeNo.setInputPrompt("Select Cheque Number");

		txtChequeDate = new PopupDateField();
		txtChequeDate.setImmediate(true);
		txtChequeDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtChequeDate.setValue(new Date());
		txtChequeDate.setWidth("105px");
		txtChequeDate.setDateFormat("dd-MM-yyyy");
		txtChequeDate.setDescription("Cheque Date");
		txtChequeDate.setInputPrompt("Cheque Date");
		Label llChq = new Label("Cheque Details: ");
		llChq.setWidth("-1px");
		glayout.addComponent(llChq, 2, 2);
		groupl.addComponents(txtChequeNo, cmbChequeNo, txtChequeDate);
		glayout.addComponent(groupl, 3, 2, 4, 2);

		buildTable();
		glayout.addComponent(tblPurchaseDetails, 0, 3, 6, 3);
		return glayout;
	}

	private void buildTable()
	{
		tblPurchaseDetails = new Table();
		tblPurchaseDetails.setFooterVisible(true);
		tblPurchaseDetails.setSelectable(true);
		tblPurchaseDetails.setColumnCollapsingAllowed(true);
		tblPurchaseDetails.addStyleName(ValoTheme.TABLE_SMALL);
		tblPurchaseDetails.setWidth("100%");
		tblPurchaseDetails.setRowHeaderMode(RowHeaderMode.INDEX);
		tblPurchaseDetails.setPageLength(7);

		tblPurchaseDetails.addContainerProperty("Purchase Id", Label.class, new Label(), null, null, Align.CENTER);
		tblPurchaseDetails.setColumnCollapsed("Purchase Id", true);

		tblPurchaseDetails.addContainerProperty("Select", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblPurchaseDetails.addContainerProperty("Purchase No", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseDetails.addContainerProperty("Purchase Date", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseDetails.addContainerProperty("Purchase Reference", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseDetails.addContainerProperty("Total Item", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseDetails.addContainerProperty("Balance", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseDetails.addContainerProperty("Amount", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPurchaseDetails.setColumnWidth("Amount", 120);

		tblPurchaseDetails.addContainerProperty("Remain", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseDetails.addContainerProperty("Status", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseDetails.addContainerProperty("Rem", Button.class, new Button(), null, null, Align.CENTER);
		tblPurchaseDetails.setColumnWidth("Rem", 50);
	}

	public void tableRowAdd(final int ar)
	{
		try
		{
			tbLblPurchaseId.add(ar, new Label());
			tbLblPurchaseId.get(ar).setWidth("100%");
			tbLblPurchaseId.get(ar).setImmediate(true);
			tbLblPurchaseId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbChkSelect.add(ar, new CheckBox());
			tbChkSelect.get(ar).setWidth("100%");
			tbChkSelect.get(ar).setImmediate(true);
			tbChkSelect.get(ar).setDescription("Click table header '"+"Select"+"' to select all.");
			tbChkSelect.get(ar).addStyleName(ValoTheme.COMBOBOX_TINY);
			tbChkSelect.get(ar).addValueChangeListener(event ->
			{
				setBalanceAmount(ar);
				tbTxtAmount.get(ar).focus();
				addChangesDetails();
			});

			tbLblPurchaseNo.add(ar, new Label());
			tbLblPurchaseNo.get(ar).setWidth("100%");
			tbLblPurchaseNo.get(ar).setImmediate(true);
			tbLblPurchaseNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblPurchaseDate.add(ar, new Label());
			tbLblPurchaseDate.get(ar).setWidth("100%");
			tbLblPurchaseDate.get(ar).setImmediate(true);
			tbLblPurchaseDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblPurchaseRef.add(ar, new Label());
			tbLblPurchaseRef.get(ar).setWidth("100%");
			tbLblPurchaseRef.get(ar).setImmediate(true);
			tbLblPurchaseRef.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblNoOfItem.add(ar, new Label());
			tbLblNoOfItem.get(ar).setWidth("100%");
			tbLblNoOfItem.get(ar).setImmediate(true);
			tbLblNoOfItem.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblBalance.add(ar, new Label());
			tbLblBalance.get(ar).setWidth("100%");
			tbLblBalance.get(ar).setImmediate(true);
			tbLblBalance.get(ar).addStyleName(ValoTheme.LABEL_TINY);
			tbLblBalance.get(ar).addStyleName(ValoTheme.TEXTAREA_ALIGN_RIGHT);

			tbTxtAmount.add(ar, new CommaField());
			tbTxtAmount.get(ar).setWidth("100%");
			tbTxtAmount.get(ar).setImmediate(true);
			tbTxtAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbTxtAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbTxtAmount.get(ar).setEnabled(false);
			tbTxtAmount.get(ar).addValueChangeListener(event ->
			{
				setRemainAmount(ar);
				addChangesDetails();
			});

			tbLblRemain.add(ar, new Label());
			tbLblRemain.get(ar).setWidth("100%");
			tbLblRemain.get(ar).setImmediate(true);
			tbLblRemain.get(ar).addStyleName(ValoTheme.LABEL_TINY);
			tbLblRemain.get(ar).addStyleName(ValoTheme.TEXTAREA_ALIGN_RIGHT);

			tbLblStatus.add(ar, new Label());
			tbLblStatus.get(ar).setWidth("100%");
			tbLblStatus.get(ar).setImmediate(true);
			tbLblStatus.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbBtnRemove.add(ar, new Button(""));
			tbBtnRemove.get(ar).setWidth("100%");
			tbBtnRemove.get(ar).setImmediate(true);
			tbBtnRemove.get(ar).setIcon(FontAwesome.REMOVE);
			tbBtnRemove.get(ar).setStyleName(ValoTheme.BUTTON_TINY);
			tbBtnRemove.get(ar).addStyleName(ValoTheme.BUTTON_DANGER);
			tbBtnRemove.get(ar).setDescription("Remove");
			tbBtnRemove.get(ar).addClickListener(event -> removeRow(ar));

			tblPurchaseDetails.addItem(new Object[]{tbLblPurchaseId.get(ar), tbChkSelect.get(ar), tbLblPurchaseNo.get(ar),
					tbLblPurchaseDate.get(ar), tbLblPurchaseRef.get(ar), tbLblNoOfItem.get(ar), tbLblBalance.get(ar),
					tbTxtAmount.get(ar), tbLblRemain.get(ar), tbLblStatus.get(ar), tbBtnRemove.get(ar)}, ar);
		}
		catch (Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}
}
