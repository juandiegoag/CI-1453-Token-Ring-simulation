package src;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Clase que maneja las salidas a txt o consola de cada corrida. 
 * 
 * @author Juan Diego Araya
 * @author Eduardo Solorzano
 */

public class Logger {
	String global;
	/**
	 * Constructor que inicliza el unico string de la clase como nula. 
	 */
	public Logger() {
		global = "";
	}
	/**
	 * Imprime la string que recibe en pantalla, y lo guarda en el string global.
	 * @param s cualquier string
	 */
	public void imprimir(String s) {
		global += s + "\n";
		System.out.println(s);
	}
	/**
	 * Concatena el string que recibe, al global, para al final imprimirlo todo
	 * @param s cualquier string
	 */
	public void guardar(String s) {// guarda datos y agrega un cambio de linea por cada string
		global += s + "\n";
	}
	/**
	 * Guarda el string global en un archivo de texto cuyo nombre sea el parametro
	 * que recibe el metodo. 
	 * @param nombre nombre del txt donde se guarda el string global. 
	 */
	public void exportarTxt(String nombre) {// exporta todos los datos a un .txt cuando se llama
		File archivo = new File(nombre + ".txt");
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(archivo);
			printWriter.println(global);
		} catch (FileNotFoundException e) {
		} finally {
			if (printWriter != null) {
				printWriter.close();
			}
		}

	}
        
        public String getGlobal(){
            return global;
        }

}
