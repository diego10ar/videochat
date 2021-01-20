package edu.uclm.esi.videochat.websockets;

import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import edu.uclm.esi.videochat.model.User;

@Component
public class WebSocketSignaling extends WebSocketVideoChat {
	private ConcurrentHashMap<String, VideoRoom> videoRooms = new ConcurrentHashMap<>();
	
	
	@Override
	protected void handleTextMessage(WebSocketSession navegadorDelRemitente, TextMessage message) throws Exception {
		JSONObject jso = new JSONObject(message.getPayload());
		String type = jso.getString("type");
		
		User remitente = this.sessionsById.get(navegadorDelRemitente.getId()).getUser();
		String nombreRemitente = remitente.getName();
		
		String recipient = jso.optString("recipient");
		WebSocketSession navegadorDelDestinatario = null;
		WebSocketSession navegadorDelDestinatario2 = null;
		if (recipient.length()>0)
			navegadorDelDestinatario = this.sessionsByUserName.get(recipient).getSession();

		if (type.equals("OFFER")) {
			VideoRoom videoRoom = new VideoRoom(navegadorDelRemitente, navegadorDelDestinatario);
			this.videoRooms.put("1", videoRoom);
			this.send(navegadorDelDestinatario, "type", "OFFER", "remitente", nombreRemitente, "sessionDescription", jso.get("sessionDescription"));
			return;
		}
		if (type.equals("OFFER_GRUPO")) {
			VideoRoom videoRoom = new VideoRoom(navegadorDelRemitente, navegadorDelDestinatario);
			this.videoRooms.put("1", videoRoom);
			this.send(navegadorDelDestinatario, "type", "OFFER_GRUPO", "remitente", nombreRemitente, "sessionDescription", jso.get("sessionDescription"));
			return;
		}
		if (type.equals("OFFER_GRUPO1")) {
			VideoRoom videoRoom = new VideoRoom(navegadorDelRemitente, navegadorDelDestinatario);
			this.videoRooms.put("2", videoRoom);
			this.send(navegadorDelDestinatario, "type", "OFFER_GRUPO2", "remitente", nombreRemitente, "sessionDescription", jso.get("sessionDescription"));
			return;
		}

		if (type.equals("ARRANCA")) {
		
			System.out.println("llego arranca para llamar a "+recipient+ " de parte de "+remitente.getName());
			JSONObject jsoMessage2 = new JSONObject();
			jsoMessage2.put("type",  "BE_READY");
			jsoMessage2.put("recibeLlamda", recipient);
			jsoMessage2.put("haceLlamada", remitente.getName());
			this.broadcast2(recipient, jsoMessage2);
			return;
		}
		if (type.equals("ARRANCA_GRUPO")) {

			JSONObject jsoMessage2 = new JSONObject();
			jsoMessage2.put("type",  "BE_READY_GRUPO");
			jsoMessage2.put("recibeLlamda", recipient);
			jsoMessage2.put("haceLlamada", remitente.getName());
			this.broadcast2(recipient, jsoMessage2);
			return;
		}
		if (type.equals("ARRANCA_GRUPO1")) {

			JSONObject jsoMessage2 = new JSONObject();
			jsoMessage2.put("type",  "BE_READY_GRUPO");
			jsoMessage2.put("recibeLlamda", recipient);
			jsoMessage2.put("haceLlamada", remitente.getName());
			this.broadcast2(recipient, jsoMessage2);
			return;
		}
		if (type.equals("RECHAZO")) {
			System.out.println("OYE "+recipient+" que "+jso.getString("recibe")+" no quiere hablar contigo");
			JSONObject jsoMessage2 = new JSONObject();
			jsoMessage2.put("type",  "RECHAZO");
			jsoMessage2.put("recibeLlamda", jso.getString("recibe"));
			jsoMessage2.put("haceLlamada", recipient);
			this.broadcast2(recipient, jsoMessage2);
			return;
		}
		if (type.equals("IM_READY")) {
			
			
			JSONObject jsoMessage2 = new JSONObject();
			jsoMessage2.put("type",  "IM_READY");
			jsoMessage2.put("haceLlamada", recipient);
			jsoMessage2.put("recibe", remitente.getName());
			this.broadcast2(recipient, jsoMessage2);
			return;
		}
			if (type.equals("IM_READY_GRUPO")) {
			
			
			JSONObject jsoMessage2 = new JSONObject();
			jsoMessage2.put("type",  "IM_READY_GRUPO");
			jsoMessage2.put("haceLlamada", recipient);
			jsoMessage2.put("recibe", remitente.getName());
			this.broadcast2(recipient, jsoMessage2);
			return;
		}
			
		if (type.equals("ANSWER")) {
			VideoRoom videoRoom = this.videoRooms.get("1");
			this.send(videoRoom.getA(), "type", "ANSWER", "sessionDescription", jso.get("sessionDescription"));
			return;
		}
		if (type.equals("ANSWER_GRUPO")) {
			VideoRoom videoRoom = this.videoRooms.get("1");
			this.send(videoRoom.getA(), "type", "ANSWER_GRUPO", "sessionDescription", jso.get("sessionDescription"));
			return;
		}if (type.equals("ANSWER_GRUPO1")) {
			VideoRoom videoRoom = this.videoRooms.get("2");
			this.send(videoRoom.getA(), "type", "ANSWER_GRUPO1", "sessionDescription", jso.get("sessionDescription"));
			return;
		}
		
		if (type.equals("CANDIDATE")) {
			VideoRoom videoRoom = this.videoRooms.get("1");
			videoRoom.broadcast("type", "CANDIDATE", "candidate", jso.get("candidate"));
		}
		if (type.equals("CANDIDATE_GRUPO")) {
			VideoRoom videoRoom = this.videoRooms.get("1");
			videoRoom.broadcast("type", "CANDIDATE", "candidate", jso.get("candidate"));
		}
		if (type.equals("CANDIDATE_GRUPO1")) {
			VideoRoom videoRoom = this.videoRooms.get("2");
			videoRoom.broadcast("type", "CANDIDATE", "candidate", jso.get("candidate"));
		}

	}
}
