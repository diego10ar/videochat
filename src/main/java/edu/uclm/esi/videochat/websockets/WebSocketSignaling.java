package edu.uclm.esi.videochat.websockets;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import edu.uclm.esi.videochat.model.Manager;
import edu.uclm.esi.videochat.model.User;

@Component
public class WebSocketSignaling extends TextWebSocketHandler {
	private ConcurrentHashMap<String, WrapperSession> sessionsByUserName = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, WrapperSession> sessionsById = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, VideoRoom> videoRooms = new ConcurrentHashMap<>();
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		session.setTextMessageSizeLimit(64*1024);
		
		User user = getUser(session);
		user.setSession(session);

		WrapperSession wrapper = new WrapperSession(session, user);
		this.sessionsByUserName.put(user.getName(), wrapper);
		this.sessionsById.put(session.getId(), wrapper);
		
		System.out.println(user.getName() + "-> " + session.getId());
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
	protected void handleTextMessage(WebSocketSession navegadorDelRemitente, TextMessage message) throws Exception {
		JSONObject jso = new JSONObject(message.getPayload());
		String type = jso.getString("type");
		
		WrapperSession wrapperRemitente = this.sessionsById.get(navegadorDelRemitente.getId());
		User remitente = wrapperRemitente.getUser();
		String nombreRemitente = remitente.getName();
		
		String nombreDestinatario = jso.optString("destinatario");
		WrapperSession wrapperDestinatario = null;
		WebSocketSession navegadorDelDestinatario = null;
		if (nombreDestinatario.length()>0) {
			wrapperDestinatario = this.sessionsByUserName.get(nombreDestinatario);
			navegadorDelDestinatario = wrapperDestinatario.getSession();
		}
		
		String sala = jso.optString("sala");

		if (type.equals("CONECTAR_A_SALA")) {
			VideoRoom videoRoom = this.videoRooms.get(sala);
			if (videoRoom==null) {
				videoRoom = new VideoRoom(navegadorDelRemitente, null);
				this.videoRooms.put(sala, videoRoom);
				this.send(navegadorDelRemitente, "type", "SALA_CREADA");
			} else {
				videoRoom.setB(navegadorDelRemitente);
				videoRoom.broadcast("type", "SALA_COMPLETA");
			}
			return;
		} 
		if (type.equals("VIDEO_LOCAL_CONECTADO")) {
			VideoRoom videoRoom = this.videoRooms.get(sala);
			videoRoom.broadcast("type", "VIDEO_LOCAL_CONECTADO");
			return;
		}
		if (type.equals("CANDIDATE")) {
			VideoRoom videoRoom = this.videoRooms.get(sala);
			videoRoom.broadcastString(jso.toString());
			return;
		}
		if (type.equals("SESSION_DESCRIPTION")) {
			VideoRoom videoRoom = this.videoRooms.get(sala);
			videoRoom.broadcastString(jso.getJSONObject("sessionDescription").toString());
			return;
		}
		
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		WrapperSession wrapper = this.sessionsByUserName.remove(session.getId());
		Manager.get().remove(wrapper.getUser());
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
