package com.guicedee.activitymaster.mail.servers;

/**
 * The transport-level security mode used when connecting to a {@link MailServer}.
 */
public enum MailSecurity
{
	/** No transport security (plain text — only suitable for trusted networks / test servers). */
	NONE,
	/** Implicit SSL/TLS from connect (e.g. IMAPS:993, SMTPS:465). */
	SSL_TLS,
	/** Opportunistic upgrade to TLS after connecting on a plain port (STARTTLS). */
	STARTTLS
}

