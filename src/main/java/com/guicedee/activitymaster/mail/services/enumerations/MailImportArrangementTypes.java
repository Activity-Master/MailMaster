package com.guicedee.activitymaster.mail.services.enumerations;

import static com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum MailImportArrangementTypes
{
	MailImport("MailImport", ArrangementType)
	;
	private String classificationValue;
	private com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	MailImportArrangementTypes(String classificationValue, com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	MailImportArrangementTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}


	public String classificationValue()
	{
		return classificationValue;
	}


	public String classificationDescription()
	{
		return classificationValue;
	}


	public com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts concept() {
		return ArrangementType;
	}

}
