package com.guicedee.activitymaster.mail.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.mail.services.dto.MailAddress;
import com.guicedee.activitymaster.mail.services.dto.MailAttachment;
import com.guicedee.activitymaster.mail.services.dto.MailMessage;
import com.guicedee.activitymaster.mail.services.enumerations.MailDirection;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;

/**
 * Maps mail messages onto the FSDM warehouse: involved parties (resolved by email address, with
 * support for address aliases), mail events, resource items (the message body plus each attachment)
 * and a per-owner mailbox arrangement.
 * <p>
 * The granular operations run on a caller-supplied {@link Mutiny.StatelessSession} — the leanest
 * session for these transport-shaped writes (no first-level cache, no dirty-checking, no auto-flush).
 * The high-level {@link #ingest} pipeline parallelises the independent work, running each branch on
 * its <strong>own</strong> stateless session (Hibernate Reactive forbids concurrent operations on a
 * single session, so parallelism is achieved across sessions, not within one).
 *
 * @param <J> the concrete service type
 */
public interface IMailFsdmService<J extends IMailFsdmService<J>>
{
	/** The Mail Master system name. */
	String MailSystemName = "Mail Master";

	/**
	 * Finds an involved party by email address, creating one when none exists.
	 *
	 * @param session the stateless session
	 * @param address the email address
	 * @param system  the system performing the work
	 * @param token   optional security identity tokens
	 * @return a Uni emitting the resolved involved party
	 */
	Uni<IInvolvedParty<?, ?>> findOrCreateParty(Mutiny.StatelessSession session, MailAddress address, ISystems<?, ?> system, UUID... token);

	/**
	 * Adds an additional email address (an <em>alias</em>) to an existing involved party.
	 *
	 * @param session    the stateless session
	 * @param party      the party to add the alias to
	 * @param aliasEmail the additional email address
	 * @param system     the system performing the work
	 * @param token      optional security identity tokens
	 * @return a Uni emitting the party
	 */
	Uni<IInvolvedParty<?, ?>> addEmailAlias(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, String aliasEmail, ISystems<?, ?> system, UUID... token);

	/**
	 * Stores a mail message body as an FSDM resource item (with its metadata classifications).
	 *
	 * @param session the stateless session
	 * @param message the message to store
	 * @param system  the system performing the work
	 * @param token   optional security identity tokens
	 * @return a Uni emitting the message-body resource item
	 */
	Uni<IResourceItem<?, ?>> storeMessageResource(Mutiny.StatelessSession session, MailMessage message, ISystems<?, ?> system, UUID... token);

	/**
	 * Stores a mail attachment as an FSDM resource item (with its metadata classifications).
	 *
	 * @param session    the stateless session
	 * @param attachment the attachment to store
	 * @param system     the system performing the work
	 * @param token      optional security identity tokens
	 * @return a Uni emitting the attachment resource item
	 */
	Uni<IResourceItem<?, ?>> storeAttachmentResource(Mutiny.StatelessSession session, MailAttachment attachment, ISystems<?, ?> system, UUID... token);

	/**
	 * Full ingest pipeline. Records a mail event for the message, resolves and links the sender and
	 * every recipient (by email address, creating parties as needed), stores the message body and
	 * attachments as resource items linked to the event, and wires the message into the owning
	 * mailbox arrangement.
	 * <p>
	 * Independent steps run in parallel, each on its own session: the event is created (stateful), the
	 * parties are resolved and the resource items are stored (stateless). Once those are committed, the
	 * links and classifications are written in parallel too. The method opens and manages its own
	 * sessions, so it is the canonical top-level entry point (REST / event-bus).
	 *
	 * @param message           the parsed mail message
	 * @param mailboxOwnerEmail the email address of the mailbox the event belongs to (may be {@code null})
	 * @param direction         whether the message was received or sent
	 * @param enterpriseName    the enterprise the message belongs to
	 * @return a Uni emitting the created mail event's id
	 */
	Uni<UUID> ingest(MailMessage message, String mailboxOwnerEmail, MailDirection direction, String enterpriseName);
}

