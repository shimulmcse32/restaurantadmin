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
import com.example.gateway.UserInfoGateway;
import com.example.model.UserInfoModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditUser extends Window
{
	private SessionBean sessionBean;
	private String flag, userId;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private UserInfoGateway uig = new UserInfoGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private TabSheet tsUser = new TabSheet();
	private TabWebUser tabWeb;
	private TabPosUser tabPOS;

	private CommonMethod cm;

	public AddEditUser(SessionBean sessionBean, String flag, String userId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.userId = userId;
		this.setCaption(flag+" User Information :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		setWidth("650px");
		setHeight("620px");

		cm = new CommonMethod(sessionBean);

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event -> addValidation());

		cBtn.btnExit.addClickListener(event -> close());

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void addValidation()
	{
		if (tabWeb.cmbRoleName.getValue() != null)
		{
			if (tabWeb.cmbBranchName.getValue() != null)
			{
				if (!tabWeb.txtFullName.getValue().toString().trim().isEmpty())
				{
					if (!tabWeb.txtUserName.getValue().toString().trim().isEmpty())
					{
						if (!uig.checkExist(tabWeb.txtUserName.getValue().toString().trim(), userId))
						{
							if (!tabWeb.txtPassword.getValue().toString().isEmpty())
							{
								/*if (uig.checkPass(txtPassword.getValue().toString()))
								{*/
								if (tabWeb.txtPassword.getValue().toString().equals(tabWeb.txtConfirmPass.getValue().toString()))
								{
									if (tabWeb.txtExpiryDate.getValue() != null)
									{
										if (tabWeb.cmbUserType.getValue() != null)
										{
											cBtn.btnSave.setEnabled(false);
											insertEditData();
										}
										else
										{
											tsUser.setSelectedTab(0);
											tabWeb.cmbUserType.focus();
											cm.showNotification("warning", "Warning!", "Select user type.");
										}
									}
									else
									{
										tsUser.setSelectedTab(0);
										tabWeb.txtExpiryDate.focus();
										cm.showNotification("warning", "Warning!", "Select expiry date.");
									}
								}
								else
								{
									tsUser.setSelectedTab(0);
									tabWeb.txtConfirmPass.focus();
									cm.showNotification("warning", "Warning!", "Both password mismatched.");
								}
								/*}
								else
								{
									txtPassword.focus();
									cm.showNotification("warning", "Warning!", "Provide minimum 8 digit password.");
								}*/
							}
							else
							{
								tsUser.setSelectedTab(0);
								tabWeb.txtPassword.focus();
								cm.showNotification("warning", "Warning!", "Provide password.");
							}
						}
						else
						{
							tsUser.setSelectedTab(0);
							tabWeb.txtUserName.focus();
							cm.showNotification("warning", "Warning!", "User name already exist.");
						}
					}
					else
					{
						tsUser.setSelectedTab(0);
						tabWeb.txtUserName.focus();
						cm.showNotification("warning", "Warning!", "Provide user name.");
					}
				}
				else
				{
					tsUser.setSelectedTab(0);
					tabWeb.txtFullName.focus();
					cm.showNotification("warning", "Warning!", "Provide first name.");
				}
			}
			else
			{
				tsUser.setSelectedTab(0);
				tabWeb.cmbBranchName.focus();
				cm.showNotification("warning", "Warning!", "Select branch name.");
			}
		}
		else
		{
			tsUser.setSelectedTab(0);
			tabWeb.cmbRoleName.focus();
			cm.showNotification("warning", "Warning!", "Select role name.");
		}
	}

	private void insertEditData()
	{
		MessageBox mb = new MessageBox(getUI(), "Are you sure?",
				MessageBox.Icon.QUESTION, "Do you want to save information?",
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
						String userIdN = flag.equals("Add")?uig.getUserId():userId;
						uim.setRoleId(tabWeb.cmbRoleName.getValue().toString());
						uim.setBranchId(tabWeb.cmbBranchName.getValue().toString().replace("[", "").replace("]", "").trim());
						uim.setUserId(userIdN);
						uim.setUserName(tabWeb.txtUserName.getValue().toString().trim());
						uim.setPassWord(tabWeb.txtPassword.getValue().toString());
						uim.setExpiryDate(tabWeb.txtExpiryDate.getValue());
						uim.setFullName(tabWeb.txtFullName.getValue().toString().trim());
						uim.setMobileNumber(tabWeb.txtContactNo.getValue().toString().trim());
						uim.setEmailId(tabWeb.txtEmailAddress.getValue().toString().trim());
						uim.setUserType(tabWeb.cmbUserType.getValue().toString());
						uim.setEmployeeId(tabWeb.cmbEmployeeName.getValue() != null? tabWeb.cmbEmployeeName.getValue().toString():"");
						uim.setUserPicture("0");
						uim.setCreatedBy(sessionBean.getUserId());
						uim.setPosAccess(tabPOS.getPosValue());
						uim.setAppAccess(tabPOS.getAppValue());
						if (uig.insertEditData(uim, flag))
						{
							tabWeb.txtClear();
							tabPOS.chkClear();
							cm.showNotification("success", "Successfull!", "All information saved successfully.");
							cBtn.btnSave.setEnabled(true);

							if (flag.equals("Edit"))
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

	private void setEditData()
	{
		UserInfoModel uim = new UserInfoModel();
		try
		{
			if (uig.selectEditData(uim, userId))
			{
				tabWeb.setValue(uim);
				setMultiCombo(uim.getBranchId());
				tabPOS.setPosValue(uim.getPosAccess());
				tabPOS.setAppValue(uim.getAppAccess());
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private void setMultiCombo(String branchIds)
	{
		String sql = "select Item, 'Branch' vValue from dbo.Split('"+branchIds+"')";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			if (element[1].toString().equals("Branch"))
			{ tabWeb.cmbBranchName.select(element[0].toString()); }
		}
	}

	private VerticalLayout buildLayout()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSizeFull();

		tsUser.setStyleName("framed padded-tabbar");
		tsUser.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

		tabWeb = new TabWebUser(sessionBean, flag);
		tabPOS = new TabPosUser(sessionBean, flag);

		tsUser.addTab(tabWeb, "Back Office User", FontAwesome.USERS, 0);
		tsUser.addTab(tabPOS, "POS & Waiter App User", FontAwesome.DESKTOP, 1);

		layout.addComponents(tsUser, cBtn);
		layout.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return layout;
	}

	private void focusEnter()
	{
		allComp.add(tabWeb.cmbRoleName);
		allComp.add(tabWeb.cmbBranchName);
		allComp.add(tabWeb.txtFullName);
		allComp.add(tabWeb.txtUserName);
		allComp.add(tabWeb.txtPassword);
		allComp.add(tabWeb.txtConfirmPass);
		allComp.add(tabWeb.txtExpiryDate);
		allComp.add(tabWeb.cmbEmployeeName);
		allComp.add(tabWeb.txtContactNo);
		allComp.add(tabWeb.txtEmailAddress);
		allComp.add(tabWeb.cmbUserType);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}
}
