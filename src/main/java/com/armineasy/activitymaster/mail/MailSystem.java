package com.armineasy.activitymaster.mail;

import com.armineasy.activitymaster.activitymaster.implementations.ClassificationService;
import com.armineasy.activitymaster.activitymaster.implementations.SystemsService;
import com.armineasy.activitymaster.activitymaster.services.IActivityMasterProgressMonitor;
import com.armineasy.activitymaster.activitymaster.services.IActivityMasterSystem;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.services.system.IArrangementsService;
import com.armineasy.activitymaster.activitymaster.systems.SystemsSystem;
import com.armineasy.activitymaster.mail.services.IMailSystem;
import com.armineasy.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.armineasy.activitymaster.mail.services.enumerations.MailImportArrangementTypes;
import com.armineasy.activitymaster.mail.services.enumerations.MailImportStage;
import com.google.inject.Singleton;
import com.jwebmp.guicedinjection.GuiceContext;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.armineasy.activitymaster.mail.services.classifications.MailSystemClassifications.*;
import static com.armineasy.activitymaster.mail.services.enumerations.MailImportArrangementTypes.*;

@Singleton
public class MailSystem
		implements IMailSystem<MailSystem>, IActivityMasterSystem<MailSystem>
{
	@Getter
	private static final Map<IEnterprise<?> , UUID> systemTokens = new HashMap<>();
	@Getter
	private static final Map<IEnterprise<?> , ISystems> newSystem = new HashMap<>();

	private UUID uuid;

	private void createClassifications(IEnterprise<?> enterprise)
	{
		ClassificationService classificationService = GuiceContext.get(ClassificationService.class);
		ISystems activityMasterSystem = GuiceContext.get(SystemsService.class)
		                                            .getActivityMaster(enterprise);

		IArrangementsService<?> arrangementsService = GuiceContext.get(IArrangementsService.class);
		arrangementsService.createArrangementType(MailImportArrangementTypes.MailImport, newSystem.get(enterprise), uuid);

		classificationService.create(MailSystemClassifications.MailImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(GoogleUserNameKey, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(FoldersForImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(CurrentDayOfImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(CurrentSizeOfImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(ConfirmedSourceMailImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(ConfirmedDestinationMailImport, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));

		classificationService.create(MailImportStage.MailImportCompleted, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(MailImportStage.MailImportInProgress, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(MailImportStage.MailImportLoginError, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
		classificationService.create(MailImportStage.MailImportNotStarted, newSystem.get(enterprise), uuid).createDefaultSecurity(activityMasterSystem,MailSystem.getSystemTokens().get(enterprise));
	}

	@Override
	public void createDefaults(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{

	}

	@Override
	public int totalTasks()
	{
		return 0;
	}

	@Override
	public void postUpdate(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		newSystem.put(enterprise, GuiceContext.get(SystemsService.class)
		                                      .create(enterprise, "Mail System",
		                                              "The system for managing User Profiles", ""));
		UUID uuid = GuiceContext.get(SystemsSystem.class)
		                        .registerNewSystem(enterprise, newSystem.get(enterprise));
		systemTokens.put(enterprise, uuid);
		createClassifications(enterprise);
	}


}
