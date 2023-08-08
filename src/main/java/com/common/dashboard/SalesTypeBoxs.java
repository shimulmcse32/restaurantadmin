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
public class SalesTypeBoxs extends Panel
{
	private SessionBean sessionBean;
	private CommonMethod cm;

	private PopupDateField txtFromDate, txtToDate;
	private int totalDine = 0, totalDeli = 0, totalTake = 0, totalCate = 0;

	private SparklineChart sparkDin, sparkDel, sparkTak, sparkCat;

	public SalesTypeBoxs(SessionBean sessionBean)
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

		sparkDin = new SparklineChart("Total Dine In", "", "",
				DummyDataGenerator.chartColors[0], 10, 20, 0, 0, "Int", totalDine);
		sparks.addComponent(sparkDin);

		sparkDel = new SparklineChart("Total Delivery", "", "",
				DummyDataGenerator.chartColors[2], 10, 89, 0, 0, "Int", totalDeli);
		sparks.addComponent(sparkDel);

		sparkTak = new SparklineChart("Total Take Away", "", "",
				DummyDataGenerator.chartColors[3], 10, 30, 0, 0, "Int", totalTake);
		sparks.addComponent(sparkTak);

		sparkCat = new SparklineChart("Total Catering", "", "",
				DummyDataGenerator.chartColors[5], 10, 34, 0, 0, "Int", totalCate);
		sparks.addComponent(sparkCat);

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
		totalDine = 0; totalDeli = 0; totalTake = 0; totalCate = 0;
		try
		{
			String from = cm.dfDb.format(txtFromDate.getValue());
			String to = cm.dfDb.format(txtToDate.getValue());
			String sql = "select sty.iSalesTypeId, sty.vSalesType, COUNT(ini.vInvoiceId) iTotal from trans.tbInvoiceInfo ini,"+
					" master.tbSalesType sty where ini.iSalesTypeId = sty.iSalesTypeId and CONVERT(date, ini.dSettleDate)"+
					" between '"+from+"' and '"+to+"' and ini.vBranchId like '"+branch+"' group by sty.iSalesTypeId,"+
					" sty.vSalesType";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (element[0].toString().equals("1")) { totalDine = Integer.parseInt(element[2].toString()); }
				if (element[0].toString().equals("2")) { totalDeli = Integer.parseInt(element[2].toString()); }
				if (element[0].toString().equals("3")) { totalTake = Integer.parseInt(element[2].toString()); }
				if (element[0].toString().equals("4")) { totalCate = Integer.parseInt(element[2].toString()); }
			}

			sparkDin.current.setValue(cm.deciInt.format(totalDine));
			sparkDel.current.setValue(cm.deciInt.format(totalDeli));
			sparkTak.current.setValue(cm.deciInt.format(totalTake));
			sparkCat.current.setValue(cm.deciInt.format(totalCate));
		}
		catch (Exception e)
		{ System.out.println(e); }
	}
}