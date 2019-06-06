package com.armineasy.activitymaster.mail.services.classifications;

import com.armineasy.activitymaster.activitymaster.services.IClassificationValue;
import com.armineasy.activitymaster.activitymaster.services.IClassificationDataConceptValue;
import com.armineasy.activitymaster.activitymaster.services.classifications.involvedparty.IInvolvedPartyClassification;

import static com.armineasy.activitymaster.activitymaster.services.concepts.EnterpriseClassificationDataConcepts.*;

public enum MailSystemClassifications
		implements IClassificationValue<MailSystemClassifications>
				           , IInvolvedPartyClassification<MailSystemClassifications>
{
	MailImport("The key to use for google", GlobalClassificationsDataConceptName),
	GoogleUserNameKey("The key to use for google", InvolvedPartyXClassification),
	FoldersForImport("The folders to copy across, separated with a /", InvolvedPartyXClassification),
	CurrentDayOfImport("The current date that the import is running on", InvolvedPartyXClassification),
	CurrentSizeOfImport("The accumulated size of the import for that given day", InvolvedPartyXClassification),
	ConfirmedSourceMailImport("If the mail has been confirmed for the source mail import", InvolvedPartyXClassification),
	ConfirmedDestinationMailImport("If the mail destination has been confirmed", InvolvedPartyXClassification),


	;

	private String description;
	private IClassificationDataConceptValue<?> concept;

	MailSystemClassifications(String description, IClassificationDataConceptValue<?> concept)
	{
		this.description = description;
		this.concept = concept;
	}

	MailSystemClassifications(String description)
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
