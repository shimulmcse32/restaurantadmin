package com.example.gateway;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionFactoryUtil;
import com.example.model.TransactionApprovedCancelModel;

public class TransAppCanGateway
{
	public TransAppCanGateway()
	{ }

	public boolean TransactionCancel(TransactionApprovedCancelModel tacm, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String update = "";
			if (flag.equals("Purchase Order"))
			{
				update = " update trans.tbPurchaseOrderInfo set vCancelledBy = :cancelledBy,"+
						" vCancelledTime = getdate(), vCancelReason = :cancelReason,"+
						" vStatusId = :statusId where vOrderId = :transId";
				SQLQuery updateSql = session.createSQLQuery(update);
				updateSql.setParameter("cancelledBy", tacm.getCancelBy());
				updateSql.setParameter("cancelReason", tacm.getCancelReason());
				updateSql.setParameter("statusId", tacm.getStatusId());
				//Where clause
				updateSql.setParameter("transId", tacm.getTransactionId());
				updateSql.executeUpdate();
			}
			else if (flag.equals("Purchase"))
			{
				update = " update trans.tbPurchaseInfo set vCancelledBy = :cancelledBy,"+
						" vCancelledTime = getdate(), vCancelReason = :cancelReason,"+
						" vStatusId = :statusId where vPurchaseId = :transId";
				SQLQuery updateSql = session.createSQLQuery(update);
				updateSql.setParameter("cancelledBy", tacm.getCancelBy());
				updateSql.setParameter("cancelReason", tacm.getCancelReason());
				updateSql.setParameter("statusId", tacm.getStatusId());
				//Where clause
				updateSql.setParameter("transId", tacm.getTransactionId());
				updateSql.executeUpdate();
			}
			else if (flag.equals("Adjustment"))
			{
				update = " update trans.tbStockAdjustmentInfo set vCancelledBy = :cancelledBy,"+
						" vCancelledTime = getdate(), vCancelReason = :cancelReason,"+
						" vStatusId = :statusId where vAdjustId = :transId";
				SQLQuery updateSql = session.createSQLQuery(update);
				updateSql.setParameter("cancelledBy", tacm.getCancelBy());
				updateSql.setParameter("cancelReason", tacm.getCancelReason());
				updateSql.setParameter("statusId", tacm.getStatusId());
				//Where clause
				updateSql.setParameter("transId", tacm.getTransactionId());
				updateSql.executeUpdate();
			}
			else if (flag.equals("Requisition"))
			{
				update = " update trans.tbRequisitionInfo set vCancelledBy = :cancelledBy,"+
						" vCancelledTime = getdate(), vCancelReason = :cancelReason,"+
						" vStatusId = :statusId where vRequisitionId = :transId";
				SQLQuery updateSql = session.createSQLQuery(update);
				updateSql.setParameter("cancelledBy", tacm.getCancelBy());
				updateSql.setParameter("cancelReason", tacm.getCancelReason());
				updateSql.setParameter("statusId", tacm.getStatusId());
				//Where clause
				updateSql.setParameter("transId", tacm.getTransactionId());
				updateSql.executeUpdate();
			}
			else if (flag.equals("Purchase Return"))
			{
				update = " update trans.tbPurchaseReturnInfo set vCancelledBy = :cancelledBy,"+
						" vCancelledTime = getdate(), vCancelReason = :cancelReason,"+
						" vStatusId = :statusId where vReturnId = :transId";
				SQLQuery updateSql = session.createSQLQuery(update);
				updateSql.setParameter("cancelledBy", tacm.getCancelBy());
				updateSql.setParameter("cancelReason", tacm.getCancelReason());
				updateSql.setParameter("statusId", tacm.getStatusId());
				//Where clause
				updateSql.setParameter("transId", tacm.getTransactionId());
				updateSql.executeUpdate();
			}
			else if (flag.equals("Physical Stock"))
			{
				update = " update trans.tbPhysicalStockInfo set vCancelledBy = :cancelledBy,"+
						" vCancelledTime = getdate(), vCancelReason = :cancelReason,"+
						" vStatusId = :statusId where vPhysicalStockId = :transId";
				SQLQuery updateSql = session.createSQLQuery(update);
				updateSql.setParameter("cancelledBy", tacm.getCancelBy());
				updateSql.setParameter("cancelReason", tacm.getCancelReason());
				updateSql.setParameter("statusId", tacm.getStatusId());
				//Where clause
				updateSql.setParameter("transId", tacm.getTransactionId());
				updateSql.executeUpdate();
			}
			else if (flag.equals("Issue"))
			{
				update = " update trans.tbIssueInfo set vCancelledBy = :cancelledBy,"+
						" vCancelledTime = getdate(), vCancelReason = :cancelReason,"+
						" vStatusId = :statusId where vIssueId = :transId";
				SQLQuery updateSql = session.createSQLQuery(update);
				updateSql.setParameter("cancelledBy", tacm.getCancelBy());
				updateSql.setParameter("cancelReason", tacm.getCancelReason());
				updateSql.setParameter("statusId", tacm.getStatusId());
				//Where clause
				updateSql.setParameter("transId", tacm.getTransactionId());
				updateSql.executeUpdate();
			}
			else if (flag.equals("Issue Return"))
			{
				update = " update trans.tbIssueReturnInfo set vCancelledBy = :cancelledBy,"+
						" vCancelledTime = getdate(), vCancelReason = :cancelReason,"+
						" vStatusId = :statusId where vIssueReturnId = :transId";
				SQLQuery updateSql = session.createSQLQuery(update);
				updateSql.setParameter("cancelledBy", tacm.getCancelBy());
				updateSql.setParameter("cancelReason", tacm.getCancelReason());
				updateSql.setParameter("statusId", tacm.getStatusId());
				//Where clause
				updateSql.setParameter("transId", tacm.getTransactionId());
				updateSql.executeUpdate();
			}
			else
			{
				update = " update trans.tbReceiptPurchaseInfo set vCancelledBy = :cancelledBy,"+
						" vCancelledTime = getdate(), vCancelReason = :cancelReason,"+
						" vStatusId = :statusId where vReceiptId = :transId";
				SQLQuery updateSql = session.createSQLQuery(update);
				updateSql.setParameter("cancelledBy", tacm.getCancelBy());
				updateSql.setParameter("cancelReason", tacm.getCancelReason());
				updateSql.setParameter("statusId", tacm.getStatusId());
				//Where clause
				updateSql.setParameter("transId", tacm.getTransactionId());
				updateSql.executeUpdate();

			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update/insert: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean TransactionApprove(String transId, String userId, String flag)
	{
		boolean ret = false;
		String sqlApprove = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equalsIgnoreCase("Purchase Order"))
			{
				sqlApprove = "update trans.tbPurchaseOrderInfo set vStatusId = 'S6', vApprovedBy = '"+userId+"',"+
						" vApproveTime = getdate() where vOrderId like '"+transId+"'";
			}
			else if (flag.equalsIgnoreCase("Purchase Return"))
			{
				sqlApprove = "update trans.tbPurchaseReturnInfo set vStatusId = 'S6', vApprovedBy = '"+userId+"',"+
						" vApproveTime = getdate() where vReturnId like '"+transId+"'";
			}
			else if (flag.equalsIgnoreCase("Purchase Receipt"))
			{
				sqlApprove = "update trans.tbReceiptPurchaseInfo set vStatusId = 'S6', vApprovedBy = '"+userId+"',"+
						" vApproveTime = getdate() where vReceiptId like '"+transId+"'";
			}
			else if (flag.equalsIgnoreCase("Purchase"))
			{
				sqlApprove = "update trans.tbPurchaseInfo set vStatusId = 'S6', vApprovedBy = '"+userId+"',"+
						" vApproveTime = getdate() where vPurchaseId = '"+transId+"'";
			}
			else if (flag.equalsIgnoreCase("Adjustment"))
			{
				sqlApprove = "update trans.tbStockAdjustmentInfo set vStatusId = 'S6', vApprovedBy = '"+userId+"',"+
						" vApproveTime = getdate() where vAdjustId = '"+transId+"'";
			}
			else if (flag.equalsIgnoreCase("Physical Stock"))
			{
				sqlApprove = "update trans.tbPhysicalStockInfo set vStatusId = 'S6', vApprovedBy = '"+userId+"',"+
						" vApproveTime = getdate() where vPhysicalStockId = '"+transId+"'";
			}
			else if (flag.equalsIgnoreCase("Issue"))
			{
				sqlApprove = "update trans.tbIssueInfo set vStatusId = 'S6', vApprovedBy = '"+userId+"',"+
						" vApproveTime = getdate() where vIssueId = '"+transId+"'";
			}
			else if (flag.equals("Requisition"))
			{
				sqlApprove = "update trans.tbRequisitionInfo set vStatusId = 'S6', vApprovedBy = '"+userId+"',"+
						" vApproveTime = getdate() where vRequisitionId = '"+transId+"'";
			}
			else if (flag.equals("Issue Return"))
			{
				sqlApprove = "update trans.tbIssueReturnInfo set vStatusId = 'S6', vApprovedBy = '"+userId+"',"+
						" vApproveTime = getdate() where vIssueReturnId = '"+transId+"'";
			}

			SQLQuery insertMain = session.createSQLQuery(sqlApprove);
			insertMain.executeUpdate();
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update/insert: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}
}
