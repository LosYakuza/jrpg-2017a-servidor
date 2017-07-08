package comandos;

import java.io.IOException;

import mensajeria.PaqueteOfertaMercado;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class OfertaMercado extends ComandoServer  {

	@Override
	public void ejecutar() {
		PaqueteOfertaMercado paqueteOfertaMercado = (PaqueteOfertaMercado) paquete;
		Servidor.getConector().enviarNuevaOferta(paqueteOfertaMercado);
	}

}
