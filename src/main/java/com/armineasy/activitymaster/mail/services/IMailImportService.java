package com.armineasy.activitymaster.mail.services;

import com.armineasy.activitymaster.activitymaster.services.classifications.enterprise.IEnterpriseName;
import com.armineasy.activitymaster.activitymaster.services.dto.IArrangement;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.mail.services.dto.MailImportTicket;

import java.util.List;
import java.util.UUID;

public interface IMailImportService<J extends IMailImportService<J>>
{
	List<MailImportTicket> fromArrangements(List<IArrangement<?>> arrangements, IEnterpriseName<?> enterpriseName);
	ISystems<?> getSystem(IEnterpriseName<?> enterpriseName);
	UUID getSystemUUID(IEnterpriseName<?> enterpriseName);

	void updateMailImportTicket(MailImportTicket mailImportTicket, IEnterpriseName<?> enterpriseName);
}
