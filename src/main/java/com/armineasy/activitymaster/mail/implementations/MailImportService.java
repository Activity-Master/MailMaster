package com.armineasy.activitymaster.mail.implementations;

import com.armineasy.activitymaster.activitymaster.services.classifications.enterprise.IEnterpriseName;
import com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.IResourceItemClassification;
import com.armineasy.activitymaster.activitymaster.services.dto.IArrangement;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IRelationshipValue;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.services.security.Passwords;
import com.armineasy.activitymaster.activitymaster.services.system.IArrangementsService;
import com.armineasy.activitymaster.activitymaster.services.system.IEnterpriseService;
import com.armineasy.activitymaster.mail.MailSystem;
import com.armineasy.activitymaster.mail.servers.MailServer;
import com.armineasy.activitymaster.mail.services.IMailImportService;
import com.armineasy.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.armineasy.activitymaster.mail.services.dto.MailFoldersStatus;
import com.armineasy.activitymaster.mail.services.dto.MailImportTicket;
import com.armineasy.activitymaster.mail.services.enumerations.MailImportStage;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.armineasy.activitymaster.mail.services.classifications.MailSystemClassifications.*;
import static com.armineasy.activitymaster.mail.services.classifications.MailSystemResourceItemClassifications.*;
import static com.armineasy.activitymaster.mail.services.enumerations.MailImportResourceItemTypes.*;
import static com.jwebmp.guicedinjection.GuiceContext.*;

@Accessors(chain = true)
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
		ISystems<?> mailSystem = MailSystem.getNewSystem()
		                                   .get(enterprise);
		UUID uuid = MailSystem.getSystemTokens()
		                      .get(enterprise);

		List<MailImportTicket> tickets = new ArrayList<>();
		if (arrangements != null)
		{
			for (IArrangement<?> arrangement : arrangements)
			{
				if (!arrangement.has(MailSystemClassifications.MailImport, mailSystem, uuid))
				{
					continue;
				}
				MailImportTicket ticket = new MailImportTicket();

				List<Object[]> rows = arrangement.getValues(MailImport, null,mailSystem, uuid
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
				if(rows.isEmpty())
					continue;
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
					ticket.setSanrgMailAddress(new String(Passwords.integerDecrypt(row[11].toString())));
				}
				catch (Exception e)
				{
					ticket.setSanrgMailAddress("In Progress");
				}
				try
				{
					ticket.setGmailAddress(new String(Passwords.integerDecrypt(row[12].toString())));
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
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);
		return MailSystem.getNewSystem()
		                 .get(enterprise);
	}

	@Override
	public UUID getSystemUUID(IEnterpriseName<?> enterpriseName)
	{
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);
		return MailSystem.getSystemTokens()
		                 .get(enterprise);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateMailImportTicket(MailImportTicket mailImportTicket, IEnterpriseName<?> enterpriseName)
	{
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);
		ISystems<?> systems = MailSystem.getNewSystem()
		                                .get(enterprise);
		UUID uuid = MailSystem.getSystemTokens()
		                      .get(enterprise);

		IArrangement<?> arrangement = get(IArrangementsService.class).find(mailImportTicket.getArrangementId(), systems.getEnterpriseID(), uuid);

		arrangement.addOrUpdate(ConfirmedSourceMailImport, "" + mailImportTicket.isGmailChecked(), systems);
		arrangement.addOrUpdate(ConfirmedDestinationMailImport, "" + mailImportTicket.isSanrgChecked(), systems);

		arrangement.addOrUpdate(CurrentDayOfImport, "" + mailImportTicket.getLastRunDate(), systems);
		arrangement.addOrUpdate(CurrentDaySizeOfImport, "" + mailImportTicket.getTotalSizeForToday(), systems);

		arrangement.addOrUpdate(CurrentFolderImport, mailImportTicket.getCurrentFolder(), systems);

		arrangement.addOrUpdate(TotalCountOfMailImport, "" + mailImportTicket.getTotalMails(), systems);
		arrangement.addOrUpdate(TotalFoldersForMailImport, "" + mailImportTicket.getTotalFolders(), systems);
		arrangement.addOrUpdate(CompletedMailImport, "" + mailImportTicket.getCompletedMails(), systems);
		arrangement.addOrUpdate(CompletedFolderImport, "" + mailImportTicket.getCompletedFolders(), systems);
		arrangement.addOrUpdate(CompletedSizeImport, "" + mailImportTicket.getCompletedSize(), systems);
		arrangement.addOrUpdate(LastDayOfImport, "" + mailImportTicket.getLastRunDate(), systems);
		arrangement.addOrUpdate(TotalSizeForMailImport, "" + mailImportTicket.getTotalSize(), systems);
	}

	public void updateMailFolderStatus(IArrangement<?> arrangement, MailFoldersStatus foldersStatus, IEnterpriseName<?> enterpriseName)
	{
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);
		ISystems<?> systems = MailSystem.getNewSystem()
		                                .get(enterprise);
		UUID uuid = MailSystem.getSystemTokens()
		                      .get(enterprise);

		IArrangementsService<?> arrangementsService = get(IArrangementsService.class);
		List output = arrangement.getValues((IResourceItemClassification<?>) FolderStatusObject, foldersStatus.getFolderName(), systems, uuid);
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

	public int hashCode()
	{
		int result = 1;
		return result;
	}

	public String toString()
	{
		return "MailImportService()";
	}
}
