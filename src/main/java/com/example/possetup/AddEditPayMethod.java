package com.example.possetup;

import java.util.ArrayList;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.ItemUnitGateway;
import com.example.model.ItemUnitModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditPayMethod extends Window
{
	private SessionBean sessionBean;
	private String flag, payId = "";

	private TextField txtPaymentDetails;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private ItemUnitGateway iug = new ItemUnitGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();
	private CommonMethod cm;

	public AddEditPayMethod(SessionBean sessionBean, String flag, String payId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.payId = payId;
		this.setCaption(flag+" Payment Information :: "+this.sessionBean.getCompanyName());
		cm = new CommonMethod(sessionBean);
		setWidth("500px");
		setHeight("200px");

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
		if (!txtPaymentDetails.getValue().toString().trim().isEmpty())
		{
			if (!iug.checkExistPay(txtPaymentDetails.getValue().toString().trim(), payId))
			{
				cBtn.btnSave.setEnabled(false);
				insertEditData();
			}
			else
			{
				txtPaymentDetails.focus();
				cm.showNotification("warning", "Warning!", "Payment Method already exist.");
			}
		}
		else
		{
			txtPaymentDetails.focus();
			cm.showNotification("warning", "Warning!", "Provide payment method details.");
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
						ItemUnitModel ium = new ItemUnitModel();
						String payIdN = flag.equals("Add")?iug.getPayId():payId;
						ium.setUnitId(payIdN);
						ium.setUnitName(txtPaymentDetails.getValue().toString().trim());
						ium.setCreatedBy(sessionBean.getUserId());

						if (iug.insertEditDataPay(ium, flag))
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
		ItemUnitModel ium = new ItemUnitModel();
		try
		{
			if (iug.selectEditDataPay(ium, payId))
			{ txtPaymentDetails.setValue(ium.getUnitName()); }
			else
			{ cm.showNotification("warning", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(10, 2);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtPaymentDetails = new TextField();
		txtPaymentDetails.setImmediate(true);
		txtPaymentDetails.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtPaymentDetails.setWidth("100%");
		txtPaymentDetails.setInputPrompt("Payment Method(30 Character)");
		txtPaymentDetails.setRequired(true);
		txtPaymentDetails.setRequiredError("This field is required");
		txtPaymentDetails.setDescription("30 Character are allowed");
		txtPaymentDetails.setMaxLength(30);
		Label lbl = new Label("Payment Method: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 0);
		grid.addComponent(txtPaymentDetails, 1, 0, 9, 0);

		grid.addComponent(cBtn, 0, 1, 9, 1);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return grid;
	}

	private void focusEnter()
	{
		allComp.add(txtPaymentDetails);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{ txtPaymentDetails.setValue(""); }
}
