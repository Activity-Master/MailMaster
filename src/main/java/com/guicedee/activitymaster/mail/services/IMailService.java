package com.guicedee.activitymaster.mail.services;

import com.guicedee.activitymaster.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.exceptions.ProfileServiceException;
import com.guicedee.activitymaster.sessions.services.dto.UserLoginDTO;

import java.util.UUID;

public interface IMailService<J extends IMailService<J>>
{
	String MailSystemName = "Mail Master";
	
	IInvolvedParty<?,?> findByEmail(String email, ISystems<?,?> enterprise, UUID... token);

	ProfileServiceDTO<?> loginUser(UserLoginDTO<?> profileServiceDTO, String enterpriseName, UUID... identityToken) throws ProfileServiceException;
}
