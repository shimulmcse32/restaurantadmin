package com.example.possetup;

import java.util.ArrayList;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.SessionBean;
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
public class AddEditFinishedItem extends Window
{
	private SessionBean sessionBean;
	private String flag, itemId;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private ItemInfoGateway iig = new ItemInfoGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	//All tabs classes
	private TabSheet tsItem = new TabSheet();
	private TabItemMasterInfo tabItemMaster;
	private TabItemAddModifier tabItemAddModif;
	private TabItemProfileInfo tabItemProfile;
	private TabItemInventory tabItemInventory;

	private CommonMethod cm;

	public AddEditFinishedItem(SessionBean sessionBean, String flag, String itemId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.itemId = itemId;
		this.setCaption(flag+" Menu Information :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);
		setWidth("1200px");
		setHeight("650px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event -> masterValidation());

		cBtn.btnExit.addClickListener(event -> close());

		tabItemMaster.ogItemType.addValueChangeListener(event ->
		{
			tabItemMaster.loadCategory();
			enableModifier();
			checkModifier();
		});

		tabItemMaster.chkInventory.addValueChangeListener(event ->
		{
			boolean chk = tabItemMaster.chkInventory.getValue().booleanValue();
			tabItemInventory.setEnabled(chk);
		});

		tabItemMaster.cmbPackageName.addValueChangeListener(event -> setUnitToTable());

		//Set default size
		tabItemMaster.chkOnlineMenu.setValue(true);
		tabItemMaster.chkInventory.setValue(true);
		tabItemMaster.cmbPackageName.select("1");

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void setUnitToTable()
	{
		String ids = cm.getMultiComboValue(tabItemMaster.cmbPackageName);
		tabItemMaster.tableClear();
		int ar = 0;
		try
		{
			String sql = "select iUnitId, vUnitName from master.tbUnitInfo where iUnitId in"+
					" (select Item from dbo.Split('"+ids+"')) order by iUnitId";
			//System.out.println(sql);
			for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tabItemMaster.tbLblPackageId.size() <= ar)
				{ tabItemMaster.tableRowAdd(ar); }

				tabItemMaster.tbLblPackageId.get(ar).setValue(element[0].toString());
				tabItemMaster.tbLblPackageName.get(ar).setValue(element[1].toString());
				ar++;
			}
			tabItemMaster.setHashmapValue();
			tabItemProfile.setTableColumn(ids);
			checkModifier();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}
	
	private void checkModifier()
	{
		String type = tabItemMaster.ogItemType.getValue().toString(); 
		if (type.equals("Modifier") || type.equals("Both"))
		{
			if (tabItemMaster.tbLblPackageId.size() > 1)
			{
				cm.showNotification("warning", "Warning!", "Multiple sizes can't be selected for "+type);
				tabItemMaster.cmbPackageName.unselectAll();
			}
		}
	}

	private void masterValidation()
	{
		if (!cm.getMultiComboValue(tabItemMaster.cmbBranchName).isEmpty())
		{
			if (!tabItemMaster.txtItemName.getValue().toString().trim().isEmpty())
			{
				if (!iig.checkExist(tabItemMaster.txtItemName.getValue().toString().trim(), itemId))
				{
					if (!cm.getMultiComboValue(tabItemMaster.cmbPackageName).isEmpty())
					{
						if (tabItemMaster.cmbFinishCategory.getValue() != null)
						{
							if (!cm.getMultiComboValue(tabItemMaster.cmbSalesType).isEmpty())
							{
								if (tabItemMaster.cmbVatCategory.getValue() != null)
								{
									if (tabItemMaster.getTableData())
									{
										cBtn.btnSave.setEnabled(false);
										insertEditData();
									}
									else
									{
										tsItem.setSelectedTab(0);
										tabItemMaster.tbTxtMainPrice.get(0).focus();
										cm.showNotification("warning", "Warning!", "Provide main price.");
									}
								}
								else
								{
									tsItem.setSelectedTab(0);
									tabItemMaster.cmbVatCategory.focus();
									cm.showNotification("warning", "Warning!", "Select vat category.");
								}
							}
							else
							{
								tsItem.setSelectedTab(0);
								tabItemMaster.cmbSalesType.focus();
								cm.showNotification("warning", "Warning!", "Select sales type.");
							}
						}
						else
						{
							tsItem.setSelectedTab(0);
							tabItemMaster.cmbFinishCategory.focus();
							cm.showNotification("warning", "Warning!", "Select menu category.");
						}
					}
					else
					{
						tsItem.setSelectedTab(0);
						tabItemMaster.cmbPackageName.focus();
						cm.showNotification("warning", "Warning!", "Select menu package(s).");
					}
				}
				else
				{
					tsItem.setSelectedTab(0);
					tabItemMaster.txtItemName.focus();
					cm.showNotification("warning", "Warning!", "Menu already exist.");
				}
			}
			else
			{
				tsItem.setSelectedTab(0);
				tabItemMaster.txtItemName.focus();
				cm.showNotification("warning", "Warning!", "Provide menu name.");
			}
		}
		else
		{
			tsItem.setSelectedTab(0);
			tabItemMaster.cmbBranchName.focus();
			cm.showNotification("warning", "Warning!", "Select branch name.");
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
						ItemInfoModel iim = new ItemInfoModel();
						String itemIdN = flag.equals("Add")? iig.getItemId():itemId;
						iim.setItemId(itemIdN);

						tabItemMaster.getValue(iim);
						tabItemAddModif.getValue(iim);
						tabItemProfile.getValue(iim);
						tabItemInventory.getValue(iim);

						if (iig.insertEditData(iim, flag))
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
					{ System.out.println("Save/Update Error: "+ex); }
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
			if (iig.selectEditData(iim, itemId))
			{
				tabItemMaster.setValue(iim);
				tabItemAddModif.setValue(iim);
				tabItemProfile.setValue(itemId);
				tabItemInventory.setValue(iim);
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private void setEditModifData()
	{
		ItemInfoModel iim2 = new ItemInfoModel();
		try
		{
			if (iig.selectEditData(iim2, itemId))
			{ tabItemAddModif.setValue(iim2); }
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private void enableModifier()
	{
		boolean res = tabItemMaster.ogItemType.getValue().equals("Modifier")? false:true;

		tabItemAddModif.cmbModifier.setValue(null);
		tabItemAddModif.cmbItemCompany.setValue(null);
		tabItemAddModif.cmbSupplierName.setValue(null);
		tabItemAddModif.cmbModifier.setEnabled(res);
		tabItemAddModif.btnAddModifier.setEnabled(res);

		res = (tabItemMaster.ogItemType.getValue().equals("Modifier")
				|| tabItemMaster.ogItemType.getValue().equals("Both"))? false:true;

		tabItemMaster.btnAddPackage.setEnabled(res);

		tabItemMaster.cmbSalesType.setEnabled(res);

		if (flag.equals("Edit") && !tabItemMaster.ogItemType.getValue().equals("Modifier"))
		{ setEditModifData(); }
	}

	private VerticalLayout buildLayout()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();

		tsItem.setStyleName("framed padded-tabbar");
		tsItem.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

		tabItemMaster = new TabItemMasterInfo(sessionBean, flag);
		tabItemAddModif = new TabItemAddModifier(sessionBean, flag);
		tabItemProfile = new TabItemProfileInfo(sessionBean, flag);
		tabItemInventory = new TabItemInventory(sessionBean);

		tsItem.addTab(tabItemMaster, "Master Information", FontAwesome.DOWNLOAD, 0);
		tsItem.addTab(tabItemAddModif, "Additional and Modifier's", FontAwesome.PLUS, 1);
		tsItem.addTab(tabItemProfile, "Recipe Information", FontAwesome.BOOK, 2);
		tsItem.addTab(tabItemInventory, "Inventory Info", FontAwesome.LIST, 3);

		layout.addComponents(tsItem, cBtn);
		layout.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return layout;
	}

	private void focusEnter()
	{
		allComp.add(tabItemMaster.ogItemType);
		allComp.add(tabItemMaster.cmbBranchName);
		allComp.add(tabItemMaster.txtItemName);
		allComp.add(tabItemMaster.cmbPackageName);
		allComp.add(tabItemMaster.cmbFinishCategory);
		allComp.add(tabItemMaster.cmbSalesType);
		allComp.add(tabItemMaster.cmbVatCategory);
		allComp.add(tabItemMaster.ogVatOption);
		allComp.add(tabItemMaster.cmbColor);
		allComp.add(tabItemMaster.txtDescription);
		allComp.add(tabItemMaster.chkOnlineMenu);
		allComp.add(tabItemMaster.chkInventory);

		allComp.add(tabItemInventory.ogRawItemType);
		allComp.add(tabItemInventory.cmbRawCategory);
		allComp.add(tabItemInventory.cmbRawUnit);
		//allComp.add(tabItemInventory.txtCostPrice);
		allComp.add(tabItemInventory.txtCostMargin);

		allComp.add(tabItemAddModif.txtArabicName);
		allComp.add(tabItemAddModif.txtKitchenName);
		allComp.add(tabItemAddModif.cmbModifier);
		allComp.add(tabItemAddModif.txtBarcode);
		allComp.add(tabItemAddModif.cmbItemCompany);
		allComp.add(tabItemAddModif.cmbSupplierName);

		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		tabItemMaster.txtClear();
		tabItemAddModif.txtClear();
		tabItemProfile.tableClear();
		tabItemInventory.txtClear();
	}
}
