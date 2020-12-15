define(['knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils'],
		function(ko, app, moduleUtils, accUtils) {

	function ChatViewModel() {
		var self = this;
		
		this.user = app.user;
		
		self.estado = ko.observable("No conectado");
		self.error = ko.observable("");

		self.recipient = ko.observable("");

		self.chat = ko.observable(new Chat(ko));
		
		// Header Config
		self.headerConfig = ko.observable({'view':[], 'viewModel':null});
		moduleUtils.createView({'viewPath':'views/header.html'}).then(function(view) {
			self.headerConfig({'view':view, 'viewModel': app.getHeaderModel()})
		})

		self.connected = function() {
			accUtils.announce('Chat page loaded.');
			document.title = "Chat";

			getUsuariosConectados();			
		};

		self.setRecipient = function(interlocutor) {
			self.recipient(interlocutor);
			var conversacion = buscarConversacion(interlocutor.nombre);
			if (conversacion==null) {
				conversacion = new Conversacion(ko, interlocutor.nombre, self.chat, self.videoLlamada);
				self.conversaciones.push(conversacion);
			}
			ponerVisible(interlocutor.nombre);
		}

		function ponerVisible(nombreInterlocutor) {
			for (var i=0; i<self.conversaciones().length; i++) {
				var conversacion = self.conversaciones()[i];
				conversacion.visible(conversacion.nombreInterlocutor == nombreInterlocutor);
			}
		}

		function getUsuariosConectados() {
			var data = {	
				url : "users/getUsuariosConectados",
				type : "get",
				contentType : 'application/json',
				success : function(response) {
					for (var i=0; i<response.length; i++) {
						var userName = response[i].name;
						var picture = response[i].picture;
						self.chat().addUsuario(userName, picture);
					}
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
