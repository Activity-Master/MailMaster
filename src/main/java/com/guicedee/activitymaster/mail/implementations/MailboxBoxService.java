package com.guicedee.activitymaster.mail.implementations;

import com.guicedee.activitymaster.mail.engine.MailMessageParser;
import com.guicedee.activitymaster.mail.servers.MailProtocol;
import com.guicedee.activitymaster.mail.servers.MailServer;
import com.guicedee.activitymaster.mail.services.IMailBoxService;
import com.guicedee.activitymaster.mail.services.dto.MailMessage;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default {@link IMailBoxService} implementation. Opens a Jakarta Mail {@link Store} for the
 * configured {@link MailServer}'s store protocol and reads / parses messages into {@link MailMessage}s.
 */
public class MailboxBoxService implements IMailBoxService<MailboxBoxService>
{
	private static final Logger log = Logger.getLogger(MailboxBoxService.class.getName());

	private final MailServer<?> server;
	private Session session;
	private Store store;

	public MailboxBoxService(MailServer<?> server)
	{
		this.server = server;
	}

	/**
	 * Factory for ad-hoc, non-DI usage.
	 *
	 * @param server the server to connect to
	 * @return a new mailbox service
	 */
	public static MailboxBoxService get(MailServer<?> server)
	{
		return new MailboxBoxService(server);
	}

	@Override
	public MailboxBoxService connect() throws MessagingException
	{
		this.session = server.toStoreSession();
		MailProtocol protocol = server.getStoreProtocol();
		this.store = session.getStore(protocol.providerName());
		int port = portFor(protocol);
		if (server.isAuth())
		{
			store.connect(server.getHostname(), port, server.getUsername(), server.getPassword());
		}
		else
		{
			store.connect(server.getHostname(), port, null, null);
		}
		return this;
	}

	@Override
	public boolean isConnected()
	{
		return store != null && store.isConnected();
	}

	@Override
	public List<String> listFolders() throws MessagingException
	{
		ensureConnected();
		List<String> names = new ArrayList<>();
		collectFolders(store.getDefaultFolder(), names);
		return names;
	}

	private void collectFolders(Folder folder, List<String> names) throws MessagingException
	{
		for (Folder child : folder.list("*"))
		{
			names.add(child.getFullName());
			if ((child.getType() & Folder.HOLDS_FOLDERS) != 0)
			{
				try
				{
					collectFolders(child, names);
				}
				catch (MessagingException e)
				{
					log.log(Level.FINE, "Cannot recurse folder " + child.getFullName(), e);
				}
			}
		}
	}

	@Override
	public int messageCount(String folderName) throws MessagingException
	{
		ensureConnected();
		Folder folder = store.getFolder(folderName);
		if (!folder.exists())
		{
			return 0;
		}
		folder.open(Folder.READ_ONLY);
		try
		{
			return folder.getMessageCount();
		}
		finally
		{
			folder.close(false);
		}
	}

	@Override
	public List<MailMessage> fetch(String folderName, int max) throws MessagingException
	{
		ensureConnected();
		Folder folder = store.getFolder(folderName);
		if (!folder.exists())
		{
			return List.of();
		}
		folder.open(Folder.READ_ONLY);
		try
		{
			int count = folder.getMessageCount();
			if (count == 0)
			{
				return List.of();
			}
			int start = (max > 0 && count > max) ? count - max + 1 : 1;
			Message[] messages = folder.getMessages(start, count);
			List<MailMessage> result = new ArrayList<>(messages.length);
			for (Message message : messages)
			{
				try
				{
					result.add(MailMessageParser.parse(message, folderName));
				}
				catch (IOException | MessagingException e)
				{
					log.log(Level.WARNING, "Failed to parse a message in folder " + folderName, e);
				}
			}
			return result;
		}
		finally
		{
			folder.close(false);
		}
	}

	@Override
	public Folder openFolder(String folderName, boolean create) throws MessagingException
	{
		ensureConnected();
		Folder folder = store.getFolder(folderName);
		if (!folder.exists() && create)
		{
			folder.create(Folder.HOLDS_MESSAGES);
		}
		if (folder.exists() && !folder.isOpen())
		{
			folder.open(Folder.READ_WRITE);
		}
		return folder;
	}

	private void ensureConnected() throws MessagingException
	{
		if (!isConnected())
		{
			connect();
		}
	}

	private int portFor(MailProtocol protocol)
	{
		Object explicit = server.getExtraProperties().get("mail." + protocol.providerName() + ".port");
		if (explicit != null)
		{
			try
			{
				return Integer.parseInt(explicit.toString());
			}
			catch (NumberFormatException ignored)
			{
				// fall through
			}
		}
		return server.getPort();
	}

	public MailServer<?> getServer()
	{
		return server;
	}

	public Store getStore()
	{
		return store;
	}

	@Override
	public void close() throws IOException
	{
		if (store != null)
		{
			try
			{
				store.close();
			}
			catch (MessagingException e)
			{
				throw new IOException("Failed to close the mail store", e);
			}
			finally
			{
				store = null;
			}
		}
	}
}

