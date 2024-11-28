/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Luis Velasco
 */
package client;
import java.io.*;
import java.net.*;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        new GUI().setVisible(true);
        try (
            Socket socket = new Socket(HOST, PORT);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to server");

            // Hilo para escuchar mensajes del servidor
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = input.readLine()) != null) {
                        System.out.println("received meesage " + mensaje);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Enviar mensajes al servidor
            String mensaje;
            while ((mensaje = keyboard.readLine()) != null) {
                output.println(mensaje);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
