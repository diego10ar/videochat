class Mensaje {
	constructor(nombreEnviador, texto, hora) {
		this.nombreEnviador = nombreEnviador;
		this.texto = texto;
		this.hora = hora ? hora : Date.now();
	}
}