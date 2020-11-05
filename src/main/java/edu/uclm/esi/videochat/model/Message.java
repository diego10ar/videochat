package edu.uclm.esi.videochat.model;

import java.util.UUID;

public class Message {
	private String id;
	private String sender;
	private String recipient;
	private String message;
	private long date;

	public Message(String sender, String recipient, String message) {
		this.id = UUID.randomUUID().toString();
		this.sender = sender;
		this.recipient = recipient;
		this.message = message;
		this.date = System.currentTimeMillis();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	
}
