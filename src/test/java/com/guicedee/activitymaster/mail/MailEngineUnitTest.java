package com.guicedee.activitymaster.mail;

import com.guicedee.activitymaster.mail.engine.MailMessageParser;
import com.guicedee.activitymaster.mail.engine.MailMimeBuilder;
import com.guicedee.activitymaster.mail.engine.VertxMailMapper;
import com.guicedee.activitymaster.mail.servers.LocalMailServer;
import com.guicedee.activitymaster.mail.servers.MailProtocol;
import com.guicedee.activitymaster.mail.services.dto.MailAttachment;
import com.guicedee.activitymaster.mail.services.dto.MailMessage;
import io.vertx.ext.mail.LoginOption;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.StartTLSOptions;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the mail engine that run without any mail server: MIME build/parse round-trips,
 * the Vert.x message/config mapping, and address-alias + attachment handling.
 */
class MailEngineUnitTest
{
	private static final byte[] ATTACHMENT_BYTES = "Quarterly numbers: 42".getBytes(StandardCharsets.UTF_8);

	private MailMessage sampleMessage()
	{
		return new MailMessage()
				.from("Alice Example", "alice@example.com")
				.addTo("Bob Example", "bob@example.com")
				.addCc("carol@example.com")
				.addReplyTo("alice.replies@example.com")
				.setSubject("Quarterly report")
				.setTextBody("Please find the plain-text body and the report attached.")
				.setHtmlBody("<p>Please find the <b>report</b> attached.</p>")
				.addAttachment(new MailAttachment("report.txt", "text/plain", ATTACHMENT_BYTES));
	}

	@Test
	void buildAndParseRoundTripPreservesAliasBodiesAndAttachment() throws Exception
	{
		MailMessage source = sampleMessage();
		Session session = Session.getInstance(new Properties());

		MimeMessage mime = MailMimeBuilder.build(session, source);
		MailMessage parsed = MailMessageParser.parse(mime, "INBOX");

		// Sender alias (display name) is preserved end-to-end.
		assertEquals("Alice Example", parsed.getFrom().getPersonal());
		assertEquals("alice@example.com", parsed.getFrom().getAddress());

		// Recipients.
		assertEquals(1, parsed.getTo().size());
		assertEquals("bob@example.com", parsed.getTo().get(0).getAddress());
		assertEquals("Bob Example", parsed.getTo().get(0).getPersonal());
		assertEquals("carol@example.com", parsed.getCc().get(0).getAddress());

		// Subject + both bodies.
		assertEquals("Quarterly report", parsed.getSubject());
		assertTrue(parsed.getTextBody().contains("plain-text body"));
		assertTrue(parsed.getHtmlBody().contains("<b>report</b>"));

		// Attachment bytes survive the round trip.
		assertTrue(parsed.hasAttachments());
		assertEquals(1, parsed.getAttachments().size());
		MailAttachment attachment = parsed.getAttachments().get(0);
		assertEquals("report.txt", attachment.getFileName());
		assertTrue(attachment.getContentType().startsWith("text/plain"));
		assertArrayEquals(ATTACHMENT_BYTES, attachment.getContent());
	}

	@Test
	void mapsToVertxMessage()
	{
		io.vertx.ext.mail.MailMessage vertxMessage = VertxMailMapper.toVertxMessage(sampleMessage());

		assertEquals("Alice Example <alice@example.com>", vertxMessage.getFrom());
		assertTrue(vertxMessage.getTo().contains("Bob Example <bob@example.com>"));
		assertTrue(vertxMessage.getCc().contains("carol@example.com"));
		assertEquals("Quarterly report", vertxMessage.getSubject());
		assertTrue(vertxMessage.getText().contains("plain-text"));
		assertTrue(vertxMessage.getHtml().contains("<b>report</b>"));
		assertEquals(1, vertxMessage.getAttachment().size());
		assertEquals("report.txt", vertxMessage.getAttachment().get(0).getName());
		// Reply-To is carried as a header.
		assertEquals("alice.replies@example.com", vertxMessage.getHeaders().get("Reply-To"));
	}

	@Test
	void mapsLocalServerToVertxConfig()
	{
		LocalMailServer server = new LocalMailServer();
		MailConfig config = VertxMailMapper.toConfig(server);

		assertEquals("localhost", config.getHostname());
		assertEquals(25, config.getPort());
		assertFalse(config.isSsl());
		assertEquals(StartTLSOptions.DISABLED, config.getStarttls());
		assertEquals(LoginOption.DISABLED, config.getLogin());
		assertEquals(MailProtocol.SMTP, server.getTransportProtocol());
	}
}

