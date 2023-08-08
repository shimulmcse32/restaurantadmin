package com.example.config;

import com.common.share.CommaField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TabAdditional extends HorizontalLayout
{
	public TextField txtOnlineMenuName, txtPhone, txtFax, txtEmail,
	txtLicense, txtVatRegNo, txtWebsite, txtUrlLink;
	public TextArea txtAddress;

	public CommaField txtMinOrderAmt;

	public CheckBox chkEmailNotification, chkStockZeroSale;

	public TabAdditional(String flag)
	{
		setSizeFull();

		addComponent(buildLayout());
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(5, 12);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtOnlineMenuName = new TextField();
		txtOnlineMenuName.setImmediate(true);
		txtOnlineMenuName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtOnlineMenuName.setWidth("100%");
		txtOnlineMenuName.setInputPrompt("Online Menu");
		Label lbl = new Label("Online Menu Name: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 0);
		grid.addComponent(txtOnlineMenuName, 1, 0, 4, 0);

		txtAddress = new TextArea();
		txtAddress.setImmediate(true);
		txtAddress.addStyleName(ValoTheme.TEXTAREA_TINY);
		txtAddress.setWidth("100%");
		txtAddress.setHeight("70px");
		txtAddress.setInputPrompt("Address");
		grid.addComponent(new Label("Address : "), 0, 1);
		grid.addComponent(txtAddress, 1, 1, 4, 1);

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
		txtWebsite.setWidth("100%");
		txtWebsite.setInputPrompt("www.website.com");
		grid.addComponent(new Label("Website: "), 0, 8);
		grid.addComponent(txtWebsite, 1, 8, 4, 8);

		txtUrlLink = new TextField();
		txtUrlLink.setImmediate(true);
		txtUrlLink.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtUrlLink.setWidth("100%");
		txtUrlLink.setInputPrompt("http://online_menu");
		grid.addComponent(new Label("URL: "), 0, 9);
		grid.addComponent(txtUrlLink, 1, 9, 4, 9);

		txtMinOrderAmt = new CommaField();
		txtMinOrderAmt.setWidth("70px");
		txtMinOrderAmt.setValue("0");
		txtMinOrderAmt.setImmediate(true);
		txtMinOrderAmt.setInputPrompt("Minimum Order Amount");
		txtMinOrderAmt.setDescription("Minimum Order Amount");
		txtMinOrderAmt.addStyleName(ValoTheme.TEXTFIELD_TINY);
		grid.addComponent(new Label("Minimum Order Amount: "), 0, 10);
		grid.addComponent(txtMinOrderAmt, 1, 10);

		chkStockZeroSale = new CheckBox("Stock Zero Sale");
		chkStockZeroSale.setImmediate(true);
		//chkEmailNotification.setValue(true);
		chkStockZeroSale.addStyleName(ValoTheme.CHECKBOX_SMALL);
		chkStockZeroSale.setDescription("If you tick it then you can not sale zero stock item. other wise you can sale.");
		grid.addComponent(chkStockZeroSale, 1, 11);

		//QRcode qrcode = new QRcode();
		//grid.addComponent(qrcode, 2, 3, 4, 9);

		return grid;
	}
}