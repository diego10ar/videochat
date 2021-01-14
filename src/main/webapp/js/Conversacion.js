class Conversacion {
	constructor(ko, nombreInterlocutor, chat) {
		this.nombreInterlocutor = nombreInterlocutor;
		this.mensajes = ko.observableArray([]);
		this.textoAEnviar = ko.observable("");
		this.chat = chat;
		this.visible = ko.observable(true);
	}
	
	addMensaje(mensaje) {
		this.mensajes.push(mensaje);
	}
	
	enviar() {
		var d = new Date();
var de = d.getDate()+"-"+ d.getMonth()+1+"-"+d.getFullYear()+", "+d.getHours()+"-"+ d.getMinutes()+"-"+d.getSeconds();
		var mensaje = {
			type : "PARTICULAR",
			destinatario : this.nombreInterlocutor,
			texto : this.textoAEnviar()
		};
		this.chat.enviar(mensaje);
		var mensaje = new Mensaje("YO", this.textoAEnviar(),de);
		this.addMensaje(mensaje);
	}
}