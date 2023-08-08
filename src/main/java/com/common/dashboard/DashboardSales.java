package com.common.dashboard;

import java.util.Iterator;

import com.common.share.CommonMethod;
import com.common.share.SessionBean;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class DashboardSales extends Panel implements View
{
	private SessionBean sessionBean;
	//private static String TITLE_ID = "dashboard-title";

	private Label titleLabel;
	private CssLayout dashboardPanels;
	private VerticalLayout root;
	private ComboBox cmbBranch;
	private CommonMethod cm;

	private RevenueBoxs revenueBox;
	private SalesTypeBoxs saleTypeBox;
	private MostSellingItems mostItems;
	private PaymentMethod payMethods;
	private MonthlySalesType monthlySales;

	public DashboardSales(SessionBean sessionBean)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		addStyleName(ValoTheme.PANEL_BORDERLESS);
		setSizeFull();

		root = new VerticalLayout();
		root.setSizeFull();
		root.setSpacing(true);
		root.addStyleName("dashboard-view");
		setContent(root);

		//buildCharts();

		Responsive.makeResponsive(root);
	}

	private void buildCharts()
	{
		root.removeAllComponents();

		root.addComponent(buildHeader());

		root.addComponent(buildRevenueBoxs());

		root.addComponent(buildSalesTypeBoxs());

		root.addComponent(buildMostSelling());

		root.addComponent(buildPaymentMethod());

		root.addComponent(buildMonthlySales());

		Component content = buildContent();
		root.addComponent(content);
		root.setExpandRatio(content, 1);

		addActions();
	}

	private void addActions()
	{ cmbBranch.addValueChangeListener(event -> refreshCharts()); }

	private void refreshCharts()
	{
		String branch = cmbBranch.getValue() != null? cmbBranch.getValue().toString():"%";
		revenueBox.drawChart(branch);
		saleTypeBox.drawChart(branch);
		mostItems.drawChart(branch);
		payMethods.drawChart(branch);
		monthlySales.drawChart(branch);
	}

	private Component buildHeader()
	{
		HorizontalLayout header = new HorizontalLayout();
		header.setSpacing(true);
		header.addStyleName("viewheader");

		titleLabel = new Label("Canteen System Dashboard");
		header.addComponent(titleLabel);
		/*titleLabel.setId(TITLE_ID);
		titleLabel.setSizeUndefined();
		titleLabel.addStyleName(ValoTheme.LABEL_H2);
		titleLabel.addStyleName(ValoTheme.LABEL_NO_MARGIN);*/

		cmbBranch = new ComboBox();
		cmbBranch.setImmediate(true);
		cmbBranch.setWidth("-1px");
		cmbBranch.setFilteringMode(FilteringMode.CONTAINS);
		cmbBranch.setStyleName(ValoTheme.COMBOBOX_TINY);
		cmbBranch.setDescription("Search branch");
		cmbBranch.setInputPrompt("Search a branch");
		//cm.setAuthorize(sessionBean.getUserId(), "branchInfo");
		//cmbBranch.setVisible(cm.insert);

		HorizontalLayout tools = new HorizontalLayout(cmbBranch);
		tools.addStyleName("toolbar");
		header.addComponent(tools);
		header.setResponsive(true);

		loadBranchData();

		return header;
	}

	private void loadBranchData()
	{
		String sql = "select vBranchId, vBranchName from master.tbBranchMaster order by vBranchName";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbBranch.addItem(element[0].toString());
			cmbBranch.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private Component buildContent()
	{
		dashboardPanels = new CssLayout();
		dashboardPanels.addStyleName("dashboard-panels");
		Responsive.makeResponsive(dashboardPanels);

		return dashboardPanels;
	}

	private Component buildRevenueBoxs()
	{
		revenueBox = new RevenueBoxs(sessionBean);
		return revenueBox;
	}

	private Component buildSalesTypeBoxs()
	{
		saleTypeBox = new SalesTypeBoxs(sessionBean);
		return saleTypeBox;
	}

	private Component buildMostSelling()
	{
		CssLayout mostSell = new CssLayout();
		mostSell.addStyleName("sparks");
		mostSell.setWidth("100%");
		Responsive.makeResponsive(mostSell);

		mostItems = new MostSellingItems(sessionBean);
		mostSell.addComponent(mostItems);

		return mostSell;	
	}

	private Component buildPaymentMethod()
	{
		CssLayout paymethod = new CssLayout();
		paymethod.addStyleName("sparks");
		paymethod.setWidth("100%");
		Responsive.makeResponsive(paymethod);

		payMethods = new PaymentMethod(sessionBean);
		paymethod.addComponent(payMethods);

		return paymethod;
	}

	private Component buildMonthlySales()
	{
		CssLayout mnthsale = new CssLayout();
		mnthsale.addStyleName("sparks");
		mnthsale.setWidth("100%");
		Responsive.makeResponsive(mnthsale);

		monthlySales = new MonthlySalesType(sessionBean);
		mnthsale.addComponent(monthlySales);

		return mnthsale;
	}

	public void enter(ViewChangeEvent event)
	{
		buildCharts();
	}
}
