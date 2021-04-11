package com.guicedee.activitymaster.mail.services.dto;

import com.fasterxml.jackson.annotation.*;

import java.util.Objects;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,getterVisibility = NONE,setterVisibility = NONE)
public class MailImportTicket
{
	private UUID arrangementId;
	private String destMailAddress;
	private String destMailPassword;
	private String gmailAddress;
	private String gmailPassword;

	private String status;
	private String lastRunDate;

	private boolean started;
	private boolean paused;
	private boolean destChecked;
	private boolean gmailChecked;

	private long totalMails;
	private long totalFolders;
	private long totalSize;

	private long totalSizeForToday;

	private long completedMails;
	private long completedFolders;
	private long completedSize;

	private String currentFolder;

	public MailImportTicket()
	{
	}

	public UUID getArrangementId()
	{
		return arrangementId;
	}

	public MailImportTicket setArrangementId(UUID arrangementId)
	{
		this.arrangementId = arrangementId;
		return this;
	}

	public String getGoToMailAddress()
	{
		return this.destMailAddress;
	}

	public  String getDestMailPassword()
	{
		return this.destMailPassword;
	}

	public  String getGmailAddress()
	{
		return this.gmailAddress;
	}

	public  String getGmailPassword()
	{
		return this.gmailPassword;
	}

	public String getStatus()
	{
		return status;
	}

	public MailImportTicket setStatus(String status)
	{
		this.status = status;
		return this;
	}

	public boolean isStarted()
	{
		return this.started;
	}

	public boolean isPaused()
	{
		return this.paused;
	}

	public boolean isDestChecked()
	{
		return this.destChecked;
	}

	public boolean isGmailChecked()
	{
		return this.gmailChecked;
	}

	public long getTotalMails()
	{
		return this.totalMails;
	}

	public long getTotalFolders()
	{
		return this.totalFolders;
	}

	public long getTotalSize()
	{
		return this.totalSize;
	}

	public long getTotalSizeForToday()
	{
		return this.totalSizeForToday;
	}

	public long getCompletedMails()
	{
		return this.completedMails;
	}

	public long getCompletedFolders()
	{
		return this.completedFolders;
	}

	public long getCompletedSize()
	{
		return this.completedSize;
	}



	public MailImportTicket setDestMailAddress(String destMailAddress)
	{
		this.destMailAddress = destMailAddress;
		return this;
	}

	public MailImportTicket setDestMailPassword(String destMailPassword)
	{
		this.destMailPassword = destMailPassword;
		return this;
	}

	public MailImportTicket setGmailAddress( String gmailAddress)
	{
		this.gmailAddress = gmailAddress;
		return this;
	}

	public MailImportTicket setGmailPassword( String gmailPassword)
	{
		this.gmailPassword = gmailPassword;
		return this;
	}

	public MailImportTicket setStarted(boolean started)
	{
		this.started = started;
		return this;
	}

	public MailImportTicket setPaused(boolean paused)
	{
		this.paused = paused;
		return this;
	}

	public MailImportTicket setDestChecked(boolean destChecked)
	{
		this.destChecked = destChecked;
		return this;
	}

	public MailImportTicket setGmailChecked(boolean gmailChecked)
	{
		this.gmailChecked = gmailChecked;
		return this;
	}

	public MailImportTicket setTotalMails(long totalMails)
	{
		this.totalMails = totalMails;
		return this;
	}

	public MailImportTicket setTotalFolders(long totalFolders)
	{
		this.totalFolders = totalFolders;
		return this;
	}

	public MailImportTicket setTotalSize(long totalSize)
	{
		this.totalSize = totalSize;
		return this;
	}

	public MailImportTicket setTotalSizeForToday(long totalSizeForToday)
	{
		this.totalSizeForToday = totalSizeForToday;
		return this;
	}

	public MailImportTicket setCompletedMails(long completedMails)
	{
		this.completedMails = completedMails;
		return this;
	}

	public MailImportTicket setCompletedFolders(long completedFolders)
	{
		this.completedFolders = completedFolders;
		return this;
	}

	public MailImportTicket setCompletedSize(long completedSize)
	{
		this.completedSize = completedSize;
		return this;
	}

	public String getLastRunDate()
	{
		return lastRunDate;
	}

	public MailImportTicket setLastRunDate(String lastRunDate)
	{
		this.lastRunDate = lastRunDate;
		return this;
	}

	public String getCurrentFolder()
	{
		return currentFolder;
	}

	public MailImportTicket setCurrentFolder(String currentFolder)
	{
		this.currentFolder = currentFolder;
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
		MailImportTicket that = (MailImportTicket) o;
		return Objects.equals(getGoToMailAddress(), that.getGoToMailAddress()) &&
		       Objects.equals(getGmailAddress(), that.getGmailAddress());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(getGoToMailAddress(), getGmailAddress());
	}
}
