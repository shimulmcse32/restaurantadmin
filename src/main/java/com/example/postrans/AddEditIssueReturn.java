package com.example.postrans;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.common.share.CommaField;
import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.IssueReturnGateway;
import com.example.model.IssueReturnModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditIssueReturn extends Window
{
	private SessionBean sessionBean;
	private String flag, returnId;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private IssueReturnGateway irg = new IssueReturnGateway();

	private TextField txtReturnNo/* , txtRemarks, txtReferenceNo, txtReturnedBy */;
	private ComboBox cmbIssueDetails;
	private PopupDateField txtReturnDate , txtIssueDate;

	private Table tblReturnDetailsList;
	private ArrayList<Label> tbLblItemId = new ArrayList<Label>();
	private ArrayList<Label> tbLblItemDetails = new ArrayList<Label>();
	private ArrayList<Label> tbLblUnitId = new ArrayList<Label>();
	private ArrayList<Label> tbLblUnitName = new ArrayList<Label>();
	private ArrayList<Label> tbLblCatId = new ArrayList<Label>();
	private ArrayList<Label> tbLblCatName = new ArrayList<Label>();
	private ArrayList<TextField> tbTxtDescription = new ArrayList<TextField>();
	private ArrayList<CommaField> tbAmtStockQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtIssuedQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtReturnedQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtMainRate = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtCostMargin = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtIssueRate = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtTotalAmount = new ArrayList<CommaField>();

	private ArrayList<Component> allComp = new ArrayList<Component>();
	private HashMap<String, String> hmIssBranchId = new HashMap<String, String>();

	private CommonMethod cm;
	private boolean changes = false, action = false;

	public AddEditIssueReturn(SessionBean sessionBean, String flag, String returnId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.returnId = returnId;
		this.setCaption(flag+" Issue Return :: "+this.sessionBean.getCompanyName() +
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);
		setWidth("100%");
		setHeight("600px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
		{ masterValidation(); });

		cBtn.btnExit.addClickListener(event ->
		{ close(); });

		cmbIssueDetails.addValueChangeListener(event ->
		{ setIssueDetails(); });

		txtReturnDate.addValueChangeListener(event ->
		{ setReturnNo(); });

		txtIssueDate.addValueChangeListener(event ->
		{ loadIssueNo(); });

		loadIssueNo();
		setReturnNo();
		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void setReturnNo()
	{
		if (txtReturnDate.getValue() != null)
		{
			txtReturnNo.setReadOnly(false);
			txtReturnNo.setValue(irg.getIssueReturnNo(cm.dfDb.format(txtReturnDate.getValue())));
			txtReturnNo.setReadOnly(true);
		}
	}

	private void loadIssueNo()
	{
		cmbIssueDetails.removeAllItems();
		String date = cm.dfDb.format(txtIssueDate.getValue());
		String sql = "select iss.vIssueId, iss.vIssueNo, bm.vBranchName, iss.vBranchFrom from trans.tbIssueInfo iss,"+
				" master.tbBranchMaster bm where iss.vBranchFrom = bm.vBranchId and iss.vIssueId not in (select vIssueId"+
				" from trans.tbIssueReturnInfo where vIssueReturnId != '"+returnId+"') and iss.vBranchTo = '"+sessionBean.getBranchId()+"'"+
				" and vStatusId = 'S6' and iss.dIssueDate = '"+date+"' order by iss.dIssueDate, iss.iAutoId desc";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbIssueDetails.addItem(element[0].toString());
			cmbIssueDetails.setItemCaption(element[0].toString(), element[1].toString()+" - "+element[2].toString());
			hmIssBranchId.put(element[0].toString(), element[3].toString());
		}
	}

	private void setIssueDetails()
	{
		tableClear();
		String issId = cmbIssueDetails.getValue() != null?cmbIssueDetails.getValue().toString():"";
		String sql = "select isd.vItemId, rii.vItemCode+' - '+rii.vItemName vItemDetails, isd.vUnitId, uni.vUnitName,"+
				" rii.vCategoryId, cat.vCategoryName, isd.vRemarks, isd.mIssueQty, isd.mCostMargin, isd.mMainRate,"+
				" isd.mIssueRate, (select [dbo].[funcStockQty](isd.vItemId, '"+sessionBean.getBranchId()+"')) mStockQty"+
				" from trans.tbIssueInfo isi, trans.tbIssueDetails isd, master.tbRawItemInfo rii, master.tbUnitInfo uni,"+
				" master.tbItemCategory cat where isi.vIssueId = isd.vIssueId and isd.vItemId = rii.vItemId and"+
				" isd.vUnitId = uni.iUnitId and rii.vCategoryId = cat.vCategoryId and isi.vIssueId = '"+issId+"' order"+
				" by isd.iAutoId asc";
		int ar = 0;
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (tbLblItemDetails.size() <= ar)
			{ tableRowAdd(tbLblItemDetails.size()); }

			tbLblItemId.get(ar).setValue(element[0].toString());
			tbLblItemDetails.get(ar).setValue(element[1].toString());
			tbLblUnitId.get(ar).setValue(element[2].toString());
			tbLblUnitName.get(ar).setValue(element[3].toString());
			tbLblCatId.get(ar).setValue(element[4].toString());
			tbLblCatName.get(ar).setValue(element[5].toString());
			tbTxtDescription.get(ar).setValue(element[6].toString());
			tbAmtIssuedQty.get(ar).setValue(Double.parseDouble(element[7].toString()));
			tbAmtCostMargin.get(ar).setValue(Double.parseDouble(element[8].toString()));
			tbAmtMainRate.get(ar).setValue(Double.parseDouble(element[9].toString()));
			tbAmtIssueRate.get(ar).setValue(Double.parseDouble(element[10].toString()));

			tbAmtStockQty.get(ar).setReadOnly(false);
			tbAmtStockQty.get(ar).setValue(Double.parseDouble(element[11].toString()));
			tbAmtStockQty.get(ar).setReadOnly(true);
			ar++;
		}
		action = true;
	}

	private void masterValidation()
	{
		if (txtReturnDate.getValue() != null)
		{
			if (!txtReturnNo.getValue().toString().trim().isEmpty())
			{
				if (cmbIssueDetails.getValue() != null)
				{
					if (totalQty() > 0)
					{
						cBtn.btnSave.setEnabled(false);
						insertEditData();
					}
					else
					{
						tbAmtReturnedQty.get(0).focus();
						cm.showNotification("warning", "Warning!", "Provide valid data in table.");
					}
				}
				else
				{
					cmbIssueDetails.focus();
					cm.showNotification("warning", "Warning!", "Provide Issue No");
				}
			}
			else
			{ cm.showNotification("warning", "Warning!", "Return No not found"); }
		}
		else
		{
			txtReturnDate.focus();
			cm.showNotification("warning", "Warning!", "Provide Return Date");
		}
	}

	private double totalQty()
	{
		double ret = 0;
		for (int i=0; i<tbLblItemDetails.size(); i++)
		{
			if (cm.getAmtValue(tbAmtReturnedQty.get(i)) > 0)
			{ ret += cm.getAmtValue(tbAmtReturnedQty.get(i)); }
		}
		return ret;
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
		IssueReturnModel irm = new IssueReturnModel();
		setValueForSave(irm);
		if (irg.insertEditData(irm, flag))
		{
			txtClear();
			cm.showNotification("success", "Successfull!", "All information saved successfully.");
			cBtn.btnSave.setEnabled(true);

			IssueReturnInfo report = new IssueReturnInfo(sessionBean, "");
			report.viewReportSingle(irm.getIssueReturnId());

			if (flag.equals("Edit"))
			{ close(); }
		}
		else
		{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
	}

	private void setValueForSave(IssueReturnModel irm)
	{
		irm.setBranchId(sessionBean.getBranchId());
		irm.setIssueReturnId(flag.equals("Add")? irg.getIssueReturnId(sessionBean.getBranchId()):returnId);
		irm.setIssueReturnNo(flag.equals("Add")? irg.getIssueReturnNo(cm.dfDb.format(txtReturnDate.getValue())).toString():
			txtReturnNo.getValue().toString());
		irm.setIssueId(cmbIssueDetails.getValue().toString());
		irm.setIssueReturnDate(txtReturnDate.getValue());
		irm.setBranchFrom(sessionBean.getBranchId());
		irm.setBranchTo(hmIssBranchId.get(cmbIssueDetails.getValue().toString()));
		irm.setTotalAmount(Double.parseDouble(tblReturnDetailsList.getColumnFooter("Total Amount").replace(",", "")));
		irm.setStatusId("S5");
		irm.setApproveBy("");
		irm.setCancelBy("");
		irm.setCancelReason("");
		irm.setCreatedBy(sessionBean.getUserId());
		irm.setDetailsSql(saveDetails(irm.getIssueReturnId()));
		irm.setDetailsChange(changes);
	}

	private String saveDetails(String returnId)
	{
		String issueId = cmbIssueDetails.getValue() == null? "":cmbIssueDetails.getValue().toString();
		String sql = flag.equals("Add")? "":" delete trans.tbIssueReturnDetails where vIssueReturnId = '"+returnId+"'";

		int ar = 0;
		if (ar == 0)
		{
			sql += " insert into trans.tbIssueReturnDetails(vIssueReturnId, vIssueId, vItemId, vUnitId, vRemarks,"+
					" mIssueQty, mReturnedQty, mMainRate, mCostMargin, mIssueRate, mAmount, iActive) values";
		}
		for (int i=0; i < tbLblItemDetails.size(); i++)
		{
			if (!tbLblItemId.get(ar).getValue().toString().isEmpty() && cm.getAmtValue(tbAmtTotalAmount.get(ar))>0)
			{
				sql += "('"+returnId+"', '"+issueId+"',"+
						" '"+tbLblItemId.get(ar).getValue().toString()+"',"+
						" '"+tbLblUnitId.get(ar).getValue().toString()+"',"+
						" '"+tbTxtDescription.get(ar).getValue().toString()+"',"+
						" '"+cm.getAmtValue(tbAmtIssuedQty.get(ar))+"',"+ 
						" '"+cm.getAmtValue(tbAmtReturnedQty.get(ar))+"',"+
						" '"+cm.getAmtValue(tbAmtMainRate.get(ar))+"',"+
						" '"+cm.getAmtValue(tbAmtCostMargin.get(ar))+"',"+
						" '"+cm.getAmtValue(tbAmtIssueRate.get(ar))+"',"+
						" '"+cm.getAmtValue(tbAmtTotalAmount.get(ar))+"', 1),";
				ar++;
			}
		}
		return sql.substring(0, sql.length()-1)+" exec trans.prcRawInventoryProcess '"+returnId+"', 'Return'";
	}

	private void setEditData()
	{
		tableClear();
		int ar = 0;
		String sql = "select iri.vIssueReturnNo, iri.dReturnDate, isu.dIssueDate, iri.vIssueId, ird.vItemId,"+
				" ird.mReturnedQty from trans.tbIssueReturnInfo iri, trans.tbIssueReturnDetails ird, trans.tbIssueInfo"+
				" isu where iri.vIssueReturnId = ird.vIssueReturnId and iri.vIssueId = isu.vIssueId and"+
				" iri.vIssueReturnId = '"+returnId+"' order by ird.iAutoId";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			if (ar == 0)
			{
				txtReturnNo.setReadOnly(false);
				txtReturnNo.setValue(element[0].toString());
				txtReturnNo.setReadOnly(true);
				txtReturnDate.setValue((Date) element[1]);
				cmbIssueDetails.setValue(element[3].toString());
			}
			setTableEditQty(element[4].toString(), Double.parseDouble(element[5].toString()));
			ar++;
		}
		action = true;
	}

	private void setTableEditQty(String itemIdEdit, double qty)
	{
		double stock = 0;
		for (int ar = 0; ar < tbLblItemId.size(); ar++)
		{
			String itemId = tbLblItemId.get(ar).getValue().toString();
			if (itemId.equals(itemIdEdit))
			{
				stock = cm.getAmtValue(tbAmtStockQty.get(ar));
				tbAmtStockQty.get(ar).setReadOnly(false);
				tbAmtStockQty.get(ar).setValue(stock - qty);
				tbAmtStockQty.get(ar).setReadOnly(true);
				tbAmtReturnedQty.get(ar).setValue(qty);
				break;
			}
		}
	}

	private void focusEnter()
	{
		allComp.add(txtReturnDate);
		allComp.add(cmbIssueDetails);
		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		txtReturnNo.setReadOnly(false);
		txtReturnNo.setValue(irg.getIssueReturnNo(cm.dfDb.format(txtReturnDate.getValue())));
		txtReturnNo.setReadOnly(true);
		loadIssueNo();
		tableClear();
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(9, 4);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		CssLayout group = new CssLayout();
		group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		txtReturnNo = new TextField();
		txtReturnNo.setImmediate(true);
		txtReturnNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReturnNo.setWidth("140px");
		txtReturnNo.setInputPrompt("Return No");
		txtReturnNo.setDescription("Return No");
		txtReturnNo.setRequired(true);
		txtReturnNo.setRequiredError("This field is required.");

		txtReturnDate  = new PopupDateField();
		txtReturnDate.setImmediate(true);
		txtReturnDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtReturnDate.setValue(new Date());
		txtReturnDate.setWidth("110px");
		txtReturnDate.setDateFormat("dd-MM-yyyy");
		txtReturnDate.setDescription("Return Date");
		txtReturnDate.setRequired(true);
		txtReturnDate.setRequiredError("This field is required");
		Label lblpd = new Label("Return Details: ");
		lblpd.setWidth("-1px");
		grid.addComponent(lblpd, 0, 0);

		group.addComponents(txtReturnNo, txtReturnDate);
		grid.addComponent(group, 1, 0, 2, 0);

		CssLayout groupIssue = new CssLayout();
		groupIssue.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		txtIssueDate = new PopupDateField();
		txtIssueDate.setImmediate(true);
		txtIssueDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtIssueDate.setValue(new Date()); 
		txtIssueDate.setWidth("110px");
		txtIssueDate.setDateFormat("dd-MM-yyyy");
		txtIssueDate.setDescription("Issue Date For Search");

		cmbIssueDetails = new ComboBox();
		cmbIssueDetails.setInputPrompt("Select Issue No");
		cmbIssueDetails.setWidth("300px");
		cmbIssueDetails.setDescription("Issue No - Issued From");
		cmbIssueDetails.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbIssueDetails.setRequired(true);
		cmbIssueDetails.setRequiredError("This field is required");
		cmbIssueDetails.setFilteringMode(FilteringMode.CONTAINS);
		Label lbls = new Label("Issue Details: ");
		lbls.setWidth("-1px");
		grid.addComponent(lbls, 3, 0);

		groupIssue.addComponents(cmbIssueDetails, txtIssueDate);
		grid.addComponent(groupIssue, 4, 0, 5, 0);

		grid.addComponent(buildTable(), 0, 1, 8, 1);

		grid.addComponent(cBtn, 0, 2, 8, 2);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return grid;
	}

	private Table buildTable()
	{
		tblReturnDetailsList = new Table();
		tblReturnDetailsList.setSelectable(true);
		tblReturnDetailsList.setFooterVisible(true);
		tblReturnDetailsList.setColumnCollapsingAllowed(true);
		tblReturnDetailsList.addStyleName(ValoTheme.TABLE_SMALL);
		tblReturnDetailsList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblReturnDetailsList.setPageLength(10);
		tblReturnDetailsList.setWidth("100%");

		tblReturnDetailsList.addContainerProperty("Item Id", Label.class, new Label(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnCollapsed("Item Id", true);

		tblReturnDetailsList.addContainerProperty("Item Name", Label.class, new Label(), null, null, Align.CENTER);

		tblReturnDetailsList.addContainerProperty("Unit Id", Label.class, new Label(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnCollapsed("Unit Id", true);

		tblReturnDetailsList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);

		tblReturnDetailsList.addContainerProperty("Category Id", Label.class, new Label(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnCollapsed("Category Id", true);

		tblReturnDetailsList.addContainerProperty("Category", Label.class, new Label(), null, null, Align.CENTER);

		tblReturnDetailsList.addContainerProperty("Description", TextField.class, new TextField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnWidth("Description", 120);
		tblReturnDetailsList.setColumnCollapsed("Description", true);

		tblReturnDetailsList.addContainerProperty("Stk. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnWidth("Stk. Qty", 80);

		tblReturnDetailsList.addContainerProperty("Issue Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnWidth("Issue Qty", 80);

		tblReturnDetailsList.addContainerProperty("Return Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnWidth("Return Qty", 80);

		tblReturnDetailsList.addContainerProperty("Main Rate", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnWidth("Main Rate", 80);

		tblReturnDetailsList.addContainerProperty("C. Margin (%)", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblReturnDetailsList.setColumnWidth("C. Margin (%)", 100);

		tblReturnDetailsList.addContainerProperty("Issue Rate", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblReturnDetailsList.setColumnWidth("Issue Rate", 100);

		tblReturnDetailsList.addContainerProperty("Total Amount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblReturnDetailsList.setColumnWidth("Total Amount", 120);

		return tblReturnDetailsList;
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblItemId.add(ar, new Label());
			tbLblItemId.get(ar).setWidth("100%");
			tbLblItemId.get(ar).setImmediate(true);
			tbLblItemId.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblItemDetails.add(ar, new Label());
			tbLblItemDetails.get(ar).setWidth("100%");
			tbLblItemDetails.get(ar).setImmediate(true);
			tbLblItemDetails.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblUnitId.add(ar, new Label());
			tbLblUnitId.get(ar).setWidth("100%");
			tbLblUnitId.get(ar).setImmediate(true);
			tbLblUnitId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblUnitName.add(ar, new Label());
			tbLblUnitName.get(ar).setWidth("100%");
			tbLblUnitName.get(ar).setImmediate(true);
			tbLblUnitName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbLblCatId.add(ar, new Label());
			tbLblCatId.get(ar).setWidth("100%");
			tbLblCatId.get(ar).setImmediate(true);
			tbLblCatId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblCatName.add(ar, new Label());
			tbLblCatName.get(ar).setWidth("100%");
			tbLblCatName.get(ar).setImmediate(true);
			tbLblCatName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

			tbTxtDescription.add(ar, new TextField());
			tbTxtDescription.get(ar).setWidth("100%");
			tbTxtDescription.get(ar).setImmediate(true);
			tbTxtDescription.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);

			tbAmtStockQty.add(ar, new CommaField());
			tbAmtStockQty.get(ar).setWidth("100%");
			tbAmtStockQty.get(ar).setImmediate(true);
			tbAmtStockQty.get(ar).setReadOnly(true);
			tbAmtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtStockQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtStockQty.get(ar).setDescription("Current Qty");

			tbAmtIssuedQty.add(ar, new CommaField());
			tbAmtIssuedQty.get(ar).setWidth("100%");
			tbAmtIssuedQty.get(ar).setImmediate(true);
			tbAmtIssuedQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtIssuedQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtIssuedQty.get(ar).setEnabled(false);
			tbAmtIssuedQty.get(ar).setDescription("Issue Qty");

			tbAmtReturnedQty.add(ar, new CommaField());
			tbAmtReturnedQty.get(ar).setWidth("100%");
			tbAmtReturnedQty.get(ar).setImmediate(true);
			tbAmtReturnedQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtReturnedQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtReturnedQty.get(ar).setDescription("Return Qty");
			tbAmtReturnedQty.get(ar).setRequired(true);
			tbAmtReturnedQty.get(ar).setRequiredError("This field is required.");
			tbAmtReturnedQty.get(ar).addValueChangeListener(event ->
			{ issueQtyValidation(ar); });

			tbAmtMainRate.add(ar, new CommaField());
			tbAmtMainRate.get(ar).setWidth("100%");
			tbAmtMainRate.get(ar).setImmediate(true);
			tbAmtMainRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtMainRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtMainRate.get(ar).setEnabled(false);

			tbAmtCostMargin.add(ar, new CommaField());
			tbAmtCostMargin.get(ar).setWidth("100%");
			tbAmtCostMargin.get(ar).setImmediate(true);
			tbAmtCostMargin.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtCostMargin.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtCostMargin.get(ar).setEnabled(false);
			tbAmtCostMargin.get(ar).setDescription("Cost Margin");

			tbAmtIssueRate.add(ar, new CommaField());
			tbAmtIssueRate.get(ar).setWidth("100%");
			tbAmtIssueRate.get(ar).setImmediate(true);
			tbAmtIssueRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtIssueRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtIssueRate.get(ar).setEnabled(false);

			tbAmtTotalAmount.add(ar, new CommaField());
			tbAmtTotalAmount.get(ar).setWidth("100%");
			tbAmtTotalAmount.get(ar).setImmediate(true);
			tbAmtTotalAmount.get(ar).setReadOnly(true);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tblReturnDetailsList.addItem(new Object[]{tbLblItemId.get(ar), tbLblItemDetails.get(ar), tbLblUnitId.get(ar),
					tbLblUnitName.get(ar), tbLblCatId.get(ar), tbLblCatName.get(ar), tbTxtDescription.get(ar),
					tbAmtStockQty.get(ar), tbAmtIssuedQty.get(ar), tbAmtReturnedQty.get(ar), tbAmtMainRate.get(ar),
					tbAmtCostMargin.get(ar), tbAmtIssueRate.get(ar), tbAmtTotalAmount.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private void issueQtyValidation(int ar)
	{
		if (cm.getAmtValue(tbAmtReturnedQty.get(ar)) <= cm.getAmtValue(tbAmtIssuedQty.get(ar)))
		{ calculateVatData(ar); }
		else
		{
			tbAmtReturnedQty.get(ar).setValue(cm.getAmtValue(tbAmtIssuedQty.get(ar)));
			cm.showNotification("warning", "Warning!", "Issued quantity exceeded.");
			tbAmtReturnedQty.get(ar).focus();
		}
		totalAmount("Total");
	}

	private void calculateVatData(int ar)
	{
		double totalQty = cm.getAmtValue(tbAmtReturnedQty.get(ar));
		double rate = cm.getAmtValue(tbAmtMainRate.get(ar));
		double totalAmountCal = cm.getRound(totalQty * rate);

		tbAmtTotalAmount.get(ar).setReadOnly(false);
		tbAmtTotalAmount.get(ar).setValue(totalAmountCal);
		tbAmtTotalAmount.get(ar).setReadOnly(true);
		addChanges();
	}

	private void addChanges()
	{
		if (flag.equals("Edit") && action)
		{ changes = true; }
	}

	private void tableClear()
	{
		tblReturnDetailsList.removeAllItems();
		tbLblItemDetails.clear();
		totalAmount("");
	}

	private double totalAmount(String flag)
	{
		double ret = 0, totalamt = 0;
		for (int i=0; i<tbLblItemDetails.size(); i++)
		{
			if (!tbLblItemId.get(i).getValue().toString().isEmpty() && cm.getAmtValue(tbAmtTotalAmount.get(i))>0)
			{ totalamt += cm.getAmtValue(tbAmtTotalAmount.get(i)); }
		}
		tblReturnDetailsList.setColumnFooter("Total Amount", cm.setComma(totalamt));
		tblReturnDetailsList.setColumnAlignment("Total Amount", Align.RIGHT);

		if (flag.equals("Total")) { ret = totalamt; }
		return ret;
	}
}
