package com.example.possetup;

import java.util.ArrayList;

import com.common.share.CommaField;
import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.SupplierInfoGateway;
import com.example.model.SupplierInfoModel;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditSupplierInfo extends Window
{
	private SessionBean sessionBean;
	private String flag, supplierId;

	private TextField txtSupplierCode, txtSupplierName, txtPhone, txtFax, txtEmail,
	txtLicense, txtVatRegNo;
	private TextArea txtAddress;
	private CommaField txtCreditDays, txtDisPercent;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private SupplierInfoGateway sig = new SupplierInfoGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private CommonMethod cm;

	public AddEditSupplierInfo(SessionBean sessionBean, String flag, String supplierId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.supplierId = supplierId;
		this.setCaption(flag+" Supplier Information :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(this.sessionBean);
		setWidth("550px");
		setHeight("580px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
		{ addValidation(); });

		cBtn.btnExit.addClickListener(event ->
		{ close(); });

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void addValidation()
	{
		if (!sig.checkExistCode(txtSupplierCode.getValue().toString().trim(), supplierId))
		{
			if (!txtSupplierName.getValue().toString().trim().isEmpty())
			{
				if (!sig.checkExist(txtSupplierName.getValue().toString().trim(), supplierId))
				{
					cBtn.btnSave.setEnabled(false);
					insertEditData();
				}
				else
				{
					txtSupplierName.focus();
					cm.showNotification("warning", "Warning!", "Supplier name already exist.");
				}
			}
			else
			{
				txtSupplierName.focus();
				cm.showNotification("warning", "Warning!", "Provide supplier Code.");
			}
		}
		else
		{
			txtSupplierCode.focus();
			cm.showNotification("warning", "Warning!", "Supplier code already exist.");
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
					{
						SupplierInfoModel sim = new SupplierInfoModel();
						String supplierIdN = flag.equals("Add")?sig.getSupplierId():supplierId;
						sim.setBranchId(sessionBean.getBranchId());
						sim.setSupplierId(supplierIdN);
						sim.setSupplierName(txtSupplierName.getValue().toString().trim());
						sim.setSupplierCode(txtSupplierCode.getValue().toString().trim());
						sim.setAddress(txtAddress.getValue().toString().trim());
						sim.setPhone(txtPhone.getValue().toString().trim());
						sim.setFax(txtFax.getValue().toString().trim());
						sim.setEmail(txtEmail.getValue().toString().trim());
						sim.setLicense(txtLicense.getValue().toString().trim());
						sim.setVatRegNo(txtVatRegNo.getValue().toString().trim());
						sim.setCreditLimit(txtCreditDays.getValue().toString().trim());
						sim.setDiscount(cm.getAmtValue(txtDisPercent));
						sim.setCreatedBy(sessionBean.getUserId());
						if (sig.insertEditData(sim, flag))
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
					catch(Exception ex)
					{ System.out.println(ex); }
				}
				else if (buttonType == ButtonType.NO)
				{ cBtn.btnSave.setEnabled(true); }
			}
		});
	}

	private void setEditData()
	{
		SupplierInfoModel sim = new SupplierInfoModel();
		try
		{
			if (sig.selectEditData(sim, supplierId))
			{
				txtSupplierCode.setValue(sim.getSupplierCode());
				txtSupplierName.setValue(sim.getSupplierName());
				txtAddress.setValue(sim.getAddress());
				txtPhone.setValue(sim.getPhone());
				txtFax.setValue(sim.getFax());
				txtEmail.setValue(sim.getEmail());
				txtLicense.setValue(sim.getLicense());
				txtVatRegNo.setValue(sim.getVatRegNo());
				txtCreditDays.setValue(sim.getCreditLimit());
				txtDisPercent.setValue(sim.getDiscount());
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(10, 11);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtSupplierCode = new TextField();
		txtSupplierCode.setImmediate(true);
		txtSupplierCode.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtSupplierCode.setWidth("100%");
		txtSupplierCode.setInputPrompt("Supplier Code");
		txtSupplierCode.setValue(sig.getSupCode());
		grid.addComponent(new Label("Supplier Code: "), 0, 0);
		grid.addComponent(txtSupplierCode, 1, 0, 4, 0);

		txtSupplierName = new TextField();
		txtSupplierName.setImmediate(true);
		txtSupplierName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtSupplierName.setWidth("100%");
		txtSupplierName.setInputPrompt("Supplier Name");
		txtSupplierName.setRequired(true);
		txtSupplierName.setRequiredError("This field is required");
		grid.addComponent(new Label("Supplier Name: "), 0, 1);
		grid.addComponent(txtSupplierName, 1, 1, 9, 1);

		txtAddress = new TextArea();
		txtAddress.setImmediate(true);
		txtAddress.addStyleName(ValoTheme.TEXTAREA_TINY);
		txtAddress.setWidth("100%");
		txtAddress.setHeight("90px");
		txtAddress.setInputPrompt("Address");
		grid.addComponent(new Label("Address: "), 0, 2);
		grid.addComponent(txtAddress, 1, 2, 9, 2);

		txtPhone = new TextField();
		txtPhone.setImmediate(true);
		txtPhone.setWidth("100%");
		txtPhone.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPhone.setInputPrompt("Phone");
		grid.addComponent(new Label("Phone: "), 0, 3);
		grid.addComponent(txtPhone, 1, 3, 9, 3);

		txtFax = new TextField();
		txtFax.setImmediate(true);
		txtFax.setWidth("100%");
		txtFax.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFax.setInputPrompt("Fax");
		grid.addComponent(new Label("Fax: "), 0, 4);
		grid.addComponent(txtFax, 1, 4, 9, 4);

		txtEmail = new TextField();
		txtEmail.setImmediate(true);
		txtEmail.setWidth("100%");
		txtEmail.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtEmail.setInputPrompt("sample@email.com");
		grid.addComponent(new Label("Email: "), 0, 5);
		grid.addComponent(txtEmail, 1, 5, 9, 5);

		txtLicense = new TextField();
		txtLicense.setImmediate(true);
		txtLicense.setWidth("100%");
		txtLicense.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtLicense.setInputPrompt("CR");
		grid.addComponent(new Label("CR No: "), 0, 6);
		grid.addComponent(txtLicense, 1, 6, 9, 6);

		txtVatRegNo = new TextField();
		txtVatRegNo.setImmediate(true);
		txtVatRegNo.setWidth("100%");
		txtVatRegNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtVatRegNo.setInputPrompt("VAT Registration No");
		grid.addComponent(new Label("VAT Reg No: "), 0, 7);
		grid.addComponent(txtVatRegNo, 1, 7, 9, 7);

		txtCreditDays = new CommaField();
		txtCreditDays.setImmediate(true);
		txtCreditDays.setWidth("100%");
		txtCreditDays.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCreditDays.setInputPrompt("Credit Limit");
		grid.addComponent(new Label("Credit Limit: "), 0, 8);
		grid.addComponent(txtCreditDays, 1, 8, 3, 8);
		grid.addComponent(new Label("(Days)"), 4, 8);

		txtDisPercent = new CommaField();
		txtDisPercent.setImmediate(true);
		txtDisPercent.setWidth("100%");
		txtDisPercent.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDisPercent.setInputPrompt("Discount Percentage");
		Label lbl = new Label("Discount Percent: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 9);
		grid.addComponent(txtDisPercent, 1, 9, 3, 9);

		grid.addComponent(cBtn, 0, 10, 9, 10);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return grid;
	}

	private void focusEnter()
	{
		allComp.add(txtSupplierCode);
		allComp.add(txtSupplierName);
		allComp.add(txtAddress);
		allComp.add(txtPhone);
		allComp.add(txtFax);
		allComp.add(txtEmail);
		allComp.add(txtLicense);
		allComp.add(txtVatRegNo);
		allComp.add(txtCreditDays);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		txtSupplierCode.setValue(sig.getSupCode());
		txtSupplierName.setValue("");
		txtAddress.setValue("");
		txtPhone.setValue("");
		txtFax.setValue("");
		txtEmail.setValue("");
		txtLicense.setValue("");
		txtVatRegNo.setValue("");
	}
}
