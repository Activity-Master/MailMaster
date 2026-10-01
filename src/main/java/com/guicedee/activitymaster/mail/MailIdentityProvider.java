package com.guicedee.activitymaster.mail;

import com.google.inject.ImplementedBy;
import io.smallrye.mutiny.Uni;

/** The consuming host binds fresh authentication, including authorized background mailbox jobs. */
@ImplementedBy(MailIdentityProvider.Deny.class)
public interface MailIdentityProvider {
    Uni<MailIdentity> current();
    final class Deny implements MailIdentityProvider {
        public Uni<MailIdentity> current() {
            return Uni.createFrom().failure(new SecurityException("Authenticated mail identity required"));
        }
    }
}
