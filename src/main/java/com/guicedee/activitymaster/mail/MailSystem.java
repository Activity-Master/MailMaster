package com.guicedee.activitymaster.mail;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterDefaultSystem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;
import com.guicedee.activitymaster.mail.services.IMailSystem;

import static com.guicedee.activitymaster.mail.services.IMailService.*;

public class MailSystem
		extends ActivityMasterDefaultSystem<MailSystem>
		implements IMailSystem<MailSystem>, IActivityMasterSystem<MailSystem>
{
	@Inject
	private Provider<ISystemsService<?>> systemsService;
	
	@Override
	public ISystems<?,?> registerSystem(IEnterprise<?,?> enterprise)
	{
		ISystems<?, ?> iSystems = systemsService.get()
		                                        .create(enterprise, getSystemName(), getSystemDescription());
		systemsService.get()
		              .registerNewSystem(enterprise, getSystem(enterprise));
		return iSystems;
	}
	
	@Override
	public void createDefaults(IEnterprise<?,?> enterprise)
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
