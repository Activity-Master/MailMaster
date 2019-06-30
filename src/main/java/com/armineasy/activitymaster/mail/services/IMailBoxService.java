package com.armineasy.activitymaster.mail.services;

import com.armineasy.activitymaster.activitymaster.services.dto.IArrangement;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.mail.implementations.MailboxBoxService;
import com.armineasy.activitymaster.mail.servers.MailServer;

import javax.mail.Folder;
import javax.mail.MessagingException;
import java.io.Closeable;
import java.util.List;
import java.util.UUID;

public interface IMailBoxService<J extends IMailBoxService<J>> extends Closeable
{
	static IMailBoxService get(MailServer server)
	{
		return new MailboxBoxService(server);
	}

	IInvolvedParty<?> findByEmail(String emailAddress, ISystems<?> systems, UUID... identityToken);

	ISystems<?> getMailSystem(IEnterprise<?> enterprise);
	UUID getMailUUID(IEnterprise<?> enterprise);

	MailboxBoxService login();

	IArrangement<?> createArrangement(IInvolvedParty<?> ip, String value, UUID... identityToken);

	MailboxBoxService loadFolders() throws MessagingException;

	long getNumberOfMails() throws MessagingException;

	List<Folder> getFolders(String folderPath) throws MessagingException;

	Folder addFolder(String folder, String folderPath) throws MessagingException;

	long getTotalFolders();

	long getTotalSize();

	long getTotalMails();
}
