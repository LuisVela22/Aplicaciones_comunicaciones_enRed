package network;

import java.io.*;
import java.net.*;
import javax.swing.*;
//import javax.swing.*;
import logica.Juego;
import ui.Menu;

public class Cliente {
    
    private String userName;
    private String difficultyy;

    
    public static void main(String[] args) {
        Menu menu = new Menu();
        menu.setVisible(true);
        menu.setLocationRelativeTo(null);
    }
    
    public void save(String username, String difficulty) {
        this.userName = username;
        this.difficultyy = difficulty;
        enviarDatosAlServidor(userName, difficultyy); 
    }
    
    public static void enviarDatosAlServidor(String username, String dificultad) {
        try {
            // Conectarse al servidor
            Socket cl = new Socket("localhost", 3500);
            System.out.println("Conectado al servidor en el puerto: "+ cl.getPort());

            // Enviar los datos al servidor
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
            dos.writeUTF(username);  // Enviar nombre del usuario
            dos.writeUTF(dificultad);  // Enviar dificultad
            System.out.println("Datos enviados al servidor: " + username + " con dificultad " + dificultad);
            
            ObjectInputStream ois = new ObjectInputStream(cl.getInputStream());
            int[][] tablero = (int[][]) ois.readObject();

            //System.out.println(tablero.toString());

            Juego juego = new Juego(tablero);

            // Configurar el listener para recibir el tiempo final
            /*juego.setGameListener((minutes, seconds, won) -> {
                if (won) {
                    // Aquí puedes enviar el tiempo al servidor
                    System.out.println("Ganaste! Tiempo: " + minutes + " minutos y " + seconds + " segundos.");
                    // Enviar tiempo al servidor si es necesario
                }
            });*/

            /*JFrame frame = new JFrame("Buscaminas");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setLayout(new java.awt.GridLayout(tablero.length, tablero[0].length));

            for (int i = 0; i < tablero.length; i++) {
                for (int j = 0; j < tablero[i].length; j++) {
                    System.out.println("Tablero en la posicion :" + i + " " + tablero[i]);
                    JButton btn = new JButton();
                    // Mostrar el estado del botón según el contenido del tablero
                    if (tablero[i][j] == -2) { // Mina
                        btn.setText("Mina"); // Puedes cambiar esto por un icono
                    } else if (tablero[i][j] > 0) { // Número de minas adyacentes
                        btn.setText(String.valueOf(tablero[i][j]));
                    } else {
                        btn.setText(""); // Espacio vacío
                    }
                    frame.add(btn);
                }
            }
            
            frame.pack();
            frame.setVisible(true);
            */
            // Cerrar conexiones
            dos.close();
            cl.close();
        } catch(IOException | ClassNotFoundException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
