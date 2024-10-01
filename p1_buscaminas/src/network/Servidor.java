package network;


import java.io.*;
import java.net.*;
import logica.GenerarTablero;

/**
 *
 * @author Luis Velasco
 */
public class Servidor {

    public static void main(String[] args) {

        try {
            ServerSocket ss = new ServerSocket(3500);
            System.out.println("Servidor iniciado");

            for(;;) {
                //aqui enlaso el client con el server
                Socket cl = ss.accept();
                DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                DataInputStream dis = new DataInputStream(cl.getInputStream());

                String username = (String) dis.readUTF();
                String difficulty = (String) dis.readUTF();
                System.out.println("Usuario: " + username +" recibido desde "+ cl.getInetAddress()+ ":"+ cl.getLocalPort());
                System.out.println("Con dificultad: "+ difficulty);
                
                /*int[][] tablero;
                switch (difficulty) {
                    case "facil":
                        tablero = GenerarTablero.generarTablero(9, 9, 10);
                        break;
                    case ""
                }*/

                dis.close();
                dos.close();
                cl.close();
            }


        } catch(IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
