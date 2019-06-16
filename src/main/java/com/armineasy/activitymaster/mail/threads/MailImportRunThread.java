package com.armineasy.activitymaster.mail.threads;

import com.armineasy.activitymaster.activitymaster.services.classifications.enterprise.IEnterpriseName;
import com.armineasy.activitymaster.activitymaster.threads.TransactionalIdentifiedThread;
import com.armineasy.activitymaster.mail.MailService;
import com.armineasy.activitymaster.mail.servers.GMailMailServer;
import com.armineasy.activitymaster.mail.servers.SaNrgMailServer;
import com.armineasy.activitymaster.mail.services.IMailImportService;
import com.armineasy.activitymaster.mail.services.dto.MailImportTicket;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

import static com.jwebmp.guicedinjection.GuiceContext.*;

@Log
@Data
@NoArgsConstructor
public class MailImportRunThread
		extends TransactionalIdentifiedThread
		implements Runnable
{
	private MailImportTicket ticket;
	private IEnterpriseName<?> enterprise;

	private MailService source;
	private MailService dest;

	private String prefix;
	private String a;
	private Integer b;

	private long maxMails = 5L;
	private long maxSize = 15032385536L;

	private long startMail = 1L;

	private long currentSize = 0L;
	private long currentMails = 0L;

	public MailImportRunThread(IEnterpriseName<?> enterprise, MailImportTicket ticket)
	{
		this.enterprise = enterprise;
		this.ticket = ticket;
		configure();
	}

	public void configure(){
		this.source = new MailService(new GMailMailServer(ticket.getGmailAddress(), ticket.getGmailPassword()));
		this.dest = new MailService(new SaNrgMailServer(ticket.getSanrgMailAddress(), ticket.getSanrgMailPassword()));
		this.startMail = ticket.getCompletedMails() + 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void perform()
	{

		ticket.setLastRunDate(new Date().toString());
		ticket.setPaused(false);
		ticket.setStatus("STARTING");
		get(IMailImportService.class)
		            .updateMailImportTicket(ticket,enterprise);
		try
		{
			goThrough();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.log(Level.SEVERE,"MAIL IMPORT OOPS",e);
			ticket.setStatus("FAILED - " + e.getMessage());
			ticket.setPaused(true);
			ticket.setTotalSizeForToday(currentSize);
			get(IMailImportService.class)
			            .updateMailImportTicket(ticket, enterprise);
		}
	}

	private void goThrough() throws MessagingException
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
			this.prefix = prefix;
			this.a = a;
			this.b = b;
			runIt();
		}
	}

	private void runIt() throws MessagingException
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
		//for (int i = 1; i <= srcFolder.getMessageCount(); i++)
		for (int i = (int) startMail; i <= (startMail + maxMails) && currentSize <= maxSize; i++)
		{
			Message m = srcFolder.getMessage(i);
			Long messageId = foldSrc.getUID(m);
			Message exists = foldDest.getMessageByUID(messageId);
			//if (exists == null)
			{
				try
				{
					destFolder.appendMessages(new Message[]{m});
					ticket.setCompletedMails(startMail + currentMails);
					this.currentMails++;
					this.currentSize += m.getSize();
					ticket.setCompletedSize(ticket.getCompletedSize() + m.getSize());
					ticket.setTotalSizeForToday(currentSize);
					get(IMailImportService.class).updateMailImportTicket(ticket,enterprise);
				}catch(MessagingException me)
				{
					log.log(Level.WARNING, "Message UID does not exist [" + messageId + "]",me);
				}
				catch(Exception me)
				{
					log.log(Level.SEVERE, "Big Exception In Message UID [" + messageId + "]",me);
				}
			}
		}
	}
}
