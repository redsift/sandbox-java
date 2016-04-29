package io.jmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@JsonInclude(content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private static ObjectMapper mapper = new ObjectMapper();

    public String id;
    public String threadId;
    public String[] mailboxIds;
    public String inReplyToMessageId;
    public boolean isUnread;
    public boolean isFlagged;
    public boolean isAnswered;
    public boolean isDraft;
    public boolean hasAttachment;
    public Map<String, String> headers;
    public Emailer from;
    public Emailer[] to;
    public Emailer[] cc;
    public Emailer[] bcc;
    public Emailer replyTo;
    public String subject;
    public String date;
    public long size;
    public String preview;
    public String textBody;
    public String htmlBody;
    public String strippedHtmlBody;
    public Attachment[] attachments;
    public Map<String, Message> attachedMessages;
    public String user;

    public Message(@JsonProperty("id") String id, @JsonProperty("threadId") String threadId,
                   @JsonProperty("mailboxIds") String[] mailboxIds,
                   @JsonProperty("inReplyToMessageId") String inReplyToMessageId,
                   @JsonProperty("isUnread") boolean isUnread, @JsonProperty("isFlagged") boolean isFlagged,
                   @JsonProperty("isAnswered") boolean isAnswered, @JsonProperty("isDraft") boolean isDraft,
                   @JsonProperty("hasAttachment") boolean hasAttachment,
                   @JsonProperty("headers") Map<String, String> headers, @JsonProperty("from") Emailer from,
                   @JsonProperty("to") Emailer[] to, @JsonProperty("cc") Emailer[] cc,
                   @JsonProperty("bcc") Emailer[] bcc, @JsonProperty("replyTo") Emailer replyTo,
                   @JsonProperty("subject") String subject, @JsonProperty("date") String date,
                   @JsonProperty("size") long size, @JsonProperty("preview") String preview,
                   @JsonProperty("textBody") String textBody, @JsonProperty("htmlBody") String htmlBody,
                   @JsonProperty("strippedHtmlBody") String strippedHtmlBody,
                   @JsonProperty("attachments") Attachment[] attachments,
                   @JsonProperty("attachedMessages") Map<String, Message> attachedMessages,
                   @JsonProperty("user") String user) {
        this.id = id;
        this.threadId = threadId;
        this.mailboxIds = mailboxIds;
        this.inReplyToMessageId = inReplyToMessageId;
        this.isUnread = isUnread;
        this.isFlagged = isFlagged;
        this.isAnswered = isAnswered;
        this.isDraft = isDraft;
        this.hasAttachment = hasAttachment;
        this.headers = headers;
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.replyTo = replyTo;
        this.subject = subject;
        this.date = date;
        this.size = size;
        this.preview = preview;
        this.textBody = textBody;
        this.htmlBody = htmlBody;
        this.strippedHtmlBody = strippedHtmlBody;
        this.attachments = attachments;
        this.attachedMessages = attachedMessages;
        this.user = user;
    }

    public Message(byte[] b) throws IOException {
        Message m = mapper.readValue(b, Message.class);
        this.id = m.id;
        this.threadId = m.threadId;
        this.mailboxIds = m.mailboxIds;
        this.inReplyToMessageId = m.inReplyToMessageId;
        this.isUnread = m.isUnread;
        this.isFlagged = m.isFlagged;
        this.isAnswered = m.isAnswered;
        this.isDraft = m.isDraft;
        this.hasAttachment = m.hasAttachment;
        this.headers = m.headers;
        this.from = m.from;
        this.to = m.to;
        this.cc = m.cc;
        this.bcc = m.bcc;
        this.replyTo = m.replyTo;
        this.subject = m.subject;
        this.date = m.date;
        this.size = m.size;
        this.preview = m.preview;
        this.textBody = m.textBody;
        this.htmlBody = m.htmlBody;
        this.strippedHtmlBody = m.strippedHtmlBody;
        this.attachments = m.attachments;
        this.attachedMessages = m.attachedMessages;
        this.user = m.user;
    }
}
