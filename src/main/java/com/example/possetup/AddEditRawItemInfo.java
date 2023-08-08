package com.example.possetup;

import java.util.ArrayList;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.ItemInfoGateway;
import com.example.model.ItemInfoModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditRawItemInfo extends Window
{
	private SessionBean sessionBean;
	private String flag, rawItemId;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private ItemInfoGateway iig = new ItemInfoGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private TabSheet tsRawItem = new TabSheet();
	private TabRawItemMaster tabRawMaster;
	//private TabRawItemProfile tabRawProfile;

	private CommonMethod cm;

	public AddEditRawItemInfo(SessionBean sessionBean, String flag, String rawItemId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.rawItemId = rawItemId;
		this.setCaption(flag+" Inventory Item Information :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(this.sessionBean);
		setWidth("600px");
		setHeight("610px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
		{ addValidation(); });

		cBtn.btnExit.addClickListener(event ->
		{ close(); });

		/*tabRawMaster.cmbUnit.addValueChangeListener(event ->
		{
			if (tabRawMaster.cmbUnit.getValue() != null)
			{ tabRawProfile.setTableColumn(tabRawMaster.cmbUnit.getValue().toString()); }
		});*/

		/*tabRawMaster.ogRawItemType.addValueChangeListener(event ->
		{
			String val = tabRawMaster.ogRawItemType.getValue().toString();
			if (val.equals("Raw"))
			{ tabRawProfile.tableClear(); }
			tabRawProfile.setEnabled(!val.equals("Raw"));
		});*/

		//To effect immediately
		tabRawMaster.ogRawItemType.select("Raw");

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void addValidation()
	{
		if (!iig.checkExistRawCode(tabRawMaster.txtItemCode.getValue().toString().trim(), rawItemId))
		{
			if (!tabRawMaster.txtItemName.getValue().toString().trim().isEmpty())
			{
				if (!iig.checkExistRaw(tabRawMaster.txtItemName.getValue().toString().trim(), rawItemId))
				{
					if (tabRawMaster.cmbCategory.getValue() != null)
					{
						if (tabRawMaster.cmbUnit.getValue() != null)
						{
							if (tabRawMaster.cmbVatCategory.getValue() != null)
							{
								if (!tabRawMaster.txtIssueRate.getValue().toString().isEmpty())
								{
									//if (tabRawMaster.ogRawItemType.getValue().toString().equals("Raw") || checkProfile())
									{
										cBtn.btnSave.setEnabled(false);
										insertEditData();
									}
									/*else
									{
										tsRawItem.setSelectedTab(1);
										//tabRawProfile.tbCmbItemName.get(0).focus();
										cm.showNotification("warning", "Warning!", "Select profile items.");
									}*/
								}
								else
								{
									tsRawItem.setSelectedTab(0);
									tabRawMaster.txtIssueRate.focus();
									cm.showNotification("warning", "Warning!", "Provide issue rate.");
								}
							}
							else
							{
								tsRawItem.setSelectedTab(0);
								tabRawMaster.cmbVatCategory.focus();
								cm.showNotification("warning", "Warning!", "Select vat category.");
							}
						}
						else
						{
							tsRawItem.setSelectedTab(0);
							tabRawMaster.cmbUnit.focus();
							cm.showNotification("warning", "Warning!", "Select unit.");
						}
					}
					else
					{
						tsRawItem.setSelectedTab(0);
						tabRawMaster.cmbCategory.focus();
						cm.showNotification("warning", "Warning!", "Select category.");
					}
				}
				else
				{
					tsRawItem.setSelectedTab(0);
					tabRawMaster.txtItemName.focus();
					cm.showNotification("warning", "Warning!", "Item name already exist.");
				}
			}
			else
			{
				tsRawItem.setSelectedTab(0);
				tabRawMaster.txtItemName.focus();
				cm.showNotification("warning", "Warning!", "Provide item name.");
			}
		}
		else
		{
			tsRawItem.setSelectedTab(0);
			tabRawMaster.txtItemCode.focus();
			cm.showNotification("warning", "Warning!", "Item code already exist.");
		}
	}

	/*private boolean checkProfile()
	{
		boolean ret = false;
		for (int ar = 0; ar < tabRawProfile.tbCmbItemName.size(); ar++)
		{
			if (tabRawProfile.tbCmbItemName.get(ar).getValue() != null &&
					cm.getAmtValue(tabRawProfile.tbTxtSize1Qty.get(ar))>0)
			{ ret = true; break; }
		}
		return ret;
	}*/

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
						ItemInfoModel iim = new ItemInfoModel();
						String itemIdN = flag.equals("Add")?iig.getItemIdRaw():rawItemId;
						iim.setItemId(itemIdN);

						tabRawMaster.getValue(iim);
						//tabRawProfile.getValue(iim, flag);

						if (iig.insertEditDataRaw(iim, flag))
						{
							tabRawMaster.txtClear();
							//tabRawProfile.tableClear();
							tsRawItem.setSelectedTab(0);
							tabRawMaster.txtItemCode.focus();

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
		ItemInfoModel iim = new ItemInfoModel();
		try
		{
			if (iig.selectEditDataRaw(iim, rawItemId))
			{
				tabRawMaster.txtItemCode.setValue(iim.getKitchenName());
				tabRawMaster.ogRawItemType.select(iim.getItemType());
				tabRawMaster.txtItemName.setValue(iim.getItemName());
				tabRawMaster.txtItemNameAr.setValue(iim.getItemNameArabic());
				tabRawMaster.txtBarcode.setValue(iim.getBarCode());
				tabRawMaster.cmbCategory.select(iim.getRawCategory());
				tabRawMaster.cmbUnit.select(iim.getRawUnit());
				tabRawMaster.cmbVatCategory.select(iim.getVatCategoryId());
				tabRawMaster.txtIssueRate.setValue(iim.getIssueRate());
				tabRawMaster.txtCostMargin.setValue(iim.getCostMargin());
				setEditSupplier(iim.getSupplierIds());
				//setEditProfile();
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex);  cm.showNotification("failure", "Error!", ex.toString());  }
	}

	private void setEditSupplier(String supIds)
	{
		String sqls = "select Item, 0 zero from dbo.Split('"+supIds+"')";
		//System.out.println(sqls);
		for (Iterator<?> iter = cm.selectSql(sqls).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tabRawMaster.cmbSupplierName.select(element[0].toString());
		}
	}

	/*private void setEditProfile()
	{
		String sql = "select vProfileId, vItemIdProfile, mUnitQty1 from master.tbRawItemProfile"+
				" where vItemId = '"+rawItemId+"' order by iAutoId";
		//System.out.println(sql);
		int ar = 0;
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			tabRawProfile.tbLblPrfileId.get(ar).setValue(element[0].toString());
			tabRawProfile.tbCmbItemName.get(ar).setValue(element[1].toString());
			tabRawProfile.tbTxtSize1Qty.get(ar).setValue(Double.parseDouble(element[2].toString()));
			ar++;
		}
		tabRawProfile.action = true;
	}*/

	private VerticalLayout buildLayout()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();

		tsRawItem.setStyleName("framed padded-tabbar");
		tsRawItem.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

		tabRawMaster = new TabRawItemMaster(sessionBean, flag);
		//tabRawProfile = new TabRawItemProfile(sessionBean, flag);

		tsRawItem.addTab(tabRawMaster, "Master Information", FontAwesome.DOWNLOAD, 0);
		//tsRawItem.addTab(tabRawProfile, "Profile Information", FontAwesome.BOOK, 1);

		//tabRawProfile.setVisible(false);

		layout.addComponents(tsRawItem, cBtn);
		layout.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return layout;
	}

	private void focusEnter()
	{
		allComp.add(tabRawMaster.txtItemCode);
		allComp.add(tabRawMaster.txtItemName);
		allComp.add(tabRawMaster.txtItemNameAr);
		allComp.add(tabRawMaster.txtBarcode);
		allComp.add(tabRawMaster.cmbSupplierName);
		allComp.add(tabRawMaster.cmbCategory);
		allComp.add(tabRawMaster.cmbUnit);
		allComp.add(tabRawMaster.cmbVatCategory);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}
}
