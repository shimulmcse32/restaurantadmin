/**
 * 
 * @author Sebastian Hennebrueder
 * created Feb 22, 2006
 * copyright 2006 by http://www.laliluna.de
 */
package com.common.share;

import java.util.TimerTask;

import com.example.gateway.UserInfoGateway;
import com.example.model.UserInfoModel;

public class MyTask extends TimerTask
{
	public MyTask()
	{ }

	public void run() 
	{
		UserInfoGateway uig = new UserInfoGateway();
		SessionBean sessionBean = new SessionBean();
		//System.out.println("Hi see you after 2 hour");
		UserInfoModel uim = new UserInfoModel();
		uim.setBranchId("B0001");
		uim.setUserName("admin");
		uim.setPassWord("sa");
		if(uig.userCheck(uim, sessionBean))
		{ 
			if(uim.getIsEmailNotification()) 
			{ new SendEmail(sessionBean); }
		}
	}
}
