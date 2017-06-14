package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.PaqueteFinalizarBatalla;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class FinalizarBatalla extends ComandoServer {

	@Override
	public void ejecutar() {
		PaqueteFinalizarBatalla paqueteFinalizarBatalla = (PaqueteFinalizarBatalla) paquete;
		Servidor.getPersonajesConectados().get(paqueteFinalizarBatalla.getId()).setEstado(Estado.estadoJuego);
		Servidor.getPersonajesConectados().get(paqueteFinalizarBatalla.getIdEnemigo()).setEstado(Estado.estadoJuego);
		for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
			if(conectado.getIdPersonaje() == paqueteFinalizarBatalla.getIdEnemigo()) {
				try {
					conectado.getSalida().writeObject(paqueteFinalizarBatalla.getJson());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		synchronized(Servidor.atencionConexiones){
			Servidor.atencionConexiones.notify();
		}

	}

}
