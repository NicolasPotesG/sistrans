package uniandes.isis2304.epsandes.negocio;

public class UsuarioIPS implements VOUsuarioIPS
{
	
	private String nombre;
	
	private String estado;
	
	private long numDocumento;
	
	private int tipoDocumento;
	
	private String fechaNacimiento;
	
	private String esAfiliado;
	
	private String correo;
	
	private long idEPS;
	
	private int edad;
	
	private String genero;
	

	/**
     * Constructor por defecto
     */
	public UsuarioIPS() {
		
    	nombre = "";
    	estado = "";
    	numDocumento = 0;
    	tipoDocumento = 0;
    	fechaNacimiento = "";
    	idEPS = 0;
    	esAfiliado = "";
    	correo = "";
    	edad = 0;
    	genero = "";
		
		
	}
	
	
	/**
	 * Constructor con valores de un usuario de IPS
	 */
    public UsuarioIPS(String nombre, String estado, Long numDocumento, int tipoDocumento, String fechaNacimiento, long idEPS, String esAfiliado, String correo, int edad, String genero) 
    {
    	
    	this.nombre = nombre;
    	this.estado = estado;
    	this.numDocumento = numDocumento;
    	this.tipoDocumento = tipoDocumento;
    	this.fechaNacimiento = fechaNacimiento;
    	this.idEPS = idEPS;
    	this.esAfiliado = esAfiliado;
    	this.correo = correo;
    	this.edad = edad;
    	this.genero = genero;
		
	}


	@Override
	public String getEstado() {
		// TODO Auto-generated method stub
		return estado;
	}

	@Override
	public String getNombre() {
		// TODO Auto-generated method stub
		return nombre;
	}

	@Override
	public String getFechaNacimiento() {
		// TODO Auto-generated method stub
		return fechaNacimiento;
	}

	@Override
	public long getNumDocumento() {
		// TODO Auto-generated method stub
		return numDocumento;
	}

	@Override
	public int getTipoDocumento() {
		// TODO Auto-generated method stub
		return tipoDocumento;
	}

	@Override
	public String getEsAfiliado() {
		// TODO Auto-generated method stub
		return esAfiliado;
	}
	
	
	@Override
	public Long getIdEPS() {
		// TODO Auto-generated method stub
		return idEPS;
	}



	@Override
	public int getEdad() {
		// TODO Auto-generated method stub
		return edad;
	}


	@Override
	public String getGenero() {
		// TODO Auto-generated method stub
		return genero;
	}


	@Override
	public String toString() {
		return "UsuarioIPS [nombre=" + nombre + ", estado=" + estado + ", numDocumento=" + numDocumento
				+ ", tipoDocumento=" + tipoDocumento + ", fechaNacimiento=" + fechaNacimiento + ", esAfiliado="
				+ esAfiliado + ", correo=" + correo + ", idEPS=" + idEPS + ", edad=" + edad + ", genero=" + genero
				+ "]";
	}
	

}