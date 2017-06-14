package comandos;

import mensajeria.Comando;
import servidor.EscuchaCliente;

public abstract class ComandoServer extends Comando {

	public static final String PACKAGEO = "comandos";
	
	protected EscuchaCliente context;
	
	public void setContext(EscuchaCliente context){
		this.context = context;
	}

}
