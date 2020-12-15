class VideoLlamada {
	constructor(viewModel) {
		this.viewModel = viewModel;
		this.isInitiator = false;
		this.channelReady =false;
		this.started = false;
		this.connection = null;
		this.sendChannel = null;
		this.receiveChannel = null;
		this.localStream = null;
		this.remoteStream = null;
		
		var self = this;
		
		this.ws = new WebSocket("wss://" + window.location.host + "/wsSignaling");
		
		this.ws.onopen = function() {
			if (this.viewModel)
				viewModel.estado("Conectado al servidor de signaling");
			console.log("Conectado al servidor de signaling");
		}
		
		this.ws.onclose = function() {
			if (this.viewModel)
				viewModel.estado("Desconectado del servidor de signaling");
			console.log("Desconectado del servidor de signaling");
		}
		
		this.ws.onmessage = function(event) {
			var data = JSON.parse(event.data);
			if (data.type == "SOLICITUD_VIDEO") {
				self.isInitiator = false;
				var remitente = data.remitente;
				var respuesta = window.confirm(remitente + " quiere hacer una videoconferencia contigo. ¿Estás de acuerdo?");
				if (respuesta) {
					var mensaje = {
						type : "ACEPTACION",
						destinatario : remitente
					};
					self.ws.send(JSON.stringify(mensaje));
				} else {
					var mensaje = {
						type : "RECHAZO",
						destinatario : remitente
					};
					self.ws.send(JSON.stringify(mensaje));
				}
				return;
			}
			if (data.type == "SOLICITUD_ENVIADA") {
				self.encenderCamaraLocal();
				self.checkAndStart();
				return;
			}
			if (data.type == "VIDEO_LOCAL_CONECTADO") {
				self.checkAndStart();
				return;
			}
			if (data.type == "OFFER") {
				if (!self.isInitiator && !self.started)
					self.checkAndStart();
				self.connection.setRemoteDescription(new RTCSessionDescription(data.message));
				self.responder();
			}
		}
	}
	
	encenderCamaraLocal() {
		var self = this;
		var caracteristicas = {
			audio: false, 
			video: { width: 300, height: 300 },
		};
		
		navigator.mediaDevices.getUserMedia(caracteristicas).
			then(function(stream) {
				var widgetVideoLocal = document.getElementById("widgetVideoLocal");
				self.localStream = stream;
				widgetVideoLocal.srcObject = stream;
				var msg = {
					type : "VIDEO_LOCAL_CONECTADO"
				};
				self.ws.send(JSON.stringify(msg));
			}, function(error) {
				console.log("Error con vídeo local: " + error);
			});
	}
	
	checkAndStart() {
		if (!this.started && typeof this.localStream!='undefined' && this.channelReady) {
			this.crearConexion();
			this.started = true;
			if (this.isInitiator)
				this.llamar();
		}
	}
	
	crearConexion() {	
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
				var msg = {
					type : "CANDIDATE",
					label : event.candidate.sdpMLineIndex,
					id : event.candidate.sdpMid,
					candidate : event.candidate.candidate
				};
				self.ws.send(JSON.stringify(msg));
			}
		}
		
		self.connection.onaddstream = function(event) {
			var widgetVideoRemoto = document.getElementById("widgetVideoRemoto");
			self.remoteStream = event.stream;
			widgetVideoRemoto.srcObject = stream;
		}
		
		self.connection.onremovestream = function(event) {
			console.log("Conexión finalizada");
		}
		
		if (self.isInitiator) {
			self.sendChannel = self.connection.createDataChannel("sendChannel", { reliable : true} );
			self.sendChannel.onopen = function(event) {
				console.log("sendChannel abierto");
			}
			self.sendChannel.onmessage = function(event) {
				console.log("Mensaje recibido por el sendChannel ???");
			}
			self.sendChannel.onclose = function(event) {
				console.log("sendChannel cerrado");
			}
		} else {
			self.connection.ondatachannel = function(event) {
				self.receiveChannel = event.channel;
				self.receiveChannel.onopen = function(event) {
					console.log("receiveChannel abierto");
				}
				self.receiveChannel.onmessage = function(event) {
					console.log("Mensaje recibido por el receiveChannel");
				}
				self.receiveChannel.onclose = function(event) {
					console.log("receiveChannel cerrado");
				}
			}
		}
		
		self.llamar();
	}
	
	llamar() {
		var self = this;
		let sdpConstraints = {};
		this.connection.createOffer(
			function(sessionDescription) {
				self.connection.setLocalDescription(sessionDescription);
			}, 
			function(error) {
				console.log("Error en createOffer: " + error);
			}, 
			sdpConstraints
		);
	} 
	
	responder() {
		var self = this;
		let sdpConstraints = {};
		self.connection.createAnswer(
			function(sessionDescription) {
				self.connection.setLocalDescription(sessionDescription);
				let msg = {
					type : "SESSION_DESCRIPTION",
					sessionDescription : sessionDescription
				};
				self.ws.send(JSON.stringify(msg));
			},
			onSignalingError, 
			sdpConstraints
		);
	}
	
}