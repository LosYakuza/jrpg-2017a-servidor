package comandos;

public class Salir extends ComandoServer {

	@Override
	public void ejecutar() {
		context.desconectar();

	}

}
