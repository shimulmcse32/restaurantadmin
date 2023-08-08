package com.example.config;

import com.common.share.SessionBean;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.CheckBox;

@SuppressWarnings("serial")
public class TabPosUser extends HorizontalLayout
{
	public CheckBox chkPosUser, chkSave, chkSettle, chkGiveDis, chkCancel,
	chkSettings, chkReports;

	public CheckBox chkPosUserApp, chkSaveApp, chkSettleApp, chkGiveDisApp,
	chkCancelApp, chkSettingsApp, chkReportsApp;

	public CheckBox chkAppUser;

	public Panel posPanel, appPanel;

	public TabPosUser(SessionBean sessionBean, String flag)
	{
		setSizeFull();
		setMargin(true);
		setSpacing(true);

		addComponents(posLayout(), appLayout());
		addActions();
	}

	private void addActions()
	{
		setPosEnable(false);
		setAppEnable(false);
		chkPosUser.addValueChangeListener(event -> setPosEnable(chkPosUser.getValue().booleanValue()));

		chkAppUser.addValueChangeListener(event -> setAppEnable(chkAppUser.getValue().booleanValue()));
	}

	private void setPosEnable(boolean boo)
	{
		if (!boo)
		{
			chkSave.setValue(false);
			chkSettle.setValue(false);
			chkGiveDis.setValue(false);
			chkCancel.setValue(false);
			chkSettings.setValue(false);
			chkReports.setValue(false);
		}
		chkSave.setEnabled(boo);
		chkSettle.setEnabled(boo);
		chkGiveDis.setEnabled(boo);
		chkCancel.setEnabled(boo);
		chkSettings.setEnabled(boo);
		chkReports.setEnabled(boo);
	}

	private void setAppEnable(boolean boo)
	{
		if (!boo)
		{
			chkSaveApp.setValue(false);
			chkSettleApp.setValue(false);
			chkGiveDisApp.setValue(false);
			chkCancelApp.setValue(false);
			chkSettingsApp.setValue(false);
			chkReportsApp.setValue(false);
		}
		chkSaveApp.setEnabled(boo);
		chkSettleApp.setEnabled(boo);
		chkGiveDisApp.setEnabled(boo);
		chkCancelApp.setEnabled(boo);
		chkSettingsApp.setEnabled(boo);
		chkReportsApp.setEnabled(boo);
	}

	private Panel posLayout()
	{
		posPanel = new Panel("POS User Config");

		VerticalLayout lay = new VerticalLayout();
		lay.setMargin(true);
		lay.setSpacing(true);
		lay.setSizeFull();

		chkPosUser = new CheckBox("POS User Access");
		chkPosUser.setImmediate(true);
		lay.addComponent(chkPosUser);

		chkSave = new CheckBox("Save Invoice(s)");
		chkSave.setImmediate(true);
		lay.addComponent(chkSave);

		chkSettle = new CheckBox("Settle Invoice(s)");
		chkSettle.setImmediate(true);
		lay.addComponent(chkSettle);

		chkGiveDis = new CheckBox("Discount on Invoice(s)");
		chkGiveDis.setImmediate(true);
		lay.addComponent(chkGiveDis);

		chkCancel = new CheckBox("Cancel Invoice(s)");
		chkCancel.setImmediate(true);
		lay.addComponent(chkCancel);

		chkSettings = new CheckBox("Access Setting(s)");
		chkSettings.setImmediate(true);
		lay.addComponent(chkSettings);

		chkReports = new CheckBox("Access Report(s)");
		chkReports.setImmediate(true);
		lay.addComponent(chkReports);

		posPanel.setContent(lay);
		return posPanel;
	}

	private Panel appLayout()
	{
		appPanel = new Panel("Waiter App User Config");

		VerticalLayout lay = new VerticalLayout();
		lay.setMargin(true);
		lay.setSpacing(true);
		lay.setSizeFull();

		chkAppUser = new CheckBox("APP User Access");
		chkAppUser.setImmediate(true);
		lay.addComponent(chkAppUser);

		chkSaveApp = new CheckBox("Save Invoice(s)");
		chkSaveApp.setImmediate(true);
		lay.addComponent(chkSaveApp);

		chkSettleApp = new CheckBox("Settle Invoice(s)");
		chkSettleApp.setImmediate(true);
		lay.addComponent(chkSettleApp);

		chkGiveDisApp = new CheckBox("Discount on Invoice(s)");
		chkGiveDisApp.setImmediate(true);
		lay.addComponent(chkGiveDisApp);

		chkCancelApp = new CheckBox("Cancel Invoice(s)");
		chkCancelApp.setImmediate(true);
		lay.addComponent(chkCancelApp);

		chkSettingsApp = new CheckBox("Access Setting(s)");
		chkSettingsApp.setImmediate(true);
		lay.addComponent(chkSettingsApp);

		chkReportsApp = new CheckBox("Access Report(s)");
		chkReportsApp.setImmediate(true);
		lay.addComponent(chkReportsApp);

		appPanel.setContent(lay);
		return appPanel;
	}

	public void chkClear()
	{
		chkPosUser.setValue(false);
		chkPosUser.setValue(false);
	}

	public String getPosValue()
	{
		String value = " ";
		//Hard coded value for user access
		//chkPosUser = 0, chkSave = 1, chkSettle = 2, chkGiveDis = 3,
		//chkCancel = 4, chkSettings = 5, chkReports = 6
		if (chkPosUser.getValue().booleanValue())
		{ value = value+ "0,"; }
		else
		{ value = value.replaceAll("0,", ""); }

		if (chkSave.getValue().booleanValue())
		{ value = value+ "1,"; }
		else
		{ value = value.replaceAll("1,", ""); }

		if (chkSettle.getValue().booleanValue())
		{ value = value+ "2,"; }
		else
		{ value = value.replaceAll("2,", ""); }

		if (chkGiveDis.getValue().booleanValue())
		{ value = value+ "3,"; }
		else
		{ value = value.replaceAll("3,", ""); }

		if (chkCancel.getValue().booleanValue())
		{ value = value+ "4,"; }
		else
		{ value = value.replaceAll("4,", ""); }

		if (chkSettings.getValue().booleanValue())
		{ value = value+ "5,"; }
		else
		{ value = value.replaceAll("5,", ""); }

		if (chkReports.getValue().booleanValue())
		{ value = value+ "6,"; }
		else
		{ value = value.replaceAll("6,", ""); }

		return value.substring(0, value.length()-1);
	}

	public String getAppValue()
	{
		String value = " ";
		//Hard coded value for user access
		//chkAppUser = 0
		if (chkAppUser.getValue().booleanValue())
		{ value = value+ "0,"; }
		else
		{ value = value.replaceAll("0,", ""); }

		if (chkSaveApp.getValue().booleanValue())
		{ value = value+ "1,"; }
		else
		{ value = value.replaceAll("1,", ""); }

		if (chkSettleApp.getValue().booleanValue())
		{ value = value+ "2,"; }
		else
		{ value = value.replaceAll("2,", ""); }

		if (chkGiveDisApp.getValue().booleanValue())
		{ value = value+ "3,"; }
		else
		{ value = value.replaceAll("3,", ""); }

		if (chkCancelApp.getValue().booleanValue())
		{ value = value+ "4,"; }
		else
		{ value = value.replaceAll("4,", ""); }

		if (chkSettingsApp.getValue().booleanValue())
		{ value = value+ "5,"; }
		else
		{ value = value.replaceAll("5,", ""); }

		if (chkReportsApp.getValue().booleanValue())
		{ value = value+ "6,"; }
		else
		{ value = value.replaceAll("6,", ""); }

		return value.substring(0, value.length()-1);
	}

	public void setPosValue(String posAccess)
	{
		if (posAccess.contains("0"))
		{ chkPosUser.setValue(true); }
		if (posAccess.contains("1"))
		{ chkSave.setValue(true); }
		if (posAccess.contains("2"))
		{ chkSettle.setValue(true); }
		if (posAccess.contains("3"))
		{ chkGiveDis.setValue(true); }
		if (posAccess.contains("4"))
		{ chkCancel.setValue(true); }
		if (posAccess.contains("5"))
		{ chkSettings.setValue(true); }
		if (posAccess.contains("6"))
		{ chkReports.setValue(true); }
	}

	public void setAppValue(String posAccess)
	{
		if (posAccess.contains("0"))
		{ chkAppUser.setValue(true); }
		if (posAccess.contains("1"))
		{ chkSaveApp.setValue(true); }
		if (posAccess.contains("2"))
		{ chkSettleApp.setValue(true); }
		if (posAccess.contains("3"))
		{ chkGiveDisApp.setValue(true); }
		if (posAccess.contains("4"))
		{ chkCancelApp.setValue(true); }
		if (posAccess.contains("5"))
		{ chkSettingsApp.setValue(true); }
		if (posAccess.contains("6"))
		{ chkReportsApp.setValue(true); }
	}
}
