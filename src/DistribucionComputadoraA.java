package src;

/**
 * Clase que maneja la distribucion de llegada de archivos de la computadora A. 
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */

public class DistribucionComputadoraA extends Distribucion {
	/* (non-Javadoc)
	 * @see src.Distribucion#generarLlegadaArchivo()
	 */
	@Override
	public double generarLlegadaArchivo() {
		double r = (double) (Math.random());
		double x = -5 * Math.log(1 - r);
		return x;		
	}

}
