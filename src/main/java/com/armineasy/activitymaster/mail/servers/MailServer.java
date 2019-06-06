package com.armineasy.activitymaster.mail.servers;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class MailServer
{
	private String hostname;
	private int port = 993;
	private String username;
	private String password;
}
