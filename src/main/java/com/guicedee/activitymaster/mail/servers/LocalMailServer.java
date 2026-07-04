package com.guicedee.activitymaster.mail.servers;

/**
 * Preset {@link MailServer} for a plain-text local mail server on {@code localhost}, intended for
 * development and integration tests (e.g. an in-memory GreenMail server).
 * <p>
 * Defaults to no transport security and no authentication so it works out-of-the-box against a
 * test server. Provide explicit ports for the in-memory server's SMTP / IMAP / POP3 listeners.
 */
public class LocalMailServer extends MailServer<LocalMailServer>
{
	public LocalMailServer()
	{
		applyDefaults();
	}

	/**
	 * @param username the login user (test servers usually accept any value)
	 * @param password the login password
	 */
	public LocalMailServer(String username, String password)
	{
		applyDefaults();
		setUsername(username);
		setPassword(password);
	}

	private void applyDefaults()
	{
		setHostname("localhost");
		setStoreProtocol(MailProtocol.IMAP);
		setTransportProtocol(MailProtocol.SMTP);
		setSecurity(MailSecurity.NONE);
		setTrustAll(true);
		// Match the common local dev server (e.g. smtp4dev): SMTP:25, IMAP:143, POP3:110, no TLS, no auth.
		setAuth(false);
		setPort(143);
		withProperty("mail.smtp.port", "25");
		withProperty("mail.imap.port", "143");
		withProperty("mail.pop3.port", "110");
	}

	/**
	 * Convenience setter for the SMTP send port of the local server.
	 *
	 * @param smtpPort the SMTP listener port
	 * @return this server
	 */
	public LocalMailServer withSmtpPort(int smtpPort)
	{
		withProperty("mail.smtp.port", String.valueOf(smtpPort));
		return this;
	}

	/**
	 * Convenience setter for the IMAP receive port of the local server (also sets the default port).
	 *
	 * @param imapPort the IMAP listener port
	 * @return this server
	 */
	public LocalMailServer withImapPort(int imapPort)
	{
		setPort(imapPort);
		withProperty("mail.imap.port", String.valueOf(imapPort));
		return this;
	}
}


