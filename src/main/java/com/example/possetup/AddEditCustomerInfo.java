package com.example.possetup;

import java.util.ArrayList;
import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.SupplierInfoGateway;
import com.example.model.SupplierInfoModel;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditCustomerInfo extends Window
{
	private SessionBean sessionBean;
	private String flag, customerId;

	private TextField txtCustomerCode, txtCustomerName, txtMobile,
	txtVatRegNo, txtEmailAddress;
	private CommaField txtCreditDays;

	private Table tblAddressList;
	private ArrayList<TextField> tbTxtArea = new ArrayList<TextField>();
	private ArrayList<TextField> tbTxtBulding = new ArrayList<TextField>();
	private ArrayList<TextField> tbTxtFlat = new ArrayList<TextField>();
	private ArrayList<TextField> tbTxtBlock = new ArrayList<TextField>();
	private ArrayList<TextField> tbTxtRoad = new ArrayList<TextField>();

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private SupplierInfoGateway sig = new SupplierInfoGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private CommonMethod cm;

	public AddEditCustomerInfo(SessionBean sessionBean, String flag, String customerId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.customerId = customerId;
		this.setCaption(flag+" Customer Information :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(this.sessionBean);
		setWidth("700px");
		setHeight("520px");

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
		if (!sig.checkExistCodeCust(txtCustomerCode.getValue().toString().trim(), customerId))
		{
			if (!txtCustomerName.getValue().toString().trim().isEmpty())
			{
				if (!sig.checkExistCust(txtCustomerName.getValue().toString().trim(), customerId))
				{
					cBtn.btnSave.setEnabled(false);
					insertEditData();
				}
				else
				{
					txtCustomerName.focus();
					cm.showNotification("warning", "Warning!", "Customer name already exist.");
				}
			}
			else
			{
				txtCustomerName.focus();
				cm.showNotification("warning", "Warning!", "Provide customer name.");
			}
		}
		else
		{
			txtCustomerCode.focus();
			cm.showNotification("warning", "Warning!", "Customer code already exist.");
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
						SupplierInfoModel cim = new SupplierInfoModel();
						String customerIdN = flag.equals("Add")?sig.getCustomerId(sessionBean.getBranchId()):customerId;
						cim.setBranchId(sessionBean.getBranchId());
						cim.setSupplierId(customerIdN);
						cim.setSupplierCode(txtCustomerCode.getValue().toString().trim());
						cim.setSupplierName(txtCustomerName.getValue().toString().trim());
						cim.setVatRegNo(txtVatRegNo.getValue().toString().trim());
						cim.setPhone(txtMobile.getValue().toString().trim());
						cim.setEmail(txtEmailAddress.getValue().toString().trim());
						cim.setCreditLimit(txtCreditDays.getValue().toString().trim());
						cim.setDetailsSql(getDetailsSql(customerIdN));
						cim.setCreatedBy(sessionBean.getUserId());
						//System.out.println(getDetailsSql(customerIdN));
						if (sig.insertEditDataCust(cim, flag))
						{
							txtClear();
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

	private String getDetailsSql(String customerId)
	{
		String sql = " ";
		for (int ar = 0; ar < tblAddressList.size(); ar++)
		{
			if (ar == 0)
			{
				sql = "delete from master.tbCustomerAddresses where vCustomerId = '"+customerId+"' insert into"+
						" master.tbCustomerAddresses(vAddId, vCustomerId, vArea, vBuildingNo, vFlatNo, vBlockNo,"+
						" vRoadNo) values "; 
			}
			if (!tbTxtArea.get(ar).getValue().toString().isEmpty() || !tbTxtBlock.get(ar).getValue().toString().isEmpty() ||
					!tbTxtBulding.get(ar).getValue().toString().isEmpty() || !tbTxtFlat.get(ar).getValue().toString().isEmpty() ||
					!tbTxtRoad.get(ar).getValue().toString().isEmpty())
			{
				sql = sql + "('"+(ar+"-"+customerId)+"',"+
						" '"+customerId+"', '"+tbTxtArea.get(ar).getValue().toString().trim().replaceAll("'", "")+"',"+
						" '"+tbTxtBulding.get(ar).getValue().toString().trim().replaceAll("'", "")+"',"+
						" '"+tbTxtFlat.get(ar).getValue().toString().trim().replaceAll("'", "")+"',"+
						" '"+tbTxtBlock.get(ar).getValue().toString().trim().replaceAll("'", "")+"',"+
						" '"+tbTxtRoad.get(ar).getValue().toString().trim().replaceAll("'", "")+"'),";
			}
		}
		return sql.substring(0, sql.length()-1);
	}

	private void setEditData()
	{
		int ar = 0;
		try
		{
			String sql = "select vCustomerCode, vCustomerName, vMobileNo, vEmailId, iCreditLimit, ISNULL(vAddId, '')"+
					" vAddId, ISNULL(vArea, '')vArea, ISNULL(vBuildingNo, '')vBuildingNo, ISNULL(vFlatNo, '')vFlatNo,"+
					" ISNULL(vBlockNo,'')vBlockNo, ISNULL(vRoadNo, '')vRoadNo, vVatRegNo from master.tbCustomerInfo"+
					" cus left join master.tbCustomerAddresses cua on cus.vCustomerId = cua.vCustomerId where"+
					" cus.vCustomerId = '"+customerId+"'";
			//System.out.println(sql);
			for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				if (ar == 0)
				{
					txtCustomerCode.setValue(element[0].toString());
					txtCustomerName.setValue(element[1].toString());
					txtMobile.setValue(element[2].toString());
					txtEmailAddress.setValue(element[3].toString());
					txtCreditDays.setValue(element[4].toString());
					txtVatRegNo.setValue(element[11].toString());
				}

				tbTxtArea.get(ar).setValue(element[6].toString());
				tbTxtBulding.get(ar).setValue(element[7].toString());
				tbTxtFlat.get(ar).setValue(element[8].toString());
				tbTxtBlock.get(ar).setValue(element[9].toString());
				tbTxtRoad.get(ar).setValue(element[10].toString());
				ar++;
			}
			if (ar == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
		}
		catch (Exception e)
		{ System.out.println("Table Query "+e); }
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(8, 8);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtCustomerCode = new TextField();
		txtCustomerCode.setImmediate(true);
		txtCustomerCode.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCustomerCode.setWidth("100%");
		txtCustomerCode.setInputPrompt("Customer Code");
		txtCustomerCode.setValue(sig.getCustCode());
		grid.addComponent(new Label("Customer Code: "), 0, 0);
		grid.addComponent(txtCustomerCode, 1, 0, 3, 0);

		txtCustomerName = new TextField();
		txtCustomerName.setImmediate(true);
		txtCustomerName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCustomerName.setWidth("100%");
		txtCustomerName.setInputPrompt("Customer Name");
		txtCustomerName.setRequired(true);
		txtCustomerName.setRequiredError("This field is required");
		Label lbl = new Label("Customer Name: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 1);
		grid.addComponent(txtCustomerName, 1, 1, 7, 1);

		txtVatRegNo = new TextField();
		txtVatRegNo.setImmediate(true);
		txtVatRegNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtVatRegNo.setWidth("100%");
		txtVatRegNo.setInputPrompt("VAT Reg. No");
		grid.addComponent(new Label("VAT Reg. No: "), 0, 2);
		grid.addComponent(txtVatRegNo, 1, 2, 7, 2);

		txtMobile = new TextField();
		txtMobile.setImmediate(true);
		txtMobile.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtMobile.setWidth("100%");
		txtMobile.setInputPrompt("Contact No");
		grid.addComponent(new Label("Contact No: "), 0, 3);
		grid.addComponent(txtMobile, 1, 3, 7, 3);

		txtEmailAddress = new TextField();
		txtEmailAddress.setImmediate(true);
		txtEmailAddress.setWidth("100%");
		txtEmailAddress.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtEmailAddress.setInputPrompt("sample@email.com");
		grid.addComponent(new Label("Email Address: "), 0, 4);
		grid.addComponent(txtEmailAddress, 1, 4, 7, 4);

		txtCreditDays = new CommaField();
		txtCreditDays.setImmediate(true);
		txtCreditDays.setWidth("100%");
		txtCreditDays.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCreditDays.setInputPrompt("Credit Limit");
		grid.addComponent(new Label("Credit Limit: "), 0, 5);
		grid.addComponent(txtCreditDays, 1, 5);
		grid.addComponent(new Label("(Days)"), 2, 5);

		grid.addComponent(buildTable(), 0, 6, 7, 6);

		grid.addComponent(cBtn, 0, 7, 7, 7);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return grid;
	}

	private Table buildTable()
	{
		tblAddressList = new Table();
		tblAddressList.setSelectable(true);
		tblAddressList.setColumnCollapsingAllowed(true);
		tblAddressList.addStyleName(ValoTheme.TABLE_SMALL);
		tblAddressList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblAddressList.setPageLength(3);
		tblAddressList.setWidth("100%");

		tblAddressList.addContainerProperty("Area", TextField.class, new TextField(), null, null, Align.CENTER);

		tblAddressList.addContainerProperty("Building", TextField.class, new TextField(), null, null, Align.CENTER);
		tblAddressList.setColumnWidth("Building", 90);

		tblAddressList.addContainerProperty("Flat", TextField.class, new TextField(), null, null, Align.CENTER);
		tblAddressList.setColumnWidth("Flat", 90);

		tblAddressList.addContainerProperty("Block", TextField.class, new TextField(), null, null, Align.CENTER);
		tblAddressList.setColumnWidth("Block", 90);

		tblAddressList.addContainerProperty("Road", TextField.class, new TextField(), null, null, Align.CENTER);
		tblAddressList.setColumnWidth("Road", 90);

		for (int ar = 0; ar < 3; ar++)
		{ tableRowAdd(ar); }

		return tblAddressList;
	}

	public void tableRowAdd(int ar)
	{
		try
		{
			tbTxtArea.add(ar, new TextField());
			tbTxtArea.get(ar).setWidth("100%");
			tbTxtArea.get(ar).setImmediate(true);
			tbTxtArea.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);

			tbTxtBulding.add(ar, new TextField());
			tbTxtBulding.get(ar).setWidth("100%");
			tbTxtBulding.get(ar).setImmediate(true);
			tbTxtBulding.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);

			tbTxtFlat.add(ar, new TextField());
			tbTxtFlat.get(ar).setWidth("100%");
			tbTxtFlat.get(ar).setImmediate(true);
			tbTxtFlat.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);

			tbTxtBlock.add(ar, new TextField());
			tbTxtBlock.get(ar).setWidth("100%");
			tbTxtBlock.get(ar).setImmediate(true);
			tbTxtBlock.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);

			tbTxtRoad.add(ar, new TextField());
			tbTxtRoad.get(ar).setWidth("100%");
			tbTxtRoad.get(ar).setImmediate(true);
			tbTxtRoad.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);

			tblAddressList.addItem(new Object[]{tbTxtArea.get(ar), tbTxtBulding.get(ar),
					tbTxtFlat.get(ar), tbTxtBlock.get(ar), tbTxtRoad.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void focusEnter()
	{
		allComp.add(txtCustomerCode);
		allComp.add(txtCustomerName);
		allComp.add(txtVatRegNo);
		allComp.add(txtMobile);
		allComp.add(txtEmailAddress);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		txtCustomerCode.setValue("");
		txtCustomerName.setValue("");
		txtVatRegNo.setValue("");
		txtMobile.setValue("");
		txtEmailAddress.setValue("");
		tableClear();
	}

	private void tableClear()
	{
		for (int ar = 0; ar < tblAddressList.size(); ar++)
		{
			tbTxtBulding.get(ar).setValue("");
			tbTxtFlat.get(ar).setValue("");
			tbTxtArea.get(ar).setValue("");
			tbTxtBlock.get(ar).setValue("");
			tbTxtRoad.get(ar).setValue("");
		}
	}
}
