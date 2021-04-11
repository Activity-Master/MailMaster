package com.guicedee.activitymaster.mail.services.enumerations;


import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum MailImportResourceItemTypes
{
	FolderStatusResourceItem("FolderStatusResourceItem", GlobalClassificationsDataConceptName);


	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	MailImportResourceItemTypes(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	MailImportResourceItemTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	public String classificationName()
	{
		return name();
	}

	public String classificationValue()
	{
		return classificationValue;
	}

	public String classificationDescription()
	{
		return classificationValue;
	}


	public com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept()
	{
		return dataConceptValue;
	}

}
