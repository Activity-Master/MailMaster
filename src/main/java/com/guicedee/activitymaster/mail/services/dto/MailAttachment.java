package com.guicedee.activitymaster.mail.services.dto;

import java.io.Serializable;

/**
 * An email attachment (or inline body part), carrying its bytes, file name and content type.
 */
public class MailAttachment implements Serializable
{
	private String fileName;
	private String contentType;
	private byte[] content;
	/** {@code true} when the part is referenced inline in the HTML body (Content-ID). */
	private boolean inline;
	/** The Content-ID used to reference an inline part from HTML ({@code cid:...}). */
	private String contentId;

	public MailAttachment()
	{
	}

	public MailAttachment(String fileName, String contentType, byte[] content)
	{
		this.fileName = fileName;
		this.contentType = contentType;
		this.content = content;
	}

	/**
	 * @return the attachment size in bytes (0 when empty).
	 */
	public long getSize()
	{
		return content == null ? 0 : content.length;
	}

	public String getFileName()
	{
		return fileName;
	}

	public MailAttachment setFileName(String fileName)
	{
		this.fileName = fileName;
		return this;
	}

	public String getContentType()
	{
		return contentType;
	}

	public MailAttachment setContentType(String contentType)
	{
		this.contentType = contentType;
		return this;
	}

	public byte[] getContent()
	{
		return content;
	}

	public MailAttachment setContent(byte[] content)
	{
		this.content = content;
		return this;
	}

	public boolean isInline()
	{
		return inline;
	}

	public MailAttachment setInline(boolean inline)
	{
		this.inline = inline;
		return this;
	}

	public String getContentId()
	{
		return contentId;
	}

	public MailAttachment setContentId(String contentId)
	{
		this.contentId = contentId;
		return this;
	}

	@Override
	public String toString()
	{
		return "MailAttachment(fileName=" + fileName + ", contentType=" + contentType + ", size=" + getSize()
				+ ", inline=" + inline + ")";
	}
}

