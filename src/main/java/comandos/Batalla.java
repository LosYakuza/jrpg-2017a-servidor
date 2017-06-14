package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.PaqueteBatalla;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class Batalla extends ComandoServer {

	@Override
	public void ejecutar() {
		// Le reenvio al id del personaje batallado que quieren pelear
		PaqueteBatalla paqueteBatalla = (PaqueteBatalla) paquete;
		Servidor.log.append(paqueteBatalla.getId() + " quiere batallar con " + paqueteBatalla.getIdEnemigo() + System.lineSeparator());
		
		//seteo estado de batalla
		Servidor.getPersonajesConectados().get(paqueteBatalla.getId()).setEstado(Estado.estadoBatalla);
		Servidor.getPersonajesConectados().get(paqueteBatalla.getIdEnemigo()).setEstado(Estado.estadoBatalla);
		paqueteBatalla.setMiTurno(true);
		try {
			context.getSalida().writeObject(paqueteBatalla.getJson());
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(EscuchaCliente conectado : Servidor.getClientesConectados()){
			if(conectado.getIdPersonaje() == paqueteBatalla.getIdEnemigo()){
				int aux = paqueteBatalla.getId();
				paqueteBatalla.setId(paqueteBatalla.getIdEnemigo());
				paqueteBatalla.setIdEnemigo(aux);
				paqueteBatalla.setMiTurno(false);
				try {
					conectado.getSalida().writeObject(paqueteBatalla.getJson());
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
		synchronized(Servidor.atencionConexiones){
			Servidor.atencionConexiones.notify();
		}

	}

}
