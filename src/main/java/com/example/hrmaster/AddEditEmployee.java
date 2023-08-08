package com.example.hrmaster;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.EmployeeInfoGateway;
import com.example.model.EmployeeInfoModel;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditEmployee extends Window
{
	private SessionBean sessionBean;
	private String flag, empId;

	private TextField txtEmployeeCode, txtContactNo, txtEmailAddress, txtCprNumber;
	private TextArea txtEmployeeName;
	private ComboBox cmbDesignation;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private EmployeeInfoGateway eig = new EmployeeInfoGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private CommonMethod cm;

	public AddEditEmployee(SessionBean sessionBean, String flag, String empId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.empId = empId;
		this.setCaption(flag+" Employee Information :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(this.sessionBean);
		setWidth("550px");
		setHeight("390px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event -> addValidation());

		cBtn.btnExit.addClickListener(event -> close());

		loadDesignation();

		if(flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void loadDesignation()
	{
		cmbDesignation.removeAllItems();
		String deptId = "D1";
		String sql = "select vDesignationId, vDesignation from master.tbDesignationMaster"+
				" where iActive = 1 and vDepartmentId = '"+deptId+"' order by vDesignation";
		//System.out.println(sqlA);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbDesignation.addItem(element[0].toString());
			cmbDesignation.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void addValidation()
	{
		if (!txtEmployeeName.getValue().toString().trim().isEmpty())
		{
			if (!eig.checkExistCode(txtEmployeeCode.getValue().toString().trim(), empId))
			{
				String cpr = txtCprNumber.getValue().toString().replaceAll("'", "").trim();
				if (!eig.checkExistCpr(cpr, empId) || cpr.isEmpty())
				{
					cBtn.btnSave.setEnabled(false);
					insertEditData();
				}
				else
				{
					txtEmployeeName.focus();
					cm.showNotification("warning", "Warning!", "Employee cpr already exist.");
				}
			}
			else
			{
				txtEmployeeName.focus();
				cm.showNotification("warning", "Warning!", "Employee code already exist.");
			}
		}
		else
		{
			txtEmployeeName.focus();
			cm.showNotification("warning", "Warning!", "Provide employee name.");
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
						EmployeeInfoModel eim = new EmployeeInfoModel();

						String empIdN = flag.equals("Add")?eig.getEmployeeId():empId;
						eim.setBranchId(sessionBean.getBranchId());

						//Officials Informations
						eim.setEmployeeId(empIdN);
						getValue(eim);
						eim.setCreatedBy(sessionBean.getUserId());

						if (eig.insertEditData(eim, flag))
						{
							txtClear();
							cm.showNotification("success", "Successfull!", "All information saved successfully.");
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

	private void getValue(EmployeeInfoModel eim)
	{
		String imagePathProfile = "";
		eim.setEmployeeCode(txtEmployeeCode.getValue().toString().trim());
		eim.setEmployeeName(txtEmployeeName.getValue().toString().trim());
		eim.setGender("");
		eim.setMaritialStatus("");
		eim.setReligion("");
		eim.setContactNo(txtContactNo.getValue().toString().trim());
		eim.setEmailId(txtEmailAddress.getValue().toString().trim());
		eim.setEmployeeTypeId("T1");
		eim.setEmpTypeDate(cm.dfBd.format(new Date()));
		eim.setNationality("");
		eim.setVisaTypeId("V1");
		eim.setVisaTypeDate(cm.dfBd.format(new Date()));
		eim.setEmployeeStatusId("S1");
		eim.setEmpStatusDate(cm.dfBd.format(new Date()));
		eim.setDateOfBirth(new Date());
		eim.setDateOfInterview(new Date());
		eim.setDateOfJoin(new Date());
		//eim.setDateOfLease(txtDateOfLease.getValue());
		eim.setDeptId("D1");
		eim.setDesigId(cmbDesignation.getValue().toString());
		eim.setEmpType("General");
		eim.setOtEnable(0);
		eim.setConfidential(0);
		eim.setEmployeePhoto(imagePathProfile);

		getValueLegal(eim);
		getValuePersonal(eim);
	}

	public void getValueLegal(EmployeeInfoModel eim)
	{
		eim.setContractId("");
		eim.setDateContStart(new Date());
		eim.setDateContEnd(new Date());

		eim.setCPRNo(txtCprNumber.getValue().toString());
		eim.setDateCPRIssue(new Date());
		eim.setDateCPRExpiry(new Date());

		eim.setPassport("");
		eim.setDatePPIssue(new Date());
		eim.setDatePPExpiry(new Date());

		eim.setResidentPermit("");
		eim.setDateRPIssue(new Date());
		eim.setDateRPExpiry(new Date());

		eim.setDrivingLicense("");
		eim.setDateDLIssue(new Date());
		eim.setDateDLExpiry(new Date());

		eim.setInsurance("");
		eim.setDateInsIssue(new Date());
		eim.setDateInsExpiry(new Date());
	}

	public void getValuePersonal(EmployeeInfoModel eim)
	{
		eim.setFatherName("");
		eim.setMotherName("");
		eim.setSpouseName("");
		eim.setBloodGroup("");
		eim.setBahrainAddress("");
		eim.setCountryAddress("");

		eim.setNomineeId("");
		eim.setNomineeChange(false);
		eim.setNomineeSql("");

		//Payroll Information
		eim.setSalaryId("");
		eim.setSalaryChange(false);
		eim.setAllowDedChange(false);
		getValuePay(eim);
		eim.setAllowDeductionSql("");

		//Education Experience Information
		eim.setEduExpId("");
		eim.setEduExpChange(false);
		eim.setEduExpSql("");

		//Dependents Experience Information
		eim.setDependentsId("");
		eim.setDependChange(false);
		eim.setDependentsSql("");

		//Leave Information
		eim.setLeaveList("");
		eim.setLeaveBalanceId("");
		eim.setLeaveChange(false);
		eim.setLeaveBalanceSql("");

		//Attachment Information
		eim.setAttachId("");
		eim.setAttachChange(false);
		eim.setAttachSql("");

		eim.setCreatedBy(sessionBean.getUserId());

	}

	public void getValuePay(EmployeeInfoModel eim)
	{
		eim.setBasicSalary(0);
		eim.setIndemPercent(0);
		eim.setIntaxPercent(0);
		eim.setGosiPercentEmp(0);
		eim.setGosiPercentCom(0);
		eim.setSalaryPayType("");
		eim.setBankId("");
		eim.setIBanNo("");
		eim.setAccountNo("");
	}

	private void setEditData()
	{
		EmployeeInfoModel eim = new EmployeeInfoModel();
		try
		{
			if (eig.selectEditData(eim, empId))
			{
				txtEmployeeCode.setValue(eim.getEmployeeCode());
				txtEmployeeName.setValue(eim.getEmployeeName());
				txtContactNo.setValue(eim.getContactNo());
				txtCprNumber.setValue(eim.getCPRNo());
				txtEmailAddress.setValue(eim.getEmailId());
				cmbDesignation.setValue(eim.getDesigId());
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(10, 7);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtEmployeeCode = new TextField();
		txtEmployeeCode.setImmediate(true);
		txtEmployeeCode.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtEmployeeCode.setWidth("100%");
		txtEmployeeCode.setInputPrompt("Employee Code");
		txtEmployeeCode.setValue(eig.getEmployeeCode());
		grid.addComponent(new Label("Employee Code: "), 0, 0);
		grid.addComponent(txtEmployeeCode, 1, 0, 4, 0);

		txtEmployeeName = new TextArea();
		txtEmployeeName.setImmediate(true);
		txtEmployeeName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtEmployeeName.setWidth("100%");
		txtEmployeeName.setHeight("50px");
		txtEmployeeName.setInputPrompt("Employee Name");
		txtEmployeeName.setRequired(true);
		txtEmployeeName.setRequiredError("This field is required");
		Label lbl = new Label("Employee Name: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 1);
		grid.addComponent(txtEmployeeName, 1, 1, 9, 1);

		txtCprNumber = new TextField();
		txtCprNumber.setImmediate(true);
		txtCprNumber.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCprNumber.setWidth("100%");
		txtCprNumber.setInputPrompt("CPR Number");
		grid.addComponent(new Label("CPR Number: "), 0, 2);
		grid.addComponent(txtCprNumber, 1, 2, 9, 2);

		txtContactNo = new TextField();
		txtContactNo.setImmediate(true);
		txtContactNo.setWidth("100%");
		txtContactNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtContactNo.setInputPrompt("Contact No");
		grid.addComponent(new Label("Contact No: "), 0, 3);
		grid.addComponent(txtContactNo, 1, 3, 9, 3);

		txtEmailAddress = new TextField();
		txtEmailAddress.setImmediate(true);
		txtEmailAddress.setWidth("100%");
		txtEmailAddress.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtEmailAddress.setInputPrompt("sample@email.com");
		grid.addComponent(new Label("Email Address: "), 0, 4);
		grid.addComponent(txtEmailAddress, 1, 4, 9, 4);

		cmbDesignation = new ComboBox();
		cmbDesignation.setImmediate(true);
		cmbDesignation.setFilteringMode(FilteringMode.CONTAINS);
		cmbDesignation.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbDesignation.setInputPrompt("Select employee designation");
		cmbDesignation.setWidth("100%");
		cmbDesignation.setRequired(true);
		cmbDesignation.setRequiredError("This field is required.");
		grid.addComponent(new Label("Designation: "), 0, 5);
		grid.addComponent(cmbDesignation, 1, 5, 9, 5);

		grid.addComponent(cBtn, 0, 6, 9, 6);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return grid;
	}

	private void focusEnter()
	{
		allComp.add(txtEmployeeCode);
		allComp.add(txtEmployeeName);
		allComp.add(txtCprNumber);
		allComp.add(txtContactNo);
		allComp.add(txtEmailAddress);
		allComp.add(cmbDesignation);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		txtEmployeeCode.setValue(eig.getEmployeeCode());
		txtEmployeeName.setValue("");
		txtContactNo.setValue("");
		txtEmailAddress.setValue("");
		cmbDesignation.setValue(null);
	}
}
