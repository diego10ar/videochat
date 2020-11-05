package edu.uclm.esi.videochat.classicdao;

import java.sql.PreparedStatement;

import edu.uclm.esi.videochat.model.Message;

public class MessageDAO {

	public static void insert(Message message) {
		String sql = "insert into message (id, sender, recipient, message, date) values (?, ?, ?, ?, ?)";
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try(BD bd = Broker.get().getBD()) {
					try(PreparedStatement ps = bd.prepareStatement(sql)) {
						ps.setString(1, message.getId());
						ps.setString(2, message.getSender());
						ps.setString(3, message.getRecipient());
						ps.setString(4, message.getMessage());
						ps.setLong(5, message.getDate());
						ps.executeUpdate();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}		
			}
		};
		
		new Thread(r).start();
	}

}
