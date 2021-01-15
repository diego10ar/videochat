package edu.uclm.esi.videochat.springdao;

import java.sql.ResultSet;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.uclm.esi.videochat.model.Message;


public interface MessageRepository extends CrudRepository <Message, String> {

	@Query(value = "SELECT * FROM message where (sender=:sender and recipient=:recipient) or (sender=:recipient and recipient=:sender) order by date", nativeQuery = true)
	public List<Message> findByEnvAndDest(@Param("sender") String sender,@Param("recipient") String recipient);
	
}
