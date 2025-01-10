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

public class Servidor {
    public static void main(String[] args) {
        try {
            String destinoArchivos = "./Servidor/"; // Carpeta destino para archivos
          
            //Si la carpeta llamada Servidor no existe, se crea a continuación
            File carpetaDestino = new File(destinoArchivos);
            if (!carpetaDestino.exists()) {
                carpetaDestino.mkdirs();
            }
            // Creación del canal no bloqueante de datagramas
            DatagramChannel serverChannel = DatagramChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(1234));
            Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_READ);


            System.out.println("Bienvenido a su Drive");
            System.out.println("Servidor iniciado, esperando cliente...");

            while (true) {
                //Se toma como tamaño de paquete 1024
                //Como en este caso el socket no se bloquea al usar UDP y la ventana deslizante, se realiza una verificación de los 
                //paquetes cada 15 paquetes, por lo que el tamaño de ventana es de 15360 
                int i = 0;
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    //Si se esta leyendo, se crea una canal de datagrama y posteriormente se crea el buffer
                    //para guardar la información
                    if (key.isReadable()) {
                        DatagramChannel channel = (DatagramChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        //La dirección que se toma es la que se obtiene al recibir un paquete
                        InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);

                        if (clientAddress == null) continue;
                        //Flip nos ayudará para que los datos se escriban correctamente en el buffer
                        buffer.flip();
                        String mensaje = new String(buffer.array(), 0, buffer.limit());
                        //Si el mensaje recibido contiene la palabra Creacion al principio, se crea la carpeta 
                        if (mensaje.startsWith("Creacion:")) {
                            //Se empieza a extraer el nombre de la carpeta a partir del indice 9
                            String nombreCarpeta = mensaje.substring(9).trim();
                            File nuevaCarpeta = new File(destinoArchivos + nombreCarpeta);
                            System.out.println("Paquete " + i + " recibido correctamente");
                            if (!nuevaCarpeta.exists()) {
                                nuevaCarpeta.mkdirs();
                                System.out.println("Carpeta creada: " + nombreCarpeta);
                                channel.send(ByteBuffer.wrap("Carpeta creada exitosamente.".getBytes()), clientAddress);
                            } else {
                                channel.send(ByteBuffer.wrap("La carpeta ya existe.".getBytes()), clientAddress);
                            }
                        } else if (mensaje.startsWith("Subir:")) {
                            //Se recibe el mensaje y se descompone en partes (Acción:Nombre.extensión:Tamaño)
                            String[] parts = mensaje.split(":", 3);
                            //Pasa el nombre del archivo a fileName
                            String fileName = parts[1].trim();
                            //Coloca el tamaño del archivo en la variable fileSize
                            long fileSize = Long.parseLong(parts[2].trim());
                            //Se coloca la ruta en donde se guardará el archivo, que en este caso se guarda en archivoDestino
                            File archivoDestino = new File(destinoArchivos + fileName);

                            System.out.println("Recibiendo archivo: " + fileName + " con tamaño " + fileSize + " bytes.");

                            try (RandomAccessFile raf = new RandomAccessFile(archivoDestino, "rw")) {
                                //Se crea el buffer para recibir los datos
                                ByteBuffer buffer1 = ByteBuffer.allocate(1024);  
                                int paqueteNum = 0;
                                long bytesRecibidosTotal = 0;

                                // Preparar buffer para recibir los paquetes, mientras los bytes recibidos no excedan el tamaño del archivo
                                while (bytesRecibidosTotal < fileSize) {
                                    // Recibir paquete y limpiar el buffer para cada paquete
                                    buffer1.clear(); 
                                    //Se toma como dirección a la que se obtiene de recibir el paquete
                                    clientAddress = (InetSocketAddress) serverChannel.receive(buffer1);
                                    //Si no hay una dirección significa que todavia no se recibe ningún paquete
                                    if (clientAddress == null) continue;
                                    //Se prepara el buffer para que se puedan leer los datos
                                    buffer1.flip();
                                    //Se obtiene el número de bytes leídos
                                    int bytesLeidos = buffer1.limit();
                                    byte[] data = new byte[bytesLeidos];
                                    buffer1.get(data);  // Copiar datos al arreglo de byte

                                    // Escribir el paquete en el archivo
                                    raf.write(data);
                                    bytesRecibidosTotal += bytesLeidos;

                                    // Mostrar el progreso
                                    int porcentaje = (int) ((bytesRecibidosTotal * 100) / fileSize);
                                    System.out.println("Paquete " + paqueteNum + " recibido. Progreso: " + porcentaje + "%");

                                    // Incrementar el número de paquete
                                    paqueteNum++;
                                }

                                System.out.println("Archivo recibido completamente.");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                         else if (mensaje.startsWith("Descargar:")) {
                            //Se descompone el mensaje en dos partes (Descargar:Nombre.extensión)
                            String[] parts = mensaje.split(":", 2);
                            //Se guarda el nombre del archivo
                            String fileName = parts[1].trim();
                            File archivo = new File(destinoArchivos + fileName);
                            //Si el archivo no existe, no se hace nada y el programa continua
                            if (!archivo.exists()) {
                                System.out.println("Archivo no encontrado: " + fileName);
                                return;  
                            }

                            try (FileChannel fileChannel = new FileInputStream(archivo).getChannel()) {
                                //Se crea el buffer para enviar los datos
                                ByteBuffer buffer2 = ByteBuffer.allocate(1024);
                                long fileSize = archivo.length();
                                long bytesEnviados = 0;
                                int paqueteNum = 0;

                                // Enviar el tamaño del archivo primero para que el cliente sepa que paquete llega
                                String sizeMessage = "Tam:" + fileSize;
                                ByteBuffer sizeBuffer = ByteBuffer.wrap(sizeMessage.getBytes());
                                serverChannel.send(sizeBuffer, clientAddress);
                                System.out.println("Enviando archivo: " + fileName + " de tamaño " + fileSize + " bytes.");

                                // Enviar los paquetes del archivo al cliente
                                while (fileChannel.read(buffer2) != -1) {
                                    buffer2.flip();
                                    serverChannel.send(buffer2, clientAddress);
                                    bytesEnviados += buffer2.limit();

                                    // Mostrar progreso en el servidor
                                    int porcentaje = (int) ((bytesEnviados * 100) / fileSize);
                                    System.out.println("Enviando paquete " + paqueteNum + " con " + buffer2.limit() + " bytes. Progreso: " + porcentaje + "%");
                                    //Se limpia el buffer para que otro paquete pueda ser enviado
                                    buffer2.clear();
                                    paqueteNum++;
                                }

                                System.out.println("Archivo enviado completamente.");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                         else if (mensaje.startsWith("Borrado:")) {
                            //Se recibe el nombre de la carpeta a borrar, el cual se lee desde el indice 8 
                            String nombreCarpeta = mensaje.substring(8).trim();
                            File carpetaABorrar = new File(destinoArchivos + nombreCarpeta);
                            System.out.println("Paquete " + i + " recibido correctamente");
                            //Si la carpeta existe y esta dentro del directorio se llama a la función para borrar todo lo que este en la carpeta
                            if (carpetaABorrar.exists() && carpetaABorrar.isDirectory()) {
                                if (BorrarRecursivamente(carpetaABorrar)) {
                                    System.out.println("Carpeta eliminada: " + nombreCarpeta);
                                    channel.send(ByteBuffer.wrap("Carpeta eliminada exitosamente.".getBytes()), clientAddress);
                                }
                            }
                        } else if (mensaje.startsWith("Modificar:")) {
                            //Se divide el mensaje en tres partes (Modificar:NombreActual:NuevoNombre)
                            String[] partes = mensaje.split(":", 3);
                            System.out.println("Paquete " + i + " recibido correctamente");
                            //Si se reciben las 3 partes correctamente, se asigna el nombre actual y el nombre nuevo del archivo
                            if (partes.length == 3) {
                                String nombreActual = partes[1].trim();
                                String nombreNuevo = partes[2].trim();
                                //Se especifica la ruta del archivo para renombrar el archivo
                                File archivoActual = new File(destinoArchivos + nombreActual);
                                File archivoNuevo = new File(destinoArchivos + nombreNuevo);
                                //Si el archivo existe y esta en el directorio se manda un mensaje, de lo contrario se renombra el archivo
                                if (archivoActual.exists() && archivoActual.isFile()) {
                                    if (archivoNuevo.exists()) {
                                        System.out.println("El archivo con el nuevo nombre ya existe");
                                    } else {
                                        if (archivoActual.renameTo(archivoNuevo)) {
                                            System.out.println("Archivo renombrado de " + nombreActual + " a " + nombreNuevo);
                                        }
                                    }
                                }
                            }
                        } else {
                            System.out.println("Mensaje desconocido recibido: " + mensaje);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   //Función para borrar todos los archivos que se encuentren dentro del directorio
    private static boolean BorrarRecursivamente(File directorio) {
        //Si el directorio no existe, la función regresa un false
        if (!directorio.exists()) {
            return false;
        }

        File[] files = directorio.listFiles();
        //Si existe el directorio, se aplica la recursividad para borrar las carpetas que existan dentro
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!BorrarRecursivamente(file)) {
                        return false;
                    }
                } else {
                    if (!file.delete()) {
                        return false;
                    }
                }
            }
        }

        return directorio.delete();
    }
}
