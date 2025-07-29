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
import com.guicedee.logger.LogFactory;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import static com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications.*;
import static com.guicedee.activitymaster.mail.services.classifications.MailSystemResourceItemClassifications.*;
import static com.guicedee.activitymaster.mail.services.enumerations.MailImportResourceItemTypes.*;
import static com.guicedee.client.IGuiceContext.*;

@SortedUpdate(sortOrder = 1500, taskCount = 6)
@Log4j2
public class MailMasterInstall implements ISystemUpdate
{
	@Override
	public Uni<Boolean> update(Mutiny.Session session, IEnterprise<?,?> enterprise)
	{
		log.info("Starting mail master installation");
		return createClassifications(session, enterprise)
			.onFailure().invoke(error -> log.error("Error during mail master installation: {}", error.getMessage(), error))
			.onItem().invoke(() -> log.info("Mail master installation completed successfully"));
	}

	private Uni<Boolean> createClassifications(Mutiny.Session session, IEnterprise<?,?> enterprise)
	{
		IClassificationService<?> classificationService = get(IClassificationService.class);
		ISystems<?,?> activityMasterSystem = get(ISystemsService.class)
				.getActivityMaster(enterprise);
		MailSystem systemM = com.guicedee.client.IGuiceContext.get(MailSystem.class);
		ISystems<?,?> system = systemM.getSystem(enterprise);
		UUID token = systemM.getSystemToken(enterprise);
		IArrangementsService<?> arrangementsService = get(IArrangementsService.class);

		// First check if classifications already exist
		return classificationService.find(session, MailImport, system, identityToken)
			.onItem().invoke(() -> log.info("Mail import classifications already exist, skipping creation"))
			.onFailure().recoverWithUni(() -> {
				// Classifications don't exist, create them
				logProgress("Mail Master", "Creating Mail Import Fields");
				
				LogFactory.getLog("MailSystem")
						.warning("Waiting for all systems to generate their security identities");
				
				// Start the sequential chain of classification creation
				return classificationService.create(session, MailSystemClassifications.MailImport, system, identityToken)
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, MailImportFor, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, TargetUserNameKey, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, TargetPassKey, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, SourceUserNameKey, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, SourcePassKey, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, FoldersForImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, LastDayOfImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, ConfirmedSourceMailImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, ConfirmedDestinationMailImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					
					.chain(() -> {
						logProgress("Mail Master", "Creating Mail Folder Fields");
						return classificationService.create(session, CurrentDayOfImport, system, identityToken);
					})
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, CurrentDaySizeOfImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, TotalCountOfMailImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, CompletedMailImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, CompletedFolderImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, CurrentFolderImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, CompletedSizeImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, TotalFoldersForMailImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, TotalSizeForMailImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, JobStartedForMailImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, JobPausedForMailImport, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					
					.chain(() -> {
						logProgress("Mail Master", "Creating Mail Progress Fields");
						return classificationService.create(session, MailImportStage.MailImportCompleted, system, identityToken);
					})
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, MailImportStage.MailImportInProgress, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, MailImportStage.MailImportLoginError, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, MailImportStage.MailImportNotStarted, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					.chain(() -> classificationService.create(session, FolderStatusObject, system, identityToken))
					.chain(classification -> classification.createDefaultSecurity(session, activityMasterSystem, identityToken))
					
					.chain(() -> {
						logProgress("Mail System", "Checking Arrangement Types", 1);
						return arrangementsService.createArrangementType(session, MailImportArrangementTypes.MailImport.toString(), system, identityToken);
					})
					
					.chain(() -> {
						logProgress("Mail System", "Checking Resource Item Types", 1);
						IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
						return resourceItemService.createType(session, FolderStatusResourceItem, system, identityToken);
					})
					.map(result -> true);
			})
			.chain(() -> {
				logProgress("Mail System", "Checking Mail Import Classifications", 1);
				return Uni.createFrom().item(true);
			});
	}
}