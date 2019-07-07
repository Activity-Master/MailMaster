package com.armineasy.activitymaster.mail;

import com.armineasy.activitymaster.activitymaster.ActivityMasterService;
import com.armineasy.activitymaster.activitymaster.implementations.ClassificationService;
import com.armineasy.activitymaster.activitymaster.implementations.SystemsService;
import com.armineasy.activitymaster.activitymaster.services.IActivityMasterProgressMonitor;
import com.armineasy.activitymaster.activitymaster.services.IActivityMasterSystem;
import com.armineasy.activitymaster.activitymaster.services.dto.IArrangement;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.services.system.IArrangementsService;
import com.armineasy.activitymaster.activitymaster.services.system.IResourceItemService;
import com.armineasy.activitymaster.activitymaster.systems.SystemsSystem;
import com.armineasy.activitymaster.mail.services.IMailSystem;
import com.armineasy.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.armineasy.activitymaster.mail.services.enumerations.MailImportArrangementTypes;
import com.armineasy.activitymaster.mail.services.enumerations.MailImportStage;
import com.armineasy.activitymaster.mail.updates.UpdatesLocator;
import com.google.inject.Singleton;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.interfaces.JobService;
import com.jwebmp.logger.LogFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.armineasy.activitymaster.mail.services.classifications.MailSystemClassifications.*;
import static com.armineasy.activitymaster.mail.services.classifications.MailSystemResourceItemClassifications.*;
import static com.armineasy.activitymaster.mail.services.enumerations.MailImportResourceItemTypes.*;
import static com.jwebmp.guicedinjection.GuiceContext.*;

@Singleton
public class MailSystem
		implements IMailSystem<MailSystem>, IActivityMasterSystem<MailSystem>
{
	private static final Map<IEnterprise<?>, UUID> systemTokens = new HashMap<>();
	private static final Map<IEnterprise<?>, ISystems> newSystem = new HashMap<>();

	private UUID uuid;

	public static Map<IEnterprise<?>, UUID> getSystemTokens()
	{
		return MailSystem.systemTokens;
	}

	public static Map<IEnterprise<?>, ISystems> getNewSystem()
	{
		return MailSystem.newSystem;
	}

	@SuppressWarnings("Duplicates")
	private void createClassifications(IEnterprise<?> enterprise,  IActivityMasterProgressMonitor progressMonitor)
	{
		ClassificationService classificationService = get(ClassificationService.class);
		ISystems activityMasterSystem = get(SystemsService.class)
		                                            .getActivityMaster(enterprise);

		IArrangementsService<?> arrangementsService = get(IArrangementsService.class);

		logProgress("Mail System", "Checking Mail Import Classifications", 1, progressMonitor);

		classificationService.create(MailSystemClassifications.MailImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(MailImportFor, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(TargetUserNameKey, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(TargetPassKey, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(SourceUserNameKey, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(SourcePassKey, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(FoldersForImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		//classificationService.create(CurrentDayOfImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(LastDayOfImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		//classificationService.create(CurrentSizeOfImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(ConfirmedSourceMailImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(ConfirmedDestinationMailImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));

		classificationService.create(CurrentDayOfImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		//classificationService.create(CurrentSizeOfImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(CurrentDaySizeOfImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(TotalCountOfMailImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(CompletedMailImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(CompletedFolderImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(CurrentFolderImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));

		classificationService.create(CompletedSizeImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(TotalFoldersForMailImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(TotalSizeForMailImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));

		classificationService.create(JobStartedForMailImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(JobPausedForMailImport, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));

		classificationService.create(MailImportStage.MailImportCompleted, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(MailImportStage.MailImportInProgress, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(MailImportStage.MailImportLoginError, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(MailImportStage.MailImportNotStarted, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));
		classificationService.create(FolderStatusObject, newSystem.get(enterprise), uuid)
		                     .createDefaultSecurity(activityMasterSystem, MailSystem.getSystemTokens()
		                                                                            .get(enterprise));

		logProgress("Mail System", "Checking Arrangement Types", 1, progressMonitor);

		arrangementsService.createArrangementType(MailImportArrangementTypes.MailImport, newSystem.get(enterprise), uuid);


		logProgress("Mail System", "Checking Resource Item Types", 1, progressMonitor);
		IResourceItemService resourceItemService = get(IResourceItemService.class);
		resourceItemService.createType(FolderStatusResourceItem, newSystem.get(enterprise), uuid);
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
	public void postUpdate(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		newSystem.put(enterprise, get(SystemsService.class)
		                                      .create(enterprise, "Mail System",
		                                              "The system for managing User Profiles", ""));
		uuid = get(SystemsSystem.class)
		                   .registerNewSystem(enterprise, newSystem.get(enterprise));

		LogFactory.getLog("MailSystem")
		          .warning("Waiting for all systems to generate their security identities");
		JobService.getInstance()
		          .waitForJob("SecurityTokenStore", 5L, TimeUnit.MINUTES);

		systemTokens.put(enterprise, uuid);
		createClassifications(enterprise,progressMonitor);

		update20190622AddCurrentMailFolder(newSystem.get(enterprise), enterprise, uuid);
	}

	private void update20190622AddCurrentMailFolder(ISystems<?> systems, IEnterprise<?> ent,UUID...identityToken)
	{
		try(InputStream is = UpdatesLocator.class.getResourceAsStream("update20190622AddCurrentFolderImport.sql"))
		{
			byte[] output = is.readAllBytes();
			String script = new String(output);
			script = script.replace("-9223372036854775807", "" + ent.getId());
			get(ActivityMasterService.class).runScript(script);
		}
		catch (Exception e)
		{
			LogFactory.getLog("MailSystem")
			          .warning("Cant Read Current Folder Import Update File - " + e);
		}
	}
}
