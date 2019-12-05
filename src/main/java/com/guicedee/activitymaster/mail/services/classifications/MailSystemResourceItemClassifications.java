package com.guicedee.activitymaster.mail.services.classifications;

import com.guicedee.activitymaster.core.services.classifications.arrangement.IArrangementClassification;
import com.guicedee.activitymaster.core.services.classifications.involvedparty.IInvolvedPartyClassification;
import com.guicedee.activitymaster.core.services.classifications.resourceitems.IResourceItemClassification;
import com.guicedee.activitymaster.core.services.enumtypes.IClassificationDataConceptValue;
import com.guicedee.activitymaster.core.services.enumtypes.IClassificationValue;

import static com.guicedee.activitymaster.core.services.concepts.EnterpriseClassificationDataConcepts.*;

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
