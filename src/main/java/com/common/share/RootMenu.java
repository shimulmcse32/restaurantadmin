package com.common.share;

import com.common.dashboard.DashboardSales;
import com.common.dashboard.UserHome;
import com.example.config.BranchInformation;
import com.example.config.CompanyInfo;
import com.example.config.UpdatePassword;
import com.example.config.UpdateProfile;
import com.example.config.UserInformation;
import com.example.config.UserRoleInfo;
import com.example.hrmaster.EmployeeInformation;
import com.example.main.MainUI;
import com.example.main.ValoMenuLayout;
import com.example.posreport.IssueAndReturn;
import com.example.posreport.ItemStockReport;
import com.example.posreport.PurchaseReturnReport;
import com.example.posreport.SalesClosingReport;
import com.example.posreport.VATReturnReport;
import com.example.possetup.CategoryInformation;
import com.example.possetup.CustomerInformation;
import com.example.possetup.FinishedItemInfo;
import com.example.possetup.ModifierInformation;
import com.example.possetup.NoteInformation;
import com.example.possetup.PaymentMethodInfo;
import com.example.possetup.RawItemInformation;
import com.example.possetup.SupplierInformation;
import com.example.postrans.IssueInformation;
import com.example.postrans.IssueReceivedInfo;
import com.example.postrans.IssueReturnInfo;
import com.example.postrans.PhysicalStockInfo;
import com.example.postrans.PurchaseInformation;
import com.example.postrans.PurchaseOrderInfo;
import com.example.postrans.PurchaseReturnInfo;
import com.example.postrans.ReceiptAgainstPurchase;
import com.example.postrans.RequisitionInformation;
import com.example.postrans.StockAdjustmentInfo;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.*;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.ValoTheme;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class RootMenu extends ValoMenuLayout
{
	private ValoMenuLayout root = new ValoMenuLayout();
	private ComponentContainer viewDisplay = root.getContentContainer();
	private CssLayout menu = new CssLayout();
	private CssLayout menuItemsLayout = new CssLayout();
	{ menu.setId("testMenu"); }
	private Navigator navigator;
	private LinkedHashMap<String, String> menuItems = new LinkedHashMap<String, String>();

	public MainUI ui;
	private CommonMethod cm;
	private SessionBean sessionBean;

	public RootMenu(MainUI ui, SessionBean sessionBean)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(sessionBean);
		this.ui = ui;

		setSizeFull();

		if (browserCantRenderFontsConsistently())
		{ this.ui.getPage().getStyles().add(".v-app.v-app.v-app {font-family: Sans-Serif;}"); }

		if (this.ui.getPage().getWebBrowser().isIE() && this.ui.getPage().getWebBrowser().getBrowserMajorVersion() == 9)
		{ menu.setWidth("320px"); }

		this.ui.setContent(root);
		root.addMenu(buildMenu());
		root.setWidth("100%");
		Responsive.makeResponsive(this);
		addStyleName(ValoTheme.UI_WITH_MENU);

		addNevigator();
	}

	private void addNevigator()
	{
		viewDisplay.addStyleName("view-content");
		navigator = new Navigator(this.ui, viewDisplay);

		//Admin menus
		addAdminMenu();

		//Operation master menus
		addOperationMaster();

		//Operation transaction menus
		addOperationTrans();

		//Operation report menus
		addOperationReport();

		//by default go to home after login
		String f = Page.getCurrent().getUriFragment();
		if (f == null || f.equals("") || !f.isEmpty())
		{ navigator.navigateTo("home"); }

		//navigator.setErrorView(new DashboardView(sessionBean));
		//navigator.setErrorView(new CommonParts());
		navigator.setErrorView(new UserHome(sessionBean));

		navigator.addViewChangeListener(new ViewChangeListener()
		{
			public boolean beforeViewChange(ViewChangeEvent event)
			{ return true; }

			public void afterViewChange(ViewChangeEvent event)
			{
				for (Iterator<Component> it = menuItemsLayout.iterator(); it.hasNext();)
				{ it.next().removeStyleName("selected"); }
				for (Entry<String, String> item : menuItems.entrySet())
				{
					if (event.getViewName().equals(item.getKey()))
					{
						for (Iterator<Component> it = menuItemsLayout.iterator(); it.hasNext();)
						{
							Component c = it.next();
							if (c.getCaption() != null && c.getCaption().startsWith(item.getValue()))
							{
								c.addStyleName("selected");
								break;
							}
						}
						break;
					}
				}
				menu.removeStyleName("valo-menu-visible");
			}
		});
	}

	private void addAdminMenu()
	{
		navigator.addView("home", new UserHome(sessionBean));
		navigator.addView("dashboard", new DashboardSales(sessionBean));
		navigator.addView("comInfo#1", new CompanyInfo(sessionBean, "comInfo"));
		navigator.addView("brnInfo#1", new BranchInformation(sessionBean, "brnInfo"));
		navigator.addView("userRole#1", new UserRoleInfo(sessionBean, "userRole"));
		navigator.addView("userInfo#1", new UserInformation(sessionBean, "userInfo"));
	}

	private void addOperationMaster()
	{
		navigator.addView("empInfo#2", new EmployeeInformation(sessionBean, "empInfo"));
		navigator.addView("suppInfo#2", new SupplierInformation(sessionBean, "suppInfo"));
		navigator.addView("custInfo#2", new CustomerInformation(sessionBean, "custInfo"));
		navigator.addView("rawItem#2", new RawItemInformation(sessionBean, "rawItem"));

		navigator.addView("catInfo#2", new CategoryInformation(sessionBean, "catInfo"));
		navigator.addView("finiItem#2", new FinishedItemInfo(sessionBean, "finiItem"));
		navigator.addView("modiMas#2", new ModifierInformation(sessionBean, "modiMas"));
		navigator.addView("noteInfo#2", new NoteInformation(sessionBean, "noteInfo"));
		navigator.addView("payMeth#2", new PaymentMethodInfo(sessionBean, "payMeth"));
	}

	private void addOperationTrans()
	{
		navigator.addView("stkAdjst#3", new StockAdjustmentInfo(sessionBean, "stkAdjst"));
		navigator.addView("reqInfo#3", new RequisitionInformation(sessionBean, "reqInfo"));

		navigator.addView("purOrdr#3", new PurchaseOrderInfo(sessionBean, "purOrdr"));
		navigator.addView("purInfo#3", new PurchaseInformation(sessionBean, "purInfo"));
		navigator.addView("purRetu#3", new PurchaseReturnInfo(sessionBean, "purRetu"));
		
		navigator.addView("recpInfo#3", new ReceiptAgainstPurchase(sessionBean, "recpInfo"));

		navigator.addView("phyStock#3", new PhysicalStockInfo(sessionBean, "phyStock"));

		navigator.addView("issuInfo#3", new IssueInformation(sessionBean, "issuInfo"));
		navigator.addView("penIssue#3", new IssueReceivedInfo(sessionBean, "penIssue"));
		navigator.addView("issReturn#3", new IssueReturnInfo(sessionBean, "issReturn"));
	}

	private void addOperationReport()
	{
		navigator.addView("itmRept#4", new ItemStockReport(sessionBean, "itmRept"));
		navigator.addView("purRept#4", new PurchaseReturnReport(sessionBean, "purRept"));
		navigator.addView("vatRept#4", new VATReturnReport(sessionBean, "vatRept"));
		navigator.addView("issRept#4", new IssueAndReturn(sessionBean, "issRept"));
		navigator.addView("salRept#4", new SalesClosingReport(sessionBean, "salRept"));
	}

	private CssLayout buildMenu()
	{
		//Add items
		menuItems.put("home", "User Home");
		//if (sessionBean.getIsAdmin())
		{ menuItems.put("dashboard", "Dashboard"); }
		//Dynamic menu from database
		String module = sessionBean.getModuleId().isEmpty()?"iModuleId like '%'":
			"iModuleId in ("+sessionBean.getModuleId()+")";

		String sql = "select vURL, vMenuName, vParentId from master.tbUserInfo ui, master.tbUserRoleDetails"+
				" urd, master.tbMenuList ml where ui.vRoleId = urd.vRoleId and urd.vMenuId = ml.vMenuId and"+
				" ui.vUserId = '"+sessionBean.getUserId()+"' and "+module+" and ml.iActive = 1 order by"+
				" cast(ml.vParentId as int), ml.iSerialNo";
		//System.out.println(sql);
		for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			menuItems.put(element[0].toString()+"#"+element[2].toString(), element[1].toString());
		}

		HorizontalLayout top = new HorizontalLayout();
		top.setWidth("100%");
		top.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		top.addStyleName(ValoTheme.MENU_TITLE);
		menu.addComponent(top);
		//menu.addComponent(createThemeSelect());

		Button showMenu = new Button("Menu", event ->
		{
			if (menu.getStyleName().contains("valo-menu-visible"))
			{ menu.removeStyleName("valo-menu-visible"); }
			else
			{ menu.addStyleName("valo-menu-visible"); }
		});

		showMenu.addStyleName(ValoTheme.BUTTON_PRIMARY);
		showMenu.addStyleName(ValoTheme.BUTTON_SMALL);
		showMenu.addStyleName("valo-menu-toggle");
		showMenu.setIcon(FontAwesome.LIST);
		showMenu.setDescription("Menu");
		menu.addComponent(showMenu);

		Label title = new Label("<h4><strong>HiPOS</strong> Admin</h4>", ContentMode.HTML);
		title.setSizeUndefined();
		top.addComponent(title);
		top.setExpandRatio(title, 1/2);

		menu.addComponent(userMenu());

		menuItemsLayout.setPrimaryStyleName("valo-menuitems");
		menu.addComponent(menuItemsLayout);

		Label label = null;
		int i = 0, j = 0, k = 0, l = 0;
		for (final Entry<String, String> item : menuItems.entrySet())
		{
			String str = item.getKey().toString().substring(item.getKey().indexOf("#")+1,
					item.getKey().toString().length());
			if (str.equals("1"))
			{
				if (i == 0)
				{
					label = new Label("CONFIGURATION", ContentMode.HTML);
					label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
					label.setSizeUndefined();
					menuItemsLayout.addComponent(label);
				}
				i++;
			}
			if (str.equals("2"))
			{
				if (j == 0)
				{
					/*label.setValue(label.getValue() + " <span class=\"valo-menu-badge\">" + count + "</span>");
					count = 0;*/
					label = new Label("OPERATION MASTER", ContentMode.HTML);
					label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
					label.setSizeUndefined();
					menuItemsLayout.addComponent(label);
				}
				j++;
			}
			if (str.equals("3"))
			{
				if (k == 0)
				{
					/*label.setValue(label.getValue() + " <span class=\"valo-menu-badge\">" + count + "</span>");
					count = 0;*/
					label = new Label("OPERATION TRANSACTIONS", ContentMode.HTML);
					label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
					label.setSizeUndefined();
					menuItemsLayout.addComponent(label);
				}
				k++;
			}
			if (str.equals("4"))
			{
				if (l == 0)
				{
					/*label.setValue(label.getValue() + " <span class=\"valo-menu-badge\">" + count + "</span>");
					count = 0;*/
					label = new Label("OPERATION REPORTS", ContentMode.HTML);
					label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
					label.setSizeUndefined();
					menuItemsLayout.addComponent(label);
				}
				l++;
			}
			Button btnAction = new Button(item.getValue(), new ClickListener()
			{
				public void buttonClick(ClickEvent event)
				{
					//System.out.println(item.getKey());
					navigator.navigateTo(item.getKey());
				}
			});
			/*if (count == 2)
			{
				//btnAction.setCaption(btnAction.getCaption() + " <span class=\"valo-menu-badge\">123</span>");
			}*/
			btnAction.setHtmlContentAllowed(true);
			btnAction.setPrimaryStyleName(ValoTheme.MENU_ITEM);

			//SET ICONS
			String menuId = item.getKey().toString();
			//Configuration
			if (menuId.equals("home")) btnAction.setIcon(FontAwesome.HOME);
			if (menuId.equals("dashboard")) btnAction.setIcon(FontAwesome.DASHBOARD);

			if (menuId.equals("comInfo#1")) btnAction.setIcon(FontAwesome.CLOUD);
			if (menuId.equals("brnInfo#1")) btnAction.setIcon(FontAwesome.SITEMAP);
			if (menuId.equals("termInfo#1")) btnAction.setIcon(FontAwesome.TERMINAL);
			if (menuId.equals("userRole#1")) btnAction.setIcon(FontAwesome.PAINT_BRUSH);
			if (menuId.equals("userInfo#1")) btnAction.setIcon(FontAwesome.USER);
			if (menuId.equals("onlineMenu#1")) btnAction.setIcon(FontAwesome.GLOBE);

			//HR master
			if (menuId.equals("empInfo#2")) btnAction.setIcon(FontAwesome.EDGE);

			//Operation Master
			if (menuId.equals("suppInfo#2")) btnAction.setIcon(FontAwesome.BUYSELLADS);
			if (menuId.equals("custInfo#2")) btnAction.setIcon(FontAwesome.SELLSY);
			if (menuId.equals("catInfo#2")) btnAction.setIcon(FontAwesome.BARS);
			if (menuId.equals("rawItem#2")) btnAction.setIcon(FontAwesome.RA);
			if (menuId.equals("modiMas#2")) btnAction.setIcon(FontAwesome.PLUS_SQUARE_O);
			if (menuId.equals("noteInfo#2")) btnAction.setIcon(FontAwesome.FILE_TEXT);
			if (menuId.equals("payMeth#2")) btnAction.setIcon(FontAwesome.EURO);
			if (menuId.equals("finiItem#2")) btnAction.setIcon(FontAwesome.PIE_CHART);

			//Operation Transaction
			if (menuId.equals("purOrdr#3")) btnAction.setIcon(FontAwesome.CART_ARROW_DOWN);
			if (menuId.equals("purInfo#3")) btnAction.setIcon(FontAwesome.SHOPPING_CART);
			if (menuId.equals("purRetu#3")) btnAction.setIcon(FontAwesome.REMOVE);
			if (menuId.equals("recpInfo#3")) btnAction.setIcon(FontAwesome.CLIPBOARD);
			if (menuId.equals("stkAdjst#3")) btnAction.setIcon(FontAwesome.ADJUST);
			if (menuId.equals("phyStock#3")) btnAction.setIcon(FontAwesome.STAR_O);
			if (menuId.equals("issuInfo#3")) btnAction.setIcon(FontAwesome.ARCHIVE);
			if (menuId.equals("penIssue#3")) btnAction.setIcon(FontAwesome.RECYCLE);
			if (menuId.equals("issReturn#3")) btnAction.setIcon(FontAwesome.RENREN);
			if (menuId.equals("reqInfo#3")) btnAction.setIcon(FontAwesome.REORDER);

			//Operation Report
			if (menuId.equals("vatRept#4")) btnAction.setIcon(FontAwesome.FILE_PDF_O);
			if (menuId.equals("purRept#4")) btnAction.setIcon(FontAwesome.FILE_PDF_O);
			if (menuId.equals("salRept#4")) btnAction.setIcon(FontAwesome.CLIPBOARD);
			if (menuId.equals("issRept#4")) btnAction.setIcon(FontAwesome.FILE_PDF_O);
			if (menuId.equals("itmRept#4")) btnAction.setIcon(FontAwesome.FILE_PDF_O);

			menuItemsLayout.addComponent(btnAction);
			//count++;
		}
		return menu;
	}

	private MenuBar userMenu()
	{
		MenuBar settings = new MenuBar();
		settings.addStyleName("user-menu");
		String filePath = sessionBean.getUserPicture();

		File file = new File(filePath);
		MenuItem settingsItem = settings.addItem(sessionBean.getFullName(), new FileResource(file), null);
		settingsItem.addItem("Edit Profile", new Command()
		{
			public void menuSelected(MenuItem selectedItem)
			{ UpdateProfile.open(sessionBean); }
		}).setIcon(FontAwesome.USER);

		settingsItem.addItem("Change Password", new Command()
		{
			public void menuSelected(MenuItem selectedItem)
			{ UpdatePassword.open(sessionBean); }
		}).setIcon(FontAwesome.LOCK);

		settingsItem.addSeparator();

		settingsItem.addItem("Sign Out", new Command()
		{
			public void menuSelected(final MenuItem selectedItem)
			{
				for (Window window : ui.getWindows())
				{ window.close(); }
				ui.setContent(new LoginView(ui));
			}
		}).setIcon(FontAwesome.SIGN_OUT);
		return settings;
	}

	private boolean browserCantRenderFontsConsistently()
	{
		// PhantomJS renders font correctly about 50% of the time, so
		// disable it to have consistent screenshots
		// https://github.com/ariya/phantomjs/issues/10592
		// IE8 also has randomness in its font rendering...

		return ui.getPage().getWebBrowser().getBrowserApplication().contains("PhantomJS")
				|| (ui.getPage().getWebBrowser().isIE() && ui.getPage().getWebBrowser().getBrowserMajorVersion() <= 9);
	}
}
