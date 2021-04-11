package com.guicedee.activitymaster.mail.services.classifications;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum MailSystemClassifications
{
	MailImport("A mail import classification", GlobalClassificationsDataConceptName),

	MailImportFor("Who the mail import is for", GlobalClassificationsDataConceptName),

	UseMailForLogin("Whether or not to use mail for login", ArrangementXClassification),

	SourceUserNameKey("The key to use for source on mail import", ArrangementXClassification),
	SourcePassKey("The key to use for source on mail import", ArrangementXClassification),
	TargetUserNameKey("The key to use for target on mail import", ArrangementXClassification),
	TargetPassKey("The key to use for target on mail import", ArrangementXClassification),

	FoldersForImport("The folders to copy across, separated with a /", ArrangementXClassification),

	LastDayOfImport("The last/previous date for the the import or a predefined one", ArrangementXClassification),

	CurrentDayOfImport("The current date that the import is running on", ArrangementXClassification),
	CurrentDaySizeOfImport("The accumulated size of the import for that given day", ArrangementXClassification),

	ConfirmedSourceMailImport("If the mail has been confirmed for the source mail import", ArrangementXClassification),
	ConfirmedDestinationMailImport("If the mail destination has been confirmed", ArrangementXClassification),

	TotalCountOfMailImport("Total count of mails for mail import", ArrangementXClassification),
	CompletedMailImport("Total count of mails completed for mail import", ArrangementXClassification),
	CurrentFolderImport("The current folder for the import", ArrangementXClassification),
	CompletedFolderImport("Total count of mails completed for mail import", ArrangementXClassification),
	CompletedSizeImport("Total count of mails completed for mail import", ArrangementXClassification),

	TotalFoldersForMailImport("Total folders for mail import", ArrangementXClassification),
	TotalSizeForMailImport("Total size for mail import", ArrangementXClassification),

	JobStartedForMailImport("Status of the job started for mail import", ArrangementXClassification),
	JobPausedForMailImport("If the mail import has been paused", ArrangementXClassification),

	;

	private String description;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept;

	MailSystemClassifications(String description, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept)
	{
		this.description = description;
		this.concept = concept;
	}

	MailSystemClassifications(String description)
	{
		this.description = description;
	}

	public String classificationDescription()
	{
		return this.description;
	}

	public com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept()
	{
		return concept;
	}
}
