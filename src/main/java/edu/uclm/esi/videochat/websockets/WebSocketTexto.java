package edu.uclm.esi.videochat.websockets;

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import edu.uclm.esi.videochat.model.Manager;
import edu.uclm.esi.videochat.model.Message;
import edu.uclm.esi.videochat.model.User;
import edu.uclm.esi.videochat.springdao.MessageRepository;


@Component
public class WebSocketTexto extends WebSocketVideoChat {
	@Autowired
	private MessageRepository msgRepo;
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		JSONObject jso = new JSONObject(message.getPayload());
		String type = jso.getString("type");

		String enviador = getUser(session).getName();

		if (type.equals("BROADCAST")) {
			JSONObject jsoMessage = new JSONObject();
			jsoMessage.put("type", "FOR ALL");
			jsoMessage.put("time", formatDate(System.currentTimeMillis()));
			jsoMessage.put("message", jso.getString("message"));
			jsoMessage.put("nombreEnviador", enviador);
			broadcast(jsoMessage);
			Message mensaje = new Message();
			mensaje.setMessage(jso.getString("message"));
			mensaje.setSender(enviador);
			mensaje.setRecipient("ALL_USERS");
			mensaje.setDate(System.currentTimeMillis());
			guardarMensaje(mensaje);
		} else if (type.equals("PARTICULAR")) {
			String destinatario = jso.getString("destinatario");
			String texto=jso.getString("texto");
			//	System.out.println("Aqui llego con mensaje= "+texto+" que se lo envio a "+destinatario);

			User user = Manager.get().findUser(destinatario);
			WebSocketSession navegadorDelDestinatario = user.getSession();

			JSONObject jsoMessage = new JSONObject();
			jsoMessage.put("time", formatDate(System.currentTimeMillis()));
			jsoMessage.put("message", jso.get("texto"));

			JSONObject jsoMessage2 = new JSONObject();
			jsoMessage2.put("type",  "PARTICULAR");
			jsoMessage2.put("remitente", enviador);
			jsoMessage2.put("message", jsoMessage);

			//this.send(navegadorDelDestinatario, "type", "PARTICULAR", "remitente", enviador, "message", jsoMessage);
			if(!enviador.equals(destinatario)) {
				this.broadcast2(destinatario, jsoMessage2);
				Message mensaje = new Message();
				mensaje.setMessage(jso.getString("texto"));
				mensaje.setSender(enviador);
				mensaje.setRecipient(destinatario);
				mensaje.setDate(System.currentTimeMillis());
				guardarMensaje(mensaje);
			}
		}
		else if(type.equals("HISTO")) {
			String destinatario = jso.getString("destinatario");
			JSONObject jsoMessage = new JSONObject();
			jsoMessage.put("type",  "HISTO");
			jsoMessage.put("enviador", enviador);
			jsoMessage.put("destinatario", destinatario);

			this.broadcast2(enviador, jsoMessage);
		}
	

	}

	private void guardarMensaje(Message mensaje) {
		Manager.get().getMessageRepo().save(mensaje);	
	}

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		session.setBinaryMessageSizeLimit(1000*1024*1024);

		byte[] payload = message.getPayload().array();
		System.out.println("La sesión " + session.getId() + " manda un binario de " + payload.length + " bytes");
	}
}
