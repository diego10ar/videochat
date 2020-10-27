package edu.uclm.esi.videochat.classicdao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import edu.uclm.esi.videochat.model.User;

public class UsersDAO {
	public static User insert(User user) throws Exception {
		String sql = "insert into user (id, email, name, pwd, picture) values (?, ?, ?, ?, ?)";
		try(BD bd = Broker.get().getBD()) {
			try(PreparedStatement ps = bd.prepareStatement(sql)) {
				ps.setString(1, user.getId());
				ps.setString(2, user.getEmail());
				ps.setString(3, user.getName());
				ps.setString(4, user.getPwd());
				ps.setBytes(5, user.getPicture());
				ps.executeUpdate();
				return user;
			}
		}
	}
	
	public static User findByUserName(String userName) throws Exception {
		String sql = "SELECT id, email, picture from user where name=?";
		try(BD bd = Broker.get().getBD()) {
			try(PreparedStatement ps = bd.prepareStatement(sql)) {
				ps.setString(1, userName);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					User user = new User();
					user.setName(userName);
					user.setId(rs.getString(1));
					user.setEmail(rs.getString(2));
					user.setPicture(rs.getBytes(3));
					return user;
				}
				return null;
			} 
		}
	}
	
	public static User findByUserNameAndPassword(String userName, String pwd, String ip) throws Exception {
		String sql = "SELECT id from user where name=? and pwd=?";
		try(BD bd = Broker.get().getBD()) {
			try(PreparedStatement ps = bd.prepareStatement(sql)) {
				ps.setString(1, userName);
				ps.setString(2, pwd);
				ResultSet rs = ps.executeQuery();
				String id;
				if (rs.next())
					id = rs.getString(1);
				else
					throw new Exception("Credenciales inválidas");
				
				UsersDAO.insertLogin(id, ip, System.currentTimeMillis());
				User user = new User();
				user.setName(userName);
				return user;
			} 
		}
	}

	private static void insertLogin(String id, String ip, long hora) throws Exception {
		String sql = "insert into logins (id_user, ip, date) values (?, ?, ?)";
		try(BD bd = Broker.get().getBD()) {
			try(PreparedStatement ps = bd.prepareStatement(sql)) {
				ps.setString(1, id);
				ps.setString(2, ip);
				ps.setLong(3, hora);
				ps.executeUpdate();
			}
		}
	}

	public static boolean checkPassword(String name, String pwd) throws Exception {
		String sql = "SELECT count(*) from user where name=? and pwd=?";
		try(BD bd = Broker.get().getBD()) {
			try(PreparedStatement ps = bd.prepareStatement(sql)) {
				ps.setString(1, name);
				ps.setString(2, pwd);
				ResultSet rs = ps.executeQuery();
				rs.next();
				return rs.getInt(1)==1;
			} 
		}
	}

	public static void updatePassword(String name, String pwd1) throws Exception {
		String sql = "update user set pwd=? where name=?";
		try(BD bd = Broker.get().getBD()) {
			try(PreparedStatement ps = bd.prepareStatement(sql)) {
				ps.setString(1, pwd1);
				ps.setString(2, name);
				ps.executeUpdate();
			} 
		}
	}
}
