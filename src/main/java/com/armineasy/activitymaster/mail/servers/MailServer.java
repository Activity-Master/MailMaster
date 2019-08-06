package com.armineasy.activitymaster.mail.servers;

public abstract class MailServer
{
	private String hostname;
	private int port = 993;
	private String username;
	private String password;

	public MailServer()
	{
	}

	public String getHostname()
	{
		return this.hostname;
	}

	public int getPort()
	{
		return this.port;
	}

	public String getUsername()
	{
		return this.username;
	}

	public String getPassword()
	{
		return this.password;
	}

	public MailServer setHostname(String hostname)
	{
		this.hostname = hostname;
		return this;
	}

	public MailServer setPort(int port)
	{
		this.port = port;
		return this;
	}

	public MailServer setUsername(String username)
	{
		this.username = username;
		return this;
	}

	public MailServer setPassword(String password)
	{
		this.password = password;
		return this;
	}

	public boolean equals(final Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (!(o instanceof MailServer))
		{
			return false;
		}
		final MailServer other = (MailServer) o;
		if (!other.canEqual((Object) this))
		{
			return false;
		}
		final Object this$hostname = this.getHostname();
		final Object other$hostname = other.getHostname();
		if (this$hostname == null ? other$hostname != null : !this$hostname.equals(other$hostname))
		{
			return false;
		}
		if (this.getPort() != other.getPort())
		{
			return false;
		}
		final Object this$username = this.getUsername();
		final Object other$username = other.getUsername();
		if (this$username == null ? other$username != null : !this$username.equals(other$username))
		{
			return false;
		}
		final Object this$password = this.getPassword();
		final Object other$password = other.getPassword();
		if (this$password == null ? other$password != null : !this$password.equals(other$password))
		{
			return false;
		}
		return true;
	}

	protected boolean canEqual(final Object other)
	{
		return other instanceof MailServer;
	}

	public int hashCode()
	{
		final int PRIME = 59;
		int result = 1;
		final Object $hostname = this.getHostname();
		result = result * PRIME + ($hostname == null ? 43 : $hostname.hashCode());
		result = result * PRIME + this.getPort();
		final Object $username = this.getUsername();
		result = result * PRIME + ($username == null ? 43 : $username.hashCode());
		final Object $password = this.getPassword();
		result = result * PRIME + ($password == null ? 43 : $password.hashCode());
		return result;
	}

	public String toString()
	{
		return "MailServer(hostname=" + this.getHostname() + ", port=" + this.getPort() + ", username=" + this.getUsername() + ", password=" + this.getPassword() + ")";
	}
}
