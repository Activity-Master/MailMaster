package com.armineasy.activitymaster.mail.importer;

import com.armineasy.activitymaster.mail.MailService;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;

public class MessageImportJob
		implements Runnable
{
	private MailService source;
	private MailService dest;
	private final String prefix;
	private final String a;
	private final Integer b;

	public MessageImportJob(MailService source, MailService dest, String prefix, String a, Integer b)
	{
		this.source = source;
		this.dest = dest;
		this.prefix = prefix;
		this.a = a;
		this.b = b;
	}

	@Override
	public void run()
	{
		try
		{
			Folder destFolder;
			Folder srcFolder;
			if (!dest.getFolderMessages()
			         .containsKey(prefix + a))
			{

				destFolder = dest.addFolder(a, prefix);

				dest.getFolderMessages()
				    .put(destFolder.getFullName(), 0);
				srcFolder = source.getFolder(a);
			}
			else
			{
				srcFolder = source.getFolder(a);
				destFolder = dest.getFolder(a);
			}
			if (!srcFolder.isOpen())
			{
				srcFolder.open(Folder.READ_ONLY);
			}
			if (!destFolder.isOpen())
			{
				destFolder.open(Folder.READ_WRITE);
			}
			UIDFolder foldDest = (UIDFolder) destFolder;
			UIDFolder foldSrc = (UIDFolder) srcFolder;
			for (int i = 1; i <= srcFolder.getMessageCount(); i++)
			{
				Message m = srcFolder.getMessage(i);
				Long messageId = foldSrc.getUID(m);
				try
				{
					Message exists = foldDest.getMessageByUID(messageId);
					if (exists == null)
					{
						destFolder.appendMessages(new Message[]{m});
					}
				}
				catch (MessagingException me)
				{
					me.printStackTrace();
				}
			}
		}
		catch (MessagingException e)
		{
			e.printStackTrace();
		}
	}
}
