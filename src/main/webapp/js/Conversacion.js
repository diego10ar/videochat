class Conversacion {
	constructor(ko, interlocutor) {
		this.interlocutor = interlocutor;
		this.mensajes = ko.observableArray([]); // = new Array();
	}
	
	addMensaje(mensaje) {
		this.mensajes.push(mensaje);
	}
}