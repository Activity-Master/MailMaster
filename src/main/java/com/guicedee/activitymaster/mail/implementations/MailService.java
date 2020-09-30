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
import com.guicedee.activitymaster.profiles.exceptions.ProfileServiceException;
import com.guicedee.activitymaster.profiles.exceptions.UserExistsException;
import com.guicedee.activitymaster.profiles.exceptions.WaitingForConfirmationKeyException;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.activitymaster.profiles.webdto.UserLoginDTO;
import com.guicedee.activitymaster.profiles.webdto.UserRegistrationDTO;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.pairing.Pair;

import java.util.UUID;

import static com.guicedee.activitymaster.core.services.classifications.classification.Classifications.NoClassification;
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
        implements IMailService<MailService> {

    @Override
    public IInvolvedParty<?> findByEmail(String email, ISystems<?> systems, UUID... token) {
        IInvolvedParty<?> party = get(IInvolvedPartyService.class).findByIdentificationType(IdentificationTypeEmailAddress,
                new Passwords().integerEncrypt(email.getBytes()), systems, token);
        return party;
    }

    @Override
    public ProfileServiceDTO<?> loginUser(UserLoginDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException {
        IInvolvedPartyService<?> involvedPartyService = GuiceContext.get(IInvolvedPartyService.class);
        IEnterprise<?> enterprise = GuiceContext.get(IEnterpriseService.class)
                .getEnterprise(enterpriseName);

        profileServiceDTO.setEnterprise(enterpriseName);
        UUID identity = GuiceContext.get(MailSystem.class)
                .getSystemToken(enterpriseName);
        ISystems<?> mailSystem = GuiceContext.get(MailSystem.class)
                .getSystem(enterpriseName);

        if ((identityToken == null || identityToken.length == 0) && profileServiceDTO.getIdentityToken() == null) {
            identityToken = new UUID[]{identity};
        }

        IInvolvedParty<?> newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID,
                profileServiceDTO.getWebClientUUID()
                        .toString(), mailSystem, identity);

        try (IMailBoxService<?> service = IMailBoxService.get(new SaNrgMailServer(profileServiceDTO.getUserName(), profileServiceDTO.getPassword()))) {
            service.login();

            IMailService<?> mailService = get(IMailService.class);
            IInvolvedParty<?> foundParty = mailService.findByEmail(profileServiceDTO.getUserName(),
                    mailSystem,
                    identity);

            if (foundParty == null) {
                UserRegistrationDTO<?> newDto = new UserRegistrationDTO();
                newDto.setUserName(profileServiceDTO.getUserName());
                newDto.setTermsandconditions(true);
                newDto.setEnterprise(enterpriseName);
                newDto.setWebClientUUID(profileServiceDTO.getWebClientUUID());
                newDto.setIdentityToken(profileServiceDTO.getIdentityToken());
                foundParty = registerVisitor(newDto, enterpriseName, identityToken);
            }

            profileServiceDTO.setIdentityToken(foundParty.getSecurityIdentity());
            if (!newIp.equals(foundParty)) {
                var orUpdateIdentificationType
                        = foundParty.addOrUpdateIdentificationType(IdentificationTypeWebClientUUID,
                        profileServiceDTO.getWebClientUUID()
                                .toString(),
                        mailSystem.getEnterprise(), identity);
                newIp.archiveIdentificationType(orUpdateIdentificationType, identity);
                newIp = foundParty;
            }
            newIp.addOrUpdate(LoggedOn, "true", mailSystem, identity);

            //newIp.addOrUpdate(RememberMe, profileServiceDTO.isRememberMe() + "", profileSystem, profileSystemUUID);
            if (newIp.hasIdentificationType(IdentificationTypeEnterpriseCreatorRole,null, mailSystem.getEnterprise(), identity)) {
                get(IRolesService.class).addRole(newIp, Administrator, profileServiceDTO, mailSystem, identityToken);
                get(IRolesService.class).addRole(newIp, MailAdministrator, profileServiceDTO, mailSystem, identityToken);
            }
        } catch (Exception e) {
            throw new SecurityAccessException("Invalid username or password", e);
        }

        profileServiceDTO.setPassword(null);

        if (profileServiceDTO.isRememberMe()) {
            newIp.addOrUpdate(RememberMe, "true", mailSystem, identity);
        } else {
            newIp.addOrUpdate(RememberMe, "false", mailSystem, identity);
        }
        profileServiceDTO.findRoles();
        return profileServiceDTO;
    }

    IInvolvedParty<?> registerVisitor(UserRegistrationDTO<?> userRegistrationDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws UserExistsException, WaitingForConfirmationKeyException {
        IInvolvedPartyService<?> involvedPartyService = GuiceContext.get(IInvolvedPartyService.class);
        IEnterprise<?> enterprise = GuiceContext.get(IEnterpriseService.class)
                .getEnterprise(enterpriseName);

        UUID identity = GuiceContext.get(MailSystem.class)
                .getSystemToken(enterpriseName);
        ISystems<?> mailSystem = GuiceContext.get(MailSystem.class)
                .getSystem(enterpriseName);

        IEvent<?> registerEvent = GuiceContext.get(IEventService.class)
                .createEvent(UserRegistered, mailSystem, identity);

        IInvolvedParty<?> ipExists = involvedPartyService.findByIdentificationType(IdentificationTypeEmailAddress,
                new Passwords().integerEncrypt(userRegistrationDTO.getUserName()
                        .getBytes())
                , mailSystem, identity);
        if (ipExists != null) {
            if (ipExists.has(ConfirmationKey, mailSystem, identityToken)) {
                throw new WaitingForConfirmationKeyException("The email address is waiting for a confirmation key");
            }
            throw new UserExistsException("That email address is already in use as a valid identifier");
        }
        IInvolvedParty<?> newIp;
        newIp = createNewVisitor(registerEvent, userRegistrationDTO, mailSystem.getEnterpriseID(), mailSystem, identity);
        newIp.add(MailSystemClassifications.UseMailForLogin, "true", mailSystem, identity);

        return newIp;
    }

    IInvolvedParty<?> createNewVisitor(IEvent<?> event, UserRegistrationDTO<?> profileServiceDTO, IEnterprise<?> enterprise, ISystems<?> profileSystem, UUID... identityToken) {
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

        newIp.addOrUpdateIdentificationType(IdentificationTypeUUID,(IClassification)null, myToken.getSecurityToken(), profileSystem.getEnterprise(), identityToken);
        newIp.addOrUpdateIdentificationType(IdentificationTypeUserName, (IClassification)null, new Passwords().integerEncrypt(profileServiceDTO.getUserName()
                        .getBytes())
                , profileSystem.getEnterprise(), identityToken);

        newIp.addOrReuseNameType(PreferredNameType,(IClassification)null, "Agent", enterprise, identityToken);
        newIp.addOrReuse(CreatedBy, Long.toString(newIp.getId()), profileSystem, identityToken);
        event.addOrReuse(PerformedBy, newIp.getSecurityIdentity()
                .toString(), profileSystem, identityToken);

        profileServiceDTO.setIdentityToken(java.util.UUID.fromString(myToken.getSecurityToken()));
        UUID identity = GuiceContext.get(MailSystem.class)
                .getSystemToken(enterprise);
        ISystems<?> mailSystem = GuiceContext.get(MailSystem.class)
                .getSystem(enterprise);

        get(IRolesService.class).addRole(newIp, MailUserRoles.MailUser, profileServiceDTO, profileSystem, identity);

        return newIp;
    }
}
