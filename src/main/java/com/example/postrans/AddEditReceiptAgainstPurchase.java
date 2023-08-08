package com.example.postrans;

import java.util.ArrayList;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.ReceiptAgainstPurcGateway;
import com.example.model.PurchaseReceiptModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;

@SuppressWarnings("serial")
public class AddEditReceiptAgainstPurchase extends Window
{
	private SessionBean sessionBean;
	private String flag, receiptId = "";

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private ReceiptAgainstPurcGateway pog = new ReceiptAgainstPurcGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	//All tabs classes
	private TabSheet tsReceiptPurchase = new TabSheet();
	private TabReceiptAgainstPurchase tabReceiptPurchase;
	private CommonMethod cm;

	public AddEditReceiptAgainstPurchase(SessionBean sessionBean, String flag, String receiptId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.receiptId = receiptId;
		this.setCaption(flag+" Receipt Against Purchase :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);
		setWidth("100%");
		setHeight("600px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event -> addValidation());

		cBtn.btnExit.addClickListener(event -> close());

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void addValidation()
	{
		if (tabReceiptPurchase.cmbSupName.getValue() != null)
		{
			/*if (!cm.checkYearClosed(tabReceiptPurchase.txtReceiptDate.getValue()))
				{*/
			if (tabReceiptPurchase.ogReceiptType.getValue().toString().equals("Cash") ||
					tabReceiptPurchase.ogPaymentType.getValue().toString().equals("Online") || checkBankCheque())
			{
				if (tabReceiptPurchase.totalAmount() > 0)
				{ insertEditData(); }
				else
				{
					tsReceiptPurchase.setSelectedTab(0);
					if (tabReceiptPurchase.tbChkSelect.size()>0)
					{ tabReceiptPurchase.tbChkSelect.get(0).focus(); }
					cm.showNotification("warning", "Warning!", "Provide data in the table.");
				}
			}
			else
			{				
				tsReceiptPurchase.setSelectedTab(0);
				if (tabReceiptPurchase.ogInvoiceType.getValue().toString().equals("Purchase")) {tabReceiptPurchase.cmbChequeNo.focus();}
				else {tabReceiptPurchase.txtChequeNo.focus();}
				cm.showNotification("warning", "Warning!", "Provide cheque number.");				
			}
			/*}
				else
				{
					tsReceiptPurchase.setSelectedTab(0);
					tabReceiptPurchase.txtReceiptDate.focus();
					cm.showNotification("warning", "Warning!", "Select valid date within financial year.");
				}*/
		}
		else
		{
			tsReceiptPurchase.setSelectedTab(0);
			tabReceiptPurchase.cmbSupName.focus();
			cm.showNotification("warning", "Warning!", "Select Supplier name.");
		}
	}

	private boolean checkBankCheque()
	{
		boolean ret = false;
		if (tabReceiptPurchase.ogPaymentType.getValue().toString().equals("Cheque"))
		{
			if (tabReceiptPurchase.ogInvoiceType.getValue().toString().equals("Purchase"))
			{
				/*if (tabReceiptPurchase.cmbChequeNo.getValue() != null)
				{ ret = true; }*/
				if (!tabReceiptPurchase.txtChequeNo.getValue().toString().isEmpty())
				{ ret = true; }
			}
			else
			{
				if (!tabReceiptPurchase.txtChequeNo.getValue().toString().isEmpty())
				{ ret = true; }
			}
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
		try
		{
			PurchaseReceiptModel rpm = new PurchaseReceiptModel();
			getInvoiceData(rpm);

			if (pog.insertEditReceiptPurchase(rpm, flag))
			{
				txtClear();
				tabReceiptPurchase.totalAmount();
				cm.showNotification("success", "Successfull!", "All information saved successfully.");
				cBtn.btnSave.setEnabled(true);

				//Report
				ReceiptAgainstPurchase rai = new ReceiptAgainstPurchase(sessionBean, "");
				rai.viewReportMoneyReceipt(rpm.getReceiptId());

				if (flag.equals("Edit"))
				{ close(); }
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private void getInvoiceData(PurchaseReceiptModel rpm)
	{
		String receiptIdN = flag.equals("Add")?pog.getReceiptId(sessionBean.getBranchId()):receiptId;
		String receiptNoN = flag.equals("Add")?pog.getReceiptNo(sessionBean.getBranchId(),
				tabReceiptPurchase.txtReceiptDate.getValue(), tabReceiptPurchase.ogInvoiceType.getValue().toString())
				:tabReceiptPurchase.txtReceiptNo.getValue().toString();
		rpm.setBranchId(sessionBean.getBranchId());

		rpm.setReceiptId(receiptIdN);
		rpm.setReceiptNo(receiptNoN);
		rpm.setReceiptFor(tabReceiptPurchase.ogInvoiceType.getValue().toString());
		rpm.setReceiptType(tabReceiptPurchase.ogReceiptType.getValue().toString());
		rpm.setPayType(tabReceiptPurchase.ogPaymentType.getValue() != null? tabReceiptPurchase.ogPaymentType.getValue().toString():"");
		rpm.setSupCusId(tabReceiptPurchase.cmbSupName.getValue().toString());
		rpm.setReceiptDate(tabReceiptPurchase.txtReceiptDate.getValue());

		String chqNo = rpm.getPayType().equals("Cheque")?tabReceiptPurchase.txtChequeNo.getValue().toString():"";
		rpm.setChequeNo(chqNo);
		rpm.setChequeDate(chqNo.isEmpty()?"":cm.dfBd.format(tabReceiptPurchase.txtChequeDate.getValue()));

		rpm.setStatusId("S5");
		rpm.setApproveBy("");
		rpm.setCancelBy("");
		rpm.setCancelReason("");
		rpm.setCreatedBy("");
		rpm.setLedgerId("0");
		rpm.setCreatedBy(sessionBean.getUserId());

		//Details SQL
		rpm.setDetailsSql(tabReceiptPurchase.getDeleteInsertDetailsSql(rpm));
		rpm.setOrderChangeDetails(tabReceiptPurchase.changesDetails);
	}

	private void setEditData()
	{
		tabReceiptPurchase.setValue(receiptId);
		tabReceiptPurchase.txtFromDate.setEnabled(false);
		tabReceiptPurchase.txtToDate.setEnabled(false);
		tabReceiptPurchase.cmbSupName.setEnabled(false);
	}

	private VerticalLayout buildLayout()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();

		tsReceiptPurchase.setStyleName("framed padded-tabbar");
		tabReceiptPurchase = new TabReceiptAgainstPurchase(sessionBean, flag, receiptId);
		tsReceiptPurchase.addTab(tabReceiptPurchase, "Receive Against Purchase", FontAwesome.INFO, 0);

		layout.addComponents(tsReceiptPurchase, cBtn);
		layout.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return layout;
	}

	private void focusEnter()
	{
		allComp.add(tabReceiptPurchase.ogInvoiceType);
		allComp.add(tabReceiptPurchase.ogReceiptType);
		allComp.add(tabReceiptPurchase.ogPaymentType);
		allComp.add(tabReceiptPurchase.txtFromDate);
		allComp.add(tabReceiptPurchase.txtToDate);
		allComp.add(tabReceiptPurchase.cmbSupName);
		allComp.add(tabReceiptPurchase.txtReceiptDate);
		allComp.add(tabReceiptPurchase.txtChequeNo);
		allComp.add(tabReceiptPurchase.cmbChequeNo);
		allComp.add(tabReceiptPurchase.txtChequeDate);

		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{ tabReceiptPurchase.txtClear(); }
}
