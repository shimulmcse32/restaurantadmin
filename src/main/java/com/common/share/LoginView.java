package com.common.share;

import java.util.Iterator;

import com.example.config.ForgetPassword;
import com.example.gateway.UserInfoGateway;
import com.example.main.MainUI;
import com.example.model.UserInfoModel;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class LoginView extends VerticalLayout
{
	private ComboBox cmbBranchName;
	private TextField txtUserName;
	private PasswordField txtPassword;
	private Button btnSignIn, btnForgetPass;
	private MainUI ui;
	private UserInfoGateway uig = new UserInfoGateway();
	private SessionBean sessionBean = new SessionBean();
	private CommonMethod cm;

	public LoginView(MainUI ui)
	{
		this.ui = ui;
		setSizeFull();

		cm = new CommonMethod(sessionBean);

		Component loginForm = buildLoginForm();
		addComponent(loginForm);
		setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);

		notification();

		addAction();
		txtUserName.focus();
	}

	private void addAction()
	{
		btnSignIn.addClickListener(event -> validation());

		btnForgetPass.addClickListener(event -> forgetPassword());

		cmbBranchName.addValueChangeListener(event -> txtUserName.focus());
	}

	private void forgetPassword()
	{
		btnSignIn.setEnabled(false);
		btnForgetPass.setEnabled(false);
		ForgetPassword win = new ForgetPassword(sessionBean);
		getUI().addWindow(win);
		win.center();
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event ->
		{
			btnSignIn.setEnabled(true);
			btnForgetPass.setEnabled(true);
		});
	}

	private void validation()
	{
		if (cmbBranchName.getValue() != null)
		{
			if (!txtUserName.getValue().isEmpty())
			{
				if (!txtPassword.getValue().isEmpty())
				{
					btnSignIn.setEnabled(false);
					loginValidation();
				}
				else
				{
					txtPassword.focus();
					cm.showNotification("warning", "Warning!", "Provide password.");
				}
			}
			else
			{
				txtUserName.focus();
				cm.showNotification("warning", "Warning!", "Provide username.");
			}
		}
		else
		{
			cmbBranchName.focus();
			cm.showNotification("warning", "Warning!", "Select branch name.");
		}
	}

	private void loginValidation()
	{
		UserInfoModel uim = new UserInfoModel();
		uim.setBranchId(cmbBranchName.getValue().toString());
		uim.setUserName(txtUserName.getValue().toString());
		uim.setPassWord(txtPassword.getValue().toString());

		if(uig.userCheck(uim, sessionBean))
		{
			//win.close();
			/*if (sessionBean.getUserType().equals("Operation"))
			{
				getUI().getPage().open("http://google.com", "");
			}
			else*/
			{
				new RootMenu(ui, sessionBean);
			}
		}
		else
		{
			btnSignIn.setEnabled(true);
			cm.showNotification("warning", "Warning!", "Invalid branch, username or password.");
		}
	}

	private void notification()
	{
		Notification notification = new Notification("Welcome to ERP Application");
		notification.setDescription("<span>This application is developed by <a href=\"http://www.hitech.cc\""
				+ " target=_blank>HiTech WLL</a>.</span> <span>Username or password is required, then click the"
				+ " <b>Sign In</b> button to continue.</span>");
		notification.setHtmlContentAllowed(true);
		notification.setStyleName("tray dark small closable login-help");
		notification.setPosition(Position.BOTTOM_CENTER);
		notification.setDelayMsec(20000);
		notification.show(Page.getCurrent());
	}

	private Component buildLoginForm()
	{
		final VerticalLayout loginPanel = new VerticalLayout();
		loginPanel.setSizeUndefined();
		loginPanel.setSpacing(true);
		Responsive.makeResponsive(loginPanel);
		loginPanel.addStyleName("login-panel");

		loginPanel.addComponent(buildLabels());
		loginPanel.addComponent(buildFields());
		return loginPanel;
	}

	private Component buildFields()
	{
		VerticalLayout fields = new VerticalLayout();
		fields.setSpacing(true);
		fields.addStyleName("fields");

		cmbBranchName = new ComboBox("Branch Name");
		cmbBranchName.setImmediate(true);
		cmbBranchName.setRequired(true);
		cmbBranchName.setIcon(FontAwesome.CAB);
		cmbBranchName.setRequiredError("This field is required.");
		cmbBranchName.setFilteringMode(FilteringMode.CONTAINS);
		cmbBranchName.setStyleName(ValoTheme.COMBOBOX_SMALL);
		cmbBranchName.setInputPrompt("Select branch name");
		branchDataLoad();

		txtUserName = new TextField("Username");
		txtUserName.setIcon(FontAwesome.USER);
		txtUserName.setInputPrompt("Username");
		txtUserName.setRequired(true);
		txtUserName.setRequiredError("This field is required.");
		txtUserName.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtUserName.addStyleName(ValoTheme.TEXTFIELD_SMALL);

		txtPassword = new PasswordField("Password");
		txtPassword.setIcon(FontAwesome.LOCK);
		txtPassword.setInputPrompt("Password");
		txtPassword.setRequired(true);
		txtPassword.setRequiredError("This field is required.");
		txtPassword.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtPassword.addStyleName(ValoTheme.TEXTFIELD_SMALL);

		btnSignIn = new Button("Sign In");
		btnSignIn.setIcon(FontAwesome.SIGN_IN);
		btnSignIn.addStyleName(ValoTheme.BUTTON_PRIMARY);
		btnSignIn.addStyleName(ValoTheme.BUTTON_SMALL);
		btnSignIn.setClickShortcut(KeyCode.ENTER);
		btnSignIn.focus();

		fields.addComponents(cmbBranchName, txtUserName, txtPassword, btnSignIn, forgetPass());
		fields.setComponentAlignment(btnSignIn, Alignment.BOTTOM_LEFT);

		/* signin.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                DashboardEventBus.post(new UserLoginRequestedEvent(username
                        .getValue(), password.getValue()));
            }
        });*/
		return fields;
	}

	private Component forgetPass()
	{
		GridLayout lay = new GridLayout(2, 1);
		lay.setSpacing(true);

		Label forget = new Label("Forget password?");
		forget.addStyleName(ValoTheme.LABEL_COLORED);
		lay.addComponent(forget, 0, 0);

		btnForgetPass = new Button();
		btnForgetPass.setIcon(FontAwesome.SEND);
		btnForgetPass.setDescription("Click Here");
		btnForgetPass.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
		btnForgetPass.setStyleName(ValoTheme.BUTTON_TINY);
		lay.addComponent(btnForgetPass, 1, 0);

		return lay;
	}

	private void branchDataLoad()
	{
		String value = "";
		String sql = "select vBranchId, vBranchName, iBranchTypeId from master.tbBranchMaster"+
				" where iActive = 1 and vCompanyId = 'C1'";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbBranchName.addItem(element[0].toString());
			cmbBranchName.setItemCaption(element[0].toString(), element[1].toString());
			if (element[2].toString().equals("1"))
			{ value = element[0].toString(); }
		}
		cmbBranchName.setValue(value);
	}

	private Component buildLabels()
	{
		CssLayout labels = new CssLayout();
		labels.addStyleName("labels");

		Label welcome = new Label("Welcome to HiPOS Application");
		welcome.addStyleName(ValoTheme.LABEL_H3);
		welcome.addStyleName(ValoTheme.LABEL_COLORED);
		labels.addComponent(welcome);

		return labels;
	}
}
