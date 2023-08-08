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
package com.example.posreport;

import java.util.HashMap;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.MultiComboBox;
import com.common.share.ReportViewer;
import com.common.share.SessionBean;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class ItemStockReport extends VerticalLayout implements View
{
	private SessionBean sessionBean;

	//Stock Report
	private CommonButton cBtnItem = new CommonButton("", "", "", "", "", "", "", "View", "");
	private OptionGroup ogStockValue;
	private MultiComboBox cmbCategory, cmbRawItem; 
	private Panel panelItem;

	private CommonMethod cm;

	public ItemStockReport(SessionBean sessionBean, String formId)
	{
		this.sessionBean = sessionBean;
		cm = new CommonMethod(this.sessionBean);
		setMargin(true);
		setSpacing(true);

		addComponents(addItem());

		addActionsItem();
		loadCategory();
	}

	//Stock Report Start
	private Panel addItem()
	{
		panelItem = new Panel("Item Stock Report :: "+sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		HorizontalLayout hori = new HorizontalLayout();
		hori.setSpacing(true);
		hori.setMargin(true);
		hori.setSizeFull();

		GridLayout lay = new GridLayout(2, 4);
		lay.setSpacing(true);

		cmbCategory = new MultiComboBox();
		cmbCategory.setWidth("350px");
		cmbCategory.setInputPrompt("Select Category Name");
		cmbCategory.setRequired(true);
		cmbCategory.setRequiredError("This field is required");
		lay.addComponent(new Label("Category Name : "), 0, 0);
		lay.addComponent(cmbCategory, 1, 0);

		cmbRawItem = new MultiComboBox();
		cmbRawItem.setWidth("350px");
		cmbRawItem.setInputPrompt("Select Item(s) Name");
		cmbRawItem.setRequired(true);
		cmbRawItem.setRequiredError("This field is required");
		lay.addComponent(new Label("Item Name : "), 0, 1);
		lay.addComponent(cmbRawItem, 1, 1);

		ogStockValue = new OptionGroup();
		ogStockValue.addItem("All");
		ogStockValue.addItem("With Stock");
		ogStockValue.addItem("Without Stock");
		ogStockValue.select("All");
		ogStockValue.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogStockValue.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		lay.addComponent(ogStockValue, 1, 2);

		lay.addComponent(cBtnItem, 1, 3);

		hori.addComponent(lay);
		hori.setComponentAlignment(lay, Alignment.MIDDLE_CENTER);
		panelItem.setContent(hori);

		return panelItem;
	}

	private void addActionsItem()
	{
		cmbCategory.addValueChangeListener(event -> loadRawItem());

		cBtnItem.btnPreview.addClickListener(event -> addValidationItem());
	}

	private void loadCategory()
	{
		cmbCategory.removeAllItems();
		String sql = "select vCategoryId, vCategoryName from master.tbItemCategory where"+
				" vCategoryType = 'Raw' order by vCategoryName";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbCategory.addItem(element[0].toString());
			cmbCategory.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadRawItem()
	{
		cmbRawItem.removeAllItems();
		String catIds = cmbCategory.getValue().toString().replace("]", "").replace("[", "").trim();
		String sql = "select vItemId, vItemCode, vItemName, dbo.funGetNumeric(vItemCode) iSerial from master.tbRawItemInfo"+
				" where vCategoryId in (select Item from dbo.Split('"+catIds+"')) order by iSerial";
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbRawItem.addItem(element[0].toString());
			cmbRawItem.setItemCaption(element[0].toString(), element[1].toString()+" - "+element[2].toString());
		}
	}

	private void addValidationItem()
	{
		if (!cmbCategory.getValue().toString().replace("]", "").replace("[", "").isEmpty())
		{
			if (!cmbRawItem.getValue().toString().replace("]", "").replace("[", "").isEmpty())
			{ viewReport(); }
			else
			{
				cmbRawItem.focus();
				cm.showNotification("warning", "Warning!", "Select item name.");
			}
		}
		else
		{
			cmbCategory.focus();
			cm.showNotification("warning", "Warning!", "Select item category.");
		}
	}

	private void viewReport()
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String reportSource = "", sql = "", stock = "";
		try
		{
			String branch = sessionBean.getBranchId();
			String itemIds =  cmbRawItem.getValue().toString().replace("]", "").replace("[", "").trim();

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("branchName", sessionBean.getBranchName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("userName", sessionBean.getFullName());
			hm.put("devloperInfo", sessionBean.getDeveloper());
			hm.put("userIp", sessionBean.getUserIp());
			hm.put("logo", sessionBean.getCompanyLogo());

			if (ogStockValue.getValue().toString().equals("With Stock"))
			{ stock = "where mStockQty > 0"; }
			else if (ogStockValue.getValue().toString().equals("Without Stock"))
			{ stock = "where mStockQty <= 0"; }
			sql = "select vItemCode+' - '+vItemName vItemDetails, vUnitName, vCategoryName, vVatCatName, mCostRate,"+
					" mStockQty, (mCostRate*mStockQty) mAmount, dDatetime from (select rii.vItemId, rii.vItemCode,"+
					" rii.vItemName, uni.vUnitName, cat.vCategoryName, vat.vVatCatName, [dbo].[funcItemRate]"+
					" (rii.vItemId, '"+branch+"') mCostRate, [dbo].[funcStockQty] (rii.vItemId, '"+branch+"') mStockQty,"+
					" dbo.funGetNumeric(rii.vItemCode) iSerial, getdate() dDatetime from master.tbRawItemInfo rii,"+
					" master.tbItemCategory cat, master.tbVatCatMaster vat, master.tbUnitInfo uni where rii.vCategoryId"+
					" = cat.vCategoryId and rii.vVatCatId = vat.vVatCatId and rii.vUnitId = convert(varchar(10),"+
					" iUnitId) and vItemId in (select Item from dbo.Split('"+itemIds+"'))) as tbTemp "+stock+""+
					" order by vCategoryName, iSerial, vItemName";
			//System.out.println(sql);
			reportSource = "com/jasper/postransaction/rptItemStockValue.jasper";
			hm.put("sql", sql);
			new ReportViewer(hm, reportSource);
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}

	public void enter(ViewChangeEvent event)
	{ loadCategory(); }
}
