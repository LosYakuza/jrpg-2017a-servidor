package comandos;

import mensajeria.PaquetePersonaje;
import servidor.Servidor;

public class MostrarMapas extends ComandoServer {

	@Override
	public void ejecutar() {
		PaquetePersonaje paquetePersonaje = (PaquetePersonaje) paquete;
		Servidor.log.append(paquetePersonaje.getIp() + " ha elegido el mapa " + paquetePersonaje.getMapa() + System.lineSeparator());
		

	}

}
