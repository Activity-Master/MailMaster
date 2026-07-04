package com.guicedee.activitymaster.mail.servers;

/**
 * Preset {@link MailServer} for Microsoft Outlook / Office 365
 * (IMAPS for reading, SMTP/STARTTLS on 587 for sending).
 */
public class OutlookMailServer extends MailServer<OutlookMailServer>
{
	public OutlookMailServer()
	{
		applyDefaults();
	}

	public OutlookMailServer(String username, String password)
	{
		applyDefaults();
		setUsername(username);
		setPassword(password);
	}

	private void applyDefaults()
	{
		setHostname("outlook.office365.com");
		setStoreProtocol(MailProtocol.IMAPS);
		setTransportProtocol(MailProtocol.SMTP);
		setSecurity(MailSecurity.SSL_TLS);
		withProperty("mail.smtp.host", "smtp.office365.com");
		withProperty("mail.smtp.port", "587");
		withProperty("mail.smtp.starttls.enable", "true");
	}
}

