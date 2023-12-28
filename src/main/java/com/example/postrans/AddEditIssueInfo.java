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
import com.example.gateway.IssueInfoGateway;
import com.example.model.IssueInfoModel;
import com.example.possetup.AddEditRawItemInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditIssueInfo extends Window
{
	private SessionBean sessionBean;
	private String flag, issueId;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private IssueInfoGateway iig = new IssueInfoGateway();

	private TextField txtIssueNo, txtRemarks, txtReferenceNo, txtReceivedBy;
	private PopupDateField txtIssueDate;
	private ComboBox cmbRequisition;
	private CheckBox chkReceived;
	private CheckBox chkCostMargin;

	private Table tblIssueDetailsList;
	private ArrayList<ComboBox> tbCmbItemName = new ArrayList<ComboBox>();
	private ArrayList<Label> tbLblUnitName = new ArrayList<Label>();
	private ArrayList<Label> tbLblCatName = new ArrayList<Label>();
	private ArrayList<TextField> tbTxtDescription = new ArrayList<TextField>();
	private ArrayList<CommaField> tbAmtReqQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtPendingQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtStockQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtIssueQty = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtCostMargin = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtMainRate = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtIssueRate = new ArrayList<CommaField>();
	private ArrayList<CommaField> tbAmtTotalAmount = new ArrayList<CommaField>();
	private ArrayList<Label> tbLblUnitId  = new ArrayList<Label>();
	private ArrayList<Label> tbLblCatId  = new ArrayList<Label>();
	private ArrayList<Button> tbBtnAddItem  = new ArrayList<Button>();
	private ArrayList<Button> tbBtnRemove  = new ArrayList<Button>();
	private ArrayList<Component> allComp = new ArrayList<Component>();

	private HashMap<String, Double> hmCostMargin = new HashMap<String, Double>();
	private HashMap<String, String> hmReqBranchId = new HashMap<String, String>();

	private CommonMethod cm;
	private boolean changes = false, action = false;

	public AddEditIssueInfo(SessionBean sessionBean, String flag, String issueId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.issueId = issueId;
		this.setCaption(flag+" Issue Info :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);
		setWidth("100%");
		setHeight("590px");

		setContent(buildLayout());
		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
		{ masterValidation(); });

		cBtn.btnExit.addClickListener(event ->
		{ close(); });

		txtIssueDate.addValueChangeListener(event ->
		{ loadIssueNo(); });

		cmbRequisition.addValueChangeListener(event ->
		{ setRequisitionDetails(); });

		chkCostMargin.addValueChangeListener(event ->
		{ calculateCostMargin(); });

		loadIssueNo();
		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void loadIssueNo()
	{
		if (!flag.equals("Edit"))
		{
			txtIssueNo.setReadOnly(false);
			txtIssueNo.setValue(iig.getIssueNo(cm.dfDb.format(txtIssueDate.getValue())));
			txtIssueNo.setReadOnly(true);
		}
		loadRequisition();
	}

	private void loadRequisition()
	{
		String caption = "";
		String sql = "select ri.vRequisitionId, ri.vRequisitionNo, ri.dRequisitionDate, ri.vBranchId, bm.vBranchName from"+
				" trans.tbRequisitionInfo ri, master.tbBranchMaster bm where ri.vBranchId = bm.vBranchId and ri.iActive = 1"+
				" and ri.vStatusId = 'S6' and ri.vReqBranchId = '"+sessionBean.getBranchId()+"' and (select (select isnull"+
				"(sum(mQuantity),0) from trans.tbRequisitionDetails where vRequisitionId = ri.vRequisitionId) - isnull(sum"+
				" (a.mIssueQty),0) from trans.tbIssueDetails a where a.vRequisitionId = ri.vRequisitionId and a.vIssueId !="+
				" '"+issueId+"') > 0 order by dRequisitionDate desc";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			caption = element[4].toString()+" ("+element[1].toString()+"-"+cm.dfBd.format(element[2])+")";
			cmbRequisition.addItem(element[0].toString());
			cmbRequisition.setItemCaption(element[0].toString(), caption);
			hmReqBranchId.put(element[0].toString(), element[3].toString());
		}
	}

	private void calculateCostMargin()
	{
		try
		{
			for (int ar=0; ar<tbCmbItemName.size(); ar++)
			{
				if (tbCmbItemName.get(ar).getValue() != null)
				{ 
					double costMargin = hmCostMargin.get(tbCmbItemName.get(ar).getValue().toString());
					tbAmtCostMargin.get(ar).setReadOnly(false);
					tbAmtCostMargin.get(ar).setValue(chkCostMargin.getValue().booleanValue()?costMargin:0);
					tbAmtCostMargin.get(ar).setReadOnly(true);
					calculateVatData(ar);
				}
			}
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	public void setRequisitionDetails()
	{
		String reqId = cmbRequisition.getValue() != null? cmbRequisition.getValue().toString():"";
		if (!reqId.isEmpty())
		{
			tblIssueDetailsList.setColumnCollapsed("Pen. Qty.", false);
			tblIssueDetailsList.setColumnCollapsed("Req. Qty", false);
		}
		else
		{
			tblIssueDetailsList.setColumnCollapsed("Pen. Qty.", true);
			tblIssueDetailsList.setColumnCollapsed("Req. Qty", true);
		}
		tableClear();
		String sql = "select vItemId, mQuantity, mBalance from (select vItemId, mQuantity, (select (select isnull"+
				" (sum(mQuantity), 0) from trans.tbRequisitionDetails re where vRequisitionId = '"+reqId+"' and"+
				" re.vItemId = req.vItemId) - isnull(sum(isd.mIssueQty), 0) from trans.tbIssueDetails isd where"+
				" isd.vRequisitionId = '"+reqId+"' and isd.vIssueId != '"+issueId+"' and isd.vItemId = req.vItemId)"+
				" mBalance from trans.tbRequisitionDetails req where vRequisitionId = '"+reqId+"') as tbTemp"+
				" where mBalance > 0";
		int ar = 0;
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (tbCmbItemName.size() <= ar)
			{ tableRowAdd(tbCmbItemName.size()); }

			tbCmbItemName.get(ar).setValue(element[0].toString());
			tbAmtReqQty.get(ar).setReadOnly(false);
			tbAmtReqQty.get(ar).setValue(Double.parseDouble(element[1].toString()));
			tbAmtReqQty.get(ar).setReadOnly(true);

			tbAmtPendingQty.get(ar).setReadOnly(false);
			tbAmtPendingQty.get(ar).setValue(Double.parseDouble(element[2].toString()));
			tbAmtPendingQty.get(ar).setReadOnly(true);
			ar++;
		}
		action = true;
	}

	public void txtEnableDisableTable(boolean t)
	{
		for (int i=0; i<tbCmbItemName.size(); i++)
		{
			if (tbCmbItemName.get(i).getValue() != null)
			{ tbCmbItemName.get(i).setEnabled(t); }
		}
	}

	private void addEditItem(int ar)
	{
		String itemId = tbCmbItemName.get(ar).getValue() != null? tbCmbItemName.get(ar).getValue().toString():"";
		String addEdit = itemId.isEmpty()? "Add":"Edit";

		AddEditRawItemInfo win = new AddEditRawItemInfo(sessionBean, addEdit, itemId);
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event ->
		{ 
			for (int i=0; i<tbCmbItemName.size(); i++)
			{ loadtableComboData(ar); } 
		});
	}

	private void removeRow(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null && tbCmbItemName.size() > 0 &&
				!tbAmtTotalAmount.get(ar).getValue().toString().isEmpty())
		{
			tbCmbItemName.get(ar).setValue(null);
			tblIssueDetailsList.removeItem(ar);
			tbCmbItemName.get(ar).clear();
			tbAmtTotalAmount.get(ar).setReadOnly(false);
			tbAmtTotalAmount.get(ar).clear();
		}
		totalAmount("Total");
	}

	private void masterValidation()
	{
		if (!txtReceivedBy.getValue().toString().trim().isEmpty())
		{
			if (!txtIssueNo.getValue().toString().trim().isEmpty())
			{
				if (txtIssueDate.getValue() != null)
				{
					if (totalAmount("Total") > 0)
					{
						cBtn.btnSave.setEnabled(false);
						insertEditData();
					}
					else
					{
						tbCmbItemName.get(0).focus();
						cm.showNotification("warning", "Warning!", "Provide valid data in table.");
					}
				}
				else
				{
					txtIssueDate.focus();
					cm.showNotification("warning", "Warning!", "Provide Issue date.");
				}
			}
			else
			{
				txtIssueNo.focus();
				cm.showNotification("warning", "Warning!", "Issue number not found.");
			}
		}
		else
		{
			txtReceivedBy.focus();
			cm.showNotification("warning", "Warning!", "Provide Received By.");
		}
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
		IssueInfoModel iim = new IssueInfoModel();
		setValueForSave(iim);
		if (iig.insertEditData(iim, flag))
		{
			txtClear();
			cm.showNotification("success", "Successfull!", "All information saved successfully.");
			cBtn.btnSave.setEnabled(true);

			IssueInformation report = new IssueInformation(sessionBean, "");
			report.viewReportSingle(iim.getIssueId());

			if (flag.equals("Edit"))
			{ close(); }
		}
		else
		{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
	}

	private void setValueForSave(IssueInfoModel iim)
	{
		iim.setBranchId(sessionBean.getBranchId());
		iim.setIssueId(flag.equals("Add")? iig.getIssueId(sessionBean.getBranchId()):issueId);
		iim.setIssueNo(flag.equals("Add")? iig.getIssueNo(cm.dfDb.format(txtIssueDate.getValue())).toString():
			txtIssueNo.getValue().toString());
		iim.setIssueDate(txtIssueDate.getValue());
		iim.setRemarks(txtRemarks.getValue().toString().trim());
		iim.setReferenceNo(txtReferenceNo.getValue().toString());
		iim.setTotalAmount(Double.parseDouble(tblIssueDetailsList.getColumnFooter("Total Amount").replace(",", "")));
		iim.setCreatedBy(sessionBean.getUserId());
		iim.setStatusId(chkReceived.getValue().equals(true)? "1":"0");
		iim.setCostMargin(chkCostMargin.getValue().equals(true)? "1":"0");

		String reqId = cmbRequisition.getValue() == null? "":cmbRequisition.getValue().toString();
		iim.setRequisitionId(reqId);
		iim.setBranchFrom(sessionBean.getBranchId());
		iim.setBranchTo(reqId.isEmpty()? "":hmReqBranchId.get(reqId));

		iim.setReceivedBy(txtReceivedBy.getValue().toString());
		iim.setCancelBy("");
		iim.setCancelReason("");
		iim.setStatusMainId("S5");
		iim.setApproveBy("");
		iim.setDetailsSql(getDetails(iim.getIssueId(), iim.getRequisitionId()));
		iim.setDetailsChange(changes);
	}

	private String getDetails(String issueId, String reqId)
	{
		String branchId = sessionBean.getBranchId();
		String sql = flag.equals("Add")? "" : "delete trans.tbIssueDetails where vIssueId = '"+issueId+"' ";
		for (int i = 0; i < tbCmbItemName.size(); i++)
		{
			if (i == 0)
			{
				sql += "insert into trans.tbIssueDetails(vIssueId, vRequisitionId, vBranchFrom, vBranchTo,"+
						" vItemId, vUnitId, vCategoryId, vRemarks, mStockQty, mIssueQty, mCostMargin,"+
						" mMainRate, mIssueRate, mAmount, iActive) values ";
			}
			if (tbCmbItemName.get(i).getValue() != null && cm.getAmtValue(tbAmtTotalAmount.get(i)) > 0)
			{
				sql += "('"+issueId+"', '"+reqId+"', '"+branchId+"',"+
						" '"+(reqId.isEmpty()? "":hmReqBranchId.get(reqId))+"',"+
						" '"+tbCmbItemName.get(i).getValue().toString()+"',"+
						" '"+tbLblUnitId.get(i).getValue().toString()+"',"+
						" '"+tbLblCatId.get(i).getValue().toString()+"',"+
						" '"+tbTxtDescription.get(i).getValue().toString()+"',"+
						" '"+cm.getAmtValue(tbAmtStockQty.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtIssueQty.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtCostMargin.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtIssueRate.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtMainRate.get(i))+"',"+
						" '"+cm.getAmtValue(tbAmtTotalAmount.get(i))+"', 1),";
			}
		}
		return sql.substring(0, sql.length()-1)+" exec trans.prcRawInventoryProcess '"+issueId+"', 'Issue'";
	}

	private void setEditData()
	{
		IssueInfoModel iim = new IssueInfoModel();
		try
		{
			if (iig.selectEditData(iim, issueId))
			{
				txtIssueDate.setValue((Date) iim.getIssueDate());
				txtRemarks.setValue(iim.getRemarks());
				txtReferenceNo.setValue(iim.getReferenceNo());
				txtIssueNo.setReadOnly(false);
				txtIssueNo.setValue(iim.getIssueNo());
				txtIssueNo.setReadOnly(true);

				chkReceived.setValue(iim.getStatusId().toString().equals("1"));
				chkCostMargin.setValue(iim.getCostMargin().toString().equals("1"));

				cmbRequisition.setValue(iim.getRequisitionId());
				cmbRequisition.setEnabled(iim.getRequisitionId().isEmpty());

				txtReceivedBy.setValue(iim.getReceivedBy());
				setTableValue(issueId, iim.getRequisitionId());
			}
			else
			{ cm.showNotification("failure", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	public void setTableValue(String issueId, String reqId)
	{
		int ar = 0;
		String sql = "select isd.vItemId, isd.vRemarks, isd.vUnitId, uni.vUnitName, isd.vCategoryId, isd.mIssueQty,"+
				" isd.mCostMargin, isd.mMainRate, isd.mIssueRate from trans.tbIssueDetails isd, master.tbUnitInfo uni"+
				" where isd.vUnitId = convert(varchar(10), uni.iUnitId) and vIssueId = '"+issueId+"'";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (tbCmbItemName.size() <= ar)
			{ tableRowAdd(tbCmbItemName.size()); }

			tbCmbItemName.get(ar).setEnabled(reqId.isEmpty());

			tbTxtDescription.get(ar).setValue(element[1].toString());
			tbLblUnitId.get(ar).setValue(element[2].toString());
			tbLblUnitName.get(ar).setValue(element[3].toString());
			tbLblCatId.get(ar).setValue(element[4].toString());

			if (reqId.isEmpty())
			{
				tbCmbItemName.get(ar).setValue(element[0].toString());
				tbAmtIssueQty.get(ar).setValue(Double.parseDouble(element[5].toString()));

				tbAmtCostMargin.get(ar).setReadOnly(false);
				if (chkCostMargin.getValue().equals(false))
				{ tbAmtCostMargin.get(ar).setValue("0"); }
				else
				{ tbAmtCostMargin.get(ar).setValue(Double.parseDouble(element[6].toString())); }
				tbAmtCostMargin.get(ar).setReadOnly(true);

				tbAmtMainRate.get(ar).setReadOnly(false);
				tbAmtMainRate.get(ar).setValue(Double.parseDouble(element[8].toString()));
				tbAmtMainRate.get(ar).setReadOnly(true);

				tbAmtIssueRate.get(ar).setReadOnly(false);
				tbAmtIssueRate.get(ar).setValue(Double.parseDouble(element[7].toString()));
				tbAmtIssueRate.get(ar).setReadOnly(true);
			}
			else
			{
				setTableEditQty(element[0].toString(), Double.parseDouble(element[5].toString()),
						Double.parseDouble(element[6].toString()), Double.parseDouble(element[7].toString()),
						Double.parseDouble(element[8].toString()));
			}

			ar++;
		}
		action = true;
	}

	private void setTableEditQty(String itemIdEdit, double qty, double cost, double mainRate, double issRate)
	{
		for (int ar = 0; ar < tbCmbItemName.size(); ar++)
		{
			String itemId = tbCmbItemName.get(ar).getValue() != null? tbCmbItemName.get(ar).getValue().toString():"";
			if (itemId.equals(itemIdEdit))
			{
				tbAmtIssueQty.get(ar).setValue(qty);

				tbAmtCostMargin.get(ar).setReadOnly(false);
				if (chkCostMargin.getValue().equals(false))
				{ tbAmtCostMargin.get(ar).setValue("0"); }
				else
				{ tbAmtCostMargin.get(ar).setValue(cost); }
				tbAmtCostMargin.get(ar).setReadOnly(true);

				tbAmtMainRate.get(ar).setReadOnly(false);
				tbAmtMainRate.get(ar).setValue(issRate);
				tbAmtMainRate.get(ar).setReadOnly(true);

				tbAmtIssueRate.get(ar).setReadOnly(false);
				tbAmtIssueRate.get(ar).setValue(mainRate);
				tbAmtIssueRate.get(ar).setReadOnly(true);
				break;
			}
		}
	}

	private void setItemDetails(int ar, String ItemId)
	{
		try
		{
			String sql = "select U.vUnitName, C.vCategoryName, I.vUnitId, I.vCategoryId, I.mIssueRate, (select [dbo].[funcStockQty]"+
					"('"+ItemId+"', '"+sessionBean.getBranchId()+"')) mStockQty, I.mCostMargin, I.vItemId from master.tbRawItemInfo I"+
					" inner join master.tbUnitInfo U on I.vUnitId = U.iUnitId inner join master.tbItemCategory C on I.vCategoryId ="+
					" C.vCategoryId where vItemId = '"+ItemId+"'";
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				tbLblUnitName.get(ar).setValue(element[0].toString());
				tbLblCatName.get(ar).setValue(element[1].toString());
				tbLblUnitId.get(ar).setValue(element[2].toString());
				tbLblCatId.get(ar).setValue(element[3].toString());
				tbAmtMainRate.get(ar).setReadOnly(false);
				tbAmtMainRate.get(ar).setValue(Double.parseDouble(element[4].toString()));
				tbAmtMainRate.get(ar).setReadOnly(true);
				tbAmtIssueRate.get(ar).setReadOnly(false);
				tbAmtIssueRate.get(ar).setValue(Double.parseDouble(element[4].toString()));
				tbAmtIssueRate.get(ar).setReadOnly(true);
				tbAmtStockQty.get(ar).setReadOnly(false);
				tbAmtStockQty.get(ar).setValue(Double.parseDouble(element[5].toString()));
				tbAmtStockQty.get(ar).setReadOnly(true);

				tbAmtCostMargin.get(ar).setReadOnly(false);
				if (chkCostMargin.getValue().equals(false))
				{ tbAmtCostMargin.get(ar).setValue(Double.parseDouble("0")); }
				else
				{ tbAmtCostMargin.get(ar).setValue(Double.parseDouble(element[6].toString())); }
				tbAmtCostMargin.get(ar).setReadOnly(true);

				hmCostMargin.put(element[7].toString(), Double.parseDouble(element[6].toString()));
				calculateVatData(ar);
			}
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private void calculateVatData(int ar)
	{
		double costMargin = cm.getAmtValue(tbAmtCostMargin.get(ar));
		double avgRate = cm.getAmtValue(tbAmtIssueRate.get(ar));
		double TotalQty = cm.getAmtValue(tbAmtIssueQty.get(ar));
		double costRate  = 0.0;

		if (costMargin>0)
		{ costRate = cm.getRound(avgRate*(costMargin/100)); }

		double Issuerate = cm.getRound(avgRate + costRate);
		double TotalAmountCal = cm.getRound(TotalQty * Issuerate);

		tbAmtMainRate.get(ar).setReadOnly(false);
		tbAmtMainRate.get(ar).setValue(Issuerate);
		tbAmtMainRate.get(ar).setReadOnly(true);

		tbAmtTotalAmount.get(ar).setReadOnly(false);
		tbAmtTotalAmount.get(ar).setValue(TotalAmountCal);
		tbAmtTotalAmount.get(ar).setReadOnly(true);
		totalAmount("Total");
		addChanges();
	}

	private void addChanges()
	{
		if (flag.equals("Edit") && action)
		{ changes = true; }
	}

	public void tableClear()
	{
		tblIssueDetailsList.removeAllItems();
		tbCmbItemName.clear();
		tableRowAdd(0);
	}

	private void focusEnter()
	{
		allComp.add(txtIssueDate);
		allComp.add(cmbRequisition);
		allComp.add(txtReceivedBy);
		allComp.add(chkReceived);
		allComp.add(chkCostMargin);
		allComp.add(txtReferenceNo);
		allComp.add(txtRemarks);
		allComp.add(tbCmbItemName.get(0));

		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}

	public void txtClear()
	{
		txtIssueNo.setReadOnly(false);
		txtIssueNo.setValue(iig.getIssueNo(cm.dfDb.format(txtIssueDate.getValue())));
		txtIssueNo.setReadOnly(true);

		cmbRequisition.setValue(null);
		txtReceivedBy.setValue("");
		chkReceived.setValue(false);
		chkCostMargin.setValue(false);
		txtReferenceNo.setValue("");
		txtRemarks.setValue("");
		txtIssueDate.setValue(new Date());
		tableClear();
		totalAmount("");
		loadRequisition();
	}

	//Master Component Set
	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(11, 4);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		CssLayout group = new CssLayout();
		group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		txtIssueNo = new TextField();
		txtIssueNo.setImmediate(true);
		txtIssueNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtIssueNo.setWidth("150px");
		txtIssueNo.setInputPrompt("Issue No");
		txtIssueNo.setDescription("Issue No");
		txtIssueNo.setRequired(true);
		txtIssueNo.setRequiredError("This field is required.");
		grid.addComponent(new Label("Issue Details: "), 0, 0);

		txtIssueDate = new PopupDateField();
		txtIssueDate.setImmediate(true);
		txtIssueDate.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtIssueDate.setValue(new Date());
		txtIssueDate.setWidth("110px");
		txtIssueDate.setDateFormat("dd-MM-yyyy");
		txtIssueDate.setDescription("Issue Date");
		txtIssueDate.setRequired(true);
		txtIssueDate.setRequiredError("This field is required");

		group.addComponents(txtIssueNo, txtIssueDate);
		grid.addComponent(group, 1, 0);

		cmbRequisition = new ComboBox();
		cmbRequisition.setImmediate(true);
		cmbRequisition.setWidth("300px");
		cmbRequisition.setInputPrompt("Select Requisition No");
		cmbRequisition.setDescription("Select Requisition No");
		cmbRequisition.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbRequisition.setFilteringMode(FilteringMode.CONTAINS);
		Label lbl1 = new Label("Requisition No: ");
		lbl1.setWidth("-1px");
		grid.addComponent(lbl1, 0, 1);
		grid.addComponent(cmbRequisition, 1, 1);

		txtReceivedBy = new TextField();
		txtReceivedBy.setImmediate(true);
		txtReceivedBy.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReceivedBy.setWidth("200px");
		txtReceivedBy.setRequired(true);
		txtReceivedBy.setRequiredError("This field is required.");
		txtReceivedBy.setInputPrompt("Received By");
		Label lbl2 = new Label("Received By: ");
		lbl2.setWidth("-1px");
		grid.addComponent(lbl2, 2, 0);
		grid.addComponent(txtReceivedBy, 3, 0);

		HorizontalLayout hori = new HorizontalLayout();
		hori.setSpacing(true);

		chkReceived = new CheckBox("Received");
		chkReceived.setImmediate(true);
		chkReceived.setDescription("Select to receive automatically from branch side");
		chkReceived.addStyleName(ValoTheme.COMBOBOX_ALIGN_CENTER);

		chkCostMargin = new CheckBox("Cost Margin");
		chkCostMargin.setImmediate(true);
		chkCostMargin.setDescription("Apply cost margin");
		chkCostMargin.addStyleName(ValoTheme.COMBOBOX_ALIGN_CENTER);

		hori.addComponents(chkReceived, chkCostMargin);
		grid.addComponent(new Label("Options: "), 2, 1);
		grid.addComponent(hori, 3, 1);

		txtReferenceNo = new TextField();
		txtReferenceNo.setImmediate(true);
		txtReferenceNo.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReferenceNo.setWidth("100%");
		txtReferenceNo.setInputPrompt("Reference No");
		Label lbl4 = new Label("Reference No: ");
		lbl4.setWidth("-1px");
		grid.addComponent(lbl4, 4, 0);
		grid.addComponent(txtReferenceNo, 5, 0, 10, 0);

		txtRemarks = new TextField();
		txtRemarks.setImmediate(true);
		txtRemarks.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRemarks.setWidth("100%");
		txtRemarks.setInputPrompt("Remarks");
		grid.addComponent(new Label("Remarks: "), 4, 1);
		grid.addComponent(txtRemarks, 5, 1, 10, 1);

		grid.addComponent(buildTable(), 0, 2, 10, 2);

		grid.addComponent(cBtn, 0, 3, 10, 3);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return grid;
	}

	//Table Component Set
	private Table buildTable()
	{
		tblIssueDetailsList = new Table();
		tblIssueDetailsList.setSelectable(true);
		tblIssueDetailsList.setFooterVisible(true);
		tblIssueDetailsList.setColumnCollapsingAllowed(true);
		tblIssueDetailsList.addStyleName(ValoTheme.TABLE_SMALL);
		tblIssueDetailsList.setRowHeaderMode(RowHeaderMode.INDEX);
		tblIssueDetailsList.setPageLength(9);
		tblIssueDetailsList.setWidth("100%");

		tblIssueDetailsList.addContainerProperty("Add", Button.class, new Button(), null, null, Align.CENTER);
		tblIssueDetailsList.setColumnWidth("Add", 50);

		tblIssueDetailsList.addContainerProperty("Item Name", ComboBox.class, new ComboBox(), null, null, Align.CENTER);

		tblIssueDetailsList.addContainerProperty("Unit", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueDetailsList.addContainerProperty("Category", Label.class, new Label(), null, null, Align.CENTER);

		tblIssueDetailsList.addContainerProperty("Description", TextField.class, new TextField(), null, null, Align.CENTER);
		tblIssueDetailsList.setColumnCollapsed("Description", true);

		tblIssueDetailsList.addContainerProperty("Cur. Stk", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblIssueDetailsList.setColumnWidth("Cur. Stk", 70);

		tblIssueDetailsList.addContainerProperty("Req. Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblIssueDetailsList.setColumnWidth("Req. Qty", 70);
		tblIssueDetailsList.setColumnCollapsed("Req. Qty", true);

		tblIssueDetailsList.addContainerProperty("Pen. Qty.", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblIssueDetailsList.setColumnWidth("Pen. Qty.", 70);
		tblIssueDetailsList.setColumnCollapsed("Pen. Qty.", true);

		tblIssueDetailsList.addContainerProperty("Issued Qty", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblIssueDetailsList.setColumnWidth("Issued Qty", 70);

		tblIssueDetailsList.addContainerProperty("C. Margin (%)", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblIssueDetailsList.setColumnWidth("C. Margin (%)", 80);

		tblIssueDetailsList.addContainerProperty("Main Rate", CommaField.class, new CommaField(), null, null, Align.CENTER);
		tblIssueDetailsList.setColumnWidth("Main Rate", 80);

		tblIssueDetailsList.addContainerProperty("Issue Rate", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblIssueDetailsList.setColumnWidth("Issue Rate", 80);

		tblIssueDetailsList.addContainerProperty("Total Amount", CommaField.class, new CommaField(), null, null, Align.RIGHT);
		tblIssueDetailsList.setColumnWidth("Total Amount", 100);

		tblIssueDetailsList.addContainerProperty("Unit Id", Label.class, new Label(), null, null, Align.CENTER);
		tblIssueDetailsList.setColumnCollapsed("Unit Id", true);

		tblIssueDetailsList.addContainerProperty("Category Id", Label.class, new Label(), null, null, Align.CENTER);
		tblIssueDetailsList.setColumnCollapsed("Category Id", true);

		tblIssueDetailsList.addContainerProperty("Rem", Button.class, new Button(), null, null, Align.CENTER);
		tblIssueDetailsList.setColumnWidth("Rem", 50);

		tableRowAdd(0);
		return tblIssueDetailsList;
	}

	public void tableRowAdd(int ar)
	{
		try
		{
			tbBtnAddItem.add(ar, new Button(""));
			tbBtnAddItem.get(ar).setWidth("100%");
			tbBtnAddItem.get(ar).setImmediate(true);
			tbBtnAddItem.get(ar).setIcon(FontAwesome.PLUS);
			tbBtnAddItem.get(ar).setStyleName(ValoTheme.BUTTON_TINY);
			tbBtnAddItem.get(ar).setDescription("Add Item");
			cm.setAuthorize(sessionBean.getUserId(), "rawItem");
			tbBtnAddItem.get(ar).setEnabled(cm.insert);
			tbBtnAddItem.get(ar).addClickListener(new ClickListener()
			{
				public void buttonClick(ClickEvent event)
				{ addEditItem(ar); }
			});

			tbCmbItemName.add(ar, new ComboBox());
			tbCmbItemName.get(ar).setWidth("100%");
			tbCmbItemName.get(ar).setImmediate(true);
			tbCmbItemName.get(ar).setStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbItemName.get(ar).setFilteringMode(FilteringMode.CONTAINS);	
			tbCmbItemName.get(ar).setRequired(true);
			tbCmbItemName.get(ar).setRequiredError("This field is required");
			tbCmbItemName.get(ar).setInputPrompt("Select Item Name.");
			tbCmbItemName.get(ar).setEnabled(cmbRequisition.getValue()==null);
			loadtableComboData(ar);
			tbCmbItemName.get(ar).addValueChangeListener(event ->
			{ itemValidation(ar); });

			tbLblUnitName.add(ar, new Label());
			tbLblUnitName.get(ar).setWidth("100%");
			tbLblUnitName.get(ar).setImmediate(true);
			tbLblUnitName.get(ar).setStyleName(ValoTheme.LABEL_TINY);

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

			tbAmtReqQty.add(ar, new CommaField());
			tbAmtReqQty.get(ar).setWidth("100%");
			tbAmtReqQty.get(ar).setImmediate(true);
			tbAmtReqQty.get(ar).setReadOnly(true);
			tbAmtReqQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtReqQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtReqQty.get(ar).setDescription("Requisition Qty");

			tbAmtPendingQty.add(ar, new CommaField());
			tbAmtPendingQty.get(ar).setWidth("100%");
			tbAmtPendingQty.get(ar).setImmediate(true);
			tbAmtPendingQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtPendingQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtPendingQty.get(ar).setDescription("Pending Qty");
			tbAmtPendingQty.get(ar).setInputPrompt("Pending Qty");
			tbAmtPendingQty.get(ar).setReadOnly(true);

			tbAmtIssueQty.add(ar, new CommaField());
			tbAmtIssueQty.get(ar).setWidth("100%");
			tbAmtIssueQty.get(ar).setImmediate(true);
			tbAmtIssueQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtIssueQty.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
			tbAmtIssueQty.get(ar).setRequired(true);
			tbAmtIssueQty.get(ar).setRequiredError("This field is required.");
			tbAmtIssueQty.get(ar).setInputPrompt("Issued Qty");
			tbAmtIssueQty.get(ar).setDescription("Issued Qty");
			tbAmtIssueQty.get(ar).addValueChangeListener(event ->
			{
				if (checkQty(ar))
				{
					calculateVatData(ar);
					if (cmbRequisition.getValue() == null)
					{
						if ((ar+1) == tbCmbItemName.size())
						{ tableRowAdd(tbCmbItemName.size()); }
						tbCmbItemName.get(ar+1).focus();
					}
					else
					{
						if ((ar+1) < tbCmbItemName.size())
						{ tbAmtIssueQty.get(ar+1).focus(); }
					}
				}
			});

			tbAmtCostMargin.add(ar, new CommaField());
			tbAmtCostMargin.get(ar).setWidth("100%");
			tbAmtCostMargin.get(ar).setImmediate(true);
			tbAmtCostMargin.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtCostMargin.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtCostMargin.get(ar).setReadOnly(true);
			tbAmtCostMargin.get(ar).setDescription("Cost Margin (%)");

			tbAmtIssueRate.add(ar, new CommaField());
			tbAmtIssueRate.get(ar).setWidth("100%");
			tbAmtIssueRate.get(ar).setImmediate(true);
			tbAmtIssueRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtIssueRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtIssueRate.get(ar).setReadOnly(true);

			tbAmtMainRate.add(ar, new CommaField());
			tbAmtMainRate.get(ar).setWidth("100%");
			tbAmtMainRate.get(ar).setImmediate(true);
			tbAmtMainRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtMainRate.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
			tbAmtMainRate.get(ar).setReadOnly(true);

			tbAmtTotalAmount.add(ar, new CommaField());
			tbAmtTotalAmount.get(ar).setWidth("100%");
			tbAmtTotalAmount.get(ar).setImmediate(true);
			tbAmtTotalAmount.get(ar).setReadOnly(true);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbAmtTotalAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbLblUnitId.add(ar, new Label());
			tbLblUnitId.get(ar).setWidth("100%");
			tbLblUnitId.get(ar).setImmediate(true);
			tbLblUnitId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblCatId.add(ar, new Label());
			tbLblCatId.get(ar).setWidth("100%");
			tbLblCatId.get(ar).setImmediate(true);
			tbLblCatId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbBtnRemove.add(ar, new Button(""));
			tbBtnRemove.get(ar).setWidth("100%");
			tbBtnRemove.get(ar).setImmediate(true);
			tbBtnRemove.get(ar).setIcon(FontAwesome.REMOVE);
			tbBtnRemove.get(ar).setStyleName(ValoTheme.BUTTON_TINY);
			tbBtnRemove.get(ar).addStyleName(ValoTheme.BUTTON_DANGER);
			tbBtnRemove.get(ar).setDescription("Remove");
			tbBtnRemove.get(ar).addClickListener(event ->
			{ removeRow(ar); });

			tblIssueDetailsList.addItem(new Object[]{tbBtnAddItem.get(ar), tbCmbItemName.get(ar), tbLblUnitName.get(ar),
					tbLblCatName.get(ar), tbTxtDescription.get(ar), tbAmtStockQty.get(ar), tbAmtReqQty.get(ar),
					tbAmtPendingQty.get(ar), tbAmtIssueQty.get(ar), tbAmtCostMargin.get(ar), tbAmtIssueRate.get(ar),
					tbAmtMainRate.get(ar), tbAmtTotalAmount.get(ar), tbLblUnitId.get(ar), tbLblCatId.get(ar),
					tbBtnRemove.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private boolean checkQty(int ar)
	{
		boolean ret = true;
		if (cmbRequisition.getValue() != null)
		{
			if (cm.getAmtValue(tbAmtIssueQty.get(ar)) > cm.getAmtValue(tbAmtPendingQty.get(ar)))
			{
				ret = false;
				tbAmtIssueQty.get(ar).setValue(cm.getAmtValue(tbAmtPendingQty.get(ar)));
				tbAmtIssueQty.get(ar).focus();
				cm.showNotification("warning", "Warning!", "Pending qty exceeded.");
			}
		}
		return ret;
	}

	private void loadtableComboData(int ar)
	{
		String sql = "select vItemId, vItemName, vItemCode, dbo.funGetNumeric(vItemCode) iCode"+
				" from master.tbRawItemInfo where iActive = 1 order by iCode asc";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			tbCmbItemName.get(ar).addItem(element[0].toString());
			tbCmbItemName.get(ar).setItemCaption(element[0].toString(),
					element[2].toString()+" - "+element[1].toString());
		}
	}

	private void itemValidation(int ar)
	{
		if (tbCmbItemName.get(ar).getValue() != null)
		{
			addChanges();
			String selectId = tbCmbItemName.get(ar).getValue().toString();
			if (!checkDuplicate(selectId, ar))
			{ 
				setItemDetails(ar, selectId); 
				tbAmtIssueQty.get(ar).focus();
			}
			else
			{
				tbCmbItemName.get(ar).setValue(null);
				cm.showNotification("warning", "Warning!", "Duplicate item selected.");
				tbCmbItemName.get(ar).focus();
			}
		}
	}

	private boolean checkDuplicate(String selectId, int ar)
	{
		boolean ret = false;
		for (int i=0; i<tbCmbItemName.size(); i++)
		{
			if (i != ar)
			{
				if (tbCmbItemName.get(i).getValue() != null)
				{
					if (selectId.equals(tbCmbItemName.get(i).getValue().toString()))
					{ ret = true; break; }
				}
			}
		}
		return ret;
	}

	public double totalAmount(String flag)
	{
		double ret = 0, totalamt = 0;

		for (int i=0; i<tbCmbItemName.size(); i++)
		{ totalamt += cm.getAmtValue(tbAmtTotalAmount.get(i)); }

		tblIssueDetailsList.setColumnFooter("Total Amount", cm.setComma(totalamt));
		tblIssueDetailsList.setColumnAlignment("Total Amount", Align.RIGHT);

		if (flag.equals("Total")) { ret = totalamt; }
		return ret;
	}
}
