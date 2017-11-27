package src;


/**
 * Ejecuta el programa dependiendo de los parametros que se especifiquen
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */
public class Main {
	
	public static void main(String[] args) {
	/*	int numeroCorridas = 3;
		int token = 100;
		int segundos = 500;
		int i = 0;
		boolean delay = false;
		Simulador S = new Simulador();
		while (i++ < numeroCorridas) {
			S.correrSimulacion(token, segundos, "lalala" + i, delay);
		}
		S.calcularEstadisticasGlobales(numeroCorridas);*/
		
		int numeroCorridas = 3;
		int token = 100;
		int segundos = 500;
		int i = 0;
		boolean delay = false;
		Simulador simulador = new Simulador();
		while(i++<numeroCorridas){
		simulador.definirEstadoInicial(token);
		double siguiente = 0;// para controalr los ciclos de tiempo
		while (siguiente <= segundos) {// cantidad de segundos por corrida
			simulador.stepSimulador(siguiente,delay);
			siguiente = simulador.getTablaEventos().getNext();// siguiente evento en la tabla
		}
		simulador.estadisticas.guardarResultados(simulador.estadisticas.devolverArrayEstadisticasCorrida(), simulador.getLogger());
		simulador.getLogger().exportarTxt("micanal"+i);// despues de correr la simulacion por el tiempo y condiciones deseados, exporta los resultados de la corrida
		simulador.sumarArray(simulador.getEstadisticasGlobales(), simulador.estadisticas.devolverArrayEstadisticasCorrida());
		}
		simulador.calcularEstadisticasGlobales(numeroCorridas);
	}

}
