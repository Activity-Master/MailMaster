package com.guicedee.activitymaster.mail.implementations;

import com.google.inject.PrivateModule;
import com.guicedee.activitymaster.mail.MailSystem;
import com.guicedee.activitymaster.mail.services.IMailFsdmService;
import com.guicedee.activitymaster.mail.services.IMailSystem;
import com.guicedee.activitymaster.mail.services.IMailTransportService;
import com.guicedee.client.services.lifecycle.IGuiceModule;

/**
 * Guice bindings for the Mail Master module: the mail system, the reactive Vert.x-backed transport
 * service and the FSDM mapping service.
 */
public class MailMasterBinder
		extends PrivateModule
		implements IGuiceModule<MailMasterBinder>
{
	@Override
	protected void configure()
	{
		bind(IMailSystem.class).to(MailSystem.class);
		expose(IMailSystem.class);

		bind(IMailTransportService.class).to(MailTransportService.class);
		expose(IMailTransportService.class);

		bind(IMailFsdmService.class).to(MailFsdmService.class);
		expose(IMailFsdmService.class);
	}

}
