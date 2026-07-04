package com.guicedee.activitymaster.mail.implementations;

import com.guicedee.client.services.config.IGuiceScanModuleInclusions;

import java.util.HashSet;
import java.util.Set;

/**
 * Ensures the Mail Master module's classes are scanned by the GuicedEE classpath scanner so that
 * its {@code ISystemUpdate} installer and other SPI implementations are discovered.
 */
public class MailMasterModuleInclusion implements IGuiceScanModuleInclusions<MailMasterModuleInclusion>
{
	@Override
	public Set<String> includeModules()
	{
		Set<String> set = new HashSet<>();
		set.add("com.guicedee.activitymaster.mail");
		return set;
	}
}

