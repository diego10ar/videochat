package edu.uclm.esi.videochat.classicdao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BD implements AutoCloseable {
	private Connection bd;
	private Broker broker;

	public BD(Broker broker) throws SQLException {
		this.broker = broker;
		Connection bd = DriverManager.getConnection("jdbc:mysql://localhost:3306/videochat?serverTimezone=UTC", "videochat", "root");
		this.bd = bd;
	}
	
	public Connection getConnection() {
		return bd;
	}

	@Override
	public void close() throws Exception {
		this.broker.close(this);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return bd.prepareStatement(sql);
	}

	public PreparedStatement prepareStatement(String sql, int returnGeneratedKeys) throws SQLException {
		return bd.prepareStatement(sql, returnGeneratedKeys);
	}
}
