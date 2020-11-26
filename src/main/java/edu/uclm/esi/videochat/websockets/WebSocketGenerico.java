package edu.uclm.esi.videochat.websockets;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import edu.uclm.esi.videochat.model.Manager;
import edu.uclm.esi.videochat.model.Message;
import edu.uclm.esi.videochat.model.User;


@Component
public class WebSocketGenerico extends TextWebSocketHandler {
	private Vector<WebSocketSession> sesiones = new Vector<>();
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		session.setBinaryMessageSizeLimit(1000*1024*1024);
		session.setTextMessageSizeLimit(64*1024);
		System.out.println(session.getId());
		User user = getUser(session);
		user.setSession(session);
		JSONObject mensaje = new JSONObject();
		mensaje.put("type", "ARRIVAL");
		mensaje.put("user", user.getName());
		this.broadcast(mensaje);
		this.sesiones.add(session);
	}

	private User getUser(WebSocketSession session) {
		HttpHeaders headers = session.getHandshakeHeaders();
		List<String> cookies = headers.get("cookie");
		for (String cookie : cookies) {
			int posJSessionId = cookie.indexOf("JSESSIONID=");
			String sessionId = cookie.substring(posJSessionId + 11);
			HttpSession httpSession = Manager.get().getSession(sessionId);
			return (User) httpSession.getAttribute("user");
		}
		return null;
	}

	@Override
	protected void handleTextMessage(WebSocketSession remitente, TextMessage message) throws Exception {
		JSONObject jso = new JSONObject(message.getPayload());
		String type = jso.getString("type");
		
		String enviador = getUser(remitente).getName();
		
		if (type.equals("BROADCAST")) {
			JSONObject jsoMessage = new JSONObject();
			jsoMessage.put("type", "FOR ALL");
			jsoMessage.put("message", jso.getString("message"));
			broadcast(jsoMessage);
		} else if (type.equals("PARTICULAR")) {
			String recipient = jso.getString("recipient");
			User destinatario = Manager.get().findUser(recipient);
			WebSocketSession navegadorDelDestinatario = destinatario.getSession();
			
			JSONObject jsoMensajeAEnviar = new JSONObject();
			jsoMensajeAEnviar.put("texto", jso.get("message"));
			jsoMensajeAEnviar.put("hora", System.currentTimeMillis());
			
			this.send(navegadorDelDestinatario, "type", "PARTICULAR",
					"remitente", enviador, "message", jsoMensajeAEnviar);
		}
		Message mensaje = new Message();
		mensaje.setMessage(jso.getString("message"));
		mensaje.setSender(enviador);
//		if( type.equals("PARTICULAR")) {
//			mensaje.setRecipient(recipient);
//		} else {
//			mensaje.setRecipient(null);
//		}
//		mensaje.setSender(enviador);
//		long date = java.lang.System.currentTimeMillis();
//		mensaje.setDate(date);
		guardarMensaje(mensaje);
		
	}

	private void guardarMensaje(Message mensaje) {
		Manager.get().getMessageRepo().save(mensaje);
		
	}
	private void broadcast(JSONObject jsoMessage) {
		TextMessage message = new TextMessage(jsoMessage.toString());
		for (WebSocketSession destinatario : this.sesiones) {
			try {
				destinatario.sendMessage(message);
			} catch (IOException e) {
				this.sesiones.remove(destinatario);
			}
		}
	}
	
	private void broadcast(String... values) {
		JSONObject jsoMessage = new JSONObject();
		for (int i=0; i<values.length; i=i+2) {
			jsoMessage.put(values[i], values[i+1]);
		}
		TextMessage message = new TextMessage(jsoMessage.toString());
		for (WebSocketSession destinatario : this.sesiones) {
			try {
				destinatario.sendMessage(message);
			} catch (IOException e) {
				this.sesiones.remove(destinatario);
			}
		}
	}
	
	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		session.setBinaryMessageSizeLimit(1000*1024*1024);
		
		byte[] payload = message.getPayload().array();
		System.out.println("La sesión " + session.getId() + " manda un binario de " + payload.length + " bytes");
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		this.sesiones.remove(session);
		this.broadcast("type", "BYE", "user", "Un usuario");
	}
	
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		exception.printStackTrace();
	}
	
	private void send(WebSocketSession session, Object... typesAndValues) {
		JSONObject jso = new JSONObject();
		int i=0;
		while (i<typesAndValues.length) {
			jso.put(typesAndValues[i].toString(), typesAndValues[i+1]);
			i+=2;
		}
		WebSocketMessage<?> wsMessage=new TextMessage(jso.toString());
		try {
			session.sendMessage(wsMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
