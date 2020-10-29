package edu.uclm.esi.videochat.model;

import java.util.UUID;
import java.util.Vector;

import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {
	private String id;
	private String email;
	private String name;
	private String pwd;
	private byte[] picture;
	private WebSocketSession session;
	
	public User() {
		this.id = UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String userName) {
		this.name = userName;
	}

	@JsonIgnore
	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public byte[] getPicture() {
		return picture;
	}

	public void setPicture(byte[] picture) {
		this.picture = picture;
	}

	public void setSession(WebSocketSession session) {
		this.session = session;
	}
	
	public WebSocketSession getSession() {
		return session;
	}
}
