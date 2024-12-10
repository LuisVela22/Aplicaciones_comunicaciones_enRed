package logica;
import java.util.Random;

public class Tablero {
    private int[][] mat;
    private int filas;
    private int columnas;
    private int minas;

    public Tablero(int filas, int columnas, int minas) {
        this.filas = filas;
        this.columnas = columnas;
        this.minas = minas;
        this.mat = new int[filas][columnas];
        inicializarMatriz();
    }

    private void inicializarMatriz() {
        Random aleatorio = new Random();
        int f, c;

        // Inicializar el tablero vacío
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                mat[i][j] = 0;
            }
        }

        // Agregar minas al tablero
        for (int i = 0; i < minas; i++) {
            do {
                f = aleatorio.nextInt(filas);
                c = aleatorio.nextInt(columnas);
            } while (mat[f][c] == -2);
            mat[f][c] = -2; // -2 indica una mina
        }

        // Calcular los números alrededor de las minas
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (mat[i][j] == -2) {
                    // Verificar las celdas circundantes
                    incrementarCeldasVecinas(i, j);
                }
            }
        }
    }

    private void incrementarCeldasVecinas(int i, int j) {
        int[] dx = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] dy = {-1, 0, 1, 1, 1, 0, -1, -1};

        for (int k = 0; k < dx.length; k++) {
            int ni = i + dx[k];
            int nj = j + dy[k];

            if (ni >= 0 && ni < filas && nj >= 0 && nj < columnas && mat[ni][nj] != -2) {
                mat[ni][nj]++;
            }
        }
    }

    public int[][] getTablero() {
        return mat;
    }
}
