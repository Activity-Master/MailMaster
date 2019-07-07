package com.armineasy.activitymaster.mail.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailFoldersStatus
{
	private int current;
	private int total;

	private String sourceFolderName;
	private String destFolderName;

	public MailFoldersStatus()
	{
		//No config required
	}

	@Override
	public String toString()
	{
		try
		{
			return new ObjectMapper().writeValueAsString(this);
		}
		catch (JsonProcessingException e)
		{
			return "CANNOT SERIALIZE MAIL FOLDER STATUS - " + getFolderName();
		}
	}

	public String getFolderName()
	{
		return sourceFolderName;
	}

	public int getCurrent()
	{
		return current;
	}

	public MailFoldersStatus setCurrent(int current)
	{
		this.current = current;
		return this;
	}

	public int getTotal()
	{
		return total;
	}

	public MailFoldersStatus setTotal(int total)
	{
		this.total = total;
		return this;
	}

	public String getSourceFolderName()
	{
		return sourceFolderName;
	}

	public MailFoldersStatus setSourceFolderName(String sourceFolderName)
	{
		this.sourceFolderName = sourceFolderName;
		return this;
	}

	public String getDestFolderName()
	{
		return destFolderName;
	}

	public MailFoldersStatus setDestFolderName(String destFolderName)
	{
		this.destFolderName = destFolderName;
		return this;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		MailFoldersStatus that = (MailFoldersStatus) o;
		return Objects.equals(getFolderName(), that.getFolderName());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(getFolderName());
	}
}
