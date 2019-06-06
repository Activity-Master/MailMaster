package com.armineasy.activitymaster.mail.services.enumerations;

import com.armineasy.activitymaster.activitymaster.services.IClassificationDataConceptValue;
import com.armineasy.activitymaster.activitymaster.services.classifications.arrangement.IArrangementClassification;

import static com.armineasy.activitymaster.activitymaster.services.concepts.EnterpriseClassificationDataConcepts.*;

public enum MailImportStage
		implements IArrangementClassification<MailImportStage>
{
	MailImportNotStarted("MailImportNotStarted", ArrangementXClassification),
	MailImportLoginError("MailImportLoginError", ArrangementXClassification),
	MailImportInProgress("MailImportInProgress", ArrangementXClassification),
	MailImportCompleted("MailImportCompleted", ArrangementXClassification),
	;
	private String classificationValue;
	private IClassificationDataConceptValue<?> dataConceptValue;

	MailImportStage(String classificationValue, IClassificationDataConceptValue<?> dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	MailImportStage(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	@Override
	public String classificationDescription()
	{
		return classificationValue;
	}

	@Override
	public IClassificationDataConceptValue<?> concept()
	{
		return dataConceptValue;
	}
}
