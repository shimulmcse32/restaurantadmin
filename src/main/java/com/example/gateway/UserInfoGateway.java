package com.example.gateway;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.common.share.SessionBean;
import com.common.share.SessionFactoryUtil;
import com.example.model.UserInfoModel;

public class UserInfoGateway
{
	public SimpleDateFormat dfDb = new SimpleDateFormat("yyyy-MM-dd");

	public UserInfoGateway()
	{ }

	public boolean checkExist(String userName, String userId)
	{
		boolean ret = false;
		String edit = (userId.isEmpty()?"":"and vUserId != '"+userId+"'");
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vUserId from master.tbUserInfo where vUserName like '"+userName+"' "+edit+"";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = true; }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist user"); }
		finally{ session.close(); }
		return ret;
	}

	public String getUserId()
	{
		String maxId = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String query = "select isnull(max(cast(SUBSTRING(vUserId, 2, 10) as int)),"+
					" 0)+1 from master.tbUserInfo";
			Iterator<?> iter = session.createSQLQuery(query).list().iterator();
			if (iter.hasNext())
			{ maxId = "U"+iter.next().toString(); }
		}
		catch (Exception ex)
		{ System.out.print(ex+" User Id"); }
		finally{ session.close(); };
		return maxId;
	}

	public String getForgetUserId(String userName, String emailId)
	{
		String ret = "";
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vUserId from master.tbUserInfo where vUserName = '"+userName+"' and vEmailId = '"+emailId+"'";
			Iterator<?> iter = session.createSQLQuery(sql).list().iterator();
			if (iter.hasNext())
			{ ret = iter.next().toString(); }
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist user"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean insertEditData(UserInfoModel uim, String flag)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			if (flag.equals("Add"))
			{
				String sql = "insert into master.tbUserInfo(vRoleId, vBranchId, vUserId, vUserName, vPassword,"+
						" dExpiryDate, vFullName, vMobileNo, vEmailId, iUserTypeId, iActive, iSynced, vSyncedMacId,"+
						" dLastLogin, vPosAccess, vAppAccess, vEmployeeId, vCreatedBy, dCreatedDate, vModifiedBy,"+
						" dModifiedDate, vUserImage) values (:roleId, :branchId, :userId, :userName, :password, :expiryDate,"+
						" :fullName, :mobileNo, :emailId, :userTypeId, 1, 0, '', getDate(), :posAccess, :appAccess, :employeeId,"+
						" :createdBy, getDate(), :modifiedBy, getDate(), :userImage)";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("roleId", uim.getRoleId());
				insert.setParameter("branchId", uim.getBranchId());
				insert.setParameter("userId", uim.getUserId());
				insert.setParameter("userName", uim.getUserName());
				insert.setParameter("password", uim.getPassWord());
				insert.setParameter("expiryDate", dfDb.format(uim.getExpiryDate()));
				insert.setParameter("fullName", uim.getFullName());
				insert.setParameter("mobileNo", uim.getMobileNumber());
				insert.setParameter("emailId", uim.getEmailId());
				insert.setParameter("userTypeId", uim.getUserType());
				insert.setParameter("posAccess", uim.getPosAccess());
				insert.setParameter("appAccess", uim.getAppAccess());
				insert.setParameter("employeeId", uim.getEmployeeId());
				insert.setParameter("createdBy", uim.getCreatedBy());
				insert.setParameter("modifiedBy", uim.getCreatedBy());
				insert.setParameter("userImage", uim.getUserPicture());
				insert.executeUpdate();
			}
			else if (flag.equals("Edit"))
			{
				String sql = " update master.tbUserInfo set vRoleId = :roleId, vBranchId = :branchId,"+
						" vUserName = :userName, vPassword = :password, dExpiryDate = :expiryDate,"+
						" vFullName = :fullName, vMobileNo = :mobileNo, vEmployeeId = :employeeId,"+
						" vEmailId = :emailId, iUserTypeId = :userTypeId, vModifiedBy = :modifiedBy,"+
						" dModifiedDate = getDate(), vPosAccess = :posAccess, vAppAccess = :appAccess,"+
						" iSynced = 0, vSyncedMacId = '' where vUserId = :userId ";
				SQLQuery insert = session.createSQLQuery(sql);
				insert.setParameter("roleId", uim.getRoleId());
				insert.setParameter("branchId", uim.getBranchId());
				insert.setParameter("userName", uim.getUserName());
				insert.setParameter("password", uim.getPassWord());
				insert.setParameter("expiryDate", dfDb.format(uim.getExpiryDate()));
				insert.setParameter("fullName", uim.getFullName());
				insert.setParameter("mobileNo", uim.getMobileNumber());
				insert.setParameter("emailId", uim.getEmailId());
				insert.setParameter("posAccess", uim.getPosAccess());
				insert.setParameter("appAccess", uim.getAppAccess());
				insert.setParameter("userTypeId", uim.getUserType());
				insert.setParameter("employeeId", uim.getEmployeeId());
				insert.setParameter("modifiedBy", uim.getCreatedBy());
				//where clause
				insert.setParameter("userId", uim.getUserId());
				insert.executeUpdate();
			}
			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error insert user: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean activeInactiveData(String userId, String modifiedId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sql = "update master.tbUserInfo set iActive = (select case when iActive = 1"+
					" then 0 else 1 end from master.tbUserInfo where vUserId = :user1),"+
					" vModifiedBy = :modifiedBy, dModifiedDate = getdate(), iSynced = 0,"+
					" vSyncedMacId = '' where vUserId = :user2";

			Query insert = session.createSQLQuery(sql);
			insert.setParameter("user1", userId);
			insert.setParameter("user2", userId);
			insert.setParameter("modifiedBy", modifiedId);
			insert.executeUpdate();

			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update user: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean updatePassword(String userId, String password)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sql = "update master.tbUserInfo set vPassword = :password, vModifiedBy ="+
					" :modifiedBy, dModifiedDate = getdate() where vUserId = :userId";
			Query insert = session.createSQLQuery(sql);
			insert.setParameter("userId", userId);
			insert.setParameter("password", password);
			insert.setParameter("modifiedBy", userId);
			insert.executeUpdate();

			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update password: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean updateProfile(UserInfoModel uim)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sql = "update master.tbUserInfo set vFullName = :fullName, vMobileNo = :mobileNo,"+
					" vEmailId = :emailId, vModifiedBy = :modifiedBy, dModifiedDate = getdate(),"+
					" vUserImage = :userImage where vUserId = :userId";
			Query insert = session.createSQLQuery(sql);
			insert.setParameter("fullName", uim.getFullName());
			insert.setParameter("mobileNo", uim.getMobileNumber());
			insert.setParameter("emailId", uim.getEmailId());
			insert.setParameter("modifiedBy", uim.getCreatedBy());
			insert.setParameter("userId", uim.getUserId());
			insert.setParameter("userImage", uim.getUserPicture());
			insert.executeUpdate();

			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update password: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}

	public boolean selectEditData(UserInfoModel uim, String userIdFind)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select vRoleId, vBranchId, vUserName, vPassword, dExpiryDate, vFullName, vMobileNo, vEmailId,"+
					" iUserTypeId, vEmployeeId, vPosAccess, ISNULL(vAppAccess, '') vAppAccess from master.tbUserInfo"+
					" where vUserId = :userId";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("userId", userIdFind);
			//System.out.println(sql);
			for (Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				uim.setRoleId(element[0].toString());
				uim.setBranchId(element[1].toString());
				uim.setUserName(element[2].toString());
				uim.setPassWord(element[3].toString());
				uim.setExpiryDate((Date) element[4]);
				uim.setFullName(element[5].toString());
				uim.setMobileNumber(element[6].toString());
				uim.setEmailId(element[7].toString());
				uim.setUserType(element[8].toString());
				uim.setEmployeeId(element[9].toString());
				uim.setPosAccess(element[10].toString());
				uim.setAppAccess(element[11].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Select edit data"); }
		finally{ session.close(); }
		return ret;
	}

	public boolean userCheck(UserInfoModel uim, SessionBean sessionBean)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select cm.vCompanyId, cm.vCompanyName, cm.vAddress, cm.vPhone+', '+cm.vFax+', '+cm.vEmail" + 
					" vCompanyContact, bm.vBranchId, bm.vBranchName, bm.iBranchTypeId, bm.vAddress, bm.vPhone+',"+
					" '+bm.vFax+', '+bm.vEmail vBranchContact, ui.vUserId, ui.vUserName, ui.vFullName, ui.iUserTypeId,"+
					" ui.vUserImage, cm.vVatRegNo, cm.vSurName, ui.vPassword, ui.vRoleId, cm.vLogo, bm.iBranchTypeId"+
					" from master.tbCompanyMaster cm, master.tbBranchMaster bm, master.tbUserInfo ui where cm.vCompanyId"+
					" = bm.vCompanyId and bm.vBranchId in (select Item from dbo.Split(ui.vBranchId) where Item = :branchId)"+
					" and ui.vUserName = :userName and ui.vPassword = :password and :branchId in (select Item from"+
					" dbo.Split(ui.vBranchId) where Item = :branchId) and ui.dExpiryDate >= convert(date, getdate())"+
					" and ui.iActive = 1 and cm.iActive = 1 and bm.iActive = 1";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("userName", uim.getUserName());
			select.setParameter("password", uim.getPassWord());
			select.setParameter("branchId", uim.getBranchId());

			for (Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				Object[] element = (Object[]) iter.next();
				if (element[10].toString().equals(uim.getUserName()) && element[16].toString().equals(uim.getPassWord()))
				{
					ret = true;
					sessionBean.setCompanyId(element[0].toString());
					sessionBean.setCompanyName(element[1].toString());
					sessionBean.setCompanyAddress(element[2].toString());
					sessionBean.setCompanyContact(element[3].toString());
					sessionBean.setBranchId(element[4].toString());
					sessionBean.setBranchName(element[5].toString());
					sessionBean.setCentralBranch((element[6].toString().equals("1")?true:false));
					sessionBean.setBranchAddress(element[7].toString());
					sessionBean.setBranchContact(element[8].toString());
					sessionBean.setUserId(element[9].toString());
					sessionBean.setPassword(uim.getPassWord());
					sessionBean.setModuleId("1, 2, 3, 4, 5");//Fixed module id
					sessionBean.setUserName(element[10].toString());
					sessionBean.setFullName(element[11].toString());
					sessionBean.setIsAdmin((element[12].toString().equals("1")?true:false));
					sessionBean.setIsSuperAdmin((element[12].toString().equals("3")?true:false));
					sessionBean.setUserType("");
					sessionBean.setVatRegNo(element[14].toString());
					sessionBean.setSurName(element[15].toString());
					sessionBean.war = element[15].toString();

					setAttachLocation(sessionBean, sessionBean.war);

					sessionBean.setCompanyLogo(element[18].toString().isEmpty()?"./0.png":element[18].toString());
					sessionBean.setUserPicture(element[13].toString().equals("0")?
							sessionBean.imagePathUser+"\\default.jpg":element[13].toString());
					sessionBean.setBranchType(element[19].toString());
					updateLogin(element[9].toString());
				}
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist user"); }
		finally{ session.close(); }
		return ret;
	}

	/*public boolean userCheckModule(String userId, SessionBean sessionBean)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		session.beginTransaction();
		try
		{
			String sql = "select cm.vCompanyId, cm.vCompanyName, cm.vAddress, cm.vPhone+', '+cm.vFax+', '+cm.vEmail"+
					" vCompanyContact, bm.vBranchId, bm.vBranchName, bm.iBranchTypeId, bm.vAddress, bm.vPhone+',"+
					" '+bm.vFax+', '+bm.vEmail vBranchContact, ui.vUserId, ui.vUserName, ui.vFullName, ui.iUserTypeId,"+
					" ui.vUserImage, cm.vVatRegNo, ui.vPassword, cm.vLogo from master.tbCompanyMaster cm,"+
					" master.tbBranchMaster bm, master.tbUserInfo ui where cm.vCompanyId = bm.vCompanyId and"+
					" bm.vBranchId = ui.vBranchId and ui.vUserId = :userId ";
			SQLQuery select = session.createSQLQuery(sql);
			select.setParameter("userId", userId);

			for (Iterator<?> iter = select.list().iterator(); iter.hasNext();)
			{
				ret = true;
				Object[] element = (Object[]) iter.next();
				sessionBean.setCompanyId(element[0].toString());
				sessionBean.setCompanyName(element[1].toString());
				sessionBean.setCompanyAddress(element[2].toString());
				sessionBean.setCompanyContact(element[3].toString());
				sessionBean.setBranchId(element[4].toString());
				sessionBean.setBranchName(element[5].toString());
				sessionBean.setCentralBranch((element[6].toString().equals("1")?true:false));
				sessionBean.setBranchAddress(element[7].toString());
				sessionBean.setBranchContact(element[8].toString());
				sessionBean.setUserId(element[9].toString());
				sessionBean.setPassword(element[15].toString());
				sessionBean.setUserName(element[10].toString());
				sessionBean.setFullName(element[11].toString());
				sessionBean.setIsAdmin((element[12].toString().equals("1")?true:false));
				sessionBean.setIsSuperAdmin((element[12].toString().equals("3")?true:false));
				sessionBean.setUserType("");
				sessionBean.setVatRegNo(element[14].toString());
				sessionBean.setCompanyLogo(element[15].toString());
				sessionBean.setUserPicture(element[13].toString().equals("0")?
						SessionBean.imagePathUser+"\\default.jpg":element[13].toString());
				updateLogin(element[9].toString());
			}
		}
		catch (Exception e)
		{ System.out.println(e+" Check exist user"); }
		finally{ session.close(); }
		return ret;
	}*/

	public boolean checkPass(String password)
	{
		boolean hasLetter = false, hasDigit = false, ret = false;
		if (password.length() >= 8)
		{
			for (int i = 0; i < password.length(); i++)
			{
				char x = password.charAt(i);
				if (Character.isLetter(x))
				{ hasLetter = true; }

				else if (Character.isDigit(x))
				{ hasDigit = true; }

				// no need to check further, break the loop
				if (hasLetter && hasDigit)
				{ ret = false; break; }
			}
			if (hasLetter && hasDigit)
			{ System.out.println("STRONG"); }
			else
			{ System.out.println("NOT STRONG"); }
		}
		else
		{ System.out.println("HAVE AT LEAST 8 CHARACTERS"); }
		return ret;
	}

	private void setAttachLocation(SessionBean sessionBean, String war)
	{
		sessionBean.employeePhoto = "D:/Tomcat 8.0/webapps/report/"+war+"/HR/ProfilePhoto/";
		sessionBean.imagePathDoc = "D:/Tomcat 8.0/webapps/report/"+war+"/HR/Documents/";
		sessionBean.imagePathAtt = "D:/Tomcat 8.0/webapps/report/"+war+"/HR/Attendance/";
		sessionBean.imagePathAcc = "D:/Tomcat 8.0/webapps/report/"+war+"/Acc/Voucher/";
		sessionBean.imagePathAss = "D:/Tomcat 8.0/webapps/report/"+war+"/Acc/Asset/";
		sessionBean.imagePathUser = "D:/Tomcat 8.0/webapps/report/"+war+"/User/";
		sessionBean.imagePathTmp = "D:/Tomcat 8.0/webapps/report/"+war+"/Temp/";
		sessionBean.imagePathLogo = "D:/Tomcat 8.0/webapps/report/"+war+"/";
		sessionBean.itemPhoto = "D:/Tomcat 8.0/webapps/report/"+war+"/POS/ItemImages/";
		sessionBean.commonFiles = "D:/Tomcat 8.0/webapps/report/";
		sessionBean.acc_db = "["+war+"_ACC].";
	}

	public boolean updateLogin(String userId)
	{
		boolean ret = false;
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = session.beginTransaction();
		try
		{
			String sql = "update master.tbUserInfo set dLastLogin = getdate() where vUserId = :userId";
			Query insert = session.createSQLQuery(sql);
			insert.setParameter("userId", userId);
			insert.executeUpdate();

			tx.commit();
			ret = true;
		}
		catch(Exception exp)
		{
			System.out.println("Error update password: "+exp);
			tx.rollback();
		}
		finally{ session.close(); }
		return ret;
	}
}
