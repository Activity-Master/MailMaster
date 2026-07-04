package com.guicedee.activitymaster.mail.servers;

/**
 * The supported mail protocols and their canonical Jakarta Mail provider names and default ports.
 * <p>
 * Store protocols ({@code IMAP}, {@code IMAPS}, {@code POP3}, {@code POP3S}) are used to <em>receive</em>
 * mail; transport protocols ({@code SMTP}, {@code SMTPS}) are used to <em>send</em> mail.
 */
public enum MailProtocol
{
	/** Plain IMAP (typically upgraded with STARTTLS). */
	IMAP("imap", 143, false, false),
	/** Implicit-SSL IMAP. */
	IMAPS("imaps", 993, false, true),
	/** Plain POP3. */
	POP3("pop3", 110, false, false),
	/** Implicit-SSL POP3. */
	POP3S("pop3s", 995, false, true),
	/** Plain SMTP (typically upgraded with STARTTLS on 587). */
	SMTP("smtp", 587, true, false),
	/** Implicit-SSL SMTP. */
	SMTPS("smtps", 465, true, true),
	;

	private final String providerName;
	private final int defaultPort;
	private final boolean transport;
	private final boolean implicitSsl;

	MailProtocol(String providerName, int defaultPort, boolean transport, boolean implicitSsl)
	{
		this.providerName = providerName;
		this.defaultPort = defaultPort;
		this.transport = transport;
		this.implicitSsl = implicitSsl;
	}

	/**
	 * @return the Jakarta Mail provider name (e.g. {@code imaps}, {@code smtp}).
	 */
	public String providerName()
	{
		return providerName;
	}

	/**
	 * @return the IANA default port for this protocol.
	 */
	public int defaultPort()
	{
		return defaultPort;
	}

	/**
	 * @return {@code true} for sending protocols (SMTP/SMTPS), {@code false} for receiving protocols.
	 */
	public boolean isTransport()
	{
		return transport;
	}

	/**
	 * @return {@code true} if the protocol negotiates SSL/TLS from the first byte (implicit SSL).
	 */
	public boolean isImplicitSsl()
	{
		return implicitSsl;
	}
}

