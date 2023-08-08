package com.example.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.ImageUpload;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.UserInfoGateway;
import com.example.model.UserInfoModel;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class UpdateProfile extends Window
{
	public static final String ID = "profilepreferenceswindow";
	private SessionBean sessionBean;
	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "", "Exit");

	private TextField txtFullName, txtMobileNo, txtEmailId;
	private ImageUpload Image;

	private UserInfoGateway uig = new UserInfoGateway();
	private CommonMethod cm;

	private String userPicture = "0", editImage = "0";

	private UpdateProfile(SessionBean sessionBean)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		userPicture = sessionBean.imagePathUser;

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

		detailsWrapper.addComponent(buildProfileTab());
		//detailsWrapper.addComponent(buildPreferencesTab());

		content.addComponent(buildFooter());
		setData();
	}

	/*private Component buildPreferencesTab() {
		VerticalLayout root = new VerticalLayout();
		root.setCaption("Preferences");
		root.setIcon(FontAwesome.COGS);
		root.setSpacing(true);
		root.setMargin(true);
		root.setSizeFull();

		Label message = new Label("Not implemented in this demo");
		message.setSizeUndefined();
		message.addStyleName(ValoTheme.LABEL_LIGHT);
		root.addComponent(message);
		root.setComponentAlignment(message, Alignment.MIDDLE_CENTER);

		return root;
	}*/

	private Component buildProfileTab()
	{
		HorizontalLayout root = new HorizontalLayout();
		root.setCaption("Update Profile");
		root.setIcon(FontAwesome.USER);
		root.setWidth(100.0f, Unit.PERCENTAGE);
		root.setHeight(100.0f, Unit.PERCENTAGE);
		root.setMargin(true);
		root.addStyleName("profile-form");

		Image = new ImageUpload("Photo");
		Image.upload.setButtonCaption("Photo");
		VerticalLayout pic = new VerticalLayout();
		pic.setSizeUndefined();
		pic.setSpacing(true);
		pic.setWidth("150px");
		pic.addComponent(Image);
		root.addComponent(pic);

		FormLayout details = new FormLayout();
		details.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
		root.addComponent(details);
		root.setExpandRatio(details, 1);

		txtFullName = new TextField("Full Name ");
		txtFullName.setWidth("-1px");
		txtFullName.setRequired(true);
		txtFullName.setRequiredError("This field is required");
		txtFullName.setStyleName(ValoTheme.TEXTFIELD_TINY);
		details.addComponent(txtFullName);

		Label section = new Label("Contact Information");
		section.addStyleName(ValoTheme.LABEL_H4);
		section.addStyleName(ValoTheme.LABEL_COLORED);
		details.addComponent(section);

		txtMobileNo = new TextField("Mobile No ");
		txtMobileNo.setImmediate(true);
		txtMobileNo.setWidth("100%");
		txtMobileNo.setRequired(true);
		txtMobileNo.setRequiredError("This field is required");
		txtMobileNo.setStyleName(ValoTheme.TEXTFIELD_TINY);
		// add validation that not empty, use binder
		details.addComponent(txtMobileNo);

		txtEmailId = new TextField("Email Address ");
		txtEmailId.setImmediate(true);
		txtEmailId.setWidth("130%");
		txtEmailId.setRequired(true);
		txtEmailId.setRequiredError("This field is required");
		txtEmailId.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtEmailId.setInputPrompt("sample@email.com");
		details.addComponent(txtEmailId);

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
				{ Notification.show("Error while updating profile", Type.ERROR_MESSAGE); }
			}
		});

		cBtn.btnExit.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{
				try
				{ close(); }
				catch (Exception e)
				{ Notification.show("Error while updating profile", Type.ERROR_MESSAGE); }
			}
		});
		cBtn.btnSave.focus();
		footer.addComponent(cBtn);
		footer.setComponentAlignment(cBtn, Alignment.TOP_CENTER);
		return footer;
	}

	private void addValidation()
	{
		if(!txtFullName.getValue().toString().isEmpty())
		{
			if(!txtMobileNo.getValue().toString().isEmpty())
			{
				if(!txtEmailId.getValue().toString().isEmpty())
				{ updateProfile(); }
				else
				{
					txtEmailId.focus();
					cm.showNotification("warning", "Warning!", "Provide email address.");
				}
			}
			else
			{
				txtMobileNo.focus();
				cm.showNotification("warning", "Warning!", "Provide mobile number.");
			}
		}
		else
		{
			txtFullName.focus();
			cm.showNotification("warning", "Warning!", "Provide full name.");
		}
	}

	private void updateProfile()
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
					{
						cBtn.btnSave.setEnabled(false);
						UserInfoModel uim = new UserInfoModel();
						uim.setFullName(txtFullName.getValue().toString());
						uim.setMobileNumber(txtMobileNo.getValue().toString());
						uim.setEmailId(txtEmailId.getValue().toString());
						uim.setUserId(sessionBean.getUserId());
						uim.setCreatedBy(sessionBean.getUserId());

						String userImg = Image.success ? (imagePath(1, sessionBean.getUserId()) == null ? "0" :
							imagePath(1, sessionBean.getUserId())) : editImage;
						uim.setUserPicture(userImg);

						if(uig.updateProfile(uim))
						{
							sessionBean.setFullName(txtFullName.getValue().toString());
							sessionBean.setUserPicture(userImg);
							cm.showNotification("success", "Successfull!", "All information updated successfully.");
							cBtn.btnSave.setEnabled(true);
							close();
						}
						else
						{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
					}
					catch(Exception e)
					{ System.out.println(e); }
				}
				else if(buttonType == ButtonType.NO)
				{ cBtn.btnSave.setEnabled(true); }
			}
		});
	}

	private String imagePath(int flag, String path)
	{
		String basePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"".replace("\\","/")+"/VAADIN/rpttmp/";
		String ReturnImagePath = "0";

		if(flag==1)
		{
			// image move
			if(Image.fileName.trim().length()>0)
			{
				try 
				{
					if(Image.fileName.toString().endsWith(".jpg"))
					{
						String Origin = basePath + Image.fileName.trim();
						String Destin = userPicture + path + ".jpg";
						//System.out.println(Origin);

						//Copying file
						fileCopy(Origin, Destin);
						ReturnImagePath = Destin;
					}
					else
					{
						Image.upload.focus();
						cm.showNotification("warning", "Warning!", "Invalid image format(jpg only).");
					}
				}
				catch(IOException e) 
				{ e.printStackTrace(); }
			}
			return ReturnImagePath;
		}
		return null;
	}

	private void fileCopy(String fStr, String tStr) throws IOException
	{
		try
		{
			File f1 = new File(tStr);
			if (f1.isFile())
			{ f1.delete(); }
		}
		catch(Exception exp)
		{ System.out.println("Photo move: "+exp); }
		FileInputStream ff = new FileInputStream(fStr);

		File ft = new File(tStr);
		FileOutputStream fos = new FileOutputStream(ft);

		while (ff.available() != 0)
		{ fos.write(ff.read()); }
		fos.close();
		ff.close();
	}

	private void setData()
	{
		UserInfoModel uim = new UserInfoModel();
		uig.selectEditData(uim, sessionBean.getUserId());
		txtFullName.setValue(uim.getFullName());
		txtMobileNo.setValue(uim.getMobileNumber());
		txtEmailId.setValue(uim.getEmailId());
		Image.setImage(sessionBean.getUserPicture());
		editImage = sessionBean.getUserPicture();
	}

	public static void open(SessionBean sessionBean)
	{
		Window w = new UpdateProfile(sessionBean);
		UI.getCurrent().addWindow(w);
		w.focus();
	}
}
