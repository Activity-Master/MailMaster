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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.guicedee.client.IGuiceContext.get;

/**
 * Default {@link IMailFsdmService} implementation. All FSDM writes use a {@link Mutiny.StatelessSession};
 * {@link #ingest} checks the verified user's installation and consent before creating private
 * events, resources and mailbox links together in one stateless transaction.
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

    @Override
    public Uni<UUID> ingest(MailMessage message, String mailboxOwnerEmail, MailDirection direction, String enterpriseName) {
        return Uni.createFrom().deferred(() -> get(com.guicedee.activitymaster.mail.MailIdentityProvider.class).current())
                .onItem().ifNull().failWith(() -> new SecurityException("Authenticated mail identity required"))
                .chain(identity -> ingest(message, mailboxOwnerEmail, direction, enterpriseName, identity));
    }

    @Override
    public Uni<UUID> ingest(MailMessage message, String mailboxOwnerEmail, MailDirection direction, String enterpriseName,
                             com.guicedee.activitymaster.mail.MailIdentity identity) {
        java.util.Objects.requireNonNull(message); java.util.Objects.requireNonNull(direction);
        java.util.Objects.requireNonNull(identity);
        // Identity is captured before entering the transaction. Headers and ownerEmail are labels, never actors.
        return SessionUtils.withActivityMaster(enterpriseName, MailSystemName, t -> {
            if (!identity.enterpriseId().equals(t.getItem2().getId()))
                return Uni.createFrom().failure(new SecurityException("Mail enterprise scope mismatch"));
            Mutiny.StatelessSession session = t.getItem1();
            ISystems<?, ?> system = t.getItem3();
            UUID[] tokens = identity.tokens();
            var plugins = get(com.guicedee.activitymaster.fsdm.plugins.PluginService.class);
            IEventService<?> events = get(IEventService.class);
            IInvolvedPartyService<?> parties = get(IInvolvedPartyService.class);
            com.guicedee.activitymaster.fsdm.client.services.ISystemsService<?> systems =
                    get(com.guicedee.activitymaster.fsdm.client.services.ISystemsService.class);
            String type = (direction == MailDirection.Inbound ? MailEventTypes.MailReceived : MailEventTypes.MailSent).name();
            return plugins.checkBuiltIn(session, system, identity.user(), identity.installationPartyId())
                    .chain(() -> parties.find(session, identity.partyId()))
                    .chain(owner -> events.createEvent(session, type, system, tokens)
                            .chain(event -> restrictNewRow(session, system, identity,
                                            (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) event,
                                            "event.eventsecuritytoken", "eventsid")
                                    .chain(() -> storeMessageResource(session, message, system, tokens))
                                    .chain(resource -> restrictNewRow(session, system, identity,
                                                    (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) resource,
                                                    "resource.resourceitemsecuritytoken", "resourceitemid")
                                            .chain(() -> ((IEvent) event).addOrReuseResourceItem(session, MailClassifications.MailMessageLink.name(),
                                                    resource, nz(message.getSubject()), system, tokens))
                                            .chain(() -> writeIngestLinks(session, system, identity, event, message, direction))
                                            .chain(() -> mailbox(session, system, identity, owner, resource, mailboxOwnerEmail)))
                                    .chain(() -> systems.getActivityMaster(session, system.getEnterprise())
                                            .chain(core -> plugins.audit(session, core, identity.user(),
                                                    new com.guicedee.activitymaster.fsdm.plugins.PluginModels.Invocation(system.getId(), identity.installationPartyId()),
                                                    "mail.ingest")))
                                    .replaceWith(event.getId())));
        });
    }

    private Uni<Void> writeIngestLinks(Mutiny.StatelessSession session, ISystems<?, ?> system,
                                       com.guicedee.activitymaster.mail.MailIdentity identity, IEvent<?, ?> event,
                                       MailMessage message, MailDirection direction) {
        UUID[] tokens = identity.tokens();
        Uni<Void> chain = reuse(session, event, MailClassifications.MailDirection, direction.name(), system, tokens)
                .chain(() -> reuse(session, event, MailClassifications.MailMessageId, nz(message.getMessageId()), system, tokens))
                .chain(() -> reuse(session, event, MailClassifications.MailSubject, nz(message.getSubject()), system, tokens))
                .chain(() -> reuse(session, event, MailClassifications.MailFolder, nz(message.getFolder()), system, tokens))
                .chain(() -> reuse(session, event, MailClassifications.MailHasAttachments,
                        Boolean.toString(message.getAttachments().stream().anyMatch(attachment -> !attachment.isInline())), system, tokens));
        List<MailAddress> addresses = new ArrayList<>();
        if (message.getFrom() != null) addresses.add(message.getFrom());
        addresses.addAll(message.getAllRecipients());
        for (int index = 0; index < addresses.size(); index++) {
            MailAddress address = addresses.get(index);
            String role = message.getFrom() != null && index == 0 ? MailClassifications.MailSender.name() : MailClassifications.MailRecipient.name();
            chain = chain.chain(() -> findOrCreateParty(session, address, system, tokens)
                    .chain(party -> ((IEvent) event).addInvolvedParty(session, party, role, address.getAddress(), system, tokens)).replaceWithVoid());
        }
        for (MailAttachment attachment : message.getAttachments()) {
            if (attachment.isInline()) continue;
            chain = chain.chain(() -> storeAttachmentResource(session, attachment, system, tokens)
                    .chain(resource -> restrictNewRow(session, system, identity,
                                    (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) resource,
                                    "resource.resourceitemsecuritytoken", "resourceitemid")
                            .chain(() -> ((IEvent) event).addOrReuseResourceItem(session, MailClassifications.MailAttachmentLink.name(),
                                    resource, nz(attachment.getFileName()), system, tokens))).replaceWithVoid());
        }
        return chain;
    }

    private Uni<Void> mailbox(Mutiny.StatelessSession session, ISystems<?, ?> system,
                              com.guicedee.activitymaster.mail.MailIdentity identity, IInvolvedParty<?, ?> owner,
                              IResourceItem<?, ?> resource, String label) {
        // The actor determines the mailbox, never an address taken from the message.
        String key = identity.partyId().toString();
        return ((Uni<List<IArrangement<?, ?>>>) (Uni) get(IArrangementsService.class).findArrangementsByClassification(session,
                MailClassifications.MailboxOwner.name(), key, system, identity.tokens()))
                .chain(rows -> {
                    Uni<IArrangement<?, ?>> box;
                    if (rows.size() > 1) return Uni.createFrom().failure(new SecurityException("Ambiguous mailbox"));
                    if (!rows.isEmpty()) {
                        IArrangement<?, ?> existing = rows.getFirst();
                        box = ((com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) existing)
                                .canWrite(session, system, identity.tokens()).chain(allowed -> Boolean.TRUE.equals(allowed)
                                        ? Uni.createFrom().item(existing) : Uni.createFrom().failure(new SecurityException("Mailbox unavailable")));
                    } else {
                        box = ((Uni<IArrangement<?, ?>>) (Uni) get(IArrangementsService.class).create(session, (UUID) null,
                                MailArrangementTypes.Mailbox.name(), MailClassifications.MailboxOwner.name(), key, system, identity.tokens()))
                                .chain(created -> restrictNewRow(session, system, identity,
                                                (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) created,
                                                "arrangement.arrangementsecuritytoken", "arrangementid")
                                        .chain(() -> ((IArrangement) created).addInvolvedParty(session, owner,
                                                MailClassifications.MailboxOwner.name(), key, system, identity.tokens())).replaceWith(created));
                    }
                    return box.chain(arrangement -> ((IArrangement) arrangement).addOrReuseResourceItem(session,
                            MailClassifications.MailMessageLink.name(), resource, "mailbox", system, identity.tokens())).replaceWithVoid();
                });
    }

    private Uni<Void> restrictNewRow(Mutiny.StatelessSession session, ISystems<?, ?> system,
                                     com.guicedee.activitymaster.mail.MailIdentity identity,
                                     com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable<?, ?, ?, ?> row,
                                     String table, String column) {
        com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService<?> security =
                get(com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService.class);
        com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService<?> flags =
                get(com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService.class);
        return flags.getArchivedFlag(session, system.getEnterprise(), identity.tokens())
                .chain(archived -> session.createNativeQuery("update " + table + " set activeflagid=:flag,effectivetodate=statement_timestamp() "
                                + "where " + column + "=:id and enterpriseid=:enterprise and effectivetodate>statement_timestamp()")
                        .setParameter("flag", archived.getId()).setParameter("id", row.getId())
                        .setParameter("enterprise", identity.enterpriseId()).executeUpdate())
                .chain(() -> flags.getActiveFlag(session, system.getEnterprise(), identity.tokens()))
                .chain(active -> security.getAdministratorsFolder(session, system, identity.tokens())
                        .chain(admin -> row.createSecurityGrant(session, system, system.getEnterprise(), active, admin,
                                true, true, true, true, identity.tokens()))
                        .chain(() -> security.getSecurityToken(session, identity.identityToken(), system, identity.tokens()))
                        .chain(user -> row.createSecurityGrant(session, system, system.getEnterprise(), active, user,
                                true, true, true, true, identity.tokens()))).replaceWithVoid();
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

