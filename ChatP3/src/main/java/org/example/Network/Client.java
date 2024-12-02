package org.example.Network;

import java.io.*;
import java.net.*;

import org.example.UI.GUIChat;

public class Client extends Thread {

    GUIChat ui = new GUIChat(0);

    public void run() {
        InetAddress group = null;
        try {
            group = InetAddress.getByName("231.0.0.0");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            MulticastSocket socket = new MulticastSocket(4000);
            socket.joinGroup(group);
            DatagramPacket contacto = new DatagramPacket(("<inicio>" + ui.getNombre()).getBytes(), ("<inicio>" + ui.getNombre()).length(), group, 4000);
            socket.send(contacto);
            while (true) {
                if (ui.getStatus() == 0) {     //Lectura 
                    socket.setSoTimeout(100);
                    try {
                        byte[] buf = new byte[2048];
                        DatagramPacket recv = new DatagramPacket(buf, buf.length);
                        socket.receive(recv);
                        byte[] data = recv.getData();
                        String mensaje = new String(data);
                        ui.setNewMessage(mensaje); //procesa el mensaje recibido y lo muestra en la pantalla    
                    } catch (Exception e) {
                    }
                } else if (ui.getStatus() == 1) {   //Escritura
                    String mensaje = "";
                    
                    if(ui.getSalida() == 1){//EL USAURIO YA SALIO DEL CHAT // CASO CONTRARIO SIGUE EN EÑ CHAT
                        mensaje = "<salida>" + ui.getNombre();
                    }else{
                        if(ui.getActiveTab() != 0){//devuelve el inddice de la pantalla en uso
                            mensaje = "C<msj><privado><" + ui.getNombre() + "><" + ui.getContactosChat(ui.getActiveTab()) + ">" + ui.getActiveMessage();
                        }else if(ui.getActiveTab() == 0){                           //devuelve el nombre del cintacto con la pestaña asociada
                            mensaje = "C<msj> " + ui.getNombre() + " Dice: " + ui.getActiveMessage();
                        }
                    }
                    DatagramPacket packet = new DatagramPacket(mensaje.getBytes(), mensaje.length(), group, 4000);
                    socket.send(packet);
                    ui.setStatus(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

    }//run

    public static void main(String[] args) {

        try {
            Client cliente = new Client();
            cliente.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
