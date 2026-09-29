package com.guicedee.activitymaster.mail.implementations;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.guicedee.activitymaster.mail.MailSystem;
import com.guicedee.activitymaster.mail.services.IMailFsdmService;
import com.guicedee.activitymaster.mail.services.IMailSystem;
import com.guicedee.activitymaster.mail.services.IMailTransportService;
import com.guicedee.client.services.lifecycle.IGuiceModule;

/**
 * Guice bindings for the Mail Master module: the mail system, the reactive Vert.x-backed transport
 * service and the FSDM mapping service.
 * <p>
 * Each service is a CRTP interface ({@code IMailTransportService<J>}), so it is bound under three
 * keys that all resolve to the same singleton: the concrete parameterisation, the wildcard
 * {@code IMailTransportService<?>} a consumer normally injects, and the raw type. Binding only the
 * raw key — as this module previously did — makes the natural
 * {@code @Inject IMailTransportService<?>} fail at injector boot with a missing-implementation
 * error, which is a confusing failure for something that is in fact bound.
 */
public class MailMasterBinder
		extends PrivateModule
		implements IGuiceModule<MailMasterBinder>
{
	@Override
	protected void configure()
	{
		bindService(IMailSystem.class, new TypeLiteral<IMailSystem<?>>() {},
				new TypeLiteral<IMailSystem<MailSystem>>() {}, MailSystem.class);

		bindService(IMailTransportService.class, new TypeLiteral<IMailTransportService<?>>() {},
				new TypeLiteral<IMailTransportService<MailTransportService>>() {}, MailTransportService.class);

		bindService(IMailFsdmService.class, new TypeLiteral<IMailFsdmService<?>>() {},
				new TypeLiteral<IMailFsdmService<MailFsdmService>>() {}, MailFsdmService.class);
	}

	/**
	 * Binds one CRTP service under its concrete, wildcard and raw keys, all sharing a single
	 * singleton, and exposes all three from this private module.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private <I, C extends I> void bindService(Class<I> rawType, TypeLiteral<?> wildcard, TypeLiteral<?> concrete,
	                                          Class<C> implementation)
	{
		Key wildcardKey = Key.get(wildcard);
		Key concreteKey = Key.get(concrete);

		bind(concreteKey).to(implementation)
		                 .in(Singleton.class);
		bind(wildcardKey).to(concreteKey);
		bind(rawType).to(wildcardKey);

		expose(concreteKey);
		expose(wildcardKey);
		expose(rawType);
	}
}
