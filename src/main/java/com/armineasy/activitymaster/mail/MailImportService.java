package com.armineasy.activitymaster.mail;

import com.armineasy.activitymaster.mail.servers.MailServer;
import com.armineasy.activitymaster.mail.services.IMailImportService;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

@Data
@Accessors(chain = true)
@Log
public class MailImportService
		implements IMailImportService<MailImportService>
{
	public boolean checkCredentials(MailServer server)
	{
		try (MailService mm = new MailService(server))
		{
			mm.login();
			return true;
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE, "Server not verified", e);
			return false;
		}
	}


}
