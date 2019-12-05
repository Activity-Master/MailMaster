package com.guicedee.activitymaster.mail.roles;

import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;

public enum MailUserRoles implements IUserRole<MailUserRoles>
{
	MailUser,
	MailImportUser,
	MailAdministrator,
	;
}
