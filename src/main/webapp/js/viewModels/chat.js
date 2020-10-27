/**
 * @license
 * Copyright (c) 2014, 2020, Oracle and/or its affiliates.
 * Licensed under The Universal Permissive License (UPL), Version 1.0
 * as shown at https://oss.oracle.com/licenses/upl/
 * @ignore
 */
/*
 * Your incidents ViewModel code goes here
 */
define(['knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils'],
		function(ko, app, moduleUtils, accUtils) {

	function ChatViewModel() {
		var self = this;
		self.estado = ko.observable("No conectado");
		self.error = ko.observable("");

		self.mensajesRecibidos = ko.observableArray([]);
		self.mensajeQueVoyAEnviar = ko.observable("");

		self.usuarios = ko.observableArray([]);

		self.chat = null;

		// Header Config
		self.headerConfig = ko.observable({'view':[], 'viewModel':null});
		moduleUtils.createView({'viewPath':'views/header.html'}).then(function(view) {
			self.headerConfig({'view':view, 'viewModel': app.getHeaderModel()})
		})

		self.connected = function() {
			accUtils.announce('Chat page loaded.');
			document.title = "Chat";

			getUsuariosConectados();
			
			self.startCamera();

			self.chat = new WebSocket("wss://" + window.location.host + "/wsGenerico");
			self.chat.onopen = function() {
				self.estado("Conectado al servidor");
			}

			self.chat.onmessage = function(event) {
				var mensaje = JSON.parse(event.data);
				if (mensaje.type == "FOR ALL") {
					self.mensajesRecibidos.push(mensaje.message);
				} else if (mensaje.type == "ARRIVAL") {
					var userName = mensaje.user;
					self.usuarios.push(userName);
				} else if (mensaje.type == "BYE") {
					var userName = mensaje.user;
					for (var i=0; i<self.usuarios().length; i++) {
						if (self.usuarios()[i] == userName) {
							self.usuarios.splice(i, 1);
							break;
						}
					}
				} 
			}

			self.chat.onclose = function() {
				self.estado("WebSocket cerrado");
			}
		};
		
		self.enviarATodos = function() {
			var mensaje = {
				type : "BROADCAST",
				message : self.mensajeQueVoyAEnviar()
			};
			self.chat.send(JSON.stringify(mensaje));
		}

		function getUsuariosConectados() {
			var data = {	
					url : "getUsuariosConectados",
					type : "get",
					contentType : 'application/json',
					success : function(response) {
						for (var i=0; i<response.length; i++)
							self.usuarios.push(response[i]);
					},
					error : function(response) {
						self.error(response.responseJSON.error);
					}
			};
			$.ajax(data);
		}
		
		self.startCamera = function() {
			if (navigator.mediaDevices) {
				const constraints = {
					audio: false,
					video: {
						width: 100, height: 100
					}
				};		
		
				(async function() {
					const stream = await navigator.mediaDevices.getUserMedia(constraints);
					self.handleStream(stream);
				})();
			}
		}
		
		self.handleStream = function(stream) {
			window.stream = stream;
			var video = document.getElementById("video");
			video.srcObject = stream;		
		}


		self.disconnected = function() {
			self.chat.close();
		};

		self.transitionCompleted = function() {
			// Implement if needed
		};
	}

	return ChatViewModel;
}
);
