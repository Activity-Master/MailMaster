package com.guicedee.activitymaster.mail.engine;

import com.guicedee.activitymaster.mail.servers.MailProtocol;
import com.guicedee.activitymaster.mail.servers.MailSecurity;
import com.guicedee.activitymaster.mail.servers.MailServer;
import com.guicedee.activitymaster.mail.services.dto.MailAddress;
import com.guicedee.activitymaster.mail.services.dto.MailAttachment;
import com.guicedee.activitymaster.mail.services.dto.MailMessage;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.mail.LoginOption;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.StartTLSOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps the mail module's transport-agnostic {@link MailServer} / {@link MailMessage} model onto the
 * Vert.x mail client model ({@link MailConfig} / {@link io.vertx.ext.mail.MailMessage}).
 * <p>
 * This is the bridge that lets the comprehensive {@link MailMessage} DTO (from alias, multiple
 * recipients, reply-to, text + HTML bodies, file and inline attachments) be sent through the
 * reactive Vert.x {@code MailClient}.
 */
public final class VertxMailMapper
{
	private VertxMailMapper()
	{
	}

	/**
	 * Builds a Vert.x {@link MailConfig} from a {@link MailServer}'s transport (SMTP) settings,
	 * translating the {@link MailSecurity} mode and authentication flags.
	 *
	 * @param server the server connection details
	 * @return a configured Vert.x mail config
	 */
	public static MailConfig toConfig(MailServer<?> server)
	{
		MailProtocol protocol = server.getTransportProtocol();
		int port = resolvePort(server, protocol);

		MailConfig config = new MailConfig()
				.setHostname(server.getHostname())
				.setPort(port)
				.setConnectTimeout(server.getConnectTimeoutMs())
				.setKeepAlive(true)
				.setAllowRcptErrors(false);

		switch (server.getSecurity())
		{
			case SSL_TLS ->
			{
				config.setSsl(true);
				config.setStarttls(StartTLSOptions.DISABLED);
			}
			case STARTTLS ->
			{
				config.setSsl(false);
				config.setStarttls(StartTLSOptions.REQUIRED);
			}
			case NONE ->
			{
				config.setSsl(false);
				config.setStarttls(StartTLSOptions.DISABLED);
			}
		}
		if (protocol.isImplicitSsl())
		{
			config.setSsl(true);
		}
		if (server.isTrustAll())
		{
			config.setTrustAll(true);
			config.setHostnameVerificationAlgorithm("");
		}

		if (server.isAuth() && server.getUsername() != null)
		{
			config.setLogin(LoginOption.REQUIRED);
			config.setUsername(server.getUsername());
			config.setPassword(server.getPassword());
		}
		else
		{
			config.setLogin(LoginOption.DISABLED);
		}
		return config;
	}

	private static int resolvePort(MailServer<?> server, MailProtocol protocol)
	{
		Object explicit = server.getExtraProperties().get("mail." + protocol.providerName() + ".port");
		if (explicit != null)
		{
			try
			{
				return Integer.parseInt(explicit.toString());
			}
			catch (NumberFormatException ignored)
			{
				// fall through
			}
		}
		return protocol.defaultPort();
	}

	/**
	 * Builds a Vert.x {@link io.vertx.ext.mail.MailMessage} from the module's {@link MailMessage} DTO,
	 * preserving sender / recipient display-name aliases, reply-to, both body parts and attachments
	 * (file and inline).
	 *
	 * @param dto the message to convert
	 * @return the Vert.x mail message
	 */
	public static io.vertx.ext.mail.MailMessage toVertxMessage(MailMessage dto)
	{
		io.vertx.ext.mail.MailMessage vertxMessage = new io.vertx.ext.mail.MailMessage();

		if (dto.getFrom() != null)
		{
			vertxMessage.setFrom(dto.getFrom().toString());
		}
		if (!dto.getTo().isEmpty())
		{
			vertxMessage.setTo(render(dto.getTo()));
		}
		if (!dto.getCc().isEmpty())
		{
			vertxMessage.setCc(render(dto.getCc()));
		}
		if (!dto.getBcc().isEmpty())
		{
			vertxMessage.setBcc(render(dto.getBcc()));
		}
		vertxMessage.setSubject(dto.getSubject());
		if (dto.getTextBody() != null)
		{
			vertxMessage.setText(dto.getTextBody());
		}
		if (dto.getHtmlBody() != null)
		{
			vertxMessage.setHtml(dto.getHtmlBody());
		}

		MultiMap headers = MultiMap.caseInsensitiveMultiMap();
		for (Map.Entry<String, String> header : dto.getHeaders().entrySet())
		{
			headers.add(header.getKey(), header.getValue());
		}
		if (!dto.getReplyTo().isEmpty())
		{
			headers.add("Reply-To", dto.getReplyTo().stream().map(MailAddress::toString).collect(Collectors.joining(", ")));
		}
		if (!headers.isEmpty())
		{
			vertxMessage.setHeaders(headers);
		}

		List<io.vertx.ext.mail.MailAttachment> files = new ArrayList<>();
		List<io.vertx.ext.mail.MailAttachment> inline = new ArrayList<>();
		for (MailAttachment attachment : dto.getAttachments())
		{
			io.vertx.ext.mail.MailAttachment converted = toVertxAttachment(attachment);
			if (attachment.isInline())
			{
				inline.add(converted);
			}
			else
			{
				files.add(converted);
			}
		}
		if (!files.isEmpty())
		{
			vertxMessage.setAttachment(files);
		}
		if (!inline.isEmpty())
		{
			vertxMessage.setInlineAttachment(inline);
		}
		return vertxMessage;
	}

	private static io.vertx.ext.mail.MailAttachment toVertxAttachment(MailAttachment attachment)
	{
		io.vertx.ext.mail.MailAttachment vertxAttachment = io.vertx.ext.mail.MailAttachment.create()
				.setName(attachment.getFileName())
				.setContentType(attachment.getContentType())
				.setData(Buffer.buffer(attachment.getContent() == null ? new byte[0] : attachment.getContent()));
		if (attachment.isInline())
		{
			vertxAttachment.setDisposition("inline");
			if (attachment.getContentId() != null)
			{
				vertxAttachment.setContentId("<" + attachment.getContentId() + ">");
			}
		}
		else
		{
			vertxAttachment.setDisposition("attachment");
		}
		return vertxAttachment;
	}

	private static List<String> render(List<MailAddress> addresses)
	{
		return addresses.stream().map(MailAddress::toString).collect(Collectors.toList());
	}
}

