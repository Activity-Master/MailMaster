package com.armineasy.activitymaster.mail.services;

import com.armineasy.activitymaster.activitymaster.services.classifications.enterprise.IEnterpriseName;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.profiles.dto.ProfileServiceDTO;
import com.armineasy.activitymaster.profiles.dto.UserLoginDTO;
import com.armineasy.activitymaster.profiles.exceptions.ProfileServiceException;

import java.util.UUID;

public interface IMailService<J extends IMailService<J>>
{
	IInvolvedParty<?> findByEmail(String email, ISystems<?> enterprise, UUID... token);

	ProfileServiceDTO<?> loginUser(UserLoginDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException;
}
