package com.mycompany.practica5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        try {
            //Nombre de la carpeta a crear
            String carpetaArchivos = "./Cliente/"; 
            //Si la carpeta no existe se crea a continuación
            File carpetaOrigen = new File(carpetaArchivos);
            if (!carpetaOrigen.exists()) {
                carpetaOrigen.mkdirs();
            }
            // Crear canal de datagrama y configurarlo como no bloqueante
            DatagramChannel clientChannel = DatagramChannel.open();
            clientChannel.configureBlocking(false);
            Selector selector = Selector.open();
            clientChannel.register(selector, SelectionKey.OP_WRITE);
            //Se crea la dirección del servidor para que se manden los paquetes hacia ese lugar
            InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 1234);
            //Se utiliza Scanner para poder manejar de mejor forma los datos enviados
            Scanner scanner = new Scanner(System.in);

            Map<Integer, String> ventana = new HashMap<>(); // Ventana deslizante
            int base = 0; // Base de la ventana
            int nextSeqNum = 0; // Próximo número de secuencia
            int ventanaTam = 0; // Tamaño de la ventana = 15 paquetes
            int i = 0;

            while (true) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isWritable()) {
                        //Se muestran las opciones a utilizar en el programa
                        //Los nombres de archivos deben ir seguidos de su extensión
                        System.out.println("Seleccione una opción:");
                        System.out.println("1. Creación de carpetas");
                        System.out.println("2. Enviar archivo");
                        System.out.println("3. Descargar archivo");
                        System.out.println("4. Borrado de carpetas");
                        System.out.println("5. Renombrado de archivos");
                        int opcion = scanner.nextInt();
                        scanner.nextLine();
                        //Creación de carpetas
                        if (opcion == 1) {
                            System.out.print("Ingrese el nombre de la carpeta a crear: ");
                            String nombreCarpeta = scanner.nextLine();
                            // Crear mensaje concatenado para el servidor
                            String mensaje = "Creacion:" + nombreCarpeta;

                            // Enviar el mensaje al servidor
                            ByteBuffer buffer = ByteBuffer.wrap(mensaje.getBytes());
                            clientChannel.send(buffer, serverAddress);

                            System.out.println("Solicitud de creación enviada: " + nombreCarpeta);
                            System.out.println("Paquete " + i + " enviado y recibido correctamente");
                            ventanaTam++;
                        } 
                        //Se envian archivos al servidor
                        else if (opcion == 2) {
                            System.out.print("Ingrese el nombre del archivo: ");
                            String nombreArchivo = scanner.nextLine();
                            File archivo = new File(carpetaArchivos + nombreArchivo);
                            //Se muestra un mensaje de error si el archivo no existe
                            if (!archivo.exists()) {
                                System.out.println("Archivo no encontrado en la carpeta raíz.");
                                continue;
                            }
                            // Leer el archivo y enviarlo en paquetes
                            try (FileChannel fileChannel = new FileInputStream(archivo).getChannel()) {
                                ByteBuffer buffer = ByteBuffer.allocate(1024);  // Tamaño del paquete
                                int bytesLeidos;
                                int paqueteNum = 0;  // Número de paquete
                                long fileSize = archivo.length();
                                long bytesEnviados = 0;

                                // Enviar el nombre del archivo y su tamaño al servidor
                                String mensaje = "Subir:" + nombreArchivo + ":" + fileSize;
                                ByteBuffer headerBuffer = ByteBuffer.wrap(mensaje.getBytes());
                                clientChannel.send(headerBuffer, serverAddress);
                                System.out.println("Enviando archivo: " + nombreArchivo);

                                // Enviar paquetes
                                while ((bytesLeidos = fileChannel.read(buffer)) != -1) {
                                    buffer.flip(); // Prepara el buffer para leer

                                    // Enviar paquete
                                    clientChannel.send(buffer, serverAddress);
                                    System.out.println("Enviando paquete " + paqueteNum + " con " + bytesLeidos + " bytes.");
                                    // Calcular el progreso y mostrarlo
                                    bytesEnviados += bytesLeidos;
                                    int porcentaje = (int) ((bytesEnviados * 100) / fileSize);
                                    System.out.println("Progreso: " + porcentaje + "%");

                                    buffer.clear(); // Limpiar el buffer para la siguiente lectura
                                    paqueteNum++;
                                    ventanaTam++;  
                                }
                                System.out.println("Archivo enviado completamente.");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //Descargar archivos
                        else if (opcion == 3) {
                            System.out.print("Ingrese el nombre del archivo a descargar: ");
                            String nombreArchivo = scanner.nextLine();

                            // Crear mensaje para el servidor solicitando la descarga
                            String mensaje = "Descargar:" + nombreArchivo;
                            ByteBuffer mensajeBuffer = ByteBuffer.wrap(mensaje.getBytes());
                            clientChannel.send(mensajeBuffer, serverAddress);
                            System.out.println("Solicitud de descarga enviada para el archivo: " + nombreArchivo);

                            // Preparar el buffer para recibir el archivo
                            try (RandomAccessFile raf = new RandomAccessFile(carpetaArchivos + nombreArchivo, "rw")) {
                                ByteBuffer buffer = ByteBuffer.allocate(1024);  // Buffer para recibir paquetes
                                long fileSize = 0;
                                boolean sizeReceived = false;

                                // Recibir el tamaño del archivo primero
                                while (!sizeReceived) {
                                    buffer.clear();
                                    clientChannel.receive(buffer);
                                    buffer.flip();
                                    String sizeMessage = new String(buffer.array(), 0, buffer.limit());
                                    if (sizeMessage.startsWith("Tam:")) {
                                        fileSize = Long.parseLong(sizeMessage.split(":")[1].trim());
                                        System.out.println("Tamaño del archivo recibido: " + fileSize + " bytes.");
                                        sizeReceived = true;
                                    }
                                }
                                // Validar que el tamaño del archivo sea mayor a 0
                                if (fileSize <= 0) {
                                    System.out.println("El tamaño del archivo es inválido. Operación cancelada.");
                                    return;
                                }
                                // Recibir paquetes del servidor
                                long bytesRecibidosTotal = 0;
                                int paqueteNum = 0;
                                //Mientras no se rebase el tamaño del archivo, se seguiran recibiendo paquetes
                                while (bytesRecibidosTotal < fileSize) {
                                    buffer.clear();
                                    InetSocketAddress clientAddress = (InetSocketAddress) clientChannel.receive(buffer);
                                    //Si aún no hay una dirección siginifica que todavía no se reciben paquetes
                                    if (clientAddress == null) {
                                        continue;  // No hay datos disponibles
                                    }
                                    //Se ajustan los límites del buffer para que reciba la información correctamente
                                    buffer.flip();
                                    byte[] data = new byte[buffer.remaining()];
                                    buffer.get(data);
                                    //Se escriben los datos en el archivo
                                    raf.write(data);
                                    bytesRecibidosTotal += data.length;
                                    // Mostrar el progreso
                                    int porcentaje = (int) ((bytesRecibidosTotal * 100) / fileSize);
                                    System.out.println("Paquete " + paqueteNum + " recibido. Progreso: " + porcentaje + "%");
                                    paqueteNum++;
                                    ventanaTam++;
                                }

                                System.out.println("Archivo descargado completamente.");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //Borrado de carpetas
                         else if (opcion == 4) {
                            System.out.print("Ingrese el nombre de la carpeta a borrar: ");
                            String nombreCarpeta = scanner.nextLine();
                            // Crear mensaje para el servidor
                            String mensaje = "Borrado:" + nombreCarpeta;
                            // Enviar el mensaje al servidor
                            ByteBuffer buffer = ByteBuffer.wrap(mensaje.getBytes());
                            clientChannel.send(buffer, serverAddress);

                            System.out.println("Solicitud de creación enviada: " + nombreCarpeta);
                            System.out.println("Paquete " + i + " enviado y recibido correctamente");

                        } 
                        //Renombrado de archivos 
                         else if (opcion == 5) {
                            System.out.print("Ingrese el nombre del archivo a modificar seguido de la extensión: ");
                            String nombreArchivo = scanner.nextLine();

                            System.out.print("Ingrese el nuevo nombre seguido de la extensión: ");
                            String nombreNuevo = scanner.nextLine();

                            // Crear un único mensaje para el servidor
                            String mensaje = "Modificar:" + nombreArchivo + ":" + nombreNuevo;

                            // Enviar el mensaje al servidor
                            ByteBuffer buffer = ByteBuffer.wrap(mensaje.getBytes());
                            clientChannel.send(buffer, serverAddress);

                            System.out.println("Solicitud de modificación enviada.");
                            System.out.println("Nombre actual: " + nombreArchivo);
                            System.out.println("Nuevo nombre: " + nombreNuevo);
                            System.out.println("Paquete " + i + " enviado y recibido correctamente");

                        }
                    }

                    if (key.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        clientChannel.receive(buffer);
                        buffer.flip();
                        String respuesta = new String(buffer.array(), 0, buffer.limit());
                        System.out.println("Respuesta recibida: " + respuesta);

                        if (respuesta.startsWith("ACK:")) {
                            int ackNum = Integer.parseInt(respuesta.substring(4).trim());
                            if (ackNum >= base) {
                                base = ackNum + 1;
                            }

                            // Retransmitir paquetes si es necesario
                            for (int j = base; j < nextSeqNum; j++) {
                                String paquete = ventana.get(j);
                                if (paquete != null) {
                                    ByteBuffer retransmitBuffer = ByteBuffer.wrap(paquete.getBytes());
                                    clientChannel.send(retransmitBuffer, serverAddress);
                                    System.out.println("Paquete retransmitido: " + j);
                                }
                            }
                        }
                        if (base == nextSeqNum) {
                            System.out.println("Todos los paquetes enviados y confirmados.");
                            ventana.clear();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
