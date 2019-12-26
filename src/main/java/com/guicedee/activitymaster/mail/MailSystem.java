package com.guicedee.activitymaster.mail;

import com.guicedee.activitymaster.core.ActivityMasterService;
import com.guicedee.activitymaster.core.services.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.core.services.IActivityMasterSystem;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IArrangementsService;
import com.guicedee.activitymaster.core.services.system.IClassificationService;
import com.guicedee.activitymaster.core.services.system.IResourceItemService;
import com.guicedee.activitymaster.core.services.system.ISystemsService;
import com.guicedee.activitymaster.mail.services.IMailSystem;
import com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.guicedee.activitymaster.mail.services.enumerations.MailImportArrangementTypes;
import com.guicedee.activitymaster.mail.services.enumerations.MailImportStage;
import com.guicedee.activitymaster.mail.updates.UpdatesLocator;
import com.google.inject.Singleton;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.interfaces.JobService;
import com.guicedee.logger.LogFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications.*;
import static com.guicedee.activitymaster.mail.services.classifications.MailSystemResourceItemClassifications.*;
import static com.guicedee.activitymaster.mail.services.enumerations.MailImportResourceItemTypes.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@Singleton
public class MailSystem
		implements IMailSystem<MailSystem>, IActivityMasterSystem<MailSystem>
{
	private static final Map<IEnterprise<?>, UUID> systemTokens = new HashMap<>();
	private static final Map<IEnterprise<?>, ISystems<?>> systemsMap = new HashMap<>();

	public static Map<IEnterprise<?>, UUID> getSystemTokens()
	{
		return MailSystem.systemTokens;
	}

	public static Map<IEnterprise<?>, ISystems<?>> getSystemsMap()
	{
		return MailSystem.systemsMap;
	}

	@SuppressWarnings("Duplicates")
	private void createClassifications(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		IClassificationService<?> classificationService = get(IClassificationService.class);
		ISystems<?> activityMasterSystem = get(ISystemsService.class)
				                                .getActivityMaster(enterprise);

		IArrangementsService<?> arrangementsService = get(IArrangementsService.class);

		try
		{
			classificationService.find(MailImport, enterprise, getSystemTokens().get(enterprise));
		}
		catch (Exception e)
		{
			logProgress("Mail Master", "Creating Mail Import Fields", progressMonitor);

			LogFactory.getLog("MailSystem")
			          .warning("Waiting for all systems to generate their security identities");
			JobService.getInstance()
			          .waitForJob("SecurityTokenStore", 5L, TimeUnit.MINUTES);
			UUID uuid = systemTokens.get(enterprise);

			classificationService.create(MailSystemClassifications.MailImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(MailImportFor, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(TargetUserNameKey, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(TargetPassKey, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(SourceUserNameKey, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(SourcePassKey, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(FoldersForImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			//classificationService.create(CurrentDayOfImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
			classificationService.create(LastDayOfImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			//classificationService.create(CurrentSizeOfImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
			classificationService.create(ConfirmedSourceMailImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(ConfirmedDestinationMailImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));

			logProgress("Mail Master", "Creating Mail Folder Fields", progressMonitor);
			classificationService.create(CurrentDayOfImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			//classificationService.create(CurrentSizeOfImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
			classificationService.create(CurrentDaySizeOfImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(TotalCountOfMailImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(CompletedMailImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(CompletedFolderImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(CurrentFolderImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));

			classificationService.create(CompletedSizeImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(TotalFoldersForMailImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(TotalSizeForMailImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));

			classificationService.create(JobStartedForMailImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(JobPausedForMailImport, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));

			logProgress("Mail Master", "Creating Mail Progress Fields", progressMonitor);

			classificationService.create(MailImportStage.MailImportCompleted, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(MailImportStage.MailImportInProgress, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(MailImportStage.MailImportLoginError, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(MailImportStage.MailImportNotStarted, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));
			classificationService.create(FolderStatusObject, systemsMap.get(enterprise), uuid)
			                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
			                                                                            .get(enterprise));

			logProgress("Mail System", "Checking Arrangement Types", 1, progressMonitor);

			arrangementsService.createArrangementType(MailImportArrangementTypes.MailImport, systemsMap.get(enterprise), uuid);

			logProgress("Mail System", "Checking Resource Item Types", 1, progressMonitor);
			IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
			resourceItemService.createType(FolderStatusResourceItem, systemsMap.get(enterprise), uuid);
		}
		logProgress("Mail System", "Checking Mail Import Classifications", 1, progressMonitor);

	}

	@Override
	public void createDefaults(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{

	}

	@Override
	public int totalTasks()
	{
		return 10;
	}

	@Override
	public void postStartup(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		final String systemName = "Mail Master";
		final String systemDesc = "The system for handling the receiving, importing and sending of emails";
		ISystems<?> sys = GuiceContext.get(ISystemsService.class)
		                              .findSystem(enterprise, systemName);
		UUID securityToken = null;
		if (sys == null)
		{
			sys = GuiceContext.get(ISystemsService.class)
			                  .create(enterprise, systemName, systemDesc, systemName);
			securityToken = GuiceContext.get(ISystemsService.class)
			                            .registerNewSystem(enterprise, sys);
		}
		else
		{
			securityToken = GuiceContext.get(ISystemsService.class)
			                            .getSecurityIdentityToken(sys);
		}
		systemTokens.put(enterprise, securityToken);
		systemsMap.put(enterprise, sys);
	}

	@Override
	public void loadUpdates(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		createClassifications(enterprise, progressMonitor);
	}
}
