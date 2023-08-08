package com.example.config;

import com.common.share.ImageUpload;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TabMaster extends HorizontalLayout
{
	public TextField txtCompanyName, txtPhone, txtFax, txtEmail,
	txtLicense, txtVatRegNo, txtWebsite;
	public TextArea txtAddress;

	public CheckBox chkEmailNotification;

	public ImageUpload Image;
	public String companyLogo = "0", editImage = "0";

	public TabMaster(String flag)
	{
		setSizeFull();

		addComponent(buildLayout());
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(5, 10);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtCompanyName = new TextField();
		txtCompanyName.setImmediate(true);
		txtCompanyName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCompanyName.setWidth("100%");
		txtCompanyName.setInputPrompt("Company Name");
		txtCompanyName.setRequired(true);
		txtCompanyName.setRequiredError("This field is required");
		Label lbl = new Label("Company Name: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 0);
		grid.addComponent(txtCompanyName, 1, 0, 3, 0);

		txtAddress = new TextArea();
		txtAddress.setImmediate(true);
		txtAddress.addStyleName(ValoTheme.TEXTAREA_TINY);
		txtAddress.setWidth("100%");
		txtAddress.setHeight("70px");
		txtAddress.setInputPrompt("Address");
		txtAddress.setRequired(true);
		txtAddress.setRequiredError("This field is required");
		grid.addComponent(new Label("Address : "), 0, 1);
		grid.addComponent(txtAddress, 1, 1, 3, 1);

		txtPhone = new TextField();
		txtPhone.setImmediate(true);
		txtPhone.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPhone.setInputPrompt("Phone");
		grid.addComponent(new Label("Phone: "), 0, 2);
		grid.addComponent(txtPhone, 1, 2);

		txtFax = new TextField();
		txtFax.setImmediate(true);
		txtFax.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFax.setInputPrompt("Fax");
		grid.addComponent(new Label("Fax: "), 0, 3);
		grid.addComponent(txtFax, 1, 3);

		txtEmail = new TextField();
		txtEmail.setImmediate(true);
		txtEmail.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtEmail.setInputPrompt("sample@email.com");
		grid.addComponent(new Label("Email: "), 0, 4);
		grid.addComponent(txtEmail, 1, 4);

		chkEmailNotification = new CheckBox("Email Notification");
		chkEmailNotification.setImmediate(true);
		//chkEmailNotification.setValue(true);
		chkEmailNotification.addStyleName(ValoTheme.CHECKBOX_SMALL);
		chkEmailNotification.setDescription("Save as an Email Notification.");
		grid.addComponent(chkEmailNotification, 1, 5);
		//grid.setComponentAlignment(chkEmailNotification, Alignment.MIDDLE_CENTER);

		txtLicense = new TextField();
		txtLicense.setImmediate(true);
		txtLicense.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtLicense.setInputPrompt("CR");
		txtLicense.setRequired(true);
		txtLicense.setRequiredError("This field is required");
		grid.addComponent(new Label("CR No: "), 0, 6);
		grid.addComponent(txtLicense, 1, 6);

		txtVatRegNo = new TextField();
		txtVatRegNo.setImmediate(true);
		txtVatRegNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtVatRegNo.setInputPrompt("VAT Reg. No");
		grid.addComponent(new Label("VAT Reg. No: "), 0, 7);
		grid.addComponent(txtVatRegNo, 1, 7);

		txtWebsite = new TextField();
		txtWebsite.setImmediate(true);
		txtWebsite.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtWebsite.setInputPrompt("www.website.com");
		grid.addComponent(new Label("Website: "), 0, 8);
		grid.addComponent(txtWebsite, 1, 8);

		Image = new ImageUpload("Photo");
		Image.upload.setButtonCaption("Logo");
		Image.setWidth("100%");
		grid.addComponent(Image, 2, 3, 4, 9);

		return grid;
	}
}
