package comandos;

import mensajeria.PaqueteMovimiento;
import servidor.Servidor;

public class Movimiento extends ComandoServer {

	@Override
	public void ejecutar() {
		PaqueteMovimiento paqueteMovimiento = (PaqueteMovimiento)paquete;
		
		Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setPosX(paqueteMovimiento.getPosX());
		Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setPosY(paqueteMovimiento.getPosY());
		Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setDireccion(paqueteMovimiento.getDireccion());
		Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setFrame(paqueteMovimiento.getFrame());
		
		synchronized(Servidor.atencionMovimientos){
			Servidor.atencionMovimientos.notify();
		}

	}

}
