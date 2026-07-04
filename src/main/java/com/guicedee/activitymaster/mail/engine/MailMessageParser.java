package com.guicedee.activitymaster.mail.engine;

import com.guicedee.activitymaster.mail.services.dto.MailAddress;
import com.guicedee.activitymaster.mail.services.dto.MailAttachment;
import com.guicedee.activitymaster.mail.services.dto.MailMessage;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Parses a Jakarta Mail {@link Message} (typically a {@link MimeMessage}) into the transport-agnostic
 * {@link MailMessage} DTO, recursively walking multipart bodies to extract the plain-text body, HTML
 * body and every {@link MailAttachment} (including inline parts).
 */
public final class MailMessageParser
{
	private MailMessageParser()
	{
	}

	/**
	 * Parses a message into a {@link MailMessage}.
	 *
	 * @param message the source message
	 * @param folder  the originating folder name (may be {@code null})
	 * @return the parsed DTO
	 * @throws MessagingException on a mail protocol error
	 * @throws IOException        when reading a body part fails
	 */
	public static MailMessage parse(Message message, String folder) throws MessagingException, IOException
	{
		MailMessage out = new MailMessage();
		out.setFolder(folder);

		if (message instanceof MimeMessage mime)
		{
			try
			{
				out.setMessageId(mime.getMessageID());
			}
			catch (MessagingException ignored)
			{
				// Some IMAP servers return an ENVELOPE that cannot be parsed; the Message-ID is optional.
			}
		}

		Address[] froms = message.getFrom();
		if (froms != null && froms.length > 0 && froms[0] instanceof InternetAddress ia)
		{
			out.setFrom(MailAddress.of(ia));
		}

		addAddresses(out, message.getReplyTo(), Recipient.REPLY_TO);
		addAddresses(out, message.getRecipients(Message.RecipientType.TO), Recipient.TO);
		addAddresses(out, message.getRecipients(Message.RecipientType.CC), Recipient.CC);
		addAddresses(out, message.getRecipients(Message.RecipientType.BCC), Recipient.BCC);

		out.setSubject(message.getSubject());
		out.setSentDate(toLocalDateTime(message.getSentDate()));
		out.setReceivedDate(toLocalDateTime(message.getReceivedDate()));
		try
		{
			out.setSeen(message.isSet(jakarta.mail.Flags.Flag.SEEN));
		}
		catch (MessagingException ignored)
		{
			// flags may be unavailable on some stores
		}

		walk(message, out);
		return out;
	}

	private enum Recipient
	{REPLY_TO, TO, CC, BCC}

	private static void addAddresses(MailMessage out, Address[] addresses, Recipient type)
	{
		if (addresses == null)
		{
			return;
		}
		for (Address address : addresses)
		{
			if (address instanceof InternetAddress ia)
			{
				MailAddress ma = MailAddress.of(ia);
				switch (type)
				{
					case REPLY_TO -> out.getReplyTo().add(ma);
					case TO -> out.getTo().add(ma);
					case CC -> out.getCc().add(ma);
					case BCC -> out.getBcc().add(ma);
				}
			}
		}
	}

	private static void walk(Part part, MailMessage out) throws MessagingException, IOException
	{
		Object content;
		try
		{
			content = part.getContent();
		}
		catch (IOException e)
		{
			// Unparseable content type — store the raw bytes as an attachment.
			out.addAttachment(toAttachment(part));
			return;
		}

		if (content instanceof Multipart multipart)
		{
			for (int i = 0; i < multipart.getCount(); i++)
			{
				walk(multipart.getBodyPart(i), out);
			}
			return;
		}

		String disposition = part.getDisposition();
		boolean isAttachment = Part.ATTACHMENT.equalsIgnoreCase(disposition)
				|| Part.INLINE.equalsIgnoreCase(disposition)
				|| (part.getFileName() != null && !part.getFileName().isBlank());

		if (content instanceof String text && !isAttachment)
		{
			if (part.isMimeType("text/html"))
			{
				out.setHtmlBody(appendBody(out.getHtmlBody(), text));
			}
			else
			{
				out.setTextBody(appendBody(out.getTextBody(), text));
			}
			return;
		}

		// Anything else (binary stream, or a String marked as an attachment) becomes an attachment.
		out.addAttachment(toAttachment(part));
	}

	private static String appendBody(String existing, String addition)
	{
		if (existing == null || existing.isEmpty())
		{
			return addition;
		}
		return existing + "\n" + addition;
	}

	private static MailAttachment toAttachment(Part part) throws MessagingException, IOException
	{
		MailAttachment attachment = new MailAttachment();
		attachment.setFileName(decode(part.getFileName()));
		attachment.setContentType(stripParameters(part.getContentType()));
		attachment.setInline(Part.INLINE.equalsIgnoreCase(part.getDisposition()));
		if (part instanceof MimeMessage)
		{
			// not expected
		}
		String[] cid = part.getHeader("Content-ID");
		if (cid != null && cid.length > 0)
		{
			attachment.setContentId(cid[0].replaceAll("[<>]", ""));
		}
		try (InputStream is = part.getInputStream())
		{
			attachment.setContent(readAll(is));
		}
		return attachment;
	}

	private static byte[] readAll(InputStream is) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int read;
		while ((read = is.read(buffer)) != -1)
		{
			bos.write(buffer, 0, read);
		}
		return bos.toByteArray();
	}

	private static String stripParameters(String contentType)
	{
		if (contentType == null)
		{
			return null;
		}
		int semi = contentType.indexOf(';');
		return (semi > 0 ? contentType.substring(0, semi) : contentType).trim();
	}

	private static String decode(String name)
	{
		if (name == null)
		{
			return null;
		}
		try
		{
			return jakarta.mail.internet.MimeUtility.decodeText(name);
		}
		catch (Exception e)
		{
			return name;
		}
	}

	private static LocalDateTime toLocalDateTime(Date date)
	{
		return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}
}


