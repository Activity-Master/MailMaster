package com.guicedee.activitymaster.mail;

import com.guicedee.activitymaster.mail.importer.MailImporter;
import com.guicedee.activitymaster.mail.servers.GMailMailServer;
import com.guicedee.activitymaster.mail.servers.SaNrgMailServer;
import com.jwebmp.guicedhazelcast.HazelcastConfigHandler;
import com.jwebmp.guicedhazelcast.implementations.HazelcastBinderGuice;
import com.jwebmp.guicedhazelcast.implementations.HazelcastPreStartup;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.logger.LogFactory;
import com.jwebmp.logger.logging.LogColourFormatter;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

@Log
class MailServiceTest
{

	@Test
	void login()
	{
		LogFactory.configureConsoleColourOutput(Level.FINE);
		LogColourFormatter.setRenderBlack(false);
		HazelcastConfigHandler.startLocal = true;
		GuiceContext.inject();


		SaNrgMailServer sanrg = new SaNrgMailServer("tester@sanrg.net", "12345678");
		GMailMailServer gmail = new GMailMailServer("marc.magon@gmail.com", "qjieuqbubkfgxylz");
		try(MailService mm = new MailService(sanrg);MailService gm = new MailService(gmail))
		{
			mm.login();
			gm.login();

			new MailImporter().importMail(gm,mm);


			//mm.addFolder("TestNewFolder", "INBOX");
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE, "OOPS", e);
		}
		catch (MessagingException e)
		{
			log.log(Level.SEVERE, "OOPS", e);
		}
	}
}
