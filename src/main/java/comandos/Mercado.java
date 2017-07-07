package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.PaqueteMercado;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class Mercado extends ComandoServer {

	@Override
	public void ejecutar() {
		// Le reenvio al id del personaje con el que se quieren intercambiar ítems
		PaqueteMercado paqueteMercado = (PaqueteMercado) paquete;
		//Servidor.log.append(paqueteMercado.getId() + " quiere intercambiar objetos con " + paqueteMercado.getIdEnemigo() + System.lineSeparator());
		
		//seteo estado de mercado
		Servidor.getPersonajesConectados().get(paqueteMercado.getId()).setEstado(Estado.estadoMercado);
		//paqueteMercado.setMiTurno(true);
		try {
			context.getSalida().writeObject(paqueteMercado.getJson());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		synchronized(Servidor.atencionConexiones){
			Servidor.atencionConexiones.notify();
		}

	}

}
