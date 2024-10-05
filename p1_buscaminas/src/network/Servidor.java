package network;


import java.io.*;
import java.net.*;


import logica.Juego;

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

                Juego juego = null;
                switch (difficulty) {
                    case "facil":
                        //tablero = GenerarTablero.generarTablero(9, 9, 10);
                        //Juego juego = new Juego();
                        //Juego juego = new Juego("facil");
                        juego = new Juego("facil");
                        break;
                    case "intermedio":
                        //tablero = GenerarTablero.generarTablero(16, 16, 40);
                        //Juego juego = new Juego();
                        //Juego juego1 = new Juego("intermedio");
                        juego = new Juego("intermedio");
                        break;
                    case "experto":
                        //tablero = GenerarTablero.generarTablero(16, 30, 99);
                        //Juego juego = new Juego();
                        //Juego juego2 = new Juego("experto");
                        juego = new Juego("experto");
                        break;
                    default:
                        //tablero = GenerarTablero.generarTablero(9, 9, 10);
                        //Juego juego = new Juego();
                        //Juego juego3 = new Juego("facil");
                        juego = new Juego("facil");
                        break;
                }

                int[][] tablero = juego.getTablero();

                //ya que tenemos el tablero creado, se lo enviamos al cliente
                ObjectOutputStream oos =  new ObjectOutputStream(cl.getOutputStream());
                oos.writeObject(tablero);
                oos.flush();
                
                

                dis.close();
                dos.close();
                oos.close();
                cl.close();
            }


        } catch(IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
