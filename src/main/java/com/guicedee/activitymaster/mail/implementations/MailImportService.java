package com.guicedee.activitymaster.mail.implementations;

import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.core.services.classifications.resourceitems.IResourceItemClassification;
import com.guicedee.activitymaster.core.services.dto.IArrangement;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.security.Passwords;
import com.guicedee.activitymaster.core.services.system.IArrangementsService;
import com.guicedee.activitymaster.core.services.system.IEnterpriseService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications.*;
import static com.guicedee.activitymaster.mail.services.classifications.MailSystemResourceItemClassifications.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

public class MailImportService
		implements IMailImportService<MailImportService>
{
	private static final Logger log = Logger.getLogger(MailImportService.class.getName());

	public MailImportService()
	{
	}

	@Override
	public List<MailImportTicket> fromArrangements(List<IArrangement<?>> arrangements, IEnterpriseName<?> enterpriseName)
	{
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);
		UUID uuid = GuiceContext.get(MailSystem.class)
		                        .getSystemToken(enterpriseName);
		ISystems<?> mailSystem = GuiceContext.get(MailSystem.class)
		                                     .getSystem(enterpriseName);

		List<MailImportTicket> tickets = new ArrayList<>();
		if (arrangements != null)
		{
			for (IArrangement<?> arrangement : arrangements)
			{
				if (!arrangement.hasResourceItems(MailSystemClassifications.MailImport, mailSystem, uuid))
				{
					continue;
				}
				MailImportTicket ticket = new MailImportTicket();

				List<Object[]> rows = arrangement.getValues(MailImport, null, mailSystem, new UUID[]{uuid}
						, CurrentFolderImport
						, CompletedFolderImport
						, CompletedMailImport
						, CompletedSizeImport
						, TotalFoldersForMailImport
						, TotalCountOfMailImport
						, TotalSizeForMailImport
						, CurrentDaySizeOfImport
						, ConfirmedSourceMailImport
						, ConfirmedDestinationMailImport
						, TargetUserNameKey
						, SourceUserNameKey
						, LastDayOfImport);
				if (rows.isEmpty())
				{
					continue;
				}
				Object[] row = rows.get(0);

				ticket.setArrangementId(arrangement.getId());
				MailImportStage stage = MailImportStage.valueOf(row[0].toString());
				ticket.setStatus(stage.classificationName());
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

	@Override
	public ISystems<?> getSystem(IEnterpriseName<?> enterpriseName)
	{
		return get(MailSystem.class).getSystem(enterpriseName);
	}

	@Override
	public UUID getSystemUUID(IEnterpriseName<?> enterpriseName)
	{
		return get(MailSystem.class).getSystemToken(enterpriseName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateMailImportTicket(MailImportTicket mailImportTicket, IEnterpriseName<?> enterpriseName)
	{
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);
		UUID uuid = GuiceContext.get(MailSystem.class)
		                        .getSystemToken(enterpriseName);
		ISystems<?> mailSystem = GuiceContext.get(MailSystem.class)
		                                     .getSystem(enterpriseName);
		IArrangement<?> arrangement = get(IArrangementsService.class).find(mailImportTicket.getArrangementId(), mailSystem.getEnterpriseID(), uuid);

		arrangement.addOrUpdate(ConfirmedSourceMailImport, "" + mailImportTicket.isGmailChecked(), mailSystem);
		arrangement.addOrUpdate(ConfirmedDestinationMailImport, "" + mailImportTicket.isSanrgChecked(), mailSystem);

		arrangement.addOrUpdate(CurrentDayOfImport, "" + mailImportTicket.getLastRunDate(), mailSystem);
		arrangement.addOrUpdate(CurrentDaySizeOfImport, "" + mailImportTicket.getTotalSizeForToday(), mailSystem);

		arrangement.addOrUpdate(CurrentFolderImport, mailImportTicket.getCurrentFolder(), mailSystem);

		arrangement.addOrUpdate(TotalCountOfMailImport, "" + mailImportTicket.getTotalMails(), mailSystem);
		arrangement.addOrUpdate(TotalFoldersForMailImport, "" + mailImportTicket.getTotalFolders(), mailSystem);
		arrangement.addOrUpdate(CompletedMailImport, "" + mailImportTicket.getCompletedMails(), mailSystem);
		arrangement.addOrUpdate(CompletedFolderImport, "" + mailImportTicket.getCompletedFolders(), mailSystem);
		arrangement.addOrUpdate(CompletedSizeImport, "" + mailImportTicket.getCompletedSize(), mailSystem);
		arrangement.addOrUpdate(LastDayOfImport, "" + mailImportTicket.getLastRunDate(), mailSystem);
		arrangement.addOrUpdate(TotalSizeForMailImport, "" + mailImportTicket.getTotalSize(), mailSystem);
	}

	public void updateMailFolderStatus(IArrangement<?> arrangement, MailFoldersStatus foldersStatus, IEnterpriseName<?> enterpriseName)
	{
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);
		UUID identity = GuiceContext.get(MailSystem.class)
		                            .getSystemToken(enterpriseName);
		ISystems<?> mailSystem = GuiceContext.get(MailSystem.class)
		                                     .getSystem(enterpriseName);

		IArrangementsService<?> arrangementsService = get(IArrangementsService.class);
		List output = arrangement.getValues((IResourceItemClassification<?>) FolderStatusObject, foldersStatus.getFolderName(), mailSystem, identity);
		for (Object o : output)
		{

		}
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
