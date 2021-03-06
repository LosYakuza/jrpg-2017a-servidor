package chatServidor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import chat.Message;

public class ChatHandler extends Thread {

	public static int WAITING_LOGIN = 0;
	public static int LOGGED = 1;
	private final Pattern patternUsr = Pattern.compile("^[A-Za-z0-9]{4,10}$");
	public static HashMap<String, ChatHandler> clients;
	public static Logger log;
	
	private Socket s;
	private DataInputStream i;
	private DataOutputStream o;
	private int status;
	private String user;

	/**
	 * Crea instancia y buffers para enviar datos
	 * 
	 * @param s
	 * @throws IOException
	 */
	public ChatHandler(Socket s) throws IOException {
		this.s = s;
		this.i = new DataInputStream(new BufferedInputStream(this.s.getInputStream()));
		this.o = new DataOutputStream(new BufferedOutputStream(this.s.getOutputStream()));
		user = "";
		handshake();
	}

	/**
	 * Espera mensaje del cliente
	 */
	@Override
	public void run() {
		boolean c = true;
		while (c) {
			try {
				Message msg = new Message(i.readUTF());
				msg.setSource(this.user);
				process(msg);
			} catch (IOException e) {
				kissoff();
				c=false;
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Procesa mensaje recibido
	 * 
	 * @param m
	 * @throws IOException
	 */
	private synchronized void process(Message m) throws IOException {
		if (this.status == WAITING_LOGIN) {
			if (m.getType() == Message.CLIENT_DATA && m.getDestination().equals("user")) {
				login(m);
			} else {
				kissoff();
			}
		} else {
			m.setSource(this.user);
			if (m.getType() == Message.USR_MSJ) {
				try{
					broadcast(m);
				}catch (Exception e) {
					m.setSource(m.getDestination());
					m.setDestination(this.user);
					m.setText("Usuario no conectado.");
					m.setType(Message.STATUS_INFO);
					broadcast(m);
				}
			} else {
				
			}
		}
	}

	/**
	 * Ejecuta logueo
	 * 
	 * @param m
	 * @throws IOException
	 */
	private void login(Message m) throws IOException {
		this.user = m.getText();
		Iterator<String> list = ChatHandler.clients.keySet().iterator();
		if(this.user.toLowerCase().equals("all") || !patternUsr.matcher(this.user).matches()){
			sendError("login","Nombre de usuario invalido. (4 a 10 caracteres, solo letras y numeros)");
			return;
		}
		while (list.hasNext()) {
			if(this.user.toUpperCase().equals(list.next().toUpperCase())){
				this.user="";
				sendError("login","Usuario ya existe.");
				return;
			}
		}
		ChatHandler.clients.put(this.user, this);
		sendClientList();
		this.status = ChatHandler.LOGGED;
	}

	private void sendError(String group,String msg) throws IOException{
		Message m = new Message();
		m.setType(Message.SERVER_FATAL);
		m.setDestination(group);
		m.setSource("server");
		m.setText(msg);
		send(m);
	}
	
	/**
	 * Envia lista de clientes a cada uno de ellos
	 * 
	 * @throws IOException
	 */
	private void sendClientList() throws IOException {
		Message m = new Message();
		String ulist = "";
		m.setDestination("clientlist");
		m.setSource("server");
		m.setType(Message.STATUS_INFO);
		Iterator<String> list = ChatHandler.clients.keySet().iterator();
		while (list.hasNext()) {
			ulist = ulist + list.next() + ",";
		}
		if(!ulist.equals("")){
			ulist = ulist.substring(0, ulist.length() - 1);
		}
		m.setText(ulist);
		broadcastAll(m);
	}

	
	private void broadcastAll(Message m){
		Iterator<ChatHandler> listClients = ChatHandler.clients.values().iterator();
		while (listClients.hasNext()) {
			try {
				listClients.next().send(m);
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * Transmite mensaje a destino
	 * 
	 * @param m
	 * @throws IOException
	 */
	private void broadcast(Message m) throws IOException {
		if(m.getDestination().equals("all")){
			broadcastAll(m);
		}else{
			ChatHandler.clients.get(m.getDestination()).send(m);
		}
	}

	/**
	 * Envia mensaje a this
	 * 
	 * @param m
	 * @throws IOException
	 */
	private void send(Message m) throws IOException {
		this.o.writeUTF(m.toString());
		this.o.flush();
	}

	/**
	 * Envia request para login
	 * 
	 * @throws IOException
	 */
	private void handshake() throws IOException {
		this.status = WAITING_LOGIN;
		Message m = new Message();
		m.setType(Message.SERVER_ASK);
		m.setText("login");
		send(m);

	}

	public static void kick(String usr){
		if(clients.containsKey(usr)){
			ChatHandler ch = clients.get(usr);
			try {
				ch.sendError("login", "Fuiste desconectado por el server");
			} catch (IOException e) {
				
			}
			clients.remove(usr);
			ch.kissoff();
		}
	}
	
	/**
	 * Cierra conexion con cliente
	 * @throws IOException 
	 */
	public void kissoff() {
		if(!clients.containsKey(this.user))
			return;
		try {
			this.s.close();
			if (!this.user.equals("")) {
				ChatHandler.clients.remove(this.user);
			}
			try{
				sendClientList();
			}catch(Exception e){}
		} catch (IOException e) {

		}
		
	}
}
