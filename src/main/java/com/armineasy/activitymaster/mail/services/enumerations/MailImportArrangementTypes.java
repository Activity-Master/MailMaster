package com.armineasy.activitymaster.mail.services.enumerations;

import com.armineasy.activitymaster.activitymaster.services.enumtypes.IArrangementTypes;
import com.armineasy.activitymaster.activitymaster.services.enumtypes.IClassificationDataConceptValue;
import com.armineasy.activitymaster.activitymaster.services.concepts.EnterpriseClassificationDataConcepts;

public enum MailImportArrangementTypes implements IArrangementTypes<MailImportArrangementTypes>
{
	MailImport("MailImport", EnterpriseClassificationDataConcepts.GlobalClassificationsDataConceptName)
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

}
