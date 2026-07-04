package com.guicedee.activitymaster.mail.servers;

/**
 * Preset {@link MailServer} for Google Mail (IMAPS for reading, SMTP/SSL for sending).
 * <p>
 * Note: Gmail requires an <em>app password</em> (or OAuth2) — the account password will be rejected
 * when 2FA is enabled.
 */
public class GMailMailServer extends MailServer<GMailMailServer>
{
	public GMailMailServer()
	{
		applyDefaults();
	}

	public GMailMailServer(String username, String password)
	{
		applyDefaults();
		setUsername(username);
		setPassword(password);
	}

	private void applyDefaults()
	{
		setHostname("imap.gmail.com");
		setStoreProtocol(MailProtocol.IMAPS);
		setTransportProtocol(MailProtocol.SMTPS);
		setSecurity(MailSecurity.SSL_TLS);
		// SMTP host differs from the IMAP host; callers use the protocol-specific properties.
		withProperty("mail.smtps.host", "smtp.gmail.com");
		withProperty("mail.smtp.host", "smtp.gmail.com");
	}
}

