package com.example.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.CompanyInfoGateway;
import com.example.model.CompanyInfoModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditCompany extends Window
{
	private SessionBean sessionBean;
	private String flag, companyId;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private CompanyInfoGateway cig = new CompanyInfoGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private TabSheet tsCompany = new TabSheet();
	private TabMaster tabMaster;
	private TabAdditional tabAdditioal;

	private CommonMethod cm;

	public AddEditCompany(SessionBean sessionBean, String flag, String companyId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.companyId = companyId;
		cm = new CommonMethod(sessionBean);
		this.setCaption(flag+" Company Information :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		setWidth("670px");
		setHeight("700px");

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
		if (!tabMaster.txtCompanyName.getValue().toString().trim().isEmpty())
		{
			if (!tabMaster.txtAddress.getValue().toString().trim().isEmpty())
			{
				if (!tabMaster.txtLicense.getValue().toString().trim().isEmpty())
				{
					cBtn.btnSave.setEnabled(false);
					insertEditData();
				}
				else
				{
					tabMaster.txtLicense.focus();
					cm.showNotification("warning", "Warning!", "Provide CR number.");
				}
			}
			else
			{
				tabMaster.txtAddress.focus();
				cm.showNotification("warning", "Warning!", "Provide address.");
			}
		}
		else
		{
			tabMaster.txtCompanyName.focus();
			cm.showNotification("warning", "Warning!", "Provide company name.");
		}
	}

	private void insertEditData()
	{
		MessageBox mb = new MessageBox(getUI(), "Are you sure?",
				MessageBox.Icon.QUESTION, "Do you want to update information?",
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
						CompanyInfoModel cim = new CompanyInfoModel();
						getCompanyData(cim);

						if (cig.insertEditData(cim, flag))
						{
							txtClear();
							cm.showNotification("success", "Successfull!", "All information updated successfully."+
									" Please re-login again to effect the updates.");
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

	private void getCompanyData(CompanyInfoModel cim)
	{
		//String companyIdN = flag.equals("Add")?cig.getCompanyId():companyId;
		cim.setCompanyId(companyId);
		cim.setCompanyName(tabMaster.txtCompanyName.getValue().toString().trim());
		cim.setAddress(tabMaster.txtAddress.getValue().toString().trim());
		cim.setPhone(tabMaster.txtPhone.getValue().toString().trim());
		cim.setFax(tabMaster.txtFax.getValue().toString().trim());
		cim.setEmail(tabMaster.txtEmail.getValue().toString().trim());
		cim.setLicense(tabMaster.txtLicense.getValue().toString().trim());
		cim.setVatRegNo(tabMaster.txtVatRegNo.getValue().toString().trim());
		cim.setWebsite(tabMaster.txtWebsite.getValue().toString().trim());
		cim.setEmailNotification(tabMaster.chkEmailNotification.getValue().booleanValue()? 1:0);

		cim.setAddOnlineMenuName(tabAdditioal.txtOnlineMenuName.getValue().toString().trim());
		cim.setAddAddress(tabAdditioal.txtAddress.getValue().toString().trim());
		cim.setAddPhone(tabAdditioal.txtPhone.getValue().toString().trim());
		cim.setAddFax(tabAdditioal.txtFax.getValue().toString().trim());
		cim.setAddEmail(tabAdditioal.txtEmail.getValue().toString().trim());
		cim.setAddLicenseNo(tabAdditioal.txtLicense.getValue().toString().trim());
		cim.setAddVatRegNo(tabAdditioal.txtVatRegNo.getValue().toString().trim());
		cim.setAddWebsite(tabAdditioal.txtWebsite.getValue().toString().trim());
		cim.setAddURL(tabAdditioal.txtUrlLink.getValue().toString().trim());
		cim.setAddMinOrderAmount(tabAdditioal.txtMinOrderAmt.getValue().toString().trim());
		cim.setAddEmailNotification(tabAdditioal.chkEmailNotification.getValue().booleanValue()? 1:0);
		cim.setaAddStockZeroSale(tabAdditioal.chkStockZeroSale.getValue().booleanValue()? 1:0);
		cim.setSurName(sessionBean.war);
		//cim.setInvoiceType("0");
		cim.setCreatedBy(sessionBean.getUserId());

		//Logo
		byte[] imageInBytes = null;
		File imagePath = null;
		String imagePathItem = tabMaster.Image.success ? (imagePath(1, cim.getCompanyId()) == null ? "0" :
			imagePath(1, cim.getCompanyId())) : tabMaster.editImage;
		if (!imagePathItem.equals("0") && !imagePathItem.isEmpty())
		{
			try
			{
				imagePath = new File(imagePathItem);
				imageInBytes = new byte[(int)imagePath.length()];
				FileInputStream inputStream = new FileInputStream(imagePath); //input stream object create to read the file
				inputStream.read(imageInBytes); // here input stream object read the file
				inputStream.close();
				cim.setComLogo(imageInBytes);
			}
			catch (Exception e)
			{ System.out.println("Here image can't read: "+e); }
		}
		cim.setLogo(imagePathItem);

		sessionBean.setCompanyName(tabMaster.txtCompanyName.getValue().toString().trim());
		sessionBean.setCompanyAddress(tabMaster.txtAddress.getValue().toString().trim());
		sessionBean.setCompanyContact(tabMaster.txtPhone.getValue().toString().trim()+", "+
				tabMaster.txtFax.getValue().toString().trim()+", "+
				tabMaster.txtEmail.getValue().toString().trim());
		sessionBean.setCompanyLogo(imagePathItem);
	}

	private String imagePath(int flag, String path)
	{
		String basePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"".replace("\\","/")+"/VAADIN/rpttmp/";
		String ReturnImagePath = "0";

		if (flag == 1)
		{
			// image move
			if (tabMaster.Image.fileName.trim().length()>0)
			{
				if (tabMaster.Image.fileName.toString().endsWith(".jpg"))
				{
					String Origin = basePath + tabMaster.Image.fileName.trim();
					String Destin = tabMaster.companyLogo + path + ".jpg";
					//System.out.println(Origin);

					//Copying file
					try { fileCopy(Origin, Destin); }
					catch (IOException e) { e.printStackTrace(); }
					ReturnImagePath = Destin;
				}
				else
				{
					tabMaster.Image.upload.focus();
					cm.showNotification("warning", "Warning!", "Invalid image format(jpg only).");
				}
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

	@SuppressWarnings("deprecation")
	private void companyLogo(String img)
	{
		String main = img.equals("0")?sessionBean.imagePathLogo+"default.png":img;
		File fileStu_I = new File(main);

		Embedded eStu_I = new Embedded("", new FileResource(fileStu_I));
		eStu_I.requestRepaint();
		eStu_I.setWidth("140px");
		eStu_I.setHeight("100px");

		tabMaster.Image.image.removeAllComponents();
		tabMaster.Image.image.addComponent(eStu_I);
	}

	private void setEditData()
	{
		CompanyInfoModel cim = new CompanyInfoModel();
		try
		{
			if (cig.selectEditData(cim, companyId))
			{
				tabMaster.txtCompanyName.setValue(cim.getCompanyName());
				tabMaster.txtAddress.setValue(cim.getAddress());
				tabMaster.txtPhone.setValue(cim.getPhone());
				tabMaster.txtFax.setValue(cim.getFax());
				tabMaster.txtEmail.setValue(cim.getEmail());
				tabMaster.txtLicense.setValue(cim.getLicense());
				tabMaster.txtVatRegNo.setValue(cim.getVatRegNo());
				tabMaster.txtWebsite.setValue(cim.getWebsite());
				tabMaster.chkEmailNotification.setValue(cim.getEmailNotification()==1? true:false);

				tabAdditioal.txtOnlineMenuName.setValue(cim.getAddOnlineMenuName());
				tabAdditioal.txtAddress.setValue(cim.getAddAddress());
				tabAdditioal.txtPhone.setValue(cim.getAddPhone());
				tabAdditioal.txtFax.setValue(cim.getAddFax());
				tabAdditioal.txtEmail.setValue(cim.getAddEmail());
				tabAdditioal.txtLicense.setValue(cim.getAddLicenseNo());
				tabAdditioal.txtVatRegNo.setValue(cim.getAddVatRegNo());
				tabAdditioal.txtWebsite.setValue(cim.getAddWebsite());
				tabAdditioal.txtUrlLink.setValue(cim.getAddURL());
				tabAdditioal.txtMinOrderAmt.setValue(Double.parseDouble(cim.getAddMinOrderAmount()));
				tabAdditioal.chkEmailNotification.setValue(cim.getAddEmailNotification()==1? true:false);
				tabAdditioal.chkStockZeroSale.setValue(cim.getAddStockZeroSale()==1? true:false);

				companyLogo(cim.getLogo());
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private VerticalLayout buildLayout()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSizeFull();

		tsCompany.setStyleName("framed padded-tabbar");
		tsCompany.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

		tabMaster = new TabMaster(flag);
		tabAdditioal = new TabAdditional(flag);

		tsCompany.addTab(tabMaster, "Master Information", FontAwesome.BUILDING, 0);
		tsCompany.addTab(tabAdditioal, "Additional Information", FontAwesome.DESKTOP, 1);

		layout.addComponents(tsCompany, cBtn);
		layout.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return layout;
	}

	private void focusEnter()
	{
		allComp.add(tabMaster.txtCompanyName);
		allComp.add(tabMaster.txtAddress);
		allComp.add(tabMaster.txtPhone);
		allComp.add(tabMaster.txtFax);
		allComp.add(tabMaster.txtEmail);
		allComp.add(tabMaster.txtLicense);
		allComp.add(tabMaster.txtVatRegNo);
		allComp.add(tabMaster.txtWebsite);
		allComp.add(tabAdditioal.txtOnlineMenuName);
		allComp.add(tabAdditioal.txtAddress);
		allComp.add(tabAdditioal.txtPhone);
		allComp.add(tabAdditioal.txtFax);
		allComp.add(tabAdditioal.txtEmail);
		allComp.add(tabAdditioal.txtLicense);
		allComp.add(tabAdditioal.txtVatRegNo);
		allComp.add(tabAdditioal.txtWebsite);
		allComp.add(tabAdditioal.txtUrlLink);
		allComp.add(tabAdditioal.txtMinOrderAmt);

		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		/*txtCompanyName.setValue("");
		txtAddress.setValue("");
		txtPhone.setValue(""); 
		txtFax.setValue("");
		txtEmail.setValue("");
		txtLicense.setValue("");
		txtVatRegNo.setValue("");
		txtWebsite.setValue("");*/
	}
}
