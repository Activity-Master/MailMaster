package com.guicedee.activitymaster.mail;

import com.google.inject.Singleton;
import com.guicedee.activitymaster.core.services.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.core.services.IActivityMasterSystem;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.*;
import com.guicedee.activitymaster.mail.services.IMailSystem;
import com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.guicedee.activitymaster.mail.services.enumerations.MailImportArrangementTypes;
import com.guicedee.activitymaster.mail.services.enumerations.MailImportStage;
import com.guicedee.guicedinjection.interfaces.JobService;
import com.guicedee.logger.LogFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications.*;
import static com.guicedee.activitymaster.mail.services.classifications.MailSystemResourceItemClassifications.*;
import static com.guicedee.activitymaster.mail.services.enumerations.MailImportResourceItemTypes.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@Singleton
public class MailSystem
		extends ActivityMasterDefaultSystem<MailSystem>
		implements IMailSystem<MailSystem>, IActivityMasterSystem<MailSystem>
{
	@Override
	public void createDefaults(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{

	}

	@Override
	public int totalTasks()
	{
		return 0;
	}

	@Override
	public String getSystemName()
	{
		return "Mail Master";
	}

	@Override
	public String getSystemDescription()
	{
		return "The system for handling the receiving, importing and sending of emails";
	}
}
