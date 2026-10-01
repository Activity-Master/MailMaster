package com.guicedee.activitymaster.mail;

import com.guicedee.activitymaster.fsdm.plugins.PluginModels;
import java.util.Objects;
import java.util.UUID;

/** Host-verified mailbox user and installation party; mail headers never establish identity. */
public record MailIdentity(UUID partyId, UUID enterpriseId, UUID identityToken, UUID installationPartyId) {
    public MailIdentity(UUID partyId, UUID enterpriseId, UUID identityToken) {
        this(partyId, enterpriseId, identityToken, partyId);
    }
    public MailIdentity {
        Objects.requireNonNull(partyId); Objects.requireNonNull(enterpriseId);
        Objects.requireNonNull(identityToken); Objects.requireNonNull(installationPartyId);
    }
    public UUID[] tokens() { return new UUID[]{identityToken}; }
    public PluginModels.Identity user() { return new PluginModels.Identity(partyId, enterpriseId, identityToken); }
}
