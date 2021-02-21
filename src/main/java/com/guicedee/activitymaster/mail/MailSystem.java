package com.guicedee.activitymaster.mail;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.guicedee.activitymaster.core.services.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.core.services.IActivityMasterSystem;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.system.ActivityMasterDefaultSystem;
import com.guicedee.activitymaster.core.services.system.ISystemsService;
import com.guicedee.activitymaster.mail.services.IMailSystem;

import static com.guicedee.activitymaster.mail.services.IMailService.*;

public class MailSystem
		extends ActivityMasterDefaultSystem<MailSystem>
		implements IMailSystem<MailSystem>, IActivityMasterSystem<MailSystem>
{
	@Inject
	private Provider<ISystemsService<?>> systemsService;
	
	@Override
	public void registerSystem(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		systemsService.get()
		              .create(enterprise, getSystemName(), getSystemDescription());
		systemsService.get()
		              .registerNewSystem(enterprise, getSystem(enterprise));
	}
	
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
		return MailSystemName;
	}

	@Override
	public String getSystemDescription()
	{
		return "The system for handling the receiving, importing and sending of emails";
	}
}
