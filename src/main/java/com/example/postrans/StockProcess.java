package com.example.postrans;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.MessageBox;
import com.example.gateway.PhysicalStockGateway;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class StockProcess extends Window
{
	private SessionBean sessionBean;
	private PhysicalStockGateway psg = new PhysicalStockGateway();

	private CommonButton cBtnSearch = new CommonButton("", "", "", "", "", "Search", "", "", "");
	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "", "Exit");
	private PopupDateField txtFromDate, txtToDate;

	private Table tblPhysicalStockDetailsList;
	private ArrayList<Label> tblblItemName = new ArrayList<Label>();
	private ArrayList<Label> tblblPhysicalStockNo = new ArrayList<Label>();
	private ArrayList<Label> tblblPhysicalStockDate = new ArrayList<Label>();
	private ArrayList<Label> tblblUnitName = new ArrayList<Label>();
	private ArrayList<Label> tblblCatName = new ArrayList<Label>();
	private ArrayList<CommaField> tbAmtSoftStkQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtPhyStkQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtDiffQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtPurchaseRate = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtTotalAmount = new ArrayList<CommaField>();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private CommonMethod cm;

	public StockProcess(SessionBean sessionBean)
	{
		this.sessionBean = sessionBean;
		this.setCaption("Process Physical Stock ::"+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);
		setWidth("1300px");
		setHeight("600px");

		setContent(buildLayout());
		addActions();
		loadMenuList();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{ insertEditData(); }
		});

		cBtn.btnExit.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{ close(); }
		});

		cBtnSearch.btnSearch.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{ loadMenuList(); }
		});
		focusEnter();
	}

	private void insertEditData()
	{
		MessageBox mb = new MessageBox(getUI(), "Are you sure?", MessageBox.Icon.QUESTION, "Do you want to save information?",
				new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
				new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
		mb.show(new EventListener()
		{
			public void buttonClicked(ButtonType buttonType)
			{
				if (buttonType == ButtonType.YES)
				{
					try
					{ insertData(); }
					catch(Exception ex)
					{ System.out.println(ex); }
				}
				else if (buttonType == ButtonType.NO)
				{ cBtn.btnSave.setEnabled(true); }
			}
		});
	}

	private void insertData()
	{		
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());
		String branchId = sessionBean.getBranchId();
		String userId = sessionBean.getUserId();
		String userName = sessionBean.getUserName();

		if (psg.PhysicalStockProcess(fromDate, toDate, userName, userId, branchId))
		{
			txtClear();
			cm.showNotification("success", "Successfull!", "Processed successfully.");
			cBtn.btnSave.setEnabled(true);
		}
		else
		{ cm.showNotification("failure", "Error!", "Couldn't Processed information."); }
	}

	private void loadMenuList()
	{
		tableClear();
		int i = 0;
		String sql = "";

		sql = "select I.vItemCode+'-'+I.vItemName, vCategoryName, vUnitName, Sum(b.mPhysicalStockQty)mPhysicalStockQty, (select [dbo].[funcItemRate]"+
				" ('"+cm.dfDb.format(txtToDate.getValue())+"',b.vItemId,'"+sessionBean.getBranchId()+"'))mPurchaseRate, (select [dbo].[funcStockQty]"+
				" ('"+cm.dfDb.format(txtToDate.getValue())+"',b.vItemId,'"+sessionBean.getBranchId()+"'))mSoftwareQty, vPhysicalStockNo, a.dPhysicalStockDate"+
				" from trans.tbPhysicalStockInfo a inner join trans.tbPhysicalStockDetails b  on a.vPhysicalStockId = b.vPhysicalStockId inner join"+
				" master.tbRawIteminfo I on b.vItemId=I.vItemId where a.vBranchId like '"+sessionBean.getBranchId()+"' and a.dPhysicalStockDate between"+
				" '"+cm.dfDb.format(txtFromDate.getValue())+"' and '"+cm.dfDb.format(txtToDate.getValue())+"' and b.iActive=1 group by b.vItemId, I.vItemName,"+
				" vCategoryName, vUnitName, vPhysicalStockNo, a.dPhysicalStockDate order by a.dPhysicalStockDate, vPhysicalStockNo, b.vItemId";
		//System.out.println(sql);
		for(Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			if(tblblItemName.size() <= i)
			{ tableRowAdd(i); }

			tblblItemName.get(i).setValue(element[0].toString());
			tblblUnitName.get(i).setValue(element[2].toString());
			tblblCatName.get(i).setValue(element[1].toString());
			tbAmtPhyStkQty.get(i).setReadOnly(false);
			tbAmtPhyStkQty.get(i).setValue(cm.deciMn.format(Double.parseDouble(element[3].toString())));
			tbAmtPhyStkQty.get(i).setReadOnly(true);
			tbAmtPurchaseRate.get(i).setReadOnly(false);
			tbAmtPurchaseRate.get(i).setValue(cm.deciMn.format(Double.parseDouble(element[4].toString())));
			tbAmtPurchaseRate.get(i).setReadOnly(true);
			tbAmtSoftStkQty.get(i).setReadOnly(false);
			tbAmtSoftStkQty.get(i).setValue(cm.deciMn.format(Double.parseDouble(element[5].toString())));
			tbAmtSoftStkQty.get(i).setReadOnly(true);
			tblblPhysicalStockNo.get(i).setValue(element[6].toString());
			tblblPhysicalStockDate.get(i).setValue(cm.dfBd.format(element[7]).toString());
			totalAmount("Total");
			i++;
		}
	}

	private void calculateVatData(int ar)
	{
		double TotalSoftQty = cm.getAmtValue(tbAmtSoftStkQty.get(ar));
		double TotalPhyQty = cm.getAmtValue(tbAmtPhyStkQty.get(ar));
		double Rate = cm.getAmtValue(tbAmtPurchaseRate.get(ar));
		double TotalAmountCal = cm.getRound(TotalPhyQty * Rate);
		double TotalDiff = cm.getRound(TotalSoftQty - TotalPhyQty);

		tbAmtTotalAmount.get(ar).setReadOnly(false);
		tbAmtTotalAmount.get(ar).setValue(cm.deciMn.format(TotalAmountCal));
		tbAmtTotalAmount.get(ar).setReadOnly(true);

		tbAmtDiffQty.get(ar).setReadOnly(false);
		tbAmtDiffQty.get(ar).setValue(cm.deciMn.format(TotalDiff));
		tbAmtDiffQty.get(ar).setReadOnly(true);
		totalAmount("Total");
	}

	public void tableClear()
	{
		tblPhysicalStockDetailsList.removeAllItems();
		tblblItemName.clear();
		tableRowAdd(0);
	}

	private void focusEnter()
	{
		allComp.add(txtFromDate);
		allComp.add(txtToDate);
		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}

	public void txtClear()
	{
		txtFromDate.setValue(new Date());
		txtToDate.setValue(new Date());
		tableClear();
		totalAmount("");
	}

	//Master Component Set
	private Component buildLayout()
	{
		GridLayout grid = new GridLayout(6, 6);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();
		
		HorizontalLayout hori = new HorizontalLayout();
		hori.setSpacing(true);

		txtFromDate  = new PopupDateField();
		txtFromDate.setImmediate(true);
		txtFromDate.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFromDate.setValue(new Date());
		txtFromDate.setWidth("120px");
		txtFromDate.setDateFormat("dd-MM-yyyy");
		txtFromDate.setDescription("From Date");
		txtFromDate.setRequired(true);
		txtFromDate.setRequiredError("This field is required");

		txtToDate = new PopupDateField();
		txtToDate.setImmediate(true);
		txtToDate.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtToDate.setValue(new Date());
		txtToDate.setWidth("120px");
		txtToDate.setDateFormat("dd-MM-yyyy");
		txtToDate.setDescription("To Date");
		txtToDate.setRequired(true);
		txtToDate.setRequiredError("This field is required");

		hori.addComponents(new Label("To Date: "), txtFromDate, new Label("To Date: "), txtToDate, cBtnSearch);

		grid.addComponent(hori, 0, 0);
		grid.addComponent(buildTable(), 0, 1, 5, 1);
		grid.addComponent(cBtn, 0, 2, 5, 2);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);
		
		return grid;
	}

	//Table Component Set
	private Table buildTable()
	{
		tblPhysicalStockDetailsList = new Table();
		tblPhysicalStockDetailsList.setSelectable(true);
		tblPhysicalStockDetailsList.setFooterVisible(true);
		tblPhysicalStockDetailsList.setColumnCollapsingAllowed(true);
		tblPhysicalStockDetailsList.addStyleName(ValoTheme.TABLE_SMALL);
		tblPhysicalStockDetailsList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblPhysicalStockDetailsList.setPageLength(11);
		tblPhysicalStockDetailsList.setWidth("100%");

		tblPhysicalStockDetailsList.addContainerProperty("Date", Label.class, new Label(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnWidth("Date", 80);

		tblPhysicalStockDetailsList.addContainerProperty("Phy.Stk No", Label.class, new Label(), null, null, Align.CENTER);
		
		tblPhysicalStockDetailsList.addContainerProperty("Item Name", Label.class, new Label(), null, null, Align.CENTER);
		
		tblPhysicalStockDetailsList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);
		
		tblPhysicalStockDetailsList.addContainerProperty("Category", Label.class, new Label(), null, null, Align.CENTER);
		
		tblPhysicalStockDetailsList.addContainerProperty("Soft.Stk Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnWidth("Soft.Stk Qty", 90);

		tblPhysicalStockDetailsList.addContainerProperty("Phy.Stk Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnWidth("Phy.Stk Qty", 90);

		tblPhysicalStockDetailsList.addContainerProperty("Difference", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblPhysicalStockDetailsList.setColumnWidth("Difference", 100);

		tblPhysicalStockDetailsList.addContainerProperty("Rate", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblPhysicalStockDetailsList.setColumnWidth("Rate", 120);

		tblPhysicalStockDetailsList.addContainerProperty("Total Amount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblPhysicalStockDetailsList.setColumnWidth("Total Amount", 120);
		tableRowAdd(0);
		return tblPhysicalStockDetailsList;
	}

	public void tableRowAdd(final int ar)
	{
		try
		{
			tblblPhysicalStockDate.add(ar, new Label());
			tblblPhysicalStockDate.get(ar).setWidth("100%");
			tblblPhysicalStockDate.get(ar).setImmediate(true);
			tblblPhysicalStockDate.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tblblPhysicalStockNo.add(ar, new Label());
			tblblPhysicalStockNo.get(ar).setWidth("100%");
			tblblPhysicalStockNo.get(ar).setImmediate(true);
			tblblPhysicalStockNo.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tblblItemName.add(ar, new Label());
			tblblItemName.get(ar).setWidth("100%");
			tblblItemName.get(ar).setImmediate(true);
			tblblItemName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tblblUnitName.add(ar, new Label());
			tblblUnitName.get(ar).setWidth("100%");
			tblblUnitName.get(ar).setImmediate(true);
			tblblUnitName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tblblCatName.add(ar, new Label());
			tblblCatName.get(ar).setWidth("100%");
			tblblCatName.get(ar).setImmediate(true);
			tblblCatName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbAmtSoftStkQty.add(ar, new CommaField());
			tbAmtSoftStkQty.get(ar).setWidth("100%");
			tbAmtSoftStkQty.get(ar).setImmediate(true);
			tbAmtSoftStkQty.get(ar).setReadOnly(true);
			tbAmtSoftStkQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtSoftStkQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtSoftStkQty.get(ar).setDescription("Software Stock Qty");
			tbAmtSoftStkQty.get(ar).addValueChangeListener(new ValueChangeListener()
			{
				public void valueChange(ValueChangeEvent event)
				{ calculateVatData(ar); }
			});

			tbAmtPhyStkQty.add(ar, new CommaField());
			tbAmtPhyStkQty.get(ar).setWidth("100%");
			tbAmtPhyStkQty.get(ar).setImmediate(true);
			tbAmtPhyStkQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtPhyStkQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtPhyStkQty.get(ar).setReadOnly(true);
			tbAmtPhyStkQty.get(ar).setDescription("Physical Stock Qty");
			tbAmtPhyStkQty.get(ar).addValueChangeListener(new ValueChangeListener()
			{
				public void valueChange(ValueChangeEvent event)
				{ calculateVatData(ar); }
			});

			tbAmtDiffQty.add(ar, new CommaField());
			tbAmtDiffQty.get(ar).setWidth("100%");
			tbAmtDiffQty.get(ar).setImmediate(true);
			tbAmtDiffQty.get(ar).setReadOnly(true);
			tbAmtDiffQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtDiffQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

			tbAmtPurchaseRate.add(ar, new CommaField());
			tbAmtPurchaseRate.get(ar).setWidth("100%");
			tbAmtPurchaseRate.get(ar).setImmediate(true);
			tbAmtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtPurchaseRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtPurchaseRate.get(ar).setReadOnly(true);
			tbAmtPurchaseRate.get(ar).addValueChangeListener(new ValueChangeListener()
			{
				public void valueChange(ValueChangeEvent event)
				{ calculateVatData(ar); }
			});

			tbAmtTotalAmount.add(ar, new CommaField());
			tbAmtTotalAmount.get(ar).setWidth("100%");
			tbAmtTotalAmount.get(ar).setImmediate(true);
			tbAmtTotalAmount.get(ar).setReadOnly(true);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tblPhysicalStockDetailsList.addItem(new Object[]{tblblPhysicalStockDate.get(ar), tblblPhysicalStockNo.get(ar), tblblItemName.get(ar), 
					tblblUnitName.get(ar), tblblCatName.get(ar), tbAmtSoftStkQty.get(ar), tbAmtPhyStkQty.get(ar), tbAmtDiffQty.get(ar), 
					tbAmtPurchaseRate.get(ar), tbAmtTotalAmount.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	public double totalAmount(String flag)
	{
		double ret = 0, totalamt = 0;

		for (int i=0; i<tblblItemName.size(); i++)
		{ totalamt += cm.getAmtValue(tbAmtTotalAmount.get(i)); }
		
		tblPhysicalStockDetailsList.setColumnFooter("Total Amount", cm.setComma(totalamt));
		tblPhysicalStockDetailsList.setColumnAlignment("Total Amount", Align.RIGHT);

		if (flag.equals("Total")) 
		{ ret = totalamt; }
		return ret;
	}
}
