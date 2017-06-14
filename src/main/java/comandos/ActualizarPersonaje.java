package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class ActualizarPersonaje extends ComandoServer {

	@Override
	public void ejecutar() {
		PaquetePersonaje paquetePersonaje = (PaquetePersonaje) paquete;
		Servidor.getConector().actualizarPersonaje(paquetePersonaje);

		Servidor.getPersonajesConectados().put(paquetePersonaje.getId(), paquetePersonaje);

		for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
			try {
				conectado.getSalida().writeObject(paquetePersonaje.getJson());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
