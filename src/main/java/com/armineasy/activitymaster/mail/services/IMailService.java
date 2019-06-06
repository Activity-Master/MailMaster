package com.armineasy.activitymaster.mail.services;

import com.armineasy.activitymaster.activitymaster.db.entities.arrangement.ArrangementXInvolvedParty;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.mail.MailService;

import javax.mail.Folder;
import javax.mail.MessagingException;
import java.util.UUID;

public interface IMailService<J extends IMailService<J>>
{
	InvolvedParty findByEmail(String emailAddress, ISystems<?> systems, UUID... identityToken);

	ISystems<?> getMailSystem(IEnterprise<?> enterprise);

	MailService login();

	ArrangementXInvolvedParty createArrangement(InvolvedParty ip, String value, UUID... identityToken);

	MailService loadFolders() throws MessagingException;

	long getNumberOfMails() throws MessagingException;

	Folder addFolder(String folder, String folderPath) throws MessagingException;
}
