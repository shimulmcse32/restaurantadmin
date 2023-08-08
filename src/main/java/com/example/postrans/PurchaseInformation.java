/*
 * Copyright 2000-2014 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.postrans;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.ExcelGenerator;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.ReportViewer;
import com.example.gateway.PurchaseInfoGateway;
import com.example.gateway.TransAppCanGateway;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class PurchaseInformation extends VerticalLayout implements View
{
	private CommonButton cBtn = new CommonButton("New", "", "", "", "Refresh", "", "", "", "");

	private TablePaged tblPurchaseList;
	private ArrayList<Label> tbLblPurchaseId = new ArrayList<Label>();
	private ArrayList<Label> tbLblPurchaseNo = new ArrayList<Label>();
	private ArrayList<Label> tbLblPurchaseDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblSupplierName = new ArrayList<Label>();
	private ArrayList<Label> tbLblDeliveryDate = new ArrayList<Label>();
	private ArrayList<Label> tbLblStatus = new ArrayList<Label>();
	private ArrayList<Label> tbLblAmount = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkActive = new ArrayList<CheckBox>();
	private ArrayList<ComboBox> tbCmbAction = new ArrayList<ComboBox>();

	private SessionBean sessionBean;
	private Panel pnlTable;
	private TextField txtSearch;
	private ComboBox cmbSupplierName, cmbStatus;
	private PopupDateField txtFromDate, txtToDate;
	private CommonButton cBtnS = new CommonButton("", "", "", "", "", "Search", "", "", "");

	private CommonMethod cm;
	private PurchaseInfoGateway pig = new PurchaseInfoGateway();

	///Report panel  purchases
	private Panel panelReport;
	private PopupDateField txtFromDateReport, txtToDateReport;
	private CommonButton cBtnReport = new CommonButton("", "", "", "", "", "", "", "View", "");
	private OptionGroup  ogReportFormatReport;
	private ComboBox cmbStatusReport;

	//Report panel Supplier purchases
	private Panel panelAging;
	private ComboBox cmbSupAging;
	private CommonButton cBtnAging = new CommonButton("", "", "", "", "", "", "", "View", "");
	private OptionGroup ogReportTypeAging, ogReportFormatAging;

	public PurchaseInformation(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		//Check authorization
		cm.setAuthorize(sessionBean.getUserId(), formId);
		addComponents(cBtn, addPanel(), addReportPanel());
		cBtn.btnNew.setEnabled(cm.insert);

		addActions();
		loadSupplier();
	}

	private void addActions()
	{
		cBtn.btnNew.addClickListener(event ->
		{
			addEditWindow("Add", "", "");
			event.getButton().setEnabled(false);
		});

		cBtn.btnRefresh.addClickListener(event ->
		{ loadTableInfo(); });

		cBtnS.btnSearch.addClickListener(event ->
		{ loadTableInfo(); });

		cBtnReport.btnPreview.addClickListener(event ->
		{ addValidationReport(); });

		cBtnAging.btnPreview.addClickListener(event ->
		{ addValidationAging(); });

		txtFromDate.addValueChangeListener(event ->
		{ checkSearchSupplierLoad(); });

		txtToDate.addValueChangeListener(event ->
		{ checkSearchSupplierLoad(); });

		tblPurchaseList.addItemClickListener(event ->
		{
			if (event.isDoubleClick() && cm.update)
			{
				int ar = Integer.valueOf(event.getItemId()+"");
				String id = tbLblPurchaseId.get(ar).getValue().toString();
				EditSelectPurchase(id, ar);
			}
		});
	}

	//Table Method
	private void checkSearchSupplierLoad()
	{
		if (txtFromDate.getValue() != null)
		{
			if (txtToDate.getValue() != null)
			{ loadSupplier(); }
			else
			{
				txtToDate.focus();
				cm.showNotification("warning", "Warning!", "Select to date.");
			}
		}
		else
		{
			txtFromDate.focus();
			cm.showNotification("warning", "Warning!", "Select from date.");
		}
	}

	private void loadSupplier()
	{
		cmbSupplierName.removeAllItems();
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());

		String sqlC = "select distinct a.vSupplierId, b.vSupplierName, b.vSupplierCode, dbo.funGetNumeric(b.vSupplierCode)"+
				" iCode from trans.tbPurchaseInfo a inner join master.tbSupplierMaster b on b.vSupplierId = a.vSupplierId"+
				" where dPurchaseDate between '"+fromDate+"' and '"+toDate+"' and a.vBranchId = '"+sessionBean.getBranchId()+"'"+
				" order by iCode asc";

		for (Iterator<?> iter = cm.selectSql(sqlC).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbSupplierName.addItem(element[0].toString());
			cmbSupplierName.setItemCaption(element[0].toString(), element[2].toString()+" - "+element[1].toString());
		}

		String sqlStatus = "select vStatusId, vStatusName from master.tbAllStatus where iActive = 1 and"+
				" vFlag = 'st' order by vStatusName";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sqlStatus).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbStatus.addItem(element[0].toString());
			cmbStatus.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void addEditWindow(String addEdit, String itemId, String ar)
	{
		AddEditPurchaseInfo win = new AddEditPurchaseInfo(sessionBean, addEdit, itemId);
		getUI().addWindow(win);
		win.center();
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();
		win.addCloseListener(event ->
		{
			if (!ar.isEmpty())
			{ tbCmbAction.get(Integer.parseInt(ar)).setEnabled(true); }
			cBtn.btnNew.setEnabled(true);
			loadTableInfo();
			totalAmount();
		});
	}

	private void loadTableInfo()
	{
		String supplier = cmbSupplierName.getValue() == null? "%":cmbSupplierName.getValue().toString();
		String search = "%"+txtSearch.getValue().toString()+"%";
		String status = cmbStatus.getValue() != null? cmbStatus.getValue().toString():"%";
		String fromDate = cm.dfDb.format(txtFromDate.getValue());
		String toDate = cm.dfDb.format(txtToDate.getValue());

		tableClear();
		int i = 0;
		try
		{
			String sql = "select pin.vPurchaseId, vPurchaseNo, dPurchaseDate, dDeliveryDate, (select vSupplierName from"+
					" master.tbSupplierMaster where vSupplierId = pin.vSupplierId) vSupplierName, vRemarks, (select"+
					" isnull(sum(mNetAmount), 0) from trans.tbPurchaseDetails where vPurchaseId = pin.vPurchaseId) Amount,"+
					" pin.iActive, pin.vStatusId, ast.vStatusName from trans.tbPurchaseInfo pin, master.tbAllStatus ast where"+
					" pin.vStatusId = ast.vStatusId and vPurchaseNo like '"+search+"' and pin.vStatusId like '"+status+"'"+
					" and vSupplierId like '"+supplier+"' and dPurchaseDate between '"+fromDate+"' and '"+toDate+"' and"+
					" pin.vBranchId = '"+sessionBean.getBranchId()+"' order by pin.dPurchaseDate, pin.iAutoId desc";
			for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();

				if (tbLblPurchaseId.size() <= i)
				{ tableRowAdd(i); }

				tbLblPurchaseId.get(i).setValue(element[0].toString());
				tbLblPurchaseNo.get(i).setValue(element[1].toString());
				tbLblPurchaseDate.get(i).setValue(cm.dfBd.format(element[2]));
				tbLblDeliveryDate.get(i).setValue(cm.dfBd.format(element[3]));
				tbLblSupplierName.get(i).setValue(element[4].toString());
				tbLblStatus.get(i).setValue(element[9].toString());
				tbLblAmount.get(i).setValue(cm.setComma(Double.parseDouble(element[6].toString())));
				tbChkActive.get(i).setValue((element[7].toString().equals("1")? true:false));
				tbChkActive.get(i).setEnabled(false);
				if (element[8].toString().equals("S6"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Approve");
				}
				if (element[8].toString().equals("S7"))
				{
					tbCmbAction.get(i).removeItem("Edit");
					tbCmbAction.get(i).removeItem("Cancel");
					tbCmbAction.get(i).removeItem("Approve");
				}
				i++;
			}
			tblPurchaseList.nextPage();
			tblPurchaseList.previousPage();
			if (i == 0)
			{ cm.showNotification("warning", "Sorry!", "No data found."); }
			totalAmount();
		}
		catch (Exception e)
		{ System.out.println(e); }
	}

	private double totalAmount()
	{
		double amt = 0;
		for (int i=0; i<tbLblPurchaseId.size(); i++)
		{ amt += cm.getAmtValue(tbLblAmount.get(i)); }
		tblPurchaseList.setColumnFooter("Amount", cm.setComma(amt));
		tblPurchaseList.setColumnAlignment("Amount", Align.RIGHT);
		return amt;
	}

	private void EditSelectPurchase(String purchaseId, int ar)
	{
		if (!pig.getPurchaseUseEdit(purchaseId))
		{
			if (checkPurchase(purchaseId))
			{ addEditWindow("Edit", purchaseId, ar+""); }
		}
		else
		{
			cm.showNotification("failure", "Error!", "Order is in use another Purchase After it.");
			tbCmbAction.get(ar).setEnabled(true);
		}
	}

	private void ActiveInactiveSelectPurchase(String purchaseId, int ar)
	{
		if (checkPurchase(purchaseId))
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
						if (pig.activeInactiveData(purchaseId, sessionBean.getUserId()))
						{
							tbChkActive.get(ar).setValue(!tbChkActive.get(ar).getValue().booleanValue());
							cm.showNotification("success", "Successfull!", "All information updated successfully.");
							tbCmbAction.get(ar).setEnabled(true);
						}
						else
						{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
					}
					else if (buttonType == ButtonType.NO)
					{ tbCmbAction.get(ar).setEnabled(true); }
				}
			});
		}
	}

	private void PurchaseCancelWindow(String purchaseId, String ar)
	{
		if (checkPurchase(purchaseId))
		{
			TransactionCancel win = new TransactionCancel(sessionBean, purchaseId, "purchase");
			getUI().addWindow(win);
			win.center();
			win.setModal(true);
			win.addCloseShortcut(KeyCode.ESCAPE, null);
			win.focus();
			win.addCloseListener(event ->
			{
				if (!ar.isEmpty())
				{ tbCmbAction.get(Integer.parseInt(ar)).setEnabled(true); }
				cBtn.btnNew.setEnabled(true);
				loadTableInfo();
			});
		}
	}

	private boolean checkPurchase(String purchaseId)
	{
		boolean ret = false;
		if (!pig.getPurchaseUse(purchaseId))
		{ ret = true; }
		else
		{ cm.showNotification("warning", "Warning!", "Invoice is in use."); }
		return ret;
	}

	public void viewReportInvoice(String purchaseId)
	{
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
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", sessionBean.getUserIp());
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
			//System.out.println(sql);
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

	private void tableClear()
	{ cm.tableClear(tblPurchaseList, tbLblPurchaseId); }

	//Report Method
	private void addValidationReport()
	{
		if (txtFromDateReport.getValue() != null)
		{
			if (txtToDateReport.getValue() != null)
			{ viewPurchaseReport(); }
			else
			{
				txtToDateReport.focus();
				cm.showNotification("warning", "Warning!", "Select to date.");
			}
		}
		else
		{
			txtFromDateReport.focus();
			cm.showNotification("warning", "Warning!", "Select from date.");
		}
	}

	@SuppressWarnings("deprecation")
	private void viewPurchaseReport()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", xsql = "", fileName = "";

		String fromDate = cm.dfDb.format(txtFromDateReport.getValue());
		String toDate = cm.dfDb.format(txtToDateReport.getValue());
		String status = cmbStatusReport.getValue().toString();
		String datePara = "From: "+cm.dfBd.format(txtFromDateReport.getValue())+" To "+cm.dfBd.format(txtToDateReport.getValue());
		try
		{
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", sessionBean.getUserIp());
			hm.put("fromToDate", datePara);

			sql = "select ld.vSupplierName vLedgerDetails, SUM(oid.mDiscount) mDiscount, SUM(oid.mAmount) mAmount,"+
					" SUM(oid.mVatAmount) mVatAmount, SUM(oid.mNetAmount) mNetAmount from trans.tbPurchaseInfo oii,"+
					" trans.tbPurchaseDetails oid, master.tbSupplierMaster ld where oii.vPurchaseId = oid.vPurchaseId"+
					" and oii.vSupplierId = ld.vSupplierId and oii.dPurchaseDate between '"+fromDate+"' and '"+toDate+"'"+
					" and oid.iActive = 1 and oii.vStatusId like '"+status+"' and oii.vBranchId = '"+sessionBean.getBranchId()+"'"+
					" group by ld.vSupplierName order by vLedgerDetails";

			if (ogReportFormatReport.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				reportSource = "com/jasper/postransaction/rptPurchaseDateBetween.jasper";
				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = sql;
				fileName = "From_"+cm.dfBd.format(txtFromDateReport.getValue())+"_To_"+cm.dfBd.format(txtToDateReport.getValue());
				hm.put("parameters", datePara);

				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private void loadComboAging()
	{
		cmbSupAging.removeAllItems();
		String sqlAging = "select distinct oi.vSupplierId, ld.vSupplierName, dbo.funGetNumeric(ld.vSupplierCode) iCode"+
				" from trans.tbPurchaseInfo oi, master.tbSupplierMaster ld where oi.vSupplierId = ld.vSupplierId and"+
				" oi.vBranchId = '"+sessionBean.getBranchId()+"'order by iCode asc";
		int i = 0;
		for (Iterator<?> iter = cm.selectSql(sqlAging).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			if (i == 0)
			{
				cmbSupAging.addItem("%");
				cmbSupAging.setItemCaption("%", "ALL");
			}
			i++;
			cmbSupAging.addItem(element[0].toString());
			cmbSupAging.setItemCaption(element[0].toString(), element[2].toString()+" - "+element[1].toString());
		}
	}

	private void addValidationAging()
	{
		if (cmbSupAging.getValue() != null)
		{ viewReportAging(); }
		else
		{
			cmbSupAging.focus();
			cm.showNotification("warning", "Warning!", "Select Supplier name.");
		}
	}

	@SuppressWarnings("deprecation")
	private void viewReportAging()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", xsql = "", fileName = "";

		String supplier = cmbSupAging.getValue().toString();
		try
		{
			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", sessionBean.getUserIp());

			sql = "select vLedgerDetails, SUM(m30Days)m30Days, SUM(m60Days)m60Days, SUM(m90Days)m90Days, SUM(m120Days)m120Days,"+
					" SUM(mAbove120Days)mAbove120Days, SUM(mBalance) mTotalAmount from (select vLedgerDetails, dPurchaseDate,"+
					" ISNULL(case when datediff(dd, getdate(), dPurchaseDate) <= 30 then mBalance else 0 end, 0) m30Days,"+
					" ISNULL(case when datediff(dd, getdate(), dPurchaseDate) between 31 and 60 then mBalance else 0 end, 0) m60Days,"+
					" ISNULL(case when datediff(dd, getdate(), dPurchaseDate) between 61 and 90 then mBalance else 0 end, 0) m90Days,"+
					" ISNULL(case when datediff(dd, getdate(), dPurchaseDate) between 91 and 120 then mBalance else 0 end, 0) m120Days,"+
					" ISNULL(case when datediff(dd, getdate(), dPurchaseDate) > 120 then mBalance else 0 end, 0) mAbove120Days, mBalance"+
					" from (select oii.vPurchaseId, ld.vSupplierName vLedgerDetails, oii.dPurchaseDate, SUM(oid.mNetAmount) - ISNULL((select "+
					" SUM(rid.mReceiptAmount) from trans.tbReceiptPurchaseDetails rid where oii.vPurchaseId = rid.vPurchaseId and"+
					" rid.iActive = 1), 0) mBalance from trans.tbPurchaseInfo oii, trans.tbPurchaseDetails oid, master.tbSupplierMaster"+ 
					" ld where oii.vPurchaseId = oid.vPurchaseId and oii.vSupplierId = ld.vSupplierId and oid.iActive = 1 and"+
					" oii.vSupplierId like '"+supplier+"'and oii.vBranchId = '"+sessionBean.getBranchId()+"' group by oii.vPurchaseId,"+
					" ld.vSupplierName, oii.dPurchaseDate) as tbTemp) as temp group by vLedgerDetails order by vLedgerDetails";

			if (ogReportFormatAging.getValue().toString().equals("PDF"))
			{
				hm.put("sql", sql);
				reportSource = "com/jasper/postransaction/rptPurchaseAging.jasper";
				new ReportViewer(hm, reportSource);
			}
			else
			{
				xsql = sql;
				fileName = "Aging_Report";
				hm.put("parameters", cmbSupAging.getItemCaption(cmbSupAging.getValue()).toString());

				ExcelGenerator excel = new ExcelGenerator(xsql, fileName, hm);
				if (excel.file != null)
				{ getUI().getPage().open(new FileResource(excel.file), "_blank", false); }
			}
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	private void TransactionApproveWindow(String transId, String ar)
	{
		MessageBox mb = new MessageBox(getUI(), "Are you sure?",
				MessageBox.Icon.QUESTION, "Do you want to approve information?",
				new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
				new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
		mb.show(new EventListener()
		{
			public void buttonClicked(ButtonType buttonType)
			{
				if (buttonType == ButtonType.YES)
				{
					TransAppCanGateway tacm = new TransAppCanGateway();
					if (tacm.TransactionApprove(transId, sessionBean.getUserId(), "Purchase"))
					{
						cm.showNotification("success", "Successfull!", "All information saved successfully.");
						loadTableInfo();
					}
					else
					{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
				}
			}
		});
	}

	//Table panel start 
	private Panel addPanel()
	{
		pnlTable = new Panel("Purchase List :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout hori = new HorizontalLayout();
		hori.setSpacing(true);

		txtSearch = new TextField();
		txtSearch.setIcon(FontAwesome.SEARCH);
		txtSearch.setInputPrompt("Search by Purchase No");
		txtSearch.setDescription("Search by");
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		txtSearch.addStyleName(ValoTheme.TEXTFIELD_TINY);

		cmbSupplierName = new ComboBox();
		cmbSupplierName.setInputPrompt("Select Supplier Name");
		cmbSupplierName.setWidth("100%");
		cmbSupplierName.setDescription("Select one Supplier");
		cmbSupplierName.addStyleName(ValoTheme.COMBOBOX_TINY);
		/*cmbSupplierName.setRequired(true);*/
		cmbSupplierName.setFilteringMode(FilteringMode.CONTAINS);

		txtFromDate  = new PopupDateField();
		txtFromDate.setImmediate(true);
		txtFromDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDate.setValue(new Date());
		txtFromDate.setWidth("110px");
		txtFromDate.setDateFormat("dd-MM-yyyy");
		txtFromDate.setDescription("From Date");
		txtFromDate.setRequired(true);

		txtToDate  = new PopupDateField();
		txtToDate.setImmediate(true);
		txtToDate.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDate.setValue(new Date());
		txtToDate.setWidth("110px");
		txtToDate.setDateFormat("dd-MM-yyyy");
		txtToDate.setDescription("To Date");
		txtToDate.setRequired(true);

		cmbStatus = new ComboBox();
		cmbStatus.setImmediate(true);
		cmbStatus.setFilteringMode(FilteringMode.CONTAINS);
		cmbStatus.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbStatus.setInputPrompt("Select status");
		cmbStatus.setWidth("115px");

		cBtnS.btnSearch.setStyleName(ValoTheme.BUTTON_TINY);
		hori.addComponents(txtFromDate, txtToDate, cmbSupplierName, txtSearch, cmbStatus, cBtnS);

		buildTable();
		content.addComponents(hori, tblPurchaseList, tblPurchaseList.createControls());
		pnlTable.setContent(content);

		return pnlTable;
	}

	private void buildTable()
	{
		tblPurchaseList = new TablePaged();

		tblPurchaseList.addContainerProperty("Purchase Id", Label.class, new Label(), null, null, Align.CENTER);
		tblPurchaseList.setColumnCollapsed("Purchase Id", true);

		tblPurchaseList.addContainerProperty("Purchase No", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseList.addContainerProperty("Purchase Date", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseList.addContainerProperty("Supplier Name", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseList.addContainerProperty("Delivery Date", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseList.addContainerProperty("Amount", Label.class, new Label(), null, null, Align.RIGHT);

		tblPurchaseList.addContainerProperty("Status", Label.class, new Label(), null, null, Align.CENTER);

		tblPurchaseList.addContainerProperty("Active", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblPurchaseList.addContainerProperty("Action", ComboBox.class, new ComboBox(), null, null, Align.CENTER);
		tblPurchaseList.setColumnWidth("Action", 100);
	}

	private void tableRowAdd(int ar)
	{
		try
		{
			tbLblPurchaseId.add(ar, new Label());
			tbLblPurchaseId.get(ar).setWidth("100%");
			tbLblPurchaseId.get(ar).setImmediate(true);
			tbLblPurchaseId.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblPurchaseNo.add(ar, new Label());
			tbLblPurchaseNo.get(ar).setWidth("100%");
			tbLblPurchaseNo.get(ar).setImmediate(true);
			tbLblPurchaseNo.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblPurchaseDate.add(ar, new Label());
			tbLblPurchaseDate.get(ar).setWidth("100%");
			tbLblPurchaseDate.get(ar).setImmediate(true);
			tbLblPurchaseDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblSupplierName.add(ar, new Label());
			tbLblSupplierName.get(ar).setWidth("100%");
			tbLblSupplierName.get(ar).setImmediate(true);
			tbLblSupplierName.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblDeliveryDate.add(ar, new Label());
			tbLblDeliveryDate.get(ar).setWidth("100%");
			tbLblDeliveryDate.get(ar).setImmediate(true);
			tbLblDeliveryDate.get(ar).addStyleName(ValoTheme.LABEL_TINY);

			tbLblAmount.add(ar, new Label());
			tbLblAmount.get(ar).setWidth("100%");
			tbLblAmount.get(ar).setImmediate(true);
			tbLblAmount.get(ar).addStyleName(ValoTheme.LABEL_TINY);
			tbLblAmount.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

			tbLblStatus.add(ar, new Label());
			tbLblStatus.get(ar).setWidth("100%");
			tbLblStatus.get(ar).setImmediate(true);

			tbChkActive.add(ar, new CheckBox());
			tbChkActive.get(ar).setWidth("100%");
			tbChkActive.get(ar).setImmediate(true);
			tbChkActive.get(ar).addStyleName(ValoTheme.TEXTFIELD_TINY);
			tbChkActive.get(ar).addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

			tbCmbAction.add(ar, new ComboBox());
			tbCmbAction.get(ar).setWidth("100%");
			tbCmbAction.get(ar).setImmediate(true);
			tbCmbAction.get(ar).addStyleName(ValoTheme.COMBOBOX_TINY);
			tbCmbAction.get(ar).addItem("Preview");
			tbCmbAction.get(ar).setItemIcon("Preview", FontAwesome.FILE_PDF_O);
			tbCmbAction.get(ar).setTextInputAllowed(false);
			tbCmbAction.get(ar).setNullSelectionAllowed(false);
			tbCmbAction.get(ar).setInputPrompt("Action");
			if (cm.update)
			{
				tbCmbAction.get(ar).addItem("Active/Inactive");
				tbCmbAction.get(ar).setItemIcon("Active/Inactive", FontAwesome.LOCK);
				tbCmbAction.get(ar).addItem("Edit");
				tbCmbAction.get(ar).setItemIcon("Edit", FontAwesome.EDIT);

				if (sessionBean.getIsAdmin() || sessionBean.getIsSuperAdmin())
				{
					tbCmbAction.get(ar).addItem("Cancel");
					tbCmbAction.get(ar).setItemIcon("Cancel", FontAwesome.CROSSHAIRS);

					tbCmbAction.get(ar).addItem("Approve");
					tbCmbAction.get(ar).setItemIcon("Approve", FontAwesome.CHECK);
				}
			}

			tbCmbAction.get(ar).addValueChangeListener(event ->
			{
				String purchaseId = tbLblPurchaseId.get(ar).getValue().toString();
				if (!purchaseId.isEmpty() && tbCmbAction.get(ar).getValue() != null)
				{
					if (tbCmbAction.get(ar).getValue().toString().equals("Edit"))
					{ EditSelectPurchase(purchaseId, ar); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Active/Inactive"))
					{ ActiveInactiveSelectPurchase(purchaseId, ar); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Preview"))
					{ viewReportInvoice(purchaseId); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Cancel"))
					{ PurchaseCancelWindow(purchaseId, ar+""); }

					else if (tbCmbAction.get(ar).getValue().toString().equals("Approve"))
					{ TransactionApproveWindow(purchaseId, ar+""); }
				}
				tbCmbAction.get(ar).select(null);
			});

			tblPurchaseList.addItem(new Object[]{tbLblPurchaseId.get(ar), tbLblPurchaseNo.get(ar),
					tbLblPurchaseDate.get(ar), tbLblSupplierName.get(ar), tbLblDeliveryDate.get(ar),
					tbLblAmount.get(ar), tbLblStatus.get(ar), tbChkActive.get(ar), tbCmbAction.get(ar)}, ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table."); }
	}

	private HorizontalLayout addReportPanel()
	{
		HorizontalLayout lay = new HorizontalLayout();
		lay.setSizeFull();
		lay.setSpacing(true);
		lay.addComponents(addPurchasePanel(), addAgingReport());

		return lay;
	}

	//Report Panel Purchase Start
	private Panel addPurchasePanel()
	{
		panelReport = new Panel("Purchase Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		panelReport.setHeight("300px");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(3, 6);
		lay.setSpacing(true);

		txtFromDateReport = new PopupDateField();
		txtFromDateReport.setImmediate(true);
		txtFromDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtFromDateReport.setValue(new Date());
		txtFromDateReport.setWidth("-1px");
		txtFromDateReport.setDateFormat("dd-MM-yyyy");
		txtFromDateReport.setDescription("Purchase Date From");
		txtFromDateReport.setInputPrompt("From Date");
		txtFromDateReport.setRequired(true);
		txtFromDateReport.setRequiredError("This field is required");
		lay.addComponent(new Label("From Date: "), 0, 0);
		lay.addComponent(txtFromDateReport, 1, 0);

		txtToDateReport = new PopupDateField();
		txtToDateReport.setImmediate(true);
		txtToDateReport.addStyleName(ValoTheme.DATEFIELD_TINY);
		txtToDateReport.setValue(new Date());
		txtToDateReport.setWidth("-1px");
		txtToDateReport.setDateFormat("dd-MM-yyyy");
		txtToDateReport.setDescription("Purchase Date To");
		txtToDateReport.setInputPrompt("To Date");
		txtToDateReport.setRequired(true);
		txtToDateReport.setRequiredError("This field is required");
		lay.addComponent(new Label("To Date: "), 0, 1);
		lay.addComponent(txtToDateReport, 1, 1);

		cmbStatusReport = new ComboBox();
		cmbStatusReport.setNullSelectionAllowed(false);
		cmbStatusReport.addItem("%");
		cmbStatusReport.setItemCaption("%", "ALL");
		cmbStatusReport.addItem("S5");
		cmbStatusReport.setItemCaption("S5", "Pending");
		cmbStatusReport.addItem("S6");
		cmbStatusReport.setItemCaption("S6", "Approved");
		cmbStatusReport.addItem("S7");
		cmbStatusReport.setItemCaption("S7", "Cancelled");
		cmbStatusReport.select("S6");
		cmbStatusReport.addStyleName(ValoTheme.COMBOBOX_TINY);
		lay.addComponent(new Label("Status: "), 0, 2);
		lay.addComponent(cmbStatusReport, 1, 2);

		ogReportFormatReport = new OptionGroup();
		ogReportFormatReport.addItem("PDF");
		ogReportFormatReport.addItem("XLS");
		ogReportFormatReport.select("PDF");
		ogReportFormatReport.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormatReport.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogReportFormatReport, 1, 3);
		lay.addComponent(cBtnReport, 1, 4);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelReport.setContent(content);

		return panelReport;
	}

	//Report Panel Aging Start
	private Panel addAgingReport()
	{
		panelAging = new Panel("Aging Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		panelAging.setHeight("300px");
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeFull();

		GridLayout lay = new GridLayout(3, 4);
		lay.setSpacing(true);

		ogReportTypeAging = new OptionGroup();
		ogReportTypeAging.addItem("Purchase");
		ogReportTypeAging.select("Purchase");
		ogReportTypeAging.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportTypeAging.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(new Label("Report Type: "), 0, 0);
		lay.addComponent(ogReportTypeAging, 1, 0);

		cmbSupAging = new ComboBox();
		cmbSupAging.setRequired(true);
		cmbSupAging.setWidth("300px");
		cmbSupAging.setRequiredError("This field is required.");
		cmbSupAging.addStyleName(ValoTheme.COMBOBOX_TINY);
		lay.addComponent(new Label("Supplier Name: "), 0, 1);
		lay.addComponent(cmbSupAging, 1, 1);

		ogReportFormatAging = new OptionGroup();
		ogReportFormatAging.addItem("PDF");
		ogReportFormatAging.addItem("XLS");
		ogReportFormatAging.select("PDF");
		ogReportFormatAging.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogReportFormatAging.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogReportFormatAging, 1, 2);
		lay.addComponent(cBtnAging, 1, 3);

		content.addComponent(lay);
		content.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelAging.setContent(content);
		loadComboAging();

		return panelAging;
	}

	public void enter(ViewChangeEvent event)
	{
		loadTableInfo();
		loadComboAging();
	}
}
