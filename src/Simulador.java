package src;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
/**
 * Clase principal del programa, donde se maneja el esqueleto de la simulacion,
 * y se procesa cada franja de tiempo en la tabla de eventos, ademas se indican las variables
 * de la simulacion, con delay, sin delay, tiempo de token, entre otros. 
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */
public class Simulador {
	private static final int LLEGADACPU = 1;// correspondientes al switch de posibles eventos
	private static final int SALIDACPU = 2;
	private static final int LLEGADAANT = 3;
	private static final int SALIDAANT = 4;
	private static final int LLEGADAROU = 5;
	private static final int SALIDAROU = 6;
	private static final int CAMBIODETOKEN = 7;
	// parametros necesarios para la simulacion, tres CPU, router, antivirus, tablaeventos, salidas
	public Estadisticas estadisticas;
	Computadora cpuA;
	Computadora cpuB;
	Computadora cpuC;
	TablaEventos tablaEventos;
	private Antivirus antivirus;
	Router router;
	Logger logger;
	double token;// tiempo de token
	int tienetoken;// cual computadora tiene token
	double tiempodeAsignacionToken; //en cual tiempo T fue asignado el token por ultima vez, variable de estado. 
	private double[] estadisticasGlobales; //array que contiene las estadisticas de la corrida en orden
	private ArrayList<Double> listaTiemposArchivo; //lista con todos los tiempos de duracion de los arhivos en el sistema
	//para calcular el intervalo de 95% de confianza

	/**
	 * Inicializa el array de estadisticas globales, para el numero de corridas del 
	 * simulador
	 */
	public Simulador() {
		listaTiemposArchivo = new ArrayList<Double>();
		setEstadisticasGlobales(new double[9]);
	}
	/**
	 *Define el estado inical del sistema, antes de comenzar la simulacion 
	 * @param token tiempo de token para cada cpu
	 */
	public void definirEstadoInicial(double token){
		this.token = token;// token del constructor
		tienetoken = 0;// nadie tiene el tkoen todavia
		tiempodeAsignacionToken=0;
		
		logger = new Logger();// INICALIZACION DE PARAMETROS DE LA SIMULACION
		cpuA = new Computadora(new DistribucionComputadoraA(),token,1);
		cpuB = new Computadora(new DistribucionComputadoraB(),token,2);
		cpuC = new Computadora(new DistribucionComputadoraC(),token,3);
		setAntivirus(new Antivirus());
		router = new Router();
		tablaEventos = new TablaEventos();
		estadisticas = new Estadisticas();
		// establece el estado inicial, con un archivo en cada computadora para el tiempo 0
		Evento e  = new Evento(new Archivo(1));
		Evento e1 = new Evento(new Archivo(2));
		Evento e2 = new Evento(new Archivo(3));

		tablaEventos.agregarEvento(0, e);
		tablaEventos.agregarEvento(0, e1);
		tablaEventos.agregarEvento(0, e2);
		// evento con archivo NULO, que dispara el cambio de token de indefinido a la primera computadora
		tablaEventos.agregarEvento(15, new Evento());
	}
	/**
	 * Ejecuta una corrida de la simulacion entera, dependiendo de los parametros
	 * que se le pasen al metodo. Inicializa todas las variables de la clase, y al 
	 * final, exporta el txt con las salidas de la ejecución, y las estadisticas de
	 * la corrida.
	 * 
	 * @param token tiempo de token para cada cpu.
	 * @param segundos cantidad de segundos por los que ejecutar la corrida. 
	 * @param nombre nombre del archivo con que guardar la salida en .txt
	 * @param delay  variable que maneja si se corre la simulacion con o sin delay.
	 */
	public void correrSimulacion(double token, int segundos, String nombre, boolean delay){
		definirEstadoInicial(token);
		double siguiente = 0;// para controalr los ciclos de tiempo
		while (siguiente <= segundos) {// cantidad de segundos por corrida
			stepSimulador(siguiente,delay);
			siguiente = tablaEventos.getNext();// siguiente evento en la tabla
		}
		estadisticas.guardarResultados(estadisticas.devolverArrayEstadisticasCorrida(), logger);
		logger.exportarTxt(nombre);// despues de correr la simulacion por el tiempo y condiciones deseados, exporta los resultados de la corrida
		sumarArray(getEstadisticasGlobales(), estadisticas.devolverArrayEstadisticasCorrida());
	}
	/**
	 * Corre un ciclo de reloj con todos su eventos y devuelve las variables de 
	 * estado, solo se utiliza para la GUI. 
	 * 
	 * @param tActual, tiempo actual
	 * @param delay hay o no delay, boolean
	 * @return array de variables de estado
	 */
	public double[] stepSimulador(double tActual, boolean delay){
		if (delay) {// booleano para ver si se corre con delay o no
			try {
				TimeUnit.SECONDS.sleep(1);// delay de un segundo
			}catch (InterruptedException ex) {
			}
		}
		return timeSlice(tActual);// comienza en cero pero se actualiza constantemente
		
	}
	/**
	 * Toma la cola de eventos asociada al tiempo de reloj actual, y dependiendo de la
	 * naturaleza del evento, llama distintos metodos. Al final, borra la entrada de 
	 * la tabla de eventos, puesto que es un treemap por dentro, el orden de los eventos
	 * no se pierde. 
	 * @param tActual tiempo de reloj actual
	 */
	public double[] timeSlice(double tActual) {
		logger.guardar("----------------------------------------------------------------------------");
		logger.guardar("TIEMPO T " + tActual);
		logger.guardar("----------------------------------------------------------------------------");
		while (tablaEventos.getCola(tActual).size() > 0) {// mientras haya eventos queprocesar en la cola de ese tiempo
			Evento evento = tablaEventos.getCola(tActual).remove();// saca el evento actual, y lo envia al switch
			switch (evento.getEvento()) {// que tipo de evento de archivo es?
			case LLEGADACPU:
				procesarLlegadaCPU(evento, tActual);
				break;
			case SALIDACPU:
				procesarSalidaCPU(evento, tActual);
				break;
			case LLEGADAANT:
				procesarLlegadaANT(evento, tActual);
				break;
			case SALIDAANT:
				procesarSalidaANT(evento, tActual);
				break;
			case LLEGADAROU:
				procesarLlegadaROU(evento, tActual);
				break;
			case SALIDAROU:
				procesarSalidaRou(evento, tActual,listaTiemposArchivo);
				break;
			case CAMBIODETOKEN:
				cambiarToken(tActual);
				procesarCambioToken(tActual);
			default:
				break;
			}
		}
		tablaEventos.eliminarColaEventos(tActual);
		estadisticas.aumentarNumTimeSlice();
		estadisticas.contarTamanoColasCPU(cpuA.getColaPrioridad().size(),cpuB.getColaPrioridad().size(),cpuC.getColaPrioridad().size(),getAntivirus().getCola().size());
		
		double tiempoRestanteToken = (tienetoken==0?0:token-(tActual-tiempodeAsignacionToken));
		double[] variablesdeEstado = {
		tActual,cpuA.getColaPrioridad().size(),cpuA.getNumPrioridadUno(),cpuA.getNumPrioridadDos(),
		cpuB.getColaPrioridad().size(),cpuB.getNumPrioridadUno(),cpuB.getNumPrioridadDos(),
		cpuC.getColaPrioridad().size(),cpuC.getNumPrioridadUno(),cpuC.getNumPrioridadDos(),
		getAntivirus().getCola().size(),tienetoken,tiempoRestanteToken,router.getNumServ()
		};
		return variablesdeEstado;
	}
	/**
	 * El metodo se utiliza unicamente para dar un array de todas las variables de estado
	 * incluyendo el evento que se esta procesando actualmente, solo es utilizado en la interfaz
	 * puesto que la simulacion puede ser inicializada de una manera mas sencilla.
	 * @param tActual tiempo actual
	 * @return array de variables de estado para cada tipo de evento a procesar
	 */
	public double[] eleccionEventos(double tActual) {
		Evento evento = tablaEventos.getCola(tActual).remove();// saca el evento actual, y lo envia al switch
		double tipoEvento = evento.getEvento();
		switch (evento.getEvento()) {// que tipo de evento de archivo es?
		case LLEGADACPU:
			procesarLlegadaCPU(evento, tActual);
			break;
		case SALIDACPU:
			procesarSalidaCPU(evento, tActual);
			break;
		case LLEGADAANT:
			procesarLlegadaANT(evento, tActual);
			break;
		case SALIDAANT:
			procesarSalidaANT(evento, tActual);
			break;
		case LLEGADAROU:
			procesarLlegadaROU(evento, tActual);
			break;
		case SALIDAROU:
			procesarSalidaRou(evento, tActual,listaTiemposArchivo);
			break;
		case CAMBIODETOKEN:
			cambiarToken(tActual);
			procesarCambioToken(tActual);
		default:
			break;
		}
		double tiempoRestanteToken = (tienetoken==0?0:token-(tActual-tiempodeAsignacionToken));
		double[] variablesdeEstado = {
		tActual,cpuA.getColaPrioridad().size(),cpuA.getNumPrioridadUno(),cpuA.getNumPrioridadDos(),
		cpuB.getColaPrioridad().size(),cpuB.getNumPrioridadUno(),cpuB.getNumPrioridadDos(),
		cpuC.getColaPrioridad().size(),cpuC.getNumPrioridadUno(),cpuC.getNumPrioridadDos(),
		getAntivirus().getCola().size(),tienetoken,tiempoRestanteToken,router.getNumServ(),tipoEvento
		};
		return variablesdeEstado;
	}

	/**
	 * Metodo que apenas hace que una cpu envie un archivo apenas reciba un token,
	 * para no perder la continuidad de la simulacion. 
	 * @param tiempoactual tiempo de reloj donde ocurre
	 */
	private void procesarCambioToken(double tiempoactual) {// se llama inmediatamente despues de un cambio de token
		switch (tienetoken) {// para no perder continuidad en la simulacion, puesto que la computadora que recibe el token
		case 1:// ya tiene el derecho a enviar apenas suceda
			cpuA.procesarEnvio(tiempoactual, tablaEventos, logger);
			break;
		case 2:
			cpuB.procesarEnvio(tiempoactual, tablaEventos, logger);
			break;
		case 3:
			cpuC.procesarEnvio(tiempoactual, tablaEventos, logger);
			break;
		default:
			break;
		}

	}

	/**
	 * Llama al metodo que procesa una salida del router, con los parametros que ocupa. 
	 * @param evento evento a procesar
	 * @param tActual tiempo de reloj en el que ocurre
	 */
	private void procesarSalidaRou(Evento evento, double tActual, ArrayList<Double> listaTiempos) {// llama al metodo de procesar salidas del router
		router.procesarSalida(evento, tActual, logger, estadisticas, listaTiempos);
	}

	/**
	 * Llama al metodo que procesa una llegada router, con los parametros que ocupa.
	 * @param evento evento a procesar
	 * @param tActual tiempo de reloj en el que ocurre
	 */
	private void procesarLlegadaROU(Evento evento, double tActual) {// llama al metodo de procesar llegadas al  router
		router.procesarLlegada(evento, tActual, tablaEventos, logger);
	}

	/**
	 * Llama al metodo que procesa una salida del antivirus, con los parametros que ocupa.
	 * @param evento evento a procesar
	 * @param tActual tiempo de reloj en el que ocurre
	 */
	private void procesarSalidaANT(Evento evento, double tActual) {// llama al metodo de procesar salidas del antivirus
		getAntivirus().procesarSalida(evento, tActual, tablaEventos, logger, router);
	}

	/**
	 * Llama al metodo que procesa una llegada al antivirus, con los parametros que ocupa.
	 * @param evento evento a procesar
	 * @param tActual tiempo de reloj en el que ocurre
	 */
	private void procesarLlegadaANT(Evento evento, double tActual) {// llama al metodo de procesar llegadas en el antivirus
		getAntivirus().procesarLlegada(evento, tActual, tablaEventos, logger);
	}

	/**
	 * Dependiendo de si la computadora o no tiene el token, se le permite enviar
	 * archivos, llamando al metodo encargado de eso para cada cpu, si no, se
	 *  mantiene esperando hasta que se pueda.
	 * @param evento evento a procesar
	 * @param tActual tiempo de reloj en el que ocurre
	 */
	private void procesarSalidaCPU(Evento evento, double tActual) {
		switch (tienetoken) {// dependiendo de cual computadora tiene el token
		case 1:// se le permite enviar sus archivos
			cpuA.procesarSalida(evento, tablaEventos, tActual, logger, estadisticas);
			break;
		case 2:
			cpuB.procesarSalida(evento, tablaEventos, tActual, logger, estadisticas);
			break;
		case 3:
			cpuC.procesarSalida(evento, tablaEventos, tActual, logger, estadisticas);
			break;
		default:
			break;
		}
	}

	/**
	 * Llama al metodo que procesa una llegada a cualquier cpu, con los parametros que ocupa.
	 * @param evento evento a procesar
	 * @param tActual tiempo de reloj en el que ocurre
	 */
	private void procesarLlegadaCPU(Evento evento, double tActual) {// dependiendo de a que CPU pertenezca el archivo que viene llegando
		switch (evento.getArchivo().getCpu()) {// se procesa la llegada al CPU con los metodos de la calse computadora
		case 1:
			cpuA.procesarLlegada(evento, tablaEventos, tActual, logger);
			break;
		case 2:
			cpuB.procesarLlegada(evento, tablaEventos, tActual, logger);
			break;
		case 3:
			cpuC.procesarLlegada(evento, tablaEventos, tActual, logger);
			break;
		default:
			break;
		}

	}

	/**
	 * Cambia la variable tienetoken, dependiendo de a cual computadora le corresponda ahora. 
	 */
	private void cambiarToken(double tiempoactual) {// dependiendo de donde esta, lo mueve de forma circular
		estadisticas.aumentarNumToken();
		switch (tienetoken) {// de A a B, de B a C, de C a A, y asi  sucesivamente
		case 0:// estado inicial del simulador
			tienetoken++;
			cpuA.setToken(this.token);
			break;
		case 1:
			tienetoken++;
			cpuB.setToken(this.token);
			break;
		case 2:
			tienetoken++;
			cpuC.setToken(this.token);
			break;
		case 3:
			tienetoken = 1;
			cpuA.setToken(this.token);
			break;
		default:
			break;
		}
		logger.guardar("TOKEN AHORA EN: " + tienetoken);
		tiempodeAsignacionToken=tiempoactual;

	}

	/**
	 * Metodo que facilita la impresion de resultados en la GUI.
	 * @return devuelve el atributo logger de la clase. 
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Se utiliza para mantener control de las estadisticas por corrida, y las globales. 
	 * @return devuelve las estadisticas de la corrida actual. 
	 */
	public double[] getEstadisticasCorrida() {
		return getEstadisticasGlobales();
	}
	
	public TablaEventos getTablaEventos(){
		return tablaEventos;
	}
	/**
	 * El metodo calcula las estadisticas globales del sistema, mediante la division
	 * del array que guarda la suma de las estadisticas de cada corrida, entre el numero
	 * de corridas del simulador.  
	 * 
	 * @param numCorridas numero de corridas dentro del simulador S
	 * @return el array conteniendo las estadisticas globales de la corrida
	 */
	public double[] calcularEstadisticasGlobales(double numCorridas){
		for (int i = 0; i < estadisticasGlobales.length; i++) {
			double temporal=(double)(estadisticasGlobales[i] / numCorridas);
			estadisticasGlobales[i] = temporal;
		}	
	return estadisticasGlobales;
	}
	/**
	 * suma dos arrays a y b, y los guarda en a. 
	 * @param a cualquier array de double
	 * @param b cualquier array de double
	 */
	
	public void sumarArray(double[] a, double[] b) {
		for (int i = 0; i < a.length; i++) {
			try {
				a[i] += b[i];
			} catch (IndexOutOfBoundsException e) {
			}
		}
		
	}

	/**
	 * Transforma el atributo lista de la clase Simulador con todos los tiempos de duracion
	 * de archivos que han salido del sistema exitosamente, y lo transforma a un array para
	 * calcular el intervalo de 95% de la media. 
	 * 
	 * @return string con el intervalo de confianza al 95%
	 */
	public String calcularIntervalo95(){
		String intervalo="";
		Object[]listaTiempos= listaTiemposArchivo.toArray();
		double suma=0.0;
		double mediaMuestral=0.0;
		double varianzaMuestral=0.0;
		double desviacionEstandarMuestra=0.0;
		//calcula la media muestral
		for (int i = 0; i < listaTiempos.length; i++) {
			suma += (double)listaTiempos[i];
		}
		mediaMuestral=(double)(suma/listaTiempos.length);
		for (int i = 0; i < listaTiempos.length; i++) {
			varianzaMuestral += Math.pow(((double)listaTiempos[i] - mediaMuestral),2);
		}
		varianzaMuestral=(double)(varianzaMuestral/listaTiempos.length-1);
		desviacionEstandarMuestra=Math.sqrt(varianzaMuestral);
		
		intervalo+="["+(mediaMuestral - 1.96 * desviacionEstandarMuestra)+" , "+(mediaMuestral + 1.96 * desviacionEstandarMuestra)+"]";
		return intervalo;
	}
	
	public double[] getEstadisticasGlobales() {
		return estadisticasGlobales;
	}
	public void setEstadisticasGlobales(double[] estadisticasGlobales) {
		this.estadisticasGlobales = estadisticasGlobales;
	}
	public Computadora getCpuA(){
		return cpuA;
	}
	public Computadora getCpuB(){
		return cpuB;
	}
	public Computadora getCpuC(){
		return cpuC;
	}
	public Antivirus getAntivirus() {
		return antivirus;
	}
	public void setAntivirus(Antivirus antivirus) {
		this.antivirus = antivirus;
	}
}