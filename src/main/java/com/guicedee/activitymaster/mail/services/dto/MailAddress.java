package com.guicedee.activitymaster.mail.services.dto;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

/**
 * An email address with an optional display name (the "alias" / personal part).
 * <p>
 * Supports parsing from and rendering to RFC 822 form ({@code "Display Name" <user@host>}).
 */
public class MailAddress implements Serializable
{
	private String personal;
	private String address;

	public MailAddress()
	{
	}

	public MailAddress(String address)
	{
		this.address = normalise(address);
	}

	public MailAddress(String personal, String address)
	{
		this.personal = personal;
		this.address = normalise(address);
	}

	/**
	 * Builds a {@code MailAddress} from a Jakarta {@link InternetAddress}.
	 *
	 * @param internetAddress the source address
	 * @return the parsed mail address
	 */
	public static MailAddress of(InternetAddress internetAddress)
	{
		return new MailAddress(internetAddress.getPersonal(), internetAddress.getAddress());
	}

	/**
	 * Parses an RFC 822 address string.
	 *
	 * @param raw the raw address (e.g. {@code "Jane <jane@acme.com>"})
	 * @return the parsed mail address
	 * @throws AddressException if the string is not a valid address
	 */
	public static MailAddress parse(String raw) throws AddressException
	{
		InternetAddress ia = new InternetAddress(raw, false);
		return of(ia);
	}

	/**
	 * Renders this address to a Jakarta {@link InternetAddress}, preserving the display name (alias).
	 *
	 * @return the internet address
	 * @throws AddressException             if the address part is invalid
	 * @throws UnsupportedEncodingException if the personal name cannot be encoded
	 */
	public InternetAddress toInternetAddress() throws AddressException, UnsupportedEncodingException
	{
		if (personal != null && !personal.isBlank())
		{
			return new InternetAddress(address, personal, "UTF-8");
		}
		return new InternetAddress(address);
	}

	private static String normalise(String address)
	{
		return address == null ? null : address.trim().toLowerCase();
	}

	public String getPersonal()
	{
		return personal;
	}

	public MailAddress setPersonal(String personal)
	{
		this.personal = personal;
		return this;
	}

	public String getAddress()
	{
		return address;
	}

	public MailAddress setAddress(String address)
	{
		this.address = normalise(address);
		return this;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof MailAddress that))
		{
			return false;
		}
		return Objects.equals(address, that.address);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(address);
	}

	@Override
	public String toString()
	{
		return personal == null || personal.isBlank() ? String.valueOf(address) : personal + " <" + address + ">";
	}
}

