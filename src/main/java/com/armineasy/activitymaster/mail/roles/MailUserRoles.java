package com.armineasy.activitymaster.mail.roles;

import com.armineasy.activitymaster.profiles.services.interfaces.IUserRole;

public enum MailUserRoles implements IUserRole<MailUserRoles>
{
	MailUser,
	MailImportUser,
	MailAdministrator,
	;
}
