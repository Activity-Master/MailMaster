package com.guicedee.activitymaster.mail;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.administration.MasterDefaultSystem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.IMasterSystem;
import com.guicedee.activitymaster.mail.services.IMailSystem;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import static com.guicedee.activitymaster.mail.services.IMailFsdmService.MailSystemName;

/**
 * The Mail Master FSDM system. Registered as an {@code IMasterSystem} so it is provisioned into
 * every enterprise; the taxonomy itself is installed by the {@code MailMasterInstall} system update.
 */
@Singleton
public class MailSystem
		extends MasterDefaultSystem<MailSystem>
		implements IMailSystem<MailSystem>, IMasterSystem<MailSystem>
{
	@Inject
	private Provider<ISystemsService<?>> systemsService;

	@Override
	public Uni<ISystems<?, ?>> registerSystem(Mutiny.Session session, IEnterprise<?, ?> enterprise)
	{
		return systemsService.get()
				.create(session, enterprise, getSystemName(), getSystemDescription())
				.chain(system -> getSystem(session, enterprise)
						.chain(sys -> systemsService.get().registerNewSystem(session, enterprise, sys))
						.chain(() -> Uni.createFrom().item(system)));
	}

	@Override
	public Uni<Void> createDefaults(Mutiny.Session session, IEnterprise<?, ?> enterprise)
	{
		return Uni.createFrom().voidItem();
	}

	@Override
	public Uni<Void> createDefaults(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
	{
		return Uni.createFrom().voidItem();
	}

	@Override
	public int totalTasks()
	{
		return 0;
	}

	@Override
	public String getSystemName()
	{
		return MailSystemName;
	}

	@Override
	public String getSystemDescription()
	{
		return "The system for sending, receiving and warehousing emails";
	}
}

