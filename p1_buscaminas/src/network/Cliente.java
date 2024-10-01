package network;

import java.io.*;
import java.net.*;
//import javax.swing.*;
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

            // Cerrar conexiones
            dos.close();
            cl.close();
        } catch(IOException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
