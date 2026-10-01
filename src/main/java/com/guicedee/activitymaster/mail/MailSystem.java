package com.guicedee.activitymaster.mail;

import com.google.inject.Singleton;
import com.guicedee.activitymaster.fsdm.client.services.administration.MasterDefaultPlugin;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.mail.services.IMailSystem;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import static com.guicedee.activitymaster.mail.services.IMailFsdmService.MailSystemName;

/**
 * The Mail Master FSDM plugin. Its Master lifecycle provisions the capability into
 * every enterprise; the taxonomy itself is installed by the {@code MailMasterInstall} system update.
 */
@Singleton
public class MailSystem
		extends MasterDefaultPlugin<MailSystem>
		implements IMailSystem<MailSystem>
{
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

