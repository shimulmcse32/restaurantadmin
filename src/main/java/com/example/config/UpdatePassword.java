package com.example.config;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.UserInfoGateway;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class UpdatePassword extends Window
{
	public static final String ID = "profilepreferenceswindow";
	private SessionBean sessionBean;
	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "", "Exit");

	private TextField txtFullName;
	private PasswordField txtCurrent, txtNew, txtReNew;

	private UserInfoGateway uig = new UserInfoGateway();
	private CommonMethod cm;

	private UpdatePassword(SessionBean sessionBean)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);

		addStyleName("profile-window");
		setId(ID);
		Responsive.makeResponsive(this);

		setModal(true);
		addCloseShortcut(KeyCode.ESCAPE, null);
		setResizable(false);
		setHeight(60.0f, Unit.PERCENTAGE);

		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setMargin(new MarginInfo(true, false, false, false));
		content.setSpacing(false);
		setContent(content);

		TabSheet detailsWrapper = new TabSheet();
		detailsWrapper.setSizeFull();
		detailsWrapper.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
		detailsWrapper.addStyleName(ValoTheme.TABSHEET_ICONS_ON_TOP);
		detailsWrapper.addStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
		content.addComponent(detailsWrapper);
		content.setExpandRatio(detailsWrapper, 1f);

		detailsWrapper.addComponent(buildPassword());

		content.addComponent(buildFooter());
	}

	private Component buildPassword()
	{
		HorizontalLayout root = new HorizontalLayout();
		root.setCaption("Update Password");
		root.setIcon(FontAwesome.LOCK);
		root.setWidth(100.0f, Unit.PERCENTAGE);
		root.setHeight(100.0f, Unit.PERCENTAGE);
		root.setMargin(true);
		root.addStyleName("profile-form");

		FormLayout details = new FormLayout();
		details.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
		root.addComponent(details);
		root.setExpandRatio(details, 1);

		txtFullName = new TextField("Full Name ");
		txtFullName.setWidth("-1px");
		txtFullName.setRequired(true);
		txtFullName.setRequiredError("This field is required");
		txtFullName.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFullName.setValue(sessionBean.getFullName());
		txtFullName.setReadOnly(true);
		details.addComponent(txtFullName);

		Label section = new Label("Password Information");
		section.addStyleName(ValoTheme.LABEL_H4);
		section.addStyleName(ValoTheme.LABEL_COLORED);
		details.addComponent(section);

		txtCurrent = new PasswordField("Current Password ");
		txtCurrent.setImmediate(true);
		txtCurrent.setWidth("100%");
		txtCurrent.setRequired(true);
		txtCurrent.setRequiredError("This field is required");
		txtCurrent.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCurrent.setInputPrompt("12345678");
		details.addComponent(txtCurrent);

		txtNew = new PasswordField("New Password ");
		txtNew.setImmediate(true);
		txtNew.setWidth("130%");
		txtNew.setRequired(true);
		txtNew.setRequiredError("This field is required");
		txtNew.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtNew.setInputPrompt("12345678");
		details.addComponent(txtNew);

		txtReNew = new PasswordField("Re-New Password ");
		txtReNew.setImmediate(true);
		txtReNew.setWidth("130%");
		txtReNew.setRequired(true);
		txtReNew.setRequiredError("This field is required");
		txtReNew.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReNew.setInputPrompt("12345678");
		details.addComponent(txtReNew);

		return root;
	}

	private Component buildFooter()
	{
		HorizontalLayout footer = new HorizontalLayout();
		footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
		footer.setWidth(100.0f, Unit.PERCENTAGE);
		footer.setSpacing(false);

		cBtn.btnSave.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{
				try
				{ addValidation(); }
				catch (Exception e)
				{ Notification.show("Error while updating password", Type.ERROR_MESSAGE); }
			}
		});

		cBtn.btnExit.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{
				try
				{ close(); }
				catch (Exception e)
				{ Notification.show("Error while updating password", Type.ERROR_MESSAGE); }
			}
		});
		cBtn.btnSave.focus();
		footer.addComponent(cBtn);
		footer.setComponentAlignment(cBtn, Alignment.TOP_CENTER);
		return footer;
	}

	private void addValidation()
	{
		if (!txtCurrent.getValue().toString().isEmpty())
		{
			if (!txtNew.getValue().toString().isEmpty())
			{
				if (!txtReNew.getValue().toString().isEmpty())
				{
					if (txtNew.getValue().toString().equals(txtReNew.getValue().toString()))
					{
						if (txtCurrent.getValue().toString().equals(sessionBean.getPassword()))
						{
							if (!txtCurrent.getValue().toString().equals(txtNew.getValue().toString()))
							{ updatePassword(); }
							else
							{
								txtCurrent.focus();
								cm.showNotification("warning", "Warning!", "Current and new password are same.");
							}
						}
						else
						{
							txtCurrent.focus();
							cm.showNotification("warning", "Warning!", "Invalid current password.");
						}
					}
					else
					{
						txtReNew.focus();
						cm.showNotification("warning", "Warning!", "Both new password doesn't match.");
					}
				}
				else
				{
					txtReNew.focus();
					cm.showNotification("warning", "Warning!", "Provide re-new password.");
				}
			}
			else
			{
				txtNew.focus();
				cm.showNotification("warning", "Warning!", "Provide new password.");
			}
		}
		else
		{
			txtCurrent.focus();
			cm.showNotification("warning", "Warning!", "Provide current password.");
		}
	}

	private void updatePassword()
	{
		MessageBox mb = new MessageBox(getUI(), "Are you sure?", MessageBox.Icon.QUESTION, "Do you want to save information?",
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
						cBtn.btnSave.setEnabled(false);

						if (uig.updatePassword(sessionBean.getUserId(), txtNew.getValue().toString()))
						{
							sessionBean.setPassword(txtNew.getValue().toString());
							cm.showNotification("success", "Successfull!", "All information updated successfully.");
							cBtn.btnSave.setEnabled(true);
							close();
						}
						else
						{
							cm.showNotification("failure", "Error!", "Couldn't save information.");
						}
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
				}
				else if (buttonType == ButtonType.NO)
				{
					cBtn.btnSave.setEnabled(true);
				}
			}
		});
	}

	public static void open(SessionBean sessionBean)
	{
		Window w = new UpdatePassword(sessionBean);
		UI.getCurrent().addWindow(w);
		w.focus();
	}
}
