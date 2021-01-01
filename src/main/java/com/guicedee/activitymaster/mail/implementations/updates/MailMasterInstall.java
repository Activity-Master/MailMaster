package com.guicedee.activitymaster.mail.implementations.updates;

import com.guicedee.activitymaster.core.services.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IArrangementsService;
import com.guicedee.activitymaster.core.services.system.IClassificationService;
import com.guicedee.activitymaster.core.services.system.IResourceItemService;
import com.guicedee.activitymaster.core.services.system.ISystemsService;
import com.guicedee.activitymaster.core.updates.DatedUpdate;
import com.guicedee.activitymaster.core.updates.ISystemUpdate;
import com.guicedee.activitymaster.mail.MailSystem;
import com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.guicedee.activitymaster.mail.services.enumerations.MailImportArrangementTypes;
import com.guicedee.activitymaster.mail.services.enumerations.MailImportStage;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.interfaces.JobService;
import com.guicedee.logger.LogFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications.*;
import static com.guicedee.activitymaster.mail.services.classifications.MailSystemResourceItemClassifications.FolderStatusObject;
import static com.guicedee.activitymaster.mail.services.enumerations.MailImportResourceItemTypes.FolderStatusResourceItem;
import static com.guicedee.guicedinjection.GuiceContext.get;

@DatedUpdate(date = "2020/12/15", taskCount = 6)
public class MailMasterInstall implements ISystemUpdate
{
	@Override
	public void update(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		createClassifications(enterprise, progressMonitor);
	}
	
	@SuppressWarnings("Duplicates")
	private void createClassifications(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		IClassificationService<?> classificationService = get(IClassificationService.class);
		ISystems<?> activityMasterSystem = get(ISystemsService.class)
				.getActivityMaster(enterprise);
		MailSystem systemM = GuiceContext.get(MailSystem.class);
		ISystems<?> system = systemM.getSystem(enterprise);
		UUID token = systemM.getSystemToken(enterprise);
		IArrangementsService<?> arrangementsService = get(IArrangementsService.class);
		
		try
		{
			classificationService.find(MailImport, system, token);
		}
		catch (Exception e)
		{
			logProgress("Mail Master", "Creating Mail Import Fields", progressMonitor);
			
			LogFactory.getLog("MailSystem")
			          .warning("Waiting for all systems to generate their security identities");
			JobService.getInstance()
			          .waitForJob("SecurityTokenStore", 5L, TimeUnit.MINUTES);
			classificationService.create(MailSystemClassifications.MailImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(MailImportFor, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(TargetUserNameKey, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(TargetPassKey, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(SourceUserNameKey, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(SourcePassKey, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(FoldersForImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			//classificationService.create(CurrentDayOfImport, newSystem.get(enterprise), token).createDefaultSecurity(activityMasterSystem,MailSystem.token);
			classificationService.create(LastDayOfImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			//classificationService.create(CurrentSizeOfImport, newSystem.get(enterprise), token).createDefaultSecurity(activityMasterSystem,MailSystem.token);
			classificationService.create(ConfirmedSourceMailImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(ConfirmedDestinationMailImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			
			logProgress("Mail Master", "Creating Mail Folder Fields", progressMonitor);
			classificationService.create(CurrentDayOfImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			//classificationService.create(CurrentSizeOfImport, newSystem.get(enterprise), token).createDefaultSecurity(activityMasterSystem,MailSystem.token);
			classificationService.create(CurrentDaySizeOfImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(TotalCountOfMailImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(CompletedMailImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(CompletedFolderImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(CurrentFolderImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			
			classificationService.create(CompletedSizeImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(TotalFoldersForMailImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(TotalSizeForMailImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			
			classificationService.create(JobStartedForMailImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(JobPausedForMailImport, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			
			logProgress("Mail Master", "Creating Mail Progress Fields", progressMonitor);
			
			classificationService.create(MailImportStage.MailImportCompleted, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(MailImportStage.MailImportInProgress, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(MailImportStage.MailImportLoginError, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(MailImportStage.MailImportNotStarted, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			classificationService.create(FolderStatusObject, system, token)
			                     .createDefaultSecurity(activityMasterSystem, token);
			
			logProgress("Mail System", "Checking Arrangement Types", 1, progressMonitor);
			
			arrangementsService.createArrangementType(MailImportArrangementTypes.MailImport, system, token);
			
			logProgress("Mail System", "Checking Resource Item Types", 1, progressMonitor);
			IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
			resourceItemService.createType(FolderStatusResourceItem, system, token);
		}
		logProgress("Mail System", "Checking Mail Import Classifications", 1, progressMonitor);
		
	}
}
