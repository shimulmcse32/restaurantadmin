package com.example.postrans;

import java.util.ArrayList;
import java.util.Date;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.PurchaseInfoGateway;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class OrderToPurchase extends Window
{
	private SessionBean sessionBean;
	private String primaryId;

	private PurchaseInfoGateway pig = new PurchaseInfoGateway();

	private PopupDateField txtPurchaseDate;
	private TextField txtReferenceNo;
	private TextArea txtRemark;
	private OptionGroup ogPaymentType;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private ArrayList<Component> allComp = new ArrayList<Component>();
	private CommonMethod cm;

	public OrderToPurchase(SessionBean sessionBean, String primaryId)
	{
		this.sessionBean = sessionBean;
		this.primaryId = primaryId;
		this.setCaption("Add Purchase :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);

		setWidth("500px");
		setHeight("315px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{ addValidation(); }
		});

		cBtn.btnExit.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{ close(); }
		});
		focusEnter();
	}

	private void addValidation()
	{
		if (txtPurchaseDate.getValue() != null)
		{
			if (!txtRemark.getValue().toString().trim().isEmpty())
			{
				cBtn.btnSave.setEnabled(false);
				insertEditData();
			}
			else
			{
				txtRemark.focus();
				cm.showNotification("warning", "Warning!", "Provide remarks.");
			}
		}
		else
		{
			txtPurchaseDate.focus();
			cm.showNotification("warning", "Warning!", "Select purchase date.");
		}
	}

	private void insertEditData()
	{
		MessageBox mb = new MessageBox(getUI(), "Are you sure?", MessageBox.Icon.QUESTION, "Do you want to save information?",
				new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
				new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
		mb.show(new EventListener()
		{
			public void buttonClicked(ButtonType buttonType)
			{
				if(buttonType == ButtonType.YES)
				{
					try
					{ saveInformation(); }
					catch(Exception ex)
					{ System.out.println(ex); }
				}
				else if(buttonType == ButtonType.NO)
				{ cBtn.btnSave.setEnabled(true); }
			}
		});
	}

	private void saveInformation()
	{
		if(SavePurchase(primaryId))
		{
			cm.showNotification("success", "Successfull!", "All information saved successfully.");
			cBtn.btnSave.setEnabled(true);
			close();
		}
		else
		{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
	}

	public boolean SavePurchase(String orderIds)
	{
		boolean ret = false;

		String PurchaseId = "", PurchaseNo = "", Purchasedate = "", EntryBy = "",
				ReferenceNo = "", PaymentType = "", Remark = "";
		PurchaseId = pig.getPurchaseId(sessionBean.getBranchId());
		PurchaseNo = pig.getPurchaseNo(cm.dfDb.format(txtPurchaseDate.getValue()));
		Purchasedate = cm.dfDb.format(txtPurchaseDate.getValue());
		EntryBy = sessionBean.getUserId();
		ReferenceNo = txtReferenceNo.getValue().toString().replaceAll("'", "''").trim();
		PaymentType = ogPaymentType.getValue().toString();
		Remark = txtRemark.getValue().toString().replaceAll("'", "''").trim();

		String sqlInfo = "insert into trans.tbPurchaseInfo (vBranchId, vPurchaseId, vPurchaseNo, vPurchaseType, vSupplierId,"+ 
				" vOrderId, dPurchaseDate, vReferenceNo, dDeliveryDate, vRemarks, vStatusId, vApprovedBy, vApproveTime,"+
				" vCancelledBy, vCancelledTime, vCancelReason, iInvoiceType, iActive, vCreatedBy, dCreatedDate, vModifiedBy,"+
				" dModifiedDate) select vBranchId, '"+PurchaseId+"' , '"+PurchaseNo+"' , '"+PaymentType+"', vSupplierId,"+
				" vOrderId, '"+Purchasedate+"', '"+ReferenceNo+"', '"+Purchasedate+"', '"+Remark+"', 'S6', '"+EntryBy+"', "+
				" getdate(), '', '', '', 0, 1, '"+EntryBy+"', getdate(), '"+EntryBy+"', getdate() FROM trans.tbPurchaseOrderInfo"+
				" where vOrderId like '"+orderIds+"'";

		String sqlDetails = " insert into trans.tbPurchaseDetails (vPurchaseId, vOrderId, vItemId, vDescription, vUnitName,"+
				" vVatCatId, vVatOption, mVatPercent, mQuantity, mUnitRate, mDiscount, mAmount, mVatAmount, mNetAmount, iActive)"+
				" select '"+PurchaseId+"' PurchaseId, vOrderId, vItemId, vDescription, vUnitName, vVatCatId, vVatOption, mVatPercent,"+
				" mQuantity, mUnitRate, mDiscount, mAmount, mVatAmount, mNetAmount, iActive FROM trans.tbPurchaseOrderDetails"+
				" where vOrderId like '"+orderIds+"'";

		String sqlApprove = " update trans.tbPurchaseOrderInfo set vStatusId = 'S6', vApprovedBy = '"+EntryBy+"',"+
				" vApproveTime = getdate(), vModifiedBy = '"+EntryBy+"', dModifiedDate = getDate() where vOrderId"+
				" like '"+orderIds+"'";

		if (pig.insertData(sqlInfo+sqlDetails+sqlApprove))
		{ ret = true; }

		return ret;
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(11, 5);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		ogPaymentType = new OptionGroup();
		ogPaymentType.addItem("Cash");
		ogPaymentType.addItem("Credit");
		ogPaymentType.select("Cash");
		ogPaymentType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		ogPaymentType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		Label lblty = new Label("Payment Type: ");
		lblty.setWidth("120px");
		grid.addComponent(lblty, 0, 0);
		grid.addComponent(ogPaymentType, 1, 0, 10, 0);

		txtPurchaseDate = new PopupDateField();
		txtPurchaseDate.setImmediate(true);
		txtPurchaseDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtPurchaseDate.setValue(new Date());
		txtPurchaseDate.setWidth("110px");
		txtPurchaseDate.setDateFormat("dd-MM-yyyy");
		txtPurchaseDate.setDescription("Purchase Date");
		txtPurchaseDate.setInputPrompt("Purchase Date");
		txtPurchaseDate.setRequired(true);
		txtPurchaseDate.setRequiredError("This field is required");
		grid.addComponent(new Label("Purchase Date: "), 0, 1);
		grid.addComponent(txtPurchaseDate, 1, 1, 10, 1);

		txtReferenceNo = new TextField();
		txtReferenceNo.setImmediate(true);
		txtReferenceNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReferenceNo.setWidth("100%");
		txtReferenceNo.setInputPrompt("Reference No");
		Label lbl = new Label("Reference No: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 2);
		grid.addComponent(txtReferenceNo, 1, 2, 10, 2);

		txtRemark = new TextArea();
		txtRemark.setImmediate(true);
		txtRemark.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtRemark.setWidth("100%");
		txtRemark.setHeight("56px");
		txtRemark.setInputPrompt("Remarks");
		txtRemark.setRequired(true);
		txtRemark.setRequiredError("This field is required");
		grid.addComponent(new Label("Remarks: "), 0, 3);
		grid.addComponent(txtRemark, 1, 3, 10, 3);

		grid.addComponent(cBtn, 0, 4, 10, 4);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);
		return grid;
	}

	private void focusEnter()
	{
		allComp.add(ogPaymentType);
		allComp.add(txtPurchaseDate);
		allComp.add(txtReferenceNo);
		allComp.add(txtRemark);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}
}
