package com.mycompany.directorios;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ClienteDrive {

    public static void main(String[] args) {
        String Ruta = "C:\\Users\\Luis Velasco\\OneDrive\\Documentos\\5to s escom\\redes2\\practicas\\Directorios\\Cliente";
        // Creación del JFrame del usuario
        JFrame frame = new JFrame("Bienvenido a su Drive");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 500);

        // Creación del panel
        JPanel panel = new JPanel();
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setPreferredSize(new Dimension(400, 400));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Coloca las etiquetas en vertical

        // Componentes del panel
        JLabel etiqueta = new JLabel("                                                                         Las operaciones que puede realizar son las siguientes:");
        JLabel caracteristicas = new JLabel("Subir archivos(1), Descargar archivos(2), Creación de carpetas(3), Borrado de carpetas(4) y Renombrado de archivos(5)");
        JLabel instruccion = new JLabel("Ingrese la opción deseada en el primer cuadro y el nombre de la carpeta o archivo en el segundo cuadro, dependiendo del caso. Si desea subir archivos, deje el segundo cuadro en blanco: ");
        // Cajas de texto
        JTextField CajaOpcion = new JTextField();
        CajaOpcion.setPreferredSize(new Dimension(600, 30));
        CajaOpcion.setMaximumSize(new Dimension(200, 30));

        JTextField CajaNombre = new JTextField();
        CajaNombre.setPreferredSize(new Dimension(60, 30));
        CajaNombre.setMaximumSize(new Dimension(200, 30));

        // Botón para enviar
        JButton BotonEnviar = new JButton("Enviar");

        // Acción al hacer click
        BotonEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtención de los textos de las cajas de texto
                String opcion = CajaOpcion.getText();
                String nombre = CajaNombre.getText();
                String nuevoNombre = "";

                // Procesamiento de las opciones según lo seleccionado
                switch (opcion) {
                    case "1":
                        subirArchivo(nombre);
                        break;
                    case "2":
                        descargarArchivo(nombre);
                        break;
                    case "3":
                        crearCarpeta(nombre);
                        break;
                    case "4":
                        borrarCarpeta(nombre);
                        break;
                    case "5":
                        renombrarArchivo(nombre);
                        break;
                    default:
                        JOptionPane.showMessageDialog(frame, "Opción no válida.");
                        break;
                }
            }
        });

        panel.add(etiqueta);
        panel.add(caracteristicas);
        panel.add(instruccion);
        panel.add(CajaOpcion);
        panel.add(CajaNombre);
        panel.add(BotonEnviar);

        // Agregar el panel al JFrame
        frame.add(panel);
        // Mostrar la ventana
        frame.setVisible(true);
    }

    // Método para subir archivo
    
    private static void subirArchivo(String nombre) {
        JOptionPane.showMessageDialog(null, "Elegiste la opción 1: Subir archivo.");
        try {
            int pto = 1234;
            int ptoS = 8000;
            String dir = "127.0.0.1";
            InetAddress dst = InetAddress.getByName(dir);
            // Paso de los parámetros opción y nombre al servidor
            String opcion = "1";
            byte[] o = opcion.getBytes();
            DatagramSocket cl = new DatagramSocket(pto);
            cl.setBroadcast(true);
            DatagramPacket PaqOp = new DatagramPacket(o, o.length, dst, ptoS);
            cl.send(PaqOp);
            System.out.println("Se ha enviado el datagrama a " + PaqOp.getAddress() + ":" + PaqOp.getPort() + " con el mensaje: 1");
            //-------------------------------------------------------
            //esto de arriba me lo salto porque no necesito enviar opcones

            // Selección del archivo
            JFileChooser jf = new JFileChooser();
            jf.setCurrentDirectory(new File("C:\\Users\\Luis Velasco\\OneDrive\\Documentos\\5to s escom\\redes2\\practicas\\Directorios\\Cliente\\"));
            int r = jf.showOpenDialog(null);

            if (r == JFileChooser.APPROVE_OPTION) {
                File f = jf.getSelectedFile();
                String nombreArchivo = f.getName();
                String path = f.getAbsolutePath();
                long tam = f.length();
                System.out.println("Preparandose para enviar archivo " + path + " de " + tam + " bytes");

                // Se envia el nombre del archivo
                byte[] buffer = nombreArchivo.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, dst, ptoS);
                cl.send(packet);

                // Se envía el tamaño del archivo
                buffer = String.valueOf(tam).getBytes();
                packet = new DatagramPacket(buffer, buffer.length, dst, ptoS);
                cl.send(packet);

                // Se fragmenta el archivo y se envía
                FileInputStream fis = new FileInputStream(path);
                long enviados = 0;
                int porcentaje = 0;
                int paquetes = 0;
                int i = 0;
                long numPaquetes = tam / 4096;
                System.out.println("El número de paquetes a enviar es : " + numPaquetes);
                
                while (enviados < tam) {
                    i++;
                    int len = fis.available() > 4096 ? 4096 : fis.available();
                    buffer = new byte[len];
                    fis.read(buffer);

                    packet = new DatagramPacket(buffer, buffer.length, dst, ptoS);
                    cl.send(packet);

                    enviados += buffer.length;
                    porcentaje = (int) ((enviados * 100) / tam);
                    System.out.print("\rEnviado el " + porcentaje + "% del archivo");
                    System.out.println("Enviando paquete: " + i);
                }
                System.out.println("\nArchivo enviado.");
                fis.close();
            }
            cl.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
       
    }

    private static void descargarArchivo(String nombre) {
        JOptionPane.showMessageDialog(null, "Elegiste la opción 2: Descargar archivo. Nombre: " + nombre);

        try {
            int pto = 8000; // Puerto de comunicación del servidor
            String dir = "127.0.0.1"; // Dirección IP del servidor (localhost en este caso)
            InetAddress dst = InetAddress.getByName(dir); // Obtenemos la dirección del servidor

            // Enviar la opción "2" al servidor para indicar que es una solicitud de descarga
            String opcion = "2";
            byte[] o = opcion.getBytes();
            DatagramSocket cl = new DatagramSocket();
            cl.setBroadcast(true);  // Activar el broadcast
            DatagramPacket PaqOp = new DatagramPacket(o, o.length, dst, pto);
            cl.send(PaqOp);
            System.out.println("Se ha enviado el datagrama a " + PaqOp.getAddress() + ":" + PaqOp.getPort() + " con el mensaje: " + opcion);

            // Enviar el nombre del archivo que queremos descargar
            byte[] nombreBytes = nombre.getBytes();
            DatagramPacket PaqNombre = new DatagramPacket(nombreBytes, nombreBytes.length, dst, pto);
            cl.send(PaqNombre);
            System.out.println("Se ha enviado el nombre del archivo al servidor: " + nombre);
                
            File f = new File("");
            String ruta = f.getAbsolutePath();
            String carpeta = "Cliente";
            String ruta_archivos = ruta + "\\" + carpeta + "\\";
            System.out.println("Ruta de archivos: " + ruta_archivos);
            File f2 = new File(ruta_archivos);
            f2.mkdirs();

            byte[] buffer = new byte[4096];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                                    
            // Recibe el tamaño del archivo
            cl.receive(packet);
            long tam = Long.parseLong(new String(packet.getData(), 0, packet.getLength()));
            System.out.println("Tamaño del archivo: " + tam + " bytes");
            // Crea un archivo de salida para recibir el archivo enviado
            FileOutputStream fos = new FileOutputStream(ruta_archivos + nombre);
            long recibidos = 0;
            int porcentaje = 0;
            int i = 0;
            // Recibe el contenido del archivo
            while (recibidos < tam) {
                i++;
                cl.receive(packet);
                fos.write(packet.getData(), 0, packet.getLength());
                recibidos += packet.getLength();
                porcentaje = (int) ((recibidos * 100) / tam);
                System.out.print("\rRecibido el " + porcentaje + "% del archivo");
                System.out.println("Paquete recibido: " + i);
            }
            System.out.println("\nArchivo recibido.");
            fos.close();
            cl.close();
                       
        }catch (IOException exc2) {
            exc2.printStackTrace();
        }
        
}


    // Método para crear carpeta
    private static void crearCarpeta(String nombre) {
        JOptionPane.showMessageDialog(null, "Elegiste la opción 3: Crear carpeta. Nombre de la carpeta: " + nombre);
        try {
            int pto = 8000; //Puerto del servidor
            String dir = "127.0.0.1";
            InetAddress dst = InetAddress.getByName(dir);
            String opcion = "3";
            byte[] o = opcion.getBytes();
            byte[] n = nombre.getBytes();
            DatagramSocket cl = new DatagramSocket();
            cl.setBroadcast(true);
            DatagramPacket PaqOp = new DatagramPacket(o, o.length, dst, pto);
            DatagramPacket PaqNom = new DatagramPacket(n, n.length, dst, pto);
            cl.send(PaqOp);
            cl.send(PaqNom);
            System.out.println("Se ha enviado el datagrama con el mensaje: 3 y el nombre de la carpeta " + nombre);
            cl.close();
        } catch (IOException exc3) {
            exc3.printStackTrace();
        }
    }

    // Método para borrar carpeta
    private static void borrarCarpeta(String nombre) {
        JOptionPane.showMessageDialog(null, "Elegiste la opción 4: Borrar carpeta. Nombre de la carpeta: " + nombre);
        try {
            int pto = 8000; //Puerto del servidor
            String dir = "127.0.0.1";
            InetAddress dst = InetAddress.getByName(dir);
            String opcion = "4";
            byte[] o = opcion.getBytes();
            byte[] n = nombre.getBytes();
            DatagramSocket cl = new DatagramSocket();
            cl.setBroadcast(true);
            DatagramPacket PaqOp = new DatagramPacket(o, o.length, dst, pto);
            DatagramPacket PaqNom = new DatagramPacket(n, n.length, dst, pto);
            cl.send(PaqOp);
            cl.send(PaqNom);
            System.out.println("Se ha enviado el datagrama con el mensaje: 4 y el nombre de la carpeta " + nombre);
            cl.close();
        } catch (IOException exc4) {
            exc4.printStackTrace();
        }
    }

    // Método para renombrar archivo
    private static void renombrarArchivo(String nombre) {
        JFrame NuevoN = new JFrame("Renombrar archivo");
        JPanel panelRenombrar = new JPanel();
        panelRenombrar.setLayout(new BoxLayout(panelRenombrar, BoxLayout.Y_AXIS));

        // Creación de campos de texto
        JLabel etiquetaNuevoNombre = new JLabel("Introduce el nuevo nombre para el archivo:");
        JTextField NuevoNombreCaja = new JTextField();
        NuevoNombreCaja.setPreferredSize(new Dimension(600, 30));
        JButton botonRenombrar = new JButton("Renombrar");

        // Acción al hacer click en "Renombrar"
        botonRenombrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String NuevoNombre = NuevoNombreCaja.getText();
                if (NuevoNombre.isEmpty()) {
                    JOptionPane.showMessageDialog(NuevoN, "El nuevo nombre no puede estar vacío.");
                    return;
                }

                try {
                    int pto = 8000; //Puerto del servidor
                    String dir = "127.0.0.1";
                    InetAddress dst = InetAddress.getByName(dir);

                    String opcion = "5";
                    byte[] o = opcion.getBytes();
                    byte[] n = nombre.getBytes();
                    byte[] N = NuevoNombre.getBytes();

                    DatagramSocket cl = new DatagramSocket();
                    cl.setBroadcast(true);

                    DatagramPacket PaqOp = new DatagramPacket(o, o.length, dst, pto);
                    DatagramPacket PaqNom = new DatagramPacket(n, n.length, dst, pto);
                    DatagramPacket PaqNuevNom = new DatagramPacket(N, N.length, dst, pto);

                    cl.send(PaqOp);
                    cl.send(PaqNom);
                    cl.send(PaqNuevNom);

                    System.out.println("Archivo renombrado a: " + NuevoNombre);

                    cl.close();
                } catch (IOException exc5) {
                    exc5.printStackTrace();
                }

                // Cerrar el JFrame después de realizar la acción
                NuevoN.dispose();
            }
        });

        panelRenombrar.add(etiquetaNuevoNombre);
        panelRenombrar.add(NuevoNombreCaja);
        panelRenombrar.add(botonRenombrar);
        NuevoN.add(panelRenombrar);
        NuevoN.setSize(400, 200);
        NuevoN.setVisible(true);
    }
}
