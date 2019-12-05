package com.guicedee.activitymaster.mail.servers;

public class SaNrgMailServer
		extends MailServer
{
	public SaNrgMailServer(String username, String password)
	{
		setHostname("mail.sanrg.net");
		setPort(993);
		setUsername(username);
		setPassword(password);
	}


}
