class ConversacionHistorial {
	constructor(ko,enviador, remitente, chat) {
		this.enviador = enviador;
		this.remitente = remitente;
		this.mensajes = ko.observableArray([]);
		this.chat = chat;
		this.visible = ko.observable(true);
	}
	
	addMensaje(mensaje) {
		this.mensajes.push(mensaje);
	}
	
	
}