package logica;

import java.util.Random;

/**
 *
 * @author Luis Velasco
 */
public class GenerarTablero {
    public static int[][] generarTablero(int filas, int columnas, int minas){
        int[][] tablero = new int[filas][columnas];
        Random rand = new Random();
        
        int minasColocadas = 0;
        while (minasColocadas < minas) {
            int fila = rand.nextInt(filas);
            int columna = rand.nextInt(columnas);
            
            if (tablero[fila][columna] != -1) {
                tablero[fila][columna] = -1; // -1 representa una mina
                minasColocadas++;
            }
        }
        return tablero;

    }
    
}
