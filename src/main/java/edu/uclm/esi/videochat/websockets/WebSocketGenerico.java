package edu.uclm.esi.videochat.websockets;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
	private ConcurrentHashMap<String, WrapperSession> sessions = new ConcurrentHashMap<>();
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		session.setBinaryMessageSizeLimit(1000*1024*1024);
		session.setTextMessageSizeLimit(64*1024);
		
		User user = getUser(session);
		user.setSession(session);

		JSONObject mensaje = new JSONObject();
		mensaje.put("type", "ARRIVAL");
		mensaje.put("userName", user.getName());
		mensaje.put("picture", user.getPicture());
		
		this.broadcast(mensaje);
		
		WrapperSession wrapper = new WrapperSession(session, user);
		this.sessions.put(session.getId(), wrapper);
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
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		JSONObject jso = new JSONObject(message.getPayload());
		String type = jso.getString("type");
		
		String enviador = getUser(session).getName();
		
		if (type.equals("BROADCAST")) {
			JSONObject jsoMessage = new JSONObject();
			jsoMessage.put("type", "FOR ALL");
			jsoMessage.put("time", System.currentTimeMillis());
			jsoMessage.put("message", jso.getString("texto"));
			broadcast(jsoMessage);
			Message mensaje = new Message();
			mensaje.setMessage(jso.getString("texto"));
			mensaje.setSender(enviador);
			guardarMensaje(mensaje);
		} else if (type.equals("PARTICULAR")) {
			String destinatario = jso.getString("destinatario");
			User user = Manager.get().findUser(destinatario);
			WebSocketSession navegadorDelDestinatario = user.getSession();
			
			JSONObject jsoMessage = new JSONObject();
			jsoMessage.put("time", System.currentTimeMillis());
			jsoMessage.put("message", jso.get("texto"));
			
			this.send(navegadorDelDestinatario, "type", "PARTICULAR",
					"remitente", enviador, "message", jsoMessage);
			Message mensaje = new Message();
			mensaje.setMessage(jso.getString("texto"));
			mensaje.setSender(enviador);
			guardarMensaje(mensaje);
		} else if (type.equals("SOLICITUD_VIDEO")) {
			String destinatario = jso.getString("destinatario");
			WrapperSession wrapper = this.sessions.get(session.getId());
			User remitente = wrapper.getUser();
			User user = Manager.get().findUser(destinatario);
			WebSocketSession navegadorDelDestinatario = user.getSession();
			this.send(navegadorDelDestinatario, "type", "SOLICITUD_VIDEO", "remitente", remitente.getName());
		} else if (type.equals("RECHAZO")) {
			String destinatario = jso.getString("destinatario");
			User user = Manager.get().findUser(destinatario);
			WebSocketSession navegadorDeDestinatario = user.getSession();
			this.send(navegadorDeDestinatario, "type", "RECHAZO");
		}
	}

	private void guardarMensaje(Message mensaje) {
		Manager.get().getMessageRepo().save(mensaje);
		
	}
	private void broadcast(JSONObject jsoMessage) {
		TextMessage message = new TextMessage(jsoMessage.toString());
		Collection<WrapperSession> wrappers = this.sessions.values();
		for (WrapperSession wrapper : wrappers) {
			try {
				wrapper.getSession().sendMessage(message);
			} catch (IOException e) {
				this.sessions.remove(wrapper.getSession().getId());
			}
		}
	}
	
	private void broadcast(String... values) {
		JSONObject jsoMessage = new JSONObject();
		for (int i=0; i<values.length; i=i+2) {
			jsoMessage.put(values[i], values[i+1]);
		}
		TextMessage message = new TextMessage(jsoMessage.toString());
		Collection<WrapperSession> wrappers = this.sessions.values();
		for (WrapperSession wrapper : wrappers) {
			try {
				wrapper.getSession().sendMessage(message);
			} catch (IOException e) {
				this.sessions.remove(wrapper.getSession().getId());
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
		WrapperSession wrapper = this.sessions.remove(session.getId());
		Manager.get().remove(wrapper.getUser());
		this.broadcast("type", "BYE", "userName", wrapper.getUser().getName());
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
