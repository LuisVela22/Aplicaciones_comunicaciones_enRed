package org.example.Network;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Server extends Thread {

    public static final String MCAST_ADDR = "230.1.1.1";//dir clase D valida, grupo al que nos vamos a unir
    public static final int MCAST_PORT = 4000;
    public static final int DGRAM_BUF_LEN = 2048;
    private ArrayList<String> contactos;

    public void run() {
        contactos = new ArrayList();
        String msg = "";
        InetAddress group = null;
        try {
            group = InetAddress.getByName(MCAST_ADDR);	
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
        for (;;) {
            try {
                MulticastSocket socket = new MulticastSocket(MCAST_PORT);
                socket.joinGroup(group);
                
                //Espera a recibir los datos de algun cliente
                
                byte[] buf = new byte[DGRAM_BUF_LEN];
                DatagramPacket recv = new DatagramPacket(buf,buf.length);
                socket.receive(recv);
                byte [] data = recv.getData();
                msg = new String(data);
                System.out.println("Datos recibidos: " + msg);
                
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
                    System.out.println("Enviando: " + cont);
                    DatagramPacket packet = new DatagramPacket( cont.getBytes(), cont.length(), group, MCAST_PORT);
                    //System.out.println("Enviando: " + msg + "  con un TTL= " + socket.getTimeToLive());
                    socket.send(packet);
                    socket.close();
                }else if(msg.contains("C<msj>")){
                    msg = msg.substring(1);
                    msg = "S" + msg;
                    DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), group, MCAST_PORT);
                    System.out.println("Enviando: " + msg.toString() + "  con un TTL= " + socket.getTimeToLive());
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
                    System.out.println("Enviando: " + cont);
                    DatagramPacket packet = new DatagramPacket( cont.getBytes(), cont.length(), group, MCAST_PORT);
                    //System.out.println("Enviando: " + msg + "  con un TTL= " + socket.getTimeToLive());
                    socket.send(packet);
                    socket.close(); 
                }
                //Envia los datos recibidos a todo el grupo
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(2);
            }

            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException ie) {
            }
        }
    }

    public static void main(String[] args) {

        try {
            Server mc2 = new Server();
            mc2.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

