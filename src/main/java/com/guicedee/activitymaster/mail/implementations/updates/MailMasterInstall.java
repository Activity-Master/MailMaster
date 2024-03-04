package com.guicedee.activitymaster.mail.implementations.updates;

import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import com.guicedee.activitymaster.mail.MailSystem;
import com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.guicedee.activitymaster.mail.services.enumerations.MailImportArrangementTypes;
import com.guicedee.activitymaster.mail.services.enumerations.MailImportStage;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.interfaces.JobService;
import com.guicedee.logger.LogFactory;


import java.util.concurrent.TimeUnit;

import static com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications.*;
import static com.guicedee.activitymaster.mail.services.classifications.MailSystemResourceItemClassifications.*;
import static com.guicedee.activitymaster.mail.services.enumerations.MailImportResourceItemTypes.*;
import static com.guicedee.client.IGuiceContext.*;

@SortedUpdate(sortOrder = 1500, taskCount = 6)
public class MailMasterInstall implements ISystemUpdate
{
	@Override
	public void update(IEnterprise<?,?> enterprise)
	{
		createClassifications(enterprise);
	}
	
	@SuppressWarnings("Duplicates")
	private void createClassifications(IEnterprise<?,?> enterprise)
	{
		IClassificationService<?> classificationService = get(IClassificationService.class);
		ISystems<?,?> activityMasterSystem = get(ISystemsService.class)
				.getActivityMaster(enterprise);
		MailSystem systemM = com.guicedee.client.IGuiceContext.get(MailSystem.class);
		ISystems<?,?> system = systemM.getSystem(enterprise);
		UUID token = systemM.getSystemToken(enterprise);
		IArrangementsService<?> arrangementsService = get(IArrangementsService.class);
		
		try
		{
			classificationService.find(MailImport, system, identityToken);
		}
		catch (Exception e)
		{
			logProgress("Mail Master", "Creating Mail Import Fields");
			
			LogFactory.getLog("MailSystem")
			          .warning("Waiting for all systems to generate their security identities");
			JobService.getInstance()
			          .waitForJob("SecurityTokenStore", 5L, TimeUnit.MINUTES);
			classificationService.create(MailSystemClassifications.MailImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(MailImportFor, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(TargetUserNameKey, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(TargetPassKey, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(SourceUserNameKey, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(SourcePassKey, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(FoldersForImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			//classificationService.create(CurrentDayOfImport, newSystem.get(enterprise), identityToken).createDefaultSecurity(activityMasterSystem,MailSystem.token);
			classificationService.create(LastDayOfImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			//classificationService.create(CurrentSizeOfImport, newSystem.get(enterprise), identityToken).createDefaultSecurity(activityMasterSystem,MailSystem.token);
			classificationService.create(ConfirmedSourceMailImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(ConfirmedDestinationMailImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			
			logProgress("Mail Master", "Creating Mail Folder Fields");
			classificationService.create(CurrentDayOfImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			//classificationService.create(CurrentSizeOfImport, newSystem.get(enterprise), identityToken).createDefaultSecurity(activityMasterSystem,MailSystem.token);
			classificationService.create(CurrentDaySizeOfImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(TotalCountOfMailImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(CompletedMailImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(CompletedFolderImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(CurrentFolderImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			
			classificationService.create(CompletedSizeImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(TotalFoldersForMailImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(TotalSizeForMailImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			
			classificationService.create(JobStartedForMailImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(JobPausedForMailImport, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			
			logProgress("Mail Master", "Creating Mail Progress Fields");
			
			classificationService.create(MailImportStage.MailImportCompleted, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(MailImportStage.MailImportInProgress, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(MailImportStage.MailImportLoginError, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(MailImportStage.MailImportNotStarted, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			classificationService.create(FolderStatusObject, system, identityToken)
			                     .createDefaultSecurity(activityMasterSystem, identityToken);
			
			logProgress("Mail System", "Checking Arrangement Types", 1);
			
			arrangementsService.createArrangementType(MailImportArrangementTypes.MailImport.toString(), system, identityToken);
			
			logProgress("Mail System", "Checking Resource Item Types", 1);
			IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
			resourceItemService.createType(FolderStatusResourceItem, system, identityToken);
		}
		logProgress("Mail System", "Checking Mail Import Classifications", 1);
		
	}
}
