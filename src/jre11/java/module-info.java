module com.armineasy.activitymaster.mail {

	requires java.mail;

	requires lombok;
	requires org.mapstruct;
	requires net.sf.uadetector.core;
	requires org.json;
	requires com.jwebmp.guicedpersistence;
	requires com.jwebmp.guicedservlets;

	requires cache.annotations.ri.common;
	requires cache.annotations.ri.guice;
	requires cache.api;

	requires com.armineasy.activitymaster.activitymaster;
	requires com.google.guice;

	requires com.jwebmp.guicedinjection;
	requires com.google.common;
	requires javax.servlet.api;

	provides com.armineasy.activitymaster.activitymaster.services.IActivityMasterSystem with com.armineasy.activitymaster.mail.MailSystem;
	provides com.jwebmp.guicedinjection.interfaces.IGuiceModule with com.armineasy.activitymaster.mail.implementations.MailMasterBinder;

	opens com.armineasy.activitymaster.mail;
	exports com.armineasy.activitymaster.mail.servers;
	exports com.armineasy.activitymaster.mail.services;
	exports com.armineasy.activitymaster.mail.services.enumerations;
	exports com.armineasy.activitymaster.mail;

}
