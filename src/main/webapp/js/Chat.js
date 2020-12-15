class Chat {
	constructor(ko) {
		let self = this;
		this.ko = ko;
		
		this.estado = ko.observable();
		this.usuarios = ko.observableArray([]);
		this.mensajesRecibidos = ko.observableArray([]);
		this.conversaciones = ko.observableArray([]);
		
		this.destinatario = ko.observable();
		this.mensajeQueVoyAEnviar = ko.observable();
		
		this.chat = new WebSocket("wss://" + window.location.host + "/wsTexto");
		
		this.chat.onopen = function() {
			self.estado("Conectado al servidor");
		}
		
		this.chat.onmessage = function(event) {
			var data = JSON.parse(event.data);
			if (data.type == "FOR ALL") {
				var mensaje = new Mensaje(data.message, data.time);
				self.mensajesRecibidos.push(mensaje);
			} else if (data.type == "ARRIVAL") {
				var usuario = new Usuario(data.userName, data.picture);
				self.usuarios.push(usuario);
			} else if (data.type == "BYE") {
				var userName = data.userName;
				for (var i=0; i<self.usuarios().length; i++) {
					if (self.usuarios()[i].nombre == userName) {
						self.usuarios.splice(i, 1);
						break;
					}
				}
			} else if (data.type == "PARTICULAR") {
				var conversacionActual = self.buscarConversacion(data.remitente);
				if (conversacionActual!=null) {
					var mensaje = new Mensaje(data.message.message, data.message.time);
					conversacionActual.addMensaje(mensaje);
				} else {
					conversacionActual = new Conversacion(ko, data.remitente, self, self.videoLlamada);
					var mensaje = new Mensaje(data.message.message, data.message.time);
					conversacionActual.addMensaje(mensaje);
					self.conversaciones.push(conversacionActual);
				}
				self.ponerVisible(data.remitente);
			} 
		}

		this.chat.onclose = function() {
			self.estado("WebSocket cerrado");
		}
	}
	
	close() {
		this.chat.close();
	}
	
	enviar(mensaje) {
		this.chat.send(JSON.stringify(mensaje));
	}
	
	enviarATodos(mensaje) {
		var mensaje = {
			type : "BROADCAST",
			message : mensaje
		};
		this.chat.send(JSON.stringify(mensaje));
	}
	
	buscarConversacion(nombreInterlocutor) {
		for (var i=0; i<this.conversaciones().length; i++) {
			if (this.conversaciones()[i].nombreInterlocutor==nombreInterlocutor)
				return this.conversaciones()[i];
		}
		return null;
	}
	
	setDestinatario(interlocutor) {
		this.destinatario(interlocutor);
		var conversacion = this.buscarConversacion(interlocutor.nombre);
		if (conversacion==null) {
			conversacion = new Conversacion(this.ko, interlocutor.nombre, this, self.videoLlamada);
			this.conversaciones.push(conversacion);
		}
		this.ponerVisible(interlocutor.nombre);
	}
	
	ponerVisible(nombreInterlocutor) {
		for (var i=0; i<this.conversaciones().length; i++) {
			var conversacion = this.conversaciones()[i];
			conversacion.visible(conversacion.nombreInterlocutor == nombreInterlocutor);
		}
	}
	
	addUsuario(userName, picture) {
		this.usuarios.push(new Usuario(userName, picture));
	}
}
	