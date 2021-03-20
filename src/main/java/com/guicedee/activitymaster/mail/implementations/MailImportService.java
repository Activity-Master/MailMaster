package com.guicedee.activitymaster.mail.implementations;

import com.guicedee.activitymaster.client.implementations.Passwords;
import com.guicedee.activitymaster.client.services.IArrangementsService;
import com.guicedee.activitymaster.client.services.IEnterpriseService;
import com.guicedee.activitymaster.client.services.builders.warehouse.arrangements.IArrangement;
import com.guicedee.activitymaster.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.mail.MailSystem;
import com.guicedee.activitymaster.mail.servers.MailServer;
import com.guicedee.activitymaster.mail.services.IMailImportService;
import com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.guicedee.activitymaster.mail.services.dto.MailFoldersStatus;
import com.guicedee.activitymaster.mail.services.dto.MailImportTicket;
import com.guicedee.activitymaster.mail.services.enumerations.MailImportStage;
import com.guicedee.guicedinjection.GuiceContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

public class MailImportService
		implements IMailImportService<MailImportService>
{
	private static final Logger log = Logger.getLogger(MailImportService.class.getName());

	public MailImportService()
	{
	}

	@Override
	public List<MailImportTicket> fromArrangements(List<IArrangement<?,?>> arrangements, String enterpriseName)
	{
		IEnterprise<?,?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);
		UUID uuid = GuiceContext.get(MailSystem.class)
		                        .getSystemToken(enterpriseName);
		ISystems<?,?> mailSystem = GuiceContext.get(MailSystem.class)
		                                     .getSystem(enterpriseName);

		List<MailImportTicket> tickets = new ArrayList<>();
		if (arrangements != null)
		{
			for (IArrangement<?,?> arrangement : arrangements)
			{
				if (!arrangement.hasResourceItems(MailSystemClassifications.MailImport.toString(),null, mailSystem, uuid))
				{
					continue;
				}
				MailImportTicket ticket = new MailImportTicket();

				List<Object[]> rows = arrangement.builder().getClassificationsValuePivot(MailImport.toString(), (String)null, mailSystem, new UUID[]{uuid}
						, CurrentFolderImport.toString()
						, CompletedFolderImport.toString()
						, CompletedMailImport.toString()
						, CompletedSizeImport.toString()
						, TotalFoldersForMailImport.toString()
						, TotalCountOfMailImport.toString()
						, TotalSizeForMailImport.toString()
						, CurrentDaySizeOfImport.toString()
						, ConfirmedSourceMailImport.toString()
						, ConfirmedDestinationMailImport.toString()
						, TargetUserNameKey.toString()
						, SourceUserNameKey.toString()
						, LastDayOfImport.toString());
				if (rows.isEmpty())
				{
					continue;
				}
				Object[] row = rows.get(0);

				ticket.setArrangementId(arrangement.getId());
				MailImportStage stage = MailImportStage.valueOf(row[0].toString());
				ticket.setStatus(stage.toString());
				switch (stage)
				{
					case MailImportLoginError:
					case MailImportNotStarted:
					case MailImportCompleted:
						ticket.setStarted(false);
						break;
					case MailImportInProgress:
						ticket.setStarted(true);
						break;
				}

				try
				{
					ticket.setCurrentFolder(row[1].toString());
				}
				catch (Exception e)
				{
					ticket.setCompletedFolders(-1);
				}

				try
				{
					ticket.setCompletedFolders(Long.valueOf(row[2].toString()));
				}
				catch (Exception e)
				{
					ticket.setCompletedFolders(-1);
				}
				try
				{
					ticket.setCompletedMails(Long.valueOf(row[3].toString()));
				}
				catch (Exception e)
				{
					ticket.setCompletedMails(0);
				}

				try
				{
					ticket.setCompletedSize(Long.valueOf(row[4].toString()));
				}
				catch (Exception e)
				{
					ticket.setCompletedSize(0);
				}

				try
				{
					ticket.setTotalFolders(Long.valueOf(row[5].toString()));
				}
				catch (Exception e)
				{
					ticket.setTotalFolders(0);
				}
				try
				{
					ticket.setTotalMails(Long.valueOf(row[6].toString()));
				}
				catch (Exception e)
				{
					ticket.setTotalMails(0);
				}
				try
				{
					ticket.setTotalSize(Long.valueOf(row[7].toString()));
				}
				catch (Exception e)
				{
					ticket.setTotalSize(0);
				}

				try
				{
					ticket.setTotalSizeForToday(Long.valueOf(row[8].toString()));
				}
				catch (Exception e)
				{
					ticket.setTotalSizeForToday(0);
				}

				try
				{
					ticket.setGmailChecked(Boolean.valueOf(row[9].toString()));
				}
				catch (Exception e)
				{
					ticket.setGmailChecked(false);
				}

				try
				{
					ticket.setSanrgChecked(Boolean.valueOf(row[10].toString()));
				}
				catch (Exception e)
				{
					ticket.setSanrgChecked(false);
				}

				ticket.setPaused(true);
				try
				{
					ticket.setSanrgMailAddress(new String(new Passwords().integerDecrypt(row[11].toString())));
				}
				catch (Exception e)
				{
					ticket.setSanrgMailAddress("In Progress");
				}
				try
				{
					ticket.setGmailAddress(new String(new Passwords().integerDecrypt(row[12].toString())));
				}
				catch (Exception e)
				{
					ticket.setGmailAddress("In Progress");
				}
				try
				{
					ticket.setLastRunDate(row[13].toString());
				}
				catch (Exception e)
				{
					ticket.setLastRunDate(LocalDateTime.now()
					                                   .format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss.SSS")));
				}
				tickets.add(ticket);
			}
		}

		return tickets;
	}
	
	public ISystems<?,?> getSystem(String enterpriseName)
	{
		return get(MailSystem.class).getSystem(enterpriseName);
	}


	public UUID getSystemUUID(String enterpriseName)
	{
		return get(MailSystem.class).getSystemToken(enterpriseName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateMailImportTicket(MailImportTicket mailImportTicket, String enterpriseName)
	{
		IEnterprise<?,?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);
		UUID uuid = GuiceContext.get(MailSystem.class)
		                        .getSystemToken(enterpriseName);
		ISystems<?,?> mailSystem = GuiceContext.get(MailSystem.class)
		                                     .getSystem(enterpriseName);
		IArrangement<?,?> arrangement = get(IArrangementsService.class).find(mailImportTicket.getArrangementId(), mailSystem, uuid);

		arrangement.addOrUpdateClassification(ConfirmedSourceMailImport.toString(), "" + mailImportTicket.isGmailChecked(), mailSystem);
		arrangement.addOrUpdateClassification(ConfirmedDestinationMailImport.toString(), "" + mailImportTicket.isSanrgChecked(), mailSystem);

		arrangement.addOrUpdateClassification(CurrentDayOfImport.toString(), "" + mailImportTicket.getLastRunDate(), mailSystem);
		arrangement.addOrUpdateClassification(CurrentDaySizeOfImport.toString(), "" + mailImportTicket.getTotalSizeForToday(), mailSystem);

		arrangement.addOrUpdateClassification(CurrentFolderImport.toString(), mailImportTicket.getCurrentFolder(), mailSystem);

		arrangement.addOrUpdateClassification(TotalCountOfMailImport.toString(), "" + mailImportTicket.getTotalMails(), mailSystem);
		arrangement.addOrUpdateClassification(TotalFoldersForMailImport.toString(), "" + mailImportTicket.getTotalFolders(), mailSystem);
		arrangement.addOrUpdateClassification(CompletedMailImport.toString(), "" + mailImportTicket.getCompletedMails(), mailSystem);
		arrangement.addOrUpdateClassification(CompletedFolderImport.toString(), "" + mailImportTicket.getCompletedFolders(), mailSystem);
		arrangement.addOrUpdateClassification(CompletedSizeImport.toString(), "" + mailImportTicket.getCompletedSize(), mailSystem);
		arrangement.addOrUpdateClassification(LastDayOfImport.toString(), "" + mailImportTicket.getLastRunDate(), mailSystem);
		arrangement.addOrUpdateClassification(TotalSizeForMailImport.toString(), "" + mailImportTicket.getTotalSize(), mailSystem);
	}

	public void updateMailFolderStatus(IArrangement<?,?> arrangement, MailFoldersStatus foldersStatus, String enterpriseName)
	{
		IEnterprise<?,?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);
		UUID identity = GuiceContext.get(MailSystem.class)
		                            .getSystemToken(enterpriseName);
		ISystems<?,?> mailSystem = GuiceContext.get(MailSystem.class)
		                                     .getSystem(enterpriseName);
	}

	public boolean checkCredentials(MailServer server)
	{
		try (MailboxBoxService mm = new MailboxBoxService(server))
		{
			mm.login();
			return true;
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE, "Server not verified", e);
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		int result = 1;
		return result;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (!(o instanceof MailImportService))
		{
			return false;
		}
		final MailImportService other = (MailImportService) o;
		if (!other.canEqual((Object) this))
		{
			return false;
		}
		return true;
	}

	protected boolean canEqual(final Object other)
	{
		return other instanceof MailImportService;
	}

	@Override
	public String toString()
	{
		return "MailImportService()";
	}
}
