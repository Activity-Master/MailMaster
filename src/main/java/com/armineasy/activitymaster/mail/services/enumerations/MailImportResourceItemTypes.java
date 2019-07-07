package com.armineasy.activitymaster.mail.services.enumerations;

import com.armineasy.activitymaster.activitymaster.services.enumtypes.IClassificationDataConceptValue;
import com.armineasy.activitymaster.activitymaster.services.enumtypes.IResourceType;

import static com.armineasy.activitymaster.activitymaster.services.concepts.EnterpriseClassificationDataConcepts.*;

public enum MailImportResourceItemTypes
		implements IResourceType<MailImportResourceItemTypes>
{
	FolderStatusResourceItem("FolderStatusResourceItem", GlobalClassificationsDataConceptName);


	private String classificationValue;
	private IClassificationDataConceptValue<?> dataConceptValue;

	MailImportResourceItemTypes(String classificationValue, IClassificationDataConceptValue<?> dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	MailImportResourceItemTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	@Override
	public String classificationName()
	{
		return name();
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
	public IClassificationDataConceptValue<?> concept()
	{
		return dataConceptValue;
	}

}
