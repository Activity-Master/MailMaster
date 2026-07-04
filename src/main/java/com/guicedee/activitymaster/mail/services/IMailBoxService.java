package com.guicedee.activitymaster.mail.services;

import com.guicedee.activitymaster.mail.servers.MailServer;
import com.guicedee.activitymaster.mail.services.dto.MailMessage;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;

import java.io.Closeable;
import java.util.List;

/**
 * Connects to an IMAP/POP3 {@link MailServer} to read mail: list folders, count messages and
 * fetch fully-parsed {@link MailMessage}s (including attachments and address aliases).
 * <p>
 * Implementations are {@link Closeable} — use try-with-resources to guarantee the connection is
 * released.
 *
 * @param <J> the concrete service type
 */
public interface IMailBoxService<J extends IMailBoxService<J>> extends Closeable
{
	/**
	 * Connects (logs in) to the configured server.
	 *
	 * @return this service, connected
	 * @throws MessagingException when the connection or authentication fails
	 */
	J connect() throws MessagingException;

	/**
	 * @return {@code true} when a live connection is currently held.
	 */
	boolean isConnected();

	/**
	 * Lists the names of all folders reachable from the default folder.
	 *
	 * @return the folder names
	 * @throws MessagingException on a mail protocol error
	 */
	List<String> listFolders() throws MessagingException;

	/**
	 * Counts the messages in a folder.
	 *
	 * @param folderName the folder to count (e.g. {@code INBOX})
	 * @return the message count
	 * @throws MessagingException on a mail protocol error
	 */
	int messageCount(String folderName) throws MessagingException;

	/**
	 * Fetches and parses up to {@code max} of the most recent messages from a folder.
	 *
	 * @param folderName the folder to read (e.g. {@code INBOX})
	 * @param max        the maximum number of messages to fetch ({@code <= 0} for all)
	 * @return the parsed messages, oldest first
	 * @throws MessagingException on a mail protocol error
	 */
	List<MailMessage> fetch(String folderName, int max) throws MessagingException;

	/**
	 * Opens (or creates) a folder under the default folder.
	 *
	 * @param folderName the folder name
	 * @param create     whether to create the folder if it does not exist
	 * @return the folder
	 * @throws MessagingException on a mail protocol error
	 */
	Folder openFolder(String folderName, boolean create) throws MessagingException;
}

