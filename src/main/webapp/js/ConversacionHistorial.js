class ConversacionHistorial {
	constructor(ko, enviador, remitente, chat) {
		this.enviador = enviador;
		this.remitente = remitente;
		this.mensajesHis = ko.observableArray([]);
		this.chat = chat;
		this.visibleHis = ko.observable(true);
	}
	
	addMensaje(mensaje) {
		this.mensajesHis.push(mensaje);
	}
	
	clearData(){
		console.log("tam: "+this.mensajesHis().length)
		for(var i=0; i<this.mensajesHis().length; i++){
		 this.mensajesHis().pop();
		}
	}
	
	
}