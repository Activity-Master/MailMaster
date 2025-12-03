import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;

module com.guicedee.activitymaster.mail {

	requires jakarta.mail;

	requires com.guicedee.activitymaster.profiles;

	requires net.sf.uadetector.core;
	requires org.json;
	requires com.guicedee.guicedpersistence;
	requires com.guicedee.guicedservlets;

	requires cache.annotations.ri.common;
	requires cache.annotations.ri.guice;
	requires cache.api;

	requires java.sql;

	requires com.guicedee.activitymaster.fsdm;
	requires com.google.guice;

	requires com.guicedee.guicedinjection;
	requires com.google.common;

	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires com.guicedee.activitymaster.sessions;
	requires com.guicedee.activitymaster.fsdm.client;
	requires com.entityassist;
	
	provides IActivityMasterSystem with com.guicedee.activitymaster.mail.MailSystem;
	provides com.guicedee.client.services.lifecycle.IGuiceModule with com.guicedee.activitymaster.mail.implementations.MailMasterBinder;

	opens com.guicedee.activitymaster.mail;
	exports com.guicedee.activitymaster.mail.servers;
	exports com.guicedee.activitymaster.mail.services;
	exports com.guicedee.activitymaster.mail.services.enumerations;
	exports com.guicedee.activitymaster.mail;
	exports com.guicedee.activitymaster.mail.services.classifications;
	exports com.guicedee.activitymaster.mail.services.dto;

	opens com.guicedee.activitymaster.mail.services.dto to com.fasterxml.jackson.databind;
	opens com.guicedee.activitymaster.mail.implementations to com.fasterxml.jackson.databind, com.google.guice;
	exports com.guicedee.activitymaster.mail.importer;
	exports com.guicedee.activitymaster.mail.threads;
	exports com.guicedee.activitymaster.mail.roles;

}
