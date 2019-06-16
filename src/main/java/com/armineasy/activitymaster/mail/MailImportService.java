package com.armineasy.activitymaster.mail;

import com.armineasy.activitymaster.activitymaster.services.classifications.enterprise.IEnterpriseName;
import com.armineasy.activitymaster.activitymaster.services.dto.IArrangement;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IRelationshipValue;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.services.security.Passwords;
import com.armineasy.activitymaster.activitymaster.services.system.IArrangementsService;
import com.armineasy.activitymaster.activitymaster.services.system.IEnterpriseService;
import com.armineasy.activitymaster.mail.servers.MailServer;
import com.armineasy.activitymaster.mail.services.IMailImportService;
import com.armineasy.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.armineasy.activitymaster.mail.services.dto.MailImportTicket;
import com.armineasy.activitymaster.mail.services.enumerations.MailImportStage;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static com.armineasy.activitymaster.mail.services.classifications.MailSystemClassifications.*;
import static com.jwebmp.guicedinjection.GuiceContext.*;

@Data
@Accessors(chain = true)
@Log
public class MailImportService
		implements IMailImportService<MailImportService>
{
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
		for (IArrangement<?> arrangement : arrangements)
		{
			if (!arrangement.has(MailSystemClassifications.MailImport, mailSystem, uuid))
			{
				continue;
			}
			MailImportTicket ticket = new MailImportTicket();
			ticket.setArrangementId(arrangement.getId());
			MailImportStage stage = MailImportStage.valueOf(arrangement.find(MailSystemClassifications.MailImport, mailSystem, uuid)
			                                                           .get()
			                                                           .getValue());
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
			ticket.setCompletedFolders(arrangement.find(CompletedFolderImport, mailSystem, uuid)
			                                      .get()
			                                      .getValueAsLong());
			ticket.setCompletedMails(arrangement.find(CompletedMailImport, mailSystem, uuid)
			                                    .get()
			                                    .getValueAsLong());
			ticket.setCompletedSize(arrangement.find(CompletedSizeImport, mailSystem, uuid)
			                                   .get()
			                                   .getValueAsLong());

			ticket.setTotalFolders(arrangement.find(TotalFoldersForMailImport, mailSystem, uuid)
			                                  .get()
			                                  .getValueAsLong());
			ticket.setTotalMails(arrangement.find(TotalCountOfMailImport, mailSystem, uuid)
			                                .get()
			                                .getValueAsLong());
			ticket.setTotalSize(arrangement.find(TotalSizeForMailImport, mailSystem, uuid)
			                               .get()
			                               .getValueAsLong());

			ticket.setTotalSizeForToday(arrangement.find(CurrentDaySizeOfImport, mailSystem, uuid)
			                                       .get()
			                                       .getValueAsNumber());
			ticket.setGmailChecked(arrangement.find(ConfirmedSourceMailImport, mailSystem, uuid)
			                                  .get()
			                                  .getValueAsBoolean());
			ticket.setSanrgChecked(arrangement.find(ConfirmedDestinationMailImport, mailSystem, uuid)
			                                  .get()
			                                  .getValueAsBoolean());
			ticket.setPaused(true);
			ticket.setSanrgMailAddress(new String(Passwords.integerDecrypt(arrangement.find(TargetUserNameKey, mailSystem, uuid)
			                                                                          .get()
			                                                                          .getValue())));
			ticket.setGmailAddress(new String(Passwords.integerDecrypt(arrangement.find(SourceUserNameKey, mailSystem, uuid)
			                                                                      .get()
			                                                                      .getValue())));
			ticket.setLastRunDate(arrangement.find(LastDayOfImport, mailSystem, uuid)
			                                 .orElse(new IRelationshipValue()
			                                 {
				                                 @Override
				                                 public IRelationshipValue setValue(String value)
				                                 {
					                                 return null;
				                                 }

				                                 @Override
				                                 public String getValue()
				                                 {
					                                 return "";
				                                 }
			                                 })
			                                 .getValue());

			tickets.add(ticket);
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

		arrangement.addOrUpdate(MailSystemClassifications.ConfirmedSourceMailImport, "" + mailImportTicket.isGmailChecked(), systems);
		arrangement.addOrUpdate(MailSystemClassifications.ConfirmedDestinationMailImport, "" + mailImportTicket.isSanrgChecked(), systems);

		arrangement.addOrUpdate(MailSystemClassifications.CurrentDayOfImport, "" + mailImportTicket.getLastRunDate(), systems);
		arrangement.addOrUpdate(MailSystemClassifications.CurrentDaySizeOfImport, "" + mailImportTicket.getTotalSizeForToday(), systems);

		arrangement.addOrUpdate(MailSystemClassifications.TotalCountOfMailImport, "" + mailImportTicket.getTotalMails(), systems);
		arrangement.addOrUpdate(MailSystemClassifications.TotalFoldersForMailImport, "" + mailImportTicket.getTotalFolders(), systems);
		arrangement.addOrUpdate(CompletedMailImport, "" + mailImportTicket.getCompletedMails(), systems);
		arrangement.addOrUpdate(CompletedFolderImport, "" + mailImportTicket.getCompletedFolders(), systems);
		arrangement.addOrUpdate(CompletedSizeImport, ""  + mailImportTicket.getCompletedSize(), systems);
		arrangement.addOrUpdate(MailSystemClassifications.TotalSizeForMailImport, "" + mailImportTicket.getTotalSize(), systems);
	}

	public boolean checkCredentials(MailServer server)
	{
		try (MailService mm = new MailService(server))
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


}
