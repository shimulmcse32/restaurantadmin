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
import com.vaadin.addon.charts.model.Credits;
import com.vaadin.addon.charts.model.Exporting;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.Tooltip;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class MonthlySalesType extends AbstractVaadinChartExample
{
	private SessionBean sessionBean;
	private CommonMethod cm;
	private Chart chart;
	private Configuration config;
	private ListSeries list;
	private PopupDateField txtMonth;

	public MonthlySalesType(SessionBean sessionBean)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
	}

	protected Component getChart()
	{
		VerticalLayout lay = new VerticalLayout();
		lay.setSizeFull();

		Component ret = createChart();
		ret.setWidth("100%");
		ret.setHeight("100%");

		HorizontalLayout hori = new HorizontalLayout();
		hori.setSpacing(true);

		txtMonth = new PopupDateField();
		txtMonth.setImmediate(true);
		txtMonth.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtMonth.setValue(new Date());
		txtMonth.setWidth("140px");
		txtMonth.setDateFormat("MMMMM-yyyy");
		txtMonth.setDescription("Month");
		txtMonth.setInputPrompt("Month");
		txtMonth.setResolution(Resolution.MONTH);

		Label lbl = new Label("Month: ");
		lbl.setStyleName(ValoTheme.LABEL_SMALL);

		hori.setSpacing(true);
		hori.addComponents(lbl, txtMonth);

		lay.addComponents(hori, ret);

		addAction();
		drawChart("%");

		return lay;
	}

	private void addAction()
	{
		txtMonth.addValueChangeListener(event ->
		{
			clear();
			drawChart("%");
		});
	}

	protected Chart createChart()
	{
		chart = new Chart(ChartType.LINE);

		config = chart.getConfiguration();
		config.setTitle(new Title("Monthly Sales Type Details"));
		setSizeFull();

		Tooltip tooltip = new Tooltip();
		tooltip.setShared(true);
		tooltip.setUseHTML(true);
		tooltip.setHeaderFormat("<small>{point.key}</small><table>");
		tooltip.setPointFormat("<tr><td style=\"color: {series.color}\">{series.name}:"+
				" </td><td style=\"text-align: right\"><b>{point.y} Orders</b></td></tr>");
		tooltip.setFooterFormat("</table>");
		config.setTooltip(tooltip);

		XAxis xAxis = new XAxis();
		xAxis.setCategories("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
				"14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26",
				"27", "28", "29", "30", "31");
		config.addxAxis(xAxis);

		YAxis yAxis = new YAxis();
		yAxis.setTitle(new AxisTitle("Orders"));
		Labels labels = new Labels();
		labels.setFormatter("this.value");
		yAxis.setLabels(labels);
		config.addyAxis(yAxis);

		Exporting exporting = new Exporting(true);
		exporting.setFilename("Monthly Sales Type Details");
		config.setExporting(exporting);

		config.setCredits(new Credits(false));

		//setTitle(cm.dfBd.format(fromDate)+" To "+cm.dfBd.format(toDate));

		chart.drawChart(config);
		return chart;
	}

	public void clear()
	{
		config.setSeries(new ArrayList<>());
		chart.drawChart(config);
	}

	public void drawChart(String branch)
	{
		try
		{
			config.getSubTitle().setText("Month: "+cm.dfMY.format(txtMonth.getValue()));
			String month = cm.dfDb.format(txtMonth.getValue());
			String sql = "select iSalesTypeId, vSalesType, mDay1, mDay2, mDay3, mDay4, mDay5, mDay6, mDay7, mDay8,"+
					" mDay9, mDay10, mDay11, mDay12, mDay13, mDay14, mDay15, mDay16, mDay17, mDay18, mDay19,"+
					" mDay20, mDay21, mDay22, mDay23, mDay24, mDay25, mDay26, mDay27, mDay28, mDay29, mDay30,"+
					" mDay31 from [dbo].[funDashSalesTypeMonthly]('"+month+"', '"+branch+"') order by iSalesTypeId";
			//System.out.println(sql);
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				list = new ListSeries();
				list.setName(element[1].toString());
				list.setData(Double.parseDouble(cm.deciMn.format(element[2])),
						Double.parseDouble(cm.deciMn.format(element[3])),
						Double.parseDouble(cm.deciMn.format(element[4])),
						Double.parseDouble(cm.deciMn.format(element[5])),
						Double.parseDouble(cm.deciMn.format(element[6])),
						Double.parseDouble(cm.deciMn.format(element[7])),
						Double.parseDouble(cm.deciMn.format(element[8])),
						Double.parseDouble(cm.deciMn.format(element[9])),
						Double.parseDouble(cm.deciMn.format(element[10])),
						Double.parseDouble(cm.deciMn.format(element[11])),
						Double.parseDouble(cm.deciMn.format(element[12])),
						Double.parseDouble(cm.deciMn.format(element[13])),
						Double.parseDouble(cm.deciMn.format(element[14])),
						Double.parseDouble(cm.deciMn.format(element[15])),
						Double.parseDouble(cm.deciMn.format(element[16])),
						Double.parseDouble(cm.deciMn.format(element[17])),
						Double.parseDouble(cm.deciMn.format(element[18])),
						Double.parseDouble(cm.deciMn.format(element[19])),
						Double.parseDouble(cm.deciMn.format(element[20])),
						Double.parseDouble(cm.deciMn.format(element[21])),
						Double.parseDouble(cm.deciMn.format(element[22])),
						Double.parseDouble(cm.deciMn.format(element[23])),
						Double.parseDouble(cm.deciMn.format(element[24])),
						Double.parseDouble(cm.deciMn.format(element[25])),
						Double.parseDouble(cm.deciMn.format(element[26])),
						Double.parseDouble(cm.deciMn.format(element[27])),
						Double.parseDouble(cm.deciMn.format(element[28])),
						Double.parseDouble(cm.deciMn.format(element[29])),
						Double.parseDouble(cm.deciMn.format(element[30])),
						Double.parseDouble(cm.deciMn.format(element[31])),
						Double.parseDouble(cm.deciMn.format(element[32])));

				/*DataLabels dataLabels = new DataLabels();
				dataLabels.setEnabled(true);
				//dataLabels.setRotation(-90);
				dataLabels.setColor(new SolidColor(255, 255, 255));
				dataLabels.setAlign(HorizontalAlign.CENTER);
				dataLabels.setX(4);
				dataLabels.setY(10);
				dataLabels.setFormatter("this.y");
				PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
				plotOptionsColumn.setDataLabels(dataLabels);
				list.setPlotOptions(plotOptionsColumn);*/

				config.addSeries(list);
			}
		}
		catch (Exception ex)
		{ System.out.println("Select "+ex); }
	}
}