package src;
/**
 * Clase que maneja el archivo y todos sus atributos, ademas de los eventos que pueden
 * suceder a un archivo dependiendo del valor de sus atributos. 
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */

public class Archivo {
	private int prioridad;// prioridad del archivo
	private int tamano;// tamano del archivo
	private int cpu;// computadora a la que llega
	private int intentoDescarto;// numero de intentos que ha tomado descartarlo o no

	// eventos con respecto al tiempo

	// CPU
	private double llegadaCPU;// tiempo de llegada a una cpu
	private double salidaCPU;
	// ANTIVIRUS
	private double llegadaANT;// legada al antivirus
	private double salidaANT;
	// ROUTER
	private double llegadaROU;// legada al router
	private double salidaROU;

	/**
	 * El constructor comienza poniendo todos los atributos en su valor default. Tambien,
	 * calcula el tamano y la prioridad del archivo. 
	 * 
	 * @param cpu el numero de cpu a donde llega el archivo
	 */

	public Archivo(int cpu) {// parametro el numero de computadora a la que llega, el codigo en orden A=1,B=2,C=3
		prioridad = prioridadArchivo();// genera la prioridad segun la distribucion
		tamano = tamArchivo(1, 64);// genera el tamano segun la distribucion
		setCpu(cpu);// el constructor indica en cual CPU estuvo el archivo
		setIntentoDescarto(0);// para contabilizar el numero de intentos que toma revisarlo por el antivirus
		setLlegadaCPU(-1);// TODAS LAS VARIABLES COMIENZAN EN -1
		setLlegadaANT(-1);
		setLlegadaROU(-1);
		setSalidaCPU(-1);
		setSalidaANT(-1);
		setSalidaROU(-1);
	}

	/**
	 * El metodo devuelve el tamano del archivo, dependiendo de la distribucion,
	 * usando transformacion inversa. 
	 * 
	 * @param a donde comienza la distribucion uniforme.
	 * @param b donde termina la distribucion uniforme. 
	 * @return
	 */
	public int tamArchivo(double a, double b) {// distribucion para el tamano de un archivo
		return (int) Math.round((a + (b - a) * (double) (Math.random())));// redondea para arriba por si el paquete no calza
	}

	/**
	 * Tipo de simulacion de Monte Carlo para saber si el archivo es prioridad uno o dos. 
	 * 
	 * @return 1 o 2
	 */
	public int prioridadArchivo() {// distribucion para ver si es prioridad uno o dos
		return (Math.random() <= 0.25 ? 1 : 2);
	}
	/**
	 * Busca el primer evento que NO este en su valor default, para discernir cual evento
	 * corresponde ahora al archivo. 
	 * 
	 * @return
	 */
	public int getEventoActual() {// metodo que permite encontrar el evento que sigue para
		// cualquier archivo determinado
		int evento = -1;
		boolean encontrado = false;
		if (getLlegadaCPU() == -1 && !encontrado) {// devuelve el primer evento no establecido por
			evento = 1;// el simulador, y este en su estado default por el constructor, de forma que se identifiquen por su numero
			encontrado = true;// las llegadas y salidas de cada archivo a cada parte del sistema permiten identificar la siguiente accion
		} else if (getSalidaCPU() == -1 && !encontrado) {
			evento = 2;
			encontrado = true;
		} else if (getLlegadaANT() == -1 && !encontrado) {
			evento = 3;
			encontrado = true;
		} else if (getSalidaANT() == -1 && !encontrado) {
			evento = 4;
			encontrado = true;
		} else if (getLlegadaROU() == -1 && !encontrado) {
			evento = 5;
			encontrado = true;
		} else if (getSalidaROU() == -1 && !encontrado) {
			evento = 6;
			encontrado = true;
		}

		return evento;
	}

	// GETTERS Y SETTERS
	public int getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(int prioridad) {
		this.prioridad = prioridad;
	}

	public int getTamano() {
		return tamano;
	}

	public void setTamano(int tamano) {
		this.tamano = tamano;
	}

	public double getLlegadaCPU() {
		return llegadaCPU;
	}

	public void setLlegadaCPU(double llegadaCPU) {
		this.llegadaCPU = llegadaCPU;
	}

	public double getSalidaCPU() {
		return salidaCPU;
	}

	public void setSalidaCPU(double salidaCPU) {
		this.salidaCPU = salidaCPU;
	}

	public double getLlegadaANT() {
		return llegadaANT;
	}

	public void setLlegadaANT(double llegadaANT) {
		this.llegadaANT = llegadaANT;
	}

	public double getSalidaANT() {
		return salidaANT;
	}

	public void setSalidaANT(double salidaANT) {
		this.salidaANT = salidaANT;
	}

	public double getLlegadaROU() {
		return llegadaROU;
	}

	public void setLlegadaROU(double llegadaROU) {
		this.llegadaROU = llegadaROU;
	}

	public double getSalidaROU() {
		return salidaROU;
	}

	public void setSalidaROU(double salidaROU) {
		this.salidaROU = salidaROU;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public int getIntentoDescarto() {
		return intentoDescarto;
	}

	public void setIntentoDescarto(int intentoDescarto) {
		this.intentoDescarto = intentoDescarto;
	}

}
