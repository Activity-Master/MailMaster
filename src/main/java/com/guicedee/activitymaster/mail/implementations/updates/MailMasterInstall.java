package com.guicedee.activitymaster.mail.implementations.updates;

import com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService;
import com.guicedee.activitymaster.fsdm.client.services.IArrangementsService;
import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.IEventService;
import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import com.guicedee.activitymaster.mail.services.classifications.MailClassifications;
import com.guicedee.activitymaster.mail.services.enumerations.MailArrangementTypes;
import com.guicedee.activitymaster.mail.services.enumerations.MailEventTypes;
import com.guicedee.activitymaster.mail.services.enumerations.MailResourceItemTypes;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;

import static com.guicedee.activitymaster.mail.services.IMailFsdmService.MailSystemName;
import static com.guicedee.client.IGuiceContext.get;

/**
 * Installs the Mail Master taxonomy into a new enterprise: the mail classifications (relationship
 * roles and metadata fields), the mail event types ({@code MailReceived} / {@code MailSent}), the
 * {@code Mailbox} arrangement type and the {@code MailMessage} / {@code MailAttachment} resource-item
 * types.
 * <p>
 * Only the lightweight taxonomy is created here — never bulk data.
 */
@SortedUpdate(sortOrder = 1500, taskCount = 4)
@Log4j2
public class MailMasterInstall implements ISystemUpdate
{
	@Override
	public Uni<Boolean> update(Mutiny.Session session, IEnterprise<?, ?> enterprise)
	{
		log.info("Installing Mail Master taxonomy for enterprise {}", enterprise.getName());
		return IActivityMasterService.getISystem(session, MailSystemName, enterprise)
				.chain(system -> IActivityMasterService.getISystemToken(session, MailSystemName, enterprise)
						.chain(token -> createTaxonomy(session, system, token)))
				.onFailure().invoke(error -> log.error("Mail Master installation failed: {}", error.getMessage(), error))
				.replaceWith(Boolean.TRUE);
	}

	private Uni<Void> createTaxonomy(Mutiny.Session session, ISystems<?, ?> system, UUID token)
	{
		IClassificationService<?> classificationService = get(IClassificationService.class);
		IEventService<?> eventService = get(IEventService.class);
		IArrangementsService<?> arrangementsService = get(IArrangementsService.class);
		IResourceItemService<?> resourceItemService = get(IResourceItemService.class);

		Uni<Void> chain = Uni.createFrom().voidItem();

		logProgress("Mail Master", "Creating mail classifications");
		for (MailClassifications classification : MailClassifications.values())
		{
			chain = chain.chain(() -> classificationService.create(session, classification, system, token).replaceWithVoid());
		}

		chain = chain
				.chain(() -> {
					logProgress("Mail Master", "Creating mail event types", 1);
					return eventService.createEventType(session, MailEventTypes.MailReceived, system, token).replaceWithVoid();
				})
				.chain(() -> eventService.createEventType(session, MailEventTypes.MailSent, system, token).replaceWithVoid())
				.chain(() -> {
					logProgress("Mail Master", "Creating mailbox arrangement type", 1);
					return arrangementsService.createArrangementType(session, MailArrangementTypes.Mailbox, system, token).replaceWithVoid();
				})
				.chain(() -> {
					logProgress("Mail Master", "Creating mail resource item types", 1);
					return resourceItemService.createType(session, MailResourceItemTypes.MailMessage, system, token).replaceWithVoid();
				})
				.chain(() -> resourceItemService.createType(session, MailResourceItemTypes.MailAttachment, system, token).replaceWithVoid());

		return chain;
	}

	/** Stateless twin of {@link #update(Mutiny.Session, IEnterprise)}. */
	@Override
	public Uni<Boolean> update(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
	{
		log.info("Installing Mail Master taxonomy for enterprise {} (stateless)", enterprise.getName());
		return IActivityMasterService.getISystem(session, MailSystemName, enterprise)
				.chain(system -> IActivityMasterService.getISystemToken(session, MailSystemName, enterprise)
						.chain(token -> createTaxonomy(session, system, token)))
				.onFailure().invoke(error -> log.error("Mail Master installation failed (stateless): {}", error.getMessage(), error))
				.replaceWith(Boolean.TRUE);
	}

	private Uni<Void> createTaxonomy(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID token)
	{
		IClassificationService<?> classificationService = get(IClassificationService.class);
		IEventService<?> eventService = get(IEventService.class);
		IArrangementsService<?> arrangementsService = get(IArrangementsService.class);
		IResourceItemService<?> resourceItemService = get(IResourceItemService.class);

		logProgress("Mail Master", "Creating mail classifications");
		// Sequential (concatenated, never merged) create of every mail classification — one statement at a
		// time on the single reactive connection, honouring the "one action per session" rule.
		return Multi.createFrom().items(MailClassifications.values())
				.onItem().transformToUniAndConcatenate(c -> classificationService.create(session, c, system, token))
				.collect().last()
				.chain(() -> {
					logProgress("Mail Master", "Creating mail event types", 1);
					return eventService.createEventType(session, MailEventTypes.MailReceived, system, token).replaceWithVoid();
				})
				.chain(() -> eventService.createEventType(session, MailEventTypes.MailSent, system, token).replaceWithVoid())
				.chain(() -> {
					logProgress("Mail Master", "Creating mailbox arrangement type", 1);
					return arrangementsService.createArrangementType(session, MailArrangementTypes.Mailbox, system, token).replaceWithVoid();
				})
				.chain(() -> {
					logProgress("Mail Master", "Creating mail resource item types", 1);
					return resourceItemService.createType(session, MailResourceItemTypes.MailMessage, system, token).replaceWithVoid();
				})
				.chain(() -> resourceItemService.createType(session, MailResourceItemTypes.MailAttachment, system, token).replaceWithVoid());
	}
}



