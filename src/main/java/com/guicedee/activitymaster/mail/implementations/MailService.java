package com.guicedee.activitymaster.mail.implementations;


import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.core.services.dto.*;
import com.guicedee.activitymaster.core.services.enumtypes.IIdentificationType;
import com.guicedee.activitymaster.core.services.exceptions.SecurityAccessException;
import com.guicedee.activitymaster.core.services.security.Passwords;
import com.guicedee.activitymaster.core.services.system.IEnterpriseService;
import com.guicedee.activitymaster.core.services.system.IEventService;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.core.services.system.ISecurityTokenService;
import com.guicedee.activitymaster.mail.MailSystem;
import com.guicedee.activitymaster.mail.roles.MailUserRoles;
import com.guicedee.activitymaster.mail.servers.SaNrgMailServer;
import com.guicedee.activitymaster.mail.services.IMailBoxService;
import com.guicedee.activitymaster.mail.services.IMailService;
import com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.webdto.UserLoginDTO;
import com.guicedee.activitymaster.profiles.webdto.UserRegistrationDTO;
import com.guicedee.activitymaster.profiles.exceptions.ProfileServiceException;
import com.guicedee.activitymaster.profiles.exceptions.UserExistsException;
import com.guicedee.activitymaster.profiles.exceptions.WaitingForConfirmationKeyException;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.pairing.Pair;

import java.util.UUID;

import static com.guicedee.activitymaster.core.services.classifications.events.EventInvolvedPartiesClassifications.*;
import static com.guicedee.activitymaster.core.services.classifications.securitytokens.SecurityTokenClassifications.*;
import static com.guicedee.activitymaster.core.services.types.IdentificationTypes.*;
import static com.guicedee.activitymaster.core.services.types.NameTypes.*;
import static com.guicedee.activitymaster.mail.roles.MailUserRoles.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.services.enumerations.UserRoles.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@SuppressWarnings("Duplicates")
public class MailService
		implements IMailService<MailService>
{

	@Override
	public IInvolvedParty<?> findByEmail(String email, ISystems<?> systems, UUID... token)
	{
		IInvolvedParty<?> party = get(IInvolvedPartyService.class).findByIdentificationType(IdentificationTypeEmailAddress,
		                                                                                    new Passwords().integerEncrypt(email.getBytes()), systems, token);
		return party;
	}

	@Override
	public ProfileServiceDTO<?> loginUser(UserLoginDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException
	{
		IInvolvedPartyService<?> involvedPartyService = GuiceContext.get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = GuiceContext.get(IEnterpriseService.class)
		                                        .getEnterprise(enterpriseName);

		profileServiceDTO.setEnterprise(enterpriseName);

		ISystems profileSystem = MailSystem.getNewSystem()
		                                   .get(enterprise);
		UUID profileSystemUUID = MailSystem.getSystemTokens()
		                                   .get(enterprise);

		if ((identityToken == null || identityToken.length == 0) && profileServiceDTO.getIdentityToken() == null)
		{
			identityToken = new UUID[]{profileSystemUUID};
		}

		IInvolvedParty<?> newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID,
		                                                                        profileServiceDTO.getWebClientUUID()
		                                                                                         .toString(), profileSystem, profileSystemUUID);

		try (IMailBoxService<?> service = IMailBoxService.get(new SaNrgMailServer(profileServiceDTO.getUserName(), profileServiceDTO.getPassword())))
		{
			service.login();

			IMailService<?> mailService = get(IMailService.class);
			IInvolvedParty<?> foundParty = mailService.findByEmail(profileServiceDTO.getUserName(),
			                                                       profileSystem,
			                                                       profileSystemUUID);

			if (foundParty == null)
			{
				UserRegistrationDTO<?> newDto = new UserRegistrationDTO();
				newDto.setUserName(profileServiceDTO.getUserName());
				newDto.setTermsandconditions(true);
				newDto.setEnterprise(enterpriseName);
				newDto.setWebClientUUID(profileServiceDTO.getWebClientUUID());
				newDto.setIdentityToken(profileServiceDTO.getIdentityToken());
				foundParty = registerVisitor(newDto, enterpriseName, identityToken);
			}

			profileServiceDTO.setIdentityToken(foundParty.getSecurityIdentity());
			if (!newIp.equals(foundParty))
			{
				foundParty.addOrUpdate(IdentificationTypeWebClientUUID,
				                       profileServiceDTO.getWebClientUUID()
				                                        .toString(),
				                       profileSystem, profileSystemUUID);
				newIp.archive(IdentificationTypeWebClientUUID, profileSystem, profileSystemUUID);
				newIp = foundParty;
			}
			newIp.addOrUpdate(LoggedOn, "true", profileSystem, profileSystemUUID);

			//newIp.addOrUpdate(RememberMe, profileServiceDTO.isRememberMe() + "", profileSystem, profileSystemUUID);
			if (newIp.has(IdentificationTypeEnterpriseCreatorRole, profileSystem, profileSystemUUID))
			{
				get(IRolesService.class).addRole(newIp,Administrator, profileServiceDTO, profileSystem, identityToken);
				get(IRolesService.class).addRole(newIp,MailAdministrator, profileServiceDTO, profileSystem, identityToken);
			}
		}
		catch (Exception e)
		{
			throw new SecurityAccessException("Invalid username or password", e);
		}

		profileServiceDTO.setPassword(null);

		if (profileServiceDTO.isRememberMe())
		{
			newIp.addOrUpdate(RememberMe, "true", profileSystem, profileSystemUUID);
		}
		else
		{
			newIp.addOrUpdate(RememberMe, "false", profileSystem, profileSystemUUID);
		}
		profileServiceDTO.findRoles();
		return profileServiceDTO;
	}

	IInvolvedParty<?> registerVisitor(UserRegistrationDTO<?> userRegistrationDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws UserExistsException, WaitingForConfirmationKeyException
	{
		IInvolvedPartyService<?> involvedPartyService = GuiceContext.get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = GuiceContext.get(IEnterpriseService.class)
		                                        .getEnterprise(enterpriseName);

		ISystems profileSystem = MailSystem.getNewSystem()
		                                   .get(enterprise);
		UUID profileSystemUUID = MailSystem.getSystemTokens()
		                                   .get(enterprise);

		IEvent<?> registerEvent = GuiceContext.get(IEventService.class)
		                                      .createEvent(UserRegistered, profileSystem, profileSystemUUID);

		IInvolvedParty<?> ipExists = involvedPartyService.findByIdentificationType(IdentificationTypeEmailAddress,
		                                                                           new Passwords().integerEncrypt(userRegistrationDTO.getUserName()
		                                                                                                                       .getBytes())
				, profileSystem, profileSystemUUID);
		if (ipExists != null)
		{
			if (ipExists.has(ConfirmationKey, profileSystem, identityToken))
			{
				throw new WaitingForConfirmationKeyException("The email address is waiting for a confirmation key");
			}
			throw new UserExistsException("That email address is already in use as a valid identifier");
		}
		IInvolvedParty<?> newIp;
		newIp = createNewVisitor(registerEvent, userRegistrationDTO, profileSystem.getEnterpriseID(), profileSystem, profileSystemUUID);
		newIp.add(MailSystemClassifications.UseMailForLogin, "true", profileSystem, profileSystemUUID);

		return newIp;
	}


	IInvolvedParty<?> createNewVisitor(IEvent<?> event, UserRegistrationDTO<?> profileServiceDTO, IEnterprise<?> enterprise, ISystems<?> profileSystem, UUID... identityToken)
	{
		IInvolvedPartyService<?> involvedPartyService = GuiceContext.get(IInvolvedPartyService.class);
		IInvolvedParty<?> newIp;
		//Create new guest record
		Pair<IIdentificationType<?>, String> guestIDType = new Pair<>();
		guestIDType.setKey(IdentificationTypeEmailAddress)
		           .setValue(new Passwords().integerEncrypt(profileServiceDTO.getUserName()
		                                                               .getBytes()));

		profileServiceDTO.setWebClientUUID(UUID.randomUUID());

		newIp = involvedPartyService.create(profileSystem, guestIDType, true, identityToken);

		ISecurityToken<?> visitorsGroup = GuiceContext.get(ISecurityTokenService.class)
		                                              .getRegisteredGuestsFolder(enterprise, identityToken);

		ISecurityToken<?> myToken = get(ISecurityTokenService.class).create(Identity,
		                                                                    new Passwords().integerEncrypt(profileServiceDTO.getUserName()
		                                                                                                             .getBytes()),
		                                                                   "An agent registration",
		                                                                   profileSystem,
		                                                                   visitorsGroup,
		                                                                   identityToken);

		newIp.addOrUpdate(IdentificationTypeUUID, myToken.getSecurityToken(), profileSystem, identityToken);
		newIp.addOrUpdate(IdentificationTypeUserName, new Passwords().integerEncrypt(profileServiceDTO.getUserName()
		                                                                                        .getBytes())
				, profileSystem, identityToken);

		newIp.addOrReuse(PreferredNameType, "Agent", profileSystem, identityToken);
		newIp.addOrReuse(CreatedBy, Long.toString(newIp.getId()), profileSystem, identityToken);
		event.addOrReuse(PerformedBy, newIp.getSecurityIdentity()
		                                   .toString(), profileSystem, identityToken);

		profileServiceDTO.setIdentityToken(java.util.UUID.fromString(myToken.getSecurityToken()));
		UUID profileSystemUUID = MailSystem.getSystemTokens()
		                                   .get(enterprise);

		get(IRolesService.class).addRole(newIp,MailUserRoles.MailUser, profileServiceDTO, profileSystem, profileSystemUUID);

		return newIp;
	}
}
