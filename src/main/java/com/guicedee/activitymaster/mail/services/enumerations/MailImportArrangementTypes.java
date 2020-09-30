package com.guicedee.activitymaster.mail.services.enumerations;

import com.guicedee.activitymaster.core.services.enumtypes.IArrangementTypes;
import com.guicedee.activitymaster.core.services.enumtypes.IClassificationDataConceptValue;

import static com.guicedee.activitymaster.core.services.concepts.EnterpriseClassificationDataConcepts.*;

public enum MailImportArrangementTypes implements IArrangementTypes<MailImportArrangementTypes>
{
	MailImport("MailImport", GlobalClassificationsDataConceptName)
	;
	private String classificationValue;
	private IClassificationDataConceptValue<?> dataConceptValue;

	MailImportArrangementTypes(String classificationValue, IClassificationDataConceptValue<?> dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	MailImportArrangementTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	@Override
	public String classificationValue()
	{
		return classificationValue;
	}

	@Override
	public String classificationDescription()
	{
		return classificationValue;
	}

	@Override
	public IClassificationDataConceptValue<?> concept() {
		return ArrangementType;
	}

}
