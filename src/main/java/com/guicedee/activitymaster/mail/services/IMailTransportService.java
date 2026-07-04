package com.guicedee.activitymaster.mail.services;

import com.guicedee.activitymaster.mail.servers.MailServer;
import com.guicedee.activitymaster.mail.services.dto.MailMessage;
import io.smallrye.mutiny.Uni;

/**
 * Reactive SMTP transport backed by the <strong>Vert.x Mail Client</strong>.
 * <p>
 * Sends {@link MailMessage}s through any SMTP/SMTPS {@link MailServer} (supporting the full matrix of
 * login modes and TLS security), with multiple recipients (To/Cc/Bcc), reply-to, sender display-name
 * <em>aliases</em>, text + HTML bodies and file / inline attachments. A pooled Vert.x
 * {@code MailClient} is created (and shared) per distinct server connection.
 *
 * @param <J> the concrete service type
 */
public interface IMailTransportService<J extends IMailTransportService<J>>
{
	/**
	 * Sends a message through the supplied server, returning the assigned message id.
	 *
	 * @param server  the transport (SMTP) server to send through
	 * @param message the message to send
	 * @return a Uni emitting the server-assigned message id on success
	 */
	Uni<String> send(MailServer<?> server, MailMessage message);
}


