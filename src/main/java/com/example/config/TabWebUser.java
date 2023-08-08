package com.example.config;

import java.util.Date;
import java.util.Iterator;

import com.common.share.CommonMethod;
import com.common.share.SessionBean;
import com.example.model.UserInfoModel;
import com.common.share.MultiComboBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TabWebUser extends HorizontalLayout
{
	private SessionBean sessionBean;

	public ComboBox cmbRoleName, cmbUserType, cmbEmployeeName;
	public MultiComboBox cmbBranchName;
	public TextField txtUserName, txtFullName, txtContactNo, txtEmailAddress;
	public PasswordField txtPassword, txtConfirmPass;
	public PopupDateField txtExpiryDate;

	private CommonMethod cm;

	public TabWebUser(SessionBean sessionBean, String flag)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(sessionBean);
		setSizeFull();

		addComponent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		loadComboData();
		cmbEmployeeName.addValueChangeListener(event -> setEmployeeData());
	}

	private void setEmployeeData()
	{
		txtContactNo.setValue("");
		txtEmailAddress.setValue("");

		String empId = cmbEmployeeName.getValue() != null? cmbEmployeeName.getValue().toString():"";
		String sqlI = "select vContactNo, vEmailAddress from master.tbEmployeeMaster where vEmployeeId = '"+empId+"'";
		//System.out.println(sqlI);
		for (Iterator<?> iter = cm.selectSql(sqlI).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			txtContactNo.setValue(element[0].toString());
			txtEmailAddress.setValue(element[1].toString());
		}
	}

	private void loadComboData()
	{
		//Role name
		String sqlR = "select vRoleId, vRoleName from master.tbUserRoleInfo"+
				" where iActive = 1 order by vRoleName";
		for (Iterator<?> iter = cm.selectSql(sqlR).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbRoleName.addItem(element[0].toString());
			cmbRoleName.setItemCaption(element[0].toString(), element[1].toString());
		}

		//Branch name
		String sqlB = "select vBranchId, vBranchName from master.tbBranchMaster"+
				" where iActive = 1 order by vBranchName";
		for (Iterator<?> iter = cm.selectSql(sqlB).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbBranchName.addItem(element[0].toString());
			cmbBranchName.setItemCaption(element[0].toString(), element[1].toString());
		}

		//User type
		String type = "2";
		if (sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin())
		{ type = "%"; }
		String sqlU = "select iUserTypeId, vUserTypeName from master.tbUserType"+
				" where iActive = 1 and iUserTypeId like '"+type+"' order by vUserTypeName";
		for (Iterator<?> iter = cm.selectSql(sqlU).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbUserType.addItem(element[0].toString());
			cmbUserType.setItemCaption(element[0].toString(), element[1].toString());
		}
		loadEmployeeData("");
	}

	public void loadEmployeeData(String empId)
	{
		cmbEmployeeName.removeAllItems();
		String sql = "select vEmployeeId, vEmployeeCode, vEmployeeName, vCPRNumber from master.tbEmployeeMaster"+
				" where iActive = 1 and vEmployeeId not in (select vEmployeeId from master.tbUserInfo where"+
				" vEmployeeId != '"+empId+"') order by vEmployeeCode, vEmployeeName asc";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbEmployeeName.addItem(element[0].toString());
			cmbEmployeeName.setItemCaption(element[0].toString(), element[1].toString()+"-"+
					element[2].toString()/*+"-"+element[3].toString()*/);
		}
	}

	public void setValue(UserInfoModel uim)
	{
		cmbRoleName.setValue(uim.getRoleId());
		txtUserName.setValue(uim.getUserName());
		txtPassword.setValue(uim.getPassWord());
		txtConfirmPass.setValue(uim.getPassWord());
		txtExpiryDate.setValue(uim.getExpiryDate());
		txtFullName.setValue(uim.getFullName());
		cmbUserType.setValue(uim.getUserType());
		loadEmployeeData(uim.getEmployeeId());
		cmbEmployeeName.setValue(uim.getEmployeeId());
		txtContactNo.setValue(uim.getMobileNumber());
		txtEmailAddress.setValue(uim.getEmailId());

		cmbRoleName.setEnabled((sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()));
		cmbBranchName.setEnabled((sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()));
		txtUserName.setEnabled((sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()));
		txtPassword.setEnabled((sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()));
		txtConfirmPass.setEnabled((sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()));
		txtExpiryDate.setEnabled((sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()));
		cmbUserType.setEnabled((sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin()));
	}

	public void txtClear()
	{
		cmbRoleName.setValue(null);
		cmbBranchName.setValue(null);

		txtFullName.setValue("");
		txtUserName.setValue("");
		txtPassword.setValue("");
		txtConfirmPass.setValue("");
		cmbEmployeeName.setValue(null);
		txtEmailAddress.setValue("");
		txtContactNo.setValue("");

		cmbUserType.setValue(null);
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(3, 11);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		cmbRoleName = new ComboBox();
		cmbRoleName.setImmediate(true);
		cmbRoleName.setFilteringMode(FilteringMode.CONTAINS);
		cmbRoleName.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbRoleName.setInputPrompt("Select Role Name");
		cmbRoleName.setRequired(true);
		cmbRoleName.setRequiredError("This field is required");
		grid.addComponent(new Label("Role Name: "), 0, 0);
		grid.addComponent(cmbRoleName, 1, 0);

		cmbBranchName = new MultiComboBox();
		cmbBranchName.setWidth("100%");
		cmbBranchName.setRequired(true);
		cmbBranchName.setInputPrompt("Select branch name.");
		cmbBranchName.setRequiredError("This field is required");
		grid.addComponent(new Label("Branch Name: "), 0, 1);
		grid.addComponent(cmbBranchName, 1, 1, 2, 1);

		txtFullName = new TextField();
		txtFullName.setWidth("100%");
		txtFullName.setImmediate(true);
		txtFullName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFullName.setInputPrompt("Full Name");
		txtFullName.setRequired(true);
		txtFullName.setRequiredError("This field is required");
		grid.addComponent(new Label("Full Name: "), 0, 2);
		grid.addComponent(txtFullName, 1, 2, 2, 2);

		txtUserName = new TextField();
		txtUserName.setImmediate(true);
		txtUserName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtUserName.setInputPrompt("Username");
		txtUserName.setRequired(true);
		txtUserName.setRequiredError("This field is required");
		grid.addComponent(new Label("User Name: "), 0, 3);
		grid.addComponent(txtUserName, 1, 3);

		txtPassword = new PasswordField();
		txtPassword.setImmediate(true);
		txtPassword.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPassword.setDescription("Password");
		txtPassword.setInputPrompt("Password");
		txtPassword.setRequired(true);
		txtPassword.setRequiredError("Provide minimum 8 digit password");
		grid.addComponent(new Label("Password: "), 0, 4);
		grid.addComponent(txtPassword, 1, 4);

		txtConfirmPass = new PasswordField();
		txtConfirmPass.setImmediate(true);
		txtConfirmPass.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtConfirmPass.setDescription("Confirm Password");
		txtConfirmPass.setInputPrompt("Password");
		txtConfirmPass.setRequired(true);
		txtConfirmPass.setRequiredError("This field is required");
		grid.addComponent(txtConfirmPass, 2, 4);

		txtExpiryDate = new PopupDateField();
		txtExpiryDate.setImmediate(true);
		txtExpiryDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtExpiryDate.setValue(new Date());
		txtExpiryDate.setDateFormat("dd-MM-yyyy");
		txtExpiryDate.setRequired(true);
		txtExpiryDate.setRequiredError("This field is required");
		grid.addComponent(new Label("Expiry Date: "), 0, 5);
		grid.addComponent(txtExpiryDate, 1, 5);

		cmbEmployeeName = new ComboBox();
		cmbEmployeeName.setImmediate(true);
		cmbEmployeeName.setFilteringMode(FilteringMode.CONTAINS);
		cmbEmployeeName.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbEmployeeName.setInputPrompt("Select Employee Name");
		cmbEmployeeName.setWidth("100%");
		Label ll = new Label("Employee Name: ");
		ll.setWidth("-1px");
		grid.addComponent(ll, 0, 6);
		grid.addComponent(cmbEmployeeName, 1, 6, 2, 6);

		txtContactNo = new TextField();
		txtContactNo.setWidth("100%");
		txtContactNo.setInputPrompt("+973 XXXX XXXX");
		txtContactNo.setImmediate(true);
		txtContactNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		Label lbl = new Label("Phone Number: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 7);
		grid.addComponent(txtContactNo, 1, 7, 2, 7);

		txtEmailAddress = new TextField();
		txtEmailAddress.setWidth("100%");
		txtEmailAddress.setInputPrompt("example@email.com");
		txtEmailAddress.setImmediate(true);
		txtEmailAddress.addStyleName(ValoTheme.TEXTFIELD_TINY);
		grid.addComponent(new Label("Email Address: "), 0, 8);
		grid.addComponent(txtEmailAddress, 1, 8, 2, 8);

		cmbUserType = new ComboBox();
		cmbUserType.setImmediate(true);
		cmbUserType.setFilteringMode(FilteringMode.CONTAINS);
		cmbUserType.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbUserType.setInputPrompt("Select User Type");
		cmbUserType.setRequired(true);
		cmbUserType.setRequiredError("This field is required");
		grid.addComponent(new Label("User Type: "), 0, 9);
		grid.addComponent(cmbUserType, 1, 9);

		return grid;
	}
}
