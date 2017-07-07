package comandos;

import java.io.IOException;

import mensajeria.Comando;
import mensajeria.Paquete;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;
import servidor.Servidor;

public class InicioSesion extends ComandoServer {

	@Override
	public void ejecutar() {
		
		Paquete paqueteSv = new Paquete();
		// Recibo el paquete usuario
		PaqueteUsuario paqueteUsuario = (PaqueteUsuario) this.paquete;
		
		// Si se puede loguear el usuario le envio un mensaje de exito y el paquete personaje con los datos
		if (Servidor.loguearUsuario(paqueteUsuario)) {
			
			PaquetePersonaje paquetePersonaje = new PaquetePersonaje();
			paquetePersonaje = Servidor.getConector().getPersonaje(paqueteUsuario);
			paquetePersonaje.setComando(Comando.INICIOSESION);
			paquetePersonaje.setMensaje(Paquete.msjExito);
			context.setPaqueteUsuario(paqueteUsuario);
			context.setPaquetePersonaje(paquetePersonaje);
			paqueteSv = paquetePersonaje;
			
		} else {
			paqueteSv.setComando(Comando.INICIOSESION);
			paqueteSv.setMensaje(Paquete.msjFracaso);
		}
		
		try {
			context.getSalida().writeObject(paqueteSv.getJson());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
}
