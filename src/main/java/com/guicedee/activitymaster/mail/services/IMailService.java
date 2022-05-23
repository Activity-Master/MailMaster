package com.guicedee.activitymaster.mail.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.exceptions.ProfileServiceException;
import com.guicedee.activitymaster.sessions.services.dto.UserLoginDTO;



public interface IMailService<J extends IMailService<J>>
{
	String MailSystemName = "Mail Master";
	
	IInvolvedParty<?,?> findByEmail(String email, ISystems<?,?> enterprise, java.util.UUID... identityToken);

	ProfileServiceDTO<?> loginUser(UserLoginDTO<?> profileServiceDTO, String enterpriseName, java.util.UUID... identityToken) throws ProfileServiceException;
}
