define(['knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils'],
		function(ko, app, moduleUtils, accUtils) {

	class DeviceManagerViewModel {
		constructor() {
			this.dispositivosConectados = ko.observableArray([]);
			this.caracteristicasSoportadas = ko.observableArray([]);
			
			this.menuCaracteristicas = ko.observableArray([
				{ audio: true },
				{ video: true },
				{ audio: true, video: true },
				{ audio: true, video: { width: 1280, height: 720 } },
				{ audio: true, video : { facingMode : "user"} },
				{ audio: true, video : { facingMode : "environment"} }
			]);
			
			this.tracksDeAudio = ko.observableArray([]);
			this.tracksDeVideo = ko.observableArray([]);
			
			this.headerConfig = ko.observable({'view':[], 'viewModel' : null});
			
			var self = this;
			moduleUtils.createView({'viewPath':'views/header.html'}).
				then(function(view) {
					self.headerConfig({'view':view, 'viewModel': app.getHeaderModel()})
				});
		}

		connected() {
			accUtils.announce('DeviceManager page loaded.');
			document.title = "Gestor de dispositivos";
			
			this.cargarDispositivos();
		}
		
		cargarDispositivos() {
			var self = this;
			navigator.mediaDevices.enumerateDevices().
				then(function(devices) { 
					self.dispositivosConectados([]);
					for (var i=0; i<devices.length; i++)
						self.dispositivosConectados.push(devices[i]);
				});
			var cs = navigator.mediaDevices.getSupportedConstraints();
			for (var field in cs)
				this.caracteristicasSoportadas.push({nombre : field, valor : cs[field]});
		}
		
		buscarDispositivo(caracteristicasBuscadas) {
			var self = this;
			navigator.mediaDevices.getUserMedia(caracteristicasBuscadas).
				then(function(stream) {
					var widgetVideo = document.getElementById("widgetVideo");
					widgetVideo.srcObject = stream;
					self.inspeccionar(stream);
				});
		}
		
		inspeccionar(stream) {
			this.tracksDeAudio(stream.getAudioTracks());
			this.tracksDeVideo(stream.getVideoTracks());
		}

		disconnected() {
			// Implement if needed
		};

		transitionCompleted = function() {
			// Implement if needed
		};
	}

	return DeviceManagerViewModel;
}
);
