package src;
/**
 * Clase abstracta para el polimorfismo de distribuciones de envio
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */

public abstract class Distribucion {
	/**
	 * Metodo polimorfico
	 * 
	 * @return la llegada del archivo, dependiendo de la herencia que se elija. 
	 */
	public abstract double generarLlegadaArchivo();
}
