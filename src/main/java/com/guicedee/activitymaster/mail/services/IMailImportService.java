package com.guicedee.activitymaster.mail.services;

import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.core.services.dto.IArrangement;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.mail.services.dto.MailImportTicket;

import java.util.List;
import java.util.UUID;

public interface IMailImportService<J extends IMailImportService<J>>
{
	List<MailImportTicket> fromArrangements(List<IArrangement<?>> arrangements, IEnterpriseName<?> enterpriseName);
	ISystems<?> getSystem(IEnterpriseName<?> enterpriseName);
	UUID getSystemUUID(IEnterpriseName<?> enterpriseName);

	void updateMailImportTicket(MailImportTicket mailImportTicket, IEnterpriseName<?> enterpriseName);
}
