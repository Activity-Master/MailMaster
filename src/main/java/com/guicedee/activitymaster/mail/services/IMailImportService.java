package com.guicedee.activitymaster.mail.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.mail.services.dto.MailImportTicket;

import java.util.List;


public interface IMailImportService<J extends IMailImportService<J>>
{
	List<MailImportTicket> fromArrangements(List<IArrangement<?,?>> arrangements, String enterpriseName);
	ISystems<?,?> getSystem(String enterpriseName);
	UUID getSystemUUID(String enterpriseName);

	void updateMailImportTicket(MailImportTicket mailImportTicket, String enterpriseName);
}
