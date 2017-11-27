package src;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
/**
 * La clase modela la tabla de eventos del programa de simulacion.
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */
public class TablaEventos {
	private Map<Double, Queue<Evento>> tablaEventos;// mapa para la tabla de eventos

	/**
	 * El constructor inicaliza el treemap de double y cola de eventos. Para simular
	 * la tabla de eventos del programa. 
	 */
	public TablaEventos() {
		tablaEventos = new TreeMap<Double, Queue<Evento>>(); // treemap porque se ocupan llaves ordenadas
	}

	/**
	 * Agrega el evento a la llave que representa el tiempo que indica el parametro,
	 * si no existe la llave, se crea la entrada con la cola vacia, y se agrega el 
	 * evento, si si existe la llave, solo se agrega el evento a la cola
	 * @param key
	 * @param evento
	 */
	public void agregarEvento(double key, Evento evento) {
		if (!tablaEventos.containsKey(key)) {
			Queue<Evento> l = new LinkedList<Evento>();// crea la cola de eventos
			l.add(evento); // y agrega el elemento
			tablaEventos.put(key, l); // al final, genera la llave respectiva al momento en el tiempo, y agrega la lista
		} else {
			tablaEventos.get(key).add(evento); // si no, solo agrega el evento a la lista de cosas asociadas al tiempo
		}
	}

	/**
	 * La implementacion del mapa es por un tree map, el metodo devuelve la primera llave de la cola,
	 *  y a su vez, el siguiente evento, ya que, se elemina la entrada de la tabla apenas
	 *  se lee en la clase simulador.  
	 * @return double del siguiente tiempo a procesar 
	 */
	public double getNext() {
		 Map.Entry<Double, Queue<Evento>> entry= tablaEventos.entrySet().iterator().next();
		 double llave= entry.getKey();
		 return llave;
	}
	/**
	 * Elimina la cola de eventos asociada a la llave.
	 * @param llave
	 */
	public void eliminarColaEventos(double llave){
		tablaEventos.remove(llave);
	}
	/**
	 * Devuelve la cola asociada a la llave. 
	 * @param key
	 * @return devuelve la cola asociada a la llave.
	 */
	public Queue<Evento> getCola(double key) {
		return tablaEventos.get(key);// deveulve la cola de eventos asociada a la llave
	}

}
