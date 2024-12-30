import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServidorWEB {
	public static final int PUERTO = 8000;
	ServerSocket ss;
	private final ExecutorService threadPool;

	class  Manejador implements Runnable {
		protected Socket socket;
		protected PrintWriter pw;
		protected BufferedOutputStream bos;
		protected BufferedReader br;
		protected String FileName;
		private int contentLength; // Variable global para longitud del contenido
		private String headerLine;


		public Manejador(Socket _socket) {
			this.socket = _socket;
		}

		@Override
		public void run() {
			try {
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				bos = new BufferedOutputStream(socket.getOutputStream());
				pw = new PrintWriter(new OutputStreamWriter(bos));
				String line = br.readLine();

				if (line == null) {
					pw.print("<html><head><title>Servidor WEB</title></head>");
					pw.print("<body bgcolor=\"#AACCFF\"><br>Linea Vacia</br>");
					pw.print("</body></html>");
					pw.flush();
					socket.close();
					return;
				}

				System.out.println("\nCliente Conectado desde: " + socket.getInetAddress());
				System.out.println("Por el puerto: " + socket.getPort());
				System.out.println("Datos: " + line + "\r\n\r\n");

				//Aqui entra porque la peticion no tiene parametros "?"
				if (line.indexOf("?") == -1 && line.toUpperCase().startsWith("GET")) {
					//System.out.println("siii?");
					getArch(line);
					if (FileName.compareTo("") == 0) {
						SendAA("index.html", "text/html", 200);
					}
					else { //esto respode a cualquier metodo
						File file = new File("recursos/" + FileName);
						if(file.exists()) {
							if(FileName.equals("shaq.jpg")) {
								SendAA("shaq.jpg", "image/jpeg", 200);
							} else {
								String mimeType = getMimeType(FileName);
								//System.out.println("MIME Type: " + mimeType);
								SendAA("recursos/"+FileName, mimeType, 200);
							}

						} else {
							System.out.println("apoco aqui-----------?");
							SendAA("404.html", "text/html", 404);
						}
					}
					System.out.println(FileName);
				} else if((line.toUpperCase().startsWith("TRACE") || line.toUpperCase().startsWith("OPTIONS")) && line.indexOf("?") == -1 ) {
					SendAA("405.html", "text/html", 405);
				}
                //en el caso de que la peticion tenga parametros
                else {
                    // Extraer el metodo HTTP de la línea
                    String method = line.split(" ")[0].toUpperCase();
					System.out.println("Metodo: " + method);
                    switch (method) {

                        case "GET":
							StringTokenizer tokenizer = new StringTokenizer(line, " /?");
							tokenizer.nextToken(); // Salta el método HTTP (GET)
							String resource = tokenizer.nextToken(); // Captura el path segment (aesto)
							System.out.println("Recurso solicitado: " + resource);
							if (line.toUpperCase().contains("?")) {
								System.out.println("Aqui si entra no??");
								// Si tiene parámetros, verificamos el tipo de solicitud GET
								//esta parte esta TRUQUEADA-----------------------------------
								if (!(line.toUpperCase().startsWith("GET /" + resource + "?"))) {
									System.out.println("supongo que no entra aqui");
									// Caso: Petición GET con parámetros en un recurso específico ("/recurso?clave1=valor1")
									StringTokenizer tokens = new StringTokenizer(line, " /?");
									tokens.nextToken(); // Saltar el método HTTP (GET)
									//tokens.nextToken(); // Saltar el recurso
									String url = tokens.nextToken(); // Captura los parámetros después del ?
									System.out.println("URL: " + url);
									obtenerParametros(url); // Manejar parámetros
								} else {
									// Petición GET mal formada (no contiene parámetros en el lugar esperado)
									System.out.println("Petición GET mal formada");
									SendAA("400.html", "text/html", 400); // Bad Request
								}
							} else {
								// Si no tiene parámetros
								System.out.println("No se encontraron parámetros");
								SendAA("404.html", "text/html", 404); // Recurso no encontrado
							}
                            break;

                        case "POST":
							// Leer el encabezado `Content-Length` para determinar la longitud del cuerpo
							String contentType = "";
							while (!(headerLine = br.readLine()).isEmpty()) {
								if (headerLine.startsWith("Content-Length:")) {
									contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
								}
								if (headerLine.startsWith("Content-Type:")) {
									contentType = headerLine.split(":")[1].trim();
								}
							}

							// Leer el cuerpo de la solicitud
							if (contentLength > 0) {
								char[] body = new char[contentLength];
								br.read(body, 0, contentLength); // Leer el cuerpo según la longitud indicada
								String postData = new String(body);
								//System.out.println("Datos recibidos en el cuerpo de la solicitud POST: " + postData);

								// Crear la carpeta 'recursos' si no existe
								File recursosDir = new File("recursos");
								if (!recursosDir.exists()) {
									recursosDir.mkdir();  // Crear la carpeta
								}

								// Generar un nombre único para el archivo (por ejemplo, usando la fecha y hora actual)
								String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
								String fileName = "recursos/datos_post_" + timestamp + ".txt";

								// Guardar los datos en el archivo con el nombre generado
								try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
									writer.write(postData);  // Escribir los datos en el archivo
									writer.flush();
									System.out.println("Datos guardados en el archivo '" + fileName + "'.");
								} catch (IOException e) {
									e.printStackTrace();
								}

								// Parsear los parámetros (como clave1=valor1&clave2=valor2)
								obtenerParametros(postData);
								System.out.println("HTTP/1.0 200");
								System.out.println("Server: Luis Server/1.0");
								System.out.println("Date: " + new Date());
								System.out.println("Content-Type: " + contentType);
								System.out.println("Content-Length: " + contentLength);
								System.out.println("Connection: keep-alive");

								//sendHeadersOnly("200 OK", mimeTypeHead, fileHead.length());
								//SendAA("200.html", "text/html", 200);
							} else {
								SendAA("400.html", "text/html", 400);
								System.out.println("No se recibieron datos en el cuerpo de la solicitud POST");
							}
							break;
						case "HEAD":
							getArch(line);
							//System.out.println("Petición HEAD recibida");

							if (FileName == null || FileName.isEmpty() || FileName.equals("/")) {
								FileName = "index.html"; // Archivo por defecto
							}
							File fileHead = new File("recursos/" + FileName);
							if (fileHead.exists()) {
								String mimeTypeHead = getMimeType(FileName);
								sendHeadersOnly("200 OK", mimeTypeHead, fileHead.length());
							} else {
								sendHeadersOnly("404 Not Found", "text/html", 0);
							}
							break;

                        case "PUT":
							getArch(line);
							//System.out.println("Petición PUT recibida");
							System.out.println(FileName);
							// Suponiendo que el nombre del archivo es enviado como parámetro en la solicitud PUT
							String fileNameToModify = "";  // Variable para almacenar el nombre del archivo recibido

							// Leer el encabezado y los parámetros
							while (!(headerLine = br.readLine()).isEmpty()) {
								fileNameToModify = FileName;
								if (headerLine.startsWith("Content-Length:")) {
									contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
								}
								if (headerLine.startsWith("File-Name:")) {  // Suponemos que el nombre del archivo se envía en el encabezado
									fileNameToModify = FileName/*headerLine.split(":")[1].trim()*/;
									//System.out.println("esta en el segundo if");
								}
							}

							// Leer el cuerpo de la solicitud PUT
							if (contentLength > 0) {
								char[] body = new char[contentLength];
								br.read(body, 0, contentLength); // Leer el cuerpo
								String putData = new String(body);
								System.out.println("Datos recibidos en el cuerpo de la solicitud PUT: " + putData);

								// Comprobar que se ha recibido el nombre del archivo y los datos
								if (fileNameToModify.isEmpty()) {
									System.out.println("No se proporcionó el nombre del archivo.");
									SendAA("400.html", "text/html", 400);
								} else {
									File file = new File("recursos/"+fileNameToModify);
									if (file.exists()) {
										// Sobrescribir el archivo con los nuevos datos
										try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
											writer.write(putData);  // Sobrescribir el archivo con los nuevos datos
											writer.flush();
											System.out.println("Datos modificados en el archivo '" + fileNameToModify + "'.");
										} catch (IOException e) {
											e.printStackTrace();
										}
										SendAA("200.html", "text/html", 200);
									} else {
										SendAA("404.html", "text/html", 404);
										//System.out.println("El archivo especificado no existe: " + fileNameToModify);
									}
								}
							} else {
								System.out.println("No se recibieron datos en el cuerpo de la solicitud PUT");
							}
							break;
                        case "DELETE":
							//System.out.println("Petición DELETE recibida");
							getArch(line);
							//System.out.println("Recurso solicitado: " + FileName);
							eliminarRecurso(FileName);
							break;
                        default:
                            // Si el método no es reconocido o implementado
                            SendAA("501.html", "text/html", 501);
                            break;
                    }
                }

				pw.flush();
				bos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void obtenerParametros(String line) {
			pw.print("HTTP/1.0 200 OK\n");
			pw.print("Content-Type: text/html\n\n");
			pw.print("<html><head><title>Servidor WEB</title></head>");
			//System.out.println("Llego a obtener parametros");
			pw.print("<body bgcolor=\"#AACCFF\">");
			pw.print("<h1>Parametros Obtenidos</h1>");
			pw.print("<h3>Clave: Valor</h3>");
			pw.print("<br>");
			pw.print("<ul>");
			String[] parts = line.split("&");
			for (String part : parts) {
				pw.print("<li>"+part+"</li>");
				System.out.println(part);
			}
			pw.print("</ul>");
			pw.print("</body></html>");
			pw.flush();

		}
		private void sendHeadersOnly(String status, String mimeType, long contentLength) {
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("HTTP/1.0 ").append(status).append("\n");
				sb.append("Server: Luis Server/1.0\n");
				sb.append("Date: ").append(new Date()).append("\n");
				sb.append("Content-Type: ").append(mimeType).append("\n");
				sb.append("Content-Length: ").append(contentLength).append("\n");
				sb.append("Connection: keep-alive\n");
				sb.append("\n");
				System.out.println("HTTP/1.0 " + status);
				System.out.println("Server: Luis Server/1.0");
				System.out.println("Date: " + new Date());
				System.out.println("Content-Type: " + mimeType);
				System.out.println("Content-Length: " + contentLength);
				System.out.println("Connection: keep-alive");

				bos.write(sb.toString().getBytes());
				bos.flush();


				pw.print(sb.toString());
				pw.flush();

			} catch (Exception e) {
				System.err.println("Error al enviar encabezados: " + e.getMessage());
			}
		}

		public String getMimeType(String fileName) {
			if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
				return "text/html";
			} else if (fileName.endsWith(".pdf")) {
				return "application/pdf";
			} else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
				return "image/jpeg";
			} else if (fileName.endsWith(".png")) {
				return "image/png";
			} else if (fileName.endsWith(".gif")) {
				return "image/gif";
			} else if (fileName.endsWith(".css")) {
				return "text/css";
			} else if (fileName.endsWith(".js")) {
				return "application/javascript";
			} else if (fileName.endsWith(".txt")) {
				return "text/plain";
			} else if (fileName.endsWith(".ico")) {
				return "image/x-icon";
			} else {
				return "application/octet-stream"; // Tipo MIME por defecto (descarga de archivos binarios)
			}
		}

		public void eliminarRecurso(String arg){
			//System.out.println("llega a eliminar recursos");
			try {
				//System.out.println(arg);
				String folderPath = "recursos";
				File f = new File(folderPath + "/" + arg);

				if(f.exists()) {
					//System.out.println("entra al priner if?");
					if (f.delete()) {
						System.out.println("------> Archivo " + arg + " eliminado exitosamente\n");

						String deleteOK = "HTTP/1.1 200 OK\n" +
								"Date: " + new Date() + " \n" +
								"Server: Luis Server/1.0 \n" +
								"Content-Type: text/html \n\n"+
								"<html><head><meta charset='UTF-8'><title style=\"text-align: center;\">202 OK Recurso eliminado</title></head>" +
								"<body><h1>202 OK Recurso eliminado exitosamente.</h1>" +
								"<h4 style=\"text-align: center;\">El recurso " + arg + " ha sido eliminado permanentemente del servidor." +
								"Ya no se podra acceder más a él.</h4>" +
								"</body></html>";

						bos.write(deleteOK.getBytes());
						bos.flush();
						System.out.println("Respuesta DELETE: \n" + deleteOK);
					}
					else {
						System.out.println("El archivo " + arg + " no pudo ser borrado\n");

						String error404 = "HTTP/1.1 404 Not Found\n" +
								"Date: " + new Date() + " \n" +
								"Server: EnrikeAbi Server/1.0 \n" +
								"Content-Type: text/html \n\n" +

								"<html><head><meta charset='UTF-8'><title>404 Not found</title></head>" +
								"<body><h1>404 Not found</h1>" +
								"<p>Archivo " + arg + " no encontrado.</p>" +
								"</body></html>";

						bos.write(error404.getBytes());
						bos.flush();
						System.out.println("Respuesta DELETE - ERROR 404: \n" + error404);
					}
				}
				else {
					SendAA("404.html", "text/html", 404);
				}
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}


		public void getArch(String line) {
			int i;
			int f;
			if (line.toUpperCase().startsWith("GET") || line.toUpperCase().startsWith("POST") || line.toUpperCase().startsWith("HEAD") || line.toUpperCase().startsWith("PUT") || line.toUpperCase().startsWith("DELETE") || line.toUpperCase().startsWith("TRACE") || line.toUpperCase().startsWith("CONNECT") || line.toUpperCase().startsWith("PATCH")) {
				i = line.indexOf("/");
				f = line.indexOf(" ", i);
				FileName = line.substring(i + 1, f);
			}
			else {
				FileName = "";
				System.out.println("HTTP/1.0 501 Not Implemented\nhttps://http.cat/status/501");

			}

		}

		public void SendAA(String fileName, String mimeType, int statusCode) {
			try {
				// Abrir el archivo solicitado
				BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(fileName));
				byte[] buf = new byte[65535];
				int b_leidos;

				// Obtener el tamaño del archivo
				File file = new File(fileName);
				int tam_archivo = (int) file.length();

				// Crear encabezado HTTP
				StringBuilder sb = new StringBuilder();
				sb.append("HTTP/1.0 " + statusCode  +"\n");
				sb.append("Server: Luis Server/1.0\n");
				sb.append("Date: " + new Date() + "\n");
				sb.append("Content-Type: " + mimeType + "\n");
				sb.append("Content-Length: " + tam_archivo + "\n");
				sb.append("Connection: keep-alive\n");
				sb.append("\n");

				System.out.println("RESPONSE HEADER");
				System.out.println("HTTP/1.0 " + statusCode);
				System.out.println("Server: Luis Server/1.0");
				System.out.println("Date: " + new Date());
				System.out.println("Content-Type: " + mimeType);
				System.out.println("Content-Length: " + tam_archivo);
				System.out.println("Connection: keep-alive");
				System.out.println();  // Línea en blanco para separar los encabezados del cuerpo

				// Enviar encabezado HTTP
				bos.write(sb.toString().getBytes());
				bos.flush();

				// Leer y enviar el contenido del archivo
				while ((b_leidos = bis2.read(buf, 0, buf.length)) != -1) {
					bos.write(buf, 0, b_leidos);
				}
				bos.flush();

				// Cerrar flujo de entrada
				bis2.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

		/*public void SendA(String arg)
		{
			try{
				int b_leidos=0;
				BufferedInputStream bis2=new BufferedInputStream(new FileInputStream(arg));
				byte[] buf=new byte[1024];
				int tam_bloque=0;
				if(bis2.available()>=1024)
				{
					tam_bloque=1024;
				}
				else
				{
					bis2.available();
				}

				int tam_archivo=bis2.available();

				String sb = "";
				sb = sb+"HTTP/1.0 200 ok\n";
				sb = sb +"Server: Luis Server/1.0 \n";
				sb = sb +"Date: " + new Date()+" \n";
				sb = sb +"Content-Type: text/html \n";
				sb = sb +"Content-Length: "+tam_archivo+" \n";
				sb = sb + "Connection: keep-alive\n";
				sb = sb +"\n";

				System.out.println("RESPONSE HEADER");
				System.out.println("HTTP/1.0 200 OK");
				System.out.println("Server: Luis Server/1.0");
				System.out.println("Date: " + new Date());
				System.out.println("Content-Type: " + "text/html");
				System.out.println("Content-Length: " + tam_archivo);
				System.out.println("Connection: keep-alive");
				System.out.println();  // Línea en blanco para separar los encabezados del cuerpo
				bos.write(sb.getBytes());
				bos.flush();




				while((b_leidos=bis2.read(buf,0,buf.length))!=-1)
				{
					bos.write(buf,0,b_leidos);


				}
				bos.flush();
				bis2.close();

			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
			}

		}*/
	}

	public ServidorWEB() throws Exception {
		System.out.println("Iniciando Servidor.......");
		this.ss = new ServerSocket(PUERTO);
		//creamos una alberca de hilos con 100 hilos
		this.threadPool = Executors.newFixedThreadPool(100);
        System.out.println("Servidor iniciado:---OK");
        System.out.println("Esperando por Cliente....");
        for (;;) {
            Socket accept = ss.accept();
            // Se crea un nuevo hilo con un Runnable
			threadPool.execute(new Manejador(accept));
            //new Thread(new Manejador(accept)).start();
        }
    }

    public static void main(String[] args) throws Exception {
        ServidorWEB sWEB = new ServidorWEB();
    }
}
