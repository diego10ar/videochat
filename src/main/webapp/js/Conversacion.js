class Conversacion {
	constructor(ko, nombreInterlocutor, ws, videoLlamada) {
		this.nombreInterlocutor = nombreInterlocutor;
		this.mensajes = ko.observableArray([]);
		this.textoAEnviar = ko.observable("");
		this.ws = ws;
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
		this.videoLlamada.ws.send(JSON.stringify(mensaje));
	}
	
	enviar() {
		var mensaje = {
			type : "PARTICULAR",
			destinatario : this.nombreInterlocutor,
			texto : this.textoAEnviar()
		};
		this.ws.send(JSON.stringify(mensaje));
		var mensaje = new Mensaje(this.textoAEnviar());
		this.addMensaje(mensaje);
	}
}