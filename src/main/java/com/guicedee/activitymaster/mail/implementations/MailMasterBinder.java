package com.guicedee.activitymaster.mail.implementations;

import com.guicedee.activitymaster.mail.MailSystem;
import com.guicedee.activitymaster.mail.services.IMailImportService;
import com.guicedee.activitymaster.mail.services.IMailBoxService;
import com.guicedee.activitymaster.mail.services.IMailService;
import com.guicedee.activitymaster.mail.services.IMailSystem;
import com.google.inject.PrivateModule;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;

public class MailMasterBinder
		extends PrivateModule
		implements IGuiceModule<MailMasterBinder>
{
	@Override
	protected void configure()
	{
		bind(IMailSystem.class).to(MailSystem.class);
		expose(IMailSystem.class);

		bind(IMailImportService.class).to(MailImportService.class);
		expose(IMailImportService.class);

		bind(IMailBoxService.class).to(MailboxBoxService.class);
		expose(IMailBoxService.class);

		bind(IMailService.class).to(MailService.class);
		expose(IMailService.class);
	}

}
