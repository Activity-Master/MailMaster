package com.guicedee.activitymaster.mail.implementations;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEvent;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.exceptions.SecurityAccessException;
import com.guicedee.activitymaster.mail.MailSystem;
import com.guicedee.activitymaster.mail.roles.MailUserRoles;
import com.guicedee.activitymaster.mail.servers.GMailMailServer;
import com.guicedee.activitymaster.mail.services.IMailBoxService;
import com.guicedee.activitymaster.mail.services.IMailService;
import com.guicedee.activitymaster.mail.services.classifications.MailSystemClassifications;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.exceptions.*;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.activitymaster.profiles.webdto.UserRegistrationDTO;
import com.guicedee.activitymaster.sessions.services.dto.UserLoginDTO;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.pairing.Pair;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications.*;
import static com.guicedee.activitymaster.fsdm.client.services.classifications.EventInvolvedPartiesClassifications.*;
import static com.guicedee.activitymaster.fsdm.client.services.classifications.SecurityTokenClassifications.*;
import static com.guicedee.activitymaster.mail.roles.MailUserRoles.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.services.enumerations.UserRoles.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@SuppressWarnings("Duplicates")
public class MailService
        implements IMailService<MailService> {
    
    @Inject
    private IEnterprise<?,?> enterprise;
    
    @Inject
    private IInvolvedPartyService<?> involvedPartyService;
    
    @Override
    public IInvolvedParty<?,?> findByEmail(String email, ISystems<?,?> systems, java.util.UUID... identityToken) {
        IInvolvedParty<?, ?> party = involvedPartyService.get()
                                                         .builder()
                                                         .findByIdentificationType(IdentificationTypeEmailAddress.toString(), email, systems, identityToken)
                                                         .get()
                                                         .orElse(null);
        return party;
    }

    @Override
    public ProfileServiceDTO<?> loginUser(UserLoginDTO<?> profileServiceDTO, String enterpriseName, java.util.UUID... identityToken) throws ProfileServiceException {

        profileServiceDTO.setEnterprise(enterprise);
        UUID identity = GuiceContext.get(MailSystem.class)
                .getSystemToken(enterpriseName);
        ISystems<?,?> mailSystem = GuiceContext.get(MailSystem.class)
                                               .getSystem(enterpriseName);

        if ((identityToken == null || identityToken.length == 0) && profileServiceDTO.getIdentityToken() == null) {
            identityToken = new UUID[]{identity};
        }
    
        IInvolvedParty<?, ?> newIp = involvedPartyService.get()
                                                         .builder()
                                                         .findByIdentificationType(IdentificationTypeWebClientUUID.toString(),
                                                                 profileServiceDTO.getWebClientUUID()
                                                                                  .toString(), mailSystem, identity)
                                                         .get()
                                                         .orElse(null);

        try (IMailBoxService<?> service = IMailBoxService.get(new GMailMailServer(profileServiceDTO.getUserName(), profileServiceDTO.getPassword()))) {
            service.login();

            IMailService<?> mailService = get(IMailService.class);
            IInvolvedParty<?,?> foundParty = mailService.findByEmail(profileServiceDTO.getUserName(),
                    mailSystem,
                    identity);

            if (foundParty == null) {
                UserRegistrationDTO<?> newDto = new UserRegistrationDTO();
                newDto.setUserName(profileServiceDTO.getUserName());
                newDto.setTermsandconditions(true);
                newDto.setEnterprise(enterprise);
                newDto.setWebClientUUID(profileServiceDTO.getWebClientUUID());
                newDto.setIdentityToken(profileServiceDTO.getIdentityToken());
                foundParty = registerVisitor(newDto, enterpriseName, identityToken);
            }

            profileServiceDTO.setIdentityToken(foundParty.getId());
            if (!newIp.equals(foundParty)) {
                var orUpdateIdentificationType
                        = foundParty.addOrUpdateInvolvedPartyIdentificationType(IdentificationTypeWebClientUUID.toString(),
                        profileServiceDTO.getWebClientUUID()
                                .toString(),null,null,
                        mailSystem, identity);
                orUpdateIdentificationType.archive(mailSystem,identityToken);
              //  newIp.archiveIdentificationType(orUpdateIdentificationType, identity);
                newIp = foundParty;
            }
            newIp.addOrUpdateClassification(LoggedOn.toString(), "true",null, mailSystem, identity);

            //newIp.addOrUpdateClassification(RememberMe, profileServiceDTO.isRememberMe() + "", profileSystem, profileSystemUUID);
            if (newIp.hasInvolvedPartyIdentificationTypes(NoClassification.toString(), IdentificationTypeEnterpriseCreatorRole.toString(),null, mailSystem, identity)) {
                get(IRolesService.class).addRole(newIp, Administrator.toString(), profileServiceDTO, mailSystem, identityToken);
                get(IRolesService.class).addRole(newIp, MailAdministrator.toString(), profileServiceDTO, mailSystem, identityToken);
            }
        } catch (Exception e) {
            throw new SecurityAccessException("Invalid username or password", e);
        }

        profileServiceDTO.setPassword(null);

        if (profileServiceDTO.isRememberMe()) {
            newIp.addOrUpdateClassification(RememberMe.toString(), "true",null, mailSystem, identity);
        } else {
            newIp.addOrUpdateClassification(RememberMe.toString(), "false",null, mailSystem, identity);
        }
        profileServiceDTO.findRoles();
        return profileServiceDTO;
    }

    IInvolvedParty<?,?> registerVisitor(UserRegistrationDTO<?> userRegistrationDTO, String  enterpriseName, java.util.UUID... identityToken) throws UserExistsException, WaitingForConfirmationKeyException {

        UUID identity = GuiceContext.get(MailSystem.class)
                .getSystemToken(enterpriseName);
        ISystems<?,?> mailSystem = GuiceContext.get(MailSystem.class)
                .getSystem(enterpriseName);

        IEvent<?,?> registerEvent = GuiceContext.get(IEventService.class)
                .createEvent(UserRegistered.toString(), mailSystem, identity);
    
        IInvolvedParty<?, ?> ipExists = involvedPartyService.get()
                                                            .builder()
                                                            .findByIdentificationType(IdentificationTypeEmailAddress.toString(),
                                                                    userRegistrationDTO.getUserName()
                                                                    , mailSystem, identity)
                                                            .get()
                                                            .orElse(null);
        if (ipExists != null) {
            if (ipExists.hasResourceItems(ConfirmationKey.toString(),null, mailSystem, identityToken)) {
                throw new WaitingForConfirmationKeyException("The email address is waiting for a confirmation key");
            }
            throw new UserExistsException("That email address is already in use as a valid identifier");
        }
        IInvolvedParty<?,?> newIp;
        newIp = createNewVisitor(registerEvent, userRegistrationDTO, mailSystem.getEnterpriseID(), mailSystem, identity);
        newIp.addClassification(MailSystemClassifications.UseMailForLogin.toString(), "true", mailSystem, identity);

        return newIp;
    }

    IInvolvedParty<?,?> createNewVisitor(IEvent<?,?> event, UserRegistrationDTO<?> profileServiceDTO, IEnterprise<?,?> enterprise, ISystems<?,?> profileSystem, java.util.UUID... identityToken) {
        IInvolvedParty<?,?> newIp;
        //Create new guest record
        Pair<String, String> guestIDType = new Pair<>();
        guestIDType.setKey(IdentificationTypeEmailAddress.toString())
                .setValue(profileServiceDTO.getUserName());

        profileServiceDTO.setWebClientUUID(UUID.randomUUID());

        newIp = involvedPartyService.create(profileSystem, guestIDType, true, identityToken);

        ISecurityToken<?,?> visitorsGroup = GuiceContext.get(ISecurityTokenService.class)
                                                      .getRegisteredGuestsFolder(profileSystem, identityToken);

        ISecurityToken<?,?> myToken = get(ISecurityTokenService.class).create(Identity.toString(),
                profileServiceDTO.getUserName(),
                "An agent registration",
                profileSystem,
                visitorsGroup,
                identityToken);

        newIp.addOrUpdateInvolvedPartyIdentificationType(NoClassification.toString(), IdentificationTypeUUID,null, myToken.getSecurityToken(), profileSystem, identityToken);
        newIp.addOrUpdateInvolvedPartyIdentificationType(NoClassification.toString(),IdentificationTypeUserName,null,profileServiceDTO.getUserName(), profileSystem, identityToken);

        newIp.addOrReuseInvolvedPartyNameType(NoClassification.toString(),PreferredNameType.toString(),"Agent", profileSystem, identityToken);
        newIp.addOrReuseClassification(CreatedBy, newIp.getId().toString(), profileSystem, identityToken);
        event.addOrReuseClassification(PerformedBy, newIp.getSecurityIdentity()
                .toString(), profileSystem, identityToken);

        profileServiceDTO.setIdentityToken(newIp.getId());
        UUID identity = GuiceContext.get(MailSystem.class)
                .getSystemToken(enterprise);
        ISystems<?,?> mailSystem = GuiceContext.get(MailSystem.class)
                .getSystem(enterprise);

        get(IRolesService.class).addRole(newIp, MailUserRoles.MailUser.toString(), profileServiceDTO, profileSystem, identity);

        return newIp;
    }
}
