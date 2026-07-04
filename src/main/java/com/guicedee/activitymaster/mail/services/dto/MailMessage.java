package com.guicedee.activitymaster.mail.services.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A transport-agnostic representation of an email message: the envelope (from / reply-to /
 * to / cc / bcc), the content (subject, plain-text and HTML bodies), {@link MailAttachment attachments}
 * and arbitrary headers.
 * <p>
 * Instances are produced when reading a mailbox (see the mailbox service) and consumed when sending
 * (see the transport service). Recipients carry their display names so address <em>aliases</em> are
 * preserved end-to-end.
 */
public class MailMessage implements Serializable
{
	private String messageId;
	private MailAddress from;
	private final List<MailAddress> replyTo = new ArrayList<>();
	private final List<MailAddress> to = new ArrayList<>();
	private final List<MailAddress> cc = new ArrayList<>();
	private final List<MailAddress> bcc = new ArrayList<>();

	private String subject;
	private String textBody;
	private String htmlBody;

	private LocalDateTime sentDate;
	private LocalDateTime receivedDate;
	private String folder;
	private boolean seen;

	private final List<MailAttachment> attachments = new ArrayList<>();
	private final Map<String, String> headers = new LinkedHashMap<>();

	public MailMessage()
	{
	}

	// ---- Fluent helpers ---------------------------------------------------------------------

	public MailMessage from(String address)
	{
		this.from = new MailAddress(address);
		return this;
	}

	public MailMessage from(String personal, String address)
	{
		this.from = new MailAddress(personal, address);
		return this;
	}

	public MailMessage addTo(String address)
	{
		this.to.add(new MailAddress(address));
		return this;
	}

	public MailMessage addTo(String personal, String address)
	{
		this.to.add(new MailAddress(personal, address));
		return this;
	}

	public MailMessage addCc(String address)
	{
		this.cc.add(new MailAddress(address));
		return this;
	}

	public MailMessage addBcc(String address)
	{
		this.bcc.add(new MailAddress(address));
		return this;
	}

	public MailMessage addReplyTo(String address)
	{
		this.replyTo.add(new MailAddress(address));
		return this;
	}

	public MailMessage addAttachment(MailAttachment attachment)
	{
		this.attachments.add(attachment);
		return this;
	}

	public MailMessage addAttachment(String fileName, String contentType, byte[] content)
	{
		this.attachments.add(new MailAttachment(fileName, contentType, content));
		return this;
	}

	public MailMessage addHeader(String name, String value)
	{
		this.headers.put(name, value);
		return this;
	}

	/**
	 * @return every distinct recipient address across To, Cc and Bcc.
	 */
	public Set<MailAddress> getAllRecipients()
	{
		Set<MailAddress> all = new LinkedHashSet<>();
		all.addAll(to);
		all.addAll(cc);
		all.addAll(bcc);
		return all;
	}

	/**
	 * @return every distinct party on the message: the sender plus all recipients.
	 */
	public Set<MailAddress> getAllParties()
	{
		Set<MailAddress> all = new LinkedHashSet<>();
		if (from != null)
		{
			all.add(from);
		}
		all.addAll(getAllRecipients());
		return all;
	}

	/**
	 * @return {@code true} when the message has at least one (non-inline) attachment.
	 */
	public boolean hasAttachments()
	{
		return attachments.stream().anyMatch(a -> !a.isInline());
	}

	// ---- Accessors --------------------------------------------------------------------------

	public String getMessageId()
	{
		return messageId;
	}

	public MailMessage setMessageId(String messageId)
	{
		this.messageId = messageId;
		return this;
	}

	public MailAddress getFrom()
	{
		return from;
	}

	public MailMessage setFrom(MailAddress from)
	{
		this.from = from;
		return this;
	}

	public List<MailAddress> getReplyTo()
	{
		return replyTo;
	}

	public List<MailAddress> getTo()
	{
		return to;
	}

	public List<MailAddress> getCc()
	{
		return cc;
	}

	public List<MailAddress> getBcc()
	{
		return bcc;
	}

	public String getSubject()
	{
		return subject;
	}

	public MailMessage setSubject(String subject)
	{
		this.subject = subject;
		return this;
	}

	public String getTextBody()
	{
		return textBody;
	}

	public MailMessage setTextBody(String textBody)
	{
		this.textBody = textBody;
		return this;
	}

	public String getHtmlBody()
	{
		return htmlBody;
	}

	public MailMessage setHtmlBody(String htmlBody)
	{
		this.htmlBody = htmlBody;
		return this;
	}

	public LocalDateTime getSentDate()
	{
		return sentDate;
	}

	public MailMessage setSentDate(LocalDateTime sentDate)
	{
		this.sentDate = sentDate;
		return this;
	}

	public LocalDateTime getReceivedDate()
	{
		return receivedDate;
	}

	public MailMessage setReceivedDate(LocalDateTime receivedDate)
	{
		this.receivedDate = receivedDate;
		return this;
	}

	public String getFolder()
	{
		return folder;
	}

	public MailMessage setFolder(String folder)
	{
		this.folder = folder;
		return this;
	}

	public boolean isSeen()
	{
		return seen;
	}

	public MailMessage setSeen(boolean seen)
	{
		this.seen = seen;
		return this;
	}

	public List<MailAttachment> getAttachments()
	{
		return attachments;
	}

	public Map<String, String> getHeaders()
	{
		return headers;
	}

	@Override
	public String toString()
	{
		return "MailMessage(from=" + from + ", to=" + to + ", subject=" + subject
				+ ", attachments=" + attachments.size() + ")";
	}
}

