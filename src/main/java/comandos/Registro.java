package comandos;

import mensajeria.Comando;
import mensajeria.Paquete;
import mensajeria.PaqueteUsuario;
import servidor.Servidor;

public class Registro extends ComandoServer {

	@Override
	public void ejecutar() {
		PaqueteUsuario paqueteUsuario = (PaqueteUsuario) paquete;
		// Paquete que le voy a enviar al usuario
		paqueteUsuario.setComando(Comando.REGISTRO);
		

		// Si el usuario se pudo registrar le envio un msj de exito
		if (Servidor.getConector().registrarUsuario(paqueteUsuario)) {
			paqueteUsuario.setMensaje(Paquete.msjExito);
			context.setPaqueteUsuario(paqueteUsuario);
		// Si el usuario no se pudo registrar le envio un msj de fracaso
		} else {
			paqueteUsuario.setMensaje(Paquete.msjFracaso);
		}
		try{
			context.getSalida().writeObject(paqueteUsuario.getJson());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
