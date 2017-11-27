package src;

import java.util.ArrayList;

/**
 * Clase que maneja todo lo relacionado con el modulo del router, y sus funcionalidades.  
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */

public class Router {
	private int numServ;// variable de estado, numero de servidores disponibles,
	private double salidaMasCercana;// maneja la salida mas proxima del router

	/**
	 *Constructor que inicializa el numero de servidores libres, y la salida mas 
	 *proxima del router.  
	 */
	public Router() {
		setNumServ(2);// dos hilos libres
		setSalidaMasCercana(-1);// no hay salidas todavia
	}

	/**
	 * El metodo establece la llegada de una archivo, reduce el numero de servidores,
	 * y se calcula el tiempo de servicio y salida del mismo. Puesto que el antivirus
	 * no envia archivos a menos que el router tenga al menos uno de sus hilos libres,
	 * el chequeo de si hay servidores antes de dar servicio esta de mas. 
	 * 
	 * @param evento el evento con el archivo que llega al router.
	 * @param tiempoactual tiempo en el que llega
	 * @param tablaEventos tabla en donde guardar acciones posteriores
	 * @param logger instancia donde guardar salidas de datos
	 */
	public void procesarLlegada(Evento evento, double tiempoactual, TablaEventos tablaEventos, Logger logger) {	
		evento.getArchivo().setLlegadaROU(tiempoactual);// pone la llegada al router como el momento en que se llama el metodo
		this.numServ--;// reduce el numero de servidores disponibles
		double salida = tiempoactual + (double) tiempoSalidaRouter(evento.getArchivo().getTamano());// calcula el tiempo de servicio
		tablaEventos.agregarEvento(salida, evento);// agrega la salida a la tabla
		actSalidaMasCercana(salida, tiempoactual);// actualiza si la salida del evento que acaba de llegar es mas pronto que la mas proxima en el router
		logger.guardar("ARCHIVO LLEGO AL ROUTER, SALIDA ESTIMADA PARA T " + salida);// se guarda la salida de texto
	}

	/**
	 * Este metodo establece la salida del archivo del sistema, y libera uno de los hilos del router
	 * posteriormente, guarda las estadisticas del archivo y actualiza las estadisticas globales.
	 *  
	 * @param evento evento con el archivo que sale del sistema
	 * @param tiempoactual tiempo en el que ocurre
	 * @param logger instancia para las salidas de texto y consola
	 * @param listaTiempos lista de tiempos de archivos para calcular el intervalo de 95%
	 * @param estadisticas instancia donde se efectuan calculos de estadisticas del sistema
	 */
	public void procesarSalida(Evento evento, double tiempoactual, Logger logger, Estadisticas estadisticas, ArrayList<Double> listaTiempos) {
		evento.getArchivo().setSalidaROU(tiempoactual);// establece la salida del router como el momento en  que se llama el metodo
		this.numServ++;// devuelve la cantidad de servidores libres
		Archivo ar = evento.getArchivo();
		// estadisticas del archivo que recien salio del sistema
		logger.guardar("ARCHIVO SALE DEL ROUTER A LA LINEA, ESTADISTICAS DEL ARCHIVO:");
		logger.guardar("\t PERMANENCIA EN EL SISTEMA: " + (ar.getSalidaROU() - ar.getLlegadaCPU()));
		logger.guardar("\t PROVENIENTE DE COMPUTADORA: " + ar.getCpu());
		logger.guardar("\t TAMANO DEL ARCHIVO: " + ar.getTamano());
		logger.guardar("\t PRIORIDAD DEL ARCHIVO: " + ar.getPrioridad());
		logger.guardar("\t APROBADO TRAS LA REVISION NUMERO: " + ar.getIntentoDescarto() + " EN EL ANTIVIRUS");
		estadisticas.revisarArchivo(ar);
		listaTiempos.add((ar.getSalidaROU() - ar.getLlegadaCPU()));
	}

	/**
	 * Revisa si la nueva salida es mas proxima que la que ya era la mas cercana, para asi
	 * actualizar la variable. 
	 * 
	 * @param nuevaSalida nueva salida del router
	 * @param tiempoActual tiempoa actual
	 */
	public void actSalidaMasCercana(double nuevaSalida, double tiempoActual) {// si la nueva salida es mucho mas pronto que la 
		//antigua salida mas cercana, tomando en cuenta el tiemmpo actual, se cambian, si no, se mantiene asi
		salidaMasCercana = (salidaMasCercana < nuevaSalida && salidaMasCercana > tiempoActual ? salidaMasCercana : nuevaSalida);
	}

	/**
	 * @return
	 */
	public boolean revisarEstado() {// devuelve libre u ocupado, dependiendo de cuantos servidores disponibles hayan
		return (numServ > 0 ? true : false);
	}

	// SETTERS Y GETTERS
	/**
	 * @param numServ
	 */
	public void setNumServ(int numServ) {
		this.numServ = numServ;
	}

	public int getNumServ() {
		return numServ;
	}

	/**
	 * @return
	 */
	public double getSalidaMasCercana() {
		return salidaMasCercana;
	}

	/**
	 * @param salidaMasCercana
	 */
	public void setSalidaMasCercana(int salidaMasCercana) {
		this.salidaMasCercana = salidaMasCercana;
	}

	/**
	 * @param m tamano del archivo
	 * @return tiempo de la revision del archivo
	 */
	public double tiempoSalidaRouter(double m) {
		return (m / 64);// tiempo de revision en el router
	}

}
