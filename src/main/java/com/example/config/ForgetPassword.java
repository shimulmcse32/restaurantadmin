package com.example.config;

import java.util.ArrayList;

import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.SessionBean;
import com.example.gateway.UserInfoGateway;
import com.example.model.UserInfoModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class ForgetPassword extends Window
{
	private TextField txtUserName, txtEmail;

	private Button btnSend;
	private ArrayList<Component> allComp = new ArrayList<Component>();
	private CommonMethod cm;
	private UserInfoGateway uig = new UserInfoGateway();

	public ForgetPassword(SessionBean sessionBean)
	{
		cm = new CommonMethod(sessionBean);
		this.setCaption("Forget Password");
		this.setResizable(false);
		setWidth("300px");
		setHeight("240px");

		setContent(buildLayout());

		addActions();
	}

	private void addActions()
	{
		btnSend.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{ addValidation(); }
		});

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
		MessageBox mb = new MessageBox(getUI(), "Are you sure?", MessageBox.Icon.QUESTION, "Do you want to update information?",
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
						UserInfoModel uim = new UserInfoModel();

						String userName = txtUserName.getValue().toString().trim();
						String email = txtEmail.getValue().toString().trim();
						uim.setUserName(userName);
						uim.setEmailId(email);
						uim.setUserId(uig.getForgetUserId(userName, email));

						/*if (uig.insertEditData(uim))
						{
							txtClear();
							cm.showNotification("success", "Successfull!", "All information updated successfully.");
							cBtn.btnSave.setEnabled(true);
						}
						else
						{
							cm.showNotification("failure", "Error!", "Couldn't save information.");
						}*/
					}
					catch(Exception ex)
					{
						System.out.println(ex);
					}
				}
				else if(buttonType == ButtonType.NO)
				{
					btnSend.setEnabled(true);
				}
			}
		});
	}

	private Component buildLayout()
	{
		VerticalLayout lay = new VerticalLayout();
		lay.setMargin(true);
		lay.setSpacing(true);
		lay.setSizeFull();

		txtUserName = new TextField("Username");
		txtUserName.setImmediate(true);
		txtUserName.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtUserName.setWidth("100%");
		txtUserName.setInputPrompt("Username");
		txtUserName.setRequired(true);
		txtUserName.setRequiredError("This field is required");

		txtEmail = new TextField("Email Address");
		txtEmail.setImmediate(true);
		txtEmail.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtEmail.setWidth("100%");
		txtEmail.setInputPrompt("sample@mail.com");
		txtEmail.setRequired(true);
		txtEmail.setRequiredError("This field is required");
		lay.addComponents(txtUserName, txtEmail);

		btnSend = new Button();
		btnSend.setIcon(FontAwesome.SEND_O);
		btnSend.setStyleName(ValoTheme.BUTTON_SMALL);
		btnSend.setDescription("Send");
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
