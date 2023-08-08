package com.common.share;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Random;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import net.sf.jasperreports.engine.JasperRunManager;

public class ReportViewer
{
	private HashMap<String, Object> hm = new HashMap<String, Object>();
	private String reportSource = "";

	public ReportViewer(HashMap<String, Object> hm, String reportSource)
	{
		this.hm = hm;
		this.reportSource = reportSource;

		createShowReport();
	}

	@SuppressWarnings({ "deprecation", "serial" })
	private void createShowReport()
	{
		try
		{
			Random r = new Random();
			String fname = r.nextInt(1000000)+".pdf";
			String pdfOutPath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"".replace("\\","/")+"/VAADIN/rpttmp";
			final String fpath = pdfOutPath+"/"+fname;
			FileOutputStream of = new FileOutputStream(fpath);

			Session session = SessionFactoryUtil.getInstance().openSession();
			Transaction tx = session.beginTransaction();
			try
			{
				JasperRunManager.runReportToPdfStream(getClass().getClassLoader().getResourceAsStream(reportSource), of, hm, session.connection());
				tx.commit();
				of.close();
			}
			catch(Exception exp)
			{
				tx.rollback();
				System.out.println(exp);
			}
			finally
			{
				session.close();
			}

			File file = new File(fpath);
			StreamResource resource = new StreamResource(null, fpath);
			resource.setMIMEType("application/pdf");

			VerticalLayout v = new VerticalLayout();
			v.setSizeFull();
			//v.setMargin(true);

			if(file.length()>1000)
			{
				Embedded e = new Embedded("", new FileResource(file));
				e.setSizeFull();
				e.setType(Embedded.TYPE_BROWSER);
				v.addComponent(e);

				Window w = new Window();
				w.addCloseListener(new CloseListener()
				{
					public void windowClose(CloseEvent e)
					{
						try
						{
							File file = new File(fpath);
							file.delete();
						}
						catch(Exception exp)
						{
							System.out.println(exp);
						}
					}
				});
				w.setCaption("REPORT");
				w.setSizeFull();
				w.center();
				w.setContent(v);
				UI.getCurrent().addWindow(w);
			}
			else
			{
				Notification noti = new Notification("");
				noti.setStyleName("warning bar small");
				noti.setCaption("Sorry!");
				noti.setDescription("No data found by given parameter.");
				noti.setPosition(Position.TOP_CENTER);
				noti.setDelayMsec(Integer.parseInt("2500"));
				noti.show(Page.getCurrent());
			}
		}
		catch (Exception e)
		{
			System.out.println("Error: "+e);
		}
	}
}
