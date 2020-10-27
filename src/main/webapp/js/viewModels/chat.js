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

		self.enviarATodos = function() {
			var mensaje = {
				type : "BROADCAST",
				message : self.mensajeQueVoyAEnviar()
			};
			self.chat.send(JSON.stringify(mensaje));
		}

		self.connected = function() {
			accUtils.announce('Chat page loaded.');
			document.title = "Chat";

			getUsuariosConectados();

			self.chat = new WebSocket("ws://localhost:7500/wsGenerico");
			self.chat.onopen = function() {
				self.estado("Conectado al servidor");
			}

			self.chat.onmessage = function(event) {
				var mensaje = JSON.parse(event.data);
				if (mensaje.type == "FOR ALL") {
					self.mensajesRecibidos.push(mensaje);
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
