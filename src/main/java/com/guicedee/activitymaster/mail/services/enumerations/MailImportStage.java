package com.guicedee.activitymaster.mail.services.enumerations;


import static com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum MailImportStage
{
	MailImportNotStarted("MailImportNotStarted", ArrangementXClassification),
	MailImportLoginError("MailImportLoginError", ArrangementXClassification),
	MailImportInProgress("MailImportInProgress", ArrangementXClassification),
	MailImportCompleted("MailImportCompleted", ArrangementXClassification),
	;
	private String classificationValue;
	private com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	MailImportStage(String classificationValue, com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	MailImportStage(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	public String classificationDescription()
	{
		return classificationValue;
	}

	public com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts concept()
	{
		return dataConceptValue;
	}
}
