package com.common.share;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;

public class CommonMethod implements Serializable
{
	private static final long serialVersionUID = -4548629751099132680L;

	public SessionBean sessionBean;
	public SimpleDateFormat dfMY = new SimpleDateFormat("MMMMM-yyyy");
	public SimpleDateFormat dfBd = new SimpleDateFormat("dd-MM-yyyy");
	public SimpleDateFormat dfDb = new SimpleDateFormat("yyyy-MM-dd");
	public SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
	public SimpleDateFormat dfBdHMA = new SimpleDateFormat("dd-MM-yyyy hh:mm aa");
	public SimpleDateFormat dfDbHMA = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
	public SimpleDateFormat dfT = new SimpleDateFormat("hh:mm aa");

	public DecimalFormat deciInt = new DecimalFormat("#0");
	public DecimalFormat deciFloat = new DecimalFormat("#0.00");
	public DecimalFormat deciMn = new DecimalFormat("#0.000");
	public boolean insert = false, delete = false, update = false, preview = false;

	public CommonMethod(SessionBean sessionBean)
	{ this.sessionBean = sessionBean; }

	public Date getOpeningDate()
	{
		Date OpDate = null;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		String sql = "Select dFiscalYearStart from .[funFiscalYearInfo]("+
				"'"+dfDb.format(new Date())+"', '"+dfDb.format(new Date())+"', '"+sessionBean.getBranchId()+"')";
		//System.out.println(sql);
		Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
		if (iter.hasNext())
		{ OpDate = (Date) iter.next(); }
		return OpDate;
	}

	//to get fiscal year id using from date for transactions
	public String fiscalYearId(Object VoucherDate)
	{
		String fiscalYearId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select vFiscalYearId from [dbo].[funFiscalYearInfo]('"+dfDb.format(VoucherDate)+"',"
					+ " '"+dfDb.format(VoucherDate)+"', '"+sessionBean.getBranchId()+"')";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ fiscalYearId = iter.next().toString(); }
		}
		catch (Exception e)
		{ System.out.println(e+" Common method 1"); }
		finally{ session.close(); }
		return fiscalYearId;
	}

	//to check fiscal year id between from date and to date
	public String checkFindDate(Object FromDate, Object ToDate)
	{
		String chechYearId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select iCheckFiscalYear from [dbo].[funFiscalYearInfo]('"+dfDb.format(FromDate)+"',"
					+ " '"+dfDb.format(ToDate)+"', '"+sessionBean.getBranchId()+"')";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ chechYearId = iter.next().toString(); }
		}
		catch (Exception e)
		{ System.out.println(e+" Find date"); }
		finally{ session.close(); }
		return chechYearId;
	}

	//back Driver Control Account
	public String getDriverControlAcc()
	{
		String driConAcc = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vLedgerId from [dbo].[funLeaseLedgers]('DCA')";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ driConAcc = iter.next().toString(); }
		}
		catch (Exception e)
		{ System.out.println(e+" Lease Driver"); }
		finally{ session.close(); }
		return driConAcc;
	}

	//to check fiscal year is close or not if closed then return 1
	public boolean checkYearClosed(Object Date)
	{
		boolean isClosed = true;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select iClosed from [dbo].[funFiscalYearInfo]('"+dfDb.format(Date)+"',"
					+ " '"+dfDb.format(Date)+"', '"+sessionBean.getBranchId()+"')";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			//System.out.println(sql);
			if (iter.hasNext())
			{ isClosed = iter.next().toString().equals("1")? false:true; }
		}
		catch (Exception e)
		{ System.out.println(e+" YearClosed"); }
		finally{ session.close(); }
		return isClosed;
	}

	public String getPriGroupPath(String grpId)
	{
		String path = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String //query = "select vItemOf from master.tbPrimaryGroup where vPrimaryGroupId = '"+grpId+"'";
			query = "select pg.vItemOf+' / '+gn.vGroupName from master.tbPrimaryGroup pg,"+
					" master.tbGroupName gn where pg.vGroupId = gn.vGroupId and pg.vPrimaryGroupId = '"+grpId+"'";
			//System.out.println(query);
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ path = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" Pri Group Path"); }
		finally{ session.close(); };
		return path;
	}

	public double getAmtValue(Label field)
	{
		double value = 0;
		value = Double.parseDouble(field.getValue().toString().trim().isEmpty()? "0":field.getValue().toString().replaceAll(",", "").trim());
		return value;
	}

	public double getAmtValue(CommaField field)
	{
		double value = 0;
		value = Double.parseDouble(field.getValue().toString().trim().isEmpty()? "0":field.getValue().toString().replaceAll(",", "").trim());
		return value;
	}

	public String getSystemLedger(String flag)
	{
		String ledgerId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select [dbo].[funSystemLedger]('"+flag+"')";
			//System.out.println(query);
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ ledgerId = iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" SystemLedger"); }
		finally{ session.close(); };
		return ledgerId;
	}

	public String getVoucherNo(String voucherType, Object voucherDate)
	{
		String getVoucherNo = "", fiscalId = fiscalYearId(voucherDate), voucherPrefix = "";
		if (!fiscalId.equals("0"))
		{
			Session session = SessionFactoryUtil.getInstance().openSession();
			session.beginTransaction();
			try
			{
				if (voucherType.equals("cpv"))
				{ voucherPrefix = "PCV"; }
				if (voucherType.equals("crv"))
				{ voucherPrefix = "CRV"; }
				if (voucherType.equals("bpv"))
				{ voucherPrefix = "BPV"; }
				if (voucherType.equals("brv"))
				{ voucherPrefix = "BRV"; }
				if (voucherType.equals("jnv"))
				{ voucherPrefix = "JNV"; }
				if (voucherType.equals("cnv"))
				{ voucherPrefix = "CON"; }
				if (voucherType.equals("apv"))
				{ voucherPrefix = "APV"; }
				if (voucherType.equals("asv"))
				{ voucherPrefix = "ASV"; }
				if (voucherType.equals("dep"))
				{ voucherPrefix = "DEP"; }
				if (voucherType.equals("wtc"))
				{ voucherPrefix = "WTC"; }
				if (voucherType.equals("inv"))
				{ voucherPrefix = "INV"; }
				if (voucherType.equals("rnv"))
				{ voucherPrefix = "RNV"; }
				if (voucherType.equals("shv"))
				{ voucherPrefix = "SHV"; }

				String query = "Select right('0000' + Convert(varchar(10), ISNULL(MAX(CAST(SUBSTRING(vVoucherNo, "+(voucherPrefix.length()+1)+","+
						" 10) as int)), 0) + 1), 4) from trans.tbVoucher where vVoucherType = '"+voucherType+"' and"+
						" vBranchId = '"+sessionBean.getBranchId()+"' and vFiscalYearId = '"+fiscalId+"'";
				//System.out.println(query);
				Iterator<?> iter = session.createSQLQuery(query).list().iterator();
				if (iter.hasNext())
				{ getVoucherNo = voucherPrefix + iter.next().toString(); }
			}
			catch(Exception ex)
			{ System.out.print(ex+ "Voucher type"); }
			finally{ session.close(); }
		}
		return getVoucherNo;
	}

	public Object fiscalYearDate(String openClose, Object Date)
	{
		Object openCloseDate = null;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select dFiscalYearStart, dFiscalYearEnd from [dbo].[funFiscalYearInfo]('"+dfDb.format(Date)+"',"
					+ " '"+dfDb.format(Date)+"', '"+sessionBean.getBranchId()+"')";
			List<?> list = session.createSQLQuery(sql).list();
			for(Iterator<?> iter = list.iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				if (openClose.equalsIgnoreCase("Open"))
				{openCloseDate = element[0];}
				else
				{openCloseDate = element[1];}
			}
		}
		catch(Exception e)
		{ System.out.print(e+ " Year Date"); }
		finally{ session.close(); };
		return openCloseDate;
	}

	public String ledgerPath(String ledgerId, Object Date)
	{
		String ledgerPath = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vLedgerPath from [dbo].[funLedgerBalanceBudget]('"+ledgerId+"',"+
					" '"+Date+"', '%', '"+sessionBean.getBranchId()+"')";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ledgerPath = iter.next().toString(); }
		}
		catch(Exception e)
		{ System.out.print(e+" Ledger Path"); }
		finally{ session.close(); };
		return ledgerPath;
	}

	public List<?> selectSql(String sql)
	{
		List<?> list = null; 
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{ list = session.createSQLQuery(sql).list(); }
		catch(Exception e)
		{ System.out.println("Select Query: "+e); }
		finally{ session.close(); }
		return list;
	}

	public void setAuthorize(String userId, String formId)
	{
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select urd.iInsert, urd.iDelete, urd.iUpdate from master.tbUserInfo ui,"+
					" master.tbUserRoleInfo uri, master.tbUserRoleDetails urd, master.tbMenuList ml"+
					" where ui.vRoleId = uri.vRoleId and uri.vRoleId = urd.vRoleId and urd.vMenuId"+
					" = ml.vMenuId and ui.vUserId = :userId and ml.vURL = :formId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("userId", userId);
			select.setParameter("formId", formId);

			for(Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				insert = (element[0].toString().equals("1")?true:false);
				delete = (element[1].toString().equals("1")?true:false);
				update = (element[2].toString().equals("1")?true:false);
				//System.out.println(element[0].toString()+" "+formId);
				//preview = false;
			}
		}
		catch(Exception e)
		{ System.out.println("User authentications "+e); }
		finally{ session.close(); };
	}

	public String setComma(Double value)
	{ return new CommaSeparator().setComma(value); }

	public String setComma(String value)
	{ return new CommaSeparator().setComma(Double.parseDouble(value)); }

	public String getModuleName()
	{
		String moduleName = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vModuleName from master.tbModuleList where"+
					" vModuleId in ("+sessionBean.getModuleId()+")";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ moduleName = iter.next().toString(); }
		}
		catch (Exception e)
		{ System.out.println(e+" Module Name"); }
		finally{ session.close(); }
		return moduleName;
	}

	public String getComboValue(ComboBox combobox)
	{ return combobox.getValue() != null? combobox.getValue().toString():""; }

	public String getComboCaption(ComboBox combobox)
	{
		String retValue = "";
		if (combobox.getValue() != null)
		{ retValue = combobox.getItemCaption(combobox.getValue()).toString().trim(); }
		return retValue;
	}

	public String getMultiComboValue(MultiComboBox combobox)
	{
		String retValue = "";
		retValue = combobox.getValue().toString().replace("[", "").replace("]", "").trim();
		return retValue;
	}

	public double getRound(double amount)
	{
		double round = 0;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select round("+amount+", 3) mAmount";
			//System.out.println(query);
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ round = Double.parseDouble(iter.next().toString()); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" getRound"); }
		finally{ session.close(); };
		return round;
	}

	public void showNotification(String Type, String Title, String Message)
	{
		Notification noti = new Notification("");
		noti.setStyleName(Type+" bar small");
		noti.setCaption(Title);
		noti.setDescription(Message);
		noti.setPosition(Position.TOP_CENTER);
		noti.setDelayMsec(Integer.parseInt("2500"));
		noti.show(Page.getCurrent());
	}

	public void showNotification(String Type, String Title, String Message, String ledger)
	{
		Notification noti = new Notification("");
		noti.setStyleName(Type+" bar small");
		noti.setCaption(Title);
		noti.setDescription(Message);
		noti.setPosition(Position.BOTTOM_RIGHT);
		noti.setDelayMsec(Integer.parseInt("2500"));
		noti.show(Page.getCurrent());
	}

	public void dateCompare(PopupDateField from, PopupDateField to)
	{
		if (from.getValue().compareTo(to.getValue()) > 0)
		{ to.setValue(from.getValue()); }
	}

	@SuppressWarnings("deprecation")
	public void tableClear(TablePaged tp, ArrayList<Label> comp)
	{
		tp.removeAllItems();
		tp.requestRepaintAll();
		comp.clear();
	}
}
