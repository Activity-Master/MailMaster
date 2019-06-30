package com.armineasy.activitymaster.mail.importer;

import com.armineasy.activitymaster.mail.implementations.MailboxBoxService;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;

public class MessageImportJob
		implements Runnable
{
	private MailboxBoxService source;
	private MailboxBoxService dest;
	private final String prefix;
	private final String a;
	private final Integer b;



	public MessageImportJob(MailboxBoxService source, MailboxBoxService dest, String prefix, String folderA, Integer folderB)
	{
		this.source = source;
		this.dest = dest;
		this.prefix = prefix;
		this.a = folderA;
		this.b = folderB;
	}

	@Override
	public void run()
	{
		try
		{
			Folder destFolder;
			Folder srcFolder;
			if (!dest.getFolderMessages()
			         .containsKey(prefix + a))
			{

				destFolder = dest.addFolder(a, prefix);

				dest.getFolderMessages()
				    .put(destFolder.getFullName(), 0);
				srcFolder = source.getFolder(a);
			}
			else
			{
				srcFolder = source.getFolder(a);
				destFolder = dest.getFolder(a);
			}
			if (!srcFolder.isOpen())
			{
				srcFolder.open(Folder.READ_ONLY);
			}
			if (!destFolder.isOpen())
			{
				destFolder.open(Folder.READ_WRITE);
			}
			UIDFolder foldDest = (UIDFolder) destFolder;
			UIDFolder foldSrc = (UIDFolder) srcFolder;
			for (int i = 1; i <= srcFolder.getMessageCount(); i++)
			{
				Message m = srcFolder.getMessage(i);
				Long messageId = foldSrc.getUID(m);
				try
				{
					Message exists = foldDest.getMessageByUID(messageId);
					if (exists == null)
					{
						destFolder.appendMessages(new Message[]{m});
					}
				}
				catch (MessagingException me)
				{
					me.printStackTrace();
				}
			}
		}
		catch (MessagingException e)
		{
			e.printStackTrace();
		}
	}

	public MailboxBoxService getSource()
	{
		return this.source;
	}

	public MailboxBoxService getDest()
	{
		return this.dest;
	}

	public String getPrefix()
	{
		return this.prefix;
	}

	public String getA()
	{
		return this.a;
	}

	public Integer getB()
	{
		return this.b;
	}

	public void setSource(MailboxBoxService source)
	{
		this.source = source;
	}

	public void setDest(MailboxBoxService dest)
	{
		this.dest = dest;
	}

	public boolean equals(final Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (!(o instanceof MessageImportJob))
		{
			return false;
		}
		final MessageImportJob other = (MessageImportJob) o;
		if (!other.canEqual((Object) this))
		{
			return false;
		}
		final Object this$source = this.getSource();
		final Object other$source = other.getSource();
		if (this$source == null ? other$source != null : !this$source.equals(other$source))
		{
			return false;
		}
		final Object this$dest = this.getDest();
		final Object other$dest = other.getDest();
		if (this$dest == null ? other$dest != null : !this$dest.equals(other$dest))
		{
			return false;
		}
		final Object this$prefix = this.getPrefix();
		final Object other$prefix = other.getPrefix();
		if (this$prefix == null ? other$prefix != null : !this$prefix.equals(other$prefix))
		{
			return false;
		}
		final Object this$a = this.getA();
		final Object other$a = other.getA();
		if (this$a == null ? other$a != null : !this$a.equals(other$a))
		{
			return false;
		}
		final Object this$b = this.getB();
		final Object other$b = other.getB();
		if (this$b == null ? other$b != null : !this$b.equals(other$b))
		{
			return false;
		}
		return true;
	}

	protected boolean canEqual(final Object other)
	{
		return other instanceof MessageImportJob;
	}

	public int hashCode()
	{
		final int PRIME = 59;
		int result = 1;
		final Object $source = this.getSource();
		result = result * PRIME + ($source == null ? 43 : $source.hashCode());
		final Object $dest = this.getDest();
		result = result * PRIME + ($dest == null ? 43 : $dest.hashCode());
		final Object $prefix = this.getPrefix();
		result = result * PRIME + ($prefix == null ? 43 : $prefix.hashCode());
		final Object $a = this.getA();
		result = result * PRIME + ($a == null ? 43 : $a.hashCode());
		final Object $b = this.getB();
		result = result * PRIME + ($b == null ? 43 : $b.hashCode());
		return result;
	}

	public String toString()
	{
		return "MessageImportJob(source=" + this.getSource() + ", dest=" + this.getDest() + ", prefix=" + this.getPrefix() + ", a=" + this.getA() + ", b=" + this.getB() + ")";
	}
}
