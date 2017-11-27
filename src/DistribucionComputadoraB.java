package src;

/**
 * Clase que maneja la distribucion de llegada de archivos de la computadora B. 
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */
public class DistribucionComputadoraB extends Distribucion {
	/* (non-Javadoc)
	 * @see src.Distribucion#generarLlegadaArchivo()
	 */
	@Override
	public double generarLlegadaArchivo() {
		double r = (float) (Math.random());
		double x = Math.sqrt(80 * r);
		return x;
	}

}
