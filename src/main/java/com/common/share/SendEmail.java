package com.common.share;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

/*import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;*/

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.impl.SessionImpl;

import net.sf.jasperreports.engine.JasperRunManager;

public class SendEmail
{
	private FileWriter log;
	private SessionBean sessionBean;
	private CommonMethod cm;

	public SendEmail(SessionBean sessionBean)
	{
		cm = new CommonMethod(this.sessionBean);

		try
		{
			File f = new File(sessionBean.emailPath);
			f.mkdirs();

			log = new FileWriter(sessionBean.emailPath+"Email/"+"log.txt");

			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			session.beginTransaction();

			/*String host = "smtp.gmail.com";
			String from = "eitysqb1525@gmail.com";
			String pass = "uyhgloluamhmfqsq";*/

			Properties props = System.getProperties();

			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "465");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.starttls.required", "true");
			props.put("mail.smtp.ssl.protocols", "TLSv1.2");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

			//reportGenerate
			//javax.mail.Session esession = javax.mail.Session.getDefaultInstance(props, null);

			viewReportClosing(sessionBean,sessionBean.emailPath+"Email/"+"DailySalesReport"+cm.dfBd.format(new Date())+".pdf");

			/*}
				//Send E-Mail
				for(int k=0;k<vNo.size();k++)
				{String EmailTo=sessionBean.getCompanyEmail().toString();
				MimeMessage message = new MimeMessage(esession);
				message.setFrom(new InternetAddress(from));
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(EmailTo));
				message.setSubject("Daily Sales Report"+cm.dfBd.format(new Date()));
				message.setText("Daily Sales Report"+cm.dfBd.format(new Date()));
				System.out.printf("7");
				// create the message part 
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				//fill message
				messageBodyPart.setText("Daily Sales Report"+cm.dfBd.format(new Date()));
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
				//System.out.printf("8");
				// Part two is attachment
				messageBodyPart = new MimeBodyPart();
				//DataSource source = new FileDataSource(sessionBean.emailPath+"Email/"+cm.dfBdHMS.format(new Date())+".pdf");
				DataSource source = new FileDataSource(sessionBean.emailPath+"Email/"+"DailySalesReport"+cm.dfBd.format(new Date())+".pdf");
				messageBodyPart.setDataHandler( new DataHandler(source));
				messageBodyPart.setFileName(sessionBean.emailPath+"Email/"+"DailySalesReport"+cm.dfBd.format(new Date())+".pdf");
				multipart.addBodyPart(messageBodyPart);
				//System.out.printf("9");
				// Put parts in message
				message.setContent(multipart);
				//System.out.printf("10");
				Transport transport = esession.getTransport("smtp");
				//System.out.printf("11");
				//System.out.printf("host "+host+" from "+from+" pass "+pass);
				transport.connect(host, from, pass);
				//System.out.printf("12");
				transport.sendMessage(message, message.getAllRecipients());
				//System.out.printf("13");
				transport.close();
				//System.out.printf("14");
				log.write("Info:"+"E-mail Send for client id: "+EmailTo+"\n");
				//System.out.printf("15");*/
				/*}*/
				System.out.printf("E-mail Send Successfully.");	

		}
		catch(Exception exp)
		{
			try { log.write("Error:"+exp+"\n"); } catch (IOException e) { e.printStackTrace(); }
			//System.out.printf("Error"+exp);
		}
		finally
		{
			try { log.close(); } catch (IOException e) { e.printStackTrace(); }
		}
	}

	private void viewReportClosing(SessionBean sessionBean, String fpath)
	{
		try
		{
			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			Transaction tx = session.beginTransaction();

			HashMap<String, Object> hm = new HashMap<String, Object>();

			String branch = "%";
			String saleType = "%";
			String userId = "%";
			String invType = "1, 2, 3, 4, 5, 6";
			String datePara = cm.dfBd.format(new Date())+" to "+cm.dfBd.format(new Date());
			String invFrom = "%";

			hm.put("companyName", sessionBean.getCompanyName());
			hm.put("address", sessionBean.getCompanyAddress());
			hm.put("phoneFax", sessionBean.getCompanyContact());
			hm.put("branchName", "ALL");

			hm.put("userName", sessionBean.getFullName());
			hm.put("developerInfo", sessionBean.getDeveloper());
			hm.put("logo", sessionBean.getCompanyLogo());
			hm.put("userIp", "192.0.0.1");
			hm.put("fromToDate", datePara);

			hm.put("salesType", "ALL");
			hm.put("invType", "ALL");
			hm.put("invFrom", "Both POS & Apps");

			String sql = "select ini.vInvoiceNo, convert(date, ini.dSaveDate) dInvoiceDate, SUM(ind.mNetAmount) mAmount,"+
					" SUM(ind.mDisCalculated) mDiscount, SUM(ind.mVoidCalculated) mVoid, SUM(ind.mAmountWithoutVat)"+
					" mAmountWoVat, SUM(ind.mTotalVatAmount) mVatAmount, SUM(ind.mFinalAmount) mNetAmount, st.vSalesType,"+
					" ui.vFullName from trans.tbInvoiceInfo ini, trans.tbInvoiceDetails ind, master.tbUserInfo ui,"+
					" master.tbSalesType st where ini.vInvoiceId = ind.vInvoiceId and ini.vModifiedBy = ui.vUserId"+
					" and ini.iSalesTypeId = st.iSalesTypeId and convert(date, ini.dSaveDate) between '2021-12-22'"+
					" and '2021-12-22' and ini.vModifiedBy like '"+userId+"' and ini.vBranchId like '"+branch+"' and"+
					" ini.iSalesTypeId like '"+saleType+"' and ini.vSplitTicketId like '"+invFrom+"' and ini.iStatusId"+
					" in ("+invType+") group by ini.vInvoiceNo, convert(date, ini.dSaveDate), st.vSalesType, ui.vFullName"+
					" order by st.vSalesType, dInvoiceDate, ui.vFullName";
			//System.out.println(sql);
			hm.put("sql", sql);
			//System.out.printf("\npath"+fpath);
			FileOutputStream of = new FileOutputStream(fpath);

			SessionImpl sessionImpl = (SessionImpl) session;
			Connection conn = sessionImpl.connection();

			JasperRunManager.runReportToPdfStream(getClass().getClassLoader().getResourceAsStream("com/jasper/operation/rptSalesDetails.jasper"),
					of, hm, conn);
			tx.commit();
			of.close();
			log.write("Info:"+"Report generated for client id: "+"\n");
		}
		catch (Exception ex)
		{ System.out.println("Error in view report: "+ex); }
	}
}
