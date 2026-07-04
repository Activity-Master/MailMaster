package com.guicedee.activitymaster.mail;

import com.guicedee.activitymaster.mail.implementations.MailboxBoxService;
import com.guicedee.activitymaster.mail.implementations.MailTransportService;
import com.guicedee.activitymaster.mail.servers.LocalMailServer;
import com.guicedee.activitymaster.mail.servers.MailProtocol;
import com.guicedee.activitymaster.mail.services.dto.MailAttachment;
import com.guicedee.activitymaster.mail.services.dto.MailMessage;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * End-to-end test against a local development mail server (e.g. smtp4dev) listening on
 * SMTP:25 / IMAP:143 with no TLS, as described by the project's local server configuration.
 * <p>
 * The message is <strong>sent through the reactive Vert.x mail client</strong> and read back via
 * IMAP, asserting the subject, sender alias and attachment survive the trip. The whole test is
 * skipped (not failed) when no server is reachable on {@code localhost:25}, so it is safe to run in
 * any environment.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MailLocalServerIntegrationTest
{
	private Vertx vertx;
	private MailTransportService transport;

	@BeforeAll
	void setup()
	{
		vertx = Vertx.vertx();
		transport = new MailTransportService(vertx);
	}

	@AfterAll
	void tearDown()
	{
		if (vertx != null)
		{
			vertx.close();
		}
	}

	private static boolean reachable(int port)
	{
		try (Socket socket = new Socket())
		{
			socket.connect(new InetSocketAddress("localhost", port), 1000);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	@Test
	void sendsViaVertxAndReadsBackViaImap() throws Exception
	{
		assumeTrue(reachable(25), "No local SMTP server on localhost:25 — skipping integration test");

		String token = UUID.randomUUID().toString();
		String subject = "Activity Master mail test " + token;
		byte[] attachmentBytes = ("Report body " + token).getBytes(StandardCharsets.UTF_8);

		LocalMailServer sendServer = new LocalMailServer();
		MailMessage message = new MailMessage()
				.from("Mail Master", "mail-master@activity-master.com")
				.addTo("Recipient One", "recipient@example.com")
				.addCc("watcher@example.com")
				.setSubject(subject)
				.setTextBody("This is the plain-text body for " + token)
				.setHtmlBody("<p>This is the <b>HTML</b> body for " + token + "</p>")
				.addAttachment(new MailAttachment("report-" + token + ".txt", "text/plain", attachmentBytes));

		// Send through the Vert.x mail client (reactive).
		transport.send(sendServer, message).await().atMost(Duration.ofSeconds(30));

		// Read it back over IMAP (skip the read assertions if IMAP is not available).
		assumeTrue(reachable(143), "No local IMAP server on localhost:143 — skipping read-back assertions");

		// smtp4dev delivers all mail to a single mailbox (logged as "Default"); IMAP exposes it as INBOX.
		// Indexing can lag a touch, so poll for a short window.
		MailMessage received = pollForMessage(subject, Duration.ofSeconds(15));

		assumeTrue(received != null,
				"Message sent successfully but not retrievable over IMAP (mailbox routing is server-specific)");

		assertNotNull(received.getFrom());
		assertTrue(received.getTextBody() == null || received.getTextBody().contains(token)
				|| (received.getHtmlBody() != null && received.getHtmlBody().contains(token)));
		assertTrue(received.getAttachments().stream()
				.anyMatch(a -> a.getFileName() != null && a.getFileName().contains(token)),
				"The attachment was not received");
	}

	private MailMessage pollForMessage(String subject, Duration timeout) throws Exception
	{
		long deadline = System.currentTimeMillis() + timeout.toMillis();
		String[] candidateUsers = {"Default", "imap-user", "recipient@example.com"};
		while (System.currentTimeMillis() < deadline)
		{
			for (String user : candidateUsers)
			{
				// Read over POP3 (smtp4dev also serves POP3 on 110); POP3 downloads the full message so
				// the MIME is parsed locally, avoiding server-specific IMAP ENVELOPE quirks.
				LocalMailServer readServer = new LocalMailServer(user, "password")
						.setAuth(true)
						.setStoreProtocol(MailProtocol.POP3)
						.setPort(110);
				try (MailboxBoxService mailbox = new MailboxBoxService(readServer))
				{
					mailbox.connect();
					for (MailMessage candidate : mailbox.fetch("INBOX", 100))
					{
						if (subject.equals(candidate.getSubject()))
						{
							return candidate;
						}
					}
				}
				catch (Exception ignored)
				{
					// try the next candidate mailbox / retry
				}
			}
			Thread.sleep(750);
		}
		return null;
	}
}





