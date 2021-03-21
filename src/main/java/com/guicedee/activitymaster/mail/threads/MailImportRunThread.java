package com.guicedee.activitymaster.mail.threads;

import com.guicedee.activitymaster.client.services.annotations.ActivityMasterDB;
import com.guicedee.activitymaster.client.services.builders.warehouse.arrangements.IArrangement;
import com.guicedee.activitymaster.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.mail.MailSystem;
import com.guicedee.activitymaster.mail.implementations.MailboxBoxService;
import com.guicedee.activitymaster.mail.servers.GMailMailServer;
import com.guicedee.activitymaster.mail.servers.SaNrgMailServer;
import com.guicedee.activitymaster.mail.services.IMailImportService;
import com.guicedee.activitymaster.mail.services.dto.MailFoldersStatus;
import com.guicedee.activitymaster.mail.services.dto.MailImportTicket;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedpersistence.db.annotations.Transactional;
import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.guicedee.activitymaster.mail.services.classifications.MailSystemResourceItemClassifications.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

public class MailImportRunThread
        extends Thread
        implements Runnable {
    private static final Logger log = Logger.getLogger(MailImportRunThread.class.getName());

    private IArrangement<?,?> arrangement;

    private MailImportTicket ticket;
    private IEnterprise<?,?> enterprise;

    private MailboxBoxService source;
    private MailboxBoxService dest;

    private String prefix;
    private String currentSourceFolderName;

    private long maxMails = 5000L;
    //private long maxSize = 15032385536L;
    private long maxSize = 2147483648L;

    private long startMail = 1L;

    private long currentSize = 0L;
    private long currentMails = 0L;

    private String currentFolder;

    private MailFoldersStatus foldersStatus;

    public MailImportRunThread(IArrangement<?,?> arrangement, IEnterprise<?,?> enterprise, MailImportTicket ticket) {
        this.arrangement = arrangement;
        this.enterprise = enterprise;
        this.ticket = ticket;
        configure();
    }

    public void configure() {
        this.source = new MailboxBoxService(new GMailMailServer(ticket.getGmailAddress(), ticket.getGmailPassword()));
        this.dest = new MailboxBoxService(new SaNrgMailServer(ticket.getSanrgMailAddress(), ticket.getSanrgMailPassword()));
        this.startMail = ticket.getCompletedMails() + 1;
        this.currentFolder = ticket.getCurrentFolder();
    }

    public MailImportRunThread() {
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(entityManagerAnnotation = ActivityMasterDB.class)
    public void run() {
        ticket.setLastRunDate(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss.SSS")
                .format(LocalDateTime.now()));
        ticket.setPaused(false);
        ticket.setStatus("STARTING");
        get(IMailImportService.class)
                .updateMailImportTicket(ticket, enterprise.getName());
        try {
            Map<String, String> foldersToWorkOn = createFolders(dest, source);
            goThrough(foldersToWorkOn);
        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "MAIL IMPORT OOPS", e);
            ticket.setStatus("FAILED - " + e.getMessage());
            ticket.setPaused(true);
            ticket.setTotalSizeForToday(currentSize);
            get(IMailImportService.class)
                    .updateMailImportTicket(ticket, enterprise.getName());
        }
    }

    private Map<String, String> createFolders(MailboxBoxService dest, MailboxBoxService src) {
        Map<String, String> folderMappings = new HashMap<>();
        boolean isGmail = src.getServer() instanceof GMailMailServer;

        for (Map.Entry<String, Integer> entry : source.getFolderMessages()
                .entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            String result = key;
            if (isGmail && !key.equalsIgnoreCase("inbox")) {
                if (key.startsWith("[Gmail]")) {
                    result = result.replace("[Gmail]/", "");
                    try {
                        IMAPFolder imf = (IMAPFolder) src.getFolder(key);
                        for (String attribute : imf.getAttributes()) {
                            if (attribute.contains("\\Trash")) {
                                result = "Trash";
                                break;
                            }
                            if (attribute.contains("\\Sent")) {
                                result = "Sent";
                                break;
                            }
                            if (attribute.contains("\\Junk")) {
                                result = "Spam";
                                break;
                            }
                        }

                        String[] attrs = imf.getAttributes();
                        List<String> attrList = Arrays.asList(attrs);
                        if (attrList.contains("\\All") ||
                                attrList.contains("\\Important") ||
                                attrList.contains("\\Flagged") ||
                                attrList.contains("\\Starred")
                        ) {
                            continue;
                        }

                        boolean doneChildren = false;
                        String allOfIt = result;
                        Folder labelFolder = dest.getFolder(allOfIt);
                        while (!doneChildren) {
                            if (!labelFolder.exists()) {
                                labelFolder.create(Folder.HOLDS_MESSAGES);
                                log.log(Level.INFO, "Created Mail Folder : " + allOfIt);
                            }
                            if (allOfIt.contains("/")) {
                                allOfIt = allOfIt.substring(allOfIt.indexOf('/') + 1);
                                labelFolder = dest.getFolder(allOfIt);
                            } else {
                                doneChildren = true;
                            }
                        }
                    } catch (MessagingException e) {
                        log.log(Level.SEVERE, "Cannot create gmail real destination", e);
                    }
                    folderMappings.put(key, result);
                } else {
                    log.log(Level.WARNING, "Not a real gmail inbox - " + key);
                    try {

                        boolean doneChildren = false;
                        String allOfIt = result;
                        Folder labelFolder = dest.getFolder(allOfIt);
                        while (!doneChildren) {
                            if (!labelFolder.exists()) {
                                labelFolder.create(Folder.HOLDS_MESSAGES);
                                log.log(Level.INFO, "Created Mail Folder : " + allOfIt);
                            }
                            if (allOfIt.contains("/")) {
                                allOfIt = allOfIt.substring(allOfIt.indexOf('/') + 1);
                                labelFolder = dest.getFolder(allOfIt);
                            } else {
                                doneChildren = true;
                            }
                        }
                    } catch (MessagingException e) {
                        log.log(Level.SEVERE, "Can't create destination label folder", e);
                    }
                }
            } else {
                folderMappings.put(result, result);
            }
        }

        for (Map.Entry<String, String> entry : folderMappings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            MailFoldersStatus foldersStatus = new MailFoldersStatus();
            UUID identity = GuiceContext.get(MailSystem.class)
                    .getSystemToken(enterprise);
            ISystems<?,?> mailSystem = GuiceContext.get(MailSystem.class)
                                                   .getSystem(enterprise);

            var objects = arrangement.findResourceItemsAll(FolderStatusObject.toString(), key,mailSystem,false,identity);

            for (var object : objects) {
    
                IResourceItem<?, ?> secondary = object.getSecondary();
                arrangement.addOrReuseResourceItem(FolderStatusObject.toString(),
                        secondary,
                        foldersStatus.toString(),
                        mailSystem,
                        identity);
            }
        }
        return folderMappings;
    }

    private void goThrough(Map<String, String> folderMappings) throws MessagingException {
        String prefix = "";
        for (Map.Entry<String, String> entry : folderMappings
                .entrySet()) {
            String a = entry.getKey();
            String b = entry.getValue();

            if (!a.equals(currentFolder)) {
                continue;
            }

            this.prefix = prefix;
            this.currentSourceFolderName = a;
            runIt(dest, source, folderMappings);
        }
    }

    private void runIt(MailboxBoxService dest, MailboxBoxService src, Map<String, String> folderMappings) throws MessagingException {
        String destFolderName = folderMappings.get(currentSourceFolderName);
        Folder destFolder = dest.getFolder(destFolderName);
        Folder srcFolder = src.getFolder(currentSourceFolderName);

        try {
            if (!srcFolder.isOpen()) {
                srcFolder.open(Folder.READ_ONLY);
            }
            if (!destFolder.isOpen()) {
                destFolder.open(Folder.READ_WRITE);
            }
        } catch (FolderNotFoundException nfe) {
            log.log(Level.WARNING, "Folder not found - " + srcFolder.getFullName(), nfe);
            return;
        }
        try {
            UIDFolder foldDest = (UIDFolder) destFolder;
            UIDFolder foldSrc = (UIDFolder) srcFolder;
            ticket.setCurrentFolder(currentSourceFolderName);
            setCurrentFolder(currentSourceFolderName);
            log.info("Starting Folder : " + currentSourceFolderName);
            //for (int i = 1; i <= srcFolder.getMessageCount(); i++)
            for (int i = (int) startMail; i <= (startMail + maxMails) && currentSize <= maxSize; i++) {
                try {
                    Message m = srcFolder.getMessage(i);
                    Long messageId = foldSrc.getUID(m);
                    //if (exists == null)
                    destFolder.appendMessages(new Message[]{m});
                    ticket.setCompletedMails(startMail + currentMails);
                    this.currentMails++;
                    this.currentSize += m.getSize();
                    ticket.setCompletedSize(ticket.getCompletedSize() + m.getSize());
                    ticket.setTotalSizeForToday(currentSize);
                    get(IMailImportService.class).updateMailImportTicket(ticket, enterprise.getName());
                } catch (MessagingException me) {
                    log.log(Level.WARNING, "Message UID does not exist [" + i + "] - Moving onto the next folder starting at 0", me);
                    ticket.setCompletedMails(0);
                } catch (Exception me) {
                    log.log(Level.SEVERE, "Big Exception In Message UID [" + i + "]", me);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Folder is not a UID folder", e);
        }
    }

    public MailImportTicket getTicket() {
        return this.ticket;
    }

    public void setTicket(MailImportTicket ticket) {
        this.ticket = ticket;
    }

    public IEnterprise<?,?> getEnterprise() {
        return this.enterprise;
    }

    public void setEnterprise(IEnterprise<?,?> enterprise) {
        this.enterprise = enterprise;
    }

    public MailboxBoxService getSource() {
        return this.source;
    }

    public void setSource(MailboxBoxService source) {
        this.source = source;
    }

    public MailboxBoxService getDest() {
        return this.dest;
    }

    public void setDest(MailboxBoxService dest) {
        this.dest = dest;
    }

    public long getMaxMails() {
        return this.maxMails;
    }

    public void setMaxMails(long maxMails) {
        this.maxMails = maxMails;
    }

    public long getMaxSize() {
        return this.maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public long getStartMail() {
        return this.startMail;
    }

    public void setStartMail(long startMail) {
        this.startMail = startMail;
    }

    public long getCurrentSize() {
        return this.currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public long getCurrentMails() {
        return this.currentMails;
    }

    public void setCurrentMails(long currentMails) {
        this.currentMails = currentMails;
    }

    public String getCurrentFolder() {
        return currentFolder;
    }

    public MailImportRunThread setCurrentFolder(String currentFolder) {
        this.currentFolder = currentFolder;
        return this;
    }

    public IArrangement<?,?> getArrangement() {
        return arrangement;
    }

    public MailImportRunThread setArrangement(IArrangement<?,?> arrangement) {
        this.arrangement = arrangement;
        return this;
    }
}
