package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.Servidor;

public class CreacionPJ extends ComandoServer {

	@Override
	public void ejecutar() {
		
		// Guardo el personaje en ese usuario
		Servidor.getConector().registrarPersonaje((PaquetePersonaje) this.paquete, 
				context.getPaqueteUsuario());
		
		// Le envio el id del personaje
		try {
			context.getSalida().writeObject(((PaquetePersonaje) this.paquete).getJson());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
