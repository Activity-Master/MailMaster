package com.armineasy.activitymaster.mail.threads;

import com.armineasy.activitymaster.activitymaster.services.classifications.enterprise.IEnterpriseName;
import com.armineasy.activitymaster.activitymaster.services.dto.IArrangement;
import com.armineasy.activitymaster.activitymaster.services.dto.IRelationshipValue;
import com.armineasy.activitymaster.activitymaster.services.dto.IResourceItem;
import com.armineasy.activitymaster.activitymaster.threads.TransactionalIdentifiedThread;
import com.armineasy.activitymaster.mail.MailSystem;
import com.armineasy.activitymaster.mail.implementations.MailboxBoxService;
import com.armineasy.activitymaster.mail.servers.GMailMailServer;
import com.armineasy.activitymaster.mail.servers.SaNrgMailServer;
import com.armineasy.activitymaster.mail.services.IMailImportService;
import com.armineasy.activitymaster.mail.services.dto.MailFoldersStatus;
import com.armineasy.activitymaster.mail.services.dto.MailImportTicket;
import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.armineasy.activitymaster.mail.services.classifications.MailSystemResourceItemClassifications.*;
import static com.armineasy.activitymaster.mail.services.enumerations.MailImportResourceItemTypes.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

public class MailImportRunThread
		extends TransactionalIdentifiedThread
		implements Runnable
{
	private static final Logger log = Logger.getLogger(MailImportRunThread.class.getName());

	private IArrangement<?> arrangement;

	private MailImportTicket ticket;
	private IEnterpriseName<?> enterprise;

	private MailboxBoxService source;
	private MailboxBoxService dest;

	private String prefix;
	private String currentSourceFolderName;

	private long maxMails = 5000L;
	//private long maxSize = 15032385536L;
	private long maxSize = 2147483648L;

	private long startMail = 1L;

	private long currentSize = 0L;
	private long currentMails = 0L;

	private String currentFolder;

	private MailFoldersStatus foldersStatus;

	public MailImportRunThread(IArrangement<?> arrangement, IEnterpriseName<?> enterprise, MailImportTicket ticket)
	{
		this.arrangement = arrangement;
		this.enterprise = enterprise;
		this.ticket = ticket;
		configure();
	}

	public MailImportRunThread()
	{
	}

	public void configure()
	{
		this.source = new MailboxBoxService(new GMailMailServer(ticket.getGmailAddress(), ticket.getGmailPassword()));
		this.dest = new MailboxBoxService(new SaNrgMailServer(ticket.getSanrgMailAddress(), ticket.getSanrgMailPassword()));
		this.startMail = ticket.getCompletedMails() + 1;
		this.currentFolder = ticket.getCurrentFolder();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void perform()
	{
		ticket.setLastRunDate(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss.SSS")
		                                       .format(LocalDateTime.now()));
		ticket.setPaused(false);
		ticket.setStatus("STARTING");
		get(IMailImportService.class)
				.updateMailImportTicket(ticket, enterprise);
		try
		{
			Map<String, String> foldersToWorkOn = createFolders(dest, source);
			goThrough(foldersToWorkOn);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.log(Level.SEVERE, "MAIL IMPORT OOPS", e);
			ticket.setStatus("FAILED - " + e.getMessage());
			ticket.setPaused(true);
			ticket.setTotalSizeForToday(currentSize);
			get(IMailImportService.class)
					.updateMailImportTicket(ticket, enterprise);
		}
	}

	private void goThrough(Map<String, String> folderMappings) throws MessagingException
	{
		String prefix = "";
		for (Map.Entry<String, String> entry : folderMappings
				                                       .entrySet())
		{
			String a = entry.getKey();
			String b = entry.getValue();

			if (!a.equals(currentFolder))
			{
				continue;
			}

			this.prefix = prefix;
			this.currentSourceFolderName = a;
			runIt(dest, source, folderMappings);
		}
	}

	private Map<String, String> createFolders(MailboxBoxService dest, MailboxBoxService src)
	{
		Map<String, String> folderMappings = new HashMap<>();
		boolean isGmail = src.getServer() instanceof GMailMailServer;

		for (Map.Entry<String, Integer> entry : source.getFolderMessages()
		                                              .entrySet())
		{
			String key = entry.getKey();
			Integer value = entry.getValue();
			String result = key;
			if (isGmail && !key.equalsIgnoreCase("inbox"))
			{
				if (key.startsWith("[Gmail]"))
				{
					result = result.replace("[Gmail]/", "");
					try
					{
						IMAPFolder imf = (IMAPFolder) src.getFolder(key);
						for (String attribute : imf.getAttributes())
						{
							if (attribute.contains("\\Trash"))
							{
								result = "Trash";
								break;
							}
							if (attribute.contains("\\Sent"))
							{
								result = "Sent";
								break;
							}
							if (attribute.contains("\\Junk"))
							{
								result = "Spam";
								break;
							}
						}

						String[] attrs = imf.getAttributes();
						List<String> attrList = Arrays.asList(attrs);
						if (attrList.contains("\\All") ||
						    attrList.contains("\\Important") ||
						    attrList.contains("\\Flagged") ||
						    attrList.contains("\\Starred")
						)
						{
							continue;
						}

						boolean doneChildren = false;
						String allOfIt = result;
						Folder labelFolder = dest.getFolder(allOfIt);
						while (!doneChildren)
						{
							if (!labelFolder.exists())
							{
								labelFolder.create(Folder.HOLDS_MESSAGES);
								log.log(Level.INFO, "Created Mail Folder : " + allOfIt);
							}
							if (allOfIt.contains("/"))
							{
								allOfIt = allOfIt.substring(allOfIt.indexOf('/') + 1);
								labelFolder = dest.getFolder(allOfIt);
							}
							else
							{
								doneChildren = true;
							}
						}
					}
					catch (MessagingException e)
					{
						log.log(Level.SEVERE, "Cannot create gmail real destination", e);
					}
					folderMappings.put(key, result);
				}
				else
				{
					log.log(Level.WARNING, "Not a real gmail inbox - " + key);
					try
					{

						boolean doneChildren = false;
						String allOfIt = result;
						Folder labelFolder = dest.getFolder(allOfIt);
						while (!doneChildren)
						{
							if (!labelFolder.exists())
							{
								labelFolder.create(Folder.HOLDS_MESSAGES);
								log.log(Level.INFO, "Created Mail Folder : " + allOfIt);
							}
							if (allOfIt.contains("/"))
							{
								allOfIt = allOfIt.substring(allOfIt.indexOf('/') + 1);
								labelFolder = dest.getFolder(allOfIt);
							}
							else
							{
								doneChildren = true;
							}
						}
					}
					catch (MessagingException e)
					{
						log.log(Level.SEVERE, "Can't create destination label folder", e);
					}
				}
			}
			else
			{
				folderMappings.put(result, result);
			}
		}

		for (Map.Entry<String, String> entry : folderMappings.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			MailFoldersStatus foldersStatus = new MailFoldersStatus();
			List objects = arrangement.findAll(FolderStatusObject, key, MailSystem.getNewSystem()
			                                                                      .get(arrangement.getEnterpriseID()),
			                                   MailSystem.getSystemTokens()
			                                             .get(arrangement.getEnterpriseID()));

			for (Object object : objects)
			{

				IRelationshipValue<IArrangement<?>, IResourceItem<?>, ?> relVal = arrangement.addOrReuse(FolderStatusObject, FolderStatusResourceItem,
				                                                                                         foldersStatus.toString(),
				                                                                                         foldersStatus.toString(),
				                                                                                         foldersStatus.toString()
				                                                                                                      .getBytes(),
				                                                                                         "application/json",
				                                                                                         MailSystem.getNewSystem()
				                                                                                                   .get(arrangement.getEnterpriseID()),
				                                                                                         MailSystem.getSystemTokens()
				                                                                                                   .get(arrangement.getEnterpriseID()
				                                                                                                       ));
				System.out.println("Stuff");

			}


		}
		return folderMappings;
	}

	private void runIt(MailboxBoxService dest, MailboxBoxService src, Map<String, String> folderMappings) throws MessagingException
	{
		String destFolderName = folderMappings.get(currentSourceFolderName);
		Folder destFolder = dest.getFolder(destFolderName);
		Folder srcFolder = src.getFolder(currentSourceFolderName);

		try
		{
			if (!srcFolder.isOpen())
			{
				srcFolder.open(Folder.READ_ONLY);
			}
			if (!destFolder.isOpen())
			{
				destFolder.open(Folder.READ_WRITE);
			}
		}
		catch (FolderNotFoundException nfe)
		{
			log.log(Level.WARNING, "Folder not found - " + srcFolder.getFullName(), nfe);
			return;
		}
		try
		{
			UIDFolder foldDest = (UIDFolder) destFolder;
			UIDFolder foldSrc = (UIDFolder) srcFolder;
			ticket.setCurrentFolder(currentSourceFolderName);
			setCurrentFolder(currentSourceFolderName);
			log.info("Starting Folder : " + currentSourceFolderName);
			//for (int i = 1; i <= srcFolder.getMessageCount(); i++)
			for (int i = (int) startMail; i <= (startMail + maxMails) && currentSize <= maxSize; i++)
			{
				try
				{
					Message m = srcFolder.getMessage(i);
					Long messageId = foldSrc.getUID(m);
					//if (exists == null)
					destFolder.appendMessages(new Message[]{m});
					ticket.setCompletedMails(startMail + currentMails);
					this.currentMails++;
					this.currentSize += m.getSize();
					ticket.setCompletedSize(ticket.getCompletedSize() + m.getSize());
					ticket.setTotalSizeForToday(currentSize);
					get(IMailImportService.class).updateMailImportTicket(ticket, enterprise);
				}
				catch (MessagingException me)
				{
					log.log(Level.WARNING, "Message UID does not exist [" + i + "] - Moving onto the next folder starting at 0", me);
					ticket.setCompletedMails(0);
				}
				catch (Exception me)
				{
					log.log(Level.SEVERE, "Big Exception In Message UID [" + i + "]", me);
				}
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Folder is not a UID folder", e);
		}
	}

	public MailImportTicket getTicket()
	{
		return this.ticket;
	}

	public IEnterpriseName<?> getEnterprise()
	{
		return this.enterprise;
	}

	public MailboxBoxService getSource()
	{
		return this.source;
	}

	public MailboxBoxService getDest()
	{
		return this.dest;
	}


	public long getMaxMails()
	{
		return this.maxMails;
	}

	public long getMaxSize()
	{
		return this.maxSize;
	}

	public long getStartMail()
	{
		return this.startMail;
	}

	public long getCurrentSize()
	{
		return this.currentSize;
	}

	public long getCurrentMails()
	{
		return this.currentMails;
	}

	public void setTicket(MailImportTicket ticket)
	{
		this.ticket = ticket;
	}

	public void setEnterprise(IEnterpriseName<?> enterprise)
	{
		this.enterprise = enterprise;
	}

	public void setSource(MailboxBoxService source)
	{
		this.source = source;
	}

	public void setDest(MailboxBoxService dest)
	{
		this.dest = dest;
	}

	public void setMaxMails(long maxMails)
	{
		this.maxMails = maxMails;
	}

	public void setMaxSize(long maxSize)
	{
		this.maxSize = maxSize;
	}

	public void setStartMail(long startMail)
	{
		this.startMail = startMail;
	}

	public void setCurrentSize(long currentSize)
	{
		this.currentSize = currentSize;
	}

	public void setCurrentMails(long currentMails)
	{
		this.currentMails = currentMails;
	}

	public String getCurrentFolder()
	{
		return currentFolder;
	}

	public IArrangement<?> getArrangement()
	{
		return arrangement;
	}

	public MailImportRunThread setArrangement(IArrangement<?> arrangement)
	{
		this.arrangement = arrangement;
		return this;
	}

	public MailImportRunThread setCurrentFolder(String currentFolder)
	{
		this.currentFolder = currentFolder;
		return this;
	}
}
