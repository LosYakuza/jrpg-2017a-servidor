package servidor;

import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;

import cliente.*;
import comandos.ComandoServer;
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
	private PaqueteUsuario paqueteUsuario = new PaqueteUsuario();
	private boolean conectado = true;

	public EscuchaCliente(String ip, Socket socket, ObjectInputStream entrada, ObjectOutputStream salida) {
		this.socket = socket;
		this.entrada = entrada;
		this.salida = salida;
		paquetePersonaje = new PaquetePersonaje();
	}

	public void run() {
		Paquete paquete = new Paquete();
		String cadenaLeida;

		while (conectado) {

			try {
				cadenaLeida = (String) entrada.readObject();
				paquete = Paquete.loadJson(cadenaLeida);
				ComandoServer cs = (ComandoServer) paquete.getComandoObj(ComandoServer.PACKAGEO);
				cs.setContext(this);
				cs.ejecutar();

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				desconectar();
			}

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

	public PaquetePersonaje getPaquetePersonaje() {
		return paquetePersonaje;
	}

	public void setPaquetePersonaje(PaquetePersonaje pp) {
		this.paquetePersonaje = pp;
		this.idPersonaje = pp.getId();
	}

	public PaqueteUsuario getPaqueteUsuario() {
		return this.paqueteUsuario;
	}

	public void setPaqueteUsuario(PaqueteUsuario pu) {
		this.paqueteUsuario = pu;
	}

	public int getIdPersonaje() {
		return idPersonaje;
	}

	public void desconectar() {
		try {
			entrada.close();
			salida.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (paquetePersonaje.getEstado() != Estado.estadoOffline) {
			Servidor.getPersonajesConectados().remove(paquetePersonaje.getId());
			Servidor.getUbicacionPersonajes().remove(paquetePersonaje.getId());
			Servidor.getClientesConectados().remove(this);

			Servidor.log.append("Desconectado " + paquetePersonaje.getNombre() + System.lineSeparator());

			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				PaqueteDePersonajes paqueteDePersonajes = new PaqueteDePersonajes(Servidor.getPersonajesConectados());
				paqueteDePersonajes.setComando(Comando.CONEXION);
				try {
					conectado.salida.writeObject(paqueteDePersonajes.getJson());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.conectado = false;

	}
}
