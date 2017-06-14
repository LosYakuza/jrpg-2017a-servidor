package comandos;

import java.io.IOException;

import mensajeria.PaqueteAtacar;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class Atacar extends ComandoServer {

	@Override
	public void ejecutar() {
		PaqueteAtacar paqueteAtacar = (PaqueteAtacar) paquete;
		for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
			if(conectado.getIdPersonaje() == paqueteAtacar.getIdEnemigo()) {
				try {
					conectado.getSalida().writeObject(paqueteAtacar.getJson());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
