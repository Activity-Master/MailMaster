package com.armineasy.activitymaster.mail.servers;

public class GMailMailServer extends MailServer
{
	public GMailMailServer(String username, String password)
	{
		setHostname("imap.gmail.com");
		setPort(993);
		setUsername(username);
		setPassword(password);
	}
}
