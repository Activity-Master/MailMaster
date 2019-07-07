package com.armineasy.activitymaster.mail.services.classifications;

import com.armineasy.activitymaster.activitymaster.services.classifications.arrangement.IArrangementClassification;
import com.armineasy.activitymaster.activitymaster.services.classifications.involvedparty.IInvolvedPartyClassification;
import com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.IResourceItemClassification;
import com.armineasy.activitymaster.activitymaster.services.enumtypes.IClassificationDataConceptValue;
import com.armineasy.activitymaster.activitymaster.services.enumtypes.IClassificationValue;

import static com.armineasy.activitymaster.activitymaster.services.concepts.EnterpriseClassificationDataConcepts.*;

public enum MailSystemResourceItemClassifications
		implements IClassificationValue<MailSystemResourceItemClassifications>
				           , IInvolvedPartyClassification<MailSystemResourceItemClassifications>,
				           IArrangementClassification<MailSystemResourceItemClassifications>,
				           IResourceItemClassification<MailSystemResourceItemClassifications>
{
	FolderStatusObject("A folder status object", ArrangementXResourceItem),
	;

	private String description;
	private IClassificationDataConceptValue<?> concept;

	MailSystemResourceItemClassifications(String description, IClassificationDataConceptValue<?> concept)
	{
		this.description = description;
		this.concept = concept;
	}

	MailSystemResourceItemClassifications(String description)
	{
		this.description = description;
	}

	@Override
	public String classificationDescription()
	{
		return this.description;
	}

	@Override
	public IClassificationDataConceptValue<?> concept()
	{
		return concept;
	}
}
