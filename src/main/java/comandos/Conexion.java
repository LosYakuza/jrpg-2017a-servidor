package comandos;

import estados.Estado;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaquetePersonaje;
import servidor.Servidor;

public class Conexion extends ComandoServer {

	@Override
	public void ejecutar() {
		PaquetePersonaje paquetePersonaje = (PaquetePersonaje) paquete;
		context.setPaquetePersonaje(paquetePersonaje);
		Servidor.getPersonajesConectados().put(paquetePersonaje.getId(), (PaquetePersonaje) paquetePersonaje);
		Servidor.getUbicacionPersonajes().put(paquetePersonaje.getId(), (PaqueteMovimiento) new PaqueteMovimiento(paquetePersonaje.getId()));
		
		synchronized(Servidor.atencionConexiones){
			Servidor.atencionConexiones.notify();
		}

	}

}
