package src;
/**
 * Clase que maneja la distribucion de llegada de archivos de la computadora C. 
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */
public class DistribucionComputadoraC extends Distribucion {

	/* (non-Javadoc)
	 * @see src.Distribucion#generarLlegadaArchivo()
	 */
	@Override
	public double generarLlegadaArchivo() {
		double r = 0.0;
		int i = 0;
		while (i++ <= 12) {// suma de doce variables uniformes
			r += (double) (Math.random());
		}
		r -= 6.0;
		double x = 4 + (0.01 * r);
		return x;
	}

}
