package com.armineasy.activitymaster.mail;

import org.junit.jupiter.api.Test;

import javax.mail.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class MailServiceTest
{
	@Test
	void testMe() throws MessagingException
	{
		String host = "imap.gmail.com";
		String username = "dominique.magon@sanrg.co.za";
		String password = "fORGETFULL2";

		Properties props = new Properties();
		props.setProperty("mail.imap.ssl.enable", "true");
		props.put("mail.transport.protocol", "imap");
		props.put("mail.imap.port", 993);
		props.put("mail.imap.host", host);
		props.put("mail.imap.user", username);
		props.put("mail.imap.password", password);
		props.put("mail.imap.timeout", 10);
		props.put("mail.imap.starttls.enable", true);

		Session session = javax.mail.Session.getInstance(props, null);
		Store store = session.getStore("imaps");
		store.connect(host, username, password);

		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);
		Message[] messages = inbox.getMessages();


		inbox.close(false);
		store.close();
	}
}