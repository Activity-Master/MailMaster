package com.guicedee.activitymaster.mail.services;

import com.guicedee.activitymaster.client.services.builders.warehouse.arrangements.IArrangement;
import com.guicedee.activitymaster.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.mail.implementations.MailboxBoxService;
import com.guicedee.activitymaster.mail.servers.MailServer;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;

import java.io.Closeable;
import java.util.List;
import java.util.UUID;

public interface IMailBoxService<J extends IMailBoxService<J>> extends Closeable
{
	static IMailBoxService get(MailServer server)
	{
		return new MailboxBoxService(server);
	}

	IInvolvedParty<?,?> findByEmail(String emailAddress, ISystems<?,?> systems, UUID... identityToken);

	ISystems<?,?> getMailSystem(IEnterprise<?,?> enterprise);
	UUID getMailUUID(IEnterprise<?,?> enterprise);

	MailboxBoxService login();

	IArrangement<?,?> createArrangement(IInvolvedParty<?,?> ip, String value, UUID... identityToken);

	MailboxBoxService loadFolders() throws MessagingException;

	long getNumberOfMails() throws MessagingException;

	List<Folder> getFolders(String folderPath) throws MessagingException;

	Folder addFolder(String folder, String folderPath) throws MessagingException;

	long getTotalFolders();

	long getTotalSize();

	long getTotalMails();
}
