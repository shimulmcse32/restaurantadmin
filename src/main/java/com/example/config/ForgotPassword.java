package com.example.config;

import java.util.ArrayList;

import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.SessionBean;
import com.example.gateway.UserInfoGateway;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class ForgotPassword extends Window
{
	private TextField txtUserName, txtEmail;

	private Button btnSend;
	private ArrayList<Component> allComp = new ArrayList<Component>();
	private CommonMethod cm;
	private UserInfoGateway uig = new UserInfoGateway();

	public ForgotPassword(SessionBean sessionBean)
	{
		cm = new CommonMethod(sessionBean);
		this.setCaption("Forget Password");
		this.setResizable(false);
		setWidth("330px");
		setHeight("230px");

		setContent(buildLayout());

		addActions();
	}

	private void addActions()
	{
		btnSend.addClickListener(event ->
		{ addValidation(); });

		focusEnter();
	}

	private void addValidation()
	{
		if (!txtUserName.getValue().toString().trim().isEmpty())
		{
			if (!txtEmail.getValue().toString().trim().isEmpty())
			{
				if (!uig.getForgetUserId(txtUserName.getValue().toString().trim(),
						txtEmail.getValue().toString().trim()).isEmpty())
				{
					btnSend.setEnabled(false);
					insertEditData();
				}
				else
				{
					txtUserName.focus();
					cm.showNotification("warning", "Warning!", "Invalid username or email address.");
				}
			}
			else
			{
				txtEmail.focus();
				cm.showNotification("warning", "Warning!", "Provide email address.");
			}
		}
		else
		{
			txtUserName.focus();
			cm.showNotification("warning", "Warning!", "Provide username.");
		}
	}

	private void insertEditData()
	{

	}

	private Component buildLayout()
	{
		VerticalLayout lay = new VerticalLayout();
		lay.setMargin(true);
		lay.setSpacing(true);
		lay.setSizeFull();
		lay.addStyleName("fields");

		txtUserName = new TextField("Username");
		txtUserName.setImmediate(true);
		txtUserName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtUserName.setWidth("100%");
		txtUserName.setInputPrompt("Username");
		txtUserName.setRequired(true);
		txtUserName.setRequiredError("This field is required.");

		txtEmail = new TextField("Email Address");
		txtEmail.setImmediate(true);
		txtEmail.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtEmail.setWidth("100%");
		txtEmail.setInputPrompt("sample@email.com");
		txtEmail.setRequired(true);
		txtEmail.setRequiredError("This field is required.");
		txtEmail.addValidator(new EmailValidator("Invalid e-mail address {0}"));
		lay.addComponents(txtUserName, txtEmail);

		btnSend = new Button("Send Credentials");
		btnSend.setIcon(FontAwesome.ENVELOPE);
		btnSend.setStyleName(ValoTheme.BUTTON_TINY);
		btnSend.addStyleName(ValoTheme.BUTTON_PRIMARY);
		btnSend.setWidth("100%");
		btnSend.setDescription("Send Credentials");
		lay.addComponent(btnSend);

		lay.setComponentAlignment(btnSend, Alignment.BOTTOM_CENTER);

		return lay;
	}

	private void focusEnter()
	{
		allComp.add(txtUserName);
		allComp.add(txtEmail);
		allComp.add(btnSend);

		new FocusMoveByEnter(this, allComp);
	}
}
