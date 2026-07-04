package com.guicedee.activitymaster.mail.services.classifications;

/**
 * Classifications contributed by the Mail Master system, used as relationship roles and metadata
 * fields when mapping mail messages onto the FSDM warehouse (events, involved parties, resource
 * items and arrangements).
 * <p>
 * All mail classifications are created under a single, Mail-specific concept so the names never
 * collide with classifications from other systems.
 */
public enum MailClassifications
{
	/** Event metadata: the direction of the message (Inbound/Outbound). */
	MailDirection("The direction of a mail event (Inbound/Outbound)"),
	/** Event / resource metadata: the RFC 822 Message-ID. */
	MailMessageId("The RFC 822 Message-ID of a mail message"),
	/** Event / resource metadata: the subject line. */
	MailSubject("The subject of a mail message"),
	/** Event / resource metadata: the originating folder name. */
	MailFolder("The mailbox folder a message belongs to"),
	/** Event metadata: the sent date/time (ISO-8601). */
	MailSentDate("The sent date of a mail message"),
	/** Resource metadata: the MIME content type of a message body or attachment. */
	MailContentType("The MIME content type of a mail body or attachment"),
	/** Resource metadata: the attachment file name. */
	MailFileName("The file name of a mail attachment"),
	/** Event metadata: whether the message carries attachments. */
	MailHasAttachments("Whether a mail message has attachments"),

	/** Involved-party role: the sender of a message (linked by email address). */
	MailSender("The sender of a mail message"),
	/** Involved-party role: a recipient of a message (To/Cc/Bcc, linked by email address). */
	MailRecipient("A recipient of a mail message"),
	/** Involved-party role: the owner of a mailbox. */
	MailboxOwner("The owner of a mailbox"),

	/** Resource-link role for the stored message body. */
	MailMessageLink("Links a mail event to its stored message body"),
	/** Resource-link role for a stored attachment. */
	MailAttachmentLink("Links a mail event to a stored attachment"),
	;

	private final String description;

	MailClassifications(String description)
	{
		this.description = description;
	}

	public String classificationDescription()
	{
		return description;
	}
}

