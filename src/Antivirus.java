package src;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Clase que maneja todas las acciones que corresponden al modulo del antivirus
 *
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 *
 */
public class Antivirus {

	private boolean libre; // estado del atirvirus, ocupado o no, depende de la cola
	private Queue<Evento> Cola; // cola de espera

	/**
	 * El constructor de la clase antivirus comienza poniendo el antivirus en libre, no 
	 * esta procesando archivos en ese momento, e inicializa la cola de eventos que maneja.
	 */
	public Antivirus() {
		setLibre(true);// comienza libre y con cola vacia
		setCola(new LinkedList<Evento>());
	}

	/**
	 * El metodo recibe una llegada, de un evento, con el tiempo en el que llega,
	 * y comienza a preguntar priemro, si la cola del antivirus NO contiene el elemento,
	 * de ser aso. agrega la llegada al antivirus del archivo que llega por medio de evento,
	 * y luego agrega el elemento a la cola, posteriormente, si el antivirus esta libre, 
	 * envia el archivo a procesar su ciclo de servicio en el antivirus. 
	 * Es importante destacar, que si bien agrega todos los archivos a la cola del antivirus,
	 * el unico caso donde no deberia agregarlo a la cola, es cuando el archivo llega
	 * por primera vez y el antivirus esta libre, sin embargo, de ser asi, se mete y saca
	 * de la cola en el mismo tiempo, siendo esta transicion despreciable. 
	 * 
	 * @param evento El objeto de tipo evento que llega al antivirus.
	 * @param tiempoactual El tiempo actual de reloj en el que llega.
	 * @param tablaEventos La tabla de eventos en donde guardar los 
	 * proximos eventos del archivo dependiendo de lo que suceda en el 
	 * antivirus.
	 * @param logger La clase de salidas que se utiliza para generar 
	 * la salida en .txt de la corrida.
	 */
	public void procesarLlegada(Evento evento, double tiempoactual, TablaEventos tablaEventos, Logger logger) {
		logger.guardar("TAMANO DE LA COLA DEL ANTIVIRUS: " + getCola().size());
		if (!getCola().contains(evento)) {
			logger.guardar("ARCHIVO DE LA LINEA LLEGO AL ANTIVIRUS");
			evento.getArchivo().setLlegadaANT(tiempoactual);// agrega su llegada
			getCola().add(evento);
		}
		if (libre) {
				logger.guardar("ARCHIVO DE LA COLA ENTRANDO A REVISION DE ANTIVIRUS");
				entradaAntivirus(getCola().remove(), tiempoactual, tablaEventos, logger);
			}
	}

	/**
	 * Primero, se establece el antivirus como ocupado, y se llama al metodo
	 * que calcula su tiempo de revision, por ultimo, solo se agrega la salida del archivo
	 * a la tabla de eventos, mientras el antivirus permanece ocupado. 
	 * 
	 * @param evento el evento que entra a ser procesado en el antivirus
	 * @param tiempoactual el tiempo en el que entra dicho evento
	 * @param tablaEventos la tabla en donde guardar los eventos posteriores del mismo
	 * @param logger la salida en donde guardar las acciones de dicho evento
	 */
	public void entradaAntivirus(Evento evento, double tiempoactual, TablaEventos tablaEventos, Logger logger) {// metodo que trata el proceso de llegada en si
		setLibre(false);
		double tiempoSalida  = tiempoRevisionArchivo(evento, tiempoactual);
		logger.guardar("ARCHIVO ENTRANDO A REVISION EN EL ANTIVIRUS");
		tablaEventos.agregarEvento(tiempoSalida, evento);// agrega el evento a su respectivo momento
	}

	/**
	 * Comienza preguntando, si despues de la revision, el archivo que se envio
	 * ya esta descartado, de ser asi, lo desecha, libera el antivirus, y procesa la
	 * siguiente llegada, por otro lado, si no esta desechado, revisa si el router
	 * tiene alguna linea de transmision disponible, de ser asi, libera el antivirus
	 * y envia el archivo, agergando la salida y el evento de llegada al router(tiempo de 0),
	 * si no hay lineas disponibles, el antivirus se mantiene ocupado, y agrega la salida
	 * del archivo al momento en donde se libere la primera linea de transmision del router.
	 * Para darle continuidad a la tasa salida/servicio, y que sea un ciclo, ejecuta la llegada
	 * del proximo archivo al sistema, unicamente si esta libre, y hay algun archivo en cola-
	 * 
	 * @param evento El evento que define la salida
	 * @param tiempoactual tiempo en el que ocurre
	 * @param tablaEventos tabla en donde guardar los proximos eventos
	 * @param logger clase que registra las salidas de texto de la corrida
	 * @param router router a donde seran enviados los archivos
	 */
	public void procesarSalida(Evento evento, double tiempoactual, TablaEventos tablaEventos, Logger logger, Router router) {
		if (evento.getArchivo().getIntentoDescarto() > 3) {// si el que llego es un evento de archivo descartado
			setLibre(true);// solo se libera el antivirus, y no se vuelve a insertar dicho archivo en la tabla
			logger.guardar("ARCHIVO DESECHADO TRAS EL TERCER INTENTO");
		} else {
			logger.guardar("ARCHIVO ACEPTADO POR EL ANTIVIRUS");
			if (router.revisarEstado()) {// si hay hilos disponibles en el router
				setLibre(true);// se libera el antivirus
				evento.getArchivo().setSalidaANT(tiempoactual);// se le da una salida al archivo que logro salir del  antivirus
				tablaEventos.agregarEvento(tiempoactual, evento);// se agrega a la tabla la llegada de dicho archivo al router, como el tiempo es despreciable, es a ese mismo instante
			} else {// si no hay campo en el router, se agrega despues de la salida mas proxima del router
				tablaEventos.agregarEvento(router.getSalidaMasCercana(), evento);
			}
		}
		if (isLibre() && getCola().peek() != null) {// revisa si hay archivos en cola, pero no lo saca, puesto que eso se hace en el procesarLlegada
			procesarLlegada(getCola().peek(), tiempoactual, tablaEventos, logger);// por ultimo,apenas se dio un envio al router, se vuelve a revisar
		} // servicio para que el proceso sea acorde a los tiempos
	}

	/**
	 * El metodo calcula la duracion de la revision del archivo, dependiendo de
	 * en cual intento de revision fue descartado, calcula dicho evento, si es 
	 * superior a 3, le agrega su tiempo de revision como si fuera revisado por
	 * tercera vez, pero despues de eso, el archivo se descarta mas adelante. 
	 * 
	 * @param evento el evento cuyo archivo se revisa
	 * @param tiempoActual el tiempo actual
	 * @return
	 */
	public double tiempoRevisionArchivo(Evento evento,double tiempoActual) {// metodo que calcula los intentos que tiene un archivo antes de ser descartado o aprobado
		double tiempoRevision = 0.0;
		int intento = evento.getArchivo().getIntentoDescarto();// ningun intento  probado todavia
		int tamano = evento.getArchivo().getTamano();// tamano del archivo
		do {// el ciclo se hace una vez
			tiempoRevision += tiempoEnvioRouter(tamano, intento);//
			intento++;// se aumentan los intentos para ese archivo
			evento.getArchivo().setIntentoDescarto(intento);// se suma el contador
		} while (intento <= 4 && !aceptadoAntivirus());// si se cumple que el archivo no se acepto o todavia quedan  intentos, se repite
		tiempoRevision=(evento.getArchivo().getIntentoDescarto()>3?tiempoEnvioRouter(tamano, 3):tiempoRevision);
		return (tiempoActual+tiempoRevision);
	}

	// sets y gets
	
	/**
	 * @return antivirus libre o no
	 */
	public boolean isLibre() {
		return libre;
	}

	/**
	 * @param set libre
	 */
	public void setLibre(boolean libre) {
		this.libre = libre;
	}

	/**
	 * @return verdadero o falso, dependiendo si el numero U(0,1) cae 
	 * en la franja donde el antivirus acepta o rechaza el archivo. 
	 */
	public boolean aceptadoAntivirus() {// distribucion que dice si el archivo se acepta o no en el antivirus
		return (Math.random() > 0.15 ? true : false);
	}

	/**
	 * Usa la formula (m*i)/8, y calcula la duracion de la revision. 
	 * 
	 * @param m tamano del archivo
	 * @param i intento
	 * @return tiempo que toma cada revision dependiendo de los parametros
	 */
	public double tiempoEnvioRouter(double m, double i) {// tiempo que pasa en revision hasta el envio al router
		return (double) ((m * i) / 8);
	}

	public Queue<Evento> getCola() {
		return Cola;
	}

	public void setCola(Queue<Evento> cola) {
		Cola = cola;
	}

}
