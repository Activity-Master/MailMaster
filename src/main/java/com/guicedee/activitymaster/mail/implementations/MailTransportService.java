package com.guicedee.activitymaster.mail.implementations;

import com.google.inject.Inject;
import com.guicedee.activitymaster.mail.engine.VertxMailMapper;
import com.guicedee.activitymaster.mail.servers.MailServer;
import com.guicedee.activitymaster.mail.services.IMailTransportService;
import com.guicedee.activitymaster.mail.services.dto.MailMessage;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default {@link IMailTransportService} implementation built on the reactive Vert.x
 * {@link MailClient}. A pooled, shared client is created per distinct server connection
 * (keyed by host:port/user) and reused for the lifetime of the JVM.
 */
public class MailTransportService implements IMailTransportService<MailTransportService>
{
	private final ConcurrentMap<String, MailClient> clients = new ConcurrentHashMap<>();
	private Vertx vertx;

	@Inject
	public MailTransportService()
	{
	}

	/**
	 * Constructs a transport bound to a specific Vert.x instance (for ad-hoc / test usage that does
	 * not boot the full Guice context).
	 *
	 * @param vertx the Vert.x instance to use
	 */
	public MailTransportService(Vertx vertx)
	{
		this.vertx = vertx;
	}

	@Override
	public Uni<String> send(MailServer<?> server, MailMessage message)
	{
		MailClient client = clientFor(server);
		io.vertx.ext.mail.MailMessage vertxMessage = VertxMailMapper.toVertxMessage(message);
		return Uni.createFrom()
				.completionStage(() -> client.sendMail(vertxMessage).toCompletionStage())
				.map(result -> result.getMessageID());
	}

	private MailClient clientFor(MailServer<?> server)
	{
		String poolName = poolName(server);
		return clients.computeIfAbsent(poolName, name -> {
			MailConfig config = VertxMailMapper.toConfig(server);
			return MailClient.createShared(vertx(), config, name);
		});
	}

	private String poolName(MailServer<?> server)
	{
		return "am-mail:" + server.getHostname() + ":" + server.getTransportProtocol().providerName()
				+ ":" + server.getUsername();
	}

	private Vertx vertx()
	{
		if (vertx == null)
		{
			vertx = IGuiceContext.get(Vertx.class);
		}
		return vertx;
	}
}

