class Conversacion {
	constructor(ko, nombreInterlocutor, chat, videoLlamada) {
		this.nombreInterlocutor = nombreInterlocutor;
		this.mensajes = ko.observableArray([]);
		this.textoAEnviar = ko.observable("");
		this.chat = chat;
		this.videoLlamada = videoLlamada;
		this.visible = ko.observable(true);
	}
	
	addMensaje(mensaje) {
		this.mensajes.push(mensaje);
	}
	
	solicitarVideo() {
		var mensaje = {
			type : "SOLICITUD_VIDEO",
			destinatario : this.nombreInterlocutor
		};
		this.videoLlamada.isInitiator = true; 
		this.videoLlamada.chat.send(JSON.stringify(mensaje));
	}
	
	enviar() {
		var mensaje = {
			type : "PARTICULAR",
			destinatario : this.nombreInterlocutor,
			texto : this.textoAEnviar()
		};
		this.chat.enviar(mensaje);
		var mensaje = new Mensaje(this.textoAEnviar());
		this.addMensaje(mensaje);
	}
}