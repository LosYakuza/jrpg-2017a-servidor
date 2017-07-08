package comandos;

import mensajeria.PaqueteOfertaMercado;
import servidor.Servidor;

public class IntercambioItems extends ComandoServer {

	@Override
	public void ejecutar() {
		PaqueteOfertaMercado paqueteOfertaMercado = (PaqueteOfertaMercado) paquete;
		Servidor.getConector().realizarIntercambio(paqueteOfertaMercado);
		
		// Para actualizar el inventario
		/*int id;
		id = paqueteOfertaMercado.getIdPjQueQuiereElItem();
		Servidor.getPersonajesConectados().get(id).setInventario(
				Servidor.getConector().getInventarioSegunIdPj(id));
		
		id = paqueteOfertaMercado.getOfertas().getFirst().getIdPersonaje();
		Servidor.getPersonajesConectados().get(id).setInventario(
				Servidor.getConector().getInventarioSegunIdPj(id));*/
		
	}

}
