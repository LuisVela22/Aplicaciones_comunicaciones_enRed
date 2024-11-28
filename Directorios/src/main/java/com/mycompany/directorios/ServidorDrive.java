package com.mycompany.directorios;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import javax.swing.*;

public class ServidorDrive {

    // Creación de carpetas
    public static void CrearCarpeta(String Ruta, String carpeta) {
        File CrearCarpeta = new File(Ruta, carpeta);
        if (CrearCarpeta.mkdir()) {
            System.out.println("Carpeta creada en: " + CrearCarpeta.getPath());
        } else {
            System.out.println("No se pudo crear la carpeta. Es posible que ya exista o que la ruta no sea válida.");
        }
    }

    // Método para borrar una carpeta
    public static void BorrarCarpeta(String Ruta, String carpeta) {
        File carpetaABorrar = new File(Ruta, carpeta);
        if (carpetaABorrar.exists() && carpetaABorrar.isDirectory()) {
            borrarContenido(carpetaABorrar);
            if (carpetaABorrar.delete()) {
                System.out.println("Carpeta borrada: " + carpetaABorrar.getPath());
            } else {
                System.out.println("No se pudo borrar la carpeta.");
            }
        } else {
            System.out.println("La carpeta no existe o no es una carpeta válida.");
        }
    }

    // Método recursivo para borrar el contenido de la carpeta antes de borrarla
    private static void borrarContenido(File carpeta) {
        File[] listaArchivos = carpeta.listFiles();
        if (listaArchivos != null) {
            for (File archivo : listaArchivos) {
                if (archivo.isDirectory()) {
                    borrarContenido(archivo);
                }
                archivo.delete();
            }
        }
    }

    public static void main(String[] args) {
        // Ruta de la carpeta donde están los archivos
        String Ruta = "C:\\Users\\Luis Velasco\\OneDrive\\Documentos\\5to s escom\\redes2\\practicas\\Directorios\\Servidor";

        try {
            int pto = 8000;
            int ptoC = 1234;
            String dir = "127.0.0.1";
            InetAddress dst = InetAddress.getByName(dir);
            DatagramSocket s = new DatagramSocket(pto);
            s.setReuseAddress(true);
            s.setBroadcast(true);
            System.out.println("Servidor iniciado... esperando datagramas...");

            for (;;) {
                byte[] o = new byte[65535];
                DatagramPacket PaqOp = new DatagramPacket(o, o.length);
                s.receive(PaqOp);
                String msj = new String(PaqOp.getData(), 0, PaqOp.getLength());
                System.out.println("Se ha recibido datagrama con la opción: " + msj);

                switch (msj) {
                    case "1":
                        System.out.println("Se seleccionó la opción 1: Subir archivo.");
                        //Se recibe el archivo enviado por el cliente
                        recibirArchivo(s, pto, ptoC);
                        break;
                    case "2":
                        System.out.println("Se seleccionó la opción 2: Descargar archivo.");
                        // Recibir el nombre del archivo del cliente
                        String nombreArchivo = recibirNombreArchivo(s);
                        System.out.println(nombreArchivo);

                        // Se obtiene la dirección y el puerto del ultimo datagrama enviado
                        InetAddress clienteDireccion = PaqOp.getAddress();  // Dirección IP del cliente 
                        int clientePuerto = PaqOp.getPort();  // Puerto donde el cliente está escuchando
                        // Enviar el archivo al cliente
                        enviarArchivo(s, nombreArchivo, clienteDireccion, clientePuerto);
                        break;
                    case "3":
                        System.out.println("Se seleccionó la opción 3: Crear carpeta.");
                        //Se guarda el nombre de la carpeta a crear
                        String carpetaCrear = recibirNombreCarpeta(s);
                        //Se llama a la función crear carpeta
                        CrearCarpeta(Ruta, carpetaCrear);
                        break;
                    case "4":
                        System.out.println("Se seleccionó la opción 4: Borrar carpeta.");
                        //Se guarda el nombre de la carpeta a borrar
                        String carpetaBorrar = recibirNombreCarpeta(s);
                        BorrarCarpeta(Ruta, carpetaBorrar);
                        break;
                    case "5":
                        System.out.println("Se seleccionó la opción 5: Renombrar archivo.");
                       //Se llama a la función renombrar archivo
                        renombrarArchivo(s, Ruta);
                        break;
                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
}

    private static void recibirArchivo(DatagramSocket s, int pto, int ptoC) {
      try {
          
            //Se crea una clase archivo para poder manipular el archivo recibido
            File f = new File("");
            String ruta = f.getAbsolutePath();
            String carpeta = "Servidor";
            String ruta_archivos = ruta + "\\" + carpeta + "\\";
            //Se especifica la ruta donde se guardará el archivo
            System.out.println("Ruta de archivos: " + ruta_archivos);
            File f2 = new File(ruta_archivos);
            f2.mkdirs();

            //Se crea la ventana de 4096 bytes para cada datagrama
            byte[] buffer = new byte[4096];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            
            // Se recibe el nombre del archivo
            s.receive(packet);
            String nombre = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Nombre de archivo recibido: " + nombre);

            // Se recibe el nombre del tamaño
            s.receive(packet);
            long tam = Long.parseLong(new String(packet.getData(), 0, packet.getLength()));
            System.out.println("Tamaño del archivo: " + tam + " bytes");

            // Se crea un archivo de salida para guardar los datagramas
            FileOutputStream fos = new FileOutputStream(ruta_archivos + nombre);
            long recibidos = 0;
            int porcentaje = 0;
            int i = 0;
            // Se reciben los contenidos
            
            //Mientras los bytes recibidos sean menores que el tamaño del archivo, se seguiran recibiendo paqutes
            while (recibidos < tam) {
                i++;
                s.receive(packet);
                fos.write(packet.getData(), 0, packet.getLength());
                recibidos += packet.getLength();
                porcentaje = (int) ((recibidos * 100) / tam);
                System.out.print("\rRecibido el " + porcentaje + "% del archivo");
                System.out.println("Paquete recibido: " + i);
            }
            System.out.println("\nArchivo recibido.");
            fos.close();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
      
  }


    // Método para recibir el nombre del archivo del cliente
    private static String recibirNombreArchivo(DatagramSocket s) {
        try {
            byte[] buffer = new byte[65535];  // Buffer para recibir datos
            DatagramPacket Nombre = new DatagramPacket(buffer, buffer.length);
            s.receive(Nombre);  // Recibir el nombre del archivo del cliente
            return new String(Nombre.getData(), 0, Nombre.getLength());  // Convertir los datos recibidos a String (nombre del archivo)
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // Método para enviar el archivo al cliente
    private static void enviarArchivo(DatagramSocket s, String nombreArchivo, InetAddress clienteDireccion, int clientePuerto) {
        try {
            File archivo = new File("C:\\Users\\Luis Velasco\\OneDrive\\Documentos\\5to s escom\\redes2\\practicas\\Directorios\\Servidor\\" + nombreArchivo);
            if (!archivo.exists()) {
                System.out.println("El archivo no existe: " + nombreArchivo);
                return;
            }

            // Nombre y tamaño del archivo
            String nombreArch = archivo.getName();
            long tam = archivo.length();
            System.out.println("Preparándose para enviar archivo " + nombreArch + " de " + tam + " bytes");

            // Nombre del archivo
            byte[] buffer = nombreArch.getBytes();

            // Enviar el tamaño del archivo
            buffer = String.valueOf(tam).getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clienteDireccion, clientePuerto);
            s.send(packet);
            System.out.println("Se ha enviado el tamaño del archivo que es de: " + tam);


            // Fragmentar el archivo y enviar en paquetes de 4096 bytes
            FileInputStream fis = new FileInputStream(archivo);
            long enviados = 0;
            int porcentaje = 0;
            int i = 0;
            int len;

            while (enviados < tam) {
                len = fis.available() > 4096 ? 4096 : fis.available();
                buffer = new byte[len];
                fis.read(buffer);

                packet = new DatagramPacket(buffer, buffer.length, clienteDireccion, clientePuerto);
                s.send(packet);

                enviados += buffer.length;
                porcentaje = (int) ((enviados * 100) / tam);
                System.out.print("\rEnviado el " + porcentaje + "% del archivo");
                System.out.println(" Enviando paquete: " + (++i));
            }
            System.out.println("\nArchivo enviado completamente.");
            fis.close();
           

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private static String recibirNombreCarpeta(DatagramSocket s) {
        try {
            byte[] buffer = new byte[65535];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            s.receive(packet);
            return new String(packet.getData(), 0, packet.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void renombrarArchivo(DatagramSocket s, String Ruta) {
     try {
         // Recibir antiguo nombre del archivo
         String antiguoNombre = recibirNombreArchivo(s);

         // Recibir nuevo nombre del archivo
         String nuevoNombre = recibirNombreArchivo(s);

         File antiguoArchivo = new File(Ruta + "\\" + antiguoNombre);
         File nuevoArchivo = new File(Ruta + "\\" + nuevoNombre);

         if (antiguoArchivo.exists()) {
             boolean success = antiguoArchivo.renameTo(nuevoArchivo);
             if (success) {
                 System.out.println("Archivo renombrado con éxito de " + antiguoNombre + " a " + nuevoNombre);
             } else {
                 System.out.println("No se pudo renombrar el archivo.");
             }
         } else {
             System.out.println("El archivo no existe.");
         }
      }catch (Exception e) {
         e.printStackTrace();
         }
    }

}
