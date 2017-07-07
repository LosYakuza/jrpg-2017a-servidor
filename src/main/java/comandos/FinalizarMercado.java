package comandos;

import estados.Estado;
import mensajeria.PaqueteFinalizarMercado;
import servidor.Servidor;

public class FinalizarMercado extends ComandoServer {

	@Override
	public void ejecutar() {
		PaqueteFinalizarMercado paqueteFinalizarMercado = (PaqueteFinalizarMercado) paquete;
		Servidor.getPersonajesConectados().get(paqueteFinalizarMercado.getId()).setEstado(Estado.estadoJuego);

		synchronized(Servidor.atencionConexiones){
			Servidor.atencionConexiones.notify();
		}

	}
}
