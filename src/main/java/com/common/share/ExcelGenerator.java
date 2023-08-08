package com.common.share;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.hibernate.Session;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;

public class ExcelGenerator
{
	private Connection con;
	private FileWriter fw;
	public File file;
	public String sql, fileName;
	private HashMap<String, Object> hm = new HashMap<String, Object>(); 

	public ExcelGenerator(String sql, String fileName, HashMap<String, Object> hm)
	{
		this.sql = sql;
		this.fileName = fileName;
		this.hm = hm;

		checkData();
	}

	private void checkData()
	{
		if (checkAvaliableData())
		{ generateCSV(); }
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

	private boolean checkAvaliableData()
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if(iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check data"); }
		finally{session.close();}
		return ret;
	}

	@SuppressWarnings("deprecation")
	private void generateCSV()
	{
		try
		{
			Session session = SessionFactoryUtil.getInstance().openSession();
			con = session.connection();
			Statement st = con.createStatement();

			String baseDir = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"".replace("\\","/")+"/VAADIN/xlstmp/";

			//this query gets all the tables in your database(put your db name in the query)
			ResultSet res = null;

			Random r = new Random();
			fileName = baseDir+fileName+"_"+r.nextInt(1000000)+".xls";

			//Delete old files same name
			try
			{
				File file = new File(fileName);
				file.delete();
			}
			catch(Exception exp)
			{ System.out.println(exp); }

			//select all data from table
			res = st.executeQuery(sql);
			//colunm count is necessay as the tables are dynamic and we need to figure out the numbers of columns
			int colunmCount = getColumnCount(res);
			try
			{
				String columnName = "";
				fw = new FileWriter(fileName);

				//Header information format for excel file
				fw.append(hm.get("companyName").toString());
				fw.append("	");
				fw.append(System.getProperty("line.separator"));
				fw.append(hm.get("address").toString().replaceAll(",", "	"));
				fw.append("	");
				fw.append(System.getProperty("line.separator"));
				fw.append(hm.get("phoneFax").toString().replaceAll(",", "	"));
				fw.append("	");
				fw.append(System.getProperty("line.separator"));
				fw.append(System.getProperty("line.separator"));

				//Report parameter
				fw.append(hm.get("parameters").toString().replaceAll(",", "	"));
				fw.append("	");
				fw.append(System.getProperty("line.separator"));
				fw.append(System.getProperty("line.separator"));

				//this loop is used to add column names at the top of file , if you do not need it just comment this loop
				for (int i = 1 ; i <= colunmCount; i++)
				{
					columnName = res.getMetaData().getColumnName(i);
					fw.append(columnName.substring(1, columnName.length()));
					fw.append("	");
				}

				fw.append(System.getProperty("line.separator"));

				while (res.next())
				{
					for (int i = 1; i <= colunmCount; i++)
					{
						//you can update it here by using the column type but i am fine with the data so just converting 
						//everything to string first and then saving
						if (res.getObject(i) != null)
						{
							String data = res.getObject(i).toString();
							fw.append(data) ;
							fw.append("	");
						}
						else
						{
							String data = "null";
							fw.append(data) ;
							fw.append("	");
						}
					}
					//new line entered after each row
					fw.append(System.getProperty("line.separator"));
				}
				fw.flush();
				fw.close();
				file = new File(fileName);
			}
			catch (IOException e)
			{ e.printStackTrace(); }
			con.close();
		}
		catch(SQLException ex)
		{ System.err.println("SQLException information"); }
	}

	//to get no of columns in result set
	private static int getColumnCount(ResultSet res) throws SQLException
	{ return res.getMetaData().getColumnCount(); }
}
