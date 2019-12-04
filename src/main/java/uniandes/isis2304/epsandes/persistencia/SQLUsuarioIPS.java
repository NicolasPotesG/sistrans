/**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Universidad	de	los	Andes	(Bogotá	- Colombia)
 * Departamento	de	Ingeniería	de	Sistemas	y	Computación
 * Licenciado	bajo	el	esquema	Academic Free License versión 2.1
 * 		
 * Curso: isis2304 - Sistemas Transaccionales
 * Proyecto: Parranderos Uniandes
 * @version 1.0
 * @author Germán Bravo
 * Julio de 2018
 * 
 * Revisado por: Claudia Jiménez, Christian Ariza
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package uniandes.isis2304.epsandes.persistencia;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.FetchGroup;

import uniandes.isis2304.epsandes.negocio.EPS;
import uniandes.isis2304.epsandes.negocio.UsuarioIPS;

/**
 * Clase que encapsula los métodos que hacen acceso a la base de datos para el concepto BAR de Parranderos
 * Nótese que es una clase que es sólo conocida en el paquete de persistencia
 * 
 * @author Germán Bravo
 */
class SQLUsuarioIPS 
{
	/* ****************************************************************
	 * 			Constantes
	 *****************************************************************/
	/**
	 * Cadena que representa el tipo de consulta que se va a realizar en las sentencias de acceso a la base de datos
	 * Se renombra acá para facilitar la escritura de las sentencias
	 */
	private final static String SQL = PersistenciaEPSAndes.SQL;

	/* ****************************************************************
	 * 			Atributos
	 *****************************************************************/
	/**
	 * El manejador de persistencia general de la aplicación
	 */
	private PersistenciaEPSAndes pp;

	/* ****************************************************************
	 * 			Métodos
	 *****************************************************************/

	/**
	 * Constructor
	 * @param pp - El Manejador de persistencia de la aplicación
	 */
	public SQLUsuarioIPS (PersistenciaEPSAndes pp)
	{
		this.pp = pp;
	}
	
	/**
	 * Crea y ejecuta la sentencia SQL para adicionar un BAR a la base de datos de Parranderos
	 * @param pm - El manejador de persistencia
	 * @param idBar - El identificador del bar
	 * @param nombre - El nombre del bar
	 * @param ciudad - La ciudad del bar
	 * @param presupuesto - El presupuesto del bar (ALTO, MEDIO, BAJO)
	 * @param sedes - El número de sedes del bar
	 * @return El número de tuplas insertadas
	 */
	public long adicionarUsuarioIPS (PersistenceManager pm,  String nombre, String estado, long numDocumento , int tipodocumento, String fechaNacimiento, 
								long idEPS, String esAfiliado, String correo, String genero, int edad) 
	{
        Query q = pm.newQuery(SQL, "INSERT INTO " + "USUARIO_IPS" + "(nombre, estado, numDocumento , tipodocumento, fechaNacimiento, idEPS, esAfiliado, correo, genero, edad) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        q.setParameters(nombre, estado, numDocumento , tipodocumento, fechaNacimiento, idEPS, esAfiliado, correo, genero, edad);

        return (long) q.executeUnique();
	}

	/**
	 * Crea y ejecuta la sentencia SQL para eliminar UN BAR de la base de datos de Parranderos, por su identificador
	 * @param pm - El manejador de persistencia
	 * @param idBar - El identificador del bar
	 * @return EL número de tuplas eliminadas
	 */
	public long eliminarUsuarioIPSPorId (PersistenceManager pm, long id)
	{
        Query q = pm.newQuery(SQL, "DELETE FROM " + pp.darTablaUsuarioIPS () + " WHERE id = ?");
        q.setParameters(id);
        return (long) q.executeUnique();
	}

	/**
	 * Crea y ejecuta la sentencia SQL para encontrar la información de UN BAR de la 
	 * base de datos de Parranderos, por su identificador
	 * @param pm - El manejador de persistencia
	 * @param id - El identificador del bar
	 * @return El objeto BAR que tiene el identificador dado
	 */
	public UsuarioIPS darUsuarioIPSPorId (PersistenceManager pm, long id) 
	{
		Query q = pm.newQuery(SQL, "SELECT * FROM " + pp.darTablaUsuarioIPS () + " WHERE id = ?");
		q.setResultClass(UsuarioIPS.class);
		q.setParameters(id);
		return (UsuarioIPS) q.executeUnique();
	}

	/**
	 * Crea y ejecuta la sentencia SQL para encontrar la información de LOS BARES de la 
	 * base de datos de Parranderos
	 * @param pm - El manejador de persistencia
	 * @return Una lista de objetos BAR
	 */
	public List<UsuarioIPS> darUsuarioIPSs (PersistenceManager pm)
	{
		Query q = pm.newQuery(SQL, "SELECT * FROM " + pp.darTablaUsuarioIPS ());
		q.setResultClass(UsuarioIPS.class);
		return (List<UsuarioIPS>) q.executeList();
	}
	
	public List<UsuarioIPS> AfiliadoExigente(PersistenceManager pm, String fechaInicio, String fechaFin){
		String sql ="SELECT usuario_ips.* FROM";
		sql +="(cita.idUsuarioIPS idUsuario, COUNT(cita.idConsulta) AS numConsultas, COUNT(cita.idTerapia) AS numTerapias, "
				+ "COUNT(cita.idProcedimientoEsp) AS numProcedimieto, COUNT(cita.idHospitalizacion) AS numHospitalizaciones ";
		sql += "FROM cita INNER JOIN ";
		sql += "receta ON  cita.id = receta.idCita";
		sql	+= "WHERE TO_DATE('cita.fechaInicio', 'YYYY-MM-DD')>= TO_DATE('"+fechaInicio+",'YYYY-MM-DD')";
		sql	+= "AND TO_DATE('cita.fechaFin', 'YYYY-MM-DD')<= TO_DATE('"+fechaFin+",'YYYY-MM-DD')";
	  	sql	+= "GROUP BY cita.idUsuarioIPS";
	  	sql	+= "HAVING  (COUNT(cita.idConsulta)+ COUNT(cita.idTerapia)+ COUNT(cita.idProcedimientoEsp)+COUNT(cita.idHospitalizacion)>12)";
	  	sql	+= "AND";
	  	
	    Query q = pm.newQuery(SQL, sql);
		
			return q.executeList(); 	
	}
	
	public List<Object[]> RFC9(PersistenceManager pm, String fechaInicio, String fechaFin, String nombreIPS, String asendente, String numeroconsultas, String servicioSalud){
		
		String tipoServicio = "";
		if(fechaInicio.equals("")){
			fechaInicio = "2/10/2017";
			fechaFin= "11/1/2021";
		}
		if(!servicioSalud.equals("")){
			String procedimiento = servicioSalud.replace("_", "");
			tipoServicio = "INNER JOIN "+servicioSalud+" ON CITA.id"+procedimiento+ " = "+servicioSalud+".id";
			
		}
		String sql = " SELECT USUARIO_IPS.numdocumento, USUARIO_IPS.nombre , x.numero"+servicioSalud+",  USUARIO_IPS.estado, ";
		sql +="               USUARIO_IPS.fechaNacimiento, USUARIO_IPS.esAfiliado,  USUARIO_IPS.correo, USUARIO_IPS.genero, USUARIO_IPS.edad " ;
		sql +="        FROM( ";
		sql +="                select usuario_ips.numdocumento, usuario_ips.nombre, count(usuario_ips.numdocumento)  numero"+servicioSalud;
		sql +="                FROM USUARIO_IPS INNER JOIN CITA ON USUARIO_IPS.numdocumento = CITA.idUSUARIOIPS ";
		sql +="                INNER JOIN RECETA ON CITA.ID = RECETA.idCita ";
		sql += tipoServicio;
		sql +="                WHERE TO_DATE(cita.fechaInicio, 'MM-DD-YYYY')>= TO_DATE('"+fechaInicio+"','MM-DD-YYYY') ";
		sql +="                      AND TO_DATE(cita.fechaFin, 'MM-DD-YYYY')<= TO_DATE('"+fechaFin+"','MM-DD-YYYY') ";
		sql +="                GROUP BY (usuario_ips.numdocumento, usuario_ips.nombre  ) )x ";
		
		sql +="        INNER JOIN USUARIO_IPS ON X.numdocumento = USUARIO_IPS.numdocumento ";
		
	    Query q = pm.newQuery(SQL, sql);
		List<Object[]> lista = q.executeList(); 	
			return lista;
	}
	
	
}
