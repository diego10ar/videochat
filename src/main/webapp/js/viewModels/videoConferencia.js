define(['knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils'],
		function(ko, app, moduleUtils, accUtils) {

	function VideoConferenciaViewModel() {
		var self = this;

		this.ws = new WebSocket("wss://" + window.location.host + "/wsSignaling2");
		prepararWebSocket();
		
		this.mensajes = ko.observableArray([]);
		this.error = ko.observable("");

		this.headerConfig = ko.observable({'view':[], 'viewModel':null});
		moduleUtils.createView({'viewPath':'views/header.html'}).then(function(view) {
			self.headerConfig({'view':view, 'viewModel': app.getHeaderModel()})
		});
		
		this.connected = function() {
			accUtils.announce('Chat page loaded.');
			document.title = "Chat";
		};
		
		function prepararWebSocket() {
			self.ws.onopen = function() {
				self.addMensaje("Conectado al servidor de signaling", "green");
			}
			
			self.ws.onclose = function() {
				self.error("Desconectado del servidor de signaling");
				self.addMensaje("Desconectado del servidor de signaling", "red");
			}
	
			self.ws.onmessage = function(event) {
				var data = JSON.parse(event.data);
				if (data.type=="OFFER") {
					self.anunciarLlamada(data.remitente, data.sessionDescription);
					return;
				}
				if (data.type=="CANDIDATE" && data.candidate) {
					self.addMensaje("Recibido candidato desde Signaling", "blue");
					try {
						self.conexion.addIceCandidate(data.candidate);
					} catch (error) {
						self.addMensaje(error, "red");
					}
					return;
				}
				if (data.type=="ANSWER") {
					let sessionDescription = data.sessionDescription;
					let rtcSessionDescription = new RTCSessionDescription(sessionDescription);
					self.addMensaje("Añadiendo sessionDescription a la remoteDescription", "orange");
					self.conexion.setRemoteDescription(rtcSessionDescription);
					self.addMensaje("sessionDescription añadida a la remoteDescription", "orange");
					return;
				}
			}
		}
		
		this.anunciarLlamada  = function(remitente, sessionDescription) {
			this.addMensaje("Se recibe llamada de " + remitente + " con su sessionDescription", "black");
			let aceptar = window.confirm("Te llama " + remitente + ". ¿Contestar?\n");
			if (aceptar)
				this.aceptarLlamada(remitente, sessionDescription);
			else
				this.rechazarLlamada(remitente, sessionDescription);			
		}
		
		this.aceptarLlamada = function(remitente, sessionDescription) {
			if (!self.localStream)
				self.encenderVideoLocal();
			
			if (!self.conexion)
				self.crearConexion();
			
			let rtcSessionDescription = new RTCSessionDescription(sessionDescription);
			self.addMensaje("Añadiendo sessionDescription a la remoteDescription", "grey");
			self.conexion.setRemoteDescription(rtcSessionDescription);
			self.addMensaje("sessionDescription añadida a la remoteDescription", "grey");
						
			this.addMensaje("Llamada aceptada", "black");
			this.addMensaje("Creando respuesta mediante el servidor Stun");
			// Asumimos que se acepta
			let sdpConstraints = {};
			this.conexion.createAnswer(
				function(sessionDescription) {
					self.addMensaje("sessionDescription recibida del servidor stun");
					self.conexion.setLocalDescription(sessionDescription).then(
						function() {
							self.addMensaje("sessionDescription enlazada a la RTCPeerConnnection local");
							self.addMensaje("Enviando aceptación al servidor de Signaling");
							let msg = {
								type : "ANSWER",
								sessionDescription : sessionDescription
							};
							self.ws.send(JSON.stringify(msg));
							self.addMensaje("Respuesta enviada al servidor de Signaling");
						}
					);
				},
				function(error) {
					self.addMensaje("Error al crear oferta en el servidor Stun: " + error, red);
				},
				sdpConstraints
			);
		}
		
		this.rechazarLlamada = function(remitente, sessionDescription) {
			this.addMensaje("Llamada de " + remitente + " rechazada");
			this.addMensaje("Implementar función rechazarLlamada", "red");
		}
		
		this.encenderVideoLocal = function() {
			let constraints = {
				video : true,
				audio : false
			};
			navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
			navigator.getUserMedia(
				constraints, 
				function(stream) {
					let widgetVideoLocal = document.getElementById("widgetVideoLocal");
					self.localStream = stream;
					widgetVideoLocal.srcObject = stream;
					self.addMensaje("Vídeo local conectado", "green");
				}, 
				function(error) {
					self.addMensaje("Error al cargar vídeo local: " + error, "red");
				}
			);
		}
		
		this.crearConexion = async function() {
			let servers = { 
				iceServers : [ 
					//{ "url" : "stun:stun.1.google.com:19302" }
					{ 
						urls : "turn:localhost",
						username : "webrtc",
						credential : "turnserver"
					}
				]
			};
			self.conexion = new RTCPeerConnection(servers);
			self.addMensaje("RTCPeerConnection creada");
			
			self.addMensaje("Asociando pistas locales a la RTCPeerConnection");
			let localTracks = self.localStream.getTracks();
			localTracks.forEach(track =>
				{
					self.conexion.addTrack(track, self.localStream);
				}
			);
			
			self.conexion.onicecandidate = function(event) {
				if (event.candidate) {
					self.addMensaje("self.conexion.onicecandidate (<i>recibido candidate desde el Stun</i>)");
					let msg = {
						type : "CANDIDATE",
						candidate : event.candidate
					};
					self.ws.send(JSON.stringify(msg));
					self.addMensaje("Candidate enviado al servidor de Signaling");
				}  else {
					self.addMensaje("Recibidos todos los candidates desde el Stun");
				}
			}
			
			self.conexion.oniceconnectionstatechange = function(event) {
				self.addMensaje("self.conexion.oniceconnectionstatechange: " + self.conexion.iceConnectionState, "DeepPink");
			}
				
			self.conexion.onicegatheringstatechange = function(event) {
				self.addMensaje("self.conexion.onicegatheringstatechange: " + self.conexion.iceGatheringState, "DeepPink");
			}
			
			self.conexion.onsignalingstatechange = function(event) {
				self.addMensaje("self.conexion.onsignalingstatechange: " + self.conexion.signalingState, "DeepPink");
			}
		
			self.conexion.onnegotiationneeded = async function(event) {
				self.addMensaje("Negociación finalizada: self.conexion.onnegotiationneeded", "black");
				self.addMensaje("Listo para enviar oferta", "black");
			}
				
			self.conexion.ontrack = function(event) {
				self.addMensaje("Asociando pistas remotas a la RTCPeerConnection");
				let widgetVideoRemoto = document.getElementById("widgetVideoRemoto");
				widgetVideoRemoto.srcObject = event.streams[0];
				self.addMensaje("Vídeo remoto conectado");
			}
			
			self.conexion.onremovetrack = function(event) {
				self.addMensaje("self.conexion.onremovetrack");
			}
		}	
		
		this.enviarOferta  = function() {
			let sdpConstraints = {};
			self.addMensaje("Creando oferta en el servidor Stun");
			this.conexion.createOffer(
				function(sessionDescription) {
					self.addMensaje("sessionDescription recibida del servidor Stun");
					self.conexion.setLocalDescription(sessionDescription);
					self.addMensaje("sessionDescription enlazada a la RTCPeerConnnection local");
					let recipient = "ana";
					self.addMensaje("Enviando oferta a " + recipient + " mediante el servidor de Signaling");
					let msg = {
						type : "OFFER",
						sessionDescription : sessionDescription,
						recipient : recipient
					};
					self.ws.send(JSON.stringify(msg));
					self.addMensaje("Oferta enviada al servidor de signaling");
				},
				function(error) {
					self.addMensaje("Error al crear oferta en el servidor Stun", true);
				},
				sdpConstraints
			);
		}
		
		this.addMensaje = function(texto, color) {
			let mensaje = {
				texto : texto,
				color : color ? color : "blue"
			};
			this.mensajes.push(mensaje);
		}
		
		this.debug = function() {
			console.log("En debug");
		}

		this.disconnected = function() {
		};

		this.transitionCompleted = function() {
		};
	}

	return VideoConferenciaViewModel;
}
);
