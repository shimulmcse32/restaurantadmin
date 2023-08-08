package com.example.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.common.share.CommonMethod;
import com.common.share.LoginView;
import com.common.share.MyTask;
import com.common.share.ReportViewer;
import com.common.share.SessionBean;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.data.Container.Hierarchical;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.server.Resource;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@SuppressWarnings("serial")
@Title("HiPOS Restaurant")
@Theme("dashboard")
@PreserveOnRefresh
public class MainUI extends UI
{
	private String companyName = "", address = "", phonFax = "", surName = "";
	private boolean testMode = false;

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = MainUI.class,
	closeIdleSessions = true)
	public static class Servlet extends VaadinServlet
	{
		protected void servletInitialized() throws ServletException
		{
			super.servletInitialized();
			getService().addSessionInitListener(new ValoThemeSessionInitListener());

			Timer t = new Timer();
			MyTask mTask = new MyTask();
			// This task is scheduled to run every 24 hour
			t.scheduleAtFixedRate(mTask, 0, 86400000);
		}
	}

	protected void init(VaadinRequest request)
	{
		String voucherId = request.getParameter("voucher");
		String purchaseId = request.getParameter("purchase");
		String returnId = request.getParameter("return");

		if (voucherId != null && !voucherId.isEmpty())
		{ viewReport(voucherId); }
		else if (purchaseId != null && !purchaseId.isEmpty())
		{ viewReportPurchase(purchaseId); }
		else if (returnId != null && !returnId.isEmpty())
		{ viewReportReturn(returnId); }
		else
		{ LoginView(); }
	}

	private void viewReport(String voucherId)
	{
		SessionBean sessionBean = new SessionBean();
		CommonMethod cm = new CommonMethod(sessionBean);
		reportDataLoad(cm);

		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "";
		try
		{
			//sql = "Select * from "+SessionBean.acc_db+"[dbo].[funVoucher]('"+voucherId+"')";
			hm.put("sql", sql);
			hm.put("companyName", companyName);
			hm.put("address", address);
			hm.put("phoneFax", phonFax);
			hm.put("surName", surName);
			hm.put("userName", "");
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("userIp", sessionBean.getUserIp());
			reportSource = "com/jasper/accounts/rptVoucher.jasper";

			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private void reportDataLoad(CommonMethod cm)
	{
		String sql = "select cm.vCompanyName, cm.vAddress, cm.vPhone+case when cm.vFax = '' then '' else ', '+"+
				"cm.vFax end+case when cm.vEmail = '' then '' else +', '+ cm.vEmail end vCompanyContact,"+
				" cm.vSurName from master.tbCompanyMaster cm where cm.vCompanyId = 'C1'";
		for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			companyName = element[0].toString();
			address = element[1].toString();
			phonFax = element[2].toString();
			surName = element[3].toString();
		}
	}

	private void viewReportPurchase(String purchaseId)
	{
		SessionBean sessionBean = new SessionBean();
		CommonMethod cm = new CommonMethod(sessionBean);
		reportDataLoad(cm);

		String reportSource = "", sql = "";
		try
		{
			HashMap<String, Object> hm = new HashMap<String, Object>();
			hm.put("companyName", companyName);
			hm.put("address", address);
			hm.put("phoneFax", phonFax);
			hm.put("branchName", "");
			hm.put("userName", "");
			hm.put("devloperInfo", "");
			hm.put("userIp", "");

			sql = "select pin.vPurchaseNo, pin.dPurchaseDate, isnull(poi.vOrderNo, '')vOrderNo, pid.vVatOption, pid.mVatAmount,"+
					" poi.dOrderDate, pin.dDeliveryDate, pin.vRemarks, pin.vReferenceNo, pid.vDescription, pid.mVatPercent,"+
					" pid.mQuantity, pid.mUnitRate, vat.vVatCatName, pid.mVatAmount, pid.mAmount, pid.mDiscount, pid.mNetAmount,"+
					" pin.vPurchaseType, ast.vStatusName, sup.vSupplierName, sup.vAddress, sup.vPhone, sup.vFax, sup.vEmail,"+
					" ri.vItemName, cat.vCategoryName, uni.vUnitName, sup.vContactMobile, ui.vFullName, bm.vBranchName, ISNULL((select"+
					" ui.vFullName from master.tbUserInfo ui where ui.vUserId = pin.vApprovedBy), '') vApprovedBy, ISNULL((select"+
					" ui.vFullName from master.tbUserInfo ui where ui.vUserId = pin.vCancelledBy), '') vCancelledBy from"+
					" trans.tbPurchaseInfo pin inner join trans.tbPurchaseDetails pid on pin.vPurchaseId = pid.vPurchaseId inner"+
					" join master.tbSupplierMaster sup on pin.vSupplierId = sup.vSupplierId inner join master.tbRawItemInfo ri"+
					" on pid.vItemId = ri.vItemId inner join master.tbItemCategory cat on ri.vCategoryId = cat.vCategoryId inner"+
					" join master.tbVatCatMaster vat on pid.vVatCatId = vat.vVatCatId left join trans.tbPurchaseOrderInfo poi on"+
					" pin.vOrderId = poi.vOrderId inner join master.tbAllStatus ast on pin.vStatusId = ast.vStatusId inner join"+
					" master.tbUserInfo ui on ui.vUserId = pin.vModifiedBy inner join master.tbBranchMaster bm on pin.vBranchId ="+
					" bm.vBranchId inner join master.tbUnitInfo uni on pid.vUnitId = convert(varchar(10), uni.iUnitId) where"+
					" pin.vPurchaseId = '"+purchaseId+"' order by pin.vPurchaseId, pid.iAutoId";
			reportSource = "com/jasper/postransaction/rptPurchaseInfo.jasper";

			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{
			System.out.println(sql);
			System.out.println("Error in view report: "+ex);
		}
	}

	private void viewReportReturn(String returnId)
	{
		SessionBean sessionBean = new SessionBean();
		CommonMethod cm = new CommonMethod(sessionBean);
		reportDataLoad(cm);

		String reportSource = "", sql = "";
		try
		{
			HashMap<String, Object> hm = new HashMap<String, Object>();
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("userIp", sessionBean.getUserIp());

			sql = "select a.vReturnNo, a.dReturnDate, O.vPurchaseNo, b.vVatOption, b.mVatAmount, O.dPurchaseDate, a.vRemarks,"+
					" a.vReferenceNo, b.vDescription, b.mVatPercent, b.mQuantity, b.mUnitRate, V.vVatCatName, b.mVatAmount,"+
					" b.mAmount, b.mDiscount, b.mNetAmount, a.vReturnType, ast.vStatusName, s.vSupplierName, s.vAddress,"+
					" s.vPhone, s.vFax, s.vEmail, I.vItemName, C.vCategoryName, b.vUnitName, s.vContactMobile from"+
					" trans.tbPurchaseReturnInfo a inner join trans.tbPurchaseReturnDetails b on a.vReturnId = b.vReturnId"+
					" inner join master.tbSupplierMaster s on a.vSupplierId = s.vSupplierId inner join master.tbRawItemInfo I"+
					" on b.vItemId = I.vItemId inner join master.tbItemCategory C on I.vCategoryId = C.vCategoryId inner join"+
					" master.tbVatCatMaster V on b.vVatCatId = v.vVatCatId left join trans.tbPurchaseInfo O on a.vPurchaseId"+
					" = O.vPurchaseId inner join master.tbAllStatus ast on a.vStatusId = ast.vStatusId where a.vReturnId ="+
					" '"+returnId+"' order by a.vReturnId,b.iAutoId";

			reportSource = "com/jasper/postransaction/rptPurchaseReturn.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private void LoginView()
	{
		testMode = true;
		if (browserCantRenderFontsConsistently())
		{ getPage().getStyles().add(".v-app.v-app.v-app {font-family: Sans-Serif;}"); }
		VaadinSession.getCurrent().getSession().setMaxInactiveInterval(1800); // 30 minutes
		Responsive.makeResponsive(this);
		addStyleName(ValoTheme.UI_WITH_MENU);
		//setContent(new Login(this));
		setContent(new LoginView(this));
		//addStyleName("loginview");
	}

	private boolean browserCantRenderFontsConsistently()
	{
		return getPage().getWebBrowser().getBrowserApplication().contains("PhantomJS")
				|| (getPage().getWebBrowser().isIE() && getPage()
						.getWebBrowser().getBrowserMajorVersion() <= 9);
	}

	public static boolean isTestMode()
	{ return ((MainUI) getCurrent()).testMode; }

	public static String CAPTION_PROPERTY = "caption";
	public static String DESCRIPTION_PROPERTY = "description";
	public static String ICON_PROPERTY = "icon";
	public static String INDEX_PROPERTY = "index";

	@SuppressWarnings("unchecked")
	public static IndexedContainer generateContainer(int size, boolean hierarchical)
	{
		TestIcon testIcon = new TestIcon(90);
		IndexedContainer container = hierarchical ? new HierarchicalContainer() : new IndexedContainer();
		StringGenerator sg = new StringGenerator();
		container.addContainerProperty(CAPTION_PROPERTY, String.class, null);
		container.addContainerProperty(ICON_PROPERTY, Resource.class, null);
		container.addContainerProperty(INDEX_PROPERTY, Integer.class, null);
		container.addContainerProperty(DESCRIPTION_PROPERTY, String.class, null);
		for (int i = 1; i < size + 1; i++)
		{
			Item item = container.addItem(i);
			item.getItemProperty(CAPTION_PROPERTY).setValue(sg.nextString(true)+" "+sg.nextString(false));
			item.getItemProperty(INDEX_PROPERTY).setValue(i);
			item.getItemProperty(DESCRIPTION_PROPERTY).setValue(sg.nextString(true)+" "+sg.nextString(false)+" "
					+sg.nextString(false));
			item.getItemProperty(ICON_PROPERTY).setValue(testIcon.get());
		}
		container.getItem(container.getIdByIndex(0)).getItemProperty(ICON_PROPERTY).setValue(testIcon.get());

		if (hierarchical)
		{
			for (int i = 1; i < size + 1; i++)
			{
				for (int j = 1; j < 5; j++)
				{
					String id = i + " -> "+j;
					Item child = container.addItem(id);
					child.getItemProperty(CAPTION_PROPERTY).setValue(sg.nextString(true) + " "+sg.nextString(false));
					child.getItemProperty(ICON_PROPERTY).setValue(testIcon.get());
					((Hierarchical) container).setParent(id, i);

					for (int k = 1; k < 6; k++)
					{
						String id2 = id + " -> "+k;
						child = container.addItem(id2);
						child.getItemProperty(CAPTION_PROPERTY).setValue(sg.nextString(true)+" "+sg.nextString(false));
						child.getItemProperty(ICON_PROPERTY).setValue(testIcon.get());
						((Hierarchical) container).setParent(id2, id);

						for (int l = 1; l < 5; l++)
						{
							String id3 = id2 + " -> "+l;
							child = container.addItem(id3);
							child.getItemProperty(CAPTION_PROPERTY).setValue(sg.nextString(true)+" "+sg.nextString(false));
							child.getItemProperty(ICON_PROPERTY).setValue(testIcon.get());
							((Hierarchical) container).setParent(id3, id2);
						}
					}
				}
			}
		}
		return container;
	}

	public static Handler actionHandler = new Handler()
	{
		private Action ACTION_ONE = new Action("Action One");
		private Action ACTION_TWO = new Action("Action Two");
		private Action ACTION_THREE = new Action("Action Three");
		private Action[] ACTIONS = new Action[] { ACTION_ONE, ACTION_TWO, ACTION_THREE };

		public void handleAction(Action action, Object sender, Object target)
		{ Notification.show(action.getCaption()); }

		public Action[] getActions(Object target, Object sender)
		{ return ACTIONS; }
	};

	public static Handler getActionHandler()
	{ return actionHandler; }
}
