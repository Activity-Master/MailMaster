package com.armineasy.activitymaster.mail.importer;

import com.armineasy.activitymaster.mail.MailService;
import com.jwebmp.guicedinjection.interfaces.JobService;
import lombok.Data;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import java.util.Map;

import static java.util.concurrent.TimeUnit.*;

@Data
public class MailImporter
{
	public void importMail(MailService source, MailService dest) throws MessagingException
	{

	}
}
