package com.guicedee.activitymaster.mail.services;

import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.dto.UserLoginDTO;
import com.guicedee.activitymaster.profiles.exceptions.ProfileServiceException;

import java.util.UUID;

public interface IMailService<J extends IMailService<J>>
{
	IInvolvedParty<?> findByEmail(String email, ISystems<?> enterprise, UUID... token);

	ProfileServiceDTO<?> loginUser(UserLoginDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException;
}
