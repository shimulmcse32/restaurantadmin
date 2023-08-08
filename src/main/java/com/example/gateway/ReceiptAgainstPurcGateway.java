package com.example.gateway;

import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.PurchaseReceiptModel;

public class ReceiptAgainstPurcGateway
{
	private SimpleDateFormat dfDb = new SimpleDateFormat("yyyy-MM-dd");

	public ReceiptAgainstPurcGateway()
	{ }

	public String getReceiptId(String BranchId)
	{
		String masterNo = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = " Select isnull( max( cast( SUBSTRING( vReceiptId, 7, 50) as int)), 0)+1"+
					" from trans.tbReceiptPurchaseInfo ";

			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ masterNo = BranchId+"R"+ iter.next().toString(); }
		}
		catch (Exception ex) 
		{ System.out.print(ex); }
		finally{ session.close(); }

		return masterNo;
	}

	public String getReceiptNo(String branchId, Object recDate, String recType)
	{
		String receiptNo = "", track = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "Select convert(varchar(10), RIGHT(year('"+dfDb.format(recDate)+"'), 2)) + convert(varchar(10),"+
					" RIGHT('0' + RTRIM(MONTH('"+dfDb.format(recDate)+"')), 2)) + right('0000' + convert(varchar(10),"+
					" ISNULL(MAX(CAST (SUBSTRING(vReceiptNo, 8, 10) as int)), 0) + 1), 4) from trans.tbReceiptPurchaseInfo"+
					" where vBranchId = '"+branchId+"' and MONTH(dReceiptDate) = MONTH('"+dfDb.format(recDate)+"') and"+
					" YEAR(dReceiptDate) = YEAR('"+dfDb.format(recDate)+"') and vReceiptFor = '"+recType+"'";

			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{
				track ="P";
				receiptNo = "RE"+ track + iter.next().toString();
			}
		}
		catch(Exception ex)
		{ System.out.print(ex); }
		finally{ session.close(); }

		return receiptNo;
	}


	public boolean insertEditReceiptPurchase(PurchaseReceiptModel rpm, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sqlMain = " insert into trans.tbReceiptPurchaseInfo (vBranchId, vReceiptId,"+
						" vReceiptNo, vReceiptFor, vReceiptType, vPaymentType, vSupplierId, vRecLedgerId,"+
						" dReceiptDate, vChequeNo, vChequeDate, vStatusId, vApprovedBy, vApproveTime, vCancelledBy,"+
						" vCancelledTime, vCancelReason, iActive, vCreatedBy, dCreatedDate, vModifiedBy, dModifiedDate)"+
						" values (:branchId, :receiptId, :receiptNo, :receiptFor, :receiptType, :paymentType,"+
						" :supplierId, :recLedgerId, :receiptDate, :chequeNo, :chequeDate, :statusId, :approvedBy, '',"+
						" :cancelledBy, :cancelledTime, :cancelReason, 1, :createdBy, getdate(), :modifiedBy, getdate())";
				SQLQuery insertMain = session.createSQLQuery(sqlMain);
				insertMain.setParameter("branchId", rpm.getBranchId());
				insertMain.setParameter("receiptId", rpm.getReceiptId());
				insertMain.setParameter("receiptNo", rpm.getReceiptNo());
				insertMain.setParameter("receiptFor", rpm.getReceiptFor());
				insertMain.setParameter("receiptType", rpm.getReceiptType());
				insertMain.setParameter("paymentType", rpm.getPayType());
				insertMain.setParameter("supplierId", rpm.getSupCusId());
				insertMain.setParameter("recLedgerId", rpm.getLedgerId());
				insertMain.setParameter("receiptDate", dfDb.format(rpm.getReceiptDate()));
				insertMain.setParameter("chequeNo", rpm.getChequeNo());
				insertMain.setParameter("chequeDate", rpm.getChequeDate());
				insertMain.setParameter("statusId", rpm.getStatusId());
				insertMain.setParameter("approvedBy", rpm.getApproveBy());
				insertMain.setParameter("cancelledBy", rpm.getCancelBy());
				insertMain.setParameter("cancelledTime", "");
				insertMain.setParameter("cancelReason", rpm.getCancelReason());
				insertMain.setParameter("createdBy", rpm.getCreatedBy());
				insertMain.setParameter("modifiedBy", rpm.getCreatedBy());
				insertMain.executeUpdate();

				//Details Info
				if (!rpm.getDetailsSql().trim().isEmpty())
				{ session.createSQLQuery(rpm.getDetailsSql()).executeUpdate(); }
			}
			else if (flag.equals("Edit"))
			{
				String sqlMain = " update trans.tbReceiptPurchaseInfo set vReceiptType = :receiptType,"+
						" vPaymentType = :paymentType, vRecLedgerId = :recLedgerId, dReceiptDate = :receiptDate, vChequeNo ="+
						" :chequeNo, vChequeDate = :chequeDate, vModifiedBy = :modifiedBy, dModifiedDate = getdate(),"+
						" vApprovedBy = :approvedBy, vApproveTime = getdate() where vReceiptId = :receiptId";
				SQLQuery insertMain = session.createSQLQuery(sqlMain);
				insertMain.setParameter("receiptType", rpm.getReceiptType());
				insertMain.setParameter("paymentType", rpm.getPayType());
				insertMain.setParameter("recLedgerId", rpm.getLedgerId());
				insertMain.setParameter("receiptDate", rpm.getReceiptDate());
				insertMain.setParameter("chequeNo", rpm.getChequeNo());
				insertMain.setParameter("chequeDate", rpm.getChequeDate());
				insertMain.setParameter("modifiedBy", rpm.getCreatedBy());
				insertMain.setParameter("approvedBy", rpm.getApproveBy());

				//Where clause
				insertMain.setParameter("receiptId", rpm.getReceiptId());
				insertMain.executeUpdate();

				//Details Info
				if (!rpm.getDetailsSql().trim().isEmpty() && rpm.getOrderChangeDetails())
				{ session.createSQLQuery(rpm.getDetailsSql()).executeUpdate(); }
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update insert: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

}
