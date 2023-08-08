package com.common.dashboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.common.share.AbstractVaadinChartExample;
import com.common.share.CommonMethod;
import com.common.share.SessionBean;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AxisTitle;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataLabels;
import com.vaadin.addon.charts.model.Exporting;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.PlotOptionsColumn;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.Tooltip;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Style;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class PaymentMethod extends AbstractVaadinChartExample
{
	private SessionBean sessionBean;
	private CommonMethod cm;
	private Chart chart;
	private Configuration config;
	private PopupDateField txtFromDate, txtToDate;

	private XAxis xAxis = new XAxis();
	private ListSeries list;

	public PaymentMethod(SessionBean sessionBean)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
	}

	protected Component getChart()
	{
		VerticalLayout lay = new VerticalLayout();
		lay.setSizeFull();

		HorizontalLayout hori = new HorizontalLayout();
		hori.setSpacing(true);

		txtFromDate = new PopupDateField();
		txtFromDate.setImmediate(true);
		txtFromDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDate.setValue(new Date());
		txtFromDate.setWidth("110px");
		txtFromDate.setDateFormat("dd-MM-yyyy");
		txtFromDate.setDescription("From date");
		txtFromDate.setInputPrompt("From date");

		txtToDate = new PopupDateField();
		txtToDate.setImmediate(true);
		txtToDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDate.setValue(new Date());
		txtToDate.setWidth("110px");
		txtToDate.setDateFormat("dd-MM-yyyy");
		txtToDate.setDescription("To date");
		txtToDate.setInputPrompt("To date");

		Label lbl = new Label("Dates: ");
		lbl.setStyleName(ValoTheme.LABEL_SMALL);

		hori.setSpacing(true);
		hori.addComponents(lbl, txtFromDate, txtToDate);

		Component ret = createChart();
		ret.setWidth("100%");
		ret.setHeight("100%");

		lay.addComponents(hori, ret);

		drawChart("%");
		addAction();

		return lay;
	}

	private void addAction()
	{
		txtFromDate.addValueChangeListener(event -> { cm.dateCompare(txtFromDate, txtToDate); drawChart("%"); } );
		txtToDate.addValueChangeListener(event -> { drawChart("%"); } );
	}

	protected Chart createChart()
	{
		chart = new Chart(ChartType.COLUMN);
		chart.setHeight("450px");
		chart.setWidth("100%");

		config = chart.getConfiguration();
		config.setTitle(new Title("Payment Method"));
		setSizeFull();

		Labels labels = new Labels();
		labels.setRotation(-45);
		labels.setAlign(HorizontalAlign.RIGHT);
		Style style = new Style();
		style.setFontSize("13px");
		style.setFontFamily("Verdana, sans-serif");
		labels.setStyle(style);
		xAxis.setLabels(labels);
		config.addxAxis(xAxis);

		YAxis yAxis = new YAxis();
		yAxis.setMin(0);
		yAxis.setTitle(new AxisTitle(""));
		config.addyAxis(yAxis);

		Legend legend = new Legend();
		legend.setEnabled(false);
		config.setLegend(legend);

		Exporting exporting = new Exporting(true);
		exporting.setFilename("Most selling top 10 menus");
		config.setExporting(exporting);

		Tooltip tooltip = new Tooltip();
		tooltip.setFormatter("'<b>'+ this.x +'</b><br/>'+'Total amount: '+ Highcharts.numberFormat(this.y, 0) +' '");
		config.setTooltip(tooltip);

		chart.drawChart(config);
		return chart;
	}

	public void drawChart(String branch)
	{
		clear();
		String[] name = new String[50];
		Number[] amount = new Number[10];
		try
		{
			String from = cm.dfDb.format(txtFromDate.getValue());
			String to = cm.dfDb.format(txtToDate.getValue());
			config.getSubTitle().setText("From "+cm.dfBd.format(txtFromDate.getValue())+" to "+cm.dfBd.format(txtToDate.getValue()));

			String sql = "select pm.vMethodName, SUM(ins.mAmount) + SUM(ins.mAdjustment) mAmount"+
					" from trans.tbInvoiceInfo ini inner join trans.tbInvoiceSettle ins on ins.vInvoiceId = ini.vInvoiceId"+
					" inner join master.tbPaymentMethods pm on ins.iMethodId = pm.iMethodId where ini.iClosed = 1 and"+
					" CONVERT(date, ini.dSettleDate) between '"+from+"' and '"+to+"' and ini.vBranchId like '"+branch+"' group"+
					" by pm.vMethodName order by pm.vMethodName";
			//System.out.println(sql);
			int i = 0;
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				name[i] = element[0].toString();
				amount[i] = (Number) element[1];
				i++;
			}
		}
		catch (Exception ex)
		{ System.out.println("Select "+ex); }

		xAxis.setCategories(name);
		list = new ListSeries("", amount);

		DataLabels dataLabels = new DataLabels();
		dataLabels.setEnabled(true);
		dataLabels.setRotation(-90);
		dataLabels.setColor(new SolidColor(255, 255, 255));
		dataLabels.setAlign(HorizontalAlign.RIGHT);
		dataLabels.setX(4);
		dataLabels.setY(10);
		dataLabels.setFormatter("this.y");

		PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
		plotOptionsColumn.setDataLabels(dataLabels);
		list.setPlotOptions(plotOptionsColumn);
		config.addSeries(list);
	}

	public void clear()
	{
		config.setSeries(new ArrayList<>());
		chart.drawChart(config);
	}
}