package src;
import org.omg.CORBA.Environment;

/**
 * Clase que maneja todo lo relacionado con estadisticas de las corridas. 
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */
@SuppressWarnings("unused")
public class Estadisticas {
	private int numTimeSlice;//suma la cantidad de tiempos en la tabla de eventos, por corrida
	private double colaA; //tamano de la cola A
	private double colaB; //tamano de la cola B
	private double colaC; //tamano de la cola C
	private double colaAnt;//tamano de la cola del antivirus
	private double numArchivos;//numero de archivos que han atravesado el sistema sin ser descartados
	private double numPrioridadUno;//cuales de esos son prioridad uno
	private double numPrioridadDos;//cuales de esos son prioridad dos
	private double permanenciaTotal;//suma la permanencia de todos los archivos
	private double permanenciaPrioridadUno;//de los de prioridad uno
	private double permanenciaPrioridadDos;//de los de prioridad dos
	private double revisionesArchivos;//suma cuantas revisiones de arhivo se han hecho, por cada uno
	private double numeroTokens; //suma cuantos cambios de token ha habido
	private double archivosEnviados;//suma cuantos archivos han sido enviados desde las CPU, descartados o no

	/**
	 * Cuando se hace una isntancia, se inicalizan todas las variables que se ocupan
	 * para manejar las estadisticas de cada corrida. 
	 */
	public Estadisticas() {
		numTimeSlice = 0;
		colaA = 0;
		colaB = 0;
		colaC = 0;
		colaAnt = 0;
		numArchivos = 0;
		numPrioridadUno = 0;
		numPrioridadDos = 0;
		permanenciaTotal = 0;
		permanenciaPrioridadUno = 0;
		permanenciaPrioridadDos = 0;
		revisionesArchivos = 0;
		archivosEnviados = 0;
		numeroTokens = 0;
	}

	/**
	 * Cuando un archivo marca su salida en el router, se actualizan las variables de la clase
	 * como numArchivos, permanenciaTotal,permanenciaUno,permanenciaDos y revisiones por archivo
	 * @param archivo
	 */
	public void revisarArchivo(Archivo archivo) {
		double permanencia = (archivo.getSalidaROU() - archivo.getLlegadaCPU());
		numArchivos++;
		permanenciaTotal += permanencia;
		if (archivo.getPrioridad() == 1) {
			numPrioridadUno++;
			permanenciaPrioridadUno += permanencia;
		} else {
			numPrioridadDos++;
			permanenciaPrioridadDos += permanencia;
		}
		revisionesArchivos += archivo.getIntentoDescarto();

	}

	/**
	 * Auemta el contador de timeslices de la corrida
	 */
	public void aumentarNumTimeSlice() {
		numTimeSlice++;
	}
	
	/**
	 * Actualiza el tamano de las colas en promedio, por cada time slice.
	 * @param a tamano de la cola A para un timeslice
	 * @param b tamano de la cola B para un timeslice
	 * @param c tamano de la cola C para un timeslice
	 * @param ant tamano de la cola del antivirus para un timeslice
	 * 
	 */
	public void contarTamanoColasCPU(int a,int b, int c,int ant){
		colaA += a;
		colaB += b;
		colaC += c;
		colaAnt += ant;
	}


	/**
	 * Aumenta el numero de cambios de token
	 */
	public void aumentarNumToken() {
		numeroTokens++;
	}

	/**
	 * Aumenta la cantidad de arhivos enviados, sean o no rechazados
	 */
	public void aumentarArchivosEnviados() {
		archivosEnviados++;
	}

	/**
	 * Genera un array con todas las estadisticas necesarias de cada corrida, ordenadas.
	 * @return array de estadisticas de la corrida. 
	 */
	public double[] devolverArrayEstadisticasCorrida() {
		double promColaA 	  = (double) (colaA / numTimeSlice);
		double promColaB 	  = (double) (colaB / numTimeSlice);
		double promColaC 	  = (double) (colaC / numTimeSlice);
		double promColaAnt 	  = (double) (colaAnt / numTimeSlice);
		double permGeneral 	  = (double) (permanenciaTotal / numArchivos);
		double permUno 		  = (double) (permanenciaPrioridadUno / numPrioridadUno);
		double permDos 		  = (double) (permanenciaPrioridadDos / numPrioridadDos);
		double envToken 	  = (double) (archivosEnviados / numeroTokens);
		double promRevisiones = (double) (revisionesArchivos / numArchivos);

		double[] estFinales = { promColaA, promColaB, promColaC, promColaAnt, permGeneral, permUno, permDos, envToken,promRevisiones };
		return estFinales;

	}

	/**
	 * El metodo recibe un array e imprime cada uno de sus miembros, segun un orden
	 * especifico. 
	 * 
	 * @param a un array que imprimir en el orden de las inserciones en el logger
	 * @param logger genera salidas en consola y .txt
	 */
	public void guardarResultados(double[] a, Logger logger) {
		double[] estadisticas = a;
		logger.imprimir("----------------------------------------------------------------------------");
		logger.imprimir("ESTADISTICAS FINALES:");
		logger.imprimir("----------------------------------------------------------------------------");
		for (int i = 0; i < estadisticas.length; i++) {
			switch (i) {
			case 0:
				logger.imprimir("EL TAMANO PROMEDIO DE LA COLA DE LA CPU 1 ES: " + estadisticas[i]);
				break;
			case 1:
				logger.imprimir("EL TAMANO PROMEDIO DE LA COLA DE LA CPU 2 ES: " + estadisticas[i]);
				break;
			case 2:
				logger.imprimir("EL TAMANO PROMEDIO DE LA COLA DE LA CPU 3 ES: " + estadisticas[i]);
				break;
			case 3:
				logger.imprimir("EL TAMANO PROMEDIO DE LA COLA DEL ANTIVIRUS ES: " + estadisticas[i]);
				break;
			case 4:
				logger.imprimir("EL TIEMPO PROMEDIO DE PERMANENCIA DE UN ARCHIVO EN GENERAL ES: " + estadisticas[i]);
				break;
			case 5:
				logger.imprimir("EL TIEMPO PROMEDIO DE PERMANENCIA DE UN ARCHIVO DE PRIORIDAD 1 ES: " + estadisticas[i]);
				break;
			case 6:
				logger.imprimir("EL TIEMPO PROMEDIO DE PERMANENCIA DE UN ARCHIVO DE PRIORIDAD 2 ES: " + estadisticas[i]);
				break;
			case 7:
				logger.imprimir("EL PROMEDIO DE ARCCHIVOS ENVIADOS POR CADA TURNO DE TOKEN ES: " + estadisticas[i]);
				break;
			case 8:
				logger.imprimir("EL PROMEDIO DE REVISIONES DEL ANTIVIRUS POR ARCHIVO ES: " + estadisticas[i]);
				break;

			default:
				break;
			}
		}
	}


}