package com.armineasy.activitymaster.mail.services.dto;

import java.util.Objects;

public class MailFoldersStatus
{
	private String folderName;
	private int current;
	private int total;

	public MailFoldersStatus()
	{
		//No config required
	}

	public String getFolderName()
	{
		return folderName;
	}

	public MailFoldersStatus setFolderName(String folderName)
	{
		this.folderName = folderName;
		return this;
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
