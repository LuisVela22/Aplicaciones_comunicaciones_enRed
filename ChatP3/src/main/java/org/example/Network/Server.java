package org.example.Network;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Server extends Thread {

    private ArrayList<String> contactos;

    public void run() {
        contactos = new ArrayList();
        String msg = "";
        InetAddress group = null;
        try {
            group = InetAddress.getByName("231.0.0.0");	
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Servidor corriendo en la dirección 231.0.0.0, puerto 4000, esperando clientes...");
        for (;;) {
            try {
                MulticastSocket socket = new MulticastSocket(4000);
                socket.joinGroup(group);
                
                //Espera a recibir los datos de algun cliente
                
                byte[] buf = new byte[2048];
                DatagramPacket recv = new DatagramPacket(buf,buf.length);
                socket.receive(recv);
                byte [] data = recv.getData();
                msg = new String(data);
                
                if(msg.contains("<inicio>")){
                    msg = msg.substring(8);
                    String nombre = "";
                    int i = 0;
                    while(Character.isLetter(msg.charAt(i))){
                        nombre = nombre + msg.charAt(i);
                        i++;
                    }
                    contactos.add(nombre);
                    String cont = "<contactos>" + contactos.toString();
                    //Envio de los contactos
                    DatagramPacket packet = new DatagramPacket( cont.getBytes(), cont.length(), group, 4000);
                    socket.send(packet);
                    socket.close();
                }else if(msg.contains("C<msj>")){
                    msg = msg.substring(1);
                    msg = "S" + msg;
                    DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), group, 4000);
                    socket.send(packet);
                    socket.close();
                }else if(msg.contains("<salida>")){
                    String salida = "";
                    int i = 8;
                    while(Character.isLetter(msg.charAt(i))){
                        salida = salida + msg.charAt(i);
                        i++;
                    }
                    contactos.remove(salida);
                    String cont = "<contactos>" + contactos.toString();
                    //Envio de los contactos
                    DatagramPacket packet = new DatagramPacket( cont.getBytes(), cont.length(), group, 4000);
                    socket.send(packet);
                    socket.close(); 
                }
                //Envia los datos recibidos a todo el grupo
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(2);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
            }
        }
    }

    public static void main(String[] args) {

        try {
            Server srv = new Server();
            srv.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

