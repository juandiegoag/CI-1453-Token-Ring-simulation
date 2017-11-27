package src;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Clase que maneja la computadora y todos sus atributos. 
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */

public class Computadora {

	private Queue<Archivo> ColaPrioridad;// cola de prioridad para los archivos que llegan
	private double prxLleg;	 // proxima llegada a la cpu
	private int cpu;			 // numero de cpu, debido a la herencia
	private double token;		 // todo el token, se va reduciendo conforme se vaya pasando el tiempo
	private Distribucion distribucion;
	private boolean libre;
	private int numPrioridadUno; //contadores de cuantos archivos de prioridad uno o dos hay en la computadora
	private int numPrioridadDos; //para imprimir dentro de las variables de estado, y evitar recorrer la cola 
	//cada vez que se ocupan esos datos
	public static Comparator<Archivo> prioridades = new Comparator<Archivo>() {
		@Override // comparador para agregar a la cola de prioridad, los acomoda en 1 o 2
		public int compare(Archivo o1, Archivo o2) {
			return (int) (o1.getPrioridad() - o2.getPrioridad());
		}
	};

	/**
	 * Inicializa el atributo distribucion, como una de sus implementaciones polimorficas,
	 * el token, y la CPU, los demas atributos en default.
	 * 
	 * @param distribucion dependiendo de cual computadora sea 
	 * @param token tiempo de token asignado 
	 * @param cpu numero de CPU
	 */
	public Computadora(Distribucion distribucion, double token, int cpu) {
		setColaPrioridad(new PriorityQueue<Archivo>(11,prioridades));//cola inicializada con la cantidad default de 11 nodos, pero no es un limite, puede seguir expandiendo
		this.token 		  = token;
		this.distribucion = distribucion;
		prxLleg = -1;
		this.cpu=cpu;
		this.setLibre(true);
		setNumPrioridadUno(0);
		setNumPrioridadDos(0);
	}

	/**
	 * Agrega el evento a la cola de la cpu, puesto que el metodo de eleccion de servicio
	 * no es directo, si no que se maneja con prioridad token y tamano, solo se agrega, y
	 * se calcula la proxima llegada. 
	 * 
	 * @param evento el evento que llega a la CPU
	 * @param tablaEventos en donde guardar las acciones posteriores para dicho evento
	 * @param tActual tiempo en el que llega el evento
	 * @param logger instancia donde guardar las salidas de texto
	 */
	public void procesarLlegada(Evento evento, TablaEventos tablaEventos, double tActual, Logger logger) {
		getColaPrioridad().add(evento.getArchivo());// agrego el archivo que entra
		if (evento.getArchivo().getPrioridad()==1) {//si el evento que entra a la computadoraes prioridad uno
			setNumPrioridadUno(getNumPrioridadUno() + 1);//aumenta el contador de archivos en la computadora de prioridad uno
		} else {
			setNumPrioridadDos(getNumPrioridadDos() + 1);//aumenta el contador de archivos en la computadora de prioridad dos
		}
		evento.getArchivo().setLlegadaCPU(tActual);// agrega la llegada como el momento actual calculo el siguiente
		Evento ev = new Evento(new Archivo(getCpu()));// cra la siguiente llegada
		double llegada = tActual + distribucion.generarLlegadaArchivo(); 
		
		tablaEventos.agregarEvento(llegada, ev);// la agrega a la tabla
		actPrxLleg(llegada, tActual); // actualiza en caso de que sea mas pronta que la mas cercana de la cpu
		// imprimo resultados
		logger.guardar("LLEGANDO ARCHIVO CON PRIORIDAD: " + evento.getArchivo().getPrioridad() + " Y TAMANO: " + evento.getArchivo().getTamano() + " A LA COLA DE LA CPU: " + getCpu());
		logger.guardar("TAMANO DE LA COLA: " + getColaPrioridad().size());
		logger.guardar("\tPROXIMA LLEGADA A LA CPU " + getCpu() + " en T" + llegada);
	}

	/**
	 * Puesto que es una cola ordenada por prioridad, basta con analizar la cabeza de la cola
	 * para saber si hay elementos de prioridad uno o no
	 * @return verdadero o falso, dependiendo de la condicion. 
	 */
	public boolean hayPrioridadUno() {
		return (getColaPrioridad().peek().getPrioridad() == 1 ? true : false);// true or false dependiendo si hay elementos de prioridad uno
	}

	/**
	 * Metodo que itera sobre los elementos de la cola para buscar el mayor elemento que cabe
	 * en el tiempo restante de envio de la cpu, dependiendo de la prioridad. 
	 * 
	 * @param tiemporestante el tiempo de transmision de archivo restante (token)
	 * @param prioridad la prioridad con la que buscar
	 * @return devuelve el archivo mas adecuado a las condiciones de envio
	 */
	public Archivo removerArchivoSiguiente(double tiemporestante, int prioridad) {
		Object[] colaArchivos = getColaPrioridad().toArray();// metodo de escogencia del siguiente archivo, dadas las condiciones de ser el mayor de los menores
		Archivo mayormenor = new Archivo(getCpu());// y por priodaidad
		mayormenor.setPrioridad(0);// cambia la cola a un array para poder iterar
		mayormenor.setTamano(0);
		for (Object archivoenCola : colaArchivos) {
			Archivo archivo = (Archivo) archivoenCola;//
			if (archivo.getPrioridad() == prioridad && archivo.getTamano() * 0.5 <= tiemporestante) {// si es de la prioridad adecuada, y el mas apto para cruzar en esa franja de tiempo
				if (archivo.getTamano() > mayormenor.getTamano()) {
					mayormenor = archivo;// se elige
				}
			}
		}
		mayormenor = (mayormenor.getTamano() == 0 ? null : mayormenor);// si no hay archivo, o sea, cambio de token, devuelve nulo
		getColaPrioridad().remove(mayormenor);// saca el que eligio de la cola
		return mayormenor;
	}

	/**
	 * Crea un archivo nulo, si la cola no esta vacia, y el token esta disponible para enviar
	 * itera sobre los archivos de prioridad uno, para encontrar el mas indicado de envio, 
	 * si en efecto habian archivos de prioridad uno, pero el archivo a enviar sigue siendo 
	 * nulo, es porque todos los archivos que restaban de envio, eran muy grandes para adjuntarse,
	 * entonces se busca en prioridad dos. Si al final de todo, el archivo a enviar sigue siendo
	 * nulo, la computadora no tiene nada mas que enviar. 
	 * @return el archivo a enviar
	 */
	public Archivo enviarArchivo() {// comienza con un archivo nulo, si queda tiempo de token y la cola no esta vacia
		Archivo archivoaEnviar = null;
		if (!getColaPrioridad().isEmpty() && token > 0) {
			int prioridad = (hayPrioridadUno() ? 1 : 2);// revisa cual prioridad hay
			archivoaEnviar = removerArchivoSiguiente(getToken(), prioridad);// saca el archivo dependiendo de la prioridad que hay
			if (archivoaEnviar == null) {// si despues de revisar el archivo sigue siendo nulo
				prioridad++;
				archivoaEnviar = removerArchivoSiguiente(getToken(), prioridad);// revisa con la siguiente prioridad
			}
		}
		return archivoaEnviar;// si a pesar de todo sigue siendo nulo, no hay nada mas que enviar
	}

	/**
	 * El metodo busca si es posible enviar un archivo desde la CPU, de no ser posible,
	 * significa que hay un cambio de token para la CPU, y se procesa. Si el archivo no 
	 * es nulo, se le calcula su tiempo de servicio en la CPU, y se agrega en la tabla. 
	 * 
	 * @param tiempoactual tiempo donde ocurre el envio de cpu a la linea
	 * @param tablaEventos donde guardar los eventos posteriores
	 * @param logger	   donde guardar la salida de texto
	 */
	public void procesarEnvio(double tiempoactual, TablaEventos tablaEventos, Logger logger) {
		if(isLibre()){
			Evento evento = new Evento(enviarArchivo());// llama al metodo de enviar archivo,
			if (evento.getArchivo() != null) {// si el archivo existe y se tomo de la cola
				setLibre(false);
				double salidaCPU = tiempoactual + 0.5 * evento.getArchivo().getTamano();// calcula el tiempo total de envio hasta la linea
				tablaEventos.agregarEvento(salidaCPU, evento);// agrega el evento a la tabla para ese momento de tiempo
				logger.guardar("ENVIANDO ARCHIVO CON PRIORIDAD: " + evento.getArchivo().getPrioridad() + " Y TAMANO: "+ evento.getArchivo().getTamano() + " A LA LINEA DESDE LA CPU: " + getCpu());
				logger.guardar("TIEMPO RESTANTE DE TOKEN: " + token + "s");
				setToken(getToken() - 0.5 * evento.getArchivo().getTamano());// actualiza el token
				if (evento.getArchivo().getPrioridad()==1) {//si el evento que entra a la computadoraes prioridad uno
					setNumPrioridadUno(getNumPrioridadUno() - 1);//disminuye el contador de archivos en la computadora de prioridad uno
				} else {
					setNumPrioridadDos(getNumPrioridadDos() - 1);//disminuye el contador de archivos en la computadora de prioridad dos
				}
			} else {
				setToken(0);// si no hay nada que enviar, pone el token en 0
				logger.guardar("IMPOSIBLE ENVIAR MAS ARCHIVOS DESDE CPU " + getCpu() + " CAMBIO EN POSESION DE TOKEN");
				logger.guardar("TIEMPO RESTANTE DE TOKEN PARA LA CPU " + getCpu() + " : " + token + "s");
				tablaEventos.agregarEvento(tiempoactual, evento);// agrega el evento NULO (interpretado como cambio de token) a la tabla
			} // para el tiempo actual
		}
	}

	/**
	 * Se aumenta la cantidad de archivos enviados, se le agrega un tiempo de salida al archivo
	 * y de llegada al antivirus, y procesa otro envio si ya la computadora vuelve a estar libre. 
	 * 
	 * @param evento el evento que sale de la cpu
	 * @param tablaEventos guarda eventos posteriores
	 * @param tiempoactual tiempo en el que ocurre
	 * @param logger instancia de clase para salidas de texto
	 * @param estadisticas instancia de clase para recolectar datos de los archivos
	 */
	public void procesarSalida(Evento evento, TablaEventos tablaEventos, double tiempoactual, Logger logger, Estadisticas estadisticas) {
		setLibre(true);
		estadisticas.aumentarArchivosEnviados();
		evento.getArchivo().setSalidaCPU(tiempoactual);// agrega la salida del evento como el momento actual en el tiempo
		tablaEventos.agregarEvento(tiempoactual + .25, evento);// agrega el evento de llegada al antivirus con el .25 de movimiento en la linea
		logger.guardar("ENVIANDO ARCHIVO CON PRIORIDAD: " + evento.getArchivo().getPrioridad() + " Y TAMANO: "+ evento.getArchivo().getTamano() + " DESDE LA LINEA AL ANTIVIRUS");
		procesarEnvio(tiempoactual, tablaEventos, logger);// si ya esta libre, procesa otro envio, si es que tiene token o mas archivos que enviar
	}

	// SETTERS Y GETTERS

	public double getPrxLleg() {
		return prxLleg;
	}

	public void setPrxLleg(double prxLleg) {
		this.prxLleg = prxLleg;
	}

	/**
	 * @param nuevaSalida
	 * @param tiempoActual
	 */
	public void actPrxLleg(double nuevaSalida, double tiempoActual) {
		prxLleg = (prxLleg < nuevaSalida && prxLleg > tiempoActual ? prxLleg : nuevaSalida);// si la
		//proxima llegada que intenta tomar la variable de estado es
		// mayor que el valor actual de la proxima llegada, y ademas el antiguo
		// valor es mayor al tiempo actual, entonces el
		// antiguo valor es la proxima llegada mas cercana, de lo contrario, es
		// el nuevo.
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public double getToken() {
		return token;
	}

	public void setToken(double token) {
		this.token = token;
	}

	public boolean isLibre() {
		return libre;
	}

	public void setLibre(boolean libre) {
		this.libre = libre;
	}

	public int getNumPrioridadUno() {
		return numPrioridadUno;
	}

	public void setNumPrioridadUno(int numPrioridadUno) {
		this.numPrioridadUno = numPrioridadUno;
	}

	public int getNumPrioridadDos() {
		return numPrioridadDos;
	}

	public void setNumPrioridadDos(int numPrioridadDos) {
		this.numPrioridadDos = numPrioridadDos;
	}

	public Queue<Archivo> getColaPrioridad() {
		return ColaPrioridad;
	}

	public void setColaPrioridad(Queue<Archivo> colaPrioridad) {
		ColaPrioridad = colaPrioridad;
	}

}