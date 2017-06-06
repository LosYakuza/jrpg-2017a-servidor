package servidor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

/**
 * Clase que realiza el acceso y modificación de los datos en la BD.
 */
public class Conector {

	private String url = "BaseWome.bd";
	private Connection connect;

	/**
	 * Conectar a la base de datos.
	 */
	public void connect() {
		try {
			Servidor.log.append("Estableciendo conexión con la base de datos..." + System.lineSeparator());
			connect = DriverManager.getConnection("jdbc:sqlite:" + url);
			Servidor.log.append("Conexion con la base de datos establecida con exito." + System.lineSeparator());
		} catch (SQLException ex) {
			Servidor.log.append("Fallo al intentar establecer la conexión con la base de datos. " + ex.getMessage()
					+ System.lineSeparator());
		}
	}

	/**
	 * Cerrar la conexión con la base de datos.
	 */
	public void close() {
		try {
			connect.close();
		} catch (SQLException ex) {
			Servidor.log.append("Error al intentar cerrar la conexion con la base de datos." + System.lineSeparator());
			Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Registra al usuario. Si ya existe el usuario indica que está en uso, sino lo registra en la BD.
	 * @param user datos empaquetados del usuario
	 * @return true si pudo registrar al usuario;
	 *         false en caso contrario.
	 */
	public boolean registrarUsuario(PaqueteUsuario user) {
		ResultSet result = null;
		try {
			PreparedStatement st1 = connect.prepareStatement(
					"SELECT usuario, password, idPersonaje FROM registro WHERE usuario = ?");
			st1.setString(1, user.getUsername());
			result = st1.executeQuery();

			if (!result.next()) {
				PreparedStatement st = connect.prepareStatement(
						"INSERT INTO registro (usuario, password, idPersonaje) VALUES (?,?,?)");
				st.setString(1, user.getUsername());
				st.setString(2, user.getPassword());
				st.setInt(3, user.getIdPj());
				st.execute();
				Servidor.log.append("El usuario " + user.getUsername() + " se ha registrado." + System.lineSeparator());
				return true;
			} else {
				Servidor.log.append("El usuario " + user.getUsername() + " ya se encuentra en uso." + System.lineSeparator());
				return false;
			}
		} catch (SQLException ex) {
			Servidor.log.append("Eror al intentar registrar el usuario " + user.getUsername() + System.lineSeparator());
			System.err.println(ex.getMessage());
			return false;
		}

	}

	/**
	 * Inserta en la tabla personaje
	 * @param paquetePersonaje datos del personaje
	 * @param paqueteUsuario datos del usuario
	 * @return true si se pudo registrar el personaje, linkearlo al usuario y registrar su inventario.
	 *         false en caso contrario.
	 */
	public boolean registrarPersonaje(PaquetePersonaje paquetePersonaje, PaqueteUsuario paqueteUsuario) {
		try {
			PreparedStatement stRegistrarPersonaje = connect.prepareStatement(
					"INSERT INTO personaje ("
					+ "casta, raza, fuerza, destreza, inteligencia,"
					+ "saludTope, energiaTope, nombre, experiencia, nivel, idAlianza"
					+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?)",
					PreparedStatement.RETURN_GENERATED_KEYS);
			stRegistrarPersonaje.setString(1, paquetePersonaje.getCasta());
			stRegistrarPersonaje.setString(2, paquetePersonaje.getRaza());
			stRegistrarPersonaje.setInt(3, paquetePersonaje.getFuerza());
			stRegistrarPersonaje.setInt(4, paquetePersonaje.getDestreza());
			stRegistrarPersonaje.setInt(5, paquetePersonaje.getInteligencia());
			stRegistrarPersonaje.setInt(6, paquetePersonaje.getSaludTope());
			stRegistrarPersonaje.setInt(7, paquetePersonaje.getEnergiaTope());
			stRegistrarPersonaje.setString(8, paquetePersonaje.getNombre());
			stRegistrarPersonaje.setInt(9, 0);
			stRegistrarPersonaje.setInt(10, 1);
			stRegistrarPersonaje.setInt(11, -1);
			stRegistrarPersonaje.execute();

			// Recupero la última key generada
			ResultSet rs = stRegistrarPersonaje.getGeneratedKeys();
			if (rs != null && rs.next()) {

				// Obtengo el id
				int idPersonaje = rs.getInt(1);

				// Le asigno el id al paquete personaje que voy a devolver
				paquetePersonaje.setId(idPersonaje);

				// Le asigno el personaje al usuario
				PreparedStatement stAsignarPersonaje = connect.prepareStatement(
						"UPDATE registro SET idPersonaje=? WHERE usuario=? AND password=?");
				stAsignarPersonaje.setInt(1, idPersonaje);
				stAsignarPersonaje.setString(2, paqueteUsuario.getUsername());
				stAsignarPersonaje.setString(3, paqueteUsuario.getPassword());
				stAsignarPersonaje.execute();

				// Por ultimo registro el inventario y la mochila
				if (this.registrarInventario(idPersonaje)) {
					Servidor.log.append("El usuario " + paqueteUsuario.getUsername() + " ha creado el personaje "
							+ paquetePersonaje.getId() + System.lineSeparator());
					return true;
				} else {
					Servidor.log.append("Error al registrar la mochila y el inventario del usuario " + paqueteUsuario.getUsername() + " con el personaje" + paquetePersonaje.getId() + System.lineSeparator());
					return false;
				}
			}


		} catch (SQLException e) {
			Servidor.log.append(
					"Error al intentar crear el personaje " + paquetePersonaje.getNombre() + System.lineSeparator());
			e.printStackTrace();
			return false;
		}
		return false;

	}

	/**
	 * Vincula al inventario con el personaje.
	 * @param idPersonaje id del personaje
	 * @return true si pudo registrar el inventario;
	 *         false en caso contrario.
	 */
	public boolean registrarInventario(final int idPersonaje) {
		try {
			// Preparo la consulta para el registro el inventario en la base de datos
			PreparedStatement stRegistrarInventario = connect.prepareStatement(
					"INSERT INTO inventario(idPersonaje) VALUES (?)");
			stRegistrarInventario.setInt(1, idPersonaje);

			// Registro inventario
			stRegistrarInventario.execute();

			Servidor.log.append("Se ha registrado el inventario de " + idPersonaje + System.lineSeparator());
			return true;
		} catch (SQLException e) {
			Servidor.log.append("Error al registrar el inventario de " + idPersonaje + System.lineSeparator());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Logueo de usuario.
	 * @param user datos de usuario
	 * @return true si se encontró la combinación usuario-contraseña.
	 *         false en caso contrario.
	 */
	public boolean loguearUsuario(PaqueteUsuario user) {
		ResultSet result = null;
		try {
			// Busco usuario y contraseña
			PreparedStatement st = connect
					.prepareStatement("SELECT * FROM registro WHERE usuario = ? AND password = ? ");
			st.setString(1, user.getUsername());
			st.setString(2, user.getPassword());
			result = st.executeQuery();

			// Si existe inicio sesion
			if (result.next()) {
				Servidor.log.append("El usuario " + user.getUsername() + " ha iniciado sesión." + System.lineSeparator());
				return true;
			}

			// Si no existe informo y devuelvo false
			Servidor.log.append("El usuario " + user.getUsername() + " ha realizado un intento fallido de inicio de sesión." + System.lineSeparator());
			return false;

		} catch (SQLException e) {
			Servidor.log.append("El usuario " + user.getUsername() + " fallo al iniciar sesión." + System.lineSeparator());
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Actualización de datos del personaje.
	 * @param paquetePersonaje datos del personaje
	 */
	public void actualizarPersonaje(PaquetePersonaje paquetePersonaje) {
		try {
			PreparedStatement stActualizarPersonaje = connect
					.prepareStatement("UPDATE personaje SET fuerza=?, destreza=?, inteligencia=?, saludTope=?, energiaTope=?, experiencia=?, nivel=? "
							+ "  WHERE idPersonaje=?"); //Ver si acá me llegan los valores modificados por los items. Si es así, hay que corregirlo.

			stActualizarPersonaje.setInt(1, paquetePersonaje.getFuerza());
			stActualizarPersonaje.setInt(2, paquetePersonaje.getDestreza());
			stActualizarPersonaje.setInt(3, paquetePersonaje.getInteligencia());
			stActualizarPersonaje.setInt(4, paquetePersonaje.getSaludTope());
			stActualizarPersonaje.setInt(5, paquetePersonaje.getEnergiaTope());
			stActualizarPersonaje.setInt(6, paquetePersonaje.getExperiencia());
			stActualizarPersonaje.setInt(7, paquetePersonaje.getNivel());
			stActualizarPersonaje.setInt(8, paquetePersonaje.getId());

			stActualizarPersonaje.executeUpdate();

			Servidor.log.append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con exito."  + System.lineSeparator());;
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()  + System.lineSeparator());
			e.printStackTrace();
		}

	}

	public PaquetePersonaje getPersonaje(PaqueteUsuario user) {
		ResultSet result = null;
		try {
			// Selecciono el personaje de ese usuario
			PreparedStatement st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
			st.setString(1, user.getUsername());
			result = st.executeQuery();

			// Obtengo el id
			int idPersonaje = result.getInt("idPersonaje");

			// Selecciono los datos del personaje
			PreparedStatement stSeleccionarPersonaje = connect
					.prepareStatement("SELECT * FROM personaje WHERE idPersonaje = ?");
			stSeleccionarPersonaje.setInt(1, idPersonaje);
			result = stSeleccionarPersonaje.executeQuery();

			// Obtengo los atributos del personaje
			PaquetePersonaje personaje = new PaquetePersonaje();
			personaje.setId(idPersonaje);
			personaje.setRaza(result.getString("raza"));
			personaje.setCasta(result.getString("casta"));
			personaje.setFuerza(result.getInt("fuerza"));
			personaje.setInteligencia(result.getInt("inteligencia"));
			personaje.setDestreza(result.getInt("destreza"));
			personaje.setEnergiaTope(result.getInt("energiaTope"));
			personaje.setSaludTope(result.getInt("saludTope"));
			personaje.setNombre(result.getString("nombre"));
			personaje.setExperiencia(result.getInt("experiencia"));
			personaje.setNivel(result.getInt("nivel"));

			// Devuelvo el paquete personaje con sus datos
			return personaje;

		} catch (SQLException ex) {
			Servidor.log.append("Fallo al intentar recuperar el personaje " + user.getUsername() + System.lineSeparator());
			Servidor.log.append(ex.getMessage() + System.lineSeparator());
			ex.printStackTrace();
		}

		return new PaquetePersonaje();
	}
	
	public PaqueteUsuario getUsuario(String usuario) {
		ResultSet result = null;
		PreparedStatement st;
		
		try {
			st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
			st.setString(1, usuario);
			result = st.executeQuery();

			String password = result.getString("password");
			int idPersonaje = result.getInt("idPersonaje");
			
			PaqueteUsuario paqueteUsuario = new PaqueteUsuario();
			paqueteUsuario.setUsername(usuario);
			paqueteUsuario.setPassword(password);
			paqueteUsuario.setIdPj(idPersonaje);
			
			return paqueteUsuario;
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar recuperar el usuario " + usuario + System.lineSeparator());
			Servidor.log.append(e.getMessage() + System.lineSeparator());
			e.printStackTrace();
		}
		
		return new PaqueteUsuario();
	}
}
