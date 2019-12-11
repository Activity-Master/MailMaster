package com.guicedee.activitymaster.mail.implementations;

import com.guicedee.activitymaster.core.services.dto.IArrangement;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.security.Passwords;
import com.guicedee.activitymaster.core.services.system.IArrangementsService;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.core.services.types.IdentificationTypes;
import com.guicedee.activitymaster.mail.MailSystem;
import com.guicedee.activitymaster.mail.servers.MailServer;
import com.guicedee.activitymaster.mail.services.IMailBoxService;
import com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.guicedee.guicedinjection.GuiceContext;
import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.guicedee.activitymaster.mail.services.enumerations.MailImportArrangementTypes.*;


public class MailboxBoxService
		implements IMailBoxService<MailboxBoxService>, Closeable
{
	private static final Logger log = Logger.getLogger(MailboxBoxService.class.getName());
	private MailServer server;

	private Session session;
	private Properties properties;

	private Store store;

	private Map<String, Integer> folderMessages = new LinkedHashMap<>();

	private boolean loggedIn;

	private long totalMails;
	private long totalFolders;
	private long totalSize;

	public MailboxBoxService()
	{
	}

	public MailboxBoxService(MailServer server)
	{
		this.server = server;
		properties = new Properties();
	}

	public MailServer getServer()
	{
		return server;
	}

	public MailboxBoxService setServer(MailServer server)
	{
		this.server = server;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IInvolvedParty<?> findByEmail(String emailAddress, ISystems<?> systems, UUID... identityToken)
	{
		UUID identity = MailSystem.getSystemTokens()
		                          .get(systems.getEnterpriseID());
		IInvolvedParty<?> involvedPartyService = GuiceContext.get(IInvolvedPartyService.class)
		                                                     .findByIdentificationType(IdentificationTypes.IdentificationTypeEmailAddress,
		                                                                               new Passwords().integerEncrypt(emailAddress.getBytes())
				                                                     , systems, identity);

		return involvedPartyService;
	}

	@Override
	public ISystems<?> getMailSystem(IEnterprise<?> enterprise)
	{
		return MailSystem.getNewSystem()
		                 .get(enterprise);
	}

	@Override
	public UUID getMailUUID(IEnterprise<?> enterprise)
	{
		return MailSystem.getSystemTokens()
		                 .get(enterprise);
	}

	@Override

	public MailboxBoxService login()
	{
		properties.put("mail.transport.protocol", "imap");
		properties.put("mail.imap.port", server.getPort());
		properties.put("mail.imap.host", server.getHostname());
		properties.put("mail.imap.user", server.getUsername());
		properties.put("mail.imap.password", server.getPassword());
		properties.put("mail.imap.timeout", 10);
		properties.put("mail.imap.starttls.enable", true);

		session = Session.getDefaultInstance(properties, null);

		try
		{
			store = session.getStore("imaps");
			store.connect(server.getHostname(), server.getUsername(), server.getPassword());
			loggedIn = true;
		}
		catch (AuthenticationFailedException afe)
		{
			throw new RuntimeException("Can't Connect", afe);
		}
		catch (NoSuchProviderException e)
		{
			log.log(Level.SEVERE, "IMAPS NOT AVAILABLE", e);
		}
		catch (MessagingException e)
		{
			log.log(Level.SEVERE, "Could not connect", e);
		}
		return this;
	}

	@Override
	public IArrangement<?> createArrangement(IInvolvedParty<?> ip, String value, UUID... identityToken)
	{
		UUID identity = MailSystem.getSystemTokens()
		                          .get(ip.getEnterpriseID());
		ISystems<?> system = MailSystem.getNewSystem()
		                            .get(ip.getEnterpriseID());

		IArrangementsService<?> arrangementsService = GuiceContext.get(IArrangementsService.class);
		IArrangement<?> a = arrangementsService.create(MailImport, value, system, identityToken);
		a.add(ip, MailSystemClassifications.MailImport, value, system, identity);

		return a;
	}

	@Override
	public MailboxBoxService loadFolders() throws MessagingException
	{
		if (!loggedIn)
		{
			login();
		}
		folderMessages.clear();
		try
		{
			goThroughFolders("", store.getDefaultFolder());
		}
		catch (Throwable T)
		{
			log.log(Level.SEVERE, "OIOOOOPPS", T);
		}
		totalMails = getNumberOfMails();
		return this;
	}

	@Override
	public long getNumberOfMails() throws MessagingException
	{
		Integer totalMails = 0;
		for (Map.Entry<String, Integer> entry : folderMessages.entrySet())
		{
			String a = entry.getKey();
			Integer b = entry.getValue();
			totalMails += b;
		}
		return totalMails;
	}

	boolean gotMyQuota = false;
	boolean isGmail = false;

	private Folder goThroughFolders(String prefix, Folder fd) throws MessagingException
	{
		try
		{
			if (!fd.isOpen())
			{
				fd.open(Folder.READ_WRITE);
			}
		}
		catch (MessagingException me)
		{
			for (Folder folder : fd.list("*"))
			{
				goThroughFolders(prefix + folder.getName() + "/", folder);
				try
				{
					folder.close();
				}
				catch (Exception me2)
				{
					//Expected
				}
			}
			return fd;
		}
		if (!fd.isOpen())
		{
			fd.open(Folder.READ_WRITE);
		}
		IMAPFolder imf = (IMAPFolder) fd;
		int type = fd.getType();
		String[] attrs = imf.getAttributes();
		List<String> attrList = Arrays.asList(attrs);
		if (attrList.contains("\\All") ||
		    attrList.contains("\\Important") ||
		    attrList.contains("\\Starred")
		)
		{
			try
			{
				fd.close();
			}
			catch (Exception e)
			{
				//in case
			}
			return fd;
		}

		String fullPathName = prefix + fd.getFullName();
		if (!isGmail && fullPathName.contains("[Gmail]"))
		{
			isGmail = true;
		}

		String folderName = fd.getFullName();
		folderMessages.put(folderName, fd.getMessageCount());
		totalFolders += 1;
		Quota[] quotas = null;
		if (!gotMyQuota)
		{
			if (quotas == null)
			{
				try
				{
					quotas = imf.getQuota();

					for (Quota quota : quotas)
					{
						System.out.println(String.format("quotaRoot:'%s'", quota.quotaRoot));
						for (Quota.Resource resource : quota.resources)
						{
							System.out.println(String.format("name:'%s', limit:'%s', usage:'%s'",
							                                 resource.name, resource.limit, resource.usage));
							totalSize = resource.usage * 1024;
							gotMyQuota = true;
						}
					}
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Cannot Get Quota for " + imf.getFullName());
				}
			}
		}
		for (Folder folder : fd.list("*"))
		{
			goThroughFolders(prefix + folder.getName() + "/", folder);
			folder.close();
		}

		return fd;
	}

	@Override
	public List<Folder> getFolders(String folderPath) throws MessagingException
	{
		List<Folder> fold = new ArrayList<>();
		String[] list = folderPath.split("/");
		Folder f = store.getDefaultFolder();
		for (String s : list)
		{
			f = f.getFolder(s);
			fold.add(f);
		}
		return fold;
	}

	public Folder getFolder(String folderPath) throws MessagingException
	{
		Folder f = store.getDefaultFolder();
		return f.getFolder(folderPath);
	}

	@Override
	public Folder addFolder(String folder, String folderPath) throws MessagingException
	{
		if (!loggedIn)
		{
			login();
		}
		String[] list = folderPath.split("/");
		Folder f = store.getDefaultFolder();
		for (String s : list)
		{
			f = f.getFolder(s);
			if (!f.isOpen())
			{
				try
				{
					f.open(Folder.READ_ONLY);
				}
				catch (MessagingException me)
				{
					//cant use folder
					continue;
				}
			}
			f.close(true);
		}
		Folder newFolder = f.getFolder(folder);
		if (!newFolder.exists())
		{
			newFolder.create(Folder.HOLDS_MESSAGES);
		}
		try
		{
			if (!newFolder.isOpen())
			{
				newFolder.open(Folder.READ_WRITE);
			}
			if (!f.isOpen())
			{
				f.open(Folder.READ_WRITE);
			}
			folderMessages.put(newFolder.getFullName(), 0);
			f.close(true);
			newFolder.close(true);
		}
		catch (FolderNotFoundException nfe)
		{
			log.log(Level.WARNING, "Folder not found : " + f.getFullName(), nfe);
		}
		return newFolder;
	}

	@Override
	public void close() throws IOException
	{
		loggedIn = false;
		try
		{
			store.close();
		}
		catch (MessagingException e)
		{
			log.log(Level.SEVERE, "Couldn't close store", e);
		}
	}

	public Map<String, Integer> getFolderMessages()
	{
		return folderMessages;
	}

	public Session getSession()
	{
		return this.session;
	}

	public Properties getProperties()
	{
		return this.properties;
	}

	public Store getStore()
	{
		return this.store;
	}

	public boolean isLoggedIn()
	{
		return this.loggedIn;
	}

	public long getTotalMails()
	{
		return this.totalMails;
	}

	public long getTotalFolders()
	{
		return this.totalFolders;
	}

	public long getTotalSize()
	{
		return this.totalSize;
	}

	public boolean isGotMyQuota()
	{
		return this.gotMyQuota;
	}

	public MailboxBoxService setSession(Session session)
	{
		this.session = session;
		return this;
	}

	public MailboxBoxService setProperties(Properties properties)
	{
		this.properties = properties;
		return this;
	}

	public MailboxBoxService setStore(Store store)
	{
		this.store = store;
		return this;
	}

	public MailboxBoxService setFolderMessages(Map<String, Integer> folderMessages)
	{
		this.folderMessages = folderMessages;
		return this;
	}

	public MailboxBoxService setLoggedIn(boolean loggedIn)
	{
		this.loggedIn = loggedIn;
		return this;
	}

	public MailboxBoxService setTotalMails(long totalMails)
	{
		this.totalMails = totalMails;
		return this;
	}

	public MailboxBoxService setTotalFolders(long totalFolders)
	{
		this.totalFolders = totalFolders;
		return this;
	}

	public MailboxBoxService setTotalSize(long totalSize)
	{
		this.totalSize = totalSize;
		return this;
	}

	public MailboxBoxService setGotMyQuota(boolean gotMyQuota)
	{
		this.gotMyQuota = gotMyQuota;
		return this;
	}

	private class PasswordAuthenticator
			extends javax.mail.Authenticator
	{
		@Override
		public javax.mail.PasswordAuthentication getPasswordAuthentication()
		{
			return new PasswordAuthentication(server.getUsername(), server.getPassword());
		}
	}
}
