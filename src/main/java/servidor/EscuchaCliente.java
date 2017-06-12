package servidor;

import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;

import cliente.*;
import dominio.*;
import estados.Estado;
import mensajeria.Comando;
import mensajeria.Paquete;
import mensajeria.PaqueteAtacar;
import mensajeria.PaqueteBatalla;
import mensajeria.PaqueteDeMovimientos;
import mensajeria.PaqueteDePersonajes;
import mensajeria.PaqueteFinalizarBatalla;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

public class EscuchaCliente extends Thread {

	private final Socket socket;
	private final ObjectInputStream entrada;
	private final ObjectOutputStream salida;
	private int idPersonaje;
	
	private PaquetePersonaje paquetePersonaje;
	private PaqueteMovimiento paqueteMovimiento;
	private PaqueteBatalla paqueteBatalla;
	private PaqueteAtacar paqueteAtacar;
	private PaqueteFinalizarBatalla paqueteFinalizarBatalla;
	
	private PaqueteDeMovimientos paqueteDeMovimiento;
	private PaqueteDePersonajes paqueteDePersonajes;

	public EscuchaCliente(String ip, Socket socket, ObjectInputStream entrada, ObjectOutputStream salida) {
		this.socket = socket;
		this.entrada = entrada;
		this.salida = salida;
		paquetePersonaje = new PaquetePersonaje();
	}

	public void run() {
		try {

			Paquete paquete;
			Paquete paqueteSv = new Paquete(null, 0);
			PaqueteUsuario paqueteUsuario = new PaqueteUsuario();

			String cadenaLeida = (String) entrada.readObject();
		
			while (!((paquete = Paquete.loadJson(cadenaLeida)).getComando() == Comando.DESCONECTAR)){
								
				switch (paquete.getComando()) {
				
				case Comando.REGISTRO:
					
					// Paquete que le voy a enviar al usuario
					paqueteSv.setComando(Comando.REGISTRO);
					
					paqueteUsuario = (PaqueteUsuario)paquete;

					// Si el usuario se pudo registrar le envio un msj de exito
					if (Servidor.getConector().registrarUsuario(paqueteUsuario)) {
						paqueteSv.setMensaje(Paquete.msjExito);
						salida.writeObject(paqueteSv.getJson());
					// Si el usuario no se pudo registrar le envio un msj de fracaso
					} else {
						paqueteSv.setMensaje(Paquete.msjFracaso);
						salida.writeObject(paqueteSv.getJson());
					}
					break;

				case Comando.CREACIONPJ:
					
					// Casteo el paquete personaje
					paquetePersonaje = (PaquetePersonaje) paquete;
					
					// Guardo el personaje en ese usuario
					Servidor.getConector().registrarPersonaje(paquetePersonaje, paqueteUsuario);
					
					// Le envio el id del personaje
					salida.writeObject(paquetePersonaje.getJson());
					
					break;

				case Comando.INICIOSESION:
					paqueteSv.setComando(Comando.INICIOSESION);
					
					// Recibo el paquete usuario
					paqueteUsuario = (PaqueteUsuario) paquete;
					
					// Si se puede loguear el usuario le envio un mensaje de exito y el paquete personaje con los datos
					if (Servidor.getConector().loguearUsuario(paqueteUsuario)) {
						
						paquetePersonaje = new PaquetePersonaje();
						paquetePersonaje = Servidor.getConector().getPersonaje(paqueteUsuario);
						paquetePersonaje.setComando(Comando.INICIOSESION);
						paquetePersonaje.setMensaje(Paquete.msjExito);
						idPersonaje = paquetePersonaje.getId();
						
						salida.writeObject(paquetePersonaje.getJson());
						
					} else {
						paqueteSv.setMensaje(Paquete.msjFracaso);
						salida.writeObject(paqueteSv.getJson());
					}
					break;

				case Comando.SALIR:
					
					// Cierro todo
					entrada.close();
					salida.close();
					socket.close();
					
					// Lo elimino de los clientes conectados
					Servidor.getClientesConectados().remove(this);
					
					// Indico que se desconecto
					Servidor.log.append(paquete.getIp() + " se ha desconectado." + System.lineSeparator());
					
					return;

				case Comando.CONEXION:
					paquetePersonaje = (PaquetePersonaje) paquete;

					Servidor.getPersonajesConectados().put(paquetePersonaje.getId(), (PaquetePersonaje) paquetePersonaje.clone());
					Servidor.getUbicacionPersonajes().put(paquetePersonaje.getId(), (PaqueteMovimiento) new PaqueteMovimiento(paquetePersonaje.getId()).clone());
					
					synchronized(Servidor.atencionConexiones){
						Servidor.atencionConexiones.notify();
					}
					
					break;

				case Comando.MOVIMIENTO:					
					
					paqueteMovimiento = (PaqueteMovimiento)paquete;
					
					Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setPosX(paqueteMovimiento.getPosX());
					Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setPosY(paqueteMovimiento.getPosY());
					Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setDireccion(paqueteMovimiento.getDireccion());
					Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setFrame(paqueteMovimiento.getFrame());
					
					synchronized(Servidor.atencionMovimientos){
						Servidor.atencionMovimientos.notify();
					}
					
					break;

				case Comando.MOSTRARMAPAS:
					
					// Indico en el log que el usuario se conecto a ese mapa
					paquetePersonaje = (PaquetePersonaje) paquete;
					Servidor.log.append(socket.getInetAddress().getHostAddress() + " ha elegido el mapa " + paquetePersonaje.getMapa() + System.lineSeparator());
					break;
					
				case Comando.BATALLA:
					
					// Le reenvio al id del personaje batallado que quieren pelear
					paqueteBatalla = (PaqueteBatalla) paquete;
					Servidor.log.append(paqueteBatalla.getId() + " quiere batallar con " + paqueteBatalla.getIdEnemigo() + System.lineSeparator());
					
					//seteo estado de batalla
					Servidor.getPersonajesConectados().get(paqueteBatalla.getId()).setEstado(Estado.estadoBatalla);
					Servidor.getPersonajesConectados().get(paqueteBatalla.getIdEnemigo()).setEstado(Estado.estadoBatalla);
					paqueteBatalla.setMiTurno(true);
					salida.writeObject(paqueteBatalla.getJson());
					for(EscuchaCliente conectado : Servidor.getClientesConectados()){
						if(conectado.getIdPersonaje() == paqueteBatalla.getIdEnemigo()){
							int aux = paqueteBatalla.getId();
							paqueteBatalla.setId(paqueteBatalla.getIdEnemigo());
							paqueteBatalla.setIdEnemigo(aux);
							paqueteBatalla.setMiTurno(false);
							conectado.getSalida().writeObject(paqueteBatalla.getJson());
							break;
						}
					}
					
					synchronized(Servidor.atencionConexiones){
						Servidor.atencionConexiones.notify();
					}
					
					break;
					
				case Comando.ATACAR: 
					paqueteAtacar = (PaqueteAtacar) paquete;
					for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
						if(conectado.getIdPersonaje() == paqueteAtacar.getIdEnemigo()) {
							conectado.getSalida().writeObject(paqueteAtacar.getJson());
						}
					}
					break;
					
				case Comando.FINALIZARBATALLA: 
					paqueteFinalizarBatalla = (PaqueteFinalizarBatalla) paquete;
					Servidor.getPersonajesConectados().get(paqueteFinalizarBatalla.getId()).setEstado(Estado.estadoJuego);
					Servidor.getPersonajesConectados().get(paqueteFinalizarBatalla.getIdEnemigo()).setEstado(Estado.estadoJuego);
					for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
						if(conectado.getIdPersonaje() == paqueteFinalizarBatalla.getIdEnemigo()) {
							conectado.getSalida().writeObject(paqueteFinalizarBatalla.getJson());
						}
					}
					
					synchronized(Servidor.atencionConexiones){
						Servidor.atencionConexiones.notify();
					}
					
					break;
					
				case Comando.ACTUALIZARPERSONAJE:
					paquetePersonaje = (PaquetePersonaje) paquete;
					Servidor.getConector().actualizarPersonaje(paquetePersonaje);
					
					Servidor.getPersonajesConectados().remove(paquetePersonaje.getId());
					Servidor.getPersonajesConectados().put(paquetePersonaje.getId(), paquetePersonaje);

					for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
						conectado.getSalida().writeObject(paquetePersonaje.getJson());
					}
					
					break;
				
				default:
					break;
				}
				
				cadenaLeida = (String) entrada.readObject();
			}

			entrada.close();
			salida.close();
			socket.close();

			Servidor.getPersonajesConectados().remove(paquetePersonaje.getId());
			Servidor.getUbicacionPersonajes().remove(paquetePersonaje.getId());
			Servidor.getClientesConectados().remove(this);

			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				paqueteDePersonajes = new PaqueteDePersonajes(Servidor.getPersonajesConectados());
				paqueteDePersonajes.setComando(Comando.CONEXION);
				conectado.salida.writeObject(paqueteDePersonajes.getJson());
			}

			Servidor.log.append(paquete.getIp() + " se ha desconectado." + System.lineSeparator());

		} catch (IOException | ClassNotFoundException e) {
			Servidor.log.append("Error de conexion: " + e.getMessage() + System.lineSeparator());
			e.printStackTrace();
		} 
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public ObjectInputStream getEntrada() {
		return entrada;
	}
	
	public ObjectOutputStream getSalida() {
		return salida;
	}
	
	public PaquetePersonaje getPaquetePersonaje(){
		return paquetePersonaje;
	}
	
	public int getIdPersonaje() {
		return idPersonaje;
	}
}

