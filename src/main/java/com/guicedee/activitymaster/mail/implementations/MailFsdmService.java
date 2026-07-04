package com.guicedee.activitymaster.mail.implementations;

import com.guicedee.activitymaster.fsdm.client.services.IArrangementsService;
import com.guicedee.activitymaster.fsdm.client.services.IEventService;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEvent;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedPartyQueryBuilder;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications;
import com.guicedee.activitymaster.fsdm.client.services.classifications.types.IdentificationTypes;
import com.guicedee.activitymaster.mail.services.IMailFsdmService;
import com.guicedee.activitymaster.mail.services.classifications.MailClassifications;
import com.guicedee.activitymaster.mail.services.dto.MailAddress;
import com.guicedee.activitymaster.mail.services.dto.MailAttachment;
import com.guicedee.activitymaster.mail.services.dto.MailMessage;
import com.guicedee.activitymaster.mail.services.enumerations.MailArrangementTypes;
import com.guicedee.activitymaster.mail.services.enumerations.MailDirection;
import com.guicedee.activitymaster.mail.services.enumerations.MailEventTypes;
import com.guicedee.activitymaster.mail.services.enumerations.MailResourceItemTypes;
import com.guicedee.client.utils.Pair;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import jakarta.persistence.NoResultException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.guicedee.client.IGuiceContext.get;

/**
 * Default {@link IMailFsdmService} implementation. All FSDM writes use a {@link Mutiny.StatelessSession};
 * the {@link #ingest} pipeline parallelises independent work across separate sessions. The two core
 * operations without a stateless variant — event creation and mailbox-arrangement creation — run on a
 * short dedicated stateful transaction.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MailFsdmService implements IMailFsdmService<MailFsdmService>
{
	// ---- Granular stateless operations ------------------------------------------------------

	@Override
	public Uni<IInvolvedParty<?, ?>> findOrCreateParty(Mutiny.StatelessSession session, MailAddress address, ISystems<?, ?> system, UUID... token)
	{
		String email = address.getAddress();
		return findPartyByEmail(session, email, system, token)
				.chain(found -> found != null
						? Uni.createFrom().item(found)
						: createParty(session, email, system, token));
	}

	private Uni<IInvolvedParty<?, ?>> findPartyByEmail(Mutiny.StatelessSession session, String email, ISystems<?, ?> system, UUID... token)
	{
		IInvolvedPartyQueryBuilder builder = (IInvolvedPartyQueryBuilder) get(IInvolvedPartyService.class).get().builder(session);
		Uni<IInvolvedParty<?, ?>> query = (Uni<IInvolvedParty<?, ?>>) (Uni) builder
				.findByIdentificationType(IdentificationTypes.IdentificationTypeEmailAddress.toString(), email, system, token)
				.inActiveRange()
				.inDateRange()
				.get();
		return query
				.onFailure(NoResultException.class).recoverWithNull()
				.onFailure(NoSuchElementException.class).recoverWithNull();
	}

	private Uni<IInvolvedParty<?, ?>> createParty(Mutiny.StatelessSession session, String email, ISystems<?, ?> system, UUID... token)
	{
		Pair<String, String> identifier = new Pair<>();
		identifier.setKey(IdentificationTypes.IdentificationTypeEmailAddress.toString()).setValue(email);
		return get(IInvolvedPartyService.class).create(session, system, identifier, true, token);
	}

	@Override
	public Uni<IInvolvedParty<?, ?>> addEmailAlias(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, String aliasEmail, ISystems<?, ?> system, UUID... token)
	{
		return party.addOrReuseInvolvedPartyIdentificationType(session,
						DefaultClassifications.NoClassification.toString(),
						IdentificationTypes.IdentificationTypeEmailAddress.toString(),
						aliasEmail, system, token)
				.replaceWith(party);
	}

	@Override
	public Uni<IResourceItem<?, ?>> storeMessageResource(Mutiny.StatelessSession session, MailMessage message, ISystems<?, ?> system, UUID... token)
	{
		String body = message.getHtmlBody() != null ? message.getHtmlBody() : nz(message.getTextBody());
		byte[] data = body.getBytes(StandardCharsets.UTF_8);
		String subject = nz(message.getSubject());
		String contentType = message.getHtmlBody() != null ? "text/html" : "text/plain";

		return get(IResourceItemService.class)
				.create(session, MailResourceItemTypes.MailMessage.name(), subject, data, system, token)
				.chain(resourceItem -> reuse(session, resourceItem, MailClassifications.MailMessageId, nz(message.getMessageId()), system, token)
						.chain(() -> reuse(session, resourceItem, MailClassifications.MailSubject, subject, system, token))
						.chain(() -> reuse(session, resourceItem, MailClassifications.MailFolder, nz(message.getFolder()), system, token))
						.chain(() -> reuse(session, resourceItem, MailClassifications.MailContentType, contentType, system, token))
						.replaceWith((IResourceItem<?, ?>) resourceItem));
	}

	@Override
	public Uni<IResourceItem<?, ?>> storeAttachmentResource(Mutiny.StatelessSession session, MailAttachment attachment, ISystems<?, ?> system, UUID... token)
	{
		byte[] data = attachment.getContent() == null ? new byte[0] : attachment.getContent();
		String fileName = nz(attachment.getFileName());
		return get(IResourceItemService.class)
				.create(session, MailResourceItemTypes.MailAttachment.name(), fileName, data, system, token)
				.chain(resourceItem -> reuse(session, resourceItem, MailClassifications.MailFileName, fileName, system, token)
						.chain(() -> reuse(session, resourceItem, MailClassifications.MailContentType, nz(attachment.getContentType()), system, token))
						.replaceWith((IResourceItem<?, ?>) resourceItem));
	}

	// ---- High-level parallel ingest ---------------------------------------------------------

	@Override
	public Uni<UUID> ingest(MailMessage message, String mailboxOwnerEmail, MailDirection direction, String enterpriseName)
	{
		MailAddress sender = message.getFrom();
		List<MailAddress> recipients = new ArrayList<>(message.getAllRecipients());
		List<MailAttachment> attachments = new ArrayList<>();
		for (MailAttachment attachment : message.getAttachments())
		{
			if (!attachment.isInline())
			{
				attachments.add(attachment);
			}
		}
		MailAddress ownerAddress = (mailboxOwnerEmail == null || mailboxOwnerEmail.isBlank()) ? null : new MailAddress(mailboxOwnerEmail);

		// Phase A — create the independent records in parallel, each on its own session.
		Uni<IEvent<?, ?>> eventUni = createEvent(enterpriseName, direction);
		Uni<IInvolvedParty<?, ?>> senderUni = sender == null ? Uni.createFrom().<IInvolvedParty<?, ?>>nullItem() : resolveParty(enterpriseName, sender);
		Uni<List<IInvolvedParty<?, ?>>> recipientsUni = resolveParties(enterpriseName, recipients);
		Uni<IResourceItem<?, ?>> messageResourceUni = SessionUtils.withSystemAndTokenStateless(enterpriseName, MailSystemName,
				t -> storeMessageResource(t.getItem1(), message, t.getItem3(), t.getItem4()));
		Uni<List<IResourceItem<?, ?>>> attachmentResourcesUni = storeAttachments(enterpriseName, attachments);
		Uni<IInvolvedParty<?, ?>> ownerUni = ownerAddress == null ? Uni.createFrom().<IInvolvedParty<?, ?>>nullItem() : resolveParty(enterpriseName, ownerAddress);

		return Uni.combine().all().unis(eventUni, senderUni, recipientsUni, messageResourceUni, attachmentResourcesUni, ownerUni)
				.asTuple()
				.chain(tuple -> {
					IEvent<?, ?> event = tuple.getItem1();
					IInvolvedParty<?, ?> senderParty = tuple.getItem2();
					List<IInvolvedParty<?, ?>> recipientParties = tuple.getItem3();
					IResourceItem<?, ?> messageResource = tuple.getItem4();
					List<IResourceItem<?, ?>> attachmentResources = tuple.getItem5();
					IInvolvedParty<?, ?> ownerParty = tuple.getItem6();

					// Phase B — links + classifications referencing the now-committed records, in parallel.
					List<Uni<Void>> ops = new ArrayList<>();
					ops.add(writeEventMetadata(enterpriseName, event, message, direction));
					if (senderParty != null)
					{
						ops.add(linkEventParty(enterpriseName, event, senderParty, MailClassifications.MailSender, sender.getAddress()));
					}
					for (int i = 0; i < recipientParties.size(); i++)
					{
						IInvolvedParty<?, ?> party = recipientParties.get(i);
						String address = recipients.get(i).getAddress();
						ops.add(linkEventParty(enterpriseName, event, party, MailClassifications.MailRecipient, address));
					}
					ops.add(linkEventResource(enterpriseName, event, messageResource, MailClassifications.MailMessageLink, nz(message.getSubject())));
					for (int i = 0; i < attachmentResources.size(); i++)
					{
						IResourceItem<?, ?> resource = attachmentResources.get(i);
						String fileName = nz(attachments.get(i).getFileName());
						ops.add(linkEventResource(enterpriseName, event, resource, MailClassifications.MailAttachmentLink, fileName));
					}
					if (ownerParty != null)
					{
						ops.add(linkMailbox(enterpriseName, ownerParty, messageResource, mailboxOwnerEmail));
					}

					return Uni.combine().all().unis(ops).discardItems().replaceWith(event.getId());
				});
	}

	// ---- Per-task session helpers (parallelised across sessions) ----------------------------

	private Uni<IEvent<?, ?>> createEvent(String enterpriseName, MailDirection direction)
	{
		String type = (direction == MailDirection.Inbound ? MailEventTypes.MailReceived : MailEventTypes.MailSent).name();
		return SessionUtils.withSystemAndTokenStateless(enterpriseName, MailSystemName, t -> {
			return get(IEventService.class).createEvent(t.getItem1(), type, t.getItem3(), t.getItem4())
					.map(event -> (IEvent<?, ?>) event);
		});
	}

	private Uni<IInvolvedParty<?, ?>> resolveParty(String enterpriseName, MailAddress address)
	{
		return SessionUtils.withSystemAndTokenStateless(enterpriseName, MailSystemName,
				t -> findOrCreateParty(t.getItem1(), address, t.getItem3(), t.getItem4()));
	}

	private Uni<List<IInvolvedParty<?, ?>>> resolveParties(String enterpriseName, List<MailAddress> addresses)
	{
		if (addresses.isEmpty())
		{
			return Uni.createFrom().item(List.of());
		}
		List<Uni<IInvolvedParty<?, ?>>> unis = new ArrayList<>();
		for (MailAddress address : addresses)
		{
			unis.add(resolveParty(enterpriseName, address));
		}
		return Uni.combine().all().unis(unis).combinedWith(items -> {
			List<IInvolvedParty<?, ?>> out = new ArrayList<>();
			for (Object item : items)
			{
				out.add((IInvolvedParty<?, ?>) item);
			}
			return out;
		});
	}

	private Uni<List<IResourceItem<?, ?>>> storeAttachments(String enterpriseName, List<MailAttachment> attachments)
	{
		if (attachments.isEmpty())
		{
			return Uni.createFrom().item(List.of());
		}
		List<Uni<IResourceItem<?, ?>>> unis = new ArrayList<>();
		for (MailAttachment attachment : attachments)
		{
			unis.add(SessionUtils.withSystemAndTokenStateless(enterpriseName, MailSystemName,
					t -> storeAttachmentResource(t.getItem1(), attachment, t.getItem3(), t.getItem4())));
		}
		return Uni.combine().all().unis(unis).combinedWith(items -> {
			List<IResourceItem<?, ?>> out = new ArrayList<>();
			for (Object item : items)
			{
				out.add((IResourceItem<?, ?>) item);
			}
			return out;
		});
	}

	private Uni<Void> writeEventMetadata(String enterpriseName, IEvent<?, ?> event, MailMessage message, MailDirection direction)
	{
		return SessionUtils.withSystemAndTokenStateless(enterpriseName, MailSystemName, t -> {
			Mutiny.StatelessSession session = t.getItem1();
			ISystems<?, ?> system = t.getItem3();
			UUID[] token = t.getItem4();
			return reuse(session, event, MailClassifications.MailDirection, direction.name(), system, token)
					.chain(() -> reuse(session, event, MailClassifications.MailMessageId, nz(message.getMessageId()), system, token))
					.chain(() -> reuse(session, event, MailClassifications.MailSubject, nz(message.getSubject()), system, token))
					.chain(() -> reuse(session, event, MailClassifications.MailFolder, nz(message.getFolder()), system, token))
					.chain(() -> reuse(session, event, MailClassifications.MailHasAttachments, String.valueOf(message.hasAttachments()), system, token));
		});
	}

	private Uni<Void> linkEventParty(String enterpriseName, IEvent<?, ?> event, IInvolvedParty<?, ?> party, MailClassifications role, String value)
	{
		return SessionUtils.withSystemAndTokenStateless(enterpriseName, MailSystemName,
				t -> ((IEvent) event).addInvolvedParty(t.getItem1(), party, role.name(), nz(value), t.getItem3(), t.getItem4()).replaceWithVoid());
	}

	private Uni<Void> linkEventResource(String enterpriseName, IEvent<?, ?> event, IResourceItem<?, ?> resource, MailClassifications role, String value)
	{
		return SessionUtils.withSystemAndTokenStateless(enterpriseName, MailSystemName,
				t -> ((IEvent) event).addOrReuseResourceItem(t.getItem1(), role.name(), resource, nz(value), t.getItem3(), t.getItem4()).replaceWithVoid());
	}

	private Uni<Void> linkMailbox(String enterpriseName, IInvolvedParty<?, ?> ownerParty, IResourceItem<?, ?> messageResource, String ownerEmail)
	{
		return SessionUtils.withSystemAndTokenStateless(enterpriseName, MailSystemName, t -> {
			Mutiny.StatelessSession session = t.getItem1();
			ISystems<?, ?> system = t.getItem3();
			UUID[] token = t.getItem4();
			Uni<List<IArrangement<?, ?>>> finder = (Uni<List<IArrangement<?, ?>>>) (Uni)
					get(IArrangementsService.class).findArrangementsByClassification(session,
							MailClassifications.MailboxOwner.name(), ownerEmail, system, token);
			return finder
					.onFailure().recoverWithItem(Collections.<IArrangement<?, ?>>emptyList())
					.chain(list -> {
						if (list != null && !list.isEmpty())
						{
							return ((IArrangement) list.get(0)).addOrReuseResourceItem(session,
									MailClassifications.MailMessageLink.name(), messageResource, "mailbox", system, token).replaceWithVoid();
						}
						return ((Uni<IArrangement<?, ?>>) (Uni) get(IArrangementsService.class)
								.create(session, (UUID) null, MailArrangementTypes.Mailbox.name(),
										MailClassifications.MailboxOwner.name(), ownerEmail, system, token))
								.chain(arrangement -> ((IArrangement) arrangement).addInvolvedParty(session, ownerParty,
												MailClassifications.MailboxOwner.name(), ownerEmail, system, token)
										.replaceWith((IArrangement<?, ?>) arrangement))
								.chain(arrangement -> ((IArrangement) arrangement).addOrReuseResourceItem(session,
										MailClassifications.MailMessageLink.name(), messageResource, "mailbox", system, token).replaceWithVoid());
					});
		});
	}

	// ---- Helpers ----------------------------------------------------------------------------

	private Uni<Void> reuse(Mutiny.StatelessSession session, Object entity, MailClassifications classification, String value, ISystems<?, ?> system, UUID... token)
	{
		return ((com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications) entity)
				.addOrReuseClassification(session, classification.name(), nz(value), system, token);
	}

	private static String nz(String value)
	{
		return value == null ? "" : value;
	}
}




