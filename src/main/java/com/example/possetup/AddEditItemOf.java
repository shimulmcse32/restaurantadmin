package com.example.possetup;

import java.util.ArrayList;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.ItemCompanyInfoGateway;
import com.example.model.CompanyInfoModel;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditItemOf extends Window
{
	private SessionBean sessionBean;
	private String flag, companyId;

	private TextField txtCompanyName, txtPhone, txtFax, txtEmail, txtLicense;
	private TextArea txtAddress;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private ItemCompanyInfoGateway cig = new ItemCompanyInfoGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();
	private CommonMethod cm;

	public AddEditItemOf(SessionBean sessionBean, String flag, String companyId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.companyId = companyId;
		cm = new CommonMethod(sessionBean);
		this.setCaption((this.flag.equals("Add")?"Add":"Edit")+
				" Item Company Information :: "+this.sessionBean.getCompanyName());
		setWidth("600px");
		setHeight("510px");

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

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void addValidation()
	{
		if (!txtCompanyName.getValue().toString().trim().isEmpty())
		{
			if (!txtAddress.getValue().toString().trim().isEmpty())
			{
				if (!txtLicense.getValue().toString().trim().isEmpty())
				{
					cBtn.btnSave.setEnabled(false);
					insertEditData();
				}
				else
				{
					txtLicense.focus();
					cm.showNotification("warning", "Warning!", "Provide CR number.");
				}
			}
			else
			{
				txtAddress.focus();
				cm.showNotification("warning", "Warning!", "Provide address.");
			}
		}
		else
		{
			txtCompanyName.focus();
			cm.showNotification("warning", "Warning!", "Provide company name.");
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
						CompanyInfoModel cim = new CompanyInfoModel();
						String companyIdN = flag.equals("Add")?cig.getCompanyId():companyId;
						cim.setCompanyId(companyIdN);
						cim.setBranchId(sessionBean.getBranchId());
						cim.setCompanyName(txtCompanyName.getValue().toString().trim());
						cim.setAddress(txtAddress.getValue().toString().trim());
						cim.setPhone(txtPhone.getValue().toString().trim());
						cim.setFax(txtFax.getValue().toString().trim());
						cim.setEmail(txtEmail.getValue().toString().trim());
						cim.setLicense(txtLicense.getValue().toString().trim());
						cim.setCreatedBy(sessionBean.getUserId());

						if (cig.insertEditData(cim, flag))
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
		CompanyInfoModel cim = new CompanyInfoModel();
		try
		{
			if (cig.selectEditData(cim, companyId))
			{
				txtCompanyName.setValue(cim.getCompanyName());
				txtAddress.setValue(cim.getAddress());
				txtPhone.setValue(cim.getPhone());
				txtFax.setValue(cim.getFax());
				txtEmail.setValue(cim.getEmail());
				txtLicense.setValue(cim.getLicense());
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(4, 7);
		grid.setMargin(true);
		grid.setSpacing(true);

		txtCompanyName = new TextField();
		txtCompanyName.setImmediate(true);
		txtCompanyName.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtCompanyName.setWidth("100%");
		txtCompanyName.setInputPrompt("Company Name");
		txtCompanyName.setRequired(true);
		txtCompanyName.setRequiredError("Provide Company Name");
		grid.addComponent(new Label("Company Name: "), 0, 0);
		grid.addComponent(txtCompanyName, 1, 0, 3, 0);

		txtAddress = new TextArea();
		txtAddress.setImmediate(true);
		txtAddress.addStyleName(ValoTheme.TEXTAREA_SMALL);
		txtAddress.setWidth("100%");
		txtAddress.setHeight("90px");
		txtAddress.setInputPrompt("Address");
		txtAddress.setRequired(true);
		txtAddress.setRequiredError("Provide Address");
		grid.addComponent(new Label("Address: "), 0, 1);
		grid.addComponent(txtAddress, 1, 1, 3, 1);

		txtPhone = new TextField();
		txtPhone.setImmediate(true);
		txtPhone.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtPhone.setInputPrompt("Phone");
		grid.addComponent(new Label("Phone: "), 0, 2);
		grid.addComponent(txtPhone, 1, 2);

		txtFax = new TextField();
		txtFax.setImmediate(true);
		txtFax.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtFax.setInputPrompt("Fax");
		grid.addComponent(new Label("Fax: "), 0, 3);
		grid.addComponent(txtFax, 1, 3);

		txtEmail = new TextField();
		txtEmail.setImmediate(true);
		txtEmail.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtEmail.setInputPrompt("sample@email.com");
		grid.addComponent(new Label("Email: "), 0, 4);
		grid.addComponent(txtEmail, 1, 4);

		txtLicense = new TextField();
		txtLicense.setImmediate(true);
		txtLicense.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtLicense.setInputPrompt("CR");
		txtLicense.setRequired(true);
		txtLicense.setRequiredError("Provide CR");
		grid.addComponent(new Label("CR No: "), 0, 5);
		grid.addComponent(txtLicense, 1, 5);

		grid.addComponent(cBtn, 0, 6, 3, 6);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return grid;
	}

	private void focusEnter()
	{
		allComp.add(txtCompanyName);
		allComp.add(txtAddress);
		allComp.add(txtPhone);
		allComp.add(txtFax);
		allComp.add(txtEmail);
		allComp.add(txtLicense);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		txtCompanyName.setValue("");
		txtAddress.setValue("");
		txtPhone.setValue("");
		txtFax.setValue("");
		txtEmail.setValue("");
		txtLicense.setValue("");
	}
}
