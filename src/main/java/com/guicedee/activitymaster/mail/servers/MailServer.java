package com.guicedee.activitymaster.mail.servers;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;

import java.util.Properties;

/**
 * Connection details for a mail server, supporting the full matrix of protocols
 * ({@link MailProtocol}) and transport security modes ({@link MailSecurity}).
 * <p>
 * A single {@code MailServer} carries both a <em>store</em> protocol (for receiving) and a
 * <em>transport</em> protocol (for sending) so the same logical account can be used to read and
 * send mail. The {@link #toStoreSession()} and {@link #toTransportSession()} helpers build a
 * configured Jakarta Mail {@link Session} for each direction.
 *
 * @param <J> the concrete server type (CRTP fluent builder)
 */
@SuppressWarnings("unchecked")
public class MailServer<J extends MailServer<J>>
{
	private String hostname;
	private Integer port;
	private String username;
	private String password;

	/** Protocol used to read mail (defaults to IMAPS). */
	private MailProtocol storeProtocol = MailProtocol.IMAPS;
	/** Protocol used to send mail (defaults to SMTP/STARTTLS). */
	private MailProtocol transportProtocol = MailProtocol.SMTP;
	private MailSecurity security = MailSecurity.SSL_TLS;

	private boolean auth = true;
	private boolean debug = false;
	private int connectTimeoutMs = 15000;
	private int timeoutMs = 30000;
	/** Disable certificate / host verification — useful for self-signed test servers only. */
	private boolean trustAll = false;

	private final Properties extraProperties = new Properties();

	public MailServer()
	{
	}

	public MailServer(String hostname, String username, String password)
	{
		this.hostname = hostname;
		this.username = username;
		this.password = password;
	}

	// ---- Session builders -------------------------------------------------------------------

	/**
	 * Builds a Jakarta Mail {@link Session} configured for the <em>store</em> (receive) protocol.
	 *
	 * @return a configured store session
	 */
	public Session toStoreSession()
	{
		return buildSession(storeProtocol);
	}

	/**
	 * Builds a Jakarta Mail {@link Session} configured for the <em>transport</em> (send) protocol.
	 *
	 * @return a configured transport session
	 */
	public Session toTransportSession()
	{
		return buildSession(transportProtocol);
	}

	private Session buildSession(MailProtocol protocol)
	{
		Properties props = toProperties(protocol);
		if (auth && username != null)
		{
			return Session.getInstance(props, new Authenticator()
			{
				@Override
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(username, password);
				}
			});
		}
		return Session.getInstance(props);
	}

	/**
	 * Builds the Jakarta Mail property set for the supplied protocol, honouring the configured
	 * {@link MailSecurity} mode, timeouts and authentication flags.
	 *
	 * @param protocol the protocol to configure properties for
	 * @return the populated property set
	 */
	public Properties toProperties(MailProtocol protocol)
	{
		Properties props = new Properties();
		String p = protocol.providerName();
		int resolvedPort = port != null ? port : protocol.defaultPort();

		if (protocol.isTransport())
		{
			props.put("mail.transport.protocol", p);
		}
		props.put("mail." + p + ".host", hostname == null ? "" : hostname);
		props.put("mail." + p + ".port", String.valueOf(resolvedPort));
		props.put("mail." + p + ".auth", String.valueOf(auth));
		props.put("mail." + p + ".connectiontimeout", String.valueOf(connectTimeoutMs));
		props.put("mail." + p + ".timeout", String.valueOf(timeoutMs));
		props.put("mail." + p + ".writetimeout", String.valueOf(timeoutMs));

		boolean implicit = security == MailSecurity.SSL_TLS || protocol.isImplicitSsl();
		boolean startTls = security == MailSecurity.STARTTLS;

		props.put("mail." + p + ".ssl.enable", String.valueOf(implicit));
		props.put("mail." + p + ".starttls.enable", String.valueOf(startTls));
		if (startTls)
		{
			props.put("mail." + p + ".starttls.required", "true");
		}
		if (trustAll)
		{
			props.put("mail." + p + ".ssl.trust", "*");
			props.put("mail." + p + ".ssl.checkserveridentity", "false");
		}
		props.put("mail.debug", String.valueOf(debug));

		props.putAll(extraProperties);
		return props;
	}

	// ---- Fluent accessors -------------------------------------------------------------------

	public String getHostname()
	{
		return hostname;
	}

	public J setHostname(String hostname)
	{
		this.hostname = hostname;
		return (J) this;
	}

	/**
	 * @return the configured port, or the store protocol default when unset.
	 */
	public int getPort()
	{
		return port != null ? port : storeProtocol.defaultPort();
	}

	public J setPort(int port)
	{
		this.port = port;
		return (J) this;
	}

	public String getUsername()
	{
		return username;
	}

	public J setUsername(String username)
	{
		this.username = username;
		return (J) this;
	}

	public String getPassword()
	{
		return password;
	}

	public J setPassword(String password)
	{
		this.password = password;
		return (J) this;
	}

	public MailProtocol getStoreProtocol()
	{
		return storeProtocol;
	}

	public J setStoreProtocol(MailProtocol storeProtocol)
	{
		this.storeProtocol = storeProtocol;
		return (J) this;
	}

	public MailProtocol getTransportProtocol()
	{
		return transportProtocol;
	}

	public J setTransportProtocol(MailProtocol transportProtocol)
	{
		this.transportProtocol = transportProtocol;
		return (J) this;
	}

	public MailSecurity getSecurity()
	{
		return security;
	}

	public J setSecurity(MailSecurity security)
	{
		this.security = security;
		return (J) this;
	}

	public boolean isAuth()
	{
		return auth;
	}

	public J setAuth(boolean auth)
	{
		this.auth = auth;
		return (J) this;
	}

	public boolean isDebug()
	{
		return debug;
	}

	public J setDebug(boolean debug)
	{
		this.debug = debug;
		return (J) this;
	}

	public int getConnectTimeoutMs()
	{
		return connectTimeoutMs;
	}

	public J setConnectTimeoutMs(int connectTimeoutMs)
	{
		this.connectTimeoutMs = connectTimeoutMs;
		return (J) this;
	}

	public int getTimeoutMs()
	{
		return timeoutMs;
	}

	public J setTimeoutMs(int timeoutMs)
	{
		this.timeoutMs = timeoutMs;
		return (J) this;
	}

	public boolean isTrustAll()
	{
		return trustAll;
	}

	public J setTrustAll(boolean trustAll)
	{
		this.trustAll = trustAll;
		return (J) this;
	}

	public Properties getExtraProperties()
	{
		return extraProperties;
	}

	public J withProperty(String key, String value)
	{
		this.extraProperties.put(key, value);
		return (J) this;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "(hostname=" + hostname + ", port=" + getPort()
				+ ", username=" + username + ", store=" + storeProtocol + ", transport=" + transportProtocol
				+ ", security=" + security + ")";
	}
}

