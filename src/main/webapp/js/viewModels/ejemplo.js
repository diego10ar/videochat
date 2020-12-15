define(['knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils'],
		function(ko, app, moduleUtils, accUtils) {

	function EjemploViewModel() {
		var self = this;
		
		self.ws = new WebSocket("wss://" + window.location.host + "/wsSignaling");
		
		self.sala = ko.observable("Sala A");
		self.mensajesRecibidos = ko.observableArray([]);
		self.estado = ko.observable("No conectado");
		self.error = ko.observable("");
		
		self.isInitiator = false;
		self.started = false;
		self.channelReady = false;
		
		self.connection = null;
		self.localStream = null;
		self.remoteStream = null;
		self.sendChannel = null;
		
		this.ws.onopen = function() {
			self.estado("Conectado al servidor de signaling");
		}
		
		this.ws.onclose = function() {
			self.estado("Desconectado del servidor de signaling");
		}

		this.ws.onmessage = function(event) {
			var data = JSON.parse(event.data);
			if (data.type=="SALA_CREADA") {
				self.isInitiator = true;
				let constraints = {
					video : true,
					audio : false
				};
				window.navigator.getUserMedia(constraints, videoLocalConectado, errorConVideoLocal);
				self.checkAndStart();
				return;
			}
			if (data.type=="VIDEO_LOCAL_CONECTADO") {
				self.checkAndStart();
				return;
			}
			if (data.type=="SALA_COMPLETA") {
				self.channelReady = true;
				let constraints = {
					video : true,
					audio : false
				};
				if (!self.isInitiator)
					window.navigator.getUserMedia(constraints, videoLocalConectado, errorConVideoLocal);
				return;
			}
			if (data.type=="CANDIDATE" && self.started) {
				let candidate = new RTCIceCandidate(
					{ 
						sdpMLineIndex : data.label,
						candidate : data.candidate
					}
				);
				self.connection.addIceCandidate(candidate);
				return;
			}
			if (data.type=="OFFER" || data.type=="offer") {
				if (!self.isInititator && self.started)
					self.checkAndStart();
				self.connection.setRemoteDescription(new RTCSessionDescription(data));
				self.responder();
				return;
			}
			if ((data.type=="ANSWER" || data.type=="answer") && self.started) {
				self.connection.setRemoteDescription(new RTCSessionDescription(data.sdp));
				return;
			}
		}
		
		this.checkAndStart = function() {
			if (!this.started && typeof this.localStream!='undefined' && self.channelReady) {
				this.crearConexion();
				this.started = true;
				if (this.isInitiator)
					this.llamar();
			}
		}
		
		this.llamar = function() {
			let sdpConstraints = {};
			this.connection.createOffer(setLocalAndSendMessage, onSignalingError, sdpConstraints);
		}
		
		this.responder = function() {
			let sdpConstraints = {};
			this.connection.createAnswer(setLocalAndSendMessage, onSignalingError, sdpConstraints);
		}
		
		function setLocalAndSendMessage(sessionDescription) {
			self.connection.setLocalDescription(sessionDescription);
			let msg = {
				type : "SESSION_DESCRIPTION",
				sala : self.sala(),
				sessionDescription : sessionDescription
			};
			self.ws.send(JSON.stringify(msg)); 
		}
		
		function onSignalingError(error) {
			self.mensajesRecibidos.push("Error en createOffer");
		} 
		
		this.crearConexion = function() {
			var self = this;
			if (navigator.webkitGetUserMedia) { // Para Chrome
				RTCPeerConnection = webkitRTCPeerConnection;
			} else if (navigator.mozGetUserMedia) { 	// Para Firefox
				RTCPeerConnection = mozRTCPeerConnection;
				RTCSessionDescription = mozRTCSessionDescription;
				RTCIceCandidate = mozRTCIceCandidate;
			}
			
			var servers;
			if (navigator.vendor.indexOf("Google")!=-1) {
				servers = { 
					iceServers : [ { "url" : "stun:stun.1.google.com:19302" } ]
				};
			};
			var constraints = {
				optional : [
					{ DtlsSrtpKeyAgreement : true}
				]
			};
			
			self.connection = new RTCPeerConnection(servers, constraints);
			self.connection.addStream(self.localStream);
			
			self.connection.onicecandidate = function(event) {
				if (event.candidate) {
					let msg = {
						type : "CANDIDATE",
						label : event.candidate.sdpMLineIndex,
						id : event.candidate.sdpMid,
						candidate : event.candidate.candidate,
						sala : self.sala()
					};
					self.ws.send(JSON.stringify(msg));
				}
			}
			
			self.connection.onaddstream = videoRemotoConectado;
			self.connection.onremovestream = videoRemotoDesconectado;
			
			if (self.isInitiator) {
				self.sendChannel = self.connection.createDataChannel("sendDataChannel", { reliable : true });
				self.sendChannel.onopen = function(event) {
					self.mensajesRecibidos.push("sendDataChannel abierto");
				}
				self.sendChannel.onmessage = function(event) {
					self.mensajesRecibidos.push(event.data);
				}
				self.sendChannel.onclose = function(event) {
					self.mensajesRecibidos.push("sendDataChannel cerrado");
				}
			} else {
				self.connection.ondatachannel = function(event) {
					self.receiveChannel = event.channel;
					self.receiveChannel.onopen = function(event2) {
						self.mensajesRecibidos.push("receiveChannel abierto");
					}
					self.receiveChannel.onmessage = function(event2) {
						self.mensajesRecibidos.push(event.data);
					}
					self.receiveChannel.onclose = function(event2) {
						self.mensajesRecibidos.push("receiveChannel cerrado");
					}
				}
			}
		}
		
		function videoRemotoConectado(event) {
			let widgetVideoRemoto = document.getElementById("widgetVideoRemoto");
			self.remoteStream = event.stream;
			widgetVideoRemoto.srcObject = event.stream;
			self.mensajesRecibidos.push("Vídeo remoto conectado");
		}
		
		function videoRemotoDesconectado() {
			self.mensajesRecibidos.push("Vídeo remoto desconectado");
		}
		
		function videoLocalConectado(stream) {
			let widgetVideoLocal = document.getElementById("widgetVideoLocal");
			self.localStream = stream;
			widgetVideoLocal.srcObject = stream;
			let msg = {
				type : "VIDEO_LOCAL_CONECTADO",
				sala : self.sala()
			};
			self.ws.send(JSON.stringify(msg));
			self.mensajesRecibidos.push("Vídeo local conectado");
		}
		
		function errorConVideoLocal(error) {
			self.mensajesRecibidos.push("Error con vídeo local: " + error);
		}
		
		// Header Config
		self.headerConfig = ko.observable({'view':[], 'viewModel':null});
		moduleUtils.createView({'viewPath':'views/header.html'}).then(function(view) {
			self.headerConfig({'view':view, 'viewModel': app.getHeaderModel()})
		});
		
		self.conectarASala = function() {
			var msg = {
				type : "CONECTAR_A_SALA",
				sala : self.sala()
			};
			this.ws.send(JSON.stringify(msg));
		}

		self.connected = function() {
			accUtils.announce('Chat page loaded.');
			document.title = "Chat";
		};
		
		self.disconnected = function() {

		};

		self.transitionCompleted = function() {
			// Implement if needed
		};
	}

	return EjemploViewModel;
}
);
