package comandos;

public class Desconectar extends ComandoServer {

	@Override
	public void ejecutar() {
		context.desconectar();
		
	}

}
