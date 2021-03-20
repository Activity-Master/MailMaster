package com.guicedee.activitymaster.mail.services.classifications;

import static com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum MailSystemResourceItemClassifications
{
	FolderStatusObject("A folder status object", ArrangementXResourceItem),
	;

	private String description;
	private com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts concept;

	MailSystemResourceItemClassifications(String description, com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts concept)
	{
		this.description = description;
		this.concept = concept;
	}

	MailSystemResourceItemClassifications(String description)
	{
		this.description = description;
	}

	public String classificationDescription()
	{
		return this.description;
	}

	public com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts concept()
	{
		return concept;
	}
}
