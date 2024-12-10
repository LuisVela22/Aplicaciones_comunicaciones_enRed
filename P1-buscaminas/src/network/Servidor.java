package network;


import java.io.*;
import java.net.*;
import logica.Tablero;

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

                //Juego juego = null;
                Tablero tablero = null;
                switch (difficulty) {
                    case "facil":
                        //juego = new Juego("facil");
                        tablero = new Tablero(9,9,10);
                        break;
                    case "intermedio":
                        //juego = new Juego("intermedio");
                        tablero = new Tablero(16,16,40);
                        break;
                    case "experto":
                        //juego = new Juego("experto");
                        tablero = new Tablero(16,30,99);
                        break;
                    default:
                        //juego = new Juego("facil");
                        tablero = new Tablero(9,9,10);
                        break;
                }

                //int[][] tablero = juego.getTablero();

                //ya que tenemos el tablero creado, se lo enviamos al cliente
                ObjectOutputStream oos =  new ObjectOutputStream(cl.getOutputStream());
                oos.writeObject(tablero.getTablero());
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
