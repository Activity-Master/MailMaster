package com.guicedee.activitymaster.mail.engine;

import com.guicedee.activitymaster.mail.services.dto.MailAddress;
import com.guicedee.activitymaster.mail.services.dto.MailAttachment;
import com.guicedee.activitymaster.mail.services.dto.MailMessage;
import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Builds a Jakarta {@link MimeMessage} from a {@link MailMessage} DTO.
 * <p>
 * Handles the full structure: a {@code multipart/alternative} for the text + HTML bodies, wrapped in
 * a {@code multipart/related} when inline parts are present, wrapped in a {@code multipart/mixed} when
 * file attachments are present. Display names (aliases) on every address are preserved.
 */
public final class MailMimeBuilder
{
	private MailMimeBuilder()
	{
	}

	/**
	 * Builds a {@link MimeMessage} for the supplied DTO using the given session.
	 *
	 * @param session the Jakarta Mail session
	 * @param message the message DTO to render
	 * @return the assembled MIME message
	 * @throws MessagingException           on a mail protocol error
	 * @throws UnsupportedEncodingException when an address personal name cannot be encoded
	 * @throws IOException                  when building an attachment data source fails
	 */
	public static MimeMessage build(Session session, MailMessage message)
			throws MessagingException, UnsupportedEncodingException, IOException
	{
		MimeMessage mime = new MimeMessage(session);

		if (message.getFrom() != null)
		{
			mime.setFrom(message.getFrom().toInternetAddress());
		}
		if (!message.getReplyTo().isEmpty())
		{
			mime.setReplyTo(toArray(message.getReplyTo()));
		}
		mime.setRecipients(Message.RecipientType.TO, toArray(message.getTo()));
		mime.setRecipients(Message.RecipientType.CC, toArray(message.getCc()));
		mime.setRecipients(Message.RecipientType.BCC, toArray(message.getBcc()));
		mime.setSubject(message.getSubject() == null ? "" : message.getSubject(), "UTF-8");

		LocalDateTime sent = message.getSentDate() != null ? message.getSentDate() : LocalDateTime.now();
		mime.setSentDate(Date.from(sent.atZone(ZoneId.systemDefault()).toInstant()));

		for (Map.Entry<String, String> header : message.getHeaders().entrySet())
		{
			mime.setHeader(header.getKey(), header.getValue());
		}

		applyContent(mime, message);
		mime.saveChanges();
		return mime;
	}

	private static void applyContent(MimeMessage mime, MailMessage message)
			throws MessagingException, IOException
	{
		MimeBodyPart bodyPart = buildBody(message);

		List<MailAttachment> attachments = message.getAttachments();
		boolean hasInline = attachments.stream().anyMatch(MailAttachment::isInline);
		boolean hasFiles = attachments.stream().anyMatch(a -> !a.isInline());

		if (attachments.isEmpty())
		{
			// Simple body — copy the assembled body part's content onto the message.
			mime.setContent(bodyPart.getContent(), bodyPart.getContentType());
			return;
		}

		MimeBodyPart contentPart = bodyPart;
		if (hasInline)
		{
			MimeMultipart related = new MimeMultipart("related");
			related.addBodyPart(bodyPart);
			for (MailAttachment attachment : attachments)
			{
				if (attachment.isInline())
				{
					related.addBodyPart(toBodyPart(attachment));
				}
			}
			contentPart = new MimeBodyPart();
			contentPart.setContent(related);
		}

		if (hasFiles)
		{
			MimeMultipart mixed = new MimeMultipart("mixed");
			mixed.addBodyPart(contentPart);
			for (MailAttachment attachment : attachments)
			{
				if (!attachment.isInline())
				{
					mixed.addBodyPart(toBodyPart(attachment));
				}
			}
			mime.setContent(mixed);
		}
		else
		{
			mime.setContent((MimeMultipart) contentPart.getContent());
		}
	}

	private static MimeBodyPart buildBody(MailMessage message) throws MessagingException
	{
		MimeBodyPart bodyPart = new MimeBodyPart();
		boolean hasText = message.getTextBody() != null && !message.getTextBody().isEmpty();
		boolean hasHtml = message.getHtmlBody() != null && !message.getHtmlBody().isEmpty();

		if (hasText && hasHtml)
		{
			MimeMultipart alternative = new MimeMultipart("alternative");
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText(message.getTextBody(), "UTF-8");
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(message.getHtmlBody(), "text/html; charset=UTF-8");
			alternative.addBodyPart(textPart);
			alternative.addBodyPart(htmlPart);
			bodyPart.setContent(alternative);
		}
		else if (hasHtml)
		{
			bodyPart.setContent(message.getHtmlBody(), "text/html; charset=UTF-8");
		}
		else
		{
			bodyPart.setText(message.getTextBody() == null ? "" : message.getTextBody(), "UTF-8");
		}
		return bodyPart;
	}

	private static MimeBodyPart toBodyPart(MailAttachment attachment) throws MessagingException, IOException
	{
		MimeBodyPart part = new MimeBodyPart();
		String contentType = attachment.getContentType() == null ? "application/octet-stream" : attachment.getContentType();
		ByteArrayDataSource source = new ByteArrayDataSource(
				attachment.getContent() == null ? new byte[0] : attachment.getContent(), contentType);
		part.setDataHandler(new DataHandler(source));
		if (attachment.getFileName() != null)
		{
			part.setFileName(attachment.getFileName());
		}
		if (attachment.isInline())
		{
			part.setDisposition(MimeBodyPart.INLINE);
			if (attachment.getContentId() != null)
			{
				part.setContentID("<" + attachment.getContentId() + ">");
			}
		}
		else
		{
			part.setDisposition(MimeBodyPart.ATTACHMENT);
		}
		return part;
	}

	private static InternetAddress[] toArray(List<MailAddress> addresses)
			throws MessagingException, UnsupportedEncodingException
	{
		InternetAddress[] out = new InternetAddress[addresses.size()];
		for (int i = 0; i < addresses.size(); i++)
		{
			out[i] = addresses.get(i).toInternetAddress();
		}
		return out;
	}
}

