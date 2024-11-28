package server;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Luis Velasco
 */
public class Server {
    private static final int PORT = 12345;
    private static List<PrintWriter> clients = new ArrayList<>();
    
    public static void main(String[] args){
        System.out.println("Server running... Waiting for clients");
        try {
            ServerSocket server = new ServerSocket(PORT);
            for(;;) {
                Socket client = server.accept();
                System.out.println("Client connected: " + client.getInetAddress());
                ThreadClient clientThread = new ThreadClient(client);
                new Thread(clientThread).start();  // Crear un nuevo hilo usando la implementación de Runnable
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private static class ThreadClient implements Runnable {
        private Socket socket;
        private PrintWriter output;

        public ThreadClient(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                output = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clients) {
                    clients.add(output);
                }

                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    System.out.println("Mensaje recibido: " + mensaje);
                    synchronized (clients) {
                        for (PrintWriter cliente : clients) {
                            cliente.println(mensaje);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                synchronized (clients) {
                    clients.remove(output);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Client disconnected");
            }
        }
    }
}
