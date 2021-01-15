class Chat {
	constructor(ko) {
		let self = this;
		this.ko = ko;
		
		this.estado = ko.observable("");
		this.error = ko.observable();
		//this.enviador=ko.observable();
		this.usuarios = ko.observableArray([]);
		this.mensajesRecibidos = ko.observableArray([]);
		this.conversaciones = ko.observableArray([]);
		this.histo=ko.observableArray([]);
		this.conversacionesHistorial = ko.observableArray([]);
		this.destinatario = ko.observable();
		this.remitente=ko.observable();
		this.mensajeQueVoyAEnviar = ko.observable();
		this.textoAEnviar = ko.observable();
		this.chat = new WebSocket("wss://" + window.location.host + "/wsTexto");
		
		this.chat.onopen = function() {
			self.estado("Conectado al chat de texto");
			self.error("");
		}

		this.chat.onerror = function() {
			self.estado("");
			self.error("Chat de texto cerrado");
		}

		this.chat.onclose = function() {
			self.estado("");
			self.error("Chat de texto cerrado");
		}
		
		this.chat.onmessage = function(event) {
			console.log("llego al onmessage")
			
			var data = JSON.parse(event.data);
			var tipo=data.type;
			console.log(tipo);
			if (data.type == "FOR ALL") {
				var mensaje = new Mensaje(data.nombreEnviador, data.message, data.time);
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
				var mess=data;
				var remi=data.remitente;
				console.log(mess);
				console.log(remi);
				console.log(data.message.remitente);
				if(typeof remi =="undefined"){
					console.log("no esta");
					remi="YO:";
				}
				var conversacionActual = self.buscarConversacion(data.remitente);
				if (conversacionActual!=null) {
					var mensaje = new Mensaje(remi, data.message.message, data.message.time);
					conversacionActual.addMensaje(mensaje);
					
				} else {
					conversacionActual = new Conversacion(ko, data.remitente, self);
					var mensaje = new Mensaje(remi, data.message.message, data.message.time);
					conversacionActual.addMensaje(mensaje);
					self.conversaciones.push(conversacionActual);
				}
				self.ponerVisible(data.remitente);
			} 
			else if (data.type == "HISTO") {
				//console.log(data.enviador);
			//	console.log(data.destinatario);
				self.verHistorial2(data.enviador,data.destinatario);
			}
			
			
		}
	}
	
	close() {
		this.chat.close();
	}
	
	enviar(mensaje) {
		//console.log("enviar(mensaje)");
		//console.log(mensaje);
		this.chat.send(JSON.stringify(mensaje));
        document.getElementById("campoMensajePrivado").value = "";
		
	}
	
	enviarATodos() {
		var mensaje = {
			type : "BROADCAST",
			message : this.mensajeQueVoyAEnviar()
		};
		this.chat.send(JSON.stringify(mensaje));
		document.getElementById("campoMensaje").value = "";
	}
	
	buscarConversacion(nombreInterlocutor) {
		for (var i=0; i<this.conversaciones().length; i++) {
			//console.log(this.conversaciones()[i].nombreInterlocutor);
			if (this.conversaciones()[i].nombreInterlocutor==nombreInterlocutor)
				return this.conversaciones()[i];
		}
		return null;
	}
		buscarConversacionHistorial(enviador, solicitante) {
		for (var i=0; i<this.conversacionesHistorial().length; i++) {
			//console.log(this.conversaciones()[i].nombreInterlocutor);
			if (this.conversacionesHistorial()[i].enviador==enviador && this.conversacionesHistorial()[i].remitente==solicitante ){
				return this.conversacionesHistorial()[i];
			}
				
				
		}
		console.log("retorno nulo porque no tengo nada de momenot");
		return null;
	}
	
	setDestinatario(interlocutor) {
		this.destinatario(interlocutor);
		var conversacion = this.buscarConversacion(interlocutor.nombre);
		if (conversacion==null) {
			conversacion = new Conversacion(this.ko, interlocutor.nombre, this);
			this.conversaciones.push(conversacion);
		}
		this.ponerVisible(interlocutor.nombre);
	}
	
	verHistorial(inter){
		console.log("Inter nombre es:");
		console.log(inter.nombre)
		var mensaje = {
			type : "HISTO",
			destinatario: inter.nombre,
		};
		this.chat.send(JSON.stringify(mensaje));
		
	}
	
	verHistorial2(env, dest){
		self=this;
			var info = {
				env : env,
				dest : dest
			};
		
			$.ajax(
						{
				data : JSON.stringify(info),
				url : "users/conversaciones",
				type : "post",
				contentType : 'application/json',
				success : function(response) {
					//console.log(response.length);
					self.histo=response;
				   	self.escribe(self.histo, env, dest);
					
					
				},
				error : function(response) {
					console.log("Error: " + response.responseJSON.error);
				}
			}
			);

		
			
			
			
		}
		escribe(histo, e,d){
			
			
				var conversacionActual = self.buscarConversacionHistorial(e,d);
				if (conversacionActual!=null) {
					//conversacionActual.clearData();
					console.log("ya tenía "+ conversacionActual.mensajesHis().length)
					for (var i=conversacionActual.mensajesHis().length; i<histo.length; i++) {
						var sender=histo[i].sender;
						if(sender==e){
							sender="Yo"
						}
						var mensaje = new Mensaje(sender, histo[i].message, histo[i].date);
						conversacionActual.addMensaje(mensaje);
					}
				} else {
					conversacionActual = new ConversacionHistorial(this.ko, e, d, self);
					for (var i=0; i<histo.length; i++) {
						var sender=histo[i].sender;
						if(sender==e){
							sender="Yo"
						}
						var mensaje = new Mensaje(sender, histo[i].message, histo[i].date);
						//console.log(mensaje);
						conversacionActual.addMensaje(mensaje);
					}
					self.conversacionesHistorial.push(conversacionActual);
				}
				self.ponerVisibleHistorial(e,d);
			
			
			
		}
	ponerVisible(nombreInterlocutor) {
		for (var i=0; i<this.conversaciones().length; i++) {
			var conversacion = this.conversaciones()[i];
			conversacion.visible(conversacion.nombreInterlocutor == nombreInterlocutor);
			
		}
	}
	ponerVisibleHistorial(e,d) {
		console.log("lo pongo visible ")
		
		for (var i=0; i<this.conversacionesHistorial().length; i++) {
				var conversacion = this.conversacionesHistorial()[i];
			//console.log(this.conversaciones()[i].nombreInterlocutor);
	
			console.log(conversacion.enviador==e && conversacion.remitente==d);
			console.log("tamanio de");
			console.log(conversacion.mensajesHis.length);
			conversacion.visibleHis(conversacion.enviador==e && conversacion.remitente==d);
		}
		
	}
	
	addUsuario(userName, picture) {
		this.usuarios.push(new Usuario(userName, picture));
	}
}
	