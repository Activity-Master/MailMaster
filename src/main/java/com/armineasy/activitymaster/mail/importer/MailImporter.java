package com.armineasy.activitymaster.mail.importer;

import com.armineasy.activitymaster.mail.MailService;
import com.jwebmp.guicedinjection.interfaces.JobService;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import java.util.Map;

import static java.util.concurrent.TimeUnit.*;

public class MailImporter
{
	public void importMail(MailService source, MailService dest) throws MessagingException
	{
		source.loadFolders();
		dest.loadFolders();

		System.out.println("Total Source Mails : " + source.getNumberOfMails());
		System.out.println("Total Dest Mails : " + dest.getNumberOfMails());

		String prefix = "";
		for (Map.Entry<String, Integer> entry : source.getFolderMessages()
		                                              .entrySet())
		{
			String a = entry.getKey();
			Integer b = entry.getValue();
			MessageImportJob importJob = new MessageImportJob(source, dest, prefix, a, b);
			JobService.getInstance()
			          .addJob("MailMasterMigrationImports", importJob);
			JobService.getInstance().waitForJob("MailMasterMigrationImports",1L,DAYS);
		}
	}
}
