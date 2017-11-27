package src;
/**
 * Clase que maneja los eventos, y sus tipos. 
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */

public class Evento {

	private Archivo archivo; //cada evento tiene un archivo

	/**
	 * Constructor nulo de evento, unicamente se utiliza cuando hay un cambio de token
	 */
	public Evento() {
		this.archivo = null;
	}

	/**
	 * Constructor con archivo, para guiar su paso por el sistema con cada evento del mismo.
	 * @param archivo el archivo que va a generar eventos
	 */
	public Evento(Archivo archivo) {
		this.archivo = archivo;
	}

	/**
	 * @return
	 */
	public Archivo getArchivo() {
		return archivo;
	}

	/**
	 * @param archivo
	 */
	public void setArchivo(Archivo archivo) {
		this.archivo = archivo;
	}

	/**
	 * Metodo que devuelve el tipo de evento que corresponde, dependiendo de cual corresponda
	 * al archivo. El unico caso donde exista un evento sin archivo, es porque es nulo, y a su 
	 * vez indica cambio de token
	 * @return devuelve un numero identificador de eventos. 
	 */
	public int getEvento() {
		int tipoEvento = 0;
		try {
			tipoEvento = archivo.getEventoActual();
		} catch (NullPointerException e) {
			tipoEvento = 7;
		}
		return tipoEvento;
	}
}
