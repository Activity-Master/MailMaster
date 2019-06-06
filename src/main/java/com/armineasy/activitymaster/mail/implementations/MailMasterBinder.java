package com.armineasy.activitymaster.mail.implementations;

import com.armineasy.activitymaster.mail.MailImportService;
import com.armineasy.activitymaster.mail.MailService;
import com.armineasy.activitymaster.mail.MailSystem;
import com.armineasy.activitymaster.mail.services.IMailImportService;
import com.armineasy.activitymaster.mail.services.IMailService;
import com.armineasy.activitymaster.mail.services.IMailSystem;
import com.google.inject.PrivateModule;
import com.jwebmp.guicedinjection.interfaces.IGuiceModule;

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

		bind(IMailService.class).to(MailService.class);
		expose(IMailService.class);
	}

}
