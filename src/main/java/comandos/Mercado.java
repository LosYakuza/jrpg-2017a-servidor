package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.PaqueteMercado;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class Mercado extends ComandoServer {

	@Override
	public void ejecutar() {
		PaqueteMercado paqueteMercado = (PaqueteMercado) paquete;
		//seteo estado de mercado
		paqueteMercado = Servidor.getConector().getOfertas(paqueteMercado);
		Servidor.getPersonajesConectados().get(paqueteMercado.getId()).setEstado(Estado.estadoMercado);
		
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
