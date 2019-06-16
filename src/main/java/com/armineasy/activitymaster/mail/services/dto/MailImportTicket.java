package com.armineasy.activitymaster.mail.services.dto;

import com.armineasy.activitymaster.mail.MailService;
import com.armineasy.activitymaster.mail.servers.GMailMailServer;
import com.armineasy.activitymaster.mail.servers.SaNrgMailServer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.experimental.Accessors;

import javax.mail.MessagingException;
import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;

@Accessors(chain=true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,getterVisibility = NONE,setterVisibility = NONE)
public class MailImportTicket
{
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private long arrangementId;
	private String sanrgMailAddress;
	private String sanrgMailPassword;
	private String gmailAddress;
	private String gmailPassword;

	private String status;
	private String lastRunDate;

	private boolean started;
	private boolean paused;
	private boolean sanrgChecked;
	private boolean gmailChecked;

	private long totalMails;
	private long totalFolders;
	private long totalSize;

	private long totalSizeForToday;

	private long completedMails;
	private long completedFolders;
	private long completedSize;

	public MailImportTicket()
	{
	}

	public boolean verifySaNrgLogin()
	{
		try(MailService ms = new MailService(new SaNrgMailServer(sanrgMailAddress,sanrgMailPassword)))
		{
			ms.login();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}


	public boolean verifyGoogleLogin()
	{
		try(MailService ms = new MailService(new GMailMailServer(gmailAddress, gmailPassword)))
		{
			ms.login();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}


	public void loadAllFields()
	{
		try(MailService ms = new MailService(new GMailMailServer(gmailAddress, gmailPassword)))
		{
			ms.login();
			ms.loadFolders();
			totalMails = ms.getTotalMails();
			totalFolders = ms.getTotalFolders();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (MessagingException e)
		{
			e.printStackTrace();
		}
	}

	public long getArrangementId()
	{
		return arrangementId;
	}

	public MailImportTicket setArrangementId(long arrangementId)
	{
		this.arrangementId = arrangementId;
		return this;
	}

	public String getSanrgMailAddress()
	{
		return this.sanrgMailAddress;
	}

	public  String getSanrgMailPassword()
	{
		return this.sanrgMailPassword;
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

	public boolean isSanrgChecked()
	{
		return this.sanrgChecked;
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



	public MailImportTicket setSanrgMailAddress( String sanrgMailAddress)
	{
		this.sanrgMailAddress = sanrgMailAddress;
		return this;
	}

	public MailImportTicket setSanrgMailPassword( String sanrgMailPassword)
	{
		this.sanrgMailPassword = sanrgMailPassword;
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

	public MailImportTicket setSanrgChecked(boolean sanrgChecked)
	{
		this.sanrgChecked = sanrgChecked;
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

	public boolean equals(final Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (!(o instanceof MailImportTicket))
		{
			return false;
		}
		final MailImportTicket other = (MailImportTicket) o;
		if (!other.canEqual((Object) this))
		{
			return false;
		}
		final Object this$sanrgMailAddress = this.getSanrgMailAddress();
		final Object other$sanrgMailAddress = other.getSanrgMailAddress();
		if (this$sanrgMailAddress == null ? other$sanrgMailAddress != null : !this$sanrgMailAddress.equals(other$sanrgMailAddress))
		{
			return false;
		}
		final Object this$sanrgMailPassword = this.getSanrgMailPassword();
		final Object other$sanrgMailPassword = other.getSanrgMailPassword();
		if (this$sanrgMailPassword == null ? other$sanrgMailPassword != null : !this$sanrgMailPassword.equals(other$sanrgMailPassword))
		{
			return false;
		}
		final Object this$gmailAddress = this.getGmailAddress();
		final Object other$gmailAddress = other.getGmailAddress();
		if (this$gmailAddress == null ? other$gmailAddress != null : !this$gmailAddress.equals(other$gmailAddress))
		{
			return false;
		}
		final Object this$gmailPassword = this.getGmailPassword();
		final Object other$gmailPassword = other.getGmailPassword();
		if (this$gmailPassword == null ? other$gmailPassword != null : !this$gmailPassword.equals(other$gmailPassword))
		{
			return false;
		}
		if (this.isStarted() != other.isStarted())
		{
			return false;
		}
		if (this.isPaused() != other.isPaused())
		{
			return false;
		}
		if (this.isSanrgChecked() != other.isSanrgChecked())
		{
			return false;
		}
		if (this.isGmailChecked() != other.isGmailChecked())
		{
			return false;
		}
		if (this.getTotalMails() != other.getTotalMails())
		{
			return false;
		}
		if (this.getTotalFolders() != other.getTotalFolders())
		{
			return false;
		}
		if (this.getTotalSize() != other.getTotalSize())
		{
			return false;
		}
		if (this.getTotalSizeForToday() != other.getTotalSizeForToday())
		{
			return false;
		}
		if (this.getCompletedMails() != other.getCompletedMails())
		{
			return false;
		}
		if (this.getCompletedFolders() != other.getCompletedFolders())
		{
			return false;
		}
		if (this.getCompletedSize() != other.getCompletedSize())
		{
			return false;
		}
		return true;
	}

	protected boolean canEqual(final Object other)
	{
		return other instanceof MailImportTicket;
	}

	public int hashCode()
	{
		final int PRIME = 59;
		int result = 1;
		final Object $sanrgMailAddress = this.getSanrgMailAddress();
		result = result * PRIME + ($sanrgMailAddress == null ? 43 : $sanrgMailAddress.hashCode());
		final Object $sanrgMailPassword = this.getSanrgMailPassword();
		result = result * PRIME + ($sanrgMailPassword == null ? 43 : $sanrgMailPassword.hashCode());
		final Object $gmailAddress = this.getGmailAddress();
		result = result * PRIME + ($gmailAddress == null ? 43 : $gmailAddress.hashCode());
		final Object $gmailPassword = this.getGmailPassword();
		result = result * PRIME + ($gmailPassword == null ? 43 : $gmailPassword.hashCode());
		result = result * PRIME + (this.isStarted() ? 79 : 97);
		result = result * PRIME + (this.isPaused() ? 79 : 97);
		result = result * PRIME + (this.isSanrgChecked() ? 79 : 97);
		result = result * PRIME + (this.isGmailChecked() ? 79 : 97);
		final long $totalMails = this.getTotalMails();
		result = result * PRIME + (int) ($totalMails >>> 32 ^ $totalMails);
		final long $totalFolders = this.getTotalFolders();
		result = result * PRIME + (int) ($totalFolders >>> 32 ^ $totalFolders);
		final long $totalSize = this.getTotalSize();
		result = result * PRIME + (int) ($totalSize >>> 32 ^ $totalSize);
		final long $totalSizeForToday = this.getTotalSizeForToday();
		result = result * PRIME + (int) ($totalSizeForToday >>> 32 ^ $totalSizeForToday);
		final long $completedMails = this.getCompletedMails();
		result = result * PRIME + (int) ($completedMails >>> 32 ^ $completedMails);
		final long $completedFolders = this.getCompletedFolders();
		result = result * PRIME + (int) ($completedFolders >>> 32 ^ $completedFolders);
		final long $completedSize = this.getCompletedSize();
		result = result * PRIME + (int) ($completedSize >>> 32 ^ $completedSize);
		return result;
	}

	public String toString()
	{
		return "MailImportTicket(sanrgMailAddress=" + this.getSanrgMailAddress() + ", sanrgMailPassword=" + this.getSanrgMailPassword() + ", gmailAddress=" +
		       this.getGmailAddress() + ", gmailPassword=" + this.getGmailPassword() + ", started=" + this.isStarted() + ", paused=" + this.isPaused() + ", sanrgChecked=" +
		       this.isSanrgChecked() + ", gmailChecked=" + this.isGmailChecked() + ", totalMails=" + this.getTotalMails() + ", totalFolders=" + this.getTotalFolders() +
		       ", totalSize=" + this.getTotalSize() + ", totalSizeForToday=" + this.getTotalSizeForToday() + ", completedMails=" + this.getCompletedMails() +
		       ", completedFolders=" + this.getCompletedFolders() + ", completedSize=" + this.getCompletedSize() + ")";
	}
}
