import com.guicedee.activitymaster.fsdm.client.services.systems.IMasterSystem;
import com.guicedee.activitymaster.mail.MailSystem;
import com.guicedee.activitymaster.mail.implementations.MailMasterBinder;
import com.guicedee.activitymaster.mail.implementations.MailMasterModuleInclusion;
import com.guicedee.client.services.config.IGuiceScanModuleInclusions;
import com.guicedee.client.services.lifecycle.IGuiceModule;

module com.guicedee.activitymaster.mail {

	// Vert.x mail client (SMTP send) — the comprehensive reactive transport.
	requires com.guicedee.mailclient;
	requires io.vertx.mail.client;
	requires io.vertx.core;

	// Jakarta Mail (IMAP/POP3 receive + MIME parsing/building).
	requires jakarta.mail;
	requires jakarta.activation;

	// Reactive + persistence.
	requires io.smallrye.mutiny;
	requires org.hibernate.reactive;
	requires jakarta.persistence;

	// FSDM warehouse model + services.
	requires com.guicedee.activitymaster.fsdm;
	requires com.guicedee.activitymaster.fsdm.client;

	// GuicedEE runtime.
	requires com.guicedee.client;
	requires com.google.guice;

	requires static lombok;
	requires org.apache.logging.log4j;

	// SPI registrations.
	provides IMasterSystem with MailSystem;
	provides IGuiceModule with MailMasterBinder;
	provides IGuiceScanModuleInclusions with MailMasterModuleInclusion;

	// Public API.
	exports com.guicedee.activitymaster.mail;
	exports com.guicedee.activitymaster.mail.engine;
	exports com.guicedee.activitymaster.mail.servers;
	exports com.guicedee.activitymaster.mail.services;
	exports com.guicedee.activitymaster.mail.services.dto;
	exports com.guicedee.activitymaster.mail.services.classifications;
	exports com.guicedee.activitymaster.mail.services.enumerations;

	// Reflection for Guice / scanning.
	opens com.guicedee.activitymaster.mail to com.google.guice;
	opens com.guicedee.activitymaster.mail.implementations to com.google.guice;
	opens com.guicedee.activitymaster.mail.implementations.updates to com.google.guice;
}



