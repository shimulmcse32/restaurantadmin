package com.example.config;

import java.util.ArrayList;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.BranchInfoGateway;
import com.example.model.BranchInfoModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditBranch extends Window
{
	private SessionBean sessionBean;
	private String flag, branchId;

	private TextField txtBranchName, txtPhone, txtFax, txtEmail,
	txtLicense;
	private TextArea txtAddress;
	private ComboBox cmbBranchType;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private BranchInfoGateway big = new BranchInfoGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private CommonMethod cm;

	public AddEditBranch(SessionBean sessionBean, String flag, String branchId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.branchId = branchId;
		this.setCaption(flag+" Branch Information :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(this.sessionBean);
		setWidth("510px");
		setHeight("480px");

		setContent(buildLayout());
		userTypeLoad();
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event -> addValidation());

		cBtn.btnExit.addClickListener(event -> close());

		if(flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void addValidation()
	{
		if (!txtBranchName.getValue().toString().trim().isEmpty())
		{
			if (!txtAddress.getValue().toString().trim().isEmpty())
			{
				if (!txtLicense.getValue().toString().trim().isEmpty())
				{
					if (cmbBranchType.getValue() != null)
					{
						cBtn.btnSave.setEnabled(false);
						insertEditData();
					}
					else
					{
						cmbBranchType.focus();
						cm.showNotification("warning", "Warning!", "Select branch type.");
					}
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
			txtBranchName.focus();
			cm.showNotification("warning", "Warning!", "Provide branch name.");
		}
	}

	private void insertEditData()
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
						BranchInfoModel bim = new BranchInfoModel();
						String branchIdN = flag.equals("Add")?big.getBranchId():branchId;
						bim.setCompanyId(sessionBean.getCompanyId());
						bim.setBranchId(branchIdN);
						getBranchDetails(bim);
						if (big.insertEditData(bim, flag))
						{
							txtClear();
							cm.showNotification("success", "Successfull!", "All information updated successfully."+
									" Please re-login again to effect the updates.");
							cBtn.btnSave.setEnabled(true);

							if(flag.equals("Edit"))
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

	private void getBranchDetails(BranchInfoModel bim)
	{
		bim.setBranchName(txtBranchName.getValue().toString().trim());
		bim.setAddress(txtAddress.getValue().toString().trim());
		bim.setPhone(txtPhone.getValue().toString().trim());
		bim.setFax(txtFax.getValue().toString().trim());
		bim.setEmail(txtEmail.getValue().toString().trim());
		bim.setLicense(txtLicense.getValue().toString().trim());
		bim.setBranchType(cmbBranchType.getValue().toString());
		bim.setCreatedBy(sessionBean.getUserId());

		if (bim.getBranchId().equals(sessionBean.getBranchId()))
		{
			sessionBean.setBranchName(txtBranchName.getValue().toString().trim());
			sessionBean.setBranchAddress(txtAddress.getValue().toString().trim());
			sessionBean.setBranchContact(txtPhone.getValue().toString().trim()+", "+
					txtFax.getValue().toString().trim()+", "+
					txtEmail.getValue().toString().trim());
		}
	}

	private void setEditData()
	{
		BranchInfoModel bim = new BranchInfoModel();
		try
		{
			if (big.selectEditData(bim, branchId))
			{
				txtBranchName.setValue(bim.getBranchName());
				txtAddress.setValue(bim.getAddress());
				txtPhone.setValue(bim.getPhone());
				txtFax.setValue(bim.getFax());
				txtEmail.setValue(bim.getEmail());
				txtLicense.setValue(bim.getLicense());
				cmbBranchType.setValue(bim.getBranchType());
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private void userTypeLoad()
	{
		String sql = " select iBranchTypeId, vBranchType from master.tbBranchType ";
		for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbBranchType.addItem(element[0].toString());
			cmbBranchType.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private Component buildLayout()
	{
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout grid = new GridLayout(10, 10);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtBranchName = new TextField();
		txtBranchName.setImmediate(true);
		txtBranchName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtBranchName.setWidth("100%");
		txtBranchName.setInputPrompt("Branch Name");
		txtBranchName.setRequired(true);
		txtBranchName.setRequiredError("This field is required");
		grid.addComponent(new Label("Branch Name: "), 0, 0);
		grid.addComponent(txtBranchName, 1, 0, 9, 0);

		txtAddress = new TextArea();
		txtAddress.setImmediate(true);
		txtAddress.addStyleName(ValoTheme.TEXTAREA_TINY);
		txtAddress.setWidth("100%");
		txtAddress.setHeight("78px");
		txtAddress.setInputPrompt("Address");
		txtAddress.setRequired(true);
		txtAddress.setRequiredError("This field is required");
		grid.addComponent(new Label("Address: "), 0, 1);
		grid.addComponent(txtAddress, 1, 1, 9, 1);

		txtPhone = new TextField();
		txtPhone.setImmediate(true);
		txtPhone.setWidth("100%");
		txtPhone.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPhone.setInputPrompt("Phone");
		grid.addComponent(new Label("Phone: "), 0, 2);
		grid.addComponent(txtPhone, 1, 2, 9, 2);

		txtFax = new TextField();
		txtFax.setImmediate(true);
		txtFax.setWidth("100%");
		txtFax.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFax.setInputPrompt("Fax");
		grid.addComponent(new Label("Fax: "), 0, 3);
		grid.addComponent(txtFax, 1, 3, 9, 3);

		txtEmail = new TextField();
		txtEmail.setImmediate(true);
		txtEmail.setWidth("100%");
		txtEmail.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtEmail.setInputPrompt("sample@email.com");
		grid.addComponent(new Label("Email: "), 0, 4);
		grid.addComponent(txtEmail, 1, 4, 9, 4);

		txtLicense = new TextField();
		txtLicense.setImmediate(true);
		txtLicense.setWidth("100%");
		txtLicense.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtLicense.setInputPrompt("CR");
		txtLicense.setRequired(true);
		txtLicense.setRequiredError("This field is required");
		grid.addComponent(new Label("CR No: "), 0, 5);
		grid.addComponent(txtLicense, 1, 5, 9, 5);

		cmbBranchType = new ComboBox();
		cmbBranchType.setImmediate(true);
		cmbBranchType.setWidth("100%");
		cmbBranchType.setFilteringMode(FilteringMode.CONTAINS);
		cmbBranchType.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbBranchType.setInputPrompt("Select Branch Type");
		cmbBranchType.setRequired(true);
		cmbBranchType.setRequiredError("This field is required");
		Label lbl = new Label("Branch Type: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 6);
		grid.addComponent(cmbBranchType, 1, 6, 9, 6);

		content.addComponents(grid, cBtn);
		content.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return content;
	}

	private void focusEnter()
	{
		allComp.add(txtBranchName);
		allComp.add(txtAddress);
		allComp.add(txtPhone);
		allComp.add(txtFax);
		allComp.add(txtEmail);
		allComp.add(txtLicense);
		allComp.add(cmbBranchType);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		txtBranchName.setValue("");
		txtAddress.setValue("");
		txtPhone.setValue("");
		txtFax.setValue("");
		txtEmail.setValue("");
		txtLicense.setValue("");
		cmbBranchType.setValue(null);
	}
}
