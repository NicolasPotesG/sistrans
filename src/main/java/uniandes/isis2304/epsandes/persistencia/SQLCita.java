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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.datanucleus.store.query.CandidateIdsQueryResult.ResultIterator;

import uniandes.isis2304.epsandes.negocio.Cita;
import uniandes.isis2304.epsandes.negocio.EPS;
import uniandes.isis2304.epsandes.negocio.Cita;

/**
 * Clase que encapsula los métodos que hacen acceso a la base de datos para el concepto BAR de Parranderos
 * Nótese que es una clase que es sólo conocida en el paquete de persistencia
 * 
 * @author Germán Bravo
 */
class SQLCita 
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
	public SQLCita (PersistenciaEPSAndes pp)
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
	public long adicionarCita (PersistenceManager pm, long id, String horaInicio, String horaFin, long idMedico, long idConsulta, long idTerapia, long idProcedimientoEsp, long idHospitalizacion, long idUsuarioIPS) 
			throws Exception{


		String tabla = "";

		long idSS = 0;

		if(idConsulta != 0) {

			tabla = "CONSULTA";
			idSS = idConsulta;

		} else if(idTerapia != 0) {

			tabla = "TERAPIA";
			idSS = idTerapia;

		} else if(idProcedimientoEsp != 0) {

			tabla = "PROCEDIMIENTO_ESP";
			idSS = idProcedimientoEsp;

		} else if(idHospitalizacion != 0) {

			tabla = "HOSPITALIZACION";
			idSS = idHospitalizacion;

		} 





		//Verifica que el medico trabaja en la IPS que presta ese servicio de salud        
		Query darIPSSS = pm.newQuery(SQL, "SELECT idips FROM " + tabla + " WHERE id = ?");
		darIPSSS.setParameters(idSS);

		Query darIPSMedico = pm.newQuery(SQL, "SELECT idips FROM " + "IPS_MEDICO" + " WHERE idmedico = ?");
		darIPSMedico.setParameters(idMedico);


		BigDecimal result = (BigDecimal) darIPSSS.executeUnique();
		BigDecimal result2 = (BigDecimal) darIPSMedico.executeUnique();


		long resultIPSSS = result.longValue();
		long resultIPSMedico = result2.longValue();


		if(resultIPSSS != resultIPSMedico) {

			throw new Exception("El medico no pertenece a la ips que ofrece ese servicio de salud");

		}


		//Verifica que el usuario pertenezca a una EPS que tenga esa IPS que presta ese servicio de salud

		Query darEPSUsuario = pm.newQuery(SQL, "SELECT ideps FROM " + "USUARIO_IPS" + " WHERE numdocumento = ?");
		darEPSUsuario.setParameters(idUsuarioIPS);

		BigDecimal idEPSUsuario = (BigDecimal) darEPSUsuario.executeUnique();
		long idEPSUsuario2 = idEPSUsuario.longValue();

		Query darEPSCita = pm.newQuery(SQL, "SELECT ideps FROM " + "IPS" + " WHERE id = ?");
		darEPSCita.setParameters(resultIPSSS);

		BigDecimal idEPSCita = (BigDecimal) darEPSCita.executeUnique();
		long idEPSCita2 = idEPSCita.longValue();



		if(idEPSUsuario2 != idEPSCita2) {

			throw new Exception("El usuario no pertenece a la IPS que contiene este servicio de salud");

		}


		//Verificar que la hora de la cita este en los rangos de horas del ss

		Query darHora = pm.newQuery(SQL, "SELECT id FROM " + tabla + " WHERE id = ? AND (TO_DATE(?,'DD-MM-YY HH24:MI:SS') BETWEEN TO_DATE(horainicio,'DD-MM-YY HH24:MI:SS') AND TO_DATE(horafin,'DD-MM-YY HH24:MI:SS')) "
				+ "AND (TO_DATE(?,'DD-MM-YY HH24:MI:SS') BETWEEN TO_DATE(horainicio,'DD-MM-YY HH24:MI:SS') AND TO_DATE(horafin,'DD-MM-YY HH24:MI:SS'))");

		darHora.setParameters(idSS, horaInicio, horaFin);

		BigDecimal idHoraSS = (BigDecimal)darHora.executeUnique(); 
		long idHoraSS2 = idHoraSS.longValue();


		if(idHoraSS2 != idSS) {

			throw new Exception("El rango horario de la cita no coincide con el del servicio de salud");

		}


		//Verifica que el Usuario de la IPS puede acceder a este servicio de salud (afiliado o no)

		Query darAfiliadoUsuario = pm.newQuery(SQL, "SELECT esafiliado FROM USUARIO_IPS WHERE numdocumento = ?");
		darAfiliadoUsuario.setParameters(idUsuarioIPS);

		String resultAfiliado = (String) darAfiliadoUsuario.executeUnique();


		Query darAfiliadoSS = pm.newQuery(SQL, "SELECT esafiliado FROM " + tabla +  " WHERE id = ?");
		darAfiliadoSS.setParameters(idSS);


		String resultAfiliadoSS = (String) darAfiliadoSS.executeUnique();

		if(resultAfiliadoSS.equals("SI") && resultAfiliado.equals("NO")) {

			throw new Exception("El usuario no puede hacer uso del ss ya que no es afiliado");

		}



		Query q = pm.newQuery(SQL, "INSERT INTO " + "CITA" + "(id, horainicio, horafin, idmedico, idconsulta, idterapia, idprocedimientoesp, idhospitalizacion, idusuarioips) values (?,?,?,?,?,?,?,?,?)");
		q.setParameters(id, horaInicio, horaFin, idMedico, null, idTerapia, null, null, idUsuarioIPS);



		if(idConsulta != 0) {

			q.setParameters(id, horaInicio, horaFin, idMedico, idConsulta, null, null, null, idUsuarioIPS);

		} else if(idTerapia != 0) {

			q.setParameters(id, horaInicio, horaFin, idMedico, null, idTerapia, null, null, idUsuarioIPS);

		} else if(idProcedimientoEsp != 0) {

			q.setParameters(id, horaInicio, horaFin, idMedico, null, null, idProcedimientoEsp, null, idUsuarioIPS);

		} else if(idHospitalizacion != 0) {

			q.setParameters(id, horaInicio, horaFin, idMedico, null, null, null, idHospitalizacion, idUsuarioIPS);

		} 

		return (long) q.executeUnique();
	}

	/**
	 * Crea y ejecuta la sentencia SQL para eliminar BARES de la base de datos de Parranderos, por su nombre
	 * @param pm - El manejador de persistencia
	 * @param nombreBar - El nombre del bar
	 * @return EL número de tuplas eliminadas
	 */
	public long eliminarCitaPorNombre (PersistenceManager pm, String nombre)
	{
		Query q = pm.newQuery(SQL, "DELETE FROM " + pp.darTablaCita () + " WHERE nombre = ?");
		q.setParameters(nombre);
		return (long) q.executeUnique();
	}

	/**
	 * Crea y ejecuta la sentencia SQL para eliminar UN BAR de la base de datos de Parranderos, por su identificador
	 * @param pm - El manejador de persistencia
	 * @param idBar - El identificador del bar
	 * @return EL número de tuplas eliminadas
	 */
	public long eliminarCitaPorId (PersistenceManager pm, long id)
	{
		Query q = pm.newQuery(SQL, "DELETE FROM " + pp.darTablaCita () + " WHERE id = ?");
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
	public Cita darCitaPorId (PersistenceManager pm, long id) 
	{
		Query q = pm.newQuery(SQL, "SELECT * FROM " + pp.darTablaCita () + " WHERE id = ?");
		q.setResultClass(Cita.class);
		q.setParameters(id);
		return (Cita) q.executeUnique();
	}

	/**
	 * Crea y ejecuta la sentencia SQL para encontrar la información de LOS BARES de la 
	 * base de datos de Parranderos, por su nombre
	 * @param pm - El manejador de persistencia
	 * @param nombreBar - El nombre de bar buscado
	 * @return Una lista de objetos BAR que tienen el nombre dado
	 */
	public List<Cita> darCitaPorNombre (PersistenceManager pm, String nombre) 
	{
		Query q = pm.newQuery(SQL, "SELECT * FROM " + pp.darTablaCita () + " WHERE nombre = ?");
		q.setResultClass(Cita.class);
		q.setParameters(nombre);
		return (List<Cita>) q.executeList();
	}

	/**
	 * Crea y ejecuta la sentencia SQL para encontrar la información de LOS BARES de la 
	 * base de datos de Parranderos
	 * @param pm - El manejador de persistencia
	 * @return Una lista de objetos BAR
	 */
	public List<Cita> darCitas (PersistenceManager pm)
	{
		Query q = pm.newQuery(SQL, "SELECT * FROM " + pp.darTablaCita ());
		q.setResultClass(Cita.class);
		return (List<Cita>) q.executeList();
	}

	public List<Object []> darRFC2 (PersistenceManager pm,String fechaInicio, String fechaFin)
	{
		String sql = "SELECT* FROM";
		sql +="(SELECT cita.idConsulta, cita.idTerapia, cita.idProcedimientoEsp, cita.idHospitalizacion";
		sql += "COUNT(cita.idConsulta, cita.idTerapia, cita.idProcedimientoEsp, cita.idHospitalizacion) AS CUANTOS";
		sql	+= "FROM cita";
		sql	+= "INNER JOIN consulta ON cita.idConsulta = consulta.id";
		sql	+= "INNER JOIN terapia ON cita.idTerapia = terapia.id";
		sql	+= "INNER JOIN procedimiento_esp ON cita.procedimientoEsp = procedimiento_esp.id";
		sql	+= "INNER JOIN hospitalizacion ON cita.idHospitalizacion = hospitalizacion.id";
		sql	+= "WHERE TO_DATE(cita.fechaInicio, 'YYYY-MM-DD')>= TO_DATE('"+fechaInicio+"' ,'YYYY-MM-DD') \n";
		sql	+= "AND TO_DATE(cita.fechaFin, 'YYYY-MM-DD')<= TO_DATE('"+fechaFin+"' ,'YYYY-MM-DD') \n";
		sql	+= "GROUP BY (cita.idConsulta, cita.idTerapia, cita.idProcedimientoEsp, cita.idHospitalizacion)";
		sql	+= "ORDER BY CUANTOS DESC)t";
		sql	+="WHERE ROWNUM BETWEN 1 AND 20";

		Query q = pm.newQuery(SQL, sql);

		return q.executeList();
	}

	public List<Object> darRFC6(PersistenceManager pm, String unidadTiempo, String servicio){

		String sql = "SELECT* FROM";
		sql +="(SELECT cita.fechaInicio, COUNT(cita.fechaInicio) AS numcitas";
		sql	+= "FROM cita";
		sql	+= "cita.id"+servicio+" IS NOT NULL";
		sql += "GROUP BY cita.fechaInicio";
		sql	+= "ORDER BY CUANTOS DESC)t";
		sql	+="WHERE ROWNUM BETWEN 1 AND 2";

		Query q = pm.newQuery(SQL, sql);

		return q.executeList();

	}


	//Metodos it3

	public String reqC11(PersistenceManager pm, String semana, String semana2) {
		
		String respuestaReq = "Reporte semanal entre: " + semana + " y " + semana2 +" \n\n";


		//Variables de busqueda
		String[] arrayTipoSS = new String[4];

		arrayTipoSS[0] = "idconsulta";
		arrayTipoSS[1] = "idterapia";
		arrayTipoSS[2] = "idprocedimientoesp";
		arrayTipoSS[3] = "idhospitalizacion";

		int max = 0;
		int min = 100000000;

		int max2 = 0;
		int min2 = 100000000;
		String ssMax2 = "";
		String ssMin2 = "";

		String ssMax = "";
		String ssMin = "";
		int idMax = 0;
		int idMin = 0;


		for(int i = 0; i < 4; i++) {

			//Sentencia ss mas usado
			String sql = 
					"select t." +  arrayTipoSS[i] + ", t.total ";
			sql += "from (SELECT " + arrayTipoSS[i] + ", COUNT( "+ arrayTipoSS[i] +" ) AS total ";
			sql += "FROM  cita ";
			sql	+= "where fechainicio between ' "+ semana +" ' and " + " ' " + semana2 + " ' ";
			sql	+= "and fechafin between ' "+ semana +" ' and " + " ' " + semana2 + " ' ";
			sql += "GROUP BY " + arrayTipoSS[i];
			sql += " ORDER BY total DESC)t ";

			//se ejecuta el query por cada ss
			Query q1 = pm.newQuery(SQL, sql);
			List<Object[]> lista1 = q1.executeList();

			Object[] objSSMax = (Object[]) lista1.get(0);
			Object[] objSSMin = (Object[]) lista1.get(lista1.size()-2);

			BigDecimal x = ((BigDecimal) objSSMax[1]);
			BigDecimal y = ((BigDecimal) objSSMin[1]);

			BigDecimal x1 = ((BigDecimal) objSSMax[0]);
			BigDecimal y1 = ((BigDecimal) objSSMin[0]);



			if(x.intValue() > max) {

				ssMax = arrayTipoSS[i].substring(2, arrayTipoSS[i].length());
				idMax = x1.intValue();
				max = x.intValue();

			}

			if(y.intValue()  < min) {

				ssMin = arrayTipoSS[i].substring(2, arrayTipoSS[i].length());
				idMin = y1.intValue();
				min = y.intValue();

			}

			//Query para tipo de ss mas y menos usado
			String sql2 =	
					"select count(*) ";
			sql2+="from cita ";
			sql2+="where " +arrayTipoSS[i] + " >=  1 ";
			sql2+= "and fechainicio between ' "+ semana +" ' and " + " ' " + semana2 + " ' ";
			sql2+= "and fechafin between ' "+ semana +" ' and " + " ' " + semana2 + " ' ";

			//se ejecuta el query por cada ss
			Query q2 = pm.newQuery(SQL, sql2);
			List<BigDecimal> lista2 = q2.executeList();
			BigDecimal resultTipo = (BigDecimal) lista2.get(0);


			if(resultTipo.intValue() > max2) {

				max2 = resultTipo.intValue();
				ssMax2 = arrayTipoSS[i];

			}

			if(resultTipo.intValue() < min2) {

				min2 = resultTipo.intValue();
				ssMin2 = arrayTipoSS[i];

			}

		}


		String r = "";
		//System.out.println("Servicio de salud mas usado:");
		
		respuestaReq += "Servicio de salud mas usado:"+ "\n";

		//Query para obtener el ss de salud mas usado
		Query qSSMax = pm.newQuery(SQL, "select * from " + ssMax + " where id = " + idMax);
		List<Object[]> listSSMax = qSSMax.executeList();
		Object[] objectSSMax = listSSMax.get(0);

		if(ssMax.equals("terapia")) {

			r += "Terapia: " + objectSSMax[4] + " con id: " + idMax + " con una intensidad de: " + max;

		} else if(ssMax.equals("procedimientoesp")) {

			r += "Procedimiento especial con conocimiento en: " + objectSSMax[3] + " con id: " + idMax + " con una intensidad de: " + max;

		} else if(ssMax.equals("consulta")) {

			r += "Consulta" + objectSSMax[0] + " con id: " + idMax + " con una intensidad de: " + max;

		}

		//System.out.println(r);
		
		respuestaReq += r + "\n";

		r = "";

		//System.out.println("Servicio de salud menos usado:");
		
		respuestaReq += "Servicio de salud menos usado:" + "\n";

		//Query para obtener el ss de salud menos usado
		Query qSSMin = pm.newQuery(SQL, "select * from " + ssMin + " where id = " + idMin);
		qSSMin.setParameters(ssMin, idMin);
		Object[] objectSSMin = listSSMax.get(0);

		if(ssMax.equals("terapia")) {

			r += "Terapia: " + objectSSMin[4] + " con id: " + idMin + " con una intensidad de: " + min;

		} else if(ssMax.equals("procedimientoesp")) {

			r += "Procedimiento especial con conocimiento en: " + objectSSMin[3] + " con id: " + idMin + " con una intensidad de: " + min;

		} else if(ssMax.equals("consulta")) {

			r += "Consulta" + objectSSMin[0] + " con id: " + idMin + " con una intensidad de: " + min;

		}

		//System.out.println(r);
		
		respuestaReq += r + "\n" + "\n";

		//System.out.println();

		//Tipo de servicio mas usado
		//System.out.println("Tipo de servicio mas usado: " + ssMax2.substring(2,ssMax2.length()));
		respuestaReq += "Tipo de servicio mas usado: " + ssMax2.substring(2,ssMax2.length()) +"\n";

		//Tipo de servicio menos usado
		//System.out.println("Tipo de servicio menos usado: " + ssMin2.substring(2,ssMin2.length()));
		respuestaReq += "Tipo de servicio menos usado: " + ssMin2.substring(2,ssMin2.length()) +"\n"+ "\n";


		//System.out.println();

		//IPS mas y menos usada
		String sql3 = 
				"select t." +  "idips" + ", t.total ";
		sql3 += "from (SELECT " + "idips" + ", COUNT( "+ "idips" +" ) AS total ";
		sql3 += "FROM  cita ";
		sql3	+= "where fechainicio between ' "+ semana +" ' and " + " ' " + semana2 + " ' ";
		sql3	+= "and fechafin between ' "+ semana +" ' and" + " ' " + semana2 + " ' ";
		sql3 += "GROUP BY " + "idips";
		sql3 += " ORDER BY total DESC)t ";

		Query q3 = pm.newQuery(SQL, sql3);
		List<Object[]> lista3 = q3.executeList();

		Object[] objIPSMax = (Object[]) lista3.get(0);
		Object[] objIPSMin = (Object[]) lista3.get(lista3.size()-1);

		//Mas usada
		String sql4 =
				"select nombre ";
		sql4+="from ips ";
		sql4+="where id = " + objIPSMax[0];

		Query q4 = pm.newQuery(SQL, sql4);
		List<String> lista4 = q4.executeList();
		String nombreIPSMax = (String) lista4.get(0);

		//System.out.println("La IPS mas usada es: " + nombreIPSMax + " con id: " + objIPSMax[0] + " con cantidad de: " + objIPSMax[1]);
		respuestaReq += "La IPS mas usada es: " + nombreIPSMax + " con id: " + objIPSMax[0] + " con cantidad de: " + objIPSMax[1] + "\n";

		//Mas usada
		String sql5 =
				"select nombre ";
		sql5+="from ips ";
		sql5+="where id = " + objIPSMin[0];

		Query q5 = pm.newQuery(SQL, sql5);
		List<String> lista5 = q5.executeList();
		String nombreIPSMin = (String) lista5.get(0);

		//System.out.println("La IPS menos usada es: " + nombreIPSMin + " con id: " + objIPSMin[0] + " con cantidad de: " + objIPSMin[1]);
		respuestaReq += "La IPS menos usada es: " + nombreIPSMin + " con id: " + objIPSMin[0] + " con cantidad de: " + objIPSMin[1] + "\n" + "\n";
		
		//Afiliado que mas ha solicitado citas
		List<Object[]> lista6 = darUsuarioCitas(semana, semana2, "DESC", pm);
		Object[] usuarioIPS = (Object[]) lista6.get(0);

		BigDecimal idUsuario = (BigDecimal) usuarioIPS[0];
		BigDecimal cantCitasUsuario = (BigDecimal) usuarioIPS[1];

		//Se obtiene el nombre del usuario 
		String sql7 = 
				"select nombre ";
		sql7 += "from usuario_ips ";
		sql7 += "where numdocumento = " + idUsuario.intValue();

		Query q7 = pm.newQuery(SQL, sql7);
		List<String> lista7 = q7.executeList();
		String nombreUsuario = (String) lista7.get(0);

		//System.out.println();

		//System.out.println("El usuario con mas solicitudes de citas de ss es: " + nombreUsuario +" con id: " + idUsuario + " con " + cantCitasUsuario.intValue() + " citas pedidas");
		respuestaReq += "El usuario con mas solicitudes de citas de ss es: " + nombreUsuario +" con id: " + idUsuario + " con " + cantCitasUsuario.intValue() + " citas pedidas" + "\n";

		//Cantidad de afiliados que no han solicitado citas
	
	String sql8 =
		"select t.idusuarioips, t.total ";
		sql8+= "from (SELECT idusuarioips, COUNT( idusuarioips ) AS total ";
		sql8+="FROM  cita ";
		sql8+="where fechainicio between ' 1/1/2019 ' and ' 1/8/2019 ' ";
		sql8+="and fechafin between ' 1/1/2019 ' and ' 1/8/2019 ' ";
		sql8+="GROUP BY idusuarioips)t ";
		sql8+="where t.total = 0 ";
		
		Query q8 = pm.newQuery(SQL, sql8);
		List<Object[]> lista8 = q8.executeList();
		
		//System.out.println("La cantidad de usuarios de ips que no pidieron citas fue de: " + lista8.size());
		respuestaReq += "La cantidad de usuarios de ips que no pidieron citas fue de: " + lista8.size();
		
		return (respuestaReq);
	}



	public List<Object[]> darUsuarioCitas(String semana, String semana2, String orden, PersistenceManager pm) {

		String sql6 = 
				"select t." +  "idusuarioips" + ", t.total ";
		sql6 += "from (SELECT " + "idusuarioips" + ", COUNT( "+ "idusuarioips" +" ) AS total ";
		sql6 += "FROM  cita ";
		sql6 += "where fechainicio between ' "+ semana +" ' and " + " ' " + semana2 + " ' ";
		sql6 += "and fechafin between ' "+ semana +" ' and" + " ' " + semana2 + " ' ";
		sql6 += "GROUP BY " + "idusuarioips";
		sql6 += " ORDER BY total " + orden +")t ";
		sql6 += " where rownum = 1";

		Query q6 = pm.newQuery(SQL, sql6);
		List<Object[]> lista6 = q6.executeList();

		return lista6;
	}


}
