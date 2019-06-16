package com.armineasy.activitymaster.mail;

import com.armineasy.activitymaster.activitymaster.services.dto.IArrangement;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.services.system.IArrangementsService;
import com.armineasy.activitymaster.activitymaster.services.system.IInvolvedPartyService;
import com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes;
import com.armineasy.activitymaster.mail.servers.MailServer;
import com.armineasy.activitymaster.mail.services.IMailService;
import com.armineasy.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.jwebmp.guicedinjection.GuiceContext;
import com.sun.mail.imap.IMAPFolder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import javax.mail.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import static com.armineasy.activitymaster.mail.services.enumerations.MailImportArrangementTypes.*;
import static com.jwebmp.guicedinjection.GuiceContext.*;

@Data
@Accessors(chain = true)
@Log
public class MailService
		implements IMailService<MailService>, Closeable
{
	private MailServer server;

	private Session session;
	private Properties properties;

	private Store store;

	private Map<String, Integer> folderMessages = new LinkedHashMap<>();

	private boolean loggedIn;

	private long totalMails;
	private long totalFolders;
	private long totalSize;

	public MailService()
	{
	}

	public MailService(MailServer server)
	{
		this.server = server;
		properties = new Properties();
	}

	public MailServer getServer()
	{
		return server;
	}

	public MailService setServer(MailServer server)
	{
		this.server = server;
		return this;
	}

	@Override
	public IInvolvedParty<?> findByEmail(String emailAddress, ISystems<?> systems, UUID... identityToken)
	{
		UUID identity = MailSystem.getSystemTokens()
		                          .get(systems.getEnterpriseID());
		IInvolvedParty<?> involvedPartyService = get(IInvolvedPartyService.class)
				                                         .findByIdentificationType(IdentificationTypes.IdentificationTypeEmailAddress, emailAddress, systems, identity);

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
	public MailService login()
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
			throw new RuntimeException("Can't Connect",afe);
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
		ISystems system = MailSystem.getNewSystem()
		                            .get(ip.getEnterpriseID());

		IArrangementsService<?> arrangementsService = GuiceContext.get(IArrangementsService.class);
		IArrangement<?> a = arrangementsService.create(MailImport, value, system, identityToken);

		a.add(ip, MailSystemClassifications.MailImport, value, system, identity);

		return a;
	}

	@Override
	public MailService loadFolders() throws MessagingException
	{
		if (!loggedIn)
		{
			login();
		}
		folderMessages.clear();
		goThroughFolders("", store.getDefaultFolder());
		return this;
	}

	@Override
	public long getNumberOfMails() throws MessagingException
	{
		if (!loggedIn)
		{
			login();
		}
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

	private Folder goThroughFolders(String prefix, Folder fd) throws MessagingException
	{
		Quota[] quotas = null;
		for (Folder folder : fd.list())
		{
			try
			{
				if (!folder.isOpen())
				{
					folder.open(Folder.READ_ONLY);
				}
			}
			catch (MessagingException me)
			{
				continue;
			}
			folderMessages.put(folder.getFullName(), folder.getMessageCount());
			totalFolders += 1;
			totalMails += folder.getMessageCount();
			if (!gotMyQuota)
			{
				IMAPFolder imf = (IMAPFolder) folder;
				if(quotas == null)
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
					}catch(Exception e)
					{
						log.log(Level.WARNING, "Cannot Get Quota for " + imf.getFullName(), e);
					}
			}
			goThroughFolders(prefix + folder.getName() + "/", folder);
			folder.close(true);
		}
		return fd;
	}

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
