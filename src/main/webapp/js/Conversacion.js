class Conversacion {
	constructor(ko, interlocutor, ws) {
		this.interlocutor = interlocutor;
		this.mensajes = ko.observableArray([]);
		this.textoAEnviar = ko.observable("");
		this.ws = ws;
		this.visible = ko.observable(true);
	}
	
	addMensaje(mensaje) {
		this.mensajes.push(mensaje);
	}
	
	enviar() {
		var mensaje = {
			type : "PARTICULAR",
			recipient : this.interlocutor,
			message : this.textoAEnviar()
		};
		this.ws.send(JSON.stringify(mensaje));
		this.addMensaje(this.textoAEnviar());
	}
}