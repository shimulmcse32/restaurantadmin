package com.common.dashboard;

import java.util.Date;
import java.util.Iterator;

import com.common.share.CommonMethod;
import com.common.share.DummyDataGenerator;
import com.common.share.SessionBean;
import com.common.share.SparklineChart;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class RevenueBoxs extends Panel
{
	private SessionBean sessionBean;
	private CommonMethod cm;

	private PopupDateField txtFromDate, txtToDate;
	private int totalOrd = 0;
	private double totalRev = 0, totalVat = 0, totalDis = 0;

	private SparklineChart sparkOrd, sparkRev, sparkVat, sparkDis;

	public RevenueBoxs(SessionBean sessionBean)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);

		setContent(buildBoxs());
	}

	private Component buildBoxs()
	{
		Panel pnlSummary = new Panel();

		VerticalLayout vert = new VerticalLayout();
		vert.setSpacing(true);
		vert.setMargin(true);
		vert.setSizeFull();

		txtFromDate  = new PopupDateField();
		txtFromDate.setImmediate(true);
		txtFromDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDate.setDescription("From Date");
		txtFromDate.setValue(new Date());
		txtFromDate.setWidth("105px");
		txtFromDate.setDateFormat("dd-MM-yyyy");
		txtFromDate.setRequired(true);
		txtFromDate.setRequiredError("This field is required.");

		txtToDate  = new PopupDateField();
		txtToDate.setImmediate(true);
		txtToDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDate.setDescription("To Date");
		txtToDate.setValue(new Date());
		txtToDate.setWidth("105px");
		txtToDate.setDateFormat("dd-MM-yyyy");
		txtToDate.setRequired(true);
		txtToDate.setRequiredError("This field is required.");

		HorizontalLayout hori = new HorizontalLayout();
		hori.setSpacing(true);
		hori.addComponents(txtFromDate, txtToDate);

		CssLayout sparks = new CssLayout();
		sparks.addStyleName("sparks");
		sparks.setWidth("100%");
		Responsive.makeResponsive(sparks);

		drawChart("%");

		sparkOrd = new SparklineChart("Total Orders", "", "",
				DummyDataGenerator.chartColors[0], 10, 20, 0, 0, "Int", totalOrd);
		sparks.addComponent(sparkOrd);

		sparkRev = new SparklineChart("Net Revenue", "", "",
				DummyDataGenerator.chartColors[2], 10, 89, 0, totalRev, "Double", 0);
		sparks.addComponent(sparkRev);

		sparkVat = new SparklineChart("Total VAT", "", "",
				DummyDataGenerator.chartColors[3], 10, 30, 0, totalVat, "Double", 0);
		sparks.addComponent(sparkVat);

		sparkDis = new SparklineChart("Total Discount", "", "",
				DummyDataGenerator.chartColors[5], 10, 34, 0, totalDis, "Double", 0);
		sparks.addComponent(sparkDis);

		vert.addComponents(hori, sparks);

		pnlSummary.setContent(vert);

		addAction();

		return pnlSummary;
	}

	private void addAction()
	{
		txtFromDate.addValueChangeListener(event ->
		{
			cm.dateCompare(txtFromDate, txtToDate);
			drawChart("%");
		});

		txtToDate.addValueChangeListener(event ->
		{
			drawChart("%");
		});
	}

	public void drawChart(String branch)
	{
		totalOrd = 0; totalRev = 0; totalVat = 0; totalDis = 0;
		try
		{
			String from = cm.dfDb.format(txtFromDate.getValue());
			String to = cm.dfDb.format(txtToDate.getValue());
			String sql = "select (select COUNT(vInvoiceId) from trans.tbInvoiceInfo where CONVERT(date, dSettleDate)"+
					" between '"+from+"' and '"+to+"' and vBranchId like '"+branch+"' and iClosed = 1) iTotalOrders,"+
					" isnull(sum(ind.mAmountWithoutVat), 0) mNetRevenue, isnull(sum(ind.mTotalVatAmount), 0) mTotalVat,"+
					" isnull(sum(mDisCalculated), 0) mDiscount from trans.tbInvoiceInfo ini, trans.tbInvoiceDetails ind"+
					" where ini.vInvoiceId = ind.vInvoiceId and CONVERT(date, ini.dSettleDate) between '"+from+"' and"+
					" '"+to+"' and ini.vBranchId like '"+branch+"' and ini.iClosed = 1";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				totalOrd = Integer.parseInt(element[0].toString());
				totalRev = Double.parseDouble(element[1].toString());
				totalVat = Double.parseDouble(element[2].toString());
				totalDis = Double.parseDouble(element[3].toString());
			}

			sparkOrd.current.setValue(cm.deciInt.format(totalOrd));
			sparkRev.current.setValue(cm.setComma(totalRev));
			sparkVat.current.setValue(cm.setComma(totalVat));
			sparkDis.current.setValue(cm.setComma(totalDis));
		}
		catch (Exception e)
		{ System.out.println(e); }
	}
}