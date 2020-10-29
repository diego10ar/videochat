package edu.uclm.esi.videochat.model;

import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import edu.uclm.esi.videochat.model.User;

public class Manager {
	
	private ConcurrentHashMap<String, User> usersMap;
	private ConcurrentHashMap<String, HttpSession> sessions;

	private Manager() {
		this.usersMap = new ConcurrentHashMap<>();
		this.sessions = new ConcurrentHashMap<>();
	}
	
	private static class ManagerHolder {
		static Manager singleton=new Manager();
	}
	
	public static Manager get() {
		return ManagerHolder.singleton;
	}
	
	public void add(User user) {
		usersMap.put(user.getName(), user);
	}
	
	public void remove(User user) {
		this.usersMap.remove(user.getName());
	}
	
	public Vector<String> getUserNames() {
		Vector<String> users = new Vector<>();
		Enumeration<User> eUsers = this.usersMap.elements();
		while (eUsers.hasMoreElements()) {
			users.add(eUsers.nextElement().getName());
		}
		return users;
	}

	public HttpSession getSession(String sessionId) {
		return this.sessions.get(sessionId);
	}

	public void add(HttpSession session) {
		this.sessions.put(session.getId(), session);
	}

	public User findUser(String userName) {
		return this.usersMap.get(userName);
	}
}
